package tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import config.StrykerConfig;

import ar.edu.taco.TacoMain;
import ar.edu.taco.engine.JUnitStage;
import ar.edu.taco.engine.StrykerStage;
import repairer.FixCandidate;

/**
 * This class is used to access RAC, the main responsabilities of this API are:
 * 
 * <li> Given a java source file and a JUnit test, run the test on the java source file </li>
 * <li> Given a java source file and a list of JUnit tests, run the tests on the java source file </li>
 * <li> Given a java source file and a counterexample build a JUnit test </li>
 * 
 * @author Nazareno Matías Aguirre
 * @author Simón Emmanuel Gutiérrez Brida
 * 
 * @see FixCandidate
 * @see CounterExample
 * @see Path
 *
 * @version 2.0
 */
public class RacAPI {
	
	public static boolean verbose = false;
	
	private Thread runningThread = null;

	private static RacAPI instance = null;
	
	public static RacAPI getInstance() {
		if (instance == null) instance = new RacAPI();
		return instance;
	}
	
	private RacAPI() {}
	
	/**
	 * This method takes a {@code FixCandidate} and a {@code CounterExample} and build a JUnit test.
	 *
	 * @param candidate	:	the java source file for which to build the test		:	{@code FixCandidate}
	 * @param ce		:	the counter example that will be used to build the test	:	{@code CounterExample}
	 * 
	 * @return a path to the JUnit test built : {@code Path}
	 */
	public Path buildJUnit(FixCandidate candidate, CounterExample ce) {
		if (!ce.counterExampleExist()) {
			throw new IllegalArgumentException("RacAPI#buildJUnit(FixCandidate, CounterExample): trying to build a junit test with no counter example");
		}
		
    	String FILE_SEP = StrykerConfig.getInstance().getFileSeparator();
    	
    	String classToCheck = candidate.getProgram().getClassName();
      	String methodToCheck = candidate.getMethodToFix() + "_0" ;

	    JUnitStage jUnitStage = new JUnitStage(ce.getRecoveredInformation());
	    jUnitStage.execute();

	    String junitFile = jUnitStage.getJunitFileName();

	    String currentJunit = null;

	    String tempFilename = junitFile.substring(0, junitFile.lastIndexOf(FILE_SEP)+1); 
	    String editedTestFolderPath = tempFilename.replaceAll("generated", "output");
	    File editedTestFolderFile = new File(editedTestFolderPath);
	    editedTestFolderFile.mkdirs();
	    String fileClasspath = tempFilename.substring(
	    							0,
	    							tempFilename.lastIndexOf(
	    									"ar.edu.generated.junit".replaceAll("\\.", FILE_SEP)
	    							)
	    						);
	    fileClasspath = fileClasspath.replaceFirst("generated", "output");
	    String packageToWrite = "ar.edu.output.junit";
	    currentJunit = TacoMain.editTestFileToCompile(junitFile, classToCheck, packageToWrite, methodToCheck);

        if (!JavaCompilerAPI.getInstance().compile(currentJunit, new String[]{StrykerConfig.getInstance().getCompilingSandbox(), StrykerConfig.getInstance().getJunitPath(), StrykerConfig.getInstance().getHamcrestPath()})) {
        	if (RacAPI.verbose) System.err.println("Error while compiling " + currentJunit);
        	return null;
        }
	       
        if (RacAPI.verbose) System.out.println("junit counterexample compilation succeded");

        return new File(currentJunit).toPath();
	}
	
	/**
	 * This method runs a JUnit test for a given {@code FixCandidate}
	 * 
	 * @param candidate	:	the java source code for which the JUnit test will be run	:	{@code FixCandidate}
	 * @param junitTest	:	the path leading to the JUnit test to run					:	{@code Path}
	 * 
	 * @return {@code true} or {@code false} depending if the test passes or not	:	{@code boolean}
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public boolean runJUnit(FixCandidate candidate, Path junitTest) throws InstantiationException, IllegalAccessException {
		if (candidate==null) throw new IllegalArgumentException("checking if tests passes on null candidate");
		if (junitTest==null) throw new IllegalArgumentException("checking if test passes on null test");
		
		String newFileClasspath = candidate.getProgram().getAbsolutePath() + ":" + System.getProperty("user.dir")+ ":" + "lib/stryker/jml4c.jar";
		String qualifiedName = candidate.getProgram().getClassName();
		String methodName = candidate.getMethodToFix();
		String junitPackage = "ar.edu.output.junit";
		
        String testFolder = junitTest.toString();
        String className = junitTest.toString().replace(".java", "");
        int packageIdx = testFolder.indexOf(junitPackage.replaceAll("\\.", StrykerConfig.getInstance().getFileSeparator()));
        if (packageIdx > 0) {
        	testFolder = testFolder.substring(0, packageIdx);
        	className = className.substring(testFolder.length());
        	String[] junitTestClassPath = new String[]{testFolder};
        	JavaCompilerAPI.getInstance().updateReloaderClassPath(junitTestClassPath);
        }
        
        Class<?> junitTestClass = JavaCompilerAPI.getInstance().reloadClass(className.replaceAll(StrykerConfig.getInstance().getFileSeparator(), "."));
        
        Method[] methods = junitTestClass.getDeclaredMethods();
        Method methodToRun = null;
        for (Method m : methods) {
        	if(m.isAnnotationPresent(Test.class)) {
        		methodToRun = m;
        		break;
        	}
        }
        methodToRun.setAccessible(true);
        final Method methodToRunInCallable = methodToRun;
        final Object oToRun = junitTestClass.newInstance();
        final Object[] inputToInvoke = new Object[]{newFileClasspath, qualifiedName, methodName};
        Callable<Boolean> task = new Callable<Boolean>() {
            public Boolean call() throws InvocationTargetException {
                Boolean result = false;
                try {
                    runningThread = Thread.currentThread();
                    long timeprev = System.currentTimeMillis();
                    methodToRunInCallable.invoke(oToRun, inputToInvoke);
                    long timepost = System.currentTimeMillis();
                    result = true;
                    if (RacAPI.verbose) System.out.println("time taken: "+(timepost - timeprev));
                } catch (IllegalAccessException e) {
                	if (RacAPI.verbose) System.out.println("Entered IllegalAccessException");
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                	if (RacAPI.verbose) System.out.println("Entered IllegalArgumentException");
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    //                                                    e.printStackTrace();
                	if (RacAPI.verbose) System.out.println("Entered InvocationTargetException");
                	if (RacAPI.verbose) System.out.println("QUIT BECAUSE OF JML RAC");
                    String retValue = null;
                    StringWriter sw = null;
                    PrintWriter pw = null;
                    try {
                        sw = new StringWriter();
                        pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        retValue = sw.toString();
                        //                                                        System.out.println(retValue);
                        //                                                        System.out.println("------------------------------------------------------------------------------------------------");
                    } finally {
                        try {
                            if(pw != null)  pw.close();
                            if(sw != null)  sw.close();
                        } catch (IOException ignore) {}
                    }
                    if (retValue.contains("JMLInternalNormalPostconditionError")) {
                    	if (RacAPI.verbose) System.out.println("Fallo por la postcondicion!!");
                        result = false;
                    } else if (retValue.contains("JMLExitExceptionalPostconditionError")) { 
                        result = false;
                    } else if (retValue.contains("NullPointerException")) {
                    	if (RacAPI.verbose) System.out.println("NULL POINTER EXCEPTION EN RAC!!!!!!!!!!!!");
                        result = false;
                    } else if (retValue.contains("ThreadDeath")) {
                    	if (RacAPI.verbose) System.out.println("THREAD DEATH EN RAC!!!!!!!!!!!!!!!!");
                        result = true;
                    } else {
                    	if (RacAPI.verbose) System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" +
                                "\nFAILED METHODDDD FOR NO REASON!!!!!!!!!!!!!!!!!!!!" +
                                "\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    	if (RacAPI.verbose) e.printStackTrace();
                        result = false;
                    }
                } catch (Throwable e) {
                	if (RacAPI.verbose) System.out.println("Entered throwable");
                	if (RacAPI.verbose) System.out.println("THROWABLEEE!!!!!!!!!!!!!!!!!!!!!!");
                	if (RacAPI.verbose) e.printStackTrace();
                    return false;
                }
                return result;
            }
        };
        long nanoPrev = System.currentTimeMillis();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(task);
        boolean result = false;
        try {
            result = future.get(300, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            //runningThread.interrupt();
        	runningThread.interrupt();
            executor.shutdownNow();
            executor = Executors.newSingleThreadExecutor();
            result = true;
            // handle the timeout
        } catch (InterruptedException e) {
        	if (RacAPI.verbose) System.out.println("Interrupted");
        	result = true;
            // handle the interrupts
        } catch (ExecutionException e) {
            // handle other exceptions
        	if (RacAPI.verbose) System.out.println("Excecution Exception");
        	result = true;
        } catch (Throwable e) {
        	if (RacAPI.verbose) System.out.println("Exception");
        	result = true;
            // handle other exceptions
        } finally {
            future.cancel(true); // may or may not desire this	
        }
        StrykerStage.racMillis += (System.currentTimeMillis() - nanoPrev);
        if (RacAPI.verbose) System.out.println("test ran");
        return result;
	}
	
	/**
	 * This method runs a list of JUnit tests for a given {@code FixCandidate}
	 * 
	 * @param candidate		:	the java source code for which the JUnit test will be run	:	{@code FixCandidate}	
	 * @param junitTests	:	the list of paths leading to the JUnit tests to run			:	{@code List<Path>}
	 * 
	 * @return a boolean array representing the result of running each JUnit test	:	{@code boolean[]}
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public boolean[] runJUnits(FixCandidate candidate, List<Path> junitTests, boolean stopAtFirstFail) throws InstantiationException, IllegalAccessException {
		boolean[] results = new boolean[junitTests.size()];
		Arrays.fill(results, true);
		//System.out.print("About to run " + junitTests.size() + " tests...");
		int t = 0;
		for (t = 0; t < junitTests.size(); t++) {
			results[t] = this.runJUnit(candidate, junitTests.get(t));
			if (!results[t] && stopAtFirstFail) {
				break;
			}
		}
		//System.out.println("...runned " + (t<junitTests.size()?(t+1):t) + " tests");
		//System.out.println(Arrays.toString(results));
		return results;
	}
	
	/**
	 * This method runs a list of JUnit tests for a given {@code FixCandidate}
	 * 
	 * @param candidate		:	the java source code for which the JUnit test will be run	:	{@code FixCandidate}	
	 * @param junitTests	:	the list of paths leading to the JUnit tests to run			:	{@code List<Path>}
	 * 
	 * @return a boolean array representing the result of running each JUnit test	:	{@code boolean[]}
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public boolean[] runJUnits(FixCandidate candidate, List<Path> junitTests) throws InstantiationException, IllegalAccessException {
		return this.runJUnits(candidate, junitTests, true);
	}
	
}

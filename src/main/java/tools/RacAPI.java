package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import config.StrykerConfig;

import ar.edu.taco.engine.StrykerStage;
import ar.edu.taco.stryker.api.impl.DarwinistController;
import ar.edu.taco.stryker.api.impl.input.DarwinistInput;
import ar.edu.taco.stryker.api.impl.input.OpenJMLInput;
import repairer.FixCandidate;

/**
 * This class is used to access RAC, the main responsabilities of this API are:
 * 
 * <li> Given a java source file and a JUnit test, run the test on the java source file </li>
 * <li> Given a java source file and a list of JUnit tests, run the tests on the java source file </li>
 * <li> Given a java source file and a counterexample build a JUnit test </li>
 * 
 * TODO: add author
 * 
 * @see FixCandidate
 * @see CounterExample
 * @see Path
 *
 * @version 0.1u
 */
public class RacAPI {
	
	private Thread runningThread = null;

	private static RacAPI instance = null;
	
	public static RacAPI getInstance() {
		if (instance == null) instance = new RacAPI();
		return instance;
	}
	
	private RacAPI() { }
	
	/**
	 * This method takes a {@code FixCandidate} and a {@code CounterExample} and build a JUnit test.
	 *
	 * @param candidate	:	the java source file for which to build the test		:	{@code FixCandidate}
	 * @param ce		:	the counter example that will be used to build the test	:	{@code CounterExample}
	 * 
	 * @return a path to the JUnit test built : {@code Path}
	 */
	private Path buildJUnit(FixCandidate candidate, CounterExample ce) {
		//TODO: implement this method
		throw new UnsupportedOperationException ("RacAPI#buildJUnit(FixCandidate, CounterExample) : not yet implemented");
	}
	
	/**
	 * This method runs a JUnit test for a given {@code FixCandidate}
	 * 
	 * @param candidate	:	the java source code for which the JUnit test will be run	:	{@code FixCandidate}
	 * @param junitTest	:	the path leading to the JUnit test to run					:	{@code Path}
	 * 
	 * @return {@code true} or {@code false} depending if the test passes or not	:	{@code boolean}
	 */
	private boolean runJUnit(FixCandidate candidate, Path junitTest) {
		//TODO: implement this method
		throw new UnsupportedOperationException ("RacAPI#runJUnit(FixCandidate, Path) : not yet implemented");
	}
	
	/**
	 * This method runs a list of JUnit tests for a given {@code FixCandidate}
	 * 
	 * @param candidate		:	the java source code for which the JUnit test will be run	:	{@code FixCandidate}	
	 * @param junitTests	:	the list of paths leading to the JUnit tests to run			:	{@code List<Path>}
	 * 
	 * @return a boolean array representing the result of running each JUnit test	:	{@code boolean[]}
	 */
	private boolean[] runJUnits(FixCandidate candidate, List<Path> junitTests) {
		//TODO: implement this method
		throw new UnsupportedOperationException ("RacAPI#runJUnits(FixCandidate, List<Path>) : not yet implemented");
	}

	/**
	 * Checks whether fix candidate "passes" on a list of collected inputs, i.e., if the candidate, ran using RAC for each of the 
	 * collected inputs, it does not violate the contracts.
	 * @param candidate is the fix candidate to evaluate.
	 * @param collectedInputs is the list of collected inputs to check the fix candidate with.
	 * @return true iff fix candidate does not violate any contract on any of the collected inputs.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public boolean testsPassed(FixCandidate candidate, List<CounterExample> collectedInputs) throws InstantiationException, IllegalAccessException {
		if (candidate==null) throw new IllegalArgumentException("checking if collected tests pass on null candidate");
		if (collectedInputs==null) throw new IllegalArgumentException("checking if collected tests pass on null list of inputs");
		boolean testPassed = true;
		int i = 0;
		while (i<collectedInputs.size() && testPassed) {
			CounterExample curr = collectedInputs.get(i);
			// check if curr passes as a test
			testPassed = testPassed(candidate, curr);
			i++;
		}
		return testPassed;
	}

	/**
	 * Checks whether fix candidate "passes" on a given collected input, i.e., if the candidate, ran using RAC for the 
	 * collected input, it does not violate the contracts.
	 * @param candidate is the fix candidate to evaluate.
	 * @param counterexample is the input to check the fix candidate with.
	 * @return true iff fix candidate does not violate any contract on the provided input.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * FIXME this method is "borrowed" from comitaco. It may not be the best way of running a test using RAC.
	 */
	private boolean testPassed(FixCandidate candidate, CounterExample counterexample) throws InstantiationException, IllegalAccessException {
		if (candidate==null) throw new IllegalArgumentException("checking if tests passes on null candidate");
		if (counterexample==null) throw new IllegalArgumentException("checking if test passes on null test");
		
		String newFileClasspath = candidate.getProgram().getAbsolutePath() + ":" + System.getProperty("user.dir")+ ":" + "lib/stryker/jml4c.jar";
		String qualifiedName = candidate.getProgram().getClassName();//.getClassNameAsPath();
		String methodName = candidate.getMethodToFix();
		
		Boolean threadTimeout = false;
		
        Class<?> junitInputClass = counterexample.getJunitInput();
        String testFolder = counterexample.getJunitFile();
        int classNameIdx = testFolder.indexOf(junitInputClass.getName().replaceAll("\\.", StrykerConfig.getInstance().getFileSeparator()));
        if (classNameIdx > 0) {
        	testFolder = testFolder.substring(0, classNameIdx);
        	String[] junitTestClassPath = new String[]{testFolder};
        	JavaCompilerAPI.getInstance().updateReloaderClassPath(junitTestClassPath);
        }
        JavaCompilerAPI.getInstance().reloadClass(qualifiedName);
        Class<?> junitTestClass = JavaCompilerAPI.getInstance().reloadClass(junitInputClass.getName());
        
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
                    System.out.println("time taken: "+(timepost - timeprev));
                } catch (IllegalAccessException e) {
                	System.out.println("Entered IllegalAccessException");
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                	System.out.println("Entered IllegalArgumentException");
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    //                                                    e.printStackTrace();
                	System.out.println("Entered InvocationTargetException");
                	System.out.println("QUIT BECAUSE OF JML RAC");
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
                        System.out.println("Fallo por la postcondicion!!");
                        result = false;
                    } else if (retValue.contains("JMLExitExceptionalPostconditionError")) { 
                        result = null;
                    } else if (retValue.contains("NullPointerException")) {
                        System.out.println("NULL POINTER EXCEPTION EN RAC!!!!!!!!!!!!");
                        result = null;
                    } else if (retValue.contains("ThreadDeath")) {
                        System.out.println("THREAD DEATH EN RAC!!!!!!!!!!!!!!!!");
                        result = null;
//                        System.out.println("THREAD DEATH EN RAC!!!!!!!!!!!!!!!!");
                        result = null;
                    } else {
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" +
                                "\nFAILED METHODDDD FOR NO REASON!!!!!!!!!!!!!!!!!!!!" +
                                "\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        e.printStackTrace();
                        result = null;
                    }
                } catch (Throwable e) {
                	System.out.println("Entered throwable");
                    System.out.println("THROWABLEEE!!!!!!!!!!!!!!!!!!!!!!");
                    e.printStackTrace();
                    return false;
                }
                return result;
            }
        };
        threadTimeout = false;
        long nanoPrev = System.currentTimeMillis();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(task);
        boolean result = true;
        try {
            result = future.get(300, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            //                                            System.out.println("TIMEOUT POR FUERA DE RAC!!!!!!!!!!!!!!!!!!");
            result = false;
            threadTimeout = true;
            runningThread.stop();
            executor.shutdownNow();
            executor = Executors.newSingleThreadExecutor();
            // handle the timeout
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            // handle the interrupts
        } catch (ExecutionException e) {
            // handle other exceptions
        	System.out.println("Excecution Exception");
        } catch (Throwable e) {
        	System.out.println("Exception");
            // handle other exceptions
        } finally {
            future.cancel(true); // may or may not desire this	
        }
        StrykerStage.racMillis += (System.currentTimeMillis() - nanoPrev);
        System.out.println("test ran");
//        System.out.println("result: " + result);
//        System.out.println("timeout: " + threadTimeout);
        return (result==true); // apparently result can be null, that's why I'm writing it like this
    
	}
	
}

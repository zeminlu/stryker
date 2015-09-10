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

import config.StrykerConfig;
//import ar.edu.taco.TacoMain;
//import ar.edu.taco.engine.JUnitStage;
import ar.edu.taco.engine.StrykerStage;
import repairer.FixCandidate;
import tools.TestRunner.TestResult;

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
	
	public static boolean verbose = true;
	
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
		
//    	String FILE_SEP = StrykerConfig.getInstance().getFileSeparator();
//    	
//    	String classToCheck = candidate.getProgram().getClassName();
//      	String methodToCheck = candidate.getMethodToFix() + "_0" ;
//
//	    JUnitStage jUnitStage = new JUnitStage(ce.getRecoveredInformation());
//	    jUnitStage.execute();
//
//	    String junitFile = jUnitStage.getJunitFileName();
//
//	    String currentJunit = null;
//
//	    String tempFilename = junitFile.substring(0, junitFile.lastIndexOf(FILE_SEP)+1); 
//	    String editedTestFolderPath = tempFilename.replaceAll("generated", "output");
//	    File editedTestFolderFile = new File(editedTestFolderPath);
//	    editedTestFolderFile.mkdirs();
//	    String fileClasspath = tempFilename.substring(
//	    							0,
//	    							tempFilename.lastIndexOf(
//	    									"ar.edu.generated.junit".replaceAll("\\.", FILE_SEP)
//	    							)
//	    						);
//	    fileClasspath = fileClasspath.replaceFirst("generated", "output");
//	    String packageToWrite = "ar.edu.output.junit";
//	    currentJunit = TacoMain.editTestFileToCompile(junitFile, classToCheck, packageToWrite, methodToCheck);
		
		TestBuilder builder = new TestBuilder(ce.getRecoveredInformation());
		String testPath = null;
		try {
			testPath = builder.createUnitTest();
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException | SecurityException | IOException e) {
			if (RacAPI.verbose) {
				System.err.println("Error while generating test");
				e.printStackTrace();
			}
			return null;
		}

        if (!JavaCompilerAPI.getInstance().compile(testPath, new String[]{StrykerConfig.getInstance().getCompilingSandbox()})) {
        	if (RacAPI.verbose) System.err.println("Error while compiling " + testPath);
        	return null;
        }
	       
        if (RacAPI.verbose) System.out.println("junit counterexample compilation succeded");

        //return new File(currentJunit).toPath();
        return new File(testPath).toPath();
	}
	
	/**
	 * This method runs a JUnit test for a given {@code FixCandidate}
	 * 
	 * @param candidate	:	the java source code for which the JUnit test will be run	:	{@code FixCandidate}
	 * @param testPath	:	the path leading to the JUnit test to run					:	{@code Path}
	 * 
	 * @return {@code true} or {@code false} depending if the test passes or not	:	{@code boolean}
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public boolean runJUnit(FixCandidate candidate, Path testPath) throws InstantiationException, IllegalAccessException {
		if (candidate==null) throw new IllegalArgumentException("checking if tests passes on null candidate");
		if (testPath==null) throw new IllegalArgumentException("checking if test passes on null test");
        
		String testClassName = null;
		int outputDirIdx = testPath.toString().indexOf(StrykerConfig.getInstance().getTestsOutputDir());
		if (outputDirIdx > 0) {
			testClassName = testPath.toString().substring(outputDirIdx+StrykerConfig.getInstance().getTestsOutputDir().length(), testPath.toString().length());
		} else if (outputDirIdx == 0){
			testClassName = testPath.toString().substring(StrykerConfig.getInstance().getTestsOutputDir().length(), testPath.toString().length());
		} else {
			testClassName = testPath.toString();
		}
		testClassName = testClassName.replace(".java", "");
		
		testClassName = testClassName.replaceAll(StrykerConfig.getInstance().getFileSeparator(), ".");
		
//	    if (!JavaCompilerAPI.getInstance().compile(testPath.toString(), new String[]{StrykerConfig.getInstance().getCompilingSandbox()})) {
//	    	if (RacAPI.verbose) System.err.println("test failed to compile: " + testPath);
//	    	return false;
//	    }
		
        final Class<?> candidateClass = JavaCompilerAPI.getInstance().reloadClassFrom(candidate.getProgram().getClassName(), Arrays.asList(new String[]{StrykerConfig.getInstance().getTestsOutputDir(), StrykerConfig.getInstance().getCompilingSandbox()}));
        Class<?> testClass = JavaCompilerAPI.getInstance().reloadClassFrom(testClassName, Arrays.asList(new String[]{StrykerConfig.getInstance().getTestsOutputDir(), StrykerConfig.getInstance().getCompilingSandbox()}));
        
        Method[] methods = testClass.getDeclaredMethods();
        Method methodToRun = null;
        for (Method m : methods) {
        	if(m.getName().compareTo("test")==0) {
        		methodToRun = m;
        		break;
        	}
        }
        methodToRun.setAccessible(true);
        final Method methodToRunInCallable = methodToRun;
        final String methodToRepair = candidate.getMethodToFix();
        final Object oToRun = testClass.newInstance();
        final Object[] inputToInvoke = new Object[]{candidateClass, methodToRepair};
        Callable<Boolean> task = new Callable<Boolean>() {
            public Boolean call() throws InvocationTargetException {
                Boolean result = false;
                try {
                    runningThread = Thread.currentThread();
                    //DELETE+++
//                    if (!JavaCompilerAPI.getInstance().compile(StrykerConfig.getInstance().getCompilingSandbox()+"delete/ReflectionTestSimpleClass.java", new String[]{StrykerConfig.getInstance().getCompilingSandbox()})) {
//                    	System.out.println("la super kakona");
//                    }
//                    Class<?> reflectionTestSimpleClassClass = JavaCompilerAPI.getInstance().reloadClassFrom("delete.ReflectionTestSimpleClass", StrykerConfig.getInstance().getCompilingSandbox());
//                    Object reflectionTestSimpleClassInstance = reflectionTestSimpleClassClass.newInstance();
//                    Method[] methodsRTSC = reflectionTestSimpleClassClass.getDeclaredMethods();
//                    Method testMethod = null;
//                    for (Method m : methodsRTSC) {
//                    	if (m.getName().compareTo("test") == 0) {
//                    		testMethod = m;
//                    		break;
//                    	}
//                    }
//                    if (testMethod != null) {
//                    	testMethod.setAccessible(true);
//                    	testMethod.invoke(reflectionTestSimpleClassInstance, new Object[]{candidateClass});
//                    }
                    //DELETE---
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
                	if (RacAPI.verbose) System.out.println("Entered InvocationTargetException");
                	if (RacAPI.verbose) System.out.println("QUIT BECAUSE OF JML RAC");
                	if (RacAPI.verbose) e.printStackTrace();
                    String retValue = null;
                    StringWriter sw = null;
                    PrintWriter pw = null;
                    try {
                        sw = new StringWriter();
                        pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        retValue = sw.toString();
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
                        result = true;
                    } else if (retValue.contains("ThreadDeath")) {
                    	if (RacAPI.verbose) System.out.println("THREAD DEATH EN RAC!!!!!!!!!!!!!!!!");
                        result = true;
                    } else {
                    	if (RacAPI.verbose) System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" +
                                "\nFAILED METHODDDD FOR NO REASON!!!!!!!!!!!!!!!!!!!!" +
                                "\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    	if (RacAPI.verbose) e.printStackTrace();
                        result = true;
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
            result = future.get(300, TimeUnit.DAYS);//.MILLISECONDS);
        } catch (TimeoutException ex) {
            //runningThread.interrupt();
        	runningThread.interrupt();
            executor.shutdownNow();
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
		if (junitTests.isEmpty()) {
			return results;
		}
		
		candidate.getProgram().makeBackup();
		
		String[] classpathToCompile = new String[]{StrykerConfig.getInstance().getCompilingSandbox()};
		
		if (!JavaCompilerAPI.getInstance().compileWithJML4C(StrykerConfig.getInstance().getCompilingSandbox() + candidate.getProgram().getClassNameAsPath()+".java", classpathToCompile)) {
			System.err.println("error while compiling rac version of FixCandidate!");
			Arrays.fill(results, false);
			candidate.getProgram().restoreBackup();
			return results;
		}
		
		int t = 0;
		for (t = 0; t < junitTests.size(); t++) {
			results[t] = this.runJUnit(candidate, junitTests.get(t));
			if (!results[t] && stopAtFirstFail) {
				break;
			}
		}

		
		candidate.getProgram().restoreBackup();
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
	
	
	public boolean runTest(FixCandidate candidate, CounterExample ce) {
		TestResult result = TestRunner.runTest(ce);
		boolean testPassed = false;
		switch(result.getResult()) {
			case ERROR_INITIALIZATION: {
				if (RacAPI.verbose) {
					System.out.println("Error initializating test");
					if (result.getException() != null) result.getException().printStackTrace();
				}
				testPassed = true;
				break;
			}
			case ERROR_METHOD_EXCEPTION: {
				if (RacAPI.verbose) {
					System.out.println("Tested method throwed an exception");
					if (result.getException() != null) result.getException().printStackTrace();
				}
				testPassed = false;
				break;
			}
			case ERROR_RUNTIME: {
				if (RacAPI.verbose) {
					System.out.println("Exception while running test");
					if (result.getException() != null) result.getException().printStackTrace();
				}
				testPassed = true;
				break;
			}
			case ERROR_SPECIFICATION: {
				if (RacAPI.verbose) {
					System.out.println("Specification error while running test");
					if (result.getException() != null) result.getException().printStackTrace();
				}
				testPassed = false;
				break;
			}
			case ERROR_TIMEOUT: {
				if (RacAPI.verbose) {
					System.out.println("Method tested timed out");
					if (result.getException() != null) result.getException().printStackTrace();
				}
				testPassed = true;
				break;
			}
			case VALID: {
				if (RacAPI.verbose) {
					System.out.println("Test passed");
					if (result.getException() != null) result.getException().printStackTrace();
				}
				testPassed = true;
				break;
			}
		}
		return testPassed;
	}
	
	
	public boolean[] runTests(FixCandidate candidate, List<CounterExample> counterExamples, boolean stopAtFirstFail) {
		boolean[] results = new boolean[counterExamples.size()];
		Arrays.fill(results, true);
		if (counterExamples.isEmpty()) {
			return results;
		}
		
		candidate.getProgram().makeBackup();
		
		String[] classpathToCompile = new String[]{StrykerConfig.getInstance().getCompilingSandbox()};
		
		if (!JavaCompilerAPI.getInstance().compileWithJML4C(StrykerConfig.getInstance().getCompilingSandbox() + candidate.getProgram().getClassNameAsPath()+".java", classpathToCompile)) {
			System.err.println("error while compiling rac version of FixCandidate!");
			Arrays.fill(results, false);
			candidate.getProgram().restoreBackup();
			return results;
		}
		
		int t = 0;
		for (t = 0; t < counterExamples.size(); t++) {
			results[t] = this.runTest(candidate, counterExamples.get(t));
			if (!results[t] && stopAtFirstFail) {
				break;
			}
		}

		
		candidate.getProgram().restoreBackup();
		return results;
	}
	
	/**
	 * This method runs a list of counter examples for a give {@code FixCandidate}
	 * 
	 * 
	 * @param candidate
	 * @param counterExamples
	 * @return
	 */
	public boolean[] runTests(FixCandidate candidate, List<CounterExample> counterExamples) {
		return this.runTests(candidate, counterExamples, true);
	}
	
}

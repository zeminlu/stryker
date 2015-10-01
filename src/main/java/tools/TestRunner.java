package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.google.common.primitives.Primitives;

import tools.apis.ReloaderAPI;
import tools.data.CounterExample;
import ar.edu.taco.TacoException;


/**
 * This class is used to check a fix candidate against a previously built counter example without constructing a JUnit test
 * <p>
 * Given a fix candidate compiled with {@code JML RAC} and a counter example represented by a {@code CounterExample} instance
 * this will run the fix candidate using the counter example values and check if executed method ends successfully or not
 * <p>
 * @author Simon Emmanuel Gutierrez Brida
 * @version 0.1u
 */
public final class TestRunner {
	
	/**
	 * A class used when a method argument type is not yet discovered
	 * 
	 * @author Simon Emmanuel Gutierrez Brida
	 * @version 1.0
	 */
	private static final class Dummy{}
	private static final TestResult NoCounterExample = new TestResult(TestResult.Result.ERROR_INITIALIZATION, new IllegalArgumentException("Trying to run a test with no counter example"));
	private static final TestResult NullCounterExample = new TestResult(TestResult.Result.ERROR_INITIALIZATION, new IllegalArgumentException("Trying to run a test with a null counter example"));
	private static final TestResult ErrorLoadingCandidateClass = new TestResult(TestResult.Result.ERROR_INITIALIZATION, new RuntimeException("Error while loading candidate class"));
	private static final TestResult NoMethodFound = new TestResult(TestResult.Result.ERROR_INITIALIZATION, new RuntimeException("Couldn't find method to run"));
	private static Thread runningThread;
	private static boolean verbose = false;
	private static int timeout = 300;
	
	/**
	 * This class is used to describe the result of running the counter example
	 * 
	 * @author Simon Emmanuel Gutierrez Brida
	 * @version 1.0
	 */
	public static final class TestResult {
		/**
		 * An enum defining all possible results when running a fix candidate against a counter example
		 * 
		 * @author Simon Emmanuel Gutierrez Brida
		 * @version 1.0
		 */
		public static enum Result {
			/**
			 * When a fix candidate is executed against a counter example and no specification is violated
			 */
			VALID,
			
			/**
			 * When there was a runtime exception but doesn't fall inside any of the other categories
			 * This type of errors can be caused by an exception in RAC or a reflection error.
			 */
			ERROR_RUNTIME,
			
			/**
			 * When a specification was violated during runtime
			 */
			ERROR_SPECIFICATION,
			
			/**
			 * When an exception occurred before running the counter example
			 */
			ERROR_INITIALIZATION,
			
			/**
			 * When the fix candidate took more than the alloted time while running the counter example
			 */
			ERROR_TIMEOUT,
			
			/**
			 * An exception occurred while running the fix candidate method under examination
			 */
			ERROR_METHOD_EXCEPTION
		};
		
		/**
		 * The type of the result
		 */
		private Result result;
		/**
		 * The associated exception if exists
		 */
		private Exception ex;
		
		/**
		 * Constructs a new instance of this class
		 * 
		 * @param result	:	the type of the result
		 * @param ex		:	the associated exception (or {@code null} if no exception occurred)
		 */
		public TestResult(Result result, Exception ex) {
			this.result = result;
			this.ex = ex;
		}
		
		/**
		 * @return The type of the result
		 */
		public Result getResult() {
			return this.result;
		}
		
		/**
		 * @return The associated exception if exists
		 */
		public Exception getException() {
			return this.ex;
		}
		
	}
	
	/**
	 * Sets the verbosity of this class
	 * 
	 * @param verbose	:	whether to output information {@code true} or not {@code false}
	 */
	public static void setVerbose(boolean verbose) {
		TestRunner.verbose = verbose;
	}
	
	/**
	 * Defines the timeout used when running the fix candidate against a counter example
	 * 
	 * @param timeout	:	the timeout to use in milliseconds.
	 */
	public static void setTimeout(int timeout) {
		TestRunner.timeout = timeout;
	}
	
	/**
	 * Runs the method to repair in the fix candidate using the provided counter example
	 * 
	 * @param ce	:	the counter example to use (it includes information need to find which fix candidate to run)
	 * @return	a {@code TestResult} object containing information about the fix candidate run.
	 * @throws ClassNotFoundException
	 */
	public static TestResult runTest(CounterExample ce) throws ClassNotFoundException {
		if (ce == null) return TestRunner.NullCounterExample;
		if (!ce.counterExampleExist()) return TestRunner.NoCounterExample;
		ce.refresh();
		Class<?> candidateClass = ReloaderAPI.getInstance().reload(ce.getFixCandidate().getProgram().getClassName());
		if (candidateClass == null) return TestRunner.ErrorLoadingCandidateClass;
		Object[] params = retrieveParams(candidateClass, ce);
		Method methodToRun = retrieveMethodToRun(candidateClass, ce, params);
		if (methodToRun == null) return TestRunner.NoMethodFound;
		Object instance = null;
		try {
			fillDummies(candidateClass, ce, params, methodToRun);
			instance = Modifier.isStatic(methodToRun.getModifiers())?null:retrieveInstance(candidateClass, ce);
			setStaticFields(ce);
		} catch (InstantiationException | IllegalAccessException e) {
			return new TestResult(TestResult.Result.ERROR_INITIALIZATION, e);
		}
		return runTest(instance, methodToRun, params, ce);
	}
	
	private static void fillDummies(Class<?> candidateClass, CounterExample ce, Object[] params, Method methodToRun) throws InstantiationException, IllegalAccessException {
		Class<?>[] paramTypes = methodToRun.getParameterTypes();
		for (int p = 0; p < paramTypes.length; p++) {
			if (params[p] instanceof Dummy) {
				params[p] = defaultValue(paramTypes[p]);
			}
		}
	}

	private static Object retrieveInstance(Class<?> candidateClass, CounterExample ce) throws InstantiationException, IllegalAccessException {
		if (ce.getRecoveredInformation().getSnapshot().containsKey("thiz_0")) {
			return ce.getRecoveredInformation().getSnapshot().get("thiz_0");
		} else {
			return candidateClass.newInstance();
		}
	}
	
	private static Object[] retrieveParams(Class<?> candidateClass, CounterExample ce) {
		List<String> paramNames = ce.getRecoveredInformation().getMethodParametersNames();
		if (paramNames == null || paramNames.isEmpty()) {
			return null;
		} else {
			Object[] params = new Object[paramNames.size()];
			int i = 0;
			for (String param : paramNames) {
				Object paramValue;
				if (ce.getRecoveredInformation().getSnapshot().containsKey(param+"_0")) {
					paramValue = ce.getRecoveredInformation().getSnapshot().get(param+"_0");
					if (paramValue == null) {
						paramValue = new Dummy();
					}
				} else {
					paramValue = new Dummy();
				}
				params[i++] = paramValue;
			}
			return params;
		}
	}
	
	private static void setStaticFields(CounterExample ce) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		Map<String, Map<String, Object>> staticFieldsValues = ce.getRecoveredInformation().getStaticFieldsValues();
		for (Entry<String, Map<String, Object>> staticFieldValuesPerClass : staticFieldsValues.entrySet()) {
			String clazzName = staticFieldValuesPerClass.getKey();
			Map<String, Object> fieldValues = staticFieldValuesPerClass.getValue();
			Class<?> clazz = ReloaderAPI.getInstance().load(clazzName);
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (fieldValues.containsKey(field.getName())) {
					field.setAccessible(true);
					field.set(null, fieldValues.get(field.getName()));
					field.setAccessible(false);
				}
			}
		}
	}
	
	private static TestResult runTest(final Object instance, final Method methodToRun, final Object[] params, final CounterExample ce) {
		Callable<TestResult> task = new Callable<TestResult>() {
            public TestResult call() throws InvocationTargetException {
                try {
                	runningThread = Thread.currentThread();
                    long timeprev = System.currentTimeMillis();
                    methodToRun.invoke(instance, params);
                    long timepost = System.currentTimeMillis();
                    if (TestRunner.verbose) System.out.println("time taken: "+(timepost - timeprev));
                } catch (IllegalAccessException e) {
                	if (TestRunner.verbose) System.out.println("Entered IllegalAccessException");
                	if (TestRunner.verbose) e.printStackTrace();
                	return new TestResult(TestResult.Result.ERROR_RUNTIME, e);
                } catch (IllegalArgumentException e) {
                	if (TestRunner.verbose) System.out.println("Entered IllegalArgumentException");
                	if (TestRunner.verbose) e.printStackTrace();
                	return new TestResult(TestResult.Result.ERROR_RUNTIME, e);
                } catch (InvocationTargetException e) {
                	if (TestRunner.verbose) System.out.println("Entered InvocationTargetException");
                	if (TestRunner.verbose) System.out.println("QUIT BECAUSE OF JML RAC");
                	if (TestRunner.verbose) e.printStackTrace();
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
                    	if (TestRunner.verbose) System.out.println("Fallo por la postcondicion!!");
                        return new TestResult(TestResult.Result.ERROR_SPECIFICATION, e);
                    	//result = false;
                    } else if (retValue.contains("JMLExitExceptionalPostconditionError")) { 
                        return new TestResult(TestResult.Result.ERROR_SPECIFICATION, e);
                    	//result = false;
                    } else if (retValue.contains("NullPointerException")) {
                    	if (TestRunner.verbose) System.out.println("NULL POINTER EXCEPTION EN RAC!!!!!!!!!!!!");
                        return new TestResult(TestResult.Result.ERROR_RUNTIME, e);
                    	//result = true;
                    } else if (retValue.contains("ThreadDeath")) {
                    	if (TestRunner.verbose) System.out.println("THREAD DEATH EN RAC!!!!!!!!!!!!!!!!");
                    	return new TestResult(TestResult.Result.ERROR_RUNTIME, e);
                    	//result = true;
                    } else {
                    	if (TestRunner.verbose) System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" +
                                "\nFAILED METHODDDD FOR NO REASON!!!!!!!!!!!!!!!!!!!!" +
                                "\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    	if (TestRunner.verbose) e.printStackTrace();
                        return new TestResult(TestResult.Result.ERROR_RUNTIME, e);
                    	//result = true;
                    }
                } catch (Throwable e) {
                	if (TestRunner.verbose) System.out.println("Entered throwable");
                	if (TestRunner.verbose) System.out.println("THROWABLEEE!!!!!!!!!!!!!!!!!!!!!!");
                	if (TestRunner.verbose) e.printStackTrace();
                    return new TestResult(TestResult.Result.ERROR_METHOD_EXCEPTION, new RuntimeException(e));
                	//return false;
                }
                return new TestResult(TestResult.Result.VALID, null);
            }
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<TestResult> future = executor.submit(task);
        TestResult result = null;
        try {
            result = future.get(TestRunner.timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
        	if (runningThread != null) runningThread.interrupt();
            executor.shutdownNow();
            result = new TestResult(TestResult.Result.ERROR_TIMEOUT, ex);
        } catch (InterruptedException e) {
        	if (TestRunner.verbose) System.out.println("Interrupted");
        	result = new TestResult(TestResult.Result.ERROR_RUNTIME, e);
        } catch (ExecutionException e) {
        	if (TestRunner.verbose) System.out.println("Excecution Exception");
        	result = new TestResult(TestResult.Result.ERROR_RUNTIME, e);
        } catch (Throwable e) {
        	if (TestRunner.verbose) System.out.println("Exception");
        	result = new TestResult(TestResult.Result.ERROR_RUNTIME, new RuntimeException(e));
        } finally {
            future.cancel(true); // may or may not desire this	
            executor.shutdown();
        }
        return result;
	}
	
	private static Method retrieveMethodToRun(Class<?> candidateClass, CounterExample ce, Object[] params) {
		Method[] methods = candidateClass.getDeclaredMethods();
		for (Method method : methods) {
			if (!(method.getName().compareTo(ce.getRecoveredInformation().getMethodToCheck()) == 0)) {
				continue;
			}
			if (method.getParameterTypes().length != ce.getRecoveredInformation().getMethodParametersNames().size()) {
				continue;
			}
			Class<?>[] methodParamTypes = method.getParameterTypes();
			boolean correctParamTypes = true;
			for (int t = 0; t < methodParamTypes.length; t++) {
				Class<?> formalType = methodParamTypes[t];
				Class<?> actualType = params[t].getClass();
				if (actualType.getCanonicalName().compareTo(Dummy.class.getCanonicalName())==0) {
					continue;
				}
				if (!compatibleTypes(formalType, actualType)) {
					correctParamTypes = false;
					break;
				}
//				if (!formalType.isAssignableFrom(actualType)) {
//					correctParamTypes = false;
//					break;
//				}
			}
			if (correctParamTypes) {
				return method;
			}
		}
		return null;
	}
	
	/**
	 * Method that allows to check if two classes are compatible
	 * 
	 * @param p	:	the class to check compatibility with
	 * @param v	:	the class to check compatibility
	 * @return {@code true} if {@code v} can be assigned to {@code p}
	 */
	private static boolean compatibleTypes(Class<?> p, Class<?> v) {
		if (p.isAssignableFrom(v)) {
			return true;
		}
		if (p.isPrimitive() && Primitives.isWrapperType(v)) {
			return p.isAssignableFrom(Primitives.unwrap(v));
		}
		if (Primitives.isWrapperType(p) && v.isPrimitive()) {
			return Primitives.unwrap(p).isAssignableFrom(v);
		}
		return false;
	}
	
	private static Object defaultValue(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        Object value;
        if (clazz.isPrimitive()) {
            String typeSimpleName = clazz.getSimpleName();

            if (typeSimpleName.equals("boolean")) {
                value = false;
            } else if (typeSimpleName.endsWith("byte")) {
                value = 0;
            } else if (typeSimpleName.endsWith("char")) {
                value = "'a'";
            } else if (typeSimpleName.endsWith("double")) {
                value = 0;
            } else if (typeSimpleName.endsWith("float")) {
                value = 0;
            } else if (typeSimpleName.endsWith("int")) {
                value = 0;
            } else if (typeSimpleName.endsWith("long")) {
                value = 0L;
            } else if (typeSimpleName.endsWith("short")) {
                value = 0;
            } else {
                throw new TacoException("ERROR: Undefined in class UnitTestBuilder, method defaultValue");
            }
        } else {
            String name = clazz.getName();
            if (name.equals("java.lang.Boolean")) {
                value = false;
            } else if (name.endsWith("java.lang.Byte")) {
                value = 0;
            } else if (name.endsWith("java.lang.Character")) {
                value = "'a'";
            } else if (name.endsWith("java.lang.Double")) {
                value = 0;
            } else if (name.endsWith("java.lang.Float")) {
                value = 0;
            } else if (name.endsWith("java.lang.Integer")) {
                value = 0;
            } else if (name.endsWith("java.lang.Long")) {
                value = 0;
            } else if (name.endsWith("java.lang.Short")) {
                value = 0;
            } else {
                value = clazz.newInstance();
            }
        }
        return value;
    }

}

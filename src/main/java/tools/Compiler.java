package tools;

import java.util.Arrays;
import java.util.List;

import tools.apis.JavaCompilerAPI;
import config.StrykerConfig;

/**
 * This class offers convenience methods that uses {@code JavaCompileAPI}, these methods are :
 * <p>
 * <li>Compile a class that is inside the {@link config.StrykerConfig#getCompilingSandbox() compiling sandbox}</li>
 * <li>Compile a class given a working directory and a classpath</li>
 * <li>Compile a class with JML RAC given a working directory and a classpath</li>
 * <p>
 * @author Simon Emmanuel Gutierrez Brida
 * @version 0.1
 */
public class Compiler {

	
	/**
	 * Compile a class that is inside the {@link config.StrykerConfig#getCompilingSandbox() compiling sandbox}
	 * 
	 * @param classToRepair : the class to compile, all classes required will also be compiled
	 * @return {@code true} iff the compilation process was successful
	 */
	public static boolean compileProject(String classToRepair) {
		String sandboxPath = StrykerConfig.getInstance().getCompilingSandbox();
		String testsOutputDir = StrykerConfig.getInstance().getTestsOutputDir();
		return compileClass(classToRepair, sandboxPath, Arrays.asList(new String[]{testsOutputDir}));
	}
	
	/**
	 * Compile a class given a working directory and a classpath
	 * 
	 * @param className		:	the class to compile, all classes required will also be compiled
	 * @param workingDir	:	the working directory (where the class to compile is)
	 * @param classpath		:	the classpath needed to compile
	 * @return {@code true} iff the compilation process was successful
	 */
	public static boolean compileClass(String className, String workingDir, List<String> classpath) {
		return JavaCompilerAPI.getInstance().compile(workingDir + StrykerConfig.getInstance().getFileSeparator() + classNameToJavaFilePath(className), classpath.toArray(new String[classpath.size()]));
	}
	
	/**
	 * Compile a class with JML RAC given a working directory and a classpath
	 * 
	 * @param className		:	the class to compile, all classes required will also be compiled
	 * @param workingDir	:	the working directory (where the class to compile is)
	 * @param classpath		:	the classpath needed to compile
	 * @return {@code true} iff the compilation process was successful
	 */
	public static boolean compileClassWithJML4C(String className, String workingDir, List<String> classpath) {
		return JavaCompilerAPI.getInstance().compileWithJML4C(workingDir + StrykerConfig.getInstance().getFileSeparator() + classNameToJavaFilePath(className), classpath.toArray(new String[classpath.size()]));
	}
	
	
	private static String classNameToJavaFilePath(String className) {
		return className.replaceAll("\\.", StrykerConfig.getInstance().getFileSeparator()) + ".java";
	}
	
	
}

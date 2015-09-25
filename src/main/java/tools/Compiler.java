package tools;

import java.util.Arrays;
import java.util.List;

import tools.apis.JavaCompilerAPI;
import config.StrykerConfig;

public class Compiler {

	
	public static boolean compileProject(String classToRepair) {
		String sandboxPath = StrykerConfig.getInstance().getCompilingSandbox();
		String testsOutputDir = StrykerConfig.getInstance().getTestsOutputDir();
		return compileClass(classToRepair, sandboxPath, Arrays.asList(new String[]{testsOutputDir}));
	}
	
	public static boolean compileClass(String className, String workingDir, List<String> classpath) {
		return JavaCompilerAPI.getInstance().compile(workingDir + StrykerConfig.getInstance().getFileSeparator() + classNameToJavaFilePath(className), classpath.toArray(new String[classpath.size()]));
	}
	
	public static boolean compileClassWithJML4C(String className, String workingDir, List<String> classpath) {
		return JavaCompilerAPI.getInstance().compileWithJML4C(workingDir + StrykerConfig.getInstance().getFileSeparator() + classNameToJavaFilePath(className), classpath.toArray(new String[classpath.size()]));
	}
	
	
	private static String classNameToJavaFilePath(String className) {
		return className.replaceAll("\\.", StrykerConfig.getInstance().getFileSeparator()) + ".java";
	}
	
	
}

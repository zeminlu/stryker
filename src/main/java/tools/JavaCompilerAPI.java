package tools;

import java.io.File;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import config.StrykerConfig;

/**
 * A class that offers simple method to compile java files using java compiler
 * 
 * @author Simón Emmanuel Gutiérrez Brida
 * @version 0.1u
 */
public class JavaCompilerAPI {
	
	private static JavaCompilerAPI instance;
	
	/**
	 * @return an instance of this class
	 */
	public static JavaCompilerAPI getInstance() {
		if (instance == null) instance = new JavaCompilerAPI();
		return instance;
	}
	
	private JavaCompilerAPI() {}

	/**
	 * Given a path to a java file a list of folders, this method will try
	 * to compile the given java file (and any file needed) using the list
	 * of folders as classpath.
	 * 
	 * @param pathToFile	:	the path to a java file to compile	:	{@code String}
	 * @param classpath		:	all the paths needed to compile the java file pointed by {@code pathToFile}	:	{@code String[]}
	 * @return
	 */
	public boolean compile(String pathToFile, String[] classpath) {
		File fileToCompile = new File(pathToFile);
		if (!fileToCompile.exists() || !fileToCompile.isFile() || !fileToCompile.getName().endsWith(".java")) {
			return false;
		}
		File[] files = new File[]{fileToCompile};
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));
		boolean compileResult = compiler.getTask(null, fileManager, null, Arrays.asList(new String[] {"-classpath", convertPathsToString(classpath)}), null, compilationUnit).call();
		return compileResult;
	}
	
	private String convertPathsToString(String[] paths) {
		String result = "";
		for (int p = 0; p < paths.length; p++) {
			result += paths[p];
			if (p + 1 < paths.length) {
				result += StrykerConfig.getInstance().getPathSeparator();
			}
		}
		return result;
	}
	
}

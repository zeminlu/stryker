package tools;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import mujava.app.Reloader;

import config.StrykerConfig;

/**
 * A class that offers simple method to compile java files using java compiler
 * this class also offers the functionality of {@code Reloader}
 * 
 * @author Simón Emmanuel Gutiérrez Brida
 * @version 0.2.2
 * @see Reloader
 */
public class JavaCompilerAPI {
	
	private static JavaCompilerAPI instance;
	
	/**
	 * used to load/reload classes
	 * @see Reloader
	 */
	private Reloader reloader;
	
	/**
	 * the classpath that will be used by {@code reloader}
	 */
	private List<String> reloaderClasspath;
	
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
	
	/**
	 * This method will update the classpath used by the reloader
	 * any other method related to the reloader must be called after
	 * calling this method at least once
	 * 
	 * @param classpath	:	the classpath to be used	:	{@code String[]}
	 */
	public void updateReloaderClassPath(String[] classpath) {
		if (this.reloaderClasspath == null) {
			this.reloaderClasspath = new LinkedList<String>();
		}
		for (String cp : classpath) {
			if (!this.reloaderClasspath.contains(cp)) {
				this.reloaderClasspath.add(cp);
			}
		}
		if (this.reloader == null) {
			this.reloader = new Reloader(this.reloaderClasspath, Thread.currentThread().getContextClassLoader());
		}
	}
	
	/**
	 * Causes the reloader to reload a class
	 * 
	 * @param className	:	the class to reload	:	{@code String}
	 * @return the reloaded class
	 */
	public Class<?> reloadClass(String className) {
		if (this.reloader == null) {
			throw new IllegalStateException("JavaCompilerAPI#reloadClass(String) called without a reloader built");
		}
		return this.reloader.rloadClass(className, true);
	}
	
	/**
	 * Causes the reloader to load a class
	 * 
	 * @param className	:	the class to load	:	{@code String}
	 * @return the loaded class
	 */
	public Class<?> loadClass(String className) {
		if (this.reloader == null) {
			throw new IllegalStateException("JavaCompilerAPI#loadClass(String) called without a reloader built");
		}
		return this.reloader.rloadClass(className, false);
	}
	
	/**
	 * @return the reloader
	 */
	public Reloader getReloader() {
		if (this.reloader == null) {
			throw new IllegalStateException("JavaCompilerAPI#getReloader() called without a reloader built");
		}
		return this.reloader;
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

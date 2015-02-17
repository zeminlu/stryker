package tools;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import mujava.app.Reloader;
import mujava.util.JustCodeDigest;

import config.StrykerConfig;

/**
 * A class that offers simple method to compile java files using java compiler
 * this class also offers the functionality of {@code Reloader}
 * 
 * @author Simón Emmanuel Gutiérrez Brida
 * @version 0.3
 * @see Reloader
 */
public class JavaCompilerAPI {
	
	private PrintWriter out = new PrintWriter(System.out);
	private PrintWriter err = new PrintWriter(System.err);
	private String[] jml4cArgs = {
          "-Xlint:all",			//0
          "-nowarn",			//1
          "-maxProblems",		//2
          "9999999",			//3
          "-cp",				//4
          "",					//5	CLASSPATH
          "-1.7",				//6
          ""					//7	CLASS TO COMPILE
	};
	
	private Map<String, byte[]> loadedClassesHashes;
	
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
	
	public static void resetInstance() {
		instance = null;
	}
	
	/**
	 * @return an instance of this class
	 */
	public static JavaCompilerAPI getInstance(Reloader reloader) {
		if (instance == null) instance = new JavaCompilerAPI(reloader);
		if (instance != null && instance.reloader != reloader) throw new IllegalArgumentException("a previous instance was constructed with another reloader");
		return instance;
	}
	
	private JavaCompilerAPI() {
		this.loadedClassesHashes = new HashMap<String, byte[]>();
	}
	
	private JavaCompilerAPI(Reloader reloader) {
		this.reloader = reloader;
		this.loadedClassesHashes = new HashMap<String, byte[]>();
	}

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
		File compiledFile = new File(pathToFile.replace(".java", ".class"));
		if (compiledFile.exists()) {
			return true;
		}
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
	
	public boolean compileWithJML4C(String pathToFile, String[] classpath) {
		File fileToCompile = new File(pathToFile);
		if (!fileToCompile.exists() || !fileToCompile.isFile() || !fileToCompile.getName().endsWith(".java")) {
			return false;
		}
		setJML4Cclasspath(classpath);
		setJML4CtargetClass(pathToFile);
		return org.jmlspecs.jml4.rac.Main.compile(this.jml4cArgs, this.out, this.err, null);
	}
	
	private void setJML4Cclasspath(String[] classpath) {
		this.jml4cArgs[5] = this.convertPathsToString(classpath);
	}
	
	private void setJML4CtargetClass(String pathToFile) {
		this.jml4cArgs[7] = pathToFile;
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
		String classFileToReload = classToFile(className);
		if (classFileToReload == null) {
			//TODO: maybe an exception or a message log?
			return null;
		}
		File javaFileToReload = new File(classFileToReload.replace(".class", ".java"));
		if (this.loadedClassesHashes.containsKey(javaFileToReload.getPath())) {
			byte[] newMD5Hash = JustCodeDigest.digest(javaFileToReload);
			byte[] oldMD5Hash = this.loadedClassesHashes.get(javaFileToReload.getPath());
//			printHash(newMD5Hash);
//			printHash(oldMD5Hash);
			boolean javaFileWasModified = !Arrays.equals(newMD5Hash, oldMD5Hash);
			if (!javaFileWasModified) {
				System.out.println("======RELOAD AVOIDED=====");
				return this.reloader.rloadClass(className, false);
			} else {
				this.loadedClassesHashes.put(javaFileToReload.getPath(), newMD5Hash);
			}
		} else {
			if (javaFileToReload.exists()) {
				byte[] newMD5Hash = JustCodeDigest.digest(javaFileToReload);
				this.loadedClassesHashes.put(javaFileToReload.getPath(), newMD5Hash);
			}
		}
		return this.reloader.rloadClass(className, true);
	}
	
	private void printHash(byte[] theHash) {
		System.out.println("HASH: " + Arrays.toString(theHash));
	}
	
	public Class<?> reloadClass(String className, boolean forceReload) {
		if (!forceReload) {
			return this.reloadClass(className);
		}
		if (this.reloader == null) {
			throw new IllegalStateException("JavaCompilerAPI#reloadClass(String) called without a reloader built");
		}
		return this.reloader.rloadClass(className, true);
	}
	
	private String classToFile(String clazz) {
		String classAsFile = null;
		String classAsPath = clazz.replace(".", StrykerConfig.getInstance().getFileSeparator()) + ".class";
		for (String cp : this.reloaderClasspath) {
			cp = cp.endsWith(StrykerConfig.getInstance().getFileSeparator())?cp:(cp+StrykerConfig.getInstance().getFileSeparator());
			String fullClassPath = cp + classAsPath;
			File classFile = new File(fullClassPath);
			if (classFile.exists()) {
				classAsFile = fullClassPath;
				break;
			}
		}
		return classAsFile;
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

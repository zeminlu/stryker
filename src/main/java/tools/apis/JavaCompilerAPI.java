package tools.apis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Scanner;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import ar.edu.taco.stryker.api.impl.StringsToWriteInFile;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import config.StrykerConfig;

/**
 * A class that offers simple methods to compile java files using java compiler
 * this class also offers compiling using JML RAC.
 * 
 * @author Simón Emmanuel Gutiérrez Brida
 * @version 1.0
 */
public class JavaCompilerAPI {
	
	/**
	 * The instance of this class to be used with the {@code #getInstance()} method
	 */
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
	 * Given a path to a java file and a list of paths, this method will try
	 * to compile the given java file (and any file needed) using the provided classpath.
	 * 
	 * @param pathToFile	:	the path to a java file to compile	:	{@code String}
	 * @param classpath		:	all the paths needed to compile the java file pointed by {@code pathToFile}	:	{@code String[]}
	 * @return {@code true} iff the compilation process succeeded
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
	
	/**
	 * Given a path to a java file and a list of paths, this method will try
	 * to compile with JML RAC the given java file (and any file needed) using the provided classpath.
	 * 
	 * @param pathToFile	:	the path to a java file to compile	:	{@code String}
	 * @param classpath		:	all the paths needed to compile the java file pointed by {@code pathToFile}	:	{@code String[]}
	 * @return {@code true} iff the compilation process succeeded
	 */
	public boolean compileWithJML4C(String pathToFile, String[] classpath) {
		File fileToCompile = new File(pathToFile);
		if (!fileToCompile.exists() || !fileToCompile.isFile() || !fileToCompile.getName().endsWith(".java")) {
			return false;
		}
		//****FROM MuJavaController :
		File newFile = new File(pathToFile+".jmlrac");
		try {
			newFile.createNewFile();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return false;
		}
		File oldFile = new File(pathToFile);
        
		String pathSep = StrykerConfig.getInstance().getPathSeparator();
		String originalFilename = oldFile.getAbsolutePath();
		String jmlRacFileName = newFile.getAbsolutePath();
		jmlRacFileName = adaptSiblingsFileToJML4C(originalFilename, jmlRacFileName);

        if (jmlRacFileName == null) {
            //log.error("MJC: Didn't adapt for JML4C!");
        	return false;
        }
        
        try {
			Files.copy(newFile, oldFile);
			newFile.delete();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
        
        //////////////////////////////////////////////////////////////////////////////////
        String fileClasspath = newFile.getParentFile().getAbsolutePath();

        String[] systemClassPathsToFilter = System.getProperty("java.class.path").split(pathSep);

        String filteredSystemClasspath = "";

        for (int k = 0 ; k < systemClassPathsToFilter.length ; ++k) {
            if (systemClassPathsToFilter[k].contains("org.eclipse.jdt.core") ||
                    systemClassPathsToFilter[k].contains("org.eclipse.text") ||
                    systemClassPathsToFilter[k].contains("org.eclipse.equinox.common") ||
                    systemClassPathsToFilter[k].contains("org.eclipse.equinox.preferences") ||
                    systemClassPathsToFilter[k].contains("org.eclipse.osgi") ||
                    systemClassPathsToFilter[k].contains("org.eclipse.core.contenttype") ||
                    systemClassPathsToFilter[k].contains("org.eclipse.core.jobs") ||
                    systemClassPathsToFilter[k].contains("org.eclipse.core.resources") ||
                    systemClassPathsToFilter[k].contains("org.eclipse.core.runtime") || 
                    systemClassPathsToFilter[k].contains("recoder") ||
                    systemClassPathsToFilter[k].contains("mujava") ||
                    systemClassPathsToFilter[k].contains("javassist") ||
                    systemClassPathsToFilter[k].contains("commons") ||
                    systemClassPathsToFilter[k].contains("antlr") ||
                    systemClassPathsToFilter[k].contains("guava") ||
                    systemClassPathsToFilter[k].contains("jml-release") ||
                    systemClassPathsToFilter[k].contains("antlr") ||
                    systemClassPathsToFilter[k].contains("antlr") ||
                    systemClassPathsToFilter[k].contains("javassist") ||
                    systemClassPathsToFilter[k].contains("reflections")) {
                continue;
            }
            filteredSystemClasspath += systemClassPathsToFilter[k] + pathSep;
        }
        
        for (int c = 0; c < classpath.length; c++) {
        	filteredSystemClasspath += classpath[c];
        	if (c + 1 < classpath.length) {
        		filteredSystemClasspath += pathSep;
        	}
        }

        String currentClasspath = StrykerConfig.getInstance().getJML4CLibPath()+
                pathSep+fileClasspath+
                pathSep+filteredSystemClasspath;

//        String command = "java -Xmx2048m -XX:MaxPermSize=512m -jar " + StrykerConfig.getInstance().getJML4CLibPath() + " "
//                + "-nowarn " + "-maxProblems " + "9999999 " + "-cp " + currentClasspath + " " + originalFilename;
        
        String command = "java -Xmx2048m -XX:MaxPermSize=512 -cp " + currentClasspath + " org.jmlspecs.jml4.rac.Main -nowarn -maxProblems 9999999 -cp " + currentClasspath  + " " + originalFilename;
        
        Process p;
        String errors = "";
        int exitValue = -1;
		try {
			p = Runtime.getRuntime().exec(command);
			errors = CharStreams.toString(new InputStreamReader(p.getErrorStream()));
	        p.waitFor();
	        exitValue = p.exitValue();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (exitValue != 0) {
			System.err.println(errors);
		}
		//---------------------------
		return exitValue == 0;
	}
	
	/**
	 * This method allows to recursively delete all {@code .class} files inside a directory
	 * 
	 * @param dir	:	the folder where to delete all {@code .class} files
	 */
	public void removeAllClassFilesRecursively(String dir) {
		File folder = new File(dir);
		removeAllClassFilesRecursively(folder);
	}
	
	private void removeAllClassFilesRecursively(File folder) {
		for(File file: folder.listFiles()) {
			if (file.isFile() && file.exists() && file.getName().endsWith(".class")) {
				file.delete();
			}
			if (file.isDirectory()) {
				removeAllClassFilesRecursively(file);
			}
		}
	}
	
	/**
	 * This is an adaptation of the method with the same name from comitaco.
	 * <p>
	 * Differences that should be noted includes:
	 * <li>class package remains the same</li>
	 * <li>the method will simply take the code from the original file and write the modified code in the destination file</li>
	 * <li>all files are expected to exist and no new file or directory will be created by this method</li>
	 * <p>
	 * @param originalFilePath		:	the file path from where to read the original code	:	{@code String}
	 * @param destinationFilePath	:	the file path to where to write the modified code	:	{@code String}
	 * @return
	 */
	protected static String adaptSiblingsFileToJML4C(String originalFilePath, String destinationFilePath) {
	    File originalFile = new File(originalFilePath);
	    File destFile = new File(destinationFilePath);
	    if (!originalFile.exists() || !originalFile.canRead() || !originalFile.isFile()) {
	    	return null;
	    }
	    if (!destFile.exists() || !destFile.canWrite() || !destFile.isFile()) {
	    	return null;
	    }
		try {
	        FileOutputStream fos = new FileOutputStream(destFile);

	        Scanner scan = new Scanner(originalFile);
	        scan.useDelimiter("\n");

	        boolean reachAlreadyWritten = false;
	        boolean classFound = false;
	        while(scan.hasNext()) {
	            String str = scan.next();
	            if (!reachAlreadyWritten && str.contains(" class ")) {
	                classFound = true;
	                if (str.contains("{")) {
	                    int index = str.indexOf("{");
	                    String beforeBrace = str.substring(0, index);
	                    String brace = str.substring(index, index + 1);
	                    String afterBrace = str.substring(index + 1, str.length());

	                    fos.write((beforeBrace + "\n").getBytes(Charset.forName("UTF-8")));
	                    fos.write((brace + "\n").getBytes(Charset.forName("UTF-8")));
	                    reachAlreadyWritten = true;
	                    fos.write((StringsToWriteInFile.reachMethod + "\n").getBytes(Charset.forName("UTF-8")));
	                    fos.write((afterBrace + "\n").getBytes(Charset.forName("UTF-8")));
	                }
	            } else if (!reachAlreadyWritten && classFound && str.contains("{")) {
	                int index = str.indexOf("{");
	                String beforeBrace = str.substring(0, index);
	                String brace = str.substring(index, index + 1);
	                String afterBrace = str.substring(index + 1, str.length());

	                fos.write((beforeBrace + "\n").getBytes(Charset.forName("UTF-8")));
	                fos.write((brace + "\n").getBytes(Charset.forName("UTF-8")));
	                reachAlreadyWritten = true;
	                fos.write((StringsToWriteInFile.reachMethod + "\n").getBytes(Charset.forName("UTF-8")));
	                fos.write((afterBrace + "\n").getBytes(Charset.forName("UTF-8")));
	            } else if (str.contains("\\reach")) {
	                String[] eachReach = str.split("\\\\reach");
	                for(int i = 0; i < eachReach.length; i++) {
	                    /*
	                     * I can be sure of this, because since the line starts always with @
	                     * it is wrong to append \\reach to the first string...
	                     */
	                    if(i != 0) {
	                        eachReach[i] = "\\reach" + eachReach[i] + " ";
	                    }
	                }

	                for(int i = 0; i < eachReach.length; i++) {
	                    String each = eachReach[i];

	                    if(each.contains("\\reach") == false) {
	                        continue;
	                    }

	                    String beforeReplacement = "\\reach";
	                    String afterReplacement = "reach"; 
	                    each = each.replace(beforeReplacement, afterReplacement);

	                    int openBracketIndex = each.indexOf(afterReplacement) + afterReplacement.length() + 1;
	                    int closeBracketIndex = each.substring(openBracketIndex).indexOf(")") + openBracketIndex;

	                    String beforeArgs = each.substring(0, openBracketIndex);
	                    String afterArgs = each.substring(closeBracketIndex, each.length());
	                    String args = each.substring(openBracketIndex, closeBracketIndex);

	                    String[] splittedArgs = args.split(",");

	                    String modifiedString = "";
	                    modifiedString += beforeArgs;
	                    modifiedString += splittedArgs[0];
	                    modifiedString += ",";
	                    modifiedString += splittedArgs[1] + ".class";
	                    modifiedString += ",";
	                    modifiedString += "\"" + splittedArgs[2] + "\"";
	                    modifiedString += afterArgs;

	                    eachReach[i] = modifiedString;
	                }

	                String lineToWrite = "";
	                for(String each : eachReach) {
	                    lineToWrite += each;
	                }
	                fos.write((lineToWrite + "\n").getBytes(Charset.forName("UTF-8")));
	            } else {
	                fos.write((str + "\n").getBytes(Charset.forName("UTF-8")));
	            }
	        }
	        fos.close();
	        scan.close();
	    } catch (FileNotFoundException e1) {
	        e1.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    return destinationFilePath;
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

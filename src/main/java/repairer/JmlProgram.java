package repairer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.jmlspecs.checker.JmlOptions;
import org.jmlspecs.checker.Main;

import mujava.util.JustCodeDigest;

public class JmlProgram implements Program {

	private String sourceFolder; // stores the source folder of the file corresponding to the program.
	private String className; // stores the qualified name of the class corresponding to the program.
	private File program; // stores the file corresponding to the program.

	/**
	 * Constructor for class Program. It creates a Program instance from a given file name. File does not need to be
	 * a valid Java program. 
	 * @param sourceFolder is the source folder of the file to be used to create the program
	 * @param className is the qualified name of the class corresponding to the program. Class is assumed to be in a .java file 
	 */
	public JmlProgram(String sourceFolder, String className) {
		if (!isReadable(sourceFolder, className)) throw new IllegalArgumentException("creating program with non existent file");
		this.sourceFolder = sourceFolder;
		this.className = className;
		this.program = new File(className);
	}

	/**
	 * Checks whether a given class name corresponds to an actual file in the file system.
	 * @param sourceFolder is the source folder where the class is located. 
	 * @param className is the qualified name of the class
	 * @return true iff the class is an actual file (it exists and is not a directory) and can be read.
	 */
	public static boolean isReadable(String sourceFolder, String className) {
		File file = new File(sourceFolder, className+".java");
		return (file.exists() && file.canRead() && !file.isDirectory());
	}

	/** 
	 * Checks whether the program instance is a valid JML-annotated Java program.
	 * @return true iff program compiles correctly (including JML parsing).
	 * FIXME the way we are calling the compiler here is "borrowed" from JmlParser. It must be improved.
	 */
	public boolean isValid() {

		JmlOptions options = new JmlOptions("jml");

		// Paths
		File parPath = new File(this.sourceFolder);
		String absPathName = parPath.getAbsolutePath()+"/";


		String classPath = System.getProperty("java.class.path")+":"+absPathName;

		options.set_classpath(classPath);
		options.set_sourcepath(absPathName);

		// Allow generic source code (experimental)
		options.set_generic(true);

		// Parse assertions and other Java 1.4 syntax
		options.set_source("1.4");

		// Deny multi-java code
		options.set_multijava(false);

		// Type-checking configuration
		options.set_purity(true);
		options.set_assignable(true);
		options.set_Assignable(true);
		options.set_universesx("no");

		// Verbose
		options.set_verbose(false);
		options.set_Quiet(!options.verbose());
		options.set_quiet(!options.verbose());

		// Experimental options
		options.set_keepGoing(false);

		Main parser = new Main();

		String[] names = {absPathName+this.className+".java"};
		return parser.run(names, options, null);			
	}

	/**
	 * Returns the md5 digest of the program, without considering comments and blank spaces.
	 * @return the md5 digest of the file where the program is stored.
	 */
	public byte[] getMd5Digest() {
		return JustCodeDigest.digest(this.program);
	}
	
	/**
	 * TODO implement this method, that checks whether a method belongs to a class.
	 * @param methodName
	 * @return
	 */
	public boolean hasMethod(String methodName) {
		if (methodName==null) throw new IllegalArgumentException("method name is null");
		String preRegExp = "(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\]]+\\s+";
		String postRegExp =" *\\([^\\)]*\\) *(\\{?|[^;])";
		String methodDecl = preRegExp + methodName + postRegExp;
		Pattern pattern = Pattern.compile(methodDecl);
		return pattern.matcher(readFile(this.program)).find();
	}
	
	/**
	 * 
	 * @param f
	 * @return
	 */
	private static String readFile(File f) {
        String result = null;
        FileReader fr = null;
        try {
                fr = new FileReader(f);
        } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);
        String line = "";
        try {
                while ((line = br.readLine()) != null) {
                        result += line;
                }
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        try {
                br.close();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        return result;
}
}

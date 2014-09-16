package repairer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.taco.TacoConfigurator;
import ar.edu.taco.TacoException;

import org.jmlspecs.checker.JmlOptions;
import org.jmlspecs.checker.Main;
import org.multijava.mjc.JCompilationUnitType;

import ar.edu.taco.jml.parser.JmlParser;

public class JmlProgram implements Program {

	private String parentPath; // stores the parent path of the file corresponding to the program.
	private String fileName; // stores the name of the file corresponding to the program.
	private File program; // stores the file corresponding to the program.

	/**
	 * Constructor for class Program. It creates a Program instance from a given file name. File does not need to be
	 * a valid Java program. 
	 * @param parentPath is the parent path of the file to be used to create the program
	 * @param fileName is the name of the file, to be used to create the program.
	 */
	public JmlProgram(String parentPath, String fileName) {
		if (!isReadable(parentPath, fileName)) throw new IllegalArgumentException("creating program with non existent file");
		this.parentPath = parentPath;
		this.fileName = fileName;
		this.program = new File(fileName);
	}

	/**
	 * Checks whether a given file name corresponds to an actual file in the file system.
	 * @param parentPath is the parent path of the file 
	 * @param fileName is the name of the file
	 * @return true iff the file is an actual file (it exists and is not a directory) and can be read.
	 */
	public static boolean isReadable(String parentPath, String fileName) {
		File file = new File(parentPath, fileName);
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
		File parPath = new File(this.parentPath);
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

		String[] names = {absPathName+this.fileName};
		return parser.run(names, options, null);			
	}

	/**
	 * Removes the extension of a file name.
	 * @param fileName is the file name to remove the extension from
	 * @return the file name without the extension.
	 */
	private String removeExtension(String fileName) {
		if (fileName==null) throw new IllegalArgumentException("null program name");
		if (fileName.lastIndexOf('.')==-1) throw new IllegalArgumentException("program name does not have extension");
		return (fileName.substring(0, fileName.lastIndexOf('.')));
	}
}

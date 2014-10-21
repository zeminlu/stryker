package tools;

import org.jmlspecs.checker.JmlOptions;
import org.jmlspecs.checker.Main;

import repairer.JMLAnnotatedClass;

/**
 * This class is used to access JMLSpecs, the main responsabilities of this API are:
 * 
 * <li> Given a java source file annotated with JML specifications validate if the java source file and the JML specifications are syntactically correct. </li>
 * 
 * TODO: add author
 *
 * @see JMLAnnotatedClass
 * 
 * @version 0.1u
 */
public class JMLSpecsAPI {
	
	/**
	 * @param javaFile	:	a {@code JMLAnnotatedClass} representing the JML annotated java source file to validate	:	{@code JMLAnnotatedClass}
	 * @return {@code true} if the java source file and the JML specification are syntactically correct or {@code false} othertwise	:	{@code boolean}
	 */
	public boolean isValid(JMLAnnotatedClass javaFile) {
		JmlOptions options = new JmlOptions("jml");


		String classPath = System.getProperty("java.class.path")+":"+javaFile.getAbsolutePath();

		options.set_classpath(classPath);
		options.set_sourcepath(javaFile.getAbsolutePath());

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

		String[] names = {javaFile.getProgramFile().getAbsolutePath()};
		return parser.run(names, options, null);
	}

}

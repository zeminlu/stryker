package tools;

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
	public boolean validate(JMLAnnotatedClass javaFile) {
		//TODO: implement this method
		throw new UnsupportedOperationException ("JMLSpecsAPI#validate(JMLAnnotatedClass) : not yet implemented");
	}

}

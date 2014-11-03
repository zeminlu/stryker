package tools;

import repairer.JMLAnnotatedClass;

/**
 * This class contains data about a program to repair
 * 
 * @author Simón Emmanuel Gutiérrez Brida
 * @version 0.1
 */
public class ProgramData {

	/**
	 * The program (class) to repair
	 */
	private JMLAnnotatedClass programToFix;
	
	/**
	 * Class dependencies of the program (class) to repair
	 */
	private String[] classDependencies;
	
	/**
	 * Constructs a new instance of this class
	 * 
	 * @param sourceFolder	:	where the class should be found, e.g.: {@code src/}				: {@code String}
	 * @param className 	:	the qualified name of the class corresponding to the program	: {@code String}
	 */
	public ProgramData(String sourceFolder, String className) {
		this.programToFix = new JMLAnnotatedClass(sourceFolder, className);
	}
	
	/**
	 * Construct a new instance of this class
	 * 
	 * @param sourceFolder	:	where the class should be found, e.g.: {@code src/}				: 	{@code String}
	 * @param className 	:	the qualified name of the class corresponding to the program	: 	{@code String}
	 * @param dependencies  :	class dependencies of the program to fix						:	{@code String[]}
	 */
	public ProgramData(String sourceFolder, String className, String[] classDependencies) {
		this(sourceFolder, className);
		this.classDependencies = classDependencies;
	}

	/**
	 * @return a {@code JMLAnnotatedClass} representing the program (class) to fix
	 */
	public JMLAnnotatedClass getProgramToFix() {
		return programToFix;
	}

	/**
	 * @return class dependencies of the program to fix
	 */
	public String[] getClassDependencies() {
		return classDependencies;
	}
	
	/**
	 * @return {@code true} if there are class dependencies
	 */
	public boolean hasClassDependencies() {
		return this.classDependencies != null;
	}
	
}

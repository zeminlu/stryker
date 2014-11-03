package tools;

import repairer.JMLAnnotatedClass;

public class ProgramData {

	private JMLAnnotatedClass programToFix;
	
	private String[] classDependencies;
	
	public ProgramData(String sourceFolder, String className) {
		this.programToFix = new JMLAnnotatedClass(sourceFolder, className);
	}
	
	public ProgramData(String sourceFolder, String className, String[] classDependencies) {
		this(sourceFolder, className);
		this.classDependencies = classDependencies;
	}

	public JMLAnnotatedClass getProgramToFix() {
		return programToFix;
	}

	public String[] getClassDependencies() {
		return classDependencies;
	}
	
	public boolean hasClassDependencies() {
		return this.classDependencies != null;
	}
	
}

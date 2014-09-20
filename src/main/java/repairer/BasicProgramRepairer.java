package repairer;

import search.engines.BoundedIterativeDepthFirstSearchEngine;

/**
 * BasicProgramRepairer is a command line application that calls Stryker on a given class and method, and performs the
 * intra statement mutation-based repair, without any pruning.
 * @author Nazareno Aguirre
 *
 */
public class BasicProgramRepairer {
	
	private JmlProgram subjectClass; // class containing method to repair
	private String subjectMethod; // method to repair within subjectClass
	
	private int maxDepth = 3; // max depth to be considered in the search of program repairs
	
	/**
	 * Constructor of class ProgramRepair. It sets the subject of the repair process
	 * with the provided parameter.
	 * @param subjectClass is the class containing the method to be repaired.
	 * @param subjectMethod is the method to be repaired.
	 */
	public BasicProgramRepairer(JmlProgram subjectClass, String subjectMethod) {
		if (subjectClass==null) throw new IllegalArgumentException("program is null");
		if (subjectMethod==null) throw new IllegalArgumentException("method is null");
		if (!subjectClass.isValid()) throw new IllegalArgumentException("program does not compile");
		this.subjectClass = subjectClass;
		this.subjectMethod = subjectMethod;
	}

	/**
	 * Constructor of class ProgramRepair. It sets the subject of the repair process
	 * with the provided parameter.
	 * @param subjectClass is the class containing the method to be repaired.
	 * @param subjectMethod is the method to be repaired.
	 * @param maxDepth is the maximum depth to be considered for the search of repairs.
	 */
	public BasicProgramRepairer(JmlProgram subjectClass, String subjectMethod, int maxDepth) {
		if (subjectClass==null) throw new IllegalArgumentException("program is null");
		if (subjectMethod==null) throw new IllegalArgumentException("method is null");
		if (maxDepth<0) throw new IllegalArgumentException("max depth must be >=0");
		if (!subjectClass.isValid()) throw new IllegalArgumentException("program does not compile");
		this.subjectClass = subjectClass;
		this.subjectMethod = subjectMethod;
		this.maxDepth = maxDepth;
	}

	
	/**
	 * setProgram: it sets the subject of the repair process with the provided parameter.
	 * @param subject is the program that the repair process will be applied to.
	 */
	public void setProgram(JmlProgram subject) {
		if (subject==null) throw new IllegalArgumentException("program is null");
		if (!subject.isValid()) throw new IllegalArgumentException("program does not compile");
		this.subjectClass = subject;		
	}
	
	/**
	 * Sets the maximum depth to be considered in the search of program repairs.
	 * @param maxDepth is the value to be set as maximum depth for the search.
	 */
	public void setMaxDepth(int maxDepth) {
		if (maxDepth<0) throw new IllegalArgumentException("max depth must be >=0");
		this.maxDepth = maxDepth;
	}
	
	/**
	 * Initiates the search for a repair of the subject.
	 * @return true iff a repair of the subject was found.
	 */
	public boolean repair() {
		if (subjectClass==null || subjectMethod==null) throw new IllegalStateException("program or method is null");
		if (!subjectClass.isValid()) throw new IllegalStateException("program does not compile");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(subjectClass, subjectMethod);
		BoundedIterativeDepthFirstSearchEngine<FixCandidate,StrykerRepairSearchProblem> engine = new BoundedIterativeDepthFirstSearchEngine<FixCandidate,StrykerRepairSearchProblem>();
		engine.setProblem(problem);
		engine.setMaxDepth(this.maxDepth);
		return engine.performSearch();		
	}
	
}

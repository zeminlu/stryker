package repairer;

import search.engines.BoundedDepthFirstSearchEngine;

public class ProgramRepairer {
	
	private Program subject; // program to repair
	
	/**
	 * Constructor of class ProgramRepair. It sets the subject of the repair process
	 * with the provided parameter.
	 * @param subject is the program that the repair process will be applied to.
	 */
	public ProgramRepairer(Program subject) {
		if (subject==null) throw new IllegalArgumentException("program is null");
		if (!subject.isCompilable()) throw new IllegalArgumentException("program does not compile");
		this.subject = subject;
	}

	/**
	 * setProgram: it sets the subject of the repair process with the provided parameter.
	 * @param subject is the program that the repair process will be applied to.
	 */
	public void setProgram(Program subject) {
		if (subject==null) throw new IllegalArgumentException("program is null");
		if (!subject.isCompilable()) throw new IllegalArgumentException("program does not compile");
		this.subject = subject;		
	}
	
	/**
	 * Initiates the search for a repair of the subject.
	 * @return true iff a repair of the subject was found.
	 */
	public boolean repair() {
		if (subject==null) throw new IllegalStateException("program is null");
		if (!subject.isCompilable()) throw new IllegalStateException("program does not compile");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(subject);
		BoundedDepthFirstSearchEngine<FixCandidate,StrykerRepairSearchProblem> engine = new BoundedDepthFirstSearchEngine<FixCandidate,StrykerRepairSearchProblem>();
		engine.setProblem(problem);
		return engine.performSearch();		
	}
	
	

}

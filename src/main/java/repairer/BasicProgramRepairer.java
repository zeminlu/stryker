package repairer;

import search.engines.BoundedDepthFirstSearchEngine;

/**
 * BasicProgramRepairer is a command line application that calls Stryker on a given program, and performs the
 * intra statement mutation-based repair, without any pruning.
 * @author Nazareno Aguirre
 *
 */
public class BasicProgramRepairer {
	
	private JmlProgram subject; // program to repair
	
	/**
	 * Constructor of class ProgramRepair. It sets the subject of the repair process
	 * with the provided parameter.
	 * @param subject is the program that the repair process will be applied to.
	 */
	public BasicProgramRepairer(JmlProgram subject) {
		if (subject==null) throw new IllegalArgumentException("program is null");
		if (!subject.isValid()) throw new IllegalArgumentException("program does not compile");
		this.subject = subject;
	}

	/**
	 * setProgram: it sets the subject of the repair process with the provided parameter.
	 * @param subject is the program that the repair process will be applied to.
	 */
	public void setProgram(JmlProgram subject) {
		if (subject==null) throw new IllegalArgumentException("program is null");
		if (!subject.isValid()) throw new IllegalArgumentException("program does not compile");
		this.subject = subject;		
	}
	
	/**
	 * Initiates the search for a repair of the subject.
	 * @return true iff a repair of the subject was found.
	 */
	public boolean repair() {
		if (subject==null) throw new IllegalStateException("program is null");
		if (!subject.isValid()) throw new IllegalStateException("program does not compile");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(subject);
		BoundedDepthFirstSearchEngine<FixCandidate,StrykerRepairSearchProblem> engine = new BoundedDepthFirstSearchEngine<FixCandidate,StrykerRepairSearchProblem>();
		engine.setProblem(problem);
		return engine.performSearch();		
	}
	
	

}

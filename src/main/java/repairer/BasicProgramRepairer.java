package repairer;

import mujava.api.MutantIdentifier;
import search.engines.AbstractBoundedSearchEngine;
import search.engines.BoundedBreadthFirstSearchEngine;
import search.engines.BoundedDepthFirstSearchEngine;

/**
 * BasicProgramRepairer is a command line application that calls Stryker on a given class and method, and performs the
 * intra statement mutation-based repair, without any pruning.
 * @author Nazareno Aguirre
 * @version 0.3
 */
public class BasicProgramRepairer {
	
	/**
	 * jml annotated class to repair
	 */
	private JMLAnnotatedClass subjectClass;
	
	/**
	 * method to repair within {@code subjectClass}
	 */
	private String subjectMethod;
	
	
	/**
	 * Indicates which search strategy is used for searching for a program repair.
	 * By default, the strategy is (bounded) DFS.
	 */
	private boolean dfsStrategy = true;
	
	/**
	 * max depth to be considered in the search of program repairs
	 */
	private int maxDepth = 3;
	
	/**
	 * Constructor of class ProgramRepair. It sets the subject of the repair process
	 * with the provided parameter.
	 * @param subjectClass is the class containing the method to be repaired.
	 * @param subjectMethod is the method to be repaired.
	 */
	public BasicProgramRepairer(JMLAnnotatedClass subjectClass, String subjectMethod) {
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
	public BasicProgramRepairer(JMLAnnotatedClass subjectClass, String subjectMethod, int maxDepth) {
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
	public void setProgram(JMLAnnotatedClass subject) {
		if (subject==null) throw new IllegalArgumentException("program is null");
		if (!subject.isValid()) throw new IllegalArgumentException("program does not compile");
		this.subjectClass = subject;		
	}
	
	/**
	 * Sets Bounded Depth First Search as the strategy to use in the search for program repairs.
	 */
	public void setDfsStrategy() {
		this.dfsStrategy = true;
	}

	/**
	 * Sets Bounded Breadth First Search as the strategy to use in the search for program repairs.
	 */
	public void setBfsStrategy() {
		this.dfsStrategy = false;
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
		AbstractBoundedSearchEngine<FixCandidate,StrykerRepairSearchProblem> engine = null;
		if (this.dfsStrategy) {
			engine = new BoundedDepthFirstSearchEngine<FixCandidate,StrykerRepairSearchProblem>();
		}
		else {
			engine = new BoundedBreadthFirstSearchEngine<FixCandidate,StrykerRepairSearchProblem>();
		}
		engine.setProblem(problem);
		engine.setMaxDepth(this.maxDepth);
		boolean outcome = engine.performSearch();
		if (outcome) {
			FixCandidate solution = engine.getSolution();
			String solutionLocation = solution.program.absPath + solution.program.className + ".java";
			System.out.println("*** FOUND SOLUTION! Get it from: " + solutionLocation);
			System.out.println("*** Mutations that produced the fix: ");
			for (MutantIdentifier mutation : solution.getMutations()) {
				System.out.println(mutation.toString() + " in method " + (mutation.isOneLineInMethodOp()?(this.subjectMethod + " in line " + mutation.getAffectedLine()):"not a method mutation"));
			}
			System.out.println("*** Stats: " + engine.report());
		}
		else {
			System.out.println("*** COULD NOT REPAIR PROGRAM. Try increasing depth in the search for solutions");
			System.out.println("*** Stats: " + engine.report());
		}
		return outcome;
	}
	
}

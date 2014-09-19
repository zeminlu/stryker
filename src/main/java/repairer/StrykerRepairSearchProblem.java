package repairer;

import java.util.List;

import search.AbstractSearchProblem;

public class StrykerRepairSearchProblem implements AbstractSearchProblem<FixCandidate> {

	JmlProgram classToFix; // class to fix using Stryker.
	String methodToFix; // name of method in class classToFix, that is going to be repaired using Stryker.
	
	
	public StrykerRepairSearchProblem(JmlProgram programToFix, String methodToFix) {
		if (programToFix==null) throw new IllegalArgumentException("no program to fix");
		if (methodToFix==null) throw new IllegalArgumentException("no method to fix");
		this.classToFix = programToFix;
		this.methodToFix = methodToFix;
	}
	
	public FixCandidate initialState() {
		if (classToFix==null) throw new IllegalStateException("program to fix not set in stryker search problem");
		return (new FixCandidate(this.classToFix));
	}

	public List<FixCandidate> getSuccessors(FixCandidate s) {
		// TODO Must call mujava to generate mutants of fix candidate.
		return null;
	}

	public boolean success(FixCandidate s) {
		// TODO Must call TACO to check if current fix candidate is successful.
		return false;
	}

}

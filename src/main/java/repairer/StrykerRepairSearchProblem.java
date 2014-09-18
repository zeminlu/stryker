package repairer;

import java.util.List;

import search.AbstractSearchProblem;

public class StrykerRepairSearchProblem implements AbstractSearchProblem<FixCandidate> {

	JmlProgram programToFix;
	
	public StrykerRepairSearchProblem(JmlProgram programToFix) {
		if (programToFix==null) throw new IllegalArgumentException("null program");
		this.programToFix = programToFix;
	}
	
	public FixCandidate initialState() {
		if (programToFix==null) throw new IllegalStateException("program to fix not set in stryker search problem");
		return (new FixCandidate(this.programToFix));
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

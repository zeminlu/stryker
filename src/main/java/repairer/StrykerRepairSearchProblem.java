package repairer;

import java.util.List;

import search.AbstractSearchProblem;

public class StrykerRepairSearchProblem implements AbstractSearchProblem<FixCandidate> {

	JmlProgram programToFix;
	
	public StrykerRepairSearchProblem(JmlProgram programToFix) {
		this.programToFix = programToFix;
	}
	
	public FixCandidate initialState() {
		if (programToFix==null) throw new IllegalStateException("program to fix not set in stryker search problem");
		return (new FixCandidate(this.programToFix));
	}

	public List<FixCandidate> getSuccessors(FixCandidate s) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean success(FixCandidate s) {
		// TODO Auto-generated method stub
		return false;
	}

}

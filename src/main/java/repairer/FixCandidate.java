package repairer;

import search.State;

/**
 * Class that represents a fix candidate. It is, basically, an annotated program
 * @author aguirre
 *
 */

public class FixCandidate implements State {

	JmlProgram program;
	
	public FixCandidate(JmlProgram program) {
		this.program = program;
	}
	
	/**
	 * Checks whether two fix candidates are equivalent. So far, this just compares
	 * the programs within fix candidates.
	 * FIXME This method should implement a more efficient comparison that does not take into
	 * account the comments. It may be a comparison through md5 hashes. 
	 */
	public boolean equals(State other) {
		if (!(other instanceof FixCandidate)) return false;
		FixCandidate otherFix = (FixCandidate) other;
		return (this.program.equals(otherFix.program));
	}

	

}

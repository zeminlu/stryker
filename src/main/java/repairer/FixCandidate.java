package repairer;

import mujava.api.MutantIdentifier;
import search.State;

/**
 * Class that represents a fix candidate. It is, basically, an annotated program
 * @author aguirre
 *
 */
public class FixCandidate implements State {

	JmlProgram program;
	
	private MutantIdentifier mutation; // it holds the mutant identifier that led to current candidate
									   // null for initial fix candidate.
 	
	/**
	 * Constructor of class FixCandidate. It receives the jml program corresponding to the candidate.
	 * Use this constructor to create the initial fix candidate, that does not come from a mutation.
	 * @param program is the program corresponding to the fix candidate.
	 */
	public FixCandidate(JmlProgram program) {
		if (program==null) throw new IllegalArgumentException("creating candidate with null program");
		this.program = program;
		this.mutation = null; // initial candidate does not come from a mutation.
	}

	/**
	 * Constructor of class FixCandidate. It receives the jml program corresponding to the candidate, and
	 * the mutant identifier that led to the candidate. 
	 * @param program is the jml program corresponding to the candidate.
	 * @param mutation is the mutation that led to the candidate.
	 */
	public FixCandidate(JmlProgram program, MutantIdentifier mutation) {
		if (program==null) throw new IllegalArgumentException("creating candidate with null program");
		if (mutation==null) throw new IllegalArgumentException("creating candidate with null mutation");
		this.program = program;
		this.mutation = mutation; 
	}

	
	/**
	 * Checks whether two fix candidates are equivalent. If the parameter is not a fix candidate,
	 * returns false.
	 * @param other is the fix candidate to compare the current object with.
	 * @return whether the two candidates are the same modulo comments and blank spaces.
	 */
	public boolean equals(State other) {
		if (other==null) return false;
		if (!(other instanceof FixCandidate)) return false;
		return (this.program.getMd5Digest().equals(((FixCandidate) other).program.getMd5Digest()));
	}

	

}

package repairer;

import java.util.LinkedList;
import java.util.List;

import mujava.api.MutantIdentifier;
import search.State;

/**
 * Class that represents a fix candidate. It consists of a JML program (JML annotated class) and a mutation that
 * led to the candidate.
 * @author Nazareno Matías Aguirre
 * @version 0.2
 */
public class FixCandidate implements State {

	protected JmlProgram program; // JML program constituting the fix candidate.
	
	protected List<MutantIdentifier> mutations; // it holds the mutant identifiers that led to current candidate
						   			   			// empty for initial fix candidate.
 	
	/**
	 * Constructor of class FixCandidate. It receives the jml program corresponding to the candidate.
	 * Use this constructor to create the initial fix candidate, that does not come from a mutation.
	 * @param program is the program corresponding to the fix candidate.
	 */
	public FixCandidate(JmlProgram program) {
		if (program==null) throw new IllegalArgumentException("creating candidate with null program");
		this.program = program;
		this.mutations = new LinkedList<MutantIdentifier>(); // initial candidate does not come from a mutation.
	}

	/**
	 * Constructor of class FixCandidate. It receives the jml program corresponding to the candidate, and
	 * the mutant identifier that led to the candidate. 
	 * @param program is the jml program corresponding to the candidate.
	 * @param mutation is the mutation that led to the candidate.
	 */
	public FixCandidate(JmlProgram program, MutantIdentifier mutation) {
		this(program);
		if (mutation==null) throw new IllegalArgumentException("creating candidate with null mutation");
		this.mutations.add(mutation);
	}
	
	/**
	 * Constructor of class FixCandidate. It receives the jml program corresponding to the candidate, and
	 * the mutant identifier that led to the candidate. 
	 * @param program is the jml program corresponding to the candidate.
	 * @param mutation is the mutation that led to the candidate.
	 */
	public FixCandidate(JmlProgram program, List<MutantIdentifier> mutations) {
		this(program);
		if (mutations==null) throw new IllegalArgumentException("creating candidate with null mutations");
		this.mutations.addAll(mutations);
	}
	
	/**
	 * @return the mutations applied to this {@code FixCandidate}, the result will never be {@code null}
	 */
	public List<MutantIdentifier> getMutations() {
		return this.mutations;
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

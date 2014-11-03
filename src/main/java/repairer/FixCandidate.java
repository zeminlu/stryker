package repairer;

import java.util.LinkedList;
import java.util.List;

import mujava.api.Mutation;
import search.State;

/**
 * Class that represents a fix candidate. It consists of a JML program (JML annotated class) and a mutation that
 * led to the candidate.
 * @author Nazareno Matías Aguirre
 * @version 0.4
 */
public class FixCandidate implements State {

	/**
	 * Stores JML program constituting the fix candidate.
	 */
	protected JMLAnnotatedClass program; 
	
	
	/**
	 * Stores the name of the method to fix
	 */
	protected String methodToFix;
	
	/**
	 *  Holds the mutant identifiers that led to current candidate
		empty for initial fix candidate.
	 */
	protected List<Mutation> mutations;
	
	/**
	 * Constructor of class FixCandidate. It receives the jml program corresponding to the candidate, and
	 * the name of the method to fix.
	 * Use this constructor to create the initial fix candidate, that does not come from a mutation.
	 * @param program is the program corresponding to the fix candidate.
	 * @param methodToFix is the name of the method to fix.
	 */
	public FixCandidate(JMLAnnotatedClass program, String methodToFix) {
		if (program==null) throw new IllegalArgumentException("creating candidate with null program");
		if (methodToFix==null) throw new IllegalArgumentException("creating candidate with null method to fix");
		this.program = program;
		this.methodToFix = methodToFix;
		this.mutations = new LinkedList<Mutation>(); // initial candidate does not come from a mutation.
	}

	/**
	 * Constructor of class FixCandidate. It receives the jml program corresponding to the candidate, the name of
	 * the method to fix, and the mutant identifier that led to the candidate. 
	 * @param program is the jml program corresponding to the candidate.
	 * @param methodToFix is the name of the method to fix.
	 * @param mutation is the mutation that led to the candidate.
	 */
	public FixCandidate(JMLAnnotatedClass program, String methodToFix, Mutation mutation) {
		this(program, methodToFix);
		if (mutation==null) throw new IllegalArgumentException("creating candidate with null mutation");
		this.mutations.add(mutation);
	}
	
	/**
	 * Constructor of class FixCandidate. It receives the jml program corresponding to the candidate, the
	 * name of the method to fix, and the list of mutant identifiers that led to the candidate. 
	 * @param program is the jml program corresponding to the candidate.
	 * @param methodToFix is the name of the method to fix.
	 * @param mutation is the mutation that led to the candidate.
	 */
	public FixCandidate(JMLAnnotatedClass program, String methodToFix, List<Mutation> mutations) {
		this(program, methodToFix);
		if (mutations==null) throw new IllegalArgumentException("creating candidate with null mutations");
		this.mutations.addAll(mutations);
	}

	/**
	 * JML program constituting the fix candidate.
	 * @return the JML program constituting the fix candidate.
	 */
	public JMLAnnotatedClass getProgram() {
		return program;
	}

	/**
	 * @return the mutations applied to this {@code FixCandidate}, the result will never be {@code null}
	 */
	public List<Mutation> getMutations() {
		return this.mutations;
	}

	/**
	 * @return the name of the method that the current fix candidate is attempting to fix.
	 */
	public String getMethodToFix() {
		return this.methodToFix;
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
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof FixCandidate) {
			return equals((FixCandidate)other);
		}
		if (other == null) return false;
		return other.equals(this);
	}

}

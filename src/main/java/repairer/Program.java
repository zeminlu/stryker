package repairer;

/**
 * Interface Program represents different kinds of programs, each with (possibly) its own mechanisms for 
 * deciding validity.
 * @author aguirre
 *
 */
public interface Program {
	
	/**
	 * Indicates whether a given program is valid or not, for some notion of validity to be defined in 
	 * concrete implementations of the interface.
	 * @return true iff the program is valid.
	 */
	public boolean isValid();

}

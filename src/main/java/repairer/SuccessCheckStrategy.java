package repairer;

/**
 * Interface SuccessCheckStrategy represents the different possible strategies for
 * checking whether a given candidate is successful or not. A StrykerRepairSearchProblem contains a particular
 * success check strategy. Examples of known strategies are TacoSuccessCheckStrategy (that checks whether a
 * candidate is successful or not by calling TACO for bounded verification), and 
 * TacoWithRacSuccessCheckStrategy, that prior to checking candidates using TACO, makes run time checks with 
 * collected inputs. 
 * This interface is part of a Strategy design pattern employed for making more flexible the success check
 * strategy that is part of StrykerRepairSearchProblem.
 * @author Nazareno Aguirre
 *
 */
public interface SuccessCheckStrategy {

	/**
	 * Checks whether a given candidate is a successful fix or not.
	 * @param s is the fix candidate to check
	 * @return true iff the fix candidate is a successful fix.
	 */
	public boolean isSuccessful(FixCandidate s);
	
}

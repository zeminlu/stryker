package repairer;

import java.util.List;
import java.util.Properties;

import search.AbstractSearchProblem;
import ar.edu.taco.TacoAnalysisResult;
import ar.edu.taco.TacoMain;
import ar.edu.taco.TacoNotImplementedYetException;

/**
 * StrykerRepairSearchProblem is a class that contains all concrete elements necessary for implementing the
 * program repair process associated with Stryker, as a search problem. This includes:
 * - the construction of an initial fix candidate to start the search for a fix.
 * - a routine that, given a fix candidate, computes its successors for the search (mutations obtainable from
 * a given fix candidate).
 * - a routine that, given a fix candidate, decides whether it constitutes an actual fix or not.
 * Actual search strategy is decoupled from this class, so that it can be easily set and replaced. They are 
 * defined as implementations of search.engines.AbstractSearchEngine.
 * @author aguirre
 *
 */
public class StrykerRepairSearchProblem implements AbstractSearchProblem<FixCandidate> {

	protected JMLAnnotatedClass classToFix; // class to fix using Stryker.
	protected String methodToFix; // name of method in class classToFix, that is going to be repaired using Stryker.
	
	
	/**
	 * Constructor of StrykerRepairSearchProblem. It receives a JML program to fix, and the name of the
	 * method to fix in the program/class.
	 * @param programToFix is the JML program containing the method to fix
	 * @param methodToFix is the name of the method to fix.
	 */
	public StrykerRepairSearchProblem(JMLAnnotatedClass programToFix, String methodToFix) {
		if (programToFix==null) throw new IllegalArgumentException("no program to fix");
		if (methodToFix==null || methodToFix.isEmpty()) throw new IllegalArgumentException("no method to fix");
		if (!programToFix.hasMethod(methodToFix)) throw new IllegalArgumentException("class " + programToFix.className + " doesn't have method " + methodToFix);
		this.classToFix = programToFix;
		this.methodToFix = methodToFix;
	}
	
	/**
	 * Returns the initial fix candidate, to start a search for a program fix.
	 * @return the received JML program, as a fix candidate (coming from no mutation).
	 */
	public FixCandidate initialState() {
		if (classToFix==null) throw new IllegalStateException("program to fix not set in stryker search problem");
		return (new FixCandidate(this.classToFix));
	}

	/**
	 * Computes the successors of a given fix candidate. Successors are all mutants that can be obtained from
	 * a given fix candidate. These mutations are restricted by: the mutation operators considered (so far, 
	 * a fixed set of all mutators, excluding those that apply to the left-hand side of assignments), and
	 * the mutGenLimits provided in the fix candidate's program text.
	 * @param s is the fix candidate to compute the successors to.
	 * @return the list of all mutations, as fix candidates, obtainable from the fix candidate s.
	 */
	public List<FixCandidate> getSuccessors(FixCandidate s) {
		if (s==null) throw new IllegalArgumentException("null candidate passed for computing successors");
		MuJavaAPI mjAPI = new MuJavaAPI();
		return mjAPI.generateMutants(s, methodToFix);
	}

	/**
	 * Decides whether a given fix candidate is a successful repair or not. To decide it,
	 * TACO is called for bounded verification of the method to repair against its JML specification.
	 * If fix candidate does not compile, it considers the candidate unsuccessful. If the fix candidate
	 * leads to a "not yet implemented" exception when calling TACO, it consider the candidate unsuccessful.
	 * FIXME This code may not be the best way of calling TACO. It must be improved. So far, only
	 * paths are passed, no other verification parameters are checked.
	 * @param s is the fix candidate to analyze	
	 * @return whether the fix candidate is a successful repair or not.
	 */
	public boolean success(FixCandidate s) {
		if (s==null) throw new IllegalArgumentException("null fix candidate");
		if (s.program==null) throw new IllegalArgumentException("null program in fix candidate");
		if (!s.program.isValid()) return false;
		TacoMain taco = new TacoMain(null);
		Properties overridingProperties = new Properties();
		overridingProperties.put("classToCheck",s.program.className);
		overridingProperties.put("relevantClasses",s.program.className);
		overridingProperties.put("methodToCheck",this.methodToFix+"_0");
		overridingProperties.put("jmlParser.sourcePathStr", s.program.absPath);
		TacoAnalysisResult result = null;
		try {
	
			result = taco.run("genericTest.properties", overridingProperties);
		}
		catch (TacoNotImplementedYetException e) {
			// candidate is well formed JML but taco does not support syntax.
			// considering candidate invalid, for the moment.
			return false;
		} 
		return result.get_alloy_analysis_result().isUNSAT();
	}

}

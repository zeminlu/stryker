package repairer;

import java.util.List;
import java.util.Properties;

import config.StrykerConfig;

import search.AbstractSearchProblem;
import tools.MuJavaAPI;
import tools.TacoAPI;

/**
 * StrykerRepairSearchProblem is a class that contains all concrete elements necessary for implementing the
 * program repair process associated with Stryker, as a search problem. This includes:
 * - the construction of an initial fix candidate to start the search for a fix.
 * - a routine that, given a fix candidate, computes its successors for the search (mutations obtainable from
 * a given fix candidate).
 * - a routine that, given a fix candidate, decides whether it constitutes an actual fix or not.
 * Actual search strategy is decoupled from this class, so that it can be easily set and replaced. They are 
 * defined as implementations of search.engines.AbstractSearchEngine.
 * @author Nazareno Matías Aguirre
 * @version 0.1.5
 */
public class StrykerRepairSearchProblem implements AbstractSearchProblem<FixCandidate> {
	

	/**
	 * The initial state of the problem, this object should never change for each new problem
	 */
	private FixCandidate initialState;
	
	/**
	 * class to fix using Stryker.
	 */
	protected JMLAnnotatedClass classToFix;
	
	/**
	 * name of method in class classToFix, that is going to be repaired using Stryker.
	 */
	protected String methodToFix;
	
	
	/**
	 * stores the class to repair and all its dependencies (only java classes)
	 */
	private String[] relevantClasses;
	
	/**
	 * Type scopes, provided as a string
	 * FIXME improve the representation
	 */
	private String typeScopes = null;


	/**
	 * Strategy used for checking fix candidates. By default, the strategy is
	 * TACO based bounded verification.
	 */
	private SuccessCheckStrategy checkStrategy = new TacoSuccessCheckStrategy();

	/**
	 * Constructor of StrykerRepairSearchProblem. It receives a JML program to fix, and the name of the
	 * method to fix in the program/class.
	 * @param programToFix is the JML program containing the method to fix
	 * @param methodToFix is the name of the method to fix.
	 */
	public StrykerRepairSearchProblem(JMLAnnotatedClass programToFix, String methodToFix) {
		if (programToFix==null) throw new IllegalArgumentException("no program to fix");
		if (methodToFix==null || methodToFix.isEmpty()) throw new IllegalArgumentException("no method to fix");
		if (!programToFix.hasMethod(methodToFix)) throw new IllegalArgumentException("class " + programToFix.getClassName() + " doesn't have method " + methodToFix);
		this.classToFix = programToFix;
		this.methodToFix = methodToFix;
		this.relevantClasses = new String[]{programToFix.getClassName()};
		Properties overridingProperties = TacoAPI.getInstance().getOverridingProperties();
		overridingProperties.put("classToCheck",initialState().program.getClassName());//.getClassNameAsPath());
		overridingProperties.put("methodToCheck",this.methodToFix+"_0");
		overridingProperties.put("jmlParser.sourcePathStr", StrykerConfig.getInstance().getCompilingSandbox());
		overridingProperties.put("relevantClasses",mergedRelevantClasses());
		overridingProperties.put("relevancyAnalysis", true);
		overridingProperties.put("useJavaArithmetic", false);
		overridingProperties.put("checkArithmeticException", false);
	}
	
	/**
	 * Constructor of StrykerRepairSearchProblem. It receives a JML program to fix, the name of the
	 * method to fix in the program/class, and the list of classes the JML program depends on.
	 * @param programToFix is the JML program containing the method to fix.
	 * @param methodToFix is the name of the method to fix.
	 * @param dependencies is the list (as an array) of dependencies of the program to fix.
	 */
	public StrykerRepairSearchProblem(JMLAnnotatedClass programToFix, String methodToFix, String[] dependencies) {
		this(programToFix, methodToFix);
		this.relevantClasses = new String[dependencies.length + 1];
		this.relevantClasses[0] = programToFix.getClassName();
		System.arraycopy(dependencies, 0, this.relevantClasses, 1, dependencies.length);
		Properties overridingProperties = TacoAPI.getInstance().getOverridingProperties();
		overridingProperties.put("relevantClasses",mergedRelevantClasses());
	}
	
	/**
	 * Returns the initial fix candidate, to start a search for a program fix.
	 * @return the received JML program, as a fix candidate (coming from no mutation).
	 */
	public FixCandidate initialState() {
		if (classToFix==null) throw new IllegalStateException("program to fix not set in stryker search problem");
		if (this.initialState == null) this.initialState = (new FixCandidate(this.classToFix, this.methodToFix));
		return this.initialState;
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
		MuJavaAPI mjAPI = MuJavaAPI.getInstance();
		return mjAPI.generateMutants(s, methodToFix);
	}

	/**
	 * Decides whether a given fix candidate is a successful repair or not. To decide it,
	 * a particular success check strategy is used (this part of the design employs the Strategy design
	 * pattern). By default, TACO is called for bounded verification of the method to repair against its 
	 * JML specification. RAC+TACO (i.e., checking candidate against collected test inputs before calling TACO)
	 * can also be enabled, using "setRacStrategy()".
	 * If fix candidate does not compile, it considers the candidate unsuccessful. If the fix candidate
	 * leads to a "not yet implemented" exception when calling TACO, it consider the candidate unsuccessful.
	 * @param s is the fix candidate to analyze	
	 * @return whether the fix candidate is a successful repair or not.
	 */
	public boolean isSuccessful(FixCandidate s) {
		return (this.checkStrategy.isSuccessful(s));
	}
	
	/**
	 * Sets the type scopes for the search
	 * @param typeScopes is the type scopes, represented as a string
	 */
	public void setScope(String typeScopes) {
		if (typeScopes==null) throw new IllegalArgumentException("setting null type scope");
		this.typeScopes = typeScopes;
		TacoAPI.getInstance().getOverridingProperties().put("typeScopes", this.typeScopes);
	}

	/**
	 * Sets RAC+TACO as the strategy for checking fix candidates 
	 */
	public void setRacStrategy() {
		this.checkStrategy = new TacoWithRacSuccessCheckStrategy();
	}

	
	/**
	 * @return a {@code String} representation of the relevant classes : {@code String}
	 */
	public String mergedRelevantClasses() {
		String mrc = "";
		for (int d = 0; d < this.relevantClasses.length; d++) {
			mrc += this.relevantClasses[d];
			if (d + 1 < this.relevantClasses.length) {
				mrc += ",";
			}
		}
		return mrc;
	}


}

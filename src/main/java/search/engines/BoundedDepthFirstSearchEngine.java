package search.engines;

import java.util.Stack; // necessary due to the use of lists.

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import search.AbstractSearchProblem;
import search.State;

/**
 * This class implements a bounded depth-first search strategy
 * iteratively. It can be used with any instance of AbstractSearchProblem.
 * @author Nazareno Aguirre
 * @version 0.1
 */
public class BoundedDepthFirstSearchEngine<S extends State, Problem extends AbstractSearchProblem<S>> extends AbstractBoundedSearchEngine<S,Problem> {

	private int visited;
	private int bound = 3; 
	private S solutionFound;
	
	private Stack<Pair<S, Integer>> opened; // stores opened states and their corresponding depth.

	/** 
	 * Constructor for class DepthFirstEngine.  
	 * @pre. true.
	 * @post. Lists visited and path are initialised as empty.
	 */	
	public BoundedDepthFirstSearchEngine() {
		super();
		visited = 0;
		solutionFound = null;
		opened = new Stack<Pair<S, Integer>>();
	}

	/** 
	 * Constructor for class DepthFirstEngine.
	 * @param p is the search problem associated with the engine
	 * being created.
	 * @pre. p!=null.
	 * @post. A reference to p is stored in field problem. Lists visited and
	 * path are initialised as empty.
	 */	
	public BoundedDepthFirstSearchEngine(Problem p) {
		super(p);
		if (p==null) throw new IllegalArgumentException("creating engine on a null problem");
		visited = 0;
		solutionFound = null;
		opened = new Stack<Pair<S,Integer>>();
	}

	/** 
	 * Constructor for class DepthFirstEngine.
	 * @param p is the search problem associated with the engine
	 * being created.
	 * @param maxDepth is the maximum depth to be explored in the bounded dfs.
	 * @pre. p!=null.
	 * @post. A reference to p is stored in field problem. Lists visited and
	 * path are initialised as empty.
	 */	
	public BoundedDepthFirstSearchEngine(Problem p, int maxDepth) {
		super(p);
		if (p==null) throw new IllegalArgumentException("creating engine on a null problem");
		if (maxDepth<0) throw new IllegalArgumentException("invalid max depth");
		visited = 0;
		bound = maxDepth;
		solutionFound = null;
		opened = new Stack<Pair<S,Integer>>();
	}

	
	
	/**
	 * Sets the maximum depth, or bound, for the bounded depth first search
	 * @param depth is new depth to be used for bounded DFS.
	 */
	public void setMaxDepth(int depth) {
		this.bound = depth;
	}

	/** 
	 * Starts the search for successful states for problem, following a 
	 * bounded depth-first strategy.
	 * @return true iff a successful state is found in depth smaller than bound.
	 * @pre. problem!=null.
	 * @post. the bounded dfs search is performed, and the result of the search is returned. 	 
	 */
	public boolean performSearch() {
		this.solutionFound = null;
		if (this.problem==null) throw new IllegalStateException("initiating search on a null problem");
		// we get the initial state
		S initialState = problem.initialState();
		// we initialise the stack of opened states
		opened.clear();
		opened.push(new ImmutablePair<S,Integer>(initialState, 0));
		// we initiate the search
		return iterativeDepthFirst();
	} 

	/** 
	 * Method that performs the search implementing a depth-first visit 
	 * recursively.
	 * @return true iff a successful state is found within provided bounds.
	 * @pre. bound>=0 && opened!=null && problem!=null
	 * @post. the bounded dfs is performed, and true is returned iff a successful state is found.
	 */	
	private boolean iterativeDepthFirst() {
		if (opened==null) throw new IllegalStateException("calling iterative dfs on a null stack of opened states");
		boolean found = false;
		while (!opened.isEmpty() && !found) {
			Pair<S,Integer> current = opened.pop();
			S currState = current.getLeft();
			int currDepth = current.getRight();
			if (currDepth<=this.bound) {
				// state is within bounds. It must be treated.
				visited++;
				if (problem.isSuccessful(currState)) {
					found = true;
					this.solutionFound = currState;
				}
				else {
					// we only push children of curr if curr is not at the
					// last level to treat
					if (currDepth<this.bound) {
						for (S s: problem.getSuccessors(currState)) {
							Pair<S, Integer> child = new ImmutablePair<S, Integer>(s, currDepth+1);
							opened.push(child);
						}						
					}
				}
			}
		}
		return found;
	} 

	/**
	 * Returns the solution found in the last performed search. If search was unsuccessful, then this
	 * method should not be called.
	 * @return the solution found in the last performed search.
	 */
	public S getSolution() {
		if (this.solutionFound==null) throw new IllegalStateException("getSolution() can only be called if search was successful.");
		return this.solutionFound;
	}
	
	
	/** 
	 * Reports information regarding a previously executed search.   
	 * @pre. performSearch() has been executed and finished.
	 * @post. A report regarding the search is shown as a string.
	 * This report consists simply of the number of visited states.
	 */    
	public String report() {
		return("Number of visited states: "+visited);

	}

	

} 

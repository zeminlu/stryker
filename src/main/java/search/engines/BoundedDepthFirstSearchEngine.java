package search.engines;
/**
 * Title:        DepthFirstEngine<p>
 * Description:  Class DepthFirstEngine implements a depth-first search strategy
                 which can be used with any instance of AbstractSearchProblem. <p>
 * @author Nazareno Aguirre
 * @version 0.3
 */


import java.util.*; // necessary due to the use of lists.

import search.AbstractSearchProblem;
import search.State;

public class BoundedDepthFirstSearchEngine<S extends State, Problem extends AbstractSearchProblem<S>> extends AbstractSearchEngine<S,Problem> {

	private int visited;
	private static final int bound = 3; 

	/** 
	 * Constructor for class DepthFirstEngine.  
	 * @pre. true.
	 * @post. Lists visited and path are initialised as empty.
	 */	
	public BoundedDepthFirstSearchEngine() {
		super();
		visited = 0;
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
	}


	/** 
	 * Starts the search for successful states for problem, following a 
	 * depth-first strategy.
	 * @return true iff a successful state is found.
	 * @pre. problem!=null.
	 * @post. the search is performed, the visited are stored in
	 * list visited, the path in list path, and true is returned iff a       
	 * successfull state is found.	 
	 */
	public boolean performSearch() {
		if (this.problem==null) throw new IllegalStateException("initiating search on a null problem");
		// we get the initial state
		S initialState = problem.initialState();

		// now we call a recursive method implementing depth-first
		int depth = bound;
		boolean resultSearch = recursiveDepthFirst(initialState,depth);
		return resultSearch;

	} 

	/** 
	 * Method that performs the search implementing a depth-first visit 
	 * recursively.
	 * @return true iff a successful state is found.
	 * @param s is the state from which dfs is performed
	 * @param depth is the maximum depth to perform dfs (bounded dfs)
	 * @pre. s != null && depth>=0
	 * @post. the search is performed, the visited are stored in
	 * list visited, the path in list path, and true is returned iff a       
	 * successful state is found.	 
	 */	
	private boolean recursiveDepthFirst(S s, int depth) {
		if (s==null || depth<0) throw new IllegalArgumentException("calling dfs on null state or with negative depth");
		visited++;
		if (depth==0 || problem.success(s)) {

			return (problem.success(s));


		} // end then branch
		else {
			List<S> succ_s = problem.getSuccessors(s);
			boolean found = false;
			while ( (!succ_s.isEmpty()) && (!found) )  {

				S child = succ_s.get(0);
				succ_s.remove(0);
				found = recursiveDepthFirst(child,depth-1);

			} // end while
			return found;

		} // end else branch
	} // end of recursiveDepthFirst




	/** 
	 * Reports information regarding a previously executed search.   
	 * @pre. performSearch() has been executed and finished.
	 * @post. A report regarding the search is printed to standard output.
	 * This report consists of the length of the path obtained, and the number
	 * of visited states.
	 */    
	public String report() {
		return("Number of visited states: "+visited);

	} // end of report()


} 

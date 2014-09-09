package search.engines;

import java.util.*;

import search.AbstractSearchProblem;
import search.State;

/**
 * Title:        BreadthFirstEngine<p>
 * Description:  Class BreadthFirstEngine implements a Breadth-first search 
                 strategy which can be used with any instance of 
		         AbstractSearchProblem.<p>
 * @author Nazareno Aguirre & Gaston Scilingo
 * @version 0.3
 */
public class BreadthFirstEngine<S extends State, Problem extends AbstractSearchProblem<S>> extends AbstractSearchEngine<S,Problem> {

	private S goal = null;          // used to store the goal when found		    
	private Queue<S> statesToVisit;
	private int numberEnqueued;         // used to report size of queue

	/** 
	 * Constructor for class BreadthFirstEngine.  
	 * @pre. true.
	 * @post. The engine is initialised as for the superclass.
	 */		
	public BreadthFirstEngine() {
		super();
	}

	/** 
	 * Constructor for class BreadthFirstEngine.
	 * @param p is the search problem associated with the engine
	 * being created.
	 * @pre. p!=null.
	 * @post. A reference to p is stored in field problem. 
	 */		
	public BreadthFirstEngine(Problem p) {
		super(p);
	}

	/** 
	 * Starts the search for successful states for problem, following a 
	 * breadth-first strategy.
	 * @return true iff a successful state is found.
	 * @pre. problem!=null.
	 * @post. the search is performed, the visited are stored in
	 * list visited, the path in list path, and true is returned iff a       
	 * successfull state is found.	 
	 */
	public boolean performSearch() {

		statesToVisit = new LinkedList<S>();
		numberEnqueued = 0; 

		// we get the initial state
		S initialState = problem.initialState();

		statesToVisit.offer(initialState);
		numberEnqueued++;


		boolean found = false;

		while ((!statesToVisit.isEmpty())&&(!found)) {
			S current = statesToVisit.remove();
			numberEnqueued--;

			if (problem.success(current)) {

				goal = current;
				found = true;

			} 
			else {

				List<S> succ_s = problem.getSuccessors(current);
				while (!succ_s.isEmpty()) {
					S child = succ_s.get(0);
					if (!statesToVisit.contains(child)) {
						statesToVisit.offer(child);
						numberEnqueued++;
					}
					succ_s.remove(0);
				} 
			} 

		} 

		return found;

	} 


	/** 
	 * Reports information regarding a previously executed search.   
	 * @pre. performSearch() has been executed and finished.
	 * @post. A report regarding the search is printed to standard output.
	 * This report consists of the length of the path obtained, the number
	 * of visited states, and the number of elements in the queue of states to be
	 * visited when search terminated.
	 */    
	public String report() {
		return("Elements in queue when search finished: "+numberEnqueued);

	} 

} 

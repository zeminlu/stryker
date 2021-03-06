package search.engines;
/**
 * Title:        AbstractSearchEngine
 * Description:  class AbstractSearchEngine: abstract class which defines the basic 
 * elements that are required for a search engine. Different search strategies 
 * should extend this class.  
 * @author Nazareno Aguirre & Gaston Scilingo
 * @version 0.3
 */

import search.AbstractSearchProblem;
import search.State;
// necessary for the use of lists

abstract public class AbstractBoundedSearchEngine<S extends State, Problem extends AbstractSearchProblem<S>> extends AbstractSearchEngine<S, Problem>{
	
    // a reference to the problem to apply search to
    protected Problem problem;
    
	/** 
	 * Constructor for abstract class AbstractSearchEngine 
	 * @pre. true.
	 * @post. This constructor does nothing (skip).
	 */
	public AbstractBoundedSearchEngine() {};
    
	/** 
	 * Constructor for abstract class AbstractSearchEngine.
	 * @param p is the abstract search problem associated with the engine
	 * being created.
	 * @pre. p!=null.
	 * @post. A reference to p is stored in field problem.
	 */	
    public AbstractBoundedSearchEngine(Problem p) {
        problem = p;
    }
    
	/** 
	 * Starts the search for successful states for problem.
	 * @return true iff a successful state is found.
	 * @pre. problem!=null.
	 * @post. Search is started
	 * inicializa numItems en 0.  
	 */
    abstract public boolean performSearch();
    
	/** 
	 * Reports information regarding a previously executed search.
	 * @return a report of the performed search, as a string.   
	 * @pre. performSearch() has been executed and finished.
	 * @post. A report regarding the search is printed to standard output.
	 */
    abstract public String report();

    
	/** 
	 * Sets the problem associated with the search engine.
	 * @param p is the search problem to be used for search (to set 'problem' to).
	 * @pre. p!=null.
	 * @post. 'problem' is set to p.
	 */	
    public void setProblem(Problem p) {
        problem = p;
    }
	
    abstract public S getSolution();
    
	/**
	 * Sets the maximum depth, or bound, for the bounded search
	 * @param depth is new depth to be used for bounded search strategy.
	 */
    abstract public void setMaxDepth(int maxDepth);
} 
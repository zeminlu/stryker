package search.engines;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import search.AbstractSearchProblem;
import search.State;
/**
 * Unit tests for search engine BoundedBreadthFirstSearchEngine.
 * @author aguirre
 *
 */
public class BoundedBreadthFirstSearchEngineTest {

	/**
	 * IntState is a simple search state represented simply as an integer number.
	 * @author aguirre
	 *
	 */
	public class IntState implements State {
		
		private int value;  // value of current state
		
		/**
		 * Constructor of IntState. Sets value of state to the parameter
		 * @param value is the value to set the state.
		 */
		public IntState(int value) {
			this.value = value;
		}

		/**
		 * Returns the value of the current state.
		 * @return value of the current state.
		 */
		public int getValue() {
			return value;
		}
		
		/**
		 * Indicates whether two states are equal or not.
		 */
		public boolean equals(State other) {
			if (!(other instanceof IntState)) return false;
			IntState otherIntState = (IntState) other;
			return (this.value==otherIntState.value);
		}
		
	}
	
	/**
	 * Simple search problem defined for testing purposes.
	 * The search problem simply consists in searching for an integer
	 * value in a state space of integers. Initial state is 0.
	 * Successors of a state i are (i+1) and (i+2). A state is successful if it's
	 * equal to the goal of the problem (set with the constructor).
	 * @author aguirre
	 *
	 */
	public class FindIntProblem implements AbstractSearchProblem<IntState> {
		
		private int goal; // value to search for.

		/**
		 * Constructor for FindIntProblem. Sets the value to search for.
		 * @param goal is the value to search for.
		 */
		public FindIntProblem(int goal) {
			this.goal = goal;
		}

		/**
		 * Returns the initial state of the problem. In this case
		 * the initial state is zero.		
		 */
		public IntState initialState() {
			return new IntState(0);
		}

		/**
		 * Returns the successors of an initial state. In this case, the successors
		 * of a value i are (i+1) and (i+2).
		 */
		public List<IntState> getSuccessors(IntState s) {
			List<IntState> children = new LinkedList<IntState>();
			children.add(new IntState(s.getValue()+1));
			children.add(new IntState(s.getValue()+2));
			return children;
		}

		/**
		 * It decides whether a state is successful or not, comparing it with
		 * the goal of the problem. Returns true iff s is the goal
		 */
		public boolean isSuccessful(IntState s) {
			return (this.goal==s.getValue());
		}
		
		/**
		 * It sets the goal of the problem.
		 * @param goal is the new value to search for.
		 */
		public void setGoal(int goal) {
			this.goal = goal;
		}
		
	}
	
	/**
	 * Search for 0 up to depth 0, starting from 0. Should succeed!
	 */
	@Test
	public void testBoundedBfsWithNilBoundsSuccess() {
		FindIntProblem problem = new FindIntProblem(0);
		BoundedBreadthFirstSearchEngine<IntState, FindIntProblem> engine = 
				new BoundedBreadthFirstSearchEngine<IntState, FindIntProblem>();
		engine.setProblem(problem);
		engine.setMaxDepth(0);
		assertTrue("number 0 should be found in depth 0", engine.performSearch());
	}

	/**
	 * Search for 1 up to depth 0, starting from 0. Should fail!
	 */
	@Test
	public void testBoundedBfsWithNilBoundsFail() {
		FindIntProblem problem = new FindIntProblem(1);
		BoundedBreadthFirstSearchEngine<IntState, FindIntProblem> engine = 
				new BoundedBreadthFirstSearchEngine<IntState, FindIntProblem>();
		engine.setProblem(problem);
		engine.setMaxDepth(0);
		assertFalse("number 1 should not be found in depth 0", engine.performSearch());
	}

	/**
	 * Search for 2 up to depth 1, starting from 0. Should succeed!
	 */
	@Test
	public void testBoundedBfsSuccessWithinBounds() {
		FindIntProblem problem = new FindIntProblem(2);
		BoundedBreadthFirstSearchEngine<IntState, FindIntProblem> engine = 
				new BoundedBreadthFirstSearchEngine<IntState, FindIntProblem>();
		engine.setProblem(problem);
		engine.setMaxDepth(1);
		assertTrue("number 2 should be found in depth 1", engine.performSearch());
	}

	/**
	 * Search for 2 up to depth 0, starting from 0. Should fail!
	 */
	@Test
	public void testBoundedBfsSuccessOutsideBounds() {
		FindIntProblem problem = new FindIntProblem(2);
		BoundedBreadthFirstSearchEngine<IntState, FindIntProblem> engine = 
				new BoundedBreadthFirstSearchEngine<IntState, FindIntProblem>();
		engine.setProblem(problem);
		engine.setMaxDepth(0);
		assertFalse("number 2 should be not be found up to depth 0", engine.performSearch());
	}

	/**
	 * Search for 20 up to depth 10, starting from 0. Should succeed!
	 */
	@Test
	public void testBoundedBfsSuccessInsideBiggerBounds() {
		FindIntProblem problem = new FindIntProblem(20);
		BoundedBreadthFirstSearchEngine<IntState, FindIntProblem> engine = 
				new BoundedBreadthFirstSearchEngine<IntState, FindIntProblem>();
		engine.setProblem(problem);
		engine.setMaxDepth(10);
		assertTrue("number 20 should be found (search up to depth 10)", engine.performSearch());
	}

	/**
	 * Search for 21 up to depth 10, starting from 0. Should fail!
	 */
	@Test
	public void testBoundedBfsSuccessOutsideBiggerBounds() {
		FindIntProblem problem = new FindIntProblem(21);
		BoundedBreadthFirstSearchEngine<IntState, FindIntProblem> engine = 
				new BoundedBreadthFirstSearchEngine<IntState, FindIntProblem>();
		engine.setProblem(problem);
		engine.setMaxDepth(10);
		assertFalse("number 21 should not be found (search up to depth 10)", engine.performSearch());
	}
	
	
}

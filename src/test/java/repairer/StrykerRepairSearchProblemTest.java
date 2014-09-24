package repairer;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StrykerRepairSearchProblemTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	
	@SuppressWarnings("unused")
	@Test
	public void testGetSuccessors_nullJmlProgram() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("no program to fix");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(null, "method");
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testGetSuccessors_nullMethod() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("no method to fix");
		JmlProgram program = new JmlProgram("src/test/resources/java/", "SimpleClass");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, null);
	}
	
	@SuppressWarnings("unused")
	// @Test
	public void testGetSuccessors_emptyMethod() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("no method to fix");
		JmlProgram program = new JmlProgram("src/test/resources/java/", "SimpleClass");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "");
	}
	
	@Test
	public void testGetSuccessors_initialStateIsOriginal() {
		JmlProgram program = new JmlProgram("src/test/resources/java/", "SimpleClass");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "nonExistenceMethod");
		FixCandidate initialState = problem.initialState();
		assertTrue("initial state is the original program", Arrays.equals(program.getMd5Digest(), initialState.program.getMd5Digest()));
	}
	
	@Test
	public void testGetSuccessors_inexistentMethodToMutate() {
		JmlProgram program = new JmlProgram("src/test/resources/java/", "SimpleClass");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "nonExistenceMethod");
		List<FixCandidate> successors = problem.getSuccessors(problem.initialState());
		assertTrue("no successors generated", successors.isEmpty());
	}
	
	@Test
	public void testGetSuccessors_noMutGenLimitSet() {
		JmlProgram program = new JmlProgram("src/test/resources/java/", "TestClass_1");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "method");
		List<FixCandidate> successors = problem.getSuccessors(problem.initialState());
		assertTrue("no successors generated", successors.isEmpty());
	}
	
	@Test
	public void testGetSuccessors_mutGenLimitSetWith0() {
		JmlProgram program = new JmlProgram("src/test/resources/java/", "TestClass_3");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "method");
		List<FixCandidate> successors = problem.getSuccessors(problem.initialState());
		assertTrue("no successors generated", successors.isEmpty());
	}
	
	@Test
	public void testGetSuccessors_mutGenLimitSet_onlyOneGeneration() {
		JmlProgram program = new JmlProgram("src/test/resources/java/", "TestClass_2");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "method");
		List<FixCandidate> successors = problem.getSuccessors(problem.initialState());
		assertTrue("successors generated", !successors.isEmpty());
	}
	
	@Test
	public void testGetSuccessors_mutGenLimitSet_onlyOneGeneration_secondGenerationIsEmpty() {
		JmlProgram program = new JmlProgram("src/test/resources/java/", "TestClass_2");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "method");
		List<FixCandidate> successors = problem.getSuccessors(problem.initialState());
		assertTrue("successors generated", !successors.isEmpty());
		for (FixCandidate fc : successors) {
			List<FixCandidate> secondGenSuccessors = problem.getSuccessors(fc);
			assertTrue("second gen successors is empty", secondGenSuccessors.isEmpty());
		}
	}

}

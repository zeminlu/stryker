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
	public void testGetSuccessors_nullJMLAnnotatedClass() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("no program to fix");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(null, "method");
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testGetSuccessors_nullMethod() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("no method to fix");
		JMLAnnotatedClass program = new JMLAnnotatedClass("src/test/resources/java/", "utils.SimpleClass");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, null);
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testGetSuccessors_emptyMethod() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("no method to fix");
		JMLAnnotatedClass program = new JMLAnnotatedClass("src/test/resources/java/", "utils.SimpleClass");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "");
	}
	
	@Test
	public void testGetSuccessors_initialStateIsOriginal() {
		JMLAnnotatedClass program = new JMLAnnotatedClass("src/test/resources/java/", "utils.SimpleClass");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "getX");
		FixCandidate initialState = problem.initialState();
		assertTrue("initial state is the original program", Arrays.equals(program.getMd5Digest(), initialState.program.getMd5Digest()));
	}
	
	@Test
	public void testGetSuccessors_inexistentMethodToMutate() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("class " + "utils.SimpleClass" + " doesn't have method " + "nonExistenceMethod");
		JMLAnnotatedClass program = new JMLAnnotatedClass("src/test/resources/java/", "utils.SimpleClass");
		@SuppressWarnings("unused")
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "nonExistenceMethod");
	}
	
	@Test
	public void testGetSuccessors_noMutGenLimitSet() {
		JMLAnnotatedClass program = new JMLAnnotatedClass("src/test/resources/java/", "TestClass_1");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "method");
		List<FixCandidate> successors = problem.getSuccessors(problem.initialState());
		assertTrue("no successors generated", successors.isEmpty());
	}
	
	@Test
	public void testGetSuccessors_mutGenLimitSetWith0() {
		JMLAnnotatedClass program = new JMLAnnotatedClass("src/test/resources/java/", "TestClass_3");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "method");
		List<FixCandidate> successors = problem.getSuccessors(problem.initialState());
		assertTrue("no successors generated", successors.isEmpty());
	}
	
	@Test
	public void testGetSuccessors_mutGenLimitSet_onlyOneGeneration() {
		JMLAnnotatedClass program = new JMLAnnotatedClass("src/test/resources/java/", "TestClass_2");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "method");
		List<FixCandidate> successors = problem.getSuccessors(problem.initialState());
		assertTrue("successors generated", !successors.isEmpty());
	}
	
	@Test
	public void testGetSuccessors_mutGenLimitSet_onlyOneGeneration_secondGenerationIsEmpty() {
		JMLAnnotatedClass program = new JMLAnnotatedClass("src/test/resources/java/", "TestClass_2");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "method");
		List<FixCandidate> successors = problem.getSuccessors(problem.initialState());
		assertTrue("successors generated", !successors.isEmpty());
		for (FixCandidate fc : successors) {
			List<FixCandidate> secondGenSuccessors = problem.getSuccessors(fc);
			assertTrue("second gen successors is empty", secondGenSuccessors.isEmpty());
		}
	}
	
	@Test
	public void testGetSuccessors_mutationsList() {
		JMLAnnotatedClass program = new JMLAnnotatedClass("src/test/resources/java/", "utils.SimpleClass");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "twicePlusOne");
		List<FixCandidate> successors = problem.getSuccessors(problem.initialState());
		assertTrue("successors generated", !successors.isEmpty());
		for (FixCandidate fc : successors) {
			assertTrue("first candidates have only one mutation", fc.getMutations().size() == 1);
			List<FixCandidate> secondGenSuccessors = problem.getSuccessors(fc);
			for (FixCandidate sfc : secondGenSuccessors) {
				assertTrue("second candidates have two mutations", sfc.getMutations().size() == 2);
				int fcFirstMutOrigID = fc.getMutations().get(0).getOriginal().getObjectID();
				int fcFirstMutMutID = fc.getMutations().get(0).getMutant().getObjectID();
				int scFirstMutOrigID = sfc.getMutations().get(0).getOriginal().getObjectID();
				int scFirstMutMutID = sfc.getMutations().get(0).getMutant().getObjectID();
				assertTrue("second candidates first mutation is first mutation of first candidate", fcFirstMutOrigID == scFirstMutOrigID && fcFirstMutMutID == scFirstMutMutID);
			}
		}
	}
	
	@Test
	public void testMergedRelevantClasses_emptyDependencies() {
		String[] dependencies = new String[]{};
		JMLAnnotatedClass program = new JMLAnnotatedClass("src/test/resources/java/", "utils.SimpleClass");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "twicePlusOne", dependencies);
		assertTrue("merged relevant classes is correct", problem.mergedRelevantClasses().compareTo("")==0);
	}
	
	@Test
	public void testMergedRelevantClasses_nonEmptyDependencies() {
		String[] dependencies = new String[]{"a.b.Clase1", "a.Main", "a.b.util.Pair"};
		JMLAnnotatedClass program = new JMLAnnotatedClass("src/test/resources/java/", "utils.SimpleClass");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(program, "twicePlusOne", dependencies);
		assertTrue("merged relevant classes is correct", problem.mergedRelevantClasses().compareTo("a.b.Clase1,a.Main,a.b.util.Pair")==0);
	}

}

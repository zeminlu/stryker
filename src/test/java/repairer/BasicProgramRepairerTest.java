package repairer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BasicProgramRepairerTest {

	
	@Test
	public void emptyRelevantClasses() {
		String[] relevantClasses = new String[]{};
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "setX", relevantClasses, 0);
		assertTrue("BasicProgramRepairer only have the class to repair in relevant classes", repairer.getClassesDependencies().length == 1 && repairer.getClassesDependencies()[0].compareTo("SimpleClass")==0);
	}
	
	@Test
	public void nonEmptyRelevantClasses() {
		String[] relevantClasses = new String[]{"a.b.Clase1", "a.Main", "a.b.util.Pair"};
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "setX", relevantClasses, 0);
		boolean relevantClassesSizeIsCorrect = repairer.getClassesDependencies().length == 4;
		boolean relevantClassesContentIsCorrect = true;
		String[] classesToSearch = new String[]{subject.getClassName(), "a.b.Clase1", "a.Main", "a.b.util.Pair"};
		for (String cts : classesToSearch) {
			boolean found = false;
			for (String c : repairer.getClassesDependencies()) {
				if (c.compareTo(cts)==0) {
					found = true;
					break;
				}
			}
			if (!found) {
				relevantClassesContentIsCorrect = false;
				break;
			}
		}
		assertTrue("BasicProgramRepairer relevant classes size and content is correct", relevantClassesSizeIsCorrect && relevantClassesContentIsCorrect);
	}
	
	/**
	 * Tests that attempts to repair a very simple correct program
	 * Running repair only up to depth 0 (only the initial candidate considered).  
	 * Repair must return true, indicating the program is (trivially) repaired.
	 */
	@Test
	public void programRepairWithSimpleCorrectMethod() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "setX", 0);
		boolean isRepaired = repairer.repair();
		assertTrue("method cannot be repaired", isRepaired);
	}

	/**
	 * Tests that attempts to repair a very simple correct program
	 * Running repair only up to depth 0 (only the initial candidate considered), using BFS.  
	 * Repair must return true, indicating the program is (trivially) repaired.
	 */
	@Test
	public void programRepairWithSimpleCorrectMethodInBfs() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "setX", 0);
		repairer.setBfsStrategy();
		boolean isRepaired = repairer.repair();
		assertTrue("method cannot be repaired", isRepaired);
	}

	
	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 0 (only the initial candidate considered).  
	 * Repair must return false, indicating the program cannot be repair (up to depth 0).
	 */
	@Test
	public void programRepairWithSimpleIncorrectMethod() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "decX", 0);
		boolean isRepaired = repairer.repair();
		assertFalse("method cannot be repaired", isRepaired);
	}

	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 0 (only the initial candidate considered) using BFS.  
	 * Repair must return false, indicating the program cannot be repair (up to depth 0).
	 */
	@Test
	public void programRepairWithSimpleIncorrectMethodInBfs() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "decX", 0);
		repairer.setBfsStrategy();
		boolean isRepaired = repairer.repair();
		assertFalse("method cannot be repaired", isRepaired);
	}

	
	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 1.  
	 * Repair must return true, indicating the program can be repaired with a single mutation.
	 */
	@Test
	public void programRepairWithSimpleIncorrectMethodDepthOne() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "decX", 1);
		boolean isRepaired = repairer.repair();
		assertTrue("method can be repaired", isRepaired);
	}

	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 1, using BFS.  
	 * Repair must return true, indicating the program can be repaired with a single mutation.
	 */
	@Test
	public void programRepairWithSimpleIncorrectMethodDepthOneInBfs() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "decX", 1);
		repairer.setBfsStrategy();
		boolean isRepaired = repairer.repair();
		assertTrue("method can be repaired", isRepaired);
	}

	
	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 2.  
	 * Repair must return true, indicating the program can be repaired with two mutations.
	 */
	@Test
	public void programRepairWithSimpleIncorrectMethodDepthTwo() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "twicePlusOne", 2);
		boolean isRepaired = repairer.repair();
		assertTrue("method can be repaired", isRepaired);
	}

	
	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 2, using BFS.
	 * Repair must return true, indicating the program can be repaired with two mutations.
	 */
	@Test
	public void programRepairWithSimpleIncorrectMethodDepthTwoInBfs() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "twicePlusOne", 2);
		repairer.setBfsStrategy();
		boolean isRepaired = repairer.repair();
		assertTrue("method can be repaired", isRepaired);
	}

	
	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 1. Program requires 2 modifications to be repaired.  
	 * Repair must return false, indicating the program cannot be repaired with up to one mutation.
	 */
	@Test
	public void programRepairWithSimpleUnrepairableIncorrectMethodDepthOne() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "twicePlusOne", 1);
		boolean isRepaired = repairer.repair();
		assertFalse("method cannot be repaired", isRepaired);
	}

	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 1, in BFS. Program requires 2 modifications to be repaired.  
	 * Repair must return false, indicating the program cannot be repaired with up to one mutation.
	 */
	@Test
	public void programRepairWithSimpleUnrepairableIncorrectMethodDepthOneInBfs() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "twicePlusOne", 1);
		repairer.setBfsStrategy();
		boolean isRepaired = repairer.repair();
		assertFalse("method cannot be repaired", isRepaired);
	}

	
	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 3.  
	 * Repair must return true, indicating the program can be repaired with three mutations.
	 */
	@Test
	public void programRepairWithSimpleIncorrectMethodDepthThree() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "altTwicePlusOne", 3);
		boolean isRepaired = repairer.repair();
		assertTrue("method can be repaired", isRepaired);
	}

	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 3, using BFS.  
	 * Repair must return true, indicating the program can be repaired with three mutations.
	 */
	@Test
	public void programRepairWithSimpleIncorrectMethodDepthThreeInBfs() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "altTwicePlusOne", 3);
		repairer.setBfsStrategy();
		boolean isRepaired = repairer.repair();
		assertTrue("method can be repaired", isRepaired);
	}
	
	@Test
	public void programRepairWithSimpleIncorrectMethodWithDepthOne_usingDependencies_multikeymap() {
		String sourceFolder = "src/test/resources/java/";
		String[] dependencies = new String[]{"examples.stryker.multikeymap.MultiKeyMap", "examples.stryker.multikeymap.MultiKey", "examples.stryker.multikeymap.HashEntry"};
		JMLAnnotatedClass subject = new JMLAnnotatedClass(sourceFolder, "examples/stryker/multikeymap/MultiKeyMap");
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "isEqualKey", dependencies, 1);
		boolean isRepaired = repairer.repair();
		assertTrue("method can be repaired", isRepaired);
	}


}

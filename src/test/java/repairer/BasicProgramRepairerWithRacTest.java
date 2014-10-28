package repairer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import config.StrykerConfig;

public class BasicProgramRepairerWithRacTest {

	
	@Before
	public void setUp() {
		StrykerConfig.getInstance().resetCompilingSandbox();
	}
	
	/**
	 * When an empty set of relevant classes is provided, only the class containing the method to repair 
	 * is considered.
	 */
	@Test
	public void emptyRelevantClasses() {
		String[] relevantClasses = new String[]{};
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");
		PrivateStryker repairer = new PrivateStryker(subject, "getX", relevantClasses, 0);
		repairer.enableRac();
		assertTrue("Only the class to repair is relevant", repairer.getClassesDependencies().length == 1 && repairer.getClassesDependencies()[0].compareTo("SimpleClass")==0);
	}
	
	/**
	 * When relevant classes are passed, these are correctly taken into account.
	 */
	@Test
	public void nonEmptyRelevantClasses() {
		String[] relevantClasses = new String[]{"a.b.Clase1", "a.Main", "a.b.util.Pair"};
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");
		PrivateStryker repairer = new PrivateStryker(subject, "setX", relevantClasses, 0);
		repairer.enableRac();
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
		assertTrue("number and contents of relevant classes is correct", relevantClassesSizeIsCorrect && relevantClassesContentIsCorrect);
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
		PrivateStryker repairer = new PrivateStryker(subject, "getX", 0);
		repairer.enableRac();
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
		PrivateStryker repairer = new PrivateStryker(subject, "setX", 0);
		repairer.setBfsStrategy();
		repairer.enableRac();
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
		PrivateStryker repairer = new PrivateStryker(subject, "decX", 0);
		repairer.enableRac();
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
		PrivateStryker repairer = new PrivateStryker(subject, "decX", 0);
		repairer.setBfsStrategy();
		repairer.enableRac();
		boolean isRepaired = repairer.repair();
		assertFalse("method cannot be repaired", isRepaired);
	}
	
	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 0 (only the initial candidate considered) using BFS.  
	 * Repair must return false, indicating the program cannot be repair (up to depth 0).
	 */
	@Test
	public void programRepairWithSimpleIncorrectMethodInBfs_2() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "utils.SimpleClass");		
		PrivateStryker repairer = new PrivateStryker(subject, "decX", 0);
		repairer.setBfsStrategy();
		repairer.enableRac();
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
		PrivateStryker repairer = new PrivateStryker(subject, "decX", 1);
		repairer.enableRac();
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
		PrivateStryker repairer = new PrivateStryker(subject, "decX", 1);
		repairer.setBfsStrategy();
		repairer.enableRac();
		boolean isRepaired = repairer.repair();
		assertTrue("method can be repaired", isRepaired);
	}

	
	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 2.  
	 * Repair must return true, indicating the program can be repaired with two mutations.
	 */
	//@Test
	public void programRepairWithSimpleIncorrectMethodDepthTwo() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		PrivateStryker repairer = new PrivateStryker(subject, "twicePlusOne", 2);
		repairer.enableRac();
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
		PrivateStryker repairer = new PrivateStryker(subject, "twicePlusOne", 2);
		repairer.setBfsStrategy();
		repairer.enableRac();
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
		PrivateStryker repairer = new PrivateStryker(subject, "twicePlusOne", 1);
		repairer.enableRac();
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
		PrivateStryker repairer = new PrivateStryker(subject, "twicePlusOne", 1);
		repairer.setBfsStrategy();
		repairer.enableRac();
		boolean isRepaired = repairer.repair();
		assertFalse("method cannot be repaired", isRepaired);
	}

	
	/**
	 * Tests that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 3.  
	 * Repair must return true, indicating the program can be repaired with three mutations.
	 */
	// @Test
	public void programRepairWithSimpleIncorrectMethodDepthThree() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		PrivateStryker repairer = new PrivateStryker(subject, "altTwicePlusOne", 3);
		repairer.enableRac();
		boolean isRepaired = repairer.repair();
		assertTrue("method can be repaired", isRepaired);
	}

	/**
	 * Test that attempts to repair a very simple incorrect program
	 * Running repair only up to depth 3, using BFS.  
	 * Repair must return true, indicating the program can be repaired with three mutations.
	 */
	// @Test
	public void programRepairWithSimpleIncorrectMethodDepthThreeInBfs() {
		// extension .java is assumed for programs
		JMLAnnotatedClass subject = new JMLAnnotatedClass("src/test/resources/java/", "SimpleClass");		
		PrivateStryker repairer = new PrivateStryker(subject, "altTwicePlusOne", 3);
		repairer.setBfsStrategy();
		repairer.enableRac();
		boolean isRepaired = repairer.repair();
		assertTrue("method can be repaired", isRepaired);
	}
	
	/**
	 * Attempts to repair a simple program with various dependencies, failing to do so due to the max
	 * depth in the search.
	 */
	// @Test
	public void programRepairWithSimpleIncorrectMethodWithDepthOne_usingDependencies_multikeymap() {
		String sourceFolder = "src/test/resources/java/examples/stryker/multikeymap";
		String[] dependencies = new String[]{"MultiKey", "HashEntry"};
		JMLAnnotatedClass subject = new JMLAnnotatedClass(sourceFolder, "MultiKeyMap");
		PrivateStryker repairer = new PrivateStryker(subject, "equalKey", dependencies, 0);
		repairer.enableRac();
		boolean isRepaired = repairer.repair();
		assertFalse("method cannot be repaired with this depth", isRepaired);
	}

	/**
	 * Attempts to repair a SinglyLinkedList method which depends on other files (node class).
	 * It repairs the program.
	 */
	@Test
	public void programRepairWithSimpleIncorrectMethodWithDepthOne_usingDependencies_singlylinkedlist() {
		String sourceFolder = "src/test/resources/";
		String[] dependencies = new String[]{"SinglyLinkedList", "SinglyLinkedListNode"};
		JMLAnnotatedClass subject = new JMLAnnotatedClass(sourceFolder, "SinglyLinkedList");
		PrivateStryker repairer = new PrivateStryker(subject, "getNode", dependencies, 1);
		repairer.setScope("SinglyLinkedList:3,SinglyLinkedListNode:3");
		repairer.enableRac();
		boolean isRepaired = repairer.repair();
		assertTrue("method can be repaired", isRepaired);
	}


}

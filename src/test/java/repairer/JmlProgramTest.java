package repairer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JmlProgramTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	/**
	 * Tests the creation of a program object with a non existent file. Constructor must throw an 
	 * IllegalArgumentException.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void programCreationWithNonExistentFile() {
		// The following must break if file does not exist!
		new JmlProgram("src/test/resources/java/", "noProgram");
	}

	/**
	 * Tests the creation of a program object with an existent, non Java file. 
	 * Constructor must create the object. Result must be non-compilable.
	 */
	@Test
	public void programCreationWithNonJavaFile() {
		// extension .java is assumed for programs!
		JmlProgram subject = new JmlProgram("src/test/resources/java/", "whatever");
		assertFalse("program does not compile", subject.isValid());
	}

	/**
	 * Tests the creation of a program object with an existent, non Java file. 
	 * Constructor must create the object. Result must be non-compilable.
	 */
	@Test
	public void programCreationWithSimpleJavaFile() {
		// extension .java is assumed for programs
		JmlProgram subject = new JmlProgram("src/test/resources/java/", "SimpleClass");
		assertTrue("program does compile", subject.isValid());
	}
	
	@Test
	public void programHasMethodWithNullMethod() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("method name is null");
		JmlProgram program = new JmlProgram("src/test/resources/java/", "SimpleClass");
		program.hasMethod(null);
	}
	
	@Test
	public void programHasMethodWithEmptyMethod() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("empty method name");
		JmlProgram program = new JmlProgram("src/test/resources/java/", "SimpleClass");
		program.hasMethod("");
	}
	
	@Test
	public void programHasMethodWithNonExistingMethod() {
		JmlProgram program = new JmlProgram("src/test/resources/java/", "SimpleClass");
		assertTrue("SimpleClass doesn't have method spainmordortwoforone", !program.hasMethod("spainmordortwoforone"));
	}
	
	@Test
	public void programHasMethodWithExistingMethod() {
		JmlProgram program = new JmlProgram("src/test/resources/java/", "SimpleClass");
		assertTrue("SimpleClass doesn't have method twicePlusOne", program.hasMethod("twicePlusOne"));
	}

	
}

package repairer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JmlProgramTest {

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

	
}

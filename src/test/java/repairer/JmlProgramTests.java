package repairer;

import static org.junit.Assert.*;

import org.junit.Test;

public class JmlProgramTests {

	/**
	 * Tests the creation of a program object with a non existent file. Constructor must throw an 
	 * IllegalArgumentException.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void programCreationWithNonExistentFile() {
		// The following must break if file does not exist!
		new JmlProgram("src/test/resources/java", "noProgram.java");
	}

	/**
	 * Tests the creation of a program object with an existent, non Java file. 
	 * Constructor must create the object. Result must be non-compilable.
	 */
	@Test
	public void programCreationWithNonJavaFile() {
		JmlProgram subject = new JmlProgram("src/test/resources/java", "whatever.java");
		assertFalse("program does not compile", subject.isValid());
	}

	/**
	 * Tests the creation of a program object with an existent, non Java file. 
	 * Constructor must create the object. Result must be non-compilable.
	 */
	@Test
	public void programCreationWithSimpleJavaFile() {
		JmlProgram subject = new JmlProgram("src/test/resources/java", "simpleClass.java");
		assertTrue("program does compile", subject.isValid());
	}

	
}

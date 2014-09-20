package repairer;

import static org.junit.Assert.*;

import org.junit.Test;

public class BasicProgramRepairerTests {

	@Test
	public void programRepairWithSimpleJavaFile() {
		// extension .java is assumed for programs
		JmlProgram subject = new JmlProgram("src/test/resources/java/", "SimpleClass");		
		BasicProgramRepairer repairer = new BasicProgramRepairer(subject, "getX", 1);
		boolean isRepaired = repairer.repair();
		assertFalse("method cannot be repaired", isRepaired);
	}

}

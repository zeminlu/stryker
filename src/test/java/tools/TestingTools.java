package tools;

import repairer.PrivateStryker;

public class TestingTools {
	
	public static boolean programFixedAsExpected(ProgramData programData, StrykerOptions strykerOptions, String methodToFix, boolean fixExpected) {
		if (!programData.getProgramToFix().hasMethod(methodToFix)) {
			throw new IllegalArgumentException("class " + programData.getProgramToFix().getClassName() + " doesn't declare method " + methodToFix);
		}
		if (!strykerOptions.validate()) {
			throw new IllegalArgumentException("StrykerOptions argument is not valid");
		}
		PrivateStryker repairer;
		if (programData.hasClassDependencies()) {
			repairer = new PrivateStryker(programData.getProgramToFix(), methodToFix, programData.getClassDependencies());
		} else {
			repairer = new PrivateStryker(programData.getProgramToFix(), methodToFix);
		}
		if (strykerOptions.useBFS()) {
			repairer.setBfsStrategy();
		} else if (strykerOptions.useDFS()) {
			repairer.setDfsStrategy();
		}
		if (strykerOptions.useRac()) {
			repairer.enableRac();
		}
		if (!strykerOptions.getScope().isEmpty()) {
			repairer.setScope(strykerOptions.getScope());
		}
		repairer.setMaxDepth(strykerOptions.getMaxDepth());
		return repairer.repair() == fixExpected;
	}

}

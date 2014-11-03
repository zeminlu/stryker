package tools;

import repairer.PrivateStryker;

/**
 * This class allows to test stryker using a simple API
 * 
 * @author Simón Emmanuel Gutiérrez Brida
 * @version 0.1
 * @see ProgramData
 * @see StrykerOptions
 */
public class TestingTools {
	
	/**
	 * Runs stryker and returns if a fix was found as expected
	 * 
	 * @param programData		:	the data of the program to fix	:	{@code ProgramData}
	 * @param strykerOptions	:	stryker settings				:	{@code StrykerOptions}
	 * @param methodToFix		:	the method to fix				:	{@code String}
	 * @param fixExpected		:	if a fix is expected or not		:	{@code boolean}
	 * @return {@code PrivateStryker#repair() == fixExpected}
	 */
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

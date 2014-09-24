package repairer;

import java.util.List;
import java.util.Properties;

import search.AbstractSearchProblem;
import ar.edu.taco.TacoAnalysisResult;
import ar.edu.taco.TacoMain;
import ar.edu.taco.TacoNotImplementedYetException;

public class StrykerRepairSearchProblem implements AbstractSearchProblem<FixCandidate> {

	protected JmlProgram classToFix; // class to fix using Stryker.
	protected String methodToFix; // name of method in class classToFix, that is going to be repaired using Stryker.
	
	
	public StrykerRepairSearchProblem(JmlProgram programToFix, String methodToFix) {
		if (programToFix==null) throw new IllegalArgumentException("no program to fix");
		if (methodToFix==null) throw new IllegalArgumentException("no method to fix");
		this.classToFix = programToFix;
		this.methodToFix = methodToFix;
	}
	
	public FixCandidate initialState() {
		if (classToFix==null) throw new IllegalStateException("program to fix not set in stryker search problem");
		return (new FixCandidate(this.classToFix));
	}

	public List<FixCandidate> getSuccessors(FixCandidate s) {
		MuJavaAPI mjAPI = new MuJavaAPI();
		return mjAPI.generateMutants(s, methodToFix);
	}

	/**
	 * Decides whether a given fix candidate is a successful repair or not. To decide it,
	 * TACO is called for bounded verification of the method to repair against its JML specification.
	 * If fix candidate does not compile, it considers the candidate unsuccessful. If the fix candidate
	 * leads to a "not yet implemented" exception when calling TACO, it consider the candidate unsuccessful.
	 * FIXME This code may not be the best way of calling TACO. It must be improved. So far, only
	 * paths are passed, no other verification parameters are checked.
	 * @param s is the fix candidate to analyze	
	 * @return whether the fix candidate is a successful repair or not.
	 */
	public boolean success(FixCandidate s) {
		if (s==null) throw new IllegalArgumentException("null fix candidate");
		if (s.program==null) throw new IllegalArgumentException("null program in fix candidate");
		if (!s.program.isValid()) return false;
		TacoMain taco = new TacoMain(null);
		Properties overridingProperties = new Properties();
		overridingProperties.put("classToCheck",s.program.className);
		overridingProperties.put("relevantClasses",s.program.className);
		overridingProperties.put("methodToCheck",this.methodToFix+"_0");
		overridingProperties.put("jmlParser.sourcePathStr", s.program.absPath);
		TacoAnalysisResult result = null;
		try {
	
			result = taco.run("genericTest.properties", overridingProperties);
		}
		catch (TacoNotImplementedYetException e) {
			// candidate is well formed JML but taco does not support syntax.
			// considering candidate invalid, for the moment.
			return false;
		} 
		return result.get_alloy_analysis_result().isUNSAT();
	}

}

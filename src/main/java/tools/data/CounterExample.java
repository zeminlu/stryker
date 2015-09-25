package tools.data;

import java.util.List;
import org.multijava.mjc.JCompilationUnitType;

import repairer.FixCandidate;
import tools.apis.ReloaderAPI;
import ar.edu.taco.TacoAnalysisResult;
import ar.edu.taco.engine.SnapshotStage;
import ar.edu.taco.engine.StrykerStage;
import ar.edu.taco.jml.parser.JmlParser;
import ar.edu.taco.junit.RecoveredInformation;

/**
 * This class represents a counterexample built by a SAT solver when returning SAT
 * 
 * @author Nazareno Aguirre
 * @version 2.1
 */
public class CounterExample {
	
	/**
	 * The fix candidate from which this counter example as built
	 */
	private FixCandidate fixCandidate;

	/**
	 * Counter example built by TACO
	 */
	private RecoveredInformation counterexample;
	
	/**
	 * Analysis result from TACO
	 */
	private TacoAnalysisResult analysis_result;
	
	List<JCompilationUnitType> compilation_units = null;
	
	/**
	 * Builds a new instance using a {@code TacoAnalysisResult} object
	 * 
	 * @param analysis_result	:	the analysis result from TACO								:	{@code TacoAnalysisResult}
	 * @param fixCandidate		:	The fix candidate from which this counter example as built	:	{@code FixCandidate}
	 * @throws IllegalArgumentException if the analysis gave sat and the counter example is {@code null} or if the analysis gave unsat and the counter example is not {@code null}
	 */
	public CounterExample(TacoAnalysisResult analysis_result, FixCandidate fixCandidate) {
		this.counterexample = null;
		this.compilation_units = analysis_result.get_alloy_analysis_result().isSAT()?JmlParser.getInstance().getCompilationUnits():null;
		this.analysis_result = analysis_result;
		this.fixCandidate = fixCandidate;
	}
	
	/**
	 * @return {@code true} if a counter example was built by TACO
	 */
	public boolean counterExampleExist() {
		return this.analysis_result.get_alloy_analysis_result().isSAT();
	}
	
	//TODO: comment
	public void refresh() {
		this.counterexample = null;
	}
	
	/**
	 * @return the counter example (if exists) built by TACO or {@code null} if a counter example was not built
	 */
	public RecoveredInformation getRecoveredInformation() {
		if (!this.counterExampleExist()) return null;
		if (this.counterexample != null) return this.counterexample;
		ClassLoader threadLoaderBackup = Thread.currentThread().getContextClassLoader();
        String classToCheck = this.fixCandidate.getProgram().getClassName();
        String methodToCheck = this.fixCandidate.getMethodToFix() + "_0" ;
        ReloaderAPI.getInstance().setReloaderAsThreadClassLoader(Thread.currentThread());
        SnapshotStage snapshotStage = new SnapshotStage(this.compilation_units, this.analysis_result, classToCheck, methodToCheck);
	    snapshotStage.execute();
	    RecoveredInformation recoveredInformation = snapshotStage.getRecoveredInformation();
	    recoveredInformation.setFileNameSuffix(StrykerStage.fileSuffix);
	    Thread.currentThread().setContextClassLoader(threadLoaderBackup);
	    this.counterexample = recoveredInformation;
		return recoveredInformation;
	}
	
	/**
	 * @return the analysis result from TACO
	 */
	public TacoAnalysisResult getTacoAnalysisResult() {
		return this.analysis_result;
	}
	
	/**
	 * @return the fix candidate from which this counter example as built
	 */
	public FixCandidate getFixCandidate() {
		return this.fixCandidate;
	}
	
	@Override
	public boolean equals(Object o) {
		CounterExample other;
		if (o instanceof CounterExample) {
			other = (CounterExample)o;
		} else {
			if (o == null) return false;
			return o.equals(this);
		}
		boolean sameA4Solution = this.analysis_result.get_alloy_analysis_result().getAlloy_solution().toString().compareTo(other.analysis_result.get_alloy_analysis_result().getAlloy_solution().toString()) == 0;
		boolean sameCommand = this.analysis_result.get_alloy_analysis_result().getCommand().toString().compareTo(other.analysis_result.get_alloy_analysis_result().getCommand().toString()) == 0;
		return sameA4Solution && sameCommand;
//		boolean thisSat = this.analysis_result.get_alloy_analysis_result().isSAT();
//		boolean otherSat = other.analysis_result.get_alloy_analysis_result().isSAT();
//		if (!(thisSat && otherSat)) {
//			return false;
//		}
//		String thisCE = "";
//		for (Entry<String,Object> v : this.counterexample.getSnapshot().entrySet()) {
//			String key = v.getKey();
//			String value = v.getValue().toString();
//			int atIdx = value.indexOf("@");
//			if (atIdx > 0) {
//				value = value.substring(0, atIdx);
//			}
//			thisCE += key + "=" + value + "|";
//		}
//		thisCE = thisCE.substring(0, thisCE.length() - 1);
//		String otherCE = "";
//		for (Entry<String,Object> v : other.counterexample.getSnapshot().entrySet()) {
//			String key = v.getKey();
//			String value = v.getValue().toString();
//			int atIdx = value.indexOf("@");
//			if (atIdx > 0) {
//				value = value.substring(0, atIdx);
//			}
//			otherCE += key + "=" + value + "|";
//		}
//		otherCE = otherCE.substring(0, otherCE.length() - 1);
//		return thisCE.compareTo(otherCE) == 0;
	}
}

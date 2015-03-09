package tools;

import java.util.Map.Entry;

import repairer.FixCandidate;
import ar.edu.taco.TacoAnalysisResult;
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
	
	/**
	 * Builds a new instance using a {@code RecoveredInformation} and a {@code TacoAnalysisResult} object
	 * 
	 * @param counterexample	:	the counter example (if exists) built by TACO				:	{@code RecoveredInformation}
	 * @param analysis_result	:	the analysis result from TACO								:	{@code TacoAnalysisResult}
	 * @param fixCandidate		:	The fix candidate from which this counter example as built	:	{@code FixCandidate}
	 * @throws IllegalArgumentException if the analysis gave sat and the counter example is {@code null} or if the analysis gave unsat and the counter example is not {@code null}
	 */
	public CounterExample(RecoveredInformation counterexample, TacoAnalysisResult analysis_result, FixCandidate fixCandidate) {
		if (analysis_result.get_alloy_analysis_result().isSAT() && counterexample == null) {
			throw new IllegalArgumentException("Taco result is sat but counter example is null");
		}
		if (analysis_result.get_alloy_analysis_result().isUNSAT() && counterexample != null) {
			throw new IllegalArgumentException("Taco result is unsat but counter example is not null");
		}
		this.counterexample = counterexample;
		this.analysis_result = analysis_result;
		this.fixCandidate = fixCandidate;
	}
	
	/**
	 * @return {@code true} if a counter example was built by TACO
	 */
	public boolean counterExampleExist() {
		return this.counterexample != null;
	}
	
	/**
	 * @return the counter example (if exists) built by TACO or {@code null} if a counter example was not built
	 */
	public RecoveredInformation getRecoveredInformation() {
		return this.counterexample;
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
		boolean thisSat = this.analysis_result.get_alloy_analysis_result().isSAT();
		boolean otherSat = other.analysis_result.get_alloy_analysis_result().isSAT();
		if (!(thisSat && otherSat)) {
			return false;
		}
		String thisCE = "";
		for (Entry<String,Object> v : this.counterexample.getSnapshot().entrySet()) {
			String key = v.getKey();
			String value = v.getValue().toString();
			int atIdx = value.indexOf("@");
			if (atIdx > 0) {
				value = value.substring(0, atIdx);
			}
			thisCE += key + "=" + value + "|";
		}
		thisCE = thisCE.substring(0, thisCE.length() - 1);
		String otherCE = "";
		for (Entry<String,Object> v : other.counterexample.getSnapshot().entrySet()) {
			String key = v.getKey();
			String value = v.getValue().toString();
			int atIdx = value.indexOf("@");
			if (atIdx > 0) {
				value = value.substring(0, atIdx);
			}
			otherCE += key + "=" + value + "|";
		}
		otherCE = otherCE.substring(0, otherCE.length() - 1);
		return thisCE.compareTo(otherCE) == 0;
	}
}

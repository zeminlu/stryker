package tools;

import java.nio.file.Path;
import java.util.List;

import repairer.FixCandidate;

/**
 * This class is used to access RAC, the main responsabilities of this API are:
 * 
 * <li> Given a java source file and a JUnit test, run the test on the java source file </li>
 * <li> Given a java source file and a list of JUnit tests, run the tests on the java source file </li>
 * <li> Given a java source file and a counterexample build a JUnit test </li>
 * 
 * TODO: add author
 * 
 * @see FixCandidate
 * @see CounterExample
 * @see Path
 *
 * @version 0.1u
 */
public class RacAPI {
	
	/**
	 * This method takes a {@code FixCandidate} and a {@code CounterExample} and build a JUnit test.
	 *
	 * @param candidate	:	the java source file for which to build the test		:	{@code FixCandidate}
	 * @param ce		:	the counter example that will be used to build the test	:	{@code CounterExample}
	 * 
	 * @return a path to the JUnit test built : {@code Path}
	 */
	public Path buildJUnit(FixCandidate candidate, CounterExample ce) {
		//TODO: implement this method
		throw new UnsupportedOperationException ("RacAPI#buildJUnit(FixCandidate, CounterExample) : not yet implemented");
	}
	
	/**
	 * This method runs a JUnit test for a given {@code FixCandidate}
	 * 
	 * @param candidate	:	the java source code for which the JUnit test will be run	:	{@code FixCandidate}
	 * @param junitTest	:	the path leading to the JUnit test to run					:	{@code Path}
	 * 
	 * @return {@code true} or {@code false} depending if the test passes or not	:	{@code boolean}
	 */
	public boolean runJUnit(FixCandidate candidate, Path junitTest) {
		//TODO: implement this method
		throw new UnsupportedOperationException ("RacAPI#runJUnit(FixCandidate, Path) : not yet implemented");
	}
	
	/**
	 * This method runs a list of JUnit tests for a given {@code FixCandidate}
	 * 
	 * @param candidate		:	the java source code for which the JUnit test will be run	:	{@code FixCandidate}	
	 * @param junitTests	:	the list of paths leading to the JUnit tests to run			:	{@code List<Path>}
	 * 
	 * @return a boolean array representing the result of running each JUnit test	:	{@code boolean[]}
	 */
	public boolean[] runJUnits(FixCandidate candidate, List<Path> junitTests) {
		//TODO: implement this method
		throw new UnsupportedOperationException ("RacAPI#runJUnits(FixCandidate, List<Path>) : not yet implemented");
	}

}

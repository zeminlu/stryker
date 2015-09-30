package repairer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import tools.apis.JavaCompilerAPI;
import tools.apis.RacAPI;
import tools.apis.ReloaderAPI;
import tools.apis.TacoAPI;
import tools.data.CounterExample;
import ar.edu.jdynalloy.JDynAlloySemanticException;
import ar.edu.taco.TacoNotImplementedYetException;
import config.StrykerConfig;

/**
 * This class is a particular implementation of SuccessCheckStrategy. It checks whether a given candidate
 * is a successful fix or not by doing the following:
 * <p>
 * <li>it first checks whether the candidate correctly executes (i.e., does not violate its contract)
 *  on a number of collected counterexamples. If at least one fails, it considers the candidate an invalid
 *  fix.</li>
 * <li>if the candidate "passes" all collected inputs, then it checks using TACO whether the fix candidate
 * is indeed a valid fix.</li>
 * <li>This class allows to either generate JUnit tests for each counterexample or to use the counterexamples
 * without generating any new classes.</li>
 * @author Nazareno Aguirre
 *
 */
public class TacoWithRacSuccessCheckStrategy implements SuccessCheckStrategy {
	
	/**
	 * Stores the list of collected counterexamples (originating from previously found
	 * invalid candidates).
	 */
	private List<CounterExample> collectedCounterExamples = new ArrayList<CounterExample>();
	
	/**
	 * Stores a list of paths representing the JUnit test classes built from previously
	 * obtained TACO counterexamples.
	 */
	private List<Path> builtJunitTests = new ArrayList<Path>();
	
	/**
	 * Whether or not to generate JUnit tests for TACO counterexamples.
	 */
	private boolean buildJunitTest = false;

	/**
	 * It checks whether a given candidate is a successful fix or not by doing the following:
	 * - it first checks whether the candidate correctly executes (i.e., does not violate its contract)
	 *  on a number of collected counterexamples. If at least one fails, it considers the candidate an invalid
	 *  fix.
	 * - if the candidate "passes" all collected inputs, then it checks using TACO whether the fix candidate
	 * is indeed a valid fix.  
	 */
	public boolean isSuccessful(FixCandidate s) {
		if (s==null) throw new IllegalArgumentException("null fix candidate");
		if (s.program==null) throw new IllegalArgumentException("null program in fix candidate");
		
		if (!copy(s.program.getFilePath(), StrykerConfig.getInstance().getCompilingSandbox() + s.program.getClassName().replaceAll("\\.", "/") + ".java")) {
			System.err.println("couldn't copy " + s.program.getFilePath() + " to " + StrykerConfig.getInstance().getCompilingSandbox());
			return false;
		}
		
		String sourceFolderBackup = s.program.getSourceFolder();
		s.program.moveLocation(StrykerConfig.getInstance().getCompilingSandbox());
		
		if (!s.program.isValid()) {
			s.program.moveLocation(sourceFolderBackup);
			return false;
		}
		ClassLoader currentThreadClassLoaderBackup = Thread.currentThread().getContextClassLoader();
		boolean error = false;
		boolean[] testsResults;
		boolean racPassed = true;
		boolean isFix = false;
		try {
			// call jmlrac before taco
			if (this.buildJunitTest) {
				testsResults = RacAPI.getInstance().runJUnits(s, this.builtJunitTests);
			} else {
				testsResults = RacAPI.getInstance().runTests(s, this.collectedCounterExamples);
			}
			for (int tr = 0; tr < testsResults.length && racPassed; tr++) {
				racPassed = testsResults[tr];
			}
			if (racPassed) {
				// if rac checks pass, call TACO to check whether the 
				// fix candidate is indeed a fix.
				JavaCompilerAPI.getInstance().removeAllClassFilesRecursively(StrykerConfig.getInstance().getCompilingSandbox());
				String[] classpathToCompile = new String[]{StrykerConfig.getInstance().getCompilingSandbox(), StrykerConfig.getInstance().getTestsOutputDir()};
				if (!JavaCompilerAPI.getInstance().compile(StrykerConfig.getInstance().getCompilingSandbox() + s.getProgram().getClassNameAsPath()+".java", classpathToCompile)) {
					System.err.println("error while compiling FixCandidate!");
					return false;
				}
				
				ReloaderAPI.getInstance().rescan();
				
				ReloaderAPI.getInstance().reloadFrom(s.getProgram().getClassName(), s.getProgram().getAbsolutePath());
				ReloaderAPI.getInstance().setReloaderAsThreadClassLoader(Thread.currentThread());
				
				isFix = !TacoAPI.getInstance().isSAT(s);
				if (!isFix) {
					// if candidate is invalid, collect the input obtained from
					// the SAT check, for future verifications
					CounterExample lastCounterExample = TacoAPI.getInstance().getLastCounterExample();
					if (!this.collectedCounterExamples.contains(lastCounterExample)) {
						if (this.buildJunitTest) {
							Path junitTest = RacAPI.getInstance().buildJUnit(s, lastCounterExample);
							if (junitTest != null) this.builtJunitTests.add(junitTest);
						}
						collectedCounterExamples.add(TacoAPI.getInstance().getLastCounterExample());
					}
				} else {
				}
			} else {
				isFix = false;
			}
		} catch (TacoNotImplementedYetException e) {
			error = true;
		} catch (JDynAlloySemanticException e) {
			error = true;
		} catch (Exception e) {
			e.printStackTrace();
			error = true;
		}
		s.program.moveLocation(sourceFolderBackup);
		Thread.currentThread().setContextClassLoader(currentThreadClassLoaderBackup);
		return isFix && !error;
	}

	/**
	 * Auxiliary method that copies a file from a source to a destination.
	 * @param srcPath is the source to copy
	 * @param destPath is the target to copy
	 * @return true iff the copy was successful.
	 * TODO this method is redundant; it also appears in other implementations of SuccessCheckStrategy. Improve design to remove code redundancy.
	 */
	private boolean copy(String srcPath, String destPath) {
		Path source = FileSystems.getDefault().getPath(srcPath);
		Path target = FileSystems.getDefault().getPath(destPath);
		try {
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Enable JUnit test generation
	 */
	public void generateTests() {
		this.buildJunitTest = true;
	}
	
	

	
}

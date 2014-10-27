package repairer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mujava.app.Reloader;

import tools.CounterExample;
import tools.JavaCompilerAPI;
import tools.RacAPI;
import tools.TacoAPI;
import ar.edu.jdynalloy.JDynAlloySemanticException;
import ar.edu.taco.TacoNotImplementedYetException;
import config.StrykerConfig;

/**
 * This class is a particular implementation of SuccessCheckStrategy. It checks whether a given candidate
 * is a successful fix or not by doing the following:
 * - it first checks whether the candidate correctly executes (i.e., does not violate its contract)
 *  on a number of collected counterexamples. If at least one fails, it considers the candidate an invalid
 *  fix.
 * - if the candidate "passes" all collected inputs, then it checks using TACO whether the fix candidate
 * is indeed a valid fix.  
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
		
		String[] classpathToCompile = new String[]{StrykerConfig.getInstance().getCompilingSandbox()};
		if (!JavaCompilerAPI.getInstance().compile(StrykerConfig.getInstance().getCompilingSandbox() + s.getProgram().getClassNameAsPath()+".java", classpathToCompile)) {
			System.err.println("error al compilar el FixCandidate!");
			return false;
		}
		
		Reloader reloader = new Reloader(Arrays.asList(classpathToCompile), Thread.currentThread().getContextClassLoader());
		reloader.rloadClass(s.getProgram().getClassName(), true);
		Thread.currentThread().setContextClassLoader(reloader);
		
		if (!s.program.isValid()) return false;
		boolean error = false;
		boolean racPassed = true;
		boolean isFix = false;
		try {
			// call jmlrac before taco
			racPassed = RacAPI.getInstance().testsPassed(s, collectedCounterExamples);
			if (racPassed) {
				// if rac checks pass, call TACO to check whether the 
				// fix candidate is indeed a fix.
				isFix = !TacoAPI.getInstance().isSAT(s);
				if (!isFix) {
					// if candidate is invalid, collect the input obtained from
					// the SAT check, for future verifications
					collectedCounterExamples.add(TacoAPI.getInstance().getLastCounterExample());
				}
			}
			else {
				isFix = false;
			}
		} catch (TacoNotImplementedYetException e) {
			error = true;
		} catch (JDynAlloySemanticException e) {
			error = true;
		} catch (Exception e) {
			error = true;
		}
		s.program.moveLocation(sourceFolderBackup);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	
}

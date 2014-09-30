package repairer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;

import search.AbstractSearchProblem;
import ar.edu.taco.TacoAnalysisResult;
import ar.edu.taco.TacoMain;
import ar.edu.taco.TacoNotImplementedYetException;

/**
 * StrykerRepairSearchProblem is a class that contains all concrete elements necessary for implementing the
 * program repair process associated with Stryker, as a search problem. This includes:
 * - the construction of an initial fix candidate to start the search for a fix.
 * - a routine that, given a fix candidate, computes its successors for the search (mutations obtainable from
 * a given fix candidate).
 * - a routine that, given a fix candidate, decides whether it constitutes an actual fix or not.
 * Actual search strategy is decoupled from this class, so that it can be easily set and replaced. They are 
 * defined as implementations of search.engines.AbstractSearchEngine.
 * @author Nazareno Matías Aguirre
 * @version 0.1
 */
public class StrykerRepairSearchProblem implements AbstractSearchProblem<FixCandidate> {

	/**
	 * class to fix using Stryker.
	 */
	protected JMLAnnotatedClass classToFix;
	
	/**
	 * name of method in class classToFix, that is going to be repaired using Stryker.
	 */
	protected String methodToFix;
	
	
	/**
	 * stores the class to repair and all its dependencies (only java classes)
	 */
	private String[] relevantClasses;
	
	
	/**
	 * Constructor of StrykerRepairSearchProblem. It receives a JML program to fix, and the name of the
	 * method to fix in the program/class.
	 * @param programToFix is the JML program containing the method to fix
	 * @param methodToFix is the name of the method to fix.
	 */
	public StrykerRepairSearchProblem(JMLAnnotatedClass programToFix, String methodToFix) {
		if (programToFix==null) throw new IllegalArgumentException("no program to fix");
		if (methodToFix==null || methodToFix.isEmpty()) throw new IllegalArgumentException("no method to fix");
		if (!programToFix.hasMethod(methodToFix)) throw new IllegalArgumentException("class " + programToFix.getClassName() + " doesn't have method " + methodToFix);
		this.classToFix = programToFix;
		this.methodToFix = methodToFix;
		this.relevantClasses = new String[]{programToFix.getClassName()};
	}
	
	/**
	 * Constructor of StrykerRepairSearchProblem. It receives a JML program to fix, and the name of the
	 * method to fix in the program/class.
	 * @param programToFix is the JML program containing the method to fix
	 * @param methodToFix is the name of the method to fix.
	 */
	public StrykerRepairSearchProblem(JMLAnnotatedClass programToFix, String methodToFix, String[] dependencies) {
		this(programToFix, methodToFix);
		this.relevantClasses = new String[dependencies.length + 1];
		this.relevantClasses[0] = programToFix.getClassName();
		System.arraycopy(dependencies, 0, this.relevantClasses, 1, dependencies.length);
	}
	
	/**
	 * Returns the initial fix candidate, to start a search for a program fix.
	 * @return the received JML program, as a fix candidate (coming from no mutation).
	 */
	public FixCandidate initialState() {
		if (classToFix==null) throw new IllegalStateException("program to fix not set in stryker search problem");
		return (new FixCandidate(this.classToFix));
	}

	/**
	 * Computes the successors of a given fix candidate. Successors are all mutants that can be obtained from
	 * a given fix candidate. These mutations are restricted by: the mutation operators considered (so far, 
	 * a fixed set of all mutators, excluding those that apply to the left-hand side of assignments), and
	 * the mutGenLimits provided in the fix candidate's program text.
	 * @param s is the fix candidate to compute the successors to.
	 * @return the list of all mutations, as fix candidates, obtainable from the fix candidate s.
	 */
	public List<FixCandidate> getSuccessors(FixCandidate s) {
		if (s==null) throw new IllegalArgumentException("null candidate passed for computing successors");
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
		
		if (!copy(s.program.getFilePath(), FixCandidate.getSandboxDir() + s.program.getClassName().replaceAll("\\.", "/") + ".java")) {
			System.err.println("couldn't copy " + s.program.getFilePath() + " to " + FixCandidate.getSandboxDir());
			return false;
		}
		
		String sourceFolderBackup = s.program.getSourceFolder();
		s.program.moveLocation(FixCandidate.getSandboxDir());
		
		if (!s.program.isValid()) return false;
		TacoMain taco = new TacoMain(null);
		Properties overridingProperties = new Properties();
		overridingProperties.put("classToCheck",s.program.getClassNameAsPath());//s.program.getClassName());
		overridingProperties.put("relevantClasses",mergedRelevantClasses());
		overridingProperties.put("methodToCheck",this.methodToFix+"_0");
		overridingProperties.put("jmlParser.sourcePathStr", FixCandidate.getSandboxDir());//s.program.getSourceFolder());
		TacoAnalysisResult result = null;
		try {
	
			result = taco.run("genericTest.properties", overridingProperties);
		}
		catch (TacoNotImplementedYetException e) {
			// candidate is well formed JML but taco does not support syntax.
			// considering candidate invalid, for the moment.
			s.program.moveLocation(sourceFolderBackup);
			return false;
		}
		s.program.moveLocation(sourceFolderBackup);
		return result.get_alloy_analysis_result().isUNSAT();
	}
	
	
	/**
	 * @return a {@code String} representation of the relevant classes : {@code String}
	 */
	public String mergedRelevantClasses() {
		String mrc = "";
		for (int d = 0; d < this.relevantClasses.length; d++) {
			mrc += this.relevantClasses[d];
			if (d + 1 < this.relevantClasses.length) {
				mrc += ",";
			}
		}
		return mrc;
	}
	
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

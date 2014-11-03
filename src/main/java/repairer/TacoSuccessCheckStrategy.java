package repairer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import tools.JavaCompilerAPI;
import tools.TacoAPI;
import ar.edu.jdynalloy.JDynAlloySemanticException;
import ar.edu.taco.TacoNotImplementedYetException;
import config.StrykerConfig;

/**
 * This class is a particular implementation of SuccessCheckStrategy. It checks whether a given candidate
 * is a successful fix or not resorting to bounded verification using TACO.
 * @author Nazareno Aguirre
 *
 */
public class TacoSuccessCheckStrategy implements SuccessCheckStrategy {

	/**
	 * Checks whether the provided fix candidate is a successful fix or not resorting to bounded verification
	 * using TACO.
	 */
	public boolean isSuccessful(FixCandidate s) {
		if (s==null) throw new IllegalArgumentException("null fix candidate");
		if (s.program==null) throw new IllegalArgumentException("null program in fix candidate");
		
		if (!copy(s.program.getFilePath(), StrykerConfig.getInstance().getCompilingSandbox() + s.program.getClassName().replaceAll("\\.", StrykerConfig.getInstance().getFileSeparator()) + ".java")) {
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
		
		JavaCompilerAPI.getInstance().updateReloaderClassPath(classpathToCompile);
		JavaCompilerAPI.getInstance().reloadClass(s.getProgram().getClassName());
		Thread.currentThread().setContextClassLoader(JavaCompilerAPI.getInstance().getReloader());
		
		if (!s.program.isValid()) return false;
		boolean error = false;
		boolean sat = false;
		try {
			sat = TacoAPI.getInstance().isSAT(s);
		} catch (TacoNotImplementedYetException e) {
			error = true;
		} catch (JDynAlloySemanticException e) {
			error = true;
		}
		s.program.moveLocation(sourceFolderBackup);
		return !sat && !error;
	}
	
	
	/**
	 * Auxiliary method that copies a file from a source to a destination.
	 * @param srcPath is the source to copy
	 * @param destPath is the target to copy
	 * @return true iff the copy was successful.
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

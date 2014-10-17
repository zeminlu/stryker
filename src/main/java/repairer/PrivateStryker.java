package repairer;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Set;

import config.StrykerConfig;

import mujava.api.MutantIdentifier;
import search.engines.AbstractBoundedSearchEngine;
import search.engines.BoundedBreadthFirstSearchEngine;
import search.engines.BoundedDepthFirstSearchEngine;

/**
 * PrivateStryker is a command line application that calls Stryker on a given class and method, and performs the
 * intra statement mutation-based repair, without any pruning.
 * @author Nazareno Matías Aguirre
 * @version 0.4.1
 */
public class PrivateStryker {
	
	/**
	 * jml annotated class to repair
	 */
	private JMLAnnotatedClass subjectClass;
	
	/**
	 * stores the class to repair and all its dependencies (only java classes)
	 */
	private String[] relevantClasses;
	
	/**
	 * method to repair within {@code subjectClass}
	 */
	private String subjectMethod;
	
	
	/**
	 * Indicates which search strategy is used for searching for a program repair.
	 * By default, the strategy is (bounded) DFS.
	 */
	private boolean dfsStrategy = true;
	
	/**
	 * max depth to be considered in the search of program repairs
	 */
	private int maxDepth = 3;
	
	/**
	 * Constructor of class ProgramRepair. It sets the subject of the repair process
	 * with the provided parameter.
	 * @param subjectClass is the class containing the method to be repaired.
	 * @param subjectMethod is the method to be repaired.
	 */
	public PrivateStryker(JMLAnnotatedClass subjectClass, String subjectMethod) {
		if (subjectClass==null) throw new IllegalArgumentException("program is null");
		if (subjectMethod==null) throw new IllegalArgumentException("method is null");
		if (!subjectClass.isValid()) throw new IllegalArgumentException("program does not compile");
		if (!StrykerConfig.instanceBuilt()) StrykerConfig.getInstance(StrykerConfig.DEFAULT_PROPERTIES);
		this.subjectClass = subjectClass;
		this.subjectMethod = subjectMethod;
		this.relevantClasses = new String[] {subjectClass.getClassName()};
	}
	
	/**
	 * Constructor of class ProgramRepair. It sets the subject of the repair process
	 * with the provided parameter.
	 * @param subjectClass	:	the class containing the method to be repaired.						:	{@code JMLAnnotatedClass}
	 * @param subjectMethod :	the method to be repaired.											:	{@code String}
	 * @param dependencies	:	the class to repair and all its dependencies (only java classes)	:	{@code String[]}
	 */
	public PrivateStryker(JMLAnnotatedClass subjectClass, String subjectMethod, String[] dependencies) {
		this(subjectClass, subjectMethod);
		String[] mergedDependencies = new String[this.relevantClasses.length + dependencies.length];
		System.arraycopy(this.relevantClasses, 0, mergedDependencies, 0, this.relevantClasses.length);
		System.arraycopy(dependencies, 0, mergedDependencies, this.relevantClasses.length, dependencies.length);
		this.relevantClasses = mergedDependencies;
	}

	/**
	 * Constructor of class ProgramRepair. It sets the subject of the repair process
	 * with the provided parameter.
	 * @param subjectClass is the class containing the method to be repaired.
	 * @param subjectMethod is the method to be repaired.
	 * @param maxDepth is the maximum depth to be considered for the search of repairs.
	 */
	public PrivateStryker(JMLAnnotatedClass subjectClass, String subjectMethod, int maxDepth) {
		this(subjectClass, subjectMethod);
		this.maxDepth = maxDepth;
	}
	
	/**
	 * Constructor of class ProgramRepair. It sets the subject of the repair process
	 * with the provided parameter.
	 * @param subjectClass	:	the class containing the method to be repaired.						:	{@code JMLAnnotatedClass}
	 * @param subjectMethod	:	the method to be repaired.											:	{@code String}
	 * @param dependencies	:	the class to repair and all its dependencies (only java classes)	:	{@code String[]}
	 * @param maxDepth		:	the maximum depth to be considered for the search of repairs		:	{@code int}
	 */
	public PrivateStryker(JMLAnnotatedClass subjectClass, String subjectMethod, String[] dependencies, int maxDepth) {
		this(subjectClass, subjectMethod, dependencies);
		this.maxDepth = maxDepth;
	}

	
	/**
	 * setProgram: it sets the subject of the repair process with the provided parameter.
	 * @param subject is the program that the repair process will be applied to.
	 */
	public void setProgram(JMLAnnotatedClass subject) {
		if (subject==null) throw new IllegalArgumentException("program is null");
		if (!subject.isValid()) throw new IllegalArgumentException("program does not compile");
		this.subjectClass = subject;		
	}
	
	/**
	 * Sets Bounded Depth First Search as the strategy to use in the search for program repairs.
	 */
	public void setDfsStrategy() {
		this.dfsStrategy = true;
	}

	/**
	 * Sets Bounded Breadth First Search as the strategy to use in the search for program repairs.
	 */
	public void setBfsStrategy() {
		this.dfsStrategy = false;
	}
	
	/**
	 * Sets the maximum depth to be considered in the search of program repairs.
	 * @param maxDepth is the value to be set as maximum depth for the search.
	 */
	public void setMaxDepth(int maxDepth) {
		if (maxDepth<0) throw new IllegalArgumentException("max depth must be >=0");
		this.maxDepth = maxDepth;
	}
	
	/**
	 * @return the class to repair and all its dependencies (only java classes)
	 */
	public String[] getClassesDependencies() {
		return this.relevantClasses;
	}
	
	/**
	 * Initiates the search for a repair of the subject.
	 * @return true iff a repair of the subject was found.
	 */
	public boolean repair() {
		if (subjectClass==null || subjectMethod==null) throw new IllegalStateException("program or method is null");
		if (!subjectClass.isValid()) throw new IllegalStateException("program does not compile");
		StrykerRepairSearchProblem problem = new StrykerRepairSearchProblem(subjectClass, subjectMethod, this.relevantClasses);
		if (this.typeScope!=null) {
			// if a scope is provided, we pass it to the problem (to be used in success method).
			problem.setScope(this.typeScope);
		}
		// +++++++++++++++++++++++++++++++++++++++++++++++
		// create compilation sandbox
		String sandboxDir = StrykerConfig.getLastBuiltInstance().getCompilingSandbox();
		if (!createSandboxDir(sandboxDir)) {
			System.err.println("couldn't create compilation sandbox directory: " + sandboxDir);
			return false;
		}
		if (!move(problem.initialState().program.getSourceFolder(), sandboxDir, problem.initialState().program.getFilePath())) {
			System.err.println("couldn't move compilation ambient to compilation sandbox directory");
			return false;
		}
		// ------------------------------------------------
		AbstractBoundedSearchEngine<FixCandidate,StrykerRepairSearchProblem> engine = null;
		if (this.dfsStrategy) {
			engine = new BoundedDepthFirstSearchEngine<FixCandidate,StrykerRepairSearchProblem>();
		}
		else {
			engine = new BoundedBreadthFirstSearchEngine<FixCandidate,StrykerRepairSearchProblem>();
		}
		engine.setProblem(problem);
		engine.setMaxDepth(this.maxDepth);
		boolean outcome = engine.performSearch();
		if (outcome) {
			FixCandidate solution = engine.getSolution();
			String solutionLocation = solution.program.getAbsolutePath() + solution.program.getClassName() + ".java";
			System.out.println("*** FOUND SOLUTION! Get it from: " + solutionLocation);
			System.out.println("*** Mutations that produced the fix: ");
			for (MutantIdentifier mutation : solution.getMutations()) {
				System.out.println(mutation.toString() + " in method " + (mutation.isOneLineInMethodOp()?(this.subjectMethod + " in line " + mutation.getAffectedLine()):"not a method mutation"));
			}
			System.out.println("*** Stats: " + engine.report());
		}
		else {
			System.out.println("*** COULD NOT REPAIR PROGRAM. Try increasing depth in the search for solutions");
			System.out.println("*** Stats: " + engine.report());
		}
		deleteDir(sandboxDir);
		return outcome;
	}
	
	private boolean move(String sourceFolder, String sandboxDir, String ignoreFile) {
		final Path source = FileSystems.getDefault().getPath(sourceFolder);
		final Path target = FileSystems.getDefault().getPath(sandboxDir);
		final Path ignore = FileSystems.getDefault().getPath(ignoreFile);
		try {
			Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
					Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) throws IOException {
					Path targetdir = target.resolve(source.relativize(dir));
					try {
						Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwxrwx");
					    FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(perms);
						Files.createDirectory(targetdir, fileAttributes);
						//Files.copy(dir, targetdir);
					} catch (FileAlreadyExistsException e) {
						if (!Files.isDirectory(targetdir))
							throw e;
					} catch (AccessDeniedException e) {
						System.err.println("AccessDeniedException: " + e.getMessage());
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file,BasicFileAttributes attrs) throws IOException {
					try {
						if (!file.toAbsolutePath().equals(ignore)) {
							Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-rw-rw-");
						    FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(perms);
							Path resolvedPath = target.resolve(source.relativize(file));
						    Files.createFile(resolvedPath, fileAttributes);
							Files.copy(file,target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
						}
								
					} catch (AccessDeniedException e) {
						System.err.println("AccessDeniedException: " + e.getMessage());
					}
					return FileVisitResult.CONTINUE;
				}
				
				
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean deleteDir(String dir) {
		Path start = FileSystems.getDefault().getPath(dir);
		try {
			Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir,
						IOException e) throws IOException {
					if (e == null) {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					} else {
						// directory iteration failed
						throw e;
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean createSandboxDir(String sandboxDir) {
		Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwxrwx");
	    FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(perms);
		Path sandboxPath = FileSystems.getDefault().getPath(sandboxDir);
	    try {
			Files.createDirectory(sandboxPath, fileAttributes);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Sets the scope, provided as a string.
	 * FIXME This is not the best way to pass the scope (we are passing it through too many classes). Let's use
	 * configuration as a singleton.
	 * @param typeScope is the scope to be used in the repair process.
	 */
	public void setScope(String typeScope) {
		this.typeScope = typeScope;
	}
	
	private String typeScope = null;
	
}

package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.multijava.mjc.JCompilationUnitType;

import config.StrykerConfig;
import ar.edu.jdynalloy.JDynAlloySemanticException;
import ar.edu.taco.TacoAnalysisResult;
import ar.edu.taco.TacoConfigurator;
import ar.edu.taco.TacoMain;
import ar.edu.taco.TacoNotImplementedYetException;
import ar.edu.taco.engine.JUnitStage;
import ar.edu.taco.engine.SnapshotStage;
import ar.edu.taco.engine.StrykerStage;
import ar.edu.taco.jml.parser.JmlParser;
import ar.edu.taco.junit.RecoveredInformation;
import ar.edu.taco.stryker.api.impl.MuJavaController;
import ar.edu.taco.stryker.api.impl.OpenJMLController;
import ar.edu.taco.stryker.api.impl.MuJavaController.MsgDigest;
import ar.edu.taco.stryker.api.impl.input.MuJavaFeedback;
import ar.edu.taco.stryker.api.impl.input.MuJavaInput;
import ar.edu.taco.stryker.api.impl.input.OpenJMLInput;
import ar.edu.taco.stryker.api.impl.input.OpenJMLInputWrapper;
import ar.edu.taco.utils.FileUtils;
import repairer.FixCandidate;

/**
 * This class is used to access TACO, the main responsabilities of this API are:
 * 
 * <li> Given a java source file run SAT and return the result </li>
 * <li> Obtain the counter example built by SAT solver for a previous run that gave UNSAT </li>
 *
 * TODO: add author
 * 
 * @see FixCandidate
 * @see CounterExample
 * 
 * @version 0.2u
 */
public class TacoAPI {
	
	/**
	 * An instance of this class, used to implement the Singleton pattern
	 */
	private static TacoAPI instance = null;
	
	/**
	 * Access to Taco in comitaco
	 */
	private TacoMain wireToTaco = null;
	
	/**
	 * config file used by run method of {@code TacoMain}
	 */
	private String configFile = null;
	
	/**
	 * overriding properties used by run method of {@code TacoMain}
	 */
	private Properties overridingProperties = null;

	/**
	 * Stores the last analysis result, as a TacoAnalysisResult object.
	 */
	private TacoAnalysisResult lastAnalysisResult;

	/**
	 * Indicates whether the last run of taco led to a sat instance.
	 */
	private boolean lastRunIsSat;

	/**
	 * Stores the fix candidate used for the last run of taco.
	 */
	private FixCandidate lastFixCandidate;
		
	/**
	 * Returns an instance of this class
	 * 
	 * @param configFile	:	config file used by run method of {@code TacoMain}	:	{@code String}
	 * @return an instance of this class
	 * @throws IllegalStateException if an instance is already built and this method is called with a different config file
	 */
	public static TacoAPI getInstance(String configFile) throws IllegalStateException {
		if (instance != null && instance.configFile.compareTo(configFile) != 0) {
			throw new IllegalStateException("TacoAPI instance is already built using config file : " + instance.configFile);
		}
		if (instance == null) instance = new TacoAPI(configFile, new Properties());
		return instance;
	}
	
	/**
	 * @return an instance of this class
	 */
	public static TacoAPI getInstance() {
		if (instance == null) {
			instance = new TacoAPI(StrykerConfig.DEFAULT_PROPERTIES, new Properties());
		}
		return instance;
	}
	
	/**
	 * Private constructor
	 * 
	 * @param configFile			:	config file used by run method of {@code TacoMain}				:	{@code String}
	 * @param overridingProperties	:	overriding properties used by run method of {@code TacoMain}	:	{@code Properties}
	 */
	private TacoAPI(String configFile, Properties overridingProperties) {
		this.configFile = configFile;
		this.overridingProperties = overridingProperties;
		this.wireToTaco = new TacoMain(null);
	}
	
	/**
	 * @return overriding properties used by run method of {@code TacoMain}
	 */
	public Properties getOverridingProperties() {
		return this.overridingProperties;
	}

	/**
	 * Runs SAT on a java source file
	 * @param candidate	:	the java source file	:	{@code FixCandidate}
	 * @return {@code true} if the SAT solver returns SAT, {@code false} if returns UNSAT	:	{@code boolean}
	 */
	public boolean isSAT(FixCandidate candidate) throws TacoNotImplementedYetException, JDynAlloySemanticException {
		TacoAnalysisResult result = null;
		try {
			result = this.wireToTaco.run(this.configFile, this.overridingProperties);
		}
		catch (TacoNotImplementedYetException e) {
			throw e;
		}
		catch (JDynAlloySemanticException e) {
			throw e; 
		}
		
		boolean outcome = result.get_alloy_analysis_result().isSAT();
		if (outcome) {
			this.lastRunIsSat = true;
			this.lastFixCandidate = candidate;
			this.lastAnalysisResult = result;
		}
		return outcome;
	}
	
	/**
	 * @return the {@code CounterExample} generated by the last call to {@link TacoAPI#isSAT(FixCandidate)}	:	{@code CounterExample}
	 */
	public CounterExample getLastCounterExample() {
					
		if (!this.lastRunIsSat) throw new IllegalStateException("calling getLastCounterExample on invalid state");
		
    	String PATH_SEP = ":";
    	String FILE_SEP = "/";

		
        List<JCompilationUnitType> compilation_units = JmlParser.getInstance().getCompilationUnits();
        String classToCheck = this.lastFixCandidate.getProgram().getClassName();//this.lastFixCandidate.getProgram().getFilePath();
        String methodToCheck = this.lastFixCandidate.getMethodToFix() + "_0" ;
        TacoAnalysisResult analysis_result = this.lastAnalysisResult;

	        SnapshotStage snapshotStage = new SnapshotStage(
	                compilation_units, analysis_result, classToCheck, methodToCheck);
	        snapshotStage.execute();


	        RecoveredInformation recoveredInformation = snapshotStage.getRecoveredInformation();
	        recoveredInformation.setFileNameSuffix(StrykerStage.fileSuffix);


	        JUnitStage jUnitStage = new JUnitStage(recoveredInformation);
	        jUnitStage.execute();

	        String junitFile = jUnitStage.getJunitFileName();

	        String currentJunit = null;

	        String tempFilename = junitFile.substring(0, junitFile.lastIndexOf(FILE_SEP)+1) /*+ FILE_SEP*/; 
	        String editedTestFolderPath = tempFilename.replaceAll("generated", "output");
	        File editedTestFolderFile = new File(editedTestFolderPath);
	        editedTestFolderFile.mkdirs();
	        String packageToWrite = "ar.edu.output.junit";
	        String fileClasspath = tempFilename.substring(0, tempFilename.lastIndexOf(
	                new String("ar.edu.generated.junit").replaceAll("\\.", FILE_SEP)));
	        fileClasspath = fileClasspath.replaceFirst("generated", "output");
	        currentJunit = TacoMain.editTestFileToCompile(junitFile, classToCheck, packageToWrite, methodToCheck);

	        if (!JavaCompilerAPI.getInstance().compile(currentJunit, new String[]{StrykerConfig.getInstance().getCompilingSandbox(), StrykerConfig.getInstance().getJunitPath(), StrykerConfig.getInstance().getHamcrestPath()})) {
	        	System.err.println("Error while compiling " + currentJunit);
	        	return null;
	        }
	        
//	        File[] file1 = new File[]{new File(currentJunit)};
//	        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
//	        StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(null, null, null);
//	        Iterable<? extends JavaFileObject> compilationUnit1 =
//	                fileManager.getJavaFileObjectsFromFiles(Arrays.asList(file1));
//	        javaCompiler.getTask(null, fileManager, null, null, null, compilationUnit1).call();
//	        try {
//	            fileManager.close();
//	        } catch (IOException e1) {
//	            // TODO: Define what to do!
//	            e1.printStackTrace();
//	        }
//	        javaCompiler = null;
//	        file1 = null;
//	        fileManager = null;

	        //                                      if(compilationResult == 0) {
	        System.out.println("junit counterexample compilation succeded");
	        ClassLoader cl = ClassLoader.getSystemClassLoader();
	        ClassLoader cl2;
	        try {
	            cl2 = new URLClassLoader(new URL[]{new File(fileClasspath).toURI().toURL()}, cl);
	            //                                      ClassLoaderTools.addFile(fileClasspath);
	            String classToLoad = packageToWrite+"."+TacoMain.obtainClassNameFromFileName(junitFile);
	            Class<?> clazz = cl2.loadClass(classToLoad);
	            cl = null;
	            cl2 = null;
	            //                                          log.warn("The class just stored is: "+clazz.getName());
	            System.out.println("preparing to store a test class... "+packageToWrite+"." + 
	                    MuJavaController.obtainClassNameFromFileName(junitFile));
	            //                                          Result result = null;
	            //                                          final Object oToRun = clazz.newInstance();
	            DigestOutputStream dos;
	            File duplicatesTempFile = null;
	            String content = null;
	            try {
	                content = FileUtils.readFile(junitFile);
	            }
	            catch (Exception e) {
	                throw new IllegalArgumentException("invalid or null file");
	            }
	            try {
	                duplicatesTempFile = File.createTempFile("forDuplicatesJunit", null);
	                dos = new DigestOutputStream(new FileOutputStream(duplicatesTempFile, false), MessageDigest.getInstance("MD5"));
	                dos.write(content.getBytes());
	                dos.flush();
	                dos.close();
	            }
	            catch (Exception e) {
	                throw new IllegalArgumentException("exception thrown while trying to compute digest in class VariablizedSATVerdicts");
	            }
	            
	            return (new CounterExample(clazz, junitFile));
	        } catch (MalformedURLException e1) {
	            // TODO: Define what to do!
	            e1.printStackTrace();
	        } catch (ClassNotFoundException e1) {
	            // TODO: Define what to do!
	            e1.printStackTrace();
	        }

	        return null;
	    }

	
	/**
	 * @param candidate	:	an instance of {@code FixCandidate} for which {@link TacoAPI#isSAT(FixCandidate)} has previously been called	:	{@code FixCandidate}
	 * @return a previously generated {@code CounterExample} for that candidate	:	{@code CounterExample}
	 * <hr>
	 * <b>note: if this method returns {@code null} it can be because a {@code CounterExample} couldn't be built on a previous call to {@code sat(candidate)}, or {@code sat(candidate)} wasn't called yet.</b>
	 */
	public CounterExample getCounterExample(FixCandidate candidate) {
		//TODO: implement this method
		throw new UnsupportedOperationException ("TacoAPI#getCounterExample(FixCandidate) : not yet implementedt");
	}

}

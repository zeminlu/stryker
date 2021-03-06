package repairer;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import openjava.ptree.ParseTreeException;

import mujava.OpenJavaException;
import mujava.api.Mutant;
import mujava.api.MutantIdentifier;
import mujava.api.MutantsInformationHolder;
import mujava.app.Core;
import mujava.app.MutantInfo;
import mujava.app.MutationRequest;
import mujava.app.Mutator;
import mujava.util.JustCodeDigest;

/**
 * This class allows the use of {@code muJava++} to generate mutants ({@code FixCandidate})
 * 
 * @author Simón Emmanuel Gutiérrez Brida
 * @version 0.3.2
 * @see FixCandidate
 * @see JMLAnnotatedClass
 */
public class MuJavaAPI {
	/**
	 * Defines the location where all mutants will be written : {@code String}
	 */
	private String outputDirectory;
	/**
	 * Used to detect duplicate mutants : {@code Set<byte[]>}
	 */
	private Set<byte[]> mutantHashes;
	
	/**
	 * Constructor
	 * mutants will be written to {@code /tmp/} folder
	 */
	public MuJavaAPI() {
		this("/tmp/mutants/");
	}
	
	/**
	 * Constructor
	 * @param outputDirectory : the location where all mutants will be written : {@code String}
	 */
	public MuJavaAPI(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	
	
	/**
	 * Generates mutants from a java file
	 * 
	 * @param fixCandidate		:	the java file from which mutants will be generated	:	{@code FixCandidate}
	 * @param methodToMutate	:	the method to mutate								:	{@code String}
	 * @param operators			:	the mutation operators to use						:	{@code Mutant[]}
	 * @return	a list of mutants (a list of {@code FixCandidate} where each one represent a mutant)	:	{@code List<FixCandidate>}
	 */
	public List<FixCandidate> generateMutants(FixCandidate fixCandidate, String methodToMutate, Mutant[] operators) {
		List<FixCandidate> mutants = new LinkedList<FixCandidate>();
		
		this.mutantHashes = new HashSet<byte[]>();
		
		Mutator mutator = new Mutator();
		
		String clazz = fixCandidate.program.getClassNameAsPath();
		String[] methods = {methodToMutate};
		Mutant[] ops = operators;
		String inputDir = fixCandidate.program.getSourceFolder();
		String outputDir = this.outputDirectory;
		
		outputDir += randomString(10);
		
		if (!outputDir.endsWith(Core.SEPARATOR)) {
			outputDir += Core.SEPARATOR;
		}
		
		if (!fixCandidate.mutations.isEmpty()) {
			outputDir += "from_" + md5HashToString(fixCandidate.program.getMd5Digest());
		}
		
		if (!outputDir.endsWith(Core.SEPARATOR)) {
			outputDir += Core.SEPARATOR;
		}
		
		MutationRequest request = new MutationRequest(clazz, methods, ops, inputDir, outputDir);
		
		mutator.setRequest(request);
		
		List<MutantInfo> mutantsInfo = null;
		MutantsInformationHolder mutations = null;
		
		boolean couldGenerate = false;
		
		try {
		
			Map<String, MutantsInformationHolder> mutationsPerMethod = mutator.obtainMutants();
			
			if (mutationsPerMethod != null && mutationsPerMethod.containsKey(methodToMutate)) {
				mutations = mutationsPerMethod.get(methodToMutate);
			}
			
			if (mutations != null) {
				mutantsInfo = mutator.writeMutants(methodToMutate, mutations, false);
				List<MutantInfo> filteredMutants = filterRepeatedMutants(mutantsInfo);
				mutantsInfo.clear();
				mutantsInfo.addAll(filteredMutants);
			}
			
			mutator.resetMutantFolders();
			
			couldGenerate = mutations != null;
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (OpenJavaException e) {
			e.printStackTrace();
		} catch (ParseTreeException e) {
			e.printStackTrace();
		}
		
		if (couldGenerate) {
			for (MutantInfo mi : mutantsInfo) {
				mutants.add(wrapMutant(mi, fixCandidate.getMutations()));
			}
		}
		
		return mutants;
	}
	
	
	private String md5HashToString(byte[] md5Digest) {
		String result = "";
		for (byte b : md5Digest) {
			result += Byte.toString(b);
		}
		return result;
	}

	/**
	 * Generates mutants from a java file using hardcoded mutation operators
	 * 
	 * @param fixCandidate		:	the java file from which mutants will be generated	:	{@code FixCandidate}
	 * @param methodToMutate	:	the method to mutate								:	{@code String}
	 * @return	a list of mutants (a list of {@code FixCandidate} where each one represent a mutant)	:	{@code List<FixCandidate>}
	 * <hr>
	 * <b>note: the operators used are the following</b>
	 * <b>
	 * 		<li> AMC
	 * 		<li> AODS
	 * 		<li> AODU
	 * 		<li> AOIS
	 * 		<li> AOIU
	 *		<li> AORB
	 *		<li> AORS
	 *		<li> AORU
	 *		<li> ASRS
	 *		<li> COD
	 *		<li> COI
	 *		<li> COR
	 *		<li> COR
	 *		<li> EAM
	 *		<li> EMM
	 *		<li> ROR
	 *		<li> SOR
	 *		<li> EOA_STRICT
	 *		<li> EOC_SMART
	 *		<li> IHD
	 *		<li> IHI
	 *		<li> IOD
	 *		<li> IOP
	 *		<li> IPC
	 *		<li> ISD_SMART
	 *		<li> ISI_SMART
	 *		<li> JDC
	 *		<li> JID
	 *		<li> JSD
	 *		<li> JSI
	 *		<li> JTD
	 *		<li> JTI_SMART
	 *		<li> LOD
	 *		<li> LOI
	 *		<li> LOR
	 *		<li> OAN_RELAXED
	 *		<li> OMR
	 *		<li> PCC
	 *		<li> PCD
	 *		<li> PMD
	 *		<li> PNC
	 *		<li> PPD
	 *		<li> PRVOR_REFINED
	 *		<li> PRVOU_REFINED
	 * </b>
	 */
	public List<FixCandidate> generateMutants(FixCandidate fixCandidate, String methodToMutate) {
		Mutant[] operators = {
				Mutant.AMC,
				Mutant.AODS,
				Mutant.AODU,
				Mutant.AOIS,
				Mutant.AOIU,
				Mutant.AORB,
				Mutant.AORS,
				Mutant.AORU,
				Mutant.ASRS,
				Mutant.COD,
				Mutant.COI,
				Mutant.COR,
				Mutant.COR,
				Mutant.EAM,
				Mutant.EMM,
				Mutant.ROR,
				Mutant.SOR,
				Mutant.EOA_STRICT,
				Mutant.EOC_SMART,
				Mutant.IHD,
				Mutant.IHI,
				Mutant.IOD,
				Mutant.IOP,
				Mutant.IPC,
				Mutant.ISD_SMART,
				Mutant.ISI_SMART,
				Mutant.JDC,
				Mutant.JID,
				Mutant.JSD,
				Mutant.JSI,
				Mutant.JTD,
				Mutant.JTI_SMART,
				Mutant.LOD,
				Mutant.LOI,
				Mutant.LOR,
				Mutant.OAN_RELAXED,
				Mutant.OMR,
				Mutant.PCC,
				Mutant.PCD,
				Mutant.PMD,
				Mutant.PNC,
				Mutant.PPD, 
				Mutant.PRVOR_REFINED,
				Mutant.PRVOU_REFINED,
		};
		return generateMutants(fixCandidate, methodToMutate, operators);
	}
	
	
	/**
	 * This method wraps a {@code MutantInfo} object inside a {@code FixCandidate}
	 * 
	 * @param mi	:	the mutant	:	{@code MutantInfo}
	 * @return	a {@code FixCandidate} object instanciated from the data of {@code mi}	:	{@code FixCandidate}
	 */
	private FixCandidate wrapMutant(MutantInfo mi, List<MutantIdentifier> parentMutations) {
		String baseDir = removeLastPartOfPath(mi.getPath(), mi.getName());
		String clazzName = mi.getName();
		JMLAnnotatedClass program = new JMLAnnotatedClass(baseDir, clazzName);
		List<MutantIdentifier> mutations = new LinkedList<MutantIdentifier>();
		mutations.addAll(parentMutations);
		mutations.add(mi.getMutantIdentifier());
		FixCandidate wrap = new FixCandidate(program, mutations);
		return wrap;
	}
	
	private String removeLastPartOfPath(String originalPath, String className) {
		String classNameToPath = className.replaceAll("\\.", Core.SEPARATOR) + ".java";
		int indexToCut = originalPath.indexOf(classNameToPath);
		String result = originalPath.substring(0, indexToCut-1);
		if (!result.endsWith(Core.SEPARATOR)) {
			result += Core.SEPARATOR;
		}
		return result;
	}
	
	/**
	 * Filters and deleted repeated mutants, uses {@code JustCodeDigest} to calculate mutant hashes
	 * 
	 * @param newMutants	:	the list of mutants to filter	:	{@code List<MutantInfo>}
	 * @return a list of filtered mutants
	 */
	private List<MutantInfo> filterRepeatedMutants(List<MutantInfo> newMutants) {
		List<MutantInfo> filteredMutants = new LinkedList<MutantInfo>();
		for (MutantInfo mut : newMutants) {
			String path = mut.getPath();
			File mutantFile = new File(path);
			byte[] digest = JustCodeDigest.digest(mutantFile);
			if (this.mutantHashes.add(digest)) {
				filteredMutants.add(mut);
			} else {
				delete(path);					//deletes mutant
				delete(mutantFile.getParent());	//deletes the folder which contained the mutant
			}
		}
		return filteredMutants;
	}
	
	private void delete(String path) {
		String fixedPath = path;
		File f = new File(fixedPath);
		if (f.exists()) {
			f.delete();
		}
	}
	
	private String randomString(int len) {
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}
	
	
	
}

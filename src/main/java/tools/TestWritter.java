package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import tools.data.UpdateCommand;
import config.StrykerConfig;

/**
 * This class uses the test java source template file (testClassTemplate.java) and writes a complete test
 * 
 * @author Simon Emmanuel Gutierrez Brida
 * @version 0.1.5
 */
public class TestWritter {
	/**
	 * The section name where the package should be written
	 */
	private static final String PACKAGE = "PACKAGE";
	
	/**
	 * The section name where the imports should be written
	 */
	private static final String IMPORTS = "IMPORTS";
	
	/**
	 * The section name where the class profile should be written
	 */
	private static final String CLASS = "CLASS START";
	
	/**
	 * The section name where the instance should be written
	 */
	private static final String INSTANCE_VALUE = "INSTANCE VALUE";
	
	/**
	 * The section name where the initializations should be written
	 */
	private static final String INITIALIZATIONS = "INITIALIZATIONS";
	
	/**
	 * The section name where the field updates should be written
	 */
	private static final String UPDATES = "FIELD UPDATES";
	
	/**
	 * The section name where the parameters initializations should be written
	 */
	private static final String PARAMS = "PARAMS INIT";
	
	/**
	 * Where the template file is located
	 */
	private static final String TEMPLATE_PATH = StrykerConfig.getInstance().getTestTemplatePath();
	
	/**
	 * The path to the output directory
	 */
	private static final String TEST_OUTPUT_DIR = StrykerConfig.getInstance().getTestsOutputDir();
	
	/**
	 * The new test package
	 */
	private String pkg;
	
	/**
	 * The set of imports needed by the new test
	 */
	private Set<String> importList;
	
	/**
	 * The class name of the new test
	 */
	private String className;
	
	/**
	 * Whether the method to check is static or not
	 */
	private boolean isStatic;
	
	/**
	 * The list of initializations
	 */
	private List<String> initializationsList;
	
	/**
	 * The list of updates used during initialization of the test
	 */
	private Set<UpdateCommand> updatesList;
	
	/**
	 * The param list
	 * TODO: improve this comment
	 */
	private List<String> paramsList;
	
	/**
	 * Which imports to ignore (e.g.: java.lang.Object)
	 */
	private Set<String> importsToIgnore;
	
	/**
	 * The constructor of this class, for each new test a new instance of this class should be built.
	 * 
	 * @param pkg						:	The new test package
	 * @param importList				:	The set of imports needed by the new test
	 * @param className					:	The class name of the new test
	 * @param isStatic					:	Whether the method to check is static or not
	 * @param initializationsList		:	The list of initializations
	 * @param updatesList				:	The list of updates used during initialization of the test
	 * @param paramsList				:	The param list
	 * @param importsToIgnore			:	Which imports to ignore (e.g.: java.lang.Object)
	 */
	public TestWritter(String pkg, Set<String> importList, String className, boolean isStatic, List<String> initializationsList, Set<UpdateCommand> updatesList, List<String> paramsList, Set<String> importsToIgnore) {
		this.pkg = pkg;
		this.importList = importList;
		this.className = className;
		this.isStatic = isStatic;
		this.initializationsList = initializationsList;
		this.updatesList = updatesList;
		this.paramsList = paramsList;
		this.importsToIgnore = importsToIgnore;
	}
	
	/**
	 * This writes a new JUnit test using all the information passed when calling the constructor
	 * 
	 * @return the path of the generated JUnit test
	 * @throws IOException
	 */
	public String writeTest() throws IOException {
		File origFile = new File(TestWritter.TEMPLATE_PATH);
		File destFile = new File(TestWritter.TEST_OUTPUT_DIR, this.className.replaceAll("\\.", StrykerConfig.getInstance().getFileSeparator()) + ".java");
		destFile.getParentFile().mkdirs();
		destFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(destFile);
        Scanner scan = new Scanner(origFile);
        scan.useDelimiter("\n");
        String str = null;
        while(scan.hasNext()){
            str = scan.next();
            if (str.contains(TestWritter.PACKAGE)) {
            	fos.write(("package " + this.pkg + ";\n").getBytes(Charset.forName("UTF-8")));
            } else if (str.contains(TestWritter.IMPORTS)){
            	for (String imp : this.importList) {
            		if (allowImport(imp)) fos.write(("import " + imp + ";\n").getBytes(Charset.forName("UTF-8")));
            	}
            } else if (str.contains(TestWritter.CLASS)) {
            	int lastDotIndex = this.className.lastIndexOf('.') + 1;
            	String classNameWithNoPackage = this.className.substring(lastDotIndex, this.className.length());
            	fos.write(("public class " + classNameWithNoPackage + " {\n").getBytes(Charset.forName("UTF-8")));
            } else if (str.contains(TestWritter.INITIALIZATIONS)) {
            	for (String fieldInit : this.initializationsList) {
            		String modifiedString = replace(str, TestWritter.INITIALIZATIONS, fieldInit, true);
            		fos.write((modifiedString + ";\n").getBytes(Charset.forName("UTF-8")));
            	}
            } else if (str.contains(TestWritter.UPDATES)) {
            	for (UpdateCommand update : this.updatesList) {
            		String updateString = "updateValue(" + update.getObjectName() + ", \"" + update.getFieldName() + "\", " + update.getValue() + ")";
            		String modifiedString = replace(str, TestWritter.UPDATES, updateString, true);
            		fos.write((modifiedString + ";\n").getBytes(Charset.forName("UTF-8")));
            	}
            } else if (str.contains(TestWritter.PARAMS)) {
            	int i = 0;
            	String modifiedString;
            	if (this.paramsList.size() > 0) {
            		String paramsCreation = "params = new Object[" + this.paramsList.size() + "]";
            		modifiedString = replace(str, TestWritter.PARAMS, paramsCreation, true);
            		fos.write((modifiedString  + ";\n").getBytes(Charset.forName("UTF-8")));
            	}
            	for (String param : this.paramsList) {
            		String paramAssignment = "params[" + i + "] = " + param;
            		modifiedString = replace(str, TestWritter.PARAMS, paramAssignment, true);
            		fos.write((modifiedString + ";\n").getBytes(Charset.forName("UTF-8")));
            		i++;
            	}
            } else if (str.contains(TestWritter.INSTANCE_VALUE)) {
            	String value = this.isStatic?"null":"clazz.newInstance()";
            	fos.write((replace(str, TestWritter.INSTANCE_VALUE, value, true) + "\n").getBytes(Charset.forName("UTF-8")));
            } else {
            	fos.write((str + "\n").getBytes(Charset.forName("UTF-8")));
            }
        }
        if (fos != null) fos.close();
        scan.close();
        return destFile.getAbsolutePath();
	}
	
	private String replace(String original, String target, String replacement, boolean onlyReplaceTarget) {
		String result = null;
		if (onlyReplaceTarget) {
			int targetIndex = original.indexOf(target);
			int targetEndIndex = target.length();
			if (targetIndex >= 0) {
				result = original.substring(0, targetIndex) + replacement + original.substring(targetIndex+targetEndIndex, original.length());
			}
		} else {
			result = original.replace(target, replacement);
		}
		return result;
	}
	
	private boolean allowImport(String imp) {
		if (this.importsToIgnore == null) return true;
		for (String ignoreImp : this.importsToIgnore) {
			if (ignoreImp.endsWith("*")) {
				int lastDotIdx = Math.max(ignoreImp.lastIndexOf('.'), 0);
				String ignoreImpFromPackage = ignoreImp.substring(0, lastDotIdx);
				if (imp.startsWith(ignoreImpFromPackage)) return false;
			} else {
				if (imp.compareTo(ignoreImp)==0) return false;
			}
		}
		return true;
	}
	

}

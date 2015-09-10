package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import tools.utils.UpdateCommand;
import config.StrykerConfig;

/**
 * This class uses the test java source template file (testClassTemplate.java) and writes a complete test
 * 
 * @author Simon Emmanuel Gutierrez Brida
 * @version 0.1.5
 */
public class TestWritter {
	private static final String PACKAGE = "PACKAGE";
	private static final String IMPORTS = "IMPORTS";
	private static final String CLASS = "CLASS START";
	private static final String INSTANCE_VALUE = "INSTANCE VALUE";
	private static final String INITIALIZATIONS = "INITIALIZATIONS";
	private static final String UPDATES = "FIELD UPDATES";
	private static final String PARAMS = "PARAMS INIT";
	
	private static final String TEMPLATE_PATH = StrykerConfig.getInstance().getTestTemplatePath();
	private static final String TEST_OUTPUT_DIR = StrykerConfig.getInstance().getTestsOutputDir();
	
	private String pkg;
	private Set<String> importList;
	private String className;
	private boolean isStatic;
	private List<String> initializationsList;
	private Set<UpdateCommand> updatesList;
	private List<String> paramsList;
	private Set<String> importsToIgnore;
	
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

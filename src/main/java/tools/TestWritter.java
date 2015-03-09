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
 * @version 0.1u
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
	private List<UpdateCommand> updatesList;
	private List<String> paramsList;
	
	public TestWritter(String pkg, Set<String> importList, String className, boolean isStatic, List<String> initializationsList, List<UpdateCommand> updatesList, List<String> paramsList) {
		this.pkg = pkg;
		this.importList = importList;
		this.className = className;
		this.isStatic = isStatic;
		this.initializationsList = initializationsList;
		this.updatesList = updatesList;
		this.paramsList = paramsList;
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
            		fos.write(("import " + imp + ";\n").getBytes(Charset.forName("UTF-8")));
            	}
            } else if (str.contains(TestWritter.CLASS)) {
            	fos.write(("public class " + this.className + " {\n").getBytes(Charset.forName("UTF-8")));
            } else if (str.contains(TestWritter.INITIALIZATIONS)) {
            	for (String fieldInit : this.initializationsList) {
            		fos.write((fieldInit + ";\n").getBytes(Charset.forName("UTF-8")));
            	}
            } else if (str.contains(TestWritter.UPDATES)) {
            	for (UpdateCommand update : this.updatesList) {
            		fos.write(("updateValue(" + update.getObjectName() + ", " + update.getFieldName() + ", " + update.getValue() + ");\n").getBytes(Charset.forName("UTF-8")));
            	}
            } else if (str.contains(TestWritter.PARAMS)) {
            	int i = 0;
            	if (this.paramsList.size() > 0) {
            		fos.write(("params = new Object[" + this.paramsList.size() + "];\n").getBytes(Charset.forName("UTF-8")));
            	}
            	for (String param : this.paramsList) {
            		fos.write(("params[" + i + "] = " + param + ";\n").getBytes(Charset.forName("UTF-8")));
            		i++;
            	}
            } else if (str.contains(TestWritter.INSTANCE_VALUE)) {
            	String value = this.isStatic?"null":"clazz.newInstance()";
            	fos.write((str.replace(TestWritter.INSTANCE_VALUE, value) + "\n").getBytes(Charset.forName("UTF-8")));
            } else {
            	fos.write((str + ";\n").getBytes(Charset.forName("UTF-8")));
            }
        }
        if (fos != null) fos.close();
        scan.close();
        return destFile.getAbsolutePath();
	}
	

}

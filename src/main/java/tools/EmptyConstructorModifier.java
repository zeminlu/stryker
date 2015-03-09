package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.google.common.io.Files;

import config.StrykerConfig;

/**
 * This class takes a java source file, checks if the file declares an empty constructor and if not then it will insert one
 * 
 * @author Simon Emmanuel Gutierrez Brida
 * @version 0.1u
 */
public final class EmptyConstructorModifier {

	private static enum Mode {FIND, WRITE, REMOVE};
	
	
	public static boolean hasEmptyConstructor(String sourceDir, String className) {
		return emptyConstructorScanner(sourceDir, className, Mode.FIND);
	}
	
	public static void addEmptyConstructor(String sourceDir, String className) {
		emptyConstructorScanner(sourceDir, className, Mode.WRITE);
	}
	
	public static void removeEmptyConstructor(String sourceDir, String className) {
		emptyConstructorScanner(sourceDir, className, Mode.REMOVE);
	}
	
	private static String removePackageFromName(String name) {
		int lastDotIndex = name.lastIndexOf('.');
		if (lastDotIndex > 0) {
			return name.substring(lastDotIndex, name.length());
		} else {
			return name;
		}
	}
	
	private static boolean emptyConstructorScanner(String sourceDir, String className, Mode mode) {
        File origFile = new File(sourceDir,className.replaceAll("\\.", StrykerConfig.getInstance().getFileSeparator()) + ".java");
		File destFile = new File(sourceDir,className.replaceAll("\\.", StrykerConfig.getInstance().getFileSeparator()) + ".java.tmp");
        String classNameWithNoPackage = removePackageFromName(className);
        boolean result = false;
        try {
            FileOutputStream fos = null;
            String emptyConstructorRegExp = classNameWithNoPackage+ "([ ])*" + "\\(([ ])*\\)";
            Pattern pattern = Pattern.compile(emptyConstructorRegExp);
            if (mode.equals(Mode.REMOVE) || mode.equals(Mode.WRITE)) {
            	destFile.createNewFile();
            	fos = new FileOutputStream(destFile);
            }
            Scanner scan = new Scanner(origFile);
            scan.useDelimiter("\n");
            String str = null;
            boolean constructorFound = false;
            while(scan.hasNext()){
                str = scan.next();
                if (pattern.matcher(str).find()) {
                	constructorFound = true;
                	if (mode.equals(Mode.FIND)) {
                		result = true;
                		break;
                	} else if (mode.equals(Mode.WRITE)) {
                		//this method was called to write an empty constructor but it's already present
                		result = false;
                		break;
                	} else if (mode.equals(Mode.REMOVE) && str.endsWith("}")) {
                		//the empty constructor only occupies one line
                		constructorFound = false;
                		result = true;
                	}
                } else if (str.contains("}")){
                    if (constructorFound && mode.equals(Mode.REMOVE)) {
                    	constructorFound = false;
                    	result = true;
                    } else {
                    	fos.write((str + "\n").getBytes(Charset.forName("UTF-8")));
                    }
                } else if ((str.isEmpty() || str.trim().isEmpty()) && mode.equals(Mode.WRITE)) {
                	//found empty line where to add the empty constructor
                	fos.write(("\n\tpublic " + classNameWithNoPackage + "() {}" + "\n\n").getBytes(Charset.forName("UTF-8")));
                } else {
                	if (!(constructorFound && mode.equals(Mode.REMOVE))) {
                		fos.write((str + "\n").getBytes(Charset.forName("UTF-8")));
                	}
                }
            }
            if (fos != null) fos.close();
            scan.close();
            if ((mode.equals(Mode.REMOVE) || mode.equals(Mode.WRITE)) && result) {
            	Files.copy(destFile, origFile);
            	result = destFile.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }
	
}

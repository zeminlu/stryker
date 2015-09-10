package tools;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeSet;

import tools.utils.UpdateCommand;
import config.StrykerConfig;
import ar.edu.taco.TacoException;
import ar.edu.taco.junit.RecoveredInformation;
import ar.edu.taco.junit.RecoveredInformation.StaticFieldInformation;

/**
 * Class used to create a new test, this class is based on {@link ar.edu.taco.junit.UnitTestBuilder}
 * 
 * @author Simon Emmanuel Gutierrez Brida
 * @version 0.1.8
 */
public class TestBuilder {
	private static final String THIZ_0 = "thiz_0";

    final static private String PACKAGE_NAME = StrykerConfig.getInstance().getTestsPackage();
    
    private Set<String> imports = new TreeSet<String>();
    
    static private int testIndex = 0;

    private RecoveredInformation recoveredInformation;

    // Keep the variables and objects that have already been created. 
    // We use the identityHashCode of each object as the Key and the created variable name as Value
    private Map<Integer, String> createdInstances = new HashMap<Integer, String>();

    private Map<Object, Integer> instancesIndex = new HashMap<Object, Integer>();

    public TestBuilder(RecoveredInformation recoveredInformation) {
        this.recoveredInformation = recoveredInformation;
    }


    /**
     * 
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException 
     */
    public String createUnitTest() throws IllegalArgumentException, IllegalAccessException, InstantiationException, SecurityException, IOException {
        if (!this.recoveredInformation.isValidInformation()) {
            return null;
        }

        String className = TestBuilder.PACKAGE_NAME + "." + recoveredInformation.getClassToCheck().substring(recoveredInformation.getClassToCheck().lastIndexOf(".") + 1) + "Test";
        String methodName = recoveredInformation.getMethodToCheck();

        Class<?> clazz;
        clazz = JavaCompilerAPI.getInstance().loadClass(recoveredInformation.getClassToCheck());
        if (clazz == null) return null;
        
        Method methodToCheck = null;
        for (Method aMethod : clazz.getDeclaredMethods()) {
            if (	aMethod.getName().equals(recoveredInformation.getMethodToCheck())
            		&&
            		aMethod.getParameterTypes().length == recoveredInformation.getMethodParametersNames().size()) {
                methodToCheck = aMethod;
                break;
            }
        }

        List<String> initializations = new LinkedList<String>();
        Set<UpdateCommand> updates = new TreeSet<UpdateCommand>();
        List<String> params = null;
        Set<String> ignoreImports = new TreeSet<String>();
        //String packageToIgnore = recoveredInformation.getClassToCheck().substring(0, recoveredInformation.getClassToCheck().lastIndexOf('.')) + ".*";
        //ignoreImports.add(packageToIgnore);
        ignoreImports.add("java.lang.Boolean");
        ignoreImports.add("java.lang.Integer");
        ignoreImports.add("java.lang.Short");
        ignoreImports.add("java.lang.Byte");
        ignoreImports.add("java.lang.Long");
        ignoreImports.add("java.lang.Float");
        ignoreImports.add("java.lang.Double");
        ignoreImports.add("java.lang.String");
        ignoreImports.add("java.lang.Object");
        
        List<String> objectDefinitionStatements = new ArrayList<String>();
        List<String> objectInitializationStatements = new ArrayList<String>();

        imports.add("java.lang.reflect.Field");
        imports.add("java.lang.reflect.Method");
        imports.add("java.lang.IllegalAccessException");
        imports.add("java.lang.reflect.InvocationTargetException");
        imports.add("java.lang.InstantiationException");
        
        boolean isStatic = Modifier.isStatic(methodToCheck.getModifiers());
        Object thizInstance = null;
        if (recoveredInformation.getSnapshot().get(THIZ_0) != null) {
            thizInstance = recoveredInformation.getSnapshot().get(THIZ_0);
        } else if (!isStatic){
        	thizInstance = clazz.newInstance();
        }
        
        this.createdInstances.put(System.identityHashCode(thizInstance), "instance");
        
        // Fields initialization
        if (thizInstance != null) {
            getFieldsInitializationStatements(clazz, thizInstance, objectDefinitionStatements, objectInitializationStatements, updates);
        }


        // Static Fields initialization
        getStaticFieldsInitializationStatements(clazz, "instance", objectDefinitionStatements, objectInitializationStatements, updates);

        params = getParametersInitializationStatements(clazz, objectDefinitionStatements, objectInitializationStatements, updates);
        
        initializations.addAll(objectDefinitionStatements);
        initializations.addAll(objectInitializationStatements);
        
        // Write JUnit to File

        String outputClassName = className + "_" + methodName + "_" + getSuffix();
        
        TestWritter testWritter = new TestWritter(TestBuilder.PACKAGE_NAME, imports, outputClassName, isStatic, initializations, updates, params, ignoreImports);
        return testWritter.writeTest();
    }

    /**
     * Generate the statements with the initialization of each static field
     * 
     * @param clazz
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private void getStaticFieldsInitializationStatements(Class<?> clazz, String storedVariableName, List<String> objectDefinitionStatements, List<String> objectInitializationStatements, Set<UpdateCommand> updates) throws IllegalArgumentException, IllegalAccessException {

        if (!clazz.isAssignableFrom(Integer.class) && !clazz.isAssignableFrom(Long.class) && !clazz.isAssignableFrom(Float.class)){

            List<StaticFieldInformation> staticFields = recoveredInformation.getStaticFieldsNameForClass(recoveredInformation.getClassToCheck());
            
            List<String> shortFieldNames = new ArrayList<String>();
            String moduleName = getModuleName(clazz);
            for (StaticFieldInformation staticField : staticFields) {
                if (staticField.getFieldName().matches("(roops_goal|myRoopsArray).*")) //Hack in order to ignore goals added by fajita
                    continue;
                String shortFieldName = staticField.getFieldName().replace(moduleName + "_", "");
                shortFieldNames.add(shortFieldName);
            }

            if (!shortFieldNames.isEmpty()) {


                for (String shortFieldName : shortFieldNames) {
                    Field field = null;
                    try {
                        field = clazz.getDeclaredField(shortFieldName);
                    } catch (SecurityException e) {
                        throw new RuntimeException("DYNJALLOY ERROR! " + e.getMessage());
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException("DYNJALLOY ERROR! " + e.getMessage());
                    }

                    if (field.getType().isPrimitive()) {
                        String value = getValueForPrimitiveTypeField(field, null);
                        updates.add(new UpdateCommand("instance", shortFieldName, value));
                    } else if (field.getType().isArray()) {
                        Class<?> componentType = field.getType().getComponentType();
                        field.setAccessible(true);
                        Object fieldValue = field.get(null);

                        if (fieldValue != null) {
                            if (this.createdInstances.containsKey(System.identityHashCode(fieldValue))) {
                                updates.add(new UpdateCommand(storedVariableName, shortFieldName, this.createdInstances.get(System.identityHashCode(fieldValue))));
                            } else {
                                String arrayObjectVariableName = generateVariableName(fieldValue);
                                int instanceLength = Array.getLength(fieldValue);

                                this.createdInstances.put(System.identityHashCode(fieldValue), arrayObjectVariableName);
                                String statement = field.getType().getCanonicalName() + " " + arrayObjectVariableName + " = new " + componentType.getName() + "[" + instanceLength + "]";
                                objectDefinitionStatements.add(statement);
                                
                                updates.add(new UpdateCommand(storedVariableName, shortFieldName, arrayObjectVariableName));
                                getValueForArray(componentType, fieldValue, objectDefinitionStatements, objectInitializationStatements);
                            }
                        }

                    } else {
                    	String abstractClass = null;
                    	String abstractClassSimple = null;
                    	String concreteClass = null;
                    	String concreteClassSimple = null;
                    	Object fieldValue = field.get(null);
                    	if (List.class.isAssignableFrom(field.getType())) {
                    		abstractClass = "java.util.List";
                    		concreteClass = "java.util.ArrayList";
                    	} else if (Set.class.isAssignableFrom(field.getType())) {
                    		abstractClass = "java.util.Set";
                    		concreteClass = "java.util.TreeSet";
                    	} else if (Map.class.isAssignableFrom(field.getType())) {
                    		abstractClass = "java.util.Map";
                            concreteClass = "java.util.IdentityHashMap";
                    	} else if (Object.class.isAssignableFrom(field.getType().getClass())) {
                    		abstractClass = fieldValue.getClass().getCanonicalName();
                    		concreteClass = fieldValue.getClass().getCanonicalName();
                    	}
                    	imports.add(abstractClass);
                    	imports.add(concreteClass);
                    	int lastAbstractDot = abstractClass.lastIndexOf('.')+1;
                    	int lastConcreteDot = concreteClass.lastIndexOf('.')+1;
                    	abstractClassSimple = abstractClass.substring(lastAbstractDot, abstractClass.length());
                    	concreteClassSimple = concreteClass.substring(lastConcreteDot, concreteClass.length());
                    	if (fieldValue != null) {
                    		if (!this.createdInstances.containsKey(System.identityHashCode(fieldValue))) {
                    			String buildVariable = generateVariableName(fieldValue);
                    			String buildStatement = abstractClassSimple + " " + buildVariable + " = new " + concreteClassSimple + "()";
                                this.createdInstances.put(System.identityHashCode(fieldValue), buildVariable);
                                objectDefinitionStatements.add(buildStatement);
                                getStatementsForCollection(buildVariable, fieldValue, objectDefinitionStatements, objectInitializationStatements, updates);
                            }
                    	} else {
                    		updates.add(new UpdateCommand(storedVariableName, shortFieldName, "null"));
                    	}
                    	
                    }
                }
            }
        }

    }

    /**
     * @param value
     * @return
     */
    private String generateVariableName(Object value) {
        if (value == null) {
            return "null";
        }

        if (this.createdInstances.containsKey(System.identityHashCode(value))) {
            return this.createdInstances.get(System.identityHashCode(value));
        }

        int index;
        if (instancesIndex.containsKey(value.getClass())) {
            index = instancesIndex.get(value.getClass());
        } else {
            index = 0;
        }
        index++;
        instancesIndex.put(value.getClass(), index);
    
        String instanceName = "";
        // remove "_NNN" from "xxxx_NNN".
        int lastUnderscore = instanceName.lastIndexOf("_");
        if (lastUnderscore >= 0) {
            String stringBeforeLastUnderscore = instanceName.substring(lastUnderscore + 1);
            if (stringBeforeLastUnderscore.matches("[0-9]+")) {
                instanceName = instanceName.substring(0, lastUnderscore);
            }
        }

        String className = value.getClass().getSimpleName();
        className = className.replaceAll("\\[\\]", "\\_array");
        String retValue = instanceName + "_" + className + "_" + index;

        return retValue;
    }


    /**
     * 
     * @param clazz
     * @param instance
     * @param buildVariableName
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private void getFieldsInitializationStatements(Class<?> clazz, Object instance, List<String> objectDefinitionStatements, List<String> objectInitializationStatements, Set<UpdateCommand> updates) throws IllegalArgumentException, IllegalAccessException {
    	if (clazz.getDeclaredFields().length > 0 && !clazz.isAssignableFrom(Integer.class) && !clazz.isAssignableFrom(Long.class) && !clazz.isAssignableFrom(Float.class)) {
            String instanceGeneratedVariableName = generateVariableName(instance);

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (!Modifier.isStatic(field.getModifiers())) {
                    String shortFieldName = field.getName();

                    if (field.getType().isPrimitive() || this.isAutoboxingClass(field.getType())) {
                        String value = getValueForPrimitiveTypeField(field, instance);
                        updates.add(new UpdateCommand(instanceGeneratedVariableName, shortFieldName, value));
                    } else if (field.getType().isArray()) {
                        Class<?> componentType = field.getType().getComponentType();
                        Object fieldValue = field.get(instance);
                        //DPD
                        if (fieldValue != null) {
                            if (this.createdInstances.containsKey(System.identityHashCode(fieldValue))) {
                            	updates.add(new UpdateCommand(instanceGeneratedVariableName, shortFieldName, this.createdInstances.get(System.identityHashCode(fieldValue))));
                            } else {
                                String arrayObjectVariableName = generateVariableName(fieldValue);
                                int instanceLength = Array.getLength(fieldValue);

                                this.createdInstances.put(System.identityHashCode(fieldValue), arrayObjectVariableName);
                                String statement = field.getType().getCanonicalName() + " " + arrayObjectVariableName + " = new " + componentType.getName() + "[" + instanceLength + "]";
                                objectDefinitionStatements.add(statement);
                                
                                updates.add(new UpdateCommand(instanceGeneratedVariableName, shortFieldName, arrayObjectVariableName));
                                getValueForArray(componentType, fieldValue, objectDefinitionStatements, objectInitializationStatements);

                            }
                        }

                    } else {
                    	String abstractClass = null;
                    	String abstractClassSimple = null;
                    	String concreteClass = null;
                    	String concreteClassSimple = null;
                    	Object fieldValue = field.get(instance);
                    	String buildVariable = generateVariableName(fieldValue);
                    	boolean isObject = false;
                    	boolean isList = false;
                    	boolean isSet = false;
                    	boolean isMap = false;
                    	if (List.class.isAssignableFrom(field.getType())) {
                    		abstractClass = "java.util.List";
                    		concreteClass = "java.util.ArrayList";
                    		isList = true;
                    	} else if (Set.class.isAssignableFrom(field.getType())) {
                    		abstractClass = "java.util.Set";
                            concreteClass = "java.util.TreeSet";
                            isSet = true;
                    	} else if (Map.class.isAssignableFrom(field.getType())) {
                    		abstractClass = "java.util.Map";
                            concreteClass = "java.util.IdentityHashMap";
                            isMap = true;
                    	} else if (Object.class.isAssignableFrom(field.getType())) {
                    		isObject = true;
                    		abstractClass = fieldValue!=null?fieldValue.getClass().getCanonicalName():null;
                    		concreteClass = abstractClass;
                    	}
                    	
                    	if (abstractClass != null) {
                    		imports.add(abstractClass);
                    		int abstractClassLastDot = abstractClass.lastIndexOf('.')+1;
                    		abstractClassSimple = abstractClass.substring(abstractClassLastDot, abstractClass.length());
                    	}
                    	
                    	if (concreteClass != null) {
                    		imports.add(concreteClass);
                    		int concreteClassLastDot = concreteClass.lastIndexOf('.')+1;
                    		concreteClassSimple = concreteClass.substring(concreteClassLastDot, concreteClass.length());
                    	}
                    	if (fieldValue == null) {
                    		updates.add(new UpdateCommand(instanceGeneratedVariableName, shortFieldName, "null"));
                    	} else if (!this.createdInstances.containsKey(System.identityHashCode(fieldValue))) {
                    		String buildStatement = null;
                    		//BUILD STATEMENT DEFINITION+++
                    		if (!isObject) {
                    			buildStatement = abstractClassSimple + " " + buildVariable + " = new " + concreteClassSimple + "()";
                    		} else {
                                Constructor<?>[] cons = fieldValue.getClass().getConstructors();
                                Constructor<?> c = cons[0];
                                Class<?>[] parTypes = null;
                                parTypes = c.getParameterTypes();
                                Object[] concretePars = new Object[parTypes.length]; 
                                int index = 0;
                                for (Class<?> cl : parTypes){
                                    if (cl.isPrimitive()){
                                        if (cl.getName().equals("byte"))
                                            concretePars[index] = 0;
                                        if (cl.getName().equals("short"))
                                            concretePars[index] = 0;
                                        if (cl.getName().equals("int"))
                                            concretePars[index] = 0;
                                        if (cl.getName().equals("long"))
                                            concretePars[index] = 0L;
                                        if (cl.getName().equals("float"))
                                            concretePars[index] = 0.0f;
                                        if (cl.getName().equals("double"))
                                            concretePars[index] = 0.0d;
                                        if (cl.getName().equals("char"))
                                            concretePars[index] = '\u0000';
                                        if (cl.getName().equals("boolean"))
                                            concretePars[index] = false;
                                    } else {
                                        try {
                                            concretePars[index] = cl.newInstance();
                                        } catch (InstantiationException ie){
                                            concretePars[index] = null;
                                        }
                                    }
                                    index++;
                                }
                                buildStatement = abstractClassSimple + " " + buildVariable +" = new "  + concreteClassSimple + "(";
                                if (concretePars != null){
                                    for (int idx = 0; idx < concretePars.length; idx++){
                                        if (parTypes[idx].isPrimitive()){
                                            buildStatement += concretePars[idx].toString();
                                            if (parTypes[idx].getSimpleName().equals("float"))
                                                buildStatement += "f";
                                            if (parTypes[idx].getSimpleName().equals("double"))
                                                buildStatement += "d";    
                                        } else
                                            buildStatement += "(" + parTypes[idx].getSimpleName() + ")null";
                                        if (idx < concretePars.length - 1)
                                            buildStatement += ",";
                                    }
                                }
                                buildStatement += ")";
                    		}
                    		//BUILD STATEMENT DEFINITION---
                    		this.createdInstances.put(System.identityHashCode(fieldValue), buildVariable);
                            objectDefinitionStatements.add(buildStatement);
                            //GET STATEMENTS+++
                            if (isList || isSet) {
                            	getStatementsForCollection(buildVariable, fieldValue, objectDefinitionStatements, objectInitializationStatements, updates);
                            } else if (isMap) {
                            	getStatementsForMap(buildVariable, fieldValue, objectDefinitionStatements, objectInitializationStatements, updates);
                            } else if (isObject) {
                            	getFieldsInitializationStatements(field.getType(), fieldValue, objectDefinitionStatements, objectInitializationStatements, updates);
                            }
                            //GET STATEMENTS---
                    	}
                    	updates.add(new UpdateCommand(instanceGeneratedVariableName, shortFieldName, buildVariable));
                    }
                }
            }
    	}

    }


    /**
     * 
     * @param clazz
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private List<String> getParametersInitializationStatements(Class<?> clazz, List<String> objectDefinitionStatements, List<String> objectInitializationStatements, Set<UpdateCommand> updates) throws InstantiationException, IllegalAccessException {
        List<String> paramsNames = new ArrayList<String>();

        if (recoveredInformation.getMethodParametersNames().size() > 0) {

            // Gets parameters types
            Class<?>[] parameterTypes = new Class<?>[recoveredInformation.getMethodParametersNames().size()];
            for (Method aMethod : clazz.getDeclaredMethods()) {
                if (aMethod.getName().equals(recoveredInformation.getMethodToCheck())) {
                    parameterTypes = aMethod.getParameterTypes();
                }
            }

            for (int index = 0; index < parameterTypes.length; index++){
                String aParameterName = recoveredInformation.getMethodParametersNames().get(index);

                Class<?> parameterType = parameterTypes[index];

                Object parameterInstance;
                if (recoveredInformation.getSnapshot().containsKey(aParameterName + "_0")) {
                    parameterInstance = recoveredInformation.getSnapshot().get(aParameterName + "_0");
                } else {
                    parameterInstance = defaultValue(parameterType);
                }

                String generatedName = createStatementsForParameter(parameterType, aParameterName, parameterInstance, objectDefinitionStatements, objectInitializationStatements, updates);

                paramsNames.add(generatedName);
            }

        }
        return paramsNames;
    }

    private Object defaultValue(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        Object value;
        if (clazz.isPrimitive()) {
            String typeSimpleName = clazz.getSimpleName();

            if (typeSimpleName.equals("boolean")) {
                value = false;
            } else if (typeSimpleName.endsWith("byte")) {
                value = 0;
            } else if (typeSimpleName.endsWith("char")) {
                value = "'a'";
            } else if (typeSimpleName.endsWith("double")) {
                value = 0;
            } else if (typeSimpleName.endsWith("float")) {
                value = 0;
            } else if (typeSimpleName.endsWith("int")) {
                value = 0;
            } else if (typeSimpleName.endsWith("long")) {
                value = 0L;
            } else if (typeSimpleName.endsWith("short")) {
                value = 0;
            } else {
                throw new TacoException("ERROR: Undefined in class UnitTestBuilder, method defaultValue");
            }
        } else {
            String name = clazz.getName();
            if (name.equals("java.lang.Boolean")) {
                value = false;
            } else if (name.endsWith("java.lang.Byte")) {
                value = 0;
            } else if (name.endsWith("java.lang.Character")) {
                value = "'a'";
            } else if (name.endsWith("java.lang.Double")) {
                value = 0;
            } else if (name.endsWith("java.lang.Float")) {
                value = 0;
            } else if (name.endsWith("java.lang.Integer")) {
                value = 0;
            } else if (name.endsWith("java.lang.Long")) {
                value = 0;
            } else if (name.endsWith("java.lang.Short")) {
                value = 0;
            } else {
                value = clazz.newInstance();
            }
        }
        return value;
    }

    /**
     * 
     * @param clazz
     * @param instance
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InstantiationException 
     */
    private String createStatementsForParameter(Class<?> clazz, String parameterName, Object instance, List<String> objectDefinitionStatements, List<String> objectInitializationStatements, Set<UpdateCommand> updates) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        String generatedVariableName = parameterName;

        if (!this.createdInstances.containsKey(System.identityHashCode(instance))) {

            Object parameterValue = this.recoveredInformation.getSnapshot().get(parameterName + "_0");

            if (clazz.isPrimitive() || isAutoboxingClass(clazz)) {
                String value;
                if (parameterValue == null) {
                    value = String.valueOf(defaultValue(clazz));
                } else {
                    if (Character.class.isAssignableFrom(clazz)) {
                        value = "'" + String.valueOf(parameterValue) + "'";
                    } else {
                        if (parameterValue instanceof Long) {
                            value = String.valueOf(parameterValue) + "L";
                            instance = new Long(Long.parseLong(value));
                        } else if (parameterValue instanceof Float) {
                            if (((Float)parameterValue).isNaN())
                                value = "Float.NaN";
                            else if (((Float)parameterValue).isInfinite() && (Float)parameterValue > 0f)
                                value = "Float.POSITIVE_INFINITY";
                            else if (((Float)parameterValue).isInfinite() && (Float)parameterValue < 0f) {
                                value = "Float.NEGATIVE_INFINITY";
                            } else 
                                value = String.valueOf((Float)parameterValue) + "f";
                            instance = new Float(Float.parseFloat(value));
                        } else if (parameterValue instanceof Integer) {
                            value = String.valueOf(parameterValue);
                            instance = new Integer(Integer.parseInt(value));
                        } else 
                            value = String.valueOf(parameterValue);
                    }

                }

                //DPD VAR NAME fix
                objectDefinitionStatements.add(clazz.getCanonicalName() + " " + generatedVariableName + " = " + value);

            } else if (parameterValue == null) {
                //DPD NULL CASE
                String statement = clazz.getCanonicalName() + " " + generatedVariableName + " = null";
                this.createdInstances.put(System.identityHashCode(instance), generatedVariableName);
                objectDefinitionStatements.add(statement);

            } else {
            	Object fieldValue = instance;
            	String abstractClass = null;
            	String abstractClassSimple = null;
            	String concreteClass = null;
            	String concreteClassSimple = null;
            	boolean isArray = false;
            	boolean isList = false;
            	boolean isSet = false;
            	boolean isMap = false;
            	boolean isObject = false;
            	String buildStatement = null;
            	if (clazz.isArray()) {
            		isArray = true;
            	} else if (List.class.isAssignableFrom(clazz)) {
            		isList = true;
            		abstractClass = "java.util.List";
            		concreteClass = "java.util.ArrayList";
            	} else if (Set.class.isAssignableFrom(clazz)) {
            		isSet = true;
            		abstractClass = "java.util.Set";
            		concreteClass = "java.util.TreeSet";
            	} else if (Map.class.isAssignableFrom(clazz)) {
            		isMap = true;
            		abstractClass = "java.util.Map";
            		concreteClass = "java.util.IdentityHashMap";
            	} else if (Object.class.isAssignableFrom(clazz)) {
            		isObject = true;
            		abstractClass = clazz.getCanonicalName();
            		concreteClass = abstractClass;
            	}
            	if (abstractClass != null) {
            		imports.add(abstractClass);
            		int lastAbstractClassDot = abstractClass.lastIndexOf('.')+1;
            		abstractClassSimple = abstractClass.substring(lastAbstractClassDot, abstractClass.length());
            	}
        		if (concreteClass != null) {
        			imports.add(concreteClass);
        			int lastConcreteClassDot = concreteClass.lastIndexOf('.')+1;
            		concreteClassSimple = concreteClass.substring(lastConcreteClassDot, concreteClass.length());
        		}
            	if (isArray && instance != null) {
            		Class<?> componentType = clazz.getComponentType();
                    int instanceLength = Array.getLength(instance);
            		getValueForArray(componentType, parameterValue, objectDefinitionStatements, objectInitializationStatements);
            		buildStatement = clazz.getCanonicalName() + " " + generatedVariableName + " = new " + componentType.getName() + "[" + instanceLength + "]";
            	} else if (!isObject || (isArray && instance == null)) {
            		if (fieldValue == null) {
            			objectDefinitionStatements.add(clazz.getCanonicalName() + " " + generatedVariableName + " = null");
                        this.createdInstances.put(System.identityHashCode(instance), generatedVariableName);
            		} else {
            			buildStatement = abstractClassSimple + " " + generatedVariableName + " = new " + concreteClassSimple + "()";
            		}
            	} else if (isObject) {;
                    Constructor<?>[] cons = clazz.getConstructors();
                    Constructor<?> c = cons[0];
                    Class<?>[] parTypes = null;
                    parTypes = c.getParameterTypes();
                    Object[] concretePars = new Object[parTypes.length]; 
                    int index = 0;
                    for (Class<?> cl : parTypes){
                        if (cl.isPrimitive()){
                            if (cl.getName().equals("byte"))
                                concretePars[index] = 0;
                            if (cl.getName().equals("short"))
                                concretePars[index] = 0;
                            if (cl.getName().equals("int"))
                                concretePars[index] = 0;
                            if (cl.getName().equals("long"))
                                concretePars[index] = 0L;
                            if (cl.getName().equals("float"))
                                concretePars[index] = 0.0f;
                            if (cl.getName().equals("double"))
                                concretePars[index] = 0.0d;
                            if (cl.getName().equals("char"))
                                concretePars[index] = '\u0000';
                            if (cl.getName().equals("boolean"))
                                concretePars[index] = false;
                        } else {
                            concretePars[index] = null;
                        }
                        index++;
                    }
                    buildStatement = abstractClassSimple + " " + generatedVariableName +" = new "  + concreteClassSimple + "(";
                    if (concretePars != null){
                        for (int idx = 0; idx < concretePars.length; idx++){
                            if (parTypes[index].isPrimitive()){
                                buildStatement += concretePars[idx].toString();
                                if (parTypes[idx].getSimpleName().equals("float"))
                                    buildStatement += "f";
                                if (parTypes[idx].getSimpleName().equals("double"))
                                    buildStatement += "d";    
                            } else
                                buildStatement += "null";
                            if (idx < concretePars.length - 1)
                                buildStatement += ",";
                        }
                    }
                    buildStatement += ")";
            	}
            	objectDefinitionStatements.add(buildStatement);
            	this.createdInstances.put(System.identityHashCode(instance), generatedVariableName);
            	if (isList || isSet) {
            		getStatementsForCollection(generatedVariableName, fieldValue, objectDefinitionStatements, objectInitializationStatements, updates);
            	} else if (isMap) {
            		getStatementsForMap(generatedVariableName, fieldValue, objectDefinitionStatements, objectInitializationStatements, updates);
            	} else if (isObject) {
            		getFieldsInitializationStatements(clazz, instance, objectDefinitionStatements, objectInitializationStatements, updates);
            	}
            } 
        } else {
        	String currentVarHoldingTheInstance = this.createdInstances.get(System.identityHashCode(instance));
            objectInitializationStatements.add(clazz.getCanonicalName() + " " + generatedVariableName + " = " + currentVarHoldingTheInstance);
        }
        return generatedVariableName;
    }

    /**
     * 
     * @param fieldValue
     * @return
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    private void getStatementsForCollection(String variableName, Object fieldValue, List<String> objectDefinitionStatements, List<String> objectInitializationStatements, Set<UpdateCommand> updates) throws IllegalArgumentException, IllegalAccessException {
        Collection<?> listFieldValue = (Collection<?>) fieldValue;
        for (Object value : listFieldValue) {
            Class<?> clazz;
            if (value == null) {
                clazz = null;
            } else {
                clazz = value.getClass();
            }

            if (value == null) {
                objectInitializationStatements.add(variableName + ".add(null)");
            } else if (isAutoboxingClass(clazz)) {
                String contentValue;
                if (Character.class.isAssignableFrom(value.getClass())) {
                    contentValue = "'" + String.valueOf(value) + "'";
                } else {
                    contentValue = String.valueOf(value);
                }

                objectInitializationStatements.add(variableName + ".add(" + contentValue + ")");

            } else {
            	String variableToCreate = generateVariableName(value);
            	boolean isArray = false;
            	boolean isList = false;
            	boolean isSet = false;
            	boolean isMap = false;
            	boolean isObject = false;
            	String abstractClass = null;
            	String abstractClassSimple = null;
            	String concreteClass = null;
            	String concreteClassSimple = null;
            	String buildStatement = null;
            	if (clazz.isArray()) {
            		isArray = true;
            	} else if (List.class.isAssignableFrom(clazz)) {
            		isList = true;
            		abstractClass = "java.util.List";
            		concreteClass = "java.util.ArrayList";
            	} else if (Set.class.isAssignableFrom(clazz)) {
            		isSet = true;
            		abstractClass = "java.util.Set";
            		concreteClass = "java.util.TreeSet";
            	} else if (Map.class.isAssignableFrom(clazz)) {
            		isMap = true;
            		abstractClass = "java.util.Map";
            		concreteClass = "java.util.IdentityHashMap";
            	} else {
            		isObject = true;
            		abstractClass = value.getClass().getCanonicalName();
            		concreteClass = abstractClass;
            	}
            	if (abstractClass != null) {
            		imports.add(abstractClass);
            		int abstractClassLastDot = abstractClass.lastIndexOf('.')+1;
            		abstractClassSimple = abstractClass.substring(abstractClassLastDot, abstractClass.length());
            	}
            	if (concreteClass != null) {
            		imports.add(concreteClass);
            		int concreteClassLastDot = concreteClass.lastIndexOf('.')+1;
        			concreteClassSimple = concreteClass.substring(concreteClassLastDot, concreteClass.length());
            	}
            	if (!this.createdInstances.containsKey(System.identityHashCode(value))) {
            		this.createdInstances.put(System.identityHashCode(value), variableToCreate);
            		if (isArray) {
            			Class<?> componentType = clazz.getComponentType();
            			buildStatement = clazz.getCanonicalName() + " " + variableToCreate + " = new " + clazz.getCanonicalName();
            			objectDefinitionStatements.add(buildStatement);
            			getValueForArray(componentType, value, objectDefinitionStatements, objectInitializationStatements);
            		} else if (!isObject) {
            			buildStatement = abstractClassSimple + " " + variableToCreate + " = new " + concreteClassSimple + "()";
            			objectDefinitionStatements.add(buildStatement);
            			if (isList || isSet) getStatementsForCollection(variableToCreate, value, objectDefinitionStatements, objectInitializationStatements, updates);
            			else if (isMap) getStatementsForMap(variableToCreate, value, objectDefinitionStatements, objectInitializationStatements, updates);
            		} else if (isObject) {
            			if (!hasDefaultConstructor(value.getClass())) {
                            throw new RuntimeException("DYNJALLOY ERROR!: Type: " + value.getClass().getCanonicalName() + " has no default Constructor.");
                        }
            			buildStatement = abstractClassSimple + " " + variableToCreate + " = new " + concreteClassSimple + "()";
            			objectDefinitionStatements.add(buildStatement);
            			getFieldsInitializationStatements(value.getClass(), value, objectDefinitionStatements, objectInitializationStatements, updates);
            		}
            	}
            	objectInitializationStatements.add(variableName + ".add(" + variableToCreate + ")");
            }
        }
    }

    /**
     * 
     * @param buildVariable
     * @param fieldValue
     * @return
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    private void getStatementsForMap(String variableName, Object fieldValue, List<String> objectDefinitionStatements, List<String> objectInitializationStatements, Set<UpdateCommand> updates) throws IllegalArgumentException, IllegalAccessException {
        Map<?, ?> mapFieldValue = (Map<?, ?>) fieldValue;
        for (Entry<?, ?> anEntry : mapFieldValue.entrySet()) {
        	Object keyValue = anEntry.getKey();
            String keyString = getKeyValueString(variableName, fieldValue, objectDefinitionStatements, objectInitializationStatements, updates, keyValue);
            Object value = anEntry.getValue();
            String valueString = getKeyValueString(variableName, fieldValue, objectDefinitionStatements, objectInitializationStatements, updates, value);
            objectInitializationStatements.add(variableName + ".put(" + keyString + ", " + valueString + ")");

        }
    }
    
    private String getKeyValueString(String variableName, Object fieldValue, List<String> objectDefinitionStatements, List<String> objectInitializationStatements, Set<UpdateCommand> updates, Object keyValueObject) throws IllegalArgumentException, IllegalAccessException {
    	String keyValueString = null;

        Class<?> clazz;
        if (keyValueObject == null) {
            clazz = null;
        } else {
            clazz = keyValueObject.getClass();
        }

        if (keyValueObject == null) {
            keyValueString = "null";
        } else if (isAutoboxingClass(keyValueObject.getClass())) {
            if (Character.class.isAssignableFrom(keyValueObject.getClass())) {
                keyValueString = "'" + String.valueOf(keyValueObject) + "'";
            } else {
                keyValueString = String.valueOf(keyValueObject);
            }
        } else {
        	String variableToCreate = generateVariableName(keyValueObject);
        	keyValueString = variableToCreate;
        	String buildStatement = null;
        	boolean isArray = false;
        	boolean isList = false;
        	boolean isSet = false;
        	boolean isMap = false;
        	boolean isObject = false;
        	Class<?> componentType = null;
        	String abstractClass = null;
        	String concreteClass = null;
        	String abstractClassSimple = null;
        	String concreteClassSimple = null;
        	if (!this.createdInstances.containsKey(System.identityHashCode(keyValueObject))) {
        		this.createdInstances.put(System.identityHashCode(keyValueObject), variableToCreate);
        		if (clazz.isArray()) {
        			isArray = true;
        			componentType = clazz.getComponentType();
        			buildStatement = clazz.getCanonicalName() + " " + variableToCreate + " = new " + clazz.getCanonicalName();
        		} else if (List.class.isAssignableFrom(clazz)) {
        			isList = true;
        			abstractClass = "java.util.List";
        			concreteClass = "java.util.ArrayList";
        		} else if (Set.class.isAssignableFrom(clazz)) {
        			isSet = true;
        			abstractClass = "java.util.Set";
        			concreteClass = "java.util.TreeSet";
        		} else if (Map.class.isAssignableFrom(clazz)) {
        			isMap = true;
        			abstractClass = "java.util.Map";
        			concreteClass = "java.util.IdentityHashMap";
        		} else {
        			isObject = true;
        			abstractClass = keyValueObject.getClass().getCanonicalName();
        			concreteClass = abstractClass;
        		}
        		if (abstractClass != null) {
        			imports.add(abstractClass);
        			int abstractClassLastDot = abstractClass.lastIndexOf('.')+1;
        			abstractClassSimple = abstractClass.substring(abstractClassLastDot, abstractClass.length());
        		}
        		if (concreteClass != null) {
        			imports.add(concreteClass);
        			int concreteClassLastDot = concreteClass.lastIndexOf('.')+1;
        			concreteClassSimple = concreteClass.substring(concreteClassLastDot, concreteClass.length());
        		}
        		if (isArray) {
        			objectDefinitionStatements.add(buildStatement);
        			getValueForArray(componentType, keyValueObject, objectDefinitionStatements, objectInitializationStatements);
        		} else if (!isObject) {
        			buildStatement = abstractClassSimple + " " + variableToCreate + " = new " + concreteClassSimple + "()";
        			objectDefinitionStatements.add(buildStatement);
        			if (isList || isSet) getStatementsForCollection(variableToCreate, keyValueObject, objectDefinitionStatements, objectInitializationStatements, updates);
        			else if (isMap) getStatementsForMap(variableToCreate, keyValueObject, objectDefinitionStatements, objectInitializationStatements, updates);
        		} else if (isObject) {
        			if (!hasDefaultConstructor(keyValueObject.getClass())) {
                        throw new RuntimeException("DYNJALLOY ERROR!: Type: " + keyValueObject.getClass().getCanonicalName() + " has no default Constructor.");
                    }
        			buildStatement = abstractClassSimple + " " + variableToCreate + " = new " + concreteClassSimple + "()";
        			objectDefinitionStatements.add(buildStatement);
        			getFieldsInitializationStatements(keyValueObject.getClass(), keyValueObject, objectDefinitionStatements, objectInitializationStatements, updates);
        		}
        	}
        }
        return keyValueString;
    }

    
    private String valueAsString(Class<?> componentType, Object fieldValue, int x) {
    	String value = null;
    	if (componentType.isPrimitive()) {
            String typeSimpleName = componentType.getSimpleName();
                Object elementAsObject = Array.get(fieldValue, x);
                if (elementAsObject == null) {
                    if (typeSimpleName.equals("boolean")) {
                    	value = "false";
                    } else if (typeSimpleName.endsWith("byte") || typeSimpleName.endsWith("int") || typeSimpleName.endsWith("short")) {
                    	value = "0";
                    } else if (typeSimpleName.endsWith("char")) {
                    	value = "'a'";
                    } else if (typeSimpleName.endsWith("double") || typeSimpleName.endsWith("float")) {
                    	value = "0.0";
                    } else if (typeSimpleName.endsWith("long")) {
                    	value = "0L";
                    }
                } else {
                    if (typeSimpleName.equals("boolean")) {
                    	value = Boolean.toString(Array.getBoolean(fieldValue, x));
                    } else if (typeSimpleName.endsWith("byte")) {
                    	value = Byte.toString(Array.getByte(fieldValue, x));
                    } else if (typeSimpleName.endsWith("char")) {
                    	value = Character.toString(Array.getChar(fieldValue, x));
                    } else if (typeSimpleName.endsWith("double")) {
                    	value = Double.toString(Array.getDouble(fieldValue, x));
                    } else if (typeSimpleName.endsWith("float")) {
                    	value = Float.toString(Array.getFloat(fieldValue, x));
                    } else if (typeSimpleName.endsWith("int")) {
                    	value = Integer.toString(Array.getInt(fieldValue, x));
                    } else if (typeSimpleName.endsWith("long")) {
                    	value = Long.toString(Array.getLong(fieldValue, x)) + "L";
                    } else if (typeSimpleName.endsWith("short")) {
                    	value = Short.toString(Array.getShort(fieldValue, x));
                    } else {
                    	//TODO: throw an error?
                    }
                }
        } else {
                Object instance = Array.get(fieldValue, x);
                if (instance == null) {
                    value = "null";
                } else {
                    if (this.createdInstances.containsKey(System.identityHashCode(instance))) {
                        value = this.createdInstances.get(System.identityHashCode(instance));
                    } else {                      
                        String typeSimpleName = instance.getClass().getSimpleName();
                        if (	typeSimpleName.equals("boolean") || typeSimpleName.equals("Boolean")
                        		||
                        		typeSimpleName.endsWith("byte") || typeSimpleName.endsWith("Byte")
                        		||
                        		typeSimpleName.endsWith("double") || typeSimpleName.endsWith("Double")
                        		||
                        		typeSimpleName.endsWith("float") || typeSimpleName.endsWith("Float")
                        		||
                        		typeSimpleName.endsWith("int") || typeSimpleName.endsWith("Integer")
                        		||
                        		typeSimpleName.endsWith("short") || typeSimpleName.endsWith("Short")
                        		) {
                        	value = instance.toString();
                        } else if (typeSimpleName.endsWith("char") || typeSimpleName.endsWith("Character")) {
                            value = "'" + instance + "'";
                        } else if (typeSimpleName.endsWith("long") || typeSimpleName.endsWith("Long")) {
                            value = instance.toString() + "L";
                        } 
                    }
                }
        }
    	return value;
    }
    
    /**
     * 
     * @param aField
     * @param instance
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @requires fieldValue already stored in this.createdInstances
     */
    private void getValueForArray(Class<?> componentType, Object fieldValue, List<String> objectDefinitionStatements, List<String> objectInitializationStatements) throws IllegalArgumentException, IllegalAccessException {
        int length = Array.getLength(fieldValue);
        String arrayAssignedVariable = this.createdInstances.get(System.identityHashCode(fieldValue));
        
        if (componentType.isPrimitive()) {
            for (int x = 0; x < length; x++) {
            	String statement = arrayAssignedVariable + "[" + x + "] = ";
                statement += valueAsString(componentType, fieldValue, x);
                objectInitializationStatements.add(statement); 
            }
        } else {
            for (int x = 0; x < length; x++) {
            	String statement = arrayAssignedVariable + "[" + x + "] = ";
            	String value = valueAsString(componentType, fieldValue, x);
            	Object instance = value==null?null:Array.get(fieldValue, x);
            	if (value != null) {
            		statement += value;
            		objectInitializationStatements.add(statement);
            	} else if (instance.getClass().isArray()) {
            		String generatedName = generateVariableName(instance);
                    this.createdInstances.put(System.identityHashCode(instance), generatedName);
                    int instanceLength = Array.getLength(instance);
                    statement = instance.getClass().getCanonicalName() + " " + generatedName + " = new " + componentType.getName() + "[" + instanceLength +  "]";
                    objectDefinitionStatements.add(statement);
                    Class<?> aComponentType2 = instance.getClass().getComponentType();
                    getValueForArray(aComponentType2, instance, objectDefinitionStatements, objectInitializationStatements);
                    statement = arrayAssignedVariable + "[" + x + "] = " + generatedName;
                    objectInitializationStatements.add(statement);
            	} else {
            		String generatedName = generateVariableName(instance);
                    this.createdInstances.put(System.identityHashCode(instance), generatedName);
                    statement = instance.getClass().getCanonicalName() + " " + generatedName + " = new " + instance.getClass().getCanonicalName() + "()";
                    objectDefinitionStatements.add(statement);
                    statement = arrayAssignedVariable + "[" + x + "] = " + generatedName;
                    objectInitializationStatements.add(statement);
            	}              
            }

        }
    }

    /**
     * 
     * @param aField
     * @param instance
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private String getValueForPrimitiveTypeField(Field aField, Object instance) throws IllegalArgumentException, IllegalAccessException {
    	String typeSimpleName = aField.getType().getSimpleName();
        String value = null;

        aField.setAccessible(true);

        if (typeSimpleName.equals("boolean")) {
            value = Boolean.toString(aField.getBoolean(instance));
        } else if (typeSimpleName.endsWith("byte")) {
            value = Byte.toString(aField.getByte(instance));
        } else if (typeSimpleName.endsWith("char")) {
            value = "'" + Character.toString(aField.getChar(instance)) + "'";
        } else if (typeSimpleName.endsWith("double")) {
            value = Double.toString(aField.getDouble(instance));
        } else if (typeSimpleName.endsWith("float")) {
            value = Float.toString(aField.getFloat(instance));
        } else if (typeSimpleName.endsWith("int")) {
            value = Integer.toString(aField.getInt(instance));
        } else if (typeSimpleName.endsWith("long")) {
            value = Long.toString(aField.getLong(instance)) + "L";
        } else if (typeSimpleName.endsWith("short")) {
            value = Short.toString(aField.getShort(instance));
        } else {
            System.out.println("ERROR: undefined");
        }
        return value;
    }

    /**
     * 
     * @param clazz
     * @return
     */
    private boolean isAutoboxingClass(Class<?> clazz) {
        boolean ret_Value = false;

        ret_Value |= Boolean.class.isAssignableFrom(clazz);
        ret_Value |= Byte.class.isAssignableFrom(clazz);
        ret_Value |= Character.class.isAssignableFrom(clazz);
        ret_Value |= Double.class.isAssignableFrom(clazz);
        ret_Value |= Float.class.isAssignableFrom(clazz);
        ret_Value |= Integer.class.isAssignableFrom(clazz);
        ret_Value |= Long.class.isAssignableFrom(clazz);
        ret_Value |= Short.class.isAssignableFrom(clazz);

        return ret_Value;
    }

    /**
     * 
     * @param parameterType
     * @return
     */
    private boolean hasDefaultConstructor(Class<?> parameterType) {
        boolean ret_val = false;

        // I the class is defined as inner class then, the default constructor
        // contains the containing class at first parameter.
        int constructorParametersAmong = 0;
        if (parameterType.isMemberClass() && !Modifier.isStatic(parameterType.getModifiers())) {
            constructorParametersAmong = 1;
        }

        for (Constructor<?> aConstructor : parameterType.getDeclaredConstructors()) {
            if (aConstructor.getParameterTypes().length == constructorParametersAmong) {
                ret_val = true;
            }
        }

        return ret_val;
    }

    /**
     * Extract the module name for a given parameter
     * 
     * @param parameterType
     * @return
     */
    private String getModuleName(Class<?> parameterType) {
        String moduleName = null;
        if (parameterType.isMemberClass()) {
            moduleName = parameterType.getName().replace("$", ".inner.").replace('.', '_');
        } else {
            moduleName = parameterType.getCanonicalName().replace('.', '_');
        }
        return moduleName;
    }
    
    private int getSuffix() {
    	return TestBuilder.testIndex++;
    }
}

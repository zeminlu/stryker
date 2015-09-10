package delete;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;

public class ReflectionJunitTest {

	@Test
	public void test(String fileClasspath, String className, String methodName)
			throws IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, InstantiationException,
			MalformedURLException {
		String[] classpaths = fileClasspath.split(System
				.getProperty("path.separator"));
		URL[] urls = new URL[classpaths.length];
		for (int i = 0; i < classpaths.length; ++i) {
			urls[i] = new File(classpaths[i]).toURI().toURL();
		}
		ClassLoader cl2 = new URLClassLoader(urls);
		Class<?> clazz = cl2.loadClass(className);
		Object instance = clazz.newInstance();
		cl2 = null;
		roops.core.objects.SinglyLinkedListNode _SinglyLinkedListNode_1 = new roops.core.objects.SinglyLinkedListNode();
		roops.core.objects.SinglyLinkedListNode _SinglyLinkedListNode_2 = new roops.core.objects.SinglyLinkedListNode();
		roops.core.objects.SinglyLinkedListNode _SinglyLinkedListNode_3 = new roops.core.objects.SinglyLinkedListNode();
		java.lang.Boolean _Boolean_1 = new java.lang.Boolean(false);
		java.lang.Boolean _Boolean_2 = new java.lang.Boolean(false);
		java.lang.Object valueParam = null;
		// Fields Initialization for 'instance'
		// Fields Initialization for '_SinglyLinkedListNode_1'
		// Fields Initialization for '_SinglyLinkedListNode_2'
		// Fields Initialization for '_SinglyLinkedListNode_3'
		updateValue(_SinglyLinkedListNode_3, "next", null);
		updateValue(_SinglyLinkedListNode_3, "value", _Boolean_1);
		updateValue(_SinglyLinkedListNode_2, "next", _SinglyLinkedListNode_3);
		updateValue(_SinglyLinkedListNode_2, "value", null);
		updateValue(_SinglyLinkedListNode_1, "next", _SinglyLinkedListNode_2);
		updateValue(_SinglyLinkedListNode_1, "value", _Boolean_2);
		updateValue(instance, "header", _SinglyLinkedListNode_1);
		// Parameter Initialization

		// Method Invocation
		Method method = getAccessibleMethod(clazz, methodName, true);
		try {
			method.invoke(instance, new Object[] { valueParam });
			instance = null;
			method = null;
		} catch (Exception e) {
			throw (new java.lang.RuntimeException(e));
		}
	}

	private static void updateValue(Object instance, String fieldName,
			Object value) {
		for (Field aField : instance.getClass().getDeclaredFields()) {
			if (aField.getName().equals(fieldName)) {
				try {
					aField.setAccessible(true);
					if (aField.getType().isPrimitive()) {
						String typeSimpleName = aField.getType()
								.getSimpleName();
						if (typeSimpleName.equals("boolean")) {
							aField.setBoolean(instance, (Boolean) value);
						} else if (typeSimpleName.endsWith("byte")) {
							aField.setByte(instance, (Byte) value);
						} else if (typeSimpleName.endsWith("char")) {
							aField.setChar(instance, (Character) value);
						} else if (typeSimpleName.endsWith("double")) {
							aField.setDouble(instance, (Double) value);
						} else if (typeSimpleName.endsWith("float")) {
							aField.setFloat(instance, (Float) value);
						} else if (typeSimpleName.endsWith("int")) {
							aField.setInt(instance, (Integer) value);
						} else if (typeSimpleName.endsWith("long")) {
							aField.setLong(instance, (Long) value);
						} else if (typeSimpleName.endsWith("short")) {
							aField.setShort(instance, (Short) value);
						} else {
							System.out.println("ERROR: No difinida");
						}
					} else {
						aField.set(instance, value);
					}
					;

					aField.setAccessible(false);
				} catch (IllegalArgumentException e) {
					throw (new java.lang.RuntimeException(e));
				} catch (IllegalAccessException e) {
					throw (new java.lang.RuntimeException(e));
				}
			}
		}
	}

	private Method getAccessibleMethod(Class<?> clazz, String methodName,
			boolean value) {
		try {
		} catch (Exception e) {
			throw new RuntimeException("DYNJALLOY ERROR! " + e.getMessage());
		}

		Method methodToCheck = null;
		try {
			// Gets parameters types
			Class<?>[] parameterTypes = null;
			for (Method aMethod : clazz.getDeclaredMethods()) {
				if (aMethod.getName().equals(methodName)) {
					parameterTypes = aMethod.getParameterTypes();
				}
			}
			methodToCheck = clazz.getDeclaredMethod(methodName, parameterTypes);
		} catch (SecurityException e) {
			throw new RuntimeException("DYNJALLOY ERROR! " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("DYNJALLOY ERROR! " + e.getMessage());
		}
		methodToCheck.setAccessible(value);

		return methodToCheck;
	}
}

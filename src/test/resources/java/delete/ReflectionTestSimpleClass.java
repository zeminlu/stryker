package delete;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionTestSimpleClass {

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        test(utils.SimpleClass.class);
	}
	
	private static void test(Class<?> testClass) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        Object instance = testClass.newInstance();
        Method[] methods = testClass.getDeclaredMethods();
        System.out.println("utils.SimpleClass has " + methods.length + " declared methods");
        Method multByFive = null;
        for (Method m : methods) {
        	if (m.getName().compareTo("multByfive") == 0) {
        		multByFive = m;
        		break;
        	}
        }
        if (multByFive != null) {
        	multByFive.setAccessible(true);
        	try {
				System.out.println("multByfive(0) : " + (Integer)multByFive.invoke(instance, new Object[]{new Integer(0)}));
				System.out.println("multByfive(1) : " + (Integer)multByFive.invoke(instance, new Object[]{new Integer(1)}));
				System.out.println("multByfive(2) : " + (Integer)multByFive.invoke(instance, new Object[]{new Integer(2)}));
				System.out.println("multByfive(3) : " + (Integer)multByFive.invoke(instance, new Object[]{new Integer(3)}));
				System.out.println("multByfive(4) : " + (Integer)multByFive.invoke(instance, new Object[]{new Integer(4)}));
				System.out.println("multByfive(5) : " + (Integer)multByFive.invoke(instance, new Object[]{new Integer(5)}));
        	} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}


}

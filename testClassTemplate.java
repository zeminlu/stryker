PACKAGE

IMPORTS

CLASS START



  	 /**
  	   * Auxiliar function that embed awful reflection code
  	   * 
  	   * @param instance
  	   * @param fieldName
  	   * @param value
  	   */
  	  private void updateValue(Object instance, String fieldName, Object value) {
  	      for (Field aField : instance.getClass().getDeclaredFields()) {
  	          if (aField.getName().equals(fieldName)) {
  	              try {
  	                  aField.setAccessible(true);
  	                  if (aField.getType().isPrimitive()) {
  	                      String typeSimpleName = aField.getType().getSimpleName();
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
  	                          System.out.println("ERROR: undefined");
  	                      }
  	                  } else {
  	                      aField.set(instance, value);
  	                  };
	
  	                  aField.setAccessible(false);
  	              } catch (IllegalArgumentException e) {
  	         		throw(new java.lang.RuntimeException(e));
  	              } catch (IllegalAccessException e) {
  	         		throw(new java.lang.RuntimeException(e));
  	              }
  	          }
  	      }
  	}



	/**
	 * This method will return a {@code Method} object corresponding to the method to repair in the candidate
	 * 
	 * @param candidate			:	the current fix candidate	:	{@code FixCandidate}
	 * @param method			:	the method name to search	:	{@code Class<?>}
	 * @return a {@code Method} object corresponding to the method to repair
	 * TODO: improve the way the method is searched (e.g.: include parameter checks}
	 */
	private Method getMethodToRepair(String method, Class<?> candidateClass) {
		Method methodToRepair = null;
		Method[] candidateMethods = candidateClass.getDeclaredMethods();
		for (Method m : candidateMethods) {
			if (m.getName().compareTo(method)==0) {
				methodToRepair = m;
				break;
			}
		}
		return methodToRepair;
	}


    	public void test(Class<?> clazz, String methodName) throws IllegalAccessException,InvocationTargetException, InstantiationException {
 	   	Object instance = INSTANCE VALUE;
        	
		INITIALIZATIONS
	
		FIELD UPDATES
	
		Object[] params = null;
	
		PARAMS INIT
        	
        	try {
			Method method = getMethodToRepair(methodName, clazz);
			method.setAccessible(true);
        		method.invoke(instance, params);
        	} catch (Exception e) {
        		throw(new java.lang.RuntimeException(e));
        	} 
	
    	}





}

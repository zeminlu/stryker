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


    public void test(Class<?> clazz, Method method) throws IllegalAccessException,InvocationTargetException, InstantiationException {
    	Class<?> instance = INSTANCE VALUE;
        
	INITIALIZATIONS

	FIELD UPDATES

	Object[] params = null;

	PARAMS INIT
        
        try {
            method.invoke(instance, params);
        } catch (Exception e) {
           throw(new java.lang.RuntimeException(e));
        } 

    }





}

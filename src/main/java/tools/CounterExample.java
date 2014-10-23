package tools;

/**
 * This class represents a counterexample built by a SAT solver when returning SAT
 * 
 * @author Nazareno Aguirre
 */
public class CounterExample {

	/**
	 * Class object containing the junit witnessing the counterexample
	 */
	private Class<?> junitInput;
    
	/**
	 * Name of the file where the counterexample is stored.
	 */
	private String junitFile;
	
	/**
	 * Constructor of class CounterExample. It receives the class object and file name corresponding to the
	 * counterexample
	 * @param junitInput is the class object of the junit corresponding to the counterexample
	 * @param junitFile is the name of the file containing the counterexample (as a junit test)
	 */
    public CounterExample(Class<?> junitInput, String junitFile) {
    	if (junitInput == null) throw new IllegalArgumentException("null junit input");
    	if (junitFile == null || junitFile.length()==0) throw new IllegalArgumentException("null or empty junit file");
    	this.junitInput = junitInput;
    	this.junitFile = junitFile;
    }
	
    /**
     * Returns the object corresponding the junit input
     * @return the object corresponding the junit input
     */
    public Class<?> getJunitInput() {
    	return this.junitInput;
    }
    

    /**
     * Returns the name of the file containing the counterexample (as a junit test)
     * @return the name of the file containing the counterexample (as a junit test)
     */
    public String getJunitFile() {
    	return this.junitFile;
    }
}

package tools;

import java.util.Map;
import java.util.Map.Entry;

import config.StrykerConfig;

/**
 * This class contains data about how stryker will try to fix a program
 * 
 * @author Simón Emmanuel Gutiérrez Brida
 * @version 0.1
 */
public class StrykerOptions {
	
	/**
	 * The possible strategies that stryker can use to find a fix
	 * 
	 * @author Simón Emmanuel Gutiérrez Brida
	 * @version 0.1
	 */
	public static enum SearchStrategy {BFS, DFS}
	
	public static enum RAC {DISABLED, ENABLED, CONCURRENT}
	
	/**
	 * If stryker will use RAC or not to fix the program
	 */
	private RAC rac;
	
	/**
	 * The strategy that stryker will use to find a fix
	 * @see SearchStrategy
	 */
	private SearchStrategy searchStrategy;
	
	/**
	 * max depth to be considered in the search of program repairs
	 */
	private int maxDepth;
	
	/**
	 * the classes scopes to be used in the repair process
	 */
	private String scope;
	
	/**
	 * the config file that will be used by {@code StrykerConfig}
	 */
	private String configFile = null;
	
	/**
	 * Constructs a new instance of this class
	 * 
	 * @param searchStrategy	:	the strategy that stryker will use to find a fix										:	{@code SearchStrategy}
	 * @param scopes			:	the classes scopes to be used in the repair process, {@code null} to use default scopes	:	{@code Map<String, Integer>}
	 * @param maxDepth			:	max depth to be considered in the search of program repairs								:	{@code int}
	 * @param enableRac			:	if stryker will use RAC or not to fix the program										:	{@code boolean}
	 */
	public StrykerOptions(SearchStrategy searchStrategy, Map<String, Integer> scopes, int maxDepth, RAC rac) {
		this.searchStrategy = searchStrategy;
		this.maxDepth = maxDepth;
		this.rac = rac;
		this.scope = createScopeString(scopes);
	}
	
	/**
	 * Constructs a new instance of this class
	 * 
	 * @param searchStrategy	:	the strategy that stryker will use to find a fix										:	{@code SearchStrategy}
	 * @param scopes			:	the classes scopes to be used in the repair process, {@code null} to use default scopes	:	{@code Map<String, Integer>}
	 * @param maxDepth			:	max depth to be considered in the search of program repairs								:	{@code int}
	 * @param enableRac			:	if stryker will use RAC or not to fix the program										:	{@code boolean}
	 * @param configFile		:	the config file that will be used by {@code StrykerConfig}								:	{@code String}
	 */
	public StrykerOptions(SearchStrategy searchStrategy, Map<String, Integer> scopes, int maxDepth, RAC rac, String configFile) {
		this(searchStrategy, scopes, maxDepth, rac);
		this.configFile = configFile;
		if (!this.configFile.isEmpty()) { //TODO: should also check that the file exist
			StrykerConfig.getInstance(this.configFile);
		}
	}
	
	/**
	 * Transform a Map of (class, scope) into a string class:scope, using comma to separate each scope
	 * @param scopes	:	the classes scopes to be used in the repair process, {@code null} to use default scopes	:	{@code Map<String, Integer>}
	 * @return	a string class:scope, using comma to separate each scope
	 */
	private String createScopeString(Map<String, Integer> scopes) {
		String scope = "";
		if (scopes == null) return scope;
		for (Entry<String, Integer> s : scopes.entrySet()) {
			scope += s.getKey() + ":" + s.getValue() + ",";
		}
		if (!scope.isEmpty()) {
			scope = scope.substring(0, scope.length()-1); //removes the last comma
		}
		return scope;
	}
	
	/**
	 * @return {@code true} if the options set for stryker are valid
	 */
	public boolean validate() {
		boolean maxDepthIsValid = this.maxDepth >= 0;
		//TODO: add more checks
		return maxDepthIsValid;
	}

	/**
	 * @return if stryker will use RAC or not to fix the program
	 */
	public boolean useRac() {
		return this.rac.equals(RAC.ENABLED) || this.rac.equals(RAC.CONCURRENT);
	}
	
	public boolean useConcurrentRac() {
		return this.rac.equals(RAC.CONCURRENT);
	}

	/**
	 * @return the max depth to be considered in the search of program repairs
	 */
	public int getMaxDepth() {
		return maxDepth;
	}

	/**
	 * @return the classes scopes to be used in the repair process using a format class:scope separated by comma
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * @return the config file that will be used by {@code StrykerConfig} if one has been set
	 */
	public String getConfigFile() {
		return configFile;
	}
	
	/**
	 * @return {@code true} if stryker will use breadth first search
	 */
	public boolean useBFS() {
		return this.searchStrategy.equals(SearchStrategy.BFS);
	}
	
	/**
	 * @return {@code true} if stryker will use depth first search
	 */
	public boolean useDFS() {
		return this.searchStrategy.equals(SearchStrategy.DFS);
	}

}

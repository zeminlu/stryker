package tools;

import java.util.Map;
import java.util.Map.Entry;

import config.StrykerConfig;

public class StrykerOptions {
	
	public static enum SearchStrategy {BFS, DFS}
	
	private boolean useRac;
	
	private SearchStrategy searchStrategy;
	
	private int maxDepth;
	
	private String scope;
	
	private String configFile = null;
	
	public StrykerOptions(SearchStrategy searchStrategy, Map<String, Integer> scopes, int maxDepth, boolean enableRac) {
		this.searchStrategy = searchStrategy;
		this.maxDepth = maxDepth;
		this.useRac = enableRac;
		this.scope = createScopeString(scopes);
	}
	
	public StrykerOptions(SearchStrategy searchStrategy, Map<String, Integer> scopes, int maxDepth, boolean enableRac, String configFile) {
		this(searchStrategy, scopes, maxDepth, enableRac);
		this.configFile = configFile;
		if (!this.configFile.isEmpty()) { //TODO: should also check that the file exist
			StrykerConfig.getInstance(this.configFile);
		}
	}
	
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
	
	public boolean validate() {
		boolean maxDepthIsValid = this.maxDepth >= 0;
		//TODO: add more checks
		return maxDepthIsValid;
	}

	public boolean useRac() {
		return useRac;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public String getScope() {
		return scope;
	}

	public String getConfigFile() {
		return configFile;
	}
	
	public boolean useBFS() {
		return this.searchStrategy.equals(SearchStrategy.BFS);
	}
	
	public boolean useDFS() {
		return this.searchStrategy.equals(SearchStrategy.DFS);
	}

}

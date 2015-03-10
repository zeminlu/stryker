package config;

import java.util.Random;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.SystemUtils;

/**
 * This class allows access to a configuration loaded from a properties file
 * but also allows access to some values that either doesn't belong to the
 * properties file (e.g.: file separator) and properties that need arguments to
 * form an usable value (e.g.: output dir and compilation sandbox that need the
 * file separator  
 * 
 * @author Simón Emmanuel Gutiérrez Brida
 * @version 0.3.1
 */
public class StrykerConfig {
	
	/**
	 * The path to a default .properties file
	 */
	public static final String DEFAULT_PROPERTIES = "genericTest.properties";
	
	/**
	 * The {@code StrykerConfig} instance that will be returned by {@link StrykerConfig#getInstance(String)}
	 */
	private static StrykerConfig instance = null;

	/**
	 * The properties file that will be loaded
	 */
	private String propertiesFile;
	/**
	 * The configuration loaded, especified by {@link StrykerConfig#propertiesFile}
	 */
	private Configuration config;
	
	private String[] randomStrings = new String[1]; //holds random string values, currently only holds one
	
	/**
	 * Gets an instance of {@code StrykerConfig}
	 * 
	 * @param configFile	:	the properties file that will be loaded	:	{@code String}
	 * @return an instance of {@code StrykerConfig} that uses {@code configFile} to load a configuration
	 * @throws IllegalStateException if an instance is already built and this method is called with a different config file
	 */
	public static StrykerConfig getInstance(String configFile) throws IllegalStateException {
		if (instance != null) {
			if (instance.propertiesFile.compareTo(configFile) != 0) {
				throw new IllegalStateException("StrykerConfig instance is already built using config file : " + instance.propertiesFile);
			}
		} else {
			instance = new StrykerConfig(configFile);
		}
		return instance;
	}
	
	/**
	 * @return a previously built instance or construct a new instance using {@code StrykerConfig#DEFAULT_PROPERTIES}
	 */
	public static StrykerConfig getInstance() {
		if (instance == null) {
			instance = new StrykerConfig(StrykerConfig.DEFAULT_PROPERTIES);
		}
		return instance;
	}
	
	/**
	 * Private constructor
	 * 
	 * This will set the value of {@link StrykerConfig#propertiesFile} and will load the configuration
	 * 
	 * @param configFile	:	the properties file that will be loaded	:	{@code String}
	 */
	private StrykerConfig(String configFile) {
		this.propertiesFile = configFile;
		this.config = null;
		loadConfig();
	}

	/**
	 * loads the configuration defined in {@link StrykerConfig#propertiesFile}
	 */
	private void loadConfig() {
		try {
			this.config = new PropertiesConfiguration(this.propertiesFile);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return the configuration especified by {@link StrykerConfig#propertiesFile}
	 */
	public Configuration getConfiguration() {
		return this.config;
	}
	
	/**
	 * @return the file separator for the current os (e.g.: "/" for unix)
	 */
	public String getFileSeparator() {
		return SystemUtils.FILE_SEPARATOR;
	}
	
	/**
	 * @return the path separator for the current os (e.g.: ":" for unix)
	 */
	public String getPathSeparator() {
		return SystemUtils.PATH_SEPARATOR;
	}
	
	/**
	 * @return the value of property {@code path.output} replacing the only argument with {@link StrykerConfig#getFileSeparator()}
	 */
	public String getOutputDir() {
		return formatString(this.config.getString("path.output"));
	}
	
	/**
	 * @return the value of property {@code path.mujavaOutput} replacing the only argument with {@link StrykerConfig#getFileSeparator()}
	 */
	public String getMutantsDir() {
		return formatString(this.config.getString("path.mujavaOutput"));
	}
	
	/**
	 * @return the value of property {@code path.compilingSandbox} replacing the first argument with {@link StrykerConfig#getFileSeparator()} and the second with {@code randomString(10)}
	 * <hr>
	 * <b>The value returned by this method will not change until {@link StrykerConfig#getInstance(String)} is called again with a different config file</b>
	 * @see StrykerConfig#randomString(int)
	 */
	public String getCompilingSandbox() {
		return formatString(this.config.getString("path.compilingSandbox")); 
	}
	
	/**
	 * @return the value of property {@code path.junitPath} replacing the only argument with {@link StrykerConfig#getFileSeparator()}
	 */
	public String getJunitPath() {
		return formatString(this.config.getString("path.junitPath"));
	}
	
	/**
	 * @return the value of property {@code path.hamcrestPath} replacing the only argument with {@link StrykerConfig#getFileSeparator()}
	 */
	public String getHamcrestPath() {
		return formatString(this.config.getString("path.hamcrestPath"));
	}
	
	/**
	 * @return the value of property {@code tests.output}
	 */
	public String getTestsOutputDir() {
		return formatString(this.config.getString("tests.output"));
	}
	
	/**
	 * @return the value of property {@code tests.package}
	 */
	public String getTestsPackage() {
		return formatString(this.config.getString("tests.package"));
	}
	
	/**
	 * The next time {@code StrykerConfig#getCompilingSandbox()} is called a new directory will be generated
	 */
	public void resetCompilingSandbox() {
		this.randomStrings[0] = null;
	}
	
	/**
	 * Construct a random String using upper case letters and digits
	 * 
	 * @param len	:	the lenght of the resulting string	:	{@code int}
	 * @return a random String of size {@code len} using upper case letters and digits
	 */
	private String randomString(int len) {
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}

	/**
	 * @return the path to the test template file
	 */
	public String getTestTemplatePath() {
		return formatString(this.config.getString("tests.template"));
	}
	
	private String formatString(String original) {
		return formatString(original, 0);
	}
	
	private String formatString(String original, int randomStringIndex) {
		if (this.randomStrings[randomStringIndex] == null) {
			this.randomStrings[randomStringIndex] = randomString(10);
		}
		String firstFormat = original.replaceAll("\\{0\\}", getFileSeparator());
		String secondFormat = firstFormat.replaceAll("\\{1\\}", this.randomStrings[randomStringIndex]);
		return secondFormat;
	}
	
}

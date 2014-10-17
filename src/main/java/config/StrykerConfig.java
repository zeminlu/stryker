package config;

import java.text.MessageFormat;
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
 * @version 0.1.3
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
	
	/**
	 * The location of the folder that will be used as compiling environment
	 */
	private String compilingSandbox = null;

	/**
	 * Gets an instance of {@code StrykerConfig}
	 * 
	 * @param configFile	:	the properties file that will be loaded	:	{@code String}
	 * @return an instance of {@code StrykerConfig} that uses {@code configFile} to load a configuration
	 */
	public static StrykerConfig getInstance(String configFile) {
		if (instance != null) {
			if (instance.propertiesFile.compareTo(configFile) != 0) {
				instance = new StrykerConfig(configFile);
			}
		} else {
			instance = new StrykerConfig(configFile);
		}
		return instance;
	}
	
	/**
	 * @return the last instance built
	 * @throws IllegalStateException : if an instance hasn't been built before
	 */
	public static StrykerConfig getLastBuiltInstance() throws IllegalStateException {
		if (instance == null) {
			throw new IllegalStateException("StrykerConfig#getLastBuiltInstance() : must make a successful call to StrykerConfig#getInstance(String) before calling this method");
		}
		return instance;
	}
	
	/**
	 * @return {@code true} if an instance of this class has been built
	 */
	public static boolean instanceBuilt() {
		return instance != null;
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
		this.compilingSandbox = null;
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
	 * @return the path/file separator for the current os (e.g.: "/" for unix)
	 */
	public String getPathSeparator() {
		return SystemUtils.FILE_SEPARATOR;
	}
	
	/**
	 * @return the value of property {@code path.output} replacing the only argument with {@link StrykerConfig#getPathSeparator()}
	 */
	public String getOutputDir() {
		return MessageFormat.format(this.config.getString("path.output"), getPathSeparator());
	}
	
	/**
	 * @return the value of property {@code path.mujavaOutput} replacing the only argument with {@link StrykerConfig#getPathSeparator()}
	 */
	public String getMutantsDir() {
		return MessageFormat.format(this.config.getString("path.mujavaOutput"), getPathSeparator());
	}
	
	/**
	 * @return the value of property {@code path.compilingSandbox} replacing the first argument with {@link StrykerConfig#getPathSeparator()} and the second with {@code randomString(10)}
	 * <hr>
	 * <b>The value returned by this method will not change until {@link StrykerConfig#getInstance(String)} is called again with a different config file</b>
	 * @see StrykerConfig#randomString(int)
	 */
	public String getCompilingSandbox() {
		if (this.compilingSandbox == null) {
			this.compilingSandbox = MessageFormat.format(this.config.getString("path.compilingSandbox"), getPathSeparator(), randomString(10));
		}
		return this.compilingSandbox; 
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
	
}

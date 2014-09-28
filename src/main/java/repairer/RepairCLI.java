package repairer;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * RepairCLI is a command line interface for Stryker. It is a very simple interface, that receives only minimal
 * input: class name, method to fix, qualified path to class, and max depth for search.
 * @author aguirre
 *
 */
public class RepairCLI {

	/**
	 * Main method of CLI interface to Stryker. It uses Apache CLI to parse command line options:
	 * -p for qualified path to class
	 * -c for class name
	 * -m for method to fix
	 * -d for max depth for the search for fixes.
	 * All arguments are mandatory, except for max depth. Default max depth: 3.
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();
		
		Option path = new Option("p", "path", true, "qualified path e.g.: src/ or /Users/ppargento/Documents/workspace/stryker/src/");
		path.setRequired(true);
		
		Option className = new Option("c", "class-name", true, "qualified class name e.g.: main.util.Pair");
		className.setRequired(true);
		
		Option method = new Option("m", "method", true, "method to fix e.g.: add");
		method.setRequired(true);
		
		Option depth = new Option("d", "depth", true, "max depth for search");
		depth.setRequired(true);
		depth.setType(Integer.class);
		
		Option help = new Option("h", "help", false, "print commands");
		help.setRequired(false);
		
		Option classes = new Option("n", "needed-classes", true, "class dependencies of the class defined with c/class-name argument");
		classes.setRequired(false);
		classes.setArgs(Option.UNLIMITED_VALUES);
		classes.setValueSeparator(',');
		
		options.addOption(help);
		options.addOption(path);
		options.addOption(className);
		options.addOption(method);
		options.addOption(depth);
		options.addOption(classes);

		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "stryker", options );
				return;
			}
			String qualifiedPath = cmd.getOptionValue("p");
			String clazz = cmd.getOptionValue("c");
			String methodToFix = cmd.getOptionValue("m");
			int maxDepth = 3; // max depth is 3 by default.

			if (cmd.hasOption("d")) { 
				maxDepth = Integer.parseInt(cmd.getOptionValue("d"));
				if (maxDepth <= 0) throw new NumberFormatException("Incorrect options.  Max depth must be a non-negative integer.");
			}
			
			String[] dependenciesArgs = new String[]{};
			
			if (cmd.hasOption("n")) {
				dependenciesArgs = cmd.getOptionValues('n');
			}

			JMLAnnotatedClass subject = new JMLAnnotatedClass(qualifiedPath, clazz);		
			BasicProgramRepairer repairer = new BasicProgramRepairer(subject, methodToFix, dependenciesArgs, maxDepth);
			repairer.repair();
		}
		catch (ParseException e) {
			System.err.println( "Incorrect options.  Reason: " + e.getMessage() );
			return;
		}
		catch(NumberFormatException e) { 
			System.err.println(e.getMessage());
			return;
		}
		catch(IllegalArgumentException e) {
			System.err.println(e.getMessage());
		}

	}

}

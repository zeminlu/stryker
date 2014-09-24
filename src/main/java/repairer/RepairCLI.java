package repairer;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
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

		options.addOption("p", true, "qualified path");
		options.addOption("c", true, "class name");
		options.addOption("m", true, "method to fix");
		options.addOption("d", false, "max depth for search");

		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			String qualifiedPath = cmd.getOptionValue("p");
			String className = cmd.getOptionValue("c");
			String methodToFix = cmd.getOptionValue("m");
			int maxDepth = 3; // max depth is 3 by default.

			if (cmd.hasOption("d")) { 
				maxDepth = Integer.parseInt(cmd.getOptionValue("d"));
				if (maxDepth < 0) throw new NumberFormatException();				
			}

			JmlProgram subject = new JmlProgram(qualifiedPath, className);		
			BasicProgramRepairer repairer = new BasicProgramRepairer(subject, methodToFix, maxDepth);
			repairer.repair();
		}
		catch (ParseException e) {
			System.err.println( "Incorrect options.  Reason: " + e.getMessage() );
			return;
		}
		catch(NumberFormatException e) { 
			System.err.println( "Incorrect options.  Max depth must be a non-negative integer.");
			return;
		}
		catch(IllegalArgumentException e) {
			System.err.println("Class or method not found");
		}

	}

}

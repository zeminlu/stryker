package repairer;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RepairCLI {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Options options = new Options();

		options.addOption("p", true, "qualified path");
		options.addOption("c", true, "class name");
		options.addOption("m", true, "method to fix");
		options.addOption("d", false, "max depth for search");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
			String qualifiedPath = cmd.getOptionValue("p");
			String className = cmd.getOptionValue("c");
			String methodToFix = cmd.getOptionValue("m");
			int maxDepth = 3;

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

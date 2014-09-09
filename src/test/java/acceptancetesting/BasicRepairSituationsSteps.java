package acceptancetesting;

import repairer.Program;
import repairer.ProgramRepairer;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;



public class BasicRepairSituationsSteps {
		
	@Given("^a program \"(.*?)\" to be repaired$")
	public void open(String fileName) throws Throwable {
		if (!Program.fileExists(fileName))
	    	throw new IllegalArgumentException("program not found");
	}

	@Given("^\"(.*?)\" does not compile$")
	public void doesNotCompile(String fileName) throws Throwable {
		Program program = new Program(fileName);
	    if (program.isCompilable()) throw new Exception("program compiles!");
	}

	@When("^the repair process is attempted on \"(.*?)\"$")
	public void repairProcessAttempted(String fileName) throws Throwable {
		Program program = new Program(fileName);
		ProgramRepairer repairer = new ProgramRepairer(program);
		repairer.repair();
	}

	@Given("^\"(.*?)\" cannot be found$")
	public void cannot_be_found(String arg1) throws Throwable {
	    // Write code here that turns the phrase above into concrete actions
	    throw new PendingException();
	}

	@Then("^the user is informed that \"(.*?)\" cannot be found$")
	public void the_user_is_informed_that_cannot_be_found(String arg1) throws Throwable {
	    // Write code here that turns the phrase above into concrete actions
	    throw new PendingException();
	}

	@Given("^a program \"(.*?)\" that satisfies its specification$")
	public void a_program_that_satisfies_its_specification(String arg1) throws Throwable {
	    // Write code here that turns the phrase above into concrete actions
	    throw new PendingException();
	}

	@Then("^the verification stage detects no specification violation for \"(.*?)\"$")
	public void the_verification_stage_detects_no_specification_violation_for(String arg1) throws Throwable {
	    // Write code here that turns the phrase above into concrete actions
	    throw new PendingException();
	}

	@Then("^the non-compilation of \"(.*?)\" is detected$")
	public void the_non_compilation_of_is_detected(String arg1) throws Throwable {
	    // Write code here that turns the phrase above into concrete actions
	    throw new PendingException();
	}

	@Then("^the repair process is inhibited$")
	public void the_repair_process_is_inhibited() throws Throwable {
	    // Write code here that turns the phrase above into concrete actions
	    throw new PendingException();
	}

	@Then("^the user is informed of the situation$")
	public void the_user_is_informed_of_the_situation() throws Throwable {
	    // Write code here that turns the phrase above into concrete actions
	    throw new PendingException();
	}



}

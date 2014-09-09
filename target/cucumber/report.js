$(document).ready(function() {var formatter = new CucumberHTML.DOMFormatter($('.cucumber-report'));formatter.uri("basicRepairSituations.feature");
formatter.feature({
  "id": "execution-of-the-intra-statement,-mutation-based-approach-to-program-repair,-in-situations-that-do-not-demand-program-mutation.",
  "description": "\tSince the situations do not demand mutation (i.e., repair), the repair process should return without starting the repair\n\tprocess, and communicating the user about the situation.",
  "name": "Execution of the intra-statement, mutation based approach to program repair, in situations that do not demand program mutation.",
  "keyword": "Feature",
  "line": 1
});
formatter.scenario({
  "id": "execution-of-the-intra-statement,-mutation-based-approach-to-program-repair,-in-situations-that-do-not-demand-program-mutation.;application-of-repair-process-on-a-program-that-does-not-compile.",
  "description": "",
  "name": "Application of repair process on a program that does not compile.",
  "keyword": "Scenario",
  "line": 5,
  "type": "scenario"
});
formatter.step({
  "name": "a program \"Program\" to be repaired",
  "keyword": "Given ",
  "line": 6
});
formatter.step({
  "name": "\"Program\" does not compile",
  "keyword": "But ",
  "line": 7
});
formatter.step({
  "name": "the repair process is attempted on \"P\"",
  "keyword": "When ",
  "line": 8
});
formatter.step({
  "name": "the non-compilation of \"P\" is detected",
  "keyword": "Then ",
  "line": 9
});
formatter.step({
  "name": "the repair process is inhibited",
  "keyword": "And ",
  "line": 10
});
formatter.step({
  "name": "the user is informed of the situation",
  "keyword": "And ",
  "line": 11
});
formatter.match({
  "arguments": [
    {
      "val": "Program",
      "offset": 11
    }
  ],
  "location": "BasicRepairSituationsSteps.open(String)"
});
formatter.result({
  "duration": 244656000,
  "status": "pending",
  "error_message": "cucumber.api.PendingException: TODO: implement me\n\tat acceptancetesting.BasicRepairSituationsSteps.open(BasicRepairSituationsSteps.java:15)\n\tat ✽.Given a program \"Program\" to be repaired(basicRepairSituations.feature:6)\n"
});
formatter.match({
  "arguments": [
    {
      "val": "Program",
      "offset": 1
    }
  ],
  "location": "BasicRepairSituationsSteps.does_not_compile(String)"
});
formatter.result({
  "status": "skipped"
});
formatter.match({
  "arguments": [
    {
      "val": "P",
      "offset": 36
    }
  ],
  "location": "BasicRepairSituationsSteps.the_repair_process_is_attempted_on(String)"
});
formatter.result({
  "status": "skipped"
});
formatter.match({
  "arguments": [
    {
      "val": "P",
      "offset": 24
    }
  ],
  "location": "BasicRepairSituationsSteps.the_non_compilation_of_is_detected(String)"
});
formatter.result({
  "status": "skipped"
});
formatter.match({
  "location": "BasicRepairSituationsSteps.the_repair_process_is_inhibited()"
});
formatter.result({
  "status": "skipped"
});
formatter.match({
  "location": "BasicRepairSituationsSteps.the_user_is_informed_of_the_situation()"
});
formatter.result({
  "status": "skipped"
});
formatter.scenario({
  "id": "execution-of-the-intra-statement,-mutation-based-approach-to-program-repair,-in-situations-that-do-not-demand-program-mutation.;application-of-repair-process-on-a-non-existent-program.",
  "description": "",
  "name": "Application of repair process on a non-existent program.",
  "keyword": "Scenario",
  "line": 13,
  "type": "scenario"
});
formatter.step({
  "name": "a program \"P\" to be repaired",
  "keyword": "Given ",
  "line": 14
});
formatter.step({
  "name": "\"P\" cannot be found",
  "keyword": "But ",
  "line": 15
});
formatter.step({
  "name": "the repair process is attempted on \"P\"",
  "keyword": "When ",
  "line": 16
});
formatter.step({
  "name": "the user is informed that \"P\" cannot be found",
  "keyword": "Then ",
  "line": 17
});
formatter.step({
  "name": "the repair process is inhibited",
  "keyword": "And ",
  "line": 18
});
formatter.match({
  "arguments": [
    {
      "val": "P",
      "offset": 11
    }
  ],
  "location": "BasicRepairSituationsSteps.open(String)"
});
formatter.result({
  "duration": 478000,
  "status": "pending",
  "error_message": "cucumber.api.PendingException: TODO: implement me\n\tat acceptancetesting.BasicRepairSituationsSteps.open(BasicRepairSituationsSteps.java:15)\n\tat ✽.Given a program \"P\" to be repaired(basicRepairSituations.feature:14)\n"
});
formatter.match({
  "arguments": [
    {
      "val": "P",
      "offset": 1
    }
  ],
  "location": "BasicRepairSituationsSteps.cannot_be_found(String)"
});
formatter.result({
  "status": "skipped"
});
formatter.match({
  "arguments": [
    {
      "val": "P",
      "offset": 36
    }
  ],
  "location": "BasicRepairSituationsSteps.the_repair_process_is_attempted_on(String)"
});
formatter.result({
  "status": "skipped"
});
formatter.match({
  "arguments": [
    {
      "val": "P",
      "offset": 27
    }
  ],
  "location": "BasicRepairSituationsSteps.the_user_is_informed_that_cannot_be_found(String)"
});
formatter.result({
  "status": "skipped"
});
formatter.match({
  "location": "BasicRepairSituationsSteps.the_repair_process_is_inhibited()"
});
formatter.result({
  "status": "skipped"
});
formatter.scenario({
  "id": "execution-of-the-intra-statement,-mutation-based-approach-to-program-repair,-in-situations-that-do-not-demand-program-mutation.;application-of-repair-process-on-a-simple-correct-program.",
  "description": "",
  "name": "Application of repair process on a simple correct program.",
  "keyword": "Scenario",
  "line": 21,
  "type": "scenario"
});
formatter.step({
  "name": "a program \"P\" that satisfies its specification",
  "keyword": "Given ",
  "line": 22
});
formatter.step({
  "name": "the repair process is attempted on \"P\"",
  "keyword": "When ",
  "line": 23
});
formatter.step({
  "name": "the verification stage detects no specification violation for \"P\"",
  "keyword": "Then ",
  "line": 24
});
formatter.step({
  "name": "the repair process is inhibited",
  "keyword": "And ",
  "line": 25
});
formatter.step({
  "name": "the user is informed of the situation",
  "keyword": "And ",
  "line": 26
});
formatter.match({
  "arguments": [
    {
      "val": "P",
      "offset": 11
    }
  ],
  "location": "BasicRepairSituationsSteps.a_program_that_satisfies_its_specification(String)"
});
formatter.result({
  "duration": 9521000,
  "status": "pending",
  "error_message": "cucumber.api.PendingException: TODO: implement me\n\tat acceptancetesting.BasicRepairSituationsSteps.a_program_that_satisfies_its_specification(BasicRepairSituationsSteps.java:45)\n\tat ✽.Given a program \"P\" that satisfies its specification(basicRepairSituations.feature:22)\n"
});
formatter.match({
  "arguments": [
    {
      "val": "P",
      "offset": 36
    }
  ],
  "location": "BasicRepairSituationsSteps.the_repair_process_is_attempted_on(String)"
});
formatter.result({
  "status": "skipped"
});
formatter.match({
  "arguments": [
    {
      "val": "P",
      "offset": 63
    }
  ],
  "location": "BasicRepairSituationsSteps.the_verification_stage_detects_no_specification_violation_for(String)"
});
formatter.result({
  "status": "skipped"
});
formatter.match({
  "location": "BasicRepairSituationsSteps.the_repair_process_is_inhibited()"
});
formatter.result({
  "status": "skipped"
});
formatter.match({
  "location": "BasicRepairSituationsSteps.the_user_is_informed_of_the_situation()"
});
formatter.result({
  "status": "skipped"
});
});
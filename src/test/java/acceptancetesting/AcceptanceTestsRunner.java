package acceptancetesting;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.*;

import org.junit.runner.RunWith;

/**
 * AcceptanceTestsRunner: class that is used to run cucumber acceptance tests. No behaviour
 * directly provided by code in the class, but as Cucumber/JUnit options.
 * It runs all acceptance tests given as *.feature files in src/test/resources.
 * @author aguirre
 *
 */
@RunWith(Cucumber.class)
@Cucumber.Options(
		format={"pretty", "html:target/cucumber"},
		features="src/test/resources"
)
public class AcceptanceTestsRunner { }

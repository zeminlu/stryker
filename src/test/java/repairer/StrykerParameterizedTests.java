package repairer;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import config.StrykerConfig;

import tools.ProgramData;
import tools.StrykerOptions;
import tools.StrykerOptions.SearchStrategy;
import tools.TestingTools;

/**
 * Parameterized tests for stryker
 * 
 * @author Simón Emmanuel Gutiérrez Brida
 * @version 0.1u
 * @see ProgramData
 * @see StrykerOptions
 * @see TestingTools
 */
@RunWith(Parameterized.class)
public class StrykerParameterizedTests {
	/**
	 * The program to fix
	 */
	private ProgramData programData;
	/**
	 * Stryker settings to use
	 */
	private StrykerOptions strykerOptions;
	/**
	 * the method to fix
	 */
	private String methodToFix;
	/**
	 * if a fix is expected or not
	 */
	private boolean fixExpected;
	
	public StrykerParameterizedTests(ProgramData programData, StrykerOptions strykerOptions, String methodToFix, boolean fixExpected) {
		this.programData = programData;
		this.strykerOptions = strykerOptions;
		this.methodToFix = methodToFix;
		this.fixExpected = fixExpected;
	}
	
	@Before
	public void setUp() {
		StrykerConfig.getInstance().resetCompilingSandbox();
	}
	
	@SuppressWarnings("unused")
	@Parameters
	public static Collection<Object[]> firstValues() {
		//PROGRAM DATA DEFINITIONS
		ProgramData simpleClass = new ProgramData("src/test/resources/java/", "utils.SimpleClass");
		ProgramData singlyLinkedList = new ProgramData("src/test/resources/java/", "roops.core.SinglyLinkedList", new String[]{"roops.core.SinglyLinkedListNode"});
		
		//METHOD TO FIX DEFINITIONS
			//SIMPLE CLASS
			String methodToFix_SimpleClass_decX = "decX";
			String methodToFix_SimpleClass_setX = "setX";
			String methodToFix_SimpleClass_twicePlusOne = "twicePlusOne";
			String methodToFix_SimpleClass_altTwicePlusOne = "altTwicePlusOne";
			
			//SINGLY LINKED LIST
		//STRYKER OPTIONS DEFINITIONS
			//NO SCOPES
				//NO RAC
					//DFS
					StrykerOptions noScopes_noRac_DFS_MD_0 = new StrykerOptions(SearchStrategy.DFS, null, 0, false);
					StrykerOptions noScopes_noRac_DFS_MD_1 = new StrykerOptions(SearchStrategy.DFS, null, 1, false);
					StrykerOptions noScopes_noRac_DFS_MD_2 = new StrykerOptions(SearchStrategy.DFS, null, 2, false);
					StrykerOptions noScopes_noRac_DFS_MD_3 = new StrykerOptions(SearchStrategy.DFS, null, 3, false);
					//BFS
					StrykerOptions noScopes_noRac_BFS_MD_0 = new StrykerOptions(SearchStrategy.BFS, null, 0, false);
					StrykerOptions noScopes_noRac_BFS_MD_1 = new StrykerOptions(SearchStrategy.BFS, null, 1, false);
					StrykerOptions noScopes_noRac_BFS_MD_2 = new StrykerOptions(SearchStrategy.BFS, null, 2, false);
					StrykerOptions noScopes_noRac_BFS_MD_3 = new StrykerOptions(SearchStrategy.BFS, null, 3, false);
				//RAC
					//DFS
					StrykerOptions noScopes_withRac_DFS_MD_0 = new StrykerOptions(SearchStrategy.DFS, null, 0, true);
					StrykerOptions noScopes_withRac_DFS_MD_1 = new StrykerOptions(SearchStrategy.DFS, null, 1, true);
					StrykerOptions noScopes_withRac_DFS_MD_2 = new StrykerOptions(SearchStrategy.DFS, null, 2, true);
					StrykerOptions noScopes_withRac_DFS_MD_3 = new StrykerOptions(SearchStrategy.DFS, null, 3, true);
					//BFS
					StrykerOptions noScopes_withRac_BFS_MD_0 = new StrykerOptions(SearchStrategy.BFS, null, 0, true);
					StrykerOptions noScopes_withRac_BFS_MD_1 = new StrykerOptions(SearchStrategy.BFS, null, 1, true);
					StrykerOptions noScopes_withRac_BFS_MD_2 = new StrykerOptions(SearchStrategy.BFS, null, 2, true);
					StrykerOptions noScopes_withRac_BFS_MD_3 = new StrykerOptions(SearchStrategy.BFS, null, 3, true);
			//SIMPLE CLASS
			//SINGLY LINKED LIST
		
		//PARAMETERS
		return Arrays.asList(new Object[][] {
				//SIMPLE CLASS DECX
				{simpleClass, noScopes_noRac_DFS_MD_1, methodToFix_SimpleClass_decX, true},
				{simpleClass, noScopes_noRac_BFS_MD_1, methodToFix_SimpleClass_decX, true},
				{simpleClass, noScopes_withRac_DFS_MD_1, methodToFix_SimpleClass_decX, true},
				{simpleClass, noScopes_withRac_BFS_MD_1, methodToFix_SimpleClass_decX, true},
				//SIMPLE CLASS ALTTWICEPLUSONE
				{simpleClass, noScopes_noRac_DFS_MD_1, methodToFix_SimpleClass_altTwicePlusOne, false},
				{simpleClass, noScopes_noRac_BFS_MD_1, methodToFix_SimpleClass_altTwicePlusOne, false},
				{simpleClass, noScopes_withRac_DFS_MD_1, methodToFix_SimpleClass_altTwicePlusOne, false},
				{simpleClass, noScopes_withRac_BFS_MD_1, methodToFix_SimpleClass_altTwicePlusOne, false},
				{simpleClass, noScopes_noRac_DFS_MD_2, methodToFix_SimpleClass_altTwicePlusOne, false},
				{simpleClass, noScopes_noRac_BFS_MD_2, methodToFix_SimpleClass_altTwicePlusOne, false},
				{simpleClass, noScopes_withRac_DFS_MD_2, methodToFix_SimpleClass_altTwicePlusOne, false},
				{simpleClass, noScopes_withRac_BFS_MD_2, methodToFix_SimpleClass_altTwicePlusOne, false},
				{simpleClass, noScopes_noRac_DFS_MD_3, methodToFix_SimpleClass_altTwicePlusOne, true},
				{simpleClass, noScopes_noRac_BFS_MD_3, methodToFix_SimpleClass_altTwicePlusOne, true},
				{simpleClass, noScopes_withRac_DFS_MD_3, methodToFix_SimpleClass_altTwicePlusOne, true},
				{simpleClass, noScopes_withRac_BFS_MD_3, methodToFix_SimpleClass_altTwicePlusOne, true},
		});
	}
	
	@Test
	public void testFixFoundAsExpected() {
		String toFixMessage = "method " + this.methodToFix + " of " + this.programData.getProgramToFix().getClassName();
		assertTrue(toFixMessage + (fixExpected?" was not fixed as expected":" was fixed but was a fix was not expected"), TestingTools.programFixedAsExpected(this.programData, this.strykerOptions, this.methodToFix, this.fixExpected));
	}

}

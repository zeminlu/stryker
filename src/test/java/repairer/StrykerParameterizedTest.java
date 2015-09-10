package repairer;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import config.StrykerConfig;
import tools.ProgramData;
import tools.StrykerOptions;
import tools.StrykerOptions.RAC;
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
public class StrykerParameterizedTest {
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
	
	public StrykerParameterizedTest(ProgramData programData, StrykerOptions strykerOptions, String methodToFix, boolean fixExpected) {
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
		ProgramData singlyLinkedListBug7 = new ProgramData("src/test/resources/java/", "roops.core.objects.SinglyLinkedListContainsBug7", new String[]{"roops.core.objects.SinglyLinkedListNode"});
		
		//METHOD TO FIX DEFINITIONS
			//SIMPLE CLASS
			String methodToFix_SimpleClass_decX = "decX";
			String methodToFix_SimpleClass_setX = "setX";
			String methodToFix_SimpleClass_twicePlusOne = "twicePlusOne";
			String methodToFix_SimpleClass_altTwicePlusOne = "altTwicePlusOne";
			String methodToFix_SimpleClass_multByfive = "multByfive";
			
			//SINGLY LINKED LIST
			String methodToFix_SinglyLinkedList_contains = "contains";
		//STRYKER OPTIONS DEFINITIONS
			//NO SCOPES
				//NO RAC
					//DFS
					StrykerOptions noScopes_noRac_DFS_MD_0 = new StrykerOptions(SearchStrategy.DFS, null, 0, RAC.DISABLED);
					StrykerOptions noScopes_noRac_DFS_MD_1 = new StrykerOptions(SearchStrategy.DFS, null, 1, RAC.DISABLED);
					StrykerOptions noScopes_noRac_DFS_MD_2 = new StrykerOptions(SearchStrategy.DFS, null, 2, RAC.DISABLED);
					StrykerOptions noScopes_noRac_DFS_MD_3 = new StrykerOptions(SearchStrategy.DFS, null, 3, RAC.DISABLED);
					//BFS
					StrykerOptions noScopes_noRac_BFS_MD_0 = new StrykerOptions(SearchStrategy.BFS, null, 0, RAC.DISABLED);
					StrykerOptions noScopes_noRac_BFS_MD_1 = new StrykerOptions(SearchStrategy.BFS, null, 1, RAC.DISABLED);
					StrykerOptions noScopes_noRac_BFS_MD_2 = new StrykerOptions(SearchStrategy.BFS, null, 2, RAC.DISABLED);
					StrykerOptions noScopes_noRac_BFS_MD_3 = new StrykerOptions(SearchStrategy.BFS, null, 3, RAC.DISABLED);
				//RAC
					//DFS
					StrykerOptions noScopes_withRac_DFS_MD_0 = new StrykerOptions(SearchStrategy.DFS, null, 0, RAC.ENABLED);
					StrykerOptions noScopes_withRac_DFS_MD_1 = new StrykerOptions(SearchStrategy.DFS, null, 1, RAC.ENABLED);
					StrykerOptions noScopes_withRac_DFS_MD_2 = new StrykerOptions(SearchStrategy.DFS, null, 2, RAC.ENABLED);
					StrykerOptions noScopes_withRac_DFS_MD_3 = new StrykerOptions(SearchStrategy.DFS, null, 3, RAC.ENABLED);
					StrykerOptions noScopes_withRac_DFS_MD_4 = new StrykerOptions(SearchStrategy.DFS, null, 4, RAC.ENABLED);
					StrykerOptions noScopes_withRac_DFS_MD_5 = new StrykerOptions(SearchStrategy.DFS, null, 5, RAC.ENABLED);
					StrykerOptions noScopes_withRac_DFS_MD_6 = new StrykerOptions(SearchStrategy.DFS, null, 6, RAC.ENABLED);
					//BFS
					StrykerOptions noScopes_withRac_BFS_MD_0 = new StrykerOptions(SearchStrategy.BFS, null, 0, RAC.ENABLED);
					StrykerOptions noScopes_withRac_BFS_MD_1 = new StrykerOptions(SearchStrategy.BFS, null, 1, RAC.ENABLED);
					StrykerOptions noScopes_withRac_BFS_MD_2 = new StrykerOptions(SearchStrategy.BFS, null, 2, RAC.ENABLED);
					StrykerOptions noScopes_withRac_BFS_MD_3 = new StrykerOptions(SearchStrategy.BFS, null, 3, RAC.ENABLED);
			//SIMPLE CLASS
			//SINGLY LINKED LIST
					//NO RAC
					//RAC
						//DFS
						Map<String, Integer> scopes_1_3 = new HashMap<String, Integer>();
						scopes_1_3.put("roops.core.SinglyLinkedList", 1);
						scopes_1_3.put("roops.core.SinglyLinkedListNode", 3);
						StrykerOptions scopes_1_3_withRac_DFS_MD_0 = new StrykerOptions(SearchStrategy.DFS, scopes_1_3, 0, RAC.ENABLED);
						StrykerOptions scopes_1_3_withRac_DFS_MD_1 = new StrykerOptions(SearchStrategy.DFS, scopes_1_3, 1, RAC.ENABLED);
						StrykerOptions scopes_1_3_withRac_DFS_MD_2 = new StrykerOptions(SearchStrategy.DFS, scopes_1_3, 2, RAC.ENABLED);
						StrykerOptions scopes_1_3_withRac_DFS_MD_3 = new StrykerOptions(SearchStrategy.DFS, scopes_1_3, 3, RAC.ENABLED);
						StrykerOptions scopes_1_3_withRac_DFS_MD_4 = new StrykerOptions(SearchStrategy.DFS, scopes_1_3, 4, RAC.ENABLED);
			//SINGLY LINKED LIST CONTAINS BUG 7
						Map<String, Integer> scopes_1_3_SLLContains7Bug = new HashMap<String, Integer>();
						scopes_1_3_SLLContains7Bug.put("roops.core.objects.SinglyLinkedListContainsBug7", 1);
						scopes_1_3_SLLContains7Bug.put("roops.core.objects.SinglyLinkedListNode", 3);
							//NO RAC
								StrykerOptions scopes_1_3_SLLContains7Bug_withoutRac_DFS_MD_4 = new StrykerOptions(SearchStrategy.DFS, scopes_1_3_SLLContains7Bug, 4, RAC.DISABLED);
							//RAC
								StrykerOptions scopes_1_3_SLLContains7Bug_withRac_DFS_MD_0 = new StrykerOptions(SearchStrategy.DFS, scopes_1_3_SLLContains7Bug, 0, RAC.ENABLED);
								StrykerOptions scopes_1_3_SLLContains7Bug_withRac_DFS_MD_1 = new StrykerOptions(SearchStrategy.DFS, scopes_1_3_SLLContains7Bug, 1, RAC.ENABLED);
								StrykerOptions scopes_1_3_SLLContains7Bug_withRac_DFS_MD_2 = new StrykerOptions(SearchStrategy.DFS, scopes_1_3_SLLContains7Bug, 2, RAC.ENABLED);
								StrykerOptions scopes_1_3_SLLContains7Bug_withRac_DFS_MD_3 = new StrykerOptions(SearchStrategy.DFS, scopes_1_3_SLLContains7Bug, 3, RAC.ENABLED);
								StrykerOptions scopes_1_3_SLLContains7Bug_withRac_DFS_MD_4 = new StrykerOptions(SearchStrategy.DFS, scopes_1_3_SLLContains7Bug, 4, RAC.ENABLED);
		//PARAMETERS
		return Arrays.asList(new Object[][] {
//				//SIMPLE CLASS DECX
//				{simpleClass, noScopes_noRac_DFS_MD_1, methodToFix_SimpleClass_decX, true},
//				{simpleClass, noScopes_noRac_BFS_MD_1, methodToFix_SimpleClass_decX, true},
//				{simpleClass, noScopes_withRac_DFS_MD_1, methodToFix_SimpleClass_decX, true},
//				{simpleClass, noScopes_withRac_BFS_MD_1, methodToFix_SimpleClass_decX, true},
				//SIMPLE CLASS TWICEPLUSONE
//				{simpleClass, noScopes_withRac_DFS_MD_2, methodToFix_SimpleClass_twicePlusOne, true},
//				//SIMPLE CLASS ALTTWICEPLUSONE
//				{simpleClass, noScopes_noRac_DFS_MD_1, methodToFix_SimpleClass_altTwicePlusOne, false},
//				{simpleClass, noScopes_noRac_BFS_MD_1, methodToFix_SimpleClass_altTwicePlusOne, false},
//				{simpleClass, noScopes_withRac_DFS_MD_1, methodToFix_SimpleClass_altTwicePlusOne, false},
//				{simpleClass, noScopes_withRac_BFS_MD_1, methodToFix_SimpleClass_altTwicePlusOne, false},
//				{simpleClass, noScopes_noRac_DFS_MD_2, methodToFix_SimpleClass_altTwicePlusOne, false},
//				{simpleClass, noScopes_noRac_BFS_MD_2, methodToFix_SimpleClass_altTwicePlusOne, false},
//				{simpleClass, noScopes_withRac_DFS_MD_2, methodToFix_SimpleClass_altTwicePlusOne, false},
//				{simpleClass, noScopes_withRac_BFS_MD_2, methodToFix_SimpleClass_altTwicePlusOne, false},
//				{simpleClass, noScopes_noRac_DFS_MD_3, methodToFix_SimpleClass_altTwicePlusOne, true},
//				{simpleClass, noScopes_noRac_BFS_MD_3, methodToFix_SimpleClass_altTwicePlusOne, true},
//				{simpleClass, noScopes_withRac_DFS_MD_3, methodToFix_SimpleClass_altTwicePlusOne, true},
//				{simpleClass, noScopes_withRac_BFS_MD_3, methodToFix_SimpleClass_altTwicePlusOne, true},
				//SIMPLE CLASS MULTBYFIVE
//				{simpleClass, noScopes_withRac_DFS_MD_6, methodToFix_SimpleClass_multByfive, true},
				//SINGLY LINKED LIST CONTAINS
//				{singlyLinkedList, scopes_1_3_withRac_DFS_MD_0, methodToFix_SinglyLinkedList_contains, false},
//				{singlyLinkedList, scopes_1_3_withRac_DFS_MD_1, methodToFix_SinglyLinkedList_contains, false},
//				{singlyLinkedList, scopes_1_3_withRac_DFS_MD_2, methodToFix_SinglyLinkedList_contains, false},
//				{singlyLinkedList, scopes_1_3_withRac_DFS_MD_3, methodToFix_SinglyLinkedList_contains, false},
//				{singlyLinkedList, scopes_1_3_withRac_DFS_MD_4, methodToFix_SinglyLinkedList_contains, true},
				//SINGLY LINKED LIST (STRYKER) CONTAINS BUG7
				//{singlyLinkedListBug7, scopes_1_3_SLLContains7Bug_withRac_DFS_MD_4, methodToFix_SinglyLinkedList_contains, true}
				{singlyLinkedListBug7, scopes_1_3_SLLContains7Bug_withoutRac_DFS_MD_4, methodToFix_SinglyLinkedList_contains, true}
		});
	}
	
	@Test
	public void testFixFoundAsExpected() {
		String toFixMessage = "method " + this.methodToFix + " of " + this.programData.getProgramToFix().getClassName();
		assertTrue(toFixMessage + (fixExpected?" was not fixed as expected":" was fixed but was a fix was not expected"), TestingTools.programFixedAsExpected(this.programData, this.strykerOptions, this.methodToFix, this.fixExpected));
	}

}

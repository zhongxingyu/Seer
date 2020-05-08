package diffson;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import add.features.detector.repairpatterns.MappingAnalysis;
import add.main.ExtractorProperties;
import gumtree.spoon.diff.Diff;

public class DiffICSE2015Test {

	@Test
	public void testFailingTimeoutCase_1555_Move() throws Exception {
		String diffId = "1555";
		ExtractorProperties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("max_synthesis_step", "100000");

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));

		Map<String, Diff> diffOfcommit = new HashMap();

		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void testFailing_MaxNodes_966027() throws Exception {
		String diffId = "966027";
		ExtractorProperties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("max_synthesis_step", "100000");

		String out = new File("./out/tests/case" + "_unidiff").getAbsolutePath();
		DiffContextAnalyzer analyzer = new DiffContextAnalyzer(out);

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	public String getCompletePathICSE2015(String diffId) {
		return getCompletePath("icse2015", diffId);
	}

	public String getCompletePath(String dataset, String diffId) {
		String input = dataset + File.separator + diffId;
		File file = new File("./datasets/" + input);
		input = file.getAbsolutePath();
		return input;
	}

	@Test
	public void testFailingTimeoutCase_3168() throws Exception {
		String diffId = "3168";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void testFailingTimeoutCase_1792() throws Exception {
		String diffId = "1792";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void testFailingTimeoutCase_95() throws Exception {
		String diffId = "95";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void testFailingTimeoutCase_909() throws Exception {
		String diffId = "909";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void testFailingTimeoutCase_2150() throws Exception {
		String diffId = "2150";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void testFailingTimeoutCase_2954() throws Exception {
		String diffId = "2954";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void testFailingTimeoutCase_1806() throws Exception {
		String diffId = "1806";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void testFailingTimeoutCase_4185() throws Exception {
		String diffId = "4185";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void testFailingTimeoutCase_584756() throws Exception {
		String diffId = "584756";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testFailingTimeoutCase_1421510() throws Exception {

		String diffId = "1421510";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testFailingTimeoutCase_613948() throws Exception {

		String diffId = "613948";

		runAndAssertSingleDiff(diffId);
	}

//Diff file 4185_TestTypePromotion 3
	@Test
	public void testFailingTimeoutCase_1305909() throws Exception {

		String diffId = "1305909";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testFailingTimeoutCase_985877() throws Exception {

		String diffId = "985877";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testFailingTimeoutCase_932564() throws Exception {

		String diffId = "932564";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testFailingCase_1103681() throws Exception {
		// To see

		String diffId = "1103681";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testNoChangesCaseCase_1329010() throws Exception {

		String diffId = "1329010";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testChangesCaseCase_1185675() throws Exception {

		String diffId = "1185675";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testNoChangesCase_1381711() throws Exception {

		String diffId = "1381711";

		runAndAssertSingleDiff(diffId);
	}

	public void runAndAssertSingleDiff(String caseId) {
		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File("./datasets/icse2015/" + caseId);
		Map<String, Diff> diffOfcommit = new HashMap();

		analyzer.processDiff(fileDiff, diffOfcommit);

	}

	@Test
	public void test1206439_multiple_transformation() throws Exception {
		String diffId = "1206439";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void test1466707() throws Exception {
		String diffId = "1466707";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void test639434_modifier() throws Exception {
		String diffId = "639434";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void test1422671_modifier() throws Exception {
		String diffId = "1422671";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void test179609_varRef() throws Exception {
		String diffId = "179609";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void test1111185_varRef() throws Exception {
		String diffId = "111185";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void test1458114_nodesInWhile() throws Exception {
		String diffId = "1458114";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "While", MappingAnalysis.MAX_CHILDREN_WHILE);
	}

	@Test
	public void test1481004_nodesInForEach() throws Exception {
		String diffId = "1481004";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "ForEach", MappingAnalysis.MAX_CHILDREN_FOREACH);

	}

	@Test
	public void test1134895_nodesInForEach() throws Exception {
		String diffId = "1134895";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "For", MappingAnalysis.MAX_CHILDREN_FOR);

	}

	@Test
	public void test908568_nodesInForEach() throws Exception {
		String diffId = "908568";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "ForEach", MappingAnalysis.MAX_CHILDREN_FOREACH);

	}

	@Test
	public void test1134896_nodesInFor() throws Exception {
		String diffId = "1134896";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "For", MappingAnalysis.MAX_CHILDREN_FOR);

	}

	@Test
	public void test1366881_nodesInForEach() throws Exception {
		String diffId = "1366881";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "ForEach", MappingAnalysis.MAX_CHILDREN_FOREACH);

	}

	@Test
	public void test1308119_nodesInForEach() throws Exception {
		String diffId = "1308119";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "While", MappingAnalysis.MAX_CHILDREN_WHILE);

	}

	@Test
	public void test831414_nodesInForEach() throws Exception {
		String diffId = "831414";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "ForEach", MappingAnalysis.MAX_CHILDREN_FOREACH);

	}

	@Test
	public void test1099818_nodesInForEach() throws Exception {
		String diffId = "1099818";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "ForEach", MappingAnalysis.MAX_CHILDREN_FOREACH);

	}

	@Test
	public void test1227440_nodesInIf() throws Exception {
		String diffId = "1227440";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "If", MappingAnalysis.MAX_CHILDREN_IF);

	}

	@Test
	public void test733401_nodesInIf() throws Exception {
		String diffId = "733401";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "If", MappingAnalysis.MAX_CHILDREN_IF);

	}

	@Test
	public void test1469889_nodesInIf() throws Exception {
		String diffId = "1469889";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "If", MappingAnalysis.MAX_CHILDREN_IF);

	}

	@Test
	public void test520038_nodesInIf() throws Exception {
		String diffId = "520038";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "If", MappingAnalysis.MAX_CHILDREN_IF);

	}

	@Test
	public void test1580979_nodesInIf() throws Exception {
		String diffId = "1580979";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "If", MappingAnalysis.MAX_CHILDREN_IF);

	}

	@Test
	public void test941106_nodesInIf() throws Exception {
		String diffId = "941106";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "If", MappingAnalysis.MAX_CHILDREN_IF);

	}

	@Test
	public void test1210471_nodesInFor() throws Exception {
		String diffId = "1210471";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "For",
		// MappingAnalysis.MAX_CHILDREN_FOR);

	}

	@Test
	public void test642996_nodesInFor() throws Exception {
		String diffId = "642996";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "For", MappingAnalysis.MAX_CHILDREN_FOR);

	}

	@Test
	public void test998419_nodesInDoWhile() throws Exception {
		String diffId = "998419";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do", MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test1230141_nodesSwitch() throws Exception {
		String diffId = "1230141";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do", MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test761093_nodes() throws Exception {
		String diffId = "761093";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do",
		// MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test1081884_nodes() throws Exception {
		String diffId = "1081884";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do",
		// MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test1561440_nodes() throws Exception {
		String diffId = "1561440";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do",
		// MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test1544900_nodes() throws Exception {
		String diffId = "1544900";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do",
		// MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test1162489_nodes() throws Exception {
		String diffId = "1162489";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do",
		// MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test398382_nodes_var_replaced_by_method_inv() throws Exception {
		String diffId = "398382";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do",
		// MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test934156_nodes_var_replaced_by_method_inv() throws Exception {
		String diffId = "934156";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertMarkedlAST(result, "susp_wrongMethodRef", null, null, false);

	}

	@Test
	public void test14255636_nodes_ifcondition() throws Exception {
		String diffId = "1425563";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertMarkedlAST(result, "susp_wrongMethodRef", null,
		// null, false);

	}

	@Test
	public void test1296989_nodes_try() throws Exception {
		String diffId = "1296989";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertMarkedlAST(result, "susp_wrongMethodRef", null,
		// null, false);

	}

	@Test
	public void test546831_nodes() throws Exception {
		String diffId = "546831";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do",
		// MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test901907_nodesTypes() throws Exception {
		String diffId = "901907";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do",
		// MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test663649_nodesTypes() throws Exception {
		String diffId = "663649";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do",
		// MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test1065143_nodesTypes() throws Exception {
		String diffId = "1065143";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do",
		// MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test658427_nodesSwitch() throws Exception {
		String diffId = "658427";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "Do",
		// MappingAnalysis.MAX_CHILDREN_DO);

	}

	@Test
	public void test9312026_nodesTry() throws Exception {
		String diffId = "931202";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		// SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "For",
		// MappingAnalysis.MAX_CHILDREN_FOR);

	}

	@Test
	public void test1141593_nodesInFor() throws Exception {
		String diffId = "1141593";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "For", MappingAnalysis.MAX_CHILDREN_FOR);

	}

	@Test
	public void test149614_nodesWhile() throws Exception {
		String diffId = "149614";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "While", MappingAnalysis.MAX_CHILDREN_WHILE);

	}

	@Test
	public void test1410888_nodesFor() throws Exception {
		String diffId = "1410888";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "For", MappingAnalysis.MAX_CHILDREN_FOR);

	}

	@Test
	public void test448018_while() throws Exception {
		String diffId = "448018";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "While", MappingAnalysis.MAX_CHILDREN_WHILE);

	}

	@Test
	public void test1025680_while() throws Exception {
		String diffId = "1025680";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);
		printJSON(result);
		SuspiciousASTFaultyTest.assertNumberOfChildrenAST(result, "While", MappingAnalysis.MAX_CHILDREN_WHILE);

	}

	//
	public void printJSON(JsonObject result) {
		System.out.println(gson.toJson(result));
	}

	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Test
	public void test1511590_twoassignments() throws Exception {
		String diffId = "1511590";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void test1196228() throws Exception {
		String diffId = "1196228";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void test611274() throws Exception {
		String diffId = "611274";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void test999479_change_in_field() throws Exception {
		String diffId = "999479";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void test111185() throws Exception {
		String diffId = "111185";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void test_1067234_nodes_not_decorated() throws Exception {
		String diffId = "1067234";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}

	@Test
	public void test_1346833() throws Exception {
		String diffId = "1346833";

		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();

		File fileDiff = new File(getCompletePathICSE2015(diffId));
		Map<String, Diff> diffOfcommit = new HashMap();
		analyzer.processDiff(fileDiff, diffOfcommit);

		JsonObject result = analyzer.atEndCommit(fileDiff, diffOfcommit);

	}
}

package diffson;

import java.io.File;
import java.util.Date;

import org.junit.Test;

import add.main.ExtractorProperties;


/**
 * Experiment runners
 */
public class ExperimentRunner {

	@Test
	public void testICSE2015() throws Exception {
		DiffContextAnalyzer analyzer = new DiffContextAnalyzer();
		analyzer.run(ExtractorProperties.getProperty("icse15difffolder"));
	}

	@Test
	public void testICSE15() throws Exception {
		ExtractorProperties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("MAX_AST_CHANGES_PER_FILE", "20");
		File outFile = new File("./out/ICSE2015_" + (new Date()));
		String out = outFile.getAbsolutePath();
		outFile.mkdirs();
		DiffContextAnalyzer analyzer = new DiffContextAnalyzer(out);
		String input = new File("./datasets/icse2015").getAbsolutePath();
		ExtractorProperties.properties.setProperty("icse15difffolder", input);
		analyzer.run(ExtractorProperties.getProperty("icse15difffolder"));

	}

	@Test
	public void testHDRepair() throws Exception {
		ExtractorProperties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("MAX_AST_CHANGES_PER_FILE", "20");
		File outFile = new File("./out/HdRepair_" + (new Date()));
		String out = outFile.getAbsolutePath();
		outFile.mkdirs();
		DiffContextAnalyzer analyzer = new DiffContextAnalyzer(out);
		String input = new File("/Users/matias/develop/sketch-repair/datasets/pairs-bug-fixes-saner16")
				.getAbsolutePath();
		ExtractorProperties.properties.setProperty("icse15difffolder", input);
		analyzer.run(ExtractorProperties.getProperty("icse15difffolder"));

	}

	@Test
	public void testICSE2018All() throws Exception {
		ExtractorProperties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("MAX_AST_CHANGES_PER_FILE", "20");
		File outFile = new File("./out/icse18_" + (new Date()));
		String out = outFile.getAbsolutePath();
		outFile.mkdirs();
		DiffContextAnalyzer analyzer = new DiffContextAnalyzer(out);
		String input = new File("/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/icse2018-pairs-all")
				.getAbsolutePath();
		ExtractorProperties.properties.setProperty("icse15difffolder", input);
		analyzer.run(ExtractorProperties.getProperty("icse15difffolder"));

	}

	public static void main(String[] args) throws Exception {
		// String name = args[0];
		String inputpath = args[0];
		String output = args[1];

		File outFile = new File(output);
		String out = outFile.getAbsolutePath();
		outFile.mkdirs();
		DiffContextAnalyzer analyzer = new DiffContextAnalyzer(out);
		String input = new File(inputpath).getAbsolutePath();
		ExtractorProperties.properties.setProperty("icse15difffolder", input);
		analyzer.run(ExtractorProperties.getProperty("icse15difffolder"));
	}

	@Test
	public void testD4J() throws Exception {
		ExtractorProperties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("MAX_AST_CHANGES_PER_FILE", "200");
		File outFile = new File("./out/Defects4J_" + (new Date()));
		String out = outFile.getAbsolutePath();
		outFile.mkdirs();
		DiffContextAnalyzer analyzer = new DiffContextAnalyzer(out);
		String input = new File("./datasets/Defects4J").getAbsolutePath();
		ExtractorProperties.properties.setProperty("icse15difffolder", input);
		analyzer.run(ExtractorProperties.getProperty("icse15difffolder"));
	}

	@Test
	public void testCODEREP() throws Exception {
		ExtractorProperties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("excludetests", "false");
		for (int i = 3; i <= 4; i++) {
			File outFile = new File("./out/" + "codeRepDS" + i + "_" + (new Date()));
			String out = outFile.getAbsolutePath();
			outFile.mkdirs();
			DiffContextAnalyzer analyzer = new DiffContextAnalyzer(out);
			String input = new File(
					// "./datasets/codeRepDS" + i
					"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/CodeRep/ds_pairs/result_Dataset" + i
							+ "_unidiff").getAbsolutePath();
			ExtractorProperties.properties.setProperty("icse15difffolder", input);
			analyzer.run(ExtractorProperties.getProperty("icse15difffolder"));
		}
	}

	@Test
	public void testD4Reload() throws Exception {
		ExtractorProperties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("MAX_AST_CHANGES_PER_FILE", "200");
		File outFile = new File("./out/Defects4JReload_" + (new Date()));
		String out = outFile.getAbsolutePath();
		outFile.mkdirs();
		DiffContextAnalyzer analyzer = new DiffContextAnalyzer(out);
		String input = new File("/Users/matias/develop/defects4-repair-reloaded/pairs/D_unassessed/").getAbsolutePath();
		ExtractorProperties.properties.setProperty("icse15difffolder", input);
		analyzer.run(ExtractorProperties.getProperty("icse15difffolder"));
	}

	@Test
	public void test3Sfix() throws Exception {
		ExtractorProperties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("max_synthesis_step", "100000");
		ExtractorProperties.properties.setProperty("MAX_AST_CHANGES_PER_FILE", "200");
		File outFile = new File("./out/3fixtest_" + (new Date()));
		String out = outFile.getAbsolutePath();
		outFile.mkdirs();
		DiffContextAnalyzer analyzer = new DiffContextAnalyzer(out);
		String input = new File("/Users/matias/develop/overfitting/overfitting-data/data/rowdata/3sFix_files_pair/")
				.getAbsolutePath();
		ExtractorProperties.properties.setProperty("icse15difffolder", input);
		analyzer.run(ExtractorProperties.getProperty("icse15difffolder"));
	}

}

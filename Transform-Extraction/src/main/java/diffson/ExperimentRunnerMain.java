package diffson;

import java.io.File;

import add.main.ExtractorProperties;

public class ExperimentRunnerMain {

	public static void main(String[] args) throws Exception {
		// String name = args[0];
		String inputpath = args[0];
		String output = args[1];

		File outFile = new File(output);
		String out = outFile.getAbsolutePath();
		outFile.mkdirs();
		DiffContextAnalyzer analyzer = new DiffContextAnalyzer(out);
		String input = new File(inputpath).getAbsolutePath();
		ExtractorProperties.properties.setProperty("difffolder", input);
		analyzer.run(ExtractorProperties.getProperty("difffolder"));
	}
}

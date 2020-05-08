 package dotInterface;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
import model.OurModel;
 
 public class DotFileUtilHandler {
 
 	private String filename;
 	private File file;
 
 	public static String getDotOutputFolder() {
 		return "dot-test-files".concat(DotFileUtilHandler.getFileSeparator())
 				.concat("tests-output")
 				.concat(DotFileUtilHandler.getFileSeparator());
 	}
 
 	public static String getSbmlExampleModelsFolder() {
 		return "sbml-test-files".concat(DotFileUtilHandler.getFileSeparator());
 	}
 
 	public static String getNewLineSeparator() {
 		return System.getProperty("line.separator");
 	}
 
 	public static String getFileSeparator() {
 		return System.getProperty("file.separator");
 	}
 
 	public static String getPathSeparator() {
 		return System.getProperty("path.separator");
 	}
 
 	public static String getTabString() {
 		return "\t";
 	}
 
 	private DotFileUtilHandler(String filename) {
 		this.filename = filename;
 	}
 
 	public static DotFileUtilHandler MakeHandler(String filename) {
 		return new DotFileUtilHandler(filename);
 	}
 
 	public static DotFileUtilHandler MakeHandlerForExistingFile(String filename) {
 		DotFileUtilHandler dotFileUtilHandler = new DotFileUtilHandler(filename);
 		dotFileUtilHandler.file = new File(filename);
 		return dotFileUtilHandler;
 
 	}
 
 	public DotFileUtilHandler writeDotRepresentationInTestFolder(
 			DotExporter dotExporter) {
 
 		FileWriter outFile;
 		String filenameWithExtension = filename.concat(".dot");
 
 		String savingFilename = DotFileUtilHandler.getDotOutputFolder().concat(
 				filenameWithExtension);
 
 		try {
 			outFile = new FileWriter(savingFilename);
 			PrintWriter out = new PrintWriter(outFile);
 
 			out.append("digraph G {".concat(DotFileUtilHandler
 					.getNewLineSeparator()));
 
 			dotExporter.collectCompleteContent(EndLineFillerWriter
 					.MakeWrapper(out));
 
 			out.append("}");
 
 			out.close();
 
 			// only now we can set the destination, all the dot model generation
 			// steps are successfully completed
 			file = new File(savingFilename);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return this;
 	}
 
 	public static void EvaluateDotFilesInOutputFolder() {
 		File dir = new File(DotFileUtilHandler.getDotOutputFolder());
 
 		for (File existingFile : dir.listFiles()) {
 			DotFileUtilHandler.MakeHandlerForExistingFile(
 					existingFile.getAbsolutePath()).produceSvgOutput();
 		}
 	}
 
 	public void produceSvgOutput() {
 
 		String dotCommand = "dot -Tsvg " + file.getAbsolutePath() + " -o "
 				+ file.getAbsolutePath().replace(".dot", "") + ".svg";
 
 		String command = dotCommand;
 
 		try {
 			Runtime.getRuntime().exec(command).waitFor();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected static DotFileUtilHandler MakeHandlerForExampleSbmlModel(
 			String sbmlExampleSelector) {
 
 		String sourceRelativeFileName = DotFileUtilHandler
 				.getSbmlExampleModelsFolder().concat(sbmlExampleSelector)
 				.concat(".xml");
 
		DotExportable exportable = OurModel
 				.makeOurModel(sourceRelativeFileName);
 
 		DotExporter exporter = new SimpleExporter();
 		exportable.acceptExporter(exporter);
 
 		return DotFileUtilHandler.MakeHandler(sbmlExampleSelector)
 				.writeDotRepresentationInTestFolder(exporter);
 
 	}
 
 }

 package org.openscience.cdk.applications.taverna.iterativeio;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.LineNumberReader;
import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import net.sf.taverna.t2.reference.ExternalReferenceSPI;
 import net.sf.taverna.t2.reference.T2Reference;
 import net.sf.taverna.t2.reference.impl.external.file.FileReference;
 import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;
 
import org.openscience.cdk.CDKConstants;
 import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
 import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
 import org.openscience.cdk.applications.taverna.CDKTavernaException;
 import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
 import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.io.MDLV2000Reader;
 
 public class IterativeSDFileReaderActivity extends AbstractCDKActivity {
 
 	public static final String ITERATIVE_SD_FILE_READER_ACTIVITY = "Iterative SDfile Reader";
 
 	public IterativeSDFileReaderActivity() {
 		this.INPUT_PORTS = new String[] { "File", "# Of Structures Per Iteration" };
 		this.OUTPUT_PORTS = new String[] { "Structures" };
 	}
 
 	@Override
 	protected void addInputPorts() {
 		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
 		expectedReferences.add(FileReference.class);
 		expectedReferences.add(InlineStringReference.class);
 		addInput(this.INPUT_PORTS[0], 0, false, expectedReferences, null);
 		addInput(this.INPUT_PORTS[1], 0, true, null, Integer.class);
 	}
 
 	@Override
 	protected void addOutputPorts() {
 		addOutput(this.OUTPUT_PORTS[0], 1, 0);
 	}
 
 	@Override
 	public String getActivityName() {
 		return IterativeSDFileReaderActivity.ITERATIVE_SD_FILE_READER_ACTIVITY;
 	}
 
 	@Override
 	public HashMap<String, Object> getAdditionalProperties() {
 		HashMap<String, Object> properties = new HashMap<String, Object>();
 		return properties;
 	}
 
 	@Override
 	public String getDescription() {
 		return "Description: " + IterativeSDFileReaderActivity.ITERATIVE_SD_FILE_READER_ACTIVITY;
 	}
 
 	@Override
 	public String getFolderName() {
 		return CDKTavernaConstants.ITERATIVE_IO_FOLDER_NAME;
 	}
 
 	@Override
 	public void work() throws Exception {
 		// Get input
 		File file = this.getInputAsFile(this.INPUT_PORTS[0]);
 		int readSize = this.getInputAsObject(this.INPUT_PORTS[1], Integer.class);
 		// Do work
 		List<T2Reference> outputList = new ArrayList<T2Reference>();
 		int index = 0;
 		try {
 			LineNumberReader lineReader = new LineNumberReader(new FileReader(file));
 			String line;
 			String SDFilePart = "";
 			List<CMLChemFile> resultList = new ArrayList<CMLChemFile>();
 			do {
				line = lineReader.readLine();
 				if (line != null) {
 					SDFilePart += line + "\n";
 					if (line.contains("$$$$")) {
 						try {
 							CMLChemFile cmlChemFile = new CMLChemFile();
 							MDLV2000Reader tmpMDLReader = new MDLV2000Reader(new ByteArrayInputStream(
 									SDFilePart.getBytes()));
 							tmpMDLReader.read(cmlChemFile);
 							tmpMDLReader.close();
							resultList.add(cmlChemFile);	
 						} catch (Exception e) {
 							ErrorLogger.getInstance().writeError("Error reading molecule in SD file:",
 									this.getActivityName(), e);
 							ErrorLogger.getInstance().writeMessage(SDFilePart);
 						} finally {
 							SDFilePart = "";
 						}
 					}
 				}
				if (line == null || resultList.size() >= readSize) {
 					T2Reference containerRef = this.setIterativeOutputAsList(resultList, this.OUTPUT_PORTS[0], index);
 					outputList.add(index, containerRef);
 					index++;
 					resultList.clear();
 				}
 			} while (line != null);
 		} catch (Exception e) {
 			ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + file.getPath(),
 					this.getActivityName(), e);
 			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.READ_FILE_ERROR + file.getPath());
 		}
 		// Set output
 		this.setIterativeReferenceList(outputList, this.OUTPUT_PORTS[0]);
 
 	}
 
 }

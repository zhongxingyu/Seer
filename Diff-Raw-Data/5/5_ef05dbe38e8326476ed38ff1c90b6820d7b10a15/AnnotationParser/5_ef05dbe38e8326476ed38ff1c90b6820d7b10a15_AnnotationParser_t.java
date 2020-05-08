 package org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import org.apache.commons.collections15.MultiMap;
 import org.apache.commons.collections15.map.ListOrderedMap;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.geworkbench.engine.preferences.PreferencesManager;
 import org.geworkbench.engine.properties.PropertiesManager;
 
 import com.Ostermiller.util.CSVParser;
 import com.Ostermiller.util.LabeledCSVParser;
 import com.jgoodies.forms.builder.ButtonBarBuilder;
 
 /**
  * 
  * Description:This Class is for retrieving probe annotation information from
  * default annotation files provided by Affymetrix.
  * 
  * @author Xuegong Wang
  * @author manjunath at genomecenter dot columbia dot edu
  * @version $Id: AnnotationParser.java,v 1.42 2009/12/03 21:44:17 jiz Exp $
  */
 
 public class AnnotationParser implements Serializable {
 	private static final long serialVersionUID = -117234619759135916L;
 
 	static Log log = LogFactory.getLog(AnnotationParser.class);
 
 	public static final String GENE_ONTOLOGY_BIOLOGICAL_PROCESS = "Gene Ontology Biological Process";
 
 	public static final String GENE_ONTOLOGY_CELLULAR_COMPONENT = "Gene Ontology Cellular Component";
 
 	public static final String GENE_ONTOLOGY_MOLECULAR_FUNCTION = "Gene Ontology Molecular Function";
 
 	public static final String GENE_SYMBOL = "Gene Symbol";
 
 	public static final String PROBE_SET_ID = "Probe Set ID";
 
 	public static final String MAIN_DELIMITER = "///";
 
 	// field names
 	public static final String DESCRIPTION = "Gene Title"; // (full name)
 
 	public static final String ABREV = GENE_SYMBOL; // title(short name)
 
 	public static final String PATHWAY = "Pathway"; // pathway
 
 	public static final String GOTERM = GENE_ONTOLOGY_BIOLOGICAL_PROCESS; // Goterms
 
 	public static final String UNIGENE = "UniGene ID"; // Unigene
 
 	public static final String UNIGENE_CLUSTER = "Archival UniGene Cluster";
 
 	public static final String LOCUSLINK = "Entrez Gene"; // LocusLink
 
 	public static final String SWISSPROT = "SwissProt"; // swissprot
 
 	public static final String REFSEQ = "RefSeq Transcript ID"; // RefSeq
 
 	public static final String TRANSCRIPT = "Transcript Assignments";
 
 	public static final String SCIENTIFIC_NAME = "Species Scientific Name";
 
 	public static final String GENOME_VERSION = "Genome Version";
 
 	public static final String ALIGNMENT = "Alignments";
 
 	// columns read into geWorkbench
 	// probe id must be first column read in, and the rest of the columns must
 	// follow the same order
 	// as the columns in the annotation file.
 	private static final String[] labels = {
 			PROBE_SET_ID // probe id must be the first item in this list
 			, SCIENTIFIC_NAME, UNIGENE_CLUSTER, UNIGENE, GENOME_VERSION,
 			ALIGNMENT, DESCRIPTION, GENE_SYMBOL, LOCUSLINK, SWISSPROT, REFSEQ,
 			GENE_ONTOLOGY_BIOLOGICAL_PROCESS, GENE_ONTOLOGY_CELLULAR_COMPONENT,
 			GENE_ONTOLOGY_MOLECULAR_FUNCTION, PATHWAY, TRANSCRIPT };
 
 	// TODO all the DSDataSets handled in this class should be DSMicroarraySet
 	// FIELDS
 	private static DSDataSet<? extends DSBioObject> currentDataSet = null;
 
 	private static Map<DSDataSet<? extends DSBioObject>, String> datasetToChipTypes = new HashMap<DSDataSet<? extends DSBioObject>, String>();
 
 	public static HashMap<String, String> chiptypeMap = new HashMap<String, String>();
 
 	public static Map<String, ListOrderedMap<String, Vector<String>>> geneNameMap = new HashMap<String, ListOrderedMap<String, Vector<String>>>();
 
 	private static ArrayList<String> chipTypes = new ArrayList<String>();
 
 	static MultiMap<String, String> affyToGOID = null;
 	// END FIELDS
 
 	public static APSerializable getSerializable() {
 		// FIXME 
 		return new APSerializable(currentDataSet, datasetToChipTypes,
 				chiptypeMap, geneNameMap, chipTypes, affyToGOID,
 				chipTypeToAnnotation);
 	}
 
 	public static void setFromSerializable(APSerializable aps) {
 		currentDataSet = aps.currentDataSet;
 		datasetToChipTypes = aps.datasetToChipTypes;
 		chiptypeMap = aps.chiptypeMap;
 		geneNameMap = aps.geneNameMap;
 		chipTypes = aps.chipTypes;
 		affyToGOID = aps.affyToGOID;
 		chipTypeToAnnotation = aps.chipTypeToAnnotation;
 	}
 
 	final static String chiptyemapfilename = "chiptypeMap.txt";
 
 	private static String systempDir = System
 			.getProperty("temporary.files.directory");
 
 	public final static String tmpDir;
 
 	public static final String DEFAULT_CHIPTYPE = "HG_U95Av2";
 
 	public static final String TRANSCRIPTASSIGN = "Transcript Assignments";
 
 	public static final String PREF_ANNOTATIONS_MESSAGE = "annotationsMessage";
 
 	public static final String ANNOT_DIR = "annotDir";
 
 	static {
 		if (systempDir == null) {
 			systempDir = "temp" + File.separator + "GEAW";
 		}
 		tmpDir = systempDir + File.separator + "annotationParser/";
 		File dir = new File(tmpDir);
 		if (!dir.exists()) {
 			dir.mkdir();
 		}
 		BufferedReader br = new BufferedReader(new InputStreamReader(
 				AnnotationParser.class.getResourceAsStream(chiptyemapfilename)));
 		try {
 			String str = br.readLine();
 			while (str != null) {
 				String[] data = str.split(",");
 				chiptypeMap.put(data[0].trim(), data[1].trim());
 				chiptypeMap.put(data[1].trim(), data[0].trim());
 				chipTypes.add(data[1].trim());
 				str = br.readLine();
 			}
 			br.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static DSDataSet<? extends DSBioObject> getCurrentDataSet() {
 		return currentDataSet;
 	}
 
 	public static void setCurrentDataSet(DSDataSet<DSBioObject> currentDataSet) {
 		AnnotationParser.currentDataSet = currentDataSet;
 	}
 
 	public static String getCurrentChipType() {
 		if (currentDataSet != null) {
 			return datasetToChipTypes.get(currentDataSet);
 		} else {
 			return null;
 		}
 	}
 
 	public static String getChipType(DSDataSet<? extends DSBioObject> dataset) {
 		return datasetToChipTypes.get(dataset);
 	}
 
 	public static boolean setChipType(DSDataSet<? extends DSBioObject> dataset, String chiptype) {
 		datasetToChipTypes.put(dataset, chiptype);
 		currentDataSet = dataset;
 		if (chiptypeMap.containsValue(chiptype)) {
 			loadAnnotationData(chiptype);
 			return true;
 		}
 		return false;
 	}
 
 	private static boolean setChipType(DSDataSet<? extends DSBioObject> dataset, String chiptype,
 			File annotationData) {
 		datasetToChipTypes.put(dataset, chiptype);
 		currentDataSet = dataset;
 		if(dataset instanceof CSMicroarraySet) {
 			CSMicroarraySet<?> d = (CSMicroarraySet<?>)dataset;
 			d.setAnnotationFileName(annotationData.getAbsolutePath());
 		}
 		return loadAnnotationData(chiptype, annotationData);
 	}
 
 	private static boolean loadAnnotationData(String chipType) {
 		File datafile = new File(chipType + "_annot.csv");
 		return loadAnnotationData(chipType, datafile);
 	}
 
 	private static boolean loadAnnotationData(String chipType, File datafile) {
 		if (datafile.exists()) { // data file is found
 			
 			FileInputStream fis = null;
 			BufferedInputStream bis = null;
 			try {
 				fis = new FileInputStream(datafile);
 				bis = new BufferedInputStream(fis);
 				
 				CSVParser cvsParser = new CSVParser(bis);
 
 				cvsParser.setCommentStart("#;!");// Skip all comments line.
 													// XQ. The bug is reported
 													// by Bernd.
 
 				LabeledCSVParser parser = new LabeledCSVParser(cvsParser);
 
 				MarkerAnnotation markerAnnotation = new MarkerAnnotation();
 				
 				while (parser.getLine() != null) {
 					String affyId = parser.getValueByLabel(labels[0]);
 					AnnotationFields fields = new AnnotationFields();
 					for (int i = 1; i < labels.length; i++) {
 						String label = labels[i];
 						String val = parser.getValueByLabel(label);
 						if (label.equals(GENE_ONTOLOGY_BIOLOGICAL_PROCESS)
 								|| label
 										.equals(GENE_ONTOLOGY_CELLULAR_COMPONENT)
 								|| label
 										.equals(GENE_ONTOLOGY_MOLECULAR_FUNCTION)) {
 							// get rid of leading 0's
 							while (val.startsWith("0") && (val.length() > 0)) {
 								val = val.substring(1);
 							}
 						}
 						if (label.equals(GENE_SYMBOL))
 							fields.setGeneSymbol(val);
 						else if (label.equals(LOCUSLINK))
 							fields.setLocusLink(val);
 						else if (label.equals(SWISSPROT))
 							fields.setSwissProt(val);
 						else if (label.equals(DESCRIPTION))
 							fields.setDescription(val);
 						else if (label.equals(GENE_ONTOLOGY_MOLECULAR_FUNCTION))
 							fields.setMolecularFunction(val);
 						else if (label.equals(GENE_ONTOLOGY_CELLULAR_COMPONENT))
 							fields.setCellularComponent(val);
 						else if (label.equals(GENE_ONTOLOGY_BIOLOGICAL_PROCESS))
 							fields.setBiologicalProcess(val);
 						else if(label.equals(UNIGENE))
 							fields.setUniGene(val);
 						else if(label.equals(REFSEQ))
 							fields.setRefSeq(val);
 					}
 					markerAnnotation.addMarker(affyId, fields);
 				}
 
 				chipTypeToAnnotation.put(chipType, markerAnnotation);
 
 				populateGeneNameMap(chipType);
 				
 				return true;
 			} catch (Exception e) {
 				log.error("", e);
 				return false;
 			}finally{
 				try {
 					fis.close();
 					bis.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				
 			}
 		} else {
 			return false;
 		}
 	}
 
 	private static void populateGeneNameMap(String chipType) {
 		if (chipType != null) {
 			for (String affyid : chipTypeToAnnotation.get(chipType).getMarkerSet()) {
 				if (affyid != null) {
 					String geneName = getGeneName(affyid.trim());
 					if (geneName != null) {
 						if (geneNameMap.get(chipType) == null) {
 							geneNameMap
 									.put(
 											chipType,
 											new ListOrderedMap<String, Vector<String>>());
 						}
 						Vector<String> ids = geneNameMap.get(chipType).get(
 								geneName.trim());
 						if (ids == null) {
 							ids = new Vector<String>();
 						}
 						ids.add(affyid.trim());
 						geneNameMap.get(chipType).put(geneName.trim(), ids);
 					}
 				}
 			}
 		}
 	}
 
 	public static String getGeneName(String id) {
 		try {
 			String chipType = datasetToChipTypes.get(currentDataSet);
 			return chipTypeToAnnotation.get(chipType).getFields(id).getGeneSymbol();
 		} catch (Exception e) {
 			// watkin - removed because it crippled components with repeated
 			// logging
 			// log.warn("Problem getting gene name, returning id. (AffyID: " +
 			// id+")");
 			return id;
 		}
 	}
 
 	/**
 	 * This method returns required annotation field for a given affymatrix marker ID .
 	 * 
 	 * @param affyid
 	 *            affyID as string
 	 * @param fieldID
 	 * 
 	 */
 	// this method used to depend on chipTypeToAnnotations, which take unnecessary large memory
 	// the first step is to re-implement this method so it does not use chipTypeToAnnotations
 	static public String[] getInfo(String affyID, String fieldID) {
 		try {
 			String chipType = datasetToChipTypes.get(currentDataSet);
 			String field = "";
 			
 			AnnotationFields fields = chipTypeToAnnotation.get(chipType).getFields(affyID);
 			// individual field to be process separately to eventually get rid of the large map
 			if(fieldID.equals(ABREV)) { // same as GENE_SYMBOL
 				field = fields.getGeneSymbol();
 			} else if(fieldID.equals(LOCUSLINK)) {
 				field = fields.getLocusLink();
 			} else if(fieldID.equals(DESCRIPTION)) {
 				field = fields.getDescription();
 			} else if(fieldID.equals(GENE_ONTOLOGY_MOLECULAR_FUNCTION)) { 
 				field = fields.getMolecularFunction();
 			} else if(fieldID.equals(GENE_ONTOLOGY_CELLULAR_COMPONENT)) { 
 				field = fields.getCellularComponent();
 			} else if(fieldID.equals(GENE_ONTOLOGY_BIOLOGICAL_PROCESS)) { 
 				field = fields.getBiologicalProcess();
 			} else if(fieldID.equals(UNIGENE)) { 
 				field = fields.getUniGene();
 			} else if(fieldID.equals(REFSEQ)) {
 				field = fields.getRefSeq();
			} else if(fieldID.equals(SWISSPROT)) {
				field = fields.getSwissProt();
 			} else {
				log.error("trying to retreive unsupported field "+fieldID+" from marker annotation. null is returned.");
 				return null;
 			}
 			return field.split(MAIN_DELIMITER);
 		} catch (Exception e) {
 			if (affyID != null) {
 				log
 						.debug("Error getting info for affyId (" + affyID
 								+ "):" + e);
 			}
 			return null;
 		}
 	}
 
 	static public String getInfoAsString(String affyID, String fieldID) {
 		String[] result = getInfo(affyID, fieldID);
 
 		String info = " ";
 		if (result == null) {
 			return affyID;
 		}
 
 		if (result.length > 0) {
 			info = result[0];
 			for (int i = 1; i < result.length; i++) {
 				info += "/" + result[i];
 			}
 		}
 
 		return info;
 	}
 
 	public static Set<String> getSwissProtIDs(String markerID) {
 		String chipType = datasetToChipTypes.get(currentDataSet);
 
 		HashSet<String> set = new HashSet<String>();
 			String[] ids = chipTypeToAnnotation.get(chipType).getFields(markerID).getSwissProt().split("///");
 			for (String s : ids) {
 				set.add(s.trim());
 			}
 		return set;
 	}
 
 	public static String matchChipType(DSDataSet<? extends DSBioObject> dataset, String id,
 			boolean askIfNotFound) {
 		PreferencesManager preferencesManager = PreferencesManager
 				.getPreferencesManager();
 		File prefDir = preferencesManager.getPrefDir();
 		File annotFile = new File(prefDir, "annotations.prefs");
 		if (!annotFile.exists()) {
 			boolean dontShowAgain = showAnnotationsMessage();
 			if (dontShowAgain) {
 				try {
 					annotFile.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		String chip = "Other";
 		currentDataSet = dataset;
 
 		File userFile = selectUserDefinedAnnotation(dataset);
 		if (userFile != null) {
 			chip = userFile.getName();
 			chipTypes.add(chip);
 			setChipType(dataset, chip, userFile);
 		}
 		return chip;
 	}
 
 	public static boolean showAnnotationsMessage() {
 		String message = "To process Affymetrix files many geWorkbench components require information from the associated chip annotation files. Annotation files can be downloaded from the Affymetrix web site, <a href='http://www.affymetrix.com/support/technical/byproduct.affx?cat=arrays' target='_blank'>http://www.affymetrix.com/support/technical/byproduct.affx?cat=arrays</a> (due to the Affymetrix license we are precluded from shipping these files with geWorkbench). Place downloaded files to a directory of your choice; when prompted by geWorkbench point to the appropriate annotation file to be associated with the microarray data you are about to load into the application. Your data will load even if you do not associate them with an annotation file; in that case, some geWorkbench components will not be fully functional.<br>\n"
 				+ "<br>\n"
 				+ "NOTE: Affymetrix requires users to register in order to download annotation files from its web site. Registration is a one time procedure. The credentials (user id and password) acquired via the registration process can then be used in subsequent interactions with the site.<br>\n"
 				+ "<br>\n"
 				+ "Each chip type in the Affymetrix site can have several associated annotation files (with names like \"...Annotations, BLAST\", \"...Annotations, MAGE-ML XML\", etc). Only annotation files named \"...Annotations, CSV\" need to be downloaded (these are the only files that geWorkbench can process).<br>";
 		final JDialog window = new JDialog((Frame) null,
 				"Annotations Information");
 		Container panel = window.getContentPane();
 		JEditorPane textarea = new JEditorPane("text/html", message);
 		textarea.setEditable(false);
 		textarea.addHyperlinkListener(new HyperlinkListener() {
 			public void hyperlinkUpdate(HyperlinkEvent e) {
 				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 					openURL("http://www.affymetrix.com/support/technical/byproduct.affx?cat=arrays");
 				}
 			}
 		});
 		// textarea.setLineWrap(true);
 		// textarea.setWrapStyleWord(true);
 		panel.add(textarea, BorderLayout.CENTER);
 		ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();
 		JCheckBox dontShow = new JCheckBox("Don't show this again");
 		builder.addFixed(dontShow);
 		builder.addGlue();
 		JButton jButton = new JButton("Continue");
 		builder.addFixed(jButton);
 		jButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				window.dispose();
 			}
 		});
 		panel.add(builder.getPanel(), BorderLayout.SOUTH);
 		int width = 500;
 		int height = 450;
 		window.pack();
 		window.setSize(width, height);
 		window
 				.setLocation(
 						(Toolkit.getDefaultToolkit().getScreenSize().width - width) / 2,
 						(Toolkit.getDefaultToolkit().getScreenSize().height - height) / 2);
 		window.setModal(true);
 		window.setVisible(true);
 		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 		return dontShow.isSelected();
 	}
 
 	public static void openURL(String url) {
 		String osName = System.getProperty("os.name");
 		try {
 			if (osName.startsWith("Mac OS")) {
 				Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
 				Method openURL = fileMgr.getDeclaredMethod("openURL",
 						new Class<?>[] { String.class });
 				openURL.invoke(null, new Object[] { url });
 			} else if (osName.startsWith("Windows")) {
 				Runtime.getRuntime().exec(
 						"rundll32 url.dll,FileProtocolHandler " + url);
 			} else { // assume Unix or Linux
 				String[] browsers = { "firefox", "opera", "konqueror",
 						"epiphany", "mozilla", "netscape" };
 				String browser = null;
 				for (int count = 0; count < browsers.length && browser == null; count++)
 					if (Runtime.getRuntime().exec(
 							new String[] { "which", browsers[count] })
 							.waitFor() == 0)
 						browser = browsers[count];
 				if (browser == null)
 					throw new Exception("Could not find web browser");
 				else
 					Runtime.getRuntime().exec(new String[] { browser, url });
 			}
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "Unable to open browser"
 					+ ":\n" + e.getLocalizedMessage());
 		}
 	}
 
 	public static File selectUserDefinedAnnotation(DSDataSet<? extends DSBioObject> dataset) {
 		PropertiesManager properties = PropertiesManager.getInstance();
 		String annotationDir = ".";
 		try {
 			annotationDir = properties.getProperty(AnnotationParser.class,
 					ANNOT_DIR, annotationDir);
 		} catch (IOException e) {
 			e.printStackTrace(); // To change body of catch statement use
 									// File | Settings | File Templates.
 		}
 
 		JFileChooser chooser = new JFileChooser(annotationDir);
 		ExampleFilter filter = new ExampleFilter();
 		filter.addExtension("csv");
 		filter.setDescription("CSV files");
 		chooser.setFileFilter(filter);
 		chooser.setDialogTitle("Please select the annotation file");
 		int returnVal = chooser.showOpenDialog(null);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			File userAnnotations = chooser.getSelectedFile();
 			try {
 				properties.setProperty(AnnotationParser.class, ANNOT_DIR,
 						userAnnotations.getParent());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return userAnnotations;
 		} else {
 			return null;
 		}
 	}
 	
 	private static Map<String, MarkerAnnotation> chipTypeToAnnotation = new TreeMap<String, MarkerAnnotation>();
 	
 	static private class AnnotationFields {
 		String getMolecularFunction() {
 			return molecularFunction;
 		}
 
 		void setMolecularFunction(String molecularFunction) {
 			this.molecularFunction = molecularFunction;
 		}
 
 		String getCellularComponent() {
 			return cellularComponent;
 		}
 
 		void setCellularComponent(String cellularComponent) {
 			this.cellularComponent = cellularComponent;
 		}
 
 		String getBiologicalProcess() {
 			return biologicalProcess;
 		}
 
 		void setBiologicalProcess(String biologicalProcess) {
 			this.biologicalProcess = biologicalProcess;
 		}
 
 		String getUniGene() {
 			return uniGene;
 		}
 
 		void setUniGene(String uniGene) {
 			this.uniGene = uniGene;
 		}
 
 		String getDescription() {
 			return description;
 		}
 
 		void setDescription(String description) {
 			this.description = description;
 		}
 
 		String getGeneSymbol() {
 			return geneSymbol;
 		}
 
 		void setGeneSymbol(String geneSymbol) {
 			this.geneSymbol = geneSymbol;
 		}
 
 		String getLocusLink() {
 			return locusLink;
 		}
 
 		void setLocusLink(String locusLink) {
 			this.locusLink = locusLink;
 		}
 
 		String getSwissProt() {
 			return swissProt;
 		}
 
 		void setSwissProt(String swissProt) {
 			this.swissProt = swissProt;
 		}
 
 		public void setRefSeq(String refSeq) {
 			this.refSeq = refSeq;
 		}
 
 		public String getRefSeq() {
 			return refSeq;
 		}
 
 		private String molecularFunction, cellularComponent, biologicalProcess;
 		private String uniGene, description, geneSymbol, locusLink, swissProt;
 		private String refSeq;
 	}
 	
 	static class MarkerAnnotation {
 		private Map<String, AnnotationFields> annotationFields;
 		private int count;
 		
 		MarkerAnnotation() {
 			count = 0;
 			annotationFields = new TreeMap<String, AnnotationFields>();
 		}
 		
 		void addMarker(String marker, AnnotationFields fields) {
 			annotationFields.put(marker, fields);
 		}
 		
 		AnnotationFields getFields(String marker) {
 			return annotationFields.get(marker);
 		}
 		
 		Set<String> getMarkerSet() {
 			return annotationFields.keySet();
 		}
 		
 		// TODO user this to control when to release this annotation
 		void addUsage() {
 			count++;
 		}
 
 		// TODO user this to control when to release this annotation
 		void removeUsage() {
 			count--;
 		}
 	}
 
 }

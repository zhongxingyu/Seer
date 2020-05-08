 package org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.engine.preferences.PreferencesManager;
 import org.geworkbench.engine.properties.PropertiesManager;
 import org.geworkbench.util.BrowserLauncher;
 import org.geworkbench.util.CsvFileFilter;
 
 import com.jgoodies.forms.builder.ButtonBarBuilder;
 
 /**
  *
  * Description:This Class is for retrieving probe annotation information from
  * default annotation files provided by Affymetrix.
  *
  * @author Xuegong Wang
  * @author manjunath at genomecenter dot columbia dot edu
  * @version $Id$
  */
 
 public class AnnotationParser implements Serializable {
 	private static final long serialVersionUID = -117234619759135916L;
 
 	static Log log = LogFactory.getLog(AnnotationParser.class);
 
 	public static final String GENE_ONTOLOGY_BIOLOGICAL_PROCESS = "Gene Ontology Biological Process";
 
 	public static final String GENE_ONTOLOGY_CELLULAR_COMPONENT = "Gene Ontology Cellular Component";
 
 	public static final String GENE_ONTOLOGY_MOLECULAR_FUNCTION = "Gene Ontology Molecular Function";
 
 	public static final String GENE_SYMBOL = "Gene Symbol";
 
 	public static final String MAIN_DELIMITER = "\\s*///\\s*";
 
 	// field names
 	public static final String DESCRIPTION = "Gene Title"; // (full name)
 
 	public static final String ABREV = GENE_SYMBOL; // title(short name)
 
 	// FIXME this is misleading name. only used by CNKB
 	public static final String GOTERM = GENE_ONTOLOGY_BIOLOGICAL_PROCESS; // Goterms
 
 	public static final String UNIGENE = "UniGene ID"; // Unigene
 
 	public static final String LOCUSLINK = "Entrez Gene"; // LocusLink
 
 	public static final String SWISSPROT = "SwissProt"; // swissprot
 
 	public static final String REFSEQ = "RefSeq Transcript ID"; // RefSeq
 
 	// TODO all the DSDataSets handled in this class should be DSMicroarraySet
 	// FIELDS
 	private static DSDataSet<? extends DSBioObject> currentDataSet = null;
 	private static Map<DSDataSet<? extends DSBioObject>, String> datasetToChipTypes = new HashMap<DSDataSet<? extends DSBioObject>, String>();
 	private static Map<String, Map<String, AnnotationFields>> chipTypeToAnnotation = new TreeMap<String, Map<String, AnnotationFields>>();
 	// END FIELDS
 
 	/* The reason that we need APSerializable is that the status fields are designed as static. */
 	public static APSerializable getSerializable() {
 		return new APSerializable(currentDataSet, datasetToChipTypes,
 				chipTypeToAnnotation);
 	}
 
 	public static void setFromSerializable(APSerializable aps) {
 		currentDataSet = aps.currentDataSet;
 		datasetToChipTypes = aps.datasetToChipTypes;
 		chipTypeToAnnotation = aps.chipTypeToAnnotation;
 	}
 
 	private static final String ANNOT_DIR = "annotDir";
 
 	public static DSDataSet<? extends DSBioObject> getCurrentDataSet() {
 		return currentDataSet;
 	}
 
 	public static void setCurrentDataSet(DSDataSet<?> currentDataSet) {
 		if(!(currentDataSet instanceof CSMicroarraySet)) {
 			AnnotationParser.currentDataSet = null;
 		}
 		AnnotationParser.currentDataSet = currentDataSet;
 	}
 
 	// this method is only used to get the annotation info to be reused for merged dataset,
 	// which may be re-implemented in a better design.
 	// so please do not use this method unless you have a very clear reason
 	public static String getChipType(DSDataSet<? extends DSBioObject> dataset) {
 		return datasetToChipTypes.get(dataset);
 	}
 
 	public static void setChipType(DSDataSet<? extends DSBioObject> dataset, String chiptype) {
 		datasetToChipTypes.put(dataset, chiptype);
 		currentDataSet = dataset;
 	}
 
 	/* this is used to handle annotation file when the real dataset is chosen after annotation. */
 	private static CSMicroarraySet dummyMicroarraySet = new CSMicroarraySet();
 	public static String getLastAnnotationFileName () {
 		return dummyMicroarraySet.getAnnotationFileName();
 	}
 
 	/* if the annotation file is given, this method is called directly without GUI involved */
	public static void loadAnnotationFile(
 			DSDataSet<? extends DSBioObject> dataset, File annotationData) {
 		if (!annotationData.exists()) { // data file is found
 			log.error("Annotation file " + annotationData + " does not exist.");
 			return;
 		}
 
 		String chipType = annotationData.getName();
 
 		AffyAnnotationParser parser = new AffyAnnotationParser(annotationData);
 		Map<String, AnnotationFields> markerAnnotation  = parser.parse(false);
 		if(markerAnnotation!=null)
 			chipTypeToAnnotation.put(chipType, markerAnnotation);
 
 		datasetToChipTypes.put(dataset, chipType);
 		currentDataSet = dataset;
 		if (dataset == null) {
 			dummyMicroarraySet.setAnnotationFileName(annotationData
 					.getAbsolutePath());
 		}
 		if (dataset instanceof CSMicroarraySet) {
 			CSMicroarraySet d = (CSMicroarraySet) dataset;
 			d.setAnnotationFileName(annotationData.getAbsolutePath());
 		}
 	}
 
	/* !!! return value of this method depends on currentDataSet, which could be suprising if not careful */
 	public static String getGeneName(String id) {
 		try {
 			String chipType = datasetToChipTypes.get(currentDataSet);
 			return chipTypeToAnnotation.get(chipType).get(id).getGeneSymbol();
 		} catch (NullPointerException e) {
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
 
 			AnnotationFields fields = chipTypeToAnnotation.get(chipType).get(affyID);
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
 
 	// this method is similar to the previous one except it take dataset instead
 	// of using currentDataSet
 	static public String[] getInfo(DSMicroarraySet<DSMicroarray> dataset,
 			String affyID, String fieldID) {
 		String chipType = datasetToChipTypes.get(dataset);
 		String field = null;
 
 		AnnotationFields fields = chipTypeToAnnotation.get(chipType).get(
 				affyID);
 		// individual field to be process separately to eventually get rid of
 		// the large map
 		if (fieldID.equals(ABREV)) { // same as GENE_SYMBOL
 			field = fields.getGeneSymbol();
 		} else if (fieldID.equals(LOCUSLINK)) {
 			field = fields.getLocusLink();
 		} else if (fieldID.equals(DESCRIPTION)) {
 			field = fields.getDescription();
 		} else if (fieldID.equals(GENE_ONTOLOGY_MOLECULAR_FUNCTION)) {
 			field = fields.getMolecularFunction();
 		} else if (fieldID.equals(GENE_ONTOLOGY_CELLULAR_COMPONENT)) {
 			field = fields.getCellularComponent();
 		} else if (fieldID.equals(GENE_ONTOLOGY_BIOLOGICAL_PROCESS)) {
 			field = fields.getBiologicalProcess();
 		} else if (fieldID.equals(UNIGENE)) {
 			field = fields.getUniGene();
 		} else if (fieldID.equals(REFSEQ)) {
 			field = fields.getRefSeq();
 		} else if (fieldID.equals(SWISSPROT)) {
 			field = fields.getSwissProt();
 		} else {
 			log.error("trying to retreive unsupported field " + fieldID
 					+ " from marker annotation. null is returned.");
 			return null;
 		}
 		return field.split(MAIN_DELIMITER);
 	}
 
 	public static Set<String> getSwissProtIDs(String markerID) {
 		String chipType = datasetToChipTypes.get(currentDataSet);
 
 		HashSet<String> set = new HashSet<String>();
 			String[] ids = chipTypeToAnnotation.get(chipType).get(markerID).getSwissProt().split("///");
 			for (String s : ids) {
 				set.add(s.trim());
 			}
 		return set;
 	}
 
 	public static Set<String> getGeneIDs(String markerID) {
 		HashSet<String> set = new HashSet<String>();
 		String chipType = datasetToChipTypes.get(currentDataSet);
 		
 		// this happens when no annotation or bad annotation is loaded.
 		if(chipType==null) {
 			return set;
 		}
 
 		Map<String, AnnotationFields> annotation = chipTypeToAnnotation.get(chipType);
 		AnnotationFields fields = annotation.get(markerID);
 		if(fields==null) {
 			return set;
 		}
 		String locus = fields.getLocusLink();
 		if(locus==null) {
 			return set;
 		}
 		String[] ids = locus.split("///");
 		for (String s : ids) {
 			set.add(s.trim());
 		}
 		return set;
 	}
 
 	public static Map<String, List<Integer>> getGeneNameToMarkerIDMapping(
 			DSMicroarraySet<? extends DSMicroarray> microarraySet) {
 		Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
 		DSItemList<DSGeneMarker> markers = microarraySet.getMarkers();
 		int index = 0;
 		for (DSGeneMarker marker : markers) {
 			if (marker != null && marker.getLabel() != null) {			 
 				try {
 					
 					Set<String> geneNames = getGeneNames(marker.getLabel());							
 					for (String s : geneNames) {
 						List<Integer> list = map.get(s);
 						if(list==null) {
 							list = new ArrayList<Integer>();
 							list.add(index);
 							map.put(s, list);
 						} else {
 							list.add(index);
 						}
 					}
 					index++;
 				} catch (Exception e) {					 
 					continue;
 				}
 			}
 		}
 		return map;
 	}
 	
 	public static Set<String> getGeneNames(String markerID) {
 		String chipType = datasetToChipTypes.get(currentDataSet);
 
 		HashSet<String> set = new HashSet<String>();
 			String[] ids = chipTypeToAnnotation.get(chipType).get(markerID).getGeneSymbol().split("///");
 			for (String s : ids) {
 				set.add(s.trim());
 			}
 		return set;
 	}
 
 
 	public static String matchChipType(final DSDataSet<? extends DSBioObject> dataset, String id,
 			boolean askIfNotFound) {
 		PreferencesManager preferencesManager = PreferencesManager
 				.getPreferencesManager();
 		File prefDir = preferencesManager.getPrefDir();
 		final File annotFile = new File(prefDir, "annotations.prefs");
 		if (!annotFile.exists()) {
 			try {
 				SwingUtilities.invokeAndWait(new Runnable() {
 
 					public void run() {
 						boolean dontShowAgain = showAnnotationsMessage();
 						if (dontShowAgain) {
 							try {
 								annotFile.createNewFile();
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 
 				});
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			} catch (InvocationTargetException e1) {
 				e1.printStackTrace();
 			}
 		}
 
 		currentDataSet = dataset;
 
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				public void run() {
 					userFile = selectAnnotationFile();
 				}
 			});
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 
 		if (userFile != null) {
 			loadAnnotationFile(dataset, userFile);
 			return userFile.getName();
 		} else {
 			return "Other";
 		}
 	}
 
 	private volatile static File userFile = null;
 
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
 					try {
 						BrowserLauncher.openURL("http://www.affymetrix.com/support/technical/byproduct.affx?cat=arrays");
 					} catch (IOException e1) { // ignore it
 						e1.printStackTrace();
 					}
 				}
 			}
 		});
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
 
 	private static File selectAnnotationFile() {
 		PropertiesManager properties = PropertiesManager.getInstance();
 		String annotationDir = System.getProperty("user.dir"); ;
 		try {
 			annotationDir = properties.getProperty(AnnotationParser.class,
 					ANNOT_DIR, annotationDir);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		JFileChooser chooser = new JFileChooser(annotationDir);
 		chooser.setFileFilter(new CsvFileFilter());
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
 
 	public static void cleanUpAnnotatioAfterUnload(DSDataSet<? extends DSBioObject> dataset) {
 		String annotationName = datasetToChipTypes.get(dataset);
 		datasetToChipTypes.remove(dataset);
 
 		for(DSDataSet<? extends DSBioObject> dset: datasetToChipTypes.keySet() ) {
 			if(datasetToChipTypes.get(dset).equals(annotationName)) return;
 		}
 
 		// if not returned, then it is not used anymore, clean it up
 		if(annotationName!=null)
 			chipTypeToAnnotation.put(annotationName, null);
 	}
 
 }

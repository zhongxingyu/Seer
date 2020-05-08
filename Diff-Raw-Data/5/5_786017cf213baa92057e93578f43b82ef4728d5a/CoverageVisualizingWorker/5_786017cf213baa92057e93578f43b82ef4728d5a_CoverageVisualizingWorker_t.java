 package ui.utils;
 
 import base.SourceLocation;
 import ui.BusinessLogicCoverageAnalyzer;
 import ui.base.SplitCoverage;
 import ui.graphics.CoveragePanel;
 import ui.utils.uiWithWorker.TaskSwingWorker;
 import ui.io.HLDD2VHDLMappingReader;
 import ui.io.CoverageReader;
 import ui.base.HLDD2VHDLMapping;
 import ui.base.NodeItem;
 import io.ConsoleWriter;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import ui.ApplicationForm;
 import ui.ExtendedException;
 
 /**
  * @author Anton Chepurov
  */
 public class CoverageVisualizingWorker extends TaskSwingWorker {
 
 	private final File vhdlFile;
 	private final File covFile;
 	private final File mappingFile;
 	private final ApplicationForm applicationForm;
 	private final ConsoleWriter consoleWriter;
 
 	public CoverageVisualizingWorker(File vhdlFile, File covFile, File mappingFile, ApplicationForm applicationForm, ConsoleWriter consoleWriter) {
 		this.vhdlFile = vhdlFile;
 		this.covFile = covFile;
 		this.mappingFile = mappingFile;
 		this.applicationForm = applicationForm;
 		this.consoleWriter = consoleWriter;
 		executableRunnable = createRunnable();
 	}
 
 	private Runnable createRunnable() {
 		return new Runnable() {
 			public void run() {
 				try {
 					/* Read HLDD-2-VHDL mapping */
 					consoleWriter.write("Mapping HLDD to VHDL...");
 					HLDD2VHDLMapping hldd2VHDLMapping = new HLDD2VHDLMappingReader(mappingFile).getMapping();
 					consoleWriter.done();
 
 					/* Read COV file */
 					consoleWriter.write("Reading coverage file...");
 					CoverageReader coverageReader = new CoverageReader(covFile);
 					Collection<NodeItem> uncoveredNodeItems = coverageReader.getUncoveredNodeItems();
 					consoleWriter.done();
 
 					/* Extract lines for uncovered nodes */
 					SourceLocation allSources = hldd2VHDLMapping.getAllSources();
 					SourceLocation uncoveredSources = hldd2VHDLMapping.getSourceFor(uncoveredNodeItems);
 
 					/* Add tab to the FileViewer */
 					LinkedList<File> allSourceFiles = new LinkedList<File>(allSources.getFiles());
 					allSourceFiles.add(vhdlFile); // just in case it is not in allSources list (no transitions in it, only component instantiations)
 					java.util.Collections.sort(allSourceFiles);
 					for (File sourceFile : allSourceFiles) {
 						Collection<Integer> highlightedLines = uncoveredSources == null ? null : uncoveredSources.getLinesForFile(sourceFile);
 						applicationForm.addFileViewerTabFromFile(sourceFile, highlightedLines, null, null);    
 					}
 
 					/* Add coverage */
 					int total = hldd2VHDLMapping.getAllSources().getTotalLinesNum();
 					int uncovered = uncoveredSources == null ? 0 : uncoveredSources.getTotalLinesNum();
 					CoveragePanel coveragePanel = new CoveragePanel(new SplitCoverage(total - uncovered, total, SplitCoverage.STATEMENT_COVERAGE));
					coveragePanel.setToolTipText("Coverage for top level: " + vhdlFile.getPath());
					applicationForm.addCoverage(BusinessLogicCoverageAnalyzer.generateTabTitle(mappingFile),
							BusinessLogicCoverageAnalyzer.generateTabTooltip(mappingFile), true, coveragePanel);
 
 					isProcessFinished = true;
 				} catch (Exception e) {
 					occurredException = ExtendedException.create(e);
 					isProcessFinished = false;
 				}
 
 			}
 		};
 	}
 
 	protected Boolean doInBackground() {
 		/* Disable UI */
 		enableUI(false);
 
 		return super.doInBackground();
 	}
 
 	protected void done() {
 		/* Enable UI */
 		enableUI(true);
 
 		super.done();
 	}
 
 	private void enableUI(boolean enable) {
 		applicationForm.setEnabledVhdlCoverageButton(enable);
 		applicationForm.setEnabledCovButton(enable);
 		applicationForm.setEnabledShowButton(enable);
 	}
 }

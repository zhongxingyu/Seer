 package org.bh.plugin.excelexport;
 
 import org.apache.log4j.Logger;
 
 import java.awt.Component;
 import java.awt.Desktop;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 
 import org.bh.data.DTOProject;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DistributionMap;
 import org.bh.gui.swing.BHDataExchangeDialog;
 import org.bh.gui.swing.BHDefaultScenarioExportPanel;
 import org.bh.gui.swing.IBHComponent;
 import org.bh.platform.IImportExport;
 import org.bh.platform.i18n.BHTranslator;
 import org.bh.platform.i18n.ITranslator;
import org.bh.plugin.pdfexport.PDFExport.Keys;
 import org.jfree.chart.JFreeChart;
 
 /**
  * Plug-in to export certain scenarios to PDF as a kind of report
  * 
  * @author Norman
  * @version 1.0, 16.01.2010
  * 
  */
 public class ExcelExport implements IImportExport {
 	/**
 	 * Logger for this class
 	 */
 	static final Logger log = Logger.getLogger(ExcelExport.class);
 	ITranslator trans = BHTranslator.getInstance();
 
 	private static final String UNIQUE_ID = "xlsx";
 	private static final String GUI_KEY = "XLSX";
 
 	private static final String FILE_DESC = "Office Open XML Workbook (Excel 2007)";
 	private static final String FILE_EXT = "xlsx";
 
 	public enum Keys {
 		OVERWRITE, OVERWRITETITLE, NOWRITE, NOWRITETITLE;
 
 		@Override
 		public String toString() {
 			return getClass().getName() + "." + super.toString();
 		}
 	}
 
 	XSSFDocumentBuilder db = new XSSFDocumentBuilder();
 
 	@Override
 	public void exportProject(DTOProject project,
 			BHDataExchangeDialog exportDialog) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void exportProject(DTOProject project, String filePath) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void exportProjectResults(DTOProject project,
 			Map<String, Calculable[]> results,
 			BHDataExchangeDialog bhDataExchangeDialog) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void exportProjectResults(DTOProject project,
 			Map<String, Calculable[]> results, String filePath) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void exportProjectResults(DTOProject project,
 			DistributionMap results, BHDataExchangeDialog bhDataExchangeDialog) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void exportProjectResults(DTOProject project,
 			DistributionMap results, String filePath) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void exportScenario(final DTOScenario scenario,
 			final BHDataExchangeDialog exportDialog) {
 
 		final BHDefaultScenarioExportPanel dp = exportDialog
 				.setDefaulExportScenarioPanel(FILE_DESC, FILE_EXT);
 		exportDialog.pack();
 
 		exportDialog.setPluginActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				IBHComponent comp = (IBHComponent) e.getSource();
 				if (comp.getKey().equals("Mexport")) {
 					if (!checkFile(dp.getFilePath(), exportDialog)) {
 						return;
 					}
 					db.newDocument();
 					db.buildScenarioSheet(scenario);
 					db.buildPeriodSheet(scenario);
 					db.closeDocument(dp.getFilePath());
 					log.debug("excel export completed " + dp.getFilePath());
 					if (dp.openAfterExport()) {
 						try {
 							Desktop.getDesktop().open(
 									new File(dp.getFilePath()));
 						} catch (IOException e1) {
 							log.error(e1);
 						}
 					}
 					exportDialog.dispose();
 				}
 				if (comp.getKey().equals("Bcancel")) {
 					exportDialog.dispose();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void exportScenario(DTOScenario scenario, String filePath) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void exportScenarioResults(final DTOScenario scenario,
 			final Map<String, Calculable[]> results, List<JFreeChart> charts,
 			final BHDataExchangeDialog exportDialog) {
 
 		final BHDefaultScenarioExportPanel dp = exportDialog
 				.setDefaulExportScenarioPanel(FILE_DESC, FILE_EXT);
 		exportDialog.pack();
 
 		exportDialog.setPluginActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				IBHComponent comp = (IBHComponent) e.getSource();
 				if (comp.getKey().equals("Mexport")) {
 					if (!checkFile(dp.getFilePath(), exportDialog)) {
 						return;
 					}
 					db.newDocument();
 					db.buildScenarioSheet(scenario);
 					db.buildPeriodSheet(scenario);
 					db.buildResultSheet(results);
 					db.closeDocument(dp.getFilePath());
 					log.debug("excel export completed " + dp.getFilePath());
 					if (dp.openAfterExport()) {
 						try {
 							Desktop.getDesktop().open(
 									new File(dp.getFilePath()));
 						} catch (IOException e1) {
 							log.error(e1);
 						}
 					}
 					exportDialog.dispose();
 				}
 				if (comp.getKey().equals("Bcancel")) {
 					exportDialog.dispose();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void exportScenarioResults(DTOScenario scenario,
 			Map<String, Calculable[]> results, List<JFreeChart> charts,
 			String filePath) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void exportScenarioResults(final DTOScenario scenario,
 			final DistributionMap results, List<JFreeChart> charts,
 			final BHDataExchangeDialog exportDialog) {
 
 		final BHDefaultScenarioExportPanel dp = exportDialog
 				.setDefaulExportScenarioPanel(FILE_DESC, FILE_EXT);
 		exportDialog.pack();
 
 		exportDialog.setPluginActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				IBHComponent comp = (IBHComponent) e.getSource();
 				if (comp.getKey().equals("Mexport")) {
 					if (!checkFile(dp.getFilePath(), exportDialog)) {
 						return;
 					}
 					db.newDocument();
 					db.buildScenarioSheet(scenario);
 					db.buildPeriodSheet(scenario);
 					db.buildResultSheet(results);
 					db.closeDocument(dp.getFilePath());
 					log.debug("excel export completed " + dp.getFilePath());
 					if (dp.openAfterExport()) {
 						try {
 							Desktop.getDesktop().open(
 									new File(dp.getFilePath()));
 						} catch (IOException e1) {
 							log.error(e1);
 						}
 					}
 					exportDialog.dispose();
 				}
 				if (comp.getKey().equals("Bcancel")) {
 					exportDialog.dispose();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void exportScenarioResults(DTOScenario scenario,
 			DistributionMap results, List<JFreeChart> charts, String filePath) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public String getFileDescription() {
 		return FILE_DESC;
 	}
 
 	@Override
 	public String getFileExtension() {
 		return FILE_EXT;
 	}
 
 	@Override
 	public int getSupportedMethods() {
 		return IImportExport.EXP_SCENARIO + IImportExport.EXP_SCENARIO_RES;
 	}
 
 	@Override
 	public String getUniqueId() {
 		return UNIQUE_ID;
 	}
 
 	@Override
 	public DTOProject importProject(BHDataExchangeDialog importDialog) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public DTOScenario importScenario(BHDataExchangeDialog importDialog) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public String getGuiKey() {
 		return GUI_KEY;
 	}
 
 	@Override
 	public String toString() {
 		return FILE_EXT + " - " + FILE_DESC;
 	}
 
 	protected boolean checkFile(String filePath, Component parent) {
 		File f = new File(filePath);
 		if (f.exists()) {
 			if (!f.canWrite()) {
 				JOptionPane.showMessageDialog(parent, trans
 						.translate(Keys.NOWRITE), trans
 						.translate(Keys.NOWRITETITLE),
 						JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 			int res = JOptionPane.showConfirmDialog(parent, trans
 					.translate(Keys.OVERWRITE), trans
 					.translate(Keys.OVERWRITETITLE), JOptionPane.YES_NO_OPTION);
 			if (res == JOptionPane.YES_OPTION) {
 				return true;
 			}
 			return false;
 		}
 		// f does not exist
 		try {
 			f.createNewFile();
 		} catch (IOException e) {
 			// no write rights
 			JOptionPane.showMessageDialog(parent,
 					trans.translate(Keys.NOWRITE), trans
 							.translate(Keys.NOWRITETITLE),
 					JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		// can write
 		f.delete();
 		return true;
 	}
 
 }

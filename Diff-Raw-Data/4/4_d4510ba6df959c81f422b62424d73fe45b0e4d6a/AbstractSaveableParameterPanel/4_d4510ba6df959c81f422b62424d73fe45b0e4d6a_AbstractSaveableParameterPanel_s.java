 package org.geworkbench.analysis;
 
 import java.io.Serializable;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 import org.apache.commons.lang.StringUtils;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 import org.geworkbench.bison.model.analysis.ParamValidationResults;
 import org.geworkbench.bison.model.analysis.ParameterPanel;
 
 /**
  * This class is used to (1) store the parameters of an analysis, and (2) manage
  * the visual representation (gui) that will be offered to the user in order to
  * provide values for the parameters. This abstract class provides default
  * implementations for a number of tasks, including:
  * <p>
  * Assigning a name to a set of parameter values: per the relevant use
  * case, the settings used in performing an analysis can be stored under
  * user-specified name and later potentially retrieved to be reused in another
  * application of that same analysis.
  * <p>
  * 
  * @author First Genetic Trust Inc.
  * @author keshav
  * @author yc2480
  * @version $Id$
  */
 public abstract class AbstractSaveableParameterPanel extends ParameterPanel {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4513020172986430772L;
 
 	private String name = null;
 	protected DSPanel<DSGeneMarker> selectorPanel;
 
 	/**
 	 * 
 	 */
 	public AbstractSaveableParameterPanel() {
 		setName("Parameters");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.bison.model.analysis.ParameterPanel#getName()
 	 */
 	public String getName() {
 		assert name != null;
 		return name;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.bison.model.analysis.ParameterPanel#setName(java.lang.String)
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/*
 	 * We use firstTime information to select the last saved parameter set in
 	 * the JList
 	 */
 	private boolean firstTime = true;
 
 	/**
 	 * 
 	 * @return If you never called setFirstTime(false) yet, it will return true.
 	 */
 	public boolean isFirstTime() {
 		return firstTime;
 	}
 
 	/**
 	 * 
 	 * @param setTo
 	 *            Use setFirstTime(false) to indicate you already selected the
 	 *            last saved parameter
 	 */
 	public void setFirstTime(boolean setTo) {
 		firstTime = setTo;
 	}
 
 	/**
 	 * Return the result of validating the values of the parameters managed by
 	 * this panel. This method will have to be overridden by classes that extend
 	 * <code>AbstractSaveableParameterPanel</code>.
 	 * 
 	 * @return
 	 */
 	public ParamValidationResults validateParameters() {
 		// TODO This method should be made abstract when the
 		// class is made abstract.
 		return new ParamValidationResults(true, "No Error");
 	}
 
 	/*
 	 * Override this method to generate dataset history automatically from
 	 * parameter panel
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see java.awt.Component#toString()
 	 */
 	public String getDataSetHistory() { // 
 		String answer = "getDataSetHistory() not implemented in parameter panel";
 		return answer;
 	}
 
 	/**
 	 * Implement this method to set the parameters in to current parameter
 	 * panel's GUI component. see setParameters() in HierClustPanel.java as
 	 * example.
 	 */
 	public abstract void setParameters(
 			Map<Serializable, Serializable> parameters);
 
 	/**
 	 * Implement this method to let others get current parameters from current
 	 * parameter panel. see getParameters() in HierClustPanel.java as example.
 	 */
 	public abstract Map<Serializable, Serializable> getParameters();
 
 	/*
 	 * To store the Thread used for CallBack, ex: refresh the highlighted groups
 	 * when parameter panel changed
 	 */
 	private Thread callbackThread = null;
 
 	/**
 	 * 
 	 * @param callbackThread
 	 *            Pass-in the thread which will do the real work when
 	 *            notifyAnalysisPanel() been called.
 	 */
 	public void setParameterHighlightCallback(Thread callbackThread) {
 		this.callbackThread = callbackThread;
 	}
 
 	private boolean stopNotifyTemporaryFlag = false;
 
 	/**
 	 * Notify AnalysisPanel to refresh high lighted group
 	 */
 	public void notifyAnalysisPanel() {
 		if ((callbackThread != null) && (stopNotifyTemporaryFlag == false))
			callbackThread.run();
 	}
 
 	public void stopNotifyAnalysisPanelTemporary(boolean b) {
 		stopNotifyTemporaryFlag = b;
 	}
 
 	public boolean getStopNotifyAnalysisPanelTemporaryFlag() {
 		return stopNotifyTemporaryFlag;
 	}
 	
 	/**
 	 * For the parameter fields that are not in the input map, add them as default value
 	 */
 	public abstract void fillDefaultValues(Map<Serializable, Serializable> parameters);
 	
 	public static DSPanel<DSGeneMarker> chooseMarkersSet(String setLabel, DSPanel<DSGeneMarker> selectorPanel){
 		DSPanel<DSGeneMarker> selectedSet = null;
 		if (selectorPanel != null){
 			setLabel = setLabel.trim();
 			for (DSPanel<DSGeneMarker> panel : selectorPanel.panels()) {
 				if (StringUtils.equals(setLabel, panel.getLabel().trim())) {
 					selectedSet = panel;
 					break;
 				}
 			}
 		}
 
 		return selectedSet;
 	}
 	public boolean chooseMarkersFromSet(String setLabel, JTextField toPopulate) {
 		DSPanel<DSGeneMarker> selectedSet = chooseMarkersSet(setLabel, selectorPanel);
 
 		if (selectedSet != null) {
 			if (selectedSet.size() > 0) {
 				StringBuilder sb = new StringBuilder();
 				for (DSGeneMarker m : selectedSet) {
 					sb.append(m.getLabel());
 					sb.append(",");
 				}
 				sb.trimToSize();
 				sb.deleteCharAt(sb.length() - 1); // getting rid of last comma
 				toPopulate.setText(sb.toString());
 				return true;
 			} else {				
 				JOptionPane.showMessageDialog(null, "Marker set, " + setLabel
 						+ ", is empty.", "Input Error",
 						JOptionPane.ERROR_MESSAGE);				
 				
 				return false;
 			}
 		}
 
 		return false;
 	}	
 	
 
 }

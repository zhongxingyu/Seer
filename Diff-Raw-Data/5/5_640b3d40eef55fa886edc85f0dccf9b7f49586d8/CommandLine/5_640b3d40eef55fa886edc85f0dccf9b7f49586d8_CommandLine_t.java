 package org.vpac.grisu.client.view.swing.template.panels;
 
 import java.util.Date;
 
 import org.vpac.grisu.client.model.template.nodes.TemplateNode;
 
 
 /**
  * Different name so a different preprcessor is used.
  * @author Markus Binsteiner
  *
  */
 public class CommandLine extends InputString {
 
 	/**
 	 * Create the panel
 	 */
 	public CommandLine() {
 		super();
 		//
 	}
 	
 	public void reset() {
 		String value = getExternalSetValue();
 
 		if (useHistory)
			historyManager.addHistoryEntry(historyManagerKeyForThisNode, value,
 					new Date());
 
 		if (COMBOBOX_PANEL.equals(renderMode)) {
 			fillComboBox();
 		}
 
 		setDefaultValue();
 	}
 	
 	protected void setDefaultValue() {
 		
 		
 		if ( useLastInput ) {
 			String lastUserInput = holder.getExternalSetValue();
 			if ( lastUserInput != null && !"".equals(lastUserInput)) 
 				historyManager.addHistoryEntry(historyManagerKeyForThisNode+"_"+TemplateNode.LAST_USED_PARAMETER, lastUserInput, new Date(), 1);
 		}
 		
 		
 		String defaultValue = getDefaultValue();
 		if (defaultValue != null) {
 
 			holder.setComponentField(defaultValue);
 		} else {
			holder.setComponentField("");
 		}
 	}
 
 }

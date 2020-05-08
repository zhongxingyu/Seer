 package org.vamdc.validator.gui.mainframe;
 
 import javax.swing.text.JTextComponent;
 
 import org.vamdc.tapservice.vss2.VSSParser;
 import org.vamdc.validator.Setting;
 import org.vamdc.validator.gui.HistoryComboBox;
 import org.vamdc.validator.interfaces.XSAMSIOModel;
 
 public class QueryField extends HistoryComboBox implements ComponentUpdateInterface{
 	
 	private static final long serialVersionUID = -9123198309241131479L;
 	
 	private XSAMSIOModel data;
 	
 	public QueryField(){
 		super(Setting.GUIQueryHistory,";",10);
 		
 	}
 
 	@Override
 	protected void loadValues(){
 		super.loadValues();
 		if (data!=null && data.getSampleQueries()!=null)
 			for (String query:data.getSampleQueries()){
				if (query.length()>1){
					validateQuery(query);
					this.addItem(query+";");
 				}
 			}
 		
 	}
 
 	public static boolean validateQuery(String query) {
 		try{
 			VSSParser.parse(query);
 			return true;
 		}catch (IllegalArgumentException e){
 			System.out.println("Warning! Query '"+query+"' was not valid:");
 			System.out.println(e.getMessage());
 			return false;
 		}
 	}
 
 	/**
 	 * Add query to history. If query is already present, move it up the list.
 	 * @param query query string
 	 */
 	private void saveQuery(String query) {
 		this.saveValue(query);
 		
 		this.removeAllItems();
 		
 		this.loadValues();
 	}
 
 	public String getText() {
 		return this.getEditor().getItem().toString();
 	}
 	
 	public JTextComponent getTextComponent() {
 		return (JTextComponent)this.getEditor().getEditorComponent();
 	}
 
 	@Override
 	public void resetComponent() {
 		loadValues();
 	}
 
 	@Override
 	public void setModel(XSAMSIOModel data) {
 		this.data = data;
 		loadValues();
 	}
 
 	@Override
 	public void updateFromModel(boolean isFinal) {
 		if (isFinal && data!=null){
 			if (data.getLineCount() > 10)
 				saveQuery(data.getQuery());
 			else 
 				loadValues(); 
 			
 		}
 	}
 }

 package calibration;
 
 import java.sql.SQLException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 import org.apache.commons.math.stat.regression.SimpleRegression;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 import data.database.dataAccess.CocomoEstimationAccess;
 import data.database.dataAccess.ConstantsAccess;
 import data.database.dataAccess.NodeBasicInfoAccess;
 import data.database.dataEntities.CocomoEstimationRecord;
 import data.database.dataEntities.Entity;
 import data.file.COCOMOProperties;
 import estimation.COCOMO;
 
 import gui.tabs.ParameterArea;
 
 public class CalibInput extends ParameterArea {
 	private class CalibAction extends Action {
 		public CalibAction(){
 			super("校准");
 		}
 		
 		@Override
 		public void run() {
 			TableItem[] items = table.getItems();
 			SimpleRegression regression = new SimpleRegression();
 			for(TableItem it:items){
 				if(!it.getChecked())
 					continue;
 				
 				double lnSize = Math.log(Double.valueOf(it.getText(1))/1000);
 				double lnEffort = Math.log(Double.valueOf(it.getText(2)));
 				double sumSF = Double.valueOf(it.getText(3));
 				double productEM =Double.valueOf(it.getText(4));
 				regression.addData(lnSize, lnEffort-0.01*sumSF*lnSize-Math.log(productEM));
 				
 			}
 			DecimalFormat df = new DecimalFormat("0.00");
 			double intercept = regression.getIntercept();
 			double slope = regression.getSlope();
 			if(intercept != intercept+0 || slope != slope+0)
 			{
 				msg.setMessage("无法进行校准，A和B值将被重置为默认值");
 				msg.open();
 				resetAction.run();
 			}else{
 				try {
 					double a = Math.exp(intercept);
 					String aString = df.format(a);
 					constants.set("A", a);
 					aLabel.setText(aString);
 					String bString = df.format(regression.getSlope());
 					constants.set("B", slope);
 					bLabel.setText(bString);				
 				} catch (SQLException e) {
 					msg.setMessage(e.getMessage());
 					msg.open();
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	private class ResetAction extends Action{
 		public ResetAction(){
 			super("重置");
 		}
 		
 		@Override
 		public void run() {
 			try {
 				String a = COCOMOProperties.readValue("A");
 				constants.set("A", Double.valueOf(a));
 				aLabel.setText(a);
 				String b = COCOMOProperties.readValue("B");
 				constants.set("B", Double.valueOf(b));
				aLabel.setText(b);
 			} catch (Exception e) {
 				msg.setMessage(e.getMessage());
 				msg.open();
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static final int ID = -2;
 	private Label aLabel, bLabel;
 	private Table table;
 	private ResetAction resetAction;
 	private ConstantsAccess constants;
 	private MessageBox msg;
 	public CalibInput(Composite parent){
 		super(parent, null);
 		constants = new ConstantsAccess();
 		msg = new MessageBox(getShell());
 		form.setText("参数校准");
 		IToolBarManager toolBarManager = form.getToolBarManager();
 		toolBarManager.add(new CalibAction());
 		resetAction = new ResetAction();
 		toolBarManager.add(resetAction);
 		toolBarManager.update(true);
 		showParams();
 		chooseHistoricalProjects();
 	}
 
 	private void showParams() {
 		Composite panel = toolkit.createComposite(form.getBody());
 		panel.setLayout(new GridLayout(2, false));
 		try {
 			toolkit.createLabel(panel, "A =");
 			aLabel = toolkit.createLabel(panel, String.valueOf(constants.get("A")));
 			toolkit.createLabel(panel, "B =");
 			bLabel = toolkit.createLabel(panel, String.valueOf(constants.get("B")));
 		} catch (SQLException e) {
 			msg.setMessage(e.getMessage());
 			msg.open();
 			e.printStackTrace();
 		}
 	}
 	
 	private void chooseHistoricalProjects() {
 		toolkit.createLabel(form.getBody(), "请选择历史项目");
 		table = new Table(form.getBody(), SWT.BORDER | SWT.CHECK);
 		table.setLinesVisible(true);
 		table.setHeaderVisible(true);
 		String[] headers = {"节点名","实际规模（SLOC）", "实际工作量（PM）", "\u03A3SF", "\u03A0EM"};
 		for(String h:headers){
 			TableColumn column = new TableColumn (table, SWT.NONE);
 			column.setText (h);
 		}
 		refresh();
 		for(int i=0;i<headers.length;i++){
 			table.getColumn(i).pack();
 		}
 	}
 
 	@Override
 	public void refresh() {
 		table.removeAll();
 		NodeBasicInfoAccess access = new NodeBasicInfoAccess();
 		CocomoEstimationAccess cAccess = new CocomoEstimationAccess();
 		try {
 			ArrayList<Entity> historicalNodes = access.findAllWhere("realEffort >0 and realSLOC >0");
 			for(Entity node:historicalNodes){
 				TableItem item = new TableItem(table, SWT.NONE);
 				item.setChecked(true);
 				item.setText(0,(String)node.get("name"));
 				item.setText(1,String.valueOf(node.get("realSLOC")));
 				item.setText(2, String.valueOf(node.get("realEffort")));
 				CocomoEstimationRecord cocomo = cAccess.getCocomoEstimationByNodeID((Integer)node.get("id"));
 				if(cocomo!=null){
 					item.setText(3, String.valueOf(cocomo.get("sumSF")));
 					Double productEM = (Double)cocomo.get("productEM");
 					Double sced = COCOMO.getSCEDValue((String)cocomo.get("SCED"));
 					item.setText(4, String.valueOf(productEM*sced));
 				}
 			}
 		} catch (SQLException e) {
 			MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_ERROR);
 			messageBox.setMessage(e.getMessage());
 			messageBox.open();
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public int getTabID(){
 		return ID;
 	}
 }

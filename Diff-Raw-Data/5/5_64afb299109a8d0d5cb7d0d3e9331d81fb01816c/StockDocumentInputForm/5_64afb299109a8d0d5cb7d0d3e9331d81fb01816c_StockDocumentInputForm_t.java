 package custom.forms.stockdocuments;
 
 import generic.form.GenericFormToolbar;
 import generic.form.GenericInputFormI;
 import generic.form.printProcessors.GenericPrintProcessor;
 import generic.tools.MessageObject;
 import hibernate.entityBeans.CompanyCode;
 import hibernate.entityBeans.StockDocument;
 import hibernate.facades.MetadataFacade;
 
 import java.awt.BorderLayout;
 import java.awt.Dialog.ModalExclusionType;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import layouts.RiverLayout;
 import localization.Local;
 import model.custom.DocumentTypeEnum;
 import model.metadata.EntityMetadata;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.view.JasperViewer;
 import remotes.RemotesManager;
 import util.ServerResponse;
 import actions.generic.textfieldValidators.DoubleValidator;
 import actions.generic.textfieldValidators.IntegerValidator;
 import app.Appliction;
 
 public class StockDocumentInputForm extends JPanel implements GenericInputFormI{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7397266131583070611L;
 	private StockDocument stockDocument;
 	
 	private SalesPriceChooser salesPriceChooser;
 	private DocumentSubjectsChooser subjectsChooser;
 	
 	private StockDocumentItemsPanel itemsPanel;
 	
 	private NewDocumentItemPanel newItemPanel = null;
 	
 	private DocumentSummaryInfoPanel summaryInfoPanel = null;
 	
 	private GenericFormToolbar toolbar = null;
 
 	private EntityMetadata entityMetadata = null;
 	
 	private JPanel container = null;
 	
 	public StockDocumentInputForm(EntityMetadata entityMetadata, StockDocument sd){
 		this.entityMetadata = entityMetadata;
 		this.stockDocument = sd;
 		initComponents();
 		addComponents();
 	}
 	
 	private void initComponents(){
 		container = new JPanel();
 		toolbar = new GenericFormToolbar(entityMetadata);
 		toolbar.setEditMode();
 		subjectsChooser = new DocumentSubjectsChooser();
 		salesPriceChooser = new SalesPriceChooser();
 		itemsPanel = new StockDocumentItemsPanel(stockDocument);
 		newItemPanel = new NewDocumentItemPanel(stockDocument, entityMetadata);
 		
 		summaryInfoPanel = new DocumentSummaryInfoPanel();
 		
 		salesPriceChooser.addLookupListener(newItemPanel);
 		itemsPanel.addItemsSelectedListener(newItemPanel);
 		
 		itemsPanel.addTableDataChangedListener(summaryInfoPanel);
 		
 		newItemPanel.addTableDataChangedListener(itemsPanel);
 		
 		container.setLayout(new BorderLayout());
 		setLayout(new BorderLayout());
 	}
 	
 	private void addComponents(){
 		JPanel panel = new JPanel(new RiverLayout());
 		panel.add("p hfill", salesPriceChooser);
 		panel.add("br hfill", subjectsChooser);
 		panel.add("br hfill", newItemPanel);
 		add(toolbar, BorderLayout.NORTH);
 		container.add(panel, BorderLayout.WEST);
 		JPanel p = new JPanel(new BorderLayout());
 		p.add(itemsPanel, BorderLayout.CENTER);
 		p.add(summaryInfoPanel, BorderLayout.SOUTH);
 		container.add(p, BorderLayout.CENTER);
 		add(new JScrollPane(container), BorderLayout.CENTER);
 	}
 	
 	@Override
 	public Object getData() {
 		return stockDocument;
 	}
 
 	@Override
 	public void populateData(Object entity) {
 		stockDocument = (StockDocument) entity;
 		salesPriceChooser.setStockDocument(stockDocument);
 		salesPriceChooser.bindData();
 		
 		subjectsChooser.setStockDocument(stockDocument);
 		subjectsChooser.bindData();
 		
 		itemsPanel.setStockDocument(stockDocument);
 		itemsPanel.bindData();
 		
 		newItemPanel.reset();
 		newItemPanel.setStockDocument(stockDocument);
 		
 		summaryInfoPanel.setStockDocument(stockDocument);
 		summaryInfoPanel.bindData();
 	}
 
 	@Override
 	public void unbindData() {
 		subjectsChooser.unbindData();
 		summaryInfoPanel.unbindData();
 	}
 
 	@Override
 	public void reset() {
 		//ne radi snimanje zbog ovoga
 //		stockDocument = new StockDocument();
 //		newItemPanel.reset();
 	}
 
 	@Override
 	public ServerResponse saveEntity() {
 		ServerResponse sr = null;
 		
 		sr = RemotesManager.getInstance().getGenericPersistenceRemote().selectEntities(CompanyCode.class, 1, null, null);		
 		
 		if(sr.getSeverity()==ServerResponse.INFO){
 			List<CompanyCode> codes = (List<CompanyCode>) sr.getData();
 			if(codes.size()==1){
 				stockDocument.setCompanyCode(codes.get(0));
 			}else{
 				//Izbaciti gresku da nepostoji companycode
 				sr.setSeverity(ServerResponse.ERROR);
 				sr.setResponseCode("STOCKDOCUMENT.NO_COMPANY_CODE_E");
 				sr.setResponseMessage("");
 				return sr;
 			}
 		}
 		
 		if(stockDocument.getID()==null){
 			sr = RemotesManager.getInstance().getStockDocumentRemote().insertStockDocument(stockDocument);
 			stockDocument = (StockDocument) sr.getData();
 		}else
 			sr = RemotesManager.getInstance().getStockDocumentRemote().updateStockDocument(stockDocument);
 		return sr;
 	}
 	
 	public static JTextField getTextField(String dataType, boolean enabled) {
 		JTextField tf = new JTextField(25);
 		tf.setEnabled(enabled);
 		if(dataType.equals("Integer")){
 			tf.setDocument(new IntegerValidator());
 			tf.setHorizontalAlignment(JTextField.RIGHT);
 		}else if(dataType.equals("Double")){
 			tf.setDocument(new DoubleValidator());
 			tf.setHorizontalAlignment(JTextField.RIGHT);
 		}
 		return tf;
 	}
 
 	@Override
 	public MessageObject validateInput() {
 		MessageObject mo = salesPriceChooser.validateData();
 		if(mo.getSeverity()==MessageObject.NONE){
 			mo = subjectsChooser.validateData();
 		}
 		return mo;
 	}
 	
 	@Override
 	public void print() {
 		if(stockDocument.getID() != null){
 			new Thread(){
 				public void run() {
 					List<Object> stockdocList = new ArrayList<Object>();
 					stockdocList.add(stockDocument);
 					GenericPrintProcessor process = new GenericPrintProcessor(stockdocList);
 					GenericPrintProcessor itemsProcessor = new GenericPrintProcessor(new ArrayList<Object>(stockDocument.getItems()));
 					String templateName = "";
 					switch(stockDocument.getDocumentType().getID().intValue()){
 						case DocumentTypeEnum.OUTWARD:
 							templateName = "SDReport.jrxml";
 							break;
 						case DocumentTypeEnum.INWARD:
 							templateName = "SDInReport.jrxml";
 							break;
 						case DocumentTypeEnum.EXPENSE:
 							templateName = "SDExpReport.jrxml";
 							break;
 						case DocumentTypeEnum.DISCOUNT_BILL:
 							templateName = "SDDiscountReport.jrxml";
 							break;
 					}
 					try {
 						JasperReport report = RemotesManager.getInstance().getReportingRemote().getJasperReport(templateName);
 						Map<String, Object> map = new HashMap<String, Object>();
 						map.put("ItemsDataSource", itemsProcessor);
 						map.put("MetadataURL", MetadataFacade.METADATA_URL);
 						JasperPrint print = RemotesManager.getInstance().getReportingRemote().fillJasperReport(report, map, process);
 						JasperViewer jrViewer = new JasperViewer(print, false);
 						jrViewer.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
 						
 						jrViewer.setVisible(true);
 					} catch (Exception e) {
 						e.printStackTrace();
 					} finally{
 						Appliction.getInstance().getPopupProgressBar().setVisible(false);
 					}
 				}
 			}.start();
 		}else{
 			Appliction.getInstance().getPopupProgressBar().setVisible(false);
 		}
 	}
 
 	@Override
 	public void onShow() {
 		// TODO Auto-generated method stub
 		
 	}
 }

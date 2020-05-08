 /**
  * 
  */
 package com.grimesco.gcocentralapp.Resource;
 
 import java.io.FileFilter;
 import java.util.ArrayList;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.grimesco.gcocentral.FImanageFileUpload;
 import com.grimesco.gcocentral.fileDBSyncResult;
 import com.grimesco.gcocentral.axys.dao.AXYSPriceDao;
 import com.grimesco.gcocentral.axys.dao.AXYSSymbolFlagsDao;
 import com.grimesco.gcocentral.fidelity.dao.FIcommentDao;
 import com.grimesco.gcocentral.fidelity.dao.FIpositionDao;
 import com.grimesco.gcocentral.fidelity.dao.FIpriceDao;
 import com.grimesco.gcocentral.fidelity.dao.FItransactionDao;
 import com.grimesco.gcocentral.gco.dao.GCOpriceDao;
 import com.grimesco.gcocentral.schwab.dao.SWpriceDao;
import com.grimesco.gcocentral.schwab.dao.SWsymtranslateDao;
 import com.grimesco.gcocentral.td.dao.TDPriceDao;
 import com.grimesco.gcocentralapp.CustodianTabSheet;
 import com.grimesco.gcocentralapp.GCOCentralSetting;
 import com.grimesco.gcocentralapp.Fidelity.FIFileFilter;
 import com.grimesco.gcocentralapp.TD.TDsymbolTranslationLayout;
 import com.grimesco.gcocore.axys.model.AxysPrice;
 import com.grimesco.gcodataport.AxyspriceExport;
 import com.grimesco.gcodataport.BlotterTranslationErrorFoundException;
 import com.grimesco.gcodataport.FIposExport;
 import com.grimesco.gcodataport.FIpriceExport;
 import com.grimesco.gcodataport.FItrnExport;
 import com.grimesco.gcodataport.TDtrnExport;
 import com.grimesco.gcodataport.axys.FIHandleAxysBlotter;
 import com.grimesco.gcodataport.axys.FIHandleAxysPositionBlotter;
 import com.grimesco.gcodataport.axys.FIHandleAxysPrice;
 import com.grimesco.gcodataport.axys.HandleAxysPrice;
 import com.grimesco.translateFidelity.model.FIcomment;
 import com.grimesco.translateFidelity.model.FIposition;
 import com.grimesco.translateFidelity.model.FIprice;
 import com.grimesco.translateFidelity.model.FItransaction;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Button.ClickEvent;
 
 /**
  * @author jaeboston
  *
  */
 public class ResourceView extends CustodianTabSheet implements View{
 
 	private static Logger logger = LoggerFactory.getLogger(ResourceView.class);
 	
 	private static final long serialVersionUID = 1L;
 	
 	@Autowired
 	private FIpriceDao fiPriceDao;
 
 	@Autowired
 	private SWpriceDao swPriceDao;
 	
 	@Autowired
 	private TDPriceDao tdPriceDao;
 	
 	@Autowired
 	private AXYSPriceDao axysPriceDao;
 	
 	@Autowired
 	private GCOpriceDao  gcoPriceDao;
 	
	@Autowired
	private SWsymtranslateDao symtranslateDao;

	
 	public VerticalLayout gcopriceLayout 	= new GCOpriceLayout(gcoPriceDao);
 	
 	//-- constructor
 	public ResourceView() {
 
 		init();
 	
 		//-------------------------------------------
 		//-- listerner for calendar component Date click event
 		//-------------------------------------------
 		datetimeFileStatus.addValueChangeListener(new Property.ValueChangeListener() {
 			
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void valueChange(ValueChangeEvent event) {
 			
 				//-- Get the new value and format it to the current locale
 				setCurrentWorkingDate((java.util.Date) event.getProperty().getValue());
 				
 				int fipricedatasize = fiPriceDao.findAvailablePriceBySourceDate(getCurrentWorkingDate()).size();
 				int swpricedatasize = swPriceDao.findAvailablePriceBySourceDate(getCurrentWorkingDate()).size();
 				int tdpricedatasize = tdPriceDao.findAvailablePriceBySourceDate(getCurrentWorkingDate()).size();
 				int gcopricedatasize = gcoPriceDao.findAvailablePriceBySourceDate(getCurrentWorkingDate()).size();
 				
 				
 				//-- get the source to populate the file information table
 				currentTableSource = GCOCentralSetting.updatePriceContainer(fipricedatasize, swpricedatasize, tdpricedatasize, gcopricedatasize );
 				availableFileTable.setContainerDataSource(currentTableSource);
 		        
 				//-- decide what components to display depending on the content of currentTableSource
 		        if ( (fipricedatasize +swpricedatasize + tdpricedatasize + gcopricedatasize)  == 0) {
 		        	availableFileTable.setVisible(false);
 		        	priceExportButton.setVisible(false);
 		        	
 		        	Notification.show("No data available for the picked date: " + GCOCentralSetting.dateFormatter.format(getCurrentWorkingDate())); 	//-- Notification
 		        	
 		        } else {
 		        	//-- show the table with available file to import
 		        	
 		        	availableFileTable.setVisible(true);
 		        	priceExportButton.setVisible(true);
 		        } 
 				
 			}
 		});
 	
 		
 		
 		//-------------------------------------------
 		//-- listerner for price export button
 		//-------------------------------------------
 		priceExportButton.addClickListener(new Button.ClickListener() {
 			
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 
 				ArrayList<AxysPrice> masterpriceList = (ArrayList<AxysPrice>) axysPriceDao.findAvailablePriceBySourceDate(getCurrentWorkingDate());
 				
 				logger.debug("getCurrentWorkingDate()= {} ", getCurrentWorkingDate().toString());
 				logger.debug("priceList size = {} ", masterpriceList.size());
 				
 				//-- create price file
				HandleAxysPrice priceHandler = new HandleAxysPrice(getCurrentWorkingDate(), masterpriceList, symbolDao, symtranslateDao);
 				String pricefilename = "Price.pri";
 				
 				//-- Create a report object 
 				AxyspriceExport reportObj = new AxyspriceExport();
 				
 				try {
 						priceHandler.populateAxysPrice(reportObj, getCurrentWorkingDate());
 						
 				} catch (BlotterTranslationErrorFoundException e) {
 					Notification.show("Error found. See the report (F:/Axys3/GCOexports/ErrorReport_DATE.log) ", Notification.Type.ERROR_MESSAGE);
 				}
 				
 				priceHandler.exportPrice(pricefilename);					
 				Notification.show("Export done !", Notification.Type.HUMANIZED_MESSAGE);
 
 			}
 		});
 		
 		
 	} //-- end of constructor
 
 	
 	protected void init() {
 
 		super.posCB.setVisible(false);
 		super.cblCB.setVisible(false);
 		super.priCB.setVisible(false);
 		super.trnCB.setVisible(false);
 		super.exportButton.setVisible(false);
 		super.positionExportButton.setVisible(false);
 	
 		priceExportButton.setCaption(" Export PRI file");
 		
 		this.addTab(importFileLayout, "GCOcentral: Price file Export ");
 		this.addTab(gcopriceLayout, 		"GCOcentral: mannual price entry"				);
 
 		
 	}
 	
 	@Override
 	public void enter(ViewChangeEvent event) {
 		Notification.show("Resources view");
 	}
 	
 
 }

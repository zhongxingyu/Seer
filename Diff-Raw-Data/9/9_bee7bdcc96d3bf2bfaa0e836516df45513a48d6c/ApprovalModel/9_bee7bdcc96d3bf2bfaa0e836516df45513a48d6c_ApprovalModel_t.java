 package com.binar.core.requirementPlanning.approval;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.joda.time.DateTime;
 
 import com.avaje.ebean.EbeanServer;
 import com.binar.entity.ReqPlanning;
 import com.binar.generalFunction.AcceptancePyramid;
 import com.binar.generalFunction.GeneralFunction;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.TextField;
 
 public class ApprovalModel {
 	
 	GeneralFunction function;
 	EbeanServer server;
 	
 	AcceptancePyramid accept;
 	
 	public class AcceptData{
 		private int idReq;
 		private int accepted;
 		private int quantityAccepted;
 		protected int getIdReq() {
 			return idReq;
 		}
 		protected void setIdReq(int idReq) {
 			this.idReq = idReq;
 		}
 		protected int getAccepted() {
 			return accepted;
 		}
 		protected void setAccepted(int accepted) {
 			this.accepted = accepted;
 		}
 		protected int getQuantityAccepted() {
 			return quantityAccepted;
 		}
 		protected void setQuantityAccepted(int quantityAccepted) {
 			this.quantityAccepted = quantityAccepted;
 		}
 		
 	}
 	
 	public ApprovalModel(GeneralFunction function){
 		this.function=function;
 		this.accept=function.getAcceptancePyramid();
 		this.server=function.getServer();
 	}
 	//mendapatkan tabel req planning sesuai bulan yang dipilih
 	public List<ReqPlanning> getTableData(DateTime periode){
 		Date startDate=periode.toDate();
 		Date endDate=periode.withDayOfMonth(periode.dayOfMonth().getMaximumValue()).toDate();
 		List<ReqPlanning> returnValue=server.find(ReqPlanning.class).where().
 				between("period", startDate, endDate).findList();
 		return filterReqPlanning(returnValue);
 	}
 	//memfilter requirement planning sesuai dengan role (PPK, IFRS, PNJ)
 	private List<ReqPlanning> filterReqPlanning(List<ReqPlanning> list){
 		List<ReqPlanning> returnValue=new ArrayList<ReqPlanning>();
 		for(ReqPlanning req:list){
 			if(accept.isShow(req.getAcceptance())){
 				returnValue.add(req);
 			}
 		}
 		return  returnValue;
 	}
 	//mendapatkan data tabel yang mau disimpan
 	private List<AcceptData> getTableData(Container container) {
 		List<AcceptData> returnValue=new ArrayList<AcceptData>();
 		Collection<?> tableItemId=container.getItemIds();
 		for(Object itemId:tableItemId){
 			Item item=container.getItem(itemId);
 			
 			AcceptData acceptData=new AcceptData();
 			CheckBox checkboxResult=(CheckBox)item.getItemProperty("Disetujui?").getValue();
 			TextField textfieldResult=(TextField)item.getItemProperty("Jumlah Disetujui").getValue();
 			int quantityAccepted;
 			try {
 				quantityAccepted=Integer.parseInt(textfieldResult.getValue());
 				
 			} catch (NumberFormatException e) {
 				//jika ada field yang tidak sesuai format misalnya ada teksnya, maka langsung return null
 				//maka nantinya nampilin pesan error
 				return null;
 			}
			acceptData.setAccepted(accept.acceptedOrNot(checkboxResult.getValue()));
 			acceptData.setIdReq((Integer)itemId);
 			acceptData.setQuantityAccepted(quantityAccepted);
 			
 			returnValue.add(acceptData);
 		}
 		return returnValue;
 	}
 
 	//untuk menerima kebutuhan
 	public String acceptData(Container container){
 		List<AcceptData> acceptData=getTableData(container);
 		if(acceptData==null){
 			return "Jumlah kebutuhan harus berupa angka";
 		}
 		//mulai transaksi
 		server.beginTransaction();
 
 		try {
 			for(AcceptData data:acceptData){
 				ReqPlanning planning=server.find(ReqPlanning.class, data.getIdReq());
 				planning.setAcceptedQuantity(data.getQuantityAccepted());
 				
 				//Perbaikan bugs, jika sudah disetujui oleh yang lebih tinggi, persetujuan tidak akan turun pangkat, 
 				//jika yang lebih rendah menyimpan data baru. Kecuali jika checknya dihapus.
 				int currentValue=planning.getAcceptance();
 				
 				if(currentValue>=data.getAccepted() && data.getAccepted()!=accept.getUnacceptCriteria()){
 					planning.setAcceptance(currentValue);
 				}else{
 					planning.setAcceptance(data.getAccepted());					
 				}
 				
 				planning.setDateAccepted(new Date());
 				server.update(planning);
 			}
 			server.commitTransaction();
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			server.rollbackTransaction();
 			return e.getMessage();
 		}finally{
 			server.endTransaction();
 		}
 		
 	}
 	
 	public ReqPlanning getReqPlanning(int idReq){
 		return server.find(ReqPlanning.class, idReq);
 	}
 	
 	
 
 }

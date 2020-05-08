 package com.binar.core.inventoryManagement.deletionList.deletionApproval;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.joda.time.DateTime;
 
 import com.avaje.ebean.EbeanServer;
 import com.binar.core.inventoryManagement.deletionList.deletionApproval.DeletionApprovalView.ApprovalFilter;
 import com.binar.core.requirementPlanning.approval.ApprovalModel.AcceptData;
 import com.binar.entity.DeletedGoods;
 import com.binar.entity.Goods;
 import com.binar.entity.ReqPlanning;
 import com.binar.generalFunction.AcceptancePyramid;
 import com.binar.generalFunction.GeneralFunction;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.TextField;
 
 public class DeletionApprovalModel {
 	
 	GeneralFunction function;
 	EbeanServer server;
 	
 	AcceptancePyramid accept;
 	public class AcceptData{
 		private int idDel;
 		private int accepted;
 		protected int getIdDel() {
 			return idDel;
 		}
 		protected void setIdDel(int idDel) {
 			this.idDel = idDel;
 		}
 		protected int getAccepted() {
 			return accepted;
 		}
 		protected void setAccepted(int accepted) {
 			this.accepted = accepted;
 		}
 		
 	}
 	
 	public DeletionApprovalModel(GeneralFunction function){
 		this.function=function;
 		this.server=function.getServer();
 		this.accept=function.getAcceptancePyramid();
 	}
 	
 	public List<DeletedGoods> getDeletedTable(Date startDate, Date endDate, ApprovalFilter filter ){
 		try {
 			DateTime start=new DateTime(startDate);
 			start=start.withHourOfDay(start.hourOfDay().getMinimumValue());
 			DateTime end=new DateTime(endDate);
 			end=end.withHourOfDay(end.hourOfDay().getMaximumValue());
 			if(start.compareTo(end)>0){
 				DateTime buffer=start;
 				start=end;
 				end=buffer;
 			}
 			switch (filter) {
 				case ACCEPTED: return filterDeletion(server.find(DeletedGoods.class).where().between("deletionDate", start.toDate(), end.toDate()).ge("acceptance", accept.getAcceptCriteria()).findList()); 
 				case ALL: return filterDeletion(server.find(DeletedGoods.class).where().between("deletionDate", start.toDate(), end.toDate()).findList());
 				case NON_ACCEPTED: return filterDeletion(server.find(DeletedGoods.class).where().between("deletionDate", start.toDate(), end.toDate()).le("acceptance", accept.getUnacceptCriteria()).findList());
 				default: return null;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new ArrayList<DeletedGoods>();
 		}
 		
 	}
 	private List<DeletedGoods> filterDeletion(List<DeletedGoods> list){
 		List<DeletedGoods> returnValue=new ArrayList<DeletedGoods>();
 		for(DeletedGoods req:list){
 			if(accept.isShow(req.getAcceptance())){
 				returnValue.add(req);
 			}
 		}
 		return  returnValue;
 	}
 
 	private List<AcceptData> getTableData(Container container) {
 		List<AcceptData> returnValue=new ArrayList<AcceptData>();
 		Collection<?> tableItemId=container.getItemIds();
 		for(Object itemId:tableItemId){
 			Item item=container.getItem(itemId);
 			
 			AcceptData acceptData=new AcceptData();
 			CheckBox checkboxResult=(CheckBox)item.getItemProperty("Disetujui?").getValue();
 			int quantityAccepted;
			acceptData.setAccepted(accept.acceptedOrNot(checkboxResult.getValue()));
 			acceptData.setIdDel((Integer)itemId);
 			
 			returnValue.add(acceptData);
 		}
 		
 		return returnValue;
 	}
 	
 	public String acceptData(Container container){
 		List<AcceptData> acceptData=getTableData(container);
 		if(acceptData==null){
 			return "Jumlah harus berupa angka";
 		}
 		//mulai transaksi
 		server.beginTransaction();
 
 		try {
 			for(AcceptData data:acceptData){
 				DeletedGoods delGoods=server.find(DeletedGoods.class, data.getIdDel());
 				
 				//Perbaikan bugs, jika sudah disetujui oleh yang lebih tinggi, persetujuan tidak akan turun pangkat, 
 				//jika yang lebih rendah menyimpan data baru. Kecuali jika checknya dihapus.
 				int currentValue=delGoods.getAcceptance();
 				
 				if(currentValue>=data.getAccepted() && data.getAccepted()!=accept.getUnacceptCriteria()){
 					delGoods.setAcceptance(currentValue);
 				}else{
 					delGoods.setAcceptance(data.getAccepted());					
 				}
 				delGoods.setAcceptance(data.getAccepted());
 
 				Goods goods=delGoods.getGoods();
 				int stock=goods.getCurrentStock();
 
 				if(accept.isAcceptedByAll(data.getAccepted())){
 					delGoods.setApprovalDate(new Date());
 					stock=stock-delGoods.getQuantity();
 				}else{
 					stock=stock+delGoods.getQuantity();
 					delGoods.setApprovalDate(null);
 				}
 				delGoods.setStockQuantity(stock);
 				
 				server.update(delGoods);
 				Goods goodsDuplicate=server.find(Goods.class, goods.getIdGoods());
 				goodsDuplicate.setCurrentStock(stock);
 				server.update(goodsDuplicate);
 				function.getMinimumStock().update(goods.getIdGoods());
 	
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
 	
 	public DeletedGoods getDeletedGoods(int idDel){
 		return server.find(DeletedGoods.class, idDel);
 	}
 }

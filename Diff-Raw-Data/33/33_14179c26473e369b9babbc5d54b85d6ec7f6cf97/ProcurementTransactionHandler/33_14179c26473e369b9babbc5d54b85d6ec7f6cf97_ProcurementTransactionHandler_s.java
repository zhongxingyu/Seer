 package com.bis.web;
 
 import com.bis.common.DateUtils;
 import com.bis.domain.*;
 import com.bis.inventory.services.StockService;
 import com.bis.procurement.services.ProcurementTransactionService;
 import com.bis.web.viewmodel.ListElement;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static com.bis.common.DateUtils.defaultFormat;
 
 public class ProcurementTransactionHandler {
 
     private ProcurementTransactionService procurementTransactionService;
     private StockService stockService;
 
     public ProcurementTransactionHandler(ProcurementTransactionService procurementTransactionService, StockService stockService) {
         this.procurementTransactionService = procurementTransactionService;
         this.stockService = stockService;
     }
 
     @Transactional
     public void addNewTransaction(ProcurementTransaction procurementTransaction) {
         List<PtDetails> transactionDetails = procurementTransaction.getTransactionDetails();
         for (PtDetails details : transactionDetails) {
             stockService.addStock(details.getItem().getItemCode(), details.getDateOfPublishing(), details.getQuantity());
         }
         procurementTransactionService.addProcurementTransaction(procurementTransaction);
     }
 
     @Transactional
     public void updateTransaction(ProcurementTransaction procurementTransaction) {
         List<PtDetails> updatedTransactionDetails = procurementTransaction.getTransactionDetails();
         ProcurementTransaction originalTransaction = procurementTransactionService.getProcurementTransaction(procurementTransaction.getTransactionId());
         for (PtDetails updatedDetail : updatedTransactionDetails) {
             PtDetails originalDetail = originalTransaction.getPtDetails(updatedDetail.getDetailsId());
             if (originalDetail != null) {
                 Integer quantity = updatedDetail.getQuantity() - originalDetail.getQuantity();
                 if (quantity > 0) {
                     stockService.addStock(updatedDetail.getItem().getItemCode(), updatedDetail.getDateOfPublishing(), quantity);
                 } else if (quantity < 0) {
                     stockService.reduceStock(updatedDetail.getItem().getItemCode(), updatedDetail.getDateOfPublishing(), originalDetail.getQuantity() - updatedDetail.getQuantity());
                 }
             } else {
                 stockService.addStock(updatedDetail.getItem().getItemCode(), updatedDetail.getDateOfPublishing(), updatedDetail.getQuantity());
             }
         }
         for (PtDetails detailsDB : originalTransaction.getTransactionDetails()) {
             PtDetails ptDetails = procurementTransaction.getPtDetails(detailsDB.getDetailsId());
             if (ptDetails == null) {
                 stockService.reduceStock(detailsDB.getItem().getItemCode(), detailsDB.getDateOfPublishing(), detailsDB.getQuantity());
             }
         }
         procurementTransactionService.updateProcurementTransaction(procurementTransaction);
     }
 
     public List<ListElement> getStockDetails(Item item) {
         Date currentDate = DateUtils.currentDate();
        List<Stock> stockList = stockService.getAllStock(item.getItemCode(), DateUtils.addMonth(currentDate, -1), currentDate);
         List<ListElement> stockDetails = new ArrayList<ListElement>();
         if (stockList.isEmpty()) return stockDetails;
        Date date = getNextDateOfPublish(stockList.get(0).getDateOfPublishing(), item.getItemLife());
        stockDetails.add(new ListElement(defaultFormat(date), defaultFormat(date)));
         for (Stock stock : stockList) {
             String dateString = defaultFormat(stock.getDateOfPublishing());
             stockDetails.add(new ListElement(dateString, dateString + ":" + stock.getQuantity()));
         }
         return stockDetails;
     }
 
     private Date getNextDateOfPublish(Date dateOfPublishing, char itemLife) {
         if (ItemCycle.FORTNIGHT.getCode() == itemLife) return DateUtils.addDays(dateOfPublishing, 15);
         if (ItemCycle.MONTHLY.getCode() == itemLife) return DateUtils.addMonth(dateOfPublishing, 1);
         if (ItemCycle.WEEKLY.getCode() == itemLife) return DateUtils.addDays(dateOfPublishing, 7);
         if (ItemCycle.DAILY.getCode() == itemLife) return DateUtils.addDays(dateOfPublishing, 1);
         throw new RuntimeException("Invalid item life");
     }
 
 }

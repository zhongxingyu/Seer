 package com.lavida.swing.handler;
 
 import com.lavida.service.entity.DiscountCardJdo;
 import com.lavida.swing.dialog.AddNewDiscountCardsDialog;
 import com.lavida.swing.service.DiscountCardServiceSwingWrapper;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * Created: 17:06 07.09.13
  *
  * @author Ruslan
  */
 @Component
 public class AddNewDiscountCardsDialogHandler {
 
     @Resource
     private AddNewDiscountCardsDialog dialog;
 
     @Resource
     private DiscountCardServiceSwingWrapper discountCardServiceSwingWrapper;
 
 
     public void cancelButtonClicked() {
         dialog.getTableModel().setSelectedCard(null);
         dialog.getTableModel().setTableData(new ArrayList<DiscountCardJdo>());
         dialog.getTableModel().fireTableDataChanged();
         dialog.hide();
         dialog.getMainForm().update();
     }
 
     public void addRowButtonClicked() {
         DiscountCardJdo discountCardJdo = new DiscountCardJdo();
         dialog.getTableModel().getTableData().add(discountCardJdo);
         dialog.getTableModel().fireTableDataChanged();
         int row = dialog.getTableModel().getTableData().size() - 1;
         dialog.getCardTableComponent().getDiscountCardsTable().editCellAt(row, 0);
         dialog.getCardTableComponent().getDiscountCardsTable().transferFocus();
     }
 
     public void deleteRowButtonClicked() {
         DiscountCardJdo discountCardJdo = dialog.getTableModel().getSelectedCard();
         dialog.getTableModel().getTableData().remove(discountCardJdo);
         dialog.getTableModel().setSelectedCard(null);
         dialog.getTableModel().fireTableDataChanged();
     }
 
     public void acceptCardsButtonClicked() {
         List<DiscountCardJdo> discountCardJdoList = dialog.getTableModel().getTableData();
         while (discountCardJdoList.size() > 0) {
             DiscountCardJdo discountCardJdo = discountCardJdoList.get(0);
             String cardNumber = discountCardJdo.getNumber();
            if (cardNumber != null || !cardNumber.isEmpty()) {
                 DiscountCardJdo existingCard = discountCardServiceSwingWrapper.getByNumber(cardNumber);
                 if (existingCard == null) {
                     discountCardJdo.setRegistrationDate(Calendar.getInstance());
                     discountCardJdo.setActivationDate(Calendar.getInstance());
                     discountCardServiceSwingWrapper.save(discountCardJdo);
                 } else {
                     discountCardJdo.setNumber(null);
                     dialog.showMessage("mainForm.exception.message.dialog.title", "dialog.sell.handler.discount.card.number.exists.message");
                     dialog.getTableModel().fireTableDataChanged();
                     dialog.getCardTableComponent().getCardFiltersComponent().updateAnalyzeComponent();
                     return;
                 }
             } else {
                 discountCardJdo.setNumber(null);
                 dialog.showMessage("mainForm.exception.message.dialog.title", "dialog.sell.handler.discount.card.number.enter.message");
                 dialog.getTableModel().fireTableDataChanged();
                 dialog.getCardTableComponent().getCardFiltersComponent().updateAnalyzeComponent();
                 return;
             }
             discountCardJdoList.remove(discountCardJdo);
         }
         dialog.getTableModel().fireTableDataChanged();
         dialog.getCardTableComponent().getCardFiltersComponent().updateAnalyzeComponent();
     }
 }

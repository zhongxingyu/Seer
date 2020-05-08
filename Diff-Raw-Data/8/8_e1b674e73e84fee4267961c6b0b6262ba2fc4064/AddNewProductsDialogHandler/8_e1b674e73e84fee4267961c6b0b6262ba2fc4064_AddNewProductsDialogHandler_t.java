 package com.lavida.swing.handler;
 
 import com.google.gdata.util.ServiceException;
 import com.lavida.service.entity.ArticleJdo;
 import com.lavida.swing.LocaleHolder;
 import com.lavida.swing.dialog.AddNewProductsDialog;
 import com.lavida.swing.service.ArticleServiceSwingWrapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * The AddNewProductsDialogHandler is a handler for the {@link com.lavida.swing.dialog.AddNewProductsDialog}.
  * Created: 21:57 02.09.13
  *
  * @author Ruslan
  */
 @Component
 public class AddNewProductsDialogHandler {
     private static final Logger logger = LoggerFactory.getLogger(AddNewProductsDialogHandler.class);
 
     @Resource
     private AddNewProductsDialog dialog;
 
     @Resource
     private ArticleServiceSwingWrapper articleServiceSwingWrapper;
 
     @Resource
     protected MessageSource messageSource;
 
     @Resource
     protected LocaleHolder localeHolder;
 
 
     public void addRowButtonClicked() {
         ArticleJdo articleJdo = new ArticleJdo();
         articleJdo.setQuantity(1);
         articleJdo.setMultiplier(2.0);
         articleJdo.setShop(messageSource.getMessage("sellDialog.text.field.shop.LaVida", null, localeHolder.getLocale()));
         if (dialog.getTableModel().getTableData().size() > 0) {
             if (dialog.getTableModel().getTableData().get(0).getDeliveryDate() == null) {
                 dialog.showMessage("mainForm.exception.message.dialog.title", "dialog.add.new.product.deliveryDate.not.filled.message");
                 return;
             } else {
                 articleJdo.setDeliveryDate(dialog.getTableModel().getTableData().get(0).getDeliveryDate());
             }
         }
         dialog.getTableModel().getTableData().add(articleJdo);
         dialog.getTableModel().fireTableDataChanged();
         int row = dialog.getTableModel().getTableData().size() - 1;
         dialog.getArticleTableComponent().getArticlesTable().editCellAt(row, 0);
         dialog.getArticleTableComponent().getArticlesTable().transferFocus();
        dialog.getTableModel().setSelectedArticle(null);
     }
 
     public void deleteRowButtonClicked() {
         ArticleJdo selectedArticle = dialog.getTableModel().getSelectedArticle();
         dialog.getTableModel().getTableData().remove(selectedArticle);
         dialog.getTableModel().setSelectedArticle(null);
         dialog.getTableModel().fireTableDataChanged();
     }
 
     public void cancelButtonClicked() {
         dialog.getTableModel().setSelectedArticle(null);
         dialog.getTableModel().setTableData(new ArrayList<ArticleJdo>());
         dialog.getTableModel().fireTableDataChanged();
         dialog.hide();
         dialog.getMainForm().update();
     }
 
     public void acceptProductsButtonClicked() {
         List<ArticleJdo> newArticles = dialog.getTableModel().getTableData();
         while (newArticles.size() > 0) {
             ArticleJdo newArticle = newArticles.get(0);
             if (newArticle.getCode().isEmpty() || newArticle.getDeliveryDate() == null
                     || newArticle.getTotalCostUAH() == 0) {
                 dialog.showMessage("mainForm.exception.message.dialog.title", "dialog.add.new.product.code.deliveryDate.totalCostUAH.not.filled.message");
                 dialog.getTableModel().fireTableDataChanged();
                 dialog.getArticleTableComponent().getArticleFiltersComponent().updateAnalyzeComponent();
                 return;
             }
             try {
                 articleServiceSwingWrapper.updateToSpreadsheet(newArticle, null);
                 articleServiceSwingWrapper.update(newArticle);
            } catch (IOException | ServiceException e) {
                 logger.warn(e.getMessage(), e);
                 newArticle.setPostponedOperationDate(new Date());
                 articleServiceSwingWrapper.update(newArticle);
             }
             newArticles.remove(newArticle);
         }
         dialog.getTableModel().fireTableDataChanged();
         dialog.getArticleTableComponent().getArticleFiltersComponent().updateAnalyzeComponent();
     }
 
     public void copyRowButtonClicked() {
         ArticleJdo copiedArticle;
         try {
             copiedArticle = (ArticleJdo) dialog.getTableModel().getSelectedArticle().clone();
         } catch (CloneNotSupportedException e) {
             logger.warn(e.getMessage(), e);
             return;
         }
         dialog.getTableModel().getTableData().add(copiedArticle);
         dialog.getTableModel().fireTableDataChanged();
 
     }
 }

 package com.lavida.swing.handler;
 
 import com.google.gdata.util.ServiceException;
 import com.lavida.service.UserService;
 import com.lavida.service.entity.ArticleJdo;
 import com.lavida.swing.LocaleHolder;
 import com.lavida.swing.dialog.SellDialog;
 import com.lavida.swing.dialog.SoldProductsDialog;
 import com.lavida.swing.exception.LavidaSwingRuntimeException;
 import com.lavida.swing.form.MainForm;
 import com.lavida.swing.service.ArticleServiceSwingWrapper;
 import com.lavida.service.ArticleUpdateInfo;
 import com.lavida.swing.service.ArticlesTableModel;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * MainFormHandler
  * <p/>
  * Created: 21:14 09.08.13
  *
  * @author Pavel
  */
 @Component
 public class MainFormHandler {
 
     @Resource
     private MainForm form;
 
     @Resource
     private SellDialog sellDialog;
 
     @Resource
     private MessageSource messageSource;
 
     @Resource
     private SoldProductsDialog soldProductsDialog;
 
     @Resource
     private ArticleServiceSwingWrapper articleServiceSwingWrapper;
 
     @Resource
     protected LocaleHolder localeHolder;
 
     @Resource(name = "notSoldArticleTableModel")
     private ArticlesTableModel tableModel;
 
     @Resource
     private UserService userService;
 
 
     /**
      * The ActionListener for refreshButton component.
      * When button is clicked data for ArticleJdo List loads from Google spreadsheet
      * and saves to database, then it goes through filterSold() for deleting all sold
      * goods and renders to the articleTable.
      */
     public void refreshButtonClicked() {
         try {
             List<ArticleJdo> articles = articleServiceSwingWrapper.loadArticlesFromRemoteServer();
             ArticleUpdateInfo informer = articleServiceSwingWrapper.updateToDatabase(articles);
             showUpdateInfoMessage(informer);
            form.getArticleTableComponent().getArticleFiltersComponent().updateAnalyzeComponent();
             form.update();    // repaint MainForm in some time
 
         } catch (IOException e) {
             throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.GOOGLE_IO_EXCEPTION, e, form);
         } catch (ServiceException e) {
             throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.GOOGLE_SERVICE_EXCEPTION, e, form);
         }
     }
 
     /**
      * Shows a JOptionPane message with the information about the articles updating  process.
      *
      * @param informer the ArticleUpdateInfo to be shown.
      */
     private void showUpdateInfoMessage(ArticleUpdateInfo informer) {
         StringBuilder messageBuilder = new StringBuilder();
         messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.articles.added",
                 null, localeHolder.getLocale()));
         messageBuilder.append(informer.getAddedCount());
         messageBuilder.append(". \n");
         messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.articles.updated",
                 null, localeHolder.getLocale()));
         messageBuilder.append(informer.getUpdatedCount());
         messageBuilder.append(". \n");
         messageBuilder.append(messageSource.getMessage("mainForm.panel.refresh.message.articles.deleted",
                 null, localeHolder.getLocale()));
         messageBuilder.append(informer.getDeletedCount());
         messageBuilder.append(". \n");
 
         form.showMessageBox("mainForm.panel.refresh.message.title", new String(messageBuilder));
     }
 
     public void sellButtonClicked() {
         if (tableModel.getSelectedArticle() != null) {
             sellDialog.initWithArticleJdo(tableModel.getSelectedArticle());
             sellDialog.show();
 
         } else {
             form.showMessage("mainForm.exception.message.dialog.title", "mainForm.handler.sold.article.not.chosen");
         }
     }
 
     public void recommitButtonClicked() {
         List<ArticleJdo> articles = articleServiceSwingWrapper.getAll();
         for (ArticleJdo articleJdo : articles) {
             if (articleJdo.getPostponedOperationDate() != null) {
                 try {
                     if (articleJdo.getSold() != null) {   //recommit selling
                         articleServiceSwingWrapper.updateToSpreadsheet(articleJdo, new Boolean(true));
                     } else if (articleJdo.getSold() == null && articleJdo.getRefundDate() != null) {  // recommit refunding
                         articleServiceSwingWrapper.updateToSpreadsheet(articleJdo, new Boolean(false));
                         articleJdo.setSaleDate(null);
                     } else {
                         articleServiceSwingWrapper.updateToSpreadsheet(articleJdo, null); // recommit other changes
                     }
                     articleJdo.setPostponedOperationDate(null);
                     articleServiceSwingWrapper.update(articleJdo);
                     showPostponedOperationsMessage();
                 } catch (IOException e) {
                     form.showMessage("mainForm.exception.message.dialog.title", "sellDialog.handler.sold.article.not.saved.to.worksheet");
                     showPostponedOperationsMessage();
                     throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.GOOGLE_IO_EXCEPTION, e, form);
                 } catch (ServiceException e) {
                     form.showMessage("mainForm.exception.message.dialog.title", "sellDialog.handler.sold.article.not.saved.to.worksheet");
                     showPostponedOperationsMessage();
                     throw new LavidaSwingRuntimeException(LavidaSwingRuntimeException.GOOGLE_SERVICE_EXCEPTION, e, form);
                 }
             }
         }
     }
 
     public void showPostponedOperationsMessage() {
         int count = 0;
         List<ArticleJdo> articles = articleServiceSwingWrapper.getAll();
         for (ArticleJdo articleJdo : articles) {
             if (articleJdo.getPostponedOperationDate() != null) {
                 ++count;
             }
         }
         if (count > 0) {
             form.getPostponedMessage().setText(String.valueOf(count));
         }
     }
 
     public void showSoldProductsButtonClicked() {
         soldProductsDialog.filterAnalyzePanelByRoles(userService.getCurrentUserRoles());
         soldProductsDialog.show();
     }
 }

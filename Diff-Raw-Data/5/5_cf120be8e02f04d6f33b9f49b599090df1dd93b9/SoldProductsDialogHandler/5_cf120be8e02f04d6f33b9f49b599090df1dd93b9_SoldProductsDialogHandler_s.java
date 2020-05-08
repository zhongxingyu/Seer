 package com.lavida.swing.handler;
 
 import com.lavida.service.utils.CalendarConverter;
 import com.lavida.swing.ExchangerHolder;
 import com.lavida.swing.LocaleHolder;
 import com.lavida.swing.dialog.SoldProductsDialog;
 import com.lavida.swing.form.component.ArticleFiltersComponent;
 import com.lavida.swing.service.ArticleServiceSwingWrapper;
 import com.lavida.swing.service.ArticlesTableModel;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * Created: 15:32 18.08.13
  *
  * @author Ruslan
  */
 @Component
 public class SoldProductsDialogHandler {
 
     @Resource
     private SoldProductsDialog soldProductsDialog;
 
     @Resource
     private MessageSource messageSource;
 
     @Resource
     private LocaleHolder localeHolder;
 
     @Resource
     private ArticleServiceSwingWrapper articleServiceSwingWrapper;
 
     @Resource(name = "notSoldArticleTableModel")
     private ArticlesTableModel tableModel;
 
     @Resource
     private ExchangerHolder exchangerHolder;
 
     public void refundButtonClicked() {
 
     }
 
     public void cancelButtonClicked() {
         soldProductsDialog.hide();
     }
 
     public void currentDateCheckBoxSelected() {
        String currentDate = CalendarConverter.convertCalendarToString(Calendar.getInstance());
 
         List<ArticleFiltersComponent.FilterUnit> filters = soldProductsDialog.getArticleTableComponent().
                 getArticleFiltersComponent().getFilters();
         for (ArticleFiltersComponent.FilterUnit filterUnit : filters) {
             if (messageSource.getMessage("mainForm.table.articles.column.sell.date.title", null, localeHolder.getLocale()).
                     equalsIgnoreCase(filterUnit.columnTitle)) {
                 filterUnit.textField.setText(currentDate);
             }
         }
     }
 
     public void currentDateCheckBoxDeSelected() {
         List<ArticleFiltersComponent.FilterUnit> filters = soldProductsDialog.getArticleTableComponent().
                 getArticleFiltersComponent().getFilters();
         for (ArticleFiltersComponent.FilterUnit filterUnit : filters) {
             if (messageSource.getMessage("mainForm.table.articles.column.sell.date.title", null, localeHolder.getLocale()).
                     equalsIgnoreCase(filterUnit.columnTitle)) {
                 filterUnit.textField.setText("");
             }
         }
 
     }
 }

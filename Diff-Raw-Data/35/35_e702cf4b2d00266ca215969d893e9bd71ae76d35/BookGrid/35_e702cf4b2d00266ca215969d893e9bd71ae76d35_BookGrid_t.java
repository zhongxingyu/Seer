 package edu.weblibrary.client;
 
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridField;
 
 import edu.weblibrary.client.data.BookDS;
 
 public class BookGrid extends ListGrid {
 	BookGrid() {
 		setDataSource(BookDS.getInstance());
 		
 		ListGridField idField              = new ListGridField("id",              "id");
 		ListGridField titleField           = new ListGridField("title",           "Название");
		ListGridField authorInitialsField  = new ListGridField("authorInitials",  "Автор");
 		ListGridField publishingField      = new ListGridField("publishing",      "Издательство");
 		ListGridField quantityField        = new ListGridField("quantity",        "Количество");
 		ListGridField arrivalDateField     = new ListGridField("arrivalDate",     "Дата поступления");
 		ListGridField priceField           = new ListGridField("price",           "цена");
 		ListGridField publicationYearField = new ListGridField("publicationYear", "Год издания");
 		ListGridField keywordsField        = new ListGridField("keywords",        "Ключевые слова");
 		
		idField.setHidden(true);
 		quantityField.setWidth(75);
 		arrivalDateField.setWidth(110);
 		priceField.setWidth(50);
 		publicationYearField.setWidth(80);
 		
 		keywordsField.setAutoFitWidth(Boolean.TRUE);
 		
		setFields(idField, titleField, authorInitialsField, publishingField,
 				 quantityField, arrivalDateField, priceField, publicationYearField,
 				 keywordsField);
 		
 		setAutoFetchData(true);
 		setCanEdit(false);
 		setEditByCell(false);
 		setEmptyCellValue("--");
 		setDataPageSize(80);		
 	}
 }

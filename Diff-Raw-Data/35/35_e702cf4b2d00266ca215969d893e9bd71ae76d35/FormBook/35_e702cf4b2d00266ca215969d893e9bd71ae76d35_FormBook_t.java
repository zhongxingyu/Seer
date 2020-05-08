 package edu.weblibrary.client;
 
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.form.fields.DateItem;
 import com.smartgwt.client.widgets.form.fields.FloatItem;
 import com.smartgwt.client.widgets.form.fields.IntegerItem;
 import com.smartgwt.client.widgets.form.fields.SelectItem;
 import com.smartgwt.client.widgets.form.fields.SpinnerItem;
 import com.smartgwt.client.widgets.form.fields.TextAreaItem;
 import com.smartgwt.client.widgets.form.fields.TextItem;
 import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
 import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 
 import edu.weblibrary.client.data.AuthorDS;
 import edu.weblibrary.client.data.BookDS;
 
 public class FormBook extends DynamicForm {
 	public FormBook() {
 		setDataSource(BookDS.getInstance());
 		
 		IntegerItem bookId = new IntegerItem("id");		
 		final IntegerItem authorId = new IntegerItem("authorId");
 		authorId.setVisible(false);
 		
		SelectItem authorSelectItem = new SelectItem("authorInitials", "Автор");
 		authorSelectItem.setFetchMissingValues(false);
 		
 		ListGridField authorNameField = new ListGridField("name", "Имя");
 		ListGridField authorSurnameField = new ListGridField("surname", "Фамилия");
 		ListGridField authorPatronymicField = new ListGridField("patronymic", "Отчество");
 		ListGridField authorBirthYearField = new ListGridField("birthYear", "Год рождения");
 		
 		authorSelectItem.setValueField("surname");
 		authorSelectItem.setDisplayField("surname");
 		authorSelectItem.setPickListFields(authorNameField, authorSurnameField, authorPatronymicField,
 				authorBirthYearField);
 
 		authorSelectItem.addChangedHandler(new ChangedHandler() {
 			public void onChanged(ChangedEvent event) {
 				// TODO Auto-generated method stub
 				SelectItem item = (SelectItem) event.getItem();
 				ListGridRecord record = item.getSelectedRecord();			
 				
 				authorId.setValue(record.getAttribute("id"));
 				
 				String surname = record.getAttribute("surname");
 				String name = record.getAttribute("name");
              /*	String patronymic = record.getAttribute("patronymic");
 				String birthYear = record.getAttribute("birthYear"); */	
 				
 				StringBuilder sb = new StringBuilder();
 				sb.append(name).append(" ").append(surname);
 
 				item.setValue(sb.toString());				
 			}
 		});
 
 		authorSelectItem.setOptionDataSource(AuthorDS.getInstance());
 		authorSelectItem.setPickListWidth(300);
 		
 		DateItem bookArrivalDate = new DateItem("arrivalDate", "Дата поступления");
 		bookArrivalDate.setWrapTitle(false);
 		
 		TextAreaItem bookKeywords = new TextAreaItem("keywords", "Ключевые слова");
 		SpinnerItem bookQuantity = new SpinnerItem("quantity","Количество книг");
 		bookQuantity.setMin(0);
 		
 		FloatItem bookPrice = new FloatItem("price", "Цена");
 		IntegerItem bookPublicationYear = new IntegerItem("publicationYear", "Год издания");
 		TextItem bookPublishing = new TextItem("publishing", "Издательство");
 		TextItem bookTitle = new TextItem("title", "Название");
 		
 		setFields(bookId, bookTitle, authorId, authorSelectItem, bookArrivalDate, bookQuantity, bookPrice,
 				bookPublicationYear, bookPublishing, bookKeywords);
 		
 		bookId.setVisible(false);
 	}
 }

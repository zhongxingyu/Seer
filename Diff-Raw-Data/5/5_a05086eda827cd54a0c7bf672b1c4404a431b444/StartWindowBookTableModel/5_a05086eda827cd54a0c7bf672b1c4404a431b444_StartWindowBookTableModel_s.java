 package tablemodel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 
 import domain.Book;
 import domain.Library;
 
 //stupid comment
 
 public class StartWindowBookTableModel extends AbstractTableModel {
 	
 		Library library;
 		List<Book> list;
 		
 	
 		public StartWindowBookTableModel(Library library){
 			
 			this.library = library;
			this.list = library.getBooks();
			
 			
 		}
 	
	
 
 	    private String[] columnNames = {"Verfügbar", "Titel", "Author", "Verlag"};
 	    
 	    
 	    public int getColumnCount() {
 	        return columnNames.length;
 	    }
 
 	    public int getRowCount() {
 	        return list.size();
 	    }
 	    
 	    @Override
 	    public Object getValueAt(int rowIndex, int columnIndex) {
 	    
 	        Book column = list.get(rowIndex);
 	        
 	        switch (columnIndex){
 	            case 0 : 
 	                return library.getNoOfAvailableCopiesOfBook(column);
 	                            
 	            case 1:
 	                return column.getName();
 	                            
 	            case 2:
 	                return column.getAuthor();
 	                              
 	            case 3:
 	                return column.getPublisher();
 	                          
 	            default:
 	                throw new UnsupportedOperationException("Da ist wohl was Schiefgelaufen beim laden der Daten in die Tabelle");
 	                
 	        }
 	    }    
 
 	    public String getColumnName(int col) {
 	        return columnNames[col];
 	    }
 
 }

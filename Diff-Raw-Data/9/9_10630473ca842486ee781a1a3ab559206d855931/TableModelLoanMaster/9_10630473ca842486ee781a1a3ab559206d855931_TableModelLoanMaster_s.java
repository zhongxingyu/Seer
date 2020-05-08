 package tablemodel;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.table.AbstractTableModel;
 
 import domain.Book;
 import domain.Copy;
 import domain.Library;
 import domain.Loan;
 
 public class TableModelLoanMaster extends AbstractTableModel implements Observer{
 
 	Library library;
	List<Loan> loans;
 	String[] headers;
	List<Copy> copies;
 
 	public TableModelLoanMaster(Library library, String[] headers) {
 		this.library = library;
 		this.headers = headers;
		this.copies = library.getCopies();
 		addOnlyLent(library.getLoans());
 		this.library.addObserver(this);
 	}
 
 	private void addOnlyLent(List<Loan> loans)
 	{
		this.loans = new ArrayList<Loan>();
 		List<Loan> lent = loans;
 		for(Loan l : lent){
 			if(l.isLent()){
 				this.loans.add(l);
 			}
 		}
 	}
 	
 	@Override
 	public Class<?> getColumnClass(int columnIndex)
 	{
 		switch (columnIndex) {
 		case 0:
 			return String.class;
 		case 1:
 			return Long.class;
 		case 2:
 			return String.class;
 		case 3:
 			return String.class;
 		case 4:
 			return String.class;
 		default:
 			return null;
 		}
 	}
 
 	public Object getValueAt(int row, int colum) {
 		Loan loan = loans.get(row);
 
 		switch (colum) {
 		case 0:
 			return getLoanStatus(loan);
 		case 1:
 			return loan.getCopy().getInventoryNumber();
 		case 2:
 			return loan.getCopy().getTitle().getName();
 		case 3:
 				if (!loan.isOverdue()) {
 					return loan.getDueDateString() + " (Noch " + loan.getDaysTilDue() + " Tage)";
 				} else {
 					return loan.getDueDateString() + " (Fllig!)";
 				}
 		case 4:
 			return loan.getCustomer().getName();
 		default:
 			return null;
 		}
 	}
 
 	public Loan getLoanAtRow(int row) {
 		return loans.get(row);
 	}
 
 	@Override
 	public int getColumnCount() {
 		return headers.length;
 	}
 
 	@Override
 	public String getColumnName(int column) {
 		// TODO Auto-generated method stub
 		return headers[column];
 	}
 
 	@Override
 	public int getRowCount() {
 		return loans.size();
 	}
 
 	private String getLoanStatus(Loan l) {
 		if (l.isOverdue()) {
 			return "\u26A0 Fllig";
 		} else {
 			return "\u2713 Ok";
 		}
 	}
 	
 	@Override
 	public void update(Observable o, Object arg)
 	{
 		addOnlyLent(library.getLoans());
 		this.fireTableDataChanged();
 	}
 
 }

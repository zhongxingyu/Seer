 package at.epu.DataAccessLayer.DataModels.MockModels;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import at.epu.DataAccessLayer.DataModels.BankAccountDataModel;
 /*
  * ID(PK) | Buchungstext |
  * | Betrag | Umsatzsteuer | Buchungsdatum | Kategorie(FK)
  */
 public class MockBankAccountDataModel extends BankAccountDataModel {
 	private static final long serialVersionUID = -1513528315959701530L;
 	
 	public MockBankAccountDataModel() {
 		Object [][] data_ = {
			{new Integer(1), "Eingangsrechnung ID null", "Ausgangsrechnung ID 1",  new Double(34000.00+100050.00),new Double(26810.00), 
 			 new SimpleDateFormat("dd.MM.yyyy").format(new Date()), "Einnahme"},
			{new Integer(2), "Eingangsrechnung ID null", "Ausgangsrechnung ID 2", new Double(1252.00+1003.00),new Double(45100.00), 
 			 new SimpleDateFormat("dd.MM.yyyy").format(new Date()), "Einnahme"}
 		};
 
 		data = data_;
 	}
 }

 package com.immanent.tests;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 import com.immanent.models.ContactSearchModel;
import com.immanent.models.dao.ContactDetail;
 
 public class ContactDetailsModel_SaveContactsTester {
 	
 	public ArrayList<ContactDetail> dataSet() throws Exception {
 
 		ArrayList<ContactDetail> inputs = new ArrayList<ContactDetail>();
 		ContactDetail contact1 = new ContactDetail("akilai@localhost:3000", "Sanka", "Darshan", "Sanka@localhost:3000", "mathara", "dfda", "dfda", "dfda");
 		ContactDetail contact2 = new ContactDetail("akilai@localhost:3000", "Akila", "Iroshan", "akilai@localhost:3000", "galle", "addfa", "adfa", "dfda");
 		inputs.add(contact1);
 		inputs.add(contact2);
 		
 		return inputs;
 	}
 	
 	@Test
 	public void saveContacts_Test01() throws Exception {
 		ContactSearchModel cms = new ContactSearchModel();
 		boolean result = cms.saveNewContacts(dataSet());
 		assertThat(result, is(true));
 	}
 	
 	@Test
 	public void saveContacts_Test02() throws Exception {
 		ContactSearchModel cms = new ContactSearchModel();
 		boolean result = cms.saveNewContacts(null);
 		assertThat(result, is(false));
 	}
 	  
 	  
 }

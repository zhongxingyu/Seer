 package com.immanent.tests;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.ArrayList;
 
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.immanent.models.ContactSearchModel;
import com.immanent.models.dao.ContactDetail;
 
 public class ContactDetailsModel_SearchContactTester {
 	
 	  @Before
 	  public void importDataSet() throws Exception {
 	      IDataSet dataSet = readDataSet();
 	      new TestDBSetupHelper().cleanlyInsert(dataSet);
 	  }
 
 	  private IDataSet readDataSet() throws Exception {
 	      return new FlatXmlDataSetBuilder().build(this.getClass().getResource("search_contacts_input.xml"));
 	  }
 
 	  @Test
 	  public void searchContactByFirstName_Test01() throws Exception {
 	      ContactSearchModel cms = new ContactSearchModel();
 	      ArrayList<ContactDetail> results = cms.searchContacts("s", "", "", "");
 	      assertThat(results.get(0).getFirstName(), is("Sanka"));
 	  }
 	  
 	  @Test
 	  public void searchContactByFirstName_Test02() throws Exception {
 	      ContactSearchModel cms = new ContactSearchModel();
 	      ArrayList<ContactDetail> results = cms.searchContacts("a", "", "", "");
 	      cms.saveNewContacts(results);
 	      assertThat(results.get(0).getFirstName(), is("Akila"));
 	  }
 }

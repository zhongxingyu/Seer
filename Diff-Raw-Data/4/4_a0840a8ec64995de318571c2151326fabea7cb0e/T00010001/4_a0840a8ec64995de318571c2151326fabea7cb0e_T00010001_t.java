 package app.test;
 
 import common.db.JGDBConnection;
 import common.db.vo.JGDBQuery;
 import common.db.xml.JGDBXMLQueryManager;
 import common.db.xml.data.JGDBXMLQuery;
 import common.db.xml.data.JGDBXMLQuerySet;
 import common.main.JGAction;
 import common.main.data.xml.JGXMLDataSet;
 import common.main.exception.JGException;
 import common.util.JGEncryptionUtil;
 
 public class T00010001 extends JGAction {
 
 	public void testAction() throws JGException{
 		/*JGDBConnection dbConnection_ = getDBConnection();
 		
 		JGXMLDataSet testDataset_ = new JGXMLDataSet();
 		
 		testDataset_.addColumn("COL1").setKey(true);
 		testDataset_.addColumn("COL2");
 		testDataset_.addColumn("COL3");
 		testDataset_.addColumn("COL4");
 		
 		testDataset_.addRow();
 		
 		testDataset_.setColumnValue("COL1", 0, "tes");
 		testDataset_.setColumnValue("COL2", 0, "3233");
 		testDataset_.setColumnValue("COL3", 0, Integer.valueOf(11));
 		testDataset_.setColumnValue("COL4", 0, Float.valueOf(0.3f));
 		
 		testDataset_.apply();
 		testDataset_.setRowStatus(0, JGXMLDataSet.JGXML_ROWSTATUS_UPDATE);
 		
 		dbConnection_.executeUpdate(testDataset_, "test_TABLE");
 		dbConnection_.commit();*/
 		
 		
 		JGXMLDataSet testDataset_ = new JGXMLDataSet();
 		
 		testDataset_.addColumn("COL1").setKey(true);
 		testDataset_.addColumn("COL2");
 		testDataset_.addColumn("COL3");
 		testDataset_.addColumn("COL4");
 		
 		testDataset_.addRow();
 		
 		testDataset_.setColumnValue("COL1", 0, "tes");
 		testDataset_.setColumnValue("COL2", 0, "3233");
 		testDataset_.setColumnValue("COL3", 0, Integer.valueOf(11));
 		testDataset_.setColumnValue("COL4", 0, Float.valueOf(0.3f));
 		
 		//load DBXMLQueryManager
 		//getDBConnection().executeQuery("SELECT * FROM TABS");
 		JGDBXMLQuery queryItem_ = JGDBXMLQueryManager.sharedManager().getQuery("app/test/00001","testUpdate");
 		JGDBQuery query_ = queryItem_.createQuery(testDataset_);
 	}
 }

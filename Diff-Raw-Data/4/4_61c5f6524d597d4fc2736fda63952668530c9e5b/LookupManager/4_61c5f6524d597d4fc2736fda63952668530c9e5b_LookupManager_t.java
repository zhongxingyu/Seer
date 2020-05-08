 /*
  * Created on Nov 12, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.nih.nci.nautilus.lookup;
 
 import gov.nih.nci.nautilus.data.CytobandPosition;
 import gov.nih.nci.nautilus.data.DifferentialExpressionSfact;
 import gov.nih.nci.nautilus.data.ExpPlatformDim;
 import gov.nih.nci.nautilus.data.PatientData;
 
 import java.util.Collection;
 import java.util.Iterator;
 
import org.apache.log4j.Logger;
 import org.apache.ojb.broker.PersistenceBroker;
 import org.apache.ojb.broker.PersistenceBrokerFactory;
 import org.apache.ojb.broker.query.Criteria;
 import org.apache.ojb.broker.query.Query;
 import org.apache.ojb.broker.query.QueryFactory;
 
 /**
  * @author Himanso
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class LookupManager{
 	private static PatientDataLookup[] patientData;
 	private static CytobandLookup[] cytobands;
 	private static Lookup[] pathways;
 	private static ExpPlatformLookup[] expPlatforms;
 	private static PersistenceBroker broker;
 	
 	private  static Collection executeQuery(Class bean)throws Exception{
 			   Collection resultsetObjs = null;
 	           broker = PersistenceBrokerFactory.defaultPersistenceBroker();
 	           resultsetObjs = createQuery(bean);
 	           broker.close();
 	           return resultsetObjs;
 	       
 	}
 	private static Collection createQuery(Class bean) throws Exception{
 			Criteria crit = new Criteria();
 			Collection resultsetObjs = null;
 	        Query exprQuery = QueryFactory.newQuery(bean, crit,true);
 	        resultsetObjs = broker.getCollectionByQuery(exprQuery);
 			System.out.println("Got " + resultsetObjs.size() + " resultsetObjs objects.");
      
 		    return resultsetObjs;
 	}
 
 	/**
 	 * @return Returns the cytobands.
 	 */
 	public static CytobandLookup[] getCytobands() throws Exception{
 		if(cytobands == null){
 			cytobands = (CytobandLookup[]) executeQuery(CytobandPosition.class).toArray();
 		}
 		return cytobands;
 	}
 	/**
 	 * @return Returns the pathways.
 	 */
 	public Lookup[] getPathways() {
 		return pathways;
 	}
 	/**
 	 * @return Returns the patientData.
 	 * @throws Exception
 	 */
 	public static PatientDataLookup[] getPatientData() throws Exception {
 		if(patientData == null){
			patientData = (PatientDataLookup[])(executeQuery(PatientData.class).toArray(new PatientDataLookup[1]));
 		}
 		return patientData;
 	}
 	/**
 	 * @return Returns the expPlatforms.
 	 * @throws Exception
 	 */
 	public static ExpPlatformLookup[] getExpPlatforms() throws Exception {
 		if(expPlatforms == null){
 			expPlatforms = (ExpPlatformLookup[]) executeQuery(ExpPlatformDim.class).toArray();
 		}
 		return expPlatforms;
 	}
 }

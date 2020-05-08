 /*
  * Created on Jun 17, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.domain.Publication;
 import gov.nih.nci.camod.domain.PublicationStatus;
 import gov.nih.nci.camod.domain.SexDistribution;
 import gov.nih.nci.camod.service.PublicationManager;
 import gov.nih.nci.common.persistence.Persist;
 import gov.nih.nci.common.persistence.Search;
 import gov.nih.nci.common.persistence.exception.PersistenceException;
 import gov.nih.nci.common.persistence.hibernate.eqbe.Evaluation;
 import gov.nih.nci.common.persistence.hibernate.eqbe.Evaluator;
 
 import java.util.List;
 
 /**
  * @author rajputs
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class PublicationManagerImpl extends BaseManager implements PublicationManager {
 	
 	public List getAll() {		
 		List publications = null;
 		
 		try {
 			publications = Search.query(Publication.class);
 		} catch (Exception e) {
 			System.out.println("Exception in PublicationManagerImpl.getAll");
 			e.printStackTrace();
 		}
 		
 		return publications;
 	}
 	
 	public Publication get(String id) {
 		Publication publication = null;
 		
 		try {
 			publication = (Publication) Search.queryById(Publication.class, new Long(id));
 		} catch (PersistenceException pe) {
 			System.out.println("PersistenceException in PublicationManagerImpl.get");
 			pe.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("Exception in PublicationManagerImpl.get");
 			e.printStackTrace();
 		}
 		
 		return publication;
     }
 
     public void save(Publication publication, PublicationStatus publicationStatus ) {    	
     	try {
     		
             // See if there's already a sex distribution that exists, otherwise
             // save a new one
             PublicationStatus thePublication = PublicationManagerSingleton.instance().getByName( publicationStatus.getName() );
             
             if (thePublication != null) {
             	publicationStatus = thePublication;
             } else {
             	PublicationManagerSingleton.instance().savePublicationStatus( publicationStatus );
             }
             
             publication.setPublicationStatus( publicationStatus );
             
 			Persist.save(publication);			
 		} catch (PersistenceException pe) {
 			System.out.println("PersistenceException in PublicationManagerImpl.save");
 			pe.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("Exception in PublicationManagerImpl.save");
 			e.printStackTrace();
 		}
     }
 
     public PublicationStatus getByName(String inName) {
 
         PublicationStatus pubStatus = null;
 
         try {
             // The following two objects are needed for eQBE.
             PublicationStatus thePubStatus = new PublicationStatus();
             thePubStatus.setName( inName  );
 
             // Apply evaluators to object properties
             Evaluation theEvaluation = new Evaluation();
             theEvaluation.addEvaluator( "publicationStatus.name", Evaluator.EQUAL  );
 
             List theList = Search.query( thePubStatus, theEvaluation );
 
             if ( theList != null && theList.size() > 0 ) {
            	thePubStatus = ( PublicationStatus ) theList.get(0);
             }
                
         } catch (PersistenceException pe) {
             System.out.println("PersistenceException in PersonManagerImpl.getByType");
             pe.printStackTrace();
         } catch (Exception e) {
             System.out.println("Exception in PersonManagerImpl.getByType");
             e.printStackTrace();
         }
         
         return pubStatus;
     }
     
     public void savePublicationStatus(PublicationStatus publicationStatus) {    	
     	try {
 			Persist.save( publicationStatus );			
 		} catch (PersistenceException pe) {
 			System.out.println("PersistenceException in PublicationManagerImpl.save");
 			pe.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("Exception in PublicationManagerImpl.save");
 			e.printStackTrace();
 		}
     }
     
     public void remove(String id) {    	
     	try {
 			Persist.deleteById(Publication.class, new Long(id));
 		} catch (PersistenceException pe) {
 			System.out.println("PersistenceException in PublicationManagerImpl.remove");
 			pe.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("Exception in PublicationManagerImpl.remove");
 			e.printStackTrace();
 		}
     }
 }

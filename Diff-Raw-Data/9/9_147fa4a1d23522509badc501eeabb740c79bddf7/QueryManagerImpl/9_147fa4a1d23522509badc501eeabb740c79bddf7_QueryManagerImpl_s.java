 /**
  * @author dgeorge
  * 
 * $Id: QueryManagerImpl.java,v 1.32 2005-11-21 18:38:31 georgeda Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.31  2005/11/17 22:31:02  georgeda
  * Defect #131, upper case the modelDescriptor when doing a keyword search
  *
  * Revision 1.30  2005/11/17 20:22:09  georgeda
  * Defect #158.  Fixed issue w/ author name being returned for first matching pmid instead of pub. associated with animal model.
  *
  * Revision 1.29  2005/11/17 19:40:02  georgeda
  * Defect #48.  Now searches for all CI's if no specific CI is selected
  *
  * Revision 1.28  2005/11/16 21:40:30  georgeda
  * Defect #46, added code to handle CI's correctly.
  *
  * Revision 1.27  2005/11/14 16:52:37  georgeda
  * Fixed admin problem
  *
  * Revision 1.26  2005/11/14 15:14:41  schroedn
  * Defect #18
  * Changed the order the Stage 2 - Dose Reponse for is sorted, now Strain / Dosage is in order. Problem was the Dosage is stored iin Treatment as a VARCHAR instead of an integer.
  *
  * Revision 1.25  2005/11/14 14:19:13  georgeda
  * Cleanup
  *
  * Revision 1.24  2005/11/10 22:07:36  georgeda
  * Fixed part of bug #21
  *
  * Revision 1.23  2005/11/10 17:08:58  schroedn
  * Defect #12 fix schroedln (11/9/05)
  * Added the administrative site to the, In Vivo Screening Data Summary. Edited SQL query getInvivoResults
  *
  * Revision 1.22  2005/11/03 13:58:40  georgeda
  * Added function to get an InvivoResults for a specific NSC number
  *
  * Revision 1.21  2005/10/28 20:16:21  georgeda
  * Added space
  *
  * Revision 1.20  2005/10/27 18:13:48  guruswas
  * Show all publications in the publications display page.
  *
  * Revision 1.19  2005/10/24 19:36:57  georgeda
  * Changed searching to case insensitive
  *
  * Revision 1.18  2005/10/20 19:28:58  georgeda
  * Added TOC functionality
  *
  * Revision 1.17  2005/10/18 16:24:31  georgeda
  * Added getModelsByUser
  *
  * Revision 1.16  2005/10/17 13:12:47  georgeda
  * Added new method
  *
  * Revision 1.15  2005/10/12 17:24:23  georgeda
  * Get species only from approved models
  *
  * Revision 1.14  2005/10/11 18:15:10  georgeda
  * More comment changes
  *
  * Revision 1.13  2005/10/10 14:09:41  georgeda
  * Changes for comment curation and performance improvement
  *
  * Revision 1.12  2005/10/05 20:27:59  guruswas
  * implementation of drug screening search page
  *
  * Revision 1.11  2005/10/05 15:23:34  georgeda
  * Changed the theraputic approaches query to return all models w/ TA's if the search string passed in was blank
  *
  * Revision 1.10  2005/10/05 13:04:47  georgeda
  * Completed advanced search
  *
  * Revision 1.9  2005/10/04 20:19:43  georgeda
  * Updates from search changes
  *
  * Revision 1.6  2005/09/27 16:46:59  georgeda
  * Added environmental factor dropdown query
  *
  * Revision 1.5  2005/09/26 14:04:14  georgeda
  * Use HQL instead of SQL
  *
  * Revision 1.4  2005/09/16 19:30:04  guruswas
  * Display invivo data (from DTP) in the therapuetic approaches page
  *
  * Revision 1.3  2005/09/16 15:52:57  georgeda
  * Changes due to manager re-write
  *
  * 
  */
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.*;
 import gov.nih.nci.camod.util.DrugScreenResult;
 import gov.nih.nci.camod.webapp.form.SearchData;
 import gov.nih.nci.common.persistence.Search;
 import gov.nih.nci.common.persistence.exception.PersistenceException;
 import gov.nih.nci.common.persistence.hibernate.HQLParameter;
 import gov.nih.nci.common.persistence.hibernate.HibernateUtil;
 
 import java.sql.ResultSet;
 import java.util.*;
 
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 
 /**
  * Implementation of a wrapper around the HQL/JDBC interface. Used for more
  * complex instances where QBE does not have sufficient power.
  */
 public class QueryManagerImpl extends BaseManager {
 
 	/**
 	 * Return the list of environmental factor names
 	 * 
 	 * @param inType
 	 *            the type of environmental factor
 	 * 
 	 * @return a sorted list of unique environmental factors
 	 * @throws PersistenceException
 	 */
 	public List getEnvironmentalFactors(String inType) throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getAdministrativeRoutes");
 
 		// Format the query
 		HQLParameter[] theParams = new HQLParameter[1];
 		theParams[0] = new HQLParameter();
 		theParams[0].setName("type");
 		theParams[0].setValue(inType);
 		theParams[0].setType(Hibernate.STRING);
 
 		String theHQLQuery = "select distinct ef.name from EnvironmentalFactor as ef where ef.type = :type and ef.name is not null order by ef.name asc ";
 
 		List theList = Search.query(theHQLQuery, theParams);
 
 		log.debug("Found matching items: " + theList.size());
 
 		log.trace("Exiting QueryManagerImpl.getAdministrativeRoutes");
 		return theList;
 	}
 
 	/**
 	 * Return the list of environmental factor names
 	 * 
 	 * @param inType
 	 *            the type of environmental factor
 	 * 
 	 * @return a sorted list of unique environmental factors
 	 * @throws PersistenceException
 	 */
 	public List getQueryOnlyEnvironmentalFactors(String inType) throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getQueryOnlyEnvironmentalFactors");
 		ResultSet theResultSet = null;
 		List theEnvFactors = new ArrayList();
         
 		try {
 
 			// Format the query
 			String theSQLQuery = "SELECT distinct ef.name "
 					+ "FROM env_factor ef "
 					+ "WHERE ef.type = ? "
 					+ "  AND ef.name IS NOT null "
 					+ "  AND ef.env_factor_id IN (SELECT t.env_factor_id "
 					+ "     FROM therapy t, animal_model_therapy at, abs_cancer_model am WHERE t.therapeutic_experiment = 0 "
 					+ "       AND t.therapy_id = at.therapy_id "
 					+ "       AND am.abs_cancer_model_id = at.abs_cancer_model_id AND am.state = 'Edited-approved') ORDER BY ef.name asc ";
 
 			Object[] theParams = new Object[1];
 			theParams[0] = inType;
 			theResultSet = Search.query(theSQLQuery, theParams);
 
 			while (theResultSet.next()) {
 				theEnvFactors.add(theResultSet.getString(1));
 			}
             
             // Add any uncontrolled vocabs
             theSQLQuery = "SELECT distinct ef.name_unctrl_vocab "
                     + "FROM env_factor ef "
                     + "WHERE ef.type = ? "
                     + "  AND ef.name_unctrl_vocab IS NOT null "
                     + "  AND ef.env_factor_id IN (SELECT t.env_factor_id "
                     + "     FROM therapy t, animal_model_therapy at, abs_cancer_model am WHERE t.therapeutic_experiment = 0 "
                     + "       AND t.therapy_id = at.therapy_id "
                     + "       AND am.abs_cancer_model_id = at.abs_cancer_model_id AND am.state = 'Edited-approved') ORDER BY ef.name_unctrl_vocab asc ";
 
             theResultSet = Search.query(theSQLQuery, theParams);
 
             while (theResultSet.next()) {
                 String theEnvFactor = theResultSet.getString(1);
                 if (!theEnvFactors.contains(theEnvFactor)) {
                     theEnvFactors.add(theResultSet.getString(1));
                 }
             }
             
 
             Collections.sort(theEnvFactors);
             
 			log.trace("Exiting QueryManagerImpl.getQueryOnlyEnvironmentalFactors");
 		} catch (Exception e) {
 			log.error("Exception in getQueryOnlyEnvironmentalFactors", e);
 			throw new PersistenceException("Exception in getQueryOnlyEnvironmentalFactors: " + e);
 		} finally {
 			if (theResultSet != null) {
 				try {
 					theResultSet.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return theEnvFactors;
 	}
 
 	/**
 	 * Return the list of environmental factor names which were used to induce a
 	 * mutation
 	 * 
 	 * @return a sorted list of unique environmental factors
 	 * @throws PersistenceException
 	 */
 	public List getQueryOnlyInducedMutationAgents() throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getQueryOnlyInducedMutationAgents");
 		ResultSet theResultSet = null;
 		List theAgents = new ArrayList();
 		try {
 
 			// Format the query
 			String theSQLQuery = "SELECT distinct ef.name FROM env_factor ef, env_fac_ind_mutation im "
 					+ "WHERE ef.name IS NOT null AND ef.env_factor_id = im.env_factor_id";
 
 			Object[] theParams = new Object[0];
 			theResultSet = Search.query(theSQLQuery, theParams);
 
 			while (theResultSet.next()) {
 				theAgents.add(theResultSet.getString(1));
 			}
 
 			log.trace("Exiting QueryManagerImpl.getQueryOnlyInducedMutationAgents");
 		} catch (Exception e) {
 			log.error("Exception in getQueryOnlyInducedMutationAgents", e);
 			throw new PersistenceException("Exception in getQueryOnlyInducedMutationAgents: " + e);
 		} finally {
 			if (theResultSet != null) {
 				try {
 					theResultSet.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return theAgents;
 	}
 
 	/**
 	 * Return the list of species associated with animal models
 	 * 
 	 * @return a sorted list of unique species
 	 * 
 	 * @throws PersistenceException
 	 */
 	public List getQueryOnlySpecies() throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getQueryOnlyEnvironmentalFactors");
 
 		// Format the query
 		String theSQLString = "SELECT distinct scientific_name FROM taxon WHERE scientific_name IS NOT NULL "
 				+ "    AND taxon_id IN (select distinct taxon_id from abs_cancer_model where state = 'Edited-approved')";
 
 		ResultSet theResultSet = null;
 
 		List theSpeciesList = new ArrayList();
 
 		try {
 
 			log.info("getModelsIds - SQL: " + theSQLString);
 
 			Object[] params = new Object[0];
 			theResultSet = Search.query(theSQLString, params);
 
 			while (theResultSet.next()) {
 				theSpeciesList.add(theResultSet.getString(1));
 			}
 
 		} catch (Exception e) {
 			log.error("Exception in getQueryOnlySpecies", e);
 			throw new PersistenceException("Exception in getQueryOnlySpecies: " + e);
 		} finally {
 			if (theResultSet != null) {
 				try {
 					theResultSet.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return theSpeciesList;
 	}
 
 	/**
 	 * Return the list of PI's sorted by last name
 	 * 
 	 * @return a sorted list of People objects
 	 * @throws PersistenceException
 	 */
 	public List getPrincipalInvestigators() throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getPrincipalInvestigators");
 
 		// Format the query
 		HQLParameter[] theParams = new HQLParameter[0];
 		String theHQLQuery = "from Person where is_principal_investigator = 1 order by last_name asc";
 
 		List theList = Search.query(theHQLQuery, theParams);
 
 		log.debug("Found matching items: " + theList.size());
 
 		log.trace("Exiting QueryManagerImpl.getPrincipalInvestigators");
 		return theList;
 	}
 
 	/**
 	 * Return the list of PI's sorted by last name
 	 * 
 	 * @return a sorted list of People objects
 	 * @throws PersistenceException
 	 */
 	public List getPeopleByRole(String inRole) throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getPeopleByRole");
 
 		String theHQLQuery = "from Person where username is not null ";
 
 		if (!inRole.equals(Constants.Admin.Roles.ALL)) {
 			theHQLQuery += " AND party_id in (" + getPartyIdsForRole(inRole) + ") ";
 		}
 
 		// Complete the query
 		theHQLQuery += " order by last_name asc";
 
 		HQLParameter[] theParams = new HQLParameter[0];
 		List theList = Search.query(theHQLQuery, theParams);
 
 		log.debug("Found matching items: " + theList.size());
 
 		log.trace("Exiting QueryManagerImpl.getPeopleByRole");
 		return theList;
 	}
 
 	/**
 	 * Get the model id's for any model that has a histopathology with a parent
 	 * histopathology
 	 * 
 	 * @return a list of matching model ids
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getPartyIdsForRole(String inRole) throws PersistenceException {
 
 		String theSQLString = "SELECT distinct pr.party_id FROM party_role pr "
 				+ "WHERE pr.role_id IN (SELECT role_id FROM role r " + "     WHERE r.name = ?)";
 
 		Object[] theParams = new Object[1];
 		theParams[0] = inRole;
 		return getModelIds(theSQLString, theParams);
 
 	}
 
 	/**
 	 * Return the list of species associated with animal models
 	 * 
 	 * @return a sorted list of unique species
 	 * 
 	 * @throws PersistenceException
 	 */
 	public List getQueryOnlyPrincipalInvestigators() throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getQueryOnlyPrincipalInvestigators");
 
 		// Format the query
 		String theSQLString = "SELECT last_name, first_name "
 				+ "FROM party "
 				+ "WHERE is_principal_investigator = 1 "
 				+ "  AND first_name IS NOT NULL "
 				+ "  AND last_name IS NOT NULL "
 				+ "  AND party_id IN (SELECT DISTINCT principal_investigator_id FROM abs_cancer_model WHERE state = 'Edited-approved')"
 				+ "ORDER BY last_name ASC";
 
 		ResultSet theResultSet = null;
 
 		List thePIList = new ArrayList();
 
 		try {
 
 			log.info("getQueryOnlyPrincipalInvestigators - SQL: " + theSQLString);
 
 			Object[] params = new Object[0];
 			theResultSet = Search.query(theSQLString, params);
 
 			while (theResultSet.next()) {
 				String thePIEntry = theResultSet.getString(1) + ", " + theResultSet.getString(2);
 				thePIList.add(thePIEntry);
 			}
 
 		} catch (Exception e) {
 			log.error("Exception in getQueryOnlyPrincipalInvestigators", e);
 			throw new PersistenceException("Exception in getQueryOnlyPrincipalInvestigators: " + e);
 		} finally {
 			if (theResultSet != null) {
 				try {
 					theResultSet.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return thePIList;
 	}
 
 	/**
 	 * Return the latest log for an animal model
 	 * 
 	 * @param inModel
 	 *            the animal model to get the latest log for
 	 * 
 	 * @return the current log for an animal model
 	 * @throws PersistenceException
 	 */
 	public Log getCurrentLog(AnimalModel inModel) throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getCurrentLog");
 
 		HQLParameter[] theParams = new HQLParameter[1];
 		theParams[0] = new HQLParameter();
 		theParams[0].setName("abs_cancer_model_id");
 		theParams[0].setValue(inModel.getId());
 		theParams[0].setType(Hibernate.LONG);
 
 		String theHQLQuery = "from Log where abs_cancer_model_id = :abs_cancer_model_id order by timestamp desc";
 		log.debug("The HQL query: " + theHQLQuery);
 		List theLogs = Search.query(theHQLQuery, theParams);
 
 		Log theLog = null;
 		if (theLogs != null && theLogs.size() > 0) {
 			theLog = (Log) theLogs.get(0);
 			log.debug("Found a matching object: " + theLog.getId());
 		} else {
 			log.debug("No object found.");
 		}
 
 		log.trace("Exiting QueryManagerImpl.getCurrentLog");
 
 		return theLog;
 	}
 
 	/**
 	 * Return the latest log for an animal model/user combo
 	 * 
 	 * @param inModel
 	 *            the animal model to get the latest log for
 	 * @param inUser
 	 *            the user to get the latest log for
 	 * 
 	 * @return the current log for an animal model/user combination
 	 * @throws PersistenceException
 	 */
 	public Log getCurrentLogForUser(AnimalModel inModel, Person inUser) throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getCurrentLogForUser");
 
 		// Format the query
 		HQLParameter[] theParams = new HQLParameter[2];
 		theParams[0] = new HQLParameter();
 		theParams[0].setName("abs_cancer_model_id");
 		theParams[0].setValue(inModel.getId());
 		theParams[0].setType(Hibernate.LONG);
 		theParams[1] = new HQLParameter();
 		theParams[1].setName("party_id");
 		theParams[1].setValue(inUser.getId());
 		theParams[1].setType(Hibernate.LONG);
 
 		String theHQLQuery = "from Log where abs_cancer_model_id = :abs_cancer_model_id and party_id = :party_id "
 				+ "and comments_id is null order by timestamp desc";
 		log.debug("the HQL Query: " + theHQLQuery);
 		List theLogs = Search.query(theHQLQuery, theParams);
 
 		Log theLog = null;
 		if (theLogs != null && theLogs.size() > 0) {
 			theLog = (Log) theLogs.get(0);
 			log.debug("Found a matching object: " + theLog.getId());
 		} else {
 			log.debug("No object found.");
 		}
 
 		log.trace("Exiting QueryManagerImpl.getCurrentLogForUser");
 		return theLog;
 	}
 
 	/**
 	 * Return the latest log for a comment/user combo
 	 * 
 	 * @param inComments
 	 *            the comments to get the latest log for
 	 * @param inUser
 	 *            the user to get the latest log for
 	 * 
 	 * @return the current log for an comments/user combination
 	 * 
 	 * @throws PersistenceException
 	 */
 	public Log getCurrentLogForUser(Comments inComments, Person inUser) throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getCurrentLogForUser");
 
 		// Format the query
 		HQLParameter[] theParams = new HQLParameter[3];
 		theParams[0] = new HQLParameter();
 		theParams[0].setName("abs_cancer_model_id");
 		theParams[0].setValue(inComments.getCancerModel().getId());
 		theParams[0].setType(Hibernate.LONG);
 		theParams[1] = new HQLParameter();
 		theParams[1].setName("party_id");
 		theParams[1].setValue(inUser.getId());
 		theParams[1].setType(Hibernate.LONG);
 		theParams[2] = new HQLParameter();
 		theParams[2].setName("comments_id");
 		theParams[2].setValue(inComments.getId());
 		theParams[2].setType(Hibernate.LONG);
 
 		System.out.println("Comments id: " + inComments.getId());
 		System.out.println("Party id: " + inUser.getId());
 		System.out.println("CM id: " + inComments.getCancerModel().getId());
 
 		String theHQLQuery = "from Log where abs_cancer_model_id = :abs_cancer_model_id and party_id = :party_id "
 				+ "and comments_id = :comments_id order by timestamp desc";
 		log.debug("the HQL Query: " + theHQLQuery);
 		List theLogs = Search.query(theHQLQuery, theParams);
 
 		Log theLog = null;
 		if (theLogs != null && theLogs.size() > 0) {
 			theLog = (Log) theLogs.get(0);
 			log.debug("Found a matching object: " + theLog.getId());
 		} else {
 			log.debug("No object found.");
 		}
 
 		log.trace("Exiting QueryManagerImpl.getCurrentLogForUser");
 		return theLog;
 	}
 
 	/**
 	 * Return all of the comments associated with a person that match the state
 	 * passed in
 	 * 
 	 * @param inState
 	 *            the state of the comment
 	 * 
 	 * @param inPerson
 	 *            the person to match
 	 * 
 	 * @return a list of matching comments
 	 * 
 	 * @throws PersistenceException
 	 */
 	public List getCommentsBySection(String inSection, Person inPerson, AnimalModel inModel)
 			throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getCommentsBySectionForPerson");
 
 		// If no person, only get approved items
         // TODO: make the states a constant
 		String theStateHQL = "(c.state = 'Screened-approved'";
 		if (inPerson == null) {
 			theStateHQL += ") ";
 		} else {
 			theStateHQL += "or c.submitter = :party_id) ";
 		}
 
 		String theHQLQuery = "from Comments as c where "
 				+ theStateHQL
 				+ " and c.cancerModel in ("
 				+ "from AnimalModel as am where am.id = :abs_cancer_model_id) and c.modelSection in (from ModelSection where name = :name)";
 		log.debug("The HQL query: " + theHQLQuery);
 
 		Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
 		theQuery.setParameter("abs_cancer_model_id", inModel.getId());
 		theQuery.setParameter("name", inSection);
 
 		// Only query for party if the passed in party wasn't null
 		if (inPerson != null) {
 			theQuery.setParameter("party_id", inPerson.getId());
 		}
 		List theComments = theQuery.list();
 
 		if (theComments == null) {
 			theComments = new ArrayList();
 		}
 
 		log.trace("Exiting QueryManagerImpl.getCommentsByStateForPerson");
 
 		return theComments;
 	}
 
 	/**
 	 * Return all of the comments associated with a person that match the state
 	 * passed in
 	 * 
 	 * @param inState
 	 *            the state of the comment
 	 * 
 	 * @param inPerson
 	 *            the person to match
 	 * 
 	 * @return a list of matching comments
 	 * 
 	 * @throws PersistenceException
 	 */
 	public List getCommentsByStateForPerson(String inState, Person inPerson) throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getCommentsByStateForPerson");
 
 		String theHQLQuery = "from Comments as c where c.state = :state and c.id in (";
         Query theQuery = null; 
         if (inPerson == null) {
             theHQLQuery += "select l.comment from Log as l where l.type = :state)";
             theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
             theQuery.setParameter("state", inState);
         }
         else {
             theHQLQuery += "select l.comment from Log as l where l.submitter = :party_id and l.type = :state)";
             theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
             theQuery.setParameter("party_id", inPerson.getId());
             theQuery.setParameter("state", inState);
         }
         
 		log.debug("The HQL query: " + theHQLQuery);
 
 		List theComments = theQuery.list();
 
 		if (theComments == null) {
 			theComments = new ArrayList();
 		}
 
 		log.trace("Exiting QueryManagerImpl.getCommentsByStateForPerson");
 
 		return theComments;
 	}
 
 	/**
 	 * Return all of the models associated with a person that match the state
 	 * passed in
 	 * 
 	 * @param inState
 	 *            the state of the comment
 	 * 
 	 * @param inPerson
 	 *            the person to match
 	 * 
 	 * @return a list of matching models
 	 * 
 	 * @throws PersistenceException
 	 */
 	public List getModelsByStateForPerson(String inState, Person inPerson) throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getCurrentLog");
 
 		String theHQLQuery = "from AnimalModel as am where am.state = :state and am.id in (";
         Query theQuery = null; 
         
         if (inPerson == null) {
             theHQLQuery += "select l.cancerModel from Log as l where l.type = :state)";
             theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
             theQuery.setParameter("state", inState);
         }
         else {
             theHQLQuery += "select l.cancerModel from Log as l where l.submitter = :party_id and l.type = :state)";
             theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
             theQuery.setParameter("party_id", inPerson.getId());
             theQuery.setParameter("state", inState);
         }
         
 		log.debug("The HQL query: " + theHQLQuery);
 
 		List theComments = theQuery.list();
 
 		if (theComments == null) {
 			theComments = new ArrayList();
 		}
 		return theComments;
 	}
 
 	/**
 	 * Return all of the models associated which have the passed in user as
 	 * either the submitter _or_ the PI
 	 * 
 	 * @param inUsername
 	 * 
 	 * @return a list of matching models
 	 * 
 	 * @throws PersistenceException
 	 */
 	public List getModelsByUser(String inUsername) throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getModelsByUser");
 
 		String theHQLQuery = "from AnimalModel as am where "
 				+ " am.submitter in (from Person where username = :username) or"
 				+ " am.principalInvestigator in (from Person where username = :username) "
 				+ " order by model_descriptor";
 		log.debug("The HQL query: " + theHQLQuery);
 
 		Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
 		theQuery.setParameter("username", inUsername);
 
 		List theComments = theQuery.list();
 
 		if (theComments == null) {
 			theComments = new ArrayList();
 		}
 		return theComments;
 	}
 
 	/**
 	 * Return all of InvivoResult objects with a specific NSC number
 	 * 
 	 * @param inNSCNumber
 	 * 
 	 * @return a list of matching InvivoResult objects
 	 * 
 	 * @throws PersistenceException
 	 */
 	public List getInvivoResultCollectionByNSC(String inNSCNumber, String inModelId) throws PersistenceException {
 
 		log.trace("Entering QueryManagerImpl.getInvivoResultCollectionByNSC");
 
 		String theHQLQuery = "from InvivoResult as ir where " + " ir.id in ("
 				+ getInvivoIdsForNSCNumberAndModel(inNSCNumber, inModelId) + ")";
 		log.debug("The HQL query: " + theHQLQuery);
 
 		Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
 
 		List theResults = theQuery.list();
 
 		if (theResults == null) {
 			theResults = new ArrayList();
 		}
 		return theResults;
 	}
 
 	/**
 	 * Get the InvivoResult id's for for a given NSC number and model
 	 * 
 	 * @return a list of matching ids
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getInvivoIdsForNSCNumberAndModel(String inNSCNumber, String inModelId) throws PersistenceException {
 
 		String theSQLString = "SELECT distinct xeno_inv.invivo_result_id FROM xenograft_invivo_result xeno_inv "
 				+ "WHERE xeno_inv.invivo_result_id IN (SELECT ir.invivo_result_id FROM invivo_result ir, env_factor ef "
 				+ "     WHERE ir.agent_id = ef.env_factor_id and ef.nsc_number = ?) and xeno_inv.abs_cancer_model_id = ?";
 
 		Object[] theParams = new Object[2];
 		theParams[0] = inNSCNumber;
 		theParams[1] = inModelId;
 		return getModelIds(theSQLString, theParams);
 
 	}
 
 	/**
 	 * Get yeast screen result data (ave inibition etc.) for a given Agent
 	 * (drug)
 	 * 
 	 * @param agent
 	 *            refers to the compound that was used in the yeast experiment
 	 * @param stage
 	 *            is the stage of the experiment (0, 1, or 2)
 	 * 
 	 * @return the results
 	 * @throws PersistenceException
 	 */
 	public DrugScreenResult getYeastScreenResults(Agent agent, String stage, boolean useNscNumber) throws PersistenceException {
 		DrugScreenResult dsr = new DrugScreenResult();
 
 		ResultSet theResultSet = null;
 		try {
 
 			String theSQLString = "select tx.ethnicity_strain," + "\n" + "       t.dosage," + "\n"
 					+ "       sr.aveinh aveinh," + "\n" + "       sr.diffinh diffinh" + "\n"
 					+ "  from screening_Result sr," + "\n" + "       env_factor a," + "\n"
 					+ "       YST_MDL_SCRNING_RESULT ymsr," + "\n" + "       abs_cancer_model acm," + "\n"
 					+ "       treatment t," + "\n" + "       taxon tx" + "\n" + " where sr.agent_id = a.env_factor_id"
 					+ "\n" + "   and sr.screening_result_id = ymsr.screening_result_id" + "\n"
 					+ "   and sr.treatment_id = t.treatment_id" + "\n"
 					+ "   and ymsr.abs_cancer_model_id = acm.abs_cancer_model_id" + "\n"
 					+ "   and acm.taxon_id = tx.taxon_id" + "\n"
                     + "   and sr.stage = ?" + "\n";
             
             // We query for the agent id on the drug screen search pages and the nsc number on
             // the theraputic approaches pages
             Long theParam;
             if (useNscNumber == true) {
                 theSQLString += "   and a.nsc_number = ?" + "\n";
                 theParam = agent.getNscNumber();
             } else {
                 theSQLString += "   and a.env_factor_id = ?" + "\n";
                 theParam = agent.getId();
             }
                             
 			theSQLString += " order by tx.ethnicity_strain, to_number(t.dosage)";
             
 			log.info("getYeastScreenResults - SQL: " + theSQLString);
 
 			Object[] params = new Object[2];
 			params[0] = stage;
             params[1] = theParam;
 			theResultSet = Search.query(theSQLString, params);
 			while (theResultSet.next()) {
 				final String strain = theResultSet.getString(1);
 				final String dosage = theResultSet.getString(2);
 				final float aveinh = theResultSet.getFloat(3);
 				final float diffinh = theResultSet.getFloat(4);
 				dsr.addEntry(strain, dosage, aveinh, diffinh);
 			}
 			log.info("Got " + dsr.strainCount + " strains");
 		} catch (Exception e) {
 			log.error("Exception in getYeastScreenResults", e);
 			throw new PersistenceException("Exception in getYeastScreenResults: " + e);
 		} finally {
 			if (theResultSet != null) {
 				try {
 					theResultSet.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return dsr;
 	}
 
 	/**
 	 * Get the invivo (Xenograft) data (from DTP) for a given Agent.nscNumber
 	 * (drug)
 	 * 
 	 * @param agent
 	 *            refers to the compound that was used in the xenograft
 	 *            experiment
 	 * 
 	 * @return the results (list of abs_cancer_model_id, model_descriptor, and #
 	 *         of records
 	 * @throws PersistenceException
 	 */
 	public List getInvivoResults(Agent agent, boolean useNscNumber) throws PersistenceException {
 		List results = new ArrayList();
 		int cc = 0;
 		ResultSet theResultSet = null;
         log.info("Env factor_id=" + agent.getId());
 		try {
 
 			String theSQLString = 
 				      "select acm.abs_cancer_model_id," + "\n" 
 					+ "       acm.model_descriptor," + "\n"
 					+ "       tx.ethnicity_strain," + "\n"
 					+ "       acm.administrative_site," + "\n"
 					+ "       count(*)" + "\n" 					
 					+ "  from invivo_Result sr,"	+ "\n" 
 					+ "       env_factor a," + "\n" 
 					+ "       XENOGRAFT_INVIVO_RESULT ymsr," + "\n"
 					+ "       abs_cancer_model acm," + "\n" 
 					+ "       treatment t," + "\n" 
 					+ "       taxon tx" + "\n"
 					+ " where sr.agent_id = a.env_factor_id" + "\n"
 					+ "   and sr.invivo_result_id = ymsr.invivo_result_id" + "\n"
 					+ "   and sr.treatment_id = t.treatment_id" + "\n"
 					+ "   and ymsr.abs_cancer_model_id = acm.abs_cancer_model_id" + "\n"
 					+ "   and acm.taxon_id = tx.taxon_id" + "\n";
             
             Long theParam;
             if (useNscNumber == true) {
                 theSQLString += "   and a.nsc_number = ?" + "\n";
                 theParam = agent.getNscNumber();
             } else {
                 theSQLString += "   and a.env_factor_id = ?" + "\n";
                 theParam = agent.getId();
             }
             
             theSQLString += " group by acm.abs_cancer_model_id, acm.model_descriptor, tx.ethnicity_strain, acm.administrative_site" + "\n"
 					+ " order by 3, 2";
             
 			log.info("getInvivoResults - SQL: " + theSQLString);
 
 			Object[] params = new Object[1];
 			params[0] = theParam;
 			theResultSet = Search.query(theSQLString, params);
 			while (theResultSet.next()) {
 				String[] item = new String[5];
 				item[0] = theResultSet.getString(1); // the id
 				item[1] = theResultSet.getString(2); // model descriptor
 				item[2] = theResultSet.getString(3); // strain
 				item[3] = theResultSet.getString(4); // administrative site
 				item[4] = theResultSet.getString(5); // record count
 				results.add(item);
 				cc++;
 			}
 			log.info("Got " + cc + " xenograft models");
 		} catch (Exception e) {
 			log.error("Exception in getYeastScreenResults", e);
 			throw new PersistenceException("Exception in getYeastScreenResults: " + e);
 		} finally {
 			if (theResultSet != null) {
 				try {
 					theResultSet.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return results;
 	}
 
 	/**
 	 * Get the model id's for any model that has a histopathology associated
 	 * with a specific organ.
 	 * 
 	 * @param inOrgan
 	 *            the organ to search for
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIdsForHistopathologyOrgan(String inConceptCodes) throws PersistenceException {
 
 		String theConceptCodeList = "";
 
 		StringTokenizer theTokenizer = new StringTokenizer(inConceptCodes, ",");
 
 		while (theTokenizer.hasMoreElements()) {
 			theConceptCodeList += "'" + theTokenizer.nextToken() + "'";
 
 			// Only tack on a , if it's not the last element
 			if (theTokenizer.hasMoreElements()) {
 				theConceptCodeList += ",";
 			}
 		}
 
 		String theSQLString = "SELECT distinct ani_hist.abs_cancer_model_id FROM ani_mod_histopathology ani_hist "
 				+ "WHERE ani_hist.histopathology_id IN (SELECT h.histopathology_id "
 				+ "     FROM histopathology h, organ_histopathology oh, organ o "
 				+ "     WHERE h.histopathology_id = oh.histopathology_id AND oh.organ_id = o.organ_id "
 				+ "         AND o.concept_code IN (" + theConceptCodeList + "))";
 
 		Object[] theParams = new Object[0];
 		return getModelIds(theSQLString, theParams);
 
 	}
 
 	/**
 	 * Get the model id's for any model that has a histopathology with a parent
 	 * histopathology
 	 * 
 	 * @return a list of matching model ids
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIdsForHistoMetastasis() throws PersistenceException {
 
 		String theSQLString = "SELECT distinct ani_hist.abs_cancer_model_id FROM ani_mod_histopathology ani_hist "
 				+ "WHERE ani_hist.histopathology_id IN (SELECT h.parent_histopathology_id FROM histopathology h "
 				+ "     WHERE h.parent_histopathology_id IS NOT NULL)";
 
 		Object[] theParams = new Object[0];
 		return getModelIds(theSQLString, theParams);
 
 	}
 
 	/**
 	 * Get the model id's for any model that has a histopathology with a parent
 	 * histopathology
 	 * 
 	 * @return a list of matching model ids
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIdsForXenograft() throws PersistenceException {
 
 		String theSQLString = "SELECT distinct par_abs_can_model_id FROM abs_cancer_model ";
 
 		Object[] theParams = new Object[0];
 		return getModelIds(theSQLString, theParams);
 
 	}
 
 	/**
 	 * Get the model id's for any model that has associated microarray data
 	 * 
 	 * @return a list of matching model ids
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIdsForMicroArrayData() throws PersistenceException {
 
 		String theSQLString = "SELECT distinct abs_cancer_model_id FROM ani_mod_mic_array_data";
 
 		Object[] theParams = new Object[0];
 		return getModelIds(theSQLString, theParams);
 
 	}
 
 	/**
 	 * Get the model id's for any model that has a cellline w/ a matching name
 	 * 
 	 * @param inCellLineName
 	 *            the text to search for in the cell-line
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIdsForCellLine(String inCellLineName) throws PersistenceException {
 
 		String theSQLString = "SELECT distinct ani_cell.abs_cancer_model_id FROM ani_mod_cell_line ani_cell "
 				+ "WHERE ani_cell.cell_line_id IN (SELECT c.cell_line_id FROM cell_line c "
 				+ "     WHERE upper(c.name) LIKE ?)";
 
 		Object[] theParams = new Object[1];
 		theParams[0] = "%" + inCellLineName + "%";
 		return getModelIds(theSQLString, theParams);
 
 	}
 
 	/**
 	 * Get the model id's for any model that has a histopathology associated
 	 * with a specific organ.
 	 * 
 	 * @param inDisease
 	 *            the disease to search for
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIdsForTherapeuticApproach(String inTherapeuticApproach) throws PersistenceException {
 
 		String theSQLString = "SELECT distinct ani_ther.abs_cancer_model_id " + "FROM animal_model_therapy ani_ther "
 				+ "WHERE ani_ther.therapy_id IN (SELECT t.therapy_id FROM therapy t, env_factor e "
 				+ "     WHERE t.therapeutic_experiment = 1 AND t.env_factor_id = e.env_factor_id "
 				+ "         AND upper(e.name) like ?)";
 
 		String theSQLTheraputicApproach = "%";
 		if (inTherapeuticApproach != null && inTherapeuticApproach.trim().length() > 0) {
			theSQLTheraputicApproach = "%" + inTherapeuticApproach + "%";
 		}
 
 		Object[] theParams = new Object[1];
 		theParams[0] = theSQLTheraputicApproach;
 		return getModelIds(theSQLString, theParams);
 
 	}
 
 	/**
 	 * Get the model id's for any model that has a histopathology associated
 	 * with a specific organ.
 	 * 
 	 * @param inDisease
 	 *            the disease to search for
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIdsForHistopathologyDisease(String inConceptCodes) throws PersistenceException {
 
 		String theConceptCodeList = "";
 
 		StringTokenizer theTokenizer = new StringTokenizer(inConceptCodes, ",");
 
 		while (theTokenizer.hasMoreElements()) {
 			theConceptCodeList += "'" + theTokenizer.nextToken() + "'";
 
 			// Only tack on a , if it's not the last element
 			if (theTokenizer.hasMoreElements()) {
 				theConceptCodeList += ",";
 			}
 		}
 
 		String theSQLString = "SELECT distinct ani_hist.abs_cancer_model_id " + "FROM ani_mod_histopathology ani_hist "
 				+ "WHERE ani_hist.histopathology_id IN (SELECT h.histopathology_id "
 				+ "     FROM histopathology h, histopathology_disease hd, disease d "
 				+ "     WHERE h.histopathology_id = hd.histopathology_id "
 				+ "         AND hd.disease_id = d.disease_id AND d.concept_code IN (" + theConceptCodeList + "))";
 
 		Object[] theParams = new Object[0];
 		return getModelIds(theSQLString, theParams);
 
 	}
 
 	/**
 	 * Get the models with the associated engineered gene
 	 * 
 	 * @param inGeneName
 	 *            the name of the transgene or targeted modification
 	 * 
 	 * @param isEngineeredTransgene
 	 *            are we looking for a transgene?
 	 * 
 	 * @param isEngineeredTransgene
 	 *            are we looking for an induced mutation?
 	 * 
 	 * @param inGenomicSegDesignator
 	 *            the name of the genomic segment designator
 	 * 
 	 * @param inInducedMutationAgent
 	 *            the name of Agent which induced the mutation. Exact match.
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException
 	 * 
 	 */
 	private String getModelIdsForEngineeredGenes(String inGeneName, boolean isEngineeredTransgene,
 			boolean isTargetedModification, String inGenomicSegDesignator, String inInducedMutationAgent)
 			throws PersistenceException {
 
 		List theList = new ArrayList();
 
 		String theSQLString = "SELECT distinct ani_ge.abs_cancer_model_id "
 				+ "FROM ani_mod_engineered_gene ani_ge WHERE ";
 
 		String OR = " ";
 
 		if (isEngineeredTransgene == true && inGeneName.trim().length() > 0) {
 			theSQLString += OR + " ani_ge.engineered_gene_id IN (SELECT distinct engineered_gene_id "
 					+ " FROM engineered_gene WHERE upper(name) LIKE ? AND engineered_gene_type = 'T')";
 			OR = " OR ";
 			theList.add("%" + inGeneName.trim().toUpperCase() + "%");
 		}
 		if (isTargetedModification == true && inGeneName.trim().length() > 0) {
 			theSQLString += OR + " ani_ge.engineered_gene_id IN (SELECT distinct engineered_gene_id "
 					+ " FROM engineered_gene WHERE upper(name) LIKE ? AND engineered_gene_type = 'TM')";
 			OR = " OR ";
 			theList.add("%" + inGeneName.trim().toUpperCase() + "%");
 		}
 		if (inInducedMutationAgent != null && inInducedMutationAgent.trim().length() > 0) {
 			theSQLString += OR + " ani_ge.engineered_gene_id IN (SELECT distinct engineered_gene_id "
 					+ " FROM engineered_gene WHERE engineered_gene_id IN ("
 					+ " SELECT distinct im.engineered_gene_id FROM env_factor ef, env_fac_ind_mutation im "
 					+ " WHERE ef.name = ? "
 					+ " AND ef.env_factor_id = im.env_factor_id) AND engineered_gene_type = 'IM')";
 			OR = " OR ";
 			theList.add(inInducedMutationAgent.trim());
 		}
 		if (inGenomicSegDesignator != null && inGenomicSegDesignator.trim().length() > 0) {
 			theSQLString += OR + " ani_ge.engineered_gene_id IN (SELECT distinct engineered_gene_id "
 					+ " FROM engineered_gene WHERE upper(clone_designator) LIKE ? AND engineered_gene_type = 'GS')";
 			theList.add(inGenomicSegDesignator.trim().toUpperCase());
 		}
 
 		// Convert the params
 		Object[] theParams = new Object[theList.size()];
 		for (int i = 0; i < theList.size(); i++) {
 			theParams[i] = theList.get(i);
 		}
 
 		return getModelIds(theSQLString, theParams);
 	}
 
 	/**
 	 * Get the models with the associated engineered gene
 	 * 
 	 * @param inKeyword
 	 *            the keyword to search for
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException
 	 * 
 	 */
 	private String getModelIdsForAnyEngineeredGene(String inKeyword) throws PersistenceException {
 
 		String theSQLString = "SELECT distinct ani_ge.abs_cancer_model_id "
 				+ "FROM ani_mod_engineered_gene ani_ge WHERE ";
 
 		theSQLString += " ani_ge.engineered_gene_id IN (SELECT distinct engineered_gene_id "
 				+ " FROM engineered_gene WHERE upper(name) LIKE ?)";
 
 		theSQLString += " OR ani_ge.engineered_gene_id IN (SELECT distinct engineered_gene_id "
 				+ " FROM engineered_gene WHERE engineered_gene_id IN ("
 				+ " SELECT distinct im.engineered_gene_id FROM env_factor ef, env_fac_ind_mutation im "
 				+ " WHERE upper(ef.name) like ? "
 				+ " AND ef.env_factor_id = im.env_factor_id) AND engineered_gene_type = 'IM')";
 
 		theSQLString += " OR ani_ge.engineered_gene_id IN (SELECT distinct engineered_gene_id "
 				+ " FROM engineered_gene WHERE upper(clone_designator) LIKE ? AND engineered_gene_type = 'GS')";
 
 		// Convert the params
 		Object[] theParams = new Object[3];
 		theParams[0] = inKeyword;
 		theParams[1] = inKeyword;
 		theParams[2] = inKeyword;
 
 		return getModelIds(theSQLString, theParams);
 	}
 
     /**
      * Get the model id's that have an environmental factor
      * 
      * @return a list of matching model id
      * 
      * @throws PersistenceException
      */
     private String getModelIdsForAnyEnvironmentalFactor() throws PersistenceException {
 
         String theSQLString = "SELECT distinct ani_th.abs_cancer_model_id FROM animal_model_therapy ani_th "
                 + "WHERE ani_th.therapy_id IN (SELECT t.therapy_id FROM therapy t"
                 + "     WHERE t.therapeutic_experiment=0)";
 
         Object[] theParams = new Object[0];
         return getModelIds(theSQLString, theParams);
     }
     
 	/**
 	 * Get the model id's that have a matching environmental factor
 	 * 
 	 * @param inType
 	 *            the EF type
 	 * @param inName
 	 *            the name to look for
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIdsForEnvironmentalFactor(String inType, String inName) throws PersistenceException {
 
 		String theSQLString = "SELECT distinct ani_th.abs_cancer_model_id FROM animal_model_therapy ani_th "
 				+ "WHERE ani_th.therapy_id IN (SELECT t.therapy_id FROM therapy t, env_factor ef"
 				+ "     WHERE t.env_factor_id = ef.env_factor_id AND (ef.name = ? OR ef.name_unctrl_vocab = ?) AND ef.type = ?)";
 
 		Object[] theParams = new Object[3];
 		theParams[0] = inName;
         theParams[1] = inName;
 		theParams[2] = inType;
 		return getModelIds(theSQLString, theParams);
 	}
 
 	/**
 	 * Get the model id's for any model has a keyword match in the env factor
 	 * 
 	 * @param inKeyword
 	 *            the name to look for
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIdsForAnyEnvironmentalFactor(String inKeyword) throws PersistenceException {
 
 		String theSQLString = "SELECT distinct ani_th.abs_cancer_model_id FROM animal_model_therapy ani_th "
 				+ "WHERE ani_th.therapy_id IN (SELECT t.therapy_id FROM therapy t, env_factor ef"
 				+ "     WHERE t.env_factor_id = ef.env_factor_id AND upper(ef.name) like ?)";
 
 		Object[] theParams = new Object[1];
 		theParams[0] = inKeyword;
 		return getModelIds(theSQLString, theParams);
 	}
 
 	public List searchForAnimalModels(SearchData inSearchData) throws Exception {
 
 		log.trace("Entering searchForAnimalModels");
 
 		List theAnimalModels = null;
 
 		String theFromClause = "from AnimalModel as am where am.state = 'Edited-approved' AND am.availability.releaseDate < sysdate ";
 		String theOrderByClause = " ORDER BY am.modelDescriptor asc";
 
 		if (inSearchData.getKeyword() != null && inSearchData.getKeyword().length() > 0) {
 			log.debug("Doing a keyword search: " + inSearchData.getKeyword());
 			theAnimalModels = keywordSearch(theFromClause, theOrderByClause, inSearchData.getKeyword());
 		} else {
 			log.debug("Doing a criteria search");
 			theAnimalModels = criteriaSearch(theFromClause, theOrderByClause, inSearchData);
 		}
 
 		log.trace("Exiting searchForAnimalModels");
 
 		return theAnimalModels;
 	}
 
 	public int countMatchingAnimalModels(SearchData inSearchData) throws Exception {
 
 		log.trace("Entering countMatchingAnimalModels");
 
 		String theFromClause = "select count (am) from AnimalModel as am where am.state = 'Edited-approved' AND am.availability.releaseDate < sysdate ";
 		List theCountResults = null;
 
 		if (inSearchData.getKeyword() != null && inSearchData.getKeyword().trim().length() > 0) {
 			log.debug("Doing a keyword search: " + inSearchData.getKeyword());
 			String theWhereClause = buildKeywordSearchWhereClause(inSearchData.getKeyword());
 
 			try {
 				String theHQLQuery = theFromClause + theWhereClause;
 
 				log.info("HQL Query: " + theHQLQuery);
 
 				String theKeyword = "%" + inSearchData.getKeyword().toUpperCase().trim() + "%";
 				Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
 				theQuery.setParameter("keyword", theKeyword);
 
 				theCountResults = theQuery.list();
 
 			} catch (Exception e) {
 				log.error("Exception occurred searching for models", e);
 				throw e;
 			}
 		} else {
 			log.debug("Doing a criteria search");
 			String theWhereClause = buildCriteriaSearchWhereClause(inSearchData);
 
 			try {
 				String theHQLQuery = theFromClause + theWhereClause;
 
 				log.info("HQL Query: " + theHQLQuery);
 
 				Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
 				theCountResults = theQuery.list();
 
 			} catch (Exception e) {
 				log.error("Exception occurred searching for models", e);
 				throw e;
 			}
 		}
 
 		int theCount = -1;
 		if (theCountResults != null && theCountResults.size() > 0) {
 			Integer theInt = (Integer) theCountResults.get(0);
 			theCount = theInt.intValue();
 		}
 		log.trace("Exiting searchForAnimalModels");
 
 		return theCount;
 	}
 
 	// Build the where clause for the search and the count
 	private String buildKeywordSearchWhereClause(String inKeyword) throws Exception {
 		String theKeyword = "%" + inKeyword.toUpperCase().trim() + "%";
 
 		String theWhereClause = "";
 
 		theWhereClause += " AND (upper(am.modelDescriptor) like :keyword ";
 
 		theWhereClause += " OR am.species IN (from Taxon as t where upper(t.scientificName) like :keyword )";
 
 		theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForHistopathologyOrgan(theKeyword) + ")";
 
 		theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForHistopathologyDisease(theKeyword) + ")";
 
 		theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForAnyEnvironmentalFactor(inKeyword) + ")";
 
 		theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForAnyEngineeredGene(inKeyword) + ")";
 
 		theWhereClause += " OR am.phenotype IN (from Phenotype as p where upper(p.description) like :keyword )";
 
 		theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForCellLine(inKeyword) + ")";
 
 		theWhereClause += " OR abs_cancer_model_id IN (" + getModelIdsForTherapeuticApproach(inKeyword) + "))";
 
 		return theWhereClause;
 	}
 
 	private List keywordSearch(String inFromClause, String inOrderByClause, String inKeyword) throws Exception {
 
 		String theWhereClause = buildKeywordSearchWhereClause(inKeyword);
 
 		List theAnimalModels = null;
 
 		try {
 			String theHQLQuery = inFromClause + theWhereClause + inOrderByClause;
 
 			log.info("HQL Query: " + theHQLQuery);
 
 			String theKeyword = "%" + inKeyword.toUpperCase() + "%";
 			Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
 			theQuery.setParameter("keyword", theKeyword);
 
 			theAnimalModels = theQuery.list();
 		} catch (Exception e) {
 			log.error("Exception occurred searching for models", e);
 			throw e;
 		}
 
 		return theAnimalModels;
 	}
 
 	private String buildCriteriaSearchWhereClause(SearchData inSearchData) throws Exception {
 		String theWhereClause = "";
 
 		// PI criteria
 		if (inSearchData.getPiName() != null && inSearchData.getPiName().length() > 0) {
 
 			StringTokenizer theTokenizer = new StringTokenizer(inSearchData.getPiName());
 			String theLastName = theTokenizer.nextToken(",").trim();
 			String theFirstName = theTokenizer.nextToken().trim();
 
 			theWhereClause += " AND am.principalInvestigator IN (from Person as p where p.lastName like '%" + theLastName
 					+ "%' AND p.firstName like '%" + theFirstName + "%')";
 		}
 
 		// Model descriptor criteria
 		if (inSearchData.getModelDescriptor() != null && inSearchData.getModelDescriptor().trim().length() > 0) {
 
 			theWhereClause += " AND upper(am.modelDescriptor) like '%"
 					+ inSearchData.getModelDescriptor().toUpperCase().trim() + "%'";
 		}
 
 		// Species criteria
 		if (inSearchData.getSpecies() != null && inSearchData.getSpecies().length() > 0) {
 
 			theWhereClause += "AND am.species IN (from Taxon as t where t.scientificName = '"
 					+ inSearchData.getSpecies() + "')";
 		}
 
 		// Search for organ
 		if (inSearchData.getOrganTissueCode() != null && inSearchData.getOrganTissueCode().length() > 0) {
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForHistopathologyOrgan(inSearchData.getOrganTissueCode()) + ")";
 		}
 
 		// Search for disease
 		if (inSearchData.getDiagnosisCode() != null && inSearchData.getDiagnosisCode().trim().length() > 0) {
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForHistopathologyDisease(inSearchData.getDiagnosisCode()) + ")";
 		}
 
 		// ///////////////////////////////////////
 		// Carcinogenic interventions
 		// ///////////////////////////////////////
 
 		if (inSearchData.isSearchCarcinogenicInterventions() == true) {
 
 			log.debug("Searching for Carcinogenic Interventions");
 
             boolean searchForSpecificCI = false;
             
 			// Search for chemical/drug
 			if (inSearchData.getChemicalDrug() != null && inSearchData.getChemicalDrug().trim().length() > 0) {
 				theWhereClause += " AND abs_cancer_model_id IN ("
 						+ getModelIdsForEnvironmentalFactor("Chemical / Drug", inSearchData.getChemicalDrug().trim()) + ")";
                 searchForSpecificCI = true;
 			}
 
 			// Search for Surgery/Other
 			if (inSearchData.getSurgery() != null && inSearchData.getSurgery().length() > 0) {
 				theWhereClause += " AND abs_cancer_model_id IN ("
 						+ getModelIdsForEnvironmentalFactor("Other", inSearchData.getSurgery()) + ")";
                 searchForSpecificCI = true;
 			}
 
 			// Search for Hormone
 			if (inSearchData.getHormone() != null && inSearchData.getHormone().length() > 0) {
 				theWhereClause += " AND abs_cancer_model_id IN ("
 						+ getModelIdsForEnvironmentalFactor("Hormone", inSearchData.getHormone()) + ")";
                 searchForSpecificCI = true;
 			}
 
 			// Search for Growth Factor
 			if (inSearchData.getGrowthFactor() != null && inSearchData.getGrowthFactor().length() > 0) {
 				theWhereClause += " AND abs_cancer_model_id IN ("
 						+ getModelIdsForEnvironmentalFactor("Growth Factor", inSearchData.getGrowthFactor()) + ")";
                 searchForSpecificCI = true;
 			}
 
 			// Search for Radiation
 			if (inSearchData.getRadiation() != null && inSearchData.getRadiation().length() > 0) {
 				theWhereClause += " AND abs_cancer_model_id IN ("
 						+ getModelIdsForEnvironmentalFactor("Radiation", inSearchData.getRadiation()) + ")";
                 searchForSpecificCI = true;
 			}
 
 			// Search for Viral
 			if (inSearchData.getViral() != null && inSearchData.getViral().length() > 0) {
 				theWhereClause += " AND abs_cancer_model_id IN ("
 						+ getModelIdsForEnvironmentalFactor("Viral", inSearchData.getViral()) + ")";
                 searchForSpecificCI = true;
 			}
             
             // Search for any model w/ a CI
             if (searchForSpecificCI == false) {
                 theWhereClause += " AND abs_cancer_model_id IN ("
                     + getModelIdsForAnyEnvironmentalFactor() + ")";
             }
 		}
 
 		// Only call if some of the data is set
 		if ((inSearchData.getGeneName() != null && inSearchData.getGeneName().trim().length() > 0)
 				|| (inSearchData.getGenomicSegDesignator() != null && inSearchData.getGenomicSegDesignator().trim().length() > 0)
 				|| (inSearchData.getInducedMutationAgent() != null && inSearchData.getInducedMutationAgent().trim().length() > 0)) {
 
 			// Search for engineered genes
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForEngineeredGenes(inSearchData.getGeneName(), inSearchData.isEngineeredTransgene(),
 							inSearchData.isTargetedModification(), inSearchData.getGenomicSegDesignator(), inSearchData
 									.getInducedMutationAgent()) + ")";
 		}
 
 		// Search for phenotype
 		if (inSearchData.getPhenotype() != null && inSearchData.getPhenotype().trim().length() > 0) {
 			theWhereClause += " AND am.phenotype IN (from Phenotype as p where upper(p.description) like '%"
 					+ inSearchData.getPhenotype().trim().toUpperCase() + "%')";
 		}
 
 		// Search for cellline
 		if (inSearchData.getCellLine() != null && inSearchData.getCellLine().trim().length() > 0) {
 			theWhereClause += " AND abs_cancer_model_id IN (" + getModelIdsForCellLine(inSearchData.getCellLine().trim())
 					+ ")";
 		}
 
 		// Search for therapeutic approaches
 		if (inSearchData.isSearchTherapeuticApproaches()) {
 
 			theWhereClause += " AND abs_cancer_model_id IN ("
 					+ getModelIdsForTherapeuticApproach(inSearchData.getTherapeuticApproach().trim()) + ")";
 		}
 
 		// Search for therapeutic approaches
 		if (inSearchData.isSearchHistoMetastasis()) {
 			theWhereClause += " AND abs_cancer_model_id IN (" + getModelIdsForHistoMetastasis() + ")";
 		}
 
 		// Search for therapeutic approaches
 		if (inSearchData.isSearchMicroArrayData()) {
 			theWhereClause += " AND abs_cancer_model_id IN (" + getModelIdsForMicroArrayData() + ")";
 		}
 
 		// Search for therapeutic approaches
 		if (inSearchData.isSearchXenograft()) {
 			theWhereClause += " AND abs_cancer_model_id IN (" + getModelIdsForXenograft() + ")";
 		}
 
 		return theWhereClause;
 
 	}
 
 	private List criteriaSearch(String inFromClause, String inOrderByClause, SearchData inSearchData) throws Exception {
 
 		String theWhereClause = buildCriteriaSearchWhereClause(inSearchData);
 
 		List theAnimalModels = null;
 
 		try {
 			String theHQLQuery = inFromClause + theWhereClause + inOrderByClause;
 
 			log.info("HQL Query: " + theHQLQuery);
 
 			// HQLParameter[] theParameters = new HQLParameter[0];
 			// animalModels = Search.query(theHQLQuery, theParameters);
 			Query theQuery = HibernateUtil.getSession().createQuery(theHQLQuery);
 			theAnimalModels = theQuery.list();
 		} catch (Exception e) {
 			log.error("Exception occurred searching for models", e);
 			throw e;
 		}
 
 		return theAnimalModels;
 	}
 
 	/**
 	 * Extract the model ID's for an sql query with a specific organ.
 	 * 
 	 * @param inSQLString
 	 *            the SQL string that returns a set of IDs
 	 * @param inParameters
 	 *            the parameters to bind in the query
 	 * 
 	 * @return a list of matching model id
 	 * 
 	 * @throws PersistenceException
 	 */
 	private String getModelIds(String inSQLString, Object inParameters[]) throws PersistenceException {
 
 		log.trace("In getModelIds");
 
 		String theModelIds = "";
 
 		ResultSet theResultSet = null;
 		try {
 
 			log.info("getModelsIds - SQL: " + inSQLString);
 
 			theResultSet = Search.query(inSQLString, inParameters);
 
 			if (theResultSet.next()) {
 				theModelIds += theResultSet.getString(1);
 			}
 
 			while (theResultSet.next()) {
 				theModelIds += "," + theResultSet.getString(1);
 			}
 
 		} catch (Exception e) {
 			log.error("Exception in getModelIds", e);
 			throw new PersistenceException("Exception in getModelIds: " + e);
 		} finally {
 			if (theResultSet != null) {
 				try {
 					theResultSet.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 
 		if (theModelIds.equals("")) {
 			theModelIds = "-1";
 		}
 		return theModelIds;
 	}
 
 	public List getModelsForThisCompound(Long nscNumber) throws PersistenceException {
 		List models = new ArrayList();
 		int cc = 0;
 		ResultSet theResultSet = null;
 		try {
 
 			String theSQLString = "select acm.abs_cancer_model_id, " + "\n" + "       acm.model_descriptor," + "\n"
 					+ "       tx.abbreviation || ' ' || tx.ethnicity_strain" + "\n" + "  from abs_cancer_model acm,"
 					+ "\n" + "       animal_model_therapy amt," + "\n" + "       therapy t," + "\n"
 					+ "       env_factor ef," + "\n" + "       taxon tx" + "\n"
 					+ " where acm.abs_cancer_model_id = amt.abs_cancer_model_id" + "\n"
 					+ "   and acm.abs_cancer_model_type = 'AM'" + "\n" + "   and amt.therapy_id = t.therapy_id" + "\n"
 					+ "   and t.env_factor_id = ef.env_factor_id" + "\n" + "   and acm.taxon_id = tx.taxon_id" + "\n"
 					+ "   and ef.nsc_number = ?";
 			log.info("getModelsForThisCompound - SQL: " + theSQLString);
 
 			Object[] params = new Object[1];
 			params[0] = nscNumber;
 			theResultSet = Search.query(theSQLString, params);
 			while (theResultSet.next()) {
 				String[] item = new String[3];
 				item[0] = theResultSet.getString(1); // the id
 				item[1] = theResultSet.getString(2); // model descriptor
 				item[2] = theResultSet.getString(3); // strain
 				models.add(item);
 				cc++;
 			}
 			log.info("Got " + cc + " animal models");
 		} catch (Exception e) {
 			log.error("Exception in getModelsForThisCompound", e);
 			throw new PersistenceException("Exception in getModelsForThisCompound: " + e);
 		} finally {
 			if (theResultSet != null) {
 				try {
 					theResultSet.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return models;
 	}
 
 	public List getAllPublications(long absCancerModelId) throws PersistenceException {
 		List publications = new ArrayList();
 		int cc = 0;
 		ResultSet theResultSet = null;
 		try {
 			String theSQLString = "select publication_id, year, authors" + "\n" 
                     + "  from publication" + "\n"
 					+ "  where publication_id in (" + "\n" 
                     + "	  select min(publication_id) publication_id" + "\n"
 					+ "	  from (" + "\n" 
                     + "		select p.pmid, p.publication_id" + "\n"
 					+ "		  from animal_model_therapy amt," + "\n" 
                     + "		       therapy_publication tp," + "\n"
 					+ "		       publication p" + "\n" 
                     + "		  where amt.abs_cancer_model_id = ?" + "\n"
 					+ "		   and amt.therapy_id = tp.therapy_id" + "\n"
 					+ "		   and tp.publication_id = p.publication_id" + "\n" 
                     + "		union" + "\n"
 					+ "		select p.pmid, p.publication_id" + "\n" 
                     + "		  from ani_mod_cell_line acl," + "\n"
 					+ "		       cell_line_publication cp," + "\n" 
                     + "		       publication p" + "\n"
 					+ "		 where acl.abs_cancer_model_id = ?" + "\n" 
                     + "		   and acl.cell_line_id = cp.cell_line_id" + "\n" 
                     + "		   and cp.publication_id = p.publication_id" + "\n" 
                     + "		union" + "\n"
 					+ "		select p.pmid, p.publication_id" + "\n" 
                     + "		  from abs_can_mod_publication acmp," + "\n"
 					+ "		       publication p" + "\n" 
                     + "		 where acmp.abs_cancer_model_id = ?" + "\n"
 					+ "		   and acmp.publication_id = p.publication_id )" + "\n" 
                     + "	 group by pmid )" + "\n"
 					+ " order by year desc, authors" + "\n";
             
 			log.info("getAllPublications - SQL: " + theSQLString);
 			Object[] params = new Object[3];
 			params[0] = params[1] = params[2] = String.valueOf(absCancerModelId);
 			theResultSet = Search.query(theSQLString, params);
 			while (theResultSet.next()) {
 				String pid = theResultSet.getString(1); // the publication_id
 				Publication p = (Publication) get(pid, Publication.class);
 				publications.add(p);
 				cc++;
 			}
 			log.info("Got " + cc + " publications");
 		} catch (Exception e) {
 			log.error("Exception in getAllPublications", e);
 			throw new PersistenceException("Exception in getAllPublications: " + e);
 		} finally {
 			if (theResultSet != null) {
 				try {
 					theResultSet.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return publications;
 	}
 
 	public static void main(String[] inArgs) {
 		try {
 			System.out.println("Model ids: "
 					+ QueryManagerSingleton.instance().getModelIdsForHistopathologyOrgan("Skin"));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 }

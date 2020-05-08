 
 package edu.wustl.query.action;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.wustl.cab2b.server.cache.EntityCache;
 import edu.wustl.common.query.pvmanager.impl.PVManagerException;
 import edu.wustl.common.vocab.IConcept;
 import edu.wustl.common.vocab.IVocabulary;
 import edu.wustl.common.vocab.VocabularyException;
 import edu.wustl.common.vocab.impl.Concept;
 import edu.wustl.common.vocab.impl.Vocabulary;
 import edu.wustl.common.vocab.utility.VocabUtil;
 import edu.wustl.query.bizlogic.BizLogicFactory;
 import edu.wustl.query.bizlogic.SearchPermissibleValueBizlogic;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.global.VIProperties;
import edu.wustl.vi.enums.VISearchAlgorithm;
 
 /**
  * @author amit_doshi
  * Action Class to show the UI for Vocabulary Interface and to handle the Ajax request
  */
 public class SearchPermissibleValuesAction extends Action
 {
 
 	/**
 	 * This method handles the various Ajax request for VI
 	 * @param mapping mapping
 	 * @param form form
 	 * @param request request
 	 * @param response response
 	 * @return ActionForward actionForward
 	 * @throws Exception Exception
 	 */
 	@Override
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 
 		String searchTerm = request.getParameter(Constants.SEARCH_TERM);
 		String operation = request.getParameter(Constants.OPERATION);
 		String targetVocabs = request.getParameter("targetVocabsForSearchTerm");
 		//get the id of the component on which user click to search for PVs
 		String componentId = request.getParameter(Constants.COMPONENT_ID);
 		if (componentId == null)
 		{
 			//need to save componetid into the session for next Ajax requests
 			componentId = (String) request.getSession().getAttribute(Constants.COMPONENT_ID);
 		}
 
 		if (searchTerm != null && targetVocabs != null)
 		{
 			// AJAX Request handler for Getting search term Result data for source to target vocabularies
 			SearchPermissibleValueBizlogic bizLogic = (SearchPermissibleValueBizlogic) BizLogicFactory
 					.getInstance().getBizLogic(Constants.SEARCH_PV_FROM_VOCAB_BILOGIC_ID);
 			if (operation.equals(Constants.ABORT))
 			{
 				response.getWriter().write(
 						VocabUtil.getVocabProperties().getProperty("abort.message"));
 			}
 			else
 			{
 				try
 				{
 					searchTerm = searchTerm.trim();
 					String html = searchTermInTargetVocab(searchTerm, targetVocabs, componentId,
 							request);
 					response.getWriter().write(html);
 				}
 				catch (VocabularyException e)
 				{
 					response.getWriter().write(bizLogic.getErrorMessageAsHTML(e.getError().getErrorMessage()));
 				}
 			}
 
 		}
 		return null;
 	}
 
 	/**
 	 * This method will search for the entered text by the user across all the vocabularies
 	 * and return the HTML for the searched result
 	 * @param searchTerm
 	 * @param targetVocabs
 	 * @param request 
 	 * @param componentId 
 	 * @return
 	 * @throws VocabularyException
 	 * @throws PVManagerException 
 	 */
 	@SuppressWarnings("unchecked")
 	private String searchTermInTargetVocab(String searchTerm, String targetVocabs,
 			String componentId, HttpServletRequest request) throws VocabularyException,
 			PVManagerException
 	{
 		SearchPermissibleValueBizlogic bizLogic = (SearchPermissibleValueBizlogic) BizLogicFactory
 				.getInstance().getBizLogic(Constants.SEARCH_PV_FROM_VOCAB_BILOGIC_ID);
 
 		StringBuffer html = new StringBuffer();
 
 		StringTokenizer allTrgVocabs = new StringTokenizer(targetVocabs, "@");
 
 		String entityName = (String) request.getSession().getAttribute(Constants.ENTITY_NAME);
 		Entity entity = (Entity) EntityCache.getCache().getEntityById(Long.valueOf((entityName)));
 
 		Map<String, AttributeInterface> enumAttributeMap = (HashMap<String, AttributeInterface>) request
 				.getSession().getAttribute(Constants.ENUMRATED_ATTRIBUTE);
 		AttributeInterface attribute = (AttributeInterface) enumAttributeMap
 				.get(Constants.ATTRIBUTE_INTERFACE + componentId);
 
 		List<PermissibleValueInterface> pvList = bizLogic.getPermissibleValueListFromDB(attribute,
 				entity);
 		IVocabulary sourceVocabulary = bizLogic.getVocabulary(VIProperties.sourceVocabUrn);
 		while (allTrgVocabs.hasMoreTokens())
 		{
 			String[] vocabDetail = allTrgVocabs.nextToken().split("##");
 			String vocabURN = vocabDetail[0], vocabDisName = vocabDetail[1];
 			html.append(bizLogic.getRootVocabularyHTMLForSearch("srh_" + vocabURN, vocabDisName));
			//TODO: Pass the actual enum value for the selected search algorithm.
			//Change the VISearchAlgorithm.ANY_WORD to the value of the selected algorithm.
 			List<IConcept> conceptList = bizLogic.searchConcept(searchTerm, vocabURN,
					VIProperties.maxToReturnFromSearch,VISearchAlgorithm.ANY_WORD);
 			/**
 			 * to maintain the order of search results to show on UI
 			 */
 			Map<String, List<IConcept>> orderedConcepts = new LinkedHashMap<String, List<IConcept>>();
 			List<IConcept> medMappedNValiedPVConcept = new ArrayList<IConcept>();
 			List<IConcept> medMappedNNotValiedPVConcept = new ArrayList<IConcept>();
 			List<IConcept> notMEDMappedConcept = new ArrayList<IConcept>();
 			Map<String,String> sourceConceptMap = new HashMap<String, String>();
 			if (conceptList != null && conceptList.size() != 0)
 			{
 				for (IConcept concept : conceptList)
 				{
 					IConcept sourceConceptCode = new Concept();
 					int status = isSourceVocabMappedTerm(concept, pvList, sourceConceptCode,sourceVocabulary);
 					
 					sourceConceptMap.put(concept.getCode(), sourceConceptCode.getCode());
 					
 					maintainOrderOfConcepts(orderedConcepts, status, concept,
 							medMappedNValiedPVConcept, medMappedNNotValiedPVConcept,
 							notMEDMappedConcept);
 					//need to put the condition on size of each vocab
 
 					if ((medMappedNValiedPVConcept.size() == VIProperties.maxPVsToShow))
 					{
 						break;
 					}
 				}
 				createHTMLForConcept(orderedConcepts, vocabURN, html,sourceConceptMap);
 			}
 			else
 			{
 				html.append(bizLogic.getNoMappingFoundHTML());
 			}
 			int uiListSize=medMappedNValiedPVConcept.size()+medMappedNNotValiedPVConcept.size()
 							+notMEDMappedConcept.size();
 			if (uiListSize>=VIProperties.maxPVsToShow)
 			{
 				html.append(bizLogic.getInfoMessage());
 			}
 			html.append(bizLogic.getEndHTML());
 		}
 		return html.toString();
 	}
 
 	private void createHTMLForConcept(Map<String, List<IConcept>> orderedConcepts, String vocabURN,
 			StringBuffer html,Map<String,String> sourceConceptMap) throws VocabularyException
 	{
 
 		/********** Get HTML for Source Vocabulary************/
 		int rowsCreated=0;
 		if (vocabURN.equals(VocabUtil.getVocabProperties().getProperty("source.vocab.urn")))
 		{
 			rowsCreated=getHTML(orderedConcepts, vocabURN, html, Constants.MED_MAPPED_N_VALID_PVCONCEPT,
 								sourceConceptMap,rowsCreated);
 
 			rowsCreated=(rowsCreated<VIProperties.maxPVsToShow)?getHTML(orderedConcepts, vocabURN, html,
 					Constants.NOT_MED_VALED_PVCONCEPT,sourceConceptMap,rowsCreated):VIProperties.maxPVsToShow;
 			
 		}
 		else
 		{
 			/********** Get HTML for Other Vocabulary************/
 			/*first get the HTML for Result has mapping available in MED and it is valid permissible value for the entity.
 			 *  Show result as bold enabled.
 			 */
 			rowsCreated=getHTML(orderedConcepts, vocabURN, html, Constants.MED_MAPPED_N_VALID_PVCONCEPT,
 					sourceConceptMap,rowsCreated);
 			/* Result has mapping available in MED but it is not valid permissible value.
 			 * Show result as disabled bold italicized.
 			 */
 			rowsCreated=(rowsCreated<VIProperties.maxPVsToShow)? getHTML(orderedConcepts, vocabURN, html, 
 					Constants.MED_MAPPED_N_NOT_VALIED_PVCONCEPT,sourceConceptMap,rowsCreated):VIProperties.maxPVsToShow;
 			/* 	Result has no mapping available in MED.
 			 *	Show result as disabled  italicized.
 			 */
 			rowsCreated=(rowsCreated<VIProperties.maxPVsToShow)? getHTML(orderedConcepts, vocabURN, html,
 					Constants.NOT_MED_MAPPED_PVCONCEPT,sourceConceptMap,rowsCreated):VIProperties.maxPVsToShow;
 		}
 	}
 
 	/**
 	 * @param orderedConcepts
 	 * @param vocabName
 	 * @param vocabVersion
 	 * @param html
 	 * @param bizLogic
 	 * @param status
 	 * @throws VocabularyException
 	 */
 	private int getHTML(Map<String, List<IConcept>> orderedConcepts, String vocabURN,
 			StringBuffer html, String status,Map<String,String> sourceConceptMap,int rowsCreated) throws VocabularyException
 	{
 		SearchPermissibleValueBizlogic bizLogic = (SearchPermissibleValueBizlogic) BizLogicFactory
 				.getInstance().getBizLogic(Constants.SEARCH_PV_FROM_VOCAB_BILOGIC_ID);
 		List<IConcept> conceptList = orderedConcepts.get(status);
 		if (conceptList != null && conceptList.size() > 0)
 		{
 			for (IConcept medRelatedConcept : conceptList)
 			{
 				String checkboxId = vocabURN + Constants.ID_DEL + sourceConceptMap.get(medRelatedConcept.getCode());// concept.getCode();
 				html.append(bizLogic.getHTMLForSearchedConcept("srh_" + vocabURN,
 						medRelatedConcept, "srh_" + checkboxId, status));
 				rowsCreated++;
 				if(rowsCreated==VIProperties.maxPVsToShow)
 				{
 					rowsCreated++;
 					break;
 				}
 			}
 		}
 		return rowsCreated;
 	}
 
 	private void maintainOrderOfConcepts(Map<String, List<IConcept>> orderedConcepts, int status,
 			IConcept medRelatedConcept, List<IConcept> medMappedNValiedPVConcept,
 			List<IConcept> medMappedNNotValiedPVConcept, List<IConcept> notMEDMappedConcept)
 	{
 		switch (status)
 		{
 			case 1 :
 				medMappedNValiedPVConcept.add(medRelatedConcept);
 				orderedConcepts.put(Constants.MED_MAPPED_N_VALID_PVCONCEPT,
 						medMappedNValiedPVConcept);
 				break;
 
 			case 2 :
 				notMEDMappedConcept.add(medRelatedConcept);
 				orderedConcepts.put(Constants.NOT_MED_VALED_PVCONCEPT, notMEDMappedConcept);
 				break;
 
 			case 3 :
 				medMappedNValiedPVConcept.add(medRelatedConcept);
 				orderedConcepts.put(Constants.MED_MAPPED_N_VALID_PVCONCEPT,
 						medMappedNValiedPVConcept);
 				break;
 
 			case 4 :
 				medMappedNNotValiedPVConcept.add(medRelatedConcept);
 				orderedConcepts.put(Constants.MED_MAPPED_N_NOT_VALIED_PVCONCEPT,
 						medMappedNNotValiedPVConcept);
 				break;
 
 			case 5 :
 				notMEDMappedConcept.add(medRelatedConcept);
 				orderedConcepts.put(Constants.NOT_MED_MAPPED_PVCONCEPT, notMEDMappedConcept);
 				break;
 
 			default :
 				break;
 		}
 
 	}
 
 	/**
 	 * 
 	 * @param concept 
 	 * @param pvList 
 	 * @param sourceVocabulary 
 	 * @param sourceConcept 
 	 * @param componentId
 	 * @param request
 	 * @return
 	 * @throws VocabularyException 
 	 */
 	private int isSourceVocabMappedTerm(IConcept concept, List<PermissibleValueInterface> pvList,
 			IConcept sourceConceptCode, IVocabulary sourceVocabulary) throws VocabularyException
 	{
 		SearchPermissibleValueBizlogic bizLogic = (SearchPermissibleValueBizlogic) BizLogicFactory
 				.getInstance().getBizLogic(Constants.SEARCH_PV_FROM_VOCAB_BILOGIC_ID);
 
 		
 		int status = 0;
 		if (((Vocabulary) sourceVocabulary).equals((Vocabulary) concept.getVocabulary()))
 		{
 			/*In case of MED check whether the result is valid permissible value for the entity. 
 			 *If yes make it enable and show it in bold face otherwise disable the result.
 			 */
 			IConcept returnedConcept = bizLogic.isConceptExistInPVList(concept, pvList);
 			if (returnedConcept != null)
 			{
 				//If MED coded and its part of PV then show text should be bold with normal 
 				status = 1;//"Normal_Bold_Enabled";
 				((Concept)sourceConceptCode).setCode(returnedConcept.getCode());
 			}
 			else
 			{
 				/*user searched on the MED and the concept is not valid for
 				 * entity text should be normal disabled 
 				 */
 				status = 2;//"Normal_Disabled";
 				((Concept)sourceConceptCode).setCode(concept.getCode());
 			}
 
 		}
 		else
 		{
 			List<IConcept> concepts = bizLogic.isSourceVocabCodedTerm(concept,
 					VIProperties.translationAssociation, sourceVocabulary);
 			if (concepts != null)
 			{
 				IConcept sourceConcept = bizLogic.isConceptsExistInPVList(concepts, pvList);
 
 				if (sourceConcept != null)
 				{
 					/*	Result has mapping available in MED and it is valid permissible value for the entity.
 					 *  Show result as bold enabled.
 					 * (user has searched on other vocabulary and the searched concept is MED coded 
 					 * and its is  valid PV for entity then show text bold with enabled.)
 					 * 
 					 */
 					status = 3;//"Normal_Bold_Enabled";
 					((Concept)sourceConceptCode).setCode(sourceConcept.getCode());
 				}
 				else
 				{
 					/* Result has mapping available in MED but it is not valid permissible value.
 					 * Show result as disabled bold italicized.
 					 * (user has searched on other vocabulary and the searched concept is MED coded 
 					 * but  its is not valid PV  entity then show text Bold Italic Disabled.)
 					 */
 					status = 4;//"Bold_Italic_Disabled";
 					((Concept)sourceConceptCode).setCode(concept.getCode());
 				}
 			}
 			else
 			{
 				/* 	Result has no mapping available in MED.
 				 *	Show result as disabled  italicized.
 				 * (user has searched on other vocabulary and the searched concept is not MED coded
 				 * then show text Italic Normal and Disabled;)  
 				 */
 				status = 5;//"Normal_Italic_Disabled";
 				((Concept)sourceConceptCode).setCode(concept.getCode());
 			}
 		}
 		return status;
 	}
 }

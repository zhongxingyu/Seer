 /**
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.chai.kevin.survey;
 /**
  * @author Jean Kahigiso M.
  *
  */
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.LocationService;
 import org.chai.kevin.data.DataService;
 import org.chai.kevin.data.Enum;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.data.Type.TypeVisitor;
 import org.chai.kevin.data.Type.ValueType;
 import org.chai.kevin.form.FormElement;
 import org.chai.kevin.form.FormElementService;
 import org.chai.kevin.form.FormEnteredValue;
 import org.chai.kevin.form.FormValidationService;
 import org.chai.kevin.form.FormValidationService.ValidatableLocator;
 import org.chai.kevin.location.CalculationLocation;
 import org.chai.kevin.location.DataLocation;
 import org.chai.kevin.location.DataLocationType;
 import org.chai.kevin.location.Location;
 import org.chai.kevin.location.LocationLevel;
 import org.chai.kevin.reports.ReportService;
 import org.chai.kevin.survey.SurveyElement.SurveyElementCalculator;
 import org.chai.kevin.survey.SurveyElement.SurveyElementSubmitter;
 import org.chai.kevin.survey.SurveyQuestion.QuestionType;
 import org.chai.kevin.survey.validation.SurveyEnteredProgram;
 import org.chai.kevin.survey.validation.SurveyEnteredQuestion;
 import org.chai.kevin.survey.validation.SurveyEnteredSection;
 import org.chai.kevin.survey.validation.SurveyLog;
 import org.chai.kevin.value.RawDataElementValue;
 import org.chai.kevin.value.ValidatableValue;
 import org.chai.kevin.value.Value;
 import org.chai.kevin.value.ValueService;
 import org.hibernate.LockMode;
 import org.hibernate.LockOptions;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.transaction.support.TransactionCallbackWithoutResult;
 import org.springframework.transaction.support.TransactionTemplate;
 
 public class SurveyPageService {
 	
 	private static Log log = LogFactory.getLog(SurveyPageService.class);
 	
 	private FormElementService formElementService;
 	private SurveyValueService surveyValueService;
 	private ValueService valueService;
 	private DataService dataService;
 	private LocationService locationService;
 	private FormValidationService formValidationService;
 	private SessionFactory sessionFactory;
 	private PlatformTransactionManager transactionManager;
 	
 	private TransactionTemplate transactionTemplate;
 	
 	private Set<String> submitSkipLevels;
 	private Set<String> locationSkipLevels;
 
 	private TransactionTemplate getTransactionTemplate() {
 		if (transactionTemplate == null) {
 			transactionTemplate = new TransactionTemplate(transactionManager);
 			transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
 		}
 		return transactionTemplate;
 	}
 	
 	@Transactional(readOnly = true)
 	public Survey getDefaultSurvey() {
 		return (Survey)sessionFactory.getCurrentSession()
 			.createCriteria(Survey.class).add(Restrictions.eq("active", true)).uniqueResult();
 	}
 	
 	private void collectEnums(FormElement element, final Map<String, Enum> enums) {
 		element.getDataElement().getType().visit(new TypeVisitor() {
 			@Override
 			public void handle(Type type, String prefix) {
 				if (type.getType() == ValueType.ENUM) {
 					if (!enums.containsKey(type.getEnumCode())) {
 						enums.put(type.getEnumCode(), dataService.findEnumByCode(type.getEnumCode()));
 					}
 				}
 			}
 		});
 	}
 	
 	@Transactional(readOnly = false)
 	public SurveyPage getSurveyPage(DataLocation dataLocation, SurveyQuestion currentQuestion) {
 		if (log.isDebugEnabled()) log.debug("getSurveyPage(dataLocation="+dataLocation+", currentQuestion="+currentQuestion+")");
 //		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		
 		Map<SurveyElement, FormEnteredValue> elements = new HashMap<SurveyElement, FormEnteredValue>();
 		Map<SurveyQuestion, SurveyEnteredQuestion> questions = new HashMap<SurveyQuestion, SurveyEnteredQuestion>();
 		Map<String, Enum> enums = new HashMap<String, Enum>();
 		
 		SurveyEnteredQuestion enteredQuestion = surveyValueService.getOrCreateSurveyEnteredQuestion(dataLocation, currentQuestion);
 		questions.put(currentQuestion, enteredQuestion);
 		for (SurveyElement element : currentQuestion.getSurveyElements(dataLocation.getType())) {
 			FormEnteredValue enteredValue = formElementService.getOrCreateFormEnteredValue(dataLocation, element);
 			elements.put(element, enteredValue);
 			collectEnums(element, enums);
 		}
 		
 		SurveyPage page = new SurveyPage(dataLocation, currentQuestion.getSurvey(), null, null, null, null, questions, elements, enums);
 		if (log.isDebugEnabled()) log.debug("getSurveyPage(...)="+page);
 		return page;
 	}
 	
 	@Transactional(readOnly = false)
 	public SurveyPage getSurveyPage(DataLocation dataLocation, SurveySection currentSection) {
 //		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		
 		SurveyProgram currentProgram = currentSection.getProgram();
 		Survey survey = currentProgram.getSurvey();
 		
 		Map<SurveyProgram, SurveyEnteredProgram> programs = new HashMap<SurveyProgram, SurveyEnteredProgram>();
 		Map<SurveySection, SurveyEnteredSection> sections = new HashMap<SurveySection, SurveyEnteredSection>();
 		for (SurveyProgram program : survey.getPrograms(dataLocation.getType())) {
 			SurveyEnteredProgram enteredProgram = getSurveyEnteredProgram(dataLocation, program);
 			programs.put(program, enteredProgram);
 			
 			for (SurveySection section : program.getSections(dataLocation.getType())) {
 				SurveyEnteredSection enteredSection = getSurveyEnteredSection(dataLocation, section);
 				sections.put(section, enteredSection);
 			}
 		}
 		
 		Map<SurveyQuestion, SurveyEnteredQuestion> questions = new HashMap<SurveyQuestion, SurveyEnteredQuestion>();
 		Map<SurveyElement, FormEnteredValue> elements = new HashMap<SurveyElement, FormEnteredValue>();
 		Map<String, Enum> enums = new HashMap<String, Enum>();
 		for (SurveyQuestion question : currentSection.getQuestions(dataLocation.getType())) {
 			SurveyEnteredQuestion enteredQuestion = surveyValueService.getOrCreateSurveyEnteredQuestion(dataLocation, question);
 			questions.put(question, enteredQuestion);
 			
 			for (SurveyElement element : question.getSurveyElements(dataLocation.getType())) {
 				FormEnteredValue enteredValue = formElementService.getOrCreateFormEnteredValue(dataLocation, element);
 				elements.put(element, enteredValue);
 				collectEnums(element, enums);
 			}
 		}
 		
 		SurveyPage page = new SurveyPage(dataLocation, survey, currentProgram, currentSection, programs, sections, questions, elements, enums);
 		if (log.isDebugEnabled()) log.debug("getSurveyPage(...)="+page);
 		return page;
 	}
 	
 	
 	@Transactional(readOnly = false)
 	public SurveyPage getSurveyPage(DataLocation dataLocation, SurveyProgram currentProgram) {
 		Survey survey = currentProgram.getSurvey();
 		
 		Map<SurveyProgram, SurveyEnteredProgram> programs = new HashMap<SurveyProgram, SurveyEnteredProgram>();
 		Map<SurveySection, SurveyEnteredSection> sections = new HashMap<SurveySection, SurveyEnteredSection>();
 		for (SurveyProgram program : survey.getPrograms(dataLocation.getType())) {
 			SurveyEnteredProgram enteredProgram = getSurveyEnteredProgram(dataLocation, program);
 			programs.put(program, enteredProgram);
 			
 			for (SurveySection section : program.getSections(dataLocation.getType())) {
 				SurveyEnteredSection enteredSection = getSurveyEnteredSection(dataLocation, section);
 				sections.put(section, enteredSection);
 			}
 		}
 
 		Map<SurveyQuestion, SurveyEnteredQuestion> questions = new HashMap<SurveyQuestion, SurveyEnteredQuestion>();
 		Map<SurveyElement, FormEnteredValue> elements = new HashMap<SurveyElement, FormEnteredValue>();
 		Map<String, Enum> enums = new HashMap<String, Enum>();
 		for (SurveySection section : currentProgram.getSections(dataLocation.getType())) {
 			section = (SurveySection)sessionFactory.getCurrentSession().get(SurveySection.class, section.getId());
 			for (SurveyQuestion question : section.getQuestions(dataLocation.getType())) {
 				SurveyEnteredQuestion enteredQuestion = surveyValueService.getOrCreateSurveyEnteredQuestion(dataLocation, question);
 				questions.put(question, enteredQuestion);
 				
 				for (SurveyElement element : question.getSurveyElements(dataLocation.getType())) {
 					FormEnteredValue enteredValue = formElementService.getOrCreateFormEnteredValue(dataLocation, element);
 					elements.put(element, enteredValue);
 					collectEnums(element, enums);
 				}
 			}
 		}
 		
 		SurveyPage page = new SurveyPage(dataLocation, survey, currentProgram, null, programs, sections, questions, elements, enums);
 		if (log.isDebugEnabled()) log.debug("getSurveyPage(...)="+page);
 		return page;
 	}	
 	
 	@Transactional(readOnly = false)
 	public SurveyPage getSurveyPagePrint(DataLocation dataLocation,Survey survey) {
 		DataLocationType dataLocationType = dataLocation.getType();
 		
 		Map<SurveyElement, FormEnteredValue> elements = new LinkedHashMap<SurveyElement, FormEnteredValue>();
 		Map<String, Enum> enums = new HashMap<String, Enum>();
 		
 		for (SurveyProgram program : survey.getPrograms(dataLocationType)) {
 			for (SurveySection section : program.getSections(dataLocationType)) {
 				for (SurveyQuestion question : section.getQuestions(dataLocationType)) {
 					for (SurveyElement element : question.getSurveyElements(dataLocationType)) {
 						FormEnteredValue enteredValue = formElementService.getOrCreateFormEnteredValue(dataLocation, element);
 						elements.put(element, enteredValue);
 						collectEnums(element, enums);
 					}
 				}
 			}
 
 		}
		return new SurveyPage(dataLocation, survey, null, null, null, null,null, elements, enums);
 	}
 	
 
 	@Transactional(readOnly = false)
 	public SurveyPage getSurveyPage(DataLocation dataLocation, Survey survey) {
 		Map<SurveyProgram, SurveyEnteredProgram> programs = new HashMap<SurveyProgram, SurveyEnteredProgram>();
 		Map<SurveySection, SurveyEnteredSection> sections = new HashMap<SurveySection, SurveyEnteredSection>();
 		for (SurveyProgram program : survey.getPrograms(dataLocation.getType())) {
 			SurveyEnteredProgram enteredProgram = getSurveyEnteredProgram(dataLocation, program);
 			programs.put(program, enteredProgram);
 			
 			for (SurveySection section : program.getSections(dataLocation.getType())) {
 				SurveyEnteredSection enteredSection = getSurveyEnteredSection(dataLocation, section);
 				sections.put(section, enteredSection);
 			}
 		}
 		return new SurveyPage(dataLocation, survey, null, null, programs, sections, null, null, null);
 	}
 	
 	@Transactional(readOnly = false)
 	public void refresh(CalculationLocation location, final Survey survey, final boolean closeIfComplete) {
 		List<DataLocation> dataLocations = location.collectDataLocations(null, null);
 		
 		for (final DataLocation dataLocation : dataLocations) {
 //			survey = (Survey)sessionFactory.getCurrentSession().load(Survey.class, survey.getId());
 //			dataLocation = (DataLocation)sessionFactory.getCurrentSession().get(DataLocation.class, dataLocation.getId());
 
 			getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
 				@Override
 				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
 					refreshSurveyForDataLocation(dataLocation, survey, closeIfComplete);
 				}
 			});
 			sessionFactory.getCurrentSession().clear();
 		}
 	}
 	
 	@Transactional(readOnly = false)
 	public void refreshSurveyForDataLocation(DataLocation dataLocation, Survey survey, boolean closeIfComplete) {
 		if (log.isDebugEnabled()) log.debug("refreshSurveyForDataLocation(dataLocation="+dataLocation+", survey="+survey+", closeIfComplete="+closeIfComplete+")");
 //		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 //		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		Set<SurveyProgram> validPrograms = new HashSet<SurveyProgram>(survey.getPrograms(dataLocation.getType()));
 		for (SurveyProgram program : survey.getPrograms()) {
 			if (validPrograms.contains(program)) refreshProgramForDataLocation(dataLocation, program, closeIfComplete);
 			else deleteSurveyEnteredProgram(program, dataLocation);
 		}
 	}
 	
 	private void refreshProgramForDataLocation(DataLocation dataLocation, SurveyProgram program, boolean closeIfComplete) {
 		Set<SurveySection> validSections = new HashSet<SurveySection>(program.getSections(dataLocation.getType()));
 		for (SurveySection section : program.getSections()) {
 			if (validSections.contains(section)) refreshSectionForDataLocation(dataLocation, section);
 			else deleteSurveyEnteredSection(section, dataLocation);
 		}
 		
 		SurveyEnteredProgram enteredProgram = getSurveyEnteredProgram(dataLocation, program);
 		setProgramStatus(enteredProgram, dataLocation);
 		if (closeIfComplete && enteredProgram.isComplete() && !enteredProgram.isInvalid()) enteredProgram.setClosed(true); 
 		surveyValueService.save(enteredProgram);
 	}
 	
 	@Transactional(readOnly = false)
 	public void refreshSectionForDataLocation(DataLocation dataLocation, SurveySection section) {
 //		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 //		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		Set<SurveyQuestion> validQuestions = new HashSet<SurveyQuestion>(section.getQuestions(dataLocation.getType()));
 		for (SurveyQuestion question : section.getQuestions()) {
 			if (validQuestions.contains(question)) refreshQuestionForDataLocation(dataLocation, question);
 			else deleteSurveyEnteredQuestion(question, dataLocation);
 		}
 		SurveyEnteredSection enteredSection = getSurveyEnteredSection(dataLocation, section);
 		setSectionStatus(enteredSection, dataLocation);
 		surveyValueService.save(enteredSection);
 	}
 	
 	private void refreshQuestionForDataLocation(DataLocation dataLocation, SurveyQuestion question) {
 		Set<FormElement> validElements = new HashSet<FormElement>(question.getSurveyElements(dataLocation.getType()));
 		for (SurveyElement element : question.getSurveyElements()) {
 			if (validElements.contains(element)) refreshElementForDataLocation(dataLocation, element);
 			else deleteSurveyEnteredValue(element, dataLocation);
 		}
 		
 		SurveyEnteredQuestion enteredQuestion = surveyValueService.getOrCreateSurveyEnteredQuestion(dataLocation, question);
 		setQuestionStatus(enteredQuestion, dataLocation);
 		surveyValueService.save(enteredQuestion);
 	}
 	
 	private void refreshElementForDataLocation(DataLocation dataLocation, SurveyElement element) {
 		Survey survey = element.getSurvey();
 		
 		FormEnteredValue enteredValue = formElementService.getOrCreateFormEnteredValue(dataLocation, element);
 		// TODO this value should be evicted at some point
 		RawDataElementValue rawDataElementValue = valueService.getDataElementValue(element.getDataElement(), dataLocation, survey.getPeriod());
 		if (rawDataElementValue != null) enteredValue.setValue(rawDataElementValue.getValue());
 		else enteredValue.setValue(Value.NULL_INSTANCE());
 		if (survey.getLastPeriod() != null) {
 			// TODO this value should be evicted at some point
 			RawDataElementValue lastDataValue = valueService.getDataElementValue(element.getDataElement(), dataLocation, survey.getLastPeriod());
 			if (lastDataValue != null) enteredValue.setLastValue(lastDataValue.getValue());
 			else enteredValue.setLastValue(Value.NULL_INSTANCE());
 		}
 		formElementService.save(enteredValue);
 	}
 	
 	// returns the list of modified elements/questions/sections/programs (skip, validation, etc..)
 	// we set the isolation level on READ_UNCOMMITTED to avoid deadlocks because in READ_COMMITTED
 	// mode, a write lock is acquired at the beginning and never released till this method terminates
 	// which causes other sessions calling this method to timeout
 	@Transactional(readOnly = false)
 	public SurveyPage modify(DataLocation dataLocation, SurveyProgram program, List<SurveyElement> elements, Map<String, Object> params) {
 		if (log.isDebugEnabled()) log.debug("modify(dataLocation="+dataLocation+", elements="+elements+")");
 		
 		// we acquire a write lock on the program
 		// this won't change anything for MyISAM tables
 		SurveyEnteredProgram enteredProgram = getSurveyEnteredProgram(dataLocation, program);
 		sessionFactory.getCurrentSession().buildLockRequest(LockOptions.NONE).setLockMode(LockMode.PESSIMISTIC_WRITE).lock(enteredProgram);
 		
 		SurveyPage surveyPage = null;
 		// if the program is not closed, we go on with the save
 		if (!enteredProgram.isClosed()) {
 			Map<SurveyElement, FormEnteredValue> affectedElements = new HashMap<SurveyElement, FormEnteredValue>();
 			// first we save the values
 			for (SurveyElement element : elements) {
 				if (log.isDebugEnabled()) log.debug("setting new value for element: "+element);
 				
 				FormEnteredValue enteredValue = formElementService.getOrCreateFormEnteredValue(dataLocation, element);
 				ValidatableValue validatableValue = enteredValue.getValidatable();
 				
 				// merge the values
 				// this modifies the value object accordingly
 				validatableValue.mergeValue(params, "elements["+element.getId()+"].value", new HashSet<String>());
 				
 				// set the value and save
 				// here, a write lock is acquired on the FormEnteredValue that will be kept
 				// till the end of the transaction, if in READ_COMMITTED isolation mode, a timeout
 				// is likely to occur because the transaction is quite long
 				formElementService.save(enteredValue);
 				affectedElements.put(element, enteredValue);
 				
 				// if it is a checkbox question, we need to reset the values to null
 				// FIXME THIS IS A HACK
 				resetCheckboxQuestion(dataLocation, element, affectedElements);
 			}
 			// we evaluate the rules
 			surveyPage = evaluateRulesAndSave(dataLocation, elements, affectedElements);
 		}
 
 		if (log.isDebugEnabled()) log.debug("modify(...)="+surveyPage);
 		return surveyPage;
 	}
 	
 	private ValidatableLocator getLocator() {
 		return new ValidatableLocator() {
 			@Override
 			public ValidatableValue getValidatable(Long id, DataLocation location) {
 				FormElement element = formElementService.getFormElement(id);
 				FormEnteredValue enteredValue = formElementService.getOrCreateFormEnteredValue(location, element);
 				if (enteredValue == null) return null;
 				return enteredValue.getValidatable();
 			}
 		};
 	}
 	
 	private SurveyPage evaluateRulesAndSave(DataLocation dataLocation, List<? extends FormElement> elements, Map<SurveyElement, FormEnteredValue> affectedElements) {  
 		if (log.isDebugEnabled()) log.debug("evaluateRulesAndSave(dataLocation="+dataLocation+", elements="+elements+")");
 		
 		// second we get the rules that could be affected by the changes
 		List<FormEnteredValue> affectedEnteredValues = new ArrayList<FormEnteredValue>();
 		List<SurveyEnteredQuestion> affectedEnteredQuestions = new ArrayList<SurveyEnteredQuestion>();
 		SurveyElementCalculator elementCalculator = new SurveyElementCalculator(affectedEnteredValues, affectedEnteredQuestions, formValidationService, formElementService, surveyValueService, getLocator());
 		
 		for (FormElement element : elements) {
 			if (log.isDebugEnabled()) log.debug("getting skip and validation rules for element: "+element);
 
 			element.validate(dataLocation, elementCalculator);
 			element.executeSkip(dataLocation, elementCalculator);
 		}
 		
 		// third we add the affected values in the lists
 		for (FormEnteredValue formEnteredValue : affectedEnteredValues) {
 			affectedElements.put((SurveyElement)formElementService.getFormElement(formEnteredValue.getFormElement().getId(), SurveyElement.class), formEnteredValue);
 		}
 		Map<SurveyQuestion, SurveyEnteredQuestion> affectedQuestions = new HashMap<SurveyQuestion, SurveyEnteredQuestion>();
 		for (SurveyEnteredQuestion surveyEnteredQuestion : affectedEnteredQuestions) {
 			affectedQuestions.put(surveyEnteredQuestion.getQuestion(), surveyEnteredQuestion);
 		}
 		
 		// fourth we propagate the affected changes up the survey tree and save
 		if (log.isDebugEnabled()) log.debug("propagating changes up the survey tree");
 		for (FormElement element : affectedElements.keySet()) {
 			SurveyQuestion question = ((SurveyElement)element).getSurveyQuestion();
 			if (!affectedQuestions.containsKey(question)) {
 				SurveyEnteredQuestion enteredQuestion = surveyValueService.getOrCreateSurveyEnteredQuestion(dataLocation, question);
 				affectedQuestions.put(question, enteredQuestion);
 			}
 		}
 		
 		Map<SurveySection, SurveyEnteredSection> affectedSections = new HashMap<SurveySection, SurveyEnteredSection>();
 		for (SurveyEnteredQuestion question : affectedQuestions.values()) {
 			// we set the question status correctly and save
 			setQuestionStatus(question, dataLocation);
 			
 			SurveySection section = question.getQuestion().getSection();
 			if (!affectedSections.containsKey(section)) {
 				SurveyEnteredSection enteredSection = getSurveyEnteredSection(dataLocation, section);
 				affectedSections.put(section, enteredSection);
 			}
 			
 		}
 		
 		Map<SurveyProgram, SurveyEnteredProgram> affectedPrograms = new HashMap<SurveyProgram, SurveyEnteredProgram>();
 		for (SurveyEnteredSection section : affectedSections.values()) {
 			// we set the section status correctly and save
 			setSectionStatus(section, dataLocation);
 			
 			SurveyProgram program = section.getSection().getProgram();
 			if (!affectedPrograms.containsKey(program)) {
 				SurveyEnteredProgram enteredProgram = getSurveyEnteredProgram(dataLocation, program);
 				affectedPrograms.put(program, enteredProgram);
 			}
 		}
 		
 		for (SurveyEnteredProgram program : affectedPrograms.values()) {
 			// if the program is not closed and available
 			// we set the program status correctly and save
 			setProgramStatus(program, dataLocation);
 		}
 		
 		// fifth we save all the values
 		for (FormEnteredValue formEnteredValue : affectedElements.values()) {
 			formElementService.save(formEnteredValue);
 		}
 		for (SurveyEnteredQuestion surveyEnteredQuestion : affectedQuestions.values()) {
 			surveyValueService.save(surveyEnteredQuestion);
 		}
 		for (SurveyEnteredSection surveyEnteredSection : affectedSections.values()) {
 			surveyValueService.save(surveyEnteredSection);
 		}
 		for (SurveyEnteredProgram surveyEnteredProgram : affectedPrograms.values()) {
 			surveyValueService.save(surveyEnteredProgram);
 		}
 		
 		return new SurveyPage(dataLocation, null, null, null, affectedPrograms, affectedSections, affectedQuestions, affectedElements, null);
 	}
 
 	// FIXME HACK 
 	// TODO get rid of this
 	private void resetCheckboxQuestion(DataLocation dataLocation, SurveyElement element, Map<SurveyElement, FormEnteredValue> affectedElements) {
 		if (log.isDebugEnabled()) log.debug("question is of type: "+element.getSurveyQuestion().getType());
 		if (element.getSurveyQuestion().getType() == QuestionType.CHECKBOX) {
 			if (log.isDebugEnabled()) log.debug("checking if checkbox question needs to be reset");
 			boolean reset = true;
 			for (SurveyElement elementInQuestion : element.getSurveyQuestion().getSurveyElements(dataLocation.getType())) {
 				FormEnteredValue enteredValueForElementInQuestion = formElementService.getOrCreateFormEnteredValue(dataLocation, elementInQuestion);
 
 				if (enteredValueForElementInQuestion.getValue().getBooleanValue() == Boolean.TRUE) reset = false;
 			}
 			if (log.isDebugEnabled()) log.debug("resetting checkbox question: "+reset);
 			for (SurveyElement elementInQuestion : element.getSurveyQuestion().getSurveyElements(dataLocation.getType())) {
 				FormEnteredValue enteredValueForElementInQuestion = formElementService.getOrCreateFormEnteredValue(dataLocation, elementInQuestion);
 
 				if (reset) enteredValueForElementInQuestion.getValue().setJsonObject(Value.NULL_INSTANCE().getJsonObject());
 				else if (enteredValueForElementInQuestion.getValue().isNull()) {
 					enteredValueForElementInQuestion.getValue().setJsonObject(enteredValueForElementInQuestion.getType().getValue(false).getJsonObject());
 				}
 				
 				formElementService.save(enteredValueForElementInQuestion);
 				affectedElements.put(elementInQuestion, enteredValueForElementInQuestion);
 			}
 		}
 	}
 	
 	private void setProgramStatus(SurveyEnteredProgram program, DataLocation dataLocation) {
 		Boolean complete = true;
 		Boolean invalid = false;
 		for (SurveySection section : program.getProgram().getSections(dataLocation.getType())) {
 			SurveyEnteredSection enteredSection = getSurveyEnteredSection(dataLocation, section);
 			if (!enteredSection.isComplete()) complete = false;
 			if (enteredSection.isInvalid()) invalid = true;
 		}
 		program.setComplete(complete);
 		program.setInvalid(invalid);
 	}
 	
 	private void setSectionStatus(SurveyEnteredSection section, DataLocation dataLocation) {
 		Boolean complete = true;
 		Boolean invalid = false;
 		for (SurveyQuestion question : section.getSection().getQuestions(dataLocation.getType())) {
 			SurveyEnteredQuestion enteredQuestion = surveyValueService.getOrCreateSurveyEnteredQuestion(dataLocation, question);
 			if (!enteredQuestion.isComplete() && !enteredQuestion.isSkipped()) complete = false;
 			if (enteredQuestion.isInvalid() && !enteredQuestion.isSkipped()) invalid = true;
 		}
 		section.setInvalid(invalid);
 		section.setComplete(complete);
 	}
 	
 	private void setQuestionStatus(SurveyEnteredQuestion question, DataLocation dataLocation) {
 		Boolean complete = true;
 		Boolean invalid = false;
 		
 		// TODO replace this method by a call to the survey element service
 		for (SurveyElement element : question.getQuestion().getSurveyElements(dataLocation.getType())) {
 			FormEnteredValue enteredValue = formElementService.getOrCreateFormEnteredValue(dataLocation, element);
 			if (!enteredValue.getValidatable().isComplete()) complete = false;
 			if (enteredValue.getValidatable().isInvalid()) invalid = true;
 		}
 		question.setInvalid(invalid);
 		question.setComplete(complete);
 	}
 	
 	@Transactional(readOnly = false)
 	public boolean submitAll(CalculationLocation location, Set<DataLocationType> types, Survey survey, SurveyProgram program){
 		if (log.isDebugEnabled()) log.debug("submitAll(location=" + location + ", survey=" + survey + ", program="+program+")");
 //		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		
 		boolean result = false;		
 		List<DataLocation> dataLocations = location.collectDataLocations(null, types);
 		result = submitAll(dataLocations, survey, program);
 		return result;
 	}
 	
 	private boolean submitAll(List<DataLocation> dataLocations, Survey survey, SurveyProgram program) {		
 		
 		for (DataLocation dataLocation : dataLocations) {
 			submitIfNotClosed(dataLocation, survey, program);
 		}
 		
 		// commented do to Hibernate bug https://hibernate.onjira.com/browse/HHH-2763
 //		getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
 //			@Override
 //			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
 //				Survey newSurvey = (Survey)sessionFactory.getCurrentSession().load(Survey.class, survey.getId());
 //				submitIfNotClosed(survey, dataLocation);
 //			}
 //		});
 //		sessionFactory.getCurrentSession().clear();
 		
 		return true;
 	}
 	
 	private void submitIfNotClosed(DataLocation dataLocation, Survey survey, SurveyProgram program) {
 		if (log.isDebugEnabled()) log.debug("submitIfNotClosed(survey=" + survey + ", location=" + dataLocation + ")");
 		
 		List<SurveyProgram> surveyPrograms = new ArrayList<SurveyProgram>();
 		if(program != null){
 			if(program.getSurvey().getPrograms(dataLocation.getType()).contains(program))
 				surveyPrograms.add(program);	
 		}
 		else surveyPrograms = survey.getPrograms(dataLocation.getType());
 		
 		for (SurveyProgram surveyProgram : surveyPrograms) {
 			
 			if(!surveyProgram.getTypeCodes().contains(dataLocation.getType().getCode())) continue;
 			
 			// we get whether to submit anyways if the program is not closed, even if it is incomplete or invalid
 			boolean isClosed = getSurveyEnteredProgram(dataLocation, surveyProgram).isClosed();				
 			if (!isClosed) {
 				
 				// first we make sure that the program is valid and complete, so we revalidate it
 				List<SurveyElement> elements = surveyProgram.getElements(dataLocation.getType());
 				evaluateRulesAndSave(dataLocation, elements, new HashMap<SurveyElement, FormEnteredValue>());
 				
 				SurveyElementSubmitter submitter = new SurveyElementSubmitter(surveyValueService, formElementService, valueService);
 
 				// save all the values to data values
 				for (SurveyElement element : elements) {
 					element.submit(dataLocation, element.getSurvey().getPeriod(), submitter);
 				}
 				
 				// close the program
 				SurveyEnteredProgram enteredProgram = getSurveyEnteredProgram(dataLocation, surveyProgram);
 				enteredProgram.setClosed(true);
 				surveyValueService.save(enteredProgram);
 		
 				// log the event
 				//logSurveyEvent(dataLocation, program, "submit");
 			}
 			
 			if (log.isDebugEnabled()) log.debug("submit(" + dataLocation + ", " + surveyProgram + ", )");				
 		}
 	}
 	
 //	private void logSurveyEvent(DataLocation dataLocation, SurveyProgram program, String event) {
 //		SurveyLog surveyLog = new SurveyLog(program.getSurvey(), program, dataLocation);
 //		surveyLog.setEvent(event);
 //		surveyLog.setTimestamp(new Date());
 //		sessionFactory.getCurrentSession().save(surveyLog);
 //	}
 	
 	public void reopen(DataLocation dataLocation, SurveyProgram program) {
 		SurveyEnteredProgram enteredProgram = getSurveyEnteredProgram(dataLocation, program); 
 		enteredProgram.setClosed(false);
 		surveyValueService.save(enteredProgram);
 	}
 	
 	private SurveyEnteredProgram getSurveyEnteredProgram(DataLocation dataLocation, SurveyProgram surveyProgram) {
 		SurveyEnteredProgram enteredProgram = surveyValueService.getSurveyEnteredProgram(surveyProgram, dataLocation);
 		if (enteredProgram == null) {
 			enteredProgram = new SurveyEnteredProgram(surveyProgram, dataLocation, false, false, false);
 //			setProgramStatus(enteredProgram, dataLocation);
 			surveyValueService.save(enteredProgram);
 		}
 		return enteredProgram;
 	}
 	
 	private SurveyEnteredSection getSurveyEnteredSection(DataLocation dataLocation, SurveySection surveySection) {
 		SurveyEnteredSection enteredSection = surveyValueService.getSurveyEnteredSection(surveySection, dataLocation);
 		if (enteredSection == null) {
 			enteredSection = new SurveyEnteredSection(surveySection, dataLocation, false, false);
 //			setSectionStatus(enteredSection, dataLocation);
 			surveyValueService.save(enteredSection);
 		}
 		return enteredSection;
 	}
 
 	private void deleteSurveyEnteredProgram(SurveyProgram program, DataLocation dataLocation) {
 		SurveyEnteredProgram enteredProgram = surveyValueService.getSurveyEnteredProgram(program, dataLocation);
 		if (enteredProgram != null) surveyValueService.delete(enteredProgram); 
 		
 		for (SurveySection section : program.getSections()) {
 			deleteSurveyEnteredSection(section, dataLocation);
 		}
 	}
 
 	private void deleteSurveyEnteredSection(SurveySection section, DataLocation dataLocation) {
 		SurveyEnteredSection enteredSection = surveyValueService.getSurveyEnteredSection(section, dataLocation);
 		if (enteredSection != null) surveyValueService.delete(enteredSection);
 		
 		for (SurveyQuestion question : section.getQuestions()) {
 			deleteSurveyEnteredQuestion(question, dataLocation);
 		}
 	}
 
 	private void deleteSurveyEnteredQuestion(SurveyQuestion question, DataLocation dataLocation) {
 		SurveyEnteredQuestion enteredQuestion = surveyValueService.getSurveyEnteredQuestion(question, dataLocation);
 		if (enteredQuestion != null) surveyValueService.delete(enteredQuestion);
 		
 		for (FormElement element : question.getSurveyElements()) {
 			deleteSurveyEnteredValue(element, dataLocation);
 		}
 	}
 
 	private void deleteSurveyEnteredValue(FormElement element, DataLocation dataLocation) {
 		FormEnteredValue enteredValue = formElementService.getFormEnteredValue(element, dataLocation);
 		if (enteredValue != null) formElementService.delete(enteredValue);
 	}
 	
 	public void setSurveyValueService(SurveyValueService surveyValueService) {
 		this.surveyValueService = surveyValueService;
 	}
 	
 	public void setFormElementService(FormElementService formElementService) {
 		this.formElementService = formElementService;
 	}
 	
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setFormValidationService(FormValidationService formValidationService) {
 		this.formValidationService = formValidationService;
 	}
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	public void setDataService(DataService dataService) {
 		this.dataService = dataService;
 	}
 	
 	public void setTransactionManager(PlatformTransactionManager transactionManager) {
 		this.transactionManager = transactionManager;
 	}
 
 	public void setLocationService(LocationService locationService) {
 		this.locationService = locationService;
 	}
 	
 	public void setSubmitSkipLevels(Set<String> submitSkipLevels) {
 		this.submitSkipLevels = submitSkipLevels;
 	}
 	
 	public void setLocationSkipLevels(Set<String> locationSkipLevels) {
 		this.locationSkipLevels = locationSkipLevels;
 	}
 	
 	public Set<LocationLevel> getSkipSubmitLevels() {
 		Set<LocationLevel> levels = new HashSet<LocationLevel>();
 		for (String skipLevel : this.submitSkipLevels) {
 			levels.add(locationService.findLocationLevelByCode(skipLevel));
 		}
 		return levels;
 	}
 	
 	public Set<LocationLevel> getSkipLocationLevels() {
 		Set<LocationLevel> levels = new HashSet<LocationLevel>();
 		for (String skipLevel : this.locationSkipLevels) {
 			levels.add(locationService.findLocationLevelByCode(skipLevel));
 		}
 		return levels;
 	}
 	
 }

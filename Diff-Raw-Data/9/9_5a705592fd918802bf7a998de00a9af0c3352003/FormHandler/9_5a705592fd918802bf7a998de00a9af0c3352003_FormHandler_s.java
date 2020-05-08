 package org.openrosa.client.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import org.openrosa.client.Context;
 import org.openrosa.client.controller.FormDesignerController;
 import org.openrosa.client.model.Condition;
 import org.openrosa.client.model.DataDef;
 import org.openrosa.client.model.DataDefBase;
 import org.openrosa.client.model.DataInstanceDef;
 import org.openrosa.client.model.FormDef;
 import org.openrosa.client.model.GroupDef;
 import org.openrosa.client.model.IFormElement;
 import org.openrosa.client.model.ItemSetDef;
 import org.openrosa.client.model.ItextModel;
 import org.openrosa.client.model.OptionDef;
 import org.openrosa.client.model.PredicateDef;
 import org.openrosa.client.model.QuestionDef;
 import org.openrosa.client.model.RepeatQtnsDef;
 import org.openrosa.client.model.SkipRule;
 import org.openrosa.client.model.ValidationRule;
 import org.openrosa.client.xforms.XformParser;
 import org.purc.purcforms.client.locale.LocaleText;
 import org.purc.purcforms.client.model.Locale;
 import org.purc.purcforms.client.xforms.XformConstants;
 
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.Element;
 import com.google.gwt.xml.client.XMLParser;
 
 public class FormHandler {
 	
 	
 	
 	/** The next available form id. We always have one form for OpenRosa form designer. */
 	private static int nextFormId = 1;
 	
 	/** The clipboard for copying and pasting*/
 	private static List<IFormElement> clipboard;
 	
 	/** Are we copying or cutting when we paste?*/
 	private static boolean isCutMode = false;
 	
 	/**
 	 * This takes the given list of questions and turn them into their own form.
 	 * This is useful for copying existing questions/question into a new block
 	 * or question
 	 * @param questions
 	 * @param sourceForm
 	 * @param formAttrMap
 	 * @return the xml of the selected questions
 	 */
 	public static String copyQuestionsToNewForm(List<IFormElement> questions, FormDef sourceForm,
 			HashMap<String, String>formAttrMap)
 	{
 		return copyQuestionsToNewForm(questions, sourceForm, formAttrMap, null);
 	}
 	
 	/**
 	 * This takes the given list of questions and turn them into their own form.
 	 * This is useful for copying existing questions/question into a new block
 	 * or question
 	 * @param questions the questions to be pulled into a block
 	 * @param sourceForm the form these questions were pulled from
 	 * @param formAttrMap I don't really know what this does
 	 * @param blockId the ID for the block that these questions will belong to
 	 * @return the xml of the selected questions
 	 */
 	public static String copyQuestionsToNewForm(List<IFormElement> questions, FormDef sourceForm,
 			HashMap<String, String>formAttrMap, String blockId)
 	{
 		//need to force the itext stuff to update
 		updateLanguage(Context.getLocale(), Context.getLocale(), sourceForm);
 		//now save the translations and languages
 		HashMap<String, ItextModel>translations = sourceForm.getITextMap(); 
 		ListStore<ItextModel> langauges = sourceForm.getITextList();
 		
 		//create a new temporary form
 		FormDef form = new FormDef();
 		form.setName(sourceForm.getName());
 		//set the form name otherwise turning this into XML blows up
 		form.setVariableName("new_form1");
 		form.setBinding("new_form1");
 		
 		//create a map of questions to tie in validates and skip logic
 		HashMap<String, IFormElement> questionMap = new HashMap<String,IFormElement>();
 		
 		//loop through the questions and add them to the new form
 		for(IFormElement element : questions)
 		{
 			if(element instanceof QuestionDef)
 			{
 				QuestionDef question = (QuestionDef)element;
 				
 				//this does a deep copy of these questions so we don't screw up the source form
 				//and places the copy in our new form
 				QuestionDef newQuestion = (QuestionDef)(question.copy(form));
 				form.addChild(newQuestion);
 				//if there's a block id, add it
 				if(blockId != null)
 				{
 					newQuestion.setBlockId(blockId);
 				}			
 				questionMap.put(question.getBinding(), newQuestion);
 				//check if there's a data instance we need to know about
 				if(newQuestion.usesItemSet())
 				{
 					ItemSetDef itemSet = newQuestion.getItemSet();
 					//find out what data instance is used
 					DataInstanceDef did = itemSet.getDataInstance();					
 					//check if we already have this data instance
 					if(form.getDataInstance(did.getBinding()) == null)
 					{
 						//if not add it
 						form.addDataInstance(did.getBinding(), did.copy(form));
 					}
 					//get the new versions of that data instance
 					DataInstanceDef newDid = form.getDataInstance(did.getBinding());
 					//set the itemset of the new question to reference the new data instance
 					itemSet.getNodeSet().set(0, newDid);
 				}
 			}
 			else if(element instanceof GroupDef)
 			{
 				GroupDef groupDef = (GroupDef)element;
 				//this does a deep copy of these questions so we don't screw up the source form
 				//and places the copy in our new form
 				GroupDef newGroup = (GroupDef)(groupDef.copy(form));
 				form.addChild(newGroup);
 				questionMap.put(groupDef.getBinding(), newGroup);
 				
 			}
 		}
 		
 		if(sourceForm.getSkipRules() != null)
 		{
 			//now check the existing relatives and see if any of them need to be copied over
 			for(Object o : sourceForm.getSkipRules())
 			{
 				SkipRule sr = (SkipRule)o;
 				//the new skip rule, should the original pass all our tests
 				SkipRule newSr = new SkipRule(sr);
 				//clear out the conditions
 				newSr.setConditions(new Vector<Condition>());
 				
 				//
 				if(sr.getActionTarget()!= null && questionMap.containsKey(sr.getActionTarget().getBinding()))
 				{
 					newSr.setActionTarget(questionMap.get(sr.getActionTarget().getBinding()));
 				}
 				else //if the targets aren't valid, skip this SkipRul
 				{
 					continue;
 				}
 				//now check conditions
 				boolean conditionsValid = false;
 				for(Object oo :  sr.getConditions())
 				{
 					Condition c = (Condition)oo;
 					if(questionMap.containsKey(c.getQuestion().getBinding()))
 					{
 						conditionsValid = true;
 						//this condition is legit, so copy it over
 						Condition newC = new Condition(c);
 						IFormElement question = questionMap.get(c.getQuestion().getBinding());
 						if(question instanceof QuestionDef)
 						{
 							newC.setQuestion((QuestionDef)question);
 							newSr.addCondition(newC);
 						}
 											
 					}
 				}
 				//if the conditions aren't valid, skip this SkipRule
 				if(!conditionsValid)
 				{
 					continue;
 				}
 				
 				//this is a valid skip rule, so lets copy it over
 				form.addSkipRule(newSr);
 				
 			}
 		} //end if there are skip rules
 		
 		//make sure there are validation rules
 		if(sourceForm.getValidationRules() != null)
 		{
 			//now we need to check for validates and copy those over
 			for(ValidationRule vr : sourceForm.getValidationRules())
 			{
 				if(questionMap.containsKey(vr.getQuestion().getBinding()))
 				{
 					//the question is being copied over, so copy over this validate
 					ValidationRule newVr = new ValidationRule(vr);
 					IFormElement question = questionMap.get(vr.getQuestion().getBinding());
 					if(question instanceof QuestionDef)
 					{
 						newVr.setQuestion((QuestionDef)question);
 						form.addValidationRule(newVr);
 					}
 				}
 			}
 		}//end if there are validation rules
 		
 		
 		
 		form.setITextMap(translations);
 		form.setITextList(langauges);
 		form.setLocales(sourceForm.getLocales());
 		String xml = FormHandler.writeToXML(form);
 		
 		
 		return xml;
 	}
 	
 	
 
 
 	/**
 	 * This method takes in a form, a question type, and an element in the form and creates a
 	 * new question of the given type after the element in the form.
 	 * @param form Form to add a new question to
 	 * @param questionType Type of new question to add
 	 * @param insertPoint element in form that question should be added after. If null question is added at the end of the form
 	 * @return returns the newly created question
 	 */
 	public static IFormElement addNewQuestion(FormDef form, int questionType, IFormElement insertPoint)
 	{
 		
 		//handle the null case of insertPoint
 		if(insertPoint == null)
 		{
 			insertPoint = form;
 		}
 		//if the insert point is a data element, bump the question up to the form
 		if(insertPoint instanceof DataDefBase)
 		{
 			insertPoint = form;
 		}
 		
 		//if the insertPoint is a repeat question
 		if((insertPoint instanceof QuestionDef) && (((QuestionDef)insertPoint).getDataType() == QuestionDef.QTN_TYPE_REPEAT))
 		{
 			//get the binding string
 			String bindingStr = getNewQuestionBinding(form, insertPoint, false);
 			
 			//if We're adding a group
 			if(questionType == QuestionDef.QTN_TYPE_GROUP)
 			{
 				//create the group
 				GroupDef groupDef = new GroupDef("Group "+bindingStr,null,insertPoint);				
 				groupDef.setBinding(bindingStr);
 				groupDef.setItextId(groupDef.getBinding());
 				
 				addFormDefItem(groupDef, insertPoint);
 				return groupDef;
 			}
 			//we're adding everything else
 			else
 			{
 				//create the question
 				String QuestionText = questionType == QuestionDef.QTN_TYPE_REPEAT ? LocaleText.get("qtnTypeRepeat")+bindingStr : LocaleText.get("question")+bindingStr; 
 				QuestionDef questionDef = new QuestionDef(QuestionText,questionType,bindingStr,insertPoint);
 				questionDef.setItextId(questionDef.getBinding());
 				//add the question to it's parent
 				addFormDefItem(questionDef,insertPoint);
 				//if it's a question that needs some options, then make it so
 				addNewOptionsIfNeedBe(questionType, questionDef, bindingStr, form);
 				return questionDef;
 
 			}//end question type
 
 		}//end insert point is a repeat question		
 		//if the insertPoint is a non-repeat question
 		else if(insertPoint instanceof QuestionDef)
 		{
 			//get the binding string
 			String bindingStr = getNewQuestionBinding(form, insertPoint, false);
 			
 			//if We're adding a group
 			if(questionType == QuestionDef.QTN_TYPE_GROUP)
 			{
 				//create the group
 				GroupDef groupDef = new GroupDef("Group "+bindingStr,null,insertPoint.getParent());				
 				groupDef.setBinding(bindingStr);
 				groupDef.setItextId(groupDef.getBinding());
 				
 				addFormDefItem(groupDef, insertPoint.getParent());
 				return groupDef;
 			}
 			//we're adding everything else
 			else
 			{
 				//create the question
 				String QuestionText = questionType == QuestionDef.QTN_TYPE_REPEAT ? LocaleText.get("qtnTypeRepeat")+bindingStr : LocaleText.get("question")+bindingStr; 
 				QuestionDef questionDef = new QuestionDef(QuestionText,questionType,bindingStr,insertPoint);
 				questionDef.setItextId(questionDef.getBinding());
 				//add the question to it's parent
 				addFormDefItem(questionDef,insertPoint.getParent());
 				//if it's a question that needs some options, then make it so
 				addNewOptionsIfNeedBe(questionType, questionDef, bindingStr, form);
 				return questionDef;
 
 			}//end question type
 		}//end if the point of insert is a QuestionDef
 		
 		//if the insert point is an optionDef
 		else if(insertPoint instanceof OptionDef)
 		{
 			//get the bindings and IDs
 			String bindingStr = getNewQuestionBinding(form, insertPoint, false);
 			//create the new question
 			String QuestionText = questionType == QuestionDef.QTN_TYPE_REPEAT ? LocaleText.get("qtnTypeRepeat")+bindingStr : LocaleText.get("question")+bindingStr; 
 			QuestionDef questionDef = new QuestionDef(QuestionText,questionType,bindingStr,insertPoint);
 			questionDef.setItextId(questionDef.getBinding());
 			//add the question to it's parent
 			addFormDefItem(questionDef,insertPoint.getParent());
 			//if it's a question that needs some options, then make it so
 			addNewOptionsIfNeedBe(questionType, questionDef, bindingStr, form);
 			
 			return questionDef;
 		}//end if insert point is an option
 		
 		//if the insert point is a group
 		else if(insertPoint instanceof GroupDef){
 			//update the binding
 			String bindingStr = getNewQuestionBinding(form, insertPoint, false);
 			//add the question and set its attributes
 			//if We're adding a group
 			if(questionType == QuestionDef.QTN_TYPE_GROUP)
 			{
 				//create the group
 				GroupDef groupDef = new GroupDef("Group "+bindingStr,null,insertPoint);
 				groupDef.setBinding(bindingStr);
 				groupDef.setItextId(groupDef.getBinding());
 				
 				addFormDefItem(groupDef, insertPoint);
 				return groupDef;
 			}
 			//we're adding everything else
 			else
 			{
 				//create the question
 				String QuestionText = questionType == QuestionDef.QTN_TYPE_REPEAT ? LocaleText.get("qtnTypeRepeat")+bindingStr : LocaleText.get("question")+bindingStr; 
 				QuestionDef questionDef = new QuestionDef(QuestionText,questionType,bindingStr,insertPoint);
 				questionDef.setItextId(questionDef.getBinding());
 				//add the question to it's parent
 				addFormDefItem(questionDef,insertPoint);
 				//if it's a question that needs some options, then make it so
 				addNewOptionsIfNeedBe(questionType, questionDef, bindingStr, form);
 				return questionDef;
 
 			}//end question type
 		}//end if we're adding this to a group
 		
 		//if insert point is a form
 		else if(insertPoint instanceof FormDef)
 		{
 			//get the bindings
 			String bindingStr = getNewQuestionBinding(form, insertPoint, false);
 			//if We're adding a group
 			if(questionType == QuestionDef.QTN_TYPE_GROUP)
 			{
 				//create the group
 				GroupDef groupDef = new GroupDef("Group "+bindingStr,null,insertPoint);
 				groupDef.setBinding(bindingStr);
 				groupDef.setItextId(groupDef.getBinding());
 				
 				addFormDefItem(groupDef, insertPoint);
 				return groupDef;
 			}
 			//we're adding everything else
 			else
 			{
 				//create the question
 				String QuestionText = questionType == QuestionDef.QTN_TYPE_REPEAT ? LocaleText.get("qtnTypeRepeat")+bindingStr : LocaleText.get("question")+bindingStr; 
 				QuestionDef questionDef = new QuestionDef(QuestionText,questionType,bindingStr,insertPoint);
 				questionDef.setItextId(questionDef.getBinding());
 				//add the question to it's parent
 				addFormDefItem(questionDef,insertPoint);
 				//if it's a question that needs some options, then make it so
 				addNewOptionsIfNeedBe(questionType, questionDef, bindingStr, form);
 				return questionDef;
 
 			}//end question type
 		}
 		
 		
 		return null;
 	}
 	
 	
 	/**
 	 * This function lets you add in a new element under the insert point, or as the insert point child
 	 * @param insertPoint - insert point is a child
 	 * @param thingToAdd
 	 * @return
 	 */
 	public static IFormElement addNewElement(IFormElement insertPoint, IFormElement thingToAdd)
 	{
 		addFormDefItem(thingToAdd, insertPoint);
 		return thingToAdd;
 
 	} //end addNewElement()
 	
 	
 	/**
 	 * Figure out what the new id for this object should be
 	 * @param form the form we're adding a question to
 	 * @param insertPoint where the new question should go
 	 * @param isAddingChild true if this is a child
 	 * @return the string id/binding whatever you want to call it
 	 */
 	public static String getNewQuestionBinding(FormDef form, IFormElement insertPoint, boolean isAddingChild)
 	{
 		
 		//if we're currently looking at a form def
 		if(insertPoint instanceof FormDef)
 		{
 			String newQuestionBinding = form.getQuestionStartLetter() + NumberFormat.getFormat("#00").format(form.getNewQuestionId());
 			while(!isQuestionNameUnqiue(form, newQuestionBinding))
 			{
 				form.incrementNewQuestionId();
 				newQuestionBinding = form.getQuestionStartLetter() + NumberFormat.getFormat("#00").format(form.getNewQuestionId());
 			}
 			return newQuestionBinding;
 		}
 		else if(insertPoint instanceof QuestionDef)					
 		{
 			QuestionDef questionDef = (QuestionDef)insertPoint;
 			//is this a repeat or group question?
 			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_GROUP ||
 					questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT )
 			{
 				
 				
 				
 				String parentBinding = questionDef.getBinding();
 				int id = questionDef.getNewQuestionId();
 				return parentBinding+"_"+NumberFormat.getFormat("#00").format(id);
 			}
 			else // get the parent and try again
 			{
 				return getNewQuestionBinding(form, insertPoint.getParent(), isAddingChild);
 			}
 		}
 		else if (insertPoint instanceof GroupDef)
 		{
 			//check and see if this is what the user has selected, if it is
 			//go up the tree one more level since we don't want to add a new
 			//question to the groupdef, we want to add it to what the group def is in,
 			//but if we got here because of a recursive call, we should stop here
 			if(!isAddingChild)
 			{
 				return getNewQuestionBinding(form, insertPoint.getParent(), isAddingChild);	
 			}
 			GroupDef groupDef = (GroupDef)insertPoint;
 			String parentBinding = groupDef.getBinding();
 			int id = groupDef.getNewQuestionId();
 			return parentBinding+"_"+NumberFormat.getFormat("#00").format(id);
 			
 		}
 		//if we're currently on an option, get the parent and then keep moving
 		else if(insertPoint instanceof OptionDef)
 		{
 			return getNewQuestionBinding(form, insertPoint.getParent(), isAddingChild);
 		}
 
 		return "BUSTED";
 	}//end getNewQuestionBinding()
 	
 	
 	/**
 	 * This method checks to see if the questionName passed in is unique in the given form
 	 * @param form the form we're working with.
 	 * @param questionName the name we're checking for uniqueness
 	 * @return true if it is unique, false otherwise.
 	 */
 	private static boolean isQuestionNameUnqiue(FormDef form, String questionName)
 	{		
 		if(form.getChild(questionName) == null)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}//end isQuestionNameUnique
 	
 	
 	/**
 	 * Used to add kids to their parents when 
 	 * putting new things in a form
 	 * @param newElement the newly created element
 	 * @param parentItem the parent
 	 */
 	private static void addFormDefItem(IFormElement newElement,IFormElement parentItem){
 		//make the controller dirty since we've officially changed things
 		FormDesignerController.makeDirty();
 		//Is the parent a question?
 		if(parentItem instanceof QuestionDef){
 			QuestionDef parentQDef = (QuestionDef)parentItem;
 			//is the new element an option
 			if(newElement instanceof OptionDef)
 			{
 				//add the option
 				((QuestionDef)parentItem).addOption((OptionDef)newElement);
 			}
 			else
 			{
 				//if this is a group or repeat question then add
 				//the new question.
 				if(parentQDef.getDataType() == QuestionDef.QTN_TYPE_GROUP ||
 						parentQDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
 				{
 					((QuestionDef)parentItem).addRepeatQtnsDef((QuestionDef)newElement);
 				}
 				else //otherwise just add things as a child
 				{
 					((IFormElement)parentItem).getParent().addChild(newElement);
 				}
 			}
 		}
 		//if we're dealing with groups or forms, then just add them
 		else if(parentItem instanceof GroupDef || parentItem instanceof FormDef)
 		{
 			parentItem.addChild(newElement);
 		}
 				
 	}//end addFormDefItem()
 	
 	
 	/**
 	 * Used to add a new option to a newly created
 	 * select or select1 question
 	 * @param questionDef the question that was just created
 	 * @param parentItem the parent of the newly created question
 	 */
 	public static OptionDef addNewOptionDef(QuestionDef questionDef){
 		//create a new id
 		int id = questionDef.getChildCount() + 1;
 		//get the parents binding
 		String parentBinding = questionDef.getBinding();
 		//create a new option
 		OptionDef optionDef = new OptionDef(LocaleText.get("option")+parentBinding+"_"+id,parentBinding+"_"+id,String.valueOf(id), questionDef);
 		//set the new option's itext id
 		optionDef.setItextId(optionDef.getBinding());
 		//add this new option to it's parent
 		addFormDefItem(optionDef,questionDef);
 		
 		updateSelectIds(questionDef);
 		return optionDef;
 	}
 	
 	/**
 	 * Used to check if the newly created question is a 
 	 * select or select1 and thus needs a new option created
 	 * @param questionType type of question
 	 * @param questionDef the new question
 	 * @param bindingStr the binding string
 	 * @param form the form all of this needs to be inserted into
 	 */
 	private static void addNewOptionsIfNeedBe(int questionType, QuestionDef questionDef, String bindingStr, FormDef form)
 	{
 		//is this a select or select1 question?
 		if(questionType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || questionType == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
 		{
 			//if it's a select question be sure to update the binding
 			if(questionType == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
 			{
 				questionDef.setBinding(bindingStr+QuestionDef.MULTI_SUFFIX);
 				questionDef.setItextId(bindingStr+QuestionDef.MULTI_SUFFIX);
 			}					
 			//add a default option
 			addNewOptionDef(questionDef);
 		}
 		//if it's a group, add a new default question to it.
 		else if(questionType == QuestionDef.QTN_TYPE_GROUP)
 			addNewQuestion(form, QuestionDef.QTN_TYPE_TEXT, questionDef);
 	}//end addNewOptionsIfNeedBe
 
 
 	/**
 	 * This method is used to create a fresh new form
 	 * @param withDefaultQuestion do we add the default question or not
 	 * @return
 	 */
 	public static FormDef newForm(boolean withDefaultQuestion)
 	{		
 		//clear the dirty flag cause we're starting from scratch
 		FormDesignerController.clearIsDirty();
 		//increment the form ID
 		int id = nextFormId++;
 		//create the new form and set its attributes
 		FormDef form = new FormDef();
 		form.setBinding("new_form"+id);
 		form.setId(id);
 		form.setText(LocaleText.get("newForm")+" "+id);
 		
 		//now add the device ID question
 		String bindingStr = "deviceid"; 
 		QuestionDef questionDef = (QuestionDef)addNewQuestion(form, QuestionDef.QTN_TYPE_TEXT, null);
 		questionDef.setBinding(bindingStr);
 		questionDef.setText(bindingStr);
 		questionDef.setItextId("");
 		questionDef.setVisible(false);
 		questionDef.setRequired(false);
 		questionDef.setPreload("property");
 		questionDef.setPreloadParam("deviceid");
 		
 		//now add the start time question
 		bindingStr = "start"; 
 		questionDef = (QuestionDef)addNewQuestion(form, QuestionDef.QTN_TYPE_TIME, null);
 		questionDef.setBinding(bindingStr);
 		questionDef.setText(bindingStr);
 		questionDef.setItextId("");
 		questionDef.setVisible(false);
 		questionDef.setRequired(false);
 		questionDef.setPreload("timestamp");
 		questionDef.setPreloadParam("start");
 		
 		//now add the end time question
 		bindingStr = "end"; 
 		questionDef = (QuestionDef)addNewQuestion(form, QuestionDef.QTN_TYPE_TIME, null);
 		questionDef.setBinding(bindingStr);
 		questionDef.setText(bindingStr);
 		questionDef.setItextId("");
 		questionDef.setVisible(false);
 		questionDef.setRequired(false);
 		questionDef.setPreload("timestamp");
 		questionDef.setPreloadParam("end");
 		
 		//set the IDs for new questions
 		form.setNewQuestionId(1);
 		if(withDefaultQuestion)
 		{
 			//add a default select question
 			addNewQuestion(form, QuestionDef.QTN_TYPE_LIST_EXCLUSIVE, null);
 		}
 		
 		return form;
 	}//end newForm();
 	
 	
 	/**
 	 * Give a question multi or single select question this will re order the 
 	 * iText ID and, for the moment, the binding
 	 * @param qDef
 	 */
 	private static void updateSelectIds(QuestionDef qDef)
 	{
 
 		//first make sure the question is of the correct type
 		int dataType = qDef.getDataType();
 		if(!(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE))
 		{
 			return;
 		}
 		
 		String parentBinding = qDef.getBinding();
 		int count = 0; //number of selects that we've found that need ordering
 		//is the parent a multi?
 		String delimiter = "_";
 		
 		//now get the options
 		if(qDef.getOptions() instanceof List)
 		{
 			List<Object> optionList = (List)(qDef.getOptions());
 			for(Object o : optionList)
 			{
 				if(!(o instanceof OptionDef))
 				{
 					continue;
 				}
 				OptionDef option = (OptionDef)o;
 				//figure out if the iText is setup "<parent_binding>_<counter_number>"
 				String iText = option.getItextId();	
 				if(iText == null)
 				{
 					continue;
 				}
 				int counterNumber = -999999;
 				String counterNumberStr = "";
 				try
 				{
 					counterNumberStr = iText.substring(parentBinding.length()+delimiter.length());				
 					counterNumber = Integer.parseInt(counterNumberStr);
 				}
 				catch (Exception e)
 				{
 					
 				}
 				
 				//is this a valid iText ID?
 				if( iText.startsWith(parentBinding+delimiter) && counterNumber != -999999)
 				{
 					//found a valid option
 					count++;
 					//rename it
 					//option.setBinding(parentBinding+delimiter+count);
 					option.setItextId(parentBinding+delimiter+count);
 					option.setBinding(parentBinding+delimiter+count);
 					option.setDefaultValue(""+count);
 					//check if the option still has the default name, if so, update that too.
 					String optionName = option.getText();				
 					try
 					{
 						counterNumberStr = optionName.substring(("Option"+parentBinding+delimiter).length());
 						counterNumber = -999999;
 						counterNumber = Integer.parseInt(counterNumberStr);
 					}
 					catch (Exception e)
 					{
 						
 					}
 					if(optionName.startsWith("Option"+parentBinding+delimiter) &&  counterNumber != -999999)
 					{
 						option.setText("Option"+parentBinding+delimiter+count);
 					}//if name is still default
 				}//if itext is still default			
 			}//loop
 		}
 	}//end updateSelectIds	
 	
 	/**
 	 * This will take in a list of elements
 	 * and delete them from 
 	 * @param selectedElements
 	 * @return return the things that were deleted
 	 */
 	public static List<IFormElement> delete(List<IFormElement> selectedElements)
 	{
 		ArrayList<IFormElement> deletedList = new ArrayList<IFormElement>();
 		//loop over the elements and delete them
 		for(IFormElement element : selectedElements)
 		{
 			//if we're told to delete a form then handle that differently
 			if(element instanceof FormDef)
 			{
 				//check if the user really wants to do this
 				if(FormDesignerController.getIsDirty())
 				{
 					if(!Window.confirm(LocaleText.get("deleteConfirm")))
 					{
 						return deletedList;
 					}
 				}
 				FormDef form = (FormDef)element;
 				//Since other things will point
 				//to this form, so I"m going to null out importing things and
 				//hope someone notices down the line
 				form.setBinding(null);
 				form.setText(null);
 				//null out the context
 				Context.setFormDef(null);
 				//since we just blew away everthing, we can say it's clean
 				FormDesignerController.clearIsDirty();
 				deletedList.add(form);
 				return deletedList;
 			}						
 			//check if we're deleting a question that points to something else
 			else if(element instanceof QuestionDef)
 			{
 				//check if this questions is part of a block
 				String blockId = ((QuestionDef)element).getBlockId();
 				if(blockId != null && !isCutMode)
 				{
 					//check if the user is cool with this
 					String warningMsg = LocaleText.get("WarningQuestion") + " \"" + element.getText() + "\" " + 
 							LocaleText.get("isPartOfBlock") +
 							" \"" + blockId + "\". \r\n\r\n" + 
 							LocaleText.get("BlocksAreAtomicDelete"); 
 					if(!Window.confirm(warningMsg))
 					{
 						continue;
 					}
 				}
 				//now check skip logic and valids
 				//get the form
 				FormDef form = element.getFormDef();
 				//get the skip logic and relevants
 				Vector<SkipRule> skipRules = form.getSkipRules();
 				Vector<ValidationRule> validationRules = form.getValidationRules();
 				String skipWarningMsg = "";
 				//check the skip rules
 				if(skipRules != null)
 				{
 					for(SkipRule s : skipRules)
 					{
 						//make sure we're not checking against ourselves
 						if(s.getActionTarget() == element)
 						{
 							continue;
 						}
 						//check the conditions
 						Vector<Condition> conditions = s.getConditions();
 						//check the conditions
 						for(Condition c : conditions)
 						{
 							//is our question targeted
 							
 							if(c.getQuestion() == element)
 							{
 								if(!skipWarningMsg.equals(""))
 								{
 									skipWarningMsg +=", ";
 								}
 								skipWarningMsg += "\"" + s.getActionTarget().getText() + "\""; 
 								break;
 							}
 						}
 					}//end skip rules
 					//was there any cause for conern
 					if(!skipWarningMsg.equals(""))
 					{
 						skipWarningMsg = LocaleText.get("warning") + " " + 
 								LocaleText.get("question").toLowerCase() + 
 								"\"" + element.getText() + "\"" +
 								LocaleText.get("questedReferedToBySkipLogic") + skipWarningMsg;
 					}//end skip rules
 				}
 				
 				String validWarningMsg = "";
 				//start looking into valids
 				if(validationRules != null)
 				{
 					for(ValidationRule v : validationRules)
 					{
 						//make sure we're not checking against ourself
 						if(v.getQuestion() == element)
 						{
 							continue;
 						}
 						//check the conditions
 						Vector<Condition> conditions = v.getConditions();
 						//check the conditions
 						for(Condition c : conditions)
 						{
 							//is our question targeted
 							if(c.getQuestion() == element)
 							{
 								if(!validWarningMsg.equals(""))
 								{
 									validWarningMsg +=", ";
 								}
 								validWarningMsg += v.getQuestion().getText(); 
 								break;
 							}
 						}
 					}//end validation rules
 					//was there any cause for conern
 					if(!validWarningMsg.equals(""))
 					{
 						validWarningMsg = LocaleText.get("warning") + " " + 
 								LocaleText.get("question").toLowerCase() + 
 								"\"" + element.getText() + "\"" +
 								LocaleText.get("questedReferedToBySkipLogic") + validWarningMsg;
 					}
 					//end valids
 				}
 				//now put it all together
 				String andText = (validWarningMsg.length() != 0 && skipWarningMsg.length() != 0) ? "\r\n" + LocaleText.get("and") + "\r\n " : "";
 				if(validWarningMsg.length() != 0 || skipWarningMsg.length() != 0)
 				{
 					String warningMsg = skipWarningMsg + andText + validWarningMsg + ".\r\n\r\n" + 
 							LocaleText.get("areYouSureYouWantToDelete") + 
 							" " + LocaleText.get("question").toLowerCase() + " \"" + element.getText() + "\"";
 					if(!Window.confirm(warningMsg))
 					{
 						continue;
 					}
 				}
 				
 				
 			}
 			//get the parent of the thing to be deleted.
 			IFormElement parent = element.getParent();
 			//if the parent exists, drop it.
 			if(parent != null)
 			{
 				if(element instanceof DataInstanceDef)
 				{
 					((FormDef)parent).removeDataInstance(((DataInstanceDef)element).getInstanceId());
 				}
 				else
 				{
 					parent.removeChild(element);
 				}
 				deletedList.add(element);
 			}
 			//if we just dropped an option, update the option IDs
 			if(element instanceof OptionDef)
 			{
 				updateSelectIds((QuestionDef)(element.getParent()));
 			}
 		}//end loop
 		FormDesignerController.makeDirty();
 		
 		return deletedList;
 	}//end delete()
 	
 	
 	/**
 	 * This is used to copy selected components to the clipboard
 	 * @param selectedElements
 	 */
 	public static void copy(List<IFormElement> selectedElements)
 	{
 		isCutMode = false;
 		clipboard = selectedElements;
 	}//end copy()
 	
 	
 	public static void cut(List<IFormElement> selectedElements)
 	{
 		//check for blocks
 		ArrayList<IFormElement> elements = new ArrayList<IFormElement>();
 		for(IFormElement e : selectedElements)
 		{
 			if(e instanceof QuestionDef && ((QuestionDef)e).getBlockId() != null)
 			{
 				//check if this questions is part of a block
 				String blockId = ((QuestionDef)e).getBlockId();
 				//check if the user is cool with this
 				String warningMsg = LocaleText.get("WarningQuestion") + " \"" + e.getText() + "\" " + 
 						LocaleText.get("isPartOfBlock") +
 						" \"" + blockId + "\". \r\n\r\n" + 
 						LocaleText.get("BlocksAreAtomicDelete"); 
 				if(!Window.confirm(warningMsg))
 				{
 					continue;
 				}
 			}
 			//if we made it this far, then add the element to the list
 			elements.add(e);
 		}			
 		copy(elements);
 		isCutMode = true;
 	}//end cut()
 	
 	/**
 	 * Used to paste what's in the clipboard at the specified insertPoint
 	 * This version is used when not dealing with the first insertion of blocks
 	 * @return
 	 */
 	public static List<IFormElement> paste(IFormElement insertPoint)
 	{
 		if (isCutMode)
 		{
 			return pasteCut(insertPoint);
 		}
 		else
 		{
 			return paste(insertPoint, false);
 		}
 	}
 	
 	/**
 	 * Used to paste what's in the clipbaord at the specified insertPoint
 	 * This version can be used when inserting blocks for the first time
 	 * @param insertPoint point at which to insert clipboard data
 	 * @param keepBlockId if true we preserve block ids, else we do not
 	 * @return the newly created form elements from the paste.
 	 */
 	public static List<IFormElement> paste(IFormElement insertPoint, boolean keepBlockId)
 	{
 		//init the new IFormElements list
 		ArrayList<IFormElement> retVal = new ArrayList<IFormElement>();
 		
 		//first double check that there's something in the clipboard
 		if(clipboard == null || clipboard.size() == 0)
 		{
 			return retVal;
 		}
 		//make sure that there's an insert point
 		if(insertPoint == null)
 		{
 			return retVal;		
 		}
 		//prepare some look ups for mapping old bindings to new bindings
 		HashMap<String, IFormElement> oldBindingToNew = new HashMap<String,IFormElement>();
 		
 		//loop over the clipboard and start pasting
 		for(IFormElement element : clipboard)
 		{
 			//handle data instances in a special way
 			if(element instanceof DataInstanceDef)
 			{
 				IFormElement newElement = pasteHelper(insertPoint.getFormDef(), element, oldBindingToNew, keepBlockId);
 				if(newElement != null)
 				{
 					retVal.add(newElement);
 				}
 			}
 			//hanlde everything else the same way
 			else
 			{
 				IFormElement newElement = pasteHelper(insertPoint, element, oldBindingToNew, keepBlockId);
 				if(newElement != null)
 				{
 					retVal.add(newElement);
 				}
 			}
 		}
 		
 		//set the form from which the pasted elements came
 		FormDef copyForm = clipboard.get(0).getFormDef();
 		FormDef pasteForm = insertPoint.getFormDef();
 		//only do this next bit if you're not cutting from the same form
 		if(!(copyForm == pasteForm && isCutMode))
 		{
 			//handle skip logic
 			//need a deep copy of the list of skip rules
 			Vector<SkipRule> copySkipRules = new Vector<SkipRule>();
 			if(copyForm.getSkipRules() != null)
 			{
 				for(SkipRule sr : copyForm.getSkipRules())
 				{
 					copySkipRules.add(sr);
 				}
 				if(copySkipRules != null)
 				{
 					for(SkipRule copySkipRule : copySkipRules)
 					{		
 						SkipRule pasteSkipRule = new SkipRule(copySkipRule);
 						//create copy and paste lists of conditions
 						Vector<Condition> copyConditions = copySkipRule.getConditions();
 						Vector<Condition> pasteConditions =  new Vector<Condition>();
 						//need to make sure this skip rule applies to the things we just copied
 						boolean allConditionsAccountedFor = true;
 						//loop over the conditions
 						for(Condition copyCondition : copyConditions)
 						{
 							if(!oldBindingToNew.containsKey(copyCondition.getQuestion().getBinding()))
 							{
 								allConditionsAccountedFor = false;
 								break;
 							}
 							Condition pasteCondition = new Condition(copyCondition);
 							pasteCondition.setQuestion((QuestionDef)(oldBindingToNew.get(copyCondition.getQuestion().getBinding())));
 							pasteConditions.add(pasteCondition);
 						}
 						
 						//if we didn't find all the conditions, the drop this skip rule
 						if(!allConditionsAccountedFor)
 						{
 							break;
 						}
 						//set the conditions
 						pasteSkipRule.setConditions(pasteConditions);
 						
 						
 						//now lets look at targets
 						//at least one of the targets needs to be in what we copied
 						if(copySkipRule.getActionTarget()!= null && oldBindingToNew.containsKey(copySkipRule.getActionTarget().getBinding()))
 						{
 							pasteSkipRule.setActionTarget(oldBindingToNew.get(copySkipRule.getActionTarget().getBinding()));
 						}
 						else //if the targets aren't valid, skip this SkipRul
 						{
 							break;
 						}
 						
 						//set the skip rule
 						pasteForm.addSkipRule(pasteSkipRule);						
 					}//end loop over skip rules
 				}//end if skip rules list isn't null
 			}// end if(copyForm.getSkipRules() != null) 
 			//handle validates
 			
 			Vector<ValidationRule> copyValidationRules = copyForm.getValidationRules();
 			if(copyValidationRules != null)
 			{
 				for(ValidationRule copyValidationRule : copyValidationRules)
 				{		
 					ValidationRule pasteValidationRule = new ValidationRule(copyValidationRule);
 					//create copy and paste lists of conditions
 					Vector<Condition> copyConditions = copyValidationRule.getConditions();
 					Vector<Condition> pasteConditions =  new Vector<Condition>();
 					//need to make sure this skip rule applies to the things we just copied
 					boolean allConditionsAccountedFor = true;
 					//loop over the conditions
 					for(Condition copyCondition : copyConditions)
 					{
 						if(!oldBindingToNew.containsKey(copyCondition.getQuestion().getBinding()))
 						{
 							allConditionsAccountedFor = false;
 							break;
 						}
 						Condition pasteCondition = new Condition(copyCondition);
 						pasteCondition.setQuestion((QuestionDef)(oldBindingToNew.get(copyCondition.getQuestion().getBinding())));
 						pasteConditions.add(pasteCondition);
 					}
 					
 					//if we didn't find all the conditions, the drop this skip rule
 					if(!allConditionsAccountedFor)
 					{
 						break;
 					}
 					//set the conditions
 					pasteValidationRule.setConditions(pasteConditions);
 					
 					
 					//now lets look at the question
 					if(oldBindingToNew.containsKey(copyValidationRule.getQuestion().getBinding()))
 					{
 						pasteValidationRule.setQuestion((QuestionDef)(oldBindingToNew.get(copyValidationRule.getQuestion().getBinding())));
 					}
 					else
 					{
 						break;
 					}
 					pasteForm.addValidationRule(pasteValidationRule);			
 				}// end validation loop
 			}//end validation not null
 			
 			//handle languages
 			//get the copy iText stuff
 			HashMap<String, ItextModel> copyiTextMap = copyForm.getITextMap();
 			//get the paste iText stuff		
 			HashMap<String, ItextModel> pasteiTextMap = pasteForm.getITextMap();
 			//copy the iText over and update the bindings			
 			iTextCopier(clipboard, oldBindingToNew, copyiTextMap, pasteiTextMap);
 			
 			//if there was a language in the block, that's not in the current form, add it
 			//now loop over the languages
 			for(Locale locale : copyForm.getLocales())
 			{
 				
 				if(!Context.hasLocale(locale))
 				{
 					Context.addLocale(locale);
 				}
 			}
 			Context.setLocales(Context.getLocales()); //for locale change notification
 		}//end if its not the same form and cut
 		
 		//handle cut if we need to
 		if(isCutMode)
 		{
 			delete(clipboard);
 			isCutMode = false;
 		}
 		
 		if(retVal.size() == 0)
 		{
 			return null;
 		}
 		return retVal;
 	}//end paste()
 	
 	
 	/**
 	 * Used to paste things that have been cut, when something has been cut
 	 * it should just be simple enough to perform a move, which should be really simple
 	 * @param insertPoint
 	 * @return
 	 */
 	public static List<IFormElement> pasteCut(IFormElement insertPoint)
 	{
 	
 		//init the new IFormElements list, items that were successfully pasted
 		ArrayList<IFormElement> retVal = new ArrayList<IFormElement>();
 		
 		//first double check that there's something in the clipboard
 		if(clipboard == null || clipboard.size() == 0)
 		{
 			return new ArrayList<IFormElement>();
 		}
 		//make sure that there's an insert point
 		if(insertPoint == null)
 		{
 			new ArrayList<IFormElement>();		
 		}
 		
 		//loop over the clipboard and move the elements to their new homes
 		for(IFormElement element : clipboard)
 		{
 			
 			//tell the parent you're leaving
 			IFormElement parent = element.getParent();
 			parent.removeChild(element);
 			
 			//don't support cut'n pasting of the whole form
 			if(element instanceof OptionDef)
 			{
 			
 				if(insertPoint instanceof QuestionDef)
 				{
 					//make sure this question is a list style question
 					if((((QuestionDef)insertPoint).getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
 							((QuestionDef)insertPoint).getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE) &&
 							!((QuestionDef)insertPoint).usesItemSet())
 					{
 						//jump in on this question
 						element.setParent(insertPoint);
 						insertPoint.addChild(element);
 					}
 					else
 					{
 						Window.alert("You can't paste options on a question that doesn't use them. Paste operation ignored.");
 						continue;
 					}
 				}
 				else if(insertPoint instanceof OptionDef)
 				{
 					//hop in with your new brothers little option def					
 					element.setParent(insertPoint.getParent());
 					//what is the index of this option
 					int index = insertPoint.getIndex();
 					//put the new element where the insertPoint is
 					insertPoint.getParent().insert(index, element);
 				}		
 				else
 				{
 					Window.alert("Attempted to paste a select option in a place where select options are not able to go. Paste operation ignored.");
 					continue;
 				}
 			}//end optionDef
 			else if(element instanceof QuestionDef)
 			{
 				//set the parent
 				if(insertPoint instanceof FormDef || insertPoint instanceof GroupDef)
 				{
 					element.setParent(insertPoint);
 					insertPoint.addChild(element);
 					
 				}
 				else if (insertPoint instanceof RepeatQtnsDef || (insertPoint instanceof QuestionDef && ((QuestionDef)insertPoint).getDataType() == QuestionDef.QTN_TYPE_REPEAT))
 				{
 					element.setParent(insertPoint);
 					insertPoint.addChild(element);
 				}
 				else if(insertPoint instanceof QuestionDef)
 				{
 					//set the parent
 					element.setParent(insertPoint.getParent());
 					//what is the index of this option
 					int index = insertPoint.getIndex();
 					//put the new element where the insertPoint is
 					insertPoint.getParent().insert(index, element);
 			
 				}
 				else if(insertPoint instanceof OptionDef)
 				{
 					Window.alert("Attempted to paste a question in a select option. This is not allowed. Paste operation ignored.");
 					continue;
 				}
 				
 				
 			}
 			else if(element instanceof GroupDef)
 			{
 				if(insertPoint instanceof FormDef || insertPoint instanceof GroupDef)
 				{
 
 					element.setParent(insertPoint);
 					insertPoint.addChild(element);
 					
 				}
 				else if (insertPoint instanceof RepeatQtnsDef || (insertPoint instanceof QuestionDef && ((QuestionDef)insertPoint).getDataType() == QuestionDef.QTN_TYPE_REPEAT))
 				{
 					//set the parent
 					element.setParent(insertPoint);
 					//put the new question into the repeat
 					insertPoint.addChild(element);
 				}
 				else if(insertPoint instanceof QuestionDef)
 				{
 					Window.alert("Attempted to paste a group in a place where groups are not able to go. Paste operation ignored.");
 					continue;
 			
 				}
 				else if(insertPoint instanceof OptionDef)
 				{
 					Window.alert("Attempted to paste a group in a place where groups are not able to go. Paste operation ignored.");
 					continue;
 				}							
 				
 			}//end if group def
 			else
 			{
 				Window.alert("Warning, trying to paste a data type we're not prepaired to handle");
 				continue;
 			}
 			
 			//if everything worked out so far, add this to the list of things we return to the user
 			retVal.add(element);
 			
 		}//end big for loop of clipboard items
 		
 		return retVal;
 	}
 	/**
 	 * This is a helper to the paste method that pastes in everything
 	 * @param insertPoint
 	 * @param element
 	 * @param keepBlockId do we preserve block ids or not
 	 * @return
 	 */
 	private static IFormElement pasteHelper(IFormElement insertPoint, IFormElement element, HashMap<String, IFormElement> oldBindingToNew, boolean keepBlockId)
 	{
 		
 		//make sure this hasn't already been loaded
 		if(oldBindingToNew.containsKey(element.getBinding()) && !(element instanceof ItemSetDef || element instanceof DataDefBase))
 		{
 			return null;
 		}
 		
 		//do specific things depending on what kind of object is being pasted
 		IFormElement newElement = null;
 				
 		if(element instanceof FormDef)
 		{
 			//paste the children of the form, not the form itself
 			List<IFormElement> kids = element.getChildren();
 			for(IFormElement kid : kids)
 			{
 				pasteHelper(insertPoint, kid, oldBindingToNew, keepBlockId);
 			}
 			return null;
 		}
 		else if(element instanceof ItemSetDef)
 		{
 			if(insertPoint instanceof QuestionDef && 
 					(((QuestionDef)insertPoint).getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
 					((QuestionDef)insertPoint).getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE))
 			{
 				ItemSetDef itemSet = new ItemSetDef(((ItemSetDef)element));
 				itemSet.setParent(insertPoint);
 				((QuestionDef)insertPoint).addChild(itemSet);
 				newElement = itemSet;
 				
 				//check if any of the predicates now refer to questions that have been copied
 				for(Object o : itemSet.getNodeSet())
 				{
 					if(o instanceof PredicateDef)
 					{
 						PredicateDef pd = (PredicateDef)o;
 						QuestionDef qd = (QuestionDef)(pd.getQuestionDef());
 						if(qd != null)
 						{
 							if(oldBindingToNew.containsKey(pd.getQuestionDef().getBinding()))
 							{
 								pd.setQuestionDef(oldBindingToNew.get(pd.getQuestionDef().getBinding()));
 							}
 						}
 						else
 						{
 							Window.alert(LocaleText.get("missing_question_for_predicate"));
 							return null;
 						}
 					}
 				}
 				
 				//check the data instance
 				DataInstanceDef did = itemSet.getDataInstance();
 				if(oldBindingToNew.containsKey("did_"+did.getInstanceId()))
 				{
 					itemSet.setDataInstance((DataInstanceDef)(oldBindingToNew.get("did_"+did.getInstanceId())));
 				}
 			}
 			else
 			{ //it doesn't really make sense to paste item sets to anything but select questions
 				Window.alert(LocaleText.get("cant_paste_itemset_here"));
 				return null;
 			}
 		}
 		else if(element instanceof OptionDef)
 		{
 			//creat the new option def
 			OptionDef newOption = new OptionDef((OptionDef)element);			
 			newElement = newOption;
 			//set the parent
 			if(insertPoint instanceof QuestionDef)
 			{
 				//make sure we have a binding the IDs this copied option as being
 				//part of the question it is going into
 				newOption.setBinding(insertPoint.getBinding() + "_" + 1);
 				newOption.setItextId(insertPoint.getBinding() + "_" + 1);
 				newOption.setParent(insertPoint);
 				insertPoint.addChild(newOption);
 				updateSelectIds((QuestionDef)insertPoint);
 			}
 			else if(insertPoint instanceof OptionDef)
 			{
 				//make sure we have a binding the IDs this copied option as being
 				//part of the question it is going into
 				newOption.setBinding(insertPoint.getParent().getBinding() + "_" + 1);
 				newOption.setItextId(insertPoint.getParent().getBinding() + "_" + 1);
 				
 				newOption.setParent(insertPoint.getParent());
 				//what is the index of this option
 				int index = insertPoint.getIndex();
 				//put the new element where the insertPoint is
 				insertPoint.getParent().insert(index, newElement);
 				updateSelectIds((QuestionDef)(insertPoint.getParent()));
 			}		
 			else if(insertPoint instanceof DataDefBase)
 			{
 				Window.alert("You can not paste an option into a data element");
 				return null;
 			}
 			else
 			{
 				Window.alert("Attempted to paste a select option in a place where select options are not able to go. Paste operation ignored.");
 				return null;
 			}
 		}//end optionDef
 		else if(element instanceof QuestionDef)
 		{
 			//creat the new question def
 			QuestionDef newQuestion = new QuestionDef((QuestionDef)element, element.getParent());
 			//if we're supposed to preserve the block id, then do it
 			if(keepBlockId)
 			{
 				newQuestion.setBlockId(((QuestionDef)element).getBlockId());
 			}
 			newElement = newQuestion;
 			//zero out kids
 			newQuestion.setChildren(new ArrayList<IFormElement>());
 			//get the form for the place where we're putting this question
 			FormDef form = insertPoint.getFormDef();			
 			//set the parent
 			if(insertPoint instanceof FormDef || insertPoint instanceof GroupDef)
 			{
 				//get a new id
 				String bindStr = getNewQuestionBinding(form, insertPoint, true);
 				newQuestion.setBinding(bindStr);
 				newQuestion.setItextId(bindStr);
 				//set the parent
 				newQuestion.setParent(insertPoint);
 				insertPoint.addChild(newQuestion);
 				
 			}
 			else if (insertPoint instanceof RepeatQtnsDef || (insertPoint instanceof QuestionDef && ((QuestionDef)insertPoint).getDataType() == QuestionDef.QTN_TYPE_REPEAT))
 			{
 				//get a new id
 				String bindStr = getNewQuestionBinding(form, insertPoint, false);
 				newQuestion.setBinding(bindStr);
 				newQuestion.setItextId(bindStr);
 				//set the parent
 				newQuestion.setParent(insertPoint);
 				//put the new question into the repeat
 				insertPoint.addChild(newQuestion);
 			}
 			else if(insertPoint instanceof QuestionDef)
 			{
 				//get a new id
 				String bindStr = getNewQuestionBinding(form, insertPoint, false);
 				newQuestion.setBinding(bindStr);
 				newQuestion.setItextId(bindStr);
 				//set the parent
 				newQuestion.setParent(insertPoint.getParent());
 				//what is the index of this option
 				int index = insertPoint.getIndex();
 				//put the new element where the insertPoint is
 				insertPoint.getParent().insert(index, newQuestion);
 		
 			}
 			else if(insertPoint instanceof OptionDef)
 			{
 				//make the insert point the parent, since we're sitting on an option def
 				insertPoint = insertPoint.getParent();
 				//get a new id
 				String bindStr = getNewQuestionBinding(form, insertPoint, false);
 				newQuestion.setBinding(bindStr);
 				newQuestion.setItextId(bindStr);
 				//set the parent
 				newQuestion.setParent(insertPoint.getParent());
 				//what is the index of this option
 				int index = insertPoint.getIndex();
 				//put the new element where the insertPoint is
 				insertPoint.getParent().insert(index, newQuestion);
 			}
 			else if (insertPoint instanceof DataInstanceDef)
 			{
 				//get the form
 				FormDef formDef = ((DataInstanceDef)insertPoint).getFormDef();
 				newQuestion.setParent(formDef);
 				formDef.addChild(newQuestion);
 			}
 			else if(insertPoint instanceof DataDefBase)
 			{
 				Window.alert("You can not paste a question into a data element");
 				return null;
 			}
 			
 			//check for hints
 			//check if there's a hint
 			String hintStr = newQuestion.getHelpText();
 			if(hintStr != null && hintStr != "")
 			{
 				String hintBinding = ((QuestionDef)element).getBinding()+"-hint";
 				oldBindingToNew.put(hintBinding, newQuestion);
 			}
 			
 		}
 		else if(element instanceof GroupDef)
 		{
 			//creat the new question def
 			GroupDef newGroup = new GroupDef((GroupDef)element, element.getParent());
 			newElement = newGroup;
 			//zero out kids
 			newGroup.setChildren(new ArrayList<IFormElement>());
 			//get the form for the place where we're putting this question
 			FormDef form = insertPoint.getFormDef();			
 			//set the parent
 			if(insertPoint instanceof FormDef || insertPoint instanceof GroupDef)
 			{
 				//get a new id
 				String bindStr = getNewQuestionBinding(form, insertPoint, true);
 				newGroup.setBinding(bindStr);
 				newGroup.setItextId(bindStr);
 				//set the parent
 				newGroup.setParent(insertPoint);
 				insertPoint.addChild(newGroup);
 				
 			}
 			else if (insertPoint instanceof RepeatQtnsDef || (insertPoint instanceof QuestionDef && ((QuestionDef)insertPoint).getDataType() == QuestionDef.QTN_TYPE_REPEAT))
 			{
 				//get a new id
 				String bindStr = getNewQuestionBinding(form, insertPoint, false);
 				newGroup.setBinding(bindStr);
 				newGroup.setItextId(bindStr);
 				//set the parent
 				newGroup.setParent(insertPoint);
 				//put the new question into the repeat
 				insertPoint.addChild(newGroup);
 			}
 			else if(insertPoint instanceof QuestionDef)
 			{
 				Window.alert("Attempted to paste a group in a place where groups are not able to go. Paste operation ignored.");
 				return null;
 		
 			}
 			else if(insertPoint instanceof OptionDef)
 			{
 				Window.alert("Attempted to paste a group in a place where groups are not able to go. Paste operation ignored.");
 				return null;
 			}
 			else if(insertPoint instanceof DataDefBase)
 			{
 				Window.alert("You can not paste a group into a data element");
 				return null;
 			}
 			
 			//check for hints
 			//check if there's a hint
 			String hintStr = newGroup.getHelpText();
 			if(hintStr != null && hintStr != "")
 			{
 				String hintBinding = ((GroupDef)element).getBinding()+"-hint";
 				oldBindingToNew.put(hintBinding, newGroup);
 			}
 			
 		}//end if group def
 
 		else if(element instanceof DataInstanceDef)
 		{
 			FormDef form = insertPoint.getFormDef();
 			DataInstanceDef did = (DataInstanceDef)element;
 			String instanceId = did.getInstanceId();
 			//is this ID already being used
 			if(form.getDataInstances().containsKey(instanceId))
 			{
 				if(Window.confirm(LocaleText.get("data_instance_exists")+ " " + instanceId + LocaleText.get("rename_new_data_instance")))
 				{
 					oldBindingToNew.put("did_"+instanceId, did);
 					instanceId += "_"+LocaleText.get("copy");
 					did.setInstanceId(instanceId);
 				}
 				else
 				{
 					oldBindingToNew.put("did_"+instanceId, form.getDataInstance(instanceId));
 					return null;
 				}
 			}
 			form.addDataInstance(did.getInstanceId(), did);
 			did.setParent(form);
 			return did;
 			
 		}//end if element is a DataInstanceDef
 		else if(element instanceof ItemSetDef)
 		{
 			if(insertPoint instanceof QuestionDef)
 			{
 				QuestionDef parentQ = (QuestionDef)insertPoint;
 				if(parentQ.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || parentQ.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
 				{
 					parentQ.getChildren().clear();
 					ItemSetDef isd = (ItemSetDef)element;
 					isd.setParent(parentQ);
 					parentQ.addChild(isd);
 					newElement = isd;
 				}
 			}
 		}
 		
 		else
 		{
 			Window.alert("Warning, trying to paste a data type we're not prepaired to handle. " + element.toString());
 		}
 		
 		//set the old binding/new binding mapping
 		if(!(element instanceof ItemSetDef || element instanceof DataDefBase))
 		{
 			oldBindingToNew.put(element.getBinding(), newElement);
 		}
 		
 		//recurse just to make sure we've got everything
 		List<IFormElement> kids = element.getChildren();
 		if(kids != null)
 		{
 			for(IFormElement kid : kids)
 			{
 				pasteHelper(newElement, kid, oldBindingToNew, keepBlockId);
 			}
 		}
 		
 		
 		
 		return newElement;
 	}//end pasteHelper()
 
 	/**
 	 * This recurses over all the kids to help copy over new iTextes
 	 * @param elements The elements that need copying over
 	 * @param oldBindingToNew mapping of old bindings to new
 	 * @param copyiTextMap the old itext
 	 * @param pasteiTextMap the new itext
 	 */
 	private static void iTextCopier(List<IFormElement> elements, HashMap<String, IFormElement>oldBindingToNew, HashMap<String, ItextModel> copyiTextMap, HashMap<String, ItextModel> pasteiTextMap)
 	{
 		for(IFormElement element : elements)
 		{
 			if(copyiTextMap.containsKey(element.getBinding()))
 			{
 				ItextModel copyTextModel = copyiTextMap.get(element.getBinding());
 				ItextModel pasteTextModel = new ItextModel(copyTextModel);
 				//copy this to the paste form
 				if(oldBindingToNew.containsKey(element.getBinding()))
 				{
 					pasteiTextMap.put(oldBindingToNew.get(element.getBinding()).getBinding(), pasteTextModel);
 				}
 				//if it's a question check for hints
 				if(element instanceof QuestionDef)
 				{
 					QuestionDef q = (QuestionDef)element;
 					//check if there's a hint
 					String hintStr = q.getHelpText();
 					if(hintStr != null && hintStr != "")
 					{
 						String hintBinding = q.getBinding()+"-hint";
 						if(copyiTextMap.containsKey(hintBinding))
 						{
 							copyTextModel = copyiTextMap.get(hintBinding);
 							pasteTextModel = new ItextModel(copyTextModel);
 							if(oldBindingToNew.containsKey(hintBinding))
 							{
 								pasteiTextMap.put(oldBindingToNew.get(hintBinding).getBinding()+"-hint", pasteTextModel);
 							}
 						}
 					}				
 				}
 				//recurese
 				if(element.getChildren() != null && element.getChildren().size() > 0)
 				{
 					iTextCopier(element.getChildren(), oldBindingToNew, copyiTextMap, pasteiTextMap);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * This function lets other see what's in the clipboard
 	 * @return
 	 */
 	public static List<IFormElement> getClipboard()
 	{
 		return clipboard;
 	}//end getClipboard()
 	
 	
 	/**
 	 * Creates a new data instance and adds it to the form specified
 	 * then returns the new data instance object
 	 * @param formDef The form that we want to add the data instance to
 	 * @return the newly formed data instance
 	 */
 	public static DataInstanceDef addDataInstance(FormDef formDef)
 	{
 		//get a starting point for a unique ID
 		int instanceId = formDef.getDataInstances().size();
 		//iterate over until we find a truly unique ID
 		while(formDef.getDataInstances().containsKey("data_instance_"+instanceId))
 		{
 			instanceId++;
 		}
 		//create the data instance
 		DataInstanceDef retVal = new DataInstanceDef("data_instance_"+instanceId, "data_instance_"+instanceId, formDef);
 		
 		return addDataInstance(formDef, retVal);
 	}//end addDataInstance()
 	
 	
 	/**
 	 * Adds a new data instance and adds it to the form specified
 	 * then returns the new data instance object
 	 * @param formDef The form that we want to add the data instance to
 	 * @return the newly formed data instance
 	 */
 	public static DataInstanceDef addDataInstance(FormDef formDef, DataInstanceDef did)
 	{
 		//get a starting point for a unique ID
 		int instanceCounter = 0;
 		String instanceCounterStr = "";
 		String currentId = did.getInstanceId();
 		
 		//iterate over until we find a truly unique ID
 		while(formDef.getDataInstances().containsKey(did.getInstanceId()))
 		{
 			instanceCounter++;
 			instanceCounterStr = "_" + instanceCounter;
 			did.setBinding(currentId + instanceCounterStr);
 			did.setInstanceId(currentId + instanceCounterStr);
 			did.setRootNodeName(currentId + instanceCounterStr);
 		}		
 		formDef.addDataInstance(did.getInstanceId()+instanceCounterStr, did);
 		FormDesignerController.makeDirty();
 		
 		return did;
 	}//end addDataInstance()
 	
 	
 	/**
 	 * This method is used to add a data element to a form
 	 * @param selectedElement the selected items where the data element should be added
 	 * @return the newly created data element
 	 */
 	public static DataDef addDataElement(IFormElement selectedElement)
 	{
 		//If nothing is selected then something is wrong and we should bounce.
 		if(selectedElement == null){
 			return null;
 		}
 	
 		//figure out what we're dealing with
 		if(selectedElement instanceof DataDefBase)
 		{
 			DataDefBase parentData = (DataDefBase)selectedElement;
 			//figure out how many kids this thing has
 			int id = parentData.getChildCount();
 			//if this is the first child give it a generic name
 			String name = "item";
 			//otherwise get the 'last' child and use that name
 			if(id != 0)
 			{
 				name = ((DataDefBase)(parentData.getChildAt(id-1))).getName();
 			}
 			DataDef newData = new DataDef(name, parentData);
 			//now give it a value and label attribute
 			newData.addAttribute("value", String.valueOf(id+1));
 			newData.addAttribute("label", String.valueOf(id+1));
 			//add the new data to it's parent and vice versa
 			parentData.addChild(newData);
 			newData.setParent(parentData);
 	
 			FormDesignerController.makeDirty();
 			return newData;
 			
 		}
 		else
 		{
 			//sorry, but you can't add data to this element
 			return null;
 		}
 	}//end addDataElement()
 	
 	
 	
 	/**
 	 * This method moves the thingToMove to the newParentOfThingToMove and places it there at index newPostion
 	 * @param thingToMove - The thing that's getting moved around
 	 * @param newParentOfThingToMove - Where we're putting the thing that's getting move around
 	 * @param newPosition - The index of the thing we're moving around
 	 * @return true if the move happened, false otherwise
 	 */
 	public static boolean moveFormObjects(IFormElement thingToMove, IFormElement newParentOfThingToMove, int newPosition)
 	{
 		if (thingToMove == null || newParentOfThingToMove == null)
 		{
 			System.out.println("Something is null in FormHandler.moveFormObjects()");
 			return false;
 		}
 		
 		//check if we're disturbing a block
 		if(thingToMove instanceof QuestionDef && ((QuestionDef)thingToMove).getBlockId() != null)
 		{
 			//check if this questions is part of a block
 			String blockId = ((QuestionDef)thingToMove).getBlockId();
 			//check if the user is cool with this
 			String warningMsg = LocaleText.get("WarningQuestion") + " \"" + thingToMove.getText() + "\" " + 
 					LocaleText.get("isPartOfBlock") +
 					" \"" + blockId + "\". \r\n\r\n" + 
 					LocaleText.get("BlocksAreAtomicDelete"); 
 			if(!Window.confirm(warningMsg))
 			{
 				return false;
 			}
 			((QuestionDef)thingToMove).setBlockId(null);
 		}
 		
 		
 		//remove the thing to move from it's parent
 		if(thingToMove instanceof OptionDef)
 		{
 			((QuestionDef)((OptionDef)thingToMove).getParent()).removeOption((OptionDef)thingToMove);
 		}
 		else
 		{
 			thingToMove.getParent().removeChild(thingToMove);
 		}
 		
 		//set the new parent in the thing getting moved
 		thingToMove.setParent(newParentOfThingToMove);
 		
 		//add the thingToMove to it's new parent
 		if(thingToMove instanceof OptionDef)
 		{
 			((QuestionDef)newParentOfThingToMove).insertOption((OptionDef)thingToMove, newPosition);
 		}
 		else
 		{
 			newParentOfThingToMove.insert(newPosition, thingToMove);
 		}
 		return true;
 	}//end moveFormObjects()
 	
 	/**
 	 * This is used to parse XML and give back a formdef object
 	 * @param xml xml to parse
 	 * @return formDef of the xml
 	 */
 	public static FormDef parseXml(String xml) throws Exception
 	{
 		///now we setup some variables that'll hold stuff for the 
 		//internationalization of the given bit of code
 		// Mapping of itext id's to their form attribute values. eg id=name, form=short
 		HashMap<String, String> formAttrMap = new HashMap<String, String>();
 		
 		// Mapping of ItextModel objects to their ids as they are in the id column
 		// of the grid.
 		HashMap<String, ItextModel> itextMap = new HashMap<String, ItextModel>();
 		
 		// Lost of ItextModel objects as they are shown in the grid.
 		ListStore<ItextModel> itextList = new ListStore<ItextModel>();
 		
 		//clear out the parsers
 		//ItextParser.itextFormAttrList.clear();
 		//ItextBuilder.itextIds.clear();
 		
 		//define the variables that are used to pass info to the context class, if we
 		//were going to use that
 		List<Locale> locales = new ArrayList<Locale>(); //New list of locals as it comes form the parsed xform.
 		Locale defaultLocale = new Locale(null, null);
 		HashMap<String,String> map = new HashMap<String, String>();
 		
 				
 		//now do some parsing for iText
 		Document doc = ItextParser.parse(xml, itextList, formAttrMap, itextMap, locales, defaultLocale, map);
 		
 		
 		FormDef blockFormDef = XformParser.getFormDef(doc);
 		//set the internationalization stuff
 		blockFormDef.setITextList(itextList);
 		blockFormDef.setITextMap(itextMap);
 		blockFormDef.setLocales(locales);
 		
 		return blockFormDef;
 		
 	}//end parseXml()
 	
 	
 	/**
 	 * This is used to insert a form into another form at the position given
 	 * by selectedItem
 	 * @param block form to insert
 	 * @param selectedItem position to insert the form at. 
 	 */
 	public static List<IFormElement> insertBlock(FormDef block, IFormElement selectedItem)
 	{
 		List<IFormElement> retVal = null;
 		
 		//save the clipboard in case the user need it later
 		List<IFormElement> tempClipboard = clipboard;
 		boolean tempCutMode = isCutMode;
 		isCutMode = false;
 		//clear the clipboard
 		clipboard = new ArrayList<IFormElement>();
 		for(IFormElement i : block.getChildren())
 		{
 			clipboard.add(i);
 		}
 		//also add the data elments, if any
 		for(IFormElement i : block.getDataInstances().values())
 		{
 			clipboard.add(i);
 		}
 		//now paste this
 		retVal = FormHandler.paste(selectedItem, true);
 		//now put the clipboard back
 		clipboard = tempClipboard;
 		isCutMode = tempCutMode;
 		return retVal;
 	}//end insertBlock()
 	
 	
 	/**
 	 * Used to update the language of form
 	 * @param newLocale
 	 * @param oldLocale
 	 * @param form
 	 */
 	public static void updateLanguage(Locale newLocale, Locale oldLocale, FormDef form)
 	{
 		HashMap<String, ItextModel> itextMap = form.getITextMap();
 		
 		for(IFormElement kid : form.getChildren())
 		{
 				updateLanguageHelper(newLocale, oldLocale, itextMap, kid);				
 		}
 	}
 
 	
 	/**
 	 * The helper method that recursively does all the work.
 	 * @param locale
 	 * @param itextMap
 	 * @param item
 	 */
 	private static void updateLanguageHelper(Locale newLocale, Locale oldLocale, HashMap<String, ItextModel> itextMap, IFormElement item)
 	{
 
 		if(item instanceof IFormElement )
 		{
 			//make sure it's not an invisible question, since they won't have itext ids
 			//and make sure it's not a 
 			if(!( (item instanceof QuestionDef && !((QuestionDef)item).isVisible())))
 			{
 				updateLanguageHelper2(newLocale, oldLocale, itextMap, item, item);
 			}
 			
 		}
 		
 		//now recurse over the kids
 		List<IFormElement> kids = item.getChildren();
 		if(kids != null)
 		{
 			for(IFormElement kid : kids)
 			{
 				updateLanguageHelper(newLocale, oldLocale, itextMap, kid);
 			}
 		}
 	}
 
 	/**
 	 * Another helper just to keep things nice and modular
 	 * @param newLocale
 	 * @param oldLocale
 	 * @param itextMap
 	 * @param item
 	 * @param obj
 	 */
 	public static void updateLanguageHelper2(Locale newLocale, Locale oldLocale, HashMap<String, ItextModel> itextMap, IFormElement element, Object obj)
 	{
 		ItextModel itext = null;
 		//get the binding for the look up
 		String binding = element.getItextId();
 		if(binding == null)
 		{		
 			return;
 		}
 		//if it doesn't have an itext entry, make one
 		if(!itextMap.containsKey(binding))
 		{
 			itext = new ItextModel();
 			itext.set("id", binding);
 			itextMap.put(binding, itext);
 		}
 		else
 		{
 			itext = itextMap.get(binding);
 		}
 		//handle the old values before we switch over
 		if(oldLocale != null)
 		{
 			String oldLocaleKey = oldLocale.getKey();
 			String oldLocaleName = oldLocale.getName();
 			String  oldValue = element.getText();
 			//System.out.println("Old Locale: " +  oldLocaleKey + " old value: " + oldValue);
 			itext.set(oldLocaleKey, oldValue);
 			itext.set(oldLocaleName, oldValue);
 		}
 		//now update with the latest values
 		String localeKey = newLocale.getKey();
 		String localeName = newLocale.getName();
 		//not sure what's up, but it used to work with the value in .getKey(), not it just works with the value in .getName()
 		//no idea what changed.
 		
 		if(itext.getPropertyNames().contains(localeName))
 		{
 			String newStr = itext.get(localeName);
 			//System.out.println("New Locale: " +  localeKey + " New value: " + newStr);
 			element.setText(newStr);
 		}
 		
 		//check if we're dealing with a question that might have a hint
 		if(element instanceof QuestionDef)
 		{
 			QuestionDef questionDef = (QuestionDef)element;
 			String hintStr = questionDef.getHelpText();
 			if(hintStr != null && hintStr != "")
 			{
 				//get itext for this itextbinding
 				binding = binding + "-hint";
 				if(!itextMap.containsKey(binding))
 				{
 					itext = new ItextModel();
 					itext.set("id", binding);
 					itextMap.put(binding, itext);
 				}
 				else
 				{
 					itext = itextMap.get(binding);
 				}
 				if(oldLocale != null)
 				{
 					String oldLocaleKey = oldLocale.getKey();
 					String oldLocaleName = oldLocale.getName();
 					itext.set(oldLocaleKey, questionDef.getHelpText());
 					itext.set(oldLocaleName, questionDef.getHelpText());
 				}
 				if(itext.getPropertyNames().contains(localeKey))
 				{
 					questionDef.setHelpText((String)itext.get(localeName));
 				}
 			}
 		}		
 	}//end updateLanguageHelper2
 	
 	
 	public static IFormElement handleFormItemChanged(IFormElement formItem) {
 		FormDesignerController.makeDirty();
 
 		if(formItem == null)
 			return null; //How can this happen?
 
 		if(formItem instanceof QuestionDef){
 			QuestionDef element = (QuestionDef)formItem;
 
 			
 			if(element.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || element.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
 			{
 				//check if the binding has changed
 				updateSelectIds(element);			
 			}
 			
 			if(!element.getBinding().startsWith(element.getFormDef().getQuestionStartLetter()))
 			{
 				element.getFormDef().setQuestionStartLetter(element.getBinding().substring(0, 1));
 			}
 		}				
 		return formItem;
 	}//end handleFormItemChange()
 	
 	
 	/**
 	 * Takes in a form def and spits out XML
 	 * @param formDef the input form
 	 * @return the XML of said form
 	 */
 	public static String writeToXML(FormDef formDef)
 	{
 		
 		//need to force the itext stuff to update
 		updateLanguage(Context.getLocale(), Context.getLocale(), formDef);
 		
 		//create the XML Doc
 		Document doc = XMLParser.createDocument();
 		//for debuging purposes, the Eclipse GWT debugger shows the result of the toString method, and this makes
 		//sure that is pretty
 		DebugDoc debugDoc = new DebugDoc(doc);
 		
 		//add in the version and UTF stuff
 		doc.appendChild(doc.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\""));
 		
 		//now add an HTML node
 		Element htmlNode = doc.createElement("h:html");
 		//add the html node to the document
 		doc.appendChild(htmlNode);
 		//add the head node
 		Element headNode =  doc.createElement("h:head");
 		htmlNode.appendChild(headNode);
 		//add title
 		Element titleNode =  doc.createElement("h:title");
 		titleNode.appendChild(doc.createTextNode(formDef.getName()));
 		//add the iText reference here too, we'll create the iText data a little later on
 		titleNode.setAttribute(XformConstants.ATTRIBUTE_NAME_REF, "jr:itext('"+formDef.getBinding()+"')");
 		//add the title node to the head
 		headNode.appendChild(titleNode);
 		//create the body node
 		Element bodyNode =  doc.createElement("h:body");
 		//add the body to the HTML node
 		htmlNode.appendChild(bodyNode);
 		//create the model node
 		Element modelNode =  doc.createElement(XformConstants.NODE_NAME_MODEL);
 		//add the model node to the head
 		headNode.appendChild(modelNode);
 		//create the instance node
 		Element instanceNode = doc.createElement(XformConstants.NODE_NAME_INSTANCE);
 		//add the isntance to the model
 		modelNode.appendChild(instanceNode);
 		
 		/** Now we'll startup the iText Node. This is a little different because we'll do a fair bit of initailization here*/
 		//create the iText Node
 		Element iTextNode = doc.createElement(XformConstants.NODE_NAME_ITEXT);
 		//create the iText entry for the form title
 		//create a list of translation nodes
 		List<Element> iTextNodes = new ArrayList<Element>();
 		//loop over languages
 		for(Locale locale : formDef.getLocales())
 		{
 			//create the translation node
 			Element localeNode = doc.createElement(XformConstants.NODE_NAME_TRANSLATION);
 			//add that puppy to our list
 			iTextNodes.add(localeNode);
 			//set the language attribute
 			localeNode.setAttribute(XformConstants.ATTRIBUTE_NAME_LANG, locale.getName());
 			//create the text for the form title
 			Element titleTextNode = doc.createElement(XformConstants.NODE_NAME_TEXT);
 			//set the id
 			titleTextNode.setAttribute(XformConstants.ATTRIBUTE_NAME_ID, formDef.getBinding());
 			//create the value node for the form title
 			Element titleValueNode = doc.createElement(XformConstants.NODE_NAME_VALUE);
 			//set the text itself
 			titleValueNode.appendChild(doc.createTextNode(formDef.getName()));
 			//now add the value node to the text node
 			titleTextNode.appendChild(titleValueNode);
 			//add the text node to the language node
 			localeNode.appendChild(titleTextNode);
 			//add the locale node to the iText node
 			iTextNode.appendChild(localeNode);
 			
 		}
 		
 		
 		//recusivley call the elements of the form.
 		
 		formDef.writeToXML(doc, instanceNode, modelNode, iTextNodes, bodyNode);
 		
 		//add the itext node to the head node
 		modelNode.appendChild(iTextNode);
 		
 		//not sure why you do this at the end, but it's what was done back in the day
 		doc.getDocumentElement().setAttribute("xmlns:jr","http://openrosa.org/javarosa");
		doc.getDocumentElement().setAttribute("xmlns","http://www.w3.org/1999/xhtml");
		doc.getDocumentElement().setAttribute("xmlns:h","http://www.w3.org/1999/xhtml");
 		//our very own name space used by the block id attribute
		doc.getDocumentElement().setAttribute("xmlns:kobo","http://www.kobotoolbox.org/xmlns");
 		doc.getDocumentElement().setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
 		
 		
 		//for debug use only
 		for(int j = 0; j < 20; j++)
 		{
 			//System.out.println();
 		}
 		//System.out.println(debugDoc.toString());
 		//end for debug use
 		
 		return debugDoc.toString();
 	}
 	
 	/**
 	 * Simple getter to know if we're cutting or pasting
 	 * @return 
 	 */
 	public static boolean getIsCutMode()
 	{
 		return isCutMode;
 	}
 }

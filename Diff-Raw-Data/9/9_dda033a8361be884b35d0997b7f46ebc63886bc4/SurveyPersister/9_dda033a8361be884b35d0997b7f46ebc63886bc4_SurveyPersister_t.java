 /*
 *  Nokia Data Gathering
 *
 *  Copyright (C) 2011 Nokia Corporation
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/
 */
 
 package controllers.logic;
 
 import controllers.exceptions.SurveySavingException;
 import models.QuestionOption;
 import models.QuestionType;
 import models.Question;
 import models.Survey;
 import models.NdgUser;
 import controllers.util.XFormsTypeMappings;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Vector;
 import javax.persistence.EntityManager;
 import models.Category;
 import org.javarosa.core.model.FormDef;
 import org.javarosa.core.model.GroupDef;
 import org.javarosa.core.model.IFormElement;
 import org.javarosa.core.model.QuestionDef;
 import org.javarosa.core.model.SelectChoice;
 import org.javarosa.core.model.condition.Condition;
 import org.javarosa.core.model.instance.TreeElement;
 import org.javarosa.core.model.instance.TreeReference;
 import org.javarosa.core.services.locale.Localizer;
 import org.javarosa.xform.parse.XFormParser;
 import org.javarosa.xpath.XPathConditional;
 
 public class SurveyPersister {
 
     private InputStreamReader is = null;
     private Survey survey = null;
     private Category defCategory = null;
     private int currentQuestionIndex = 0;
 
     public SurveyPersister(InputStreamReader is) {
         this.is = is;
     }
 
     public void saveSurvey() throws SurveySavingException
     {
         saveSurvey(null);
     }
 
     public void saveSurvey(String surveyId) throws SurveySavingException {
         XFormParser parser = new XFormParser(is);
         FormDef formDefinition = parser.parse();
         Survey newSurvey = findOrCreateSurvey(surveyId, formDefinition);
         persistSurvey(formDefinition, newSurvey);
         persistChildSet(formDefinition, formDefinition, defCategory);
         persistTriggerables( formDefinition );
        if ( defCategory.questionCollection == null || defCategory.questionCollection.size() == 0 ) {
            defCategory.delete();
        }
     }
 
     private void persistSurvey(FormDef formDefinition, Survey newSurvey) throws SurveySavingException {
         NdgUser owner = getOwner(new Long(1));//todo other owners than admin
         newSurvey.ndgUser = owner;
         newSurvey.uploadDate = new Date();
         newSurvey.available = 0;
 
         String locale = formDefinition.getLocalizer().getDefaultLocale();
         if (locale == null) {
             String[] locales = formDefinition.getLocalizer().getAvailableLocales();
             if (locales != null && locales.length > 0) {
                 locale = locales[0];
             }
         }
 
         newSurvey.lang = locale;
 
         defCategory = new Category();
         defCategory.survey = newSurvey;
         defCategory.label = "Default";
         defCategory.objectName = "DefaultCategory";
         defCategory.categoryIndex  = 0;
        defCategory.questionCollection = new ArrayList<Question>();
 
         newSurvey.categoryCollection = new ArrayList<Category>();
 
         newSurvey.save();
        defCategory.save();
         survey = newSurvey;
     }
 
     private void persistChildSet(FormDef formDefinition, IFormElement currentNode, Category questionCategory) throws SurveySavingException {
         Vector /* <IFormElement> */ elementChildren = currentNode.getChildren();
         for (int i = 0; i < elementChildren.size(); i++) {
             if (elementChildren.get(i) instanceof QuestionDef) {
                 QuestionDef question = (QuestionDef) elementChildren.get(i);
                 persistQuestion(question, formDefinition, questionCategory);
             } else if(elementChildren.get(i) instanceof GroupDef) {
                 GroupDef group = (GroupDef) elementChildren.get(i);
                 TreeElement groupModelElement = formDefinition.getInstance().resolveReference(group.getBind()).getChildAt(i);
                 persistCategory(group, formDefinition, groupModelElement);
             }
         }
     }
 
     private void persistQuestion(QuestionDef questionDef, FormDef formDefinition, Category questionCategory)
             throws SurveySavingException {
         TreeElement questionModel = formDefinition.getInstance().resolveReference(questionDef.getBind());
 
         String questionText = questionDef.getLabelInnerText();
         if (questionText == null) {
             formDefinition.getLocalizer().setLocale("eng");// TODO
             questionText = formDefinition.getLocalizer().getLocalizedText(questionDef.getTextID());
         }
 
         String questionHint = questionDef.getHelpText();
         if (questionHint == null && questionDef.getHelpTextID() != null) {
             questionHint = formDefinition.getLocalizer().getLocalizedText(questionDef.getHelpTextID());
         }
 
         QuestionType type = findQuestionType(questionModel.dataType, questionDef.getControlType());
 
         Question newQuestion = new Question();
         newQuestion.category = questionCategory;
         newQuestion.questionType = type;
         newQuestion.objectName = questionModel.getName();
         newQuestion.label = questionText;
         newQuestion.hint = questionHint;
         if (questionModel.getConstraint() != null) {
             XPathConditional expression = (XPathConditional) questionModel.getConstraint().constraint;
             newQuestion.constraintText = expression.xpath;
         }
         newQuestion.required = questionModel.required ? new Integer(1) : new Integer(0);
 
         newQuestion.readonly = questionModel.isEnabled() ? new Integer(0) : new Integer(1);
         newQuestion.questionIndex = currentQuestionIndex;
 
         newQuestion.save();
         questionCategory.questionCollection.add( newQuestion );
         questionCategory.save();
 
         currentQuestionIndex ++;
 
         if (type.typeName.equals(XFormsTypeMappings.SELECT)
                 || type.typeName.equals(XFormsTypeMappings.SELECTONE)) {
             persistQuestionOptions(questionDef, newQuestion, formDefinition.getLocalizer());
         }
     }
 
     private void persistTriggerables( FormDef form ){
         for(Object trigger : form.triggerables ){
             Condition condition = (Condition)trigger;
             String expr = ( ( XPathConditional )condition.expr ).xpath;
 
             for(Object target : condition.getTargets() ){
                 Question q = findQuestionByReference( ( TreeReference )target );
                 if(q != null ){
                     q.relevant = expr;
                     q.save();
                 }
             }
         }
     }
 
     private Question findQuestionByReference( TreeReference ref ){
         Question q = null;
 
         if(ref.size() < 2 ){
             return null;
         }
 
         for( Category category : survey.categoryCollection ){
             if( category.objectName.equals( ref.getName( ref.size() - 2 ) ) ){
                 for ( Question question : category.questionCollection ){
                     if( question.objectName.equals( ref.getName( ref.size() - 1 ) ) ){
                         q = question;
                         break;
                     }
                 }
                 break;
             }
         }
 
         return q;
     }
 
     private QuestionType findQuestionType(int dataType, int controlType) {
         EntityManager em = null;
         QuestionType retval = null;
         {
             StringBuilder controlTypeDecoded = new StringBuilder(XFormsTypeMappings.getIntegerToType(dataType));
             if (controlTypeDecoded.toString().equals("binary")) {
                 switch (controlType) {
                     case org.javarosa.core.model.Constants.CONTROL_AUDIO_CAPTURE:
                         controlTypeDecoded.append("#audio");
                         break;
                     case org.javarosa.core.model.Constants.CONTROL_VIDEO_CAPTURE:
                         controlTypeDecoded.append("#video");
                         break;
                     case org.javarosa.core.model.Constants.CONTROL_IMAGE_CHOOSE:
                         controlTypeDecoded.append("#image");
                         break;
                     default:
                         break;
                 }
             }
             retval = QuestionType.find("byTypeName", controlTypeDecoded.toString()).first();
         }
         return retval;
     }
 
     private void persistCategory(GroupDef groupDef, FormDef formDefinition, TreeElement groupModelElement) throws SurveySavingException {
 
         Category newCategory = new Category();
         newCategory.categoryIndex = survey.categoryCollection.size();
         newCategory.survey = survey;
         newCategory.objectName = groupModelElement.getName();
         newCategory.questionCollection = new ArrayList<Question>();
 
         String groupLabel = groupDef.getLabelInnerText();
         if (groupLabel == null) {
             formDefinition.getLocalizer().setLocale("eng");// TODO
             groupLabel = formDefinition.getLocalizer().getLocalizedText(groupDef.getTextID());
         }
         newCategory.label = groupLabel;
         newCategory.save();
         survey.categoryCollection.add(newCategory);
 
         persistChildSet(formDefinition, groupDef, newCategory);
     }
 
     private NdgUser getOwner(Long userId) {
         NdgUser retval = null;
         retval = NdgUser.find("byId", userId).first();
         return retval;
     }
 
     private void persistQuestionOptions(QuestionDef questionDef, Question newQuestion, Localizer localizer)
             throws SurveySavingException {
         Vector<SelectChoice> choices = questionDef.getChoices();
         for (SelectChoice selectChoice : choices) {
 
             String label = selectChoice.getLabelInnerText();
             if (label == null) {
                 label = localizer.getLocalizedText(selectChoice.getTextID());
             }
 
             QuestionOption option = new QuestionOption();
             option.optionIndex = selectChoice.getIndex();
             option.label = label;
             option.optionValue = selectChoice.getValue();
             option.question = newQuestion;
 
             option.save();
         }
     }
 
     private Survey findOrCreateSurvey(String surveyId, FormDef formDefinition) {
         Survey retval = null;
         if(surveyId == null)
         {
             retval = new Survey(formDefinition.getInstance().getRoot().getAttributeValue("", "id"),
                  formDefinition.getName());
         }
         else
         {
             retval = Survey.find("bySurveyId", surveyId).first();
             retval.delete();
             retval = new Survey(surveyId, formDefinition.getName());
         }
         return retval;
     }
 }

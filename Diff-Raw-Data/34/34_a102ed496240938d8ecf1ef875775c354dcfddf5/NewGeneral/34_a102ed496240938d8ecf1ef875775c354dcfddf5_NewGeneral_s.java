 package com.preppa.web.pages.contribution.question.general;
 
 import com.preppa.web.components.questiontypes.gridin.NewGridin;
 import com.preppa.web.components.questiontypes.longpassage.NewDualLongPassage;
 import com.preppa.web.components.questiontypes.longpassage.NewLongPassage;
 import com.preppa.web.components.questiontypes.multichoice.NewMultiChoice;
 import com.preppa.web.components.questiontypes.prompt.NewPrompt;
 import com.preppa.web.components.questiontypes.shortpassage.NewDualShortPassage;
 import com.preppa.web.components.questiontypes.shortpassage.NewShortPassage;
 import com.preppa.web.data.QuestiontypeDAO;
 import com.preppa.web.data.TagDAO;
 import com.preppa.web.data.TestsubjectDAO;
 import com.preppa.web.entities.Questiontype;
 import com.preppa.web.entities.Testsubject;
 import com.preppa.web.entities.User;
 import com.preppa.web.utils.InjectSelectionModel;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.tapestry5.Block;
 import org.apache.tapestry5.annotations.AfterRender;
 import org.apache.tapestry5.annotations.ApplicationState;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
 import org.apache.tapestry5.annotations.IncludeStylesheet;
 import org.apache.tapestry5.annotations.Mixins;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.corelib.components.Select;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.json.JSONArray;
 import org.apache.tapestry5.json.JSONObject;
 import org.chenillekit.tapestry.core.components.Hidden;
 import org.springframework.security.annotation.Secured;
 
 /**
  *
  * @author nwt
  */
 @Secured("ROLE_USER")
 @IncludeStylesheet(value = {"context:styles/question.css"})
 @IncludeJavaScriptLibrary(value = {"context:js/question.js", "context:js/confirmexitoff.js"})
 public class NewGeneral {
 
     @ApplicationState
     private User user;
     @InjectSelectionModel(labelField = "name", idField = "id")
     private List<Testsubject> testsubjects = new ArrayList<Testsubject>();
     @InjectSelectionModel(labelField = "name", idField = "id")
     private List<Questiontype> questiontypes = new ArrayList<Questiontype>();
     @Property
     @Persist
     private Testsubject testsubject;
     @Property
     @Persist
     private Questiontype questiontype;
     @Inject
     private TestsubjectDAO testSubDAO;
     @Inject
     private QuestiontypeDAO questiontypeDAO;
     //@Inject
     //@Property
     //private Block multiplequesblock;
     /**Components**/
     @Component(parameters = {"value=testsubject", "event=change",
         "onCompleteCallback=literal:onChangeTestsubject"})
     @Mixins({"ck/OnEvent"})
     private Select testSubSelect;
     @Component(parameters = {"value=questiontype", "event=change",
         "onCompleteCallback=literal:onChangeQuestiontype"})
     @Mixins({"ck/OnEvent"})
     private Select QuestiontypeSelect;
     @Component
     private Form wizardform;
     @Property
     private String fname;
     @Property
     private List<Testsubject> subjects;
     /** Questiontype Components **/
     @Component
     private NewMultiChoice aquestion;
     @Component
     private NewDualShortPassage newshortdualpassage;
     @Component
     private NewShortPassage newshortpassage;
     @Component
     private NewGridin newgridin;
     @Component
     private NewLongPassage newlongpassage;
     @Component
     private NewDualLongPassage newlongdualpassage;
     @Component
     private NewPrompt newprompt;
     @Inject
     private TagDAO tagDAO;
     @Property
     @Persist
     private String visiblequestiontype;
     @Component
     private Hidden vhidden;
    
     //@Property
     //@Inject
     //private Block longpassageblock;
     //@Property
     //@Inject
     //private Block longdualpassageblock;
     //@Inject
     //private Block firstblock;
 
     void onActivate() {
         testsubjects = testSubDAO.findAllWithQuestions();
         questiontypes = questiontypeDAO.findAll();
        
     }
     /*
     void onValidateForm() {
         System.out.println(visiblequestiontype);
          if (questiontype.getName().equals("Multiple Choice")) {
             visiblequestiontype = "multichoice";
 
         } else if (questiontype.getName().equals("Long Passage")) {
 
             visiblequestiontype = "longpassage";
 
         } else if (questiontype.getName().equals("Long Dual Passage")) {
 
             visiblequestiontype = "longdualpassage";
 
         } else if (questiontype.getName().equals("Short Dual Passage")) {
             visiblequestiontype = "shortdualpassage";
 
         } else if (questiontype.getName().equals("Short Passage")) {
 
             visiblequestiontype = "shortpassage";
 
         } else if (questiontype.getName().equals("Grid In")) {
             visiblequestiontype = "gridin";
 
         }
         else {
             visiblequestiontype = "multichoice";
 
         }
         
     }
 */
     JSONObject onChangeFromTestSubSelect(String testId) {
         JSONObject json = new JSONObject();
 
         JSONArray ids = new JSONArray();
         JSONArray qt = new JSONArray();
         JSONArray counter = new JSONArray();
 
         if (testId != null && !testId.equals("")) {
             questiontypes = questiontypeDAO.findByTestsubject(testsubjects.get(Integer.parseInt(testId) - 1));
             testsubject = testsubjects.get(Integer.parseInt(testId) - 1);
 
             ids.put("");
             qt.put("");
             int i = 1;
             System.out.println("Counter is " + questiontypes.size());
             for (Questiontype t : questiontypes) {
                 qt.put(i, t.getName());
                 ids.put(i, t.getName());
                 counter.put(new Integer(i).toString());
                 i++;
             }
             System.out.println("Counter is " + i);
         }
         json.put("ids", ids);
         json.put("qt", qt);
         json.put("counter", counter);
         //return new TextStreamResponse("text/json", json.toString());
         return json;
     }
 
     JSONObject onChangeFromQuestiontypeSelect(String quesType) {
         JSONObject json = new JSONObject();
         
         if (quesType.equals("Multiple Choice")) {
             json.put("type", "multichoice");
             visiblequestiontype = "multichoice";
             questiontype = questiontypeDAO.findByName(quesType);
             aquestion.setSubject(testsubject);
         } else if (quesType.equals("Long Passage")) {
             json.put("type", "longpassage");
             visiblequestiontype = "longpassage";
             questiontype = questiontypeDAO.findByName(quesType);
         } else if (quesType.equals("Long Dual Passage")) {
             json.put("type", "longdualpassage");
             visiblequestiontype = "longdualpassage";
             questiontype = questiontypeDAO.findByName(quesType);
         } else if (quesType.equals("Short Dual Passage")) {
             json.put("type", "shortdualpassage");
             visiblequestiontype = "shortdualpassage";
             questiontype = questiontypeDAO.findByName(quesType);
         } else if (quesType.equals("Short Passage")) {
             json.put("type", "shortpassage");
             visiblequestiontype = "shortpassage";
             questiontype = questiontypeDAO.findByName(quesType);
         } else if (quesType.equals("Grid In")) {
             visiblequestiontype = "gridin";
             json.put("type", "gridin");
             questiontype = questiontypeDAO.findByName(quesType);
             newgridin.setSubject(testsubject);
         }
         else if(quesType.equals("Free Response Question")) {
             visiblequestiontype = "newprompt";
              json.put("type", "newprompt");
              json.put("title", "Create Prompt");
             questiontype = questiontypeDAO.findByName(quesType);
         }
          else if(quesType.equals("Sentence Completion")) {
             visiblequestiontype = "Sentence Completion";
              json.put("type", "multichoice");
              json.put("title", "Sentence Completion");
             questiontype = questiontypeDAO.findByName(quesType);
         }
         else {
             json.put("type", "multichoice");
             json.put("title", quesType);
             visiblequestiontype = "multichoice";
             questiontype = questiontypeDAO.findByName(quesType);
             aquestion.setSubject(testsubject);
         }
         
         return json;
 
     }
 
     Block onActionFromTestSelect() {
 
         return null;
     }
 
 
 }

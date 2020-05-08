 package uk.ac.cam.caret.rsf.testcomponents.producers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import uk.ac.cam.caret.rsf.testcomponents.beans.ComponentChoiceManager;
 import uk.ac.cam.caret.rsf.testcomponents.beans.DataBean;
 import uk.org.ponder.arrayutil.ListUtil;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.evolvers.DateInputEvolver;
 import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
 import uk.org.ponder.rsf.evolvers.SelectEvolver;
 import uk.org.ponder.rsf.evolvers.TextInputEvolver;
 import uk.org.ponder.rsf.flow.ARIResult;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.DefaultView;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.stringutil.StringList;
 
 /**
  * Demonstrates modular RSF components.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 
 public class IndexProducer implements ViewComponentProducer, DefaultView,
     NavigationCaseReporter {
 
   public static final String VIEW_ID = "test-components-index";
   private DateInputEvolver dateevolver1;
   private FormatAwareDateInputEvolver dateevolver2;
   private TextInputEvolver textevolver;
   private DataBean databean;
   private ComponentChoiceManager choicebean;
   private Locale locale;
   private SelectEvolver selectevolver;
 
   public String getViewID() {
     return VIEW_ID;
   }
 
   public void setDateEvolver1(DateInputEvolver dateevolver1) {
     this.dateevolver1 = dateevolver1;
   }
 
   public void setDateEvolver2(FormatAwareDateInputEvolver dateevolver2) {
     this.dateevolver2 = dateevolver2;
   }
 
   public void setTextEvolver(TextInputEvolver textevolver) {
     this.textevolver = textevolver;
   }
 
   public void setSelectEvolver(SelectEvolver selectevolver) {
     this.selectevolver = selectevolver;
   }
   
   // This would not be injected in an OTP implementation
   public void setDataBean(DataBean databean) {
     this.databean = databean;
   }
 
   public void setChoiceBean(ComponentChoiceManager choicebean) {
     this.choicebean = choicebean;
   }
 
   public void setLocale(Locale locale) {
     this.locale = locale;
   }
 
   public void fillComponents(UIContainer tofill, ViewParameters viewparamso,
       ComponentChecker checker) {
 
     UIForm cform = UIForm.make(tofill, "components-form");
 
     UIInput date1 = UIInput.make(cform, "date-1:", "#{dataBean.date1}");
     dateevolver1.evolveDateInput(date1, databean.date1);
 
     UIBranchContainer branchtest = UIBranchContainer.make(cform, "branch-test:");
     
     UIInput date2 = UIInput.make(branchtest, "date-2:", "#{dataBean.date2}");
     dateevolver2.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
     dateevolver2.evolveDateInput(date2, databean.date2);
 
     UIInput text = UIInput.make(cform, "rich-text:", "#{dataBean.text}");
     textevolver.evolveTextInput(text);
 
     UISelect select = UISelect.makeMultiple(cform, "select:", 
         new String[] {"Baboon", "Marmoset", "Wensleydale", "Tamarin", "Cheddar", "Yarg", "Macaque", "Colobus"},
         "#{dataBean.selections}",
         new String[] {"Marmoset", "Tamarin", "Cheddar", "Macaque"});
     selectevolver.evolveSelect(select);
     
     UICommand.make(cform, "submit", "#{dataBean.update}");
 
     UIForm tform = UIForm.make(tofill, "text-select-form");
     StringList texts = choicebean.getTextEvolvers();
     makeEvolveSelect(tform, texts, "text-select",
         "#{componentChoice.textEvolverIndex}");
 
     UIForm dform = UIForm.make(tofill, "date-select-form");
 
     StringList dates = choicebean.getDateEvolvers();
     makeEvolveSelect(dform, dates, "date-select",
         "#{componentChoice.dateEvolverIndex}");
 
     UIForm sform = UIForm.make(tofill, "select-select-form");
     
     StringList selects = choicebean.getSelectEvolvers();
     makeEvolveSelect(sform, selects, "select-select",
         "#{componentChoice.selectEvolverIndex}");
     
     UIForm lform = UIForm.make(tofill, "locale-select-form");
 
     Locale[] s = Locale.getAvailableLocales();
     String[] locnames = new String[s.length];
     for (int i = 0; i < s.length; ++i) {
       locnames[i] = s[i].toString();
     }
     UISelect.make(lform, "locale-select", locnames,
         "#{localeSetter.locale}", locale.toString());
     UICommand.make(lform, "submit-locale");
    
    UIInternalLink.make(tofill, "testsingle", new SimpleViewParameters(SingleProducer.VIEW_ID));
   }
 
   private void makeEvolveSelect(UIForm tform, StringList texts,
       String selectID, String EL) {
     int ctexts = texts.size();
     String[] choices = new String[ctexts];
     String[] choicenames = new String[ctexts];
 
     for (int i = 0; i < ctexts; ++i) {
       choices[i] = Integer.toString(i);
       choicenames[i] = texts.stringAt(i);
     }
     UISelect.make(tform, selectID, choices, choicenames, EL, null);
     UICommand.make(tform, "submit-" + selectID);
 
   }
 
   public List reportNavigationCases() {
     return ListUtil.instance(new NavigationCase("updated", new SimpleViewParameters(
         ResultsProducer.VIEW_ID), ARIResult.FLOW_ONESTEP));
   }
 
 }

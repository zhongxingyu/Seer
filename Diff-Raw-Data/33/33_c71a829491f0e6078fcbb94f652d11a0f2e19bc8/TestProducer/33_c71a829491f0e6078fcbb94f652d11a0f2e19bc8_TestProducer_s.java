 /*
  * Created on 8 Jan 2008
  */
 package uk.org.ponder.rsf.test.render;
 
 import uk.org.ponder.rsf.bare.RequestLauncher;
 import uk.org.ponder.rsf.components.ELReference;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIInitBlock;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIOutputMany;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 public class TestProducer implements ViewComponentProducer {
 
   public String getViewID() {
     return RequestLauncher.TEST_VIEW;
   }
 
   public void fillComponents(UIContainer tofill, ViewParameters viewparams,
       ComponentChecker checker) {
     UIVerbatim.make(tofill, "verbatim-test", "verbatim.label").setMessageKey();
 
     UIMessage.make(tofill, "message-holder", "message.key",
         new Object[] { new ELReference("testString") });
 
     for (int i = 0; i < 2; ++i) {
       UIBranchContainer branch = UIBranchContainer.make(tofill, "row:");
       UIOutput.make(branch, "target");
     }
     String[] choices = {"1", "2", "3", "4", "5"};
     UISelect select = UISelect.make(tofill, "select-1", choices, choices, "2", false);
     select.groupnames = UIOutputMany.make(new String[] {null, "", "A", "A"});
     
     UISelect select2 = UISelect.make(tofill, "select-2", choices, choices, "2", false);
     select2.groupnames = UIOutputMany.make(new String[] {"B", "B", "B", "B", "B"});
     
     UIInitBlock.make(tofill, "init-block", "init-select", 
         new Object[] {select2.selection.getFullID()});
   }
 }

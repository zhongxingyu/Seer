 /**
  * 
  */
 package com.alertscape.wizard.client;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 import com.google.gwt.user.client.ui.TreeListener;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author josh
  * 
  */
 public class Wizard extends Composite implements WizardStateListener {
 
   private List<WizardStep> steps;
   private SimplePanel stepContentPanel;
   private Tree stepsTree;
   private Map<TreeItem, WizardStep> treeItemToStep;
   private int currentStep = -1;
   private Button prevButton;
   private Button nextButton;
 
   public Wizard(List<WizardStep> steps) {
     VerticalPanel mainPanel = new VerticalPanel();
     initWidget(mainPanel);
 
     nextButton = new Button("Next", new ClickListener() {
       public void onClick(Widget sender) {
        if(currentStep == Wizard.this.steps.size()-1) {
          // Finish
          return; 
        }
         currentStep++;
         TreeItem item = stepsTree.getItem(currentStep);
         stepsTree.setSelectedItem(item);
       }
     });
     
     prevButton = new Button("Previous", new ClickListener() {
       public void onClick(Widget sender) {
         currentStep--;
         TreeItem item = stepsTree.getItem(currentStep);
         stepsTree.setSelectedItem(item);
       }
     });
 
     stepContentPanel = new SimplePanel();
     stepsTree = new Tree();
     stepsTree.addTreeListener(new WizardTreeListener());
     
     HorizontalPanel contentPanel = new HorizontalPanel();
 
     contentPanel.add(stepsTree);
 
     DecoratorPanel contentDecorator = new DecoratorPanel();
     contentDecorator.setWidget(stepContentPanel);
 
     contentPanel.add(contentDecorator);
 
     mainPanel.add(contentPanel);
     
     HorizontalPanel buttonPanel = new HorizontalPanel();
     buttonPanel.add(prevButton);
     buttonPanel.add(nextButton);
     
     mainPanel.add(buttonPanel);
 
     treeItemToStep = new HashMap<TreeItem, WizardStep>();
     setSteps(steps);
 
     stepsTree.setSelectedItem(stepsTree.getItem(0));
   }
 
   /**
    * @return the steps
    */
   public List<WizardStep> getSteps() {
     return steps;
   }
 
   /**
    * @param steps
    *          the steps to set
    */
   public void setSteps(List<WizardStep> steps) {
     this.steps = steps;
     if (this.steps != null) {
       for (int i = 0; i < steps.size(); i++) {
         WizardStep wizardStep = steps.get(i);
         wizardStep.getContent().setWizardStateListener(this);
         TreeItem treeItem = stepsTree.addItem((i + 1) + ". " + wizardStep.getLabel());
         treeItemToStep.put(treeItem, wizardStep);
       }
     } else {
       stepsTree.removeItems();
     }
     currentStep = 0;
   }
 
   /**
    * @return the contentPanel
    */
   public SimplePanel getStepContentPanel() {
     return stepContentPanel;
   }
 
   /**
    * @param contentPanel
    *          the contentPanel to set
    */
   public void setStepContentPanel(SimplePanel contentPanel) {
     this.stepContentPanel = contentPanel;
   }
 
   public void handeNotProceedable() {
     nextButton.setEnabled(false);
   }
 
   public void handleProceedable() {
     nextButton.setEnabled(true);
   }
 
   /**
    * @author josh
    * 
    */
   private final class WizardTreeListener implements TreeListener {
     public void onTreeItemSelected(TreeItem item) {
       WizardStep step = treeItemToStep.get(item);
       if (step != null) {
         if(stepContentPanel.getWidget() == step.getContent()) {
           return;
         }
         int stepIndex = steps.indexOf(step);
         currentStep = stepIndex;
         if (currentStep == 0) {
           prevButton.setEnabled(false);
         } else {
           prevButton.setEnabled(true);
         }
         if(currentStep == steps.size() - 1) {
           nextButton.setText("Finish");
         } else {
           nextButton.setText("Next");
         }
         nextButton.setEnabled(false);
         stepContentPanel.setWidget(step.getContent());
         step.getContent().onShow();
       } else {
         stepContentPanel.setWidget(new HTML("&nbsp;"));
       }
     }
 
     public void onTreeItemStateChanged(TreeItem item) {
     }
   }
 }

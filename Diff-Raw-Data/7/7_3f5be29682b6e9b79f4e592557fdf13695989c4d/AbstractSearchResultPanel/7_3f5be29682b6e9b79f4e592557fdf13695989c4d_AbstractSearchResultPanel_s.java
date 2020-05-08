 package edu.wustl.cab2b.client.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 import javax.swing.border.EmptyBorder;
 
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.JXTitledPanel;
 import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTitledPanel;
 import edu.wustl.cab2b.client.ui.main.IComponent;
 import edu.wustl.cab2b.client.ui.main.ParseXMLFile;
 import edu.wustl.cab2b.client.ui.main.SwingUIManager;
 import edu.wustl.cab2b.client.ui.pagination.JPageElement;
 import edu.wustl.cab2b.client.ui.pagination.JPagination;
 import edu.wustl.cab2b.client.ui.pagination.NumericPager;
 import edu.wustl.cab2b.client.ui.pagination.PageElement;
 import edu.wustl.cab2b.client.ui.pagination.PageElementImpl;
 import edu.wustl.cab2b.client.ui.query.ClientQueryBuilder;
 import edu.wustl.cab2b.client.ui.query.IClientQueryBuilderInterface;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.common.exception.CheckedException;
 import edu.wustl.cab2b.common.queryengine.Cab2bQueryObjectFactory;
 import edu.wustl.cab2b.common.util.AttributeInterfaceComparator;
 import edu.wustl.cab2b.common.util.EntityInterfaceComparator;
 import edu.wustl.common.querysuite.queryobject.ICondition;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IExpressionId;
 import edu.wustl.common.querysuite.queryobject.IRule;
 
 /**
  * The abstract class that contains commonalities required for displaying
  * results from the 'AddLimit' and 'choose category' section from the main
  * search dialog. Concrete classes must over ride methods to effect custom
  * layout.
  * 
  * @author mahesh_iyer/chetan_bh/gautam_shetty.
  * 
  */
 
 public abstract class AbstractSearchResultPanel extends Cab2bPanel implements ActionListener {
 
     /** The pagination component to paginate the results of the search */
     private Cab2bPanel resultPanel = new Cab2bPanel();
 
     private Cab2bButton addLimitButtonTop;
 
     private Cab2bButton addLimitButtonBottom;
 
     private Cab2bButton editLimitButtonTop;
 
     private Cab2bButton editLimitButtonBottom;
 
     /**
      * Saved reference to the content panel that needs to be refreshed for
      * appropriate events.
      */
     protected ContentPanel contentPanel = null;
 
     /**
      * Constructor
      * 
      * @param addLimitPanel
      *            Reference to the parent content panel that needs refreshing.
      * 
      * @param result
      *            The collectiond of entities.
      */
     public AbstractSearchResultPanel(ContentPanel addLimitPanel, Set<EntityInterface> result) {
         initGUI(addLimitPanel, result);
     }
 
     /**
      * Method initializes the panel by appropriately laying out child
      * components.
      * 
      * @param addLimitPanel
      *            Reference to the parent content panel that needs refreshing.
      * 
      * @param result
      *            The collectiond of entities.
      * 
      */
     private void initGUI(final ContentPanel addLimitPanel, Set<EntityInterface> resultSet) {
         this.setLayout(new RiverLayout());
         this.contentPanel = addLimitPanel;
         if (contentPanel instanceof AddLimitPanel) {
             ((AddLimitPanel) contentPanel).setSearchResultPanel(this);
         }
         Vector<PageElement> pageElementCollection = new Vector<PageElement>();
         List<EntityInterface> resultList = new ArrayList<EntityInterface>(resultSet);
         Collections.sort(resultList, new EntityInterfaceComparator());
         for (EntityInterface entity : resultList) {
             // set the proper class name
             String className = edu.wustl.cab2b.common.util.Utility.getDisplayName(entity);
             String strDescription = entity.getDescription();
 
             // Create an instance of the PageElement. Initialize with the
             // appropriate data
             PageElement pageElement = new PageElementImpl();
             pageElement.setDisplayName(className);
             pageElement.setDescription(strDescription);
             pageElement.setUserObject(entity);
             pageElementCollection.add(pageElement);
         }
 
         NumericPager numPager = new NumericPager(pageElementCollection, getPageSize());
         /* Initalize the pagination component. */
         JPagination resultsPage = new JPagination(pageElementCollection, numPager, this, true);
         resultsPage.setSelectableEnabled(false);
         resultsPage.setGroupActionEnabled(false);
         resultsPage.addPageElementActionListener(this);
         resultPanel.add("hfill vfill ", resultsPage);
 
         JXTitledPanel titledSearchResultsPanel = displaySearchSummary(resultList.size());
         titledSearchResultsPanel.setContentContainer(resultPanel);
         this.add("hfill vfill", titledSearchResultsPanel);
     }
 
     private void initializeAddLimitButtons(final JXPanel[] panelsToAdd, final EntityInterface entity) {
         addLimitButtonTop = new Cab2bButton("Add Limit");
         addLimitButtonTop.addActionListener(new AddLimitButtonListner(panelsToAdd, entity));
 
         addLimitButtonBottom = new Cab2bButton("Add Limit");
         addLimitButtonBottom.addActionListener(new AddLimitButtonListner(panelsToAdd, entity));
     }
 
     private void initializeEditLimitButtons(final JXPanel[] panelsToAdd, final IExpression expression) {
         editLimitButtonTop = new Cab2bButton("Edit Limit");
         editLimitButtonTop.addActionListener(new EditLimitButtonListner(panelsToAdd, expression));
 
         editLimitButtonBottom = new Cab2bButton("Edit Limit");
         editLimitButtonBottom.addActionListener(new EditLimitButtonListner(panelsToAdd, expression));
     }
 
     public void setResultPanel(Cab2bPanel resulPanel) {
         resultPanel.removeAll();
         resultPanel.add("hfill vfill ", resulPanel);
     }
 
     public void removeResultPanel() {
         resultPanel.removeAll();
     }
 
     /**
      * Method to create AddLimitUI
      * 
      * @param entity
      */
     protected JXPanel[] getAddLimitPanels(final EntityInterface entity) {
         final JXPanel[] componentPanel = getAddLimitComponentPanels(entity);
         final JXPanel[] panelsToAdd = new Cab2bPanel[componentPanel.length + 2];
 
         initializeAddLimitButtons(panelsToAdd, entity);
         panelsToAdd[0] = new Cab2bPanel();
         panelsToAdd[0].add("right ", addLimitButtonTop);
 
         for (int i = 0; i < componentPanel.length; i++) {
             panelsToAdd[i + 1] = componentPanel[i];
         }
 
         panelsToAdd[panelsToAdd.length - 1] = new Cab2bPanel();
         panelsToAdd[panelsToAdd.length - 1].add(addLimitButtonBottom);
 
         return panelsToAdd;
     }
 
     /**
      * The action listener for the individual page elements.
      * 
      * @param actionEvent
      *            The event that contains details of the click on the individual
      *            page elements.
      * 
      */
     public void actionPerformed(ActionEvent actionEvent) {
         Cab2bHyperlink<JPageElement> link = (Cab2bHyperlink<JPageElement>) (actionEvent.getSource());
         JPageElement jPageElement = link.getUserObject();
         jPageElement.resetLabel();
 
         JPagination pagination = jPageElement.getPagination();
         JPageElement selectedPageElement = pagination.getSelectedJPageElement();
         if (selectedPageElement != null) {
             selectedPageElement.resetHyperLink();
         }
         pagination.setSelectedJPageElement(jPageElement);
 
         /* This is the EntityInterface instance. */
         PageElement pageElement = jPageElement.getPageElement();
         final EntityInterface entity = (EntityInterface) pageElement.getUserObject();
         final JXPanel[] panelsToAdd = getAddLimitPanels(entity);
 
         if (getAddLimitComponentPanels(entity) != null) {
             // pass the appropriate class name for display
             performAction(panelsToAdd, edu.wustl.cab2b.common.util.Utility.getDisplayName(entity));
         }
 
         updateUI();
     }
 
     /**
      * Get panels array to be displayed in add limit panel
      * 
      * @param entity
      * @return
      */
     public JXPanel[] getAddLimitComponentPanels(final EntityInterface entity) {
         final Collection<AttributeInterface> attributeCollection = entity.getAttributeCollection();
         ParseXMLFile parseFile = null;
         try {
             parseFile = ParseXMLFile.getInstance();
         } catch (CheckedException ce) {
             CommonUtils.handleException(ce, this, true, true, false, false);
         }
 
         if (attributeCollection != null) {
             List<AttributeInterface> attributeList = new ArrayList<AttributeInterface>(attributeCollection);
             Collections.sort(attributeList, new AttributeInterfaceComparator());
             final JXPanel[] componentPanels = new Cab2bPanel[attributeList.size()];
             try {
                 int i = 0;
 
                 Dimension maxLabelDimension = CommonUtils.getMaximumLabelDimension(attributeList);
                 for (AttributeInterface attribute : attributeList) {
                     componentPanels[i++] = (JXPanel) SwingUIManager.generateUIPanel(parseFile, attribute,
                                                                                     maxLabelDimension);
                 }
             } catch (CheckedException e) {
                 CommonUtils.handleException(e, this, true, true, false, false);
                 // JXErrorDialog.showDialog(this,
                 // ErrorCodeHandler.getErrorMessage(e.getErrorCode()), e);
             }
             return componentPanels;
         }
         return null;
     }
 
     /**
      * Get panels array to be displayed in add limit panel
      * 
      * @param entity
      * @return
      */
     public JXPanel[] getEditLimitPanels(final IExpression expression) {
         /* This is the EntityInterface instance. */
         final EntityInterface entity = expression.getConstraintEntity().getDynamicExtensionsEntity();
         final JXPanel[] componentPanel = getAddLimitComponentPanels(entity);
         final JXPanel[] panelsToAdd = new Cab2bPanel[componentPanel.length + 2];
 
         initializeEditLimitButtons(panelsToAdd, expression);
         panelsToAdd[0] = new Cab2bPanel();
         panelsToAdd[0].add(editLimitButtonTop);
 
         // adding condition panels
         for (int i = 0; i < componentPanel.length; i++) {
             panelsToAdd[i + 1] = componentPanel[i];
         }
 
         panelsToAdd[panelsToAdd.length - 1] = new Cab2bPanel();
         panelsToAdd[panelsToAdd.length - 1].add(editLimitButtonBottom);
         return panelsToAdd;
     }
 
     /**
      * Method to handle 'Add Limit' button click event
      */
     public void performAddLimitAction(JXPanel[] componentPanel, EntityInterface entity) {
         final Collection collection = entity.getAttributeCollection();
         final int size = collection.size();
         List<AttributeInterface> attributes = new ArrayList<AttributeInterface>(size);
         List<String> conditions = new ArrayList<String>(size);
         List<List<String>> values = new ArrayList<List<String>>();
 
         // Don't consider panel first and panel last for getting attribute
         // values
         // because first and last panels are Add Limit button panels
         for (int j = 1; j < componentPanel.length - 1; j++) {
             IComponent panel = (IComponent) componentPanel[j];
             String conditionString = panel.getCondition();
 
             // Check if condition is set for this panel
             AttributeInterface attribute = getAttribute(collection, panel.getAttributeName());
             ArrayList<String> conditionValues = panel.getValues();
             if (0 == conditionString.compareToIgnoreCase("Between") && (conditionValues.size() == 1)) {
                 JOptionPane.showInternalMessageDialog((this.contentPanel).getParent().getParent().getParent(),
                                                       "Please enter both the values for between operator.",
                                                       "Error", JOptionPane.ERROR_MESSAGE);
                 return;
             }
 
             if ((conditionString.equals("Is Null")) || conditionString.equals("Is Not Null")
                     || (conditionValues.size() != 0)) {
                 attributes.add(attribute);
                 conditions.add(conditionString);
                 values.add(conditionValues);
             }
         }
 
         if (attributes.size() == 0) {
             JOptionPane.showMessageDialog((this.contentPanel).getParent().getParent().getParent(),
                                           "Please add condition(s) before proceeding", "Add Limit Warning",
                                           JOptionPane.WARNING_MESSAGE);
         } else {
             MainSearchPanel mainSearchPanel = (MainSearchPanel) ((JXPanel) contentPanel).getParent().getParent();
             if (mainSearchPanel.getQueryObject() == null) {
                 IClientQueryBuilderInterface query = new ClientQueryBuilder();
                 mainSearchPanel.setQueryObject(query);
                 contentPanel.setQueryObject(query);
             }
 
             IExpressionId expressionId = mainSearchPanel.getQueryObject().addRule(attributes, conditions, values);
             if (expressionId != null) {
                 // Pratibha's code will take over from here
                 contentPanel.refreshBottomCenterPanel(expressionId);
             } else {
                 JOptionPane.showMessageDialog(
                                               mainSearchPanel.getParent(),
                                               "This rule cannot be added as it is not associated with the added rules.",
                                               "Error", JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
     private AttributeInterface getAttribute(Collection collection, String attributeName) {
         AttributeInterface attribute = null;
         Iterator iterator = collection.iterator();
 
         while (iterator.hasNext()) {
             attribute = (AttributeInterface) iterator.next();
             if (attributeName.trim().equals(attribute.getName().trim())) {
                 break;
             }
         }
 
         return attribute;
     }
 
     /**
      * Method to perform edit limit action
      */
     public void performEditLimitAction(JXPanel[] componentPanel, IExpression expression) {
         final Collection collection = expression.getConstraintEntity().getDynamicExtensionsEntity().getAttributeCollection();
         List<ICondition> conditionList = new ArrayList<ICondition>();
         for (int j = 1; j < componentPanel.length - 1; j++) {
             IComponent panel = (IComponent) componentPanel[j];
             String conditionString = panel.getCondition();
 
             AttributeInterface attribute = getAttribute(collection, panel.getAttributeName());
             ArrayList<String> values = panel.getValues();
             if (0 == conditionString.compareToIgnoreCase("Between") && (values.size() == 1)) {
                 JOptionPane.showInternalMessageDialog((this.contentPanel).getParent().getParent().getParent(),
                                                       "Please enter both the values for between operator.",
                                                       "Error", JOptionPane.ERROR_MESSAGE);
                 return;
             }
             if ((conditionString.equals("Is Null")) || conditionString.equals("Is Not Null")
                     || (values.size() != 0)) {
 
                 ICondition condition = Cab2bQueryObjectFactory.createCondition();
                 condition.setAttribute(attribute);
                 condition.setRelationalOperator(edu.wustl.cab2b.client.ui.query.Utility.getRelationalOperator(conditionString));
                 for (int i = 0; i < values.size(); i++) {
                     condition.addValue(values.get(i));
                 }
                 conditionList.add(condition);
             }
         }
         if (conditionList.size() != 0) {
             IRule rule = (IRule) expression.getOperand(0);
             rule.removeAllConditions();
             for (int i = 0; i < conditionList.size(); i++) {
                 rule.addCondition(conditionList.get(i));
             }
         } else {
             MainSearchPanel mainSearchPanel = (MainSearchPanel) ((JXPanel) contentPanel).getParent().getParent();
             JOptionPane.showInternalMessageDialog(
                                                   mainSearchPanel.getParent(),
                                                   "This rule cannot be added as it is not associated with the added rules.",
                                                   "Error", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     /**
      * This method generates the search summary panel
      * 
      * @param numberOfResults
      *            number of results obtained
      * @return summary panel
      */
     public JXTitledPanel displaySearchSummary(int numberOfResults) {
         String message = (numberOfResults == 0) ? "No result found." : "Search Results :- " + "Total results ( "
                 + numberOfResults + " )";
         JXTitledPanel titledSearchResultsPanel = new Cab2bTitledPanel(message);
         GradientPaint gp = new GradientPaint(new Point2D.Double(.05d, 0), new Color(185, 211, 238),
                 new Point2D.Double(.95d, 0), Color.WHITE);
         titledSearchResultsPanel.setTitlePainter(new BasicGradientPainter(gp));
         titledSearchResultsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
         titledSearchResultsPanel.setTitleFont(new Font("SansSerif", Font.BOLD, 11));
         titledSearchResultsPanel.setTitleForeground(Color.BLACK);
 
         return titledSearchResultsPanel;
     }
 
     /**
      * The abstract method that needs to handle any refresh related activites
      * 
      * @param arrComponentPanel
      *            This is the array of panels that forms the dynamically
      *            generated criterion pages. Each panel corresponds to one
      *            attribute from the class/category
      * 
      * @param strClassName
      *            The class/category name.
      */
     public abstract void performAction(JXPanel[] arrComponentPanel, String strClassName);
 
     /**
      * The abstract method that is to return the number of elements to be
      * displayed/page.
      * 
      * @return int Value represents the number of elements/page.
      */
     public abstract int getPageSize();
 
     /**
      * @return the addLimitButtonBottom
      */
     public Cab2bButton getAddLimitButtonBottom() {
         return addLimitButtonBottom;
     }
 
     /**
      * @return the addLimitButtonTop
      */
     public Cab2bButton getAddLimitButtonTop() {
         return addLimitButtonTop;
     }
 
     class AddLimitButtonListner implements ActionListener {
         private JXPanel[] panelsToAdd;
 
         private EntityInterface entity;
 
         AddLimitButtonListner(final JXPanel[] panelsToAdd, final EntityInterface entity) {
             this.panelsToAdd = panelsToAdd;
             this.entity = entity;
         }
 
         public void actionPerformed(ActionEvent event) {
             performAddLimitAction(this.panelsToAdd, this.entity);
             AddLimitPanel.m_innerPane.setDividerLocation(242);
         }
     }
 
     class EditLimitButtonListner implements ActionListener {
         private JXPanel[] panelsToAdd;
 
         private IExpression expression;
 
         EditLimitButtonListner(final JXPanel[] panelsToAdd, final IExpression expression) {
             this.panelsToAdd = panelsToAdd;
             this.expression = expression;
         }
 
         public void actionPerformed(ActionEvent event) {
             performEditLimitAction(this.panelsToAdd, this.expression);
         }
     }
 
 }

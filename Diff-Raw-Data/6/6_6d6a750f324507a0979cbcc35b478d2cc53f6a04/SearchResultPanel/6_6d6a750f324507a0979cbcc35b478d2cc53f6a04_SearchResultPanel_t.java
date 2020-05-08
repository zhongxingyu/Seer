 package edu.wustl.cab2b.client.ui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.border.EmptyBorder;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableCellRenderer;
 
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.JXTitledPanel;
 import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTable;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTitledPanel;
 import edu.wustl.cab2b.client.ui.main.AbstractTypePanel;
 import edu.wustl.cab2b.client.ui.main.ParseXMLFile;
 import edu.wustl.cab2b.client.ui.main.SwingUIManager;
 import edu.wustl.cab2b.client.ui.mainframe.Cab2bContentPanel;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.pagination.JPageElement;
 import edu.wustl.cab2b.client.ui.pagination.JPagination;
 import edu.wustl.cab2b.client.ui.pagination.NumericPager;
 import edu.wustl.cab2b.client.ui.pagination.PageElement;
 import edu.wustl.cab2b.client.ui.pagination.PageElementImpl;
 import edu.wustl.cab2b.client.ui.query.ClientQueryBuilder;
 import edu.wustl.cab2b.client.ui.query.IClientQueryBuilderInterface;
 import edu.wustl.cab2b.client.ui.util.CDEDetails;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.common.exception.CheckedException;
 import edu.wustl.cab2b.common.queryengine.Cab2bQueryObjectFactory;
 import edu.wustl.cab2b.common.util.AttributeInterfaceComparator;
 import edu.wustl.cab2b.common.util.Constants;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.common.querysuite.queryobject.ICondition;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IExpressionId;
 import edu.wustl.common.querysuite.queryobject.IRule;
 
 /**
  * The class that contains commonalities required for displaying results from
  * the 'AddLimit' and 'choose category' section from the main search dialog.
  * 
  * @author mahesh_iyer/chetan_bh/gautam_shetty/Deepak_Shingan
  */
 public class SearchResultPanel extends Cab2bPanel implements ActionListener {
     private static final long serialVersionUID = 1L;
 
     /** The pagination component to paginate the results of the search */
     private Cab2bPanel resultPanel;
 
     private Cab2bButton addLimitButton;
 
     private Cab2bButton editLimitButton;
 
     private Cab2bHyperlink attributeDetailsLink;
 
     private Cab2bPanel constraintButtonPanel;
 
     private EntityInterface entityForSelectedLink;
 
     /**
      * Saved reference to the content searchPanel that needs to be refreshed for
      * appropriate events.
      */
     protected ContentPanel contentPanel;
 
     /**
      * Constructor
      * 
      * @param addLimitPanel
      *            Reference to the parent content searchPanel that needs
      *            refreshing.
      * 
      * @param result
      *            The collectiond of entities.
      */
     public SearchResultPanel(ContentPanel contentPanel, Set<EntityInterface> result) {
         this.contentPanel = contentPanel;
         initGUI(result);
     }
 
     /**
      * Method initializes the searchPanel by appropriately laying out child
      * components.
      * 
      * @param result
      *            The collectiond of entities.
      */
     private void initGUI(Set<EntityInterface> resultSet) {
         if (contentPanel instanceof AddLimitPanel) {
             ((AddLimitPanel) contentPanel).setSearchResultPanel(this);
         }
 
         Vector<PageElement> pageElementCollection = new Vector<PageElement>();
         if (resultSet != null) {
             List<EntityInterface> resultList = new ArrayList<EntityInterface>(resultSet);
             //Collections.sort(resultList, new EntityInterfaceComparator());
             for (EntityInterface entity : resultList) {
                 // Create an instance of the PageElement. Initialize with the
                 // appropriate data
                 PageElement pageElement = new PageElementImpl();
 
                 String className = Utility.getDisplayName(entity);
                 pageElement.setDisplayName(className);
 
                 String description = entity.getDescription();
                if (description == null || description.equals(""))
                    description = "*No description available. ";
                 pageElement.setDescription(description);
 
                 pageElement.setUserObject(entity);
 
                 pageElementCollection.add(pageElement);
             }
 
             NumericPager numericPager = new NumericPager(pageElementCollection, getPageSize());
 
             /* Initalize the pagination component. */
             JPagination resultsPage = new JPagination(pageElementCollection, numericPager, this, true);
             resultsPage.setSelectableEnabled(false);
             resultsPage.setGroupActionEnabled(false);
             resultsPage.addPageElementActionListener(this);
 
             resultPanel = new Cab2bPanel();
             resultPanel.add("hfill vfill ", resultsPage);
 
             JXTitledPanel titledSearchResultsPanel = displaySearchSummary(resultList.size());
             titledSearchResultsPanel.setContentContainer(resultPanel);
 
             add("hfill vfill", titledSearchResultsPanel);
         }
     }
 
     /**
      * Sets result panel
      * 
      * @param resulPanel
      */
     public void setResultPanel(Cab2bPanel resulPanel) {
         resultPanel.removeAll();
         resultPanel.add("hfill vfill ", resulPanel);
     }
 
     public EntityInterface getEntityForSelectedLink() {
         return entityForSelectedLink;
     }
 
     /**
      * Removing result panel
      */
     public void removeResultPanel() {
         if (resultPanel != null)
             resultPanel.removeAll();
     }
 
     /**
      * Initiliasing/Adding Add Limit buttons
      * 
      * @param panelsToAdd
      * @param entity
      */
     public void initializeAddLimitButton(final JXPanel[] panelsToAdd, final EntityInterface entity) {
         addLimitButton = new Cab2bButton("Add Limit");
         addLimitButton.setPreferredSize(new Dimension(95, 22));
         addLimitButton.addActionListener(new AddLimitButtonListner(panelsToAdd, entity));
     }
 
     /**
      * Initiliasing/Adding EditLimit buttons
      * 
      * @param panelsToAdd
      * @param expression
      */
     private void initializeEditLimitButtons(final JXPanel[] panelsToAdd, final IExpression expression) {
         editLimitButton = new Cab2bButton("Edit Limit");
         editLimitButton.addActionListener(new EditLimitButtonListner(panelsToAdd, expression));
         editLimitButton.setPreferredSize(new Dimension(95, 22));
     }
 
     /**
      * This method ctreates and returns a hyperlink which will display certain
      * details of all the attributes of the given entity.
      * 
      * @param entity
      * @return
      */
     private void initializeAttributeDetailLink(final EntityInterface entity) {
         attributeDetailsLink = new Cab2bHyperlink();
         attributeDetailsLink.setText("CDE Details");
         attributeDetailsLink.addActionListener(new AttributeDetailsLinkListener(entity));
     }
 
     /**
      * 
      * @param cab2bButton
      * @return
      */
     private Cab2bPanel getConstraintButtonPanel(Cab2bButton cab2bButton, EntityInterface entity) {
         constraintButtonPanel = new Cab2bPanel(new RiverLayout(5, 0));
 
         constraintButtonPanel.add(cab2bButton);
         constraintButtonPanel.add("tab", new JLabel(" | "));
         if (attributeDetailsLink == null) {
             initializeAttributeDetailLink(entity);
         }
         constraintButtonPanel.add("tab", attributeDetailsLink);
         constraintButtonPanel.setOpaque(false);
 
         return constraintButtonPanel;
     }
 
     /**
      * Method to create AddLimitUI
      * 
      * @param entity
      */
     protected JXPanel[] createAddLimitPanels(final EntityInterface entity) {
         final JXPanel[] componentPanel = getAttributeComponentPanels(entity);
         final JXPanel[] finalPanelToadd = initializePanelsForAddConstraints(componentPanel);
 
         initializeAddLimitButton(componentPanel, entity);
         finalPanelToadd[0].add(getConstraintButtonPanel(addLimitButton, entity));
         GradientPaint gp1 = new GradientPaint(new Point2D.Double(.09d, 0), Color.LIGHT_GRAY, new Point2D.Double(
                 .95d, 0), Color.WHITE);
         finalPanelToadd[0].setBackgroundPainter(new BasicGradientPainter(gp1));
         finalPanelToadd[0].setBorder(BorderFactory.createLineBorder(Color.BLACK));
         return finalPanelToadd;
     }
 
     /**
      * Get panels array to be displayed in add limit searchPanel
      * 
      * @param entity
      * @return
      */
     public JXPanel[] createEditLimitPanels(final IExpression expression) {
         /* This is the EntityInterface instance. */
         final EntityInterface entity = expression.getQueryEntity().getDynamicExtensionsEntity();
 
         final JXPanel[] componentPanel = getAttributeComponentPanels(entity);
         final JXPanel[] finalPanelToadd = initializePanelsForAddConstraints(componentPanel);
 
         initializeEditLimitButtons(componentPanel, expression);
         finalPanelToadd[0].add(getConstraintButtonPanel(editLimitButton, entity));
 
         return finalPanelToadd;
     }
 
     /**
      * The action listener for the individual page elements.
      * 
      * @param actionEvent
      *            The event that contains details of the click on the individual
      *            page elements.
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
 
         PageElement pageElement = jPageElement.getPageElement();
         entityForSelectedLink = (EntityInterface) pageElement.getUserObject();
 
         if (contentPanel instanceof Cab2bContentPanel) {
             SearchPanel searchPanel = (SearchPanel) this.getParent();
 
             (searchPanel.getAttributeSelectCDCPanel()).setEntityInterface(entityForSelectedLink);
             (searchPanel.getAttributeSelectCDCPanel()).generatePanel();
             (searchPanel.getAttributeSelectCDCPanel()).setTestDAG(searchPanel.getTestDAG());
         }
 
         initializeAttributeDetailLink(entityForSelectedLink);
         final JXPanel[] panelsToAdd = createAddLimitPanels(entityForSelectedLink);
 
         if (getAttributeComponentPanels(entityForSelectedLink) != null) {
             // pass the appropriate class name for display
             performAction(panelsToAdd, Utility.getDisplayName(entityForSelectedLink));
         }
 
         updateUI();
     }
 
     /**
      * Get panels array to be displayed in add limit searchPanel
      * 
      * @param entity
      * @return
      */
     private JXPanel[] getAttributeComponentPanels(final EntityInterface entity) {
         final Collection<AttributeInterface> attributeCollection = entity.getAttributeCollection();
 
         AbstractTypePanel[] componentPanels = null;
         if (attributeCollection != null) {
             try {
                 List<AttributeInterface> attributeList = new ArrayList<AttributeInterface>(attributeCollection);
                 Collections.sort(attributeList, new AttributeInterfaceComparator());
                 componentPanels = new AbstractTypePanel[attributeList.size()];
 
                 ParseXMLFile parseFile = ParseXMLFile.getInstance();
                 Dimension maxLabelDimension = CommonUtils.getMaximumLabelDimension(attributeList);
 
                 int i = 0;
                 for (AttributeInterface attribute : attributeList) {
                     componentPanels[i++] = (AbstractTypePanel) SwingUIManager.generateUIPanel(parseFile,
                                                                                               attribute,
                                                                                               maxLabelDimension);
                     componentPanels[i - 1].createPanelWithOperator(attribute);
                 }
             } catch (CheckedException checkedException) {
                 CommonUtils.handleException(checkedException, this, true, true, false, false);
             }
         }
         return componentPanels;
     }
 
     /**
      * 
      * @param componentPanel
      * @return
      */
     public JXPanel[] initializePanelsForAddConstraints(JXPanel[] componentPanel) {
         Cab2bPanel cab2bPanel = new Cab2bPanel(new RiverLayout(5, 5));
         for (int j = 0; j < componentPanel.length; j++) {
             cab2bPanel.add("br", componentPanel[j]);
         }
         JScrollPane pane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         pane.getViewport().setBackground(Color.WHITE);
         pane.getViewport().add(cab2bPanel);
         pane.getViewport().setBorder(null);
         pane.setBorder(null);
 
         JXPanel[] finalPanelsToAdd = new Cab2bPanel[2];
         FlowLayout flowLayout = new FlowLayout(2, 0, 3);
         finalPanelsToAdd[0] = new Cab2bPanel(flowLayout);
 
         finalPanelsToAdd[1] = new Cab2bPanel();
         finalPanelsToAdd[1].add("hfill vfill ", pane);
 
         return finalPanelsToAdd;
     }
 
     /**
      * Method to handle 'Add Limit' button click event
      * 
      * @param componentPanel
      * @param entity
      */
     public void performAddLimitAction(JXPanel[] componentPanel, EntityInterface entity) {
         List<AttributeInterface> attributes = new ArrayList<AttributeInterface>();
         List<String> conditions = new ArrayList<String>();
         List<List<String>> values = new ArrayList<List<String>>();
 
         for (int j = 0; j < componentPanel.length; j++) {
             AbstractTypePanel panel = (AbstractTypePanel) componentPanel[j];
             int conditionStatus = panel.isConditionValid(contentPanel);
             if (conditionStatus < 0)
                 return;
             else if (conditionStatus == 0) {
                 attributes.add(panel.getAttributeEntity());
                 conditions.add(panel.getConditionItem());
                 values.add(panel.getValues());
             }
         }
 
         if (attributes.isEmpty()) {
             JOptionPane.showMessageDialog(contentPanel, "Please add condition(s) before proceeding",
                                           "Add Limit Warning", JOptionPane.WARNING_MESSAGE);
         } else {
             MainSearchPanel mainSearchPanel = (MainSearchPanel) ((JXPanel) contentPanel).getParent().getParent();
             if (mainSearchPanel.getQueryObject() == null) {
                 IClientQueryBuilderInterface query = new ClientQueryBuilder();
                 mainSearchPanel.setQueryObject(query);
                 mainSearchPanel.getCenterPanel().getAddLimitPanel().setQueryObject(query);
             }
 
             IExpressionId expressionId = mainSearchPanel.getQueryObject().addRule(attributes, conditions, values);
             if (expressionId == null) {
                 JOptionPane.showMessageDialog(
                                               mainSearchPanel.getParent(),
                                               "This rule cannot be added as it is not associated with the added rules.",
                                               "Error", JOptionPane.ERROR_MESSAGE);
             } else {
                 mainSearchPanel.getCenterPanel().getAddLimitPanel().refreshBottomCenterPanel(expressionId);
             }
         }
     }
 
     /**
      * returns Attribute Interface for the name from the collection parameter
      * 
      * @param collection
      * @param attributeName
      * @return
      */
     private AttributeInterface getAttribute(Collection<AttributeInterface> attributeCollection,
                                             String attributeName) {
         AttributeInterface requriedAttribute = null;
         for (AttributeInterface attribute : attributeCollection) {
             if (attributeName.trim().equals(attribute.getName().trim())) {
                 requriedAttribute = attribute;
                 break;
             }
         }
         return requriedAttribute;
     }
 
     /**
      * Method to perform edit limit action
      */
     public void performEditLimitAction(JXPanel[] componentPanel, IExpression expression) {
         List<ICondition> conditionList = new ArrayList<ICondition>();
         for (int j = 0; j < componentPanel.length; j++) {
             AbstractTypePanel panel = (AbstractTypePanel) componentPanel[j];
             String conditionString = panel.getConditionItem();
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
                 final AttributeInterface attribute = panel.getAttributeEntity();
                 condition.setAttribute(attribute);
                 condition.setRelationalOperator(edu.wustl.cab2b.client.ui.query.Utility.getRelationalOperator(conditionString));
                 for (int i = 0; i < values.size(); i++) {
                     condition.addValue(values.get(i));
                 }
                 conditionList.add(condition);
             }
         }
 
         if (conditionList.isEmpty()) {
             MainSearchPanel mainSearchPanel = (MainSearchPanel) ((JXPanel) contentPanel).getParent().getParent();
             JOptionPane.showInternalMessageDialog(
                                                   mainSearchPanel.getParent(),
                                                   "This rule cannot be added as it is not associated with the added rules.",
                                                   "Error", JOptionPane.ERROR_MESSAGE);
         } else {
             IRule rule = (IRule) expression.getOperand(0);
             rule.removeAllConditions();
             for (int i = 0; i < conditionList.size(); i++) {
                 rule.addCondition(conditionList.get(i));
             }
         }
     }
 
     /**
      * This method generates the search summary searchPanel
      * 
      * @param numberOfResults
      *            number of results obtained
      * @return summary searchPanel
      */
     public JXTitledPanel displaySearchSummary(int numberOfResults) {
         String message = (numberOfResults == 0) ? "No result found." : "Search Results :- Total results ( "
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
      * The method that needs to handle any refresh related activites
      * 
      * @param attributeComponentPanel
      *            This is the array of panels that forms the dynamically
      *            generated criterion pages. Each searchPanel corresponds to one
      *            attribute from the class/category Method to select appropriate
      *            searchPanel and refresh the addLimit page
      * 
      * @param className
      *            The class/category name.
      */
     private void performAction(JXPanel[] attributeComponentPanel, String className) {
         Container container = ((JXPanel) (contentPanel)).getParent();
         if (container instanceof SearchCenterPanel) {
             SearchCenterPanel searchCenterPanel = (SearchCenterPanel) container;
             /*
              * Use the parent reference to in turn get a reference to the
              * navigation searchPanel, and cause it to move to the next card.
              */
             MainSearchPanel mainSearchPanel = (MainSearchPanel) (searchCenterPanel.getParent());
             mainSearchPanel.getNavigationPanel().enableButtons();
 
             /*
              * Get the searchPanel corresponding to the currently selcted card
              * and refresh it.
              */
             AddLimitPanel addLimitPanel = searchCenterPanel.getAddLimitPanel();
             addLimitPanel.refresh(attributeComponentPanel, className);
 
             // set search-result searchPanel in AddLimit searchPanel and move to
             // next tab
             if (searchCenterPanel.getSelectedCardIndex() == 0) {
                 ChooseCategoryPanel chooseCategoryPanel = searchCenterPanel.getChooseCategoryPanel();
                 addLimitPanel.addSearchPanel(chooseCategoryPanel.getSearchPanel());
                 SearchResultPanel searchResultPanel = chooseCategoryPanel.getSearchResultPanel();
                 if (searchResultPanel != null) {
                     addLimitPanel.addResultsPanel(searchResultPanel);
                     searchCenterPanel.setAddLimitPanel(addLimitPanel);
                 }
                 mainSearchPanel.getNavigationPanel().showCard(true);
             }
         }
     }
 
     /**
      * The abstract method that is to return the number of elements to be
      * displayed/page.
      * 
      * @return int Value represents the number of elements/page.
      */
     public int getPageSize() {
         return 3;
     };
 
     /**
      * @return the addLimitButtonTop
      */
     public Cab2bButton getAddLimitButton() {
         return addLimitButton;
     }
 
     /**
      * Action Listener class for Add Limit buttons
      * 
      * @author Deepak_Shingan
      * 
      */
     class AddLimitButtonListner implements ActionListener {
         private JXPanel[] panelsToAdd;
 
         private EntityInterface entity;
 
         public AddLimitButtonListner(final JXPanel[] panelsToAdd, final EntityInterface entity) {
             this.panelsToAdd = panelsToAdd;
             this.entity = entity;
         }
 
         public void actionPerformed(ActionEvent event) {
             performAddLimitAction(panelsToAdd, entity);
             AddLimitPanel.m_innerPane.setDividerLocation(242);
         }
     }
 
     /**
      * Action Listener class for Edit Limit buttons
      * 
      * @author Deepak_Shingan
      * 
      */
     class EditLimitButtonListner implements ActionListener {
         private JXPanel[] panelsToAdd;
 
         private IExpression expression;
 
         public EditLimitButtonListner(final JXPanel[] panelsToAdd, final IExpression expression) {
             this.panelsToAdd = panelsToAdd;
             this.expression = expression;
         }
 
         public void actionPerformed(ActionEvent event) {
             performEditLimitAction(this.panelsToAdd, this.expression);
         }
     }
 
     class AttributeDetailsLinkListener implements ActionListener {
         private EntityInterface entity;
 
         public AttributeDetailsLinkListener(EntityInterface entity) {
             this.entity = entity;
         }
 
         public void actionPerformed(ActionEvent event) {
 
             Cab2bTable cab2bTable = new Cab2bTable(new CDETableModel(entity));
 
             cab2bTable.setBorder(null);
             cab2bTable.setRowHeightEnabled(true);
             cab2bTable.setShowGrid(false);
             cab2bTable.getColumnModel().getColumn(0).setPreferredWidth(50);
             cab2bTable.getColumnModel().getColumn(1).setPreferredWidth(10);
             cab2bTable.getColumnModel().getColumn(2).setPreferredWidth(30);
             cab2bTable.getColumnModel().getColumn(3).setPreferredWidth(320);
             cab2bTable.setRowSelectionAllowed(false);
 
             for (int j = 0; j < 4; j++) {
                 cab2bTable.getColumnModel().getColumn(j).setCellRenderer(new MyCellRenderer());
             }
 
             cab2bTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
             JScrollPane jScrollPane = new JScrollPane(cab2bTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
             jScrollPane.setBorder(null);
 
             /*
              * WindowUtilities.showInDialog(NewWelcomePanel.mainFrame,
              * jScrollPane, "CDE Details", Constants.WIZARD_SIZE2_DIMENSION,
              * true, false);
              */
             WindowUtilities.showInDialog(NewWelcomePanel.mainFrame, jScrollPane, "CDE Details",
                                          Constants.WIZARD_SIZE2_DIMENSION, true, false);
         }
     }
 
     /**
      * @return the constraintButtonPanel
      */
     public Cab2bPanel getConstraintButtonPanel() {
         return constraintButtonPanel;
     }
 
     class MyCellRenderer extends JTextArea implements TableCellRenderer {
         public MyCellRenderer() {
             setLineWrap(true);
             setWrapStyleWord(true);
         }
 
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                        boolean hasFocus, int row, int column) {
             if (value != null) {
                 setText(value.toString());
             }
             setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
             if (table.getRowHeight(row) != getPreferredSize().height) {
                 table.setRowHeight(row, getPreferredSize().height);
             }
             return this;
         }
 
     }
 
     private class CDETableModel extends AbstractTableModel {
         private CDEDetails cdeDetails;
 
         private CDETableModel(EntityInterface entity) {
             super();
             this.cdeDetails = new CDEDetails(entity);
         }
 
         public int getRowCount() {
             return cdeDetails.getRowCount();
         }
 
         public int getColumnCount() {
             return cdeDetails.getColumnCount();
         }
 
         public Object getValueAt(int row, int column) {
             return cdeDetails.getValueAt(row, column);
         }
 
         public String getColumnName(int column) {
             return cdeDetails.getColumnName(column);
         }
 
     }
 }

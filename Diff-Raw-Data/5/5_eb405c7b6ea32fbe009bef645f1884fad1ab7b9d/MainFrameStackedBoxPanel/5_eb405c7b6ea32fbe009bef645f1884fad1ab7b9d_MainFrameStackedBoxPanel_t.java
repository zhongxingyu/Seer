 package edu.wustl.cab2b.client.ui.mainframe.stackbox;
 
 import static edu.wustl.cab2b.client.ui.util.ApplicationResourceConstants.EXPERIMENT_BOX_TEXT;
 import static edu.wustl.cab2b.client.ui.util.ApplicationResourceConstants.POPULAR_CATEGORY_BOX_TEXT;
 import static edu.wustl.cab2b.client.ui.util.ApplicationResourceConstants.QUERY_BOX_TEXT;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.MY_EXPERIMENT_IMAGE;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.MY_SEARCH_QUERIES_IMAGE;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.POPULAR_CATEGORIES_IMAGE;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collection;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.RiverLayout;
 import edu.wustl.cab2b.client.ui.controls.StackedBox;
 import edu.wustl.cab2b.client.ui.mainframe.MainFrame;
 import edu.wustl.cab2b.client.ui.mainframe.showall.ShowAllCategoryPanel;
 import edu.wustl.cab2b.client.ui.mainframe.showall.ShowAllPanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.common.cache.AbstractEntityCache;
 import edu.wustl.cab2b.common.category.CategoryPopularity;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.common.util.global.ApplicationProperties;
 
 /**
  * @author Chandrakant Talele/Deepak_Shingan
  */
 public class MainFrameStackedBoxPanel extends Cab2bPanel {
 
     private static final long serialVersionUID = 1L;
 
     private SavedQueryLinkPanel mySearchQueriesPanel;
 
     private JPanel popularSearchCategoryPanel;
 
     private MainFrame mainFrame;
 
     private Cab2bPanel myExperimentsPanel;
 
     public static Color CLICKED_COLOR = new Color(76, 41, 157);
 
     public static Color UNCLICKED_COLOR = new Color(0x034E74);
 
     public static final String SHOW_ALL_LINK = "Show All";
 
     /**
      * Constructor
      * @param frame
      * @param mainFrame
      */
     public MainFrameStackedBoxPanel(MainFrame mainFrame) {
         this.mainFrame = mainFrame;
         initUI();
     }
 
     /**
      * GUI initialising panel
      */
     private void initUI() {
         this.setLayout(new BorderLayout());
         StackedBox stackedBox = new StackedBox();
         stackedBox.setTitleBackgroundColor(new Color(200, 200, 220));
         JScrollPane scrollPane = new JScrollPane(stackedBox);
         scrollPane.setBorder(null);
         this.add(scrollPane, BorderLayout.CENTER);
 
         mySearchQueriesPanel = SavedQueryLinkPanel.getInstance();
 
         final String titleQuery = ApplicationProperties.getValue(QUERY_BOX_TEXT);
         stackedBox.addBox(titleQuery, mySearchQueriesPanel, MY_SEARCH_QUERIES_IMAGE, true);
 
         popularSearchCategoryPanel = getPopularSearchCategoriesPanel(
                                                                      CommonUtils.getPopularSearchCategoriesForMainFrame(),
                                                                      new CategoryHyperlinkActionListener());
         final String titlePopularcategories = ApplicationProperties.getValue(POPULAR_CATEGORY_BOX_TEXT);
         stackedBox.addBox(titlePopularcategories, popularSearchCategoryPanel, POPULAR_CATEGORIES_IMAGE, true);
 
         myExperimentsPanel = MyExperimentLinkPanel.getInstance();
 
         final String titleExpr = ApplicationProperties.getValue(EXPERIMENT_BOX_TEXT);
         stackedBox.addBox(titleExpr, myExperimentsPanel, MY_EXPERIMENT_IMAGE, true);
 
         stackedBox.setPreferredSize(new Dimension(250, 500));
         stackedBox.setMinimumSize(new Dimension(250, 500));
         this.setMinimumSize(new Dimension(242, this.getMinimumSize().height)); // for bug#3745
     }
 
     /**
      * This method returns panel with five most popular categories from database.
      * TODO: Currently getting all categories from database
      * @param data
      */
     public Cab2bPanel getPopularSearchCategoriesPanel(Collection<CategoryPopularity> data,
                                                       ActionListener actionClass) {
         Cab2bPanel panel = new Cab2bPanel();
         panel.setLayout(new RiverLayout(5, 5));
         panel.add(new Cab2bLabel());
         int categoryCounter = 0;
         for (CategoryPopularity category : data) {
             Cab2bHyperlink hyperlink = new Cab2bHyperlink(true);
             EntityInterface entityInterface = AbstractEntityCache.getCache().getEntityById(category.getEntityId());
             CommonUtils.setHyperlinkProperties(hyperlink, entityInterface,
                                                Utility.getDisplayName(entityInterface),
                                                entityInterface.getDescription(), actionClass);
             panel.add("br", hyperlink);
            if (++categoryCounter >= 4)
                 break;
         }
 
         ActionListener showAllExpAction = new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 mainFrame.getGlobalNavigationPanel().getGlobalNavigationGlassPane().setShowAllPanel(
                                                                                                     getAllCategoryPanel());
             }
         };
        if (categoryCounter >= 4)
             addShowAllLink(panel, showAllExpAction, data.size() > 0);
         else if (categoryCounter == 0) {
             Cab2bLabel label = new Cab2bLabel("No saved categories.");
             label.setBackground(Color.blue);
             panel.add(label);
         }
         return panel;
     }
 
     /**
      * This method returns panel with all popular categories from database 
      * @return
      */
     private ShowAllPanel getAllCategoryPanel() {
         Collection<CategoryPopularity> allPopularCategories = CommonUtils.getPopularCategoriesForShowAll();
         final Object objData[][] = new Object[allPopularCategories.size()][6];
         final String headers[] = { ShowAllCategoryPanel.CATEGORY_NAME_TITLE, ShowAllCategoryPanel.CATEGORY_POPULARITY_TITLE, ShowAllCategoryPanel.CATEGORY_DESCRIPTION_TITLE, ShowAllCategoryPanel.CATEGORY_DATE_TITLE, " Category ID-Hidden" };
         int i = 0;
         for (CategoryPopularity category : allPopularCategories) {
             EntityInterface entityInterface = AbstractEntityCache.getCache().getEntityById(category.getEntityId());
             objData[i][0] = Utility.getDisplayName(entityInterface);
             objData[i][1] = Long.toString(category.getPopularity());
             objData[i][2] = entityInterface.getDescription();
             objData[i][3] = entityInterface.getLastUpdated();
             objData[i][4] = entityInterface;
             i++;
         }
         return new ShowAllCategoryPanel(headers, objData);
     }
 
     /**
      * Method to add show all link to panel
      * @param panel
      * @param actionClass
      */
     private void addShowAllLink(Cab2bPanel panel, ActionListener actionClass, boolean enableLink) {
         Cab2bHyperlink hyperlink = new Cab2bHyperlink(true);
         hyperlink.setVisible(enableLink);
         CommonUtils.setHyperlinkProperties(hyperlink, null, SHOW_ALL_LINK, "", actionClass);
         panel.add("br right", hyperlink);
     }
 
     /**
      * Homepage Category Link action listener class
      * 
      * @author deepak_shingan
      * 
      */
     class CategoryHyperlinkActionListener implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             ShowAllCategoryPanel.categoryLinkAction((EntityInterface) ((Cab2bHyperlink) e.getSource()).getUserObject());
         }
     }
 }

 package edu.wustl.cab2b.client.ui.util;
 
 import static edu.wustl.cab2b.client.ui.util.ApplicationResourceConstants.SEARCH_FRAME_TITLE;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.APPLICATION_RESOURCES_FILE_NAME;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.ERROR_CODE_FILE_NAME;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.LIMIT_CONNECT_DESELECTED;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.LIMIT_CONNECT_SELECTED;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.PAPER_GRID_ADD_LIMIT;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.PARENTHISIS_ICON_ADD_LIMIT;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.PARENTHISIS_ICON_ADD_LIMIT_MOUSEOVER;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.PORT_IMAGE_ADD_LIMIT;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.SELECT_ICON_ADD_LIMIT;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.SELECT_ICON_ADD_LIMIT_MOUSEOVER;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.ejb.EJBHome;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import org.apache.log4j.Logger;
 import org.jdesktop.swingx.JXErrorDialog;
 import org.openide.util.Utilities;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.client.cache.PopularCategoryCache;
 import edu.wustl.cab2b.client.cache.UserCache;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.mainframe.GlobalNavigationPanel;
 import edu.wustl.cab2b.client.ui.mainframe.MainFrame;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.mainframe.UserValidator;
 import edu.wustl.cab2b.client.ui.mainframe.stackbox.MainFrameStackedBoxPanel;
 import edu.wustl.cab2b.client.ui.searchDataWizard.MainSearchPanel;
 import edu.wustl.cab2b.client.ui.searchDataWizard.SaveDatalistPanel;
 import edu.wustl.cab2b.common.BusinessInterface;
 import edu.wustl.cab2b.common.category.CategoryPopularity;
 import edu.wustl.cab2b.common.datalist.IDataRow;
 import edu.wustl.cab2b.common.domain.Experiment;
 import edu.wustl.cab2b.common.ejb.EjbNamesConstants;
 import edu.wustl.cab2b.common.ejb.category.CategoryBusinessInterface;
 import edu.wustl.cab2b.common.ejb.category.CategoryHomeInterface;
 import edu.wustl.cab2b.common.ejb.queryengine.QueryEngineBusinessInterface;
 import edu.wustl.cab2b.common.ejb.queryengine.QueryEngineHome;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeHandler;
 import edu.wustl.cab2b.common.exception.Cab2bExceptionInterface;
 import edu.wustl.cab2b.common.exception.CheckedException;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.experiment.ExperimentBusinessInterface;
 import edu.wustl.cab2b.common.experiment.ExperimentHome;
 import edu.wustl.cab2b.common.locator.Locator;
 import edu.wustl.cab2b.common.locator.LocatorException;
 import edu.wustl.cab2b.common.queryengine.ICab2bQuery;
 import edu.wustl.cab2b.common.queryengine.result.IQueryResult;
 import edu.wustl.cab2b.common.queryengine.result.IRecord;
 import edu.wustl.cab2b.common.user.UserInterface;
 import edu.wustl.cab2b.common.util.CategoryPopularityComparator;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.common.querysuite.metadata.category.Category;
 import edu.wustl.common.querysuite.queryobject.IParameterizedQuery;
 import edu.wustl.common.querysuite.queryobject.IQueryEntity;
 import edu.wustl.common.util.global.ApplicationProperties;
 
 /**
  * Utility for UI side classes
  * @author Chetan B H
  * @author Mahesh Iyer
  */
 public class CommonUtils {
     private static final Logger logger = edu.wustl.common.util.logger.Logger.getLogger(CommonUtils.class);
 
     /** Constant storing enumerated values for DAG-Images */
     public static enum DagImages {
         DocumentPaperIcon, PortImageIcon, ArrowSelectIcon, ArrowSelectMOIcon, SelectIcon, selectMOIcon, ParenthesisIcon, ParenthesisMOIcon
     };
 
     /**
      * Method to get BusinessInterface object for given bean name and home class
      * object
      * 
      * @param beanName
      *            Name of the bean class
      * @param homeClassForBean
      *            HomeClass object for this bean
      * @param parentComponent
      *            The parent component which will be parent for displaying
      *            exception messages
      * @return the businessInterface object for given bean name
      */
     public static BusinessInterface getBusinessInterface(String beanName,
                                                          Class<? extends EJBHome> homeClassForBean,
                                                          Component parentComponent) {
         BusinessInterface businessInterface = null;
         try {
             businessInterface = Locator.getInstance().locate(beanName, homeClassForBean);
         } catch (LocatorException e1) {
             handleException(e1, parentComponent, true, true, false, false);
         }
         return businessInterface;
     }
 
     /**
      * Method to disable all components from the specified container
      * 
      * @param container
      */
     public static void disableAllComponent(Container container) {
         for (int i = 0; i < container.getComponentCount(); i++) {
             container.getComponent(i).setEnabled(false);
             if (container.getComponent(i) instanceof Container)
                 disableAllComponent((Container) container.getComponent(i));
         }
     }
 
     /**
      * Method to get BusinessInterface object for given bean name and home class object
      * 
      * @param beanName Name of the bean class
      * @param homeClassForBean HomeClass object for this bean
      * @return the businessInterface object for given bean name
      * @throws LocatorException Throws exception if BusinessInterface is not located for given bean name
      */
     public static BusinessInterface getBusinessInterface(String beanName, Class<? extends EJBHome> homeClassForBean)
             throws LocatorException {
         return Locator.getInstance().locate(beanName, homeClassForBean);
     }
 
     /**
      * A Utility method to handle exception, logging and showing it in a dialog.
      * @param e Exception to handle
      * @param parentComponent parent component
      * @param showErrorDialog TRUE if you want to display a error dialog to user with exception details
      * @param logException TRUE if you wish to log exception on client side
      * @param logToConsole TRUE if you want exception trace to be printed on console.
      * @param killApplication TRUE if you want to terminate application for given exception
      */
     public static void handleException(Exception e, Component parentComponent, boolean showErrorDialog,
                                        boolean logException, boolean logToConsole, boolean killApplication) {
         String msgForUser = "Error";
         Exception exception = getOriginalException(e);
         if (exception != null) {
             e = exception;
         }
 
         String msgToLog = e.getMessage();
         if (e instanceof Cab2bExceptionInterface) {
             Cab2bExceptionInterface cab2bException = (Cab2bExceptionInterface) e;
             String errorCode = cab2bException.getErrorCode();
             if (errorCode != null) {
                 msgForUser = ErrorCodeHandler.getErrorMessage(errorCode);
                if (ErrorCodeConstants.QM_0004.equals(errorCode)) {
                    msgForUser += msgToLog.substring(msgToLog.indexOf("http"), (msgToLog.indexOf("\n") - 1));
                    msgForUser += "\nRemove it and query again";
                }
                 msgToLog = errorCode + ":" + msgForUser;
             }
         }
 
         if (logException) {
             logger.error(msgToLog, e);
         }
         if (showErrorDialog) {
             JXErrorDialog.showDialog(parentComponent, "caB2B - Application Error", msgForUser);
         }
 
         if (logToConsole) {
             e.printStackTrace();
         }
 
         if (killApplication) {
             System.exit(0);
         }
     }
 
     /**
      * This method traverses the whole hierarchy of the given exception to check
      * whether there is any exception which is {@link CheckedException} OR
      * {@link LocatorException} OR{@link RuntimeException} If yes it returns it
      * otherwise it returns the original passed exception.
      * 
      * @param cause exception to verify
      * @return The root exception
      */
     private static Exception getOriginalException(Exception cause) {
         if (cause != null && cause instanceof Exception) {
             if (cause instanceof CheckedException || cause instanceof LocatorException
                     || cause instanceof RuntimeException) {
                 return cause;
             } else {
                 return getOriginalException((Exception) cause.getCause());
             }
         }
         return cause;
     }
 
     /**
      * The method executes the encapsulated B2B query. For this it uses the
      * Locator service to locate an instance of the QueryEngineBusinessInterface
      * and uses the interface to remotely execute the query.
      */
     public static IQueryResult<? extends IRecord> executeQuery(ICab2bQuery query, JComponent comp)
             throws RemoteException {
         QueryEngineBusinessInterface queryEngineBus = (QueryEngineBusinessInterface) getBusinessInterface(
                                                                                                           EjbNamesConstants.QUERY_ENGINE_BEAN,
                                                                                                           QueryEngineHome.class,
                                                                                                           comp);
         return executeQuery(query, queryEngineBus, comp);
     }
 
     /**
      * The method executes the encapsulated B2B query. For this it uses the
      * Locator service to locate an instance of the QueryEngineBusinessInterface
      * and uses the interface to remotely execute the query.
      */
     public static IQueryResult<? extends IRecord> executeQuery(ICab2bQuery query,
                                                                QueryEngineBusinessInterface queryEngineBus,
                                                                JComponent comp) throws RemoteException {
         IQueryResult<? extends IRecord> iQueryResult = null;
         try {
             iQueryResult = executeQuery(query, queryEngineBus);
         } catch (RuntimeException re) {
             handleException(re, comp, true, false, false, false);
         } catch (Exception e) {
             handleException(e, comp, true, false, false, false);
         }
 
         return iQueryResult;
     }
 
     /**
      * The method executes the encapsulated B2B query. For this it uses the
      * Locator service to locate an instance of the QueryEngineBusinessInterface
      * and uses the interface to remotely execute the query.
      * 
      * @param query
      * @param queryEngineBus
      * @throws RuntimeException
      * @throws RemoteException
      */
     public static IQueryResult<? extends IRecord> executeQuery(ICab2bQuery query,
                                                                QueryEngineBusinessInterface queryEngineBus)
             throws RuntimeException, RemoteException, Exception {
         return queryEngineBus.executeQuery(query, UserValidator.getSerializedDelegatedCredReference(),
                                            UserValidator.getIdP());

     }
 
     /**
      * Method to get count of bit 1 set in given BitSet
      * @param bitSet The BitSet object in which to find count
      * @return The count of 1 bit in BitSet
      */
     public static int getCountofOnBits(BitSet bitSet) {
         int count = 0;
         for (int i = 0; i < bitSet.size(); i++) {
             if (bitSet.get(i))
                 count++;
         }
         return count;
     }
 
     /**
      * Utility method to find the number of occurrence of a particular character
      * in the String
      */
     public static int countCharacterIn(String str, char character) {
         int count = 0;
         for (char characterInStr : str.toCharArray()) {
             if (characterInStr == character) {
                 count++;
             }
         }
         return count;
     }
 
     /**
      * Utility method to get a Dimension relative to a reference Dimension.
      * 
      * @param referenceDimnesion
      * @param percentageWidth
      * @param percentageHeight
      * @return a new relative dimension.
      */
     public static Dimension getRelativeDimension(Dimension referenceDimnesion, float percentageWidth,
                                                  float percentageHeight) {
         if (referenceDimnesion.height <= 0 || referenceDimnesion.width <= 0) {
             throw new IllegalArgumentException("Reference dimension can't be (0,0) or less");
         }
         if ((0.0f > percentageHeight || percentageHeight > 1.0f)
                 || (0.0f > percentageWidth || percentageWidth > 1.0f)) {
             throw new IllegalArgumentException(
                     "Percentage width and height should be less than 1.0 and greater than 1.0");
         }
         Dimension relativeDimension = new Dimension();
 
         relativeDimension.setSize(percentageWidth * referenceDimnesion.getWidth(), percentageHeight
                 * referenceDimnesion.getHeight());
 
         return relativeDimension;
     }
 
     /**
      * Splits the string with qualifier
      * 
      * @param string
      * @param textQualifier
      * @param seperator
      */
     public static ArrayList<String> splitStringWithTextQualifier(String string, char textQualifier, char delimeter) {
         ArrayList<String> tokenList = new ArrayList<String>();
         StringBuffer token = new StringBuffer();
         boolean isTextQualifierStart = false;
 
         for (int i = 0; i < string.length(); i++) {
             if (string.charAt(i) == textQualifier) {
                 if (isTextQualifierStart == true) {
                     if (token.length() != 0) {
                         tokenList.add(token.toString().trim());
                     }
                     isTextQualifierStart = false;
                     token.setLength(0);
                 } else {
                     isTextQualifierStart = true;
                 }
             } else if (string.charAt(i) != delimeter) {
                 token.append(string.charAt(i));
             } else {
                 if (isTextQualifierStart == true) {
                     token.append(string.charAt(i));
                 } else {
                     if (token.length() != 0) {
                         tokenList.add(token.toString().trim());
                     }
                     token.setLength(0);
                 }
             }
         }
 
         // append last string
         String finalToken = token.toString().trim();
         if (isTextQualifierStart == true) {
             if (!finalToken.equals("")) {
                 tokenList.add("\"" + finalToken);
             }
         } else {
             if (!finalToken.equals("")) {
                 tokenList.add(finalToken);
             }
         }
         return tokenList;
     }
 
     /**
      * Removes any continuous space characters(2 or more) that may appear at the
      * beginning, end or in between to words.
      * 
      * @param str string with continuous space characters.
      * @return processed string with no continuous space characters.
      */
     public static String removeContinuousSpaceCharsAndTrim(String str) {
         char[] charsWithoutContinousSpaceChars = new char[str.length()];
         char prevCharacter = 'a';
         int index = 0;
 
         for (char character : str.toCharArray()) {
             if (!(Character.isWhitespace(character) && Character.isWhitespace(prevCharacter))) {
                 charsWithoutContinousSpaceChars[index++] = character;
             }
             prevCharacter = character;
         }
 
         String result = new String(charsWithoutContinousSpaceChars);
         return result.trim();
     }
 
     /**
      * This method takes the node string and traverses the tree till it finds
      * the node matching the string. If the match is found the node is returned
      * else null is returned
      * 
      * @param nodeStr node string to search for
      * @return tree node
      */
     public static DefaultMutableTreeNode searchNode(DefaultMutableTreeNode rootNode, Object userObject) {
 
         Enumeration<DefaultMutableTreeNode> benum = rootNode.breadthFirstEnumeration();
         while (benum.hasMoreElements()) {
             DefaultMutableTreeNode node = benum.nextElement();
             if (userObject.equals(node.getUserObject())) {
                 return node;
             }
         }
         return null;
     }
 
     /**
      * Method to get identifier attribute index for given list of attributes
      * @param attributes Ordered list of attributes
      * @return index of Id attribute
      */
     public static int getIdAttributeIndexFromAttributes(List<AttributeInterface> attributes) {
         int identifierIndex = -1;
         for (int i = 0; i < attributes.size(); i++) {
             AttributeInterface attribute = attributes.get(i);
             if (Utility.isIdentifierAttribute(attribute)) {
                 identifierIndex = i;
                 break;
             }
         }
         return identifierIndex;
     }
 
     /**
      * check the if the specified string is a valid name for an experiment, category etc.
      * @param name the name
      * @return true if name is valid otherwise false
      */
     public static boolean isNameValid(String name) {
         if (name == null || name.trim().equals("")) {
             return false;
         }
         return true;
     }
 
     /**
      * This method returns the Component form the parent Container given the
      * name of the Component
      * 
      * @param parentContainer  the parent Container
      * @param panelName name of the Component
      * @return the desired Component if present; null otherwise
      */
     public static Component getComponentByName(final Container parentContainer, final String panelName) {
         Component requiredComponent = null;
         Component[] components = parentContainer.getComponents();
         for (Component component : components) {
             String componentName = component.getName();
             if (componentName != null && componentName.equals(panelName)) {
                 requiredComponent = component;
                 break;
             }
         }
         return requiredComponent;
     }
 
     /**
      * This Method returns the maximum dimension required to generate the label
      * for the given attributes.
      * 
      * @param attributeList
      * @return Dimension
      */
     public static Dimension getMaximumLabelDimension(Collection<AttributeInterface> attributeList) {
         Dimension maxLabelDimension = new Dimension(0, 0);
         for (AttributeInterface attribute : attributeList) {
             String formattedString = attribute.getName();
             if (!Utility.isCategory(attribute.getEntity())) {
                 formattedString = Utility.getFormattedString(formattedString);
             }
 
             Cab2bLabel tempLabel = new Cab2bLabel(formattedString + " : ");
             if (maxLabelDimension.width < tempLabel.getPreferredSize().width) {
                 maxLabelDimension = tempLabel.getPreferredSize();
             }
         }
         return maxLabelDimension;
     }
 
     /**
      * Top five popular categories are returned by this method to display on main page
      * 
      * @return Top five popular categories.
      */
     public static Collection<CategoryPopularity> getPopularSearchCategoriesForMainFrame() {
         List<CategoryPopularity> attributeList = (List<CategoryPopularity>) getPopularCategoriesForShowAll();
 
         Collection<CategoryPopularity> popularCategories = new ArrayList<CategoryPopularity>();
         if (attributeList.size() > 4) {
             for (int i = 0; i < attributeList.size(); i++) {
                 popularCategories.add(attributeList.get(i));
             }
             return popularCategories;
         } else {
             return attributeList;
         }
     }
 
     /**
      * All popular categories are returned by this method to display on the click of show all link.
      * 
      * @return All popular categories.
      */
     public static Collection<CategoryPopularity> getPopularCategoriesForShowAll() {
         Collection<CategoryPopularity> categoryList = PopularCategoryCache.getInstance().getPopularCategoriesCollection();
         List<CategoryPopularity> attributeList = new ArrayList<CategoryPopularity>(categoryList);
         Collections.sort(attributeList, new CategoryPopularityComparator());
         return attributeList;
     }
 
     /**
      * @return Recent search queries
      */
     public static Collection<IParameterizedQuery> getUserSearchQueries() {
         /*
          * TODO These default UserSearchQueries will be removed later after SAVE
          * QUERY support
          */
 
         QueryEngineBusinessInterface queryEngineBusinessInterface = (QueryEngineBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                                     EjbNamesConstants.QUERY_ENGINE_BEAN,
                                                                                                                                     QueryEngineHome.class,
                                                                                                                                     null);
         Collection<IParameterizedQuery> cab2bQueryCollection = null;
         try {
 
             cab2bQueryCollection = queryEngineBusinessInterface.getAllQueryNameAndDescription(
                                                                                               UserValidator.getSerializedDelegatedCredReference(),
                                                                                               UserValidator.getIdP());
         } catch (RemoteException exception) {
             CommonUtils.handleException(exception, NewWelcomePanel.getMainFrame(), true, true, true, false);
         } catch (Exception exception) {
             CommonUtils.handleException(exception, NewWelcomePanel.getMainFrame(), true, true, true, false);
         }
 
         return cab2bQueryCollection;
 
         /*  Vector<String> userSearchQueries = new Vector<String>();
           userSearchQueries.add("Prostate Cancer Microarray Data");
           userSearchQueries.add("Glioblastoma Microarray Data");
           userSearchQueries.add("High Quality RNA Biospecimens");
           // userSearchQueries.add("Breast Cancer Microarrays (MOE430+2.0)");
 
           userSearchQueries.add("Adenocarcinoma Specimens");
           userSearchQueries.add("Lung Primary Tumor");
           return userSearchQueries;*/
     }
 
     /**
      * @return All the experiments performed by the user.
      */
     public static Vector<Experiment> getExperiments(Component comp, UserInterface user) {
 
         ExperimentBusinessInterface expBus = (ExperimentBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                             EjbNamesConstants.EXPERIMENT,
                                                                                                             ExperimentHome.class);
         List<Experiment> experiments = null;
         try {
             experiments = expBus.getExperimentsForUser(user, UserValidator.getSerializedDelegatedCredReference(),
                                                        UserValidator.getIdP());
         } catch (RemoteException e) {
             handleException(e, comp, true, false, false, false);
         }
         Vector<Experiment> experimentVector = new Vector<Experiment>();
         for (Experiment experiment : experiments) {
             experimentVector.add(experiment);
         }
         return experimentVector;
     }
 
     /**
      * Method to set hyper-link property values
      * 
      * @param hyperlink
      * @param obj
      * @param hyperlinkText
      * @param desc
      * @param actionClass
      */
     public static <T> void setHyperlinkProperties(Cab2bHyperlink<T> hyperlink, T obj, String hyperlinkText,
                                                   String desc, ActionListener actionClass) {
         hyperlink.setClickedColor(MainFrameStackedBoxPanel.CLICKED_COLOR);
         hyperlink.setUnclickedColor(MainFrameStackedBoxPanel.UNCLICKED_COLOR);
         hyperlink.setUserObject(obj);
         hyperlink.setText(hyperlinkText);
         hyperlink.setToolTipText(desc);
         hyperlink.addActionListener(actionClass);
     }
 
     /**
      * @return Popular categories at this point
      */
     public static Collection<CategoryPopularity> getUserSearchCategories() {
         return getPopularCategoriesForShowAll();
     }
 
     public static String escapeString(String input) {
         if (input.indexOf(",") != -1) {
             input = "\"" + input + "\"";
         }
         return input;
     }
 
     /**
      * It will return number of elements in current data list
      * @return return number of elements in current data list
      */
     public static int getDataListSize() {
         IDataRow rootNode = MainSearchPanel.getDataList().getRootDataRow();
         // This node is hidden node in the tree view
         return rootNode.getChildren().size();
 
     }
 
     /**
      * This method verifies whether service URLs are available for give entity.
      * 
      * @param entity
      * @return String[]
      */
     private static String[] getServiceURLs(EntityInterface entity) {
         EntityInterface en = entity;
         if (edu.wustl.cab2b.common.util.Utility.isCategory(entity)) {
             CategoryBusinessInterface bus = (CategoryBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                          EjbNamesConstants.CATEGORY_BEAN,
                                                                                                          CategoryHomeInterface.class,
                                                                                                          null);
             Category cat = null;
             try {
                 cat = bus.getCategoryByEntityId(entity.getId());
             } catch (RemoteException e) {
                 e.printStackTrace();
             }
             en = cat.getRootClass().getCategorialClassEntity();
         }
         return UserCache.getInstance().getServiceURLs(en);
     }
 
     /**
      * This method checks whether service URL is configured for the given query.
      * If service url is not available it shows an ERROR dialog and returns
      * false, otherwise returns true.
      * 
      * @param query
      * @param container
      * @return boolean
      */
     public static boolean isServiceURLConfigured(ICab2bQuery query, Container container) {
         boolean isServiceURLConfigured = true;
 
         Set<IQueryEntity> entitySet = query.getConstraints().getQueryEntities();
         for (IQueryEntity queryEntity : entitySet) {
             EntityInterface entity = queryEntity.getDynamicExtensionsEntity();
             String[] urls = getServiceURLs(entity);
             if (urls == null || urls.length == 0) {
                 isServiceURLConfigured = false;
             } else
                 for (String url : urls) {
                     if (url.equals("")) {
                         isServiceURLConfigured = false;
                         break;
                     }
                 }
         }
         if (!isServiceURLConfigured) {
             JOptionPane.showMessageDialog(container, ErrorCodeHandler.getErrorMessage(ErrorCodeConstants.DB_0007),
                                           "Query", JOptionPane.WARNING_MESSAGE);
         }
         return isServiceURLConfigured;
     }
 
     /**
      * initializes resources like error code handler , Application Properties etc
      */
     public static void initializeResources() {
         try {
             ErrorCodeHandler.initBundle(ERROR_CODE_FILE_NAME);
             ApplicationProperties.initBundle(APPLICATION_RESOURCES_FILE_NAME);
         } catch (MissingResourceException mre) {
             CheckedException checkedException = new CheckedException(mre.getMessage(), mre,
                     ErrorCodeConstants.IO_0002);
             CommonUtils.handleException(checkedException, null, true, true, false, true);
         }
     }
 
     /**
      * set the caB2B Home used to keep edit-able configurations, application logs etc.
      */
     public static void setHome() {
 
         String userHome = System.getProperty("user.home");
 
         File cab2bHome = new File(userHome, "cab2b");
         System.setProperty("cab2b.home", cab2bHome.getAbsolutePath());
     }
 
     /**
      * Method to launch caB2B Search data wizard.
      */
     public static void launchSearchDataWizard() {
 
         // Update the variable for latest screen dimension from the
         // toolkit, this is to handle the situations where
         // application is started and then screen resolution is
         // changed, but the variable still holds old resolution
         // size.
 
         Dimension dimension = MainFrame.getScreenDimesion();
         final String title = ApplicationProperties.getValue(SEARCH_FRAME_TITLE);
 
         // Clearing the data list
         SaveDatalistPanel.setDataListSaved(false);
         MainSearchPanel mainSearchPanel = GlobalNavigationPanel.getMainSearchPanel();
         if (mainSearchPanel == null)
             mainSearchPanel = new MainSearchPanel();
 
         GlobalNavigationPanel.setMainSearchPanel(mainSearchPanel);
         MainSearchPanel.getDataList().clear();
         MainFrame mainFrame = NewWelcomePanel.getMainFrame();
         JDialog searchDialog = WindowUtilities.setInDialog(mainFrame, mainSearchPanel, title, new Dimension(
                 (int) (dimension.width * 0.90), (int) (dimension.height * 0.85)), true, true);
         mainFrame.setSearchWizardDialog(searchDialog);
         searchDialog.setVisible(true);
         GlobalNavigationPanel.setMainSearchPanel(null);
     }
 
     /**
      * @return The map of DAG image enumerations versus image object
      */
     public static Map<DagImages, Image> getDagImageMap() {
         Map<DagImages, Image> imageMap = new HashMap<DagImages, Image>();
         imageMap.put(DagImages.SelectIcon, Utilities.loadImage(SELECT_ICON_ADD_LIMIT));
         imageMap.put(DagImages.selectMOIcon, Utilities.loadImage(SELECT_ICON_ADD_LIMIT_MOUSEOVER));
         imageMap.put(DagImages.ArrowSelectIcon, Utilities.loadImage(LIMIT_CONNECT_DESELECTED));
         imageMap.put(DagImages.ArrowSelectMOIcon, Utilities.loadImage(LIMIT_CONNECT_SELECTED));
         imageMap.put(DagImages.ParenthesisIcon, Utilities.loadImage(PARENTHISIS_ICON_ADD_LIMIT));
         imageMap.put(DagImages.ParenthesisMOIcon, Utilities.loadImage(PARENTHISIS_ICON_ADD_LIMIT_MOUSEOVER));
         imageMap.put(DagImages.DocumentPaperIcon, Utilities.loadImage(PAPER_GRID_ADD_LIMIT));
         imageMap.put(DagImages.PortImageIcon, Utilities.loadImage(PORT_IMAGE_ADD_LIMIT));
         return imageMap;
     }
 
     public static String getDisplayUserName(String userIdentifier) {
         return userIdentifier.substring(userIdentifier.lastIndexOf("=") + 1);
     }
 }

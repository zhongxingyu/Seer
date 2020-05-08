 package edu.wustl.cab2b.client.ui.experiment;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.JXTree;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.common.dynamicextensions.entitymanager.EntityRecord;
 import edu.common.dynamicextensions.entitymanager.EntityRecordInterface;
 import edu.common.dynamicextensions.entitymanager.EntityRecordMetadata;
 import edu.common.dynamicextensions.entitymanager.EntityRecordResult;
 import edu.common.dynamicextensions.entitymanager.EntityRecordResultInterface;
 import edu.wustl.cab2b.client.ui.RiverLayout;
 import edu.wustl.cab2b.client.ui.WindowUtilities;
 import edu.wustl.cab2b.client.ui.charts.Cab2bChartPanel;
 import edu.wustl.cab2b.client.ui.charts.ChartType;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bFormattedTextField;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTable;
 import edu.wustl.cab2b.client.ui.controls.StackedBox;
 import edu.wustl.cab2b.client.ui.filter.CaB2BFilterInterface;
 import edu.wustl.cab2b.client.ui.filter.CaB2BPatternFilter;
 import edu.wustl.cab2b.client.ui.main.IComponent;
 import edu.wustl.cab2b.client.ui.main.ParseXMLFile;
 import edu.wustl.cab2b.client.ui.main.SwingUIManager;
 import edu.wustl.cab2b.client.ui.mainframe.MainFrame;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.client.ui.util.UserObjectWrapper;
 import edu.wustl.cab2b.common.analyticalservice.ServiceDetailsInterface;
 import edu.wustl.cab2b.common.datalist.DataListBusinessInterface;
 import edu.wustl.cab2b.common.datalist.DataListHomeInterface;
 import edu.wustl.cab2b.common.domain.Experiment;
 import edu.wustl.cab2b.common.ejb.EjbNamesConstants;
 import edu.wustl.cab2b.common.ejb.analyticalservice.AnalyticalServiceOperationsBusinessInterface;
 import edu.wustl.cab2b.common.ejb.analyticalservice.AnalyticalServiceOperationsHomeInterface;
 import edu.wustl.cab2b.common.exception.CheckedException;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.experiment.ExperimentBusinessInterface;
 import edu.wustl.cab2b.common.util.AttributeInterfaceComparator;
 import edu.wustl.cab2b.common.util.Constants;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.common.util.logger.Logger;
 
 public class ExperimentStackBox extends Cab2bPanel {
 	/** Default Serial version ID */
 	private static final long serialVersionUID = 1L;
 
 	/** panel to display filters on selected data-category */
 	private static Cab2bPanel dataFilterPanel = null;
 
 	/** panel to display analysed data on selected data-category */
 	private Cab2bPanel analyseDataPanel = null;
 
 	/** panel to display visual form of data on selected data-category */
 	private Cab2bPanel visualiseDataPanel = null;
 
 	/** stack box to add all this panels */
 	private StackedBox stackedBox;
 
 	/** ExperimentOpen Panel */
 	private ExperimentDataCategoryGridPanel m_experimentDataCategoryGridPanel = null;
 
 	private JXTree datalistTree;
 
 	private JScrollPane treeViewScrollPane;
 
 	private ExperimentBusinessInterface m_experimentBusinessInterface;
 
 	private Experiment m_selectedExperiment = null;
 
 	private String columnName[] = null;
 
 	private static CaB2BFilterInterface obj = null;
 
 	private Object recordObject[][] = null;
 
 	private Map<String, AttributeInterface> attributeMap = new HashMap<String, AttributeInterface>();
 
 	private static int chartIndex = 0;
 
 	public ExperimentStackBox(ExperimentBusinessInterface expBus, Experiment selectedExperiment) {
 		m_experimentBusinessInterface = expBus;
 		m_selectedExperiment = selectedExperiment;
 		initGUI();
 	}
 
 	public ExperimentStackBox(
 			ExperimentBusinessInterface expBus,
 			Experiment selectedExperiment,
 			ExperimentDataCategoryGridPanel experimentDataCategoryGridPanel) {
 		m_experimentBusinessInterface = expBus;
 		m_selectedExperiment = selectedExperiment;
 		m_experimentDataCategoryGridPanel = experimentDataCategoryGridPanel;
 		initGUI();
 	}
 
 	public void initGUI() {
 		this.setLayout(new BorderLayout());
 		stackedBox = new StackedBox();
 		stackedBox.setTitleBackgroundColor(new Color(200, 200, 220));
 
 		Set<EntityInterface> entitySet = null;
 		try {
 			entitySet = m_experimentBusinessInterface.getDataListEntitySet(m_selectedExperiment);
 		} catch (RemoteException remoteException) {
 			CommonUtils.handleException(remoteException, MainFrame.newWelcomePanel, true, true, true, false);
 		}
 
 		DefaultMutableTreeNode rootNode = generateRootNode(entitySet);
 		initializeDataListTree(rootNode);
 
 		initializePanels();
 	}
 
 	/**
 	 * This method generates a root node given the set of entities
 	 * 
 	 * @param entitySet
 	 *            set of entities
 	 * @return root node
 	 */
 	private DefaultMutableTreeNode generateRootNode(Set<EntityInterface> entitySet) {
 		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Experiment Data Categories");
 		DefaultMutableTreeNode node = null;
 
 		// for each datalist
 		for (EntityInterface dummyEntity : entitySet) {
 			EntityInterface entity = null;
 			// get the only child of the dummy entity, which would be the real
 			// first level entity in the tree
 			for (AssociationInterface association : dummyEntity.getAssociationCollection()) {
 				entity = association.getTargetEntity();
 			}
 
 			TreeEntityWrapper treeEntityWrapper = new TreeEntityWrapper();
 			treeEntityWrapper.setEntityInterface(entity);
 			node = new DefaultMutableTreeNode(treeEntityWrapper);
 			rootNode.add(node);
 			addChildren(rootNode, node);
 		}
 
 		return rootNode;
 	}
 
 	/**
 	 * This method initalize the datalist tree given the root node
 	 * 
 	 * @param rootNode
 	 *            the root node to be set.
 	 */
 	private void initializeDataListTree(DefaultMutableTreeNode rootNode) {
 		// creating datalist tree
 		datalistTree = new JXTree(rootNode);
 
 		// setting the first node as selected and displaying the corresponding
 		// records in the table
 		if (datalistTree.getRowCount() >= 2) {
 			datalistTree.setSelectionRow(1);
 			datalistTree.expandRow(1);
 			CustomSwingWorker swingWorker = new CustomSwingWorker(datalistTree) {
 				protected void doNonUILogic() throws RuntimeException {
 					Object nodeInfo = ((DefaultMutableTreeNode) datalistTree.getLastSelectedPathComponent()).getUserObject();
 					if (nodeInfo instanceof TreeEntityWrapper) {
 						TreeEntityWrapper experimentEntity = (TreeEntityWrapper) nodeInfo;
 						getDataCategoyRecords(experimentEntity);
 						addAvailableAnalysisServices(experimentEntity);
 					}
 				}
 
 				protected void doUIUpdateLogic() throws RuntimeException {
 					m_experimentDataCategoryGridPanel.refreshTable(columnName, recordObject, attributeMap);
 				}
 			};
 			swingWorker.start();
 		}
 
 		// action listener for selected data category
 		datalistTree.addTreeSelectionListener(new TreeSelectionListener() {
 			public void valueChanged(TreeSelectionEvent e) {
 				treeSelectionListenerAction();
 			}
 		});
 	}
 
 	/**
 	 * This method initialzse the different UI panels and panes.
 	 */
 	private void initializePanels() {
 		// Adding Select data category pane
 		treeViewScrollPane = new JScrollPane(datalistTree);
 		treeViewScrollPane.setPreferredSize(new Dimension(250, 200));
 		stackedBox.addBox("Select Data Category", treeViewScrollPane, "mysearchqueries_icon.gif");
 
 		// Adding Filter data category panel
 		dataFilterPanel = new Cab2bPanel();
 		dataFilterPanel.setPreferredSize(new Dimension(250, 200));
 		dataFilterPanel.setOpaque(false);
 		stackedBox.addBox("Filter Data ", dataFilterPanel, "mysearchqueries_icon.gif");
 
 		// Adding Analyse data panel
 		analyseDataPanel = new Cab2bPanel();
 		analyseDataPanel.setPreferredSize(new Dimension(250, 150));
 		analyseDataPanel.setOpaque(false);
 		stackedBox.addBox("Analyze Data ", analyseDataPanel, "mysearchqueries_icon.gif");
 
 		// Adding Visualize data panel
 		visualiseDataPanel = new Cab2bPanel();
 		visualiseDataPanel.setPreferredSize(new Dimension(250, 200));
 		visualiseDataPanel.setOpaque(false);
 		stackedBox.addBox("Visualize Data ", visualiseDataPanel, "mysearchqueries_icon.gif");
 
 		// Set the type of charts to be displayed.
 		Vector<ChartType> chartTypes = new Vector<ChartType>();
 		chartTypes.add(ChartType.BAR_CHART);
 		chartTypes.add(ChartType.LINE_CHART);
 		chartTypes.add(ChartType.SCATTER_PLOT);
 		setChartTypesForVisualiseDataPanel(chartTypes);
 
		stackedBox.setPreferredSize(new Dimension(250, 500));
		stackedBox.setMinimumSize(new Dimension(250, 500));
		this.add(stackedBox);
 	}
 
 	/**
 	 * find and add children of the given tree node child node is added to the
 	 * current node if it is filtered, otherwise it is added to the root node
 	 * 
 	 * @param node
 	 *            the tree node
 	 */
 	private void addChildren(DefaultMutableTreeNode rootNode, DefaultMutableTreeNode node) {
 		EntityInterface entity = ((TreeEntityWrapper) node.getUserObject()).getEntityInterface();
 
 		for (AssociationInterface association : entity.getAssociationCollection()) {
 			EntityInterface targetEntity = association.getTargetEntity();
 			TreeEntityWrapper child = new TreeEntityWrapper();
 			child.setEntityInterface(targetEntity);
 			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
 
 			Collection<TaggedValueInterface> taggedValues = targetEntity.getTaggedValueCollection();
 
 			if (Utility.getTaggedValue(taggedValues, Constants.FILTERED) != null) {
 				node.add(childNode);
 			} else {
 				rootNode.add(childNode);
 			}
 
 			addChildren(rootNode, childNode);
 		}
 	}
 
 	/**
 	 * Method to perform tree node selection action for currently selected node
 	 */
 	public void treeSelectionListenerAction() {
 		CustomSwingWorker swingWorker = new CustomSwingWorker(datalistTree) {
 			protected void doNonUILogic() throws RuntimeException {
 				Cab2bPanel selectedPanel = (Cab2bPanel)m_experimentDataCategoryGridPanel.getTabComponent().getSelectedComponent();
                 Component applyFilterComponent = CommonUtils.getComponentByName(selectedPanel, Constants.APPLY_FILTER_PANEL_NAME); 
                 if(applyFilterComponent instanceof ApplyFilterPanel){
                 	ApplyFilterPanel applyFilterPanel = (ApplyFilterPanel)applyFilterComponent;
                 	applyFilterPanel.clearMap();
                 	getDataForFilterPanel(applyFilterPanel);
                 }
 				updateUI();
 
 				DefaultMutableTreeNode node = (DefaultMutableTreeNode) datalistTree.getLastSelectedPathComponent();
 				if (node == null) {
 					return;
 				}
 
 				// if rot node is selected, clear all table content
 				if (node.isRoot() == true) {
 					columnName = null;
 					recordObject = null;
 				}
 
 				Object nodeInfo = node.getUserObject();
 				if (nodeInfo instanceof TreeEntityWrapper) {
 					TreeEntityWrapper experimentEntity = (TreeEntityWrapper) nodeInfo;
 					getDataCategoyRecords(experimentEntity);
 					addAvailableAnalysisServices(experimentEntity);
 				}
 			}
 
 			protected void doUIUpdateLogic() throws RuntimeException {
 				m_experimentDataCategoryGridPanel.refreshTable(columnName, recordObject, attributeMap);
 			}
 		};
 		swingWorker.start();
 	}
 
 	/**
 	 * Method used to get dataCategory records from database and covert it into
 	 * table format
 	 */
 	private void getDataCategoyRecords(TreeEntityWrapper nodeInfo) {
 		EntityInterface entityNode = nodeInfo.getEntityInterface();
 
 		// getting datalist entity interface
 		DataListBusinessInterface dataListBI = (DataListBusinessInterface) CommonUtils.getBusinessInterface(
 																											EjbNamesConstants.DATALIST_BEAN,
 																											DataListHomeInterface.class);
 		EntityRecordResultInterface recordResultInterface = null;
 		try {
 			recordResultInterface = dataListBI.getEntityRecord(entityNode.getId());
 
 			// getting list of attributes
 			List<AbstractAttributeInterface> headerList = recordResultInterface.getEntityRecordMetadata().getAttributeList();
 			columnName = new String[headerList.size()];
 			int i = 0;
 			for (AbstractAttributeInterface abstractAttribute : headerList) {
 				AttributeInterface attribute = (AttributeInterface) abstractAttribute;
 				columnName[i++] = CommonUtils.getFormattedString(attribute.getName());
 				attributeMap.put(columnName[i - 1], attribute);
 				Logger.out.debug("Table Header :" + attribute.getName());
 			}
 
 			// getting actual records
 			List<EntityRecordInterface> recordList = recordResultInterface.getEntityRecordList();
 			recordObject = new Object[recordList.size()][headerList.size()];
 			Logger.out.debug("Record Size :: " + recordList.size());
 			i = 0;
 			int j = 0;
 			for (EntityRecordInterface record : recordList) {
 				List recordValueList = record.getRecordValueList();
 				j = 0;
 				for (Object object : recordValueList) {
 					recordObject[i][j] = new Object();
 					recordObject[i][j] = object;
 					Logger.out.debug("Data [" + i + "]" + "[" + j + "]" + recordObject[i][j]);
 					j++;
 				}
 				i++;
 			}
 		} catch (RemoteException remoteException) {
 			CommonUtils.handleException(remoteException, MainFrame.newWelcomePanel, true, true, true, false);
 		}
 	}
 
 	public static void setDataForFilterPanel(Vector<CaB2BFilterInterface> data) {
 		Logger.out.debug("setDataForMyExperimentsPanel :: data " + data);
 		dataFilterPanel.removeAll();
 		dataFilterPanel.add(new Cab2bLabel());
 		for (CaB2BFilterInterface caB2BFilterInterface : data) {
 			obj = caB2BFilterInterface;
 			String hyperlinkName = obj.toString();
 			Cab2bHyperlink hyperlink = new Cab2bHyperlink();
 			hyperlink.setBounds(new Rectangle(5, 5, 5, 5));
 			hyperlink.setText(hyperlinkName);
 			hyperlink.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (obj instanceof CaB2BPatternFilter) {
 						CaB2BPatternFilter filter = (CaB2BPatternFilter) obj;
 						System.out.println(filter.getPattern().pattern());
 					}
 				}
 			});
 			dataFilterPanel.add("br", hyperlink);
 		}
 		dataFilterPanel.revalidate();
 	}
 
 	public static void getDataForFilterPanel(ApplyFilterPanel applyFilterPanel) {
 		Vector<CaB2BFilterInterface> vector = applyFilterPanel.getFilterMap();
 		if (vector != null) {
 			setDataForFilterPanel(vector);
 		}
 	}
 
 	/**
 	 * This method displays the type of chart in the Visualize Data panel which
 	 * is at the left bottom of the screen.
 	 * 
 	 * @param chartTypes
 	 *            name of charts to be displayed.
 	 */
 	private void setChartTypesForVisualiseDataPanel(Vector<ChartType> chartTypes) {
 		Logger.out.debug("setChartTypesForVisualiseDataPanel :: chartTypes " + chartTypes);
 		visualiseDataPanel.removeAll();
 
 		for (ChartType chartType : chartTypes) {
 			Cab2bHyperlink hyperlink = new Cab2bHyperlink();
 			hyperlink.setBounds(new Rectangle(5, 5, 5, 5));
 			hyperlink.setText(chartType.getActionCommand());
 			hyperlink.setActionCommand(chartType.getActionCommand());
 
 			// String iconName = hyperlinkName.trim().replace(' ', '_') +
 			// "_icon.gif";
 			// hyperlink.setIcon(new ImageIcon("resources/images/" + iconName));
 			URL url = this.getClass().getClassLoader().getResource("mysearchqueries_icon.gif");
 			hyperlink.setIcon(new ImageIcon(url));
 
 			hyperlink.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent actionEvent) {
 					String actionCommand = actionEvent.getActionCommand();
 					ChartType chartType = ChartType.getChartType(actionCommand);
 					showChartAction(chartType);
 					updateUI();
 				}
 			});
 			hyperlink.setEnabled(false);
 			visualiseDataPanel.add("br", hyperlink);
 		}
 		visualiseDataPanel.revalidate();
 	}
 
 	/**
 	 * This method displays the chart selected in the Chart tab.
 	 * 
 	 * @param linkClicked
 	 *            the name of the chart to be displayed
 	 */
 	private void showChartAction(ChartType chartType) {
 		Cab2bTable cab2bTable = m_experimentDataCategoryGridPanel.getCurrentTable();
 
 		String entityName = null;
 		Object nodeInfo = ((DefaultMutableTreeNode) datalistTree.getLastSelectedPathComponent()).getUserObject();
 		if (nodeInfo instanceof TreeEntityWrapper) {
 			entityName = ((TreeEntityWrapper) nodeInfo).toString();
 		}
 
 		// Cab2bChartPanel cab2bChartPanel = null;
 		final JTabbedPane tabComponent = m_experimentDataCategoryGridPanel.getTabComponent();
 		Cab2bPanel currentChartPanel = m_experimentDataCategoryGridPanel.getCurrentChartPanel();
 		if (currentChartPanel == null) {
 			final Cab2bPanel newVisualizeDataPanel = new Cab2bPanel();
 			newVisualizeDataPanel.setLayout(new RiverLayout());
 			newVisualizeDataPanel.setBorder(null);
 
 			Cab2bButton closeButton = new Cab2bButton("Close");
 			closeButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent actionEvent) {
 					tabComponent.remove(newVisualizeDataPanel);
 				}
 			});
 			newVisualizeDataPanel.add("right ", closeButton);
 
 			Cab2bChartPanel cab2bChartPanel = new Cab2bChartPanel(cab2bTable);
 			cab2bChartPanel.setChartType(chartType, entityName);
 			newVisualizeDataPanel.add("br hfill vfill ", cab2bChartPanel);
 			m_experimentDataCategoryGridPanel.setCurrentChartPanel(newVisualizeDataPanel);
 
 			tabComponent.add("Chart" + ++chartIndex, newVisualizeDataPanel);
 			tabComponent.setSelectedComponent(newVisualizeDataPanel);
 		} else {
 			Cab2bChartPanel cab2bChartPanel = (Cab2bChartPanel) currentChartPanel.getComponent(0);
 			cab2bChartPanel.setChartType(chartType, entityName);
 			tabComponent.setSelectedComponent(currentChartPanel);
 		}
 	}
 
 	/**
 	 * update the tree, add the newly created entity to the current selection
 	 * 
 	 * @param newEntity
 	 */
 	public void updateStackBox(EntityInterface newEntity) {
 		// gets the currently selected node
 		DefaultMutableTreeNode node = (DefaultMutableTreeNode) datalistTree.getLastSelectedPathComponent();
 
 		TreeEntityWrapper newNodeWrapper = new TreeEntityWrapper();
 		newNodeWrapper.setEntityInterface(newEntity);
 		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newNodeWrapper);
 		node.add(newNode);
 
 		datalistTree.updateUI();
 	}
 
 	/**
 	 * This method adds all the available services as link in analysis panel
 	 * 
 	 * @param experimentEntity
 	 */
 	private void addAvailableAnalysisServices(TreeEntityWrapper treeEntityWrapper) {
 		final EntityInterface dataEntity = treeEntityWrapper.getEntityInterface();
 
 		// TODO This is a hack. To be deleted after testing.
 		EntityInterface entityInterface = DomainObjectFactory.getInstance().createEntity();
 		entityInterface.setName("gov.nih.nci.mageom.domain.BioAssay.BioAssay");
 
 		AnalyticalServiceOperationsBusinessInterface analyticalServiceOperationsBusinessInterface = (AnalyticalServiceOperationsBusinessInterface) CommonUtils.getBusinessInterface(
 																																													EjbNamesConstants.ANALYTICAL_SERVICE_BEAN,
 																																													AnalyticalServiceOperationsHomeInterface.class,
 																																													null);
 		List<ServiceDetailsInterface> serviceDetailInterfaceList = null;
 		try {
 			serviceDetailInterfaceList = analyticalServiceOperationsBusinessInterface.getApplicableAnalyticalServices(entityInterface);
 
 			analyseDataPanel.removeAll();
 			for (ServiceDetailsInterface serviceDetails : serviceDetailInterfaceList) {
 				addHyperLinkToAnalyticalPane(serviceDetails, dataEntity);
 			}
 		} catch (RemoteException remoteException) {
 			CommonUtils.handleException(remoteException, MainFrame.newWelcomePanel, true, true, true, false);
 		}
 	}
 
 	/**
 	 * This method adds a hyperlink to the Analytical Panel for the given
 	 * service
 	 * 
 	 * @param serviceDetails
 	 * @param dataEntity
 	 */
 	private void addHyperLinkToAnalyticalPane(ServiceDetailsInterface serviceDetails,
 												final EntityInterface dataEntity) {
 		String serviceName = serviceDetails.getDisplayName();
 
 		Cab2bHyperlink hyperlink = new Cab2bHyperlink();
 		hyperlink.setBounds(new Rectangle(5, 5, 5, 5));
 		hyperlink.setText(serviceName);
 		hyperlink.setActionCommand(serviceName);
 		hyperlink.setUserObject(serviceDetails);
 
 		hyperlink.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				Cab2bHyperlink hyperlink = (Cab2bHyperlink) (actionEvent.getSource());
 
 				ServiceDetailsInterface serviceDetails = (ServiceDetailsInterface) hyperlink.getUserObject();
 				List<EntityInterface> requiredEntityList = serviceDetails.getRequiredEntities();
 				for (EntityInterface requiredEntity : requiredEntityList) {
 					// TODO compare with entity id instead of name
 					// if(entity.getId() != requiredEntity.getId() &&
 					// !requiredEntity.getAbstractAttributeCollection().isEmpty())
 					// {
 					if (!dataEntity.getName().equals(requiredEntity.getName())
 							&& !requiredEntity.getAbstractAttributeCollection().isEmpty()) {
 						serviceSelectAction(serviceDetails, requiredEntity, dataEntity);
 					}
 				}
 			}
 		});
 		analyseDataPanel.add("br", hyperlink);
 	}
 
 	/**
 	 * The action listener for the individual service elements.
 	 * 
 	 * @param dataEntity
 	 * @param entityId
 	 * @param serviceDetailsInterface
 	 * @param actionEvent
 	 *            The event that contains details of the click on the individual
 	 *            service.
 	 */
 	private void serviceSelectAction(ServiceDetailsInterface serviceDetails, final EntityInterface requiredEntity,
 										final EntityInterface dataEntity) {
 		// Create panel for Analysis Title
 		JComponent tilteLabel = new Cab2bLabel("Analysis Title" + " : ");
 		JComponent titleField = new Cab2bFormattedTextField(20, Cab2bFormattedTextField.PLAIN_FIELD);
 		Cab2bPanel titlePanel = new Cab2bPanel();
 		titlePanel.setName("titlePanel");
 		titlePanel.add("br", tilteLabel);
 		titlePanel.add("tab", titleField);
 
 		// Create panels for service parameters
 		Cab2bPanel parameterPanel = getParameterPanels(requiredEntity);
 		JScrollPane jScrollPane = new JScrollPane();
 		jScrollPane.setName("jScrollPane");
 		jScrollPane.setBorder(BorderFactory.createLineBorder(Color.black, 1));
 		jScrollPane.getViewport().add(parameterPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		jScrollPane.getViewport().setBackground(Color.WHITE);
 
 		// Create finish button
 		Cab2bButton finishButton = new Cab2bButton("Finish");
 		finishButton.addActionListener(new FinishButtonActionListner(serviceDetails, dataEntity,
 				m_experimentDataCategoryGridPanel));
 
 		Cab2bPanel servicePanel = new Cab2bPanel();
 		servicePanel.add("br left ", titlePanel);
 		servicePanel.add("br center hfill vfill ", jScrollPane);
 		servicePanel.add("br right ", finishButton);
 
 		String displayName = CommonUtils.getFormattedString(requiredEntity.getName());
 		WindowUtilities.showInDialog(NewWelcomePanel.mainFrame, servicePanel, displayName,
 										Constants.WIZARD_SIZE2_DIMENSION, true, false);
 	}
 
 	/**
 	 * This method returns the parameter panel that has the controls to accept
 	 * the parameter values.
 	 * 
 	 * @param entity
 	 *            the entity for which the control panels are to be displayed
 	 * @return Array of control panel
 	 */
 	private Cab2bPanel getParameterPanels(final EntityInterface entity) {
 		ParseXMLFile parseFile = null;
 		try {
 			parseFile = ParseXMLFile.getInstance();
 		} catch (CheckedException checkedException) {
 			CommonUtils.handleException(checkedException, this, true, true, false, false);
 		}
 
 		final Collection<AttributeInterface> attributeCollection = entity.getAttributeCollection();
 		Cab2bPanel parameterPanel = new Cab2bPanel();
 		parameterPanel.setName("parameterPanel");
 		if (attributeCollection != null) {
 			List<AttributeInterface> attributeList = new ArrayList<AttributeInterface>(attributeCollection);
 			Collections.sort(attributeList, new AttributeInterfaceComparator());
 
 			try {
 
 				Dimension maxLabelDimension = CommonUtils.getMaximumLabelDimension(attributeList);
 				for (AttributeInterface attribute : attributeList) {
 					JXPanel jxPanel = (JXPanel) SwingUIManager.generateUIPanel(parseFile, attribute, false,
 																				maxLabelDimension);
 					parameterPanel.add("br", jxPanel);
 				}
 			} catch (CheckedException checkedException) {
 				CommonUtils.handleException(checkedException, this, true, true, false, false);
 			}
 		}
 
 		return parameterPanel;
 	}
 
 	/**
 	 * @return the visualiseDataPanel
 	 */
 	public Cab2bPanel getVisualiseDataPanel() {
 		return visualiseDataPanel;
 	}
 
 	/**
 	 * @param visualiseDataPanel
 	 *            the visualiseDataPanel to set
 	 */
 	public void setVisualiseDataPanel(Cab2bPanel visualiseDataPanel) {
 		this.visualiseDataPanel = visualiseDataPanel;
 	}
 }
 
 /**
  * This Listener class adds a row in the table on the Analysis tab.
  * 
  * @author chetan_patil
  */
 class FinishButtonActionListner implements ActionListener {
 	private ServiceDetailsInterface serviceDetails;
 
 	private EntityInterface dataEntity;
 
 	private ExperimentDataCategoryGridPanel experimentDataCategoryGridPanel;
 
 	/**
 	 * Parameterized constructor
 	 * 
 	 * @param serviceDetails
 	 * @param dataEntity
 	 * @param experimentDataCategoryGridPanel
 	 */
 	public FinishButtonActionListner(
 			ServiceDetailsInterface serviceDetails,
 			EntityInterface dataEntity,
 			ExperimentDataCategoryGridPanel experimentDataCategoryGridPanel) {
 		super();
 		this.serviceDetails = serviceDetails;
 		this.dataEntity = dataEntity;
 		this.experimentDataCategoryGridPanel = experimentDataCategoryGridPanel;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	public void actionPerformed(ActionEvent actionEvent) {
 		MainFrame.setStatus(MainFrame.Status.BUSY);
 		MainFrame.setStatusMessage("Analysing the data...");
 
 		Cab2bButton finishButton = (Cab2bButton) actionEvent.getSource();
 		Cab2bPanel servicePanel = (Cab2bPanel) finishButton.getParent();
 		closeDialogWindow(servicePanel);
 
 		// Obtain the analysis title
 		Cab2bPanel titlePanel = (Cab2bPanel) CommonUtils.getComponentByName(servicePanel, "titlePanel");
 		Cab2bFormattedTextField titleField = (Cab2bFormattedTextField) titlePanel.getComponents()[1];
 		String analysisTitle = titleField.getText();
 
 		// Obtain values of service parameters
 		JScrollPane jScrollPane = (JScrollPane) CommonUtils.getComponentByName(servicePanel, "jScrollPane");
 		Cab2bPanel parameterPanel = (Cab2bPanel) CommonUtils.getComponentByName(jScrollPane.getViewport(),
 																				"parameterPanel");
 		List<EntityRecordResultInterface> serviceParameterValues = collectServiceParameterValues(parameterPanel.getComponents());
 
 		// Obtain values from grid
 		List<EntityRecordResultInterface> dataValues = getGridData();
 
 		EntityRecordResultInterface entityRecordResult = null;
 		AnalyticalServiceOperationsBusinessInterface analyticalServiceOperationsBusinessInterface = (AnalyticalServiceOperationsBusinessInterface) CommonUtils.getBusinessInterface(
 																																													EjbNamesConstants.ANALYTICAL_SERVICE_BEAN,
 																																													AnalyticalServiceOperationsHomeInterface.class,
 																																													null);
 		try {
 			entityRecordResult = analyticalServiceOperationsBusinessInterface.invokeService(serviceDetails,
 																							dataValues,
 																							serviceParameterValues);
 			updateAnalysisTable(analysisTitle, entityRecordResult);
 		} catch (RemoteException remoteException) {
 			CommonUtils.handleException(remoteException, MainFrame.newWelcomePanel, true, true, true, false);
 		}
 
 		MainFrame.setStatus(MainFrame.Status.READY);
 		MainFrame.setStatusMessage("Analysis finished");
 	}
 
 	/**
 	 * This method refreshes the table of analysis in the Analysis tab. It
 	 * displays the values of entityRecordResult in the tablular form.
 	 * 
 	 * @param analysisTitle
 	 *            the analysis title to display as link value in the table
 	 * @param entityRecordResult
 	 *            the EntityRecordResult whose values are to be displayed in a
 	 *            single row of table.
 	 */
 	private void updateAnalysisTable(final String analysisTitle,
 										final EntityRecordResultInterface entityRecordResult) {
 		UserObjectWrapper<EntityRecordResultInterface> userObjectWrapper = new UserObjectWrapper<EntityRecordResultInterface>(
 				entityRecordResult, analysisTitle);
 		String entityName = CommonUtils.getFormattedString(dataEntity.getName());
 		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
 		String currentDate = simpleDateFormat.format(new Date());
 		String progress = "Completed";
 
 		Object[][] analysisTableData = new Object[][] { { entityName, userObjectWrapper, currentDate, progress } };
 
 		experimentDataCategoryGridPanel.refreshAnalysisTable(analysisTableData);
 	}
 
 	/**
 	 * This method closes the currnet dialog window
 	 * 
 	 * @param servicePanel
 	 */
 	private void closeDialogWindow(final Cab2bPanel servicePanel) {
 		Container container = servicePanel.getParent();
 		container = container.getParent();
 		container = container.getParent();
 		container = container.getParent();
 		if (container instanceof JDialog) {
 			JDialog jDialog = (JDialog) container;
 			jDialog.dispose();
 		}
 	}
 
 	/**
 	 * This method collects the values form the data grid.
 	 * 
 	 * @return List of EntityRecordResultInterface which has the data grid
 	 *         values
 	 */
 	private List<EntityRecordResultInterface> getGridData() {
 		Cab2bTable cab2bTable = experimentDataCategoryGridPanel.getTable();
 
 		List<EntityRecordInterface> entityRecordList = new ArrayList<EntityRecordInterface>();
 		for (int row = 0; row < cab2bTable.getRowCount(); row++) {
 			List<Object> valueList = new ArrayList<Object>();
 			for (int column = 0; column < cab2bTable.getColumnCount(); column++) {
 				Object value = cab2bTable.getValueAt(row, column);
 				valueList.add(value);
 			}
 			EntityRecordInterface entityRecord = new EntityRecord();
 			entityRecord.setRecordValueList(valueList);
 
 			entityRecordList.add(entityRecord);
 		}
 
 		return generateEntityRecordResultList(entityRecordList);
 	}
 
 	/**
 	 * This method collects the values from the service popup window
 	 * 
 	 * @param controlPanels
 	 *            parameter paneles of the service pop window
 	 * @return List of EntityRecordResultInterface which has the parameter
 	 *         values
 	 */
 	private List<EntityRecordResultInterface> collectServiceParameterValues(final Component[] controlPanels) {
 		List<String> valueList = new ArrayList<String>();
 		for (Component controlPanel : controlPanels) {
 			String value = null;
 			ArrayList<String> parameterValues = ((IComponent) controlPanel).getValues();
 			if (parameterValues.size() > 0) {
 				value = parameterValues.get(0);
 			}
 			valueList.add(value);
 		}
 
 		EntityRecordInterface entityRecord = new EntityRecord();
 		entityRecord.setRecordValueList(valueList);
 
 		List<EntityRecordInterface> entityRecordList = new ArrayList<EntityRecordInterface>();
 		entityRecordList.add(entityRecord);
 
 		return generateEntityRecordResultList(entityRecordList);
 	}
 
 	/**
 	 * This method generates the List of EntityRecordResultInterface given a
 	 * List of EntityRecordInterface
 	 * 
 	 * @param entityRecordList
 	 *            List of EntityRecordInterface
 	 * @return List of EntityRecordResultInterface
 	 */
 	private List<EntityRecordResultInterface> generateEntityRecordResultList(
 																				final List<EntityRecordInterface> entityRecordList) {
 		EntityRecordMetadata entityRecordMetadata = new EntityRecordMetadata();
 
 		List<AttributeInterface> attributeList = new ArrayList<AttributeInterface>(
 				dataEntity.getAttributeCollection());
 		Collections.sort(attributeList, new AttributeInterfaceComparator());
 		entityRecordMetadata.setAttributeList(new ArrayList<AbstractAttributeInterface>(attributeList));
 
 		EntityRecordResultInterface entityRecordResult = new EntityRecordResult();
 		entityRecordResult.setEntityRecordList(entityRecordList);
 		entityRecordResult.setEntityRecordMetadata(entityRecordMetadata);
 
 		List<EntityRecordResultInterface> entityRecordResultList = new ArrayList<EntityRecordResultInterface>();
 		entityRecordResultList.add(entityRecordResult);
 
 		return entityRecordResultList;
 	}
 
 }

 
 package edu.wustl.cab2b.client.ui.viewresults;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.border.EmptyBorder;
 
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.JXTitledPanel;
 import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.client.ui.MainSearchPanel;
 import edu.wustl.cab2b.client.ui.RiverLayout;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTitledPanel;
 import edu.wustl.cab2b.client.ui.mainframe.GlobalNavigationPanel;
 import edu.wustl.cab2b.client.ui.pagination.JPagination;
 import edu.wustl.cab2b.client.ui.pagination.NumericPager;
 import edu.wustl.cab2b.client.ui.pagination.PageElement;
 import edu.wustl.cab2b.client.ui.pagination.PageElementImpl;
 import edu.wustl.cab2b.client.ui.pagination.Pager;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.common.datalist.DataRow;
 import edu.wustl.cab2b.common.datalist.IDataRow;
 import edu.wustl.cab2b.common.ejb.EjbNamesConstants;
 import edu.wustl.cab2b.common.ejb.queryengine.QueryEngineBusinessInterface;
 import edu.wustl.cab2b.common.ejb.queryengine.QueryEngineHome;
 import edu.wustl.cab2b.common.queryengine.result.IQueryResult;
 import edu.wustl.common.querysuite.metadata.associations.IAssociation;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * A Panel to show the results of the query operation. Provides 
  * functionality to make selection of the displayed results and
  * add it to the data list.
  *  
  * @author chetan_bh
  */
 public class ViewSearchResultsSimplePanel extends Cab2bPanel
 {
 
 	JXPanel breadCrumbPanel;
 
 	//JTextField searchTextField;
 	//JButton searchButton;
 	JButton addToDataListButton;
 	JButton m_applyAllButton;
 
 	JXPanel searchResultsPanel;
 
 	/**
 	 * Query result object. 
 	 */
 	IQueryResult queryResult;
 
 	/**
 	 * Query result in a format required by JPagination component.
 	 */
 	Vector<PageElement> elements = null;
 
 	/**
 	 * myDataListPanel and myDataListTitledPanel  
 	 * caontains a summary of data items added to the data list
 	 */
 	static JXPanel myDataListPanel;
 	static JXTitledPanel myDataListTitledPanel;
 
 	ActionListener breadCrumbsAL;
 	JXPanel breadCrumbsPanel;
 
 	ActionListener hyperlinkAL;
 
 	DataRow parentDataRow;
 
 	ViewSearchResultsPanel viewPanel;
 
 	EntityInterface presentEntityInterface = null;
 	IAssociation queryAssociation = null;
 	JXPanel m_addSummaryParentPanel;
 
 	public ViewSearchResultsSimplePanel(IAssociation association, IQueryResult queryResult, ActionListener bCAL,
 			ActionListener hLAL, DataRow parentDataRow, ViewSearchResultsPanel viewPanel,
 			EntityInterface presentEntityInterface)
 	{
 		this.viewPanel = viewPanel;
 		this.queryResult = queryResult;
 		this.breadCrumbsAL = bCAL;
 		this.hyperlinkAL = hLAL;
 		queryAssociation = association;
 
 		// Parent data row will be null for the first query's results, but will be non-null for associated class query's results. 
 		this.parentDataRow = parentDataRow;
 		this.presentEntityInterface = presentEntityInterface;
 		initData();
 		initGUI();
 	}
 	/**
 	 * Method to add My data list summary panel to    
 	 *
 	 */
 	public void addDataSummaryPanel()
 	{
 		m_addSummaryParentPanel.add(myDataListTitledPanel, BorderLayout.EAST);
 	}
 	
 	/**
 	 * Initializes the data needed for <code>JPagination</code> component.
 	 */	
 	private void initData()
 	{
 		elements = new Vector<PageElement>();
 
 		Map<String, String[][]> allRecords = queryResult.getAllRecords();
 		Iterator ittr = allRecords.keySet().iterator();
 		while (ittr.hasNext())
 		{
 
 			/* Set the url for each data row.*/
 			String urlKey = (String) ittr.next();
 
 			Object[][] results = allRecords.get(urlKey);
 
 			//String className = queryResult.getAttributes().get(0).getEntity().getName();
 			//edu.wustl.cab2b.common.util.Utility.getDisplayName(queryResult.getAttributes().get(0).getEntity());
 			String className = edu.wustl.cab2b.common.util.Utility.getDisplayName(queryResult.getAttributes().get(0).getEntity());
 			Logger.out.info(className);
 			if (className == null || className.length() == 0)
 			{
 				/* Get the class name from the attributes, if the above is not set on the server.*/
 				className = this.getClassNameFromIattribute();
 			}
 
 			/* Initialize the count for number of attributes to be shown in the */
 			int attributeSize = queryResult.getAttributes().size();
 			int attributeLimitInDescStr = (attributeSize < 5) ? attributeSize : 5;
 			for (int i = 0; i < results.length; i++)
 			{
 				//Logger.out.info("CLASS NAME : " + className);
 				Object[] row = results[i];
 				PageElement element = new PageElementImpl();
 				element.setDisplayName(className + "_" + (i + 1));
 
 				String descStr = "";
 				for (int j = 0; j < attributeLimitInDescStr; j++)
 				{
 					if (row[j] != null)
 					{
 						if (j == attributeLimitInDescStr - 1)
 						{
 							descStr += row[j];
 						}
 						else
 						{
 							descStr += row[j] + ", ";
 						}
 					}
 				}
 				element.setDescription(descStr);
 
 				DataRow dataRow = new DataRow();
 				List<AttributeInterface> attributes = queryResult.getAttributes();
 
 				AttributeInterface attrib = attributes.get(0);
 
 				/*
 				 * Get the EntityInterface from the map only if the last parameter is null. 
 				 * This should ideally happen only the first time
 				 */
 
 				if (presentEntityInterface == null)
 				{
 					presentEntityInterface = attrib.getEntity();
 				}
 
 				//set proper class display name
 				String strclassName = edu.wustl.cab2b.common.util.Utility.getDisplayName(presentEntityInterface);
 
 				int identifierIndex = CommonUtils.getIdAttributeIndexFromAttributes(attributes);
 
 				Object id = row[identifierIndex];
 				dataRow.setRow(row);
 				dataRow.setAttributes(attributes);
 				dataRow.setClassName(strclassName);
 				dataRow.setParent(parentDataRow);
 				dataRow.setId(id);
 				dataRow.setAssociation(queryAssociation);
 				dataRow.setEntityInterface(presentEntityInterface);
 				dataRow.setURL(urlKey);				
 				element.setUserObject(dataRow);
 				elements.add(element);
 			}
 		}
 	}
 
 	private String getClassNameFromIattribute()
 	{
 		String strClassName = null;
 		List attributes = queryResult.getAttributes();
 		if (attributes != null && attributes.size() > 0)
 		{
 
 			AttributeInterface attribute = (AttributeInterface) attributes.get(0);
 			//strClassName = edu.wustl.common.util.Utility.parseClassName(attribute.getEntity().getName());
 			strClassName = 	edu.wustl.cab2b.common.util.Utility.getDisplayName(attribute.getEntity());
 
 		}
 		return strClassName;
 	}
 
 	/**
 	 * Initializes the GUI for showing query results.
 	 */
 	private void initGUI()
 	{
 		this.setLayout(new RiverLayout());
 
 		/**
 		 * Add the following selectively
 		 */
 		final JXTitledPanel titledSearchResultsPanel = new Cab2bTitledPanel("Search Results :- "
 				+ "Total results ( " + elements.size() + " )");
 		GradientPaint gp = new GradientPaint(new Point2D.Double(.05d, 0), new Color(185, 211, 238),
 				new Point2D.Double(.95d, 0), Color.WHITE);
 		titledSearchResultsPanel.setTitlePainter(new BasicGradientPainter(gp));
 		titledSearchResultsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
 		titledSearchResultsPanel.setTitleFont(new Font("SansSerif", Font.BOLD, 11));
 		titledSearchResultsPanel.setTitleForeground(Color.BLACK);
 		
 		searchResultsPanel = new Cab2bPanel();
 		searchResultsPanel.setLayout(new RiverLayout());
 		
 		Pager pager = new NumericPager(elements);
 		final JPagination pagination = new JPagination(elements, pager, this, true);
 
 		pagination.addPageElementActionListener(hyperlinkAL);
 		pagination.setPreferredSize(new Dimension(300,410));
 		searchResultsPanel.add("vfill hfill", pagination);
 		initDataListSummaryPanel();	
 
 		addToDataListButton = new Cab2bButton("Add To Data List");
 		addToDataListButton.setPreferredSize(new Dimension(140, 22));
 		addToDataListButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				List selectedUserObjects = pagination.getSelectedPageElementsUserObjects();
 				MainSearchPanel.dataList.addDataRows(selectedUserObjects);
 				updateMyDataListPanel();
 				updateUI();
//				JOptionPane.showMessageDialog(titledSearchResultsPanel, "Added " + selectedUserObjects.size() + " elements to data list" , "Information",
//						JOptionPane.INFORMATION_MESSAGE);
 			}	
 			
 		});
 		searchResultsPanel.add("br br", addToDataListButton);
 		
 		// Add Apply All button to apply currently added datalist options
 		// to the currently selected objects.
 		m_applyAllButton =  new Cab2bButton("Apply Data List");
 		m_applyAllButton.setPreferredSize(new Dimension(130, 22));
 		m_applyAllButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				// Perform apply all action
 				List selectedUserObjects = pagination.getSelectedPageElementsUserObjects();
 				if((selectedUserObjects.size() > 0)&& (false == MainSearchPanel.dataList.isTreeEmpty()))
 				{
 					performApplyAllAction(selectedUserObjects, (JComponent)titledSearchResultsPanel);
 					
 				}
 			}
 		});
 		searchResultsPanel.add("tab tab", m_applyAllButton);
 		m_addSummaryParentPanel = new Cab2bPanel();
 		m_addSummaryParentPanel.setLayout(new BorderLayout());
 		m_addSummaryParentPanel.add(searchResultsPanel, BorderLayout.CENTER);	
 		m_addSummaryParentPanel.add(myDataListTitledPanel, BorderLayout.EAST);
 		titledSearchResultsPanel.setContentContainer(m_addSummaryParentPanel);
 		this.add("p vfill hfill", titledSearchResultsPanel);	
 	}
 	
 	/**
 	 * Method to Initialize Data list summary panel 
 	 *
 	 */	
 	public static void initDataListSummaryPanel()
 	{
 			Logger.out.info("In initDataListSummaryPanel method");
 			if(myDataListTitledPanel==null)
 			{
 				Logger.out.info("In if myDataListTitledPanel==null");
 				// TODO externalize these titles.
 				myDataListTitledPanel = new Cab2bTitledPanel("My Data List Summary");
 				GradientPaint gp1 = new GradientPaint(new Point2D.Double(.05d, 0),
 				new Color(185, 211, 238), new Point2D.Double(.95d, 0), Color.WHITE);
 				myDataListTitledPanel.setTitlePainter(new BasicGradientPainter(gp1));
 
 				if(myDataListPanel ==  null)
 				{
 					myDataListPanel = new Cab2bPanel();
 					myDataListPanel.setBackground(Color.WHITE);
 					myDataListPanel.setLayout(new RiverLayout(5, 10));
 				}
 				else
 				{
 					myDataListPanel.removeAll();
 				}
 		
 				myDataListTitledPanel.setContentContainer(myDataListPanel);
 				myDataListTitledPanel.setTitleFont(new Font("SansSerif", Font.BOLD, 11));
 				myDataListTitledPanel.setTitleForeground(Color.BLACK);
 			
 				//setting the scroll bar
 				JScrollPane myDataListPane = new JScrollPane(myDataListPanel);
 				myDataListPane.getViewport().setBackground(Color.WHITE);
 				myDataListTitledPanel.add(myDataListPane);
 			}
 			else
 			{
 				if(myDataListPanel != null)
 				{
 					myDataListPanel.removeAll();
 				}
 			}
 			updateMyDataListPanel();
 	}
 	
 	
 	/**
      * Method to perform apply all action
      * for currently selected objects 
      */
 	public static void performApplyAllAction(final List selectedUserObjects, final JComponent component)
 	{
 		/* Get result by executing the Query in a worker thread. */
         CustomSwingWorker swingWorker = new CustomSwingWorker((JXPanel)component)
         {
 			@Override
 			protected void doNonUILogic() throws RuntimeException
 			{
 				// Get the path of current entity
 				List<IDataRow> pathEnitites = new ArrayList<IDataRow>();
 				IDataRow dataRow = (IDataRow) selectedUserObjects.get(0);
 				while(dataRow != null)
 				{
 					pathEnitites.add(0, dataRow);
 					dataRow = dataRow.getParent();
 				}	
 				List<IDataRow> entityTreetoFetch = MainSearchPanel.dataList.getTreeForApplyAll(pathEnitites);
 				if(entityTreetoFetch.size() == 0)
 					return;
 				// For every selected entity fetch corresponding data
 				// from data services and add it to data list
 				QueryEngineBusinessInterface queryEngineBus = (QueryEngineBusinessInterface)CommonUtils.getBusinessInterface(
 						EjbNamesConstants.QUERY_ENGINE_BEAN, 
 						QueryEngineHome.class, component);
 				List<IDataRow> parentRows = new ArrayList<IDataRow>();
 				for(int i=0; i<selectedUserObjects.size(); i++)
 				{
 					parentRows.add((IDataRow) selectedUserObjects.get(i));
 				}
 				Collection<Callable<QueryResultObject>> queryCallables = new ArrayList<Callable<QueryResultObject>>();
 				List<IDataRow> childRows = entityTreetoFetch.get(0).getChildren();
 				for(int i=0; i<parentRows.size(); i++)
 				{
 					MainSearchPanel.dataList.addDataRow(parentRows.get(i));
 					for(int j=0; j<childRows.size(); j++)
 					{
 						queryCallables.add(new QueryExecutionCallable(parentRows.get(i), childRows.get(j), queryEngineBus, childRows.get(j).getChildren()));
 					}
 				}
 				fetchApplyAllResults(queryCallables, queryEngineBus);
 			}				
 			@Override
 			protected void doUIUpdateLogic() throws RuntimeException
 			{
 				// TODO Auto-generated method stub
 				updateMyDataListPanel();
 				JOptionPane.showMessageDialog(component, "Apply All operation completed successfully", "Information",
 						JOptionPane.INFORMATION_MESSAGE);
 			}
         };
         swingWorker.start();
 	}
 	
 	/**
 	 * Method to fetch results for apply. This method spawns threads to execute each query seperately
 	 */
 	public static void fetchApplyAllResults(Collection<Callable<QueryResultObject>> queryCallables, QueryEngineBusinessInterface queryEngineBus)
 	{
 		do
 		{
 			ExecutorService executorService = Executors.newFixedThreadPool(10);
 			try 
 			{
 				List<Future<QueryResultObject>> results = executorService.invokeAll(queryCallables);
 				queryCallables.clear();
 				for (Future<QueryResultObject> future : results) 
 				{
 					QueryResultObject queryResult = future.get();
 					if(queryResult != null)
 					{
 						List<IDataRow> parentRows = queryResult.getResults();
 						List<IDataRow> childRows =  queryResult.getChilds();
 						for(int i=0; i<parentRows.size(); i++)
 						{
 							MainSearchPanel.dataList.addDataRow(parentRows.get(i));
 							for(int j=0; j<childRows.size(); j++)
 							{
 								queryCallables.add(new QueryExecutionCallable(parentRows.get(i), childRows.get(j), 
 										queryEngineBus, childRows.get(j).getChildren()));
 							}
 						}
 					}
 				}
 			} 
 			catch (InterruptedException e)
 			{
 				Logger.out.warn("Unable to get results : " + e.getMessage());
 				break;
 			}
 			catch (ExecutionException e) 
 			{
 				Logger.out.warn("Unable to get results : " + e.getMessage());
 				break;
 			}
 		}while(0 < queryCallables.size());
 	}
 	
 	/**
 	 * Updates My DataList Summary panel present on view Search result page 
 	 *
 	 */
 	public static void updateMyDataListPanel()
 	{		
 		final List <IDataRow> datalistTree = MainSearchPanel.dataList.getDataList();
 		//removing all previously added hyperlinks
 		ViewSearchResultsSimplePanel.myDataListPanel.removeAll();		
 		IDataRow rootNode = datalistTree.get(0); // This node is hidden node in the tree view		
 		for(int i=0;i<rootNode.getChildren().size();i++)
 		{
 	   		final IDataRow currentNode = rootNode.getChildren().get(i);
 	   		if(currentNode.isData())
 	   		{
 	   			createHyperlink(currentNode);	   			
 	   		}
 	   		else
 	   		{
 	   			for(int k=0; k< currentNode.getChildren().size(); k++)	
 	    		{
 	   				createHyperlink(currentNode.getChildren().get(k));
 	    		}
 	   		}
 		}
 		ViewSearchResultsSimplePanel.myDataListPanel.add("br ", new Cab2bLabel("      "));	
 	}	
 	
 	/**
 	 * Add hyperlinks for the datalist result into My Data List Summary panel      
 	 * @param row
 	 */
 	private static void createHyperlink(final IDataRow row)
 	{
 		Cab2bHyperlink selectedRootClassName = new Cab2bHyperlink();
 		
 		DataRow dataRow = (DataRow)row;
 		//edu.wustl.cab2b.common.util.Utility.getDisplayName(dataRow.getEntityInterface())
 		String displayClassName =  dataRow.getClassName();	
 		selectedRootClassName.setText(displayClassName + "_" + dataRow.getId());		    				
 		selectedRootClassName.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				GlobalNavigationPanel.mainSearchPanel.getNavigationPanel().gotoDataListPanel(row);		    						
 			}			
 		});			
 		ViewSearchResultsSimplePanel.myDataListPanel.add("br ", selectedRootClassName);
 	}
 }

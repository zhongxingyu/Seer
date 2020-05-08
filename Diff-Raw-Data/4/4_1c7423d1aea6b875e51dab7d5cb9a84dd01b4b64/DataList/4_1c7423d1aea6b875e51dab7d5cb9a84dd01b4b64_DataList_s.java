 /**
  * <p>Title: DataList Class>
  * <p>Description:  Class which represents the data list.</p>
  * Copyright: Copyright (c) year
  * Company: Washington University, School of Medicine, St. Louis.
  * @author Gautam Shetty
  * @version 1.00
  */
 package edu.wustl.cab2b.common.datalist;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import edu.wustl.cab2b.common.domain.DataListMetadata;
 
 
 /**
  * Class which represents the data list.
  * @author gautam_shetty
  */
 public class DataList implements Serializable
 {
     private static final long serialVersionUID = 3683597366517167529L;
 
 	/**
      * Annotation about the the datalist like name, description, etc.
      */
 	DataListMetadata dataListAnnotation;
 	
     /**
      * First level data in the datalist.
      */
     List<IDataRow> dataList = new ArrayList<IDataRow>();
     
     public DataList()
     {
     	addRootNode();
     }
     
     private void addRootNode()
     {
     	 IDataRow dataRow = new DataRow();
     	 dataRow.setClassName("My data list element");
     	 dataRow.setAttributes(null);
     	 dataRow.setData(false);
     	 dataList.add(dataRow);
     }
     
     /**
      * @return Returns the dataList.
      */
     public List<IDataRow> getDataList()
     {
         return dataList;
     }
     
     /**
      * Adds the collection of data rows in the datalist.
      * @param dataRows The collection of data rows.
      */
     public void addDataRows(Collection dataRows)
     {
         Iterator iterator = dataRows.iterator();
         while (iterator.hasNext())
         {
             IDataRow dataRow = (IDataRow) iterator.next();
             addDataRow(dataRow);
         }
     }
     
     /**
      * Adds the data row into the data list.
      * @param dataRow The DataRow to be added. 
      */
     public void addDataRow(IDataRow dataRow)
     {
         if (dataRow.getParent() == null)
         {
             addToList(dataRow, dataList.get(0).getChildren());
         }
         else
         {
         	//IDataRow parentRow = getDataRow(dataRow.getParent(), dataList);
         	IDataRow parentRow = getDataRow(dataRow.getParent(), dataList.get(0).getChildren());
         	if (parentRow != null)
         	{
         		addToList(dataRow, parentRow.getChildren());
         	}
         	else
         		// Here parent is not added to the list
         		// need to add parent to the list and then add this node to the list
         	{
         		addDataRow(dataRow.getParent());
         		addDataRow(dataRow);
         	}
         }
     }
     
     /**
      * Method to add node in the datalist (Tree containing added data elements)
      * @param dataRow Node to add
      * @param dataRowList List where node will be added
      */
     private void addToList(IDataRow dataRow, List<IDataRow> dataRowList)
     {
     	if (!dataRowList.isEmpty())
         {
             for(int i=0; i<dataRowList.size(); i++)
             {
             	IDataRow parentData = dataRowList.get(i);
             	if(parentData.getAttributes() != null)
             	{
             		// If node type is same as the node to add
             		if(true == parentData.getAttributes().get(0).getEntity().equals(dataRow.getAttributes().get(0).getEntity()))
             		{
             			if(parentData.isData() == false) // means this node is not a data node
             			{
             				// If node is already added to datalist
             				if(false == isNodePresent(parentData.getChildren(), dataRow))
             				{
             					parentData.getChildren().add(dataRow);
             				}
             			}
             			else // create title node
             			{
             				// If node to add is different than already existing node
             				// then create title node and move both the nodes down
             				if(false == parentData.equals(dataRow))
             				{
             					IDataRow parentDataRow = getTitleNode(parentData);
             					parentDataRow.getChildren().add(parentData);
             					parentDataRow.getChildren().add(dataRow);
             					dataRowList.add(parentDataRow);
             					dataRowList.remove(parentData);
             				}
             			}
             			return;
             		}
             		
             	}
             }
             //  just add node to this list
            	dataRowList.add(dataRow);
         }
         else
         {
             if(dataRow.getParent() == null)
             {
             	IDataRow parentDataRow = getTitleNode(dataRow);
             	parentDataRow.getChildren().add(dataRow);
             	dataRowList.add(parentDataRow);
             }
             else
             {
             	dataRowList.add(dataRow);
             }
         }
     }
     /**
      * Method to check if this node is present in  the list
      * @param dataRowList
      * @param dataRow
      * @return
      */
     private boolean isNodePresent(List<IDataRow> dataRowList, IDataRow dataRow)
     {
     	 for(int i=0; i<dataRowList.size(); i++)
          {
          	IDataRow row = dataRowList.get(i);
          	if(true == row.equals(dataRow))
          	{
          		return true;
          	}
          }
     	 return false;
     	
     }
     /**
      * Returns the group node for this data row
      * @param dataRow The node for which to construct group node
      * @return The group node for this dataRow
      */
     private IDataRow getTitleNode(IDataRow dataRow)
     {
         IDataRow titleDataRow = new DataRow();
         titleDataRow.setClassName(dataRow.getClassName());
         titleDataRow.setData(false);
         titleDataRow.setAttributes(dataRow.getAttributes());
         return titleDataRow;
     }
     
     /**
      * Returns this DataRow object from the data list.
      * @return this DataRow object from the data list.
      */
     private IDataRow getDataRow(IDataRow searchDataRow, List<IDataRow> dataList)
     {
         // Get the path of current entity
     	IDataRow actualRow = searchDataRow;
 		List<IDataRow> pathEnitites = new ArrayList<IDataRow>();
 		while(searchDataRow != null)
 		{
 			pathEnitites.add(0, searchDataRow);
 			searchDataRow = searchDataRow.getParent();
 		}	
 		//	Find this entity in the current datalist first
     	List<IDataRow> entityDataList = new ArrayList<IDataRow>();
     	findNodeInDataList(0, dataList, entityDataList, pathEnitites);
     	for(int i=0; i<entityDataList.size(); i++)
         {
     		if(true == actualRow.equals(entityDataList.get(i)))
         	{
     			return entityDataList.get(i);
         	}
         }
     	return null;
     }
     
     /**
      * This method finds given path in the data list. It finds the path recurrsive
      * @param pos Position of element in path list to search
      * @param currentDataList Datalist element in which to perform this the element in path list specified by pos location 
      * @param listWithEntity Reference list in which matched path element end point will be saved
      * @param pathEnitites The path to search
      */
     private void findNodeInDataList(int pos, List<IDataRow> currentDataList, List<IDataRow>listWithEntity, List<IDataRow> pathEnitites)
     {
     	for(int i=0; i<currentDataList.size(); i++)
     	{
     		IDataRow currentRow = currentDataList.get(i);
     		if(currentRow.isData() == true)
     		{
     			if(true == currentRow.equals(pathEnitites.get(pos)))
     			{
     				// Found that path has been completely matched
     				if(pos == pathEnitites.size()-1)
     				{
     					listWithEntity.add(currentRow);
     				}
     				else if(pos<pathEnitites.size())
     				{
     					List<IDataRow> childDataList = currentRow.getChildren();
    					getDataRowWithEntity(pos+1, childDataList, listWithEntity, pathEnitites);
     				}
     			}
     		}
     		else
     		{
     			findNodeInDataList(pos, currentRow.getChildren(), listWithEntity, pathEnitites);
     		}
     	}
     }
     /**
      * Returns this DataRow object from the data list.
      * @return this DataRow object from the data list.
      */
     private void getDataRowWithEntity(int pos, List<IDataRow> currentDataList, List<IDataRow>listWithEntity, List<IDataRow> pathEnitites)
     {
     	for(int i=0; i<currentDataList.size(); i++)
     	{
     		IDataRow currentRow = currentDataList.get(i);
     		if(currentRow.isData() == true)
     		{
     			if(true == currentRow.getAttributes().get(0).getEntity().equals(
 					pathEnitites.get(pos).getAttributes().get(0).getEntity()))
     			{
     				// Found that path has been completely matched
     				if(pos == pathEnitites.size()-1)
     				{
     					listWithEntity.add(currentRow);
     				}
     				else if(pos<pathEnitites.size())
     				{
     					List<IDataRow> childDataList = currentRow.getChildren();
     					getDataRowWithEntity(pos+1, childDataList, listWithEntity, pathEnitites);
     				}
     			}
     		}
     		else
     		{
     			getDataRowWithEntity(pos, currentRow.getChildren(), listWithEntity, pathEnitites);
     		}
     	}
     }
     
     /**
      * Method to get the tree containing the hirarchy for given entity
      * This tree will be used to fetch objects similar to the objects
      * present in the data list summary
      * @param entity
      */
     public List<IDataRow> getTreeForApplyAll(List<IDataRow> pathEnitites)
     {
     	// Find this entity in the current datalist first
     	List<IDataRow> entityDataList = new ArrayList<IDataRow>();
     	getDataRowWithEntity(0, dataList.get(0).getChildren(), entityDataList, pathEnitites);
     	
     	//  ArrayList holding actual tree to traverse
     	List<IDataRow> treeList = new ArrayList<IDataRow>();
     	Iterator<IDataRow> iter = entityDataList.iterator();
     	while(iter.hasNext())
     	{
     		addPathToTree(treeList, iter.next());
     	}
     	printTree(0, treeList);
     	return treeList;
     }
     /**
      * This method finds the path for 'Apply Data List' functionality
      * @param treeList The list containing paths for which to execute queries for 'Apply Data List' functionality
      * @param rootNode The element to search
      */
     private void addPathToTree(List<IDataRow> treeList, IDataRow rootNode)
     {
     	int posInList = -1;
     	// Check if tree already have this path starting with given root
     	for(int i=0; i<treeList.size(); i++)
     	{
     		if(true == treeList.get(i).getAttributes().get(0).getEntity().equals(rootNode.getAttributes().get(0).getEntity()))
     		{
     			posInList = i;
     			break;
     		}
     	}
     	IDataRow dataRow;
     	if(posInList == -1)
     	{
     		dataRow = new DataRow();
     		dataRow.setAttributes(rootNode.getAttributes());
     		dataRow.setClassName(rootNode.getClassName());
     		dataRow.setAssociation(rootNode.getAssociation());
     		treeList.add(dataRow);
     	}
     	else
     	{
     		dataRow = treeList.get(posInList);
     	}
     	addInternalNodesToPath(rootNode, dataRow);
     	
     }
     
     /**
      * Utility method to print tree nodes 
      * @param level the level of the node to print
      * @param treeList List containing tree hierarchy
      */
     private void printTree(int level, List<IDataRow> treeList)
     {
     	for(int i=0; i<treeList.size(); i++)
     	{
     		System.out.println("Level: " + level + " class:  "  + treeList.get(i).getClassName());
     		if(treeList.get(i).getAssociation() != null)
     		{
     			System.out.println("Level: " + level + " Association:  "  + treeList.get(i).getAssociation().toString());
     		}
     		printTree(level+1, treeList.get(i).getChildren());
     	}
     }
     /**
      * Recurrsive method to find location of perticular node in tree and return path for same
      * @param rootNode The root of the node from which to start search
      * @param dataRow The node to search
      */
     private void addInternalNodesToPath(IDataRow rootNode, IDataRow dataRow)
     {
     	//  Traverse the path and store it in the list accordingly
     	if(rootNode.getChildren().size()>0)
     	{
     		List<IDataRow> rootChildNodes = rootNode.getChildren();
     		for(int i=0; i<rootChildNodes.size(); i++)
     		{
     			// For the data noode check if element to search is present
     			if(rootChildNodes.get(i).isData() == true)
         		{
     				List<IDataRow> childNodes =  dataRow.getChildren();
     				IDataRow node;
     				int pos = -1;
     				for(int j=0; j<childNodes.size(); j++)
     				{
     					// If this entity is already present
     					if(true == childNodes.get(j).getAttributes().get(0).getEntity().equals(rootChildNodes.get(i).getAttributes().get(0).getEntity()))
     					{
     						pos = j;
     						break;
     					}
     				}
     				if(pos == -1)
     				{
     					node = new DataRow();
     					node.setAttributes(rootChildNodes.get(i).getAttributes());
     					node.setClassName(rootChildNodes.get(i).getClassName());
     					node.setAssociation(rootChildNodes.get(i).getAssociation());
     					dataRow.getChildren().add(node);
     				}
     				else
     				{
     					node = childNodes.get(pos);
     				}
     				addInternalNodesToPath(rootChildNodes.get(i), node);
         		}
     			else
     			{
     				addInternalNodesToPath(rootChildNodes.get(i), dataRow);
     			}
        		}
     	}
     }
     
     /**
      * Method to check if tree is empty
      * @return
      */
     public boolean isTreeEmpty()
     {
     	if(dataList.get(0).getChildren().size()>0)
     		return false;
     	else
     		return true;
     }
     /**
      * Method to clear datalist &
      * be ready for nextset of savings
      */
     public void clear()
     {
     	//Clear the data list and then again add root node to it
     	dataList.clear();
     	addRootNode();
     }
 
 	
 	public DataListMetadata getDataListAnnotation()
 	{
 		return dataListAnnotation;
 	}
 
 	
 	public void setDataListAnnotation(DataListMetadata dataListAnnotation)
 	{
 		this.dataListAnnotation = dataListAnnotation;
 	}
 }

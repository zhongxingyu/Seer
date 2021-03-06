 /*******************************************************************************
  * Copyright (c) 2004, 2005 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.birt.data.engine.executor.transform.group;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import org.eclipse.birt.data.engine.core.DataException;
 import org.eclipse.birt.data.engine.executor.BaseQuery;
 import org.eclipse.birt.data.engine.executor.cache.ResultSetCache;
 import org.eclipse.birt.data.engine.executor.cache.SortSpec;
 import org.eclipse.birt.data.engine.executor.transform.ResultSetPopulator;
 import org.eclipse.birt.data.engine.i18n.ResourceConstants;
 import org.eclipse.birt.data.engine.odi.IQuery;
 import org.eclipse.birt.data.engine.odi.IResultClass;
 
 /**
  * The instance of this class is used by CachedResultSet to deal with
  * group-related data transformation operations.
  */
 
 public class GroupCalculationUtil
 {
 
 	private BaseQuery query;
 
 	private IResultClass rsMeta;
 
 	/**
 	 * Group definitions. Each entry has name of key column plus interval
 	 * information. groupKeys[0] is the highest group level
 	 */
 	private GroupBy[] groupDefs;
 
 	/** data rows holds real data */
 	private ResultSetCache smartCache;
 
 	private GroupInformationUtil groupInformationUtil;
 
 	/*
 	 * groups[level] is an ArrayList of GroupInfo objects at the specified
 	 * level. Level is a 0-based group index, with 0 denoting the outermost
 	 * group, etc. Example: Row GroupKey1 GroupKey2 GroupKey3 Column4 Column5 0:
 	 * CHINA BEIJING 2003 Cola $100 1: CHINA BEIJING 2003 Pizza $320 2: CHINA
 	 * BEIJING 2004 Cola $402 3: CHINA SHANGHAI 2003 Cola $553 4: CHINA SHANGHAI
 	 * 2003 Pizza $223 5: CHINA SHANGHAI 2004 Cola $226 6: USA CHICAGO 2004
 	 * Pizza $133 7: USA NEW YORK 2004 Cola $339 8: USA NEW YORK 2004 Cola $297
 	 * 
 	 * groups: (parent, child) LEVEL 0 LEVEL 1 LEVEL 2
 	 * ============================================ 0: -,0 0,0 0,0 1: -,2 0,2
 	 * 0,2 2: 1,4 1,3 3: 1,5 1,5 4: 2,6 5: 3,7
 	 */
 
 	private ResultSetPopulator resultPopoulator;
 	
 	/**
 	 * 
 	 * @param query
 	 * @param rsMeta
 	 */
 	GroupCalculationUtil(BaseQuery query, IResultClass rsMeta,
 			ResultSetPopulator resultPopoulator)
 	{
 		this.query = query;
 		this.rsMeta = rsMeta;
 		groupInformationUtil = new GroupInformationUtil( this );
 		this.resultPopoulator = resultPopoulator;
 	}
 
 	/**
 	 * @param inputStream
 	 * @param rsMeta
 	 * @param rsCache
 	 * @throws DataException
 	 */
 	GroupCalculationUtil( InputStream inputStream, IResultClass rsMeta,
 			ResultSetCache rsCache ) throws DataException
 	{
 		try
 		{
 			this.groupInformationUtil.readGroupsFromStream( inputStream );
 		}
 		catch ( IOException e )
 		{
 			throw new DataException( ResourceConstants.RD_LOAD_ERROR,
 					e,
 					"Group Info" );
 		}
 
 		this.rsMeta = rsMeta;
 		this.smartCache = rsCache;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public GroupInformationUtil getGroupInformationUtil( )
 	{
 		return this.groupInformationUtil;
 	}
 
 	/**
 	 * @param inputStream
 	 * @throws DataException 
 	 */
 	public void doSave( OutputStream outputStream ) throws DataException
 	{
 		try
 		{
 			this.groupInformationUtil.saveGroupsToStream( outputStream );
 		}
 		catch ( IOException e )
 		{
 			throw new DataException( ResourceConstants.RD_SAVE_ERROR,
 					e,
 					"Group Information" );
 		}
 	}
 
 	/**
 	 * Everytime in CachedResultSet the result set cache changed, it must be
 	 * reset to GroupCalculationUtil. Set the value of smartCache.
 	 * 
 	 * @param rsc
 	 */
 	public void setResultSetCache( ResultSetCache rsc )
 	{
 		this.smartCache = rsc;
 	}
 
 	/**
 	 * Gets group count
 	 * 
 	 * @return number of groupKeys
 	 */
 	int getGroupCount( )
 	{
 		return groupDefs.length;
 	}
 
 	/**
 	 * Sort the group array according to the values in sortKeys[] of
 	 * GroupBoundaryInfo intances. within them.
 	 * 
 	 * @param groupArray
 	 */
 	void sortGroupBoundaryInfos( ArrayList[] groupArray )
 	{
 		for ( int i = 0; i < groupArray.length; i++ )
 		{
 			Object[] toBeSorted = new Object[groupArray[i].size( )];
 			for ( int j = 0; j < toBeSorted.length; j++ )
 			{
 				toBeSorted[j] = groupArray[i].get( j );
 			}
 			Arrays.sort( toBeSorted, new GroupBoundaryInfoComparator( ) );
 			groupArray[i].clear( );
 			for ( int j = 0; j < toBeSorted.length; j++ )
 			{
 				groupArray[i].add( toBeSorted[j] );
 			}
 		}
 	}
 
 	public BaseQuery getQuery( )
 	{
 		return this.query;
 	}
 
 	/**
 	 * This method is used to filter out the GroupBoundaryInfo instances that
 	 * are marked as "not accepted" from GroupBoundaryInfos.
 	 * 
 	 * @param groupArray
 	 * @return
 	 */
 	ArrayList[] filterGroupBoundaryInfos( ArrayList[] groupArray )
 	{
 		ArrayList[] result = new ArrayList[groupArray.length];
 		for ( int i = 0; i < result.length; i++ )
 		{
 			result[i] = new ArrayList( );
 		}
 		for ( int i = 0; i < groupArray.length; i++ )
 		{
 			for ( int j = 0; j < groupArray[i].size( ); j++ )
 			{
 				if ( ( (GroupBoundaryInfo) groupArray[i].get( j ) ).isAccpted( ) )
 				{
 					result[i].add( groupArray[i].get( j ) );
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	GroupBy[] getGroupDefn( )
 	{
 		return this.groupDefs;
 	}
 
 	ResultSetCache getResultSetCache( )
 	{
 		return this.smartCache;
 	}
 
 	/**
 	 * Performs data transforms and starts the iterator
 	 */
 	public void initGroupSpec( ) throws DataException
 	{
 		// Set up the GroupDefs structure
 		IQuery.GroupSpec[] groupSpecs = query.getGrouping( );
 		if ( groupSpecs != null )
 		{
 			groupDefs = new GroupBy[groupSpecs.length];
 			for ( int i = 0; i < groupSpecs.length; ++i )
 			{
 				int keyIndex = groupSpecs[i].getKeyIndex( );
 				String keyColumn = groupSpecs[i].getKeyColumn( );
 
 				if (resultPopoulator.getEventHandler() != null
 						&& resultPopoulator.getEventHandler().isRowID(keyIndex,
 								keyColumn)) {
 					groupDefs[i] = GroupBy.newInstanceForRowID(groupSpecs[i]);
 					continue;
 				}
 
 				// Convert group key name to index for faster future access
 				// assume priority of keyColumn is higher than keyIndex
 				if ( keyColumn != null )
 					keyIndex = rsMeta.getFieldIndex( keyColumn );
 
 				if ( keyIndex < 1 || keyIndex > rsMeta.getFieldCount( ) )
 				{
 					// Invalid group key name
 					throw new DataException( ResourceConstants.INVALID_GROUP_KEY_COLUMN,
 							keyColumn );
 				}
 				groupDefs[i] = GroupBy.newInstance( groupSpecs[i],
 						keyIndex,
 						keyColumn,
 						rsMeta.getFieldValueClass( keyIndex ) );
 			}
 		}
 		else
 		{
 			groupDefs = new GroupBy[0];
 		}
 	}
 
 	/**
 	 * Sort data rows by group information and sort specification
 	 * 
 	 * @throws DataException
 	 */
 	public SortSpec getSortSpec( ) throws DataException
 	{
 		assert groupDefs != null;
 
 		// Create an array of column indexes. We first sort by Group keys, then
 		// sort keys
 		int groupCount = 0;
 		int sortCount = 0;
 
 		for ( int i = 0; i < groupDefs.length; i++ )
 		{
 			// When group column is rowid, sort on it should not
 			// take effect.
 			if ( groupDefs[i].getColumnIndex( ) >= 0 )
 			{
 				groupCount++;
 			}
 		}
 
 		if ( query.getOrdering( ) != null )
 		{
 			sortCount = query.getOrdering( ).length;
 		}
 
 		int[] sortKeyIndexes = new int[groupCount + sortCount];
 		String[] sortKeyColumns = new String[groupCount + sortCount];
 		boolean[] sortAscending = new boolean[groupCount + sortCount];
 
 		for ( int i = 0; i < groupCount; i++ )
 		{
 			int index = groupDefs[i].getColumnIndex( );
 			if ( index >= 0 )
 			{
 				sortKeyIndexes[i] = groupDefs[i].getColumnIndex( );
 				sortKeyColumns[i] = groupDefs[i].getColumnName( );
 				sortAscending[i] = true;/*groupDefs[i].getGroupSpec( )
 						.getSortDirection( ) != IQuery.GroupSpec.SORT_DESC;*/
 			}
 		}
 		for ( int i = 0; i < sortCount; i++ )
 		{
 			int keyIndex = query.getOrdering( )[i].getIndex( );
 			String keyName = query.getOrdering( )[i].getField( );
 
 			// If sort key name exist (not null) then depend on key name, else
 			// depend on key index
 			if ( keyName != null )
 				keyIndex = rsMeta.getFieldIndex( keyName );
 
 //			if ( keyIndex < 1 || keyIndex > rsMeta.getFieldCount( ) )
 //				// Invalid sort key name
 //				throw new DataException( ResourceConstants.INVALID_KEY_COLUMN,
 //						keyName );
 			sortKeyIndexes[groupCount + i] = keyIndex;
 			sortKeyColumns[groupCount + i] = keyName;
 			sortAscending[groupCount + i] = query.getOrdering( )[i].isAscendingOrder( );
 		}
 
 		return new SortSpec( sortKeyIndexes, sortKeyColumns, sortAscending );
 	}
 }
 
 /**
  * Structure to hold a group instance with its startIndex, endIndex, filter
  * result, sortKeys and Sort directions.
  * 
  */
 final class GroupBoundaryInfo
 {
 
 	// The start index and end index of a Group
 	private int startIndex;
 	private int endIndex;
 
 	// Used by Group sorting
 	private Object[] sortKeys;
 	private boolean[] sortDirections;
 
 	// Used by Group filtering
 	private boolean accept = true;
 
 	/**
 	 * @param start
 	 *            The start index of a group
 	 * @param end
 	 *            The end index of a group
 	 */
 	GroupBoundaryInfo( int start, int end )
 	{
 		this.startIndex = start;
 		this.endIndex = end;
 		sortKeys = new Object[0];
 	}
 
 	/**
 	 * Return the start index.
 	 * 
 	 * @return
 	 */
 	int getStartIndex( )
 	{
 		return this.startIndex;
 	}
 
 	/**
 	 * Return the end index.
 	 * 
 	 * @return
 	 */
 	int getEndIndex( )
 	{
 		return this.endIndex;
 	}
 
 	/**
 	 * Detect whether the given GroupBoundaryInfo consists of the startIdx and
 	 * endIdx that included in current GroupBoundaryInfo instance.
 	 * 
 	 * @param gbi
 	 * @return
 	 */
 	boolean isInBoundary( GroupBoundaryInfo gbi )
 	{
 		if ( gbi.getStartIndex( ) >= this.getStartIndex( )
 				&& gbi.getEndIndex( ) <= this.getEndIndex( ) )
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * Set the sort conditions
 	 * 
 	 * @param sortKeys
 	 * @param sortOrderings
 	 */
 	void setSortCondition( Object[] sortKeys, boolean[] sortOrderings )
 	{
 		this.sortKeys = sortKeys;
 		this.sortDirections = sortOrderings;
 	}
 
 	/**
 	 * Return the sort keys array.
 	 * 
 	 * @return
 	 */
 	Object[] getSortKeys( )
 	{
 		return this.sortKeys;
 	}
 
 	/**
 	 * Return the sort direction array.
 	 * 
 	 * @return
 	 */
 	boolean[] getSortDirection( )
 	{
 		return this.sortDirections;
 	}
 
 	/**
 	 * Set the filter value of GroupBoundaryInfo.
 	 * 
 	 * @param accept
 	 */
 	void setAccepted( boolean accept )
 	{
 		this.accept = accept;
 	}
 
 	/**
 	 * Return whether the GroupBoundaryInfo intance is accpeted or not.
 	 * 
 	 * @return
 	 */
 	boolean isAccpted( )
 	{
 		return this.accept;
 	}
 
 }
 
 /**
  * The Comparator instance which is used to compare two GroupBoundaryInfo
  * instance.
  * 
  */
 final class GroupBoundaryInfoComparator implements Comparator
 {
 
 	/**
 	 * 
 	 */
 
 	public int compare( Object o1, Object o2 )
 	{
 		Object[] sortKeys1 = ( (GroupBoundaryInfo) o1 ).getSortKeys( );
 		Object[] sortKeys2 = ( (GroupBoundaryInfo) o2 ).getSortKeys( );
 		boolean[] sortDirection = ( (GroupBoundaryInfo) o1 ).getSortDirection( );
 
 		int result = 0;
 		for ( int i = 0; i < sortKeys1.length; i++ )
 		{
 			result = compareTwoValues( sortKeys1[i], sortKeys2[i] );
 			if ( result != 0 )
 			{
 				if ( sortDirection[i] == false )
 				{
 					result = result * -1;
 					break;
 				}
 			}
 
 		}
 
 		return result;
 	}
 
 	private int compareTwoValues( Object obj1, Object obj2 )
 	{
 		if ( obj1 == null || obj2 == null )
 		{
 			// all non-null values are greater than null value
 			if ( obj1 == null && obj2 != null )
 				return -1;
 			else if ( obj1 != null && obj2 == null )
 				return 1;
 			else
 				return 0;
 		}
 
 		if ( obj1 instanceof Boolean )
 		{
 			if ( obj1.equals( obj2 ) )
 				return 0;
 
 			Boolean bool = (Boolean) obj1;
 			if ( bool.equals( Boolean.TRUE ) )
 				return 1;
 			else
 				return -1;
 		}
 		else if ( obj1 instanceof Comparable )
 		{
			return ( (Comparable) obj1 ).compareTo( obj2 );
 		}
 		else
 		{
 			return obj1.toString( ).compareTo( obj2.toString( ) );
 		}
 	}
 }

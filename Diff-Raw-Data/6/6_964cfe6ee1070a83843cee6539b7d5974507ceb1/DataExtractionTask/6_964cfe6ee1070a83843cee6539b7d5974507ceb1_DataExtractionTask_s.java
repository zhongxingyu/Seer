 /*******************************************************************************
  * Copyright (c) 2004 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.birt.report.engine.api.impl;
 
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.birt.core.archive.IDocArchiveReader;
 import org.eclipse.birt.core.exception.BirtException;
 import org.eclipse.birt.core.util.IOUtil;
 import org.eclipse.birt.data.engine.api.DataEngine;
 import org.eclipse.birt.data.engine.api.IBaseQueryDefinition;
 import org.eclipse.birt.data.engine.api.IFilterDefinition;
 import org.eclipse.birt.data.engine.api.IPreparedQuery;
 import org.eclipse.birt.data.engine.api.IQueryDefinition;
 import org.eclipse.birt.data.engine.api.IQueryResults;
 import org.eclipse.birt.data.engine.api.IResultIterator;
 import org.eclipse.birt.data.engine.api.querydefn.BaseQueryDefinition;
 import org.eclipse.birt.data.engine.api.querydefn.QueryDefinition;
 import org.eclipse.birt.data.engine.api.querydefn.SubqueryDefinition;
 import org.eclipse.birt.report.engine.api.DataID;
 import org.eclipse.birt.report.engine.api.DataSetID;
 import org.eclipse.birt.report.engine.api.EngineException;
 import org.eclipse.birt.report.engine.api.IDataExtractionTask;
 import org.eclipse.birt.report.engine.api.IExtractionResults;
 import org.eclipse.birt.report.engine.api.IReportDocument;
 import org.eclipse.birt.report.engine.api.IReportEngine;
 import org.eclipse.birt.report.engine.api.IReportRunnable;
 import org.eclipse.birt.report.engine.api.IResultMetaData;
 import org.eclipse.birt.report.engine.api.IResultSetItem;
 import org.eclipse.birt.report.engine.api.InstanceID;
 import org.eclipse.birt.report.engine.data.IDataEngine;
 import org.eclipse.birt.report.engine.data.dte.DteDataEngine;
 import org.eclipse.birt.report.engine.ir.Report;
 import org.eclipse.birt.report.engine.ir.ReportItemDesign;
 import org.mozilla.javascript.Scriptable;
 
 public class DataExtractionTask extends EngineTask
 		implements
 			IDataExtractionTask
 {
 
 	/**
 	 * report document contains the data
 	 */
 	protected IReportDocument reportDocReader;
 
 	/**
 	 * report design in the report document.
 	 */
 	protected Report report;
 
 	/**
 	 * selected instance id
 	 */
 	protected InstanceID instanceId;
 	/**
 	 * selected rest set
 	 */
 	protected String resultSetName;
 
 	/**
 	 * selected columns
 	 */
 	protected String[] selectedColumns;
 
 	/**
 	 * current extaction results
 	 */
 	protected IExtractionResults currentResult = null;
 	
 	/**
 	 * simple filter expression
 	 */
 	protected IFilterDefinition[] filterExpressions = null;
 
 	/**
 	 * have the metadata be prepared. meta data means rsetName2IdMapping and
 	 * queryId2NameMapping
 	 */
 	protected boolean isMetaDataPrepared = false;
 
 	/**
 	 * mapping, map the rest name to rset id.
 	 */
 	protected HashMap rsetName2IdMapping = new HashMap( );
 	
 	/**
 	 * mapping, map the rest name to rset id.
 	 */
 	protected HashMap rsetId2queryIdMapping = new HashMap( );
 
 	/**
 	 * mapping, map the query Id to query name.
 	 */
 	protected HashMap queryId2NameMapping = new HashMap( );
 
 	protected HashMap queryId2QueryMapping = new HashMap( );
 
 	/**
 	 * list contains all the resultsets each entry is a
 	 */
 	protected ArrayList resultMetaList = new ArrayList( );
 	/**
 	 * the logger
 	 */
 	protected static Logger logger = Logger.getLogger( DteDataEngine.class
 			.getName( ) );
 
 	public DataExtractionTask( IReportEngine engine, IReportRunnable runnable,
 			IReportDocument reader ) throws EngineException
 	{
 		super( engine, runnable );
 		
 		this.report = ((ReportRunnable)runnable).getReportIR( );
 
 		// load the report
 		this.reportDocReader = reader;
 		executionContext.setReportDocument( reportDocReader );
 		executionContext.setFactoryMode( false );
 		executionContext.setPresentationMode( true );
 	}
 
 	/*
 	 * prepare the meta data of DataExtractionTask.
 	 */
 	private void prepareMetaData( )
 	{
 		if ( isMetaDataPrepared == true )
 			return;
 
 		IDataEngine dataEngine = executionContext.getDataEngine( );
 		dataEngine.prepare( report, appContext );
 		
 		HashMap queryIds = report.getQueryIDs( );
 		HashMap query2itemMapping = report.getReportItemToQueryMap( );
 		Iterator iter = queryIds.entrySet( ).iterator( );
 		while ( iter.hasNext( ) )
 		{
 			Map.Entry entry = (Map.Entry) iter.next( );
 			IBaseQueryDefinition baseQuery = (IBaseQueryDefinition) entry.getKey( );
 			if ( baseQuery instanceof IQueryDefinition )
 			{
 				IQueryDefinition query = (IQueryDefinition) baseQuery;
 				String queryId = (String) entry.getValue( );
 				ReportItemDesign item = (ReportItemDesign) query2itemMapping.get( query );
 				String queryName = item.getName( );
 				if ( queryName == null )
 				{
 					queryName = "ELEMENT_" + item.getID( );
 				}
 				queryId2NameMapping.put( queryId, queryName );
 				queryId2QueryMapping.put( queryId, query );;
 			}
 		}
 
 		try
 		{
 			loadResultSetMetaData( );
 		}
 		catch ( EngineException e )
 		{
 			e.printStackTrace( );
 		}
 
 		isMetaDataPrepared = true;
 	}
 
 	/**
 	 * get the query name through query id.
 	 * 
 	 * @param queryId
 	 * @return query name
 	 */
 	private String getQueryName( String queryId )
 	{
 		return (String) queryId2NameMapping.get( queryId );
 	}
 
 	/**
 	 * get the query defintion from the query id
 	 * 
 	 * @param queryId
 	 * @return
 	 */
 	private IQueryDefinition getQuery( String queryId )
 	{
 		return (IQueryDefinition) queryId2QueryMapping.get( queryId );
 	}
 
 	/**
 	 * load map from query id to result set id from report document.
 	 */
 	private void loadResultSetMetaData( ) throws EngineException
 	{
 		DataInputStream dis = null;
 		try
 		{
 			HashMap query2ResultMetaData = report.getResultMetaData( );
 			IDocArchiveReader reader = reportDocReader.getArchive( );
 			dis = new DataInputStream( reader
 					.getStream( ReportDocumentConstants.DATA_META_STREAM ) );
 
 			HashMap queryCounts = new HashMap( );
 			while ( true )
 			{
 				// skip the parent restset id
 				IOUtil.readString( dis );
 				// skip the row id
 				IOUtil.readLong( dis );
 				// this is the query id
 				String queryId = IOUtil.readString( dis );
 				// this is the rest id
 				String rsetId = IOUtil.readString( dis );
 				
 				IQueryDefinition query = getQuery( queryId );
 				if ( !isMasterQuery ( query ) )
 				{
 					continue;
 				}
 				
				rsetId2queryIdMapping.put( rsetId, queryId );
				
 				int count = -1;
 				Integer countObj = (Integer) queryCounts.get( queryId );
 				if ( countObj != null )
 				{
 					count = countObj.intValue( );
 				}
 				count++;
 				String rsetName = getQueryName( queryId );
 				if( count > 0)
 				{
 					rsetName = rsetName + "_" + count;
 				}
 				queryCounts.put( queryId, new Integer( count ) );
 				rsetName2IdMapping.put( rsetName, rsetId );
 
 				if ( null != query2ResultMetaData )
 				{
 					ResultMetaData metaData = (ResultMetaData) query2ResultMetaData.get( query );
 					if ( metaData.getColumnCount( ) > 0 )
 					{
 						IResultSetItem resultItem = new ResultSetItem( rsetName,
 								metaData );
 						resultMetaList.add( resultItem );
 					}
 				}
 			}
 		}
 		catch ( EOFException eofe )
 		{
 			// we expect that there should be an EOFexception
 		}
 		catch ( IOException ioe )
 		{
 			logger.log( Level.SEVERE, ioe.getMessage( ), ioe );
 		}
 		finally
 		{
 			if ( dis != null )
 			{
 				try
 				{
 					dis.close( );
 				}
 				catch ( IOException ex )
 				{
 
 				}
 			}
 		}
 	}
 
 	private boolean isMasterQuery( IQueryDefinition query )
 	{
 		if ( query.getDataSetName( ) == null )
 		{
 			return false;
 		}
 		IBaseQueryDefinition parent = query.getParentQuery( ); 
 		while ( parent != null )
 		{
 			if ( parent instanceof IQueryDefinition )
 			{
 				IQueryDefinition parentQuery = (IQueryDefinition) parent;
 				if ( parentQuery.getDataSetName( ) != null )
 				{
 					return false;
 				}
 			}
 			else
 			{
 				return false;
 			}
 			parent = parent.getParentQuery( );
 		}
 		return true;
 	}
 
 	/**
 	 * get the result set name used by the instance.
 	 * 
 	 * @param iid
 	 *            instance id
 	 * @return result set name.
 	 */
 	protected String instanceId2RsetName( InstanceID iid )
 	{
 		DataID dataId = iid.getDataID( );
 		if ( dataId != null )
 		{
 			DataSetID dataSetId = dataId.getDataSetID( );
 			if ( dataSetId != null )
 			{
 				String rsetId = dataSetId.getDataSetName( );
 				if ( rsetId != null )
 				{
 					return rsetId2Name( rsetId );
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * get the resultset id from the query id.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	protected String queryId2rsetId( String id )
 	{
 		// search the name/Id mapping
 		Iterator iter = rsetId2queryIdMapping.entrySet( ).iterator( );
 		while ( iter.hasNext( ) )
 		{
 			Map.Entry entry = (Map.Entry) iter.next( );
 			String queryId = (String) entry.getValue( );
 			String rsetId = (String) entry.getKey( );
 			if ( queryId.equals( id ) )
 			{
 				return rsetId;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * get the rset id from the rset name.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	protected String rsetId2Name( String id )
 	{
 		// search the name/Id mapping
 		Iterator iter = rsetName2IdMapping.entrySet( ).iterator( );
 		while ( iter.hasNext( ) )
 		{
 			Map.Entry entry = (Map.Entry) iter.next( );
 			String rsetId = (String) entry.getValue( );
 			String rsetName = (String) entry.getKey( );
 			if ( rsetId.equals( id ) )
 			{
 				return rsetName;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * get the rset name from the rset id.
 	 * 
 	 * @param name
 	 * @return
 	 */
 	protected String rsetName2Id( String name )
 	{
 		return (String) rsetName2IdMapping.get( name );
 	}
 
 	public void setInstanceID( InstanceID iid )
 	{
 		assert iid != null;
 
 		prepareMetaData( );
 
 		instanceId = iid;
 		resultSetName = null;
 		selectedColumns = null;
 	}
 
 	public void selectResultSet( String displayName )
 	{
 		assert displayName != null;
 
 		prepareMetaData( );
 
 		if (displayName.startsWith( "InstanceId:" ))
 		{
 			resultSetName = null;
 			instanceId = InstanceID.parse( displayName.substring( 11 ) );
 		}
 		else
 		{
 			resultSetName = displayName;
 			instanceId = null;
 		}
 		selectedColumns = null;
 	}
 
 	public List getMetaData( ) throws EngineException
 	{
 		return getResultSetList( );
 	}
 
 	public List getResultSetList( ) throws EngineException
 	{
 		prepareMetaData( );
 		if ( instanceId != null )
 		{
 			ArrayList rsetList = new ArrayList( );
 			String rsetName = instanceId2RsetName( instanceId );
 			if ( rsetName != null )
 			{
 				IResultMetaData metaData = getResultMetaData( rsetName );
 				if ( metaData != null )
 				{
 					rsetList.add( new ResultSetItem( rsetName, metaData ) );
 				}
 			}
 			else
 			{
 				IResultMetaData metaData = getMetaDateByInstanceID( instanceId );
 				if (metaData != null)
 				{
 					rsetList.add( new ResultSetItem( "InstanceId:" + instanceId, metaData ) );
 				}
 			}
 				
 			return rsetList;
 
 		}
 		return resultMetaList;
 	}
 
 	/**
 	 * get the metadata of a result set.
 	 * 
 	 * @param rsetName
 	 * @return
 	 */
 	protected IResultMetaData getResultMetaData( String rsetName )
 	{
 		Iterator iter = resultMetaList.iterator( );
 		while ( iter.hasNext( ) )
 		{
 			IResultSetItem rsetItem = (IResultSetItem) iter.next( );
 			if ( rsetItem.getResultSetName( ).equals( rsetName ) )
 			{
 				return rsetItem.getResultMetaData( );
 			}
 		}
 		return null;
 	}
 
 	public void selectColumns( String[] columnNames )
 	{
 		selectedColumns = columnNames;
 	}
 
 	public IExtractionResults extract( ) throws EngineException
 	{
 		String rsetName = resultSetName;
 		if ( rsetName == null )
 		{
 			if ( instanceId != null )
 			{
 				rsetName = instanceId2RsetName( instanceId );
 			}
 		}
 		if ( rsetName != null )
 		{
 			return extractByResultSetName( rsetName );
 		}
 		if (instanceId != null)
 		{
 			return extractByInstanceID( instanceId);
 		}
 		return null;
 	}
 
 	/*
 	 * export result directly from result set name
 	 */
 	private IExtractionResults extractByResultSetName( String rsetName )
 			throws EngineException
 	{
 		assert rsetName != null;
 		assert executionContext.getDataEngine( ) != null;
 
 		prepareMetaData( );
 
 		DataEngine dataEngine = executionContext.getDataEngine( )
 				.getDataEngine( );
 		try
 		{
 			String rsetId = rsetName2Id( rsetName );
 			if ( rsetId != null )
 			{
 				IQueryResults results = null;
 				if(null == filterExpressions)
 				{
 					results = dataEngine.getQueryResults( rsetId );
 				}
 				else
 				{
 					// creat new query
 					String queryId = (String) rsetId2queryIdMapping.get( rsetId );
 					QueryDefinition query = (QueryDefinition) getQuery( queryId );
 					QueryDefinition newQuery = queryCopy( query );
 					if( null == newQuery )
 					{
 						return null;
 					}
 					
 					// add filter
 					for(int iNum = 0; iNum < filterExpressions.length; iNum++)
 					{
 						newQuery.getFilters( ).add( filterExpressions[ iNum ] );
 					}
 					filterExpressions = null;
 					
 					// get new result
 					newQuery.setQueryResultsID( rsetId );
 					Scriptable scope = executionContext.getSharedScope( );
 					IPreparedQuery preparedQuery = dataEngine.prepare( newQuery);
 					results = preparedQuery.execute( scope );
 				}
 			
 				if ( null != results )
 				{
 					IResultMetaData metaData = getResultMetaData( rsetName );
 					if (metaData != null)
 					{
 						return new ExtractionResults( results, metaData,
 								this.selectedColumns );
 					}
 				}
 			}
 		}
 		catch ( BirtException e )
 		{
 			e.printStackTrace( );
 		}
 		return null;
 	}
 	
 	private IExtractionResults extractByInstanceID( InstanceID iid )
 			throws EngineException
 	{
 		DataID dataId = iid.getDataID( );
 		DataSetID dataSetId = dataId.getDataSetID( );
 
 		DataEngine dataEngine = executionContext.getDataEngine( )
 				.getDataEngine( );
 		Scriptable scope = executionContext.getSharedScope( );
 		IResultIterator dataIter = null;
 		IBaseQueryDefinition query = null;
 		try
 		{
 			if(null == filterExpressions)
 			{
 				dataIter = getResultSetIterator( dataEngine, dataSetId, scope );
 			}
 			else
 			{
 				//dataIter = getResultSet( dataEngine, dataSetId, scope );
 				//get query
 				long id = iid.getComponentID( );
 				ReportItemDesign design = (ReportItemDesign) report.getReportItemByID( id );
 				query = design.getQuery( );
 				if ( null == query )
 				{
 					return null;
 				}
 				
 				// add filter
 				for(int iNum = 0; iNum < filterExpressions.length; iNum++)
 				{
 					query.getFilters( ).add( filterExpressions[ iNum ] );
 				}
 
 				//creat new root query
 				IBaseQueryDefinition rootQuery = query;
 				while( rootQuery instanceof SubqueryDefinition)
 				{
 					rootQuery = rootQuery.getParentQuery( );
 				}
 				QueryDefinition newRootQuery = queryCopy( (QueryDefinition)rootQuery );
 				
 				//get the resultSet of the new root query
 				HashMap queryIds = report.getQueryIDs( );
 				String queryId = (String) queryIds.get( rootQuery );
 				String rsetId =  queryId2rsetId( queryId );
 				newRootQuery.setQueryResultsID( rsetId );
 				IPreparedQuery preparedQuery = dataEngine.prepare( newRootQuery);
 				IQueryResults rootResults = preparedQuery.execute( scope );
 				
 				dataIter = getFilterResultSetIterator( dataEngine, dataSetId, scope, rootResults );
 			}
 		}
 		catch ( BirtException e )
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace( );
 			throw new EngineException( "Export date by Instance Id failed!", e );
 		}
 		finally
 		{
 			if ( null != query )
 			{
 				//remove filter
 				for(int iNum = 0; iNum < filterExpressions.length; iNum++)
 				{
 					query.getFilters( ).remove( filterExpressions[ iNum ] );
 				}
 				filterExpressions = null;
 			}
 		}
 
 		IResultMetaData metaData = getMetaDateByInstanceID( iid );
 
 		if ( null != metaData )
 		{
 			return new ExtractionResults( dataIter,
 					metaData,
 					this.selectedColumns );
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	private IResultMetaData getMetaDateByInstanceID( InstanceID iid )
 	{
 		IResultMetaData metaData = null;
 		long id = iid.getComponentID( );
 		ReportItemDesign design = (ReportItemDesign) report.getReportItemByID( id );
 		IBaseQueryDefinition query = design.getQuery( );
 
 		if ( null == query )
 		{
 			return null;
 		}
 
 		HashMap query2ResultMetaData = report.getResultMetaData( );
 		if ( null != query2ResultMetaData )
 		{
 			metaData = (ResultMetaData) query2ResultMetaData.get( query );
 		}
 
 		return metaData;
 	}
 
 	private IResultIterator getResultSetIterator( DataEngine dataEngine,
 			DataSetID dataSet, Scriptable scope ) throws BirtException
 	{
 		DataSetID parent = dataSet.getParentID( );
 		if ( parent == null )
 		{
 			String rsetName = dataSet.getDataSetName( );
 			assert rsetName != null;
 			IQueryResults rset = dataEngine.getQueryResults( rsetName );
 			return rset.getResultIterator( );
 		}
 		else
 		{
 			IResultIterator iter = getResultSetIterator( dataEngine, parent, scope );
 			long rowId = dataSet.getRowID( );
 			String queryName = dataSet.getQueryName( );
 			assert rowId != -1;
 			assert queryName != null;
 
 			iter.moveTo( (int) rowId );
 			return iter.getSecondaryIterator( queryName, scope );
 		}
 	}
 	
 	private IResultIterator getFilterResultSetIterator( DataEngine dataEngine,
 			DataSetID dataSet, Scriptable scope, IQueryResults rset ) throws BirtException
 	{
 		DataSetID parent = dataSet.getParentID( );
 		if ( parent == null )
 		{
 			return rset.getResultIterator( );
 		}
 		else
 		{
 			IResultIterator iter = getFilterResultSetIterator( dataEngine, parent, scope, rset );
 			long rowId = dataSet.getRowID( );
 			String queryName = dataSet.getQueryName( );
 			assert rowId != -1;
 			assert queryName != null;
 
 			iter.moveTo( (int) rowId );
 			return iter.getSecondaryIterator( queryName, scope );
 		}
 	}
 	
 	/**
 	 * copy a query.
 	 * 
 	 * @param query
 	 * @return
 	 */
 	private QueryDefinition queryCopy( QueryDefinition query )
 	{
 		if(null == query)
 			return null;
 		
 		QueryDefinition newQuery = new QueryDefinition( (BaseQueryDefinition) query.getParentQuery( ) );
 		
 		newQuery.getSorts( ).addAll( query.getSorts( ) );
 		newQuery.getFilters( ).addAll( query.getFilters( ) );
 		newQuery.getSubqueries( ).addAll( query.getSubqueries( ) );
 		newQuery.getResultSetExpressions( ).putAll( query.getResultSetExpressions( ) );
 		
 		newQuery.getGroups( ).addAll( query.getGroups( ) );
 		newQuery.setUsesDetails( query.usesDetails( ) );
 		newQuery.setMaxRows( query.getMaxRows( ) );
 		
 		newQuery.setDataSetName( query.getDataSetName( ) );
 		newQuery.setAutoBinding( query.needAutoBinding( ) );
 		newQuery.setColumnProjection( query.getColumnProjection( ) );
 		
 		return newQuery;
 	}
 
 	/**
 	 * @param simpleFilterExpression
 	 *            add one filter condition to the extraction. Only simple filter
 	 *            expressions are supported for now, i.e., LHS must be a column
 	 *            name, only <, >, = and startWith is supported.
 	 */
 	public void setFilters( IFilterDefinition[] simpleFilterExpression )
 	{
 		filterExpressions = simpleFilterExpression;
 	}
 }

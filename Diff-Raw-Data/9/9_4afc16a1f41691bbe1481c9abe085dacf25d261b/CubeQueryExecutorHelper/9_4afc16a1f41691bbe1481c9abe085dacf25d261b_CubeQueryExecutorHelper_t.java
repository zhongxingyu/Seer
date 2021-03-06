 
 /*******************************************************************************
  * Copyright (c) 2004, 2007 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *******************************************************************************/
 package org.eclipse.birt.data.engine.olap.data.api;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.eclipse.birt.core.archive.FileArchiveReader;
 import org.eclipse.birt.core.archive.FileArchiveWriter;
 import org.eclipse.birt.core.archive.IDocArchiveReader;
 import org.eclipse.birt.core.archive.IDocArchiveWriter;
 import org.eclipse.birt.core.exception.BirtException;
 import org.eclipse.birt.data.engine.core.DataException;
 import org.eclipse.birt.data.engine.i18n.ResourceConstants;
 import org.eclipse.birt.data.engine.impl.StopSign;
 import org.eclipse.birt.data.engine.impl.document.stream.VersionManager;
 import org.eclipse.birt.data.engine.olap.data.api.cube.ICube;
 import org.eclipse.birt.data.engine.olap.data.api.cube.IDimension;
 import org.eclipse.birt.data.engine.olap.data.document.IDocumentManager;
 import org.eclipse.birt.data.engine.olap.data.impl.AggregationDefinition;
 import org.eclipse.birt.data.engine.olap.data.impl.AggregationResultSetSaveUtil;
 import org.eclipse.birt.data.engine.olap.data.impl.Cube;
 import org.eclipse.birt.data.engine.olap.data.impl.aggregation.AggregationExecutor;
 import org.eclipse.birt.data.engine.olap.data.impl.aggregation.filter.AggregationFilterHelper;
 import org.eclipse.birt.data.engine.olap.data.impl.aggregation.filter.LevelFilter;
 import org.eclipse.birt.data.engine.olap.data.impl.aggregation.filter.LevelFilterHelper;
 import org.eclipse.birt.data.engine.olap.data.impl.aggregation.filter.SimpleLevelFilter;
 import org.eclipse.birt.data.engine.olap.data.impl.aggregation.sort.AggrSortDefinition;
 import org.eclipse.birt.data.engine.olap.data.impl.aggregation.sort.AggrSortHelper;
 import org.eclipse.birt.data.engine.olap.data.impl.dimension.Dimension;
 import org.eclipse.birt.data.engine.olap.data.impl.dimension.DimensionResultIterator;
 import org.eclipse.birt.data.engine.olap.data.impl.facttable.FactTableRowIterator;
 import org.eclipse.birt.data.engine.olap.data.util.IDiskArray;
 import org.eclipse.birt.data.engine.olap.util.filter.IJSFilterHelper;
 
 /**
  * 
  */
 
 public class CubeQueryExecutorHelper implements ICubeQueryExcutorHelper
 {
 	private Cube cube;
 	private List levelFilters = null;
 	private List simpleLevelFilters = null;	// list for SimepleLevelFilter
 	private Map dimJSFilterMap = null;
 	private Map dimRowForFilterMap = null;
 	
 	private List rowSort = null;
 	private List columnSort = null;
 	
 	private boolean isBreakHierarchy = true;
 	
 	private IComputedMeasureHelper computedMeasureHelper = null;
 	
 	private Map dimLevelsMap = null;
 	private List aggrFilterHelpers;
 	
 	private static Logger logger = Logger.getLogger( CubeQueryExecutorHelper.class.getName( ) );
 	
 	/**
 	 * 
 	 * @param cube
 	 */
 	public CubeQueryExecutorHelper( ICube cube ) throws DataException
 	{
 		this(cube, null);
 	}
 	
 	/**
 	 * 
 	 * @param cube
 	 */
 	public CubeQueryExecutorHelper( ICube cube, IComputedMeasureHelper computedMeasureHelper ) throws DataException
 	{
 		Object[] params = {cube, computedMeasureHelper};
 		logger.entering( CubeQueryExecutorHelper.class.getName( ),
 				"CubeQueryExecutorHelper",//$NON-NLS-1$
 				params );
 		this.cube = (Cube) cube;
 		this.computedMeasureHelper = computedMeasureHelper;
 		if (this.computedMeasureHelper != null) 
 		{
 			validateComputedMeasureNames();
 		}
 		this.simpleLevelFilters = new ArrayList( );
 		this.levelFilters = new ArrayList( );
 		this.aggrFilterHelpers = new ArrayList( );
 		this.dimJSFilterMap = new HashMap( );
 		this.dimRowForFilterMap = new HashMap( );
 		
 		this.rowSort = new ArrayList( );
 		this.columnSort = new ArrayList( );
 		
 		dimLevelsMap = new HashMap( );
 		IDimension[] dimension = this.cube.getDimesions( );
 		for ( int i = 0; i < dimension.length; i++ )
 		{
 			ILevel[] levels = dimension[i].getHierarchy( ).getLevels( );
 			dimLevelsMap.put( dimension[i].getName( ), levels );
 		}
 		logger.exiting( CubeQueryExecutorHelper.class.getName( ),
 				"CubeQueryExecutorHelper" );//$NON-NLS-1$
 	}
 	
 	/**
 	 * TODO: get the members according to the specified level.
 	 * @param level
 	 * @return
 	 */
 	public IDiskArray getLevelMembers( DimLevel level )
 	{
 		return null;
 	}
 	
 	/**
 	 * get the attribute reference name.
 	 * @param dimName
 	 * @param levelName
 	 * @param attrName
 	 * @return
 	 */
 	public static String getAttrReference( String dimName, String levelName, String attrName )
 	{
 		return dimName + '/' + levelName + '/' + attrName;
 	}
 	
 	
 	/**
 	 * 
 	 * @param cube
 	 * @throws BirtException 
 	 * @throws IOException 
 	 */
 	public static ICube loadCube( String cubeName,
 			IDocumentManager documentManager, StopSign stopSign ) throws IOException, DataException
 	{
 		Cube cube = new Cube( cubeName, documentManager );
 		cube.load( stopSign );
 		return cube;
 	}
 	
 	/**
 	 * 
 	 * @param name
 	 * @param resultSets
 	 * @param writer
 	 * @throws IOException
 	 */
 	public static void saveAggregationResultSet( IDocArchiveWriter writer, String name, IAggregationResultSet[] resultSets ) throws IOException
 	{
 		AggregationResultSetSaveUtil.save( name, resultSets, writer );
 	}
 	
 	/**
 	 * 
 	 * @param name
 	 * @param resultSets
 	 * @throws IOException
 	 */
 	public static void saveAggregationResultSet( String pathName ,String name, IAggregationResultSet[] resultSets ) throws IOException
 	{
 		IDocArchiveWriter writer = new FileArchiveWriter( getTmpFileName( pathName, name ) );
 		AggregationResultSetSaveUtil.save( name, resultSets, writer );
 		writer.flush( );
 		writer.finish( );
 	}
 	
 	
 	/**
 	 * 
 	 * @param name
 	 * @param reader
 	 * @return
 	 * @throws IOException
 	 */
 	public static IAggregationResultSet[] loadAggregationResultSet( IDocArchiveReader reader, String name ) throws IOException
 	{
 		return AggregationResultSetSaveUtil.load( name, reader, VersionManager.getLatestVersion( ) );
 	}
 	
 	/**
 	 * 
 	 * @param pathName
 	 * @param name
 	 * @return
 	 * @throws IOException
 	 */
 	public static IAggregationResultSet[] loadAggregationResultSet( String pathName, String name ) throws IOException
 	{
 		IDocArchiveReader reader = new FileArchiveReader( getTmpFileName( pathName,
 				name ) );
 		IAggregationResultSet[] result = AggregationResultSetSaveUtil.load( name,
 				reader,
 				VersionManager.getLatestVersion( ) );
 		reader.close( );
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param pathName
 	 * @param name
 	 * @return
 	 */
 	private static String getTmpFileName( String pathName, String name )
 	{
 		return pathName + File.separator + "cubequeryresult" +name;//$NON-NLS-1$
 	}
 	
 	
 	/**
 	 * 
 	 * @param sort
 	 */
 	public void addRowSort( AggrSortDefinition sort )
 	{
 		this.rowSort.add( sort );
 	}
 	
 	/**
 	 * 
 	 * @return sortDefinition list on row edge
 	 */
 	public List getRowSort( )
 	{
 		return this.rowSort;
 	}
 	
 	/**
 	 * 
 	 * @return sortDefinition list on column edge
 	 */
 	public List getColumnSort( )
 	{
 		return this.columnSort;
 	}
 	
 	/**
 	 * 
 	 * @param sort
 	 */
 	public void addColumnSort( AggrSortDefinition sort )
 	{
 		this.columnSort.add( sort );
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.birt.data.olap.data.api.ICubeQueryExcutorHelper#addFilter(java.lang.String, org.eclipse.birt.data.olap.data.api.ISelection[])
 	 */
 	public void addFilter( LevelFilter levelFilter )
 	{		
 		levelFilters.add( levelFilter );
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.birt.data.engine.olap.data.api.ICubeQueryExcutorHelper#addSimpleLevelFilter(org.eclipse.birt.data.engine.olap.data.impl.aggregation.filter.SimpleLevelFilter)
 	 */
 	public void addSimpleLevelFilter( SimpleLevelFilter simpleLevelFilter )
 	{		
 		simpleLevelFilters.add( simpleLevelFilter );
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.birt.data.olap.data.api.ICubeQueryExcutorHelper#clear()
 	 */
 	public void clear( )
 	{
 		levelFilters.clear( );
 		aggrFilterHelpers.clear( );
 		dimJSFilterMap.clear( );
 		dimRowForFilterMap.clear( );
 		dimLevelsMap.clear( );
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.birt.data.olap.data.api.ICubeQueryExcutorHelper#close()
 	 */
 	public void close( )
 	{
 		levelFilters = null;
 		aggrFilterHelpers = null;
 		dimJSFilterMap = null;
 		dimRowForFilterMap = null;
 		dimLevelsMap = null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.birt.data.olap.data.api.ICubeQueryExcutorHelper#excute(org.eclipse.birt.data.olap.data.impl.AggregationDefinition[], org.eclipse.birt.data.olap.data.impl.StopSign)
 	 */
 	public IAggregationResultSet[] execute(
 			AggregationDefinition[] aggregations, StopSign stopSign )
 			throws IOException, BirtException
 	{
 		IAggregationResultSet[] resultSet = onePassExecute( aggregations,
 				stopSign );
 		
 		applyAggrFilters( aggregations, resultSet, stopSign );
 		
 		applyAggrSort( resultSet );
 		
 		return resultSet;
 	}	
 	
 	/**
 	 * @param resultSet
 	 * @throws DataException
 	 */
 	private void applyAggrSort( IAggregationResultSet[] resultSet )
 			throws DataException
 	{
 		if ( !this.columnSort.isEmpty( ) )
 		{
 			IAggregationResultSet column = AggrSortHelper.sort( this.columnSort,
 					resultSet );
 			resultSet[findMatchedResultSetIndex( resultSet, column )] = column;
 		}
 		if ( !this.rowSort.isEmpty( ) )
 		{
 			IAggregationResultSet row = AggrSortHelper.sort( this.rowSort,
 					resultSet );
 			resultSet[findMatchedResultSetIndex( resultSet, row )] = row;
 		}
 	}
 
 	/**
 	 * @param aggregations
 	 * @param resultSet
 	 * @param stopSign
 	 * @throws IOException
 	 * @throws DataException
 	 * @throws BirtException
 	 */
 	private void applyAggrFilters( AggregationDefinition[] aggregations,
 			IAggregationResultSet[] resultSet, StopSign stopSign )
 			throws IOException, DataException, BirtException
 	{
 		if ( aggrFilterHelpers.isEmpty( ) == false )
 		{
 			List oldFilters = new ArrayList( levelFilters );
 			AggregationFilterHelper filterHelper = new AggregationFilterHelper( cube,
 					aggrFilterHelpers );
 			// add new filters for another aggregation computation
 			List newFilters = filterHelper.generateLevelFilters( aggregations,
 					resultSet );
 			if ( newFilters == null )
 			{// the final x-tab is empty
 				for ( int i = 0; i < resultSet.length; i++ )
 				{// clear all aggregation result sets to be empty
 					resultSet[i].clear( );
 				}
 			}
 			else
 			{
 				levelFilters.addAll( newFilters );
 				for ( int i = 0; i < resultSet.length; i++ )
 				{// release all previous aggregation result sets
 					resultSet[i].close( );
 					resultSet[i] = null;
 				}
 				// recalculate the aggregation according to new filters
 				IAggregationResultSet[] temp = onePassExecute( aggregations,
 						stopSign );
 				// overwrite result with the second pass aggregation result set
 				System.arraycopy( temp, 0, resultSet, 0, resultSet.length );
 			}
 			// restore to original filter list to avoid conflict
 			levelFilters = oldFilters;
 		}
 	}
 	 
 		
 	/**
 	 * @param rSets
 	 * @param source
 	 * @return
 	 * @throws DataException
 	 */
 	private int findMatchedResultSetIndex( IAggregationResultSet[] rSets, IAggregationResultSet source ) throws DataException
 	{
 		for( int i = 0; i < rSets.length; i++ )
 		{
 			if( AggrSortHelper.isEdgeResultSet( rSets[i] ))
 			{
 				if( source.getLevel( 0 ).equals( rSets[i].getLevel( 0 ) ))
 					return i;
 			}
 		}
 		throw new DataException("Invalid");//$NON-NLS-1$
 	}
 	
 	/**
 	 * This method is responsible for computing the aggregation result according
 	 * to the specified aggregation definitions.
 	 * @param aggregations
 	 * @param stopSign
 	 * @return
 	 * @throws DataException
 	 * @throws IOException
 	 * @throws BirtException
 	 */
 	private IAggregationResultSet[] onePassExecute(
 			AggregationDefinition[] aggregations, StopSign stopSign )
 			throws DataException, IOException, BirtException
 	{
 		IDiskArray[] dimPosition = getFilterResult( );
 
 		int count = 0;
 		for ( int i = 0; i < dimPosition.length; i++ )
 		{
 			if ( dimPosition[i] != null )
 			{
 				count++;
 			}
 		}
 		IDimension[] dimensions = cube.getDimesions( );
 		String[] validDimensionName = new String[count];
 		IDiskArray[] validDimPosition = new IDiskArray[count];
 		int pos = 0;
 		for ( int i = 0; i < dimPosition.length; i++ )
 		{
 			if ( dimPosition[i] != null )
 			{
 				validDimPosition[pos] = dimPosition[i];
 				validDimensionName[pos] = dimensions[i].getName( );
 				pos++;
 			}
 		}
 		FactTableRowIterator facttableRowIterator = new FactTableRowIterator( cube.getFactTable( ),
 				validDimensionName,
 				validDimPosition,
 				computedMeasureHelper,
 				stopSign );
 
 		DimensionResultIterator[] dimensionResultIterator = populateDimensionResultIterator( dimPosition, stopSign );
 
 		AggregationExecutor aggregationCalculatorExecutor = new AggregationExecutor( dimensionResultIterator,
 				facttableRowIterator,
 				aggregations );
 		try
 		{
 			return aggregationCalculatorExecutor.execute( stopSign );
 		}
 		finally
 		{
 			facttableRowIterator.close( );
 		}
 	}
 	
 	/**
 	 * 
 	 * @param resultLevels
 	 * @param position
 	 * @param stopSign
 	 * @return
 	 * @throws DataException
 	 * @throws IOException
 	 */
 	private DimensionResultIterator[] populateDimensionResultIterator( IDiskArray[] position, StopSign stopSign ) throws DataException, IOException
 	{
 		IDimension[] dimensions = cube.getDimesions( );
 		DimensionResultIterator[] dimResultSet = new DimensionResultIterator[dimensions.length];
 		int count = 0;
 		for ( int i = 0; i < dimensions.length; i++ )
 		{
 			if ( position[i] == null )
 				{
 					dimResultSet[i] = new DimensionResultIterator( (Dimension) dimensions[i],
 							dimensions[i].findAll( ), stopSign);
 				}
 				else
 				{
 					dimResultSet[i] = new DimensionResultIterator( (Dimension) dimensions[i],
 							position[i], stopSign);
 				}
 				count++;			
 		}
 		
 		DimensionResultIterator[] result = new DimensionResultIterator[count];
 		int pos = 0;
 		for( int i=0;i<dimResultSet.length;i++)
 		{
 			if( dimResultSet[i] != null )
 			{
 				result[pos] = dimResultSet[i];
 				pos++;
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param dimensionName
 	 * @return
 	 */
 	private List getDimensionJSFilterList( String dimensionName )
 	{
 		Object value = dimJSFilterMap.get( dimensionName );
 		if( value != null )
 		{
 			return (List)value;
 		}
 		List list = new ArrayList();
 		dimJSFilterMap.put( dimensionName, list );
 		return list;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 * @throws DataException
 	 * @throws IOException
 	 */
 	private IDiskArray[] getFilterResult( ) throws DataException, IOException
 	{
 		IDimension[] dimensions = cube.getDimesions( );
 		IDiskArray[] dimPosition = new IDiskArray[dimensions.length];
 		for ( int i = 0; i < dimPosition.length; i++ )
 		{
 			Dimension dimension = (Dimension) dimensions[i];
 			List jsFilters = getDimensionJSFilterList( dimension.getName( ) );
 			LevelFilterHelper filterHelper = new LevelFilterHelper( dimension,
 					simpleLevelFilters,
 					levelFilters );
 			dimPosition[i] = filterHelper.getJSFilterResult( jsFilters, isBreakHierarchy );
 		}
 		return dimPosition;
 	}
 	
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.birt.data.engine.olap.data.api.ICubeQueryExcutorHelper#addJSFilter(org.eclipse.birt.data.engine.olap.util.filter.DimensionFilterEvalHelper)
 	 */
 	public void addJSFilter( IJSFilterHelper filterEvalHelper )
 	{
 		if ( filterEvalHelper.isAggregationFilter( ) == false )
 		{// Dimension filter
 			String dimesionName = filterEvalHelper.getDimensionName( );
 			List filterList = getDimensionJSFilterList( dimesionName );
 			filterList.add( filterEvalHelper );
 		}
 		else
 		{// Aggregation filter
 			aggrFilterHelpers.add( filterEvalHelper );
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.birt.data.engine.olap.data.api.ICubeQueryExcutorHelper#addJSFilter(java.util.List)
 	 */
 	public void addJSFilter( List filterEvalHelperList )
 	{
 		for ( int i = 0; i < filterEvalHelperList.size( ); i++ )
 		{
 			addJSFilter( (IJSFilterHelper) filterEvalHelperList.get( i ) );
 		}
 	}
 
 	
 	/**
 	 * @param isBreakHierarchy the isBreakHierarchy to set
 	 */
 	public void setBreakHierarchy( boolean isBreakHierarchy )
 	{
 		this.isBreakHierarchy = isBreakHierarchy;
 	}
 	
 	/**
 	 * 
 	 * @throws DataException
 	 */
 	private void validateComputedMeasureNames() throws DataException
 	{
		Set existNames = new HashSet(Arrays.asList( cube.getMeasureNames( ) ));
 		MeasureInfo[] mis = computedMeasureHelper.getAllComputedMeasureInfos( );
 		for (int i=0; i<mis.length; i++) 
 		{
 			String name = mis[i].getMeasureName( );
 			if (existNames.contains( name ))
 			{
 				throw new DataException(ResourceConstants.DUPLICATE_MEASURE_NAME, name);
 			}
 			existNames.add( name );
 		}
 		
 	}
 }

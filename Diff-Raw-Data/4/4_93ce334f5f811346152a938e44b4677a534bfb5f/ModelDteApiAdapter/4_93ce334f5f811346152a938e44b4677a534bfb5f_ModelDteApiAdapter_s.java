 /*
  *************************************************************************
  * Copyright (c) 2004, 2005 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *  
  *************************************************************************
  */
 
 package org.eclipse.birt.report.engine.adapter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.birt.core.data.Constants;
 import org.eclipse.birt.core.data.DataType;
 import org.eclipse.birt.core.data.ExpressionUtil;
 import org.eclipse.birt.core.exception.BirtException;
 import org.eclipse.birt.core.script.JavascriptEvalUtil;
 import org.eclipse.birt.data.engine.api.IBaseDataSetDesign;
 import org.eclipse.birt.data.engine.api.IBaseDataSourceDesign;
 import org.eclipse.birt.data.engine.api.IColumnDefinition;
 import org.eclipse.birt.data.engine.api.IComputedColumn;
 import org.eclipse.birt.data.engine.api.IConditionalExpression;
 import org.eclipse.birt.data.engine.api.IFilterDefinition;
 import org.eclipse.birt.data.engine.api.IInputParameterBinding;
 import org.eclipse.birt.data.engine.api.IJoinCondition;
 import org.eclipse.birt.data.engine.api.IJointDataSetDesign;
 import org.eclipse.birt.data.engine.api.IOdaDataSetDesign;
 import org.eclipse.birt.data.engine.api.IOdaDataSourceDesign;
 import org.eclipse.birt.data.engine.api.IParameterDefinition;
 import org.eclipse.birt.data.engine.api.IScriptDataSetDesign;
 import org.eclipse.birt.data.engine.api.IScriptDataSourceDesign;
 import org.eclipse.birt.data.engine.api.querydefn.BaseDataSetDesign;
 import org.eclipse.birt.data.engine.api.querydefn.BaseDataSourceDesign;
 import org.eclipse.birt.data.engine.api.querydefn.ColumnDefinition;
 import org.eclipse.birt.data.engine.api.querydefn.ComputedColumn;
 import org.eclipse.birt.data.engine.api.querydefn.ConditionalExpression;
 import org.eclipse.birt.data.engine.api.querydefn.FilterDefinition;
 import org.eclipse.birt.data.engine.api.querydefn.InputParameterBinding;
 import org.eclipse.birt.data.engine.api.querydefn.JoinCondition;
 import org.eclipse.birt.data.engine.api.querydefn.JointDataSetDesign;
 import org.eclipse.birt.data.engine.api.querydefn.OdaDataSetDesign;
 import org.eclipse.birt.data.engine.api.querydefn.OdaDataSourceDesign;
 import org.eclipse.birt.data.engine.api.querydefn.ParameterDefinition;
 import org.eclipse.birt.data.engine.api.querydefn.ScriptDataSetDesign;
 import org.eclipse.birt.data.engine.api.querydefn.ScriptDataSourceDesign;
 import org.eclipse.birt.data.engine.api.querydefn.ScriptExpression;
 import org.eclipse.birt.data.engine.api.script.IBaseDataSetEventHandler;
 import org.eclipse.birt.data.engine.api.script.IBaseDataSourceEventHandler;
 import org.eclipse.birt.data.engine.api.script.IScriptDataSetEventHandler;
 import org.eclipse.birt.data.engine.api.script.IScriptDataSourceEventHandler;
 import org.eclipse.birt.report.data.adapter.api.DataRequestSession;
 import org.eclipse.birt.report.engine.api.EngineException;
 import org.eclipse.birt.report.engine.executor.ExecutionContext;
 import org.eclipse.birt.report.engine.i18n.MessageConstants;
 import org.eclipse.birt.report.engine.script.internal.DataSetScriptExecutor;
 import org.eclipse.birt.report.engine.script.internal.DataSourceScriptExecutor;
 import org.eclipse.birt.report.engine.script.internal.ScriptDataSetScriptExecutor;
 import org.eclipse.birt.report.engine.script.internal.ScriptDataSourceScriptExecutor;
 import org.eclipse.birt.report.model.api.AggregationArgumentHandle;
 import org.eclipse.birt.report.model.api.ColumnHintHandle;
 import org.eclipse.birt.report.model.api.ComputedColumnHandle;
 import org.eclipse.birt.report.model.api.DataSetHandle;
 import org.eclipse.birt.report.model.api.DataSetParameterHandle;
 import org.eclipse.birt.report.model.api.DataSourceHandle;
 import org.eclipse.birt.report.model.api.ExtendedPropertyHandle;
 import org.eclipse.birt.report.model.api.FilterConditionHandle;
 import org.eclipse.birt.report.model.api.JoinConditionHandle;
 import org.eclipse.birt.report.model.api.JointDataSetHandle;
 import org.eclipse.birt.report.model.api.OdaDataSetHandle;
 import org.eclipse.birt.report.model.api.OdaDataSetParameterHandle;
 import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
 import org.eclipse.birt.report.model.api.OdaResultSetColumnHandle;
 import org.eclipse.birt.report.model.api.ParamBindingHandle;
 import org.eclipse.birt.report.model.api.ReportElementHandle;
 import org.eclipse.birt.report.model.api.ResultSetColumnHandle;
 import org.eclipse.birt.report.model.api.ScriptDataSetHandle;
 import org.eclipse.birt.report.model.api.ScriptDataSourceHandle;
 import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
 import org.eclipse.birt.report.model.api.metadata.IPropertyDefn;
 import org.eclipse.birt.report.model.elements.OdaDataSet;
 import org.mozilla.javascript.Scriptable;
 
 /**
  * An adapter class that creates data engine API interface objects from the
  * model.api objects for data set and data source definition.
  * 
  * The user of this adaptor can optionally provide an associated ExecutionContext
  * object and a Javascript scope. ExecutionContext is used to provide a context
  * for executing data source and data set event handlers. The Javascript scope
  * is used to evaluate data source and data set binding expressions. If a scope
  * is not provided, data source and data set bindings will not take effect.
  */
 public class ModelDteApiAdapter
 {
 	private ExecutionContext context;
 
 	private Scriptable jsScope;
 	
 	private DataRequestSession dteSession;
 
 	/**
 	 * @deprecated Construct an instance of this class directly
 	 */
 	public static ModelDteApiAdapter getInstance( )
 	{
 		return new ModelDteApiAdapter( );
 	}
 
 	/**
 	 * @deprecated use createDataSourceDesign( dataSource )
 	 */
 	public IBaseDataSourceDesign createDataSourceDesign(
 			DataSourceHandle dataSource, ExecutionContext context )
 			throws EngineException
 	{
 		try
 		{
 			ModelDteApiAdapter tmpAdaptor = new ModelDteApiAdapter( context,
 					null );
 			return tmpAdaptor.createDataSourceDesign( dataSource );
 		} catch ( BirtException e )
 		{
 			throw new EngineException( e.getLocalizedMessage( ) );
 		}
 	}
 
 	/**
 	 * @deprecated use createDataSetDesign( dataSource )
 	 */
 	public IBaseDataSetDesign createDataSetDesign( DataSetHandle dataSet,
 			ExecutionContext context ) throws EngineException
 	{
 		try
 		{
 			ModelDteApiAdapter tmpAdaptor = new ModelDteApiAdapter( context,
 					null );
 			return tmpAdaptor.createDataSetDesign( dataSet );
 		} catch ( BirtException e )
 		{
 			throw new EngineException( e.getLocalizedMessage( ) );
 		}
 	}
 
 	/**
 	 * Default constructor. Constructs an adaptor which uses no associated
 	 * Javascript scope and no report context.
 	 */
 	public ModelDteApiAdapter( )
 	{
 	}
 
 	/**
 	 * Constructs an instance with the given report context and scope
 	 * 
 	 * @param context
 	 *            Context for event handlers. May be null
 	 * @param jsScope
 	 *            Scope for evaluting property binding expressions. If null,
 	 *            property bindings have no effect
 	 */
 	public ModelDteApiAdapter( ExecutionContext context, Scriptable jsScope )
 	{
 		this.context = context;
 		this.jsScope = jsScope;
 	}
 
 	/**
 	 * Adapts the specified Model Data Source to a Data Engine API data source
 	 * design object
 	 */
 	public IBaseDataSourceDesign createDataSourceDesign(
 			DataSourceHandle dataSource ) throws BirtException
 	{
 		if ( dataSource instanceof OdaDataSourceHandle )
 			return newOdaDataSource( ( OdaDataSourceHandle ) dataSource );
 
 		if ( dataSource instanceof ScriptDataSourceHandle )
 			return newScriptDataSource( ( ScriptDataSourceHandle ) dataSource );
 
 		// any other types are not supported
 		assert false;
 		return null;
 	}
 
 	/**
 	 * Adapts the specified Model Data Set to a Data Engine API data set design
 	 * object
 	 */
 	public IBaseDataSetDesign createDataSetDesign( DataSetHandle dataSet )
 			throws BirtException
 	{
 		if ( dataSet instanceof OdaDataSetHandle )
 			return newOdaDataSet( ( OdaDataSetHandle ) dataSet,
 					context );
 
 		if ( dataSet instanceof ScriptDataSetHandle )
 			return newScriptDataSet( ( ScriptDataSetHandle ) dataSet,
 					context );
 		
 		if ( dataSet instanceof JointDataSetHandle )
 			return newJointDataSet( (JointDataSetHandle)dataSet, context);
 		// any other types are not supported
 		assert false;
 		return null;
 	}
 	
 	/**
 	 * Define data set and data source in DataEngine
 	 * @param dataSet
 	 * @param dteEngine
 	 * @throws BirtException
 	 */
 	public void defineDataSet( DataSetHandle dataSet, DataRequestSession dteSession )
 			throws BirtException
 	{
 		if ( dataSet == null || dteSession == null )
 			return;
 		this.dteSession = dteSession;
 		DataSourceHandle dataSource = dataSet.getDataSource( );
 		if ( dataSource != null )
 		{
 			doDefineDataSource( dataSource );
 		}
 		doDefineDataSet( dataSet );
 	}
 	
 	/**
 	 * 
 	 * @param dataSource
 	 * @throws BirtException
 	 */
 	private void doDefineDataSource( DataSourceHandle dataSource )
 			throws BirtException
 	{
 		dteSession.defineDataSource( createDataSourceDesign( dataSource ) );
 	}
 
 	/**
 	 * 
 	 * @param dataSet
 	 * @throws BirtException
 	 */
 	private void doDefineDataSet( DataSetHandle dataSet )
 			throws BirtException
 	{
 		if ( dataSet instanceof JointDataSetHandle )
 		{
 			JointDataSetHandle jointDataSet = (JointDataSetHandle) dataSet;
 			Iterator iter = ( (JointDataSetHandle) jointDataSet ).dataSetsIterator( );
 			while ( iter.hasNext( ) )
 			{
 				DataSetHandle childDataSet = (DataSetHandle) iter.next( );
 				if ( childDataSet != null )
 				{
 					DataSourceHandle childDataSource = childDataSet.getDataSource( );
 					if ( childDataSource != null )
 					{
 						doDefineDataSource( childDataSource );
 					}
 					doDefineDataSet( childDataSet );
 				}
 			}
 
 		}
 		dteSession.defineDataSet( createDataSetDesign( dataSet ) );
 	}
 
 	/**
 	 * Create an IJointDataSetDesign instance.
 	 * 
 	 * @param handle
 	 * @param context2
 	 * @return
 	 * @throws BirtException
 	 */
 	private IJointDataSetDesign newJointDataSet( JointDataSetHandle handle, ExecutionContext context2 ) throws BirtException
 	{
 		Iterator it = handle.joinConditionsIterator( );
 		List joinConditions = new ArrayList();
 		
 		JoinConditionHandle jc = null;
 		
 		while( it.hasNext( ) )
 		{
 			jc = (JoinConditionHandle)it.next( );
 			joinConditions.add( new JoinCondition( new ScriptExpression( jc.getLeftExpression( )), new ScriptExpression(jc.getRightExpression( )),toDteJoinOperator(jc.getOperator( )))) ;
 		}
 		
 		int joinType = toDteJoinType(jc.getJoinType( ));
 		
 		IBaseDataSetEventHandler eventHandler = new DataSetScriptExecutor(
 				handle, context );
 
 		JointDataSetDesign dteDataSet = new JointDataSetDesign( handle.getQualifiedName( ),
 				jc.getLeftDataSet( ),
 				jc.getRightDataSet( ),
 				joinType,
 				joinConditions );
 		dteDataSet.setEventHandler( eventHandler );
 		// Adapt base class properties
 		adaptBaseDataSet( handle, dteDataSet );
 
 		return dteDataSet;
 	}
 
 	private int toDteJoinOperator( String operator )
 	{
 		if( operator.equals( DesignChoiceConstants.JOIN_OPERATOR_EQALS ))
 			return IJoinCondition.OP_EQ;
 		return -1;
 	}
 	
 	private int toDteJoinType ( String joinType )
 	{
 		if( joinType.equals( DesignChoiceConstants.JOIN_TYPE_INNER ))
 		{
 			return IJointDataSetDesign.INNER_JOIN;
 		}
 		else if( joinType.equals( DesignChoiceConstants.JOIN_TYPE_LEFT_OUT ))
 		{
 			return IJointDataSetDesign.LEFT_OUTER_JOIN;
 		}
 		else if( joinType.equals( DesignChoiceConstants.JOIN_TYPE_RIGHT_OUT ))
 		{
 			return IJointDataSetDesign.RIGHT_OUTER_JOIN;
 		}
 		else if( joinType.equals( DesignChoiceConstants.JOIN_TYPE_FULL_OUT ))
 		{
 			return IJointDataSetDesign.FULL_OUTER_JOIN;
 		}
 		return -1;
 	}
 	
 	/**
 	 * Evaluates a property binding Javascript expression
 	 */
 	String evaluatePropertyBindingExpr( String expr ) throws BirtException
 	{
 		Object result = JavascriptEvalUtil.evaluateScript( null, jsScope, expr,
 				"property binding", 0 );
 		return result == null ? null : result.toString( );
 	}
 
 	IOdaDataSourceDesign newOdaDataSource( OdaDataSourceHandle source )
 			throws BirtException
 	{
 		OdaDataSourceDesign dteSource = new OdaDataSourceDesign( source
 				.getQualifiedName( ) );
 		IBaseDataSourceEventHandler eventHandler = new DataSourceScriptExecutor(
 				source, context );
 
 		dteSource.setEventHandler( eventHandler );
 
 		// Adapt base class properties
 		adaptBaseDataSource( source, dteSource );
 
 		// Adapt extended data source elements
 
 		// validate that a required attribute is specified
 		String driverName = source.getExtensionID( );
 		if ( driverName == null || driverName.length( ) == 0 )
 		{
 			throw new EngineException(
 					"Missing extenion id in data source definition, " + source.getName( ) ); //$NON-NLS-1$
 		}
 		dteSource.setExtensionID( driverName );
 
 		// static ROM properties defined by the ODA driver extension
 		Map staticProps = getExtensionProperties( source, source
 				.getExtensionPropertyDefinitionList( ) );
 		if ( staticProps != null && !staticProps.isEmpty( ) )
 		{
 			Iterator propNamesItr = staticProps.keySet( ).iterator( );
 			while ( propNamesItr.hasNext( ) )
 			{
 				String propName = ( String ) propNamesItr.next( );
 				assert ( propName != null );
 
 				String propValue;
 				// If property binding expression exists, use its evaluation
 				// result
 				String bindingExpr = source.getPropertyBinding( propName );
 				if ( needPropertyBinding( ) && bindingExpr != null
 						&& bindingExpr.length( ) > 0 )
 				{
 					propValue = evaluatePropertyBindingExpr( bindingExpr );
 				} else
 				{
 					propValue = ( String ) staticProps.get( propName );
 				}
 
 				dteSource.addPublicProperty( propName, propValue );
 			}
 		}
 
 		// private driver properties / private runtime data
 		Iterator elmtIter = source.privateDriverPropertiesIterator( );
 		if ( elmtIter != null )
 		{
 			while ( elmtIter.hasNext( ) )
 			{
 				ExtendedPropertyHandle modelProp = ( ExtendedPropertyHandle ) elmtIter
 						.next( );
 				dteSource.addPrivateProperty( modelProp.getName( ), modelProp
 						.getValue( ) );
 			}
 		}
 
         addPropertyConfigurationId( dteSource );
 
 		return dteSource;
 	}
 
 	/**
 	 * Adds the externalized property configuration id for use by 
 	 * a BIRT consumer application's propertyProvider extension.
 	 */
 	private void addPropertyConfigurationId( OdaDataSourceDesign dteSource )
 		throws BirtException
 	{
 		String configIdValue = dteSource.getExtensionID() + 
                                   Constants.ODA_PROP_CONFIG_KEY_SEPARATOR +
         						  dteSource.getName();
 		dteSource.addPublicProperty( Constants.ODA_PROP_CONFIGURATION_ID, configIdValue );
 	}
 	
 	/**
 	 * 
 	 * @param source
 	 * @return
 	 */
 	IScriptDataSourceDesign newScriptDataSource( ScriptDataSourceHandle source )
 	{
 		ScriptDataSourceDesign dteSource = new ScriptDataSourceDesign( source
 				.getQualifiedName( ) );
 		IScriptDataSourceEventHandler eventHandler = new ScriptDataSourceScriptExecutor(
 				source, context );
 
 		dteSource.setEventHandler( eventHandler );
 		// Adapt base class properties
 		adaptBaseDataSource( source, dteSource );
 
 		// Adapt script data source elements
 		dteSource.setOpenScript( source.getOpen( ) );
 		dteSource.setCloseScript( source.getClose( ) );
 		return dteSource;
 	}
 
 	/**
 	 * 
 	 * @param source
 	 * @param dest
 	 */
 	void adaptBaseDataSource( DataSourceHandle source, BaseDataSourceDesign dest )
 	{
 		dest.setBeforeOpenScript( source.getBeforeOpen( ) );
 		dest.setAfterOpenScript( source.getAfterOpen( ) );
 		dest.setBeforeCloseScript( source.getBeforeClose( ) );
 		dest.setAfterCloseScript( source.getAfterClose( ) );
 	}
 
 	IOdaDataSetDesign newOdaDataSet( OdaDataSetHandle modelDataSet,
 			ExecutionContext context ) throws BirtException
 	{
 		OdaDataSetDesign dteDataSet = new OdaDataSetDesign( modelDataSet
 				.getQualifiedName( ) );
 		IBaseDataSetEventHandler eventHandler = new DataSetScriptExecutor(
 				modelDataSet, context );
 
 		dteDataSet.setEventHandler( eventHandler );
 		// Adapt base class properties
 		adaptBaseDataSet( modelDataSet, dteDataSet );
 
 		// Adapt extended data set elements
 
 		// Set query text; if binding exists, use its result; otherwise
 		// use static design
 		String queryTextBinding = modelDataSet
 				.getPropertyBinding( OdaDataSet.QUERY_TEXT_PROP );
 		if ( needPropertyBinding( ) && queryTextBinding != null
 				&& queryTextBinding.length( ) > 0 )
 		{
 			dteDataSet
 					.setQueryText( evaluatePropertyBindingExpr( queryTextBinding ) );
 		} else
 		{
 			dteDataSet.setQueryText( modelDataSet.getQueryText( ) );
 		}
 
 		// type of extended data set
 		dteDataSet.setExtensionID( modelDataSet.getExtensionID( ) );
 
 		// result set name
 		dteDataSet.setPrimaryResultSetName( modelDataSet.getResultSetName( ) );
 
 		// static ROM properties defined by the ODA driver extension
 		Map staticProps = getExtensionProperties( modelDataSet, modelDataSet
 				.getExtensionPropertyDefinitionList( ) );
 		if ( staticProps != null && !staticProps.isEmpty( ) )
 		{
 			Iterator propNamesItr = staticProps.keySet( ).iterator( );
 			while ( propNamesItr.hasNext( ) )
 			{
 				String propName = ( String ) propNamesItr.next( );
 				assert ( propName != null );
 				String propValue;
 				String bindingExpr = modelDataSet.getPropertyBinding( propName );
 				if ( needPropertyBinding( ) && bindingExpr != null
 						&& bindingExpr.length( ) > 0 )
 				{
 					propValue = this.evaluatePropertyBindingExpr( bindingExpr );
 				} else
 				{
 					propValue = ( String ) staticProps.get( propName );
 				}
 				dteDataSet.addPublicProperty( ( String ) propName, propValue );
 			}
 		}
 
 		// private driver properties / private runtime data
 		Iterator elmtIter = modelDataSet.privateDriverPropertiesIterator( );
 		if ( elmtIter != null )
 		{
 			while ( elmtIter.hasNext( ) )
 			{
 				ExtendedPropertyHandle modelProp = ( ExtendedPropertyHandle ) elmtIter
 						.next( );
 				dteDataSet.addPrivateProperty( modelProp.getName( ), modelProp
 						.getValue( ) );
 			}
 		}
 
 		return dteDataSet;
 	}
 
 	IScriptDataSetDesign newScriptDataSet( ScriptDataSetHandle modelDataSet,
 			ExecutionContext context ) throws BirtException
 	{
 		ScriptDataSetDesign dteDataSet = new ScriptDataSetDesign( modelDataSet
 				.getQualifiedName( ) );
 
 		IScriptDataSetEventHandler eventHandler = new ScriptDataSetScriptExecutor(
 				modelDataSet, context );
 
 		dteDataSet.setEventHandler( eventHandler );
 
 		// Adapt base class properties
 		adaptBaseDataSet( modelDataSet, dteDataSet );
 
 		// Adapt script data set elements
 		dteDataSet.setOpenScript( modelDataSet.getOpen( ) );
 		dteDataSet.setFetchScript( modelDataSet.getFetch( ) );
 		dteDataSet.setCloseScript( modelDataSet.getClose( ) );
 		dteDataSet.setDescribeScript( modelDataSet.getDescribe( ) );
 
 		return dteDataSet;
 	}
 
 	void adaptBaseDataSet( DataSetHandle modelDataSet,
 			BaseDataSetDesign dteDataSet ) throws BirtException
 	{
 		if ( (!(modelDataSet instanceof JointDataSetHandle)) && modelDataSet.getDataSource( ) == null )
 			throw new EngineException(
 					"The data source of this data set can not be null." );
 
 		if ( !( modelDataSet instanceof JointDataSetHandle ) )
 		{
 			dteDataSet.setDataSource( modelDataSet.getDataSource( )
 					.getQualifiedName( ) );
 			dteDataSet.setBeforeOpenScript( modelDataSet.getBeforeOpen( ) );
 			dteDataSet.setAfterOpenScript( modelDataSet.getAfterOpen( ) );
 			dteDataSet.setOnFetchScript( modelDataSet.getOnFetch( ) );
 			dteDataSet.setBeforeCloseScript( modelDataSet.getBeforeClose( ) );
 			dteDataSet.setAfterCloseScript( modelDataSet.getAfterClose( ) );
			//cache row count
			dteDataSet.setCacheRowCount( modelDataSet.getCachedRowCount( ) );
 
 		}
 		populateParameter( modelDataSet, dteDataSet );
 	
 		populateComputedColumn( modelDataSet, dteDataSet );
 
 		populateFilter( modelDataSet, dteDataSet );
 
 		dteDataSet.setRowFetchLimit( modelDataSet.getRowFetchLimit( ) );
 		
 		mergeHints( modelDataSet, dteDataSet );
 
 	}
 
 	/**
 	 * 
 	 * @param modelDataSet
 	 * @param dteDataSet
 	 * @return
 	 */
 	private Iterator populateParameter( DataSetHandle modelDataSet, BaseDataSetDesign dteDataSet )
 	{
 		//dataset parameters definition
 		HashMap paramBindingCandidates = new HashMap( );
 
 		Iterator elmtIter = modelDataSet.parametersIterator( );
 		if ( elmtIter != null )
 		{
 			while ( elmtIter.hasNext( ) )
 			{
 				DataSetParameterHandle modelParam = ( DataSetParameterHandle ) elmtIter
 						.next( );
 				// collect input parameter default values as
 				// potential parameter binding if no explicit ones are
 				// defined for a parameter
 				if ( modelParam.isInput( ) )
 				{
 					String defaultValueExpr = null;
 					if ( modelParam instanceof OdaDataSetParameterHandle
 							&& ( (OdaDataSetParameterHandle) modelParam ).getParamName( ) != null )
 					{
 						defaultValueExpr = ExpressionUtil.createJSParameterExpression( ( (OdaDataSetParameterHandle) modelParam ).getParamName( ) );
 					}
 					else
 						defaultValueExpr = modelParam.getDefaultValue( );
 					if ( defaultValueExpr != null )
 					{
 						dteDataSet.addParameter( newParam( modelParam ) );
 
 						paramBindingCandidates.put( modelParam.getName( ),
 								defaultValueExpr );
 					}
 				}
 				else
 				{
 					dteDataSet.addParameter( newParam( modelParam ) );
 				}
 			}
 		}
 
 		// input parameter bindings
 		elmtIter = modelDataSet.paramBindingsIterator( );
 		if ( elmtIter != null )
 		{
 			while ( elmtIter.hasNext( ) )
 			{
 				ParamBindingHandle modelParamBinding = ( ParamBindingHandle ) elmtIter
 						.next( );
 				// replace default value of the same parameter, if defined
 				paramBindingCandidates.put( modelParamBinding.getParamName( ),
 						modelParamBinding.getExpression( ) );
 			}
 		}
 
 		// assign merged parameter bindings to the data set
 		if ( paramBindingCandidates.size( ) > 0 )
 		{
 			elmtIter = paramBindingCandidates.keySet( ).iterator( );
 			while ( elmtIter.hasNext( ) )
 			{
 				Object paramName = elmtIter.next( );
 				assert ( paramName != null && paramName instanceof String );
 				String expression = ( String ) paramBindingCandidates
 						.get( paramName );
 				dteDataSet.addInputParamBinding( newInputParamBinding(
 						( String ) paramName, expression ) );
 			}
 		}
 		return elmtIter;
 	}
 
 	/**
 	 * 
 	 * @param modelDataSet
 	 * @param dteDataSet
 	 * @throws EngineException
 	 */
 	private void populateComputedColumn( DataSetHandle modelDataSet, BaseDataSetDesign dteDataSet ) throws EngineException
 	{
 		// computed columns
 		Iterator elmtIter = modelDataSet.computedColumnsIterator( );
 		if ( elmtIter != null )
 		{
 			while ( elmtIter.hasNext( ) )
 			{
 				ComputedColumnHandle modelCmptdColumn = ( ComputedColumnHandle ) elmtIter
 						.next( );
 				IComputedColumn dteCmptdColumn = newComputedColumn( modelCmptdColumn );
 				dteDataSet.addComputedColumn( dteCmptdColumn );
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param modelDataSet
 	 * @param dteDataSet
 	 */
 	private void populateFilter( DataSetHandle modelDataSet, BaseDataSetDesign dteDataSet )
 	{
 		// filter conditions
 		Iterator elmtIter = modelDataSet.filtersIterator( );
 		if ( elmtIter != null )
 		{
 			while ( elmtIter.hasNext( ) )
 			{
 				FilterConditionHandle modelFilter = ( FilterConditionHandle ) elmtIter
 						.next( );
 				IFilterDefinition dteFilter = newFilter( modelFilter );
 				dteDataSet.addFilter( dteFilter );
 			}
 		}
 	}
 
 	/**
 	 */
 	private void mergeHints( DataSetHandle modelDataSet, BaseDataSetDesign dteDataSet )
 	{
 		// merge ResultSetHints and ColumnHints, the order is important.
 		// ResultSetHints will give each column a unique name, and
 		// column hints should base on the result of ResultSet hint.
 		// So in ResultSetHint list, the order of items should be
 		// ResultSetColumn and then ColumnHint.
 
 		// now merge model's result set column info into existing columnDefn
 		// with same column name, otherwise create new columnDefn
 		// based on the model's result set column
 		Iterator elmtIter = null;
 		if ( modelDataSet instanceof OdaDataSetHandle )
 		{
 			elmtIter = modelDataSet.resultSetIterator( );
 			if ( elmtIter != null )
 			{
 				while ( elmtIter.hasNext( ) )
 				{
 					OdaResultSetColumnHandle modelColumn = (OdaResultSetColumnHandle) elmtIter.next( );
 					if ( !modelColumn.getColumnName( )
 							.equals( modelColumn.getNativeName( ) ) )
 						dteDataSet.addResultSetHint( newColumnDefn( (ResultSetColumnHandle) modelColumn ) );
 				}
 			}
 		}
 		
 		elmtIter = modelDataSet.resultSetHintsIterator( );
 		if ( elmtIter != null )
 		{
 			while ( elmtIter.hasNext( ) )
 			{
 				ResultSetColumnHandle modelColumn = (ResultSetColumnHandle) elmtIter.next( );
 				dteDataSet.addResultSetHint( newColumnDefn( modelColumn ) );
 			}
 		}
 		
 		// merging result set column and column hints into DtE columnDefn;
 		// first create new columnDefn based on model's column hints
 		elmtIter = modelDataSet.columnHintsIterator( );
 		if ( elmtIter != null )
 		{
 			List columnDefns = dteDataSet.getResultSetHints( );
 			while ( elmtIter.hasNext( ) )
 			{
 				ColumnHintHandle modelColumnHint = ( ColumnHintHandle ) elmtIter
 						.next( );
 				ColumnDefinition existDefn = findColumnDefn( columnDefns,
 						modelColumnHint.getColumnName( ) );
 				if ( existDefn != null )
 					updateColumnDefn( existDefn, modelColumnHint );
 				else
 					dteDataSet
 							.addResultSetHint( newColumnDefn( modelColumnHint ) );
 			}
 		}
 	}
 
 	/**
 	 * Creates a new DtE API IParameterDefinition from a model's
 	 * DataSetParameterHandle.
 	 */
 	IParameterDefinition newParam( DataSetParameterHandle modelParam )
 	{
 		ParameterDefinition dteParam = new ParameterDefinition( );
 
 		dteParam.setName( modelParam.getName( ) );
 		if ( modelParam.getPosition( ) != null )
 			dteParam.setPosition( modelParam.getPosition( ).intValue( ) );
 		if( modelParam.getNativeDataType( ) != null )
 			dteParam.setNativeType( modelParam.getNativeDataType( ).intValue( ) );
 
 		dteParam.setType( toDteDataType( modelParam.getDataType( ) ) );
 		dteParam.setInputMode( modelParam.isInput( ) );
 		dteParam.setOutputMode( modelParam.isOutput( ) );
 		dteParam.setNullable( modelParam.allowNull( ) );
 		dteParam.setInputOptional( modelParam.isOptional( ) );
 		dteParam.setDefaultInputValue( modelParam.getDefaultValue( ) );
 
 		return dteParam;
 	}
 
 	/**
 	 * Creates a new DtE API InputParamBinding from a model's binding. Could
 	 * return null if no expression is bound.
 	 */
 	IInputParameterBinding newInputParamBinding(
 			ParamBindingHandle modelInputParamBndg )
 	{
 		// model provides binding by name only
 		return newInputParamBinding( modelInputParamBndg.getParamName( ),
 				modelInputParamBndg.getExpression( ) );
 	}
 
 	private IInputParameterBinding newInputParamBinding( String paramName,
 			String paramValue )
 	{
 		if ( paramValue == null )
 			return null; // no expression is bound
 		ScriptExpression paramValueExpr = new ScriptExpression( paramValue );
 		return new InputParameterBinding( paramName, paramValueExpr );
 	}
 
 	/**
 	 * Creates a new DtE API Computed Column from a model computed column. Could
 	 * return null if no expression is defined.
 	 * 
 	 * @throws EngineException
 	 */
 	IComputedColumn newComputedColumn( ComputedColumnHandle modelCmptdColumn )
 			throws EngineException
 	{
 		// no expression to define a computed column
 		if ( modelCmptdColumn.getExpression( ) == null
 				&& modelCmptdColumn.getAggregateFunction( ) == null )
 		{
 			throw new EngineException( MessageConstants.MISSING_COMPUTED_COLUMN_EXPRESSION_EXCEPTION,
 					modelCmptdColumn.getName( ) );
 		}
 
 		List argumentList = new ArrayList( );
 		Iterator argumentIter = modelCmptdColumn.argumentsIterator( );
 		while ( argumentIter.hasNext( ) )
 		{
 			argumentList.add( new ScriptExpression( ( (AggregationArgumentHandle) argumentIter.next( ) ).getValue( ) ) );
 		}
 
 		return new ComputedColumn( modelCmptdColumn.getName( ),
 				modelCmptdColumn.getExpression( ),
 				toDteDataType( modelCmptdColumn.getDataType( ) ),
 				modelCmptdColumn.getAggregateFunction( ),
 				modelCmptdColumn.getFilterExpression( ) == null
 						? null
 						: new ScriptExpression( modelCmptdColumn.getFilterExpression( ) ),
 				argumentList );
 	}
 
 	/**
 	 * Creates a new DtE API IJSExprFilter or IColumnFilter from a model's
 	 * filter condition. Could return null if no expression nor column operator
 	 * is defined.
 	 */
 	IFilterDefinition newFilter( FilterConditionHandle modelFilter )
 	{
 		String filterExpr = modelFilter.getExpr( );
 		if ( filterExpr == null || filterExpr.length( ) == 0 )
 			return null; // no filter defined
 
 		// converts to DtE exprFilter if there is no operator
 		String filterOpr = modelFilter.getOperator( );
 		if ( filterOpr == null || filterOpr.length( ) == 0 )
 			return new FilterDefinition( new ScriptExpression( filterExpr ) );
 
 		/*
 		 * has operator defined, try to convert filter condition to
 		 * operator/operand style column filter with 0 to 2 operands
 		 */
 
 		String column = filterExpr;
 		int dteOpr = toDteFilterOperator( filterOpr );
 		String operand1 = modelFilter.getValue1( );
 		String operand2 = modelFilter.getValue2( );
 		return new FilterDefinition( new ConditionalExpression( column, dteOpr,
 				operand1, operand2 ) );
 	}
 
 	private IColumnDefinition newColumnDefn( ResultSetColumnHandle modelColumn )
 	{
 		ColumnDefinition newColumn = new ColumnDefinition( modelColumn.getColumnName( ) );
 		if ( modelColumn.getPosition( ) != null )
 			newColumn.setColumnPosition( modelColumn.getPosition( ).intValue( ) );
 		if ( modelColumn.getNativeDataType( ) != null )
 			newColumn.setNativeDataType( modelColumn.getNativeDataType( )
 					.intValue( ) );
 		newColumn.setDataType( toDteDataType( modelColumn.getDataType( ) ) );
 		return newColumn;
 	}
 
 	private void updateColumnDefn( ColumnDefinition dteColumn,
 			ColumnHintHandle modelColumnHint )
 	{
 		assert dteColumn.getColumnName( ).equals(
 				modelColumnHint.getColumnName( ) );
 		dteColumn.setAlias( modelColumnHint.getAlias( ) );
 
 		String exportConstant = modelColumnHint.getExport( );
 		if ( exportConstant != null )
 		{
 			int exportHint = IColumnDefinition.DONOT_EXPORT; // default value
 			if ( exportConstant
 					.equals( DesignChoiceConstants.EXPORT_TYPE_IF_REALIZED ) )
 				exportHint = IColumnDefinition.EXPORT_IF_REALIZED;
 			else if ( exportConstant
 					.equals( DesignChoiceConstants.EXPORT_TYPE_ALWAYS ) )
 				exportHint = IColumnDefinition.ALWAYS_EXPORT;
 			else
 				assert exportConstant
 						.equals( DesignChoiceConstants.EXPORT_TYPE_NONE );
 
 			dteColumn.setExportHint( exportHint );
 		}
 
 		String searchConstant = modelColumnHint.getSearching( );
 		if ( searchConstant != null )
 		{
 			int searchHint = IColumnDefinition.NOT_SEARCHABLE;
 			if ( searchConstant
 					.equals( DesignChoiceConstants.SEARCH_TYPE_INDEXED ) )
 				searchHint = IColumnDefinition.SEARCHABLE_IF_INDEXED;
 			else if ( searchConstant
 					.equals( DesignChoiceConstants.SEARCH_TYPE_ANY ) )
 				searchHint = IColumnDefinition.ALWAYS_SEARCHABLE;
 			else
 				assert searchConstant
 						.equals( DesignChoiceConstants.SEARCH_TYPE_NONE );
 
 			dteColumn.setSearchHint( searchHint );
 		}
 
 	}
 
 	private IColumnDefinition newColumnDefn( ColumnHintHandle modelColumnHint )
 	{
 		ColumnDefinition newColumn = new ColumnDefinition( modelColumnHint
 				.getColumnName( ) );
 		updateColumnDefn( newColumn, modelColumnHint );
 		return newColumn;
 	}
 
 	/**
 	 * Find the DtE columnDefn from the given list of columnDefns that matches
 	 * the given columnName.
 	 */
 	private ColumnDefinition findColumnDefn( List columnDefns, String columnName )
 	{
 		assert columnName != null;
 		if ( columnDefns == null )
 			return null; // no list to find from
 		Iterator iter = columnDefns.iterator( );
 		if ( iter == null )
 			return null;
 
 		// iterate thru each columnDefn, and looks for a match of
 		// specified column name
 		while ( iter.hasNext( ) )
 		{
 			ColumnDefinition column = ( ColumnDefinition ) iter.next( );
 			if ( columnName.equals( column.getColumnName( ) ) )
 				return column;
 		}
 		return null;
 	}
 
 	public static int toDteDataType( String modelDataType )
 	{
 		if ( modelDataType == null )
 			return DataType.UNKNOWN_TYPE;
 		if ( modelDataType.equals( DesignChoiceConstants.COLUMN_DATA_TYPE_ANY ) )
 			return DataType.ANY_TYPE;
 		if ( modelDataType
 				.equals( DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER ) )
 			return DataType.INTEGER_TYPE;
 		if ( modelDataType
 				.equals( DesignChoiceConstants.COLUMN_DATA_TYPE_STRING ) )
 			return DataType.STRING_TYPE;
 		if ( modelDataType
 				.equals( DesignChoiceConstants.COLUMN_DATA_TYPE_DATETIME ) )
 			return DataType.DATE_TYPE;
 		if ( modelDataType
 				.equals( DesignChoiceConstants.COLUMN_DATA_TYPE_DATE ) )
 			return DataType.SQL_DATE_TYPE;
 		if ( modelDataType
 				.equals( DesignChoiceConstants.COLUMN_DATA_TYPE_TIME ) )
 			return DataType.SQL_TIME_TYPE;
 		if ( modelDataType
 				.equals( DesignChoiceConstants.COLUMN_DATA_TYPE_DECIMAL ) )
 			return DataType.DECIMAL_TYPE;
 		if ( modelDataType
 				.equals( DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT ) )
 			return DataType.DOUBLE_TYPE;
 		if ( modelDataType
 				.equals( DesignChoiceConstants.COLUMN_DATA_TYPE_BOOLEAN ) )
 			return DataType.BOOLEAN_TYPE;	
 
 		return DataType.UNKNOWN_TYPE;
 	}
 
 	// Convert model operator value to DtE IColumnFilter enum value
 	public static int toDteFilterOperator( String modelOpr )
 	{
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_EQ ) )
 			return IConditionalExpression.OP_EQ;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_NE ) )
 			return IConditionalExpression.OP_NE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_LT ) )
 			return IConditionalExpression.OP_LT;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_LE ) )
 			return IConditionalExpression.OP_LE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_GE ) )
 			return IConditionalExpression.OP_GE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_GT ) )
 			return IConditionalExpression.OP_GT;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_BETWEEN ) )
 			return IConditionalExpression.OP_BETWEEN;
 		if ( modelOpr
 				.equals( DesignChoiceConstants.FILTER_OPERATOR_NOT_BETWEEN ) )
 			return IConditionalExpression.OP_NOT_BETWEEN;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_NULL ) )
 			return IConditionalExpression.OP_NULL;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_NOT_NULL ) )
 			return IConditionalExpression.OP_NOT_NULL;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_TRUE ) )
 			return IConditionalExpression.OP_TRUE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_FALSE ) )
 			return IConditionalExpression.OP_FALSE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_LIKE ) )
 			return IConditionalExpression.OP_LIKE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_TOP_N ) )
 			return IConditionalExpression.OP_TOP_N;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_BOTTOM_N ) )
 			return IConditionalExpression.OP_BOTTOM_N;
 		if ( modelOpr
 				.equals( DesignChoiceConstants.FILTER_OPERATOR_TOP_PERCENT ) )
 			return IConditionalExpression.OP_TOP_PERCENT;
 		if ( modelOpr
 				.equals( DesignChoiceConstants.FILTER_OPERATOR_BOTTOM_PERCENT ) )
 			return IConditionalExpression.OP_BOTTOM_PERCENT;
 		
 		/*		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_ANY ) )
 			return IConditionalExpression.OP_ANY;*/
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_MATCH ) )
 			return IConditionalExpression.OP_MATCH;
 		
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_NOT_LIKE ))
 			return IConditionalExpression.OP_NOT_LIKE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_NOT_MATCH ))
 			return IConditionalExpression.OP_NOT_MATCH;
 		assert false; // unknown filter operator
 		return IConditionalExpression.OP_NONE;
 	}
 
 	/*
 	 * Gets the data handle's static ROM extension properties name and value
 	 * pairs in String values and returns them in a Map
 	 */
 	private Map getExtensionProperties( ReportElementHandle dataHandle,
 			List driverPropList )
 	{
 		if ( driverPropList == null || driverPropList.isEmpty( ) )
 			return null; // nothing to add
 
 		Map properties = new HashMap( );
 		Iterator elmtIter = driverPropList.iterator( );
 		while ( elmtIter.hasNext( ) )
 		{
 			IPropertyDefn modelExtProp = ( IPropertyDefn ) elmtIter.next( );
 
 			// First get extension property's name
 			String propName = modelExtProp.getName( );
 			assert ( propName != null && propName.length( ) > 0 );
 
 			// Use property name to get property value
 			Object propValueObj = dataHandle.getProperty( modelExtProp
 					.getName( ) );
 
 			/*
 			 * An ODA consumer does not distinguish whether a property value is
 			 * not set or explicitly set to null. Its handling is pushed down to
 			 * the underlying data provider.
 			 */
 			String propValue = ( propValueObj == null ) ? null : propValueObj
 					.toString( );
 			properties.put( propName, propValue );
 		}
 
 		return properties;
 	}
 
 	/**
 	 * temp method to decide whether need property binding
 	 * 
 	 * @return
 	 */
 	private boolean needPropertyBinding( )
 	{
 		if ( this.context == null || this.jsScope == null )
 			return false;
 		else
 			return true;
 	}
 }

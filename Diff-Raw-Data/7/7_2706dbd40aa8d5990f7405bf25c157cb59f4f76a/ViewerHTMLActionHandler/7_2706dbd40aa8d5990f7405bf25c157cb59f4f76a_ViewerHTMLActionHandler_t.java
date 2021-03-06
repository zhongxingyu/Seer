 /*************************************************************************************
  * Copyright (c) 2004 Actuate Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Actuate Corporation - Initial implementation.
  ************************************************************************************/
 
 package org.eclipse.birt.report.service;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.eclipse.birt.report.IBirtConstants;
 import org.eclipse.birt.report.engine.api.HTMLActionHandler;
 import org.eclipse.birt.report.engine.api.HTMLRenderContext;
 import org.eclipse.birt.report.engine.api.IAction;
 import org.eclipse.birt.report.engine.api.IReportDocument;
 import org.eclipse.birt.report.engine.api.IReportRunnable;
 import org.eclipse.birt.report.engine.api.PDFRenderContext;
 import org.eclipse.birt.report.engine.api.script.IReportContext;
 import org.eclipse.birt.report.model.api.ModuleHandle;
 import org.eclipse.birt.report.service.api.InputOptions;
 import org.eclipse.birt.report.utility.DataUtil;
 import org.eclipse.birt.report.utility.ParameterAccessor;
 
 /**
  * HTML action handler for url generation.
  */
 class ViewerHTMLActionHandler extends HTMLActionHandler
 {
 
 	/**
 	 * Logger for this handler.
 	 */
 
 	protected Logger log = Logger.getLogger( ViewerHTMLActionHandler.class
 			.getName( ) );
 
 	/**
 	 * Document instance.
 	 */
 
 	protected IReportDocument document = null;
 
 	/**
 	 * Locale of the requester.
 	 */
 
 	protected Locale locale = null;
 
 	/**
 	 * Page number of the action requester.
 	 */
 
 	protected long page = -1;
 
 	/**
 	 * if the page is embedded, the bookmark should always be a url to submit.
 	 */
 	protected boolean isEmbeddable = false;
 
 	/**
 	 * RTL option setting by the command line or URL parameter.
 	 */
 
 	protected boolean isRtl = false;
 
 	/**
 	 * if wanna use the master page, then set it to true.
 	 */
 
 	protected boolean isMasterPageContent = true;
 
 	/**
 	 * host format
 	 */
 	protected String hostFormat = null;
 
 	/**
 	 * resource folder setting
 	 */
 	protected String resourceFolder = null;
 
 	/**
 	 * SVG option setting by URL parameter.
 	 */
 	protected Boolean svg = null;
 
 	/**
 	 * indicates whether under designer mode
 	 */
 	protected String isDesigner = null;
 
 	/**
 	 * Constructor.
 	 */
 	public ViewerHTMLActionHandler( )
 	{
 	}
 
 	/**
 	 * Constructor. This is for renderTask.
 	 * 
 	 * @param document
 	 * @param page
 	 * @param locale
 	 * @param isEmbeddable
 	 * @param isRtl
 	 * @param isMasterPageContent
 	 * @param format
 	 * @param svg
 	 * @param isDesigner
 	 */
 
 	public ViewerHTMLActionHandler( IReportDocument document, long page,
 			Locale locale, boolean isEmbeddable, boolean isRtl,
 			boolean isMasterPageContent, String format, Boolean svg,
 			String isDesigner )
 	{
 		this.document = document;
 		this.page = page;
 		this.locale = locale;
 		this.isEmbeddable = isEmbeddable;
 		this.isRtl = isRtl;
 		this.isMasterPageContent = isMasterPageContent;
 		this.hostFormat = format;
 		this.svg = svg;
 		this.isDesigner = isDesigner;
 	}
 
 	/**
 	 * Constructor. This is for runAndRender task.
 	 * 
 	 * @param locale
 	 * @param isEmbeddable
 	 * @param isRtl
 	 * @param isMasterPageContent
 	 * @param format
 	 * @param svg
 	 * @param isDesigner
 	 */
 
 	public ViewerHTMLActionHandler( Locale locale, boolean isRtl,
 			boolean isMasterPageContent, String format, Boolean svg,
 			String isDesigner )
 	{
 		this.locale = locale;
 		this.isRtl = isRtl;
 		this.isMasterPageContent = isMasterPageContent;
 		this.hostFormat = format;
 		this.svg = svg;
 		this.isDesigner = isDesigner;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.birt.report.engine.api.HTMLActionHandler#getURL(org.eclipse.birt.report.engine.api.IAction,
 	 *      org.eclipse.birt.report.engine.api.script.IReportContext)
 	 */
 
 	public String getURL( IAction actionDefn, IReportContext context )
 	{
 		if ( actionDefn == null )
 			return null;
 
 		switch ( actionDefn.getType( ) )
 		{
 			case IAction.ACTION_BOOKMARK :
 			{
 				return buildBookmarkAction( actionDefn, context );
 			}
 			case IAction.ACTION_HYPERLINK :
 			{
 				return actionDefn.getActionString( );
 			}
 			case IAction.ACTION_DRILLTHROUGH :
 			{
 				return buildDrillAction( actionDefn, context );
 			}
 		}
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.birt.report.engine.api.HTMLActionHandler#getURL(org.eclipse.birt.report.engine.api.IAction,
 	 *      java.lang.Object)
 	 */
 	public String getURL( IAction actionDefn, Object context )
 	{
 		if ( actionDefn == null )
 			return null;
 		if ( context instanceof IReportContext )
 			return getURL( actionDefn, (IReportContext) context );
 
 		throw new IllegalArgumentException( "The context is of wrong type." ); //$NON-NLS-1$
 	}
 
 	/**
 	 * Build URL for bookmark.
 	 * 
 	 * @param action
 	 * @param context
 	 * @return the bookmark url
 	 */
 
 	protected String buildBookmarkAction( IAction action, IReportContext context )
 	{
 		if ( action == null || context == null )
 			return null;
 
 		// Get Base URL
 		String baseURL = null;
 		Object renderContext = getRenderContext( context );
 		if ( renderContext instanceof HTMLRenderContext )
 		{
 			baseURL = ( (HTMLRenderContext) renderContext ).getBaseURL( );
 		}
 		if ( renderContext instanceof PDFRenderContext )
 		{
 			baseURL = ( (PDFRenderContext) renderContext ).getBaseURL( );
 		}
 
 		if ( baseURL == null )
 			baseURL = IBirtConstants.VIEWER_PREVIEW;;
 
 		// check whether need replace servlet pattern to extract
 		baseURL = checkBaseURLWithExtractPattern( action, baseURL );
 
 		// Get bookmark
 		String bookmark = action.getBookmark( );
 
 		if ( baseURL.lastIndexOf( IBirtConstants.SERVLET_PATH_FRAMESET ) > 0
 				|| baseURL.lastIndexOf( IBirtConstants.SERVLET_PATH_RUN ) > 0 )
 		{
 			// In frameset mode, use javascript function to fire Ajax request to
 			// link to internal bookmark
 			// In run mode, append bookmark at the end of URL
 			String func = "catchBookmark('" + ParameterAccessor.htmlEncode( bookmark ) + "');"; //$NON-NLS-1$ //$NON-NLS-2$
 			return "javascript:try{" + func + "}catch(e){parent." + func + "};"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 
 		// Save the URL String
 		StringBuffer link = new StringBuffer( );
 
 		boolean realBookmark = false;
 
 		if ( this.document != null )
 		{
 			long pageNumber = this.document
 					.getPageNumber( action.getBookmark( ) );
 			realBookmark = ( pageNumber == this.page && !isEmbeddable );
 		}
 
 		try
 		{
 			bookmark = URLEncoder.encode( bookmark,
 					ParameterAccessor.UTF_8_ENCODE );
 		}
 		catch ( UnsupportedEncodingException e )
 		{
 			// Does nothing
 		}
 
 		link.append( baseURL );
 		link.append( ParameterAccessor.QUERY_CHAR );
 
 		// append user-defined query string if it is extract pattern
 		if ( isContainExtractInfo( action ) )
 		{
 			try
 			{
				link.append( action.getActionString( ) );
 				link.append( ParameterAccessor.PARAMETER_SEPARATOR );
 			}
 			catch ( Exception e )
 			{
 			}
 		}
 
 		// if the document is not null, then use it
 		if ( document != null )
 		{
 			link.append( ParameterAccessor.PARAM_REPORT_DOCUMENT );
 			link.append( ParameterAccessor.EQUALS_OPERATOR );
 			String documentName = document.getName( );
 
 			try
 			{
 				documentName = URLEncoder.encode( documentName,
 						ParameterAccessor.UTF_8_ENCODE );
 			}
 			catch ( UnsupportedEncodingException e )
 			{
 				// Does nothing
 			}
 			link.append( documentName );
 		}
 		else if ( action.getReportName( ) != null
 				&& action.getReportName( ).length( ) > 0 )
 		{
 			link.append( ParameterAccessor.PARAM_REPORT );
 			link.append( ParameterAccessor.EQUALS_OPERATOR );
 			String reportName = getReportName( context, action );
 			try
 			{
 				reportName = URLEncoder.encode( reportName,
 						ParameterAccessor.UTF_8_ENCODE );
 			}
 			catch ( UnsupportedEncodingException e )
 			{
 				// do nothing
 			}
 			link.append( reportName );
 		}
 		else
 		{
 			// its an iternal bookmark
 			return "#" + action.getActionString( ); //$NON-NLS-1$
 		}
 
 		if ( locale != null )
 		{
 			link.append( ParameterAccessor.getQueryParameterString(
 					ParameterAccessor.PARAM_LOCALE, locale.toString( ) ) );
 		}
 
 		if ( isRtl )
 		{
 			link.append( ParameterAccessor.getQueryParameterString(
 					ParameterAccessor.PARAM_RTL, String.valueOf( isRtl ) ) );
 		}
 
 		if ( svg != null )
 		{
 			link.append( ParameterAccessor.getQueryParameterString(
 					ParameterAccessor.PARAM_SVG, String.valueOf( svg
 							.booleanValue( ) ) ) );
 		}
 
 		if ( isDesigner != null )
 		{
 			link.append( ParameterAccessor.getQueryParameterString(
 					ParameterAccessor.PARAM_DESIGNER, String
 							.valueOf( isDesigner ) ) );
 		}
 
 		// add isMasterPageContent
 		link.append( ParameterAccessor.getQueryParameterString(
 				ParameterAccessor.PARAM_MASTERPAGE, String
 						.valueOf( this.isMasterPageContent ) ) );
 
 		// append resource folder setting
 		try
 		{
 			if ( resourceFolder != null )
 			{
 				String res = URLEncoder.encode( resourceFolder,
 						ParameterAccessor.UTF_8_ENCODE );
 				link.append( ParameterAccessor.getQueryParameterString(
 						ParameterAccessor.PARAM_RESOURCE_FOLDER, res ) );
 			}
 		}
 		catch ( UnsupportedEncodingException e )
 		{
 		}
 
 		if ( realBookmark )
 		{
 			link.append( "#" ); //$NON-NLS-1$
 			link.append( bookmark );
 		}
 		else
 		{
 			link.append( ParameterAccessor.getQueryParameterString(
 					ParameterAccessor.PARAM_BOOKMARK, bookmark ) );
 
 			// Bookmark is TOC name.
 			if ( !action.isBookmark( ) )
 				link.append( ParameterAccessor.getQueryParameterString(
 						ParameterAccessor.PARAM_ISTOC, "true" ) ); //$NON-NLS-1$
 		}
 
 		return link.toString( );
 	}
 
 	/**
 	 * builds URL for drillthrough action
 	 * 
 	 * @param action
 	 *            instance of the IAction instance
 	 * @param context
 	 *            the context for building the action string
 	 * @return a URL
 	 */
 	protected String buildDrillAction( IAction action, IReportContext context )
 	{
 		if ( action == null || context == null )
 			return null;
 
 		String baseURL = null;
 		Object renderContext = getRenderContext( context );
 		if ( renderContext instanceof HTMLRenderContext )
 		{
 			baseURL = ( (HTMLRenderContext) renderContext ).getBaseURL( );
 		}
 		if ( renderContext instanceof PDFRenderContext )
 		{
 			baseURL = ( (PDFRenderContext) renderContext ).getBaseURL( );
 		}
 
 		if ( baseURL == null )
 			baseURL = IBirtConstants.VIEWER_PREVIEW;
 
 		// check whether need replace servlet pattern to extract
 		baseURL = checkBaseURLWithExtractPattern( action, baseURL );
 
 		StringBuffer link = new StringBuffer( );
 		String reportName = getReportName( context, action );
 
 		if ( reportName != null && !reportName.equals( "" ) ) //$NON-NLS-1$
 		{
 			link.append( baseURL );
 
 			link
 					.append( reportName.toLowerCase( )
 							.endsWith( ".rptdocument" ) ? "?__document=" : "?__report=" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
 			try
 			{
 				link.append( URLEncoder.encode( reportName,
 						ParameterAccessor.UTF_8_ENCODE ) );
 			}
 			catch ( UnsupportedEncodingException e1 )
 			{
 				// It should not happen. Does nothing
 			}
 
 			// add format support
 			String format = action.getFormat( );
 			if ( format == null || format.length( ) == 0 )
 				format = hostFormat;
 			if ( format != null && format.length( ) > 0 )
 			{
 				link.append( ParameterAccessor.getQueryParameterString(
 						ParameterAccessor.PARAM_FORMAT, format ) );
 			}
 
 			// append user-defined query string if it is extract pattern
 			if ( isContainExtractInfo( action ) )
 			{
 				try
 				{
 					link.append( ParameterAccessor.PARAMETER_SEPARATOR );
					link.append( action.getActionString( ) );
 				}
 				catch ( Exception e )
 				{
 				}
 			}
 
 			// Adds the parameters
 			if ( action.getParameterBindings( ) != null )
 			{
 				Iterator paramsIte = action.getParameterBindings( ).entrySet( )
 						.iterator( );
 				while ( paramsIte.hasNext( ) )
 				{
 					Map.Entry entry = (Map.Entry) paramsIte.next( );
 					try
 					{
 						String key = (String) entry.getKey( );
 						Object valueObj = entry.getValue( );
 						if ( valueObj != null )
 						{
 							Object[] values;
 							if ( valueObj instanceof Object[] )
 							{
 								values = (Object[]) valueObj;
 							}
 							else
 							{
 								values = new Object[1];
 								values[0] = valueObj;
 							}
 
 							for ( int i = 0; i < values.length; i++ )
 							{
 								// TODO: here need the get the format from the
 								// parameter.
 								String value = DataUtil
 										.getDisplayValue( values[i] );
 
 								if ( value != null )
 								{
 									link
 											.append( ParameterAccessor
 													.getQueryParameterString(
 															URLEncoder
 																	.encode(
 																			key,
 																			ParameterAccessor.UTF_8_ENCODE ),
 															URLEncoder
 																	.encode(
 																			value,
 																			ParameterAccessor.UTF_8_ENCODE ) ) );
 								}
 								else
 								{
 									// pass NULL value
 									link
 											.append( ParameterAccessor
 													.getQueryParameterString(
 															ParameterAccessor.PARAM_ISNULL,
 															URLEncoder
 																	.encode(
 																			key,
 																			ParameterAccessor.UTF_8_ENCODE ) ) );
 								}
 							}
 						}
 					}
 					catch ( UnsupportedEncodingException e )
 					{
 						// Does nothing
 					}
 				}
 
 				// Adding overwrite.
 				if ( !reportName.toLowerCase( ).endsWith(
 						ParameterAccessor.SUFFIX_REPORT_DOCUMENT )
 						&& baseURL
 								.lastIndexOf( IBirtConstants.SERVLET_PATH_FRAMESET ) > 0 )
 				{
 					link.append( ParameterAccessor.getQueryParameterString(
 							ParameterAccessor.PARAM_OVERWRITE, String
 									.valueOf( true ) ) );
 				}
 			}
 
 			if ( locale != null )
 			{
 				link.append( ParameterAccessor.getQueryParameterString(
 						ParameterAccessor.PARAM_LOCALE, locale.toString( ) ) );
 			}
 			if ( isRtl )
 			{
 				link.append( ParameterAccessor.getQueryParameterString(
 						ParameterAccessor.PARAM_RTL, String.valueOf( isRtl ) ) );
 			}
 
 			if ( svg != null )
 			{
 				link.append( ParameterAccessor.getQueryParameterString(
 						ParameterAccessor.PARAM_SVG, String.valueOf( svg
 								.booleanValue( ) ) ) );
 			}
 
 			if ( isDesigner != null )
 			{
 				link.append( ParameterAccessor.getQueryParameterString(
 						ParameterAccessor.PARAM_DESIGNER, String
 								.valueOf( isDesigner ) ) );
 			}
 
 			// add isMasterPageContent
 			link.append( ParameterAccessor.getQueryParameterString(
 					ParameterAccessor.PARAM_MASTERPAGE, String
 							.valueOf( this.isMasterPageContent ) ) );
 
 			// append resource folder setting
 			try
 			{
 				if ( resourceFolder != null )
 				{
 					String res = URLEncoder.encode( resourceFolder,
 							ParameterAccessor.UTF_8_ENCODE );
 					link.append( ParameterAccessor.getQueryParameterString(
 							ParameterAccessor.PARAM_RESOURCE_FOLDER, res ) );
 				}
 			}
 			catch ( UnsupportedEncodingException e )
 			{
 			}
 
 			// add bookmark
 			String bookmark = action.getBookmark( );
 			if ( bookmark != null )
 			{
 
 				try
 				{
 					// In PREVIEW mode or pdf format, don't support bookmark as
 					// parameter
 					if ( baseURL
 							.lastIndexOf( IBirtConstants.SERVLET_PATH_PREVIEW ) > 0
 							|| IBirtConstants.PDF_RENDER_FORMAT
 									.equalsIgnoreCase( format ) )
 					{
 						link.append( "#" ); //$NON-NLS-1$
 
 						// use TOC to find bookmark, only link to document file
 						if ( !action.isBookmark( )
 								&& reportName.toLowerCase( ).endsWith(
 										".rptdocument" ) ) //$NON-NLS-1$
 						{
 							InputOptions options = new InputOptions( );
 							options.setOption( InputOptions.OPT_LOCALE, locale );
 							bookmark = BirtReportServiceFactory
 									.getReportService( ).findTocByName(
 											reportName, bookmark, options );
 						}
 
 						link.append( URLEncoder.encode( bookmark,
 								ParameterAccessor.UTF_8_ENCODE ) );
 					}
 					else
 					{
 						bookmark = URLEncoder.encode( bookmark,
 								ParameterAccessor.UTF_8_ENCODE );
 						link.append( ParameterAccessor.getQueryParameterString(
 								ParameterAccessor.PARAM_BOOKMARK, bookmark ) );
 
 						// Bookmark is TOC name.
 						if ( !action.isBookmark( ) )
 							link.append( ParameterAccessor
 									.getQueryParameterString(
 											ParameterAccessor.PARAM_ISTOC,
 											"true" ) ); //$NON-NLS-1$
 					}
 
 				}
 				catch ( UnsupportedEncodingException e )
 				{
 					// Does nothing
 				}
 			}
 		}
 
 		return link.toString( );
 	}
 
 	/**
 	 * Gets the effective report path.
 	 * 
 	 * @param context
 	 * @param action
 	 * @return the effective report path
 	 */
 
 	private String getReportName( IReportContext context, IAction action )
 	{
 		assert context != null;
 		assert action != null;
 		String reportName = action.getReportName( );
 		IReportRunnable runnable = context.getReportRunnable( );
 
 		// if WORKING_FOLDER_ACCESS_ONLY is false, return absolute path.
 		// else, return relative path.
 		if ( runnable != null && !ParameterAccessor.isWorkingFolderAccessOnly( ) )
 		{
 			ModuleHandle moduleHandle = runnable.getDesignHandle( )
 					.getModuleHandle( );
 			URL url = moduleHandle.findResource( reportName, -1 );
 			if ( url != null )
 			{
 				if ( "file".equals( url.getProtocol( ) ) ) //$NON-NLS-1$
 					reportName = url.getFile( );
 				else
 					reportName = url.toExternalForm( );
 			}
 		}
 		return reportName;
 	}
 
 	/**
 	 * @return the resourceFolder
 	 */
 	public String getResourceFolder( )
 	{
 		return resourceFolder;
 	}
 
 	/**
 	 * @param resourceFolder
 	 *            the resourceFolder to set
 	 */
 	public void setResourceFolder( String resourceFolder )
 	{
 		this.resourceFolder = resourceFolder;
 	}
 
 	/**
 	 * Replace URL with extract servlet pattern
 	 * 
 	 * @param action
 	 * @param baseURL
 	 * @return
 	 */
 	private String checkBaseURLWithExtractPattern( IAction action,
 			String baseURL )
 	{
 		if ( isContainExtractInfo( action ) )
 		{
 			// replace servlet pattern to extract path
 			while ( baseURL.endsWith( "/" ) ) //$NON-NLS-1$
 			{
 				baseURL = baseURL.substring( 0, baseURL.length( ) - 2 );
 			}
 
 			int index = baseURL.lastIndexOf( "/" ); //$NON-NLS-1$
 			if ( index >= 0 )
 				baseURL = baseURL.substring( 0, index );
 
 			baseURL = baseURL + IBirtConstants.SERVLET_PATH_EXTRACT;
 		}
 
 		return baseURL;
 	}
 
 	/**
 	 * Returns the flag that indicated whether contains data extract information
 	 * 
 	 * @param action
 	 * @return
 	 */
 	private boolean isContainExtractInfo( IAction action )
 	{
 		// check action string whether contains data extraction information
 		String actionString = action.getActionString( );
 		if ( actionString != null )
 		{
 			if ( actionString
 					.indexOf( ParameterAccessor.PARAM_DATA_EXTRACT_FORMAT ) >= 0
 					|| actionString
 							.indexOf( ParameterAccessor.PARAM_DATA_EXTRACT_EXTENSION ) >= 0 )
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 }

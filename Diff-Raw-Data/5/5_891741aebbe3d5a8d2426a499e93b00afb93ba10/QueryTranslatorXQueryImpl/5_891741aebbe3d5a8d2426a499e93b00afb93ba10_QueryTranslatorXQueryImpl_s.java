 /**
  * Copyright (C) [2013] [The FURTHeR Project]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.utah.further.ds.impl.service.query.logic;
 
 import static edu.utah.further.core.api.constant.Constants.Scope.PROTOTYPE;
 import static edu.utah.further.ds.api.util.AttributeName.META_DATA;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.io.ByteArrayInputStream;
 import java.lang.invoke.MethodHandles;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.lang.Validate;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import edu.utah.further.core.api.chain.AttributeContainerImpl;
 import edu.utah.further.core.api.chain.ChainRequest;
 import edu.utah.further.core.api.collections.CollectionUtil;
 import edu.utah.further.core.api.exception.ApplicationError;
 import edu.utah.further.core.api.exception.ApplicationException;
 import edu.utah.further.core.api.exception.WsException;
 import edu.utah.further.core.api.lang.ReflectionUtil;
 import edu.utah.further.core.api.xml.XmlService;
 import edu.utah.further.core.chain.ChainRequestImpl;
 import edu.utah.further.core.query.domain.SearchQueryTo;
 import edu.utah.further.core.xml.xquery.XQueryService;
 import edu.utah.further.ds.api.service.query.logic.QueryTranslator;
 import edu.utah.further.ds.api.util.AttributeName;
 import edu.utah.further.fqe.ds.api.domain.DsMetaData;
 import edu.utah.further.fqe.ds.api.domain.QueryContext;
 import edu.utah.further.fqe.ds.api.util.FqeDsQueryContextUtil;
 import edu.utah.further.mdr.ws.api.service.rest.AssetServiceRest;
 
 /**
  * Query translator implementation that utilizes XQuery to translate queries. Use of this
  * class requires setting the location of the XQuery artifact in the
  * {@link AttributeName#QUERY_TRANSLATION}
  * <p>
  * -----------------------------------------------------------------------------------<br>
  * (c) 2008-2012 FURTHeR Project, Health Sciences IT, University of Utah<br>
  * Contact: {@code <further@utah.edu>}<br>
  * Biomedical Informatics, 26 South 2000 East<br>
  * Room 5775 HSEB, Salt Lake City, UT 84112<br>
  * Day Phone: 1-801-581-4080<br>
  * -----------------------------------------------------------------------------------
  * 
  * @author N. Dustin Schultz {@code <dustin.schultz@utah.edu>}
  * @version Jul 30, 2013
  */
 @Service("queryTranslatorXquery")
 @Qualifier("impl")
 @Scope(PROTOTYPE)
 public class QueryTranslatorXQueryImpl implements QueryTranslator
 {
 	// ========================= CONSTANTS =================================
 
 	/**
 	 * A logger that helps identify this class' printouts.
 	 */
 	private static final Logger log = getLogger(MethodHandles.lookup().lookupClass());
 
 	/**
 	 * The byte encoding to use with Strings
 	 */
 	private static final Charset UTF_8 = Charset.forName("UTF-8");
 
 	// ========================= FIELDS =====================================
 
 	// ========================= DEPENDENCIES ===========================
 
 	/**
 	 * XQuery Service
 	 */
 	@Autowired
 	@Qualifier("xqueryService")
 	private XQueryService xqueryService;
 
 	/**
 	 * Service for marshalling and unmarshalling to XML
 	 */
 	@Autowired
 	private XmlService xmlService;
 
 	/**
 	 * MDR web service client.
 	 */
 	@Autowired
 	@Qualifier("mdrAssetServiceRestClient")
 	private AssetServiceRest assetServiceRest;
 
 	// ================== IMPL: QueryTranslatorXQueryImpl ===================
 
 	@SuppressWarnings("unchecked")
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.utah.further.ds.api.service.query.logic.QueryTranslator#translate(edu.utah.
 	 * further.fqe.ds.api.domain.QueryContext)
 	 */
 	@Override
 	public <T> T translate(final QueryContext queryContext,
 			final Map<String, Object> attributes)
 	{
 		if (log.isTraceEnabled())
 		{
 			log.trace("Translating query " + queryContext.getQuery() + " ... ");
 		}
 
 		final ChainRequest request = new ChainRequestImpl(new AttributeContainerImpl(
 				attributes));
 
 		final String query = FqeDsQueryContextUtil.marshalSearchQuery(xmlService,
 				queryContext.getQuery());
 
 		final String pathToXquery = request.getAttribute(AttributeName.QUERY_TRANSLATION);
 
 		if (pathToXquery == null)
 		{
 			throw new ApplicationException(
 					AttributeName.QUERY_TRANSLATION.getLabel()
 							+ " label was not set; don't know where to search for xquery translation");
 		}
 		String xQuery;
 		try
 		{
 			xQuery = assetServiceRest.getActiveResourceContentByPath(pathToXquery);
 
 		}
 		catch (final WsException e)
 		{
 			throw new ApplicationException("Unable to find XQuery for query translation",
 					e);
 		}
 
 		final DsMetaData dsMetaData = request.getAttribute(META_DATA);
 
 		@SuppressWarnings("serial")
 		final Map<String, String> parameters = new HashMap<String, String>()
 		{
 			{
 				put("tgNmspcId", dsMetaData.getNamespaceId().toString());
 				put("tgNmspcName", dsMetaData.getName());
 			}
 		};
 
 		final String result = xqueryService.executeIntoString(new ByteArrayInputStream(
 				xQuery.getBytes(UTF_8)), new ByteArrayInputStream(query.getBytes(UTF_8)),
 				parameters);
 
 		if (log.isTraceEnabled())
 		{
 			log.trace("XQuery Translation result: " + result);
 		}
 
 		final Object unmarshalResult;
 		try
 		{
 			unmarshalResult = xmlService.unmarshal(
 					new ByteArrayInputStream(result.getBytes()), xmlService
 							.options()
 							.addClass(SearchQueryTo.class)
 							.addClass(ApplicationError.class)
 							.buildContext()
 							.setRootNamespaceUris(CollectionUtil.<String> newSet()));
 		}
 		catch (final JAXBException e)
 		{
 			throw new ApplicationException(
 					"Unable to unmarshal SearchQuery after query translation", e);
 		}
 
 		if (ReflectionUtil.instanceOf(unmarshalResult, ApplicationError.class))
 		{
 			final ApplicationError error = (ApplicationError) unmarshalResult;
			log.error("Query translation returned error, translation failed");
			throw new ApplicationException(error.getCode(), error.getMessage());
 		}
 
 		// Sanity check
 		Validate.isTrue(ReflectionUtil.instanceOf(unmarshalResult, SearchQueryTo.class));
 
 		return (T) unmarshalResult;
 	}
 
 	// ========================= GET/SET ===========================
 
 	/**
 	 * Return the xqueryService property.
 	 * 
 	 * @return the xqueryService
 	 */
 	public XQueryService getXqueryService()
 	{
 		return xqueryService;
 	}
 
 	/**
 	 * Set a new value for the xqueryService property.
 	 * 
 	 * @param xqueryService
 	 *            the xqueryService to set
 	 */
 	public void setXqueryService(final XQueryService xqueryService)
 	{
 		this.xqueryService = xqueryService;
 	}
 
 	/**
 	 * Return the xmlService property.
 	 * 
 	 * @return the xmlService
 	 */
 	public XmlService getXmlService()
 	{
 		return xmlService;
 	}
 
 	/**
 	 * Set a new value for the xmlService property.
 	 * 
 	 * @param xmlService
 	 *            the xmlService to set
 	 */
 	public void setXmlService(final XmlService xmlService)
 	{
 		this.xmlService = xmlService;
 	}
 
 	/**
 	 * Return the assetServiceRest property.
 	 * 
 	 * @return the assetServiceRest
 	 */
 	public AssetServiceRest getAssetServiceRest()
 	{
 		return assetServiceRest;
 	}
 
 	/**
 	 * Set a new value for the assetServiceRest property.
 	 * 
 	 * @param assetServiceRest
 	 *            the assetServiceRest to set
 	 */
 	public void setAssetServiceRest(final AssetServiceRest assetServiceRest)
 	{
 		this.assetServiceRest = assetServiceRest;
 	}
 
 }

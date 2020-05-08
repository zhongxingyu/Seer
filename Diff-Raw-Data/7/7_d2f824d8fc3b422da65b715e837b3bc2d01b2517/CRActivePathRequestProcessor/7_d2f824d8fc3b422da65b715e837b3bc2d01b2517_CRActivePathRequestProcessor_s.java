 package com.gentics.cr;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.datasource.Datasource;
 import com.gentics.api.lib.datasource.DatasourceException;
 import com.gentics.api.lib.exception.ParserException;
 import com.gentics.api.lib.expressionparser.ExpressionParserException;
 import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.util.ArrayHelper;
 import com.gentics.cr.util.RequestWrapper;
 
 /**
  * This RequestProcessor fetches the active path from a child element
  * passed in the request as contentid to the root element.
  * Either a root element is given, or it will go up until there is no further
  * parent.
  * It does not support doNavigation.
  * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 541 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class CRActivePathRequestProcessor extends RequestProcessor {
 	/**
 	 * Root id key.
 	 */
 	private static final String ROOT_ID = "rootid";
 	/**
 	 * Logger.
 	 */
 	private static final Logger LOG = Logger
 			.getLogger(CRActivePathRequestProcessor.class);
 
 
 	/**
 	 * Create a new instance of CRRequestProcessor.
 	 * @param config configuration.
 	 * @throws CRException in case of error.
 	 */
 	public CRActivePathRequestProcessor(final CRConfig config)
 			throws CRException {
 		super(config);
 	}
 
 	/**
 	 * 
 	 * Fetch the matching objects using the given CRRequest. 
 	 * @param request CRRequest
 	 * @param doNavigation defines if to fetch children
 	 * @return resulting objects
 	 * @throws CRException in case of error.
 	 */
 	public final Collection<CRResolvableBean> getObjects(
 			final CRRequest request,
 			final boolean doNavigation)
 			throws CRException {
 		Datasource ds = null;
 		DatasourceFilter dsFilter;
 		Collection<CRResolvableBean> collection = null;
		RequestWrapper rW = request.getRequestWrapper();
		String rootId = rW.getParameter(ROOT_ID);
 		if (request != null) {

 			// Parse the given expression and create a datasource filter
 			try {
 				ds = this.config.getDatasource();
 				if (ds == null) {
 					throw (new DatasourceException("No Datasource available."));
 				}
 
 				dsFilter = request.getPreparedFilter(config, ds);
 
 				// add base resolvables
 				if (this.resolvables != null) {
 					for (Iterator<String> it = this.resolvables
 							.keySet().iterator(); it.hasNext();) {
 						String name = it.next();
 						dsFilter.addBaseResolvable(name,
 								this.resolvables.get(name));
 					}
 				}
 
 				
 				CRResolvableBean bean = loadSingle(ds, request);
 				if (bean != null) {
 					
 					collection = getParents(ds, bean, rootId, request);
 					if (collection == null) {
 						collection = new Vector<CRResolvableBean>();
 					}
 					collection.add(bean);
 				}
 				
 
 			} catch (ParserException e) {
 				LOG.error("Error getting filter for Datasource.", e);
 				throw new CRException(e);
 			} catch (ExpressionParserException e) {
 				LOG.error("Error getting filter for Datasource.", e);
 				throw new CRException(e);
 			} catch (DatasourceException e) {
 				LOG.error("Error getting result from Datasource.", e);
 				throw new CRException(e);
 			} finally {
 				CRDatabaseFactory.releaseDatasource(ds);
 			}
 		}
 		return collection;
 	}
 
 	/**
 	 * Create prefill attributes.
 	 * @param request request object
 	 * @return attributes as string array
 	 */
 	private String[] getPrefillAttributes(final CRRequest request) {
 		String[] prefillAttributes = request.getAttributeArray();
 		prefillAttributes = ArrayHelper.removeElements(
 				prefillAttributes,
 				"contentid", "updatetimestamp");
 		return prefillAttributes;
 	}
 	
 	/**
 	 * Fetch a single element.
 	 * @param ds datasource	
 	 * @param request request
 	 * @return element
 	 * @throws DatasourceException in case of error
 	 * @throws ParserException in case of error
 	 * @throws ExpressionParserException in case of error
 	 */
 	private CRResolvableBean loadSingle(final Datasource ds,
 			final CRRequest request) 
 			throws DatasourceException, ParserException,
 			ExpressionParserException {
 		CRResolvableBean bean = null;
 		String[] attributes = getPrefillAttributes(request);
 		Collection<Resolvable> col = this.toResolvableCollection(ds.getResult(
 				request.getPreparedFilter(config, ds),
 				attributes,
 				request.getStart().intValue(),
 				request.getCount().intValue(),
 				request.getSorting()));
 		if (col != null && col.size() > 0) {
 			bean = new CRResolvableBean(col.iterator().next(), attributes);
 		}
 		return bean;
 	}
 	
 	
 	/**
 	 * Fetches the parents.
 	 * @param ds datasource
 	 * @param current current child element
 	 * @param rootContentId id of the desired root
 	 * @param request request object
 	 * @return collection of parrents.
 	 * @throws CRException 
 	 * @throws ExpressionParserException 
 	 * @throws ParserException 
 	 * @throws DatasourceException 
 	 */
 	private Collection<CRResolvableBean> getParents(final Datasource ds,
 			final CRResolvableBean current, final String rootContentId,
 			final CRRequest request) throws CRException,
 			DatasourceException, ParserException, ExpressionParserException {
 		Collection<CRResolvableBean> ret = null;
 		
 		String mother = current.getMother_id();
 		if (mother != null && !(current.getMother_type() + "." + mother)
 				.equals(rootContentId) && !"0".equals(mother)) {
 			CRRequest nRequest = request.Clone();
 			nRequest.setRequestFilter(null);
 			nRequest.setContentid(current.getMother_type() + "." + mother);
 			CRResolvableBean parent = loadSingle(ds, nRequest);
 			ret = getParents(ds, parent, rootContentId, nRequest);
 			if (ret == null) {
 				ret = new Vector<CRResolvableBean>();
 			}
 			ret.add(parent);
 		}
 		return ret;
 	}
 
 	@Override
 	public void finalize() {
 	}
 
 }

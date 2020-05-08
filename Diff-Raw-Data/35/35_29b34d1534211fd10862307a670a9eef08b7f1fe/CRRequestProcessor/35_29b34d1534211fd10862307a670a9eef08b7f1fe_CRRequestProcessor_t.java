 package com.gentics.cr;
 
 import java.util.Collection;
 import java.util.HashMap;
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
 
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 541 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class CRRequestProcessor extends RequestProcessor {
 	
 	private static Logger logger = Logger.getLogger(CRRequestProcessor.class);
 
 	private HashMap<String, Resolvable> resolvables = null;
 	
 	/**
	 * Create a new instance of CRRequestProcessor.
 	 * @param config
 	 * @throws CRException
 	 */
 	public CRRequestProcessor(CRConfig config) throws CRException {
 		super(config);
 	}
 	
 	
 	
 	/**
 	 * 
 	 * Fetch the matching objects using the given CRRequest. 
 	 * @param request CRRequest
 	 * @param doNavigation defines if to fetch children
 	 * @return resulting objects
 	 * @throws CRException TODO javadocs
 	 */
 	public Collection<CRResolvableBean> getObjects(final CRRequest request,
 			final boolean doNavigation) throws CRException {
 		Datasource ds = null;
 		DatasourceFilter dsFilter;
 		Vector<CRResolvableBean> collection = new Vector<CRResolvableBean>();
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
 					for (Iterator<String> it = this.resolvables.keySet()
 							.iterator(); it.hasNext();) {
 						String name = it.next();
 						dsFilter.addBaseResolvable(name,
 								this.resolvables.get(name));
 					}
 				}
 
 				String[] prefillAttributes = request.getAttributeArray();
				prefillAttributes = ArrayHelper.removeElements(prefillAttributes, "contentid", "updatetimestamp");
 				// do the query
 				Collection<Resolvable> col = this.toResolvableCollection(
 						ds.getResult(dsFilter, prefillAttributes,
 								request.getStart().intValue(),
 								request.getCount().intValue(),
 								request.getSorting()));
 
 				// convert all objects to serializeable beans
 				if (col != null) {
 					for (Iterator<Resolvable> it = col.iterator(); it.hasNext();
 							) {
 						CRResolvableBean crBean = new CRResolvableBean(
 								it.next(), request.getAttributeArray());
						if (config.getFolderType().equals(crBean.getObj_type()) && doNavigation) {
 							//Process child elements
							String fltr = "object.folder_id=='" + crBean.getContentid() + "'";
 							if (request.getChildFilter() != null) {
								fltr += "AND (" + request.getChildFilter() + ")";
 							}
							//If object is a folder => retrieve the children of the object
 							CRRequest childReq = request.Clone();
 							childReq.setRequestFilter(fltr);
							crBean.fillChildRepository(getNavigation(childReq));
 						}
 						collection.add(this.replacePlinks(crBean, request));
 					}
 				}
 
 			} catch (ParserException e) {
 				logger.error("Error getting filter for Datasource.", e);
 				throw new CRException(e);
 			} catch (ExpressionParserException e) {
 				logger.error("Error getting filter for Datasource.", e);
 				throw new CRException(e);
 			} catch (DatasourceException e) {
 				logger.error("Error getting result from Datasource.", e);
 				throw new CRException(e);
 			} finally {
 				CRDatabaseFactory.releaseDatasource(ds);
 			}
 		}
 		return collection;
 	}
 
 	@Override
 	public void finalize() {
 	}
 
 }

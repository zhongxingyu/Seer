 package com.gentics.cr;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.portlet.PortletRequest;
 import javax.servlet.ServletRequest;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.datasource.Datasource;
 import com.gentics.api.lib.datasource.Datasource.Sorting;
import com.gentics.api.lib.etc.ObjectTransformer;
 import com.gentics.api.lib.exception.ParserException;
 import com.gentics.api.lib.expressionparser.Expression;
 import com.gentics.api.lib.expressionparser.ExpressionParserException;
 import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.api.portalnode.connector.PortalConnectorFactory;
 import com.gentics.cr.util.CRUtil;
 import com.gentics.cr.util.RequestWrapper;
 
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
  * 
  * @version $Revision: 541 $
  * @author $Author: supnig@constantinopel.at $
  * 
  */
 public class CRRequest implements Cloneable, Serializable {
 
 	/**
 	 * non unique serialization id.
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Log4j logger.
 	 */
 	private static Logger log = Logger.getLogger(CRRequest.class);
 	/**
 	 * key for storing the wordmatch parameter into.
 	 */
 	public static final String WORDMATCH_KEY = "wordmatch";
 
 	/**
 	 * key for storing the permissions parameter into.
 	 */
 	public static final String PERMISSIONS_KEY = "permissions";
 
 	/**
 	 * key for storing the attribute array.
 	 */
 	private static final String ATTRIBUTE_ARRAY_KEY = "attributeArray";
 
 	/**
 	 * parameter to define a prefix for host relative urls (if not set, links
 	 * will not be prefixed.).
 	 * 
 	 * (replaces relative links in a,area and img tags.)
 	 */
 	public static final String PREFIX_HOST_RELATIVE_LINKS = "prefixHostRelativeLinks";
 
 	private HashMap<String, Resolvable> objectsToDeploy = new HashMap<String, Resolvable>();
 
 	private HashMap<String, Object> params = new HashMap<String, Object>();
 
 	/**
 	 * Create a new instance of CRRequest.
 	 * 
 	 * @param requestFilter -
 	 *            Rule to fetch the objects
 	 * @param startString -
 	 *            Number of start element
 	 * @param countString -
 	 *            Count of elements from start element
 	 * @param sortArray -
 	 *            sorting array e.g. String[]{"contentid:asc", "name:desc"}
 	 * @param attributeArray -
 	 *            Attributes to fetch
 	 * @param plinkAttributeArray -
 	 *            Attributes to perform plink replacement within
 	 */
 	public CRRequest(final String requestFilter, final String startString, final String countString,
 		final String[] sortArray, final String[] attributeArray, final String[] plinkAttributeArray) {
 		this(requestFilter, startString, countString, sortArray, attributeArray, plinkAttributeArray, null);
 	}
 
 	/**
 	 * Create a new instance of CRRequest.
 	 * 
 	 * @param requestFilter -
 	 *            Rule to fetch the objects
 	 */
 	public CRRequest(final String requestFilter) {
 		setRequestFilter(requestFilter);
 	}
 
 	/**
 	 * Create a new instance of CRRequest.
 	 * 
 	 * @param requestFilter -
 	 *            Rule to fetch the objects
 	 * @param startString -
 	 *            Number of start element
 	 * @param countString -
 	 *            Count of elements from start element
 	 * @param sortArray -
 	 *            sorting array e.g. String[]{"contentid:asc", "name:desc"}
 	 * @param attributeArray -
 	 *            Attributes to fetch
 	 * @param plinkAttributeArray -
 	 *            Attributes to perform plink replacement within
 	 * @param childFilter -
 	 *            Rule to fetch the child elements (Navigation)
 	 */
 	public CRRequest(final String requestFilter, final String startString, final String countString,
 		final String[] sortArray, final String[] attributeArray, final String[] plinkAttributeArray,
 		final String childFilter) {
 
 		setRequestFilter(requestFilter);
 		setStartString(startString);
 		setCountString(countString);
 		setSortArray(sortArray);
 		setAttributeArray(attributeArray);
 		setPlinkAttributeArray(plinkAttributeArray);
 		setChildFilter(childFilter);
 	}
 
 	/**
 	 * Create new instance of CRRequest.
 	 */
 	public CRRequest() {
 	}
 
 	/**
 	 * Gets the configured request object. This method is deprecated use
 	 * {@link CRRequest#getRequestWrapper()} instead.
 	 * 
 	 * @return request
 	 */
 	@Deprecated
 	public Object getRequest() {
 		return this.get("request");
 	}
 
 	/**
 	 * gets the {@link RequestWrapper}.
 	 * 
 	 * @return the requestWrapper
 	 */
 	public RequestWrapper getRequestWrapper() {
 		return (RequestWrapper) this.get("requestWrapper");
 	}
 
 	/**
 	 * Gets the configured response object.
 	 * 
 	 * @return response
 	 */
 	public Object getResponse() {
 		return this.get("response");
 	}
 
 	/**
 	 * Set the request object. This method is deprecated use
 	 * {@link CRRequest#setRequest(RequestWrapper)} instead.
 	 * 
 	 * @param request
 	 *            the {@link ServletRequest} or the {@link PortletRequest}
 	 */
 	@Deprecated
 	public final void setRequest(final Object request) {
 		this.set("request", request);
 	}
 
 	/**
 	 * Set the request object.
 	 * 
 	 * @param requestWrapper
 	 *            RequestWrapper that holds the {@link ServletRequest} or the
 	 *            {@link PortletRequest}
 	 */
 	public final void setRequest(final RequestWrapper requestWrapper) {
 		this.set("requestWrapper", requestWrapper);
 		this.set("request", requestWrapper.getRequest());
 	}
 
 	/**
 	 * Set the response object.
 	 * 
 	 * @param response
 	 */
 	public void setResponse(Object response) {
 		this.set("response", response);
 	}
 
 	/**
 	 * Add one object to the filter that can be accessed in the rule using the
 	 * given name.
 	 * 
 	 * @param name
 	 * @param object
 	 */
 	public void addObjectForFilterDeployment(String name, Resolvable object) {
 		this.objectsToDeploy.put(name, object);
 	}
 
 	/**
 	 * Set the request filter. This filter is the rule that is used to fetch the
 	 * objects. You can deploy custom objects to this rule using
 	 * addObjectForFilterDeployment
 	 * 
 	 * @param requestFilter
 	 */
 	public void setRequestFilter(String requestFilter) {
 		this.set("requestFilter", requestFilter);
 	}
 
 	/**
 	 * Get the configured request filter. This filter is the rule that is used
 	 * to fetch the objects. You can deploy custom objects to this rule using
 	 * addObjectForFilterDeployment
 	 * 
 	 * @return requestFilter
 	 */
 	public String getRequestFilter() {
 		return (String) this.get("requestFilter");
 	}
 
 	/**
 	 * Sets the object number to start from as String e.g. "2" gets <count>
 	 * objects starting from object nr. 2
 	 * 
 	 * @param startString
 	 */
 	public void setStartString(String startString) {
		this.set("start", ObjectTransformer.getInteger(startString, 0));
 	}
 
 	/**
 	 * Gets the object number to start from as String e.g. "2" gets <count>
 	 * objects starting from object nr. 2
 	 * 
 	 * @return start number
 	 */
 	public String getStartString() {
 		Integer start = (Integer) this.get("start");
 		if (start != null) {
 			return (start.toString());
 		} else {
 			return ("0");
 		}
 	}
 
 	/**
 	 * Sets the number of objects to request as String e.g. "5" gets 5 objects
 	 * starting from <start>, defaults to "-1" and gets all objects
 	 * 
 	 * @param countString
 	 */
 	public void setCountString(String countString) {
		this.set("count", ObjectTransformer.getInteger(countString, -1));
 	}
 
 	/**
 	 * Get the number of objects to request as String e.g. "5" gets 5 objects
 	 * starting from <start>, defaults to "-1" and gets all objects
 	 * 
 	 * @return countString
 	 */
 	public String getCountString() {
 		Integer count = (Integer) this.get("count");
 		if (count != null) {
 			return (count.toString());
 		} else {
 			return ("-1");
 		}
 	}
 
 	/**
 	 * The sortArray defines the sort order of the returned objects. You can
 	 * sort by multiple parameters. e.g.
 	 * String[]{"object.name:asc","object.contentid:desc"}
 	 * 
 	 * @param sortArray
 	 */
 	public void setSortArray(String[] sortArray) {
 		this.set("sortArray", sortArray);
 	}
 
 	/**
 	 * The sortArray defines the sort order of the returned objects. You can
 	 * sort by multiple parameters. e.g.
 	 * String[]{"object.name:asc","object.contentid:desc"}
 	 * 
 	 * @return sortArray
 	 */
 	public String[] getSortArray() {
 		return (String[]) this.get("sortArray");
 	}
 
 	/**
 	 * Sets an array of attribute names that are to be prefilled for the
 	 * requested objects.
 	 * 
 	 * @param attributeArray array to use for prefilling
 	 */
 	public void setAttributeArray(final String[] attributeArray) {
 		this.set("attributeArray", attributeArray);
 	}
 
 	/**
 	 * Gets an array of attribute names that are to be prefilled for the
 	 * requested objects. 
 	 * @return Defaults to String[] { "contentid" }
 	 */
 	public String[] getAttributeArray() {
 		return getAttributeArray("contentid");
 	}
 
 	public String[] getAttributeArray(String idAttribute) {
 		String[] attributeArray = (String[]) this.get(ATTRIBUTE_ARRAY_KEY);
 		if (attributeArray == null) {
 			return new String[] { idAttribute };
 		}
 		return attributeArray;
 	}
 
 	/**
 	 * Sets an array of attribute names wherein plinks are to be replaced.
 	 * 
 	 * @param plinkAttributeArray
 	 */
 	public void setPlinkAttributeArray(String[] plinkAttributeArray) {
 		this.set("plinkAttributeArray", plinkAttributeArray);
 	}
 
 	/**
 	 * Gets an array of attribute names wherein plinks are to be replaced.
 	 * 
 	 * @return plinkAttributeArray, returns null if array is not set => no
 	 *         plinks are to be replaced
 	 */
 	public String[] getPlinkAttributeArray() {
 		return (String[]) this.get("plinkAttributeArray");
 	}
 
 	/**
 	 * Sets the child filter rule that is used to fetch sub elements. This is
 	 * only to be set for getNavigation For detailed description see
 	 * <setRequestFilter>
 	 * 
 	 * @param childFilter
 	 */
 	public void setChildFilter(String childFilter) {
 		this.set("childFilter", childFilter);
 	}
 
 	/**
 	 * Gets the child filter rule that is used to fetch sub elements. This is
 	 * only to be set for getNavigation For detailed description see
 	 * <setRequestFilter>
 	 * 
 	 * @return childFilter
 	 */
 	public String getChildFilter() {
 
 		return (String) this.get("childFilter");
 	}
 
 	/**
 	 * Set a HashMap of objects to deploy to the filter. See
 	 * <addObjectForFilterDeployment> for detailed description.
 	 * 
 	 * @param objectsToDeploy
 	 */
 	public void setObjectsToDeploy(HashMap<String, Resolvable> objectsToDeploy) {
 		this.objectsToDeploy = objectsToDeploy;
 	}
 
 	/**
 	 * Get a HashMap of objects to deploy to the filter. See
 	 * <addObjectForFilterDeployment> for detailed description.
 	 * 
 	 * @return objectsToDeploy
 	 */
 	public HashMap<String, Resolvable> getObjectsToDeploy() {
 		return objectsToDeploy;
 	}
 
 	/**
 	 * Gets the object number to start from as Integer e.g. 2 gets <count>
 	 * objects starting from object nr. 2
 	 * 
 	 * @return start
 	 */
 	public Integer getStart() {
 		Integer start = (Integer) this.get("start");
 		if (start == null) {
 			return (new Integer(0));
 		}
 		return start;
 	}
 
 	/**
 	 * Get the number of objects to request as Integer e.g. 5 gets 5 objects
 	 * starting from <start>, defaults to -1 and gets all objects.
 	 * 
 	 * @return count
 	 */
 	public Integer getCount() {
 		Integer count = (Integer) this.get("count");
 		if (count == null) {
 			return (new Integer(-1));
 		}
 		return count;
 	}
 
 	/**
 	 * Returns the configured sort array prepared as Datasource Sorting array.
 	 * 
 	 * @return sorting
 	 */
 	public Sorting[] getSorting() {
 		return CRUtil.convertSorting(this.getSortArray());
 	}
 
 	/**
 	 * Clone current instance.
 	 * 
 	 * @return cloned instance of CRRequest
 	 */
 	public CRRequest Clone() {
 		CRRequest ret = new CRRequest();
 		Iterator<String> it_p = this.params.keySet().iterator();
 		while (it_p.hasNext()) {
 			String key = it_p.next();
 			ret.set(key, this.params.get(key));
 		}
 
 		Iterator<String> it = this.objectsToDeploy.keySet().iterator();
 		while (it.hasNext()) {
 			String key = it.next();
 			ret.addObjectForFilterDeployment(key, this.getObjectsToDeploy().get(key));
 		}
 		return (ret);
 	}
 
 	/**
 	 * Get a prepared and checked instance of DatasourceFilter. The filter is
 	 * checked for sanity before it is merged with the application rule defined
 	 * in the given CRConfig.
 	 * 
 	 * @param config
 	 * @return DatasourceFilter
 	 * @throws ParserException
 	 * @throws ExpressionParserException
 	 */
 	public DatasourceFilter getPreparedFilter(CRConfig config) throws ParserException, ExpressionParserException {
 		return getPreparedFilter(config, null);
 	}
 
 	/**
 	 * Get a prepared and checked instance of DatasourceFilter. The filter is
 	 * checked for sanity before it is merged with the application rule defined
 	 * in the given CRConfig.
 	 * 
 	 * @param config
 	 * @param ds
 	 *            Datasource => Datasource does not have to be fetched from
 	 *            config => Faster
 	 * @return DatasourceFilter
 	 * @throws ParserException
 	 * @throws ExpressionParserException
 	 */
 	public DatasourceFilter getPreparedFilter(CRConfig config, Datasource ds) throws ParserException,
 			ExpressionParserException {
 		DatasourceFilter dsFilter;
 		String filter = "";
 		if (ds == null) {
 			ds = config.getDatasource();
 		}
 
 		if ((this.getRequestFilter() == null || this.getRequestFilter().equals("")) && this.getContentid() != null
 				&& !this.getContentid().equals("")) {
 			this.setRequestFilter("object.contentid=='" + this.getContentid() + "'");
 		}
 
 		// TEST IF REQUEST FILTER IS SAVE
 		Expression expression = PortalConnectorFactory.createExpression(this.getRequestFilter());
 		// IF NO EXCEPTION IS THROWN IN THE ABOVE STATEMENT, FILTER IS
 		// CONSIDERED TO BE SAVE
 
 		// ADD APPLICATION RULE IF IT IS SET
 		if (config.getApplicationRule() == null || config.getApplicationRule().equals("")) {
 			filter = this.getRequestFilter();
 		} else if (config.getApplicationRule() != null && !config.getApplicationRule().equals("")
 				&& this.getRequestFilter() != null && !this.getRequestFilter().equals("")) {
 			filter = "(" + this.getRequestFilter() + ") AND " + config.getApplicationRule();
 		} else if (config.getApplicationRule() != null && !config.getApplicationRule().equals("")
 				&& (this.getRequestFilter() == null || this.getRequestFilter().equals(""))) {
 			filter = config.getApplicationRule();
 		}
 		log.debug("Using rule: " + filter);
 		expression = PortalConnectorFactory.createExpression(filter);
 
 		dsFilter = ds.createDatasourceFilter(expression);
 		Iterator<String> it = this.getObjectsToDeploy().keySet().iterator();
 		while (it.hasNext()) {
 			String key = it.next();
 			dsFilter.addBaseResolvable(key, this.getObjectsToDeploy().get(key));
 		}
 		return (dsFilter);
 	}
 
 	/**
 	 * Sets if the RequestProcessor is to replace plinks in the configured
 	 * attributes (plink attributes).
 	 * @param doreplaceplinks true if plinks should be replaced
 	 */
 	public final void setDoReplacePlinks(final boolean doreplaceplinks) {
 		this.set("doreplaceplinks", new Boolean(doreplaceplinks));
 
 	}
 
 	/**
 	 * Gets if the RequestProcessor is to replace plinks in the configured
 	 * attributes (plink attributes).
 	 * @return boolean doreplaceplinks
 	 */
 	public final boolean getDoReplacePlinks() {
 		Boolean doR = (Boolean) this.get("doreplaceplinks");
 		if (doR == null) {
 			return (false);
 		}
 		return (doR.booleanValue());
 
 	}
 
 	/**
 	 * Sets if the RequestProcessor is to render velocity.
 	 * @param dovelocity
 	 */
 	public void setDoVelocity(boolean dovelocity) {
 		this.set("dovelocity", new Boolean(dovelocity));
 
 	}
 
 	/**
 	 * Gets if the RequestProcessor is to render velocity.
 	 * 
 	 * @return dovelocity
 	 */
 	public boolean getDoVelocity() {
 		Boolean doV = (Boolean) this.get("dovelocity");
 		if (doV == null) {
 			return (false);
 		}
 		return (doV.booleanValue());
 	}
 
 	/**
 	 * Sets the request URL. Setting the request URL makes this request an
 	 * URL-Request if a more specific key (contentid) is not set.
 	 * 
 	 * @param url
 	 */
 	public void setUrl(String url) {
 		this.set("url", url);
 
 	}
 
 	/**
 	 * Gets the configured URL and returns it as String. Returns null if URL is
 	 * not set.
 	 * 
 	 * @return URL
 	 */
 	public String getUrl() {
 		return (String) this.get("url");
 	}
 
 	/**
 	 * Sets the encoding that is used to render the responses that need a
 	 * encoding to render.
 	 * 
 	 * @param encoding
 	 */
 	public void setEncoding(String encoding) {
 		this.set("encoding", encoding);
 	}
 
 	/**
 	 * Get if the current request is an urlrequest.
 	 * @return true if this request instance is configured as URL request
 	 * (if RequestFilter parameter is not set and contentid is not set and URL is set)
 	 */
 	public boolean isUrlRequest() {
 		boolean urlrequest = false;
 		if ((this.getContentid() == null || this.getContentid().equals(""))
 				&& (this.getRequestFilter() == null || this.getRequestFilter().equals(""))
 				&& (this.getUrl() != null && !this.getUrl().equals("")))
 			urlrequest = true;
 		return urlrequest;
 	}
 
 	/**
 	 * Get configured encoding. Defaults to UTF-8.
 	 * 
 	 * @return encoding
 	 */
 	public String getEncoding() {
 		String encoding = (String) this.get("encoding");
 		if (encoding != null && !encoding.equals("")) {
 			return encoding;
 		} else {
 			return ("UTF-8");
 		}
 	}
 
 	/**
 	 * Sets the contentid which can be used to request one object per contentid
 	 * (object.contentid==x).
 	 * 
 	 * @param contentid
 	 */
 	public void setContentid(final String contentid) {
 		this.set("contentid", contentid);
 	}
 
 	/**
 	 * Gets the configured contentid and returns it as String. Returns null if
 	 * contentid is not set.
 	 * 
 	 * @return contentid
 	 */
 	public String getContentid() {
 		return (String) this.get("contentid");
 	}
 
 	/**
 	 * Returns a String that can be used as cache key and identifies this
 	 * request by requestFilter AND/OR contentid Requested attributes will not
 	 * be added to the key.
 	 * 
 	 * @return cachekey returns null if neither a contentid nor a requestfilter
 	 *         was found
 	 */
 	public String getCacheKeyFromFilter() {
 		String contentid = this.getContentid();
 		if (contentid != null) {
 			return (contentid);
 		}
 
 		String filter = this.getRequestFilter();
 		if (filter != null) {
 			return (filter);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Set custom parameter. Keep in mind that the other variables are stored in
 	 * this map as well. So x.set("contentid",contentid) is the same as
 	 * x.setContentid(contentid)
 	 * 
 	 * @param key
 	 * @param value
 	 */
 	public void set(final String key, final Object value) {
 		this.params.put(key, value);
 	}
 
 	/**
 	 * Gets a parameter using the given key.
 	 * 
 	 * @param key
 	 *            name of the parameter to get
 	 * @return value of the parameter, <code>null</code> in case parameter was
 	 *         not set before.
 	 */
 	public Object get(final String key) {
 		return params.get(key);
 	}
 
 	/**
 	 * @return hashcode of the current CRRequest.
 	 */
 	@Override
 	public int hashCode() {
 		// shamelessly copied from List#hashCode()
 		return 31 * this.params.hashCode() + this.objectsToDeploy.hashCode();
 	}
 
 	/**
 	 * @param obj
 	 * @return equals if the parameters are equal and the objectsToDeploy are also equals.
 	 */
 	@Override
 	public boolean equals(final Object obj) {
 		if (!(obj instanceof CRRequest)) {
 			return false;
 		}
 		CRRequest req = (CRRequest) obj;
 		return this.params.equals(req.params) && this.objectsToDeploy.equals(req.objectsToDeploy);
 	}
 
 }

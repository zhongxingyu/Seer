 /* Copyright (c) 2013 The University of Queensland. This software is being developed 
  * for the UQ School of History, Philosophy, Religion and Classics (HPRC).
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package au.org.paperminer.main;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.json.simple.JSONValue;
 
 import au.org.paperminer.common.CookieHelper;
 import au.org.paperminer.common.PaperMinerConstants;
 import au.org.paperminer.common.PaperMinerException;
 import au.org.paperminer.db.LocationHelper;
 
 /**
  * Retrieves geospatial data related to TROVE record identifiers.  All operations 
  * return a JSON result on success, or Failure.
  * @author Ron
  * 
  * FIXME: for some reason, this filter is NOT CHAINING to the servlet, so no error messages can be set!!!
  */
 public class LocationFilter implements Filter 
 {
     private Logger m_logger;
     private LocationHelper m_helper;
 
 	@Override
 	public void destroy() 
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void doFilter(ServletRequest req, ServletResponse resp,
 	        FilterChain filterChain) throws IOException, ServletException 
     {
         HttpServletRequest httpReq = (HttpServletRequest) req;
         String remoteReq = httpReq.getRequestURL().toString();
         int idx = remoteReq.lastIndexOf('/');
         
         m_logger.info("LocationFilter doFilter" + remoteReq.substring(idx));
         
         if (remoteReq.substring(idx).startsWith("/ref")) {
             m_logger.debug(" location filter references");
             getReferences((HttpServletRequest) req, (HttpServletResponse) resp);
         }
         else if (remoteReq.substring(idx).startsWith("/gs")) {
             m_logger.debug(" location filter GS info");
             getGSDetails((HttpServletRequest) req, (HttpServletResponse) resp);
         }
         else if (remoteReq.substring(idx).startsWith("/q")) {
             m_logger.debug(" location filter find location(s)");
             findLocation((HttpServletRequest) req, (HttpServletResponse) resp);
         }
         else if (remoteReq.substring(idx).startsWith("/rm")) {
             m_logger.debug(" location filter strikeout");
             strikeout((HttpServletRequest) req, (HttpServletResponse) resp);
         }
         else if (remoteReq.substring(idx).startsWith("/add")) {
             m_logger.debug(" location filter add GS record");
             add((HttpServletRequest) req, (HttpServletResponse) resp);
         }
         else if (remoteReq.substring(idx).startsWith("/ins")) {
             m_logger.debug(" location filter insert location and add GS record");
             insert((HttpServletRequest) req, (HttpServletResponse) resp);
         }
         
         m_logger.debug("LocationFilter chaining... ?");
         filterChain.doFilter(req, resp);
         m_logger.debug("LocationFilter chaining complete");
        //return;
 	}
 
 	@Override
 	public void init(FilterConfig arg0) throws ServletException 
 	{
         m_logger = Logger.getLogger(PaperMinerConstants.LOGGER);
         m_logger.info("LocationFilter init");
         m_helper = new LocationHelper();
 	}
 	
 	/**
 	 * Returns JSON struct for a given set of TROVE ids:
 	 *  refs: [{troveId:[locnId,freq]*]*
 	 * or blank if none.
 	 * @param req
 	 * @param resp
 	 */
 	private void getReferences (HttpServletRequest req, HttpServletResponse resp)
 	{
		//TODO: check here for problem
 		HashMap<String, ArrayList<ArrayList<String>>> map = new HashMap<String, ArrayList<ArrayList<String>>>();
         try {
             String arg = req.getParameter("lst");
             if ((arg != null) && (arg.length() > 0)) {
             	String [] refs = arg.split(",");
                 m_logger.debug("locationFilter getReferences: " + arg + " length:" + refs.length);
             	for (int i = 0; i < refs.length; i++) {
             		ArrayList<ArrayList<String>> tmp = m_helper.getLocationsForRef(refs[i]);
             		if (tmp != null) {
             			map.put(refs[i], tmp);
                         m_logger.debug("locationFilter ref: " + refs[i] + " is " + tmp);
             		}
             	}
             }
             resp.setContentType("text/json");
             PrintWriter pm = resp.getWriter();
     		String jsonStr = "";
     		if (map.size() > 0) {
     			jsonStr = "{\"refs\":" + JSONValue.toJSONString(map) + "}";
     		}
             pm.write(jsonStr);
             pm.close();
             m_logger.debug("locationFilter getReferences JSON: " + jsonStr);
 
         }
         catch (PaperMinerException ex) {
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e301");
         }
     	catch (IOException ex) {
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e114");
     	}
 	}
 
 	/**
 	 * Returns JSON struct for a specific location id, or blank.
 	 * @param req
 	 * @param resp
 	 */
 	private void getGSDetails (HttpServletRequest req, HttpServletResponse resp)
 	{
 		HashMap<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>();
         try {
             String arg = req.getParameter("lst");
             if ((arg != null) && (arg.length() > 0)) {
             	String [] ids = arg.split(",");
                 m_logger.debug("locationFilter getGSDetails: " + arg + " length:" + ids.length);
             	for (int i = 0; i < ids.length; i++) {
             		HashMap<String, String> tmp = m_helper.getLocationInfo(ids[i]);
             		map.put(ids[i], tmp);
                     m_logger.debug("  getGSDetails fetched: " + tmp.get("name") + " (" + ids[i] + ")");
             	}
             }
             resp.setContentType("text/json");
             PrintWriter pm = resp.getWriter();
     		String jsonStr = "";
     		if (map.size() > 0) {
     			jsonStr = "{\"locns\":" + JSONValue.toJSONString(map) + "}";
     		}
             pm.write(jsonStr);
             pm.close();
         }
         catch (PaperMinerException ex) {
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e302");
         }
     	catch (IOException ex) {
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e114");
     	}
 	}
 	
 	/**
 	 * Deletes a reference to a location by marking it "struckout" (no actual delete from DB)
 	 * @param req
 	 * @param resp
 	 */
 	private void strikeout (HttpServletRequest req, HttpServletResponse resp)
 	{
 		String locnList = req.getParameter("lid");
 		String troveId = req.getParameter("tid");
 		String userId = req.getParameter("uid");
 		try {
 			m_logger.debug("strikeout userId=" + userId);
 
 			int cnt = m_helper.strikeout(userId, locnList, troveId);
 			String [] tmp = locnList.split(",");
 			if (tmp.length != cnt) {
 	            req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e303");
 			}
 	        m_logger.debug("locationFilter locn strikeout: " + troveId + '/' + locnList + " Deleted: " + cnt);
             resp.setContentType("text/json");
             PrintWriter pm = resp.getWriter();
             pm.write("{\"rm\":\"" + cnt + "\"}");
             pm.close();
 		}
 		catch (PaperMinerException ex) {
 			m_logger.error("Strikeout failed", ex);
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e304");
 		}
     	catch (IOException ex) {
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e114");
     	}
 	}
 	
 	/**
 	 * Locate an existing location by its name, and optionally, state (short form) and/or country (long form).
 	 * @param req
 	 * @param resp
 	 */
 	private void findLocation (HttpServletRequest req, HttpServletResponse resp)
 	{
 		String locnName = req.getParameter("ln");
 		String stateName = req.getParameter("sn");
 		String cntryName = req.getParameter("cn");
 		try {
 			ArrayList<HashMap<String, String>> list = m_helper.locationsLike(locnName, stateName, cntryName);
 	        m_logger.debug("locationFilter locn lookup: " + locnName + " (" + stateName + ", " + cntryName + "): " + list.size());
 	        
             resp.setContentType("text/json");
             PrintWriter pm = resp.getWriter();
             pm.write(JSONValue.toJSONString(list));
             pm.close();
 		}
 		catch (PaperMinerException ex) {
 			m_logger.error("lookup failed", ex);
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e305");
 		}
     	catch (IOException ex) {
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e114");
     	}
 	}
 	
 	/**
 	 * Inserts a new reference to a new location (also inserted, though we double check it is not already known)
 	 * @param req
 	 * @param resp
 	 */
 	private void insert (HttpServletRequest req, HttpServletResponse resp)
 	{
 		String userId = req.getParameter("uid");
 		String troveId = req.getParameter("tid");
 		String freq = req.getParameter("freq");
 		String name = req.getParameter("nm");
 		String sln = req.getParameter("sln");
 		String ssn = req.getParameter("ssn");
 		String cln = req.getParameter("cln");
 		String csn = req.getParameter("csn");
 		String lat = req.getParameter("lat");
 		String lng = req.getParameter("lng");
 		String nwlat = req.getParameter("nwlat");
 		String nwlng = req.getParameter("nwlng");
 		String selat = req.getParameter("selat");
 		String selng = req.getParameter("selng");
 		m_logger.debug(name+','+ssn+','+sln+','+csn+','+cln+','+lat+','+lng+','+troveId);
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("name", name);
 		map.put("state_sn", ssn);
 		map.put("state_ln", sln);
 		map.put("cntry_sn", csn);
 		map.put("cntry_ln", cln);
 		map.put("lat", lat);
 		map.put("lng", lng);
 		map.put("nw_lat", nwlat);
 		map.put("nw_lng", nwlng);
 		map.put("se_lat", selat);
 		map.put("se_lng", selng);
 		try {
 			int locnId = m_helper.addLocation(userId, troveId, freq, map);
 	        m_logger.debug("locationFilter locn lookup: " );
 	        
             resp.setContentType("text/json");
             PrintWriter pm = resp.getWriter();
             pm.write("{ \"lid\":\"" + locnId + "\" }");
             pm.close();
 		}
 		catch (PaperMinerException ex) {
 			m_logger.error("insert location/GS records failed", ex);
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e306");
 		}
     	catch (IOException ex) {
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e114");
     	}
 	}
 	
 	/**
 	 * Adds a reference to an existing location
 	 * @param req
 	 * @param resp
 	 */
 	private void add (HttpServletRequest req, HttpServletResponse resp)
 	{
 		String userId = req.getParameter("uid");
 		String troveId = req.getParameter("tid");
 		String locnId = req.getParameter("lid");
 		String freq = req.getParameter("freq");
 		try {
 			boolean res = m_helper.addReference(userId, troveId, locnId, freq, "A");
 	        m_logger.debug("locationFilter add location " + troveId + '/' + locnId + '/' + freq + " Insert " + (res ? "ok" : "failed"));
 	        if (! res) {
 	            req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e307");
 	        }
 	        
             resp.setContentType("text/json");
             PrintWriter pw = resp.getWriter();
             pw.write("{ \"res\":" + (res ? "\"ok\"" : "\"failed\"") + " }");
             pw.close();
 		}
 		catch (PaperMinerException ex) {
 			m_logger.error("add GS record failed", ex);
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e307");
 		}
     	catch (IOException ex) {
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e114");
     	}
 	}
 	
 }
 
 // EOF

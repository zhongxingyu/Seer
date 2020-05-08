 package org.sakaiproject.hierarchy.impl;
 
 import java.util.Collection;
 
 import org.sakaiproject.hierarchy.api.HierarchyService;
 import org.sakaiproject.hierarchy.api.HierarchyServiceException;
 import org.sakaiproject.hierarchy.api.PortalHierarchyService;
 import org.sakaiproject.hierarchy.api.model.Hierarchy;
 import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.api.SessionManager;
 
 public class PortalHierarchyServiceImpl implements PortalHierarchyService {
 
 	private SessionManager sessionManager = null;
 	private HierarchyService hierarchyService = null;
 
 	
 	public Hierarchy getCurrentPortalNode()
 	{
 		return getNode(getCurrentPortalPath());
 	}
 
 	public String getCurrentPortalPath()
 	{
 		Session session = sessionManager.getCurrentSession();
 		return (String) session.getAttribute("portal-hierarchy-path");
 	}
 
 	public SessionManager getSessionManager() {
 		return sessionManager;
 	}
 
 	public void setSessionManager(SessionManager sessionManager) {
 		this.sessionManager = sessionManager;
 	}
 
 	public void setCurrentPortalPath(String portalPath)
 	{
 		Session session = sessionManager.getCurrentSession();
 		session.setAttribute("portal-hierarchy-path", portalPath);
 	}
 
 	public HierarchyService getHierarchyService() {
 		return hierarchyService;
 	}
 
 	public void setHierarchyService(HierarchyService hierarchyService) {
 		this.hierarchyService = hierarchyService;
 	}
 
 	public void abort() {
 		hierarchyService.abort();
 	}
 
 	public void begin() {
 		hierarchyService.begin();
 	}
 
 	public void deleteNode(Hierarchy node) {
 		hierarchyService.deleteNode(node);
 	}
 
 	public void end() {
 		hierarchyService.end();
 	}
 
 	public Hierarchy getNode(String nodePath) {
 		return hierarchyService.getNode(nodePath);
 	}
 
 	public Collection getRootNodes() {
 		return hierarchyService.getRootNodes();
 	}
 
 	public HierarchyProperty newHierachyProperty() {
 		return hierarchyService.newHierachyProperty();
 	}
 
 	public Hierarchy newHierarchy(String nodePath) throws HierarchyServiceException {
 		return hierarchyService.newHierarchy(nodePath);
 	}
 
 	public void save(Hierarchy node) {
 		hierarchyService.save(node);
 	}
 
 }

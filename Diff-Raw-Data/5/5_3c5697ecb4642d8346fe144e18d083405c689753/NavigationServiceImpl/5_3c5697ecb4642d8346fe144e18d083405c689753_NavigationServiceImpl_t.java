 package org.otherobjects.cms.site;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.jcr.UniversalJcrDao;
 import org.otherobjects.cms.model.BaseNode;
 import org.springframework.util.Assert;
 
 import com.ibm.icu.util.Calendar;
 
 /**
  * TODO Sort order TODO Dealing with default pages (don't duplicate with folder)
  * TODO Allow injection of other paths eg for non-jcr pages TODO Live/edit trees
  * TODO Update tree on relevant events
  * 
  * @author rich
  * 
  */
 @SuppressWarnings("unchecked")
 public class NavigationServiceImpl implements NavigationService
 {
     @Resource
     private DaoService daoService;
 
     private long lastBuildTime = 0;
 
     protected TreeNode tree;
     protected List<TreeNode> nodes;
     protected boolean testMode;
 
     public NavigationServiceImpl()
     {
     }
 
     public TreeNode getNavigation(String path, int startDepth, int endDepth)
     {
         return getNavigation(path, startDepth, endDepth, null);
     }
 
     public TreeNode getNavigation(String path, int startDepth, int endDepth, String currentPath)
     {
         try
         {
             Assert.isTrue(startDepth >= 0, "Navigation start depth must be >= 0");
             Assert.isTrue(endDepth > startDepth, "Navigation end depth must be > start depth");
 
             buildTree();
 
             // Start at correct depth and location by trimming path to correct
             // depth
             path = trimPath(path, startDepth);
             TreeNode startNode = this.tree.getNode(path);
 
             // Clone tree but only to required depth
             if (startNode == null)
                 throw new OtherObjectsException("Could not create navigation. Path does not exist: " + path);
 
             TreeNode clone = startNode.clone(endDepth - startDepth);
 
             // Mark selected nodes
             if (currentPath != null)
             {
                 markSelected(clone, currentPath);
             }
 
             return clone;
         }
         catch (Exception e)
         {
             throw new OtherObjectsException("Could not create navigation tree.", e);
         }
     }
 
     public List<TreeNode> getTrail(String path, int startDepth, boolean foldersOnly)
     {
         try
         {
             Assert.isTrue(startDepth >= 0, "Navigation start depth must be >= 0");
 
             buildTree();
 
             List<TreeNode> parents = new ArrayList<TreeNode>();
             int pos = 0;
             int depth = 0;
             while (pos < path.length())
             {
                 pos = path.indexOf("/", pos) + 1;
                 if (pos == 0)
                 {
                     if (foldersOnly)
                     {
                         break;
                     }
                     else
                     {
                         pos = path.length();
                     }
                 }
                 String p = path.substring(0, pos);
                 TreeNode node = this.tree.getNode(p);
                 if (node != null && depth++ >= startDepth)
                 {
                     parents.add(node.clone(0));
                 }
             }
             return parents;
         }
         catch (CloneNotSupportedException e)
         {
             throw new OtherObjectsException("Could not create nagivation trail.", e);
         }
     }
 
     /**
      * Finds nodes in tree which match the current path and flags them as
      * selected.
      * 
      * @param clone
      * @param currentPath
      */
     protected void markSelected(TreeNode node, String path)
     {
         int pos = 0;
         while (pos < path.length())
         {
             pos = path.indexOf("/", pos) + 1;
             if (pos == 0)
             {
                 pos = path.length();
             }
             String p = path.substring(0, pos);
             TreeNode n = node.getNode(p);
             if (n != null)
             {
                 n.setSelected(true);
             }
         }
     }
 
     /**
      * Trims a path to the correct depth. For example: / has a depth of 0,
      * /about/ has a depth of 1.
      * 
      * @param path
      * @param startDepth
      * @return
      */
     protected String trimPath(String path, int startDepth)
     {
         int pos = 0;
         int depth = 0;
         while (pos < path.length())
         {
             pos = path.indexOf("/", pos) + 1;
             if (++depth > startDepth)
             {
                 break;
             }
         }
         return path.substring(0, pos);
     }
 
     /**
      * Builds a tree representation of the navigational structure of the site.
      * This is an expensive operation and should only happen when tree structure
      * changes.
      * 
      * FIXME Need to synchronise this
      */
     private synchronized void buildTree()
     {
         if (testMode)
             return;
 
         // FIXME Temp hack
         // if (this.tree == null)
         long now = Calendar.getInstance().getTimeInMillis();
         if (Calendar.getInstance().getTimeInMillis() - lastBuildTime < 5 * 60 * 10) // Cache
         // for
         // 2
         // minutes
         {
             return;
         }
         lastBuildTime = now;
 
         TreeBuilder tb = new TreeBuilder();
 
         List<BaseNode> siteNodes = getSiteNodes();
 
         List<TreeNode> flat = new ArrayList<TreeNode>();
         int count = 10000;
         for (BaseNode b : siteNodes)
         {
             String label = b.getOoLabel();
             if (b.hasProperty("publishingOptions.navigationLabel") && b.getPropertyValue("publishingOptions.navigationLabel") != null)
                 label = (String) b.getPropertyValue("publishingOptions.navigationLabel");
             if (b.hasProperty("data.publishingOptions.navigationLabel") && b.getPropertyValue("data.publishingOptions.navigationLabel") != null)
                 label = (String) b.getPropertyValue("data.publishingOptions.navigationLabel");
 
             int sortOrder = count++;
             if (b.hasProperty("publishingOptions.sortOrder"))
             {
                 Long so = (Long) b.getPropertyValue("publishingOptions.sortOrder");
                 if (so != null)
                     sortOrder = so.intValue();
             }
             if (b.hasProperty("data.publishingOptions.sortOrder"))
             {
                 Long so = (Long) b.getPropertyValue("data.publishingOptions.sortOrder");
                 if (so != null)
                     sortOrder = so.intValue();
             }
             flat.add(new TreeNode(b.getOoUrlPath(), b.getId(), label, sortOrder));
         }
 
         appendAdditionalNodes(flat);
 
         this.tree = tb.buildTree(flat, new TreeNode("/", null, "Home", 0));
         this.nodes = flat;
     }
 
     public List<TreeNode> getAllNodes()
     {
         buildTree();
         return this.nodes;
     }
 
     public TreeNode getNode(String path, String currentPath)
     {
         try
         {
             buildTree();
 
             // Mark selected nodes
             TreeNode node = this.tree.getNode(path);
 
             node = node.clone(1);
 
             if (currentPath != null)
             {
                 markSelected(node, currentPath);
             }
 
             return node;
         }
        catch (Exception e)
         {
            throw new OtherObjectsException("Could not create nagivation trail for path: " + path, e);
         }
     }
 
     /**
      * Overide this method to add site-specific nodes to the navigation tree.
      * 
      * @param flat
      */
     protected void appendAdditionalNodes(List<TreeNode> nodes)
     {
     }
 
     /**
      * Returns all folders and all items marked as showInNavigation.
      * 
      * @return
      */
     private List getSiteNodes()
     {
         // FIXME Need folder indicator
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         return universalJcrDao.getAllByJcrExpression("/jcr:root/site//element(*) [(jcr:like(@ooType,'%Folder') and @inMenu='true') or publishingOptions/@showInNavigation='true']");
     }
 
     public void setDaoService(DaoService daoService)
     {
         this.daoService = daoService;
     }
 }

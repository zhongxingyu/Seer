 package com.gentics.cr.nav;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.template.ITemplate;
 import com.gentics.cr.template.ITemplateManager;
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class NavObject{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2977348089081661754L;
 	
 	private CRResolvableBean bean;
 	private int level=0;
 	private Vector<String> path;
 	private CRConfig conf;
 	private ITemplate template;
 	private Map<String,Object> objects;
 
 	/**
 	 * Create new instance of NavObject
 	 * @param conf
 	 * @param bean
 	 * @param level
 	 * @param path
 	 * @param template
 	 * @param objects 
 	 * @param resolvables
 	 */
 	public NavObject(CRConfig conf, CRResolvableBean bean, int level, Vector<String> path, ITemplate template, Map<String,Object> objects)
 	{
 		this.bean = bean;
 		this.path = path;
		if (this.path == null && bean != null) {
			this.path = new Vector<String>();
			this.path.add(bean.getContentid());
		}
 		this.conf = conf;
 		this.level = level;
 		this.template = template;
 		this.objects = objects;
 	}
 	
 	/**
 	 * gets the current object
 	 * @return object as CRResolvableBean
 	 */
 	public CRResolvableBean getObject()
 	{
 		return(this.bean);
 	}
 	
 	/**
 	 * gets level of current object (starts with level 0)
 	 * @return level as int
 	 */
 	public int getLevel()
 	{
 		return(this.level);
 	}
 	
 	
 	/**
 	 * returns true if the given contentid is in the path (root object of this tree and current object)
 	 * @param contentid 
 	 * @param bean
 	 * @return true if in path
 	 */
 	public boolean isInPath(String contentid)
 	{
 		return(this.path.contains(contentid));
 	}
 	
 	/**
 	 * returns true if the given CRResolvableBean is in the path (root object of this tree and current object)
 	 * @param bean
 	 * @return true if in path
 	 */
 	public boolean isInPath(CRResolvableBean bean)
 	{
 		return(this.isInPath(bean.getContentid()));
 	}
 	
 	/**
 	 * renders the current object with the given template
 	 * 
 	 * the current object can be accessed in the template using "nav" as key
 	 * @return rendered object (can include subtree if accessed in template)
 	 * @throws CRException
 	 */
 	public String render() throws CRException
 	{
 		// Initialize Velocity Context
 		ITemplateManager myTemplateManager = this.conf.getTemplateManager();
 
 		// enrich template context
 		if (objects != null) {
 			for (Iterator<Map.Entry<String, Object>> it = objects
 					.entrySet().iterator(); it.hasNext();) {
 				Map.Entry<String, Object> entry = it.next();
 				myTemplateManager.put(entry.getKey(), entry.getValue());
 			}
 		}
 		myTemplateManager.put("nav", this);
 		return( myTemplateManager.render(this.template.getKey(), this.template.getSource()));
 	}
 	
 	/**
 	 * gets the rendered subtree as string
 	 * @return subtree
 	 */
 	@SuppressWarnings("unchecked")
 	public String getSubTree()
 	{
 		String ret="";
 		if(this.path==null)this.path = new Vector<String>();
 		Vector<String> p = (Vector<String>) this.path.clone();
 		p.add(this.bean.getContentid());
 		for (CRResolvableBean child:this.bean.getChildRepository()) {
 			NavObject no = new NavObject(conf, child, level+1, p,this.template, objects);
 			try {
 				ret += no.render();
 			} catch (CRException e) {
 				e.printStackTrace();
 			}
 		}
 		return (ret);
 	}
 	
 	/**
 	 * alias for getSubTree
 	 * @return subtree as string
 	 */
 	public String getSubtree()
 	{
 		return(this.getSubTree());
 	}
 }

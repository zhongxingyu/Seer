 /**
  * 
  */
 package uk.bl.wap.memento;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 
 import dev.memento.Memento;
 import dev.memento.MementoClient;
 import dev.memento.MementoList;
 
 /**
  * @author Andrew Jackson <Andrew.Jackson@bl.uk>
  *
  */
 @ManagedBean
 @ViewScoped
 public class MementoSearchBean implements Serializable {
 	
 	private static final long serialVersionUID = -8531283933701096508L;
 	
 	private String url = "http://";
 
 	private ArrayList<MementoBean> mementos;
 	
 	private HashMap<String,Integer> hostCount;
 
 	private ArrayList<String> hosts;
 
 	private int totalCount;
 
 	private MementoList mementoList;
 	
 	/**
 	 * @return the url
 	 */
 	public String getUrl() {
 		return url;
 	}
 
 	/**
 	 * @param url the url to set
 	 */
 	public void setUrl(String url) {
 		this.url = url;
 		this.doSearch();
 	}
 	
 	public boolean isUrlSet() {
 		if( this.url == null ) return false;
 		if( "".equals(this.url)) return false;
 		if( "http://".equals(this.url)) return false;
 		return true;
 	}
 	
 	/**
 	 * @return
 	 */
 	public ArrayList<MementoBean> getMementos() {
 		return this.mementos;
 	}
 	
 	/**
 	 * @return
 	 */
 	public List<String> getHosts() {
 		return this.hosts;
 	}
 	
 	/**
 	 * @return
 	 */
 	public HashMap<String,Integer> getHostCounts() {
 		return this.hostCount;
 	}
 	
 	/**
 	 * @return
 	 */
 	public int getTotalCount() {
 		return this.totalCount;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getNumHosts() {
 		if( this.getHosts() == null ) return 0;
 		return this.getHosts().size();
 	}
 	
 	/**
 	 * @return
 	 */
 	public MementoBean getFirstMemento() {
 		if( this.mementoList != null ) {
 			return new MementoBean( this.mementoList.getFirst() );
 		}
 		return null;
 	}
 
 	/**
 	 * @return
 	 */
 	public MementoBean getLastMemento() {
 		if( this.mementoList != null ) {
 			return new MementoBean( this.mementoList.getLast() );
 		}
 		return null;
 	}
 
 	/**
 	 */
 	public void doSearch() {
 		System.out.println("Doing search...");
 		// Query:
     	MementoClient mc = new MementoClient();
     	mc.setTargetURI(this.getUrl());
     	// Get results:
     	this.mementos = new ArrayList<MementoBean>();
     	this.hostCount = new HashMap<String,Integer>();
     	this.totalCount = 0;
     	if( mc.getMementos() != null ) {
     		//mc.getMementos().displayAll();
     		this.mementoList = mc.getMementos();
     		for( Memento m : this.mementoList ) {
     			MementoBean mb = new MementoBean(m);
     			this.mementos.add( mb );
     			// Count archival copies:
     			String host = mb.getArchiveHost();
     			if( host != null ) {
     				int count = hostCount.containsKey(host) ? hostCount.get(host) : 0;
     				hostCount.put(host, count + 1);
     			}
     			this.totalCount++;
     		}
     	}
     	this.hosts = new ArrayList<String>(hostCount.keySet());
     }
 	
 	/**
 	 * @return
 	 */
 	public String submit() {
 		System.out.println("Got: "+this.getUrl());
 		// This should work, but doesn't work for me.
 		//return "success?faces-redirect=true&IncludeViewParams=true";
 		// This works fine.
 		return "/search.xhtml?faces-redirect=true&url=" + this.getUrl();
 	}
 	
 	/**
 	 * @return
 	 */
 	public String getAbsoluteUrl() {
 		final ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
 		String url = ectx.getRequestScheme()
 		  + "://" + ectx.getRequestServerName()
 		  + ":" + ectx.getRequestServerPort()
 		  + "" + ectx.getRequestContextPath();
 		return url;
 	}
 }

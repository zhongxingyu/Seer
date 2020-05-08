 package com.whysearchtwice.frames;
 
 import com.tinkerpop.blueprints.Direction;
 import com.tinkerpop.frames.Adjacency;
 import com.tinkerpop.frames.Property;
 import com.tinkerpop.frames.VertexFrame;
 
 public interface PageView extends VertexFrame {
     @Property("type")
     public void setType(String type);
 
     @Property("type")
     public String getType();
 
     @Property("pageUrl")
     public void setPageUrl(String pageUrl);
 
     @Property("pageUrl")
     public String getPageUrl();
 
     @Property("pageOpenTime")
     public void setPageOpenTime(Long pageOpenTime);
 
     @Property("pageOpenTime")
     public Long getPageOpenTime();
 
     @Property("pageCloseTime")
     public void setPageCloseTime(Long pageOpenTime);
 
     @Property("pageCloseTime")
     public Long getPageCloseTime();
 
     @Property("tabId")
     public void setTabId(int tabId);
 
     @Property("tabId")
     public int getTabId();
 
     @Property("windowId")
     public void setWindowId(int windowId);
 
     @Property("windowId")
     public int getWindowId();
 
    @Adjacency(label = "viewed", direction = Direction.IN)
     public Iterable<Device> getDevice();
 
    @Adjacency(label = "viewed", direction = Direction.IN)
     public void setDevice(Device device);
 
     @Adjacency(label = "under")
     public Iterable<Domain> getDomain();
 
     @Adjacency(label = "under")
     public void setDomain(Domain domain);
 
     @Adjacency(label = "successorTo")
     public Iterable<PageView> getPredecessors();
 
     @Adjacency(label = "successorTo")
     public void addPredecessor(PageView pageview);
     
     @Adjacency(label = "successorTo", direction = Direction.IN)
     public Iterable<PageView> getSuccessors();
 
     @Adjacency(label = "successorTo", direction = Direction.IN)
     public void addSuccessor(PageView pageview);
 
     @Adjacency(label = "childOf")
     public Iterable<PageView> getParents();
 
     @Adjacency(label = "childOf")
     public void addParent(PageView pageview);
     
     @Adjacency(label = "childOf", direction = Direction.IN)
     public Iterable<PageView> getChildren();
 
     @Adjacency(label = "childOf", direction = Direction.IN)
     public void addChild(PageView pageview);
 }

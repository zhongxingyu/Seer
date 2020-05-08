 package org.icefaces.samples.showcase.example.ace.gMap;
 
 import javax.el.MethodExpression;
 import javax.faces.application.*;
 import javax.faces.bean.SessionScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.component.*;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import java.util.ArrayList;
 import javax.faces.bean.CustomScoped;
 import javax.annotation.PostConstruct;
 import org.icefaces.samples.showcase.metadata.annotation.*;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 import java.io.Serializable;
 
 @ComponentExample(
         parent = MapBean.BEAN_NAME,
         title = "example.ace.gMap.marker.title",
         description = "example.ace.gMap.marker.description",
         example = "/resources/examples/ace/gMap/gMapMarker.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="gMapMarker.xhtml",
                     resource = "/resources/examples/ace/gMap/gMapMarker.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="MapMarkerBean.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/showcase/example/ace/gMap/MapMarkerBean.java")
         }
 )
 @ManagedBean(name= MapMarkerBean.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class MapMarkerBean extends ComponentExampleImpl<MapMarkerBean> implements Serializable {
 	public static final String BEAN_NAME = "markerBean";
     private Double[] latList = {0.0,7.5,-10.0};
     private Double[] longList = {0.0,7.5,-10.0};
    private String[] optionsList = {"title:'Hover mouse over this marker to see title'","","draggable:true"};
 
 	public MapMarkerBean() {
         super(MapMarkerBean.class);
     }
 	
     public Double[] getLatList() {
         return latList;
     }
 
     public void setLatList(Double[] latList) {
         this.latList = latList;
     }
 
     public Double[] getLongList() {
         return longList;
     }
 
     public void setLongList(Double[] longList) {
         this.longList = longList;
     }
 
     public String[] getOptionsList() {
         return optionsList;
     }
 
     public void setOptionsList(String[] optionsList) {
         this.optionsList = optionsList;
     }
 	@PostConstruct
     public void initMetaData() {
         super.initMetaData();
     }
 }

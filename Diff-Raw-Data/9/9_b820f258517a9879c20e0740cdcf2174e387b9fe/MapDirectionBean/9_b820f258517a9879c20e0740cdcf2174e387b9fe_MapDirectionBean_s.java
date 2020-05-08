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
         title = "example.ace.gMap.direction.title",
         description = "example.ace.gMap.direction.description",
         example = "/resources/examples/ace/gMap/gMapDirections.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="gMapDirections.xhtml",
                     resource = "/resources/examples/ace/gMap/gMapDirections.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="MapDirectionBean.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/showcase/example/ace/gMap/MapDirectionBean.java")
         }
 )
 @ManagedBean(name= MapDirectionBean.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class MapDirectionBean extends ComponentExampleImpl<MapDirectionBean> implements Serializable{
 	public static final String BEAN_NAME = "directionBean";
 
     private String points="Regent's Park, London: Kingston upon Thames";
    private String options="";
     private String travelMode="DRIVING";
 
 	public MapDirectionBean() {
         super(MapDirectionBean.class);
     }
 	
     public String getPoints() {
         return points;
     }
 
     public void setPoints(String points) {
         this.points = points;
     }
 
     public String getOptions() {
         return options;
     }
 
     public void setOptions(String options) {
         this.options = options;
     }
 
     public String getTravelMode() {
         return travelMode;
     }
 
     public void setTravelMode(String travelMode) {
         this.travelMode = travelMode;
     }
 	@PostConstruct
     public void initMetaData() {
         super.initMetaData();
     }
 }

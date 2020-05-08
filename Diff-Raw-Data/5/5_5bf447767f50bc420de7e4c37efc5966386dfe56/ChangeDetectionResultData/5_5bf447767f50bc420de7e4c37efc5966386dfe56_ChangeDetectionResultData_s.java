 package com.parworks.androidlibrary.response.photochangedetection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.parworks.androidlibrary.ar.AugmentedData;
 import com.parworks.androidlibrary.ar.Overlay;
 import com.parworks.androidlibrary.ar.OverlayImpl;
 import com.parworks.androidlibrary.ar.Vertex;
 import com.parworks.androidlibrary.response.ImageOverlayInfo;
 import com.parworks.androidlibrary.response.OverlayBoundary;
 import com.parworks.androidlibrary.response.OverlayConfiguration;
 import com.parworks.androidlibrary.response.OverlayContent;
 import com.parworks.androidlibrary.response.OverlayCover;
 import com.parworks.androidlibrary.response.OverlayContent.OverlaySize;
 
 public class ChangeDetectionResultData {
 	private String imageId;
 	private String imageWidth;
 	private String imageHeight;
 	private List<ChangeDetectionObject> objects;
 	
 	public ChangeDetectionResultData() {}
 	public String getImageId() {
 		return imageId;
 	}
 	public void setImageId(String imageId) {
 		this.imageId = imageId;
 	}
 	public String getImageWidth() {
 		return imageWidth;
 	}
 	public void setImageWidth(String imageWidth) {
 		this.imageWidth = imageWidth;
 	}
 	public String getImageHeight() {
 		return imageHeight;
 	}
 	public void setImageHeight(String imageHeight) {
 		this.imageHeight = imageHeight;
 	}
 	public List<ChangeDetectionObject> getObjects() {
 		return objects;
 	}
 	public void setObjects(List<ChangeDetectionObject> objects) {
 		this.objects = objects;
 	}
 	
 	public String toString() {
 		String returnString = "imageId : " + imageId
 			+ "\n imageWidth : " + imageWidth
 			+ "\n imageHeight : " + imageHeight
 			+ "\n objects : [";
 		for(ChangeDetectionObject obj : objects) {
 			returnString += "\n { \n";
 			returnString += obj.toString();
 			returnString += "\n },";
 		}
 		returnString += "]";
 		return returnString;
 	}
 	
 	public AugmentedData getAugmentedData() {
 		AugmentedData data = new AugmentedData();
 		data.setOverlays(getOverlays());
 		data.setLocalization(true);
 		return data;
 	}
 	
 	private List<Overlay> getOverlays() {
 		List<Overlay> overlays = new ArrayList<Overlay>();
 		for(ChangeDetectionObject object : objects) {
 			for(ChangeDetectionInstance instance : object.getInstances()) {
 				String description = createDescription(instance);
 				String name = getName(object,instance);
 				overlays.add(createOverlay(instance.getVertices(),imageId,name,description));
 			}
 		}
 		
 		return overlays;
 	}
 	private Overlay createOverlay(List<Vertex> vertices, String baseImageId,
 			String name, String description) {
 		
 		Overlay overlay = new OverlayImpl(baseImageId,name,description,vertices);
 		return overlay;
 	}
 	private String getName(ChangeDetectionObject object,
 			ChangeDetectionInstance instance) {
		String name = object.getObjectId() + " : " + instance.getComment();
 		return name;
 	}
 	private String createDescription(ChangeDetectionInstance instance) {
 		
 			//create overlay boundary
 		OverlayBoundary boundary = createOverlayBoundary(instance.isCorrect());
 			
 			//create overlay cover
 		OverlayCover cover = createOverlayCover();
 			
 			//create overlay content
 		OverlayContent content = createOverlayContent();
 		
 		//create overylay config, set variables
 		OverlayConfiguration configuration = new OverlayConfiguration();
 		configuration.setTitle(instance.getComment());
 		configuration.setBoundary(boundary);
 		configuration.setCover(cover);
 		configuration.setContent(content);
 		
 		//serialize it
 		String json = configuration.toJson();
 		
 		//return the string
 		return json;
 	}
 	private OverlayContent createOverlayContent() {
 		OverlayContent content = new OverlayContent();
 		content.setSize("LARGE");
 		
 		return content;
 	}
 	private OverlayCover createOverlayCover() {
 		OverlayCover cover = new OverlayCover();
 		cover.setTransparency(0);
 
 		return cover;
 	}
 	private OverlayBoundary createOverlayBoundary(boolean isCorrect) {
 		OverlayBoundary boundary = new OverlayBoundary();
 		boundary.setType("SOLID");
 		if(isCorrect) {
 			boundary.setColor("green");
 		} else {
 			boundary.setColor("red");
 		}
 		return boundary;
 	}
 
 }

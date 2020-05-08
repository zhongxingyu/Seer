 package beans;
 
 
 import java.util.ArrayList;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 import org.primefaces.model.map.DefaultMapModel;
 import org.primefaces.model.map.LatLng;
 import org.primefaces.model.map.MapModel;
 import org.primefaces.model.map.Marker;
 
 import util.Map;
 
 
 @ManagedBean
 @SessionScoped
 public class MapBean {  
 
 
 	private String address;
 	private MapModel simpleModel;
 	private int width;
 	private int height;
 	private String style;
 	private int zoom;
 	private LatLng coord;
 	private double lat;
 	private double lng;
 	private LatLng center;
 	private ArrayList<Marker> markerList;
 
 
 	public MapBean(){
 		simpleModel = new DefaultMapModel();  
 		center = new LatLng(41.381542, 2.122893);
 		markerList=new ArrayList<Marker>();
 		width = 600;
 		height = 400;
 		style=getStyle();
		System.out.println(style);
 		zoom = 15;
 
 	}
 	public MapModel getSimpleModel() {  
 		return simpleModel;  
 	}
 
 	public int getWidth() {
 		return width;
 	}
 	public void setWidth(int width) {
 		this.width = width;
 	}
 	public int getHeight() {
 		return height;
 	}
 	public void setHeight(int height) {
 		this.height = height;
 	}
 	
 	public String getStyle() {
 		return "width:"+width+"px;height:"+height+"px";
 	}
 	public void setStyle(String style) {
 		this.style = style;
 	}
 	public int getZoom() {
 		return zoom;
 	}
 	public void setZoom(int zoom) {
 		this.zoom = zoom;
 	}
 	public LatLng getCoord() {
 		return coord;
 	}
 	public void setCoord(LatLng coord) {
 		this.coord = coord;
 	}
 	public double getLat() {
 		return lat;
 	}
 	public void setLat(double lat) {
 		this.lat = lat;
 	}
 	public double getLng() {
 		return lng;
 	}
 	public void setLng(double lng) {
 		this.lng = lng;
 	}
 	public void setSimpleModel(MapModel simpleModel) {
 		this.simpleModel = simpleModel;
 	}  
 	public LatLng getCenter() {
 		return center;
 	}
 	public void setCenter(LatLng center) {
 		this.center = center;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	public void addMarqueur(){
 		System.out.println(coord);
 		System.out.println(address);
 		Marker marker = new Marker(coord, address);
 		markerList.add(marker);
 		simpleModel.addOverlay(marker);
 		center = Map.center(markerList);
 		zoom = Map.zoom(markerList);
 	}
 
 	public void geocode(){
 		coord = Map.geocode(address);
 		System.out.println(coord);
 		lat=coord.getLat();
 		lng=coord.getLng();
 	}
 
 
 }  

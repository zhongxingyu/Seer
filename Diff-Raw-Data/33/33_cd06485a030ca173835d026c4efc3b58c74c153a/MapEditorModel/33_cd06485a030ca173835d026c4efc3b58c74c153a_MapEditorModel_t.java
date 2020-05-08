 package model.entity;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Observable;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import model.MapEditorModelException;
 import model.UserDisruption;
 
 /**
  * Contains all Items from the map
  *
  */
 @Entity
 public class MapEditorModel extends Observable {
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private long id;
 	
 	public long getId() {
 		return id;
 	}
 
 	@Column(nullable=false)
 	private String name;
 	
 	@OneToMany(cascade=CascadeType.PERSIST)
 	private List<Street> streets = new ArrayList<Street>();
 	
 	@Transient
 	private Street selectedStreet;
 	
 	@Transient
 	private Node selectedNode;
 		
 	@Column(nullable=false)
 
 	@Temporal(TemporalType.TIMESTAMP)
 	private Calendar saveDate;
 	
 
 	public MapEditorModel(){
 				
 	}
 	
 	public List<Street> getStreets() {
 		return streets;
 	}
 
 	public void setStreets(List<Street> streets) {
 		this.streets = streets;
 		
 		super.setChanged();
 		super.notifyObservers(streets);
 	}
 	
 	public void addStreet(Street street) {
 		String message = "Double streets not allowed!";
 		for(Street s : streets) {
 			if(s.getStart().equals(street.getStart()) && s.getEnd().equals(street.getEnd())) {
 				throw new MapEditorModelException(message);
 			}
 			if(s.getStart().equals(street.getEnd()) && s.getEnd().equals(street.getStart())) {
 				throw new MapEditorModelException(message);
 			}
 		}
 		
 		
 		streets.add(street);
 		
 		super.setChanged();
 		super.notifyObservers(street);
 		
 	}
 	
 	public void removeStreet(Street street) {
 		streets.remove(street);
 		
 		super.setChanged();
 		super.notifyObservers();
 	}
 	
 	public void closeStreet(Street street) {
 		street.setClosed(true);
 		
 		selectedStreet = null;
 		
 		super.setChanged();
 		super.notifyObservers(new UserDisruption());
 	}
 	
 	public void setSelectedStreet(Street selectedStreet) {
 		this.selectedStreet = selectedStreet;
 		
 		super.setChanged();
 		super.notifyObservers(selectedStreet);
 	}
 	
 	public Street getSelectedStreet() {
 		return selectedStreet;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public Node getSelectedNode() {
 		return selectedNode;
 	}
 
 	public void setSelectedNode(Node selectedNode) {
 		this.selectedNode = selectedNode;
 		
 		super.setChanged();
 		super.notifyObservers(selectedNode);
 	}
 	
 	public Calendar getSaveDate() {
 		return saveDate;
 	}
 
 	public void setSaveDate(Calendar saveDate) {
 		this.saveDate = saveDate;
 	}
 
 	public void loadModel(MapEditorModel model) {
 		this.streets = model.streets;
 		this.name = model.name;
 		this.id = model.id;
 		
 		super.setChanged();
 		super.notifyObservers();
 	}
 
 	public void reOpenStreet(Street street) {
 		street.setClosed(false);
 		selectedStreet = null;
 	}
 	
	public void change() {
		this.setChanged();
		this.notifyObservers();
	}
	
 }

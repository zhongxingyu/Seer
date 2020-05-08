 package M2;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * High-level element in HADL.
  * <p>
  * Element is the parent class of each base architectural element : component,
  * connector and configuration.
  * </p>
  * <p>
  * An element is defined by its name, its architectural level and a set of 
  * constraints and properties.
  * </p>
  * @author CaterpillarTeam
  */
 public class Element {
 
 	protected String name;
 	protected int level;
 	private ArrayList<Constraint> constraints;
 	private ArrayList<Property> properties;
 	
 	/**
 	 * Create an element with the given name and the given architectural level.
 	 * @param name the name of the element.
 	 * @param level the architectural level of the element.
 	 */
 	public Element(String name, int level) {
 		this.name = name;
 		this.level = level;
 		constraints = new ArrayList<Constraint>();
 		properties = new ArrayList<Property>();
 	}
 	
 	public Element(String name, Element parent) {
		this(name,parent.level+1);
 	}
 	
 	/**
 	 * Add a constraint to the element constraints.
 	 * @param constraint the constraint to add.
 	 */
 	public void addConstraint(Constraint constraint) {
 		constraints.add(constraint);
 	}
 	
 	/**
 	 * Add a property to the element properties.
 	 * @param property the property to add.
 	 */
 	public void addProperty(Property property) {
 		properties.add(property);
 	}
 	
 	/**
 	 * Set the architectural level of the element.
 	 * @param level the level.
 	 */
 	public void setLevel(int level) {
 		this.level = level;
 	}
 
 	/**
 	 * @return the name of the element.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return the architectural level of the element.
 	 */
 	public int getLevel() {
 		return level;
 	}
 	
 	/**
 	 * @return the constraints associated to the element.
 	 */
 	public Collection<Constraint> getConstraints() {
 		return constraints;
 	}
 	
 	/**
 	 * @return the properties associated to the element.
 	 */
 	public Collection<Property> getProperties() {
 		return properties;
 	}
 }

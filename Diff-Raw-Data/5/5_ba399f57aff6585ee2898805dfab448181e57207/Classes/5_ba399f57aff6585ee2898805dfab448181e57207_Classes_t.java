 package model;
 import java.util.*;
 import java.awt.*;
 
 import java.io.*;
 
 public class Classes implements java.io.Serializable {
 	/**
 	 * Classes contains data relating to a single class within a class diagram. It holds pointers to all relationships or 'links' associated to the class and also contains miscilaneous 'Fields' for Methods and Primative types.
 	 */
 
 	private String label;
 	private Map<String,Link> links;
 	private String comment;
 	private Map<String,Field> fields;
 	private Point position; //The position of the class in the graphical panel
 
 	public Point getPosition() {
 		return position;
 	}
 
 	public void setPosition(Point position) {
 		this.position = position;
 	}
 
 	public Classes(String label) {
 		/** Ctor creating a class with a given String label. The label is used to identify the Class within the design - each must be unique.
 		 */
 		this.label = label;
 		links = new TreeMap<String,Link>();
 		comment = new String("");
 		fields = new TreeMap<String,Field>();
 		position = new Point(50, 50);
 	}
 
 	public void rename(String label, Design design) {
 		/**
 		 * Rename a class and update the design to reflect it.
 		 */
 		String oldLabel = this.label;
 		this.label = label;
 		
 		design.removeClass(oldLabel);
 		design.addClass(this);
 	}
 
 	public void addLink(Link link) {
 		/**
 		 * Add a link to the Class.
 		 */
 		String label = link.getLabel();
 		links.put(label, link);
 	}
 
 	public Link getLink(String label) {
 		/**
 		 * Retrieve a Link by its label.
 		 */
 		return links.get(label);
 	}
 
 	public void removeLink(String label) {
 		/**
 		 * Remove a link by its label.
 		 */
 		links.remove(label);
 	}
 
 	public Collection<Link> getAllLinks() {
 		return links.values();
 	}
 
 	public void setComment(String comment) {
 		/**
 		 * Set this classes' comment string.
 		 */
 		this.comment = comment;
 	}
 
 	public String getComment() {
 		/**
 		 * Return this classes' comment string.
 		 */
 		return comment;
 	}
 
 	public void addField(Field field) {
 		/**
 		 * Add a field to this class.
 		 */
 		String label = field.getLabel();
 		fields.put(label, field);
 	}
 
 	public void addField(Field field, String type, int accessModifier) {
 		String label = field.getLabel();
 		field.setAccessModifier(accessModifier);
 		field.setType(type);
 		fields.put(label, field);
 	}
 	
 	public Field getField(String label) {
 		/**
 		 * Retrieve a field by its label. Returns null if no such field exists.
 		 */
 		return fields.get(label);
 	}
 
 	public void removeField(String label) {
 		/**
 		 * Remove a field by its label.
 		 */
 		fields.remove(label);
 	}
 
 	public Collection<Field> getAllFields() {
 		return fields.values();
 	}
 	
 	public String getLabel() {
 		/**
 		 * Return a copy of the string associated with this class.
 		 * 
 		 * Edit: I just learned that Java's String is immutable, making the use of a
 		 * copy-ctor superfluous.
 		 */
 		return label;
 		
 	}
 	
 	public Collection<Field> getFields()
 	{
 		return fields.values();
 	}
 
 	public void exportToFile(File file) throws IOException {
 		BufferedWriter outputStream = new BufferedWriter(new FileWriter(file) );
 		//public class <foo>\n
 		outputStream.write("public class "+getLabel());
 		//extends <foo>, <bar>, <baz>, ...
 		exportInheritance(outputStream);
 		//{
 		outputStream.write(" {");
 		outputStream.newLine();
 		//private Collection<V> <foo>
 		exportLinks(outputStream);
 		//private V <foo> // public V <foo> (<params>) {/*...*/}
 		exportFields(outputStream);
 		outputStream.write("}");
 		outputStream.newLine();
 
 		outputStream.flush();
 		outputStream.close();
 	}
 
 	private void exportInheritance(Writer outputStream) throws IOException {
 		
 		//Find this classes Base Class.
 		//NOTE: In Java a class can only extend 1 class!
 		Classes baseClass = null;
 		for (Link link : getAllLinks() ) {
 			if (link.getClassA() == this) {
 				if (link.getCardinalityA() == Link.INHERITANCE ) {
 					baseClass = link.getClassB();
 					break;
 				}
 			}
 		}
 
 		//If this class has a baseClass, write "extends <foo> else do nothing.
 		if (baseClass != null) {
 			outputStream.write(" extends "+baseClass.getLabel() );
 		}		
 
 	}
 
 	private void exportLinks(BufferedWriter outputStream) throws IOException {
 		for (Link link : getAllLinks() ) {
 			//If A->B is 1:Many
 			if (link.getCardinalityA() == Link.CARDINALITY_ONE && 
 					link.getClassA() == this &&
 					link.getCardinalityB() == Link.CARDINALITY_MANY
 					) {
 				String className = link.getClassB().getLabel();
 				String fieldName = "collectionOf"+className;
 
 				outputStream.write("\tList<"+className+"> "+fieldName+";");
 				outputStream.newLine();
 			}
 			//If A->B is 1:1. NOTE: This will attach handles to both ends!
 			else if (link.getCardinalityA() == Link.CARDINALITY_ONE &&
 					link.getClassA() == this &&
 					link.getCardinalityB() == Link.CARDINALITY_ONE
 					) {
 				String className = link.getClassB().getLabel();
 				String fieldName = "handleTo"+className;
 				
 				outputStream.write("\t"+className+" "+fieldName+";");
 				outputStream.newLine();
 			}
 			//If A-> is Many:Many write a comment.
 			else if (link.getCardinalityA() == Link.CARDINALITY_MANY &&
 					link.getCardinalityB() == Link.CARDINALITY_MANY &&
 					link.getClassA() == this
 					) {
 				String className = link.getClassB().getLabel();
 				outputStream.write("\t/*Export feature does not support many:many assoc with class "+className+"*/");
 				
 			}
 		}
 
 	}
 
 	private void exportFields(BufferedWriter outputStream) throws IOException {
 		for (Field field : getAllFields() ) {
 			String type = field.getType();
 			String name = field.getLabel();
			if (!(field instanceof Method) ) {
 				outputStream.write("\tprivate "+type+" "+name+";");
 				outputStream.newLine();
 			}
			if (field instanceof Method ) {
 				outputStream.write("\tpublic "+type+" "+name+"(");
 				if (!(((Method)field).getParameters().isEmpty() ) ) {
 					Iterator<Field> it = ((Method)field).getParameters().iterator();
 					Field param = it.next();
 					while (it.hasNext()) {
 						outputStream.write(param.getType()+" "+param.getLabel()+",");
 						param = it.next();
 					}
 					outputStream.write(param.getType()+" "+param.getLabel());
 							
 				}
 				/*for (Field parameter : ((Method)field ).getParameters() ) {
 					outputStream.write(parameter.getType()+" "+parameter.getLabel()+",");
 				}*/
 				outputStream.write(") { /*...*/ }");
 				outputStream.newLine();
 			}
 		}
 	}
 
 }

 /**
  * 
  */
 package Tag;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 /**
  * This is the collection of Tag types that will be created at runtime
  * and passed to the programs that need to print new tags or validate
  * existing ones. The tags are all stored in a hash with their names
  * as their keys.
  * @author Eric Majchrzak
  *
  */
 public class TagCollection {
 	
 	//This is the table that will store all of the tags
 	Hashtable<String, Tag> tags;
 	
 	public TagCollection(){
 		
 		//Make the new hash table
 		tags = new Hashtable<String, Tag>();
 		
 		//Add Emphasis Tags
 		tags.put("b",new Tag("b","Bold"));
 		tags.put("i",new Tag("i","Italics"));
 		
 		//Add Header tags
 		tags.put("header",new Tag("header","Header"));
 		tags.put("h1",new Tag("h1","h1"));
 		tags.put("h2",new Tag("h2","h2"));
 		tags.put("h3",new Tag("h3","h3"));
 		tags.put("h4",new Tag("h4","h4"));
 		tags.put("h5",new Tag("h5","h5"));
 		tags.put("h6",new Tag("h6","h6"));
 		
 		//Define list Tags
 		tags.put("oi",new Tag("ol","Ordered List"));
 		tags.put("ul",new Tag("ul","Unordered list"));
 		tags.put("li",new Tag("li","list item"));
 		tags.put("dl",new Tag("dl","Definition List"));
 		tags.put("dt",new Tag("dt","Definition List Item"));
 		tags.put("dd",new Tag("dd","Definition List Item Description"));
 		
 		//Tables
 		tags.put("table",new Tag("table","Table"));
 		tags.put("tr",new Tag("tr","Table Row"));
 		tags.put("td",new Tag("td","Table cell"));
 	}
 	
 	/**
 	 * This is the getNames method used by the menu bar to populate a list of insert
 	 * commands for the menu bar.
 	 * @return ArrayList<String>
 	 */
 	public ArrayList<String> getNames(){
 		ArrayList<String> names = new ArrayList<String>();
 		for (Enumeration<Tag> temp = tags.elements(); temp.hasMoreElements();) 
 			names.add(temp.nextElement().getName());
 		return names;
 	}
 	
 	/**
 	 * This is the get tag function. This function will get a tag from the hash
 	 * and return the text to be inserted
 	 * @return String
 	 */
 	public String getTag(String tagName){
 		for (Enumeration<Tag> temp = tags.elements(); temp.hasMoreElements();){
 			Tag current = temp.nextElement();
 			if(current.getName() == tagName){
 				return current.insertTag();
 			}
 		}
 		return null;	
 	}
 
 	/**
 	 * This is the checkTag function. It will take text in and see if it is a valid
 	 * tag in the tag collection.
 	 * @return boolean
 	 */
 	public boolean checkTag(String tagToCheck){
 		return tags.containsKey(tagToCheck);
 	}
 }

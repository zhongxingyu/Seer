 package util.ast.node;
 
 import java.util.ArrayList;
 
 import util.type.Types;
 
 import back_end.Visitor;
 
 /**
  * This node is for the @Functions, @Map, @Reduce and @Main sections
  * 
  * @author sam (commented by ben)
  *
  */
 public class SectionNode extends Node {
 
 	public static enum SectionName {
 		FUNCTIONS, MAP, REDUCE, MAIN;
 		}
 	
 	// name of section, ie, main, map, reduce, functions
 	protected SectionName sectionName;
 	
 	// Used to defined key-value pair types for input and ouput
 	protected SectionTypeNode type;
 
 	// use this constructor for @functions and @Main sections
	public SectionNode(SectionTypeNode list, SectionName sectionName) {
 		SectionNode.LOGGER.fine("adding list child to @Functions or @Main SectionNode");
 		this.addChild(list);
 		this.sectionName = sectionName;
 	}
 	
 	/**
 	 * 
 	 * @param type
 	 * @param list
 	 * @param sectionName
 	 */
 	public SectionNode(){
 		
 	}
 
 	// use this constructor for @map and @reduce sections
 	public SectionNode(SectionTypeNode type, StatementListNode list, SectionName sectionName) {
 		this.children = new ArrayList<Node>();
 		SectionNode.LOGGER.fine("adding list child to @Map or @Reduce SectionNode");
 		this.addChild(type);
 		this.addChild(list);
 		this.sectionName = sectionName;
 	}
 
 	@Override
 	public void accept(Visitor v) {
 		v.visit(this);
 	}
 
 	@Override
 	public String getName() {
 		return "SectionNode: " + sectionName;
 	}
 
 	@Override
 	public int visitorTest(Visitor v) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }

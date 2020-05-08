 package confdb.parser;
 
 import java.util.ArrayList;
 
 
 /**
  * ParseNode
  * ---------
  * @author Philipp Schieferdecker
  *
  * Node holding a string in the parse tree of a configuration.
  */
 public class ParseNode
 {
     /** valid opening quotes */
     private static final String oquotes[] = {"r\"'", "r'\"", "'"};
 
     /** corresponing valid closing quotes */
     private static final String cquotes[] = {"'\"",  "\"'",  "'"};
 
     /** the content string */
     private String content = "";
     
     /** type of quotes */
     private int iquote = -1;
     
     /** the parent node */
     private ParseNode parent = null;
 
     /** child nodes, if any */
     private ArrayList<ParseNode> children = new ArrayList<ParseNode>();
 
 
     /** constructor */
     public ParseNode() {}
     
     /** constructor with parent */
     public ParseNode(ParseNode parent) { this.parent  = parent; }
 
     
     /** retrieve content */
     public String content() { return content; }
 
     /** retrieve unquoted content */
     public String unquoted()
     {
 	if (iquote<0) return content;
 
 	if (!content.startsWith(oquotes[iquote]))
 	    System.err.println("ParseNode.unquoted ERROR: "+
 			       "missing opening quotes.");
 	
 	if (!content.endsWith(cquotes[iquote]))
 	    System.err.println("ParseNode.unquoted ERROR: "+
 			       "missing closing quotes.");
 
	int olength = (oquotes[iquote].length()==1) ? 1 : oquotes[iquote].length()-1;
	int clength = (cquotes[iquote].length()==1) ? 1 : cquotes[iquote].length()-1;
	return content.substring(olength,content.length()-clength);
	//return content.substring(oquotes[iquote].length(),
	//			 content.length()-cquotes[iquote].length());
     }
     
     /** get the parent node */
     public ParseNode parent() { return parent; }
     
     /** get number of children */
     public int childCount() { return children.size(); }
 
     /** return i-th child */
     public ParseNode child(int i) { return children.get(i); }
 
     /** is the content complete? */
     public boolean isComplete()
     {
 	if (iquote<0) return false;
 	if (!content.endsWith(cquotes[iquote])) return false;
 	return true;
     }
     
     /** is this the tree's root node? */
     public boolean isRoot() { return (parent==null); }
 
     /** is this node a leaf? */
     public boolean isLeaf() { return (children.size()==0); }
 
     /** set content */
     public void setContent(String content) throws ParserException
     {
 	if (this.content.length()>0)
 	    System.out.println("ParseNode.setContent WARNING: " +
 			       "overriding content = " + this.content);
 	iquote=-1;
 	for (int i=0;i<oquotes.length;i++) 
 	    if (content.startsWith(oquotes[i])) { iquote=i; break; }
 	
 	if (iquote<0)
 	    throw new ParserException("ParseNode.setContent: "+
 				      "missing opening quotes.");
 
 	if (content.indexOf(cquotes[iquote])>0&&
 	    !content.endsWith(cquotes[iquote]))
 	    throw new ParserException("ParseNode.setContent: "+
 				      "characters found after closing quotes.");
 
 	this.content = content;
     }
     
     /** add content */
     public void addContent(String content) throws ParserException
     {
 	if (this.content.equals("")) {
 	    setContent(content);
 	    return;
 	}
 	
 	if (iquote<0) throw new ParserException("ParseNode.addContent: "+
 						 "iquote unset.");
 	
 	if (content.indexOf(cquotes[iquote])>=0&&
 	    !content.endsWith(cquotes[iquote]))
 	    throw new ParserException("ParseNode.addContent: "+
 				      "characters found after closing quotes.");
 
 	this.content += " " + content;
     }
 
     /** set parent node */
     public void setParent(ParseNode parent) { this.parent = parent; }
 
 
     /** add a child node */
     public ParseNode addNode() 
     {
 	ParseNode node = new ParseNode();
 	children.add(node);
 	node.setParent(this);
 	return node;
     }
     
     /** add a child node */
     public ParseNode addNode(ParseNode node)
     {
 	children.add(node);
 	node.setParent(this);
 	return node;
     }
 
 }

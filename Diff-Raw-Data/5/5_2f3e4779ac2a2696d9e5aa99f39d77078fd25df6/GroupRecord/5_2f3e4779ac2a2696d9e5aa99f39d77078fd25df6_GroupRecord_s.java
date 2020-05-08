 package stellar.data;
 import java.awt.Dimension;
 import java.lang.IndexOutOfBoundsException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.Vector;
 import javax.swing.text.Document;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreeNode;
 /**
  * Record of star system groups. This is kept as a tree:
  * subsectror->quadrant->sector->domain. There is a generic group as well for 
  * non-standard grouping of systems. The tree may contain one or more of the
 * groups, but none of garunteed to be there. 
  * 
 * @version $Revision: 1.7 $
  * @author $Author$
 */
 public class GroupRecord extends Record implements MutableTreeNode 
 {
     /*
      <xsd:simpleType name="group_type">
         <xsd:restriction base="xsd:string">
             <xsd:enumeration value="sector" />
             <xsd:enumeration value="subsector" />
             <xsd:enumeration value="group" />
             <xsd:enumeration value="quadrant" />
             <xsd:enumeration value="domain" />
         </xsd:restriction>
     </xsd:simpleType>
     */
     /*
     public static final String SECTOR = "sector";
     public static final String SUBSECTOR = "subsector";
     public static final String GROUP = "group";
     public static final String QUADRANT = "quadrant";
     public static final String DOMAIN = "domain";
 
     public static final int DOMAIN_INDEX = 0;
     public static final int SECTOR_INDEX = 1;
     public static final int QUADRANT_INDEX = 2;
     public static final int SUBSECTOR_INDEX = 3;
     public static final int GROUP_INDEX = 10;
     */
     private String name;
     private HexID location;
     private GroupRecord parent;
     private String parentName;
     private GroupType type;
     private int extentX;
     private int extentY;
     private int extentZ;
     private HTMLDocument comment;
     private String fileName;
     private ArrayList<StarSystem> systems;
     private HashSet links;
     private Dimension offset = new Dimension (1,1);
     private Vector children;    
 
     public GroupRecord()
     {
     }
     
     public GroupRecord (GroupRecord aGroup)
     {
         super (aGroup);
         this.name = aGroup.getName();
         setParent ((MutableTreeNode)aGroup.getParent());
         setType (aGroup.getType());
         setComment (aGroup.getComment());
         setLocation (new HexID (aGroup.getLocation()));
     }
     public String toString() { return name; }
     public HTMLDocument getComment() { return comment; }
     public void setComment(Document newComment) { comment = (HTMLDocument)newComment; }
     public String getName() { return name; }
     public void setName(String name) { this.name = name; }
     public HexID getLocation() { return location; }
     public void setLocation(HexID location) { this.location = location; }
     public GroupType getType() { return type; }
     public int getExtentX() { return extentX; }
     public void setExtentX(int extentX) { this.extentX = extentX; }
     public int getExtentY() { return extentY; }
     public void setExtentY(int extentY) { this.extentY = extentY; }
     public int getExtentZ() { return extentZ; }
     public void setExtentZ(int extentZ) { this.extentZ = extentZ; }
     public String getParentName () { return parentName; } 
    /*
     public int getTypeIndex()
     {
         if (type.matches(DOMAIN)) return DOMAIN_INDEX;
         if (type.matches(SECTOR)) return SECTOR_INDEX;
         if (type.matches(QUADRANT)) return QUADRANT_INDEX;
         if (type.matches(SUBSECTOR)) return SUBSECTOR_INDEX;
         if (type.matches(GROUP)) return GROUP_INDEX;
         return -1;
     }
     */
     public StarSystem getSystem (int index) { return systems.get(index); }
     public int getSystemCount () { return systems != null? systems.size() : 0; }
     public ListIterator<StarSystem> getSystems () { return systems != null ? systems.listIterator() : null; }
     public void addSystem (StarSystem system) 
     { 
         if (systems == null) { systems = new ArrayList<StarSystem>(); } 
         systems.add(system); 
     } 
 
     public StarSystem getSystem (HexID xy)
     {
         if (systems == null) return null;
         StarSystem s;
         try
         {
             s = systems.get(systems.indexOf(xy));
             return s;
         }
         catch (IndexOutOfBoundsException e)
         {
             return null;
         }
     }
     
     public Iterator getLinks() { return (links == null) ? Collections.EMPTY_LIST.listIterator() : links.iterator(); }
     public void addLink (Links newLink)
     {
         if (links == null) { links = new HashSet<Links> (); }
         if (links.contains (newLink)) return;
         links.add(newLink);
     }
     public int getLinkCount () { return (links == null) ? -1 : links.size(); } 
 
     public boolean inGroup (HexID h)
     {
         int x, y;
         
         /* if this hex is not in this group, make sure it's in one of the parent
          * groups. It not in any of them, we've got two branches of the root tree
          * for which we don't want to place this system within. 
          */
         if (!(h.getHexGroup().equals(getKey())))
         {
             GroupRecord parent = (GroupRecord)getParent();
             while (parent != null)
             {
                 if (h.getHexGroup().equals(parent.getKey())) break;
                 parent = (GroupRecord)parent.getParent();
             }
             if (parent == null) return false;
         }
         
         if (parentName == null)
         {
             x = 0; y = 0;
         }
         else
         {
             x = (location.x - 1) * extentX; 
             y = (location.y - 1) * extentY;
         }        
         return (h.x > x && h.x <= x + extentX && h.y > y && h.y <= y + extentY);
     }
 
     public String getFileName() { return fileName; }
     public void setFileName(String fileName) { this.fileName = fileName; }
     public Dimension getOffset () { return offset; } 
     public void setType(GroupType type) 
     { 
         this.type = type; 
         if (type == GroupType.SUBSECTOR) { extentX = 8; extentY=10; }
         if (type == GroupType.QUADRANT)  { extentX = 16; extentY=20; } 
         if (type == GroupType.SECTOR)    { extentX = 32; extentY=40; } 
         if (type == GroupType.DOMAIN)    { extentX = 64; extentY=80; } 
     }
     
     // TreeNode functions;
     public Enumeration children () 
     { 
         if (children == null) return DefaultMutableTreeNode.EMPTY_ENUMERATION;
         else return children.elements();
     }
     public boolean getAllowsChildren() { return true; }
     public TreeNode getChildAt (int index) { return children == null ? null : (TreeNode)children.elementAt(index); } 
     public int getChildCount () { return children == null ? 0 : children.size(); } 
     public int getIndex(TreeNode aChild) { return children == null ? -1 : children.indexOf(aChild); } 
     public TreeNode getParent() { return parent; }
     public boolean isLeaf() { return (getChildCount() == 0); }
     
     //MutableTreeNode functions;
     public void setParent (MutableTreeNode newParent) 
     {
         parent = (GroupRecord)newParent; 
         parentName = newParent.toString();
         offset.height = ((location.x - 1) * extentX) + 1;
         offset.width = ((location.y - 1) * extentY) + 1;
     }
     public void setUserObject (Object object) {}
     public void insert (MutableTreeNode newChild, int index) 
     { 
     	MutableTreeNode oldParent = (MutableTreeNode)newChild.getParent();
 
 	    if (oldParent != null) {
 		oldParent.remove(newChild);
 	    }
 	    newChild.setParent(this);
 	    if (children == null) {
 		children = new Vector();
 	    }
 	    children.insertElementAt(newChild, index);
     }
     
     public void remove (int index)
     {
         MutableTreeNode child = (MutableTreeNode)getChildAt(index);
         children.removeElementAt(index);
         child.setParent(null);
     }
     
     public void remove (MutableTreeNode aChild)
     {
         int index = getIndex(aChild);
         if (index >= 0) remove (index);
     }
     
     public void removeFromParent()
     {
         MutableTreeNode parent = (MutableTreeNode)getParent();
         if (parent != null) 
         {
             parent.remove(this);
         }
     }
 
 }

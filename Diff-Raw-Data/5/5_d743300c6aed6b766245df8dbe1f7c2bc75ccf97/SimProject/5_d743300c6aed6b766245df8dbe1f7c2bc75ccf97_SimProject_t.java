 package lslplus.sim;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import lslplus.gentree.Node;
 import lslplus.gentree.NodeFactory;
 import lslplus.gentree.NodeStatus;
 import lslplus.gentree.NodeVisitor;
 import lslplus.sim.SimProjectNodes.AnimationNode;
 import lslplus.sim.SimProjectNodes.BodyPartNode;
 import lslplus.sim.SimProjectNodes.ClothingNode;
 import lslplus.sim.SimProjectNodes.GestureNode;
 import lslplus.sim.SimProjectNodes.InventoryNode;
 import lslplus.sim.SimProjectNodes.InventoryPropertiesNode;
 import lslplus.sim.SimProjectNodes.LandmarkNode;
 import lslplus.sim.SimProjectNodes.NotecardLineNode;
 import lslplus.sim.SimProjectNodes.NotecardNode;
 import lslplus.sim.SimProjectNodes.SoundNode;
 import lslplus.sim.SimProjectNodes.TextureNode;
 import lslplus.sim.SimWorldDef.Avatar;
 import lslplus.sim.SimWorldDef.InventoryItem;
 import lslplus.sim.SimWorldDef.LVector;
 import lslplus.sim.SimWorldDef.Prim;
 import lslplus.sim.SimWorldDef.Region;
 import lslplus.sim.SimWorldDef.ScriptInfo;
 import lslplus.sim.SimWorldDef.SimObject;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 // TODO: add validation, so we can mark project is invalid, e.g. if referenced scripts don't exist.
 public class SimProject {
     private static final String PRIM_PROPERTIES = "prim-properties"; //$NON-NLS-1$
     private static final String OBJECT_PROPERTIES = "object-properties";
     static final NodeFactory[] EMPTY_FACTORY_LIST = { };
     private static final String DEFAULT_AVATAR_ID = "Default Avatar";
     private static HashMap ID_TO_DISPLAY = new HashMap();
     
     static {
         ID_TO_DISPLAY.put("pos", "Position"); //$NON-NLS-1$
         ID_TO_DISPLAY.put("prim-properties", "Prim properties"); //$NON-NLS-1$
         ID_TO_DISPLAY.put("avatar-properties", "Avatar properties"); //$NON-NLS-1$
         ID_TO_DISPLAY.put(OBJECT_PROPERTIES, "Object properties");
     }
     
     public static interface HasDerivedValue {
         public Object getDerivedValue();
     }
     
     private static ObjectNodeFactory objectNodeFactory = new ObjectNodeFactory();
     static PrimNodeFactory primNodeFactory = new PrimNodeFactory();
     private static ScriptNodeFactory scriptNodeFactory = new ScriptNodeFactory();
     private static AvatarNodeFactory avatarNodeFactory = new AvatarNodeFactory();
 
     public static class ObjectNodeFactory implements NodeFactory {
         public Node createNode(Node parent) {
             ObjectNode node = new ObjectNode(parent, "Object");
             PrimNode rootPrim = (PrimNode) primNodeFactory.createNode(node);
             rootPrim.setName("Object");
             node.addChild(rootPrim);
             
             return node;
         }
 
         public String getNodeTypeName() {
             return "Object";
         }
     }
 
     public static class PrimNodeFactory implements NodeFactory {
         public Node createNode(Node parent) {
             String name = computeNewName(parent.getChildren(), "prim");
             return new PrimNode(parent, name);
         }
 
         public String getNodeTypeName() {
             return "Prim";
         }
     }
 
     public static class ScriptNodeFactory implements NodeFactory {
 
         public Node createNode(Node parent) {
             PrimNode node = (PrimNode) parent;
             List nodes = node.getChildren();
             String name = SimProject.computeNewName(nodes, "New Script");
             
             return new ScriptNode(parent, name, "");
         }
 
         public String getNodeTypeName() {
             return "Script";
         }
     }
     
     public static class AvatarNodeFactory implements NodeFactory {
         public Node createNode(Node parent) {
             WorldNode world = (WorldNode) parent;
             String name = computeNewName(world.getChildren(), "Joe Avatar");
             
             return new AvatarNode(parent, name);
         }
 
         public String getNodeTypeName() {
             return "Avatar";
         }
     }
 
     static class KeyTracker {
         private SimKeyManager mgr = new SimKeyManager();
         private HashMap xref = new HashMap();
         
         public String keyFor(Object o) {
             String k = (String) xref.get(o);
             if (k == null) {
                 k = mgr.getNextKey();
                 xref.put(o, k);
             }
             return k;
         }
     }
     
     public static class WorldNode extends Node implements IAdaptable {
         private static final NodeFactory[] LEGAL_CHILD_NODES = { objectNodeFactory, avatarNodeFactory };
         private IResource resource;
         private transient KeyTracker tracker;
         public WorldNode(String name) {
             super(null, name,""); //$NON-NLS-1$
             addChild(new AnyNaturalNode(this, "max_time", 10000000)); //$NON-NLS-1$
             addChild(new EventHandlerNode(this, "event-handler", null)); //$NON-NLS-1$
             addChild(new DefaultAvatarNode(this));
         }
         
         public NodeFactory[] legalChildNodes() {
             return LEGAL_CHILD_NODES;
         }
 
         public String getValueString() {
             return getValue().toString();
         }
 
         public NodeStatus checkValueString(String s) {
             return NodeStatus.OK;
         }
 
         public void onUpdate(String s) {
             this.setValue(s);
         }
 
         public boolean isValueChangeable() {
             return false;
         }
 
         public NodeStatus checkNameString(String name) {
             return NodeStatus.OK;
         }
 
         public boolean isNameChangeable() {
             return true;
         }
 
         public boolean isDeletable() {
             return false;
         }
         
         public void setResource(IResource r) {
             resource = r;
         }
         
         public IResource getResource() { return resource; }
 
         public Object getAdapter(Class adapter) {
             return Platform.getAdapterManager().getAdapter(this, adapter);
         }
         
         public synchronized String keyFor(Object o) {
             if (this.tracker == null) this.tracker = new KeyTracker();
             return tracker.keyFor(o);
         }
     }
     
     public static class ObjectNode extends Node {
         private static final NodeFactory[] LEGAL_CHILD_NODES = { primNodeFactory };
         public ObjectNode(Node parent, String name) {
             super(parent, name, null);
             addChild(new ObjectPropertiesNode(this));
         }
         
         public NodeFactory[] legalChildNodes() {
             return LEGAL_CHILD_NODES;
         }
 
         public String getValueString() {
             return "";
         }
 
         public NodeStatus checkValueString(String s) {
             return NodeStatus.OK;
         }
 
         public void onUpdate(String s) {
         }
 
         public boolean isValueChangeable() {
             return false;
         }
 
         public NodeStatus checkNameString(String name) {
             return NodeStatus.OK;
         }
 
         public void updateName(String name) {
             List prims = findChildrenByType(PrimNode.class);
             Node root = (Node) prims.get(0);
             super.setName(name);
             root.setName(name);
         }
         
         protected void childUpdated(Node child, Object oldValue) {
 //            if (child instanceof GridCoordinateNode) {
 //                Node prim = child.findAncestorOfType(PrimNode.class);
 //                if (!isFirstChildOfType(prim, PrimNode.class)) return;
 //                // this was a coordinate of the root prim...
 //                
 //                final GridCoordinateNode coord = (GridCoordinateNode) child;
 //                final float vDelt = ((Float)coord.getValue()).floatValue() - 
 //                                    ((Float)oldValue).floatValue();
 //                this.accept(new NodeVisitor() {
 //                    public void visit(Node n) {
 //                        if (n instanceof GridCoordinateNode && n != coord) {
 //                            if (n.getName().equals(coord.getName())) {
 //                                float val = ((Float)n.getValue()).floatValue();
 //                                n.setValue(new Float(GridCoordinateNode.clipCoordinate(val + vDelt)));
 //                            }
 //                        }
 //                    }
 //                });
 //            }
         }
         
         
         protected void onChildRemoved(Node n) {
             if (n instanceof PrimNode) {
                 List prims = findChildrenByType(PrimNode.class);
                 if (n == prims.get(0)) {
                     if (prims.size() > 1) {
                         PrimNode secondPrim = (PrimNode) prims.get(1);
                         secondPrim.zeroPosition();
                     }
                 }
             }
         }
 
         public boolean isNameChangeable() {
             return true;
         }
 
         public boolean isDeletable() {
             return true;
         }
 
         public String getOwner() {
             List prims = findChildrenByType(PrimNode.class);
             if (prims.size() == 0) return DEFAULT_AVATAR_ID;
             else {
                 PrimNode prim = (PrimNode) prims.get(0);
                 return prim.getOwner();
             }
         }
 
         public SimObject getSimObject(List allPrims) { 
             List primNodes = findChildrenByType(PrimNode.class);
             String[] primKeys = new String[primNodes.size()];
             int j = 0;
             for (Iterator i1 = primNodes.iterator(); i1.hasNext(); ) {
                 PrimNode pn = (PrimNode)i1.next();
                 Prim p = (Prim)pn.getDerivedValue();
                 allPrims.add(p);
                 primKeys[j++] = p.getKey();
             }
             ObjectPropertiesNode props = (ObjectPropertiesNode) findChildByName(OBJECT_PROPERTIES);
             Map m = props.getData();
             LVector position = (LVector) m.get("position");
             return new SimObject(primKeys, position);
         }
     }
     
     public static class PrimNode extends Node implements HasDerivedValue {
         private static final NodeFactory[] LEGAL_CHILD_NODES =
             { scriptNodeFactory, SimProjectNodes.notecardFactory, SimProjectNodes.clothingFactory,
               SimProjectNodes.bodyPartFactory, SimProjectNodes.gestureFactory,
               SimProjectNodes.soundFactory, SimProjectNodes.animationFactory,
               SimProjectNodes.textureFactory, SimProjectNodes.landmarkFactory,
               SimProjectNodes.inventoryObjectFactory };
         
         public PrimNode(Node parent, String name) {
             super(parent, name, null);
             ObjectNode object = (ObjectNode) findAncestorOfType(ObjectNode.class);
             String owner = object.getOwner();
             addChild(new PrimPropertiesNode(this,owner));
         }
         
         public void zeroPosition() {
             PrimPropertiesNode props = (PrimPropertiesNode) findChildByName(SimProject.PRIM_PROPERTIES);
             
             GridPositionNode node = (GridPositionNode) props.findChildByName("pos");
             GridCoordinateNode x = (GridCoordinateNode) node.findChildByName("x");
             x.setValue(new Integer(0));
             GridCoordinateNode y = (GridCoordinateNode) node.findChildByName("y");
             y.setValue(new Integer(0));
         }
 
         public void setOwner(String owner) {
             PrimPropertiesNode props = (PrimPropertiesNode) findChildByName(SimProject.PRIM_PROPERTIES);
             
             props.setProperty("owner", owner);
         }
 
         public String getOwner() {
             PrimPropertiesNode props = (PrimPropertiesNode) findChildByName(SimProject.PRIM_PROPERTIES);
             
             return props.getProperty("owner");
         }
 
         public NodeFactory[] legalChildNodes() {
             return LEGAL_CHILD_NODES;
         }
 
         public String getValueString() {
             return "";
         }
 
         public boolean isValueChangeable() {
             return false;
         }
 
         public NodeStatus checkValueString(String s) {
             return NodeStatus.OK;
         }
 
         public void onUpdate(String s) {
         }
 
         public NodeStatus checkNameString(String name) {
             return NodeStatus.OK;
         }
 
         public void updateName(String name) {
             List prims = getParent().findChildrenByType(PrimNode.class);
             if (prims.indexOf(this) == 0) getParent().setName(name);
             super.setName(name);
         }
         
         public boolean isNameChangeable() {
             return true;
         }
 
         public boolean isDeletable() {
             List prims = getParent().findChildrenByType(PrimNode.class);
            
             return prims.size() > 1;
         }
 
         public boolean isRootPrim() {
             return this.equals(getParent().findChildrenByType(this.getClass()).get(0));
         }
 
         public Object getDerivedValue() {
             List inventoryNodes = this.findChildrenByType(InventoryNode.class);
             InventoryItem[] invItems = new InventoryItem[inventoryNodes.size()];
             int j = 0;
             for (Iterator i = inventoryNodes.iterator(); i.hasNext();) {
                 invItems[j++] = ((InventoryNode)i.next()).getInventoryItem();
             }
             String key = ((WorldNode)findRoot()).keyFor(this);
             List scriptNodes = this.findChildrenByType(ScriptNode.class);
             ScriptInfo[] scripts = new ScriptInfo[scriptNodes.size()];
             j = 0;
             for (Iterator i = scriptNodes.iterator(); i.hasNext();) {
                 ScriptNode n = (ScriptNode) i.next();
                 scripts[j++] = new ScriptInfo(n.getName(), n.getValueString());
             }
             
             PrimPropertiesNode props = (PrimPropertiesNode) findChildByName(PRIM_PROPERTIES);
             Map m = props.getData();
             LVector position = (LVector) m.get("position");
             return new SimWorldDef.Prim(getName(),
                     key, scripts, invItems, (String)m.get("description"), getOwner(), position, new LVector(0,0,0));
         }
     }
 
     public static abstract class FixedFormatNode extends Node {
         public FixedFormatNode(Node parent, String nodeName, Object value) {
             super(parent, nodeName, value);
         }
 
         public NodeStatus checkNameString(String name) {
             return NodeStatus.OK;
         }
 
         public NodeStatus checkValueString(String s) {
             return NodeStatus.OK;
         }
 
         public String getNameDisplay() {
             return (String) ID_TO_DISPLAY.get(getName());
         }
         public String getValueString() {
             return "";
         }
 
         public boolean isDeletable() {
             return false;
         }
 
         public boolean isNameChangeable() {
             return false;
         }
 
         public boolean isValueChangeable() {
             return false;
         }
 
         public NodeFactory[] legalChildNodes() {
             return EMPTY_FACTORY_LIST;
         }
 
         public void onUpdate(String s) {
         }
         
         abstract public Map getData();
     }
 
     public static class PrimPropertiesNode extends FixedFormatNode {
         public PrimPropertiesNode(Node parent, String owner) {
             super(parent, PRIM_PROPERTIES, null);
             addChild(new GridPositionNode(this,"pos"));
             addChild(new AvatarReferenceNode(this, "owner", owner));
             addChild(new StringNode(this,"description",""));
         }
 
         public void setProperty(String string, String val) {
             Node n = findChildByName(string);
             if (n != null) n.updateValue(val);
         }
 
         public String getProperty(String string) {
             return findChildByName(string).getValueString();
         }
 
         public Map getData() {
             HashMap map = new HashMap();
             GridPositionNode node = (GridPositionNode) findChildByName("pos"); //$NON-NLS-1$
            map.put("position", node.getVector()); //$NON-NLS-1$
             
             AvatarReferenceNode owner = (AvatarReferenceNode) findChildByName("owner"); //$NON-NLS-1$
             map.put("owner", owner.getValueString()); //$NON-NLS-1$
             
             StringNode description = (StringNode) findChildByName("description"); //$NON-NLS-1$
             map.put("description", description.getValueString()); //$NON-NLS-1$
             
             return map;
         }
 
         public boolean isInRootPrim() {
             return ((PrimNode)getParent()).isRootPrim();
         }
     }
 
     public static class ObjectPropertiesNode extends FixedFormatNode {
 
         public ObjectPropertiesNode(Node parent) {
             super(parent, OBJECT_PROPERTIES, null);
             addChild(new GridPositionNode(this,"pos"));
         }
 
         public Map getData() {
             HashMap map = new HashMap();
             GridPositionNode node = (GridPositionNode) findChildByName("pos"); //$NON-NLS-1$
            map.put("position", node.getVector()); //$NON-NLS-1$
             return map;
         }
         
     }
     
     public static class AvatarPropertiesNode extends FixedFormatNode {
         public AvatarPropertiesNode(Node parent) {
             super(parent, "avatar-properties", null); //$NON-NLS-1$
             addChild(new GridPositionNode(this,"pos")); //$NON-NLS-1$
         }
 
         public Map getData() {
             HashMap map = new HashMap();
             GridPositionNode node = (GridPositionNode) findChildByName("pos"); //$NON-NLS-1$
             map.put("pos", node.getVector()); //$NON-NLS-1$
             return map;
         }
     }
     
     public static class AvatarNode extends Node implements HasDerivedValue {
         private static final NodeStatus AVATAR_NAME_IN_USE = new NodeStatus(false, "Avatar name already in use");
         public AvatarNode(Node parent, String nodeName) {
             super(parent, nodeName, null);
             addChild(new AvatarPropertiesNode(this));
          }
 
         public void onRemove() {
             findRoot().accept(new NodeVisitor() {
                 public void visit(Node n) {
                     if (n instanceof AvatarReferenceNode) {
                         if (getName().equals(n.getValue())) {
                             n.setValue(DEFAULT_AVATAR_ID);
                         }
                     }
                 }
             });
         }
         
         public NodeFactory[] legalChildNodes() {
             return new NodeFactory[0];
         }
 
         public String getValueString() {
             return "";
         }
 
         public boolean isValueChangeable() {
             return false;
         }
 
         public NodeStatus checkValueString(String s) {
             return NodeStatus.OK;
         }
 
         public void onUpdate(String s) {
         }
 
         public void updateName(final String s) {
             final String name = getName();
             Node root = findRoot();
             
             root.accept(new NodeVisitor() {
                 public void visit(Node n) {
                     if (n instanceof AvatarReferenceNode && name.equals(n.getValue())) {
                         n.setValue(s);
                     }
                 }
                 
             });
             super.updateName(s);
         }
         
         public NodeStatus checkNameString(String name) {
             List list = getParent().findChildrenByType(AvatarNode.class);
             NodeStatus error = AVATAR_NAME_IN_USE;
             return SimProject.checkNameUnique(this, name, list, error);
         }
 
         public boolean isNameChangeable() {
             return true;
         }
 
         public boolean isDeletable() {
             return true;
         }
 
         public Object getDerivedValue() {
             AvatarPropertiesNode pn = (AvatarPropertiesNode) findChildByName("avatar-properties"); //$NON-NLS-1$
             Map props = pn.getData();
             LVector position = (LVector) props.get("pos"); //$NON-NLS-1$
             return new SimWorldDef.Avatar(getName(),position.getX(), position.getY(), position.getZ());
         }
         
     }
     
     public static class DefaultAvatarNode extends AvatarNode {
 
         public DefaultAvatarNode(Node parent) {
             super(parent, DEFAULT_AVATAR_ID);
         }
         
         public boolean isDeletable() { return false; }
         public boolean isNameChangeable() { return false; }
     }
     
     public static class ScriptNode extends Node {
         private static final NodeFactory[] LEGAL_CHILD_NODES = new NodeFactory[0];
         private static final NodeStatus SCRIPT_NAME_IN_USE = new NodeStatus(false, "Script name already in use");
         public NodeFactory[] legalChildNodes() {
             return LEGAL_CHILD_NODES;
         }
         
         public ScriptNode(Node parent, String name, String scriptId) {
             super(parent, name, scriptId);
         }
 
         public String getValueString() {
             return (String)getValue();
         }
 
         public boolean isValueChangeable() {
             return true;
         }
 
         public NodeStatus checkValueString(String s) {
             return NodeStatus.OK;
         }
 
         public void onUpdate(String s) {
             setValue(s);
         }
         
         public boolean hasValueChoices() {
             return true;
         }
         
         public String getChoicesId() {
             return "scripts";
         }
 
         public NodeStatus checkNameString(String name) {
             return checkNameUnique(this, name, getParent().getChildren(), SCRIPT_NAME_IN_USE);
         }
 
         public boolean isNameChangeable() {
             return true;
         }
 
         public boolean isDeletable() {
             return true;
         }
     }
     
     public static class AvatarReferenceNode extends Node {
 
         public AvatarReferenceNode(Node parent, String nodeName, Object value) {
             super(parent, nodeName, value);
         }
 
         public String getChoicesId() {
             return "avatars"; //$NON-NLS-1$
         }
 
         public boolean hasValueChoices() {
             return true;
         }
 
         public NodeStatus checkNameString(String name) {
             return NodeStatus.OK;
         }
 
         public NodeStatus checkValueString(String s) {
             return NodeStatus.OK;
         }
 
         public String getValueString() {
             return (String) getValue();
         }
 
         public boolean isDeletable() {
             return false;
         }
 
         public boolean isNameChangeable() {
             return false;
         }
 
         public boolean isValueChangeable() {
             if (getParent() instanceof PrimPropertiesNode)
                 return ((PrimPropertiesNode)getParent()).isInRootPrim();
             return true;
         }
 
         public NodeFactory[] legalChildNodes() {
             return EMPTY_FACTORY_LIST;
         }
 
         public void onUpdate(final String s) {
             setValue(s);
             
             ObjectNode n = findObjectParent();
             n.accept(new NodeVisitor() {
                 public void visit(Node n) {
                     if (n != AvatarReferenceNode.this && n instanceof AvatarReferenceNode) {
                         n.setValue(s);
                     }
                 }
                 
             });
         }
 
         private ObjectNode findObjectParent() {
             return (ObjectNode)findAncestorOfType(ObjectNode.class);
         }
     }
     
     public static class StringNode extends Node {
         private static final NodeFactory[] LEGAL_CHILD_NODES = new NodeFactory[0];
         private static final NodeStatus CANNOT_BE_NULL = new NodeStatus(false, "string cannot be null");
         public StringNode(Node parent, String name, String value) {
             super(parent, name, value);
         }
         
         public NodeFactory[] legalChildNodes() {
             return LEGAL_CHILD_NODES;
         }
 
         public String getValueString() {
             
             return (String)getValue();
         }
 
         public boolean isValueChangeable() {
             return true;
         }
 
         public NodeStatus checkValueString(String s) {
             if (s == null) return new NodeStatus(false, "string cannot be null");
             return NodeStatus.OK;
         }
 
         public void onUpdate(String s) {
             setValue(s);
         }
 
         public NodeStatus checkNameString(String name) {
             return NodeStatus.OK;
         }
 
         public boolean isNameChangeable() {
             return false;
         }
 
         public boolean isDeletable() {
             return false;
         }
     }
     
     public static class EventHandlerNode extends Node {
 
         public EventHandlerNode(Node parent, String nodeName, Object value) {
             super(parent, nodeName, null);
         }
 
         public boolean hasValueChoices() { return true; }
         public String getChoicesId() {
             return "optional-module"; //$NON-NLS-1$
         }
         
         public String getNameDisplay() {
             return "Event Handler";
         }
         public NodeStatus checkNameString(String name) {
             return NodeStatus.OK;
         }
 
         public NodeStatus checkValueString(String s) {
             return NodeStatus.OK;
         }
 
         public String getValueString() {
             if (getValue() == null) {
                 return "(none)";
             }
             return getValue().toString();
         }
 
         public boolean isDeletable() {
             return false;
         }
 
         public boolean isNameChangeable() {
             return false;
         }
 
         public boolean isValueChangeable() {
             return true;
         }
 
         public NodeFactory[] legalChildNodes() {
             return EMPTY_FACTORY_LIST;
         }
 
         protected void onUpdate(String s) {
             if ("(none)".equals(s)) setValue(null);
             else setValue(s);
         }
         
     }
     
     public static class GridPositionNode extends Node implements HasDerivedValue {
         public GridPositionNode(Node parent, String nodeName) {
             super(parent, nodeName, null);
             
             boolean prim = parent instanceof PrimPropertiesNode;
             addChild(new GridCoordinateNode(this, "x", prim? 0:128));
             addChild(new GridCoordinateNode(this, "y", prim? 0:128));
             addChild(new GridCoordinateNode(this, "z", 0));
         }
 
         public NodeStatus checkNameString(String name) {
             return NodeStatus.OK;
         }
 
         public NodeStatus checkValueString(String s) {
             return NodeStatus.OK;
         }
 
         public String getNameDisplay() {
             return (String) ID_TO_DISPLAY.get(getName());
         }
         public String getValueString() {
             return null;
         }
 
         public boolean isDeletable() {
             return false;
         }
 
         public boolean isNameChangeable() {
             return false;
         }
 
         public boolean isValueChangeable() {
             return false;
         }
 
         public NodeFactory[] legalChildNodes() {
             return EMPTY_FACTORY_LIST;
         }
 
         public void onUpdate(String s) {
         }
     
         public LVector getVector() {
             GridCoordinateNode xnode = (GridCoordinateNode) this.findChildByName("x"); //$NON-NLS-1$
             GridCoordinateNode ynode = (GridCoordinateNode) this.findChildByName("y"); //$NON-NLS-1$
             GridCoordinateNode znode = (GridCoordinateNode) this.findChildByName("z"); //$NON-NLS-1$
             return new LVector(xnode.getFloatValue(), ynode.getFloatValue(), znode.getFloatValue());
         }
 
         public Object getDerivedValue() {
             return getVector();
         }
     }
     
     public static class RegionNode extends Node implements HasDerivedValue {
         private String displayName;
         public RegionNode(Node parent, String nodeName, String displayName) {
             super(parent, nodeName, null);
             this.displayName = displayName;
             addChild(new AnyNaturalNode(this,"x",0));
             addChild(new AnyNaturalNode(this,"y",0));
         }
         
         public String getNameDisplay() {
             return displayName;
         }
         
         public NodeStatus checkNameString(String name) {
             return NodeStatus.OK;
         }
         public NodeStatus checkValueString(String s) {
             return NodeStatus.OK;
         }
         public String getValueString() {
             return null;
         }
         public boolean isDeletable() {
             return false;
         }
         public boolean isNameChangeable() {
             return false;
         }
         public boolean isValueChangeable() {
             return false;
         }
         public NodeFactory[] legalChildNodes() {
             return EMPTY_FACTORY_LIST;
         }
         protected void onUpdate(String s) {
         }
         public Object getDerivedValue() {
             AnyNaturalNode xnode = (AnyNaturalNode) this.findChildByName("x");
             AnyNaturalNode ynode = (AnyNaturalNode) this.findChildByName("y");
             Integer x = (Integer) xnode.getValue();
             Integer y = (Integer) ynode.getValue();
             return new Region(x.intValue(), y.intValue());
         }
         
     }
     
     public static class ConstrainedFloatNode extends Node {
         private float min;
         private float max;
         private String displayName;
         public ConstrainedFloatNode(Node parent, String nodeName, Object value, float min, float max, String displayName) {
             super(parent, nodeName, value);
             this.min = min;
             this.max = max;
             this.displayName = displayName;
         }
 
         public NodeStatus checkNameString(String name) {
             return NodeStatus.OK;
         }
         public String getNameDisplay() {
             return displayName;
         }
         public NodeStatus checkValueString(String s) {
             try {
                 float f = Float.parseFloat(s);
                 
                 if (f < min || f > max) 
                     return new NodeStatus(false, "number is out of range (" + min + " - " + max + ")");
 ;
                 return NodeStatus.OK;
             } catch (NumberFormatException e) {
                 return SimProject.BAD_FORMAT;
             }
         }
         public String getValueString() {
             return getValue().toString();
         }
         public boolean isDeletable() {
             return false;
         }
         public boolean isNameChangeable() {
             return false;
         }
         public boolean isValueChangeable() {
             return true;
         }
         public NodeFactory[] legalChildNodes() {
             return EMPTY_FACTORY_LIST;
         }
         protected void onUpdate(String s) {
             try {
                 float f = Float.parseFloat(s);
                 setValue(new Float(f));
             } catch (NumberFormatException e) {
                 // ignore
             }
         }
     }
     
     public static class GridCoordinateNode extends Node {
         public static final int REGION_MAX = 256;
         private static final NodeStatus OUT_OF_RANGE = new NodeStatus(false, "number is out of range (0-256");
         private static final HashMap NAME_TO_DISPLAY = new HashMap();
         
         static {
             NAME_TO_DISPLAY.put("x", "X Coordinate");
             NAME_TO_DISPLAY.put("y", "Y Coordinate");
             NAME_TO_DISPLAY.put("z", "Z Coordinate");
         }
         
         public static float clipCoordinate(float val) {
             return (val < 0) ? 0 : (val > 256 ? 256 : val);
         }
         public GridCoordinateNode(Node parent, String name, float value) {
             super(parent, name, new Float(value));
         }
         
         public NodeFactory[] legalChildNodes() {
             return EMPTY_FACTORY_LIST;
         }
 
         public String getNameDisplay() {
             return (String) NAME_TO_DISPLAY.get(getName());
         }
         
         public String getValueString() {
             return getValue().toString();
         }
 
         
         public boolean isValueChangeable() {
             /* very hacky... */
             if (getParent() != null && getParent().getParent() != null &&
                 getParent().getParent() instanceof PrimPropertiesNode) {
                 PrimNode prim = (PrimNode) getParent().getParent().getParent();
                 return !prim.getParent().isFirstChildOfType(prim, PrimNode.class);
             }
             return true;
         }
 
         public NodeStatus checkValueString(String s) {
             try {
                 float f = Float.parseFloat(s);
                 
                 if (f < 0 || f > REGION_MAX) return OUT_OF_RANGE;
                 return NodeStatus.OK;
             } catch (NumberFormatException e) {
                 return SimProject.BAD_FORMAT;
             }
         }
 
         public void onUpdate(String s) {
             try {
                 float f = Float.parseFloat(s);
                 setValue(new Float(f));
             } catch (NumberFormatException e) {
                 // ignore
             }
         }
 
         public NodeStatus checkNameString(String name) {
             return NodeStatus.OK;
         }
 
         public boolean isNameChangeable() {
             return false;
         }
 
         public boolean isDeletable() {
             return false;
         }
         
         public float getFloatValue() {
             return ((Float)getValue()).floatValue();
         }
     }
 
     public static class AnyNaturalNode extends Node {
         private String displayName;
         private static final NodeFactory[] LEGAL_CHILD_NODES = new NodeFactory[0];
         private static final NodeStatus OUT_OF_RANGE = new NodeStatus(false, "Value is out of range (must be greater than 0)");
         public AnyNaturalNode(Node parent, String name, int value) {
             this(parent, name, value, name);
         }
         
         public AnyNaturalNode(Node parent, String name, int value, String displayName) {
             super(parent, name, new Integer(value));
             this.displayName = displayName;
         }
         
         public NodeFactory[] legalChildNodes() {
             return LEGAL_CHILD_NODES;
         }
 
         public NodeStatus checkNameString(String name) {
              return NodeStatus.OK;
         }
 
         public NodeStatus checkValueString(String s) {
             try {
                 int i = Integer.parseInt(s);
                 
                 if (i < 0) return OUT_OF_RANGE;
                 return NodeStatus.OK;
             } catch (NumberFormatException e) {
                 return SimProject.BAD_FORMAT;
             }
         }
 
         public String getValueString() {
             return getValue().toString();
         }
 
         public boolean isNameChangeable() {
             return false;
         }
 
         public boolean isValueChangeable() {
             return true;
         }
 
         public void onUpdate(String s) {
             try {
                 int i = Integer.parseInt(s);
                 setValue(new Integer(i));
             } catch (NumberFormatException e) {
                 // ignore
             }
         }
 
         public boolean isDeletable() {
             return false;
         }
         
     }
 
     protected static String computeNewName(List nodes, String prefix) {
         int index = 0;
         
         for (Iterator i = nodes.iterator(); i.hasNext();) {
             Node n = (Node) i.next();
             
             if (n.getNameDisplay().startsWith(prefix)) {
                 String tail = n.getNameDisplay().substring(prefix.length());
                 if (tail.trim().length() > 0) {
                     try {
                         int j = Integer.parseInt(tail.trim());
                         if (j >= index) index = j + 1;
                     } catch (NumberFormatException e) { }
                 } else {
                     if (index < 1) index = 1;
                 }
             }
         }
         
         String name = (index > 0) ? prefix + " " + index : prefix;
         return name;
     }
 
     static NodeStatus checkNameUnique(Node n, String name, List list, NodeStatus error) {
         for (Iterator i = list.iterator(); i.hasNext();) {
             Node node = (Node) i.next();
             if (node == n) continue;
             if (node.getNameDisplay() != null && node.getNameDisplay().equals(name)) return error;
         }
         return NodeStatus.OK;
     }
 
     static final NodeStatus BAD_FORMAT = new NodeStatus(false, "format of number is incorrect");
     private static XStream xstream = new XStream(new DomDriver());
     
     private static void configureXStream(XStream xstream) {
         Class[] nodeTypes = new Class[] {
                 WorldNode.class, AvatarNode.class, ObjectNode.class,
                 PrimNode.class, ScriptNode.class, GridCoordinateNode.class,
                 AnyNaturalNode.class, StringNode.class, DefaultAvatarNode.class,
                 NotecardNode.class, NotecardLineNode.class, InventoryPropertiesNode.class,
                 GridPositionNode.class, PrimPropertiesNode.class, AvatarPropertiesNode.class,
                 AvatarReferenceNode.class, GestureNode.class, ClothingNode.class,
                 BodyPartNode.class, SoundNode.class, AnimationNode.class, TextureNode.class,
                 LandmarkNode.class, EventHandlerNode.class, ObjectPropertiesNode.class
         };
         
         xstream.omitField(Node.class, "parent"); //$NON-NLS-1$
         xstream.omitField(Node.class, "children"); //$NON-NLS-1$
         xstream.omitField(Node.class, "listeners"); //$NON-NLS-1$
         for (int i = 0; i < nodeTypes.length; i++) {
             Class c = nodeTypes[i];
             String name = c.getSimpleName();
             name = name.substring(0, 1).toLowerCase() + name.substring(1);
             xstream.alias(name, c);
         }
         
         xstream.omitField(WorldNode.class, "resource"); //$NON-NLS-1$
     }
     
     static {
         configureXStream(xstream);
     }
     public static String toXml(WorldNode world) {
         world.syncChildren();
         return xstream.toXML(world);
     }
 
     public static WorldNode fromXml(InputStream contents, IFile file) {
         WorldNode n = (WorldNode) xstream.fromXML(contents);
         n.setResource(file);
         n.propagateParent();
         return n;
     }
     
     private static Object get(Map m, Object key, Object defaultVal) {
         Object v = m.get(key);
         if (v == null) v = defaultVal;
         return v;
     }
     
     public static SimWorldDef toSimWorldDef(WorldNode node) {
         AnyNaturalNode max = (AnyNaturalNode) node.findChildByName("max_time"); //$NON-NLS-1$
         Integer max_tick = (Integer) max.getValue();
         Node eNode = node.findChildByName("event-handler");
         String handler = (String) eNode.getValue();
         
         List avatarNodes = node.findChildrenByType(AvatarNode.class);
         
         Avatar[] avatarArray = new Avatar[avatarNodes.size()];
         
         int index = 0;
         for (Iterator i = avatarNodes.iterator(); i.hasNext();) {
             AvatarNode an = (AvatarNode) i.next();
             avatarArray[index++] = (Avatar) an.getDerivedValue();
         }
         
         List simObjectNodes = node.findChildrenByType(ObjectNode.class);
         LinkedList allPrims = new LinkedList();
         SimObject[] simObjects = new SimObject[simObjectNodes.size()];
         
         index = 0;
         for (Iterator i = simObjectNodes.iterator(); i.hasNext();) {
             ObjectNode on = (ObjectNode)i.next();
             simObjects[index++] = on.getSimObject(allPrims);
         }
         
         Prim[] primArray = (Prim[]) allPrims.toArray(new Prim[allPrims.size()]);
         return new SimWorldDef(max_tick.intValue(),1000, simObjects, primArray, avatarArray, handler);
     }
 
 }

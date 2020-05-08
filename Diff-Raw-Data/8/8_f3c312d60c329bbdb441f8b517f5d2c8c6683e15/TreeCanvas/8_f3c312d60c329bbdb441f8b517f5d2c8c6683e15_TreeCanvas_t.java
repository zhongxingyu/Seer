 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mapjvm;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import javax.swing.JComponent;
 import com.sun.jdi.*;
 import com.sun.jdi.connect.AttachingConnector;
 import com.sun.jdi.connect.Connector;
 import com.sun.jdi.connect.IllegalConnectorArgumentsException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 /**
  *
  * @author esoto.student
  */
 public final class TreeCanvas extends JComponent {
 
     ArrayList<StringTree> nodes = new ArrayList<StringTree>();
 
 
     //DefaultSize
     public TreeCanvas() {
         this.setSize(500,500);
         this.setPreferredSize(new Dimension(500,500));
         this.setBackground(Color.white);        
     }
 
     public void connect()throws IOException, IllegalConnectorArgumentsException, InterruptedException, IncompatibleThreadStateException, AbsentInformationException, ClassNotLoadedException  {
         HashMap<Value,StringTree> DFSLookup = new HashMap<Value, StringTree>();
         boolean one = false;
 
         StringTree start = new StringTree();
         //TODO: Value?
        // ArrayList<Value> seen = new ArrayList<Value> ();
 
         VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
         AttachingConnector socket = null;
         VirtualMachine vm = null;
         List<AttachingConnector> aclist = vmm.attachingConnectors();
         for (AttachingConnector attachingconnector : aclist)
         {
             if(attachingconnector.transport().name().equals("dt_socket"))
             {
                 socket = attachingconnector;
                 System.out.println("Connector Found");
                 break;
             }
         }
 
         Map parametersMap = socket.defaultArguments();
         Connector.IntegerArgument portArg = (Connector.IntegerArgument)parametersMap.get("port");
         System.out.println(portArg);
         portArg.setValue(8000);
         vm = socket.attach(parametersMap);
         vm.suspend();
         System.out.println("Attached to Process '" + vm.name() + "'");
 
         List<ThreadReference> ltr = vm.allThreads();
             for(ThreadReference thread_ref : ltr)
             {
                 if(thread_ref.name().equalsIgnoreCase("main"))
                 {
                     ThreadReference main_tr = thread_ref;
                     System.out.println(main_tr);
                     List<StackFrame> sf_list = main_tr.frames();
                     System.out.println(sf_list.size() + " Stack Frames");
 
                     for(StackFrame sf : sf_list)
                     {
 
 
                     System.out.println(sf.toString());
                     start = new StringTree("main", "StackFrame");
                     try {
                             List<LocalVariable> llv = sf.visibleVariables();
                             for(LocalVariable l : llv)
                             {
                                Value v= sf.getValue(l);
                                if(!DFSLookup.containsKey(v))
                                {
                                   dfs(v, start, DFSLookup);
 
                                }
                             }
                         }
                         catch (AbsentInformationException aie) {
                             System.out.println("No info for " + sf);
                         }
                     }
 
                 }
                }
                 nodes.add(start);
                 int x= (new GraphicsTree(nodes.get(0))).finalX +200;
 //                this.setPreferredSize(new Dimension(x,nodes.get(0).getNumChildren()*300));
                   this.setPreferredSize(new Dimension(20000,20000));
            }
 
 
     public void disconnect() {
         nodes.clear();
     }
 
     @Override
     //Need this function in order to paint on canvas
     public void paint(Graphics g) {
         //For loop that draws all shapes currently in the vector
         for (int i = 0; i < nodes.size(); i++) {
             StringTree s = nodes.get(i);
             (new GraphicsTree(s)).draw(g);
         }
     }
 
 
     private void dfs(Value v, StringTree parent,HashMap<Value,StringTree> Lookup ) throws ClassNotLoadedException
     {
         StringTree z ;
 
         if(v == null)
         {
             return;
         }
         if(Lookup.containsKey(v)) {
             //TODO: make additional Connection
             return;
         }
         if(v.type() instanceof PrimitiveType)
         {
             
             z = new StringTree(v.toString(), "value");
             Lookup.put(v,z);
             parent.addChild(z);
             return;
         }
         if(v instanceof ObjectReference)
         {
             //System.out.println("DFS");
             ObjectReference o = (ObjectReference) v;
             if(o.type() instanceof ClassType)
             {
                 ClassType ct = (ClassType) o.type();
                 if(ct.toString().startsWith("class java.lang"))
                {
                    int s = ct.name().toString().lastIndexOf(".") + 1;
                    
                    z = new StringTree(ct.name().substring(s), "value");
                    Lookup.put(v,z);
                    parent.addChild(z);
                     return;
                }
                 z = new StringTree(ct.name().substring(17), "Object");
                 Lookup.put(v,z);
                 parent.addChild(z);
                 for(Field f : ct.allFields())
                 {
                    // System.out.println("Hello " + f);
                     Value u = o.getValue(f);                 
                     dfs(u, z, Lookup);
                 }
             }
         }
     }
 
 }
 
 //Code graveyard
 //TODO : Delete this test code
        /* StringTree main = new StringTree("main", "StackFrame");
         StringTree BSTa = new StringTree("BST A", "Object");
         StringTree BSTb = new StringTree("BST B", "Object");
         StringTree chAL = new StringTree("child A Left", "Object");
         StringTree chBL = new StringTree("child B Left", "Object");
         StringTree chBR = new StringTree("child B Right", "Object");
         StringTree LL0 = new StringTree("Linked List (0)", "Object");
         StringTree LL1 = new StringTree("Linked List (1)", "Object");
         StringTree LL2 = new StringTree("Linked List (2)", "Object");
         main.addChild(BSTa);
         main.addChild(BSTb);
         main.addChild(LL0);
         LL0.addChild(LL1);
         LL1.addChild(LL2);
         BSTa.addChild(chAL);
         main.addConnection(BSTa, BSTb);
         BSTb.addChild(chBL);
         BSTb.addChild(chBR);
         nodes.add(main); */
 
         //TODO: add method to find canvas max size(of graphics tree)
         //this.setPreferredSize(new Dimension(nodes.get(0).getWidth(),nodes.get(0).getHeight()));

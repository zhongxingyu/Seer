 package shellderp.bcexplorer;
 
 import org.apache.bcel.Repository;
 import org.apache.bcel.classfile.*;
 import org.apache.bcel.generic.*;
 import shellderp.bcexplorer.ui.ClassTabPane;
 import shellderp.bcexplorer.ui.ClassTree;
 import shellderp.bcexplorer.ui.DefaultTreeContextMenuProvider;
 import shellderp.bcexplorer.ui.TreeContextMenuListener;
 
 import javax.swing.*;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.*;
 import java.util.*;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 /**
  * ClassHierarchy maintains the hierarchy of loaded classes, and provides a JTree
  * component to display the classes in the explorer.
  * <p/>
  * Created by: Mike
  * Date: 1/17/12
  * Time: 8:31 PM
  */
 public class ClassHierarchy {
     public final HashMap<String, Node<ClassGen>> classes = new HashMap<>();
     public final Node<ClassGen> rootClass;
     public final Node orphanNode;
     final HashMap<String, List<Node>> orphans = new HashMap<>();
 
     private DefaultTreeModel treeModel;
     private JTree tree;
 
     public ClassHierarchy(String rootClassName) throws ClassNotFoundException {
         // initialize the root class
         rootClass = new Node(new ClassGen(Repository.lookupClass(rootClassName)));
         rootClass.setDisplayText(rootClass.get().getClassName());
         classes.put(rootClass.get().getClassName(), rootClass);
 
         orphanNode = new Node("Orphans");
 
         treeModel = new DefaultTreeModel(rootClass);
     }
 
     public void loadClasses(List<ClassGen> loadList) {
         Queue<String> loadQueue = new LinkedList<String>();
         for (ClassGen cg : loadList) {
             String className = cg.getClassName();
             if (classes.containsKey(className)) {
                 System.err.println("WARNING: skipping already loaded class: " + className);
                 continue;
             }
             classes.put(className, new Node<ClassGen>(cg));
             loadQueue.add(className);
         }
 
         while (!loadQueue.isEmpty()) {
             String className = loadQueue.poll();
             Node<ClassGen> node = classes.get(className);
             node.setDisplayText(className);
             String superName = node.get().getSuperclassName();
 
             if (classes.containsKey(superName)) {
                 classes.get(superName).addChild(node);
             } else {
                 // super wasn't in list, attempt to load from repository
                 try {
                     ClassGen cg = new ClassGen(Repository.lookupClass(superName));
                     Node<ClassGen> superNode = new Node<ClassGen>(cg);
                     classes.put(superName, superNode);
                     loadQueue.add(superName);
                     superNode.addChild(node);
                 } catch (ClassNotFoundException e) {
                     System.err.println("WARNING: superclass missing: " + className);
 
                     addOrphan(superName, node);
                 }
             }
             
             List<Node> classOrphans = orphans.get(className);
             if (classOrphans != null) {
                 for (Node orphan : classOrphans) {
                     removeOrphan(orphan, node);
                 }
                 orphans.remove(className);
             }
         }
 
         rootClass.sortAll(); // TODO find a way to preserve order while inserting, instead of sorting afterwards
 
         treeModel.reload();
     }
 
     public void loadJarFile(JarFile jar) throws IOException {
         Enumeration<JarEntry> jarEntries = jar.entries();
         List<ClassGen> classes = new LinkedList<ClassGen>();
         while (jarEntries.hasMoreElements()) {
             JarEntry je = jarEntries.nextElement();
             if (je.getName().endsWith(".class")) {
                 ClassParser cp = new ClassParser(jar.getInputStream(je), je.getName());
                 classes.add(new ClassGen(cp.parse()));
             }
         }
         loadClasses(classes);
     }
 
     public void loadDirectory(File[] dirs) throws IOException {
         List<ClassGen> classes = new LinkedList<>();
 
         Stack<File> subDirs = new Stack<>();
         for (File dir : dirs) {
             subDirs.push(dir);
         }
 
         do {
             File[] list = subDirs.pop().listFiles();
             for (File file : list) {
                 if (file.isDirectory()) {
                     subDirs.push(file);
                 } else if (file.getName().endsWith(".class")) {
                     ClassParser cp = new ClassParser(new FileInputStream(file), file.getName());
                     classes.add(new ClassGen(cp.parse()));
                 } else if (file.getName().endsWith(".jar")) {
                     loadJarFile(new JarFile(file));
                 }
             }
         } while (!subDirs.empty());
 
         loadClasses(classes);
     }
 
     public void unloadClasses() {
         rootClass.removeAllChildren();
         orphanNode.removeAllChildren();
         orphanNode.changeParent(null);
 
         classes.clear();
         classes.put(rootClass.get().getClassName(), rootClass);
 
         treeModel.reload();
     }
     
     public void unloadClass(String name) {
         if (name.equals(rootClass.get().getClassName()))
             return;
 
         Node<ClassGen> node = classes.get(name);
         if (node == null)
             return;
 
         classes.remove(name);
         node.changeParent(null);
 
         List<Node<ClassGen>> children = new ArrayList<>(node.getChildren());
         for (Node<ClassGen> child : children) {
             addOrphan(name, child);
         }
 
         treeModel.reload();
 
         // TODO invalidate References (use weak references??)
     }
     
     private void addOrphan(String superName, Node node) {
         // add this class to the list of orphans with superclass superName
         List<Node> list = orphans.get(superName);
         if (list == null)
             orphans.put(superName, list = new ArrayList<>());
         list.add(node);
 
         // if the orphan node previously had no children, add it to the tree
         if (orphanNode.isLeaf()) {
             rootClass.addChild(orphanNode);
         }
         orphanNode.addChild(node);
 
         treeModel.reload();
     }
     
     private void removeOrphan(Node node, Node parent) {
         node.changeParent(parent);
         
         if (orphanNode.isLeaf()) {
             orphanNode.changeParent(null);
         }
 
         treeModel.reload();
     }
 
     public JTree getJTree(final ClassTabPane classTabPane) {
         if (tree != null)
             return tree;
         
         tree = new JTree(treeModel);
 
         tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 
         // double click opens the class, but we can still allow the user to expand the tree with a triple click
         tree.setToggleClickCount(3);
 
         // add a listener to open new tab when a class is double clicked
         tree.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                     Node node = (Node) tree.getLastSelectedPathComponent();
                     if (node != null && node.get() instanceof ClassGen)
                         classTabPane.openClassTab((ClassGen) node.get());
                 }
             }
         });
 
         tree.addMouseListener(new TreeContextMenuListener(new DefaultTreeContextMenuProvider() {
             @Override public JPopupMenu createContextMenu(JTree tree, TreePath path, Node node) {
                 JPopupMenu menu = super.createContextMenu(tree, path, node);
                 
                 if (node.get() instanceof ClassGen) {
                    menu.addSeparator();

                     ClassGen cg = (ClassGen) node.get();
                     ClassTree.addClassMenuItems(ClassHierarchy.this, classTabPane, menu, cg.getClassName());
                 }
                 
                 return menu;
             }
         }, tree));
 
         return tree;
     }
 
     /*
      * Searches for references in all classes in the hierarchy.
      */
     public List<Reference> findReferences(InstructionFilter filter) {
         List<Reference> refs = new ArrayList<>();
 
         for (Node<ClassGen> classNode : classes.values()) {
             refs.addAll(findReferences(classNode.get(), filter));
         }
 
         return refs;
     }
 
     public static List<Reference> findReferences(ClassGen visitClass, InstructionFilter filter) {
         ConstantPoolGen cpgen = visitClass.getConstantPool();
 
         List<Reference> refs = new ArrayList<>();
 
         // Visit every instruction of every method
         for (Method method : visitClass.getMethods()) {
             InstructionList list = new MethodGen(method, visitClass.getClassName(), cpgen).getInstructionList();
             if (list == null)
                 continue;
 
             for (InstructionHandle ih : list.getInstructionHandles()) {
                 Instruction instruction = ih.getInstruction();
 
                 if (filter.process(visitClass, method, instruction)) {
                     refs.add(new InstructionReference(visitClass, method, ih));
                 }
             }
         }
 
         return refs;
     }
 
     public List<Reference> findImplementingClasses(String interfaceName) {
         List<Reference> refs = new ArrayList<>();
 
         for (Node<ClassGen> classNode : classes.values()) {
             for (String iface : classNode.get().getInterfaceNames()) {
                 if (iface.equals(interfaceName)) {
                     refs.add(new Reference(classNode.get()));
                 }
             }
         }
 
         return refs;
     }
     
     public List<FieldOrMethodReference> findOverrides(String className, Method method) {
         if (!classes.containsKey(className))
             return null;
         
         return findOverrides(classes.get(className), method);
     }
 
     public List<FieldOrMethodReference> findOverrides(Node<ClassGen> cgNode, Method method) {
         List<FieldOrMethodReference> overrides = new ArrayList<>();
         String targetName = method.getName();
         String targetSig = method.getSignature();
         for (Node<ClassGen> child : cgNode) {
             if (!child.isLeaf())
                 overrides.addAll(findOverrides(child, method));
            for (Method m : child.get().getMethods()) {
                 if (targetName.equals(m.getName()) && targetSig.equals(m.getSignature())) {
                     overrides.add(new FieldOrMethodReference(child.get(), m));
                     break;
                 }
             }
         }
         return overrides;
     }
 
     public Node<ClassGen> findSuperDeclaration(ClassGen cg, Method method) {
         Node<ClassGen> node = classes.get(cg.getClassName());
         Node<ClassGen> superNode = node.getParent();
         if (superNode == null)
             return null;
 
         for (Method m : superNode.get().getMethods()) {
             if (m.equals(method)) {
                 return superNode;
             }
         }
 
         return findSuperDeclaration(superNode.get(), method);
     }
     
     public FieldOrMethodReference findFieldOrMethod(String className, String name, String signature) {
         Node<ClassGen> node = classes.get(className);
         ClassGen classGen = node.get();
 
         Method method = classGen.containsMethod(name, signature);
         if (method != null)
             return new FieldOrMethodReference(classGen, method);
         
         for (Field field : classGen.getFields()) {
             if (field.getName().equals(name) && field.getSignature().equals(signature))
                 return new FieldOrMethodReference(classGen, field);
         }
 
         Node<ClassGen> superNode = node.getParent();
         if (superNode == null)
             return null;
 
         return findFieldOrMethod(superNode.get().getClassName(), name, signature);
     }
 
 }

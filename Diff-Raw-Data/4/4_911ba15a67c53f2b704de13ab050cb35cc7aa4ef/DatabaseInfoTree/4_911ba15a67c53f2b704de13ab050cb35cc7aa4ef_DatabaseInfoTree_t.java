 package net.argius.stew.ui.window;
 
 import static java.awt.EventQueue.invokeLater;
 import static java.awt.event.InputEvent.ALT_DOWN_MASK;
 import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
 import static java.awt.event.KeyEvent.VK_C;
 import static java.util.Collections.emptyList;
 import static java.util.Collections.nCopies;
 import static javax.swing.KeyStroke.getKeyStroke;
 import static net.argius.stew.text.TextUtilities.join;
 import static net.argius.stew.ui.window.AnyActionKey.copy;
 import static net.argius.stew.ui.window.AnyActionKey.refresh;
 import static net.argius.stew.ui.window.DatabaseInfoTree.ActionKey.*;
 import static net.argius.stew.ui.window.WindowOutputProcessor.showInformationMessageDialog;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 
 import net.argius.stew.*;
 
 /**
  * The Database Information Tree is a tree pane that provides to
  * display database object information from DatabaseMetaData.
  */
 final class DatabaseInfoTree extends JTree implements AnyActionListener, TextSearch {
 
     enum ActionKey {
         copySimpleName,
         copyFullName,
         generateWherePhrase,
         generateSelectPhrase,
         generateUpdateStatement,
         generateInsertStatement,
         jumpToColumnByName,
         toggleShowColumnNumber
     }
 
     private static final Logger log = Logger.getLogger(DatabaseInfoTree.class);
     private static final ResourceManager res = ResourceManager.getInstance(DatabaseInfoTree.class);
 
     static volatile boolean showColumnNumber;
 
     private Connector currentConnector;
     private DatabaseMetaData dbmeta;
     private AnyActionListener anyActionListener;
 
     DatabaseInfoTree(AnyActionListener anyActionListener) {
         this.anyActionListener = anyActionListener;
         setRootVisible(false);
         setShowsRootHandles(false);
         setScrollsOnExpand(true);
         setCellRenderer(new Renderer());
         setModel(new DefaultTreeModel(null));
         // [Events]
         int sckm = Utilities.getMenuShortcutKeyMask();
         AnyAction aa = new AnyAction(this);
         aa.bindKeyStroke(false, copy, KeyStroke.getKeyStroke(VK_C, sckm));
         aa.bindSelf(copySimpleName, getKeyStroke(VK_C, sckm | ALT_DOWN_MASK));
         aa.bindSelf(copyFullName, getKeyStroke(VK_C, sckm | SHIFT_DOWN_MASK));
     }
 
     @Override
     protected void processMouseEvent(MouseEvent e) {
         super.processMouseEvent(e);
         if (e.getID() == MouseEvent.MOUSE_CLICKED && e.getClickCount() % 2 == 0) {
             anyActionPerformed(new AnyActionEvent(this, jumpToColumnByName));
         }
     }
 
     @Override
     public void anyActionPerformed(AnyActionEvent ev) {
         log.atEnter("anyActionPerformed", ev);
         if (ev.isAnyOf(copySimpleName)) {
             copySimpleName();
         } else if (ev.isAnyOf(copyFullName)) {
             copyFullName();
         } else if (ev.isAnyOf(refresh)) {
             for (TreePath path : getSelectionPaths()) {
                 refresh((InfoNode)path.getLastPathComponent());
             }
         } else if (ev.isAnyOf(generateWherePhrase)) {
             List<ColumnNode> columnNodes = new ArrayList<ColumnNode>();
             for (TreeNode node : getSelectionNodes()) {
                 if (node instanceof ColumnNode) {
                     columnNodes.add((ColumnNode)node);
                 }
             }
             final String phrase = generateEquivalentJoinClause(columnNodes);
             if (phrase.length() > 0) {
                 insertTextIntoTextArea(addCommas(phrase));
             }
         } else if (ev.isAnyOf(generateSelectPhrase)) {
             final String phrase = generateSelectPhrase(getSelectionNodes());
             if (phrase.length() > 0) {
                 insertTextIntoTextArea(phrase + " WHERE ");
             }
         } else if (ev.isAnyOf(generateUpdateStatement, generateInsertStatement)) {
             final boolean isInsert = ev.isAnyOf(generateInsertStatement);
             try {
                 final String phrase = generateUpdateOrInsertPhrase(getSelectionNodes(), isInsert);
                 if (phrase.length() > 0) {
                     if (isInsert) {
                         insertTextIntoTextArea(addCommas(phrase));
                     } else {
                         insertTextIntoTextArea(phrase + " WHERE ");
                     }
                 }
             } catch (IllegalArgumentException ex) {
                 showInformationMessageDialog(this, ex.getMessage(), "");
             }
         } else if (ev.isAnyOf(jumpToColumnByName)) {
             jumpToColumnByName();
         } else if (ev.isAnyOf(toggleShowColumnNumber)) {
             showColumnNumber = !showColumnNumber;
             repaint();
         } else {
             log.warn("not expected: Event=%s", ev);
         }
         log.atExit("anyActionPerformed");
     }
 
     @Override
     public TreePath[] getSelectionPaths() {
         TreePath[] a = super.getSelectionPaths();
         if (a == null) {
             return new TreePath[0];
         }
         return a;
     }
 
     List<TreeNode> getSelectionNodes() {
         List<TreeNode> a = new ArrayList<TreeNode>();
         for (TreePath path : getSelectionPaths()) {
             a.add((TreeNode)path.getLastPathComponent());
         }
         return a;
     }
 
     private void insertTextIntoTextArea(String s) {
         AnyActionEvent ev = new AnyActionEvent(this, ConsoleTextArea.ActionKey.insertText, s);
         anyActionListener.anyActionPerformed(ev);
     }
 
     private void copySimpleName() {
         TreePath[] paths = getSelectionPaths();
         if (paths == null || paths.length == 0) {
             return;
         }
         List<String> names = new ArrayList<String>(paths.length);
         for (TreePath path : paths) {
             if (path == null) {
                 continue;
             }
             Object o = path.getLastPathComponent();
             assert o instanceof InfoNode;
             final String name;
             if (o instanceof ColumnNode) {
                 name = ((ColumnNode)o).getName();
             } else if (o instanceof TableNode) {
                 name = ((TableNode)o).getName();
             } else {
                 name = o.toString();
             }
             names.add(name);
         }
         ClipboardHelper.setStrings(names);
     }
 
     private void copyFullName() {
         TreePath[] paths = getSelectionPaths();
         if (paths == null || paths.length == 0) {
             return;
         }
         List<String> names = new ArrayList<String>(paths.length);
         for (TreePath path : paths) {
             if (path == null) {
                 continue;
             }
             Object o = path.getLastPathComponent();
             assert o instanceof InfoNode;
             names.add(((InfoNode)o).getNodeFullName());
         }
         ClipboardHelper.setStrings(names);
     }
 
     private void jumpToColumnByName() {
         TreePath[] paths = getSelectionPaths();
         if (paths == null || paths.length == 0) {
             return;
         }
         final TreePath path = paths[0];
         Object o = path.getLastPathComponent();
         if (o instanceof ColumnNode) {
             ColumnNode node = (ColumnNode)o;
             AnyActionEvent ev = new AnyActionEvent(this,
                                                    ResultSetTable.ActionKey.jumpToColumn,
                                                    node.getName());
             anyActionListener.anyActionPerformed(ev);
         }
     }
 
     private static String addCommas(String phrase) {
         int c = 0;
         for (final char ch : phrase.toCharArray()) {
             if (ch == '?') {
                 ++c;
             }
         }
        if (c >= 1) {
            return String.format("%s;%s", phrase, join("", nCopies(c - 1, ",")));
         }
         return phrase;
     }
 
     static String generateEquivalentJoinClause(List<ColumnNode> nodes) {
         if (nodes.isEmpty()) {
             return "";
         }
         Set<String> tableNames = new LinkedHashSet<String>();
         ListMap columnMap = new ListMap();
         for (ColumnNode node : nodes) {
             final String tableName = node.getTableNode().getNodeFullName();
             final String columnName = node.getName();
             tableNames.add(tableName);
             columnMap.add(columnName, String.format("%s.%s", tableName, columnName));
         }
         assert tableNames.size() >= 1;
         List<String> expressions = new ArrayList<String>();
         if (tableNames.size() == 1) {
             for (ColumnNode node : nodes) {
                 expressions.add(String.format("%s=?", node.getName()));
             }
         } else { // size >= 2
             List<String> expressions2 = new ArrayList<String>();
             for (Entry<String, List<String>> entry : columnMap.entrySet()) {
                 List<String> a = entry.getValue();
                 final int n = a.size();
                 assert n >= 1;
                 expressions2.add(String.format("%s=?", a.get(0)));
                 if (n >= 2) {
                     for (int i = 0; i < n; i++) {
                         for (int j = i + 1; j < n; j++) {
                             expressions.add(String.format("%s=%s", a.get(i), a.get(j)));
                         }
                     }
                 }
             }
             expressions.addAll(expressions2);
         }
         return String.format("%s", join(" AND ", expressions));
     }
 
     static String generateSelectPhrase(List<TreeNode> nodes) {
         Set<String> tableNames = new LinkedHashSet<String>();
         ListMap columnMap = new ListMap();
         for (TreeNode node : nodes) {
             if (node instanceof TableNode) {
                 final String tableFullName = ((TableNode)node).getNodeFullName();
                 tableNames.add(tableFullName);
                 columnMap.add(tableFullName);
             } else if (node instanceof ColumnNode) {
                 ColumnNode cn = (ColumnNode)node;
                 final String tableFullName = cn.getTableNode().getNodeFullName();
                 tableNames.add(tableFullName);
                 columnMap.add(tableFullName, cn.getNodeFullName());
             }
         }
         if (tableNames.isEmpty()) {
             return "";
         }
         List<String> columnNames = new ArrayList<String>();
         if (tableNames.size() == 1) {
             List<String> a = new ArrayList<String>();
             for (TreeNode node : nodes) {
                 if (node instanceof ColumnNode) {
                     ColumnNode cn = (ColumnNode)node;
                     a.add(cn.getName());
                 }
             }
             if (a.isEmpty()) {
                 columnNames.add("*");
             } else {
                 columnNames.addAll(a);
             }
         } else { // size >= 2
             for (Entry<String, List<String>> entry : columnMap.entrySet()) {
                 final List<String> columnsInTable = entry.getValue();
                 if (columnsInTable.isEmpty()) {
                     columnNames.add(entry.getKey() + ".*");
                 } else {
                     columnNames.addAll(columnsInTable);
                 }
             }
         }
         return String.format("SELECT %s FROM %s", join(", ", columnNames), join(", ", tableNames));
     }
 
     static String generateUpdateOrInsertPhrase(List<TreeNode> nodes, boolean isInsert) {
         Set<String> tableNames = new LinkedHashSet<String>();
         ListMap columnMap = new ListMap();
         for (TreeNode node : nodes) {
             if (node instanceof TableNode) {
                 final String tableFullName = ((TableNode)node).getNodeFullName();
                 tableNames.add(tableFullName);
                 columnMap.add(tableFullName);
             } else if (node instanceof ColumnNode) {
                 ColumnNode cn = (ColumnNode)node;
                 final String tableFullName = cn.getTableNode().getNodeFullName();
                 tableNames.add(tableFullName);
                 columnMap.add(tableFullName, cn.getName());
             }
         }
         if (tableNames.isEmpty()) {
             return "";
         }
         if (tableNames.size() >= 2) {
             throw new IllegalArgumentException(res.get("e.enables-select-just-1-table"));
         }
         final String tableName = join("", tableNames);
         List<String> columnsInTable = columnMap.get(tableName);
         if (columnsInTable.isEmpty()) {
             if (isInsert) {
                 List<TableNode> tableNodes = new ArrayList<TableNode>();
                 for (TreeNode node : nodes) {
                     if (node instanceof TableNode) {
                         tableNodes.add((TableNode)node);
                         break;
                     }
                 }
                 TableNode tableNode = tableNodes.get(0);
                 if (tableNode.getChildCount() == 0) {
                     throw new IllegalArgumentException(res.get("i.can-only-use-after-tablenode-expanded"));
                 }
                 for (int i = 0, n = tableNode.getChildCount(); i < n; i++) {
                     ColumnNode child = (ColumnNode)tableNode.getChildAt(i);
                     columnsInTable.add(child.getName());
                 }
             } else {
                 return "";
             }
         }
         final String phrase;
         if (isInsert) {
             final int columnCount = columnsInTable.size();
             phrase = String.format("INSERT INTO %s (%s) VALUES (%s)",
                                    tableName,
                                    join(",", columnsInTable),
                                    join(",", nCopies(columnCount, "?")));
         } else {
             List<String> columnExpressions = new ArrayList<String>();
             for (final String columnName : columnsInTable) {
                 columnExpressions.add(columnName + "=?");
             }
             phrase = String.format("UPDATE %s SET %s", tableName, join(", ", columnExpressions));
         }
         return phrase;
     }
 
     // text-search
 
     @Override
     public boolean search(Matcher matcher) {
         return search(resolveTargetPath(getSelectionPath()), matcher);
     }
 
     private static TreePath resolveTargetPath(TreePath path) {
         if (path != null) {
             TreePath parent = path.getParentPath();
             if (parent != null) {
                 return parent;
             }
         }
         return path;
     }
 
     private boolean search(TreePath path, Matcher matcher) {
         if (path == null) {
             return false;
         }
         TreeNode node = (TreeNode)path.getLastPathComponent();
         if (node == null) {
             return false;
         }
         boolean found = false;
         found = matcher.find(node.toString());
         if (found) {
             addSelectionPath(path);
         } else {
             removeSelectionPath(path);
         }
         if (!node.isLeaf() && node.getChildCount() >= 0) {
             @SuppressWarnings("unchecked")
             Iterable<DefaultMutableTreeNode> children = Collections.list(node.children());
             for (DefaultMutableTreeNode child : children) {
                 if (search(path.pathByAddingChild(child), matcher)) {
                     found = true;
                 }
             }
         }
         return found;
     }
 
     @Override
     public void reset() {
         // empty
     }
 
     // node expansion
 
     /**
      * Refreshes the root and its children.
      * @param env Environment
      * @throws SQLException
      */
     void refreshRoot(Environment env) throws SQLException {
         Connector c = env.getCurrentConnector();
         if (c == null) {
             if (log.isDebugEnabled()) {
                 log.debug("not connected");
             }
             currentConnector = null;
             return;
         }
         if (c == currentConnector && getModel().getRoot() != null) {
             if (log.isDebugEnabled()) {
                 log.debug("not changed");
             }
             return;
         }
         if (log.isDebugEnabled()) {
             log.debug("updating");
         }
         // initializing models
         ConnectorNode connectorNode = new ConnectorNode(c.getName());
         DefaultTreeModel model = new DefaultTreeModel(connectorNode);
         setModel(model);
         final DefaultTreeSelectionModel m = new DefaultTreeSelectionModel();
         m.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
         setSelectionModel(m);
         // initializing nodes
         final DatabaseMetaData dbmeta = env.getCurrentConnection().getMetaData();
         final Set<InfoNode> createdStatusSet = new HashSet<InfoNode>();
         expandNode(connectorNode, dbmeta);
         createdStatusSet.add(connectorNode);
         // events
         addTreeWillExpandListener(new TreeWillExpandListener() {
             @Override
             public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                 TreePath path = event.getPath();
                 final Object lastPathComponent = path.getLastPathComponent();
                 if (!createdStatusSet.contains(lastPathComponent)) {
                     InfoNode node = (InfoNode)lastPathComponent;
                     if (node.isLeaf()) {
                         return;
                     }
                     createdStatusSet.add(node);
                     try {
                         expandNode(node, dbmeta);
                     } catch (SQLException ex) {
                         throw new RuntimeException(ex);
                     }
                 }
             }
             @Override
             public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                 // ignore
             }
         });
         this.dbmeta = dbmeta;
         // showing
         model.reload();
         setRootVisible(true);
         this.currentConnector = c;
         // auto-expansion
         try {
             File confFile = Bootstrap.getSystemFile("autoexpansion.tsv");
             if (confFile.exists() && confFile.length() > 0) {
                 AnyAction aa = new AnyAction(this);
                 Scanner r = new Scanner(confFile);
                 try {
                     while (r.hasNextLine()) {
                         final String line = r.nextLine();
                         if (line.matches("^\\s*#.*")) {
                             continue;
                         }
                         aa.doParallel("expandNodes", Arrays.asList(line.split("\t")));
                     }
                 } finally {
                     r.close();
                 }
             }
         } catch (IOException ex) {
             throw new IllegalStateException(ex);
         }
     }
 
     void expandNodes(List<String> a) {
         long startTime = System.currentTimeMillis();
         AnyAction aa = new AnyAction(this);
         int index = 1;
         while (index < a.size()) {
             final String s = a.subList(0, index + 1).toString();
             for (int i = 0, n = getRowCount(); i < n; i++) {
                 TreePath target;
                 try {
                     target = getPathForRow(i);
                 } catch (IndexOutOfBoundsException ex) {
                     // FIXME when IndexOutOfBoundsException was thrown at expandNodes
                     log.warn(ex);
                     break;
                 }
                 if (target != null && target.toString().equals(s)) {
                     if (!isExpanded(target)) {
                         aa.doLater("expandLater", target);
                         Utilities.sleep(200L);
                     }
                     index++;
                     break;
                 }
             }
             if (System.currentTimeMillis() - startTime > 5000L) {
                 break; // timeout
             }
         }
     }
 
     // called by expandNodes
     @SuppressWarnings("unused")
     private void expandLater(TreePath parent) {
         expandPath(parent);
     }
 
     /**
      * Refreshes a node and its children.
      * @param node
      */
     void refresh(InfoNode node) {
         if (dbmeta == null) {
             return;
         }
         node.removeAllChildren();
         final DefaultTreeModel model = (DefaultTreeModel)getModel();
         model.reload(node);
         try {
             expandNode(node, dbmeta);
         } catch (SQLException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     /**
      * Expands a node.
      * @param parent
      * @param dbmeta
      * @throws SQLException
      */
     void expandNode(final InfoNode parent, final DatabaseMetaData dbmeta) throws SQLException {
         if (parent.isLeaf()) {
             return;
         }
         final DefaultTreeModel model = (DefaultTreeModel)getModel();
         final InfoNode tmpNode = new InfoNode(res.get("i.paren-in-processing")) {
             @Override
             protected List<InfoNode> createChildren(DatabaseMetaData dbmeta) throws SQLException {
                 return Collections.emptyList();
             }
             @Override
             public boolean isLeaf() {
                 return true;
             }
         };
         invokeLater(new Runnable() {
             @Override
             public void run() {
                 model.insertNodeInto(tmpNode, parent, 0);
             }
         });
         // asynchronous
         DaemonThreadFactory.execute(new Runnable() {
             @Override
             public void run() {
                 try {
                     final List<InfoNode> children = new ArrayList<InfoNode>(parent.createChildren(dbmeta));
                     invokeLater(new Runnable() {
                         @Override
                         public void run() {
                             for (InfoNode child : children) {
                                 model.insertNodeInto(child, parent, parent.getChildCount());
                             }
                             model.removeNodeFromParent(tmpNode);
                         }
                     });
                 } catch (SQLException ex) {
                     throw new RuntimeException(ex);
                 }
             }
         });
     }
 
     /**
      * Clears (root).
      */
     void clear() {
         for (TreeWillExpandListener listener : getListeners(TreeWillExpandListener.class).clone()) {
             removeTreeWillExpandListener(listener);
         }
         setModel(new DefaultTreeModel(null));
         currentConnector = null;
         dbmeta = null;
         if (log.isDebugEnabled()) {
             log.debug("cleared");
         }
     }
 
     // subclasses
 
     private static final class ListMap extends LinkedHashMap<String, List<String>> {
 
         ListMap() {
             // empty
         }
 
         void add(String key, String... values) {
             if (get(key) == null) {
                 put(key, new ArrayList<String>());
             }
             for (String value : values) {
                 get(key).add(value);
             }
         }
 
     }
 
     private static class Renderer extends DefaultTreeCellRenderer {
 
         Renderer() {
             // empty
         }
 
         @Override
         public Component getTreeCellRendererComponent(JTree tree,
                                                       Object value,
                                                       boolean sel,
                                                       boolean expanded,
                                                       boolean leaf,
                                                       int row,
                                                       boolean hasFocus) {
             super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
             if (value instanceof InfoNode) {
                 setIcon(Utilities.getImageIcon(((InfoNode)value).getIconName()));
             }
             if (value instanceof ColumnNode) {
                 if (showColumnNumber) {
                     TreePath path = tree.getPathForRow(row);
                     if (path != null) {
                         TreePath parent = path.getParentPath();
                         if (parent != null) {
                             final int index = row - tree.getRowForPath(parent);
                             setText(String.format("%d %s", index, getText()));
                         }
                     }
                 }
             }
             return this;
         }
 
     }
 
     private abstract static class InfoNode extends DefaultMutableTreeNode {
 
         InfoNode(Object userObject) {
             super(userObject, true);
         }
 
         @Override
         public boolean isLeaf() {
             return false;
         }
 
         abstract protected List<InfoNode> createChildren(DatabaseMetaData dbmeta) throws SQLException;
 
         String getIconName() {
             final String className = getClass().getName();
             final String nodeType = className.replaceFirst(".+?([^\\$]+)Node$", "$1");
             return "node-" + nodeType.toLowerCase() + ".png";
         }
 
         protected String getNodeFullName() {
             return String.valueOf(userObject);
         }
 
         static List<TableTypeNode> getTableTypeNodes(DatabaseMetaData dbmeta,
                                                      String catalog,
                                                      String schema) throws SQLException {
             List<TableTypeNode> a = new ArrayList<TableTypeNode>();
             ResultSet rs = dbmeta.getTableTypes();
             try {
                 while (rs.next()) {
                     TableTypeNode typeNode = new TableTypeNode(catalog, schema, rs.getString(1));
                     if (typeNode.hasItems(dbmeta)) {
                         a.add(typeNode);
                     }
                 }
             } finally {
                 rs.close();
             }
             if (a.isEmpty()) {
                 a.add(new TableTypeNode(catalog, schema, "TABLE"));
             }
             return a;
         }
 
     }
 
     private static class ConnectorNode extends InfoNode {
 
         ConnectorNode(String name) {
             super(name);
         }
 
         @Override
         protected List<InfoNode> createChildren(DatabaseMetaData dbmeta) throws SQLException {
             List<InfoNode> a = new ArrayList<InfoNode>();
             if (dbmeta.supportsCatalogsInDataManipulation()) {
                 ResultSet rs = dbmeta.getCatalogs();
                 try {
                     while (rs.next()) {
                         a.add(new CatalogNode(rs.getString(1)));
                     }
                 } finally {
                     rs.close();
                 }
             } else if (dbmeta.supportsSchemasInDataManipulation()) {
                 ResultSet rs = dbmeta.getSchemas();
                 try {
                     while (rs.next()) {
                         a.add(new SchemaNode(null, rs.getString(1)));
                     }
                 } finally {
                     rs.close();
                 }
             } else {
                 a.addAll(getTableTypeNodes(dbmeta, null, null));
             }
             return a;
         }
 
     }
 
     private static final class CatalogNode extends InfoNode {
 
         private final String name;
 
         CatalogNode(String name) {
             super(name);
             this.name = name;
         }
 
         @Override
         protected List<InfoNode> createChildren(DatabaseMetaData dbmeta) throws SQLException {
             List<InfoNode> a = new ArrayList<InfoNode>();
             if (dbmeta.supportsSchemasInDataManipulation()) {
                 ResultSet rs = dbmeta.getSchemas();
                 try {
                     while (rs.next()) {
                         a.add(new SchemaNode(name, rs.getString(1)));
                     }
                 } finally {
                     rs.close();
                 }
             } else {
                 a.addAll(getTableTypeNodes(dbmeta, name, null));
             }
             return a;
         }
 
     }
 
     private static final class SchemaNode extends InfoNode {
 
         private final String catalog;
         private final String schema;
 
         SchemaNode(String catalog, String schema) {
             super(schema);
             this.catalog = catalog;
             this.schema = schema;
         }
 
         @Override
         protected List<InfoNode> createChildren(DatabaseMetaData dbmeta) throws SQLException {
             List<InfoNode> a = new ArrayList<InfoNode>();
             a.addAll(getTableTypeNodes(dbmeta, catalog, schema));
             return a;
         }
 
     }
 
     private static final class TableTypeNode extends InfoNode {
 
         private static final String ICON_NAME_FORMAT = "node-tabletype-%s.png";
 
         private final String catalog;
         private final String schema;
         private final String tableType;
 
         TableTypeNode(String catalog, String schema, String tableType) {
             super(tableType);
             this.catalog = catalog;
             this.schema = schema;
             this.tableType = tableType;
         }
 
         @Override
         protected List<InfoNode> createChildren(DatabaseMetaData dbmeta) throws SQLException {
             List<InfoNode> a = new ArrayList<InfoNode>();
             ResultSet rs = dbmeta.getTables(catalog, schema, null, new String[]{tableType});
             try {
                 while (rs.next()) {
                     final String table = rs.getString(3);
                     final String type = rs.getString(4);
                     final boolean kindOfTable = type.matches("TABLE|VIEW|SYNONYM");
                     a.add(new TableNode(catalog, schema, table, kindOfTable));
                 }
             } finally {
                 rs.close();
             }
             return a;
         }
 
         @Override
         String getIconName() {
             final String name = String.format(ICON_NAME_FORMAT, getUserObject());
             if (getClass().getResource("icon/" + name) == null) {
                 return String.format(ICON_NAME_FORMAT, "");
             }
             return name;
         }
 
         boolean hasItems(DatabaseMetaData dbmeta) throws SQLException {
             ResultSet rs = dbmeta.getTables(catalog, schema, null, new String[]{tableType});
             try {
                 return rs.next();
             } finally {
                 rs.close();
             }
         }
 
     }
 
     static final class TableNode extends InfoNode {
 
         private final String catalog;
         private final String schema;
         private final String name;
         private final boolean kindOfTable;
 
         TableNode(String catalog, String schema, String name, boolean kindOfTable) {
             super(name);
             this.catalog = catalog;
             this.schema = schema;
             this.name = name;
             this.kindOfTable = kindOfTable;
         }
 
         @Override
         protected List<InfoNode> createChildren(DatabaseMetaData dbmeta) throws SQLException {
             List<InfoNode> a = new ArrayList<InfoNode>();
             ResultSet rs = dbmeta.getColumns(catalog, schema, name, null);
             try {
                 while (rs.next()) {
                     a.add(new ColumnNode(rs.getString(4),
                                          rs.getString(6),
                                          rs.getInt(7),
                                          rs.getString(18),
                                          this));
                 }
             } finally {
                 rs.close();
             }
             return a;
         }
 
         @Override
         public boolean isLeaf() {
             return false;
         }
 
         @Override
         protected String getNodeFullName() {
             List<String> a = new ArrayList<String>();
             if (catalog != null) {
                 a.add(catalog);
             }
             if (schema != null) {
                 a.add(schema);
             }
             a.add(name);
             return join(".", a);
         }
 
         String getName() {
             return name;
         }
 
         boolean isKindOfTable() {
             return kindOfTable;
         }
 
     }
 
     static final class ColumnNode extends InfoNode {
 
         private final String name;
         private final TableNode tableNode;
 
         ColumnNode(String name, String type, int size, String nulls, TableNode tableNode) {
             super(format(name, type, size, nulls));
             setAllowsChildren(false);
             this.name = name;
             this.tableNode = tableNode;
         }
 
         String getName() {
             return name;
         }
 
         TableNode getTableNode() {
             return tableNode;
         }
 
         private static String format(String name, String type, int size, String nulls) {
             final String nonNull = "NO".equals(nulls) ? " NOT NULL" : "";
             return String.format("%s [%s(%d)%s]", name, type, size, nonNull);
         }
 
         @Override
         public boolean isLeaf() {
             return true;
         }
 
         @Override
         protected List<InfoNode> createChildren(DatabaseMetaData dbmeta) throws SQLException {
             return emptyList();
         }
 
         @Override
         protected String getNodeFullName() {
             return String.format("%s.%s", tableNode.getNodeFullName(), name);
         }
 
     }
 
 }

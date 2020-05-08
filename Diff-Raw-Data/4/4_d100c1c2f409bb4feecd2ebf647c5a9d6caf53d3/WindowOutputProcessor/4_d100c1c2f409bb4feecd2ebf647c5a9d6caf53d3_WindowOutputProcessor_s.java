 package net.argius.stew.ui.window;
 
 import static javax.swing.JOptionPane.*;
 import static net.argius.stew.Bootstrap.getPropertyAsInt;
 import static net.argius.stew.ui.window.AnyActionKey.*;
 import static net.argius.stew.ui.window.Utilities.getImageIcon;
 import static net.argius.stew.ui.window.Utilities.sleep;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.table.*;
 
 import net.argius.stew.*;
 import net.argius.stew.io.*;
 import net.argius.stew.ui.*;
 
 /**
  * The OutputProcessor Implementation for GUI(Swing).
  */
 final class WindowOutputProcessor extends JFrame implements OutputProcessor, AnyActionListener {
 
     private static final Logger log = Logger.getLogger(WindowOutputProcessor.class);
     private static final ResourceManager res = ResourceManager.getInstance(WindowOutputProcessor.class);
 
     private final AnyAction invoker;
     private final WindowLauncher launcher;
     private final ResultSetTable resultSetTable;
     private final ConsoleTextArea textArea;
 
     private Environment env;
     private File currentDirectory;
     private String postProcessMode;
 
     WindowOutputProcessor(WindowLauncher launcher,
                           ResultSetTable resultSetTable,
                           ConsoleTextArea textArea) {
         this.launcher = launcher;
         this.resultSetTable = resultSetTable;
         this.textArea = textArea;
         this.invoker = new AnyAction(this);
     }
 
     @Override
     public void output(final Object o) {
         try {
             if (o instanceof ResultSet) {
                 outputResult(new ResultSetReference((ResultSet)o, ""));
                 return;
             } else if (o instanceof ResultSetReference) {
                 outputResult((ResultSetReference)o);
                 return;
             }
         } catch (SQLException ex) {
             throw new RuntimeException("WindowOutputProcessor", ex);
         }
         final String message;
         if (o instanceof Prompt) {
             message = o.toString();
         } else {
             message = String.format("%s%n", o);
         }
         AnyAction aa4text = new AnyAction(textArea);
         AnyActionEvent ev = new AnyActionEvent(this,
                                                ConsoleTextArea.ActionKey.outputMessage,
                                                replaceEOL(message));
         aa4text.doLater("anyActionPerformed", ev);
     }
 
     @Override
     public void close() {
         dispose();
     }
 
     @Override
     protected void processWindowEvent(WindowEvent e) {
         if (e.getID() == WindowEvent.WINDOW_CLOSING) {
             launcher.anyActionPerformed(new AnyActionEvent(this, AnyActionKey.closeWindow));
         }
     }
 
     @Override
     public void anyActionPerformed(AnyActionEvent ev) {
         log.atEnter("anyActionPerformed", ev);
         try {
             if (ev.isAnyOf(importFile)) {
                 importIntoCurrentTable();
             } else if (ev.isAnyOf(exportFile)) {
                 exportTableContent();
             } else if (ev.isAnyOf(showAbout)) {
                 showVersionInfo();
             } else {
                 log.warn("not expected: Event=%s", ev);
             }
         } catch (Exception ex) {
             log.error(ex);
             showErrorDialog(ex);
         }
         log.atExit("anyActionPerformed");
     }
 
     void setEnvironment(Environment env) {
         this.env = env;
         if (currentDirectory == null) {
             setCurrentDirectory(env.getCurrentDirectory());
         }
     }
 
     /**
      * Outputs a result.
      * @param ref
      * @throws SQLException
      */
     void outputResult(ResultSetReference ref) throws SQLException {
         // NOTICE: This method will be called by non AWT thread.
         // To access GUI, use "Later".
         final OutputProcessor opref = env.getOutputProcessor();
         invoker.doLater("clearResultSetTable");
         ResultSet rs = ref.getResultSet();
         ColumnOrder order = ref.getOrder();
         final boolean needsOrderChange = order.size() > 0;
         ResultSetMetaData meta = rs.getMetaData();
         final int columnCount = (needsOrderChange) ? order.size() : meta.getColumnCount();
         final ResultSetTableModel m = new ResultSetTableModel(ref);
         Vector<Object> v = new Vector<Object>(columnCount);
         ValueTransporter transfer = ValueTransporter.getInstance("");
         final int limit = Bootstrap.getPropertyAsInt("net.argius.stew.rowcount.limit",
                                                      Integer.MAX_VALUE);
         int rowCount = 0;
         while (rs.next()) {
             if (rowCount >= limit) {
                 invoker.doLater("notifyOverLimit", limit);
                 break;
             }
             ++rowCount;
             v.clear();
             for (int i = 0; i < columnCount; i++) {
                 final int index = needsOrderChange ? order.getOrder(i) : i + 1;
                 v.add(transfer.getObject(rs, index));
             }
             m.addRow((Vector<?>)v.clone());
             if (env.getOutputProcessor() != opref) {
                 throw new SQLException("interrupted");
             }
         }
         invoker.doLater("showResult", m);
         ref.setRecordCount(m.getRowCount());
     }
 
     @SuppressWarnings("unused")
     private void clearResultSetTable() {
         resultSetTable.setVisible(false);
         resultSetTable.getTableHeader().setVisible(false);
         resultSetTable.reset();
     }
 
     @SuppressWarnings("unused")
     private void notifyOverLimit(int limit) {
         output(res.get("w.exceeded-limit", limit));
     }
 
     @SuppressWarnings("unused")
     private void showResult(ResultSetTableModel m) {
         resultSetTable.setModel(m);
         Container p = resultSetTable.getParent();
         if (p != null && p.getParent() instanceof JScrollPane) {
             JScrollPane scrollPane = (JScrollPane)p.getParent();
             ImageIcon icon = getImageIcon(String.format("linkable-%s.png", m.isLinkable()));
             scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER,
                                  new JLabel(icon, SwingConstants.CENTER));
         }
         resultSetTable.anyActionPerformed(new AnyActionEvent(this, AnyActionKey.adjustColumnWidth));
         resultSetTable.getTableHeader().setVisible(true);
         resultSetTable.doLayout();
         resultSetTable.setVisible(true);
     }
 
     String getPostProcessMode() {
         return postProcessMode;
     }
 
     void setPostProcessMode(String postProcessMode) {
         final String oldValue = this.postProcessMode;
         this.postProcessMode = postProcessMode;
         firePropertyChange("postProcessMode", oldValue, postProcessMode);
     }
 
     void doPostProcess() {
         final AnyAction aa = new AnyAction(this);
         if (isActive()) {
             aa.doLater("focusWindow", true);
             return;
         }
         final String prefix = getClass().getName() + ".postprocess.";
         final int count = getPropertyAsInt(prefix + "count", 32);
         final int range = getPropertyAsInt(prefix + "range", 2);
         final long interval = getPropertyAsInt(prefix + "interval", 50);
         switch (AnyActionKey.of(postProcessMode)) {
             case postProcessModeNone:
                 break;
             case postProcessModeFocus:
                 aa.doLater("focusWindow", false);
                 break;
             case postProcessModeShake:
                 aa.doParallel("shakeWindow", count, range, interval);
                 break;
             case postProcessModeBlink:
                 aa.doParallel("blinkWindow", count, range, interval);
                 break;
             default:
                 log.warn("doPostProcess: postProcessMode=%s", postProcessMode);
         }
     }
 
     void focusWindow(boolean moveToFront) {
         if (moveToFront) {
             toFront();
         }
         requestFocus();
         textArea.requestFocusInWindow();
     }
 
     void shakeWindow(int count, int range, long interval) {
         AnyAction aa = new AnyAction(new PostProcessAction());
         aa.doLater("focusWindow");
         for (int i = 0, n = count >> 1 << 1; i < n; i++) {
             aa.doLater("shakeWindow", range);
             sleep(interval);
         }
     }
 
     void blinkWindow(final int count, final int range, final long interval) {
         final byte[] alpha = {0};
         final JPanel p = new PostProcessAction(alpha);
         AnyAction aa = new AnyAction(new PostProcessAction());
         aa.doLater("showComponent", p);
         for (int i = 0, n = (count / 45 + 1) * 45; i < n; i++) {
             alpha[0] = (byte)((Math.sin(i * 0.25f) + 1) * 32 * range);
             p.repaint();
             sleep(interval);
         }
         aa.doLater("removeComponent", p);
     }
 
     private void requestFocusToTextAreaInWindow() {
         textArea.requestFocusInWindow();
     }
 
     private void moveWindow(int incX, int incY) {
         setLocation(getX() + incX, getY() + incY);
     }
 
     final class PostProcessAction extends JPanel {
         int sign = -1;
         byte[] alpha;
         Frame frame = getRootFrame();
         PostProcessAction(byte... alpha) {
             this.alpha = alpha;
         }
         void focusWindow() {
             requestFocusToTextAreaInWindow();
         }
         void shakeWindow(int range) {
             sign *= -1;
             moveWindow(range * sign, 0);
         }
         void showComponent(JComponent c) {
             requestFocusToTextAreaInWindow();
             setGlassPane(c);
             c.setOpaque(false);
             c.setVisible(true);
         }
         void removeComponent(JComponent c) {
             c.setVisible(false);
             remove(c);
         }
         @Override
         public void paintComponent(Graphics g) {
             super.paintComponents(g);
             g.setColor(new Color(0, 0xE6, 0x2E, alpha[0] & 0xFF));
             g.fillRect(0, 0, getWidth(), getHeight());
         }
     }
 
     /**
      * Imports data into the current table.
      * @throws IOException
      * @throws SQLException
      */
     void importIntoCurrentTable() throws IOException, SQLException {
         if (env.getCurrentConnection() == null) {
             showMessageDialog(this, res.get("w.not-connect"));
             return;
         }
         TableModel tm = resultSetTable.getModel();
         final boolean importable;
         if (tm instanceof ResultSetTableModel) {
             ResultSetTableModel m = (ResultSetTableModel)tm;
             importable = m.isLinkable() && m.isSameConnection(env.getCurrentConnection());
         } else {
             importable = false;
         }
         if (!importable) {
             showMessageDialog(this, res.get("w.import-target-not-available"));
             return;
         }
         ResultSetTableModel m = (ResultSetTableModel)tm;
         assert currentDirectory != null;
         JFileChooser fileChooser = new JFileChooser(currentDirectory);
         fileChooser.setDialogTitle(res.get("Action.import"));
         fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fileChooser.showOpenDialog(this);
         final File file = fileChooser.getSelectedFile();
         if (file == null) {
             return;
         }
         setCurrentDirectory(file);
         Importer importer = Importer.getImporter(file);
         try {
             while (true) {
                 Object[] row = importer.nextRow();
                 if (row.length == 0) {
                     break;
                 }
                 m.addUnlinkedRow(row);
                 m.linkRow(m.getRowCount() - 1);
             }
         } finally {
             importer.close();
         }
     }
 
     /**
      * Exports data in this table.
      * @throws IOException
      */
     void exportTableContent() throws IOException {
         assert currentDirectory != null;
         JFileChooser fileChooser = new JFileChooser(currentDirectory);
         fileChooser.setDialogTitle(res.get("Action.export"));
         fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fileChooser.showSaveDialog(this);
         final File file = fileChooser.getSelectedFile();
         if (file == null) {
             return;
         }
         setCurrentDirectory(file);
         if (file.exists()) {
             if (showConfirmDialog(this, res.get("i.confirm-overwrite", file), null, YES_NO_OPTION) != YES_OPTION) {
                 return;
             }
         }
         Exporter exporter = Exporter.getExporter(file);
         try {
             TableColumnModel columnModel = resultSetTable.getTableHeader().getColumnModel();
             List<Object> headerValues = new ArrayList<Object>();
             for (TableColumn column : Collections.list(columnModel.getColumns())) {
                 headerValues.add(column.getHeaderValue());
             }
             exporter.addHeader(headerValues.toArray());
             DefaultTableModel m = (DefaultTableModel)resultSetTable.getModel();
             @SuppressWarnings("unchecked")
             Vector<Vector<Object>> rows = m.getDataVector();
             for (Vector<Object> row : rows) {
                 exporter.addRow(row.toArray());
             }
         } finally {
             exporter.close();
         }
         showMessageDialog(this, res.get("i.exported"));
     }
 
     private void showVersionInfo() {
         ImageIcon icon = new ImageIcon();
         if (getIconImage() != null) {
             icon.setImage(getIconImage());
         }
         final String about = res.get(".about", Bootstrap.getVersion());
         showMessageDialog(this, about, null, PLAIN_MESSAGE, icon);
     }
 
     private void setCurrentDirectory(File file) {
         final File dir;
         if (file.isDirectory()) {
             dir = file;
         } else {
             // when the file object is file, uses its parent's dir.
             dir = file.getParentFile();
         }
         assert dir.isDirectory();
         // I am optimistic about no-sync ...
         currentDirectory = dir;
     }
 
     void showInformationMessageDialog(String message, String title) {
         showInformationMessageDialog(this, message, title);
     }
 
     static void showInformationMessageDialog(final Component parent, String message, String title) {
         JTextArea textArea = new JTextArea(message, 6, 60);
         setupReadOnlyTextArea(textArea);
         showMessageDialog(parent, new JScrollPane(textArea), title, INFORMATION_MESSAGE);
     }
 
     void showErrorDialog(Throwable th) {
         showErrorDialog(this, th);
     }
 
     static void showErrorDialog(final Component parent, final Throwable th) {
         log.atEnter("showErrorDialog");
         log.warn(th, "");
         final String s1;
         final String s2;
         if (th == null) {
             s1 = res.get("e.error-no-detail");
             s2 = "";
         } else {
             s1 = th.getMessage();
             Writer buffer = new StringWriter();
             PrintWriter out = new PrintWriter(buffer);
             th.printStackTrace(out);
             s2 = replaceEOL(buffer.toString());
         }
         JPanel p = new JPanel(new BorderLayout());
         p.add(new JScrollPane(setupReadOnlyTextArea(new JTextArea(s1, 2, 60))), BorderLayout.NORTH);
         p.add(new JScrollPane(setupReadOnlyTextArea(new JTextArea(s2, 6, 60))), BorderLayout.CENTER);
         JDialog d = (new JOptionPane(p, ERROR_MESSAGE)).createDialog(parent, res.get("e.error"));
         d.setResizable(true);
         d.setVisible(true);
         d.dispose();
         log.atExit("showErrorDialog");
     }
 
     private static String replaceEOL(String s) {
         return s.replaceAll("\\\r\\\n?", "\n");
     }
 
     static JTextArea setupReadOnlyTextArea(JTextArea textArea) {
         textArea.setEditable(false);
         textArea.setWrapStyleWord(false);
         textArea.setLineWrap(false);
         textArea.setOpaque(false);
         textArea.setMargin(new Insets(4, 4, 4, 4));
         return textArea;
     }
 
     /**
      * Bypass OutputProcessor for breaking command.
      */
     static final class Bypass implements OutputProcessor {
 
         private OutputProcessor op;
         private volatile boolean closed;
 
         Bypass(OutputProcessor op) {
             this.op = op;
         }
 
         @Override
         public void output(Object object) {
             if (!closed) {
                 op.output(object);
             }
         }
 
         @Override
         public void close() {
             closed = true;
         }
 
     }
 
 }

 package amber.swing;
 
 import javax.swing.*;
 import javax.swing.plaf.FontUIResource;
 import java.awt.*;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DragSource;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Enumeration;
 import javax.activation.ActivationDataFlavor;
 import javax.activation.DataHandler;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileSystemView;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 /**
  * @author Tudor
  */
 public class UIUtil {
 
     public final static int POPUP = 0;
     public final static int UTILITY = 1;
     public final static int NORMAL = 2;
 
     /**
      * Attempts to set the current L&F to native, and perform appearance tweaks
      * to mimic the current OS's L&F. Has a large positive effect on Mac
      * systems.
      */
     public static void makeNative() {
         // Mac tweaks
         System.setProperty("apple.laf.useScreenMenuBar", "true");
         System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Amber IDE");
         System.setProperty("apple.awt.showGrowBox", "true");
         System.setProperty("apple.awt.brushMetalLook", "true");
 
         System.setProperty("sun.java2d.noddraw", "true");
 
         System.setProperty("swing.aatext", "true");
 
         JFrame.setDefaultLookAndFeelDecorated(false);
         JDialog.setDefaultLookAndFeelDecorated(false);
         JPopupMenu.setDefaultLightWeightPopupEnabled(false);
         ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
         ToolTipManager.sharedInstance().setReshowDelay(0);
 
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception ex) {
         }
     }
 
     public static boolean setComponentLF(JComponent comp, String ui) {
         LookAndFeel previousLF = UIManager.getLookAndFeel();
 
         try {
             for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                 if (ui.equals(info.getName())) {
                     UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
 
             comp.updateUI();
 
             UIManager.setLookAndFeel(previousLF);
         } catch (Exception e) {
             return false;
         }
         return true;
     }
 
     public static FileFilter makeFileFilter(final String desc, final String... types) {
         return new FileFilter() {
             public boolean accept(File file) {
                 if (file.isDirectory()) {
                     return true;
                 }
                 for (String extension : types) {
                     if (file.getName().toLowerCase().endsWith(extension)) {
                         return true;
                     }
                 }
                 return false;
             }
 
             @Override
             public String getDescription() {
                 return desc;
             }
         };
     }
 
     /**
      * Creates an invisible cursor
      *
      * @return the specified cursor
      */
     public static Cursor invisibleCursor() {
         return Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "invisible_cursor");
     }
 
     /**
      * Toggles the enabled set of all children of the specified Container,
      * recursively
      *
      * @param comp the root node
      * @param flag toggle for enabled state
      */
     public static void setTreeEnabled(Container comp, boolean flag) {
         comp.setEnabled(flag);
         for (Component child : comp.getComponents()) {
             if (child instanceof Container) {
                 setTreeEnabled((Container) child, flag);
             } else {
                 child.setEnabled(flag);
             }
         }
     }
 
     /**
      * Sets the default Font for the current UIManager
      *
      * @param fon the new Font
      */
     public static void setUIFont(Font fon) {
         FontUIResource f = new FontUIResource(fon);
         Enumeration keys = UIManager.getDefaults().keys();
         while (keys.hasMoreElements()) {
             Object key = keys.nextElement();
             Object value = UIManager.get(key);
             if (value != null && value instanceof javax.swing.plaf.FontUIResource) {
                 UIManager.put(key, f);
             }
         }
        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
        }
     }
 
     /**
      * Sets the look of the given dialog. Works only on Java 7+, but does not
      * crash on Java 6-.
      *
      * @param dialog
      * @param type one of UIUtil.(POPUP, UTILITY, NORMAL)
      */
     public static void decorate(JDialog dialog, int type) {
         try {
             Class windowType = Class.forName("java.awt.Window$Type");
             Method setType = Window.class.getDeclaredMethod("setType", windowType);
             switch (type) {
                 case POPUP:
                     setType.invoke(dialog, Enum.valueOf((Class<Enum>) windowType.getDeclaredField("POPUP").getType(), "POPUP"));
                     break;
                 case UTILITY:
                     setType.invoke(dialog, Enum.valueOf((Class<Enum>) windowType.getDeclaredField("UTILITY").getType(), "UTILITY"));
                     break;
                 case NORMAL:
                     setType.invoke(dialog, Enum.valueOf((Class<Enum>) windowType.getDeclaredField("NORMAL").getType(), "NORMAL"));
                     break;
             }
 
         } catch (Exception e) {
             // This ClassNotFound occurs if the user doesn't have Java 7 installed.
             // We can still create a utility-looking window, though.
             // Just doesn't look as nice.
             dialog.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
         }
     }
 
     public static void adjustColumnPreferredWidths(JTable table) {
         // strategy - get max width for cells in column and
         // make that the preferred width
         TableColumnModel columnModel = table.getColumnModel();
         for (int col = 0; col < table.getColumnCount(); col++) {
 
             int maxwidth = 0;
             for (int row = 0; row < table.getRowCount(); row++) {
                 TableCellRenderer rend =
                         table.getCellRenderer(row, col);
                 Object value = table.getValueAt(row, col);
                 Component comp =
                         rend.getTableCellRendererComponent(table,
                         value,
                         false,
                         false,
                         row,
                         col);
                 maxwidth = Math.max(comp.getPreferredSize().width, maxwidth);
             }
             TableColumn column = columnModel.getColumn(col);
             TableCellRenderer headerRenderer = column.getHeaderRenderer();
             if (headerRenderer == null) {
                 headerRenderer = table.getTableHeader().getDefaultRenderer();
             }
             Object headerValue = column.getHeaderValue();
             Component headerComp =
                     headerRenderer.getTableCellRendererComponent(table,
                     headerValue,
                     false,
                     false,
                     0,
                     col);
            maxwidth = Math.max(maxwidth, headerComp.getPreferredSize().width);
             column.setPreferredWidth(maxwidth);
         }
     }
 
     public static int getComponentTreeCount(Container c) {
         int cn = 1;
         for (Component node : c.getComponents()) {
             if (node instanceof Container) {
                 cn += getComponentTreeCount((Container) node);
             } else {
                 cn++;
             }
         }
         return cn;
     }
 
     public static void makeFluidSplitPane(final JSplitPane pane) {
     }
 
     public static void throwUnimplemented() {
         Dialogs.confirmDialog()
                 .setTitle("Easy there...")
                 .setMessage("This feature is not yet implemented. Sorry; check back later.")
                 .setOptionType(JOptionPane.OK_CANCEL_OPTION)
                 .setMessageType(JOptionPane.INFORMATION_MESSAGE)
                 .show();
     }
 
     public static Icon getFileSystemIcon(String extension) {
         File file = new File(System.getProperty("java.io.tmpdir") + File.pathSeparator + System.currentTimeMillis() + "." + extension);
         try {
             file.createNewFile();
         } catch (IOException ex) {
             Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
         }
         file.deleteOnExit();
         return FileSystemView.getFileSystemView().getSystemIcon(file);
     }
 
     public static void makeDnD(final JTable table) {
         table.setTransferHandler(new TransferHandler() {
             private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class, DataFlavor.javaJVMLocalObjectMimeType, "DnD_table");
 
             @Override
             protected Transferable createTransferable(JComponent c) {
                 return new DataHandler(new Integer(table.getSelectedRow()), localObjectFlavor.getMimeType());
             }
 
             @Override
             public boolean canImport(TransferHandler.TransferSupport info) {
                 boolean b = info.getComponent() == table && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
                 table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
                 return b;
             }
 
             @Override
             public int getSourceActions(JComponent c) {
                 return TransferHandler.COPY_OR_MOVE;
             }
 
             @Override
             public boolean importData(TransferHandler.TransferSupport info) {
                 JTable target = (JTable) info.getComponent();
                 JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
                 int index = dl.getRow();
                 int max = table.getModel().getRowCount();
                 if (index < 0 || index > max) {
                     index = max;
                 }
                 target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 try {
                     Integer rowFrom = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
                     if (rowFrom != -1 && rowFrom != index) {
                         DefaultTableModel model = (DefaultTableModel) table.getModel();
                         Object o = model.getDataVector().get(rowFrom);
                         model.removeRow(rowFrom);
 
                         if (index > rowFrom) {
                             index--;
                         }
 
                         model.getDataVector().add(index, o);
 
                         target.getSelectionModel().addSelectionInterval(index, index);
                         return true;
                     }
                 } catch (Exception e) {
                     Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                 }
                 return false;
             }
 
             @Override
             protected void exportDone(JComponent c, Transferable t, int act) {
                 if (act == TransferHandler.MOVE) {
                     table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 }
             }
         });
     }
 
     public static void setHeaderIcon(JTable table, final int column, final ImageIcon icon) {
         final TableCellRenderer orig = table.getTableHeader().getDefaultRenderer();
         table.getTableHeader().getColumnModel().getColumn(column).setHeaderRenderer(new DefaultTableCellRenderer() {
             @Override
             public Component getTableCellRendererComponent(JTable table,
                     Object value, boolean isSelected, boolean hasFocus, int row,
                     int column) {
                 JLabel com = (JLabel) orig.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 com.setHorizontalAlignment(JLabel.LEADING); // On Fedora its TRAILING; looks odd with the icon itself trailing
                 com.setIcon(icon);
                 return com;
             }
         });
     }
 
     public static void mapInput(JComponent component, int scope, final int keycode, final int modifiers, final AbstractAction action) {
         if (component instanceof JComponent) {
             JComponent jc = (JComponent) component;
             String hash = String.valueOf(System.identityHashCode(action));
             jc.getInputMap(scope).put(KeyStroke.getKeyStroke(keycode, modifiers), hash);
             jc.getActionMap().put(hash, action);
         }
     }
 
     public static ButtonGroup groupButtons(AbstractButton... buttons) {
         ButtonGroup group = new ButtonGroup();
         for (AbstractButton button : buttons) {
             group.add(button);
         }
         return group;
     }
 
     public static Window getFocusedWindow() {
         for (Window w : Window.getWindows()) {
             if (w.isFocused()) {
                 return w;
             }
         }
         return null;
     }
 }

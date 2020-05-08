 package main.demo.gui;
 
 import java.awt.*;
 import gx.realtime.*;
 import gx.realtime.Event;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * Swing panel that handles the display and interaction with the Realtime library
  */
 public class RealtimePanel extends JPanel
 {
     private RealtimeTableModel model;
     private DefaultListModel<Collaborator> collaboratorListModel = new DefaultListModel<>();
     private DefaultListModel<Event> eventListModel = new DefaultListModel<>();
     private Document document;
 
     public RealtimePanel()
     {
         // Init the components
         initComponents();
 
         this.setEnabled(false);
     }
     public void initialize(Document document, CollaborativeMap collabMap)
     {
         this.document = document;
         model = new RealtimeTableModel(collabMap);
         table.setModel(model);
 
         // Listen for ValueChangedEvents to update the UI
         collabMap.addEventListener(EventType.VALUE_CHANGED, (ValueChangedEvent event) -> {
             logEvent(event);
 
             // Don't update the map if we've thrown the events
             if (event.isLocal())
                 return;
 
             if (event.getNewValue() != null) {
                 model.updateValue(event.getProperty(), (String) event.getNewValue(), event.isLocal());
             } else {
                 model.removeValue(event.getProperty(), event.isLocal());
             }
         });
         collabMap.addEventListener(EventType.OBJECT_CHANGED, (ObjectChangedEvent event) -> {
             logEvent(event);
         });
 
         document.addEventListener(EventType.COLLABORATOR_JOINED, (CollaboratorJoinedEvent event) -> {
             logEvent(event);
             if (!collaboratorListModel.contains(event.getCollaborator()))
                 collaboratorListModel.addElement(event.getCollaborator());
         });
         document.addEventListener(EventType.COLLABORATOR_LEFT, (CollaboratorLeftEvent event) -> {
             logEvent(event);
             if (collaboratorListModel.contains(event.getCollaborator()))
                 collaboratorListModel.removeElement(event.getCollaborator());
         });
 
         //listen to UndoRedoStateChangedEvent to update the UI
         document.getModel().addEventListener(EventType.UNDO_REDO_STATE_CHANGED, (UndoRedoStateChangedEvent event) -> {
             logEvent(event);
             undoButton.setEnabled(event.canUndo());
             redoButton.setEnabled(event.canRedo());
         });
 
         // Put a selection listener on the table to prefill the key/value fields
         table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
             int row = table.getSelectedRow();
             if (row == -1)
                 return;
 
             keyField.setText((String) model.getValueAt(row, 0));
             valueField.setText((String) model.getValueAt(row, 1));
         });
 
         this.setEnabled(true);
         undoButton.setEnabled(false);
         redoButton.setEnabled(false);
     }
 
     public Document getDocument()
     {
         return document;
     }
 
     public void logEvent(Event event)
     {
         System.out.println(event);
         eventListModel.addElement(event);

         // Scroll to the bottom if the GUI is available already
         if(eventLogList != null && eventLogList.getVisibleRect() != null) {
             Rectangle visibleRect = eventLogList.getVisibleRect();
             visibleRect.y = eventLogList.getHeight() - visibleRect.height;
             eventLogList.scrollRectToVisible(visibleRect);
         }
     }
 
     @Override
     public void setEnabled(boolean enabled)
     {
         super.setEnabled(enabled);
 
         for (Component com : getComponents()) {
             com.setEnabled(enabled);
         }
     }
     /**
      * Method that creates the frame and handles some of the final setup actions.
      */
     public static RealtimePanel createUI()
     {
         JFrame frame = new JFrame("Demo Realtime Gx Application");
 
         RealtimePanel newContentPane = new RealtimePanel();
         newContentPane.setOpaque(true);
         frame.setContentPane(newContentPane);
 
         frame.pack();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.addWindowListener(new WindowAdapter()
         {
             public void windowClosing(WindowEvent e)
             {
                 System.out.println("Politely closing API link...");
                 if (newContentPane != null && newContentPane.document != null)
                     newContentPane.document.close();
             }
         });
         frame.setVisible(true);
         return newContentPane;
     }
 
     private void clearButtonActionPerformed(ActionEvent e)
     {
         model.removeAll();
     }
 
     private void removeButtonActionPerformed(ActionEvent e)
     {
         // Save keys first to prevent changing indexes while in the process of deleting rows
         int[] rows = table.getSelectedRows();
         String[] keys = new String[rows.length];
         for (int i = 0; i < rows.length; i++) {
             keys[i] = (String) model.getValueAt(rows[i], 0);
         }
 
         // Remove values
         for (String key : keys) {
             model.removeValue(key, true);
         }
     }
 
     private void putButtonActionPerformed(ActionEvent e)
     {
         model.updateValue(keyField.getText(), valueField.getText(), true);
     }
 
     private void undoButtonActionPerformed(ActionEvent e) {
         if (document.getModel().canUndo()) {
             document.getModel().undo();
             this.repaint();
         } else {
             System.err.println("Unable to undo!");
             undoButton.setEnabled(false);
         }
     }
 
     private void redoButtonActionPerformed(ActionEvent e) {
         if(document.getModel().canRedo()){
             document.getModel().redo();
             this.repaint();
         } else {
             System.err.println("Unable to redo!");
             redoButton.setEnabled(false);
         }
     }
 
     private void initComponents()
     {
         // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
         tableScrollPane = new JScrollPane();
         table = new JTable(model);
         clearButton = new JButton();
         removeButton = new JButton();
         putButton = new JButton();
         keyField = new JTextField();
         valueField = new JTextField();
         keyLabel = new JLabel();
         valueLabel = new JLabel();
         eventLogScrollPane = new JScrollPane();
         eventLogList = new JList(eventListModel);
         eventLogList.setCellRenderer(new EventRenderer(this));
         eventLogLabel = new JLabel();
         label1 = new JLabel();
         collabListScrollPane = new JScrollPane();
         collabList = new JList(collaboratorListModel);
         collabList.setCellRenderer(new CollaboratorRenderer());
         redoButton = new JButton();
         undoButton = new JButton();
 
         //======== tableScrollPane ========
         {
             tableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 
             //---- table ----
             table.setFillsViewportHeight(true);
             table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
             tableScrollPane.setViewportView(table);
         }
 
         //---- clearButton ----
         clearButton.setText("Clear map");
         clearButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 clearButtonActionPerformed(e);
             }
         });
 
         //---- removeButton ----
         removeButton.setText("Remove selection");
         removeButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 removeButtonActionPerformed(e);
             }
         });
 
         //---- putButton ----
         putButton.setText("Put key-value pair");
         putButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 putButtonActionPerformed(e);
             }
         });
 
         //---- keyField ----
         keyField.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 putButtonActionPerformed(e);
             }
         });
 
         //---- valueField ----
         valueField.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 putButtonActionPerformed(e);
             }
         });
 
         //---- keyLabel ----
         keyLabel.setText("Key:");
 
         //---- valueLabel ----
         valueLabel.setText("Value:");
 
         //======== eventLogScrollPane ========
         {
             eventLogScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
             eventLogScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
             eventLogScrollPane.setViewportView(eventLogList);
         }
 
         //---- eventLogLabel ----
         eventLogLabel.setText("Event log:");
 
         //---- label1 ----
         label1.setText("Collaborators:");
 
         //======== collabListScrollPane ========
         {
             collabListScrollPane.setViewportView(collabList);
         }
 
         //---- redoButton ----
         redoButton.setText("Redo");
         redoButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 redoButtonActionPerformed(e);
             }
         });
         redoButton.setEnabled(false);
 
         //---- undoButton ----
         undoButton.setText("Undo");
         undoButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 undoButtonActionPerformed(e);
             }
         });
         undoButton.setEnabled(false);
 
         GroupLayout layout = new GroupLayout(this);
         setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup()
                 .addGroup(layout.createSequentialGroup()
                     .addContainerGap()
                     .addGroup(layout.createParallelGroup()
                         .addComponent(eventLogScrollPane)
                         .addGroup(layout.createSequentialGroup()
                             .addGroup(layout.createParallelGroup()
                                 .addComponent(tableScrollPane, GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                                 .addGroup(layout.createSequentialGroup()
                                     .addComponent(eventLogLabel)
                                     .addGap(251, 251, 251)))
                             .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                             .addGroup(layout.createParallelGroup()
                                 .addGroup(layout.createSequentialGroup()
                                     .addComponent(label1)
                                     .addGap(0, 131, Short.MAX_VALUE))
                                 .addComponent(collabListScrollPane, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                                 .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                     .addGap(0, 12, Short.MAX_VALUE)
                                     .addGroup(layout.createParallelGroup()
                                         .addComponent(putButton, GroupLayout.Alignment.TRAILING)
                                         .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                             .addComponent(keyLabel)
                                             .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                             .addComponent(keyField, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE))
                                         .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                             .addComponent(valueLabel)
                                             .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                             .addComponent(valueField, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE))
                                         .addComponent(removeButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
                                         .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                             .addComponent(undoButton)
                                             .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                             .addComponent(redoButton)
                                             .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                             .addComponent(clearButton)))))))
                     .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup()
                 .addGroup(layout.createSequentialGroup()
                     .addContainerGap()
                     .addGroup(layout.createParallelGroup()
                         .addGroup(layout.createSequentialGroup()
                             .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                 .addComponent(clearButton)
                                 .addComponent(redoButton)
                                 .addComponent(undoButton))
                             .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                             .addComponent(removeButton)
                             .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                             .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                 .addComponent(keyField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                 .addComponent(keyLabel))
                             .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                             .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                 .addComponent(valueField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                 .addComponent(valueLabel))
                             .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                             .addComponent(putButton)
                             .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                             .addComponent(label1)
                             .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                             .addComponent(collabListScrollPane, GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))
                         .addComponent(tableScrollPane, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
                     .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(eventLogLabel)
                     .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(eventLogScrollPane, GroupLayout.PREFERRED_SIZE, 113, GroupLayout.PREFERRED_SIZE)
                     .addContainerGap())
         );
         // JFormDesigner - End of component initialization  //GEN-END:initComponents
     }
 
     // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
     private JScrollPane tableScrollPane;
     private JTable table;
     private JButton clearButton;
     private JButton removeButton;
     private JButton putButton;
     private JTextField keyField;
     private JTextField valueField;
     private JLabel keyLabel;
     private JLabel valueLabel;
     private JScrollPane eventLogScrollPane;
     private JList eventLogList;
     private JLabel eventLogLabel;
     private JLabel label1;
     private JScrollPane collabListScrollPane;
     private JList collabList;
     private JButton redoButton;
     private JButton undoButton;
     // JFormDesigner - End of variables declaration  //GEN-END:variables
 }
 
 /**
  * Custom renderer to display the Collaborators in our list
  */
 class CollaboratorRenderer extends DefaultListCellRenderer {
     JLabel label = new JLabel();
 
     @Override
     public Component getListCellRendererComponent(
             JList list,
             Object value,
             int index,
             boolean selected,
             boolean expanded) {
 
         Collaborator collaborator = (Collaborator) value;
 
         try {
             String urlString = collaborator.getPhotoUrl();
             if(!urlString.substring(0, 4).equals("http"))
                 urlString = "https:" + urlString;
 
             Image image = ImageIO.read(new URL(urlString));
             ImageIcon imageIcon = new ImageIcon(image.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
             String name = collaborator.getDisplayName();
             if(collaborator.isMe())
                 name += " (me)";
            label.setText(name);
             label.setIcon(imageIcon);
             label.setHorizontalAlignment(JLabel.LEFT);
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         label.setForeground(Color.decode(collaborator.getColor()));
 
         return label;
     }
 }
 
 
 /**
  * Renderer for the event log list.
  */
 class EventRenderer extends DefaultListCellRenderer {
     private RealtimePanel panel;
     private JLabel label = new JLabel();
 
     public EventRenderer(RealtimePanel panel) {
         this.panel = panel;
     }
 
     @Override
     public Component getListCellRendererComponent(
             JList list,
             Object value,
             int index,
             boolean selected,
             boolean expanded) {
 
         if (value instanceof ValueChangedEvent) {
             ValueChangedEvent event = (ValueChangedEvent) value;
             label.setText(event.toString());
         } else if (value instanceof CollaboratorJoinedEvent) {
             CollaboratorJoinedEvent event = (CollaboratorJoinedEvent) value;
             label.setText(event.toString());
         } else if (value instanceof CollaboratorLeftEvent) {
             CollaboratorLeftEvent event = (CollaboratorLeftEvent) value;
             label.setText(event.toString());
         } else {
             label.setText(value.toString());
         }
 
         Color color = getColor(value);
         if (color != null)
             label.setForeground(color);
 
         return label;
     }
 
     /**
      * Sets the text color of the provided label to the color of the collaborator.
      *
      * @param event
      */
     private Color getColor(Object event) {
         if(panel == null || panel.getDocument() == null)
             return null;
 
         Collaborator user = null;
 
         if (event instanceof BaseModelEvent) {
             String userid = ((BaseModelEvent) event).getUserId();
             for (Collaborator collab : panel.getDocument().getCollaborators()) {
                 if(collab.getUserId().equals(userid)) {
                     user = collab;
                 }
             }
         } else if (event instanceof CollaboratorJoinedEvent) {
             user = ((CollaboratorJoinedEvent)event).getCollaborator();
         } else if (event instanceof CollaboratorLeftEvent) {
             user = ((CollaboratorLeftEvent)event).getCollaborator();
         }
 
         return user != null ? Color.decode(user.getColor()) : null;
     }
 }

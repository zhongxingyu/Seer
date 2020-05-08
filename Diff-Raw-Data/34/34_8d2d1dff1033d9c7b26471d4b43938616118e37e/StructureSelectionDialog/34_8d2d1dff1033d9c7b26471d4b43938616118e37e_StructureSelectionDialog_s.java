 package pl.poznan.put.cs.bioserver.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 class StructureSelectionDialog extends JDialog {
     private static final long serialVersionUID = 1L;
     List<File> selectedStructures;
     DefaultListModel<File> modelAll;
     DefaultListModel<File> modelSelected;
 
     StructureSelectionDialog(Frame owner) {
         super(owner, true);
 
         modelAll = new DefaultListModel<>();
         final JList<File> listAll = new JList<>(modelAll);
         listAll.setBorder(BorderFactory
                 .createTitledBorder("Available structures:"));
 
         modelSelected = new DefaultListModel<>();
         final JList<File> listSelected = new JList<>(modelSelected);
         listSelected.setBorder(BorderFactory
                 .createTitledBorder("Selected structures:"));
 
         final JButton buttonSelect = new JButton("Select ->");
         buttonSelect.setEnabled(false);
         final JButton buttonSelectAll = new JButton("Select all ->");
         final JButton buttonDeselect = new JButton("<- Deselect");
         buttonDeselect.setEnabled(false);
         JButton buttonDeselectAll = new JButton("<- Deselect all");
 
         JPanel panelButtons = new JPanel();
         panelButtons.setLayout(new GridBagLayout());
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         constraints.fill = GridBagConstraints.HORIZONTAL;
         panelButtons.add(buttonSelect, constraints);
         constraints.gridy++;
         panelButtons.add(buttonSelectAll, constraints);
         constraints.gridy++;
         panelButtons.add(buttonDeselect, constraints);
         constraints.gridy++;
         panelButtons.add(buttonDeselectAll, constraints);
 
         JPanel panelMain = new JPanel();
        panelMain.add(listAll);
        panelMain.add(panelButtons);
        panelMain.add(listSelected);
 
         JButton buttonOk = new JButton("OK");
         JButton buttonCancel = new JButton("Cancel");

         JPanel panelOkCancel = new JPanel();
         panelOkCancel.add(buttonOk);
         panelOkCancel.add(buttonCancel);
 
         setLayout(new BorderLayout());
         add(panelMain, BorderLayout.CENTER);
         add(panelOkCancel, BorderLayout.SOUTH);
 
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dimension = new Dimension(screenSize.width / 3,
                screenSize.height / 2);
        listAll.setPreferredSize(dimension);
        listSelected.setPreferredSize(dimension);

         pack();
         int width = getPreferredSize().width;
         int height = getPreferredSize().height;
 
         int x = screenSize.width - width;
         int y = screenSize.height - height;
        setSize(width, height);
         setLocation(x / 2, y / 2);
 
         setTitle("Structure selection dialog");
 
         ListSelectionListener listSelectionListener = new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent arg0) {
                 ListSelectionModel source = (ListSelectionModel) arg0
                         .getSource();
                 if (source.equals(listAll.getSelectionModel())) {
                     buttonSelect.setEnabled(!listAll.isSelectionEmpty());
                 } else { // source.equals(listSelected)
                     buttonDeselect.setEnabled(!listSelected.isSelectionEmpty());
                 }
             }
         };
         listAll.getSelectionModel().addListSelectionListener(
                 listSelectionListener);
         listSelected.getSelectionModel().addListSelectionListener(
                 listSelectionListener);
 
         ActionListener actionListenerSelectDeselect = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 List<File> values;
                 boolean isSelect;
 
                 Object source = arg0.getSource();
                 if (source.equals(buttonSelect)) {
                     values = listAll.getSelectedValuesList();
                     isSelect = true;
                 } else if (source.equals(buttonSelectAll)) {
                     values = new ArrayList<>();
                     Enumeration<File> elements = modelAll.elements();
                     while (elements.hasMoreElements()) {
                         values.add(elements.nextElement());
                     }
                     isSelect = true;
                 } else if (source.equals(buttonDeselect)) {
                     values = listSelected.getSelectedValuesList();
                     isSelect = false;
                 } else { // source.equals(buttonSelectAll)
                     values = new ArrayList<>();
                     Enumeration<File> elements = modelSelected.elements();
                     while (elements.hasMoreElements()) {
                         values.add(elements.nextElement());
                     }
                     isSelect = false;
                 }
 
                 for (File f : values) {
                     if (isSelect) {
                         modelAll.removeElement(f);
                         modelSelected.addElement(f);
                     } else {
                         modelAll.addElement(f);
                         modelSelected.removeElement(f);
                     }
                 }
             }
         };
         buttonSelect.addActionListener(actionListenerSelectDeselect);
         buttonSelectAll.addActionListener(actionListenerSelectDeselect);
         buttonDeselect.addActionListener(actionListenerSelectDeselect);
         buttonDeselectAll.addActionListener(actionListenerSelectDeselect);
 
         buttonOk.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 selectedStructures = new ArrayList<>();
                 Enumeration<File> elements = modelSelected.elements();
                 while (elements.hasMoreElements()) {
                     selectedStructures.add(elements.nextElement());
                 }
                 dispose();
             }
         });
 
         buttonCancel.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 selectedStructures = null;
                 dispose();
             }
         });
     }
 }

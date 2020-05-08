 package client;
 
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.WindowConstants;
 
 class AdminRole implements Role {
 
     AdminRole(ServerConnection s) {
     }
 
     AdminRole() {
     }
 
     @Override
     public Iterable<JMenuItem> getMenuItems() {
         // TODO bind specific action handlers to these menu items
         JMenuItem add = new JMenuItem("Add user");
 
         JMenuItem manageItems = new JMenuItem("Manage items");
         JMenuItem manageGroups = new JMenuItem("Manage groups");
 
         add.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 new CreateUserDialog().setVisible(true);
             }
         });
 
         manageItems.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 new FrameItems();
             }
         });
         manageGroups.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 new FrameGroups();
             }
         });
         return Arrays.asList(add, /*new JMenuItem("Delete user"), new JMenuItem("Edit user"),*/ manageItems, manageGroups);
     }
 
     static class DeleteUserDialog extends JDialog {
 
         DeleteUserDialog() {
             setTitle("Delete user");
             setModalityType(ModalityType.APPLICATION_MODAL);
             //TODO show user table - id, name, contact
             //setLayout(new GridLayout);
             pack();
             setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         }
     }
 
     // Combobox (id, value) pair
     public static class Pair {
 
         final int id;
         final String name;
 
         Pair(int i, String n) {
             id = i;
             name = n;
         }
 
         Pair(String string) {
             String[] s = string.split(" ", 2);
             id = Integer.valueOf(s[0]);
             name = s[1];
         }
 
         @Override
         public String toString() { // Default combobox renderer consults toString method of unknown objects
             return name;
         }
     }
 
     // This expects one line string description of roles in system as per Protocol GET_GROUPS description
     static Pair[] processGetGroupsResponse(String response) { // Initialize from server response
         String[] tokens = response.split(";");
         Pair[] pairs = new Pair[tokens.length];
         int pos = 0;
         for (String i : tokens) {
             pairs[pos++] = new Pair(i);
         }
         return pairs;
     }
     //TODO use for edit as well
 
     static class CreateUserDialog extends JDialog {
 
         JLabel warn;
         JComboBox role, group;
         JPasswordField pass, pass2;
         JButton create, cancel;
 
         public CreateUserDialog() {
             setTitle("Create user");
             setModalityType(ModalityType.APPLICATION_MODAL);
             warn = new JLabel();
             warn.setForeground(Color.red);
             create = new JButton("Create user");
 
             role = new JComboBox(new Object[]{new Pair(Role.EMPLOYEE, "Employee"), new Pair(Role.MANAGER, "Project Manager")});
             SourceGroup sg = new SourceGroup();
             String ret = sg.loadData();
             Object[] groups = null;
             if (!ret.equals("OK")) {
                 System.out.println("SourceGroup.loadData() error: " + ret);
                 warn.setText(ret);
                 create.setEnabled(false);
             } else {
                 HashMap<Integer, Group> hg = sg.getAllGroups();
                 groups = hg.values().toArray(); // Since Group implements toString, JComboBox can handle it all by itself.
             }
 
             group = new JComboBox(groups); // TODO enable based on role, fill from server
             // new Object[]{new Pair(1, "Zednik"), new Pair(2, "Pridavac")}
             pass = new JPasswordField(10);
             pass2 = new JPasswordField(10);
 
             cancel = new JButton("Cancel");
 
             setLayout(new GridLayout(7, 1));
             add(new JLabel("User ID will be automatically generated."));
             add(warn);
             add(new JPanel(new FlowLayout()) {
 
                 {
                     add(new JLabel("User role"));
                     add(role);
                 }
             });
             add(new JPanel(new FlowLayout()) {
 
                 {
                     add(new JLabel("Password")); // TODO refactor, duplicate code in ChangePassDialog and LoginFrame
                     add(pass);
                 }
             });
             add(new JPanel(new FlowLayout()) {
 
                 {
                     add(new JLabel("Password again"));
                     add(pass2);
                 }
             });
             add(new JPanel(new FlowLayout()) {
 
                 {
                     add(new JLabel("group"));
                     add(group);
                 }
             });
             add(new JPanel(new FlowLayout()) {
 
                 {
                     add(create);
                     add(cancel);
                 }
             });
             cancel.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     setVisible(false);
                     dispose();
                 }
             });
             create.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     warn.setText("");
                     if (!Arrays.equals(pass.getPassword(), pass2.getPassword())) {
                         warn.setText("Please enter password again correctly.");
                         return;
                     }
                     int r = ((Pair) role.getSelectedItem()).id;
                     int g = ((Group) group.getSelectedItem()).getId();
                     String p = new String(pass.getPassword());
                     User u = new User(r, g, p);
                     SourceUser su = new SourceUser();
                     String ret = su.loadData();
                     if (!ret.equals("OK")) {
                         System.out.println("CreateUserDialog - SourceUser.loadData error: " + ret);
                         warn.setText(ret);
                         return;
                     }
                     ret = su.addUser(u);
                    if (!ret.equals("OK")) {
                         System.out.println("CreateUserDialog - SourceUser.addUser error: " + ret);
                         warn.setText(ret);
                         return;
                     }
                     setVisible(false);
                     dispose();
                 }
             });
             // TODO create handler possibly outside view
             // TODO create response - either warn or ok box
             setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
             pack();
         }
     }
 
     public static void main(String[] args) {
         new CreateUserDialog().setVisible(true);
     }
 }

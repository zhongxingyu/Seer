 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import javax.swing.*;
 
 /**
  * Panel with contact detail
  *
  * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
  */
 class DetailPanel extends JPanel {
 
     private String styleStart = "<font face=\"arial\" color=\"#999999\">";
     private String styleEnd = "</font>";
     static final long serialVersionUID = 0;
     private final Database db = Database.getInstance();
     private final JLabel name = new JLabel();
     private final JLabel nickname = new JLabel();
     private final JPanel address = new JPanel();
     private final JPanel maps = new JPanel();
     private final JPanel emails = new JPanel();
     private final JLabel phones = new JLabel();
     private final JPanel webs = new JPanel();
     private final JLabel birthday = new JLabel();
     private final JLabel nameday = new JLabel();
     private final JLabel celebration = new JLabel();
     private final JLabel note = new JLabel();
     private final JLabel bdayIcon = new JLabel();
     private final JLabel namedayIcon = new JLabel();
     private final JLabel celebrationIcon = new JLabel();
     private final JLabel icq = new JLabel();
     private final JLabel jabber = new JLabel();
     private final JLabel skype = new JLabel();
     private final PhotoButton photo = new PhotoButton();
     private final JLabel groups = new JLabel();
     private final JLabel nicknameLabel = new JLabel("<html><b>" + styleStart + "nickname" + styleEnd + "</b></html>");
     private final JLabel addressLabel = new JLabel("<html><b>" + styleStart + "address" + styleEnd + "</b></html>");
     private final JLabel emailLabel = new JLabel("<html><b>" + styleStart + "email" + styleEnd + "</b></html>");
     private final JLabel phoneLabel = new JLabel("<html><b>" + styleStart + "phone" + styleEnd + "</b></html>");
     private final JLabel websLabel = new JLabel("<html><b>" + styleStart + "webs" + styleEnd + "</b></html>");
     private final JLabel birthdayLabel = new JLabel("<html><b>" + styleStart + "birthday" + styleEnd + "</b></html>");
     private final JLabel namedayLabel = new JLabel("<html><b>" + styleStart + "name day" + styleEnd + "</b></html>");
     private final JLabel celebrationLabel = new JLabel("<html><b>" + styleStart + "anniversary" + styleEnd + "</b></html>");
     private final JLabel noteLabel = new JLabel("<html><b>" + styleStart + "note" + styleEnd + "</b></html>");
     private final JLabel groupsLabel = new JLabel("<html><b>" + styleStart + "groups" + styleEnd + "</b></html>");
     private final JLabel icqLabel = new JLabel("<html><b>" + styleStart + "ICQ" + styleEnd + "</b></html>");
     private final JLabel jabberLabel = new JLabel("<html><b>" + styleStart + "Jabber" + styleEnd + "</b></html>");
     private final JLabel skypeLabel = new JLabel("<html><b>" + styleStart + "Skype" + styleEnd + "</b></html>");
     private final JLabel messengerLabel = new JLabel("<html><b>" + styleStart + "messenger" + styleEnd + "</b></html>");
     private JScrollPane detailScrollPane;
 
     /**
      * Constructor
      */
     public DetailPanel() {
         setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
         setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 10));
 
         final JPanel namePanel = new JPanel();
         namePanel.setLayout(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
 
         // photo
         gbc.anchor = GridBagConstraints.PAGE_START;
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.weightx = 0.2;
         gbc.gridheight = 2;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         JPanel photoWrapper = new JPanel();
         photoWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         photo.setVisible(false);
         photo.setToolTipText("Change user photo");
         photo.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
         photo.setHorizontalAlignment(SwingConstants.RIGHT);
         photoWrapper.add(photo);
         namePanel.add(photoWrapper, gbc);
         
         // name
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         gbc.gridheight = 1;
         name.setVerticalAlignment(SwingConstants.TOP);
         namePanel.add(name, gbc);
         
         // icon panel
         gbc.gridy++;
         final JPanel iconPanel = new JPanel();
         iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.LINE_AXIS));
         bdayIcon.setIcon(new ImageIcon(getClass().getResource("/res/present.png")));
         bdayIcon.setToolTipText("User has birthday today");
         bdayIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
         bdayIcon.setVisible(false);
         namedayIcon.setIcon(new ImageIcon(getClass().getResource("/res/nameday.png")));
         namedayIcon.setToolTipText("User has nameday today");
         namedayIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
         namedayIcon.setVisible(false);
         celebrationIcon.setIcon(new ImageIcon(getClass().getResource("/res/celebration.png")));
         celebrationIcon.setToolTipText("User has celebration today");
         celebrationIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
         celebrationIcon.setVisible(false);
         iconPanel.add(bdayIcon);
         iconPanel.add(namedayIcon);
         iconPanel.add(celebrationIcon);
         iconPanel.add(Box.createHorizontalGlue());
         namePanel.add(iconPanel, gbc);
        
         // nickname
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         nicknameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         nicknameLabel.setVisible(false);
         nicknameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(nicknameLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         nickname.setLayout(new BoxLayout(nickname, BoxLayout.PAGE_AXIS));
         nickname.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         nickname.setHorizontalAlignment(SwingConstants.LEFT);
         nickname.setVisible(false);
         namePanel.add(nickname, gbc);
          
         // addresses
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         addressLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         addressLabel.setVisible(false);
         addressLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(addressLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         address.setLayout(new BoxLayout(address, BoxLayout.PAGE_AXIS));
         address.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         address.setVisible(false);
         namePanel.add(address, gbc);
         
         // email
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         emailLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         emailLabel.setVisible(false);
         emailLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(emailLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         emails.setLayout(new BoxLayout(emails, BoxLayout.PAGE_AXIS));
         emails.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         emails.setVisible(false);
         namePanel.add(emails, gbc);
       
         // phone
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         phoneLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         phoneLabel.setVisible(false);
         phoneLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(phoneLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         phones.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         phones.setVisible(false);
         namePanel.add(phones, gbc);
         
         // webs
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         websLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         websLabel.setVisible(false);
         websLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(websLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         webs.setLayout(new BoxLayout(webs, BoxLayout.PAGE_AXIS));
         webs.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         webs.setVisible(false);
         namePanel.add(webs, gbc);
           
         // icq
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         icqLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         icqLabel.setVisible(false);
         icqLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(icqLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         icq.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         icq.setVisible(false);
         namePanel.add(icq, gbc);
        
         // jabber
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         jabberLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         jabberLabel.setVisible(false);
         jabberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(jabberLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         jabber.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         jabber.setVisible(false);
         namePanel.add(jabber, gbc);
         
         // skype
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         skypeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         skypeLabel.setVisible(false);
         skypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(skypeLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         skype.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         skype.setVisible(false);
         namePanel.add(skype, gbc);
       
         // birthday
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         birthdayLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         birthdayLabel.setVisible(false);
         birthdayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(birthdayLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         birthday.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         birthday.setVisible(false);
         namePanel.add(birthday, gbc);
            
         // nameday
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         namedayLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         namedayLabel.setVisible(false);
         namedayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(namedayLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         nameday.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         nameday.setVisible(false);
         namePanel.add(nameday, gbc);
      
         // celebration
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         celebrationLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         celebrationLabel.setVisible(false);
         celebrationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(celebrationLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         celebration.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         celebration.setVisible(false);
         namePanel.add(celebration, gbc);
           
         // note
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         noteLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         noteLabel.setVisible(false);
         noteLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(noteLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         note.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         note.setVisible(false);
         namePanel.add(note, gbc);
         
         // groups
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.weightx = 0.2;
         groupsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
         groupsLabel.setVisible(false);
         groupsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         namePanel.add(groupsLabel, gbc);
         gbc.gridx = 1;
         gbc.weightx = 1.0;
         groups.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         groups.setVisible(false);
         namePanel.add(groups, gbc);
         
         // map
         gbc.gridy++;
         this.maps.setLayout(new BoxLayout(this.maps, BoxLayout.PAGE_AXIS));
         this.maps.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
         namePanel.add(this.maps, gbc);
         
         // finish
         gbc.gridy++;
         gbc.weighty = 1;
         namePanel.add(Box.createVerticalGlue(), gbc);
         add(namePanel);
     }
 
     /**
      * Show required contact detail.
      *
      * @param contact required contact
      */
     void show(Contact contact) {
         if (contact != null) {
             if (contact.hasBirthday()) {
                 bdayIcon.setVisible(true);
             } else {
                 bdayIcon.setVisible(false);
             }
             if (contact.hasNameDay()) {
                 namedayIcon.setVisible(true);
             } else {
                 namedayIcon.setVisible(false);
             }
             if (contact.hasCelebration()) {
                 celebrationIcon.setVisible(true);
             } else {
                 celebrationIcon.setVisible(false);
             }
             photo.setContact(contact);
             photo.setVisible(true);
             name.setVisible(true);
             name.setText(String.format("<html><h2>" + contact.getFullNameForDetail() + "</h2></html>"));
             if (contact.getNickName() != null && !contact.getNickName().isEmpty()) {
                 nicknameLabel.setVisible(true);
                 nickname.setVisible(true);
                 nickname.setText(contact.getNickName());
             } else {
                 nicknameLabel.setVisible(false);
                 nickname.setVisible(false);
             }
 
             address.removeAll();
            address.setVisible(false);
             addressLabel.setVisible(false);
             for (Address a : contact.getAdresses()) {
                 if (!a.getAddress().isEmpty()) {
                     addressLabel.setVisible(true);
                     address.setVisible(true);
                     JLabelButton l = new JLabelButton("<html><p>" + a.getAddress() + "</p></html>");
                     l.setCursor(new Cursor(Cursor.HAND_CURSOR));
                     l.setToolTipText("Show location at Google Maps");
                     l.addActionListener(new MapListener());
                     address.add(l);
                 }
             }
 
             emails.removeAll();
            emails.setVisible(false);
             emailLabel.setVisible(false);
             for (Email e : contact.getEmails()) {
                 if (!e.getEmail().isEmpty()) {
                     emailLabel.setVisible(true);
                     emails.setVisible(true);
                     JLabelButton lb = new JLabelButton(e.getEmail());
                     lb.setCursor(new Cursor(Cursor.HAND_CURSOR));
                     lb.setForeground(Color.BLUE);
                     lb.setToolTipText("Write an email");
                     lb.addActionListener(new EmailListener());
                     emails.add(lb);
                 }
             }
 
             webs.removeAll();
            webs.setVisible(false);
             websLabel.setVisible(false);
             for (Url u : contact.getUrls()) {
                 if (u.getValue() != null) {
                     websLabel.setVisible(true);
                     webs.setVisible(true);
                     JLabelButton lb = new JLabelButton(u.getValue().toString());
                     lb.setCursor(new Cursor(Cursor.HAND_CURSOR));
                     lb.setToolTipText("Go to the website");
                     lb.setForeground(Color.BLUE);
                     lb.addActionListener(new WebListener());
                     webs.add(lb);
                 }
             }
 
             icqLabel.setVisible(false);
             jabberLabel.setVisible(false);
             skypeLabel.setVisible(false);
             icq.setVisible(false);
             jabber.setVisible(false);
             skype.setVisible(false);
             for (Messenger m : contact.getMessenger()) {
                 if (m.getValue() != null) {
                     switch (m.getType()) {
                         case ICQ:
                             icq.setText(m.getValue());
                             icqLabel.setVisible(true);
                             icq.setVisible(true);
                             break;
                         case JABBER:
                             jabber.setText(m.getValue());
                             jabberLabel.setVisible(true);
                             jabber.setVisible(true);
                             break;
                         case SKYPE:
                             skype.setText(m.getValue());
                             skypeLabel.setVisible(true);
                             skype.setVisible(true);
                             break;
                     }
                 }
             }
 
             if (!contact.getPhoneNumbers().isEmpty()) {
                 phoneLabel.setVisible(true);
                 phones.setVisible(true);
                 phones.setText("<html>" + contact.getAllPhones().replaceAll(", ", "<br>") + "</html>");
             } else {
                 phoneLabel.setVisible(false);
                 phones.setVisible(false);
             }
 
             if (contact.getBirthday() != null && contact.getBirthday().getDate() != null) {
                 birthday.setVisible(true);
                 birthdayLabel.setVisible(true);
                 birthday.setText(DateFormat.getDateInstance().format(contact.getBirthday().getDate()));
             } else {
                 birthday.setVisible(false);
                 birthdayLabel.setVisible(false);
             }
 
             if (contact.getNameDay() != null && contact.getNameDay().getDate() != null) {
                 nameday.setVisible(true);
                 namedayLabel.setVisible(true);
                 nameday.setText(DateFormat.getDateInstance().format(contact.getNameDay().getDate()));
             } else {
                 nameday.setVisible(false);
                 namedayLabel.setVisible(false);
             }
 
             if (contact.getCelebration() != null && contact.getCelebration().getDate() != null) {
                 celebration.setVisible(true);
                 celebrationLabel.setVisible(true);
                 celebration.setText(DateFormat.getDateInstance().format(contact.getCelebration().getDate()));
             } else {
                 celebration.setVisible(false);
                 celebrationLabel.setVisible(false);
             }
 
             if (contact.getNote() != null && !contact.getNote().isEmpty()) {
                 note.setVisible(true);
                 noteLabel.setVisible(true);
                 note.setText("<html>" + contact.getNote().replaceAll("\n", "<br>") + "</html>");
             } else {
                 note.setVisible(false);
                 noteLabel.setVisible(false);
             }
 
             String separator = "";
             final StringBuilder groupstring = new StringBuilder();
             groups.setVisible(false);
             groupsLabel.setVisible(false);
             for (Group g : db.getAllGroupsForContact(contact)) {
                 groups.setVisible(true);
                 groupsLabel.setVisible(true);
                 groupstring.append(separator);
                 groupstring.append(g.getName());
                 separator = ", ";
             }
             groups.setText(groupstring.toString());
             
             this.maps.removeAll();
             for (Address a : contact.getAdresses()) {
                 if (!a.getAddress().isEmpty()) {
                     JLabelButton l = new JLabelButton();
                     l.setAlignmentY(Component.TOP_ALIGNMENT);
                     l.setAlignmentX(Component.LEFT_ALIGNMENT);
                     l.setCursor(new Cursor(Cursor.HAND_CURSOR));
                     l.setToolTipText("Show location at Google Maps");
                     try {
                         l.setIcon(new ImageIcon(new URL("http://maps.google.com/maps/api/staticmap?size=128x128&sensor=false&markers=" + URLEncoder.encode(a.getAddress(), "utf8"))));
                     } catch (IOException e) {
                         System.err.println(e);
                         continue;
                     }
                     l.addActionListener(new MapListener());
                     this.maps.add(l);
                 }
             }
 
             //detailScrollPane.setVisible(true);
         }
     }
 
     /**
      * Action for opening mail client
      */
     private class EmailListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent ev) {
             final JButton b = (JButton) ev.getSource();
             try {
                 Desktop.getDesktop().mail(new URI("mailto", b.getText(), null));
             } catch (URISyntaxException ex) {
                 System.err.println(ex);
             } catch (IOException ex) {
                 System.err.println(ex);
             }
         }
     }
 
     /**
      * Action for opening browser with maps
      */
     private class MapListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent ev) {
             final JButton b = (JButton) ev.getSource();
             try {
                 Desktop.getDesktop().browse(new URI("http://maps.google.com/maps?q=" + URLEncoder.encode(b.getText(), "utf8")));
             } catch (URISyntaxException ex) {
                 System.err.println(ex);
             } catch (IOException ex) {
                 System.err.println(ex);
             }
         }
     }
 
     /**
      * Action for opening browser with web
      */
     private class WebListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent ev) {
             final JButton b = (JButton) ev.getSource();
             try {
                 Desktop.getDesktop().browse(new URI(b.getText()));
             } catch (URISyntaxException ex) {
                 System.err.println(ex);
             } catch (IOException ex) {
                 System.err.println(ex);
             }
         }
     }
 }

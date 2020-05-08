 
 package ldap.editors;
 
 import java.util.*;
 import java.io.*;
 import javax.naming.*;
 import javax.naming.directory.*;
 import javax.swing.ImageIcon;
 
 import org.wings.*;
 import org.wings.session.*;
 
 
 public class JpegPhotoEditor
   implements Editor
 {
   
   BasicAttribute oldAttribute;
   
   public SComponent createComponent(Attributes attributes) {
     this.oldAttribute = null;
 
     
       SPanel photoPanel = new SPanel();
       SFileChooser chooser = new SFileChooser();
       photoPanel.setLayout(new SFlowDownLayout());
       photoPanel.add(chooser);
       photoPanel.add(new SLabel());
       return photoPanel;
     }
   
     public void setValue(SComponent component, Attribute attribute)
 	throws NamingException
     {
       this.oldAttribute = null;
       this.oldAttribute = (BasicAttribute)attribute;
 
      SPanel panel = (SPanel)
      SLabel photo = (SLabel)panel.getComponent(
       if (attribute == null) {
         photo.setText("kein Photo vorhanden");
         return;
       }
       try {
         File tmp = File.createTempFile("photo","tmp",null);
         FileOutputStream fos = new FileOutputStream(tmp);
         fos.write((byte[])attribute.get());
         fos.close();
         
        
         photo.setIcon(new FileImageIcon(tmp));
         return;
       }
       catch(java.io.FileNotFoundException e) {
         e.printStackTrace();
       }
      catch(java.io.IOException ){
         e.printStackTrace();
       }
       photo.setText("photo kann nicht angezeigt werden");
       
     }
       
       
   
   public Attribute getValue(SComponent component, String id) {
     
     SPanel panel = (SPanel)component;
     SLabel photo = (SLabel)panel.getComponent(1);
     SFileChooser chooser = (SFileChooser)panel.getComponent(0);
     Attribute attribute = new BasicAttribute(id);
     try {
       if (chooser.getSelectedFile()!=null) {
         FileInputStream fis = new FileInputStream(chooser.getSelectedFile());
         
         int bytesNr = fis.available();
         byte [] b = new byte[bytesNr];
         
         fis.read(b);
         fis.close();
         //photo.setIcon(new SImageIcon(new ImageIcon(b)));
         photo.setIcon(new FileImageIcon(chooser.getSelectedFile()));
         attribute.add(b);
         return attribute;
       }
 
     }
     catch(java.io.IOException e ) {
       e.printStackTrace();
     }
   
     return oldAttribute;
   }
 }
 

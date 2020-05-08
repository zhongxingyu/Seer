 //
 // $Id$
 // 
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 // 
 // Created: 2006-07-10
 // Creator: Etienne Gauthier
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Frame;
import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 
 import wjhk.jupload2.filedata.PictureFileData;
 import wjhk.jupload2.policies.UploadPolicy;
 
 /**
  * A maximized modal dialog box, that display the selected picture.
  * 
  * @author Etienne Gauthier
  */
 public class PictureDialog extends JDialog implements ActionListener {
 
     /**
      * 
      */
     private static final long serialVersionUID = 7802205907550854333L;
 
     JButton buttonClose;
 
     PictureFileData pictureFileData = null;
 
     PicturePanel picturePanel = null;
 
     UploadPolicy uploadPolicy = null;
 
     /**
      * Creates a new instance.
      * 
      * @param owner The parent frame.
      * @param pictureFileData The picture to manage.
      * @param uploadPolicy The upload policy which applies.
      */
     public PictureDialog(Frame owner, PictureFileData pictureFileData,
             UploadPolicy uploadPolicy) {
         super(owner, pictureFileData.getFileName(), true);
 
         this.uploadPolicy = uploadPolicy;
         this.pictureFileData = pictureFileData;
 
         // Creation of the image area
         this.picturePanel = new DialogPicturePanel(this, uploadPolicy,
                 pictureFileData);
 
         // Creation of the buttonClose button.
         this.buttonClose = new JButton(uploadPolicy.getString("buttonClose"));
         this.buttonClose.setMaximumSize(new Dimension(100, 100));
         this.buttonClose.addActionListener(this);
 
         getContentPane().add(this.buttonClose, BorderLayout.SOUTH);
         getContentPane().add(this.picturePanel);
 
         pack();
        //Correction given by 
        //setSize(getMaximumSize());  generate very high number under MAC OSX -> Applet Crash
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0, 0, screenSize.width, screenSize.height);
 
         // The dialog is modal: the next line will return when the DialogPicture
         // is hidden (to be closed, in our case)
         setVisible(true);
 
         // MEMORY LEAK CORRECTION :
 
         // Let's free some memory.
         // This is necessary, as the finalize method is not called (if anyone
         // has an explanation).
         // So, I have to manually free the memory consummed to display the
         // image, here.
         this.picturePanel.setPictureFile(null);
     }
 
     /**
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed(ActionEvent event) {
         if (event.getActionCommand() == this.buttonClose.getActionCommand()) {
             this.uploadPolicy.displayDebug(
                     "[PictureDialog] Before this.dispose()", 60);
             this.dispose();
         }
     }
 
     /**
      * Free all locked data. protected void finalize () throws Throwable {
      * super.finalize(); uploadPolicy.displayDebug("Within
      * PictureDialog.finalize()", 90); picturePanel = null; buttonClose = null; }
      */
 }

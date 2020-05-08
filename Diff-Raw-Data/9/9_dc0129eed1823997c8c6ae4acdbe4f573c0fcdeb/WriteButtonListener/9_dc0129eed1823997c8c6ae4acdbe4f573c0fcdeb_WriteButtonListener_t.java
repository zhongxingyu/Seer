 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.Locale;
 
 /**
  * Created with IntelliJ IDEA.
  * User: napster
  * Date: 4/25/13
  * Time: 3:36 PM
  * To change this template use File | Settings | File Templates.
  */
 public class WriteButtonListener implements ActionListener {
     @Override
     public void actionPerformed(ActionEvent e) {
         try {
         JFileChooser chooser = new JFileChooser();
             UIManager.put("FileChooser.fileNameLabelText", "Имя файла:");
             UIManager.put("FileChooser.saveInLabelText", "Каталог:");
             UIManager.put("FileChooser.filesOfTypeLabelText", "Типы файлов:");
             UIManager.put("FileChooser.cancelButtonText", "Отмена");
             UIManager.put("FileChooser.saveButtonText", "Сохранить");
             UIManager.put("FileChooser.saveButtonToolTipText", "Сохранить изображение");
             UIManager.put("FileChooser.cancelButtonToolTipText", "Закрыть диалог");
 
             chooser.updateUI();
         //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         int result = chooser.showSaveDialog(null);
 
         if (result == chooser.APPROVE_OPTION){
             ImageIO.write(GUI.getImage(100), "PNG", new File(chooser.getSelectedFile()+ ".png"));
         }
            chooser = null;
         } catch (IOException ex) {
             System.out.println(ex.toString());
         }
     }
 }

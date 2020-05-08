 package Clamshell;
 
 import java.io.*;
 import java.nio.charset.*;
 import javax.swing.*;
 import java.util.ArrayList;
 /**
  * Main UI for secure password management application
  * COSC 625 Fall 2013
  * @author Andrew Ramsey
  * @version 1.8
  */
 public class ClamshellUI extends javax.swing.JFrame {
     
     private ArrayList<Entry> passList; // must specify Entry type object
     private String writeHelper = "";
     
     // default designations for delimeters and padding
     private final String delimeter = "   "; // entry data delimeter
     private final String newline = "\n";
     private final String pad = "0";
     private final String placeholder = "000"; // placeholder represents blank fields inside entry object when encrypted to file
     private final Charset charset = Charset.forName("UTF-8");
     
     // the values passed in from initial UI window (authentication procedure)
     private File authFile;
     private String userName;
     private String masterPass;
     private boolean isAuthenticated = false;
     
     /**
      * Beginning insertion point of program
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         
         ClamshellUI clam = new ClamshellUI();
         clam.setLocationRelativeTo(null); // center window in middle of screen
         
         if (clam.isAuthenticated == true)
         {
             clam.setVisible(true);
         } else { System.exit(0); }
     }
     
     /**
      * UI constructor
      * Provides authentication and loads entry objects
      * Then create ClamshellUI and perform crypto IO
      */
     public ClamshellUI() {
         initComponents();
         passList = new ArrayList<Entry>(); // Must specify as <Entry> or else compiler throws Xlint - unsafe operations
         // Now authenticate the user before continuing ClamshellUI
         AuthenticateDialog dialog = new AuthenticateDialog(new javax.swing.JFrame(), true);
         dialog.setLocationRelativeTo(null);// center the auth dialog window in middle of screen
         dialog.addWindowListener(new java.awt.event.WindowAdapter() {
             @Override
             public void windowClosing(java.awt.event.WindowEvent e) {
                 System.exit(0);
             }
         });
         dialog.setVisible(true);
         // if authenticated, continue
         if (dialog.getAuthenticated() == true) { // ensure user is validated before continuing
             // make sure we pass the relevant information user provided
             //  to new UI screen before disposing of old UI screen
             authFile = dialog.getAuthFile();
             userName = dialog.getUsername();
             masterPass = dialog.getMasterPass();
             isAuthenticated = dialog.getAuthenticated();
             dialog.dispose(); // we are done authenticating
 
             if (authFile == null) // we are opening existing user's password file
             {
                 String bigSky = "";
                 String encryptedFileName = RC4Cipher(userName, masterPass);
 
                 bigSky = readFromFile(encryptedFileName);
                 System.out.println("loaded data: "+bigSky);
                 // decrypt raw data from password file by running RC4 (symmetric cipher)
                 String deciphered = byteToString(RC4(hexStringToByteArray(bigSky), hexStringToByteArray(stringToHex(masterPass))));
                 System.out.println("loaded data after RC4 decrypt: "+deciphered);
                 String[] entryLines = deciphered.split(newline);
                 System.out.println("#lines of data in file: "+entryLines.length);
                 for (int i = 0; i < entryLines.length; ++i) {
                     String[] rawEntries = entryLines[i].split(delimeter); // further break down 2D array
                     System.out.println("#entries total: "+rawEntries.length);
                     //first decrypt the AES encrypted password
                     //ciphertext, key, IV
                    System.out.println("AES Vars: "+rawEntries[3] + " " + stringToHex(masterPass) + " " + rawEntries[3]);
                    //String decryptedPassword = decryptAES(rawEntries[3], masterPass, rawEntries[2]);//**********************************************************************
                     // sname, uname, IV, pass
                    //System.out.println("Entry Data: "+rawEntries[0] + " " + rawEntries[1] + " " +  rawEntries[2] + " " +  decryptedPassword);
                    passList.add(new Entry(rawEntries[0], rawEntries[1], rawEntries[2], rawEntries[3]));
                     //passList.add(new Entry(rawEntries[0], rawEntries[1], rawEntries[2], rawEntries[3]));
                     
                     deleteButton.setEnabled(true);
                     updateTextArea();
                     updatePasswordFile();
 
                 }
             }// otherwise, deny access - shutdown program
             
         }else {
                 System.exit(0);
             }
     }
     
     
     // initComponents() below: 
     //Auto-Generated Code from Java Swing GUI builder 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jDialog1 = new javax.swing.JDialog();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTextPane2 = new javax.swing.JTextPane();
         sname = new javax.swing.JTextField();
         uname = new javax.swing.JTextField();
         pass = new javax.swing.JTextField();
         addButton = new javax.swing.JButton();
         deleteButton = new javax.swing.JButton();
         slider = new javax.swing.JSlider();
         RERC4 = new javax.swing.JButton();
 
         javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
         jDialog1.getContentPane().setLayout(jDialog1Layout);
         jDialog1Layout.setHorizontalGroup(
             jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 400, Short.MAX_VALUE)
         );
         jDialog1Layout.setVerticalGroup(
             jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 300, Short.MAX_VALUE)
         );
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         setIconImages(null);
         setMinimumSize(new java.awt.Dimension(554, 255));
         setName("clamshell"); // NOI18N
 
         jTextPane2.setEditable(false);
         jTextPane2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         jTextPane2.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
         jTextPane2.setDoubleBuffered(true);
         jTextPane2.setMaximumSize(new java.awt.Dimension(1280, 1280));
         jTextPane2.setName(""); // NOI18N
         jScrollPane2.setViewportView(jTextPane2);
 
         sname.setToolTipText("Enter Service Name");
         sname.setName(""); // NOI18N
 
         uname.setToolTipText("Enter User Name");
 
         pass.setToolTipText("Enter Password");
 
         addButton.setText("Add Entry");
         addButton.setToolTipText("Add Entry to Password List");
         addButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addButtonActionPerformed(evt);
             }
         });
 
         deleteButton.setText("Delete Entry");
         deleteButton.setToolTipText("Delete Entry from Password List");
         deleteButton.setEnabled(false);
         deleteButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 deleteButtonActionPerformed(evt);
             }
         });
 
         slider.setMaximum(0);
         slider.setPaintTicks(true);
         slider.setSnapToTicks(true);
         slider.setToolTipText("Adjust to go thru entry objects");
         slider.setName(""); // NOI18N
 
         RERC4.setLabel("Re-RC4");
         RERC4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 RERC4ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(sname, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                     .addComponent(RERC4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(uname, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(pass, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(addButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                         .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(slider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(8, 8, 8)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(sname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(uname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(pass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(addButton)
                     .addComponent(deleteButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(RERC4)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(slider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         RERC4.getAccessibleContext().setAccessibleName("rerc4");
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
     //DO NO TOUCH ABOVE
     
     
     /**
      * Update the text area field when passList is updated
      */
     private void updateTextArea() {
         String bigSky = "";
         String bigSky2 = ""; // used to generate raw data for password file output including init vector value to decrypt password values with AES
         for (int i = 0; i < passList.size(); ++i) {
            bigSky += i + delimeter + passList.get(i).getServiceName() + delimeter + passList.get(i).getUserName() + delimeter + decryptAES(passList.get(i).getPassword(), masterPass, passList.get(i).IV) + newline;
             // sname, uname, IV, pass ... encrypt password using AES-256
            //String encryptedPass = encryptAES(passList.get(i).getPassword(),masterPass,passList.get(i).IV);
            //System.out.println(encryptedPass);
            bigSky2 += passList.get(i).getServiceName() + delimeter + passList.get(i).getUserName() + delimeter + passList.get(i).IV + delimeter + passList.get(i).getPassword() + newline;
             // non-aes mode below:
             //bigSky2 += passList.get(i).getServiceName() + delimeter + passList.get(i).getUserName() + delimeter + passList.get(i).IV + delimeter + passList.get(i).getPassword() + newline;
         }
         jTextPane2.setText(bigSky); 
         jTextPane2.updateUI(); // update jTextField2 (text area)
         
         writeHelper = RC4Cipher(bigSky2, masterPass); // encrypt using RC4 for writing to File
         //System.out.println("output to file: "+writeHelper);
     }
     
     /**
      * Remove entry from text area based on the user input (number provided)
      * @param evt 
      */
     private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        
        if (!passList.isEmpty())
        {   
             try{
                 int entryNumber = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter Entry Number:", "Delete Entry?", JOptionPane.QUESTION_MESSAGE));
                 
                 if (entryNumber >= 0 && entryNumber < passList.size())
                 {
                     passList.remove((int)entryNumber); // delete entry from flexible object type (arraylist)
                 }
                 else
                 {
                     JOptionPane.showMessageDialog(null, "Entry Number Out of Bounds", "Error", JOptionPane.ERROR_MESSAGE);
                 }
             }
             catch(Exception e){
                 JOptionPane.showMessageDialog(null, "Entry Number Out of Bounds", "Error", JOptionPane.ERROR_MESSAGE);
             }
             if (passList.isEmpty()) // ensure turn off button for case user deleted last entry and there are no entries left
             {
                 deleteButton.setEnabled(false);
             }
             else
             {
                 deleteButton.setEnabled(true);
             }
             updateTextArea();
             updatePasswordFile();
        }
        else
        {
            deleteButton.setEnabled(false);
        }
     }//GEN-LAST:event_deleteButtonActionPerformed
         
     /**
      * Write RC4-encrypted entry data to file
      */
     private void updatePasswordFile() {
         
         if (authFile == null) {
             //String encryptedFileName = RC4Cipher(userName, masterPass);
             authFile = new File(RC4Cipher(userName, masterPass));
         }
         try {
         writeToFile();
         }catch (Exception e) {
             System.exit(66);
         }
     }
     
     public void writeToFile() throws IOException {
     OutputStream out = new FileOutputStream(authFile);
     Closeable stream = out;
     try {
       Writer writer = new OutputStreamWriter(out, charset);
       stream = writer;
       writer.write(writeHelper);
     } finally {
       stream.close();
     }
   }
 
     public String readFromFile(String file) {
 
         try {
             InputStream in = new FileInputStream(file);
             Closeable stream = in;
             Reader reader = new InputStreamReader(in, charset);
             stream = reader;
             StringBuilder inputBuilder = new StringBuilder();
             char[] buffer = new char[1024];
             while (true) {
                 int readCount = reader.read(buffer);
                 if (readCount < 0) {
                     break;
                 }
                 inputBuilder.append(buffer, 0, readCount);
             }
             stream.close();
             return inputBuilder.toString();
 
         } catch (Exception e) {
         }
         return null;
     }
     
     /**
      * Add Entry From three inputs on Clamshell UI
      * Then update passList, UI
      * @param evt 
      */
     private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
         
         String sn = sname.getText();
         String un = uname.getText();
         String pss = pass.getText();
         
         if (sn.isEmpty()) // create placeholder variables so that when decrypted, delimeters are not skipped
         {
             sn = placeholder;
         }
         if (un.isEmpty())
         {
             un = placeholder;
         }
         if (pss.isEmpty())
         {
             pss = placeholder;
         }
        Entry n = new Entry(sn, un, pss);
        n.setPassword(encryptAES(pss, masterPass, n.IV));
        passList.add(n);
         
         updateTextArea();
         updatePasswordFile();
         
         sname.setText(""); // reset UI fields
         uname.setText("");
         pass.setText("");
         
         if (!passList.isEmpty())
         {
             deleteButton.setEnabled(true); // now we can delete entries
         }
         else
         {
             deleteButton.setEnabled(false); 
         }
         
     }//GEN-LAST:event_addButtonActionPerformed
 
     private void RERC4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RERC4ActionPerformed
         
     }//GEN-LAST:event_RERC4ActionPerformed
         
     /**
      * Convert user name input to RC4-encrypted string
      * @return 
      */
     private String RC4Cipher(String u, String p)
     {
         return byteArrayToHexString(RC4(hexStringToByteArray(stringToHex(u)), hexStringToByteArray(stringToHex(p))));
     }
     
     /**
      * RC4 Cipher in Bytes
      *
      * @param unlocked
      * @param key
      * @return
      */
     public byte[] RC4(byte[] unlocked, byte[] key) {
         byte[] S = new byte[256];
         for (int temp = 0; temp < 256; temp++) {
             S[temp] = (byte) temp;
         }
         int j = 0;
         byte swap;
         int i;
         for (i = 0; i < 256; i++)//KSA
         {
             j = (j + key[i % key.length] + S[i]) & 255;
             swap = S[i];
             S[i] = S[j];
             S[j] = swap;
         }
         i = j = 0;
         byte[] keystream = new byte[unlocked.length];
         for (int place = 0; place < unlocked.length; place++)//PRGA //Middle argument is how much stream to gen
         {
             i = (i + 1) & 255;
             j = (j + S[i]) & 255;
             swap = S[i];
             S[i] = S[j];
             S[j] = swap;
             keystream[place] = S[(S[i] + S[j]) & 255];
         }
         byte[] ciphertext = new byte[keystream.length];
         for (int place = 0; place < ciphertext.length; place++) {
             ciphertext[place] = (byte) (keystream[place] ^ unlocked[place]);
         }
         return ciphertext;
     }
     
     /**
      * Decrypt ciphertext password
      * @param cipher - plaintext in hexadecimal
      * @param k - key as plain text
      * @param iv - initialization vector (already hex)
      * @return String
      */
     private String decryptAES(String cipher, String k, String iv)
     {
         // no need to convert to hexadecimal - input text is already hexadecimal
         // init vector already in hexadecimal - no need to convert
         String key = stringToHex(k);
         String decryptedPass = ""; // what we return from method
         // ensure key length to 64 chars hex
         // IV length is already set to 32 chars hex by Entry object
         if (key.length() < 64)
         {
             while (key.length() < 64){
                 key += pad; // pad with zeros  
             }
         }
         else if (key.length() > 64)
         {
             key = key.substring(0, 64); // truncate
         }
         //pad the ciphertext if not multiple of 32
         int r; // remainder of characters need to be padded to end of hex block on plaintext
         int d; // number of rounds to run AES (number of 32 character blocks)
         int length = cipher.length();
         
        if (length <= 32)
         {
            r = 32 - length;
           d = 1; // only one round required
         }
         else
         {
            r = length % 32; // r = how many we must pad to end 
            d = (int) length / 32; // d = number of AES rounds required to cycle thru to encipher all plaintext 
         }
         System.out.println("len: "+cipher.length()+"\n r: "+r+" d: "+d);
         for (int i = length; i < length+r; ++i)
         {
             cipher += pad;
             System.out.println(cipher);
         }
         // now we decipher the padded text
         Yaes aes = new Yaes(key);
         for (int i = 0; i < d; ++i)
         {
             decryptedPass += aes.decryptString(cipher.substring(i*32, i*32+32), iv);
         }// we are handed back 32 char hex string from AES 256 decryption
         // convert it back to plaintext
         return byteToString(hexStringToByteArray(decryptedPass));
     }
     
     /**
      * Encrypt using AES 256
      * @param plain - plaintext as plain text
      * @param k - key as plain text
      * @param iv - initialization vector (already hex)
      * @return String - encrypted text as hexadecimal
      */
     private String encryptAES(String p, String k, String iv)
     {
         // convert to hexadecimal
         String plain = stringToHex(p);
         String key = stringToHex(k);
         System.out.println("plain: " + plain + "key: " + key);
         // init vector already in hexadecimal - no need to convert
         String encryptedPass = "";
         // ensure key length to 64 chars hex (64 * 4 = 256)
         // IV length is already set to 32 chars hex by Entry object
         if (key.length() < 64)
         {
             while (key.length() < 64){
                 key += pad; // pad with zeros
             }
         }
         else if (key.length() > 64)
         {
             key = key.substring(0, 64); // truncate
         }
         // parse password data into & out of AES 32 hex at a time
         // first ensure that hex chars are multiple of 32
         int r; // remainder of characters need to be padded to end of hex block on plaintext
         int d; // number of rounds to run AES (number of 32 character blocks)
         int length = plain.length();
         
         if (length < 32)
         {
            r = 32 - length;
           d = 1; // only one round required
         }
         else
         {
            r = length % 32; // r = how many we must pad to end 
            d = (int) length / 32; // d = number of AES rounds required to cycle thru to encipher all plaintext 
         }
         //System.out.println("len: "+plain.length()+"\n r: "+r+" d: "+d);
         for (int i = length; i < length+r; ++i)
         {
             plain += pad;
             //System.out.println(plain);
         }
         //System.out.println("ookey booky");
         // now we encipher the padded text
         Yaes aes = new Yaes(key);
         
         for (int i = 0; i < d; ++i) // starts at round one
         {
             encryptedPass += aes.encryptString(plain.substring(i*32, i*32+32), iv);
         }
         return encryptedPass;
     }
     
     private byte[] hexStringToByteArray(String s) {
         byte[] b = new byte[s.length() / 2];
         for (int i = 0; i < b.length; i++) {
             int index = i * 2;
             int v = Integer.parseInt(s.substring(index, index + 2), 16);
             b[i] = (byte) v;
         }
         return b;
     }
     
     private String byteArrayToHexString(byte[] b) {
         String result = "";
         for (int i = 0; i < b.length; i++) {
             result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
         }
         return result;
     }
     // convert back to plaintext
     public String byteToString(byte[] in){
         String output = "";
         for(int a = 0;a < in.length;a++)
         {
             output += (char)in[a];
         }
         return output;
     }
     // convert plaintext string to hexadecimal (length is always x2 plaintext)
     private String stringToHex(String s){
         
         StringBuilder buf = new StringBuilder(200);
         for (char ch : s.toCharArray()) {
             buf.append(String.format("%02x", (int) ch));
         }
         return buf.toString();
         //return byteArrayToHexString(s.getBytes());
     }
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton RERC4;
     private javax.swing.JButton addButton;
     private javax.swing.JButton deleteButton;
     private javax.swing.JDialog jDialog1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JTextPane jTextPane2;
     private javax.swing.JTextField pass;
     private javax.swing.JSlider slider;
     private javax.swing.JTextField sname;
     private javax.swing.JTextField uname;
     // End of variables declaration//GEN-END:variables
 }

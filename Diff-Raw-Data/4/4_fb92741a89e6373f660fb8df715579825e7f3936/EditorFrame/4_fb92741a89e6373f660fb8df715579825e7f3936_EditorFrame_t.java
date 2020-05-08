 /**********************************************************************/
 /* Copyright 2013 KRV                                                 */
 /*                                                                    */
 /* Licensed under the Apache License, Version 2.0 (the "License");    */
 /* you may not use this file except in compliance with the License.   */
 /* You may obtain a copy of the License at                            */
 /*                                                                    */
 /*  http://www.apache.org/licenses/LICENSE-2.0                        */
 /*                                                                    */
 /* Unless required by applicable law or agreed to in writing,         */
 /* software distributed under the License is distributed on an        */
 /* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,       */
 /* either express or implied.                                         */
 /* See the License for the specific language governing permissions    */
 /* and limitations under the License.                                 */
 /**********************************************************************/
 package gui.graphical;
 
 // Graphical Libraries (AWT)
 import java.awt.*;
 
 // Graphical Libraries (Swing)
 import javax.swing.*;
 import javax.swing.text.*;
 
 class EditorFrame extends JFrame
 {
     JTextPane txtPane;
     
     EditorFrame()
     {
         this.setTitle("Robot's Battle - Editor");
         this.setSize(640,800);
         
         txtPane = new JTextPane();
         txtPane.setFont(new Font("Serif", Font.PLAIN, 24));
         
         SimpleAttributeSet red = new SimpleAttributeSet();
         StyleConstants.setForeground(red, Color.RED);
         StyleConstants.setBold(red, true);
        
         append("if", red);
         append(" ", null);
         
         this.add(new JScrollPane(txtPane), BorderLayout.CENTER);
         
         SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() { setVisible(true); }
             });
     }
     
     void append(String s, AttributeSet attributes)
     {
         Document d = txtPane.getDocument();
         
         try{ d.insertString(d.getLength(), s, attributes); }
         catch(BadLocationException b){}
     }
     
     String getText()
     {
         return this.txtPane.getText();
     }
 }

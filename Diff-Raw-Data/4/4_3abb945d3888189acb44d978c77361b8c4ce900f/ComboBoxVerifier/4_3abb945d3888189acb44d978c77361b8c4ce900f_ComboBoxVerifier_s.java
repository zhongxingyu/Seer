 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.gov.saudecaruaru.bpai.gui.verifiers;
 
 import br.gov.saudecaruaru.bpai.gui.MessagesErrors;
 import java.awt.Color;
 import java.awt.Component;
 import javax.swing.InputVerifier;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 
 /**
  *
  * @author Junior Pires
  */
   //classe validadora para comboBoxs
 public class ComboBoxVerifier extends InputVerifier{
         private Component component=null;
         private String fieldName; 
         public ComboBoxVerifier(Component component, String fieldName) {
             this.component=component;
             this.fieldName = fieldName;
             
         }
 
         @Override
             public boolean verify(JComponent input) {
                 JComboBox jComboBox = (JComboBox) input;
                 
                if (jComboBox.getModel().getSize()!=0 && jComboBox.getSelectedItem()==null ) {  
                      MessagesErrors.erro(component,jComboBox,fieldName+" Obrigatório");
                 return false;  
                 } 
                 //seta cor branca
                 jComboBox.setBackground(Color.GRAY); 
                 return true;
             }
 }

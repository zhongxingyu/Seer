 /*
  * Copyright (c) 2010, Soar Technology, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * 
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  * 
  * * Neither the name of Soar Technology, Inc. nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without the specific prior written permission of Soar Technology, Inc.
  * 
  * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * Created on Mar 30, 2009
  */
 package com.soartech.simjr.ui.editor;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 
 import javax.swing.InputVerifier;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.undo.UndoableEdit;
 
 import com.soartech.simjr.scenario.ModelChangeEvent;
 import com.soartech.simjr.scenario.ModelChangeListener;
 import com.soartech.simjr.scenario.ScriptBlockElement;
 import com.soartech.simjr.scenario.edits.ChangeScriptBlockEdit;
 
 /**
  * @author ray
  */
 public class ScriptEditPanel extends JPanel implements ModelChangeListener
 {
     private static final long serialVersionUID = -4762375303730867198L;
 
     static
     {
         jsyntaxpane.DefaultSyntaxKit.initKit();
     }
     
     private final UndoService undoService;
     private final JEditorPane area = new JEditorPane();
     private ScriptBlockElement script;
     
     public ScriptEditPanel(UndoService undoService, int rows)
     {
         super(new BorderLayout());
 
         this.undoService = undoService;
      
         if(rows > 0)
         {
             area.setMinimumSize(new Dimension(1, rows * 15));
             area.setPreferredSize(new Dimension(1, rows * 15));
         }
         add(new JScrollPane(area), BorderLayout.CENTER);
         
         area.setContentType("text/javascript"); // TODO: connect to script type field
         area.setFont(new Font("Monospaced", Font.PLAIN, 12));
         area.setInputVerifier(new InputVerifier() {
 
             @Override
             public boolean verify(JComponent input)
             {
                 updateScriptText();
                 return true;
             }});
 
         setScript(null);
     }
     
     /**
      * @return the script
      */
     public ScriptBlockElement getScript()
     {
         return script;
     }
     
     /**
      * @param script the script to set
      */
     public void setScript(ScriptBlockElement script)
     {
         if(this.script != null)
         {
             this.script.getModel().removeModelChangeListener(this);
             if(!this.script.getText().equals(area.getText()))
             {
                 updateScriptText();
             }
         }
         this.script = script;
         if(this.script != null)
         {
             area.setEnabled(true);
             this.script.getModel().addModelChangeListener(this);
             area.setText(this.script.getText());
         }
         else
         {
             area.setEnabled(false);
             area.setText("");
         }
     }
     
     private void updateScriptText()
     {
         if(script == null)
         {
             return;
         }
         
        final String newText = area.getText();
        final String oldText = script.getText();
         if(!oldText.equals(newText))
         {
             UndoableEdit edit = script.setText(newText);
             if(edit != null)
             {
                 undoService.addEdit(new ChangeScriptBlockEdit(script, oldText));
             }
         }
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.scenario.ModelChangeListener#onModelChanged(com.soartech.simjr.scenario.ModelChangeEvent)
      */
     public void onModelChanged(ModelChangeEvent e)
     {
         if(script != null && e.source == script && e.property.equals(ScriptBlockElement.TEXT))
         {
             area.setText(script.getText());
         }
     }
 }

 /*
  * Copyright (c) 2006-2012 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.dialogs.channellist;
 
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JLabel;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Provides a UI to search for channels in DMDirc.
  */
 public class ChannelListDialog extends StandardDialog implements
         ActionListener {
 
     /** Serial version UID. */
     private static final long serialVersionUID = 1L;
     /** List panel. */
     private ChannelListPanel list;
     /** Size label. */
     private JLabel total;
 
     public ChannelListDialog(final SwingController controller) {
         super(controller, ModalityType.MODELESS);
         setTitle("Channel List");
         total = new JLabel("No results.");
         list = new ChannelListPanel(controller.getMainFrame().getActiveFrame()
                 .getContainer().getServer(), total);
         layoutComponents();
         getCancelButton().setText("Close");
         getCancelButton().addActionListener(this);
     }
 
     /** Lays out the components in this dialog. */
     private void layoutComponents() {
         setLayout(new MigLayout("fill, wmin 40%, hmin 40%, hidemode 3"));
         add(list, "grow, push, wrap, spanx 2");
         add(total, "left");
         add(getCancelButton(), "right");
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Action event
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         dispose();
     }
 
 }

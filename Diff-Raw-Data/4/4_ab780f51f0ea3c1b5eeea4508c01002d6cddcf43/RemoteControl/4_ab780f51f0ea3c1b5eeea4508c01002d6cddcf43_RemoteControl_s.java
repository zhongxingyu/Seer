/* $Id: RemoteControl.java,v 1.3 2005-08-22 16:24:37 hampelratte Exp $
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its 
  *    contributors may be used to endorse or promote products derived from this 
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package lazybones;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import de.hampelratte.svdrp.Response;
 import de.hampelratte.svdrp.commands.CHAN;
 
 /**
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net </a>
  * 
  */
 public class RemoteControl extends JPanel implements ActionListener {
 
     private static final long serialVersionUID = 4617969625415777142L;
 
     private static final util.ui.Localizer mLocalizer = util.ui.Localizer
             .getLocalizerFor(RemoteControl.class);
 
     private NumberBlock numBlock;
 
     private NavigationBlock navBlock;
 
     private ColorButtonBlock colorButtonBlock;
 
     private JButton watch = new JButton(mLocalizer.msg("Watch", "Watch"));
 
     private LazyBones parent;
 
     public RemoteControl(LazyBones parent) {
         this.parent = parent;
         initGUI();
     }
 
     private void initGUI() {
         setLayout(new GridBagLayout());
 
         numBlock = new NumberBlock();
         add(numBlock, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 10, 5), 0, 0));
         navBlock = new NavigationBlock();
         add(navBlock, new GridBagConstraints(0, 1, 2, 1, 0.1, 0.1,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(10, 5, 5, 5), 0, 0));
         colorButtonBlock = new ColorButtonBlock();
         add(colorButtonBlock, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 10, 5), 0, 0));
 
        watch.addActionListener(Controller.getController());
         watch.addActionListener(this);
         add(watch, new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(10, 5, 5, 5), 0, 0));
     }
 
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == watch) {
             Response res = VDRConnection.send(new CHAN());
             if (res != null && res.getCode() == 250) {
                 int chan = Integer.parseInt(res.getMessage().split(" ")[0]);
                 Player.play(chan, parent);
             }
         }
     }
 }

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vysichart;
 
 /**
  *
  * @author Todd Perry
  */
 public class GraphicalUserInterface extends javax.swing.JFrame {
 
     /**
      * Creates new form GraphicalUserInterface
      */
     private static Project project;
     private Task pertBreakdownTask;
 
 
     public GraphicalUserInterface(Project project) {
         this.project = project;
         pertBreakdownTask = null;
         initComponents();
         refresh();
     }
 
     public GraphicalUserInterface() {
         project = new Project("No Project Set", "No Filepath Set");
         pertBreakdownTask = null;
         initComponents();
         refresh();
     }
 
     public void setPERTBreakdown(Task breakdown) {
         // changes PERT breakdown task
         pertBreakdownTask = breakdown;
     }
 
     public Project getProject() {
         return project;
     }
 
     public void setProject(Project newProject) {
         this.project = newProject;
     }
 
     public void reRender() {
         
         // attempt at dynamic width
         
         
 
         // reinstate tabbed render pane
         this.remove(tabbedRenderPane);
         tabbedRenderPane = new javax.swing.JTabbedPane();
         this.add(tabbedRenderPane);
 
         int renderPos = tabbedRenderPane.getSelectedIndex();
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
                 layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(layout.createSequentialGroup()
                 .addComponent(tabbedRenderPane, javax.swing.GroupLayout.PREFERRED_SIZE, 908, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE))
                 .addComponent(projectWrap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap()));
         layout.setVerticalGroup(
                 layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addGap(17, 17, 17)
                 .addComponent(projectWrap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(tabbedRenderPane, javax.swing.GroupLayout.PREFERRED_SIZE, 437, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap()));
 
 
         tabbedRenderPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         tabbedRenderPane.repaint();
         tabbedRenderPane.setSelectedIndex(renderPos);
 
 
         // re renders panels
         this.remove(ganttWrap);
         ganttWrap = new GanttRender(project.getGantt());
         this.add(ganttWrap);
 
         // re-instate gantt object
 
         ganttWrap.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Gantt", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, java.awt.Color.black));
 
         javax.swing.GroupLayout ganttWrapLayout = new javax.swing.GroupLayout(ganttWrap);
         ganttWrap.setLayout(ganttWrapLayout);
         ganttWrapLayout.setHorizontalGroup(
                 ganttWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGap(0, 855, Short.MAX_VALUE));
         ganttWrapLayout.setVerticalGroup(
                 ganttWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGap(0, 463, Short.MAX_VALUE));
 
         ganttScroll.setViewportView(ganttWrap);
 
         tabbedRenderPane.addTab("Gantt Chart", ganttScroll);
 
 
 
         this.invalidate();
         ganttWrap.repaint();
 
 
         this.remove(PERTWrap);
         PERTWrap = new PERTRender(project.getPERT(), pertBreakdownTask);
         this.add(PERTWrap);
 
         // re-instate PERT object
         PERTWrap.setBorder(javax.swing.BorderFactory.createTitledBorder("PERT"));
 
         javax.swing.GroupLayout PERTWrapLayout = new javax.swing.GroupLayout(PERTWrap);
         PERTWrap.setLayout(PERTWrapLayout);
         PERTWrapLayout.setHorizontalGroup(
                 PERTWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGap(0, 855, Short.MAX_VALUE));
         PERTWrapLayout.setVerticalGroup(
                 PERTWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGap(0, 463, Short.MAX_VALUE));
 
         pertScroll.setViewportView(PERTWrap);
 
         tabbedRenderPane.addTab("PERT Chart", pertScroll);
 
 
         this.invalidate();
         PERTWrap.repaint();
 
 
 
         this.remove(WBTWrap);
         WBTWrap = new WBTRender(project.getWBT());
         this.add(WBTWrap);
 
         // re-instate WBT object
         WBTWrap.setBorder(javax.swing.BorderFactory.createTitledBorder("WBT"));
         
         int height = WBTWrap.getRenderHeight();
         int width = WBTWrap.getRenderWidth();
 
         javax.swing.GroupLayout WBTWrapLayout = new javax.swing.GroupLayout(WBTWrap);
         WBTWrap.setLayout(WBTWrapLayout);
         WBTWrapLayout.setHorizontalGroup(
                 WBTWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGap(0, 801, Short.MAX_VALUE));
         WBTWrapLayout.setVerticalGroup(
                 WBTWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGap(0, 469, Short.MAX_VALUE));
         
         WBTWrap.setPreferredSize(new java.awt.Dimension(width, height)); // set dimenstions
         wbtScroll.setViewportView(WBTWrap);
 
         tabbedRenderPane.addTab("Work Breakdown Tree", wbtScroll);
 
         this.invalidate();
         WBTWrap.repaint();
 
 
         // TODO reset tab
 
 
     }
 
     public void refresh() {
         projectName.setText(project.getName());
         projectPath.setText(project.getFilePath());
         projectSize.setText(String.valueOf(project.getNumberOfTasks()));
         reRender();
         ganttWrap.repaint(); //
         PERTWrap.repaint();    // Refreshes Render Panels
         WBTWrap.repaint();       //
     }
 
     public int getCurrentTab() {
         return tabbedRenderPane.getSelectedIndex();
     }
     /*
      * setTab switches the current focussed tab to that passed through the
      * parameter.
      * e.g. setTab(0) - Gantt Chart tab is now selected.
      *      setTab(1) - PERT Chart tab is now selected.
      *      setTab(2) - WBT tab is now selected.
      * 
      * can be swapped for a more user-friendly String-based switch at the cost
      * of efficiency.
      */
 
     public void setTab(int tab) {
         tabbedRenderPane.setSelectedIndex(tab);
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         tabbedRenderPane = new javax.swing.JTabbedPane();
         wbtScroll = new javax.swing.JScrollPane();
         WBTWrap = new WBTRender(project.getWBT());
         pertScroll = new javax.swing.JScrollPane();
         PERTWrap = new PERTRender(project.getPERT(), pertBreakdownTask);
         ganttScroll = new javax.swing.JScrollPane();
         ganttWrap = new GanttRender(project.getGantt());
         projectWrap = new javax.swing.JPanel();
         lblProject = new javax.swing.JLabel();
         pjName = new javax.swing.JLabel();
         pjFP = new javax.swing.JLabel();
         pjSize = new javax.swing.JLabel();
         projectName = new javax.swing.JLabel();
         projectPath = new javax.swing.JLabel();
         projectSize = new javax.swing.JLabel();
         vysilogo = new javax.swing.JLabel();
         uoplogo = new javax.swing.JLabel();
         menuBar = new javax.swing.JMenuBar();
         Files = new javax.swing.JMenu();
         filesOpen = new javax.swing.JMenuItem();
         filesNew = new javax.swing.JMenuItem();
         filesSave = new javax.swing.JMenuItem();
         filesSaveAs = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JPopupMenu.Separator();
         filesRefresh = new javax.swing.JMenuItem();
         Edit = new javax.swing.JMenu();
         editUndo = new javax.swing.JMenuItem();
         editRedo = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JPopupMenu.Separator();
         editCut = new javax.swing.JMenuItem();
         editCopy = new javax.swing.JMenuItem();
         editPaste = new javax.swing.JMenuItem();
         Tasks = new javax.swing.JMenu();
         tasksAdd = new javax.swing.JMenuItem();
         editTask = new javax.swing.JMenuItem();
         configDependancies = new javax.swing.JMenuItem();
         charts = new javax.swing.JMenu();
         configPERT = new javax.swing.JMenuItem();
         Settings = new javax.swing.JMenu();
         Help = new javax.swing.JMenu();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("VysiChart");
         setResizable(false);
 
         tabbedRenderPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         tabbedRenderPane.setRequestFocusEnabled(false);
 
         WBTWrap.setBorder(javax.swing.BorderFactory.createTitledBorder("WBT"));
 
         javax.swing.GroupLayout WBTWrapLayout = new javax.swing.GroupLayout(WBTWrap);
         WBTWrap.setLayout(WBTWrapLayout);
         WBTWrapLayout.setHorizontalGroup(
             WBTWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 887, Short.MAX_VALUE)
         );
         WBTWrapLayout.setVerticalGroup(
             WBTWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 382, Short.MAX_VALUE)
         );
 
         wbtScroll.setViewportView(WBTWrap);
 
         tabbedRenderPane.addTab("Work Breakdown Tree", wbtScroll);
 
         PERTWrap.setBorder(javax.swing.BorderFactory.createTitledBorder("PERT"));
 
         javax.swing.GroupLayout PERTWrapLayout = new javax.swing.GroupLayout(PERTWrap);
         PERTWrap.setLayout(PERTWrapLayout);
         PERTWrapLayout.setHorizontalGroup(
             PERTWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 887, Short.MAX_VALUE)
         );
         PERTWrapLayout.setVerticalGroup(
             PERTWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 382, Short.MAX_VALUE)
         );
 
         pertScroll.setViewportView(PERTWrap);
 
         tabbedRenderPane.addTab("PERT Chart", pertScroll);
 
         ganttWrap.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Gantt", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, java.awt.Color.black));
 
         javax.swing.GroupLayout ganttWrapLayout = new javax.swing.GroupLayout(ganttWrap);
         ganttWrap.setLayout(ganttWrapLayout);
         ganttWrapLayout.setHorizontalGroup(
             ganttWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 887, Short.MAX_VALUE)
         );
         ganttWrapLayout.setVerticalGroup(
             ganttWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 382, Short.MAX_VALUE)
         );
 
         ganttScroll.setViewportView(ganttWrap);
 
         tabbedRenderPane.addTab("Gantt Chart", ganttScroll);
 
         projectWrap.setRequestFocusEnabled(false);
 
         lblProject.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         lblProject.setText("Project Information");
 
         pjName.setText("Project Name:");
 
         pjFP.setText("File Path:");
 
         pjSize.setText("Number Of Tasks:");
 
         projectName.setText("-");
 
         projectPath.setText("-");
 
         projectSize.setText("-");
 
         vysilogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/vysichart/vysichart logo.png"))); // NOI18N
 
         uoplogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/vysichart/vysichart uop.png"))); // NOI18N
         uoplogo.setToolTipText("");
 
         javax.swing.GroupLayout projectWrapLayout = new javax.swing.GroupLayout(projectWrap);
         projectWrap.setLayout(projectWrapLayout);
         projectWrapLayout.setHorizontalGroup(
             projectWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(projectWrapLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(vysilogo, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addGroup(projectWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblProject)
                     .addGroup(projectWrapLayout.createSequentialGroup()
                         .addGroup(projectWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(pjName)
                             .addComponent(pjFP)
                             .addComponent(pjSize))
                         .addGap(17, 17, 17)
                         .addGroup(projectWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(projectName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(projectPath, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(projectSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                 .addGap(18, 18, 18)
                 .addComponent(uoplogo, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         projectWrapLayout.setVerticalGroup(
             projectWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(projectWrapLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(projectWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(projectWrapLayout.createSequentialGroup()
                         .addComponent(lblProject)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(projectWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(pjName)
                             .addComponent(projectName))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(projectWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(pjFP)
                             .addComponent(projectPath))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(projectWrapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(pjSize)
                             .addComponent(projectSize)))
                     .addComponent(vysilogo)
                     .addComponent(uoplogo))
                 .addContainerGap(40, Short.MAX_VALUE))
         );
 
         menuBar.setPreferredSize(new java.awt.Dimension(181, 25));
 
         Files.setText("File");
 
         filesOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
         filesOpen.setText("Open");
         Files.add(filesOpen);
 
         filesNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
         filesNew.setText("New");
         filesNew.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 filesNewActionPerformed(evt);
             }
         });
         Files.add(filesNew);
 
         filesSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
         filesSave.setText("Save");
         Files.add(filesSave);
 
         filesSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
         filesSaveAs.setText("Save As");
         Files.add(filesSaveAs);
         Files.add(jSeparator1);
 
         filesRefresh.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
         filesRefresh.setText("Refresh");
         filesRefresh.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 filesRefreshActionPerformed(evt);
             }
         });
         Files.add(filesRefresh);
 
         menuBar.add(Files);
 
         Edit.setText("Edit");
 
         editUndo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
         editUndo.setText("Undo");
         editUndo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editUndoActionPerformed(evt);
             }
         });
         Edit.add(editUndo);
 
         editRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
         editRedo.setText("Redo");
         Edit.add(editRedo);
         Edit.add(jSeparator2);
 
         editCut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
         editCut.setText("Cut");
         Edit.add(editCut);
 
         editCopy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
         editCopy.setText("Copy");
         editCopy.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editCopyActionPerformed(evt);
             }
         });
         Edit.add(editCopy);
 
         editPaste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
         editPaste.setText("Paste");
         Edit.add(editPaste);
 
         menuBar.add(Edit);
 
         Tasks.setText("Tasks");
 
         tasksAdd.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
         tasksAdd.setText("Add New Task");
         tasksAdd.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tasksAddActionPerformed(evt);
             }
         });
         Tasks.add(tasksAdd);
 
         editTask.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
         editTask.setText("Edit/Delete Task");
         editTask.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editTaskActionPerformed(evt);
             }
         });
         Tasks.add(editTask);
 
         configDependancies.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
         configDependancies.setText("Configure Dependancies");
         Tasks.add(configDependancies);
 
         menuBar.add(Tasks);
 
         charts.setText("Charts");
 
         configPERT.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK));
         configPERT.setText("Configure PERT");
         configPERT.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 configPERTActionPerformed(evt);
             }
         });
         charts.add(configPERT);
 
         menuBar.add(charts);
 
         Settings.setText("Settings");
         menuBar.add(Settings);
 
         Help.setText("Help");
         menuBar.add(Help);
 
         setJMenuBar(menuBar);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(tabbedRenderPane, javax.swing.GroupLayout.PREFERRED_SIZE, 908, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(projectWrap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addGap(17, 17, 17)
                 .addComponent(projectWrap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(tabbedRenderPane, javax.swing.GroupLayout.PREFERRED_SIZE, 437, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void editUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editUndoActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_editUndoActionPerformed
 
     private void editCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editCopyActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_editCopyActionPerformed
 
     private void filesRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filesRefreshActionPerformed
         refresh();
     }//GEN-LAST:event_filesRefreshActionPerformed
 
     private void filesNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filesNewActionPerformed
         // Launch Project Adder
 
         ProjectInput pi = new ProjectInput(this);
         pi.main(null);
     }//GEN-LAST:event_filesNewActionPerformed
 
     private void tasksAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tasksAddActionPerformed
         // Launch Task Adder
 
         TaskInput ti = new TaskInput(this);
         ti.main(null);
     }//GEN-LAST:event_tasksAddActionPerformed
 
     private void editTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editTaskActionPerformed
         // Launch Task Editor
 
         EditTask et = new EditTask(this);
         et.main(null);
     }//GEN-LAST:event_editTaskActionPerformed
 
     private void configPERTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configPERTActionPerformed
         // Launch PERT Config
 
         ConfigurePERT cp = new ConfigurePERT(this);
         cp.main(null);
     }//GEN-LAST:event_configPERTActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(GraphicalUserInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(GraphicalUserInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(GraphicalUserInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(GraphicalUserInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new GraphicalUserInterface(project).setVisible(true);
             }
         });
     }
    // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JMenu Edit;
     private javax.swing.JMenu Files;
     private javax.swing.JMenu Help;
     private PERTRender PERTWrap;
     private javax.swing.JMenu Settings;
     private javax.swing.JMenu Tasks;
     private WBTRender WBTWrap;
     private javax.swing.JMenu charts;
     private javax.swing.JMenuItem configDependancies;
     private javax.swing.JMenuItem configPERT;
     private javax.swing.JMenuItem editCopy;
     private javax.swing.JMenuItem editCut;
     private javax.swing.JMenuItem editPaste;
     private javax.swing.JMenuItem editRedo;
     private javax.swing.JMenuItem editTask;
     private javax.swing.JMenuItem editUndo;
     private javax.swing.JMenuItem filesNew;
     private javax.swing.JMenuItem filesOpen;
     private javax.swing.JMenuItem filesRefresh;
     private javax.swing.JMenuItem filesSave;
     private javax.swing.JMenuItem filesSaveAs;
     private javax.swing.JScrollPane ganttScroll;
     private GanttRender ganttWrap;
     private javax.swing.JPopupMenu.Separator jSeparator1;
     private javax.swing.JPopupMenu.Separator jSeparator2;
     private javax.swing.JLabel lblProject;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JScrollPane pertScroll;
     private javax.swing.JLabel pjFP;
     private javax.swing.JLabel pjName;
     private javax.swing.JLabel pjSize;
     private javax.swing.JLabel projectName;
     private javax.swing.JLabel projectPath;
     private javax.swing.JLabel projectSize;
     private javax.swing.JPanel projectWrap;
     private javax.swing.JTabbedPane tabbedRenderPane;
     private javax.swing.JMenuItem tasksAdd;
     private javax.swing.JLabel uoplogo;
     private javax.swing.JLabel vysilogo;
     private javax.swing.JScrollPane wbtScroll;
    // End of variables declaration//GEN-END:variables
 }

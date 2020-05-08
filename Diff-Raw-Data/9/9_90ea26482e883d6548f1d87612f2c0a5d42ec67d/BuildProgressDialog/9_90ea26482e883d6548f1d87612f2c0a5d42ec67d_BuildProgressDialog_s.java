 package com.nebula2d.editor.ui;
 
 import com.nebula2d.editor.framework.Scene;
 import com.nebula2d.editor.ui.controls.N2DDialog;
 import com.nebula2d.editor.util.GradleExecutor;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.SwingUtilities;
 import java.awt.BorderLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 /**
  *
  * Created by bonazza on 11/19/14.
  */
 public class BuildProgressDialog extends N2DDialog implements BuildProgressUpdateListener {
 
     private JProgressBar progressBar;
     private JLabel nowLoadingLbl;
 
     public BuildProgressDialog(String projectName) {
         super(String.format("Building %s...", projectName), false);
        setResizable(false);
         init();
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 GradleExecutor.process.destroyForcibly();
             }
         });
     }
 
     private void init() {
         final JPanel barPanel = new JPanel();
         final JPanel textPanel = new JPanel();
 
         nowLoadingLbl = new JLabel("Starting build process.");
         progressBar = new JProgressBar();
         textPanel.add(nowLoadingLbl);
         barPanel.add(progressBar);
 
         add(barPanel, BorderLayout.CENTER);
         add(textPanel, BorderLayout.SOUTH);
         setLocationRelativeTo(null);
         pack();
     }
 
     @Override
     public void onBuildProgressUpdate(Scene scene, int idx, int size) {
         SwingUtilities.invokeLater(() -> {
             nowLoadingLbl.setText(String.format("Compiling scene %s... (%d/%d", scene.getName(), idx+1, size));
             float perc = (idx+1/size)*100.0f;
             progressBar.setValue((int)perc);
            revalidate();
         });
     }
 
     @Override
     public void onProjectCompiled() {
         SwingUtilities.invokeLater(() -> {
             nowLoadingLbl.setText("Project compiled successfully... Building game package.");
            revalidate();
         });
     }
 
     @Override
     public void onBuildComplete() {
         SwingUtilities.invokeLater(BuildProgressDialog.this::dispose);
     }
 }

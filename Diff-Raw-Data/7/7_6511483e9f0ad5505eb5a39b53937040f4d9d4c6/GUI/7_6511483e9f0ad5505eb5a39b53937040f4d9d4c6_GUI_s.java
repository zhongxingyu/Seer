 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 import javax.swing.UIManager.*;
 
 public class GUI
 extends JFrame
 implements ActionListener {
     private JButton   waveButton;
     private JButton wavierButton;
 
     private JButton defaultShaderButton;
     private JButton   phongShaderButton;
     private JButton     celShaderButton;
     
     private String theShader;
 
     // Method provided by Oracle.com Java tutorial
     private void
     attemptNimbusStyle() {
         try {
             for (LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         }
         catch (Exception e) {
         }
     }
 
     public
     GUI() {
         attemptNimbusStyle();
         setTitle("Volume Integral Visualizer");
 
         waveButton          = new JButton("Wave"  );
         wavierButton        = new JButton("Wavier");
         //=========================================|
         defaultShaderButton = new JButton("Plain" );
         phongShaderButton   = new JButton("Pretty");
         celShaderButton     = new JButton("Cel"   );
 
         theShader = null;
         disableShaderButton(defaultShaderButton);
 
         waveButton          .setActionCommand("set_func_wave"   );
         wavierButton        .setActionCommand("set_func_wavier" );
         //=======================================================|
         defaultShaderButton .setActionCommand("set_shader_none" );
         phongShaderButton   .setActionCommand("set_shader_phong");
         celShaderButton     .setActionCommand("set_shader_cel"  );
 
         Container cp = getContentPane();
         cp.setLayout(new FlowLayout());
 
        cp.add(waveButton         );
        cp.add(wavierButton       );
        //=========================|
         cp.add(defaultShaderButton);
         cp.add(phongShaderButton  );
         cp.add(celShaderButton    );
 
         pack();
 
         defaultShaderButton .addActionListener(this);
         phongShaderButton   .addActionListener(this);
         celShaderButton     .addActionListener(this);
         waveButton          .addActionListener(this);
         wavierButton        .addActionListener(this);
 
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
     }
 
     public static void
     main(String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 GUI gui = new GUI();
                 gui.setVisible(true);
             }
         });
     }
 
     public void
     actionPerformed(ActionEvent e) {
         Object src = e.getSource();
         String cmd = e.getActionCommand();
 
         if (cmd.equals("set_func_wave")) {
             onWaveButtonClicked();
         }
         else if (cmd.equals("set_func_wavier")) {
             onWavierButtonClicked();
         }
         else if (cmd.equals("set_shader_none")) {
             useShaderButton(src, null);
         }
         else if (cmd.equals("set_shader_phong")) {
             useShaderButton(src, "phong");
         }
         else if (cmd.equals("set_shader_cel")) {
             useShaderButton(src, "cel");
         }
     }
 
     private void
     useShaderButton(Object button, String shaderName) {
         JButton cButton = (JButton) button;
         Debug.println("SETTING SHADER TO " + shaderName);
         enableShaderButtons();
         disableShaderButton(cButton);
         theShader = shaderName;
     }
 
     private void
     enableShaderButtons() {
         JButton[] buttons = {
             celShaderButton,
             phongShaderButton,
             defaultShaderButton
         };
 
         for (JButton button: buttons) {
             button.setEnabled(true);
         }
     }
 
     private void
     disableShaderButton(JButton button) {
         button.setEnabled(false);
     }
 
     private void
     onWaveButtonClicked() {
         Visualizer.launchWith(new WaveFunc(), theShader);
     }
 
     private void
     onWavierButtonClicked() {
         Visualizer.launchWith(new WavierFunc(), theShader);
     }
 }

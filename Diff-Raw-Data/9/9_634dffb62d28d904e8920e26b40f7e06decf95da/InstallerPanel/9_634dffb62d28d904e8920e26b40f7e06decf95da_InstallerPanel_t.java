 /*     */ package cpw.mods.fml.installer;
 /*     */ 
 /*     */ import com.google.common.base.Throwables;
 /*     */ import java.awt.Color;
 /*     */ import java.awt.event.ActionEvent;
 /*     */ import java.awt.image.BufferedImage;
 /*     */ import java.io.File;
 /*     */ import java.io.IOException;
 /*     */ import javax.imageio.ImageIO;
 /*     */ import javax.swing.AbstractAction;
 /*     */ import javax.swing.Box;
 /*     */ import javax.swing.BoxLayout;
 /*     */ import javax.swing.ButtonGroup;
 /*     */ import javax.swing.ButtonModel;
 /*     */ import javax.swing.ImageIcon;
 /*     */ import javax.swing.JButton;
 /*     */ import javax.swing.JDialog;
 /*     */ import javax.swing.JFileChooser;
 /*     */ import javax.swing.JLabel;
 /*     */ import javax.swing.JOptionPane;
 /*     */ import javax.swing.JPanel;
 /*     */ import javax.swing.JRadioButton;
 /*     */ import javax.swing.JTextField;
 /*     */ import javax.swing.border.LineBorder;
 /*     */ 
 /*     */ public class InstallerPanel extends JPanel
 /*     */ {
 /*     */   private File targetDir;
 /*     */   private JTextField selectedDirText;
 /*     */   private JLabel infoLabel;
 /*     */   private JDialog dialog;
 /*     */   private JPanel fileEntryPanel;
 /*     */ 
 /*     */   public InstallerPanel(File targetDir)
 /*     */   {
 /*  73 */     setLayout(new BoxLayout(this, 1));
 /*     */     BufferedImage image;
 /*     */     try
 /*     */     {
 /*  77 */       image = ImageIO.read(SimpleInstaller.class.getResourceAsStream(VersionInfo.getLogoFileName()));
 /*     */     }
 /*     */     catch (IOException e)
 /*     */     {
 /*  81 */       throw Throwables.propagate(e);
 /*     */     }
 /*     */ 
 /*  84 */     JPanel logoSplash = new JPanel();
 /*  85 */     logoSplash.setLayout(new BoxLayout(logoSplash, 1));
 /*  86 */     ImageIcon icon = new ImageIcon(image);
 /*  87 */     JLabel logoLabel = new JLabel(icon);
 /*  88 */     logoLabel.setAlignmentX(0.5F);
 /*  89 */     logoLabel.setAlignmentY(0.5F);
 /*  90 */     logoLabel.setSize(image.getWidth(), image.getHeight());
 /*  91 */     logoSplash.add(logoLabel);
 /*  92 */     JLabel tag = new JLabel(VersionInfo.getWelcomeMessage());
 /*  93 */     tag.setAlignmentX(0.5F);
 /*  94 */     tag.setAlignmentY(0.5F);
 /*  95 */     logoSplash.add(tag);
/*  96 */     tag = new JLabel(VersionInfo.getVersion());
 /*  97 */     tag.setAlignmentX(0.5F);
 /*  98 */     tag.setAlignmentY(0.5F);
 /*  99 */     logoSplash.add(tag);
 /*     */ 
 /* 101 */     logoSplash.setAlignmentX(0.5F);
 /* 102 */     logoSplash.setAlignmentY(0.0F);
 /* 103 */     add(logoSplash);
 /*     */ 
 /* 128 */     JPanel entryPanel = new JPanel();
 /* 129 */     entryPanel.setLayout(new BoxLayout(entryPanel, 0));
 /*     */ 
 /* 131 */     this.targetDir = targetDir;
 /* 132 */     this.selectedDirText = new JTextField();
 /* 133 */     this.selectedDirText.setEditable(false);
 /* 134 */     this.selectedDirText.setToolTipText("Path to minecraft");
 /* 135 */     this.selectedDirText.setColumns(30);
 /*     */ 
 /* 137 */     entryPanel.add(this.selectedDirText);
 /* 138 */     JButton dirSelect = new JButton();
 /* 139 */     dirSelect.setAction(new FileSelectAction());
 /* 140 */     dirSelect.setText("...");
 /* 141 */     dirSelect.setToolTipText("Select an alternative minecraft directory");
 /* 142 */     entryPanel.add(dirSelect);
 /*     */ 
 /* 144 */     entryPanel.setAlignmentX(0.0F);
 /* 145 */     entryPanel.setAlignmentY(0.0F);
 /* 146 */     this.infoLabel = new JLabel();
 /* 147 */     this.infoLabel.setHorizontalTextPosition(2);
 /* 148 */     this.infoLabel.setVerticalTextPosition(1);
 /* 149 */     this.infoLabel.setAlignmentX(0.0F);
 /* 150 */     this.infoLabel.setAlignmentY(0.0F);
 /* 151 */     this.infoLabel.setForeground(Color.RED);
 /* 152 */     this.infoLabel.setVisible(false);
 /*     */ 
 /* 154 */     this.fileEntryPanel = new JPanel();
 /* 155 */     this.fileEntryPanel.setLayout(new BoxLayout(this.fileEntryPanel, 1));
 /* 156 */     this.fileEntryPanel.add(this.infoLabel);
 /* 157 */     this.fileEntryPanel.add(Box.createVerticalGlue());
 /* 158 */     this.fileEntryPanel.add(entryPanel);
 /* 159 */     this.fileEntryPanel.setAlignmentX(0.5F);
 /* 160 */     this.fileEntryPanel.setAlignmentY(0.0F);
 /* 161 */     add(this.fileEntryPanel);
 /* 162 */     updateFilePath();
 /*     */   }
 /*     */ 
 /*     */   private void updateFilePath()
 /*     */   {
 /*     */     try
 /*     */     {
 /* 170 */       this.targetDir = this.targetDir.getCanonicalFile();
 /* 171 */       this.selectedDirText.setText(this.targetDir.getPath());
 /*     */     }
 /*     */     catch (IOException e)
 /*     */     {
 /*     */     }
 /*     */ 
 /* 178 */     InstallerAction action = InstallerAction.valueOf("CLIENT");
 /* 179 */     boolean valid = action.isPathValid(this.targetDir);
 /*     */ 
 /* 181 */     if (valid)
 /*     */     {
 /* 183 */       this.selectedDirText.setForeground(Color.BLACK);
 /* 184 */       this.infoLabel.setVisible(false);
 /* 185 */       this.fileEntryPanel.setBorder(null);
 /* 186 */       if (this.dialog != null)
 /*     */       {
 /* 188 */         this.dialog.invalidate();
 /* 189 */         this.dialog.pack();
 /*     */       }
 /*     */     }
 /*     */     else
 /*     */     {
 /* 194 */       this.selectedDirText.setForeground(Color.RED);
 /* 195 */       this.fileEntryPanel.setBorder(new LineBorder(Color.RED));
 /* 196 */       this.infoLabel.setText("<html>" + action.getFileError(this.targetDir) + "</html>");
 /* 197 */       this.infoLabel.setVisible(true);
 /* 198 */       if (this.dialog != null)
 /*     */       {
 /* 200 */         this.dialog.invalidate();
 /* 201 */         this.dialog.pack();
 /*     */       }
 /*     */     }
 /*     */   }
 /*     */ 
 /*     */   public void run()
 /*     */   {
 /* 208 */     JOptionPane optionPane = new JOptionPane(this, -1, 2);
 /*     */ 
 /* 210 */     this.dialog = optionPane.createDialog(null, "Mod system installer");
 /* 211 */     this.dialog.setDefaultCloseOperation(2);
 /* 212 */     this.dialog.setModal(true);
 /* 213 */     this.dialog.setVisible(true);
 /* 214 */     int result = ((Integer)(optionPane.getValue() != null ? optionPane.getValue() : Integer.valueOf(-1))).intValue();
 /* 215 */     if (result == 0)
 /*     */     {
 /* 217 */       InstallerAction action = InstallerAction.CLIENT;
 /* 218 */       if (action.run(this.targetDir))
 /*     */       {
 				  JOptionPane.showMessageDialog(null, action.getSuccessMessage(), "Complete", 1);
 /*     */       }
 /*     */     }
 /* 223 */     this.dialog.dispose();
 /*     */   }
 /*     */ 
 /*     */   private class SelectButtonAction extends AbstractAction
 /*     */   {
 /*     */     private SelectButtonAction()
 /*     */     {
 /*     */     }
 /*     */ 
 /*     */     public void actionPerformed(ActionEvent e)
 /*     */     {
 /*  67 */       InstallerPanel.this.updateFilePath();
 /*     */     }
 /*     */   }
 /*     */ 
 /*     */   private class FileSelectAction extends AbstractAction
 /*     */   {
 /*     */     private FileSelectAction()
 /*     */     {
 /*     */     }
 /*     */ 
 /*     */     public void actionPerformed(ActionEvent e)
 /*     */     {
 /*  44 */       JFileChooser dirChooser = new JFileChooser();
 /*  45 */       dirChooser.setFileSelectionMode(1);
 /*  46 */       dirChooser.setFileHidingEnabled(false);
 /*  47 */       dirChooser.ensureFileIsVisible(InstallerPanel.this.targetDir);
 /*  48 */       dirChooser.setSelectedFile(InstallerPanel.this.targetDir);
 /*  49 */       int response = dirChooser.showOpenDialog(InstallerPanel.this);
 /*  50 */       switch (response)
 /*     */       {
 /*     */       case 0:
 /*  53 */         InstallerPanel.this.targetDir = dirChooser.getSelectedFile();
 /*  54 */         InstallerPanel.this.updateFilePath();
 /*  55 */         break;
 /*     */       }
 /*     */     }
 /*     */   }
 /*     */ }
 
 /* Location:           C:\Users\User\Desktop\minecraftforge-installer-1.6.2-9.10.0.804.jar
  * Qualified Name:     cpw.mods.fml.installer.InstallerPanel
  * JD-Core Version:    0.6.2
  */

 package com.kokakiwi.mclauncher.ui.simple;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.ListModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 
 import com.kokakiwi.mclauncher.api.LauncherAPI;
 import com.kokakiwi.mclauncher.utils.ConfigListModel;
 import com.kokakiwi.mclauncher.utils.lang.Translater;
 
 public class GameDialog extends JDialog
 {
     private static final long serialVersionUID = -2663368148236524858L;
     public static JList mode = new JList();
     private final LauncherAPI api;
     
     public GameDialog(final LauncherAPI api)
     {
         
         super(api.getFrame());
         this.api = api;
         addWindowListener(new WindowAdapter() {
             
             public void windowClosing(WindowEvent event)
             {
                 setVisible(false);
             }
         });
         
         mode = new JList();
         mode.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         mode.setBorder(new LineBorder(new Color(0, 0, 0),2,true));
         mode.setBounds(10, 11, 275, 146);
         refreshList();
         
         setTitle(Translater.getString("game.windowTitle"));
         setModalityType(ModalityType.TOOLKIT_MODAL);
         
         final JPanel panel = new JPanel(new BorderLayout());
         
         final JLabel label = new JLabel("Games", 0);
         label.setBorder(new EmptyBorder(0, 0, 16, 0));
         label.setFont(new Font("Default", 1, 16));
         panel.add(label, "North");
         
         final JPanel optionsPanel = new JPanel(new BorderLayout());
         final JPanel labelPanel = new JPanel(new GridLayout(0, 1));
         final JPanel fieldPanel = new JPanel(new GridLayout(0, 1));
         optionsPanel.add(labelPanel, "West");
         labelPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
         optionsPanel.add(fieldPanel, "Center");
         
        
         panel.add(optionsPanel, "Center");
         
         final JPanel buttonsPanel = new JPanel(new BorderLayout());
         buttonsPanel.add(new JPanel(), "Center");
         final JButton doneButton = new JButton(
                 Translater.getString("options.done"));
         doneButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae)
             {
                 setVisible(false);
             }
         });
         buttonsPanel.add(doneButton, "East");
         buttonsPanel.setBorder(new EmptyBorder(16, 0, 0, 0));
         
         labelPanel.add(mode);
         
         final JButton add = new JButton(
                "+");
         
         final JButton rem = new JButton(
                 "-");
         
         add.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e)
             {
             	String str = JOptionPane.showInputDialog(panel, Translater.getString("game.addgame"), "brautec", JOptionPane.OK_CANCEL_OPTION);
                 if(str.length() > 0)
                 {
                 	if(api.getConfigList().registerConfig(str))
                 	{
                 		JOptionPane.showMessageDialog(panel,Translater.getString("global.success"));
                 	} else
                 		JOptionPane.showMessageDialog(panel,Translater.getString("global.fail"));
                 	api.getMain().reload();
                 }
             }
         });
         
         rem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e)
             {
             	if(mode.getSelectedValue() != null)
             	{
             		String str = mode.getSelectedValue().toString();
             		int res = JOptionPane.showConfirmDialog(panel, Translater.getString("game.delete"));
             		if(res == JOptionPane.OK_OPTION)
             		{
             			if(api.deleteGame(str) && api.getConfigList().deleteConfig(str))
             				JOptionPane.showMessageDialog(panel,Translater.getString("global.success"));
             			else
             				JOptionPane.showMessageDialog(panel,Translater.getString("global.fail"));
            			
             			api.getMain().reload();
             		}
             	}
             }
         });
         fieldPanel.add(add);
         fieldPanel.add(rem);
         
         panel.add(buttonsPanel, "South");
         
         getContentPane().add(panel);
         panel.setBorder(new EmptyBorder(16, 24, 24, 24));
         
         pack();
         setLocationRelativeTo(api.getFrame());
     }
     
     public LauncherAPI getApi()
     {
         return api;
     }
     
     public void refreshList()
     {
         final ListModel model = new ConfigListModel(api);
         mode.setModel(model);
     }
 }

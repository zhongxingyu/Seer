 package ui.gui.graphical.game;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.PriorityQueue;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 
 import logic.Maze;
import logic.UnitEventEntry;
 
 public class SaveLoadDialog extends JDialog
 {
     private MazeGame _game;
     private boolean _editor;
     private static final String _savePath = System.getProperty("user.dir") + "/saves/";
     private JList<String> _fileList = new JList<String>();
 
     public SaveLoadDialog(JFrame frame, MazeGame m, boolean editor)
     {
         super(frame, ModalityType.APPLICATION_MODAL);
         _game = m;
         _editor = editor;
 
         UpdateFiles();
 
         //setSize(getSize().width + 50, getSize().height + 100);
         setLocation(frame.getLocation().x + frame.getSize().width / 2 - getSize().width / 2, frame.getLocation().y + frame.getSize().height / 2 - getSize().height / 2);
 
         initUI();
     }
 
     private void DeleteGame(String name)
     {
         File folder = new File(_savePath);
         if (!folder.isDirectory())
             return;
 
         for (File file : folder.listFiles())
             if (file.getName().equals(name))
             {
                 file.delete();
                 UpdateFiles();
                 return;
             }
     }
 
     private void UpdateFiles()
     {
         File folder = new File(_savePath);
         if (!folder.isDirectory())
             return;
 
         DefaultListModel<String> listModel = new DefaultListModel<String>();
 
         for (File file : folder.listFiles())
             listModel.addElement(file.getName());
 
         _fileList.setModel(listModel);
     }
 
     private void SaveGame()
     {
         ObjectOutputStream os = null;
         try
         {
             File folder = new File(_savePath);
             if (!folder.exists())
                 folder.mkdir();
 
             DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
             Date date = new Date();
 
             os = new ObjectOutputStream(new FileOutputStream(_savePath + dateFormat.format(date) + (_editor ? "-custom.maze" : ".maze")));
             os.writeObject(_game.GetMaze());
         }
         catch (IOException ex)
         {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(null, "An error occured while trying to save game.");
         }
         finally
         {
             if (os != null) try { os.close(); } catch (IOException e1) { }
             UpdateFiles();
         }
     }
 
     private void LoadGame(String name)
     {
         ObjectInputStream os = null;
         Maze m = null;
         try
         {
             os = new ObjectInputStream(new FileInputStream(_savePath + name));
             m = (Maze)os.readObject();
         }
         catch (IOException ex)
         {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(null, "An error occured while trying to load game.");
         } catch (ClassNotFoundException ex)
         {
             JOptionPane.showMessageDialog(null, "An error occured while trying to load game.");
             ex.printStackTrace();
         }
         finally
         {
             if (os != null) try { os.close(); } catch (IOException e1) { }
         }
 
         if (m != null)
         {
            m.SetEventQueue(new PriorityQueue<UnitEventEntry>());
             _game.SetMaze(m);
         }
     }
 
     private void initUI()
     {
         setLayout(new BorderLayout());
 
         _fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         _fileList.setVisibleRowCount(5);
 
         _fileList.addMouseListener(new MouseListener()
         {
             @Override public void mouseReleased(MouseEvent e) {}
             @Override public void mousePressed(MouseEvent e) { }
             @Override public void mouseExited(MouseEvent e) {}
             @Override public void mouseEntered(MouseEvent e) {}
             @Override public void mouseClicked(MouseEvent e)
             {
                 if (e.getClickCount() == 2)
                     LoadGame(_fileList.getSelectedValue());
             }
         });
 
         JButton saveButton = new JButton("Save current game");
         saveButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 SaveGame();
             }
         });
 
         JButton loadButton = new JButton("Load selected game");
         loadButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 String f = _fileList.getSelectedValue();
                 if (f != null)
                 {
                     LoadGame(f);
                     setVisible(false);
                     _game.repaint();
                 }
             }
         });
 
         JButton deleteButton = new JButton("Delete selected game");
         deleteButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 String f = _fileList.getSelectedValue();
                 if (f != null)
                     DeleteGame(f);
             }
         });
 
 
         //getContentPane().add(_fileList, BorderLayout.CENTER);
         getContentPane().add(new JScrollPane(_fileList), BorderLayout.CENTER);
 
         JPanel southPanel = new JPanel(new GridLayout(1, 3));
         getContentPane().add(southPanel, BorderLayout.SOUTH);
 
         southPanel.add(saveButton);
         southPanel.add(loadButton);
         southPanel.add(deleteButton);
 
         pack();
     }
 
     private static final long serialVersionUID = 1L;
 }

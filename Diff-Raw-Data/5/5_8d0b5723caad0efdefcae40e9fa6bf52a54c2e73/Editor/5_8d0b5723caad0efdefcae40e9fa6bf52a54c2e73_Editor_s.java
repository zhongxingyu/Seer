 package editor;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultCellEditor;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SpringLayout;
 import javax.swing.UIManager;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeSelectionModel;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import util.Compressor;
 import util.FileUtilities;
 import util.JSuggestField;
 import util.SpringUtilities;
 import entity.EntityBuilder;
 import entity.EntityRegistry;
 import entity.util.EntityLoader;
 import game.Game;
 
 /**
  * Map Editor GUI
  * 
  * @author Dakror
  * 
  */
 public class Editor extends JFrame implements TreeSelectionListener
 {
 	private static final long serialVersionUID = 1L;
 
 	File mapFile;
 	File entListFile;
 
 	// -- Components -- //
 	JScrollPane treePanel;
 	JTree tree;
 	JPanel uiPanel;
 
 	// -- Entity Tab -- //
 	JSONArray entities;
 	JSpinner entityPosX, entityPosY, entityPosZ;
 	JSpinner entityRotX, entityRotY, entityRotZ;
 	JTextField entityID;
 	JTable entityCustomValues;
 
 	// -- Events Tab -- //
 	JTable eventEvents;
 	JDialog eventDialog;
 	JSuggestField eventTarget;
 	JComboBox<String> eventFunction;
 
 	// -- menu -- //
 	JMenuItem saveFile;
 	JMenuItem saveUFile;
 	JMenu view;
 
 	public Editor()
 	{
 		super("BOLT Editor");
 		try
 		{
 			EntityLoader.findEntities(Game.getCurrentGame().entListFilePath);
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		addWindowListener(new WindowAdapter()
 		{
 			@Override
 			public void windowClosing(WindowEvent e)
 			{
 				if (isChanged())
 				{
 					int r = JOptionPane.showConfirmDialog(Editor.this, "\"" + mapFile.getName() + "\" has been modified. Save changes?", "Save Resource", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 					if (r == JOptionPane.YES_OPTION) saveMap();
 					else if (r == JOptionPane.CANCEL_OPTION) return;
 				}
 			}
 		});
 		initComponents();
 		setResizable(false);
 		setLocationRelativeTo(null);
 		setVisible(true);
 	}
 
 	public void initComponents()
 	{
 		JMenuBar menu = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenuItem newFile = new JMenuItem(new AbstractAction("New")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				refresh();
 				if (isChanged())
 				{
 					int r = JOptionPane.showConfirmDialog(Editor.this, "\"" + mapFile.getName() + "\" has been modified. Save changes?", "Save Resource", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 					if (r == JOptionPane.YES_OPTION) newMap();
 					else if (r == JOptionPane.CANCEL_OPTION) return;
 				}
 				newMap();
 			}
 		});
 		newFile.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
 		file.add(newFile);
 		JMenuItem openFile = new JMenuItem(new AbstractAction("Open...")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				refresh();
 				if (isChanged())
 				{
 					int r = JOptionPane.showConfirmDialog(Editor.this, "\"" + mapFile.getName() + "\" has been modified. Save changes?", "Save Resource", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 					if (r == JOptionPane.YES_OPTION) saveMap();
 					else if (r == JOptionPane.CANCEL_OPTION) return;
 				}
 				openMap();
 			}
 		});
 		openFile.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
 		file.add(openFile);
 		saveFile = new JMenuItem(new AbstractAction("Save")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				saveMap();
 			}
 		});
 		saveFile.setEnabled(false);
 		saveFile.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
 		file.add(saveFile);
 		saveUFile = new JMenuItem(new AbstractAction("Save as...")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				saveUMap();
 			}
 		});
 		saveUFile.setEnabled(false);
 		saveUFile.setAccelerator(KeyStroke.getKeyStroke("ctrl shift S"));
 		file.add(saveUFile);
 		menu.add(file);
 
 		view = new JMenu("View");
 		JMenuItem raw = new JMenuItem(new AbstractAction("Raw file...")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				showRawFile();
 			}
 		});
 		view.add(raw);
 		view.setEnabled(false);
 		menu.add(view);
 
 		JMenu mode = new JMenu("Mode");
 		JMenuItem ent = new JMenuItem(new AbstractAction("Entity Editor")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				new EntityEditor();
 			}
 		});
 		mode.add(ent);
 		menu.add(mode);
 
 		setJMenuBar(menu);
 
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 
 		treePanel = new JScrollPane(null, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		treePanel.setPreferredSize(new Dimension(200, 600));
 		panel.add(treePanel);
 		tree = new JTree(new DefaultMutableTreeNode("World"));
 		tree.setModel(null);
 		tree.setEnabled(false);
 		tree.setShowsRootHandles(true);
 		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		tree.addTreeSelectionListener(this);
 		tree.setExpandsSelectedPaths(true);
 		tree.addMouseListener(new MouseAdapter()
 		{
 			public void mouseClicked(MouseEvent e)
 			{
 				if (e.getButton() != 3) return;
 
 				final int row = tree.getRowForLocation(e.getX(), e.getY());
 
 				if (row <= 1) return;
 
 				tree.setSelectionRow(row);
 
 				JPopupMenu menu = new JPopupMenu();
 				JMenuItem del = new JMenuItem(new AbstractAction("Delete")
 				{
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void actionPerformed(ActionEvent e)
 					{
 						entities.remove(row - 2);
 						DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
 						dtm.removeNodeFromParent((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent());
 						refresh();
 					}
 				});
 				menu.add(del);
 
 				menu.show(e.getComponent(), e.getX(), e.getY());
 			}
 		});
 
 		treePanel.setViewportView(tree);
 
 		uiPanel = new JPanel(new FlowLayout());
 		uiPanel.setEnabled(false);
 		uiPanel.setPreferredSize(new Dimension(600, 600));
 
 		panel.add(uiPanel);
 		setContentPane(panel);
 		pack();
 	}
 
 	public boolean isChanged()
 	{
 		if (mapFile == null) return false;
 
 		try
 		{
 			return !writeValue(getData()).equals(writeValue(new JSONObject(Compressor.decompressFile(mapFile))));
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 			return true;
 		}
 	}
 
 	public static String writeValue(Object value)
 	{
 		String string = "null";
 		try
 		{
 			if (value instanceof Integer || value instanceof Double || value instanceof Boolean) return value.toString();
 			else if (value instanceof JSONArray)
 			{
 				string = "[";
 				if (((JSONArray) value).length() > 0)
 				{
 					for (int i = 0; i < ((JSONArray) value).length(); i++)
 						string += writeValue((((JSONArray) value).get(i))) + ",";
 
 					string = string.substring(0, string.length() - 1);
 				}
 
 				return string + "]";
 			}
 			else if (value instanceof JSONObject)
 			{
 				string = "{";
 				String[] keys = JSONObject.getNames((JSONObject) value);
 				if (keys != null && keys.length > 0)
 				{
 					Arrays.sort(keys);
 					for (String s : keys)
 						string += "\"" + s + "\":" + writeValue(((JSONObject) value).get(s)) + ",";
 
 					string = string.substring(0, string.length() - 1);
 				}
 				return string + "}";
 			}
 			else return "\"" + value.toString() + "\"";
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return string;
 	}
 
 	public void showRawFile()
 	{
 		try
 		{
 			JDialog frame = new JDialog(this, true);
 			frame.setTitle("BOLT Editor - Raw File Preview");
 			frame.setSize(400, 400);
 			frame.setDefaultCloseOperation(HIDE_ON_CLOSE);
 			JTextArea area = new JTextArea(getData().toString(4));
 
 			frame.setContentPane(new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
 			frame.setLocationRelativeTo(null);
 			frame.setVisible(true);
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public void setTitle(String s)
 	{
 		super.setTitle(((s != null) ? s + " - " : "") + "BOLT Editor");
 	}
 
 	public String getTitle()
 	{
 		return super.getTitle().replaceAll("( - )(BOLT Editor)", "");
 	}
 
 	public void newMap()
 	{
 		mapFile = null;
 		reset();
 	}
 
 	private void reset()
 	{
 		saveFile.setEnabled(true);
 		saveUFile.setEnabled(true);
 		view.setEnabled(true);
 		tree.setEnabled(true);
 		entities = new JSONArray();
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("World");
 		DefaultMutableTreeNode entities = new DefaultMutableTreeNode("Entities");
 		root.add(entities);
 		tree.setModel(new DefaultTreeModel(root));
 
 		uiPanel.setEnabled(true);
 	}
 
 	public void openMap()
 	{
 		JFileChooser jfc = new JFileChooser(FileUtilities.getJarFile().getParentFile());
 		jfc.setFileFilter(new FileNameExtensionFilter("BOLT Map-Files", "map"));
 		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		jfc.setMultiSelectionEnabled(false);
 		if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 		{
 			mapFile = jfc.getSelectedFile();
 			try
 			{
 				reset();
 
 				setTitle(mapFile.getPath());
 				JSONObject data = new JSONObject(Compressor.decompressFile(mapFile));
 				entities = data.getJSONArray("entities");
 				DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
 
 				for (int i = 0; i < entities.length(); i++)
 				{
 					dtm.insertNodeInto(new DefaultMutableTreeNode(entities.getJSONObject(i).getString("id")), (DefaultMutableTreeNode) tree.getPathForRow(1).getLastPathComponent(), i);
 					refresh();
 				}
 				tree.expandRow(1);
 			}
 			catch (Exception e)
 			{
 				JOptionPane.showMessageDialog(Editor.this, "Could not open file: \"" + mapFile.getPath() + "\"!", "Error!", JOptionPane.ERROR_MESSAGE);
 				mapFile = null;
 
 				DefaultMutableTreeNode root = new DefaultMutableTreeNode("World");
 				tree.setModel(new DefaultTreeModel(root));
 
 				return;
 			}
 
 		}
 	}
 
 	private JSONObject getData()
 	{
 		try
 		{
 			JSONObject data = new JSONObject();
 			data.put("entities", entities);
 			return data;
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public JSONObject validateCustomValues(JSONObject customValues, String entityName) throws JSONException
 	{
 		HashMap<String, Object> cv = EntityRegistry.getEntityBuilder(entityName).customValues;
 		if (customValues.length() == cv.size()) return customValues;
 
 		if (customValues.length() > cv.size())
 		{
 			for (int i = 0; i < customValues.length(); i++)
 			{
 				String key = customValues.names().getString(i);
 				if (!cv.containsKey(customValues.get(key))) customValues.remove(key);
 			}
 		}
 
 		for (int i = 0; i < cv.size(); i++)
 		{
 			String key = new ArrayList<String>(cv.keySet()).get(i);
 			if (!customValues.has(key)) customValues.put(key, cv.get(key));
 		}
 
 		return customValues;
 	}
 
 	public void saveMap()
 	{
 		if (mapFile == null)
 		{
 			saveUMap();
 			return;
 		}
 
 		try
 		{
 			JSONArray entities = getData().getJSONArray("entities");
 			for (int i = 0; i < entities.length(); i++)
 			{
 				JSONObject object = entities.getJSONObject(i);
 				object.put("custom", validateCustomValues(object.getJSONObject("custom"), object.getString("name")));
 
 				entities.put(i, object);
 			}
 			this.entities = entities;
 
 		}
 		catch (Exception e)
 		{
 		}
 
 		String string = writeValue(getData());
 
 		Compressor.compressFile(mapFile, string);
 		refresh();
 	}
 
 	public void saveUMap()
 	{
 		JFileChooser jfc = new JFileChooser("C:/");
 		jfc.setFileFilter(new FileNameExtensionFilter("BOLT Map-Files", "map"));
 		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		jfc.setMultiSelectionEnabled(false);
 		if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
 		{
 
 			mapFile = new File(jfc.getSelectedFile().getPath().replace(".map", "") + ".map");
 			saveMap();
 		}
 	}
 
 	private void refresh()
 	{
 		if (isChanged())
 		{
 			if (!getTitle().startsWith("*")) setTitle("*" + getTitle());
 		}
 		else
 		{
 			if (mapFile != null) setTitle(mapFile.getPath());
 			else setTitle(null);
 		}
 		revalidate();
 		repaint();
 		treePanel.revalidate();
 	}
 
 	private String[] loadEntityList()
 	{
 		ArrayList<String> list = new ArrayList<>();
 		list.add("-- Choose an Entity --");
 
 		for (String key : EntityLoader.entitiesFound.keySet())
 		{
 			list.add(key);
 		}
 		Collections.sort(list);
 		return list.toArray(new String[] {});
 	}
 
 	@Override
 	public void valueChanged(TreeSelectionEvent e)
 	{
 		uiPanel.setLayout(new FlowLayout());
 		uiPanel.removeAll();
 		refresh();
 
 		final DefaultMutableTreeNode s = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
 
 		if (tree.getRowForPath(e.getPath()) == 1) // Entities
 		showEntitesUI(s);
 
 		if (tree.getRowForPath(e.getPath()) > 1) // Entity1, Entity2, ...
 		{
 			if (entities.length() == 0) return;
 
 			if (tree.getRowForPath(e.getPath()) - 2 < 0) return;
 
 			try
 			{
 				showEntityUI(e);
 			}
 			catch (Exception e1)
 			{
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	private void showEntitesUI(final DefaultMutableTreeNode s)
 	{
 		final JButton newEntity = new JButton();
 
 		final JComboBox<String> entities = new JComboBox<>(loadEntityList());
 		entities.setSelectedIndex(0);
 		entities.addItemListener(new ItemListener()
 		{
 			@Override
 			public void itemStateChanged(ItemEvent e)
 			{
 				if (e.getStateChange() == ItemEvent.SELECTED)
 				{
 					newEntity.setEnabled(entities.getSelectedIndex() > 0);
 				}
 			}
 		});
 		uiPanel.add(entities);
 
 		newEntity.setAction(new AbstractAction("New Entity")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
 				dtm.insertNodeInto(new DefaultMutableTreeNode(s.getChildCount()), s, s.getChildCount());
 				tree.expandRow(1);
 				try
 				{
 					EntityBuilder entity = EntityRegistry.getEntityBuilder(entities.getSelectedItem().toString().replace(".entity", ""));
 
 					JSONObject object = new JSONObject();
 					object.put("name", entities.getSelectedItem().toString().replace(".entity", ""));
 					object.put("id", "" + (s.getChildCount() - 1));
 					object.put("pos", new JSONArray(new Double[] { 0d, 0d, 0d }));
 					object.put("rot", new JSONArray(new Double[] { 0d, 0d, 0d }));
 					object.put("events", new JSONArray());
 					JSONObject custom = new JSONObject();
 					for (String key : entity.customValues.keySet())
 					{
 						custom.put(key, entity.customValues.get(key));
 					}
 					object.put("custom", custom);
 					Editor.this.entities.put(object);
 					refresh();
 				}
 				catch (Exception e1)
 				{
 					e1.printStackTrace();
 				}
 			}
 		});
 		newEntity.setEnabled(false);
 		newEntity.setPreferredSize(new Dimension(300, 24));
 		uiPanel.add(newEntity);
 		refresh();
 	}
 
 	private void showEntityUI(TreeSelectionEvent e) throws Exception
 	{
 		uiPanel.setLayout(null);
 
 		JTabbedPane pane = new JTabbedPane();
 		pane.setBounds(0, -1, uiPanel.getWidth() + 3, uiPanel.getHeight() - 28);
 		pane.setPreferredSize(uiPanel.getPreferredSize());
 		final int entityIndex = tree.getRowForPath(e.getPath()) - 2;
 
 		JSONObject entityData = entities.getJSONObject(tree.getRowForPath(e.getPath()) - 2);
 
 		EntityBuilder entity = EntityRegistry.getEntityBuilder(entityData.getString("name"));
 
 		// -- Entity Tab -- //
 
 		JPanel entityPanel = new JPanel(new SpringLayout());
 
 		entityPanel.add(new JLabel("Name:"));
 		JTextField name = new JTextField(entity.fullName + " (" + entity.name + ")");
 		name.setEditable(false);
 		entityPanel.add(name);
 
 		entityPanel.add(new JLabel("ID:"));
 		entityID = new JTextField(entityData.getString("id"));
 		entityPanel.add(entityID);
 
 		entityPanel.add(new JLabel("Position:"));
 		JPanel panel = new JPanel();
 		entityPosX = new JSpinner(new SpinnerNumberModel(entityData.getJSONArray("pos").getDouble(0), -1000000, 1000000, 1));
 		panel.add(entityPosX);
 		entityPosY = new JSpinner(new SpinnerNumberModel(entityData.getJSONArray("pos").getDouble(1), -1000000, 1000000, 1));
 		panel.add(entityPosY);
 		entityPosZ = new JSpinner(new SpinnerNumberModel(entityData.getJSONArray("pos").getDouble(2), -1000000, 1000000, 1));
 		panel.add(entityPosZ);
 		entityPanel.add(panel);
 
 		entityPanel.add(new JLabel("Rotation:"));
 		panel = new JPanel();
 		entityRotX = new JSpinner(new SpinnerNumberModel(entityData.getJSONArray("rot").getDouble(0), -1000000, 1000000, 1));
 		panel.add(entityRotX);
 		entityRotY = new JSpinner(new SpinnerNumberModel(entityData.getJSONArray("rot").getDouble(1), -1000000, 1000000, 1));
 		panel.add(entityRotY);
 		entityRotZ = new JSpinner(new SpinnerNumberModel(entityData.getJSONArray("rot").getDouble(2), -1000000, 1000000, 1));
 		panel.add(entityRotZ);
 		entityPanel.add(panel);
 
 		entityPanel.add(new JLabel("Custom Values:"));
 
 		final String[][] data = new String[entity.customValues.size()][2];
 		ArrayList<String> keys = new ArrayList<>(entity.customValues.keySet());
 		for (int i = 0; i < data.length; i++)
 		{
 			data[i] = new String[] { keys.get(i) + " (" + entity.customValues.get(keys.get(i)).getClass().getSimpleName() + ")", ((entityData.getJSONObject("custom").has(keys.get(i))) ? entityData.getJSONObject("custom").get(keys.get(i)).toString() : entity.customValues.get(keys.get(i)).toString()).toString() };
 		}
 		final JButton browse = new JButton("Browse...");
 		browse.setEnabled(false);
 
 		entityCustomValues = new JTable(new DefaultTableModel(data, new String[] { "Name (Type)", "Value" }))
 		{
 			private static final long serialVersionUID = 1L;
 
 			public boolean isCellEditable(int row, int column)
 			{
 				if (column == 0) return false; // name column
 
 				if (column == 1 && entityCustomValues.getValueAt(row, 0).toString().contains("(File)")) return false; // file type
 
 				return true;
 			}
 		};
 		entityCustomValues.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
 		JScrollPane jsp = new JScrollPane(entityCustomValues, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		entityCustomValues.setFillsViewportHeight(true);
 		entityCustomValues.setRowHeight(23);
 		entityCustomValues.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		entityCustomValues.getSelectionModel().addListSelectionListener(new ListSelectionListener()
 		{
 			@Override
 			public void valueChanged(ListSelectionEvent e)
 			{
 				if (e.getValueIsAdjusting() || entityCustomValues.getSelectedRow() == -1) return;
 
 				browse.setEnabled(data[entityCustomValues.getSelectedRow()][0].contains("(File)"));
 			}
 		});
 		jsp.setPreferredSize(new Dimension(entityCustomValues.getWidth(), 150));
 		entityPanel.add(jsp);
 		entityPanel.add(new JLabel());
 		browse.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
				JFileChooser jfc = new JFileChooser(FileUtilities.getHardDrive(FileUtilities.getJarFile()));
 				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 				jfc.setMultiSelectionEnabled(false);
 				if (jfc.showSaveDialog(Editor.this) == JFileChooser.APPROVE_OPTION)
 				{
 					if (!FileUtilities.getHardDrive(jfc.getSelectedFile()).equals(FileUtilities.getHardDrive(FileUtilities.getJarFile())))
 					{
 						JOptionPane.showMessageDialog(Editor.this, "Please choose a file stored on the harddrive \"" + FileUtilities.getHardDrive(FileUtilities.getJarFile()).toString() + "\"!", "Error!", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
					entityCustomValues.setValueAt(FileUtilities.getRelativePath(FileUtilities.getJarFile().getParentFile(), jfc.getSelectedFile()), entityCustomValues.getSelectedRow(), 1);
 				}
 			}
 		});
 		entityPanel.add(browse);
 
 		SpringUtilities.makeCompactGrid(entityPanel, 6, 2, 6, 6, 6, 6);
 
 		JPanel wrap = new JPanel();
 		wrap.add(entityPanel);
 
 		pane.addTab("Entity", wrap);
 
 		// -- Events Tab -- //
 
 		if (entity.triggers.size() > 0)
 		{
 
 			JPanel eventPanel = new JPanel(new FlowLayout());
 			eventEvents = new JTable(new DefaultTableModel(new String[] { "Trigger", "Target", "Function", "Parameters" }, 0))
 			{
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public boolean isCellEditable(int row, int column)
 				{
 					if (column > 0) return false; // value column
 					return true;
 				}
 			};
 			final JComboBox<String> trigger = new JComboBox<String>(entity.triggers.toArray(new String[] {}));
 			trigger.setSelectedIndex(0);
 			eventEvents.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(trigger));
 			eventEvents.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
 			eventEvents.setRowHeight(22);
 			eventEvents.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			jsp = new JScrollPane(eventEvents, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 			jsp.setPreferredSize(new Dimension(pane.getWidth(), pane.getHeight() - 30 - 35));
 			eventEvents.setFillsViewportHeight(true);
 			eventPanel.add(jsp);
 			eventPanel.add(new JButton(new AbstractAction("New")
 			{
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					((DefaultTableModel) eventEvents.getModel()).addRow(new String[] { trigger.getItemAt(0), "" });
 				}
 			}));
 			eventPanel.add(new JButton(new AbstractAction("Edit...")
 			{
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					if (eventEvents.getSelectedRow() > -1)
 					{
 						try
 						{
 							editEvent();
 						}
 						catch (JSONException e1)
 						{
 							e1.printStackTrace();
 						}
 					}
 				}
 			}));
 			eventPanel.add(new JButton(new AbstractAction("Delete")
 			{
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					if (eventEvents.getSelectedRow() > -1) ((DefaultTableModel) eventEvents.getModel()).removeRow(eventEvents.getSelectedRow());
 				}
 			}));
 
 			pane.addTab("Events", eventPanel);
 		}
 		// -- Final -- //
 		uiPanel.add(pane);
 
 		JButton apply = new JButton(new AbstractAction("Apply")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				try
 				{
 					// -- Entity Tab Data -- //
 					if (entityID.getText().length() == 0)
 					{
 						JOptionPane.showMessageDialog(Editor.this, "Please enter a unique identifier for that entity!", "Error!", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 
 					entities.getJSONObject(entityIndex).put("id", entityID.getText());
 					entities.getJSONObject(entityIndex).put("pos", new JSONArray(new Double[] { (double) entityPosX.getValue(), (double) entityPosY.getValue(), (double) entityPosZ.getValue() }));
 					entities.getJSONObject(entityIndex).put("rot", new JSONArray(new Double[] { (double) entityRotX.getValue(), (double) entityRotY.getValue(), (double) entityRotZ.getValue() }));
 					EntityBuilder builder = EntityRegistry.entries.get(entities.getJSONObject(entityIndex).getString("name"));
 					JSONObject custom = new JSONObject();
 					boolean valid = true;
 					String message = "";
 					for (int i = 0; i < entityCustomValues.getModel().getRowCount(); i++)
 					{
 						String name = entityCustomValues.getValueAt(i, 0).toString().split(" ")[0];
 						String type = builder.customValues.get(name).getClass().getSimpleName();
 						String content = entityCustomValues.getValueAt(i, 1).toString();
 
 						if (type.equals("Integer")) custom.put(name, Integer.parseInt(content));
 						else if (type.equals("Double")) custom.put(name, Double.parseDouble(content));
 						else if (type.equals("Byte")) custom.put(name, Byte.parseByte(content));
 						else if (type.equals("Boolean")) custom.put(name, Boolean.parseBoolean(content));
 						else if (type.equals("File")) custom.put(name, content);
 					}
 
 					if (!valid)
 					{
 						JOptionPane.showMessageDialog(Editor.this, "Please enter your custom values in the same data type as specified in brackets!\n  at " + message, "Error!", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 
 					entities.getJSONObject(entityIndex).put("custom", custom);
 
 					// -- Events Tab Data -- //
 					valid = true;
 					message = "";
 					// JSONArray events = new JSONArray();
 					// for (int i = 0; i < eventEvents.getRowCount(); i++)
 					// {
 					// String trigger = eventEvents.getValueAt(i, 0).toString();
 					// String content = eventEvents.getValueAt(i, 1).toString();
 					//
 					// if (content.length() == 0)
 					// {
 					// valid = true;
 					// message = "Please edit or remove Event #" + (i + 1);
 					// break;
 					// }
 					//
 					// JSONObject o = new JSONObject();
 					// o.put("trigger", trigger);
 					// }
 
 					int selectedRow = tree.getSelectionRows()[0];
 					((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).setUserObject(entityID.getText());
 					((DefaultTreeModel) tree.getModel()).reload();
 					tree.expandRow(1);
 					tree.setSelectionRow(selectedRow);
 					refresh();
 
 				}
 				catch (JSONException e1)
 				{
 					e1.printStackTrace();
 				}
 			}
 		});
 		apply.setBounds(0, uiPanel.getHeight() - 27, uiPanel.getWidth(), 25);
 		uiPanel.add(apply);
 
 		refresh();
 	}
 
 	private void editEvent() throws JSONException
 	{
 		JSONObject entityData = entities.getJSONObject(tree.getSelectionRows()[0] - 2);
 		EntityBuilder entity = EntityRegistry.getEntityBuilder(entityData.getString("name"));
 
 		eventDialog = new JDialog(this, "BOLT Event Editor", true);
 		eventDialog.setLayout(new FlowLayout());
 		Vector<String> data = new Vector<>();
 		for (int i = 0; i < entities.length(); i++)
 		{
 			try
 			{
 				data.add(entities.getJSONObject(i).getString("id"));
 			}
 			catch (JSONException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		eventTarget = new JSuggestField((Window) eventDialog, data);
 		eventTarget.setPreferredSize(new Dimension(150, 22));
 		eventTarget.setFocusable(false);
 		eventTarget.addMouseListener(new MouseAdapter()
 		{
 			@Override
 			public void mouseClicked(MouseEvent e)
 			{
 				eventTarget.setFocusable(true);
 				eventTarget.requestFocus();
 			}
 		});
 		eventDialog.add(eventTarget);
 
 		ArrayList<String> functions = new ArrayList<>();
 		functions.add("-- Choose a function --");
 
 		eventFunction = new JComboBox<>(functions.toArray(new String[] {}));
 		eventTarget.getDocument().addDocumentListener(new DocumentListener()
 		{
 
 			@Override
 			public void removeUpdate(DocumentEvent e)
 			{
 				updateEditEventFunction();
 			}
 
 			@Override
 			public void insertUpdate(DocumentEvent e)
 			{
 				updateEditEventFunction();
 			}
 
 			@Override
 			public void changedUpdate(DocumentEvent e)
 			{
 				updateEditEventFunction();
 			}
 		});
 
 		eventDialog.add(eventFunction);
 		eventDialog.setSize(eventFunction.getPreferredSize().width + eventTarget.getPreferredSize().width + 30, 250);
 		final JTable params = new JTable(new DefaultTableModel(new String[] { "Name (Type)", "Value" }, 0))
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean isCellEditable(int row, int column)
 			{
 				if (column == 0) return false; // name column
 				return true;
 			}
 		};
 
 		eventFunction.addItemListener(new ItemListener()
 		{
 			@Override
 			public void itemStateChanged(ItemEvent e)
 			{
 				if (e.getStateChange() == ItemEvent.DESELECTED || eventFunction.getSelectedIndex() == 0) return;
 
 				DefaultTableModel dtm = (DefaultTableModel) params.getModel();
 				dtm.setRowCount(0);
 
 				String[] params = e.getItem().toString().substring(e.getItem().toString().indexOf("(") + 1, e.getItem().toString().indexOf(")")).split(", ");
 				for (String param : params)
 					dtm.addRow(new String[] { param.split(" ")[1] + " (" + param.split(" ")[0] + ")", "" });
 			}
 		});
 
 		params.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
 		params.setRowHeight(22);
 		Vector<String> cusVals = new Vector<>();
 		for (String key : entity.customValues.keySet())
 		{
 			cusVals.add("@" + key);
 		}
 		JSuggestField value = new JSuggestField(eventDialog, cusVals);
 		value.setSuggestMatcher(new JSuggestField.StartsWithMatcher());
 		params.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(value));
 		params.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		JScrollPane jsp = new JScrollPane(params, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		jsp.setPreferredSize(new Dimension(eventDialog.getWidth() - 20, eventDialog.getHeight() - 60 - 35));
 		params.setFillsViewportHeight(true);
 		eventDialog.add(jsp);
 		JButton apply = new JButton(new AbstractAction("Apply")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				boolean valid = true;
 				String message = "";
 				if (eventTarget.getText().length() == 0)
 				{
 					valid = false;
 					message += "Please enter a target ID!\n";
 				}
 
 				if (valid && eventFunction.getSelectedIndex() == 0)
 				{
 					valid = false;
 					message += "Please select a function!\n";
 				}
 
 				if (valid)
 				{
 					for (int i = 0; i < params.getRowCount(); i++)
 					{
 						if (params.getValueAt(i, 1).toString().length() == 0)
 						{
 							valid = false;
 							message += "Please enter a value for the parameter \"" + params.getValueAt(i, 0) + "\"!\n";
 							break;
 						}
 					}
 				}
 
 				if (!valid)
 				{
 					JOptionPane.showMessageDialog(eventDialog, "Please solve the following problem(s) before applying:\n" + message, "Error!", JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 
 				String p = "";
 
 				for (int i = 0; i < params.getRowCount(); i++)
 					p += params.getValueAt(i, 1).toString() + ", ";
 
 				if (p.length() > 2) p = p.substring(0, p.length() - 2);
 
 				String result = eventTarget.getText() + ", " + eventFunction.getSelectedItem().toString().replaceAll("\\(.{1,}\\)", "") + "(" + p + ")";
 				eventEvents.setValueAt(result, eventEvents.getSelectedRow(), 1);
 
 				eventDialog.dispose();
 			}
 		});
 		apply.setPreferredSize(new Dimension(eventDialog.getWidth() - 20, 24));
 		eventDialog.add(apply);
 		eventDialog.setResizable(false);
 		eventDialog.setDefaultCloseOperation(HIDE_ON_CLOSE);
 		eventDialog.setLocationRelativeTo(null);
 		eventDialog.setVisible(true);
 	}
 
 	private void updateEditEventFunction()
 	{
 		try
 		{
 			for (int i = 0; i < entities.length(); i++)
 			{
 				if (entities.getJSONObject(i).getString("id").equals(eventTarget.getText()))
 				{
 					EntityBuilder entity = EntityRegistry.getEntityBuilder(entities.getJSONObject(i).getString("name"));
 					ArrayList<String> functions = new ArrayList<>();
 					functions.add("-- Choose a function --");
 					for (String f : entity.functions)
 					{
 						functions.add(f);
 					}
 					eventFunction.setModel(new DefaultComboBoxModel<String>(functions.toArray(new String[] {})));
 
 					eventDialog.setSize(eventFunction.getPreferredSize().width + eventTarget.getPreferredSize().width + 30, 250);
 				}
 			}
 		}
 		catch (JSONException e1)
 		{
 			e1.printStackTrace();
 		}
 	}
 }

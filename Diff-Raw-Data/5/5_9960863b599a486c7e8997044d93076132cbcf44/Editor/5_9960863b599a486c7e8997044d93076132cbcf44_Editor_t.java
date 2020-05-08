 package de.dakror.liturfaliarcest.editor;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.KeyStroke;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SpringLayout;
 import javax.swing.ToolTipManager;
 import javax.swing.UIManager;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import de.dakror.gamesetup.util.Compressor;
 import de.dakror.gamesetup.util.Helper;
 import de.dakror.gamesetup.util.swing.SpringUtilities;
 import de.dakror.gamesetup.util.swing.WrapLayout;
 import de.dakror.liturfaliarcest.game.Game;
 import de.dakror.liturfaliarcest.game.animation.Animation;
 import de.dakror.liturfaliarcest.game.item.Item;
 import de.dakror.liturfaliarcest.settings.CFG;
 
 /**
  * @author Dakror
  */
 public class Editor extends JFrame
 {
 	private static final long serialVersionUID = 1L;
 	public static Editor currentEditor;
 	
 	JSONArray entities;
 	Point lt = new Point(-1, -1), rb = new Point(-1, -1);
 	
 	boolean devMode;
 	
 	Entity selectedEntity;
 	JLabel selectedEntityOriginal;
 	File entlist, map;
 	
 	MapPanel mapPanel;
 	
 	public Editor()
 	{
 		super("Liturfaliar Cest Editor");
 		Item.init();
 		Animation.init();
 		
 		currentEditor = this;
 		
 		setSize(1280, 720);
 		setLocationRelativeTo(null);
 		setResizable(false);
 		setIconImage(Game.getImage("system/editor.png"));
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		devMode = new File(System.getProperty("user.dir"), "src").exists();
 		
 		try
 		{
 			if (devMode)
 			{
 				entlist = new File(System.getProperty("user.dir"), "src/main/resources/entities.entlist");
 				entities = new JSONArray(Helper.getFileContent(entlist));
 			}
 			else entities = new JSONArray(Helper.getURLContent(getClass().getResource("/entities.entlist")));
 		}
 		catch (JSONException e1)
 		{
 			e1.printStackTrace();
 		}
 		
 		initJMenuBar();
 		initComponents();
 		
 		setVisible(true);
 	}
 	
 	public void initComponents()
 	{
 		final JTabbedPane cp = new JTabbedPane();
 		
 		if (devMode) cp.addTab("Entity Editor", initEntityEditor());
 		
 		cp.addTab("Karten-Editor", initMapEditor(devMode));
 		cp.addChangeListener(new ChangeListener()
 		{
 			@Override
 			public void stateChanged(ChangeEvent e)
 			{
 				if (cp.getSelectedIndex() == 0)
 				{
 					cp.setComponentAt(1, initEntityEditor());
 					map = null;
 				}
 				if (cp.getSelectedIndex() == 1) cp.setComponentAt(1, initMapEditor(false));
 			}
 		});
 		
 		cp.setSelectedIndex(devMode ? 1 : 0);
 		setContentPane(cp);
 	}
 	
 	private void initJMenuBar()
 	{
 		JMenuBar jmb = new JMenuBar();
 		setJMenuBar(jmb);
 		
 		JMenu file = new JMenu("Datei");
 		JMenuItem newFile = new JMenuItem(new AbstractAction("Neue Karte...")
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.dir")));
 				jfc.setMultiSelectionEnabled(false);
 				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				jfc.setFileFilter(new FileNameExtensionFilter("Verzeichis mit Karten-Dateien", "."));
 				jfc.setApproveButtonText("Erstellen");
 				jfc.setDialogTitle("Neue Karte");
 				
 				if (jfc.showOpenDialog(Editor.this) == JFileChooser.APPROVE_OPTION)
 				{
 					File f = jfc.getSelectedFile();
 					if (!isValidMapFolder(f))
 					{
 						JOptionPane.showMessageDialog(jfc, "Dieses Verzeichnis enthält keine valide Liturfaliar Cest Karte!", "Fehler: Ungültiges Verzeichnis", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 					
 					map = new File(f, f.getName() + ".json");
 					if (map.exists())
 					{
 						JOptionPane.showMessageDialog(jfc, "Diese Karte existiert bereits!", "Fehler: Karte bereits vorhanden", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 					
 					Helper.setFileContent(map, "[]");
 					openMap();
 				}
 			}
 		});
 		newFile.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
 		file.add(newFile);
 		
 		JMenuItem loadFile = new JMenuItem(new AbstractAction("Karte laden...")
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.dir")));
 				jfc.setMultiSelectionEnabled(false);
 				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 				jfc.setFileFilter(new FileNameExtensionFilter("Liturfaliar Cest Entity Karte (*.json)", "json"));
 				jfc.setDialogTitle("Karte laden");
 				
 				if (jfc.showOpenDialog(Editor.this) == JFileChooser.APPROVE_OPTION)
 				{
 					map = jfc.getSelectedFile();
 					
 					openMap();
 				}
 			}
 		});
 		loadFile.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
 		file.add(loadFile);
 		
 		JMenuItem saveFile = new JMenuItem(new AbstractAction("Karte speichern...")
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				if (map != null) saveMap();
 			}
 		});
 		saveFile.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
 		file.add(saveFile);
 		
 		getJMenuBar().add(file);
 		
 		JMenu tools = new JMenu("Werkzeuge");
 		tools.add(new JMenuItem(new AbstractAction("Bumpmap Converter")
 		{
 			
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				final JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.dir")));
 				jfc.setMultiSelectionEnabled(false);
 				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 				jfc.setFileFilter(new FileNameExtensionFilter("Liturfaliar Cest Bumpmap (*.png)", "png"));
 				jfc.setDialogTitle("Bumpmap umwandeln");
 				
 				if (jfc.showOpenDialog(Editor.this) == JFileChooser.APPROVE_OPTION)
 				{
 					if (!jfc.getSelectedFile().getName().endsWith("-2.png")) return;
 					
 					final String s = JOptionPane.showInputDialog("Bitte gib die erwünschte Anzahl an PPB ein:", 4);
 					if (s == null) return;
 					
 					final JDialog d = new JDialog(Editor.this);
 					d.setSize(400, 22);
 					d.setLocationRelativeTo(Editor.this);
 					d.setUndecorated(true);
 					final JProgressBar jsb = new JProgressBar(JScrollBar.HORIZONTAL);
 					jsb.setStringPainted(true);
 					d.setContentPane(jsb);
 					d.setVisible(true);
 					
 					new Thread()
 					{
 						@Override
 						public void run()
 						{
 							setPriority(MAX_PRIORITY);
 							
 							try
 							{
 								int ppb = Integer.parseInt(s);
 								
 								Area area = new Area();
 								BufferedImage bi = ImageIO.read(jfc.getSelectedFile());
 								int index = 0;
 								for (int i = 0; i < bi.getWidth(); i += ppb)
 								{
 									for (int j = 0; j < bi.getHeight(); j += ppb)
 									{
 										if (new Color(bi.getRGB(i, j)).equals(Color.white)) area.add(new Area(new Rectangle(i, j, ppb, ppb)));
 										index++;
 										jsb.setString(index + " / " + (bi.getWidth() * bi.getHeight() / ppb));
 										jsb.setValue(Math.round(index / (float) (bi.getWidth() / ppb * bi.getHeight() / ppb) * 100));
 									}
 								}
 								
 								ByteArrayOutputStream baos = new ByteArrayOutputStream();
 								ObjectOutputStream oos = new ObjectOutputStream(baos);
 								oos.writeObject(AffineTransform.getTranslateInstance(0, 0).createTransformedShape(area));
 								Compressor.compressFile(new File(jfc.getSelectedFile().getPath().replace("-2.png", ".bump")), baos.toByteArray());
 								
 								d.dispose();
 								JOptionPane.showMessageDialog(Editor.this, "Unwandlung abgeschlossen.", "Fertig", JOptionPane.INFORMATION_MESSAGE);
 							}
 							catch (Exception e2)
 							{
 								d.dispose();
 								JOptionPane.showMessageDialog(Editor.this, s + " ist keine Zahl!", "Fehler!", JOptionPane.ERROR_MESSAGE);
 								return;
 							}
 						}
 					}.start();
 				}
 			}
 		}));
 		tools.add(new JMenuItem(new AbstractAction("Karte umbenennen")
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.dir")));
 				jfc.setMultiSelectionEnabled(false);
 				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				jfc.setFileFilter(new FileNameExtensionFilter("Verzeichis mit Karten-Dateien", "."));
 				jfc.setApproveButtonText("Umbenennen");
 				jfc.setDialogTitle("Karte umbenennen");
 				
 				if (jfc.showOpenDialog(Editor.this) == JFileChooser.APPROVE_OPTION)
 				{
 					File f = jfc.getSelectedFile();
 					if (!isValidMapFolder(f))
 					{
 						JOptionPane.showMessageDialog(jfc, "Dieses Verzeichnis enthält keine valide Liturfaliar Cest Karte!", "Fehler: Ungültiges Verzeichnis", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 					
 					String newName = JOptionPane.showInputDialog(Editor.this, "Bitte gib den neuen Namen der Karte ein:", f.getName());
 					if (newName == null || newName.length() == 0) return;
 					
 					for (File file : f.listFiles())
 						file.renameTo(new File(f.getPath() + "/" + file.getName().replace(f.getName(), newName)));
 					
 					f.renameTo(new File(f.getParent() + "/" + f.getName().replace(f.getName(), newName)));
 				}
 			}
 		}));
 		tools.add(new JMenuItem(new AbstractAction("Icon Selecter")
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				IconSelecter.create();
 			}
 		}));
 		tools.add(new JMenuItem(new AbstractAction("Animationsliste einsehen")
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				try
 				{
 					File f = File.createTempFile("anim", ".csv");
 					Helper.copyInputStream(getClass().getResourceAsStream("/csv/anim.csv"), new FileOutputStream(f));
 					Desktop.getDesktop().open(f);
 				}
 				catch (Exception e1)
 				{
 					e1.printStackTrace();
 				}
 			}
 		}));
 		tools.add(new JMenuItem(new AbstractAction("Itemliste einsehen")
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				try
 				{
 					File f = File.createTempFile("items", ".csv");
 					Helper.copyInputStream(getClass().getResourceAsStream("/csv/items.csv"), new FileOutputStream(f));
 					Desktop.getDesktop().open(f);
 				}
 				catch (Exception e1)
 				{
 					e1.printStackTrace();
 				}
 			}
 		}));
 		tools.add(new JMenuItem(new AbstractAction("Questliste einsehen")
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				try
 				{
 					File f = File.createTempFile("quests", ".csv");
 					Helper.copyInputStream(getClass().getResourceAsStream("/csv/quests.csv"), new FileOutputStream(f));
 					Desktop.getDesktop().open(f);
 				}
 				catch (Exception e1)
 				{
 					e1.printStackTrace();
 				}
 			}
 		}));
 		tools.add(new JMenuItem(new AbstractAction("Boden-Editor")
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				new FloorEditor();
 			}
 		}));
 		
 		getJMenuBar().add(tools);
 	}
 	
 	private JSplitPane initEntityEditor()
 	{
 		((JMenu) getJMenuBar().getSubElements()[0]).setEnabled(false);
 		
 		JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 		p.setEnabled(false);
 		
 		JPanel left = new JPanel(new BorderLayout());
 		final JPanel tiles = new JPanel();
 		tiles.setLayout(null);
 		final JList<String> tilesets = new JList<>(CFG.TILES);
 		tilesets.addListSelectionListener(new ListSelectionListener()
 		{
 			@Override
 			public void valueChanged(ListSelectionEvent e)
 			{
 				new Thread()
 				{
 					@Override
 					public void run()
 					{
 						tiles.removeAll();
 						
 						BufferedImage bi = Game.getImage("tiles/" + tilesets.getSelectedValue());
 						tiles.setPreferredSize(new Dimension(bi.getWidth(), bi.getHeight()));
 						for (int i = 0; i < bi.getHeight() / 32; i++)
 						{
 							for (int j = 0; j < bi.getWidth() / 32; j++)
 							{
 								final JLabel l = new JLabel(new ImageIcon(bi.getSubimage(j * 32, i * 32, 32, 32)));
 								l.setBounds(j * 32, i * 32, 32, 32);
 								l.addMouseListener(new MouseAdapter()
 								{
 									@Override
 									public void mouseEntered(MouseEvent e)
 									{
 										if (l.getBorder() == null || !((LineBorder) l.getBorder()).getLineColor().equals(Color.red)) l.setBorder(BorderFactory.createLineBorder(Color.black));
 									}
 									
 									@Override
 									public void mouseExited(MouseEvent e)
 									{
 										if (l.getBorder() == null || !((LineBorder) l.getBorder()).getLineColor().equals(Color.red)) l.setBorder(null);
 									}
 									
 									@Override
 									public void mousePressed(MouseEvent e)
 									{
 										if (!((LineBorder) l.getBorder()).getLineColor().equals(Color.red)) l.setBorder(BorderFactory.createLineBorder(Color.red));
 										else l.setBorder(BorderFactory.createLineBorder(Color.black));
 									}
 								});
 								tiles.add(l);
 							}
 						}
 						tiles.revalidate();
 						tiles.repaint();
 						((JScrollPane) tiles.getParent().getParent()).getVerticalScrollBar().setValue(0);
 						((JScrollPane) tiles.getParent().getParent()).getHorizontalScrollBar().setValue(0);
 					}
 				}.start();
 			}
 		});
 		JScrollPane wrap = new JScrollPane(tilesets);
 		wrap.setPreferredSize(new Dimension(300, 150));
 		left.add(wrap, BorderLayout.PAGE_START);
 		wrap = new JScrollPane(tiles);
 		wrap.getVerticalScrollBar().setUnitIncrement(32);
 		wrap.setPreferredSize(new Dimension(200, 490));
 		left.add(wrap, BorderLayout.PAGE_END);
 		p.add(left);
 		
 		JPanel right = new JPanel(null);
 		final JLabel preview = new JLabel();
 		right.add(preview);
 		
 		final JSpinner bumpX = new JSpinner();
 		bumpX.setPreferredSize(new Dimension(100, bumpX.getPreferredSize().height));
 		final JSpinner bumpY = new JSpinner();
 		final JSpinner bumpWidth = new JSpinner();
 		final JSpinner bumpHeight = new JSpinner();
 		final JPanel settings = new JPanel(new SpringLayout());
 		final AbstractAction update = new AbstractAction()
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				lt = new Point(-1, -1);
 				rb = new Point(-1, -1);
 				ArrayList<JLabel> sel = new ArrayList<>();
 				for (Component c : tiles.getComponents())
 				{
 					if (!(c instanceof JLabel) || ((JLabel) c).getBorder() == null || !((LineBorder) ((JLabel) c).getBorder()).getLineColor().equals(Color.red)) continue;
 					if (lt.x == -1 || c.getX() < lt.x) lt.x = c.getX();
 					if (lt.y == -1 || c.getY() < lt.y) lt.y = c.getY();
 					
 					if (rb.x == -1 || c.getX() > rb.x) rb.x = c.getX();
 					if (rb.y == -1 || c.getY() > rb.y) rb.y = c.getY();
 					sel.add((JLabel) c);
 				}
 				
 				BufferedImage bi = new BufferedImage(rb.x - lt.x + 32, rb.y - lt.y + 32, BufferedImage.TYPE_INT_ARGB);
 				Graphics2D g = (Graphics2D) bi.getGraphics();
 				for (JLabel l : sel)
 					g.drawImage(((ImageIcon) l.getIcon()).getImage(), l.getX() - lt.x, l.getY() - lt.y, null);
 				
 				g.setColor(Color.red);
 				g.drawRect((int) bumpX.getValue(), (int) bumpY.getValue(), (int) bumpWidth.getValue(), (int) bumpHeight.getValue());
 				
 				preview.setIcon(new ImageIcon(bi));
 				preview.setBounds((950 - bi.getWidth()) / 2, 50, bi.getWidth(), bi.getHeight());
 				
 				bumpX.setModel(new SpinnerNumberModel((int) bumpX.getValue() > bi.getWidth() - 1 ? bi.getWidth() - 1 : (int) bumpX.getValue(), 0, bi.getWidth() - 1, 1));
 				bumpY.setModel(new SpinnerNumberModel((int) bumpY.getValue() > bi.getHeight() - 1 ? bi.getHeight() - 1 : (int) bumpY.getValue(), 0, bi.getHeight() - 1, 1));
 				bumpWidth.setModel(new SpinnerNumberModel((int) bumpWidth.getValue() > bi.getWidth() || (int) bumpWidth.getValue() == 0 ? bi.getWidth() : (int) bumpWidth.getValue(), 0, bi.getWidth(), 1));
 				bumpHeight.setModel(new SpinnerNumberModel((int) bumpHeight.getValue() > bi.getHeight() || (int) bumpHeight.getValue() == 0 ? bi.getHeight() : (int) bumpHeight.getValue(), 0, bi.getHeight(), 1));
 				
 				int w = settings.getPreferredSize().width;
 				int h = settings.getPreferredSize().height;
 				settings.setBounds((950 - w) / 2, 60 + bi.getHeight(), w, h);
 			}
 		};
 		
 		settings.add(new JButton(new AbstractAction("Aktualisieren")
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				update.actionPerformed(null);
 			}
 		}));
 		settings.add(new JLabel());
 		
 		settings.add(new JLabel("BumpX:"));
 		bumpX.addChangeListener(new ChangeListener()
 		{
 			@Override
 			public void stateChanged(ChangeEvent e)
 			{
 				update.actionPerformed(null);
 			}
 		});
 		settings.add(bumpX);
 		settings.add(new JLabel("BumpY:"));
 		bumpY.addChangeListener(new ChangeListener()
 		{
 			@Override
 			public void stateChanged(ChangeEvent e)
 			{
 				update.actionPerformed(null);
 			}
 		});
 		settings.add(bumpY);
 		settings.add(new JLabel("BumpWidth:"));
 		bumpWidth.addChangeListener(new ChangeListener()
 		{
 			@Override
 			public void stateChanged(ChangeEvent e)
 			{
 				update.actionPerformed(null);
 			}
 		});
 		settings.add(bumpWidth);
 		settings.add(new JLabel("BumpHeight:"));
 		bumpHeight.addChangeListener(new ChangeListener()
 		{
 			@Override
 			public void stateChanged(ChangeEvent e)
 			{
 				update.actionPerformed(null);
 			}
 		});
 		settings.add(bumpHeight);
 		settings.add(new JButton(new AbstractAction("Speichern")
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				try
 				{
 					JSONObject o = new JSONObject();
 					o.put("t", tilesets.getSelectedValue());
 					o.put("x", lt.x);
 					o.put("y", lt.y);
 					o.put("w", rb.x - lt.x + 32);
 					o.put("h", rb.y - lt.y + 32);
 					o.put("bx", bumpX.getValue());
 					o.put("by", bumpY.getValue());
 					o.put("bw", bumpWidth.getValue());
 					o.put("bh", bumpHeight.getValue());
 					entities.put(o);
 					
 					if (devMode) Helper.setFileContent(entlist, entities.toString());
 				}
 				catch (JSONException e1)
 				{
 					e1.printStackTrace();
 				}
 			}
 		}));
 		settings.add(new JLabel());
 		
 		SpringUtilities.makeCompactGrid(settings, 6, 2, 0, 0, 0, 0);
 		int w = settings.getPreferredSize().width;
 		int h = settings.getPreferredSize().height;
 		settings.setBounds((950 - w) / 2, 60, w, h);
 		right.add(settings);
 		
 		p.add(right);
 		
 		return p;
 	}
 	
 	private JSplitPane initMapEditor(boolean init)
 	{
 		((JMenu) getJMenuBar().getSubElements()[0]).setEnabled(true);
 		
 		JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 		p.setEnabled(false);
 		
 		final JPanel left = new JPanel(new WrapLayout(FlowLayout.LEFT, 0, 0));
 		left.setSize(new Dimension(280, 1));
 		
 		ArrayList<JLabel> labels = new ArrayList<>();
 		
 		for (int i = 0; i < entities.length(); i++)
 		{
 			try
 			{
 				JSONObject o = entities.getJSONObject(i);
				BufferedImage img = Helper.toBufferedImage((!o.getString("t").equals("black")) ? Game.getImage("tiles/" + o.getString("t")).getSubimage(o.getInt("x"), o.getInt("y"), o.getInt("w"), o.getInt("h")) : new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB));
 				if (o.getString("t").equals("black"))
 				{
 					Graphics2D g = (Graphics2D) img.getGraphics();
 					g.setColor(Color.black);
 					Helper.drawHorizontallyCenteredString("E", 32, 32, g, 44);
 				}
 				else
 				{
 					Graphics2D g = (Graphics2D) img.getGraphics();
					g.setColor(Color.decode("#00bb00"));
 					g.drawRect(o.getInt("bx"), o.getInt("by"), o.getInt("bw"), o.getInt("bh"));
 				}
 				final JLabel l = new JLabel(new ImageIcon(img));
 				l.setPreferredSize(new Dimension(o.getInt("w"), o.getInt("h")));
 				l.setName(i + "");
 				l.addMouseListener(new MouseAdapter()
 				{
 					@Override
 					public void mouseEntered(MouseEvent e)
 					{
 						if (l.getBorder() == null || !((LineBorder) l.getBorder()).getLineColor().equals(Color.red)) l.setBorder(BorderFactory.createLineBorder(Color.black));
 					}
 					
 					@Override
 					public void mouseExited(MouseEvent e)
 					{
 						if (l.getBorder() == null || !((LineBorder) l.getBorder()).getLineColor().equals(Color.red)) l.setBorder(null);
 					}
 					
 					@Override
 					public void mousePressed(MouseEvent e)
 					{
 						if (e.getButton() != MouseEvent.BUTTON1) return;
 						if (!((LineBorder) l.getBorder()).getLineColor().equals(Color.red))
 						{
 							for (Component c : left.getComponents())
 								((JLabel) c).setBorder(null);
 							
 							l.setBorder(BorderFactory.createLineBorder(Color.red));
 							
 							Entity clone = new Entity(l.getIcon());
 							clone.setPreferredSize(l.getPreferredSize());
 							clone.setName(l.getName());
 							selectedEntity = clone;
 							selectedEntityOriginal = l;
 						}
 						else l.setBorder(BorderFactory.createLineBorder(Color.black));
 					}
 				});
 				labels.add(l);
 			}
 			catch (JSONException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		Collections.sort(labels, new Comparator<JLabel>()
 		{
 			@Override
 			public int compare(JLabel o1, JLabel o2)
 			{
 				return Integer.compare(o1.getPreferredSize().width * o1.getPreferredSize().height, o2.getPreferredSize().width * o2.getPreferredSize().height);
 			}
 		});
 		for (JLabel l : labels)
 			left.add(l);
 		
 		if (entities.length() == 0) left.add(new JLabel("Create some entities first!"));
 		
 		
 		JScrollPane wrap = new JScrollPane(left);
 		wrap.setPreferredSize(new Dimension(300, 680));
 		wrap.getVerticalScrollBar().setUnitIncrement(32);
 		p.add(wrap);
 		
 		mapPanel = new MapPanel();
 		wrap = new JScrollPane(mapPanel);
 		wrap.addMouseWheelListener(new MouseWheelListener()
 		{
 			@Override
 			public void mouseWheelMoved(MouseWheelEvent e)
 			{
 				mapPanel.repaint();
 			}
 		});
 		wrap.getHorizontalScrollBar().setUnitIncrement(32);
 		wrap.getVerticalScrollBar().setUnitIncrement(32);
 		wrap.setPreferredSize(new Dimension(900, 680));
 		p.add(wrap);
 		
 		return p;
 	}
 	
 	public void openMap()
 	{
 		mapPanel.openMap();
 		mapPanel.repaint();
 	}
 	
 	public void saveMap()
 	{
 		JSONArray a = new JSONArray();
 		
 		for (Component c : mapPanel.getComponents())
 		{
 			if (!(c instanceof Entity)) continue;
 			Entity e = (Entity) c;
 			JSONObject o = new JSONObject();
 			try
 			{
 				o.put("i", Integer.parseInt(c.getName()));
 				o.put("x", c.getX());
 				o.put("y", c.getY());
 				o.put("uid", ((Entity) c).uid);
 				if (e.e.length() > 0) o.put("e", e.e);
 				if (e.m.length() > 0) o.put("m", e.m);
 			}
 			catch (JSONException e1)
 			{
 				e1.printStackTrace();
 			}
 			a.put(o);
 		}
 		
 		Helper.setFileContent(map, a.toString(), "UTF-8");
 	}
 	
 	public boolean isValidMapFolder(File f)
 	{
 		if (!f.isDirectory()) return false;
 		return new File(f, f.getName() + "-0.png").exists() && (new File(f, f.getName() + "-2.png").exists() || new File(f, f.getName() + ".bump").exists());
 	}
 	
 	public static void main(String[] args)
 	{
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			ToolTipManager.sharedInstance().setInitialDelay(0);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		new Game();
 		
 		new Editor();
 	}
 }

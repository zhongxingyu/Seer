 package applets.Termumformungen$in$der$Technik_03_Logistik;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JApplet;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Point;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.JComboBox;
 
 
 public class Applet extends JApplet {
 	private static final long serialVersionUID = 1L;
 	static Applet instance;
 	
 	private JPanel jContentPane = null;
 	public Content content = new Content(this);
 
 	public static Font monospaceFont; 
 	public static Font defaultFont;
 	
 	JScrollPane scrollPane = null;
 	
	/**
	 * This is the xxx default constructor
	 */
 	public Applet() {
 		super();
 		instance = this;
 	}
 	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
 	public void init() {
 		try {
 			defaultFont = Font.createFont(Font.TRUETYPE_FONT, getResource("DejaVuSans.ttf")).deriveFont(12.0f);
 			monospaceFont = Font.createFont(Font.TRUETYPE_FONT, getResource("DejaVuSansCondensed.ttf")).deriveFont(12.0f);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		//testLocalFonts();
 		
 		content.init();
 		Container pane = getJContentPane();
 		if(scrollPane != null) {
 			scrollPane.setViewportView(pane);
 			pane = scrollPane;
 		}
 		this.setContentPane(pane);
 		content.postinit();
 	}
 
 	public static void testLocalFonts() {
 		for(FileIterator i = new FileIterator(
 				new File("/usr/share/"),
 				new FileFilter() {
 					public boolean accept(File pathname) {
 						if(pathname.isDirectory()) return true;
 						return 
 							pathname.getName().toLowerCase().endsWith(".pfb")
 							|| pathname.getName().toLowerCase().endsWith(".ttf");
 					}
 		}); i.hasNext(); ) {
 			File f = i.next();
 			if(f.isFile()) {
 				int type = Font.TRUETYPE_FONT;
 				if(f.getName().toLowerCase().endsWith(".pfb")) type = Font.TYPE1_FONT;
 				Font font;
 				try {
 					font = Font.createFont(type, f);
 					if(isFontSupported(font)) {
 						System.out.println("Font " + font.getName() + " (" + f.getName() + ") is supported!");
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}		
 				
 		if(isFontSupported(new Font("Monospace", 0, 1))) {
 			System.out.println("Font Monospace is supported!");
 		}
 	}
 	
 	public static boolean isFontSupported(Font font) {
 		String displayChars =
 			"⊂∩≠⁻¹→" +
 			"→ ↦ ∞ ∈ ℝ π ℤ ℕ ℚ" +
 			"≤ ⇒ ∉ ∅ ⊆ ∩ ∪" +
 			"∙ × ÷ ± ― ≠ √" +
 			"θ ≈ ⁻¹⁰⁴ •";
 		int c = font.canDisplayUpTo(displayChars);
 		if(c == -1)
 			return true;
 		if(c > 0) // don't inform about fonts which have already problems with the first symbol
 			System.out.println("HINT: font " + font.getName() + " cannot display char " + displayChars.charAt(c));
 		return false;		
 	}
 	
 	public static VisualThing newVTLimes(int stepX, int stepY, String var, String c) {
 		return new VTFrac(stepX, stepY,
 				new VTLabel("lim", 0, 0, "monospace"),
 				new VTLabel(var + " → " + c, 0, 0, "monospace"),
 				false);
 	}
 
 	public static VisualThing newVTLimes(int stepX, int stepY, VisualThing sub) {
 		return new VTFrac(stepX, stepY,
 				new VTLabel("lim", 0, 0, "monospace"), sub, false);
 	}
 	
 	public static VisualThing newVTLimes(int stepX, int stepY, VisualThing var, VisualThing c) {
 		return new VTFrac(stepX, stepY,
 				new VTLabel("lim", 0, 0, "monospace"),
 				new VTContainer(0, 0, new VisualThing[] {
 						var, new VTLabel("→", 0, 0, "monospace"), c
 				}),
 				false);
 	}
 
 	/**
 	 * fügt alle Dinge zum panel hinzu; siehe VisualThing für weitere Details
 	 */
 	public static Point addVisualThings(JPanel panel, VisualThing[] things) {
 		return addVisualThings(panel, things, false);
 	}
 
 	public static Point addVisualThings(JPanel panel, VisualThing[] things, boolean onlyCalcSize) {
 		final boolean debugPrint = false;
 		if(debugPrint) System.out.println("VTs {");
 		int curX = 0, curY = 0;
 		List<Integer> xs_old = null;
 		List<Integer> xs = new LinkedList<Integer>();
 		Point max = new Point(0, 0);
 
 		for (int i = 0; i < things.length; i++) {
 			if (things[i].getStepY() != 0) {
 				curY = max.y + things[i].getStepY();
 				xs_old = xs;
 				xs = new LinkedList<Integer>();
 				curX = 0;
 			}
 			if (things[i].getStepX() < 0)
 				curX = (xs_old.get(-things[i].getStepX() - 1)).intValue();
 			else
 				curX += things[i].getStepX();
 			xs.add(new Integer(curX));
 
 			if(!onlyCalcSize) {
 				Component c = things[i].getComponent();
 				if(c != null) {
 					if(c.getParent() != panel)
 						panel.add(c);
 					c.setBounds(curX, curY, things[i].getWidth(), things[i].getHeight());
 					c.doLayout();
 					c.setBounds(curX, curY, things[i].getWidth(), things[i].getHeight());
 					if(debugPrint) System.out.println(things[i] + " bounds: " + c.getBounds());
 				}
 			}
 			max.x = Math.max(max.x, curX + things[i].getWidth());
 			max.y = Math.max(max.y, curY + things[i].getHeight());
 
 			curX += things[i].getWidth();
 		}
 
 		if(debugPrint) System.out.println("}");
 		return max;
 	}
 
 	public static interface ComponentWalker {
 		// false is a signal to break
 		public boolean meet(Component comp);
 	}
 
 	public static boolean ForEachComponent(JPanel panel, ComponentWalker walker) {
 		for (int i = 0; i < panel.getComponents().length; i++) {
 			walker.meet(panel.getComponents()[i]);
 			if (panel.getComponents()[i] instanceof JPanel)
 				if (!ForEachComponent((JPanel) panel.getComponents()[i], walker))
 					return false;
 		}
 		return true;
 	}
 
 	public boolean ForEachComponent(ComponentWalker walker) {
 		return ForEachComponent(getJContentPane(), walker);
 	}
 
 	/**
 	 * durchsucht alle Komponenten und gibt die erste zurück, deren Namen passt;
 	 * ansonsten null
 	 */
 	public Component getComponentByName(final String name) {
 		class CWalker implements ComponentWalker {
 			public Component comp;
 
 			public boolean meet(Component comp) {
 				String cname = comp.getName();
 				if (cname != null && cname.compareTo(name) == 0) {
 					this.comp = comp;
 					return false;
 				}
 				return true;
 			}
 		}
 		CWalker walker = new CWalker();
 		ForEachComponent(walker);
 		return walker.comp;
 	}
 
 	private Runnable createCheckButtonListener(final int startIndex,
 			final CorrectCheck check,
 			final Runnable correctAction, final Runnable wrongAction) {
 		return new Runnable() {
 			public void run() {
 				boolean correct = true;
 				if(check != null)
 					correct = check.isCorrect();
 				else
 					for (int i = startIndex;; i++) {
 						JComponent comp = (JComponent) getComponentByName("s" + i);
 						if (comp == null)
 							break;
 						String selected = comp instanceof JComboBox ? (String) ((JComboBox) comp)
 								.getSelectedItem()
 								: ((JTextField) comp).getText();
 						correct = isCorrect(i, selected);
 						if (!correct)
 							break;
 					}
 				if(check != null)
 					setResultLabel(startIndex, correct, check.getResultMsg());
 				else
 					setResultLabel(startIndex, correct);
 				if (correct) {
 					if (correctAction != null)
 						correctAction.run();
 				} else {
 					if (wrongAction != null)
 						wrongAction.run();
 				}
 				JComponent comp = (JComponent) getComponentByName("c" + startIndex + "_correct");
 				if (comp != null) comp.setVisible(correct);
 				comp = (JComponent) getComponentByName("c" + startIndex + "_wrong");
 				if (comp != null) comp.setVisible(!correct);
 			}
 		};
 	}
 
 	public Runnable createCheckButtonListener(int index, CorrectCheck check) {
 		return createCheckButtonListener(index, check, null, null);
 	}
 
 	private void setResultLabel(int index, boolean correct) {
 		if (correct) {
 			((JLabel) getComponentByName("res" + index))
 					.setForeground(new Color(0,200,0));
 			((JLabel) getComponentByName("res" + index))
 					.setText("alles ist richtig!");
 		} else {
 			((JLabel) getComponentByName("res" + index))
 					.setForeground(Color.RED);
 			((JLabel) getComponentByName("res" + index))
 					.setText("leider ist etwas falsch");
 		}
 	}
 
 	private void setResultLabel(int index, boolean correct, String msg) {
 		if (correct) {
 			((JLabel) getComponentByName("res" + index))
 					.setForeground(new Color(0,200,0));
 			((JLabel) getComponentByName("res" + index))
 					.setText(msg);
 		} else {
 			((JLabel) getComponentByName("res" + index))
 					.setForeground(Color.RED);
 			((JLabel) getComponentByName("res" + index))
 					.setText(msg);
 		}
 	}
 
 	Runnable createCheckButtonListener(int startIndex) {
 		return createCheckButtonListener(startIndex, null, null, null);
 	}
 
 	Runnable createHelpButtonListener(final int startIndex) {
 		return new Runnable() {
 			public void run() {
 				for (int i = startIndex;; i++) {
 					JComponent comp = (JComponent) getComponentByName("s" + i);
 					if (comp == null)
 						break;
 					String selected = comp instanceof JComboBox ? (String) ((JComboBox) comp)
 							.getSelectedItem()
 							: ((JTextField) comp).getText();
 					boolean correct = isCorrect(i, selected);
 					comp.setBackground(correct ? new Color(0,200,0) : Color.RED);
 				}
 			}
 		};
 	}
 
 	 Runnable createVisibler(final String name) {
 		return new Runnable() {
 			public void run() {
 				JComponent comp = (JComponent) getComponentByName(name);
 				if (comp == null)
 					return;
 				comp.setVisible(true);
 			}
 		};
 	}
 
 	public static interface CorrectCheck {
 		boolean isCorrect();
 		String getResultMsg();
 	}
 	
 	public static String getMultipliedString(String str, int n) {
 		String res = "";
 		for (int i = 0; i < n; i++)
 			res += str;
 		return res;
 	}
 
 	public void removeAllVisualThings(JPanel panel) {
 		panel.removeAll();
 	}
 
 	VTMeta vtmeta = new VTMeta(this, 0, 0, "", null, new Runnable() {
 				public void run() {
 					resetSelectorColors();
 					resetResultLabels();
 					resetResultContainers();
 				}
 			});
 	
 	public String getPackageName() {
 		return this.getClass().getPackage().getName();
 	}
 
 	public String getPackageNameBase() {
 		String name = getPackageName().substring("applets.".length());
 		int i = name.indexOf("_");
 		return name.substring(0, i);
 	}
 
 	public String getPackageNameShort() {
 		String name = getPackageName();
 		int i = name.indexOf("_");
 		return name.substring(i + 1);
 	}
 	
 	public String getPackageAsPath() {
 		return getPackageName().replace('.', '/');
 	}
 
 	final Runnable updater = new Runnable() {
 		public void run() {
 			resetSelectorColors();
 			resetResultLabels();
 			resetResultContainers();
 		}
 	};
 	
 	public InputStream getResource(String fileName) throws Exception {
 		InputStream res = null;
 		
 		try {
 			res = getClass().getClassLoader().getResourceAsStream(fileName);
 		} catch (Exception e) {}
 		if(res != null) return res;
 
 		try {
 			res = new FileInputStream(fileName);
 		} catch (Exception e) {}
 		if(res != null) return res;
 		
 		try {
 			String path = "../Lehreinheiten/" + getPackageNameBase() + "/Code/" + getPackageAsPath(); 
 			res = new FileInputStream(path + "/" + fileName);
 		} catch (Exception e) {}
 		if(res != null) return res;
 
 		try {
 			String path = "../Lehreinheiten/" + getPackageNameBase().replace('$', ' ') + "/Code/" + getPackageAsPath(); 
 			res = new FileInputStream(path + "/" + fileName);
 		} catch (Exception e) {}
 		if(res != null) return res;
 		
 		System.err.println("ERROR: FileNotFound: $workspace/" + fileName);
 		throw new Exception("ERROR: FileNotFound: " + fileName);
 	}
 		
 	private void updateDefaultVisualThings() {
 		removeAllVisualThings(jContentPane);
 
 		content.run();
 		
 		String contentStr = "";
 		try {
 			InputStreamReader file = new InputStreamReader(getResource("content.vtmeta"), "utf8");
 
 			try {
 				int c;
 				while(-1 != (c = file.read())) {
 					contentStr += (char)c;
 				}
 			}
 			finally {
 				try {
 					file.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 					
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		visualThings = vtmeta.getThingsByContentStr(contentStr);
 		jContentPane.doLayout();
 		
 		resetResultLabels();
 		resetSelectorColors();
 		resetResultContainers();				
 	}
 	
 	VisualThing[] visualThings = null;
 
 	void revalidateVisualThings() {
 		if(jContentPane != null) {
 			jContentPane.revalidate();
 		}
 	}
 	
 	public void resetResultContainers() {
 		ForEachComponent(new ComponentWalker() {
 			public boolean meet(Component comp) {
 				if (
 						comp.getName() != null
 						&& comp.getName().startsWith("c")
 						&& (comp.getName().endsWith("_wrong") || comp.getName().endsWith("_correct"))) {
 					comp.setVisible(false);
 				}
 				return true;
 			}
 		});
 	}
 	
 	/**
 	 * This method initializes jContentPane
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJContentPane() {
 		if (jContentPane == null) {
 			jContentPane = new JPanel() {
 				private static final long serialVersionUID = 1L;
 				@Override public void doLayout() {
 					Point size = addVisualThings(this, visualThings);
 					
 					if(size.x != getPreferredSize().width || size.y != getPreferredSize().height) {
 						setPreferredSize(new Dimension(size.x, size.y));
 						if(getParent() != null) getParent().validate();
 					}
 				}
 			};
 			jContentPane.setLayout(null);
 			jContentPane.setSize(getWidth(), getHeight());
 			jContentPane.setName("Applet.jContentPane");
 			updateDefaultVisualThings();
 		}
 		return jContentPane;
 	}
 
 	public static double parseNum(String txt) {
 		try {
 			txt = txt.replace(',', '.');
 			txt = txt.replaceAll(" ", "");
 			return Double.parseDouble(txt);
 		} catch (NumberFormatException e) {
 			return -666;
 		}
 	}
 	
 	public static boolean equalParseNum(String txt, double num, double eps) {
 		try {
 			txt = txt.replace(',', '.');
 			txt = txt.replaceAll(" ", "");
 			double p = Double.parseDouble(txt);
 			return Math.abs(p - num) < eps;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 	}
 
 	public String getSelected(int selId) {
 		Component comp = getComponentByName("s" + selId);
 		if(comp == null) return null;
 		if(comp instanceof JComboBox)
 			return (String) ((JComboBox)comp).getSelectedItem();
 		if(comp instanceof JTextField)
 			return ((JTextField)comp).getText();
 		return null;
 	}	
 	
 	public boolean isCorrect(int selId, String selected) {
 		Utils.Var var = vtmeta.getVar("s" + selId);
 		if(var != null) return var.value.compareToIgnoreCase(selected) == 0;
 
 		return content.isCorrect(selId, selected);
 	}
 
 	public void resetResultLabels() {
 		ForEachComponent(new ComponentWalker() {
 			public boolean meet(Component comp) {
 				if (comp.getName() != null && comp.getName().startsWith("res")) {
 					((JLabel) comp).setText("");
 				}
 				return true;
 			}
 		});
 	}
 
 	public void resetSelectorColors() {
 		ForEachComponent(new ComponentWalker() {
 			public boolean meet(Component comp) {
 				if (comp instanceof JComboBox || comp instanceof JTextField) {
 					comp.setBackground(Color.WHITE);
 				}
 				return true;
 			}
 		});
 	}
 
 
 } // @jve:decl-index=0:visual-constraint="10,10"

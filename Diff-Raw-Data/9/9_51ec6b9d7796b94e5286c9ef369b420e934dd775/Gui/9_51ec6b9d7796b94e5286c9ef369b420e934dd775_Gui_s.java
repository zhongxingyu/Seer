 package net.creapage.lessnow;
 
 import java.awt.CardLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.io.File;
 import java.security.InvalidParameterException;
 import java.util.regex.Pattern;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import net.iharder.dnd.FileDrop;
 
 public class Gui extends JFrame {
 	private static final long serialVersionUID = 1L;
 
 	private final CardLayout cl = new CardLayout();
 	private final JPanel pane = new JPanel(cl);
 	private final OverviewPane overview;
 	private CompilationProcess compilProc;
 	private ProjectConf defConf;
 
 	public Gui() {
 		this.overview = new OverviewPane(this);
 		this.compilProc = new CompilationProcess(overview);
 
 		this.setTitle("Less Now v" + LessNow.VERSION);
 		this.setIconImage(new ImageIcon(this.getClass().getResource("/res/less.png")).getImage());
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		this.getContentPane().add(pane);
 
 		JPanel dropPane = new JPanel(new GridLayout(1, 1));
 		dropPane.add(new JLabel(new ImageIcon(this.getClass().getResource("/res/drag_drop.png"))));
 
 		pane.add(dropPane, "DropPane");
 		pane.add(overview, "OverviewPane");
 
 		new FileDrop(dropPane, new FileDrop.Listener() {
 			public void filesDropped(final File[] fList) {
 				for (File d : fList) {
 					if (d.isDirectory())
 						addLessProject(d, new ProjectConf(d.getName(), defConf));
 				}
 			}
 		});
 
 		setWithOverview(false);
 		this.pack();
 	}
 
 	public void setConfigSize(Integer w, Integer h) {
 		if (w == null && h == null)
 			return;
 		if (w == null)
 			w = getSize().width;
 		if (h == null)
 			w = getSize().height;
 		setSize(w, h);
 	}
 
 	public void setConfigLocation(String locX, String locY) {
 		Dimension winSize = getSize();
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		// - x
 		int x;
 		if ("left".equals(locX))
 			x = 0;
 		else if ("right".equals(locX))
 			x = screenSize.width - winSize.width;
 		else if (locX == null || "center".equals(locX))
 			x = (screenSize.width - winSize.width) / 2;
 		else
 			throw new InvalidParameterException("Bad location-x parameter: " + locX);
 		// - y
 		int y;
 		if ("top".equals(locY))
 			y = 0;
 		else if ("bottom".equals(locY))
 			y = screenSize.height - winSize.height;
 		else if (locY == null || "middle".equals(locY))
 			y = (screenSize.height - winSize.height) / 2;
 		else
 			throw new InvalidParameterException("Bad location-y parameter: " + locY);
 		this.setLocation(x > 0 ? x : 0, y > 0 ? y : 0);
 	}
 
 	public void setDefaultProjectConf(ProjectConf defConf) {
 		this.defConf = defConf;
 	}
 
 	public void showGui() {
 		this.validate();
 	}
 
 	public void addLessProject(File dir, ProjectConf conf) {
 		if (conf.isAutoAddDirAsProjects())
 			autoAddDirAsProjects(dir, conf);
 		else
 			addDirProject(dir, conf);
 	}
 
 	public void setWithOverview(boolean b) {
 		if (b)
 			cl.show(pane, "OverviewPane");
 		else
 			cl.show(pane, "DropPane");
 	}
 
 	private void addDirProject(File dir, ProjectConf conf) {
 		System.out.println("Watch the project: " + conf.getName());
 		new DirProject(dir, conf, overview, compilProc);
 		setWithOverview(!overview.isEmpty());
 	}
 
 	private void autoAddDirAsProjects(File dir, ProjectConf conf) {
 		recursiveAddDirAsProject(dir, conf, 0);
 	}
 
 	private void recursiveAddDirAsProject(File dir, ProjectConf conf, int deepLevel) {
 		if (!dir.isDirectory())
 			throw new BugError("Auto-add project should be a directory: " + dir);
 		File[] subf = dir.listFiles();
 		if (subf != null) {
 			boolean hasLessFiles = false;
 			for (File current : subf) {
 				if (current.isDirectory())
 					recursiveAddDirAsProject(current, conf, deepLevel + 1);
 				if (current.isFile() && current.getName().endsWith(".less"))
 					hasLessFiles = true;
 			}
 			if (hasLessFiles) {
 				String regex = conf.getAutoAddDirRegexp();
 				if (regex == null || Pattern.matches(regex, dir.getPath() + '/')) {
 					String autoName = makeProjectName(conf.getName(), deepLevel, dir, conf.getAutoAddDirNameCount());
 					ProjectConf autoConf = new ProjectConf(autoName, conf);
 					addDirProject(dir, autoConf);
 				}
 			}
 		}
 	}
 
 	private static String makeProjectName(String baseProjectName, int currentDeepLevel, File dir, int dirCount) {
		StringBuilder sb = new StringBuilder(baseProjectName);
 		while (dirCount > 0 && currentDeepLevel > 0) {
			sb.append('/');
			sb.append(dir.getName());
 			dir = dir.getParentFile();
 			--currentDeepLevel;
 			--dirCount;
 		}
		return sb.toString();
 	}
 }

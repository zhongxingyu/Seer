 package ClassAdminFrontEnd;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.LinkedList;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import org.imgscalr.Scalr;
 import org.imgscalr.Scalr.Method;
 import org.imgscalr.Scalr.Mode;
 
 import com.sun.media.sound.Toolkit;
 
 import sun.java2d.pipe.BufferedBufImgOps;
 
 import ClassAdminBackEnd.EntityType;
 import ClassAdminBackEnd.Global;
 import ClassAdminBackEnd.Project;
 import ClassAdminBackEnd.RapidAssessmentComponentType;
 import ClassAdminBackEnd.RapidAssessmentContainerType;
 import ClassAdminBackEnd.RapidAssessmentMarkType;
 import ClassAdminBackEnd.RapidAssessmentRectangleType;
 
 public class RapidAssessmentCanvas extends JFrame {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	public static final int MARK_SIZE = 15;
 	private String backgroundFileName;
 
 	private Project project;
	JComboBox<RapidAssessmentContainerType> loadCombo;
 
 	private BufferedImage backGround;
 	private BufferedImage resizedBackGround = null;
 	// private LinkedList<MyRectangle> rectangles = new
 	// LinkedList<MyRectangle>();
 	private EntityType assessedEntity;
 	private MyMarkPoint lastcreated = null;
 	// private LinkedList<MyMarkPoint> marks = new LinkedList<MyMarkPoint>();
 	private ContainerPanel parentPanel;
 	private MyRectangle parentRect;
 
 	public String getBackgroundFileName() {
 		return backgroundFileName;
 	}
 
 	public void setBackgroundFileName(String backgroundFileName) {
 		this.backgroundFileName = backgroundFileName;
 	}
 
 	public BufferedImage getBackGround() {
 		return backGround;
 	}
 
 	public void setBackGround(BufferedImage backGround) {
 		this.backGround = backGround;
 	}
 
 	public BufferedImage getResizedBackGround() {
 		return resizedBackGround;
 	}
 
 	public void setResizedBackGround(BufferedImage resizedBackGround) {
 		this.resizedBackGround = resizedBackGround;
 	}
 
 	public class MyMarkTotalComponent extends JComponent {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		private static final int WIDTH = MARK_SIZE * 6;
 		private static final int HEIGHT = (int) (MARK_SIZE * 1.5);
 
 		public MyMarkTotalComponent(MyComponent parent) {
 			this.setSize(WIDTH, HEIGHT);
 			this.setLocation((int) parent.getWidth() - WIDTH,
 					(int) parent.getHeight() - HEIGHT);
 
 			this.setLayout(null);
 			parent.add(this);
 
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
 		 */
 		@Override
 		protected void paintComponent(Graphics arg0) {
 			super.paintComponent(arg0);
 			Graphics2D g2 = (Graphics2D) arg0.create();
 			g2.setColor(Color.red);
 			g2.setStroke(new BasicStroke(2f));
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 			g2.drawRect(0, 0, this.getWidth() + 5, this.getHeight() + 5);
 			g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN,
 					this.getHeight() - 4));
 			g2.drawString("/ " + ((MyComponent) this.getParent()).getMark(),
 					this.getWidth() / 3, this.getHeight() - 3);
 			g2.dispose();
 		}
 
 	}
 
 	public class MyComponent extends JComponent {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public MyComponent() {
 			super();
 
 		}
 
 		public RapidAssessmentComponentType createTreeNode(String name,
 				RapidAssessmentComponentType parent) throws ClassCastException {
 			throw new ClassCastException();
 		}
 
 		private double mark;
 
 		public double getMark() {
 			return mark;
 		}
 
 		public void setMark(double mark) {
 			this.mark = mark;
 			if (this.getParent() != parentPanel)
 				((MyComponent) this.getParent()).updateMark();
 		}
 
 		public void updateMark() {
 			int total = 0;
 			for (int x = 0; x < this.getComponentCount(); ++x) {
 				try {
 					total += ((MyComponent) (this.getComponent(x))).getMark();
 				} catch (ClassCastException e) {
 
 				}
 			}
 			this.setMark(total);
 		}
 
 		public boolean contains(MyComponent rect) {
 			return (this.getX() < rect.getX()
 					&& this.getY() < rect.getY()
 					&& this.getX() + this.getWidth() > rect.getX()
 							+ rect.getWidth() && this.getY() + this.getHeight() > rect
 					.getY() + rect.getHeight());
 		}
 
 		public boolean overlaps(MyComponent rect) {
 
 			Point[] myPoints = new Point[4];
 			Point[] rectPoints = new Point[4];
 
 			myPoints[0] = new Point(this.getLocation());
 			myPoints[1] = new Point(
 					(int) (myPoints[0].getX() + this.getWidth()),
 					(int) myPoints[0].getY());
 			myPoints[2] = new Point((int) (myPoints[1].getX()),
 					(int) myPoints[1].getY() + this.getHeight());
 			myPoints[3] = new Point((int) (myPoints[0].getX()),
 					(int) myPoints[2].getY());
 
 			rectPoints[0] = new Point(rect.getLocation());
 			rectPoints[1] = new Point(
 					(int) (rectPoints[0].getX() + rect.getWidth()),
 					(int) rectPoints[0].getY());
 			rectPoints[2] = new Point((int) (rectPoints[1].getX()),
 					(int) rectPoints[1].getY() + rect.getHeight());
 			rectPoints[3] = new Point((int) (rectPoints[0].getX()),
 					(int) rectPoints[2].getY());
 
 			for (int x = 0; x < 4; ++x) {
 				for (int y = x + 1; y < x + 3; y += 2) {
 					if (x % 2 == 0) {
 						if (myPoints[x].getY() < Math.max(
 								rectPoints[(y) % 4].getY(),
 								rectPoints[(y + 1) % 4].getY())
 								&& myPoints[x].getY() > Math.min(
 										rectPoints[(y) % 4].getY(),
 										rectPoints[(y + 1) % 4].getY())
 								&& rectPoints[y].getX() < Math.max(
 										myPoints[x].getX(),
 										myPoints[(x + 1) % 4].getX())
 								&& rectPoints[y].getX() > Math.min(
 										myPoints[x].getX(),
 										myPoints[(x + 1) % 4].getX())) {
 							return true;
 						}
 					} else {
 						if (myPoints[x].getX() < Math.max(
 								rectPoints[(y) % 4].getX(),
 								rectPoints[(y + 1) % 4].getX())
 								&& myPoints[x].getX() > Math.min(
 										rectPoints[(y) % 4].getX(),
 										rectPoints[(y + 1) % 4].getX())
 								&& rectPoints[(y) % 4].getY() < Math.max(
 										myPoints[x].getY(),
 										myPoints[(x + 1) % 4].getY())
 								&& rectPoints[(y) % 4].getY() > Math.min(
 										myPoints[x].getY(),
 										myPoints[(x + 1) % 4].getY())) {
 							return true;
 						}
 					}
 				}
 			}
 
 			return false;
 		}
 	}
 
 	public class MyMarkPoint extends MyComponent {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		private int editingPosition = 0;
 
 		/**
 		 * @return the editingPosition
 		 */
 		public int getEditingPosition() {
 			return editingPosition;
 		}
 
 		public void incrementEditingPosition() {
 			editingPosition++;
 		}
 
 		public void decrementEditingPosition() {
 			editingPosition--;
 			if (editingPosition < 0)
 				editingPosition = 0;
 		}
 
 		@Override
 		public RapidAssessmentComponentType createTreeNode(String name,
 				RapidAssessmentComponentType parent) throws ClassCastException {
 			RapidAssessmentMarkType tmp = new RapidAssessmentMarkType(name,
 					this.getX(), this.getY(), this.getWidth(),
 					this.getHeight(), this.getMark(), parent);
 			tmp.setMaxValue(this.getMark());
 			tmp.populateTreeWithEntities();
 			return tmp;
 		}
 
 		/**
 		 * @param editingPosition
 		 *            the editingPosition to set
 		 */
 		public void setEditingPosition(int editingPosition) {
 			this.editingPosition = editingPosition;
 		}
 
 		public MyMarkPoint(int x, int y, MyComponent parent) {
 			super();
 			this.setLocation(x, y);
 			this.setSize(MARK_SIZE * 3, MARK_SIZE);
 			parent.add(this);
 			this.setMark(1);
 
 			this.addMouseListener(new MouseListener() {
 
 				@Override
 				public void mouseReleased(MouseEvent arg0) {
 
 				}
 
 				@Override
 				public void mousePressed(MouseEvent arg0) {
 					if (lastcreated != null)
 						lastcreated.setEditingPosition(0);
 					lastcreated = (MyMarkPoint) arg0.getSource();
 					if (arg0.getButton() == MouseEvent.BUTTON3) {
 						lastcreated.setMark(0);
 						lastcreated.getParent().remove(lastcreated);
 						lastcreated = null;
 					}
 					parentPanel.repaint();
 				}
 
 				@Override
 				public void mouseExited(MouseEvent arg0) {
 
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent arg0) {
 
 				}
 
 				@Override
 				public void mouseClicked(MouseEvent arg0) {
 
 				}
 			});
 		}
 
 		@Override
 		protected void paintComponent(Graphics arg0) {
 
 			super.paintComponent(arg0);
 
 			Graphics2D g2 = (Graphics2D) arg0.create();
 			g2.setColor(Color.red);
 			if (lastcreated == this) {
 				g2.setStroke(new BasicStroke(4f));
 			} else {
 				g2.setStroke(new BasicStroke(2f));
 			}
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 			g2.drawRect(0, 0, this.getWidth() / 3 - 1, this.getHeight() - 1);
 			g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, MARK_SIZE - 2));
 			String text1 = "" + this.getMark();
 			String text2 = "";
 			if (this.getEditingPosition() > 0) {
 				text2 = text1.substring(this.getEditingPosition());
 				text1 = text1.substring(0, this.getEditingPosition()) + '|';
 			}
 			g2.drawString(text1 + text2, this.getWidth() / 3 + 2,
 					this.getHeight() - 1);
 			g2.dispose();
 
 		}
 
 	}
 
 	public class MyRectangle extends MyComponent {
 
 		private static final long serialVersionUID = 1L;
 
 		private String name;
 
 		private int count = 1;
 		private boolean pressing = false;
 		private Point origin = null;
 		private Point end = null;
 		private long timePressed = 0;
 		private boolean hover;
 
 		/**
 		 * @return the hover
 		 */
 		public boolean isHover() {
 			return hover;
 		}
 
 		/**
 		 * @param hover
 		 *            the hover to set
 		 */
 		public void setHover(boolean hover) {
 			this.hover = hover;
 		}
 
 		/**
 		 * @return the timePressed
 		 */
 		public long getTimePressed() {
 			return timePressed;
 		}
 
 		/**
 		 * @param timePressed
 		 *            the timePressed to set
 		 */
 		public void setTimePressed(long timePressed) {
 			this.timePressed = timePressed;
 		}
 
 		public boolean isPressing() {
 			return pressing;
 		}
 
 		@Override
 		public RapidAssessmentComponentType createTreeNode(String name,
 				RapidAssessmentComponentType parent) throws ClassCastException {
 
 			RapidAssessmentRectangleType tmp = new RapidAssessmentRectangleType(
 					name, this.getX(), this.getY(), this.getWidth(),
 					this.getHeight(), parent);
 			tmp.setMaxValue(this.getMark());
 			tmp.populateTreeWithEntities();
 			int count = 0;
 
 			for (int x = 0; x < this.getComponentCount(); ++x) {
 				try {
 					String newName;
 					if (name.compareTo("Assessment") == 0)
 						newName = "Q" + ++count;
 					else
 						newName = name + "." + ++count;
 					((MyComponent) this.getComponent(x)).createTreeNode(
 							newName, tmp);
 				} catch (ClassCastException e) {
 					--count;
 				}
 			}
 			return tmp;
 		}
 
 		public MyRectangle(int x, int y, int w, int h) {
 			super();
 			this.setLocation(x, y);
 			this.setSize(w, h);
 			this.addMouseListener(new canvasMouseListener());
 			new MyMarkTotalComponent(this);
 			this.addMouseMotionListener(new CanvasMouseMoveListener());
 
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
 		 */
 		@Override
 		protected void paintComponent(Graphics arg0) {
 
 			super.paintComponent(arg0);
 			Graphics2D g2 = (Graphics2D) arg0.create();
 			g2.setColor(Color.red);
 			g2.setStroke(new BasicStroke(3f));
 			if (hover)
 				g2.setColor(g2.getColor().darker().darker());
 
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 			g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1,
 					20, 20);
 			if (this.isPressing()) {
 				g2.setStroke(new BasicStroke(3f));
 				g2.setColor(new Color(0.3f, 0.3f, 1.0f));
 				g2.drawRect((int) (this.getOrigin().getX()), (int) (this
 						.getOrigin().getY()),
 						(int) (this.getEnd().getX() - this.getOrigin().getX()),
 						(int) (this.getEnd().getY() - this.getOrigin().getY()));
 			}
 
 			g2.dispose();
 
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public int getAndIncrementCount() {
 			return count++;
 		}
 
 		public int getCount() {
 			return count;
 		}
 
 		public void setPressing(boolean pressing) {
 			this.pressing = pressing;
 		}
 
 		public boolean getPressing() {
 			return this.pressing;
 		}
 
 		public Point getOrigin() {
 			return origin;
 		}
 
 		public void setOrigin(Point origin) {
 			this.origin = origin;
 		}
 
 		public Point getEnd() {
 			return end;
 		}
 
 		public void setEnd(Point end) {
 			this.end = end;
 		}
 
 	}
 
 	public class canvasMouseListener implements MouseListener {
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			((MyRectangle) e.getSource()).setHover(true);
 			((MyRectangle) e.getSource()).repaint();
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			((MyRectangle) e.getSource()).setHover(false);
 			((MyRectangle) e.getSource()).repaint();
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 
 			((MyRectangle) e.getSource()).setOrigin(new Point(e.getX(), e
 					.getY()));
 			((MyRectangle) e.getSource()).setEnd(new Point(e.getX(), e.getY()));
 			((MyRectangle) e.getSource()).setPressing(true);
 			((MyRectangle) e.getSource()).setTimePressed(e.getWhen());
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 
 			if (e.getButton() == MouseEvent.BUTTON3) {
 
 				if (e.getWhen()
 						- ((MyRectangle) e.getSource()).getTimePressed() < 150
 						&& e.getSource() != parentRect) {
 					MyComponent parent = ((MyComponent) ((MyRectangle) e
 							.getSource()).getParent());
 					parent.remove(((MyRectangle) e.getSource()));
 					parent.updateMark();
 					parentPanel.repaint();
 				}
 
 				return;
 			}
 
 			if (((MyRectangle) e.getSource()).getPressing())
 				createComponent(((MyRectangle) e.getSource()).getOrigin(),
 						((MyRectangle) e.getSource()).getEnd(),
 						(MyRectangle) e.getSource());
 
 			((MyRectangle) e.getSource()).setPressing(false);
 		}
 
 		private void createComponent(Point p1, Point p2, MyRectangle source) {
 			if (p1.getX() > p2.getX() || p1.getY() > p2.getY())
 				return;
 
 			if (p2.getX() - p1.getX() < MARK_SIZE * 2
 					|| p2.getY() - p1.getY() < MARK_SIZE * 2) {
 				MyMarkPoint tmp = new MyMarkPoint((int) p1.getX(),
 						(int) p1.getY(), source);
 				source.add(tmp);
 				if (lastcreated != null)
 					lastcreated.setEditingPosition(0);
 				lastcreated = tmp;
 
 			} else {
 				MyComponent tmp = new MyRectangle((int) p1.getX(),
 						(int) p1.getY(), (int) (p2.getX() - p1.getX()),
 						(int) (p2.getY() - p1.getY()));
 				Component[] siblings = source.getComponents();
 				LinkedList<Component> contained = new LinkedList<Component>();
 				boolean collision = false;
 				for (int x = 0; x < siblings.length; ++x) {
 					try {
 						MyComponent component = (MyComponent) (siblings[x]);
 						if (tmp.overlaps(component)) {
 
 							collision = true;
 
 						} else {
 							if (tmp.contains(component)) {
 								contained.add(component);
 							}
 						}
 					} catch (ClassCastException e) {
 
 					}
 				}
 				if (!collision) {
 					tmp.setName(source.getName() + "."
 							+ source.getAndIncrementCount());
 					source.add(tmp);
 					for (int x = 0; x < contained.size(); ++x) {
 
 						Point childLoc = contained.get(x).getLocationOnScreen();
 						Point parentLoc = tmp.getLocationOnScreen();
 
 						contained.get(x).setLocation(
 								(int) (childLoc.getX() - parentLoc.getX()),
 								(int) (childLoc.getY() - parentLoc.getY()));
 						source.remove(contained.get(x));
 						tmp.add(contained.get(x));
 					}
 				}
 
 			}
 			parentPanel.repaint();
 		}
 	}
 
 	public class CanvasMouseMoveListener implements MouseMotionListener {
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 
 			((MyRectangle) e.getSource()).setEnd(new Point(e.getX(), e.getY()));
 			((MyRectangle) e.getSource()).repaint();
 
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent e) {
 
 		}
 	}
 
 	public RapidAssessmentCanvas(Project project, EntityType assessedEntity) {
 		Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
 		this.setLayout(null);
 		this.project = project;
 		ContainerPanel canvas = new ContainerPanel();
 		parentPanel = canvas;
 		this.setContentPane(canvas);
 		this.setSize(screen.width, screen.height);
 		this.assessedEntity = assessedEntity;
 		if (assessedEntity != null)
 			this.setTitle("Create Assessment - " + assessedEntity.getName());
 		else
 			this.setTitle("Create Assessment - no entity selected");
 
 		parentRect = new MyRectangle(0, 0, (int) screen.getWidth() - 100,
 				(int) screen.getHeight() - 50);
 		parentRect.setVisible(true);
 		this.setLayout(null);
 		canvas.add(parentRect);
 
 		JButton btnNewButton = new JButton("Image");
 		btnNewButton.setBounds(parentRect.getWidth() + 5, 115, 89, 23);
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser fc = new JFileChooser();
 				FileNameExtensionFilter ff = new FileNameExtensionFilter(
 						"Image Files", "jpeg", "png");
 				int returnVal = fc.showOpenDialog(parentPanel);
 				File file;
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					file = fc.getSelectedFile();
 					setBackgroundFileName(file.getAbsolutePath());
 					try {
 						setBackGround(ImageIO.read(file));
 						setResizedBackGround(null);
 					} catch (IOException e) {
 
 					}
 				}
 
 			}
 		});
 		setLayout(null);
 		btnNewButton.setFocusable(false);
 		parentPanel.add(btnNewButton);
 
 		JButton btnSave = new JButton("Save");
 		btnSave.setBounds(parentRect.getWidth() + 5, 80, 89, 23);
 		parentPanel.add(btnSave);
 		btnSave.setFocusable(false);
 		btnSave.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				parentPanel.save();
 				refreshLoad();
 
 			}
 		});
 
		loadCombo = new JComboBox<RapidAssessmentContainerType>();
 		loadCombo.setBounds(parentRect.getWidth() + 5, 10, 89, 23);
 
 		parentPanel.add(loadCombo);
 		JButton btnLoad = new JButton("Load");
 		btnLoad.setBounds(parentRect.getWidth() + 5, 35, 89, 23);
 		parentPanel.add(btnLoad);
 		btnLoad.setFocusable(false);
 		btnLoad.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (loadCombo.getSelectedItem() != null)
 					try {
 						RapidAssessmentCanvas.this.assessedEntity = (EntityType) loadCombo
 								.getSelectedItem();
 					} catch (ClassCastException e) {
 
 					}
 			}
 		});
 
 		this.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 
 			}
 
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				if (lastcreated == null)
 					return;
 				MyMarkPoint source = lastcreated;
 				switch (arg0.getKeyCode()) {
 				case '.':
 					if (arg0.getKeyChar() == '.') {
 						String text1 = "" + source.getMark();
 						String text2 = "";
 						int dec = text1.indexOf(".");
 						text2 = text1.substring(dec + 1);
 						text1 = text1.substring(0, dec);
 						String text = text1 + text2;
 						if (dec < source.getEditingPosition())
 							source.decrementEditingPosition();
 						text2 = text.substring(source.getEditingPosition());
 						text1 = text.substring(0, source.getEditingPosition()) + '.';
 						try {
 							source.setMark(Double.parseDouble(text1 + text2));
 							source.incrementEditingPosition();
 						} catch (NumberFormatException e) {
 
 						}
 					}
 
 					break;
 
 				case 8:
 
 					if (source.getEditingPosition() > 0) {
 						String mark = String.valueOf(source.getMark());
 						if (mark.charAt(source.getEditingPosition() - 1) == '.') {
 							source.decrementEditingPosition();
 							break;
 						}
 						String secondPart = "";
 						try {
 							secondPart = mark.substring(source
 									.getEditingPosition());
 
 						} catch (StringIndexOutOfBoundsException e) {
 
 						}
 						mark = mark.substring(0,
 								source.getEditingPosition() - 1) + secondPart;
 						try {
 							source.setMark(Double.parseDouble(mark));
 							source.decrementEditingPosition();
 						} catch (NumberFormatException e) {
 
 						}
 					}
 					break;
 
 				case 37:
 					source.decrementEditingPosition();
 
 					break;
 				case 39:
 					source.incrementEditingPosition();
 
 					break;
 				default:
 					if (source.getEditingPosition() > 0) {
 						String mark = String.valueOf(source.getMark());
 						String secondPart = "";
 						try {
 							secondPart = mark.substring(source
 									.getEditingPosition());
 
 						} catch (StringIndexOutOfBoundsException e) {
 
 						}
 						mark = mark.substring(0, source.getEditingPosition())
 								+ arg0.getKeyChar() + secondPart;
 						try {
 							source.setMark(Double.parseDouble(mark));
 							source.incrementEditingPosition();
 						} catch (NumberFormatException e) {
 
 						}
 					} else {
 						try {
 							source.setMark(Double.parseDouble(""
 									+ arg0.getKeyChar()));
 							source.incrementEditingPosition();
 						} catch (NumberFormatException e) {
 
 						}
 					}
 					break;
 				}
 
 				parentPanel.repaint();
 			}
 		});
 
 	}
 
 	public class ContainerPanel extends JPanel {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
 		 */
 		@Override
 		protected void paintComponent(Graphics g) {
 
 			super.paintComponent(g);
 			Graphics g2 = g.create();
 			if (resizedBackGround == null && backGround != null) {
 				resizedBackGround = Scalr.resize(backGround, Method.QUALITY,
 						Mode.FIT_EXACT, parentRect.getWidth(),
 						parentRect.getHeight(), Scalr.OP_ANTIALIAS);
 			}
 
 			g2.drawImage(resizedBackGround, 0, 0, null);
 			g2.dispose();
 		}
 
 		/*
 		 * public RapidAssessmentTree createTree() { //RapidAssessmentTree tree
 		 * = new RapidAssessmentTree(null);
 		 * //tree.setHead(this.createRapidAssessmentTreeNode(tree)); //return
 		 * tree; return null; }
 		 * 
 		 * public TreeContainerNode createRapidAssessmentTreeNode(
 		 * RapidAssessmentTree tree) { TreeContainerNode tmp = tree.new
 		 * TreeContainerNode(this.getX(), this.getY(), this.getWidth(),
 		 * this.getHeight(), backgroundFileName); for (int x = 0; x <
 		 * this.getComponentCount(); ++x) { try { tmp.getChildNodes().add(
 		 * ((MyComponent) this.getComponent(x)) .createTreeNode(tree)); } catch
 		 * (ClassCastException e) {
 		 * 
 		 * } } return tmp; }
 		 */
 
 		public void save() {
 			if (assessedEntity == null)
 				return;
 			RapidAssessmentContainerType parent = new RapidAssessmentContainerType(
 					assessedEntity, this.getX(), this.getY(), this.getWidth(),
 					this.getHeight());
 
 			assessedEntity = parent;
 			parent.setImage(backGround);
 
 			LinkedList<MyComponent> list = new LinkedList<RapidAssessmentCanvas.MyComponent>();
 			for (int x = 0; x < this.getComponentCount(); ++x) {
 				try {
 					list.add((MyComponent) (this.getComponent(x)));
 				} catch (ClassCastException e) {
 
 				}
 			}
 
 			for (int x = 0; x < list.size() - 1; ++x) {
 				for (int y = x; y < list.size(); ++y) {
 					if (list.get(x).getX() + list.get(x).getY() * 3 > list.get(
 							y).getX()
 							+ list.get(y).getY() * 3) {
 						MyComponent tmp = list.get(x);
 						list.set(x, list.get(y));
 						list.set(y, tmp);
 					}
 				}
 			}
 
 			for (int x = 0; x < list.size(); ++x) {
 
 				list.get(x).createTreeNode("Assessment", parent);
 			}
 
 			JFrame frame = new RapidAssessmentMarkingCanvas(parent);
 
 			frame.setVisible(true);
 
 			/*
 			 * LinkedList<LinkedList<SuperEntity>> data = parent
 			 * .getParentEntitytype().getEntityList().get(0)
 			 * .getDataLinkedList(); System.out.println(); for (int x = 0; x <
 			 * data.size(); ++x) { for (int y = 0; y < data.get(x).size(); ++y)
 			 * { String g = ""; SuperEntity s = data.get(x).get(y); while (s !=
 			 * null) { g += " "; s = s.getParentEntity(); } System.out.println(g
 			 * + data.get(x).get(y).getType().getName()); } }
 			 */
 		}
 
 	}
 
 	public void load(RapidAssessmentContainerType container) {
 		this.removeAll();
 		this.parentPanel = null;
 		this.lastcreated = null;
 		this.resizedBackGround = null;
 		this.parentRect = null;
 
 		parentPanel = new ContainerPanel();
 		parentPanel.setBounds((int) container.getX(), (int) container.getY(),
 				(int) container.getW(), (int) container.getH());
 		this.backGround = container.getImage();
 		this.setContentPane(parentPanel);
 
 		for (int x = 0; x < container.getSubEntityType().size(); ++x) {
 			try {
 				createCanvasComponent(
 						(RapidAssessmentComponentType) (container
 								.getSubEntityType().get(x)),
 						parentPanel);
 			} catch (ClassCastException e) {
 
 			}
 		}
 
 	}
 
 	private void createCanvasComponent(RapidAssessmentComponentType component,
 			ContainerPanel parentPanel2) {
 		this.parentRect = new MyRectangle((int) component.getX(),
 				(int) component.getY(), (int) component.getW(),
 				(int) component.getH());
 		parentPanel2.add(parentRect);
 
 		for (int x = 0; x < component.getSubEntityType().size(); ++x) {
 			try {
 				createCanvasComponent(
 						(RapidAssessmentComponentType) (component
 								.getSubEntityType().get(x)),
 						parentRect);
 			} catch (ClassCastException e) {
 
 			}
 		}
 	}
 
 	public void createCanvasComponent(RapidAssessmentComponentType component,
 			MyComponent parent) {
 
 		try {
 			RapidAssessmentRectangleType comp = ((RapidAssessmentRectangleType) component);
 			MyRectangle rect = new MyRectangle((int) comp.getX(),
 					(int) comp.getY(), (int) comp.getW(), (int) comp.getH());
 			parent.add(rect);
 			for (int x = 0; x < component.getSubEntityType().size(); ++x) {
 				try {
 					createCanvasComponent(
 							(RapidAssessmentComponentType) (component
 									.getSubEntityType().get(x)),
 							rect);
 				} catch (ClassCastException e) {
 
 				}
 			}
 		} catch (ClassCastException e) {
 
 		}
 		try {
 			RapidAssessmentMarkType comp = ((RapidAssessmentMarkType) component);
 			MyMarkPoint mark = new MyMarkPoint((int) comp.getX(),
 					(int) comp.getY(), parent);
 			mark.setMark(comp.getMaxValue());
 		} catch (ClassCastException e) {
 
 		}
 	}
 
 	public void refreshLoad() {
 		LinkedList<RapidAssessmentContainerType> containers = new LinkedList<RapidAssessmentContainerType>();
 		this.project.getHeadEntityType().findRapidAssessment(containers);
 		loadCombo.removeAllItems();
 		for (int x = 0; x < containers.size(); ++x)
 			loadCombo.addItem(containers.get(x));
 	}
 
 }

 package element_actions;
 
 import gui.GElement;
 import gui.LayoutGUI;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.LineBorder;
 
 import parser.Direction;
 
 public class Resize extends JFrame {
 
 	/**
 	 * Preview button.
 	 */
 	private JButton previewBttn;
 
 	/**
 	 * Save button.
 	 */
 	private JButton saveBttn;
 
 	/**
 	 * Dimensiuni.
 	 */
 	private JTextField textHeight;
 	private JTextField textY;
 	private JTextField textX;
 	private JTextField textWidth;
 	private JLabel labelHeight;
 	private JLabel labelY;
 	private JLabel labelX;
 	private JLabel labelWidth;
 
 	/**
 	 * Fereastra curenta.
 	 */
 	private JFrame frame = this;
 
 	private JPanel previewPanel;
 
 	/**
 	 * Constructor.
 	 */
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public Resize(GElement pan, LayoutGUI gui) {
 
 		// New Panel.
 		JPanel panel = new JPanel(new GridLayout(4, 2));
 
 		// Creaza content panel.
 		Container contentPanel = getContentPane();
 
 		previewPanel = new JPanel();
 		previewPanel.setBorder(new LineBorder(Color.MAGENTA));
 		// Make it transparent.
 		previewPanel.setOpaque(false);
 
 		ActionListener saveListener = new SaveListenter(pan, gui);
 		ActionListener previewListener = new PreviewListenter(pan, gui);
 		// dimensiuni
 
 		// X
 		labelX = new JLabel();
 		textX = new JTextField();
 		labelX.setText("X");
 		textX.setText(Integer.toString(pan.getX()));
 		panel.add(labelX);
 		panel.add(textX);
 
 		// Y
 		labelY = new JLabel();
 		textY = new JTextField();
 		labelY.setText("Y");
 		textY.setText(Integer.toString(pan.getY()));
 		panel.add(labelY);
 		panel.add(textY);
 
 		// Width
 		labelWidth = new JLabel();
 		textWidth = new JTextField();
 		labelWidth.setText("Width");
 		textWidth.setText(Integer.toString(pan.getWidth()));
 		panel.add(labelWidth);
 		panel.add(textWidth);
 
 		// Height
 		labelHeight = new JLabel();
 		textHeight = new JTextField();
 		labelHeight.setText("Height");
 		textHeight.setText(Integer.toString(pan.getHeight()));
 		panel.add(labelHeight);
 		panel.add(textHeight);
 
 		contentPanel.add(panel, BorderLayout.CENTER);
 
 		// Adauga buton de preview.
 		panel = new JPanel(new GridLayout(1, 2));
 		previewBttn = new JButton("Preview");
 		previewBttn.addActionListener(previewListener);
 		panel.add(previewBttn);
 
 		// Adauga buton de save.
 		saveBttn = new JButton("Save");
 		saveBttn.addActionListener(saveListener);
 		panel.add(saveBttn);
 		contentPanel.add(panel, BorderLayout.SOUTH);
 
 		this.initFrame();
 	}
 
 	private void initFrame() {
 
 		// Dispose frame for page and OCR analyzers.
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 		this.setSize(300, 220);
 		this.setVisible(true);
 
 		setLocationRelativeTo(null);
 	}
 
 	/**
 	 * Listener pentru butonul de preview.
 	 * 
 	 * @author Unknown-Revengers
 	 */
 	class PreviewListenter implements ActionListener {
 		GElement panel;
 		LayoutGUI gui;
 
 		PreviewListenter(GElement panel, LayoutGUI gui) {
 			this.panel = panel;
 			this.gui = gui;
 
 		}
 
 		@Override
 		public void actionPerformed(final ActionEvent e) {
 			int height = Integer.parseInt(textHeight.getText());
 			int y = Integer.parseInt(textY.getText());
 			int x = Integer.parseInt(textX.getText());
 			int width = Integer.parseInt(textWidth.getText());
 
 			previewPanel.setBounds(x, y, width, height);
 
 			this.gui.getDraw().add(previewPanel);
 		}
 	}
 
 	/**
 	 * Listener pentru butonul de save.
 	 * 
 	 * @author Unknown-Revengers
 	 */
 	class SaveListenter implements ActionListener {
 		GElement panel;
 		LayoutGUI gui;
 
 		SaveListenter(GElement panel, LayoutGUI gui) {
 			this.panel = panel;
 			this.gui = gui;
 		}
 
 		@Override
 		public void actionPerformed(final ActionEvent e) {
 			int height = Integer.parseInt(textHeight.getText());
 			int y = Integer.parseInt(textY.getText());
 			int x = Integer.parseInt(textX.getText());
 			int width = Integer.parseInt(textWidth.getText());
 
 			this.panel.element.getData().left = x;
 			this.panel.element.getData().right = x + width;
 			this.panel.element.getData().top = y;
 			this.panel.element.getData().bottom = y + height;
 
 			if (this.gui.layoutParser.direction == Direction.ASCENDING) {
 				this.panel.element.getData().top = this.gui.image.getHeight()
						- (y + height);
 				this.panel.element.getData().bottom = this.gui.image
						.getHeight() - y;
 			}
 
 			this.panel.setBounds(x, y, width, height);
 			this.gui.draw.remove(previewPanel);
 			this.gui.draw.add(panel);
 
 			frame.dispose();
 		}
 	}
 
 }

 /**
  * PAINt 
  * 
  * Created for the course intro Human-Computer Interaction at the
  * Radboud Universiteit Nijmegen
  * 
  * 2013
  */
 package nl.PAINt;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.colorchooser.AbstractColorChooserPanel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  * @author Luuk Scholten & Thom Wiggers
  * 
  */
public class OptiesPanel extends JPanel implements ChangeListener {
 
 	/**
      * 
      */
 	private static final long serialVersionUID = -8647274997122730322L;
 	private CanvasPanel canvas;
 	private JColorChooser colorPicker;
 
 	/**
 	 * @param canvas
 	 * 
 	 */
 	public OptiesPanel(CanvasPanel theCanvas) {
 		canvas = theCanvas;
 
 		setBackground(Color.black);
 		setPreferredSize(new Dimension(450, canvas.getHeight()));
 
 		this.setLayout(new GridLayout(3, 1));
 
 		colorPicker = new JColorChooser();
		// colorPicker.getSelectionModel().addChangeListener(this);
 		PreviewPanel pp = new PreviewPanel();
 		colorPicker.getSelectionModel().addChangeListener(pp);
 		colorPicker.setPreviewPanel(pp);
 		boolean skipped = false;
 		for (AbstractColorChooserPanel panel : colorPicker.getChooserPanels()) {
 			if (!skipped) {
 				skipped = true;
 				continue;
 			} else
 				colorPicker.removeChooserPanel(panel);
 		}
 		this.add(colorPicker);
 	}
 
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	@Override
	public void stateChanged(ChangeEvent arg0) {
		canvas.setColor(colorPicker.getColor());

	}

	public class PreviewPanel extends JPanel implements ChangeListener,
 			ActionListener {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 3445719326561602037L;
 		private JLabel label;
 		private JPanel block;
 
 		public PreviewPanel() {
 			super();
 
 			this.setLayout(new GridLayout(0, 2));
 
 			setPreferredSize(new Dimension(400, 100));
 			setSize(getPreferredSize());
 			label = new JLabel("Huidige Kleur:");
 			label.setBackground(Color.blue);
 			label.setSize(getPreferredSize());
 			label.setVisible(true);
 			this.block = new JPanel();
 			block.setSize(80, 80);
 			block.setBackground(colorPicker.getColor());
 
 			JButton knopjeFill = new JButton("Gebruik als Fill");
 			knopjeFill.setActionCommand("FILL");
 			knopjeFill.addActionListener(this);
 			JButton knopjeLijn = new JButton("Gebruik als Lijn");
 			knopjeLijn.setActionCommand("LIJN");
 			knopjeLijn.addActionListener(this);
 
 			add(label);
 			add(block);
 			add(knopjeFill);
 			add(knopjeLijn);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
 		 * )
 		 */
 		@Override
 		public void stateChanged(ChangeEvent e) {
 			block.setBackground(colorPicker.getColor());
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		 */
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			switch (arg0.getActionCommand()) {
 			case "FILL":
 				canvas.setColor(colorPicker.getColor());
 				break;
 			case "LIJN":
 				canvas.setLineColor(colorPicker.getColor());
 				break;
 			default:
 				throw new IllegalArgumentException("WAT");
 			}
 		}
 
 	}
 }

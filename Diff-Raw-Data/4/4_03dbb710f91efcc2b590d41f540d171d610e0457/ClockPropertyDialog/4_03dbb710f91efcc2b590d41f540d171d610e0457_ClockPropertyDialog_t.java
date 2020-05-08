 package sec02.ex04;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 
 public class ClockPropertyDialog extends Frame implements ActionListener{
 	private static final long serialVersionUID = 1L;
 	private static final int NORMAL_LABEL_FONT_SIZE = 15;
 	private GridBagLayout gbl = new GridBagLayout();
 	private ClockPropertyData data;
 	
 	public static String buFont;
 	public static int buFontSize;
 	public static String buColor;
 	public static String buBackgroundColor;
 	
 	private Choice fontChoice;
 	private Choice sizeChoice;
 	private JComboBox colorComboBox;
 	private JComboBox backgroundcolorComboBox;
 	private DefaultComboBoxModel colorComboBoxModel;
 	private DefaultComboBoxModel backgrounColorComboBoxModel;
 	private MyCellRenderer colorRenderer = new MyCellRenderer();
 	private MyCellRenderer backgroundColorRenderer = new MyCellRenderer();
 	
 	private static Label preview;
 
 	public ClockPropertyDialog(final ClockPropertyData data) {
 		setTitle("Property dialog");
         setSize(500, 250);
         setLocationRelativeTo(null);
         setResizable(false);
         setLayout(gbl);
         this.data = data;
         
         data.load();
         
         // ?�?�?�x?�?�?�̔z?�u
         Label fontLabel = new Label("Font");
         Label sizeLabel = new Label("Font size");
         Label colorLabel = new Label("Color");
         Label backgroundColorLabel = new Label("Background color");
         
         fontLabel.setFont(new Font("Arial", Font.PLAIN, NORMAL_LABEL_FONT_SIZE));
         sizeLabel.setFont(new Font("Arial", Font.PLAIN, NORMAL_LABEL_FONT_SIZE));
         colorLabel.setFont(new Font("Arial", Font.PLAIN, NORMAL_LABEL_FONT_SIZE));
         backgroundColorLabel.setFont(new Font("Arial", Font.PLAIN, NORMAL_LABEL_FONT_SIZE));
         
         addLabel(fontLabel, 0, 0, 1, 1);
         addLabel(sizeLabel, 0, 1, 1, 1);
         addLabel(colorLabel, 0, 2, 1, 1);
         addLabel(backgroundColorLabel, 0, 3, 1, 1);
         
         // ?�?�?�j?�?�?�[?�{?�b?�N?�X?�̔z?�u
         fontChoice = new Choice();
         sizeChoice = new Choice();
         
         setFontChoice(fontChoice);
         setSizeChoice(sizeChoice);
         createColorComboBox();
         createBackgroundColorComboBox();
         
         addChoice(fontChoice, 1, 0, 3, 1);
         addChoice(sizeChoice, 1, 1, 1, 1);
         addComboBox(colorComboBox, 1, 2, 1, 1);
         addComboBox(backgroundcolorComboBox, 1, 3, 1, 1);
         
         // ?�{?�^?�?�?�̔z?�u
         Button okButton = new Button("OK");
         Button cancelButton = new Button("Cancel");
         
         okButton.addActionListener(this);
         cancelButton.addActionListener(this);
         
         addButton(okButton, 3, 4, 1, 1);
         addButton(cancelButton, 4, 4, 1, 1);
         
         // ?�?�?�X?�i?�[?�̒ǉ�
         fontChoice.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				Choice cho = (Choice)e.getItemSelectable();
 				ClockPropertyDialog.buFont = data.fonts[cho.getSelectedIndex()];
 				Font f = new Font(buFont, Font.PLAIN, 35);
//				ClockPropertyDialog.preview.setFont(f);
//				ClockPropertyDialog.preview.setText("Preview");
 			}
         });
         sizeChoice.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				Choice cho = (Choice)e.getItemSelectable();
 				ClockPropertyDialog.buFontSize = data.sizes[cho.getSelectedIndex()];
 			}
         });
         colorComboBox.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				ClockPropertyDialog.buColor = data.strColors[colorComboBox.getSelectedIndex()];
 			}
         });
         backgroundcolorComboBox.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				ClockPropertyDialog.buBackgroundColor = data.strColors[backgroundcolorComboBox.getSelectedIndex()];
 			}
         });
         
 //        preview = new Label("Preview", Label.CENTER);
 //        Font f = new Font(buFont, Font.PLAIN, 35);
 //        preview.setFont(f);
 //        preview.setBackground(Color.LIGHT_GRAY);
 //        preview.setForeground(Color.DARK_GRAY);
 //        addPreviewLabel(preview, 2, 2, 2, 2);
 //        
 //        Label memo = new Label("Font", Label.CENTER);
 //        Font memoFont = new Font("Serif", Font.BOLD, 20);
 //        memo.setFont(memoFont);
 //        addLabel(memo, 2, 1, 1, 1);
         
         //?�~?�?�?�?�?�?�?�ꂽ?�Ƃ�?�̏�?�?�
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e)  {
                 e.getWindow().setVisible(false);
             }
        });
 	}
 	private void createColorComboBox() {
 		colorComboBoxModel = new DefaultComboBoxModel();
 		for (String color : data.strColors) {
 			colorComboBoxModel.addElement(new ComboLabel(color, new ImageIcon("GUI/sec02/ex04/resource/" + color + ".png")));
 		}
         colorComboBox = new JComboBox(colorComboBoxModel);
         colorComboBox.setRenderer(colorRenderer);
 	}
 	
 	private void createBackgroundColorComboBox() {
 		backgrounColorComboBoxModel = new DefaultComboBoxModel();
 		for (String color : data.strColors) {
 			backgrounColorComboBoxModel.addElement(new ComboLabel(color, new ImageIcon("GUI/sec02/ex04/resource/" + color + ".png")));
 		}
         backgroundcolorComboBox = new JComboBox(backgrounColorComboBoxModel);
         backgroundcolorComboBox.setRenderer(backgroundColorRenderer);
 	}
 	
 	public void load() {
 		this.setVisible(true);
 		reloadPropertyData();
 	}
 	
 	private void setFontChoice(Choice ch) {
 		String[] fonts = data.fonts;
 		for(int i = 0; i < fonts.length; i++){
 			ch.addItem(fonts[i]);
 		}
 	}
 	
 	private void setSizeChoice(Choice ch) {
 		int[] sizes = data.sizes;
 		for(int i = 0; i < sizes.length; i++){
 			ch.addItem(Integer.toString(sizes[i]));
 		}
 	}
 	
 	private void addLabel(Label label, int x, int y, int w, int h) {
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill = GridBagConstraints.NONE;
         gbc.weightx = 100.0;
         gbc.weighty = 100.0;
         gbc.gridx = x;
         gbc.gridy = y;
         gbc.gridwidth = w;
         gbc.gridheight = h;
         gbc.anchor = GridBagConstraints.EAST;
         gbc.insets = new Insets(5, 5, 5, 5);
         gbl.setConstraints(label, gbc);
         add(label);
     }
 	
 	private void addPreviewLabel(Label label, int x, int y, int w, int h) {
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill = GridBagConstraints.NONE;
         gbc.weightx = 100.0;
         gbc.weighty = 100.0;
         gbc.gridx = x;
         gbc.gridy = y;
         gbc.gridwidth = GridBagConstraints.REMAINDER;
         gbc.gridheight = h;
         gbc.anchor = GridBagConstraints.EAST;
         gbc.fill = GridBagConstraints.BOTH;
         gbc.insets = new Insets(5, 5, 5, 5);
         gbl.setConstraints(label, gbc);
         add(label);
     }
 	
 	private void addChoice(Choice choice, int x, int y, int w, int h) {
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill = GridBagConstraints.NONE;
         gbc.weightx = 100.0;
         gbc.weighty = 100.0;
         gbc.gridx = x;
         gbc.gridy = y;
         gbc.gridwidth = w;
         gbc.gridheight = h;
         gbc.anchor = GridBagConstraints.WEST;
         gbc.insets = new Insets(5, 5, 5, 5);
         gbl.setConstraints(choice, gbc);
         add(choice);
     }
 	
 	private void addComboBox(JComboBox box, int x, int y, int w, int h) {
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill = GridBagConstraints.NONE;
         gbc.weightx = 100.0;
         gbc.weighty = 100.0;
         gbc.gridx = x;
         gbc.gridy = y;
         gbc.gridwidth = w;
         gbc.gridheight = h;
         gbc.anchor = GridBagConstraints.WEST;
         gbc.insets = new Insets(5, 5, 5, 5);
         gbl.setConstraints(box, gbc);
         add(box);
     }
 	
 	private void addButton(Button button, int x, int y, int w, int h) {
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill = GridBagConstraints.NONE;
         gbc.weightx = 100.0;
         gbc.weighty = 100.0;
         gbc.gridx = x;
         gbc.gridy = y;
         gbc.gridwidth = w;
         gbc.gridheight = h;
         gbc.anchor = GridBagConstraints.SOUTHWEST;
         gbc.insets = new Insets(5, 5, 5, 5);
         gbl.setConstraints(button, gbc);
         add(button);
     }
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand() == "OK") {
 			setPropertyData();
 			repaint();
 			setVisible(false);
 		} else if (e.getActionCommand() == "Cancel") {
 			setVisible(false);
 		}
 	}
 	
 	private void setPropertyData() {		
 		data.setData(buFont, buFontSize, buColor, buBackgroundColor);
 	}
 	
 	private void reloadPropertyData() {
 		buFont = data.font;
 		buFontSize = data.fontSize;
 		buColor = data.color.toString();
 		buBackgroundColor = data.backgroundColor.toString();
 		resetComboBoxSelectedItem();
 	}
 	
 	private void resetComboBoxSelectedItem() {
 		fontChoice.select(data.font);
 		Integer size = data.fontSize;
 		sizeChoice.select(size.toString());
 		colorComboBox.setSelectedIndex(this.getColorIndex(data.color));
 		backgroundcolorComboBox.setSelectedIndex(this.getColorIndex(data.backgroundColor));
 	}
 	
 	private int getColorIndex(String color) {
 		for (int i = 0; i < data.strColors.length; i++) {
 			String str = data.strColors[i];
 			if (str.equals(color))
 				return i;
 		}
 		return 0;
 	}
 	
 	class ComboLabel{
 		  String text;
 		  Icon icon;
 
 		  ComboLabel(String text, Icon icon){
 		    this.text = text;
 		    this.icon = icon;
 		  }
 
 		  public String getText(){
 		    return text;
 		  }
 
 		  public Icon getIcon(){
 		    return icon;
 		  }
 		}
 	class MyCellRenderer extends JLabel implements ListCellRenderer{
 
 	    MyCellRenderer(){
 	      setOpaque(true);
 	    }
 
 	    public Component getListCellRendererComponent(
 	            JList list,
 	            Object value,
 	            int index,
 	            boolean isSelected,
 	            boolean cellHasFocus){
 
 	      ComboLabel data = (ComboLabel)value;
 	      setText(data.getText());
 	      setIcon(data.getIcon());
 
 	      if (isSelected){
 	        setForeground(Color.white);
 	        setBackground(Color.black);
 	      }else{
 	        setForeground(Color.black);
 	        setBackground(Color.white);
 	      }
 
 	      return this;
 	    }
 	  }
 
 }
 

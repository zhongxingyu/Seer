 package sec01.ex04;
 
 import java.awt.*;
 import java.awt.event.*;
 
 public class PropertyDialog extends Frame implements ActionListener{
 	private static final long serialVersionUID = 1L;
 	private static final int NORMAL_LABEL_FONT_SIZE = 15;
 	private GridBagLayout gbl = new GridBagLayout();
 	
 	public static String buFont = PropertyData.font;  // obt@p
 	public static int buFontSize = PropertyData.fontSize; // obt@p
 	public static String buColor = PropertyData.color; // obt@p
 	public static String buBackgroundColor = PropertyData.backgroundColor; // obt@p
 	
 	private Choice fontChoice;
 	private Choice sizeChoice;
 	private Choice colorChoice;
 	private Choice backgroundColorChoice;
 	
 	private static Label preview;
 
 	public PropertyDialog() {
 		setTitle("Property dialog");
         setSize(560, 200);
         setLocationRelativeTo(null);
         setResizable(false);
         setLayout(gbl);
         
         PropertyData.load();
         
         // x̔zu
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
         
         // j[{bNX̔zu
         fontChoice = new Choice();
         sizeChoice = new Choice();
         colorChoice = new Choice();
         backgroundColorChoice = new Choice();
         
         setFontChoice(fontChoice);
         setSizeChoice(sizeChoice);
         setColorChoice(colorChoice);
         setColorChoice(backgroundColorChoice);
         
         addChoice(fontChoice, 1, 0, 3, 1);
         addChoice(sizeChoice, 1, 1, 1, 1);
         addChoice(colorChoice, 1, 2, 1, 1);
         addChoice(backgroundColorChoice, 1, 3, 1, 1);
         
         // {^̔zu
         Button okButton = new Button("OK");
         Button cancelButton = new Button("Cancel");
         
         okButton.addActionListener(this);
         cancelButton.addActionListener(this);
         
         addButton(okButton, 3, 4, 1, 1);
         addButton(cancelButton, 4, 4, 1, 1);
         
         // Xi[̒ǉ
         fontChoice.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				Choice cho = (Choice)e.getItemSelectable();
 				PropertyDialog.buFont = PropertyData.fonts[cho.getSelectedIndex()];
 				Font f = new Font(buFont, Font.PLAIN, 35);
 				PropertyDialog.preview.setFont(f);
				PropertyDialog.preview.setText("Preview");
 			}
         });
         sizeChoice.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				Choice cho = (Choice)e.getItemSelectable();
 				PropertyDialog.buFontSize = PropertyData.sizes[cho.getSelectedIndex()];
 			}
         });
         colorChoice.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				Choice cho = (Choice)e.getItemSelectable();
 				PropertyDialog.buColor = PropertyData.strColors[cho.getSelectedIndex()];
 			}
         });
         backgroundColorChoice.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				Choice cho = (Choice)e.getItemSelectable();
 				PropertyDialog.buBackgroundColor = PropertyData.strColors[cho.getSelectedIndex()];
 			}
         });
         
         preview = new Label("Preview", Label.CENTER);
         Font f = new Font(buFont, Font.PLAIN, 35);
         preview.setFont(f);
         preview.setBackground(Color.LIGHT_GRAY);
         preview.setForeground(Color.DARK_GRAY);
         addPreviewLabel(preview, 2, 2, 2, 2);
         
         Label memo = new Label("For font", Label.CENTER);
        Font memoFont = new Font("Arial Black", Font.BOLD, 20);
         memo.setFont(memoFont);
         addLabel(memo, 2, 1, 1, 1);
         
         //~ꂽƂ̏
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e)  {
                 e.getWindow().setVisible(false);
             }
        });
 	}
 	
 	public void load() {
 		reloadPropertyData();
 		this.setVisible(true);
 	}
 	
 	private void setFontChoice(Choice ch) {
 		String[] fonts = PropertyData.fonts;
 		for(int i = 0; i < fonts.length; i++){
 			ch.addItem(fonts[i]);
 		}
 	}
 	
 	private void setSizeChoice(Choice ch) {
 		int[] sizes = PropertyData.sizes;
 		for(int i = 0; i < sizes.length; i++){
 			ch.addItem(Integer.toString(sizes[i]));
 		}
 	}
 	
 	private void setColorChoice(Choice ch) {
 		ch.addItem("Black");
         ch.addItem("Red");
         ch.addItem("Blue");
         ch.addItem("Cyan");
         ch.addItem("DarkGray");
         ch.addItem("Gray");
         ch.addItem("Green");
         ch.addItem("LightGray");
         ch.addItem("Magenta");
         ch.addItem("Orange");
         ch.addItem("Pink");
         ch.addItem("White");
         ch.addItem("Yellow");
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
 		PropertyData.setData(buFont, buFontSize, buColor, buBackgroundColor);
 	}
 	
 	private void reloadPropertyData() {
 		buFont = PropertyData.font;
 		buFontSize = PropertyData.fontSize;
 		buColor = PropertyData.color;
 		buBackgroundColor = PropertyData.backgroundColor;
 		resetComboBoxSelectedItem();
 	}
 	
 	private void resetComboBoxSelectedItem() {
 		fontChoice.select(PropertyData.font);
 		Integer size = PropertyData.fontSize;
 		sizeChoice.select(size.toString());
 		colorChoice.select(PropertyData.color);
 		backgroundColorChoice.select(PropertyData.backgroundColor);
 	}
 }
 

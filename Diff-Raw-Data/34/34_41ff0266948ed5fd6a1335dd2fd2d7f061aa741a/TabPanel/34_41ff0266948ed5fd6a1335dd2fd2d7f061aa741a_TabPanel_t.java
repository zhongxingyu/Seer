 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.border.Border;
 
 public class TabPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	private int id;
 	private JTextArea text_area;
 	private JScrollPane scroll;
 	private Border border;
 	private int pad = 10;
 	
 	private Font font;
 	private String font_name;
 	private int font_style;
 	private int font_size;
 	private Color font_color;
 	private Color bg_color;
 	
 	private String clipboard;
 
 	public TabPanel() {
 		// Initialize variables
 		clipboard = "";
 		font_name = "Courier New";
 		font_style = Font.PLAIN;
 		font_size = 12;
 		font = new Font(font_name, font_style, font_size);
 		font_color = Color.GREEN;
		bg_color = Color.BLACK;
 		
 		// Initialize and add JComponents to JPanel
 		this.setLayout(new BorderLayout());
 		text_area = new JTextArea();
 		border = BorderFactory.createEmptyBorder(pad, pad, pad, pad);
 		text_area.setBorder(border);
 		text_area.setFont(font);
 		text_area.setForeground(font_color);
 		text_area.setBackground(bg_color);
 		text_area.setCaretColor(Color.WHITE);
 		scroll = new JScrollPane(text_area);
 		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		this.add(scroll);
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setText(String text) {
 		text_area.setText(text);
 	}
 
 	public String getText() {
 		return text_area.getText();
 	}
 	
 	public void cut() {
 		copy();
 		deleteSelectedText();
 	}
 	
 	public void copy() {
 		clipboard = text_area.getSelectedText();
 	}
 	
 	public void paste() {
		if (text_area.getSelectedText() != null) deleteSelectedText();
 		text_area.insert(clipboard, text_area.getCaretPosition());
 	}
 	
 	public void replace(String from, String to) {
 		int start = text_area.getText().indexOf(from);
 		if (start >= 0 && from.length() > 0)
 			text_area.replaceRange(to, start, start	+ from.length());
 	}
 
 	public void deleteSelectedText() {
 		String from = text_area.getSelectedText();
 		int start = text_area.getText().indexOf(from);
 		if (start >= 0 && from.length() > 0) {
 			text_area.replaceRange("", start, start + from.length());
 		}
 	}
 
 	public String getSelectedText() {
 		return text_area.getSelectedText();
 	}
 
 	public void insert(String text) {
 		text_area.insert(text, text_area.getCaretPosition());
 	}
 
 	public void setLineWrap(boolean wrap) {
 		text_area.setLineWrap(wrap);
 	}
 
 	public void updateFont() {
 		text_area.setFont(new Font(font_name, font_style, font_size));
 	}
 
 	public String getFont_name() {
 		return font_name;
 	}
 
 	public void setFont_name(String font_name) {
 		this.font_name = font_name;
 	}
 
 	public int getFont_style() {
 		return font_style;
 	}
 
 	public void setFont_style(int font_style) {
 		this.font_style = font_style;
 	}
 
 	public String getFont_size() {
 		return Integer.toString(font_size);
 	}
 
 	public void setFont_size(int font_size) {
 		this.font_size = font_size;
 	}
 
 	public Color getFont_color() {
 		return font_color;
 	}
 
 	public void setFont_color(Color font_color) {
 		this.font_color = font_color;
 	}
 
 	public Color getBg_color() {
 		return bg_color;
 	}
 
 	public void setBg_color(Color bg_color) {
 		this.bg_color = bg_color;
 	}
 }

 package com.github.recipeidea.ui.component;
 
 import net.rim.device.api.system.Display;
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.FontFamily;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.Manager;
 import net.rim.device.api.ui.Ui;
 import net.rim.device.api.ui.component.EditField;
 
 public class WEditField extends Manager {
 
 	private EditField editField;
 	private FontFamily preferredFontFamily;
 	private Font preferredFont;
 
 	/**
 	 * Default margins
 	 */
 	private final static int DEFAULT_LEFT_MARGIN = 5;
 	private final static int DEFAULT_RIGHT_MARGIN = 0;
 	private final static int DEFAULT_TOP_MARGIN = 2;
 	private final static int DEFAULT_BOTTOM_MARGIN = 2;
 
 	/**
 	 * Default paddings
 	 */
 	private final static int DEFAULT_LEFT_PADDING = 5;
 	private final static int DEFAULT_RIGHT_PADDING = 5;
 	private final static int DEFAULT_TOP_PADDING = 0;
 	private final static int DEFAULT_BOTTOM_PADDING = 0;
 
 	/**
 	 * Margins around the text box
 	 */
 	private int topMargin = DEFAULT_TOP_MARGIN;
 	private int bottomMargin = DEFAULT_BOTTOM_MARGIN;
 	private int leftMargin = DEFAULT_LEFT_MARGIN;
 	private int rightMargin = DEFAULT_RIGHT_MARGIN;
 
 	/**
 	 * Padding around the text box
 	 */
 	private int topPadding = DEFAULT_TOP_PADDING;
 	private int bottomPadding = DEFAULT_BOTTOM_PADDING;
 	private int leftPadding = DEFAULT_LEFT_PADDING;
 	private int rightPadding = DEFAULT_RIGHT_PADDING;
 
 	/**
 	 * Amount of empty space horizontally around the text box
 	 */
 	private int totalHorizontalEmptySpace = leftMargin + leftPadding
 			+ rightPadding + rightMargin;
 
 	/**
 	 * Amount of empty space vertically around the text box
 	 */
 	private int totalVerticalEmptySpace = topMargin + topPadding
 			+ bottomPadding + bottomMargin;
 
 	/**
 	 * Minimum height of the text box required to display the text entered
 	 */
 	private int minHeight = getFont().getHeight() + totalVerticalEmptySpace;
 
 	/**
 	 * Width of the text box
 	 */
 	private int width = Display.getWidth();
 
 	/**
 	 * Height of the text box
 	 */
 	private int height = minHeight;
 
 	public WEditField() {
 		super(Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
 		this.editField = new EditField(Manager.USE_ALL_WIDTH
 				| Manager.USE_ALL_HEIGHT);
 		try {
 			preferredFontFamily = FontFamily.forName("System");
 			preferredFont = preferredFontFamily.getFont(Font.PLAIN, 7,
 					Ui.UNITS_pt);
 		} catch (ClassNotFoundException e) {
 			preferredFont = Font.getDefault();
 			e.printStackTrace();
 		}
 		editField.setFont(preferredFont);
 		add(editField);
 	}
 
 	protected void sublayout(int width, int height) {
 		/*
 		 * Because we just have one and only field (EditField)
 		 */
 		Field field = getField(0);
 		layoutChild(field, getPreferredWidth() - totalHorizontalEmptySpace,
 				getPreferredHeight() - totalVerticalEmptySpace);
 		setPositionChild(field, leftMargin + leftPadding, topMargin
 				+ topPadding);
 
 		setExtent(getPreferredWidth(), getPreferredHeight());
 	}
 
 	public int getPreferredHeight() {
 		return height;
 	}
 
 	public int getPreferredWidth() {
 		return width;
 	}
 
 	protected void paint(Graphics graphics) {
 
 		EditField ef = (EditField) getField(0);
 		String entireText = ef.getText();
 
 		boolean longText = false;
 		String textToDraw = "";
 		Font font = getFont();
 		int availableWidth = width - totalHorizontalEmptySpace;
 		if (font.getAdvance(entireText) <= availableWidth) {
 			textToDraw = entireText;
 		} else {
 			int endIndex = entireText.length();
 			for (int beginIndex = 1; beginIndex < endIndex; beginIndex++) {
 				textToDraw = entireText.substring(beginIndex);
 				if (font.getAdvance(textToDraw) <= availableWidth) {
 					longText = true;
 					break;
 				}
 			}
 		}
 
 		if (longText == true) {
 			// Force the edit field display only the truncated text
 			ef.setText(textToDraw);
 
 			// Now let the components draw round rect edit Field
 			graphics.drawRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
 			graphics.setColor(Color.WHITE);
 			super.paint(graphics);
 			// Return the text field its original text
 			ef.setText(entireText);
 		} else {
 			graphics.drawRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
 			graphics.setColor(Color.WHITE);
 			super.paint(graphics);
 		}
 	}
 
 	/*
 	 * Change Color to See Another Color Border (non-Javadoc)
 	 * 
 	 * @see
 	 * net.rim.device.api.ui.Field#paintBackground(net.rim.device.api.ui.Graphics
 	 * )
 	 */
 	protected void paintBackground(Graphics graphics) {
 		graphics.setColor(Color.WHITESMOKE);
 		super.paintBackground(graphics);
 	}
 
 	/*
 	 * Set Text and Get Text to Edit Field
 	 */
 	public void setText(String text) {
 		this.editField.setText(text);
 	}
 
	public void getText() {
		this.editField.getText();
 	}
 
 }

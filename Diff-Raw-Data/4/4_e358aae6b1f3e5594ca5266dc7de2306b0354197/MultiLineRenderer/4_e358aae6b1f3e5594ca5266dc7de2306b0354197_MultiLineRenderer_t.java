 // -*- coding: utf-8-unix -*-
 package com.github.sgr.swingx;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.image.ImageObserver;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.SwingConstants;
 import javax.swing.table.TableCellRenderer;
 
 public class MultiLineRenderer implements TableCellRenderer {
     private static Color ODD_ROW_BACKGROUND = new Color(241, 246, 250);
     private JTextArea _txtRenderer = null;
     private JLabel    _imgRenderer = null;
     private SimpleDateFormat _df = null;
 
     public MultiLineRenderer(String datePattern) {
 	_txtRenderer = new JTextArea();
 	_txtRenderer.setEditable(false);
 	_txtRenderer.setLineWrap(true);
 	_txtRenderer.setWrapStyleWord(false);
 	_imgRenderer = new JLabel();
 	_imgRenderer.setOpaque(true);
 	_imgRenderer.setHorizontalTextPosition(SwingConstants.CENTER);
 	_imgRenderer.setHorizontalAlignment(SwingConstants.CENTER);
 	if (datePattern != null) {
 	    _df = new SimpleDateFormat(datePattern);
 	} else {
 	    _df = new SimpleDateFormat("HH:mm:ss");
 	}
     }
 
     public MultiLineRenderer() {
 	this(null);
     }
 
     private JComponent getTxtTableCellRendererComponent(JTable table, String value, boolean isSelected, boolean hasFocus, int row, int column) {
 	int width = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
 	_txtRenderer.setSize(new Dimension(width, 1000));
 	_txtRenderer.setText(value);
 	return _txtRenderer;
     }
     
     private JComponent getImgTableCellRendererComponent(JTable table, Icon value, boolean isSelected, boolean hasFocus, int row, int column) {
 	_imgRenderer.setIcon(value);
 	return _imgRenderer;
     }
 
     @Override
     public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
 	JComponent c = null;
 	if (value instanceof String) {
 	    c = getTxtTableCellRendererComponent(table, (String)value, isSelected, hasFocus, row, column);
 	} else if (value instanceof Date) {
 	    String dateString = _df.format((Date)value);
 	    c = getTxtTableCellRendererComponent(table, dateString, isSelected, hasFocus, row, column);
 	} else if (value instanceof Icon) {
 	    c = getImgTableCellRendererComponent(table, (Icon)value, isSelected, hasFocus, row, column);
 	} else {
	    if (value != null) {
		c = getTxtTableCellRendererComponent(table, value.toString(), isSelected, hasFocus, row, column);
	    }
 	}
 	// render stripe
 	if (isSelected) {
 	    c.setForeground(table.getSelectionForeground());
 	    c.setBackground(table.getSelectionBackground());
 	} else {
 	    c.setForeground(table.getForeground());
 	    c.setBackground(row % 2 == 0 ? table.getBackground() : ODD_ROW_BACKGROUND);
 	}
 	// update rowHeight
 	int h = c.getPreferredSize().height;
 	if (table.getRowHeight(row) < h) {
 	    table.setRowHeight(row, h);
 	}
 	return c;
     }
 }

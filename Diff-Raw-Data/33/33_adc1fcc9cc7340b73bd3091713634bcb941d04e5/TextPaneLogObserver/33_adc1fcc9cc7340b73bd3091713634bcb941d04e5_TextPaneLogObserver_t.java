 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Importer
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.tools;
 
 import java.awt.Color;
 
 import javax.swing.JTextPane;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import de.tuilmenau.ics.fog.ui.LogObserver;
 import de.tuilmenau.ics.fog.ui.Logging.Level;
 import de.tuilmenau.ics.fog.util.Logger;
 
 
 public class TextPaneLogObserver implements LogObserver
 {
 	public TextPaneLogObserver(JTextPane textPane)
 	{
 		this.textPane = textPane;
 		
 		logDoc = textPane.getStyledDocument();
 	}
 	
 	@Override
 	public void log(Level level, Object object, String message)
 	{
 		SimpleAttributeSet tStyle = new SimpleAttributeSet();
 
 		switch(level) {
 		case TRACE:
 		case DEBUG:
 			StyleConstants.setForeground(tStyle, Color.GRAY);
 			break;
 		case LOG:
 			StyleConstants.setForeground(tStyle, Color.BLACK);
 			break;
 		case INFO:
 			StyleConstants.setForeground(tStyle, Color.GREEN);
 			break;
 		case WARN:
 			StyleConstants.setForeground(tStyle, Color.ORANGE);
 			break;
 		case ERROR:
 			StyleConstants.setForeground(tStyle, Color.RED);
 			break;
 		}
 		
 		String logoutput = Logger.formatLog(level, object, message);
 		
 		try {
 			logDoc.insertString(logDoc.getLength(), logoutput +"\n", tStyle);
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 		
 		textPane.setCaretPosition(logDoc.getLength() - 1);
 	}
 	
 	@Override
 	public void close()
 	{
 		// nothing to save; ignore it
 		log(Level.LOG, this, "Disconnected from logging.");
 	}
 	
 	private JTextPane textPane;
 	private StyledDocument logDoc;
 }
 

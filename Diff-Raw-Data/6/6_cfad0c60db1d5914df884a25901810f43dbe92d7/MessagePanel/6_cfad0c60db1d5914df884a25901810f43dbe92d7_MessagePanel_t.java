 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client.ui.commandpanel;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.GridLayout;
 import java.awt.Rectangle;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ToolTipManager;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Element;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.StyleSheet;
 
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.functions.MacroLinkFunction;
 import net.rptools.maptool.client.swing.MessagePanelEditorKit;
 import net.rptools.maptool.model.TextMessage;
 
 public class MessagePanel extends JPanel {
 
 	private JScrollPane scrollPane;
 	private HTMLDocument document;
 	private JEditorPane textPane;
 
 	private static final String SND_MESSAGE_RECEIVED = "messageReceived";
 	
 	/**
 	 * From ImageView
 	 */
     private static final String IMAGE_CACHE_PROPERTY = "imageCache";
 	
 	public MessagePanel() {
 		setLayout(new GridLayout());
 
 		textPane = new JEditorPane();
 		textPane.setEditable(false);
 		textPane.setEditorKit(new MessagePanelEditorKit());
 		textPane.addComponentListener(new ComponentListener() {
 			public void componentHidden(ComponentEvent e) {}
 			public void componentMoved(ComponentEvent e) {}
 			public void componentResized(ComponentEvent e) {
 				// Jump to the bottom on new text
 				if (!MapTool.getFrame().getCommandPanel().getScrollLockButton().isSelected()) {
 					Rectangle rowBounds = new Rectangle(0, textPane.getSize().height, 1, 1);
 					textPane.scrollRectToVisible(rowBounds);
 				}
 			}
 			public void componentShown(ComponentEvent e) {}
 		});
 		textPane.addHyperlinkListener(new HyperlinkListener() {
 			public void hyperlinkUpdate(HyperlinkEvent e) {
 				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 					if (e.getURL() != null) {
 						MapTool.showDocument(e.getURL().toString());
 					} else {
 						Matcher m = Pattern.compile("([^:]*)://([^/]*)/([^?]*)(?:\\?(.*))?").matcher(e.getDescription());
 						if (m.matches()) {
 							if (m.group(1).equalsIgnoreCase("macro")) {
 								MacroLinkFunction.getInstance().runMacroLink(e.getDescription());
 							}
 						}
 					}				
 				}
 			}
 		});
 		ToolTipManager.sharedInstance().registerComponent(textPane);
 		
 		document = (HTMLDocument) textPane.getDocument();
 		
 		// Initialize and prepare for usage
 		refreshRenderer();
 		
 		scrollPane = new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setBorder(null);
 		scrollPane.getViewport().setBorder(null);
 		scrollPane.getViewport().setBackground(Color.white);
 		scrollPane.getVerticalScrollBar().addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				
 				boolean lock = (scrollPane.getSize().height + scrollPane.getVerticalScrollBar().getValue()) < scrollPane.getVerticalScrollBar().getMaximum();
 
 				// The user has manually scrolled the scrollbar, Scroll lock time baby !
 				MapTool.getFrame().getCommandPanel().getScrollLockButton().setSelected(lock);
 			}
 		});
 		
 		add(scrollPane);
 		clearMessages();
 
 		MapTool.getSoundManager().registerSoundEvent(SND_MESSAGE_RECEIVED, MapTool.getSoundManager().getRegisteredSound("Clink"));
 	}
 	
 	public void refreshRenderer() {
 		// Create the style
 		StyleSheet style = document.getStyleSheet();
 		style.addRule("body { font-family: sans-serif; font-size: " + AppPreferences.getFontSize() + "pt}");
 		style.addRule("div {margin-bottom: 5px}");
 		style.addRule("span.roll {background:#efefef}");
 		setTrustedMacroPrefixColors(AppPreferences.getTrustedPrefixFG(), AppPreferences.getTrustedPrefixBG());
 		repaint();
 	}
 
 	public void setTrustedMacroPrefixColors(Color foreground, Color background) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("span.trustedPrefix {background: #").append(String.format("%06X", (background.getRGB() & 0xFFFFFF)));
 		sb.append("; color: #").append(String.format("%06X", (foreground.getRGB() & 0xFFFFFF))).append("}");
 		StyleSheet style = document.getStyleSheet();
 		style.addRule(sb.toString());
 		repaint();
 	}
 	
 	public String getMessagesText() {
 		
 		return textPane.getText();
 	}
 	
 	public void clearMessages() {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				textPane.setText("<html><body id=\"body\"></body></html>");
 				((MessagePanelEditorKit)textPane.getEditorKit()).flush();
 			}
 		});
 	}
 	
 	public void addMessage(final TextMessage message) {
 		
 		if (!message.getSource().equals(MapTool.getPlayer().getName())) {
 			MapTool.playSound(SND_MESSAGE_RECEIVED);
 		}
 		
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				
 				String text = "<div>"+message.getMessage()+"</div>";
 				// We use ASCII control characters to mark off the rolls so that there's no limitation on what (printable) characters the output can include
 				text = text.replaceAll("\036([^\036\037]*)\037([^\036]*)\036", "<span class='roll' title='&#171; $1 &#187;'>$2</span>");
 				text = text.replaceAll("\036\01u\02([^\036]*)\036", "&#171; $1 &#187;");
 				text = text.replaceAll("\036([^\036]*)\036", "&#171;<span class='roll' style='color:blue'>&nbsp;$1&nbsp;</span>&#187;");
 //				text = text.replaceAll("\\{cmd\\s*([^\\}]*)}", "&#171;<span class='cmd' style='color:blue'>&nbsp;$1&nbsp;</span>&#187;");
 
 				// Auto inline expansion
				text = text.replaceAll("(^|\\s|>)(http://[a-zA-Z0-9_\\.%-/~?]+)", "$1<a href=\"$2\">$2</a>");
 				Element element = document.getElement("body");
 				
 				
 				if (!message.getSource().equals(MapTool.getPlayer().getName())) {
 					Matcher m = Pattern.compile("href=[\"']?\\s*(([^:]*)://(?:[^/]*)/(?:[^?]*)(?:\\?(?:.*))?)[\"']\\s*").matcher(message.getMessage());
 					while (m.find()) {
 						if (m.group(2).equalsIgnoreCase("macro")) {
 							MacroLinkFunction.getInstance().processMacroLink(m.group(1));
 						}
 					}
 				}
 				try {
 					document.insertBeforeEnd(element, text);
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 				} catch (BadLocationException ble) {
 					ble.printStackTrace();
 				}
 			}
 		});
 	}
 
 }

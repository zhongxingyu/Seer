 /**
  * Jin - a chess client for internet chess servers.
  * More information is available at http://www.jinchess.com/.
  * Copyright (C) 2002, 2003 Alexander Maryanovsky.
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package free.jin.console;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.LayoutManager;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.Calendar;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.EventListenerList;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Caret;
 import javax.swing.text.DefaultCaret;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.Position;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 //import jregex.MatchIterator;
 //import jregex.MatchResult;
 //import jregex.Pattern;
 //import jregex.PatternSyntaxException;
 import free.jin.Connection;
 import free.jin.Preferences;
 import free.util.BrowserControl;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.regex.PatternSyntaxException;
 
 
 /**
  * A Component which implements a text console in which the user can see the
  * output of the server and write/send arbitrary commands to the server. This
  * is a component that can be used by various plugins - it's mainly used by
  * free.jin.console.ConsoleManager.
  */
 //TODO: create setter for server command instead of contructor to ease creation of new consoles.
 public class Console extends JPanel implements KeyListener, ContainerListener{
 
 
   /**
    * The listener list.
    */
 
   protected final EventListenerList listenerList = new EventListenerList();
 
 
 
   /**
    * The ConsoleTextPane where the output is displayed.
    */
 
   private final ConsoleTextPane outputComponent;
 
 
 
   /**
    * The JScrollPane wrapping the output component.
    */
 
   private final JScrollPane outputScrollPane;
 
 
 
   /**
    * The ConsoleTextField which takes the input from the user.
    */
   
   private ConsoleTextField inputComponent = null;
 
   //private ConsoleImprovedInput inputComponent2;
 
   /**
    * The connection to the server.
    */
 
   private final Connection conn;
 
 
 
   /**
    * The preferences of this console.
    */
 
   private final Preferences prefs;
 
 
 
   /**
    * The regular expressions against which we match the text to find links.
    */
 
   private Pattern [] linkREs;
 
 
 
 
   /**
    * The commands executed for the matched links.
    */
 
   private String [] linkCommands;
 
 
 
 
   /**
    * The indices of the subexpression to make a link out of.
    */
 
   private int [] linkSubexpressionIndices;
 
 
 
 
   /**
    * The regular expression we use for detecting URLs.
    */
 
   private static final Pattern URL_REGEX = Pattern.compile("((([Ff][Tt][Pp]|[Hh][Tt][Tt][Pp]([Ss])?)://)|([Ww][Ww][Ww]\\.))([^\\s()<>\"])*[^\\s.,()<>\"'!?]");
 
 
 
   /**
    * The regular expression we use for detecting emails.
    */
 
   private static final Pattern EMAIL_REGEX = Pattern.compile("[^\\s()<>\"\']+@[^\\s()<>\"]+\\.[^\\s.,()<>\"'?]+");
 
 
   
 
   /**
    * Maps text types that were actually looked up to the resulting AttributeSets.
    */
 
   private final Hashtable attributesCache = new Hashtable();
 
 
 
 
   /**
    * A history of people who have told us anything.
    */
 
   private final ArrayList tellers = new ArrayList();
 
 
 
 
   /**
    * The amount of times addToOutput was called. See {@see #addToOutput(String, String)}
    * for the hack involved.
    */
 
   private int numAddToOutputCalls = 0;
 
 
 
 
   /**
    * Whether the runnable that is supposed to scroll the scrollpane to the
    * bottom already executed. See {@see #addToOutput(String, String)}
    * for the hack involved.
    */
 
   private boolean didScrollToBottom = true;
 
 
   /**
    * Panel at the bottom of the console (contains input field and other components)
    */
 
   private JPanel bottomPanel;
 
     /**
      * Text field that supplies command prefix.
      */
 
     private JTextField prefixSupplier;
 
 
   /**
    * Creates a new Console with the specified preferences and connection to the
    * server.
    */
 
   public Console(Connection conn, Preferences prefs){
     this.conn = conn;
     this.prefs = prefs;
 
     this.outputComponent = createOutputComponent();
     configureOutputComponent(outputComponent);
     this.outputScrollPane = createOutputScrollPane(outputComponent);
     this.inputComponent = createInputComponent();
 
     
     registerKeyboardAction(clearingActionListener, 
         KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
         WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
 
     createUI();
     
     outputComponent.addKeyListener(this);
     inputComponent.addKeyListener(this);
     outputComponent.addContainerListener(this);
     
     init();
   }
 
   /**
    * Creates a new Console that is used for issueing one server command only.
    */
 
   public Console(Connection conn, Preferences prefs, String serverCommand ){
     this.conn = conn;
     this.prefs = prefs;
 
 
     this.outputComponent = new ConsoleTextPane(this);
     configureOutputComponent(outputComponent);
     this.outputScrollPane = createOutputScrollPane(outputComponent);
     this.inputComponent = new ConsoleTextField(this, serverCommand);
     //this.inputComponent2 = new ConsoleImprovedInput(this, serverCommand);
     
     registerKeyboardAction(clearingActionListener, 
         KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
         WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
 
     createUI(serverCommand);
     
     outputComponent.addKeyListener(this);
     inputComponent.addKeyListener(this);
     outputComponent.addContainerListener(this);
     
     init();
   }
   
   /**
    * An action listener which clears the console.
    */
   
   private final ActionListener clearingActionListener = new ActionListener(){
     public void actionPerformed(ActionEvent evt){
       clear();
     }
   };
   
   /**
    * Creates the UI (layout) of this console.
    */
   
   private void createUI(){
     
   
     JButton clearButton = new JButton("Clear Console");
     clearButton.addActionListener(clearingActionListener);
     clearButton.setRequestFocusEnabled(false);
     
     // We always want input component to have focus
     inputComponent.setNextFocusableComponent(inputComponent);
     
     bottomPanel = new JPanel(new BorderLayout());
     bottomPanel.add(inputComponent, BorderLayout.CENTER);
     bottomPanel.add(clearButton, BorderLayout.EAST);
 
     
     setLayout(new BorderLayout());
     add(outputScrollPane, BorderLayout.CENTER);
     add(bottomPanel, BorderLayout.SOUTH);
   }
 
   /**
    * Creates UI for one-command consoles.
    */
 
   private void createUI(String serverCommand) {
 
    prefixSupplier = new JTextField(serverCommand, serverCommand.length()-1);


    //int fixedColumns = badColumns + 1;
    //prefixSupplier.setColumns(fixedColumns);
     prefixSupplier.addFocusListener(new FocusListener(){
             public void focusGained(FocusEvent e){
                 inputComponent.selectAll();
             }
             public void focusLost(FocusEvent e){
                 inputComponent.setCommandPrefix(prefixSupplier.getText().trim() + " ");
             }
         });
     JButton clearButton = new JButton("Clear Console");
     clearButton.addActionListener(clearingActionListener);
     clearButton.setRequestFocusEnabled(false);
     
     // We always want input component to have focus
     inputComponent.setNextFocusableComponent(inputComponent);
     
     bottomPanel = new JPanel(new BorderLayout());
     bottomPanel.add(prefixSupplier, BorderLayout.WEST);
     bottomPanel.add(inputComponent, BorderLayout.CENTER);
     bottomPanel.add(clearButton, BorderLayout.EAST);
 
     
     //setBorder(compoundBorder);
     setLayout(new BorderLayout());
     add(outputScrollPane, BorderLayout.CENTER);
     add(bottomPanel, BorderLayout.SOUTH);
         
         
         
     }
 
 
 
   /**
    * Returns the preferences.
    */
 
   public Preferences getPrefs(){
     return prefs;
   }
 
 
 
 
   /**
    * Creates the <code>ConsoleTextPane</code> to which the server's textual
    * output goes.
    */
 
   protected ConsoleTextPane createOutputComponent(){
     return new ConsoleTextPane(this);
   }
 
 
 
 
   /**
    * Configures the output component to be used with this console.
    */
 
   protected void configureOutputComponent(final ConsoleTextPane textPane){
     // Seriously hack the caret for our own purposes (desired scrolling and selecting).
     Caret caret = new DefaultCaret(){
       
       public void focusGained(FocusEvent evt){
         super.focusGained(evt);
         if (!dragging)
           requestDefaultFocus();
       }
       public void focusLost(FocusEvent e){
         this.setVisible(false);
       }
 
       protected void adjustVisibility(Rectangle nloc){
         if (!dragging)
           return;
 
         if (SwingUtilities.isEventDispatchThread()){
           textPane.scrollRectToVisible(nloc);
           if (nloc.y+nloc.height>textPane.getSize().height-nloc.height/2){
             BoundedRangeModel scrollModel = outputScrollPane.getVerticalScrollBar().getModel();
             scrollModel.setValue(scrollModel.getMaximum());
           }
         }
         else{
           super.adjustVisibility(nloc); // Just in case... shouldn't happen.
         } 
       }
 
       private boolean dragging = false;
 
       public void mousePressed(MouseEvent e){
         dragging = true;
         super.mousePressed(e);
       }
 
       public void mouseReleased(MouseEvent e){
         dragging = false;
         super.mouseReleased(e);
         if (isCopyOnSelect()){
           SwingUtilities.invokeLater(new Runnable(){
             public void run(){
               requestDefaultFocus();
             }
           });
         }
       }
 
 
       protected void moveCaret(MouseEvent e){
         Point pt = new Point(e.getX(), e.getY());
         Position.Bias[] biasRet = new Position.Bias[1];
         int pos = textPane.getUI().viewToModel(textPane, pt, biasRet);
         if (pos >= 0) {
           int maxPos = textPane.getDocument().getEndPosition().getOffset();
           if ((maxPos==pos+1)&&(pos>0)){
             pos--;
             moveDot(pos);
             if (dragging){
               BoundedRangeModel scrollModel = outputScrollPane.getVerticalScrollBar().getModel();
               scrollModel.setValue(scrollModel.getMaximum());
             }
           }
           else
             moveDot(pos);
         }
       }
 
       protected void positionCaret(MouseEvent e) {
         Point pt = new Point(e.getX(), e.getY());
         Position.Bias[] biasRet = new Position.Bias[1];
         int pos = textPane.getUI().viewToModel(textPane, pt, biasRet);
         if (pos >= 0) {
           int maxPos = textPane.getDocument().getEndPosition().getOffset();
           if ((maxPos==pos+1)&&(pos>0)){
             pos--;
             setDot(pos);
             if (dragging){
               BoundedRangeModel scrollModel = outputScrollPane.getVerticalScrollBar().getModel();
               scrollModel.setValue(scrollModel.getMaximum());
             }
           }
           else
             setDot(pos);
         }
       }
 
 
     };
 
     caret.addChangeListener(new ChangeListener(){
       public void stateChanged(ChangeEvent evt){
         if (isCopyOnSelect())
           textPane.copy(); // CDE/Motif style copy/paste
       }
     });
 
     textPane.setCaret(caret);
   }
 
 
 
 
 
   /**
    * The JViewport we use as the viewport for the scrollpane of the output
    * component. This class being the viewport makes sure that when a console is
    * resized, the currently displayed text remains such. The anchor is the last
    * currently visible character.
    */
 
   protected class OutputComponentViewport extends JViewport{
 
     // Used to avoid endless recursion
     private boolean settingViewSize = false;
 
 
     // This makes sure that when the viewport is resized, the last visible line
     // (or character) remains the same after the resize.
     public void reshape(int x, int y, int width, int height){
       Dimension viewSize = getViewSize();
       Dimension viewportSize = getExtentSize();
       JTextComponent view = (JTextComponent)getView();
 
       if ((viewSize.height <= viewportSize.height) || (viewportSize.height < 0) 
           || settingViewSize || ((width == this.getWidth()) && (height == this.getHeight()))
           || (view.getDocument().getLength() == 0)){
         super.reshape(x, y, width, height);
         return;
       }
 
       Point viewPosition = getViewPosition();
       Point viewCoords = 
         new Point(viewportSize.width + viewPosition.x, viewportSize.height + viewPosition.y);
       int lastVisibleIndex = view.viewToModel(viewCoords);
 
       super.reshape(x, y, width, height);
 
       settingViewSize = true;
       this.doLayout();
       this.validate();
       settingViewSize = false;
       // Otherwise the viewport doesn't update what it thinks about the size of
       // the view and may thus scroll to the wrong location.
       
       try{
         Dimension newViewportSize = getExtentSize();
         Rectangle lastVisibleIndexPosition = view.modelToView(lastVisibleIndex);
         if (lastVisibleIndexPosition != null){
           setViewPosition(new Point(0,
             Math.max(0, lastVisibleIndexPosition.y + lastVisibleIndexPosition.height - 1 - newViewportSize.height)));
         }
       } catch (BadLocationException e){}
     }
 
   }
 
 
 
 
   /**
    * Creates the JScrollPane in which the output component will be put.
    */
 
   protected JScrollPane createOutputScrollPane(JTextPane outputComponent){
     JViewport viewport = new OutputComponentViewport();
     viewport.setView(outputComponent);
 
     JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     scrollPane.setViewport(viewport);
 
     viewport.putClientProperty("EnableWindowBlit", Boolean.TRUE);
 
     return scrollPane;
   }
 
 
 
 
   /**
    * Creates the JTextField in which the user can input commands to be sent to
    * the server.
    */
 
   protected ConsoleTextField createInputComponent(){
     ConsoleTextField textField = new ConsoleTextField(this);
     return textField;
   }
 
 
 
 
   
   /**
    * Assigns the default focus to input component.
    */
 
   public boolean requestDefaultFocus(){
     inputComponent.requestFocus();
     return true;
   }
 
 
 
 
   /**
    * Initializes this console, loading all the properties from the plugin, etc.
    * The Console uses the Plugin's (and the User's properties) to determine its 
    * various properties (text color etc.)
    */
 
   private void init(){
     attributesCache.clear(); // Clear the cache
 
     /********************* OUTPUT COMPONENT ***********************/
 
     // We set it here because of a Swing bug which causes the background to be 
     // drawn with the foreground color if you set the background as an attribute.
     Color outputBg = prefs.getColor("background", null);
     if (outputBg != null)
       outputComponent.setBackground(outputBg);
 
     Color outputSelection = prefs.getColor("output-selection", null);
     if (outputSelection != null)
       outputComponent.setSelectionColor(outputSelection);
 
     Color outputSelected = prefs.getColor("output-selected", null);
     if (outputSelected != null)
       outputComponent.setSelectedTextColor(outputSelected);
 
 
 
     /********************* INPUT COMPONENT *************************/
 
     Color inputBg = prefs.getColor("input-background", null);
     if (inputBg != null)
       inputComponent.setBackground(inputBg);
 
     Color inputFg = prefs.getColor("input-foreground", null);
     if (inputFg != null)
       inputComponent.setForeground(inputFg);
 
     Color inputSelection = prefs.getColor("input-selection", null);
     if (inputSelection != null)
       inputComponent.setSelectionColor(inputSelection);
 
     Color inputSelected = prefs.getColor("input-selected", null);
     if (inputSelected != null)
       inputComponent.setSelectedTextColor(inputSelected);
 
 
     int numLinkPatterns = prefs.getInt("output-link.num-patterns", 0);
     linkREs = new Pattern[numLinkPatterns];
     linkCommands = new String[numLinkPatterns];
     linkSubexpressionIndices = new int[numLinkPatterns];
     for (int i = 0; i < numLinkPatterns; i++){
       try{
         String linkPattern = prefs.getString("output-link.pattern-" + i);
         String linkCommand = prefs.getString("output-link.command-" + i);
         int subexpressionIndex = prefs.getInt("output-link.index-"+i);
 
         linkSubexpressionIndices[i] = subexpressionIndex;
         Pattern regex = Pattern.compile(linkPattern);
         linkREs[i] = regex;
         linkCommands[i] = linkCommand;
       } catch (PatternSyntaxException e){
           e.printStackTrace();
         }
     }
   }
 
 
 
 
   /**
    * Refreshes the console by re-reading the plugin/user properties and
    * adjusting the assosiated console properties accordingly. This is useful
    * to call after a user changes the preferences.
    */
 
   public void refreshFromProperties(){
     init();
     outputComponent.refreshFromProperties();
     inputComponent.refreshFromProperties();
   }
 
 
 
 
 
   /**
    * Returns whether text will be copied into the clipboard on selection.
    */
 
   protected boolean isCopyOnSelect(){
     return prefs.getBool("copyOnSelect", true);
   }
 
 
 
 
   /**
    * This method <B>must</B> be called before adding anything to the output
    * component. This method works together with the <code>assureScrolling</code>
    * method.
    *
    * @returns whether the <code>assureScrolling</code> method should scroll the
    * scrollpane of the output component to the bottom. This needs to be passed
    * to the <code>assureScrolling</code> method.
    */
 
   protected final boolean prepareAdding(){
     // Seriously hack the scrolling to make sure if we're at the bottom, we stay there,
     // and if not, we stay there too :-) If you figure out what (and why) I'm doing, drop me an email,
     // and we'll hire you as a Java programmer.
     numAddToOutputCalls++;
     outputScrollPane.getViewport().putClientProperty("EnableWindowBlit", Boolean.FALSE); // Adding a lot of text is slow with blitting
     BoundedRangeModel verticalScroll = outputScrollPane.getVerticalScrollBar().getModel();
 
     return (verticalScroll.getMaximum()<=verticalScroll.getValue()+verticalScroll.getExtent()+5);
     // The +5 is to scroll it to the bottom even if it's a couple of pixels away.
     // This can happen if you try to scroll to the bottom programmatically
     // (a bug probably) using scrollRectToVisible(Rectangle).
   }
 
 
 
 
   /**
    * This method <B>must</B> be called after adding anything to the output
    * component. This method works together with the <code>prepareAdding</code>
    * method. Pass the value returned by <code>prepareAdding</code> as the
    * argument of this method.
    */
 
   protected final void assureScrolling(boolean scrollToBottom){
     class BottomScroller implements Runnable{
       
       private int curNumCalls;
       
       BottomScroller(int curNumCalls){
         this.curNumCalls = curNumCalls;
       }
 
       public void run(){
         if (numAddToOutputCalls == curNumCalls){
           try{
             int lastOffset = outputComponent.getDocument().getEndPosition().getOffset();
             Rectangle lastCharRect = outputComponent.modelToView(lastOffset - 1);
             outputComponent.scrollRectToVisible(lastCharRect);
           } catch (BadLocationException e){e.printStackTrace();}
 
           didScrollToBottom = true;
 
           // Enable blitting again
           outputScrollPane.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE); 
 
           outputComponent.repaint();
         }
         else{
           curNumCalls = numAddToOutputCalls;
           SwingUtilities.invokeLater(this);
         }
       }
       
     }
 
     if (scrollToBottom && didScrollToBottom){
       // This may be false if the frame containing us (for example), is iconified
       if (getPeer() != null){
         didScrollToBottom = false;
         SwingUtilities.invokeLater(new BottomScroller(numAddToOutputCalls));
       }
     }
   }
 
 
 
 
   /**
    * Adds the given component to the output.
    */
 
   public void addToOutput(JComponent component){
     boolean shouldScroll = prepareAdding();
 
     boolean wasEditable = outputComponent.isEditable();
     outputComponent.setEditable(true);
     outputComponent.setCaretPosition(outputComponent.getDocument().getLength());
     StyledDocument document = outputComponent.getStyledDocument();
     outputComponent.insertComponent(component);
 
     // See http://developer.java.sun.com/developer/bugParade/bugs/4353673.html
     LayoutManager layout = component.getParent().getLayout();
     if (layout instanceof OverlayLayout)
       ((OverlayLayout)layout).invalidateLayout(component.getParent());
 
     try{
       document.insertString(document.getLength(), "\n", null);
     } catch (BadLocationException e){
         e.printStackTrace();
       } 
     outputComponent.setEditable(wasEditable);
 
     assureScrolling(shouldScroll);
   }
 
 
 
 
   /**
    * Adds the given text of the given text type to the output.
    *
    * @param text The text to add, '\n' excluded.
    * @param textType The type of the text, "kibitz" for example.
    */
 
   public void addToOutput(String text, String textType){
     try{
       boolean shouldScroll = prepareAdding();
       addToOutputImpl(text, textType);
       assureScrolling(shouldScroll);
     } catch (BadLocationException e){
         e.printStackTrace(); // Why the heck is this checked?
       }
   }
 
 
 
 
   /**
    * Actually does the work of adding the given text to the output component's
    * Document.
    */
 
   protected void addToOutputImpl(String text, String textType) throws BadLocationException{
     StyledDocument document = outputComponent.getStyledDocument();
     int oldTextLength = document.getLength();
     document.insertString(document.getLength(), text+"\n", attributesForTextType(textType));
     
 
     AttributeSet urlAttributes = attributesForTextType("link.url");
     AttributeSet emailAttributes = attributesForTextType("link.email");
     AttributeSet commandAttributes = attributesForTextType("link.command");
 
     Matcher urlMatches = URL_REGEX.matcher(text);
     
     while(urlMatches.find()){
       int matchStart = urlMatches.start();
       int matchEnd = urlMatches.end();
 
       Command command = new Command("url "+text.substring(matchStart,matchEnd),
         Command.SPECIAL_MASK | Command.BLANKED_MASK);
       Position linkStart = document.createPosition(matchStart + oldTextLength);
       Position linkEnd = document.createPosition(matchEnd + oldTextLength);
       Link link = new Link(linkStart, linkEnd, command);
       document.setCharacterAttributes(matchStart + oldTextLength, matchEnd - matchStart, 
         urlAttributes, false);
       outputComponent.addLink(link);
         
     }
 
     /*MatchIterator emailMatches = EMAIL_REGEX.matcher(text).findAll();*/
      
      Matcher emailMatches = EMAIL_REGEX.matcher(text);
     while (emailMatches.find()){
       int matchStart = emailMatches.start();
       int matchEnd = emailMatches.end();
 
       Command command = new Command("email "+text.substring(matchStart,matchEnd),
         Command.SPECIAL_MASK | Command.BLANKED_MASK);
       Position linkStart = document.createPosition(matchStart + oldTextLength);
       Position linkEnd = document.createPosition(matchEnd + oldTextLength);
       Link link = new Link(linkStart, linkEnd, command);
       document.setCharacterAttributes(matchStart + oldTextLength, matchEnd - matchStart,
         emailAttributes, false);
       outputComponent.addLink(link);
     }
 
     for (int i = 0; i < linkREs.length; i++){
       Pattern linkRE = linkREs[i];
 
       if (linkRE == null) // Bad pattern was given in properties.
         continue;
 
       //MatchIterator matches = linkRE.matcher(text).findAll();  
       Matcher matches = linkRE.matcher(text);
       while(matches.find()){
         String linkCommand = linkCommands[i];
 
         //MatchResult result = matches.nextMatch();
         
 
         int index = -1;
         while ((index = linkCommand.indexOf("$", index+1))!=-1){
           if ((index<linkCommand.length()-1)&&(Character.isDigit(linkCommand.charAt(index+1)))){
             int subexpressionIndex = Character.digit(linkCommand.charAt(index+1),10);
             linkCommand = linkCommand.substring(0,index)+matches.group(subexpressionIndex)+linkCommand.substring(index+2);
           }
         }
 
         int linkSubexpressionIndex = linkSubexpressionIndices[i];
         int matchStart = matches.start(linkSubexpressionIndex);
         int matchEnd = matches.end(linkSubexpressionIndex);
 
         document.setCharacterAttributes(matchStart + oldTextLength, matchEnd - matchStart,
           commandAttributes, false);
 
         Position linkStart = document.createPosition(matchStart + oldTextLength);
         Position linkEnd = document.createPosition(matchEnd + oldTextLength);
         Link link = new Link(linkStart, linkEnd, new Command(linkCommand,0));
         outputComponent.addLink(link);
       }
     }
 
   }
 
 
 
 
     /**
    * Returns the size of the output area.
    */
 
   public Dimension getOutputArea(){
     return outputScrollPane.getViewport().getSize();
   }
 
 
 
 
   /**
    * Executes a special command. The following commands are recognized by this
    * method:
    * <UL>
    *   <LI> cls - Removes all text from the console.
    *   <LI> "url <url>" - Displays the URL  (the '<' and '>' don't actually appear in the string).
    *   <LI> "email <email address>" - Displays the mailer with the "To" field set to the given email address.
    * </UL>
    */
 
   protected void executeSpecialCommand(String command){
     command = command.trim();
     if (command.equalsIgnoreCase("cls")){
       clear();
     }
     else if (command.startsWith("url ")){
       String urlString = command.substring("url ".length());
       
       // A www. string
       if (urlString.substring(0, Math.min(4, urlString.length())).equalsIgnoreCase("www."))
         urlString = "http://" + urlString; // Assume http
 
       if (!BrowserControl.displayURL(urlString))
         BrowserControl.showDisplayBrowserFailedDialog(urlString, this, true);
     }
     else if (command.startsWith("email ")){
       String emailString = command.substring("email ".length());
       if (!BrowserControl.displayMailer(emailString))
         BrowserControl.showDisplayMailerFailedDialog(emailString, this, true);
     }
     else{
       addToOutput("Unknown special command: \""+command+"\"","system");
     }
   }
   
 
 
 
   
   /**
    * Executes the given command.
    */
 
   public void issueCommand(Command command){
     String commandString = command.getCommandString();
 
     if (!command.isBlanked()){
       addToOutput(commandString, "user");
     } 
 
     if (command.isSpecial())
       executeSpecialCommand(commandString);
     else{
       if (conn.isConnected())
         conn.sendCommand(commandString);
       else
         addToOutput("Unable to issue command - not connected to the server.", "info");
     }
   }
 
 
 
 
   /**
    * Removes all text from the console.
    */
 
   public void clear(){
     outputComponent.setText("");
     outputComponent.removeAll();
     outputComponent.removeLinks();
   }
 
 
 
 
   /**
    * Gets called when a tell by the given player is received. This method saves
    * the name of the teller so it can be later retrieved when F9 is hit.
    */
 
   public void tellReceived(String teller){
     tellers.remove(teller);
     tellers.add(0, teller);
     if (tellers.size() > getTellerRingSize())
       tellers.remove(tellers.size() - 1);
   }
 
 
 
 
   /**
    * Returns the size of the teller ring, the amount of last players who told us
    * something we traverse.
    */
 
   public int getTellerRingSize(){
     return prefs.getInt("teller-ring-size", 5);
   }
 
 
 
   /**
    * Returns the nth (from the end) person who told us something via "tell",
    * "say" or "atell"  which went into this console. Returns <code>null</code>
    * if no such person exists. The index is 0 based.  Sorry about the name of 
    * the method but I didn't think getColocutor() was much better :-)
    */
 
   public String getTeller(int n){
     if ((n < 0) || (n >= tellers.size()))
       return null;
 
     return (String)tellers.get(n);
   }
 
 
 
 
   /**
    * Returns the amount of people who have told us anything so far.
    */
 
   public int getTellerCount(){
     return tellers.size();
   }
 
 
 
   /**
    * Returns the AttributeSet for the given type of output text. Due to a bug
    * in Swing, this method does not address the background color.
    */
 
   protected AttributeSet attributesForTextType(String textType){
     AttributeSet attributes = (AttributeSet)attributesCache.get(textType);
     if (attributes != null){
         
         return attributes;
     }
 
     String fontFamily = (String)prefs.lookup("font-family." + textType, "Monospaced");
     Integer fontSize = (Integer)prefs.lookup("font-size." + textType, new Integer(14));
     Boolean bold = (Boolean)prefs.lookup("font-bold." + textType, Boolean.FALSE);
     Boolean italic = (Boolean)prefs.lookup("font-italic." + textType, Boolean.FALSE);
     Boolean underline = (Boolean)prefs.lookup("font-underlined." + textType, Boolean.FALSE);
     Color foreground = (Color)prefs.lookup("foreground." + textType, Color.white);
     Float indent = (Float)prefs.lookup("indent." + textType, new Float(6.0));
     //System.out.println("[*]Indent value = " + indent);
 
    
 
     SimpleAttributeSet mAttributes = new SimpleAttributeSet();
     mAttributes.addAttribute(StyleConstants.FontFamily, fontFamily);
     mAttributes.addAttribute(StyleConstants.FontSize, fontSize);
     mAttributes.addAttribute(StyleConstants.Bold, bold);
     mAttributes.addAttribute(StyleConstants.Italic, italic);
     mAttributes.addAttribute(StyleConstants.Underline, underline);
     mAttributes.addAttribute(StyleConstants.Foreground, foreground);
     mAttributes.addAttribute(StyleConstants.FirstLineIndent, indent);
 //    StyleConstants.setFontFamily(mAttributes, fontFamily);
 //    StyleConstants.setFontSize(mAttributes, fontSize);
 //    StyleConstants.setBold(mAttributes, bold);
 //    StyleConstants.setItalic(mAttributes, italic);
 //    StyleConstants.setUnderline(mAttributes, underlined);
 //    StyleConstants.setForeground(mAttributes, foreground);
     attributesCache.put(textType, mAttributes);
 
     return mAttributes;
   }
 
 
 
 
 
   /**
    * Processes Key pressed events from the components we're registered as
    * listeners for. The default implementation is registered to listen to the
    * input component.
    */
 
   public void keyPressed(KeyEvent evt){
     int keyCode = evt.getKeyCode();
     boolean isControlDown = evt.isControlDown();
 
     if ((evt.getSource() == inputComponent)){
       if (evt.getID() == KeyEvent.KEY_PRESSED){
         JScrollBar vscrollbar = outputScrollPane.getVerticalScrollBar();
         Rectangle viewRect = outputScrollPane.getViewport().getViewRect();
         int value = vscrollbar.getValue();
 
         switch (keyCode){
           case KeyEvent.VK_PAGE_UP: // Page Up
             vscrollbar.setValue(value -
               outputComponent.getScrollableBlockIncrement(viewRect,
               SwingConstants.VERTICAL, -1));
             break;
           case KeyEvent.VK_PAGE_DOWN: // Page Down
             vscrollbar.setValue(value + 
               outputComponent.getScrollableBlockIncrement(viewRect,
               SwingConstants.VERTICAL, +1));
             break;
         }
 
         if (isControlDown){
           switch (keyCode){
             case KeyEvent.VK_UP: // Ctrl-Up
               vscrollbar.setValue(value -
                 outputComponent.getScrollableUnitIncrement(viewRect, SwingConstants.VERTICAL, -1));
               break;
             case KeyEvent.VK_DOWN: // Ctrl-Down
               vscrollbar.setValue(value + 
                 outputComponent.getScrollableUnitIncrement(viewRect, SwingConstants.VERTICAL, +1));
               break;
             case KeyEvent.VK_HOME: // Ctrl-Home
               vscrollbar.setValue(vscrollbar.getMinimum());
               break;
             case KeyEvent.VK_END: // Ctrl-End
               vscrollbar.setValue(vscrollbar.getMaximum() - vscrollbar.getVisibleAmount());
               break;
             // case KeyEvent.VK_A: // Ctrl-A
             //   int documentLength = outputComponent.getDocument().getLength();
             //   outputComponent.setSelectionStart(0);
             //   outputComponent.setSelectionEnd(documentLength - 1);
               // The -1 here is important because otherwise it selects the end of
               // line at the end too, and then adding more text selects it too.
             //   break;
           }
         }
       }
     }
 
   }
 
 
 
 
   /**
    * Processes Key released events from the components we're registered as
    * listeners for. The default implementation is registered to listen to the
    * output and to the input component.
    */
 
   public void keyReleased(KeyEvent evt){}
 
 
 
 
   /**
    * Processes Key typed events from the components we're registered as
    * listeners for. The default implementation is registered to listen to the
    * output and to the input component.
    */
 
   public void keyTyped(KeyEvent evt){}
 
 
 
 
   /**
    * Listens to components being added to the output component and its descendents
    * and registers as the key and container listener for all of them, because
    * we need to transfer the focus to the input field.
    */
    
   public void componentAdded(ContainerEvent evt){
     Container container = evt.getContainer();
     Component child = evt.getChild();
 
     if (SwingUtilities.isDescendingFrom(container,outputComponent)) // Check just in case.
       registerAsListenerToHierarchy(child);
   }
 
 
 
 
   /**
    * Listens to components being removed from the output component and its
    * descendents and unregisters as the key listener.
    */
   
   public void componentRemoved(ContainerEvent evt){
     Container container = evt.getContainer();
     Component child = evt.getChild();
 
     if (SwingUtilities.isDescendingFrom(container,outputComponent)) // Check just in case.
       unregisterAsListenerToHierarchy(child);
   }
 
 
 
 
   /**
    * Recursively registers <code>this</code> as the key listener with the given
    * component and of its descendants (recursively) if they are focus
    * traversable. If they are Containers, also registers as their
    * ContainerListener.
    */
 
   private void registerAsListenerToHierarchy(Component component){
     if (component.isFocusTraversable())
       component.addKeyListener(this);
 
     if (component instanceof Container){
       Container container = (Container)component;
       container.addContainerListener(this);
       int numChildren = container.getComponentCount();
       for (int i=0; i<numChildren; i++)
         registerAsListenerToHierarchy(container.getComponent(i));        
     }
   }
 
 
 
   
   /**
    * Does the opposite of <code>registerAsListenerToHierarchy(Component)</code>,
    * unregistering <code>this</code> as the key or container listener from the
    * given component and any of its children.
    */
 
   private void unregisterAsListenerToHierarchy(Component component){
     if (component.isFocusTraversable())
       component.removeKeyListener(this);
 
     if (component instanceof Container){
       Container container = (Container)component;
       container.removeContainerListener(this);
       int numChildren = container.getComponentCount();
       for (int i=0; i<numChildren; i++)
         unregisterAsListenerToHierarchy(container.getComponent(i));        
     }
   }
 
 
   
 
 }

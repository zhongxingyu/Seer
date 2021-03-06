 /*
  * @(#)${NAME}
  *
  * Copyright 2002 - 2004 JIDE Software Inc. All rights reserved.
  */
 package com.jidesoft.swing;
 
 import com.jidesoft.plaf.UIDefaultsLookup;
 import com.jidesoft.popup.JidePopup;
 import com.jidesoft.swing.event.SearchableEvent;
 import com.jidesoft.swing.event.SearchableListener;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.EventListenerList;
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.Locale;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 /**
  * JList, JTable and JTree are three data-rich components. They can be used
  * to display a huge amount of data so searching function will be very a useful feature in those components.
  * <code>Searchable</code> is such a class that can make JList, JTable and JTree searchable.
  * User can simply type in any string they want to search for and use arrow keys to navigate
  * to next or previous occurrence.
  * <p/>
  * <code>Searchable</code> is a base abstract class. <code>ListSearchable</code>, <code>TableSearchable</code> and <code>TreeSearchable</code>
  * are implementations to make JList, JTable and JTree searchable respectively. For each implementation, there are
  * five methods need to be implemented.
  * <ul>
  * <li><code>protected abstract int getSelectedIndex()</code>
  * <li><code>protected abstract void setSelectedIndex(int index, boolean incremental)</code>
  * <li><code>protected abstract int getElementCount()</code>
  * <li><code>protected abstract Object getElementAt(int index)</code>
  * <li><code>protected abstract String convertElementToString(Object element)</code>
  * </ul>
  * <p/>
  * Please look at the javadoc of each method to learn more details.
  * <p/>
  * The keys used by this class are fully customizable. Subclass can override the methods such as {@link #isActivateKey(java.awt.event.KeyEvent)},
  * {@link #isDeactivateKey(java.awt.event.KeyEvent)}, {@link #isFindFirstKey(java.awt.event.KeyEvent)},{@link #isFindLastKey(java.awt.event.KeyEvent)},
  * {@link #isFindNextKey(java.awt.event.KeyEvent)}, {@link #isFindPreviousKey(java.awt.event.KeyEvent)} to provide its own set of keys.
  * <p/>
  * In addition to press up/down arrow to find next occurrence or previous occurrence of
  * particular string, there are several other features that are very handy.
  * <p/>
  * Multiple selection feature - If you press CTRL key and hold it while pressing up and down arrow,
  * it will find next/previous occurence while keeping existing selections.
  * <br>
  * Select all feature - If you type in a searching text and press CTRL+A, all the occurrences of
  * that searching string will be selected. This is a very handy feature.
  * For example you want to delete all rows in a table whose name column begins with "old".
  * So you can type in "old" and press CTRL+A, now all rows begining with "old" will
  * be selected. Pressing delete will delete all of them.
  * <br>
  * Basic regular expression support - It allows '?' to match any letter or digit,
  * or '*' to match several letters or digits.
  * Even though it's possible to implement full regular expression support, we don't want to do that.
  * The reason is the regular expression is very complex, it's probably not a good idea to let user
  * type in such a complex expression in a small popup window. However if your user is very familiar
  * with regular expression, you can add the feature to <code>Searchable</code>. All you need to do
  * is to override {@link #compare(String,String)} method
  * and implement by yourself.
  * <p/>
  * As this is an abstract class, please refer to to javadoc of {@link ListSearchable},{@link TreeSearchable}, and {@link TableSearchable} to find out
  * how to use it with JList, JTree and JTable respectively.
  * <p/>
  * This component has a timer. If user types very fast, it will accumulate them together and generate only one searching action.
  * The timer can be controlled by {@link #setSearchingDelay(int)}.
  * <p/>
  * By default we will use lightweight popup for the sake of performance. But if you use heavyweight component
  * which could obscure the lightweight popup, you can call {@link #setHeavyweightComponentEnabled(boolean)} to true
  * so that heavyweight popup will be used.
  */
 public abstract class Searchable {
 
     private final PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);
 
     protected final JComponent _component;
 
     private SearchPopup _popup;
     private JLayeredPane _layeredPane;
 
     private boolean _heavyweightComponentEnabled;
 
     /**
      * optional SearchableProvider
      */
     private SearchableProvider _searchableProvider;
     private Pattern _pattern;
     private String _searchText;
     private String _previousSearchText;
 
     private boolean _caseSensitive = false;
     private boolean _repeats = false;
     private boolean _wildcardEnabled = true;
     private Color _mismatchForeground;
     private Color _foreground = null;
     private Color _background = null;
     protected ComponentAdapter _componentListener;
 
     public final static String PROPERTY_SEARCH_TEXT = "searchText";
 
     private KeyAdapter _keyListener;
 
     private FocusAdapter _focusListener;
 
     private int _cursor = -1;
 
     private String _searchLabel = getResourceString("Searchable.searchFor");
 
     private String _noMatchLabel = getResourceString("Searchable.noMatch");
 
     /**
      * The popup location
      */
     private int _popupLocation = SwingConstants.TOP;
 
     private int _searchingDelay = 0;
 
     private boolean _reverseOrder = false;
 
     /**
      * A list of event listeners for this component.
      */
     protected EventListenerList listenerList = new EventListenerList();
 
     private Component _popupLocationRelativeTo;
 
     /**
      * Creates a Searchable.
      *
      * @param component component where the Searchable will be installed.
      */
     public Searchable(JComponent component) {
         _previousSearchText = null;
         _component = component;
         installListeners();
     }
 
     /**
      * Creates a Searchable.
      *
      * @param component component where the Searchable will be installed.
      */
     public Searchable(JComponent component, SearchableProvider searchableProvider) {
         _searchableProvider = searchableProvider;
         _previousSearchText = null;
         _component = component;
         installListeners();
     }
 
     /**
      * Gets the selected index in the component. The concrete implementation
      * should call methods on the component to retrieve the current selected index.
      * If the component supports multiple selection,
      * it's OK just return the index of the first selection.
      * <p>Here are some examples. In the case of JList, the index is the row index.
      * In the case of JTree, the index is the row index too. In the case of JTable, depending on the seleection mode,
      * the index could be row index (in row selection mode), could be column index (in column selection mode)
      * or could the cell index (in cell selection mode).
      *
      * @return the selected index.
      */
     protected abstract int getSelectedIndex();
 
     /**
      * Sets the selected index. The concrete implementation should call methods on the component to select
      * the element at the specified index. The incremental flag is used to do multiple select. If the flag is true,
      * the element at the index should be added to current selection. If false, you should clear previous
      * selection and then select the element.
      *
      * @param index       the index to be selected
      * @param incremental a flag to enable multiple selection. If the flag is true,
      *                    the element at the index should be added to current selection. If false, you should clear previous
      *                    selection and then select the element.
      */
     protected abstract void setSelectedIndex(int index, boolean incremental);
 
     /**
      * Gets the total element count in the component. Different concrete implementation could have different interpretation of
      * the count. This is totally OK as long as it's consistent in all the methods. For example, the index parameter in other methods
      * should be always a valid value within the total count.
      *
      * @return the total element count.
      */
     protected abstract int getElementCount();
 
     /**
      * Gets the element at the specified index. The element could be any data structure that
      * internally used in the component. The convertElementToString method will give you a chance
      * to convert the element to string which is used to compare with the string that user types in.
      *
      * @param index the index
      * @return the element at the specified index.
      */
     protected abstract Object getElementAt(int index);
 
     /**
      * Converts the element that returns from getElementAt() to string.
      *
      * @param element
      * @return the string representing the element in the component.
      */
     protected abstract String convertElementToString(Object element);
 
     /**
      * A text field for searching text.
      */
     protected class SearchField extends JTextField {
         SearchField() {
         }
 
         @Override
         public Dimension getPreferredSize() {
             Dimension size = super.getPreferredSize();
             size.width = getFontMetrics(getFont()).stringWidth(getText()) + 4;
             return size;
         }
 
         @Override
         public void processKeyEvent(KeyEvent e) {
             int keyCode = e.getKeyCode();
             if (keyCode == KeyEvent.VK_BACK_SPACE && getDocument().getLength() == 0) {
                 e.consume();
                 return;
             }
             final boolean isNavigationKey = isNavigationKey(e);
             if (isDeactivateKey(e) && !isNavigationKey) {
                 hidePopup();
                 if (keyCode == KeyEvent.VK_ESCAPE)
                     e.consume();
                 return;
             }
             super.processKeyEvent(e);
             if (keyCode == KeyEvent.VK_BACK_SPACE || isNavigationKey)
                 e.consume();
             if (isSelectAllKey(e)) {
                 e.consume();
             }
         }
     }
 
     /**
      * The popup panel for search label and search text field.
      */
     private class DefaultSearchPopup extends SearchPopup {
         private JLabel _label;
         private JLabel _noMatch;
 
         public DefaultSearchPopup(String text) {
             initComponents(text);
         }
 
         private void initComponents(String text) {
             final Color foreground = Searchable.this.getForeground();
             final Color background = Searchable.this.getBackground();
 
             // setup the label
             _label = new JLabel(_searchLabel);
             _label.setForeground(foreground);
             _label.setVerticalAlignment(JLabel.BOTTOM);
 
             _noMatch = new JLabel();
             _noMatch.setForeground(getMismatchForeground());
             _noMatch.setVerticalAlignment(JLabel.BOTTOM);
 
             //setup text field
             _textField = new SearchField();
             _textField.setFocusable(false);
             _textField.setOpaque(false);
             _textField.setBorder(BorderFactory.createEmptyBorder());
             _textField.setForeground(foreground);
             _textField.setCursor(getCursor());
             _textField.getDocument().addDocumentListener(new DocumentListener() {
                 private Timer timer = new Timer(200, new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         applyText();
                     }
                 });
 
                 public void insertUpdate(DocumentEvent e) {
                     startTimer();
                 }
 
                 public void removeUpdate(DocumentEvent e) {
                     startTimer();
                 }
 
                 public void changedUpdate(DocumentEvent e) {
                     startTimer();
                 }
 
                 protected void applyText() {
                     String text = _textField.getText().trim();
                     if (text.length() != 0) {
                         int found = findFromCursor(text);
                         if (found == -1) {
                             _textField.setForeground(getMismatchForeground());
                         }
                         else {
                             _textField.setForeground(foreground);
                         }
                         select(found, null, text);
                     }
                     else {
                         hidePopup();
                     }
                 }
 
                 void startTimer() {
                     updatePopupBounds();
                     if (getSearchingDelay() > 0) {
                         timer.setDelay(getSearchingDelay());
                         if (timer.isRunning()) {
                             timer.restart();
                         }
                         else {
                             timer.setRepeats(false);
                             timer.start();
                         }
                     }
                     else {
                         applyText();
                     }
                 }
             });
             _textField.setText(text);
 
             setBackground(background);
             setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIDefaultsLookup.getColor("controlShadow"), 1),
                     BorderFactory.createEmptyBorder(0, 6, 1, 8)));
             setLayout(new BorderLayout(2, 0));
             Dimension size = _label.getPreferredSize();
             size.height = _textField.getPreferredSize().height;
             _label.setPreferredSize(size);
             add(_label, BorderLayout.BEFORE_LINE_BEGINS);
             add(_textField, BorderLayout.CENTER);
             add(_noMatch, BorderLayout.AFTER_LINE_ENDS);
             setPopupBorder(BorderFactory.createEmptyBorder());
         }
 
         @Override
         protected void select(int index, KeyEvent e, String searchingText) {
             if (index != -1) {
                 setSelectedIndex(index, e != null && isIncrementalSelectKey(e));
                 _cursor = index;
                 _textField.setForeground(getForeground());
                 _noMatch.setText("");
             }
             else {
                 _textField.setForeground(getMismatchForeground());
                 _noMatch.setText(_noMatchLabel);
             }
             updatePopupBounds();
             firePropertyChangeEvent(searchingText);
             if (index != -1) {
                 Object element = getElementAt(index);
                 fireSearchableEvent(new SearchableEvent(Searchable.this, SearchableEvent.SEARCHABLE_MATCH, searchingText, element, convertElementToString(element)));
             }
             else {
                 fireSearchableEvent(new SearchableEvent(Searchable.this, SearchableEvent.SEARCHABLE_NOMATCH, searchingText));
             }
         }
 
         private void updatePopupBounds() {
             if (_popup != null) {
                 try {
                     _popup.setSize(_popup.getPreferredSize());
                 }
                 catch (Exception e) { // catch any potential exception
                     // see bug report at http://www.jidesoft.com/forum/viewtopic.php?p=8557#8557
                 }
                 _popup.validate();
             }
         }
     }
 
     /**
      * Hides the popup.
      */
     public void hidePopup() {
         if (_popup != null) {
             if (isHeavyweightComponentEnabled()) {
                 _popup.hidePopupImmediately();
             }
             else {
                 _layeredPane.remove(_popup);
                 _layeredPane.validate();
                 _layeredPane.repaint();
                 _layeredPane = null;
             }
             _popup = null;
             _searchableProvider = null;
             fireSearchableEvent(new SearchableEvent(this, SearchableEvent.SEARCHABLE_END));
         }
         _cursor = -1;
     }
 
     public SearchableProvider getSearchableProvider() {
         return _searchableProvider;
     }
 
     public void setSearchableProvider(SearchableProvider searchableProvider) {
         _searchableProvider = searchableProvider;
     }
 
     /**
      * Installs necessary listeners to the component. This method will be called automatically when Searchable is created.
      */
     public void installListeners() {
         if (_componentListener == null) {
             _componentListener = new ComponentAdapter() {
                 @Override
                 public void componentHidden(ComponentEvent e) {
                     super.componentHidden(e);
                     boolean passive = _searchableProvider == null || _searchableProvider.isPassive();
                     if (passive) {
                         hidePopup();
                     }
                 }
 
                 @Override
                 public void componentResized(ComponentEvent e) {
                     super.componentResized(e);
                     boolean passive = _searchableProvider == null || _searchableProvider.isPassive();
                     if (passive) {
                         updateSizeAndLocation();
                     }
                 }
 
                 @Override
                 public void componentMoved(ComponentEvent e) {
                     super.componentMoved(e);
                     boolean passive = _searchableProvider == null || _searchableProvider.isPassive();
                     if (passive) {
                         updateSizeAndLocation();
                     }
                 }
             };
         }
         _component.addComponentListener(_componentListener);
         Component scrollPane = JideSwingUtilities.getScrollPane(_component);
         if (scrollPane != null) {
             scrollPane.addComponentListener(_componentListener);
         }
 
         _keyListener = new KeyAdapter() {
             @Override
             public void keyTyped(KeyEvent e) {
                 boolean passive = _searchableProvider == null || _searchableProvider.isPassive();
                 if (passive) {
                     keyTypedOrPressed(e);
                 }
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
                 boolean passive = _searchableProvider == null || _searchableProvider.isPassive();
                 if (passive) {
                     keyTypedOrPressed(e);
                 }
             }
         };
         JideSwingUtilities.insertKeyListener(getComponent(), _keyListener, 0);
 
         _focusListener = new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent focusevent) {
                 boolean passive = _searchableProvider == null || _searchableProvider.isPassive();
                 if (passive) {
                     hidePopup();
                 }
             }
         };
         getComponent().addFocusListener(_focusListener);
     }
 
     /**
      * Uninstall the listeners that installed before. This method is never called because
      * we don't have the control of the life cyle of the component. However you can call this
      * method if you don't want the searchable component not searchable.
      */
     public void uninstallListeners() {
         if (_componentListener != null) {
             getComponent().removeComponentListener(_componentListener);
             Component scrollPane = JideSwingUtilities.getScrollPane(getComponent());
             if (scrollPane != null) {
                 scrollPane.removeComponentListener(_componentListener);
             }
             _componentListener = null;
         }
 
         if (_keyListener != null) {
             getComponent().removeKeyListener(_keyListener);
             _keyListener = null;
         }
 
         if (_focusListener != null) {
             getComponent().removeFocusListener(_focusListener);
             _focusListener = null;
         }
     }
 
     /**
      * Adds the property change listener. The only property change event that will be fired is the "searchText"
      * property which will be fired when user types in a different search text in the popup.
      *
      * @param propertychangelistener
      */
     public void addPropertyChangeListener(PropertyChangeListener propertychangelistener) {
         _propertyChangeSupport.addPropertyChangeListener(propertychangelistener);
     }
 
     /**
      * Removes the property change listener.
      *
      * @param propertychangelistener
      */
     public void removePropertyChangeListener(PropertyChangeListener propertychangelistener) {
         _propertyChangeSupport.removePropertyChangeListener(propertychangelistener);
     }
 
     public void firePropertyChangeEvent(String searchingText) {
         if (!searchingText.equals(_previousSearchText)) {
             _propertyChangeSupport.firePropertyChange(PROPERTY_SEARCH_TEXT, _previousSearchText, searchingText);
             fireSearchableEvent(new SearchableEvent(Searchable.this, SearchableEvent.SEARCHABLE_CHANGE, searchingText, _previousSearchText));
             _previousSearchText = searchingText;
         }
     }
 
     /**
      * Checks if the element matches the searching text.
      *
      * @param element
      * @param searchingText
      * @return true if matches.
      */
     protected boolean compare(Object element, String searchingText) {
         String text = convertElementToString(element);
         return text != null && compare(isCaseSensitive() ? text : text.toLowerCase(), searchingText);
     }
 
     /**
      * Checks if the element string matches the searching text. Different from {@link #compare(Object,String)},
      * this method is after the element has been converted to string using {@link #convertElementToString(Object)}.
      *
      * @param text
      * @param searchingText
      * @return true if matches.
      */
     protected boolean compare(String text, String searchingText) {
         if (!isWildcardEnabled()) {
             return searchingText != null &&
                     (searchingText.equals(text) || searchingText.length() > 0 && text.startsWith(searchingText));
         }
         else {
             // if it doesn't have the two special characters we support, we don't need to use regular expression.
             int posAny = searchingText.indexOf('*');
             int posOne = searchingText.indexOf('?');
             // 
             if ((posAny == -1/* || posAny == searchingText.length() - 1*/) && (posOne == -1/* || posOne == searchingText.length() - 1*/)) {
                 return text.startsWith(searchingText);
             }
 
             // use the prvious pattern since nothing changed.
             if (_searchText != null && _searchText.equals(searchingText) && _pattern != null) {
                 return _pattern.matcher(text).find();
             }
 
             _searchText = searchingText;
             StringBuffer buffer = new StringBuffer();
             int length = searchingText.length();
             buffer.append('^');
             for (int i = 0; i < length; i++) {
                 char c = searchingText.charAt(i);
                 // replace '?' with '.'
                 if (c == '?') {
                     buffer.append(".");
                 }
                 else if (c == '*') {
                     buffer.append(".*");
                 }
                 else if ("(){}[].^$\\".indexOf(c) != -1) { // escape all other special characters
                     buffer.append('\\');
                     buffer.append(c);
                 }
                 else {
                     buffer.append(c);
                 }
             }
             try {
                 _pattern = Pattern.compile(buffer.toString(), isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE);
                 return _pattern.matcher(text).find();
             }
             catch (PatternSyntaxException e) {
                 return false;
             }
         }
     }
 
 
     /**
      * Gets the cursor which is the index of current location when searching. The value will be
      * used in findNext and findPrevious.
      *
      * @return the current position of the cursor.
      */
     public int getCursor() {
         return _cursor;
     }
 
     /**
      * Sets the cursor which is the index of current location when searching. The value will be
      * used in findNext and findPrevious.
      *
      * @param cursor the new position of the cursor.
      */
     public void setCursor(int cursor) {
         _cursor = cursor;
     }
 
     /**
      * Finds the next matching index from the cursor.
      *
      * @param s
      * @return the next index that the element matches the searching text.
      */
     public int findNext(String s) {
         String str = isCaseSensitive() ? s : s.toLowerCase();
         int count = getElementCount();
         if (count == 0)
             return s.length() > 0 ? -1 : 0;
         int selectedIndex = (_cursor != -1 ? _cursor : getSelectedIndex());
         for (int i = selectedIndex + 1; i < count; i++) {
             Object element = getElementAt(i);
             if (compare(element, str))
                 return i;
         }
 
         if (isRepeats()) {
             for (int i = 0; i < selectedIndex; i++) {
                 Object element = getElementAt(i);
                 if (compare(element, str))
                     return i;
             }
         }
 
         return selectedIndex == -1 ? -1 : (compare(getElementAt(selectedIndex), str) ? selectedIndex : -1);
     }
 
     /**
      * Finds the previous matching index from the cursor.
      *
      * @param s
      * @return the previous index that the element matches the searching text.
      */
     public int findPrevious(String s) {
         String str = isCaseSensitive() ? s : s.toLowerCase();
         int count = getElementCount();
         if (count == 0)
             return s.length() > 0 ? -1 : 0;
         int selectedIndex = (_cursor != -1 ? _cursor : getSelectedIndex());
         for (int i = selectedIndex - 1; i >= 0; i--) {
             Object element = getElementAt(i);
             if (compare(element, str))
                 return i;
         }
 
         if (isRepeats()) {
             for (int i = count - 1; i >= selectedIndex; i--) {
                 Object element = getElementAt(i);
                 if (compare(element, str))
                     return i;
             }
         }
         return selectedIndex == -1 ? -1 : (compare(getElementAt(selectedIndex), str) ? selectedIndex : -1);
     }
 
     /**
      * Finds the next matching index from the cursor. If it reaches the end, it will restart from the beginning.
      * However is the reverseOrder flag is true, it will finds the previous matching index from the cursor. If it reaches
      * the beginning, it will restart from the end.
      *
      * @param s
      * @return the next index that the element matches the searching text.
      */
     public int findFromCursor(String s) {
         if (isReverseOrder()) {
             return reverseFindFromCursor(s);
         }
 
         String str = isCaseSensitive() ? s : s.toLowerCase();
         int selectedIndex = (_cursor != -1 ? _cursor : getSelectedIndex());
         if (selectedIndex < 0)
             selectedIndex = 0;
         int count = getElementCount();
         if (count == 0)
             return -1; // no match
 
         // find from cursor
         for (int i = selectedIndex; i < count; i++) {
             Object element = getElementAt(i);
             if (compare(element, str))
                 return i;
         }
 
         // if not found, start over from the beginning
         for (int i = 0; i < selectedIndex; i++) {
             Object element = getElementAt(i);
             if (compare(element, str))
                 return i;
         }
 
         return -1;
     }
 
     /**
      * Finds the previous matching index from the cursor. If it reaches the beginning, it will restart from the end.
      *
      * @param s
      * @return the next index that the element matches the searching text.
      */
     public int reverseFindFromCursor(String s) {
         if (!isReverseOrder()) {
             return findFromCursor(s);
         }
 
         String str = isCaseSensitive() ? s : s.toLowerCase();
         int selectedIndex = (_cursor != -1 ? _cursor : getSelectedIndex());
         if (selectedIndex < 0)
             selectedIndex = 0;
         int count = getElementCount();
         if (count == 0)
             return -1; // no match
 
         // find from cursor to beginning
         for (int i = selectedIndex; i >= 0; i--) {
             Object element = getElementAt(i);
             if (compare(element, str))
                 return i;
         }
 
         // if not found, start over from the end
         for (int i = count - 1; i >= selectedIndex; i--) {
             Object element = getElementAt(i);
             if (compare(element, str))
                 return i;
         }
 
         return -1;
     }
 
     /**
      * Finds the first element that matches the searching text.
      *
      * @param s
      * @return the first element that matches with the searching text.
      */
     public int findFirst(String s) {
         String str = isCaseSensitive() ? s : s.toLowerCase();
         int count = getElementCount();
         if (count == 0)
             return s.length() > 0 ? -1 : 0;
 
         for (int i = 0; i < count; i++) {
             int index = getIndex(count, i);
             Object element = getElementAt(index);
             if (compare(element, str))
                 return index;
         }
 
         return -1;
     }
 
     /**
      * Finds the last element that matches the searching text.
      *
      * @param s
      * @return the last element that matches the searching text.
      */
     public int findLast(String s) {
         String str = isCaseSensitive() ? s : s.toLowerCase();
         int count = getElementCount();
         if (count == 0)
             return s.length() > 0 ? -1 : 0;
         for (int i = count - 1; i >= 0; i--) {
             Object element = getElementAt(i);
             if (compare(element, str))
                 return i;
         }
         return -1;
     }
 
     /**
      * This method is called when a key is typed or pressed.
      *
      * @param e the KeyEvent.
      */
     protected void keyTypedOrPressed(KeyEvent e) {
         if (_searchableProvider != null && _searchableProvider.isPassive()) {
             _searchableProvider.processKeyEvent(e);
             return;
         }
 
         if (isActivateKey(e)) {
             String searchingText = "";
             if (e.getID() == KeyEvent.KEY_TYPED) {
                 if (((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0)) { // if alt key is pressed
                     return;
                 }
                 if (e.isAltDown()) {
                     return;
                 }
 
                 searchingText = String.valueOf(e.getKeyChar());
             }
             showPopup(searchingText);
             if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                 e.consume();
             }
         }
     }
 
     private int getIndex(int count, int index) {
         return isReverseOrder() ? count - index - 1 : index;
     }
 
     /**
      * Shows the search popup. By default, the search popup will be visible
      * automatically when user types in the first key (in the case of JList, JTree, JTable)
      * or types in designated keystroke (in the case of JTextComponent). So this method is only
      * used when you want to show the popup manually.
      *
      * @param searchingText
      */
     public void showPopup(String searchingText) {
         if (_searchableProvider == null) {
             fireSearchableEvent(new SearchableEvent(this, SearchableEvent.SEARCHABLE_START, searchingText));
             showPopup(createSearchPopup(searchingText));
             _searchableProvider = new SearchableProvider() {
                 public String getSearchingText() {
                     return _popup != null ? _popup.getSearchingText() : "";
                 }
 
                 public boolean isPassive() {
                     return true;
                 }
 
                 public void processKeyEvent(KeyEvent e) {
                     if (_popup != null) {
                         _popup.processKeyEvent(e);
                     }
                 }
             };
         }
     }
 
     /**
      * Creates the popup to hold the searching text.
      *
      * @param searchingText
      * @return the searching popup.
      */
     protected SearchPopup createSearchPopup(String searchingText) {
         return new DefaultSearchPopup(searchingText);
     }
 
     /**
      * Gets the searching text.
      *
      * @return the searching text.
      */
     public String getSearchingText() {
         return _searchableProvider != null ? _searchableProvider.getSearchingText() : "";
     }
 
     private void showPopup(SearchPopup searchpopup) {
         JRootPane rootPane = _component.getRootPane();
         if (rootPane != null)
             _layeredPane = rootPane.getLayeredPane();
         else {
             _layeredPane = null;
         }
 
         if (_layeredPane == null || isHeavyweightComponentEnabled()) {
             _popup = searchpopup;
             Point location = updateSizeAndLocation();
             if (location != null) {
                 searchpopup.showPopup(location.x, location.y);
                 _popup.setVisible(true);
             }
             else {
                 _popup = null;
             }
         }
         else {
             if (_popup != null && _layeredPane != null) {
                 _layeredPane.remove(_popup);
                 _layeredPane.validate();
                 _layeredPane.repaint();
                 _layeredPane = null;
             }
             else if (!_component.isShowing())
                 _popup = null;
             else
                 _popup = searchpopup;
 
             if (_popup == null || !_component.isDisplayable())
                 return;
 
             if (_layeredPane == null) {
                 System.err.println("Failed to find layeredPane.");
                 return;
             }
 
             _layeredPane.add(_popup, JLayeredPane.POPUP_LAYER);
 
             updateSizeAndLocation();
             _popup.setVisible(true);
             _popup.validate();
         }
     }
 
     private Point updateSizeAndLocation() {
         Component component = getPopupLocationRelativeTo();
         if (component == null) {
             component = JideSwingUtilities.getScrollPane(_component);
         }
         if (component == null) {
             component = _component;
         }
 
         Point componentLocation;
         if (_popup != null) {
             Dimension size = _popup.getPreferredSize();
             switch (_popupLocation) {
                 case SwingConstants.BOTTOM:
                     try {
                         componentLocation = component.getLocationOnScreen();
                         componentLocation.y += component.getHeight();
                     }
                     catch (IllegalComponentStateException e) {
                         return null; // can't get the location so just return.
                     }
                     if (!isHeavyweightComponentEnabled()) {
                         SwingUtilities.convertPointFromScreen(componentLocation, _layeredPane);
                         if ((componentLocation.y + size.height > _layeredPane.getHeight())) {
                             componentLocation.y = _layeredPane.getHeight() - size.height;
                         }
                     }
                     break;
                 case SwingConstants.TOP:
                 default:
                     try {
                         componentLocation = component.getLocationOnScreen();
                     }
                     catch (IllegalComponentStateException e) {
                         return null; // can't get the location so just return.
                     }
                     if (!isHeavyweightComponentEnabled()) {
                         SwingUtilities.convertPointFromScreen(componentLocation, _layeredPane);
                     }
                     componentLocation.y -= size.height;
                     if ((componentLocation.y < 0)) {
                         componentLocation.y = 0;
                     }
                     break;
             }
            if (!isHeavyweightComponentEnabled()) {
                _popup.setLocation(componentLocation);
            }
             _popup.setSize(size);
             return componentLocation;
         }
         else {
             return null;
         }
     }
 
     /**
      * Checks if the key is used as a key to find the first occurence.
      *
      * @param e
      * @return true if the key in KeyEvent is a key to find the first occurence. By default, home key is used.
      */
     protected boolean isFindFirstKey(KeyEvent e) {
         return e.getKeyCode() == KeyEvent.VK_HOME;
     }
 
     /**
      * Checks if the key is used as a key to find the last occurence.
      *
      * @param e
      * @return true if the key in KeyEvent is a key to find the last occurence. By default, end key is used.
      */
     protected boolean isFindLastKey(KeyEvent e) {
         return e.getKeyCode() == KeyEvent.VK_END;
     }
 
     /**
      * Checks if the key is used as a key to find the previous occurence.
      *
      * @param e
      * @return true if the key in KeyEvent is a key to find the previous occurence. By default, up arrow key is used.
      */
     protected boolean isFindPreviousKey(KeyEvent e) {
         return e.getKeyCode() == KeyEvent.VK_UP;
     }
 
     /**
      * Checks if the key is used as a key to find the next occurence.
      *
      * @param e
      * @return true if the key in KeyEvent is a key to find the next occurence. By default, down arrow key is used.
      */
     protected boolean isFindNextKey(KeyEvent e) {
         return e.getKeyCode() == KeyEvent.VK_DOWN;
     }
 
     /**
      * Checks if the key is used as a navigation key. Navigation keys are keys which are used to
      * navigate to other occurences of the searching string.
      *
      * @param e
      * @return true if the key in KeyEvent is a navigation key.
      */
     protected boolean isNavigationKey(KeyEvent e) {
         return isFindFirstKey(e) || isFindLastKey(e) || isFindNextKey(e) || isFindPreviousKey(e);
     }
 
     /**
      * Checks if the key in KeyEvent should activate the search popup.
      *
      * @param e
      * @return true if the keyChar is a letter or a digit or '*' or '?'.
      */
     protected boolean isActivateKey(KeyEvent e) {
         char keyChar = e.getKeyChar();
         return e.getID() == KeyEvent.KEY_TYPED && (Character.isLetterOrDigit(keyChar) || keyChar == '*' || keyChar == '?');
     }
 
     /**
      * Checks if the key in KeyEvent should hide the search popup. If this method return true and
      * the key is not used for navigation purpose ({@link #isNavigationKey(java.awt.event.KeyEvent)} return false), the popup will be hidden.
      *
      * @param e
      * @return true if the keyCode in the KeyEvent is escape key, enter key,
      *         or any of the arrow keys such as page up, page down, home, end, left, right, up and down.
      */
     protected boolean isDeactivateKey(KeyEvent e) {
         int keyCode = e.getKeyCode();
         return keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE
                 || keyCode == KeyEvent.VK_PAGE_UP || keyCode == KeyEvent.VK_PAGE_DOWN
                 || keyCode == KeyEvent.VK_HOME || keyCode == KeyEvent.VK_END
                 || keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT
                 || keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN;
     }
 
     /**
      * Checks if the key will trigger selecting all.
      *
      * @param e
      * @return true if the key in KeyEvent is a key to trigger selecting all.
      */
     protected boolean isSelectAllKey(KeyEvent e) {
         return ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) && e.getKeyCode() == KeyEvent.VK_A;
     }
 
     /**
      * Checks if the key will trigger incremental selection.
      *
      * @param e
      * @return true if the key in KeyEvent is a key to trigger incremental selection. By default, ctrl down key is used.
      */
     protected boolean isIncrementalSelectKey(KeyEvent e) {
         return (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
     }
 
     /**
      * Gets the foreground color when the searching text doesn't match with any of the elements in the component.
      *
      * @return the forground color for mismatch. If you never call
      *         {@link #setMismatchForeground(java.awt.Color)}. red color will be used.
      */
     public Color getMismatchForeground() {
         if (_mismatchForeground == null) {
             return Color.RED;
         }
         else {
             return _mismatchForeground;
         }
     }
 
     /**
      * Sets the foreground for mismatch.
      *
      * @param mismatchForeground
      */
     public void setMismatchForeground(Color mismatchForeground) {
         _mismatchForeground = mismatchForeground;
     }
 
     /**
      * Checks if the case is sensitive during searching.
      *
      * @return true if the searching is case sensitive.
      */
     public boolean isCaseSensitive() {
         return _caseSensitive;
     }
 
     /**
      * Sets the case sensitive flag. By default, it's false meaning it's a case insensitive search.
      *
      * @param caseSensitive
      */
     public void setCaseSensitive(boolean caseSensitive) {
         _caseSensitive = caseSensitive;
     }
 
     /**
      * If it returns a positive number, it will wait for that many ms
      * before doing the search. When the searching is complex, this
      * flag will be useful to make the searching efficient. In the other words,
      * if user types in several keys very quickly, there will be only one search.
      * If it returns 0 or negative number, each key will generate a search.
      *
      * @return the number of ms delay before searching starts.
      */
     public int getSearchingDelay() {
         return _searchingDelay;
     }
 
     /**
      * If this flag is set to a positive number, it will wait for that many ms
      * before doing the search. When the searching is complex, this
      * flag will be useful to make the searching efficient. In the other words,
      * if user types in several keys very quickly, there will be only one search.
      * If this flag is set to 0 or a negative number, each key will generate a search with no delay.
      *
      * @param searchingDelay the number of ms delay before searching start.
      */
     public void setSearchingDelay(int searchingDelay) {
         _searchingDelay = searchingDelay;
     }
 
     /**
      * Checks if restart from the beginning when searching reaches the end
      * or restart from the end when reaches beginning. Default is false.
      *
      * @return true or false.
      */
     public boolean isRepeats() {
         return _repeats;
     }
 
     /**
      * Sets the repeat flag. By default, it's false meaning it will stop searching
      * when reaching the end or reaching the beginning.
      *
      * @param repeats
      */
     public void setRepeats(boolean repeats) {
         _repeats = repeats;
     }
 
     /**
      * Gets the foreground color used inn the search popup.
      *
      * @return the foreground. By default it will use the foreground of tooltip.
      */
     public Color getForeground() {
         if (_foreground == null) {
             return UIDefaultsLookup.getColor("ToolTip.foreground");
         }
         else {
             return _foreground;
         }
     }
 
     /**
      * Sets the foreground color used by popup.
      *
      * @param foreground
      */
     public void setForeground(Color foreground) {
         _foreground = foreground;
     }
 
     /**
      * Gets the background color used inn the search popup.
      *
      * @return the background. By default it will use the background of tooltip.
      */
     public Color getBackground() {
         if (_background == null) {
             return UIDefaultsLookup.getColor("ToolTip.background");
         }
         else {
             return _background;
         }
     }
 
     /**
      * Sets the background color used by popup.
      *
      * @param background
      */
     public void setBackground(Color background) {
         _background = background;
     }
 
     /**
      * Checks if it supports wildcard in searching text. By default it is true which means user can type
      * in "*" or "?" to match with any charactors or any charactor. If it's false, it will treat "*" or "?"
      * as a regular charactor.
      *
      * @return true if it supports wildcard.
      */
     public boolean isWildcardEnabled() {
         return _wildcardEnabled;
     }
 
     /**
      * Enable or disable the usage of wildcard.
      *
      * @param wildcardEnabled
      * @see #isWildcardEnabled()
      */
     public void setWildcardEnabled(boolean wildcardEnabled) {
         _wildcardEnabled = wildcardEnabled;
     }
 
     /**
      * Gets the current text that appears in the search popup. By default it is "Search for: ".
      *
      * @return the text that appears in the search popup.
      */
     public String getSearchLabel() {
         return _searchLabel;
     }
 
     /**
      * Sets the text that appears in the search popup.
      *
      * @param searchLabel
      */
     public void setSearchLabel(String searchLabel) {
         _searchLabel = searchLabel;
     }
 
     /**
      * Adds the specified listener to receive searchable events from this
      * searchable.
      *
      * @param l the searchable listener
      */
     public void addSearchableListener(SearchableListener l) {
         listenerList.add(SearchableListener.class, l);
     }
 
     /**
      * Removes the specified searchable listener so that it no longer
      * receives searchable events.
      *
      * @param l the searchable listener
      */
     public void removeSearchableListener(SearchableListener l) {
         listenerList.remove(SearchableListener.class, l);
     }
 
     /**
      * Returns an array of all the <code>SearchableListener</code>s added
      * to this <code>SearchableGroup</code> with
      * <code>addSearchableListener</code>.
      *
      * @return all of the <code>SearchableListener</code>s added or an empty
      *         array if no listeners have been added
      * @see #addSearchableListener
      * @since 1.4
      */
     public SearchableListener[] getSearchableListeners() {
         return (SearchableListener[]) listenerList.getListeners(SearchableListener.class);
     }
 
     /**
      * Fires a searchable event.
      *
      * @param e the event
      */
     protected void fireSearchableEvent(SearchableEvent e) {
         Object[] listeners = listenerList.getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == SearchableListener.class) {
                 ((SearchableListener) listeners[i + 1]).searchableEventFired(e);
             }
         }
     }
 
     /**
      * Gets the actual component which installed this Searchable.
      *
      * @return the actual component which installed this Searchable.
      */
     public Component getComponent() {
         return _component;
     }
 
     /**
      * Gets the popup location. It could be either {@link SwingConstants#TOP} or {@link SwingConstants#BOTTOM}.
      *
      * @return the popup location.
      */
     public int getPopupLocation() {
         return _popupLocation;
     }
 
     /**
      * Sets the popup location.
      *
      * @param popupLocation the popup location. The valid values are either {@link SwingConstants#TOP} or {@link SwingConstants#BOTTOM}.
      */
     public void setPopupLocation(int popupLocation) {
         _popupLocation = popupLocation;
     }
 
     public abstract class SearchPopup extends JidePopup {
         protected SearchField _textField;
 
         @Override
         public void processKeyEvent(KeyEvent e) {
             _textField.processKeyEvent(e);
             if (e.isConsumed()) {
                 String text = getSearchingText();
                 if (text.length() == 0) {
                     return;
                 }
 
                 if (isSelectAllKey(e)) {
                     selectAll(e, text);
                     return;
                 }
 
                 int found;
                 if (isFindPreviousKey(e)) {
                     found = findPrevious(text);
                     select(found, e, text);
                 }
                 else if (isFindNextKey(e)) {
                     found = findNext(text);
                     select(found, e, text);
                 }
                 else if (isFindFirstKey(e)) {
                     found = findFirst(text);
                     select(found, e, text);
                 }
                 else if (isFindLastKey(e)) {
                     found = findLast(text);
                     select(found, e, text);
                 }
 //                else {
 //                    found = findFromCursor(text);
 //                }
             }
             if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                 e.consume();
             }
         }
 
         private void selectAll(KeyEvent e, String text) {
             boolean oldReverseOrder = isReverseOrder(); // keep the old reverse order and we will set it back.
             if (oldReverseOrder) {
                 setReverseOrder(false);
             }
 
             int index = findFirst(text);
             if (index != -1) {
                 setSelectedIndex(index, false); // clear side effect of ctrl-a will select all items
                 _cursor = index; // as setSelectedIndex is used directly, we have to manually set the cursor value.
             }
 
 
             boolean oldRepeats = isRepeats(); // set repeats to false and set it back later.
             if (oldRepeats) {
                 setRepeats(false);
             }
 
             while (index != -1) {
                 int newIndex = findNext(text);
                 if (index == newIndex) {
                     index = -1;
                 }
                 else {
                     index = newIndex;
                 }
                 if (index == -1) {
                     break;
                 }
                 select(index, e, text);
             }
 
             if (oldRepeats) {
                 setRepeats(oldRepeats);
             }
 
             if (oldReverseOrder) {
                 setReverseOrder(oldReverseOrder);
             }
         }
 
         public String getSearchingText() {
             return _textField != null ? _textField.getText() : "";
         }
 
         abstract protected void select(int index, KeyEvent e, String searchingText);
     }
 
     /**
      * Checks the searching order. By default the searchable starts searching from top to bottom. If this flag
      * is false, it searchs from bottom to top.
      *
      * @return the reverseOrder flag.
      */
     public boolean isReverseOrder() {
         return _reverseOrder;
     }
 
     /**
      * Sets the searching order. By default the searchable starts searching from top to bottom. If this flag
      * is false, it searchs from bottom to top.
      *
      * @param reverseOrder
      */
     public void setReverseOrder(boolean reverseOrder) {
         _reverseOrder = reverseOrder;
     }
 
     /**
      * Gets the localized string from resource bundle. Subclass can override it to provide its own string.
      * Available keys are defined in swing.properties that begin with "Searchable.".
      *
      * @param key
      * @return the localized string.
      */
     protected String getResourceString(String key) {
         return Resource.getResourceBundle(Locale.getDefault()).getString(key);
     }
 
     /**
      * Check if the searchable popup is visible.
      *
      * @return true if visible. Otherwise, false.
      */
     public boolean isPopupVisible() {
         return _popup != null;
     }
 
     public boolean isHeavyweightComponentEnabled() {
         return _heavyweightComponentEnabled;
     }
 
     public void setHeavyweightComponentEnabled(boolean heavyweightComponentEnabled) {
         _heavyweightComponentEnabled = heavyweightComponentEnabled;
     }
 
 
     /**
      * Gets the component that the location of the popup relative to.
      *
      * @return the component that the location of the popup relative to.
      */
     public Component getPopupLocationRelativeTo() {
         return _popupLocationRelativeTo;
     }
 
     /**
      * Sets the location of the popup relative to the specified component. Then based on the value of
      * {@link #getPopupLocation()}. If you never set, we will use the searchable component or its scroll pane (if exists)
      * as the popupLocationRelativeTo component.
      *
      * @param popupLocationRelativeTo
      */
     public void setPopupLocationRelativeTo(Component popupLocationRelativeTo) {
         _popupLocationRelativeTo = popupLocationRelativeTo;
     }
 }

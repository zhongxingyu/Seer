 /**
  * 
  */
 package javax.swing.peckam;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.io.File;
 
 import javax.accessibility.AccessibleContext;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.event.EventListenerList;
 
 /**
  * Text input for selecting single files/directories (with the Browse... button)
  * 
  * @author Martin Pecka
  */
 public class JFileInput extends JComponent
 {
 
     /**
      * Needed for serialization
      */
     private static final long serialVersionUID = -5082644941943366256L;
 
     /**
      * The button for opening Browse dialog
      */
     protected final JButton   browseButton     = new JButton("Prochzet...");
 
     /**
      * @return the button for opening file chooser
      */
     public JButton getBrowseButton()
     {
         return browseButton;
     }
 
     /**
      * The File chooser used to select files
      */
     protected final JFileChooser fileChooser = new JFileChooser();
 
     /**
      * @return the filechooser used for selecting files
      */
     public JFileChooser getFileChooser()
     {
         return fileChooser;
     }
 
     /**
      * The text field containing the selected path
      */
     protected final JTextField textField = new JTextField();
 
     /**
      * @return the textfield used for displaying currently selected folder
      */
     public JTextField getTextField()
     {
         return textField;
     }
 
     // initializer
     {
         int hgap = UIManager.getLookAndFeel().getLayoutStyle()
                 .getPreferredGap(textField, fileChooser, ComponentPlacement.RELATED, SwingConstants.EAST, this);
         this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
         this.add(textField);
         this.add(Box.createRigidArea(new Dimension(hgap, getPreferredSize().height)));
         this.add(browseButton);
 
         fileChooser.setMultiSelectionEnabled(false);
 
         browseButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 File currentFile = new File(getPath());
                 if (currentFile.exists())
                     fileChooser.setCurrentDirectory(currentFile);
 
                 if (fileChooser.showDialog(null, null) == JFileChooser.APPROVE_OPTION) {
                     setPath(fileChooser.getSelectedFile().getPath());
                 }
             }
         });

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fileChooser.setCurrentDirectory(new File(textField.getText()));
                fireActionPerformed();
            }
        });
     }
 
     /**
      * Constructs a new <code>JFileInput</code>. The initial path is <code>null</code>, and the number of columns is set
      * to 0.
      */
     public JFileInput()
     {
         this(null, 0);
     }
 
     /**
      * Constructs a new empty <code>JFileInput</code> with the specified number
      * of columns. The initial path is set to <code>null</code>.
      * 
      * @param columns the number of columns to use to calculate the preferred
      *            width; if columns is set to zero, the preferred width will be
      *            whatever naturally results from the component implementation
      */
     public JFileInput(int columns)
     {
         this(null, columns);
     }
 
     /**
      * Constructs a new <code>JFileInput</code> initialized with the specified
      * path and columns.
      * 
      * @param path the path to be displayed, or <code>null</code>
      * @param columns the number of columns to use to calculate the preferred
      *            width; if columns is set to zero, the preferred width will be
      *            whatever naturally results from the component implementation
      */
     public JFileInput(String path, int columns)
     {
         textField.setColumns(columns);
         String safePath = path;
         if (path == null)
             safePath = "";
         setPath(safePath);
         fileChooser.setCurrentDirectory(new File(safePath));
     }
 
     /**
      * Constructs a new <code>JFileInput</code> initialized with the specified
      * path. The number of columns is 0.
      * 
      * @param path the text to be displayed, or <code>null</code>
      */
     public JFileInput(String path)
     {
         this(path, 0);
     }
 
     @Override
     public Dimension getPreferredSize()
     {
         int gap = UIManager.getLookAndFeel().getLayoutStyle()
                 .getPreferredGap(textField, browseButton, ComponentPlacement.RELATED, SwingConstants.EAST, this);
 
         return new Dimension(textField.getPreferredSize().width + gap + browseButton.getPreferredSize().width,
                 Math.max(textField.getPreferredSize().height, browseButton.getPreferredSize().height));
     }
 
     /**
      * @return the currently selected path
      */
     public String getPath()
     {
         return textField.getText();
     }
 
     /**
      * Sets the currently active path
      * 
      * @param path The new path to set
      */
     public void setPath(String path)
     {
         String safePath = path;
         if (path == null)
             safePath = "";
         textField.setText(safePath);
         fileChooser.setCurrentDirectory(new File(safePath));
         fireActionPerformed();
     }
 
     @Override
     public void setEnabled(boolean enabled)
     {
         textField.setEnabled(enabled);
         browseButton.setEnabled(enabled);
     }
 
     @Override
     public AccessibleContext getAccessibleContext()
     {
         return textField.getAccessibleContext();
     }
 
     /**
      * Returns the number of columns in this <code>FileInput</code>.
      * 
      * @return the number of columns >= 0
      */
     public int getColumns()
     {
         return textField.getColumns();
     }
 
     /**
      * Sets the number of columns in this <code>FileInput</code>, and then
      * invalidate the layout.
      * 
      * @param columns the number of columns >= 0
      * @exception IllegalArgumentException if <code>columns</code> is less than
      *                0
      */
     public void setColumns(int columns)
     {
         int oldVal = getColumns();
         if (columns < 0) {
             throw new IllegalArgumentException("columns less than zero.");
         }
         if (columns != oldVal) {
             textField.setColumns(columns);
             invalidate();
         }
     }
 
     @Override
     public void setFont(Font f)
     {
         textField.setFont(f);
     }
 
     @Override
     public String getToolTipText(MouseEvent event)
     {
         return textField.getToolTipText(event);
     }
 
     /**
      * Returns the boolean indicating whether this <code>FileInput</code> is
      * editable or not.
      * 
      * @return the boolean value
      * @see #setEditable
      */
     public boolean isEditable()
     {
         return textField.isEditable();
     }
 
     /**
      * Sets the specified boolean to indicate whether or not this <code>FileInput</code> should be editable.
      * 
      * @param b the boolean to be set
      * @see #isEditable
      */
     public void setEditable(boolean b)
     {
         textField.setEditable(b);
         browseButton.setEnabled(b);
     }
 
     @Override
     public void setToolTipText(String text)
     {
         super.setToolTipText(text);
         textField.setToolTipText(text);
         browseButton.setToolTipText(text);
     }
 
     /**
      * Adds the specified action listener to receive action events from this input.
      * 
      * @param l The action listener to be added.
      */
     public synchronized void addActionListener(ActionListener l)
     {
         listenerList.add(ActionListener.class, l);
     }
 
     /**
      * Removes the specified action listener so that it no longer receives action events from this input.
      * 
      * @param l The action listener to be removed.
      */
     public synchronized void removeActionListener(ActionListener l)
     {
         listenerList.remove(ActionListener.class, l);
     }
 
     /**
      * Returns an array of all the {@link ActionListener}s added to this JFileInput with
      * {@link #addActionListener(ActionListener)}
      * 
      * @return All of the {@link ActionListener}s added or an empty array if no listeners have been added.
      * @since 1.4
      */
     public synchronized ActionListener[] getActionListeners()
     {
         return listenerList.getListeners(ActionListener.class);
     }
 
     /**
      * Notifies all listeners that have registered interest for notification on this event type. 
      * 
      * The listener list is processed in last to first order.
      * 
      * @see EventListenerList
      */
     protected void fireActionPerformed()
     {
         // Guaranteed to return a non-null array
         ActionListener[] listeners = getActionListeners();
         ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "action");
 
         // Process the listeners last to first, notifying those that are interested in this event
         for (int i = listeners.length - 1; i >= 0; i--) {
                 listeners[i].actionPerformed(e);
         }
     }
 }

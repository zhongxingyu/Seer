 /*
  * Framework written by Brad Johnson
  * NextBooks
  * 2011-2012
  */
 
 package gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import javax.swing.JButton;
 import javax.swing.border.EmptyBorder;
 
 /**
  * A special extension of JButton with several style modifications and
  * modified behaviour to work specifically with the NextBooks GUI.
  * @author Brad
  *
  */
 public class Tab extends JButton {
 
     /**
      *
      */
     private static final long serialVersionUID = 6303977417629057524L;
 
     /**
      * The width of the border which wraps around each tab.
      */
     private static final int TABBORDERWIDTH = 10;
 
     /**
      * The standard width of each tab.
      */
     private static final int TABWIDTH = 120;
 
     /**
      * The standard height of each tab.
      */
     private static final int TABHEIGHT = 40;
 
     /**
      * Constructor for functional tab.
      * @param title - the label to display on the tab
      * @param intention - the panel that this button will cause to appear in
      * the main
      * panel spot of the GUI, using the ints defined in PanelsManager.java
      */
     public Tab(final String title, final int intention) {
 
         super(title);
 
         /*
          * All tabs are 120 wide by 40 tall
          */
        this.setPreferredSize(new Dimension(TABWIDTH, TABHEIGHT));
 
         /*
          * All buttons default to unselectedBlue (defined in PanelsManager)
          * The color will change when the button is selected (see
          * TabActionListener)
          */
         this.setBackground(PanelsManager.UNSELECTEDBLUE);
 
         /*
          * This gets ride of the border that shows up around the label when
          * clicked
          */
         this.setFocusPainted(false);
 
         /*
          * Text color is white
          */
         this.setForeground(Color.WHITE);
 
         /*
          * invisible border
          */
         this.setBorder(new EmptyBorder(TABBORDERWIDTH, TABBORDERWIDTH,
                 TABBORDERWIDTH, TABBORDERWIDTH));
 
         /*
          * This constructor is used for functional buttons, so yes they are
          * enabled
          */
         this.setEnabled(true);
 
         /*
          * Change the GUI's display panel to 'intention' and recolor this
          * button
          * See TabActionListener.java
          */
         this.addActionListener(new TabActionListener(intention, this));
 
     }
 
     /**
      * Constructor for non-functional tabs, tabs which take up space but don't
      * do anything when clicked.
      * @param title - the label to display on the tab
      */
     public Tab(final String title) {
 
         super(title);
 
         /*
          * All tabs are 120 wide by 40 tall
          */
        this.setPreferredSize(new Dimension(TABWIDTH, TABHEIGHT));
 
         /*
          * All non-functional tabs are "background blue", this doesn't change
          */
         this.setBackground(PanelsManager.BACKGROUNDBLUE);
 
         /*
          * Label is white
          */
         this.setForeground(Color.WHITE);
 
         /*
          * Invisible border
          */
         this.setBorder(new EmptyBorder(TABBORDERWIDTH, TABBORDERWIDTH,
                 TABBORDERWIDTH, TABBORDERWIDTH));
 
         /*
          * non-functional tabs are not clickable
          */
         this.setEnabled(false);
 
     }
 
 }

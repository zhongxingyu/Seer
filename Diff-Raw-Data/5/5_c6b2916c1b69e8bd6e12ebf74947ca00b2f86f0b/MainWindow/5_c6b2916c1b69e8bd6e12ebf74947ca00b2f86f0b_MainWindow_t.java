 package soundfun.ui;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import net.miginfocom.swing.MigLayout;
 
 /*
  * This class is NOT to be considered part of the API at this stage.
  * 
  */
 public class MainWindow {	
 	private Frame mMainWindowFrame = new Frame();
 	private Panel mTopPanel = new Panel(new MigLayout("fill"));
 	private Panel mBottomPanel = new Panel(new MigLayout("fill"));
 	
 	// The three visible panels as the user knows them.
 	private Panel mButtonPanel = new Panel(new MigLayout("fill"));
 	private Panel mActionPanel = new Panel(new MigLayout("fill"));
 	private Panel mActionDetailsPanel = new Panel(new MigLayout("fill"));
 	
 	/*
 	 * Listbox for the Action panel.
 	 */
 	private ListPanel mActionListPanel = new ListPanel();
 	private javax.swing.JScrollPane mActionScrollPane = new javax.swing.JScrollPane();
 	private ListModel mActionListModel = mActionListPanel.getListModel();
 	
 	/*
 	 * Listbox for the Button panel.
 	 */
 	private ListPanel mButtonListPanel = new ListPanel();
 	private javax.swing.JScrollPane mButtonScrollPane = new javax.swing.JScrollPane();
 	private ListModel mButtonListModel = mButtonListPanel.getListModel();
 	
 	public ListPanel getActionListPanel() {
 		return mActionListPanel;
 	}
 	
 	public ListPanel getButtonListPanel() {
 		return mButtonListPanel;
 	}
 	
 	/**
      * Creates new form MainFrame
      */
     public MainWindow() {
     	///mMainWindowFrame.getContentPane().setLayout(new javax.swing.BoxLayout(mMainWindowFrame.getContentPane(), javax.swing.BoxLayout.Y_AXIS));
     	mMainWindowFrame.getContentPane().setLayout(new MigLayout("fill"));
     	mMainWindowFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
     	
     	/*
     	 * Define the outer-most layout. This layout
     	 * splits the frames along the x-axis.
     	 */
     	///GridBagConstraints frameConstraints = new GridBagConstraints();
     	///frameConstraints.fill = GridBagConstraints.HORIZONTAL;
     	///frameConstraints.gridx = 0;
     	///frameConstraints.gridy = 0;
     	///frameConstraints.weightx = 1.0;
     	///mMainWindowFrame.add(mTopPanel, frameConstraints);
     	mMainWindowFrame.add(mTopPanel, "w 100%, wrap, grow");
     	
     	///frameConstraints.fill = GridBagConstraints.HORIZONTAL;
     	///frameConstraints.gridx = 0;
     	///frameConstraints.gridy = 1;
     	///mMainWindowFrame.add(mBottomPanel, frameConstraints);
     	mMainWindowFrame.add(mBottomPanel, "w 100%, grow");
     	
     	/*
     	 * Arrange the panels for the top panel.
     	 * There are 2 panels here: the button
     	 * panel that corresponds with the device,
     	 * and the action selection list box.
     	 */
     	///FlowLayout layout = new FlowLayout();
     	///mTopPanel.setLayout(layout);
     	///layout.setAlignment(FlowLayout.CENTER);
     	///layout.layoutContainer(mTopPanel);
     	mTopPanel.add(mButtonPanel, "w 50%, h 50%, grow");
     	
     	///mTopPanel.add(Box.createHorizontalGlue());
     	
     	///layout.setAlignment(FlowLayout.CENTER);
     	///layout.layoutContainer(mTopPanel);
     	mTopPanel.add(mActionPanel, "w 50%, h 50%, grow");
     	
     	/*
     	 * Attach the action details panel, which is normally
     	 * manipulated by whichever plugin is currently selected.
     	 * mActionDetailsPanel should fill mBottomPanel.
     	 */
    	mBottomPanel.add(mActionDetailsPanel, "w 100%, h 100%, grow 100");
     	
     	
     	mButtonPanel.add(mButtonListPanel);
     	mActionPanel.add(mActionListPanel);
     	
     	mMainWindowFrame.setTitle("SoundFun");
     	
     	mMainWindowFrame.setMinimumSize(new Dimension(800, 600));
     	mMainWindowFrame.setPreferredSize(new Dimension(800, 600));
     	mMainWindowFrame.setMaximumSize(new Dimension(Short.MAX_VALUE,
                                           Short.MAX_VALUE));
     	
     	/*
     	 * Setup for the action panel (top-right).
     	 */
     	mActionListPanel.getList().setModel(mActionListModel);
         mActionScrollPane.setViewportView(mActionListPanel.getList());
         mActionPanel.add(mActionScrollPane, "w 100%, h 100%");
         mActionPanel.add(mActionListPanel);
         
         /*
          * Setup for the button panel (top-left).
          */
         mButtonListPanel.getList().setModel(mButtonListModel);
         mButtonScrollPane.setViewportView(mButtonListPanel.getList());
         mButtonPanel.add(mButtonScrollPane, "w 100%, h 100%");
         mButtonPanel.add(mButtonListPanel);
         
         /****************************
          * COMPONENT DIMENSIONS
          * (PANELS, ETC.)
          ****************************/
         
         Dimension halfSizeBoth = new Dimension(
         		mMainWindowFrame.getSize().width / 2 - 50,
         		mMainWindowFrame.getSize().height / 2 - 50
         		);
         
         ///mBottomPanel.setMinimumSize(halfSizeY);
         ///mBottomPanel.setPreferredSize(halfSizeY);
        /// mBottomPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,
         ///                                  Short.MAX_VALUE));
         
         ///mActionDetailsPanel.setMinimumSize(halfSizeY);
         ///mActionDetailsPanel.setPreferredSize(halfSizeY);
         ///mActionDetailsPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,
         ///                                  Short.MAX_VALUE));
         
        /// mTopPanel.setMinimumSize(halfSizeY);
        /// mTopPanel.setPreferredSize(new Dimension(Short.MAX_VALUE,
        ///         Short.MAX_VALUE));
        /// mTopPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,
         ///                                  Short.MAX_VALUE));
         /*
         mActionPanel.setMinimumSize(halfSizeBoth);
         mActionPanel.setPreferredSize(halfSizeBoth);
         mActionPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,
                                           Short.MAX_VALUE));
         
         mButtonListPanel.setMinimumSize(halfSizeBoth);
         mButtonListPanel.setPreferredSize(halfSizeBoth);
         mButtonListPanel.setMaximumSize(halfSizeBoth);
         */
         mButtonScrollPane.setMinimumSize(halfSizeBoth);
         //mButtonScrollPane.setPreferredSize(halfSizeBoth);
         //mButtonScrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE,
          //                                 Short.MAX_VALUE));
 /*
         mButtonPanel.setMinimumSize(halfSizeBoth);
         mButtonPanel.setPreferredSize(halfSizeBoth);
         mButtonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,
                                           Short.MAX_VALUE));
         
         
         
         /*
         mActionListPanel.setMinimumSize(halfSizeBoth);
         mActionListPanel.setPreferredSize(halfSizeBoth);
         mActionListPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,
                                           Short.MAX_VALUE));
                                          */
         
         mActionScrollPane.setMinimumSize(halfSizeBoth);
         //mActionScrollPane.setPreferredSize(halfSizeBoth);
        // mActionScrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE,
         //                                  Short.MAX_VALUE));
     	
     	mMainWindowFrame.pack();
     	mMainWindowFrame.setVisible(true);
     }
     
     /*
      * Retrieve the action details panel.
      */
     public javax.swing.JPanel getActionDetailsPanel() {
 		return mActionDetailsPanel;
 	}
     
     /*
      * Set the current active ActionDetailsPanel object.
      * This is similar to a Swing CardLayout, except this
      * method is probably much less efficient.
      */
     public void setActionDetailsPanel(Panel panel) throws Exception {
     	boolean found = false;
     	for(Component c : mActionDetailsPanel.getComponents()) {
     		// Disable and hide all child components first.
     		c.setVisible(false);
     		c.setEnabled(false);
     		if(panel == c) {
     			// If the component already exists as a child of
     			// mActionDetailsPanel, set it to be visible/enabled.
     			found = true;
     			c.setVisible(true);
     			c.setEnabled(true);
     		} else {
     			mActionDetailsPanel.remove(c);
     		}
     	}
     	
     	// If the component does not already exist as a child,
     	// set it to be a child, and set it as visible/enabled.
     	if(!found) {
     		
    		mActionDetailsPanel.add(panel, "w 100%, h 100%");
     		panel.setVisible(true);
     		panel.setEnabled(true);
     	}
     	
     	// Restore validity.
     	mMainWindowFrame.validate();
     	//mMainWindowFrame.repaint(50L);
     }
 }

 package testing.hello_world;
 
 import java.awt.GridLayout;
 
 import javax.swing.JPanel;
 
 /**
  * The Class buttonVComp. creates a JPannel of lightButtons in a grid.
  */
 public class buttonVComp extends JPanel {
 	
 	/** The I size of the array. */
 	private int mIval;
 	
 	/**
 	 * Gets the I size of the array.
 	 *
 	 * @return the I size of the array
 	 */
 	public int getmIval() {
 		return mIval;
 	}
 
 	/**
 	 * Sets the I size of the array.
 	 *
 	 * @param mIval the new I size of the array
 	 */
 	public void setmIval(int mIval) {
 		this.mIval = mIval;
 	}
 
 	/** The J size of the array. */
 	private int mJval;
 	
 	/**
 	 * Gets the J size of the array.
 	 *
 	 * @return the J size of the array
 	 */
 	public int getmJval() {
 		return mJval;
 	}
 
 	/**
 	 * Sets J size of the array.
 	 *
 	 * @param mJval the new J size of the array
 	 */
 	public void setmJval(int mJval) {
 		this.mJval = mJval;
 	}
 	private buttonArray mButtons;
 
 	/** The Constant serialVersionUID. */
 	private static final long serialVersionUID = 8250550689045737102L;
 
 	private static final int HEADER_OFFSET = 6;
 	
 	/** The layout manager. */
 	private GridLayout mLayoutManager;
 	
 	/**
 	 * Instantiates a new button visual comp.
 	 *
 	 * @param ival the I size of the array
 	 * @param jval the J size of the array
 	 */
 	public buttonVComp(int ival, int jval) {
 		this.mIval = ival;
 		this.mJval = jval;
 		// create a pane of buttons
 	    mButtons = new buttonArray(ival,jval);
 	    mButtons.initArray();
 
 	    this.mLayoutManager = new GridLayout(ival,jval);
 	    this.setLayout(this.mLayoutManager);
 	    mButtons.randomiseButtons();
 	    mButtons.addToFrame(this);
 	}
 
 	/**
 	 * Redraw buttons and randomises.
 	 */
 	public void redrawButtons(){
 		this.removeAll();
 		mButtons = new buttonArray(mIval,mJval);
 		mButtons.initArray();
 	    mButtons.randomiseButtons();
 	    mButtons.addToFrame(this);
 	    this.mLayoutManager.setColumns(mJval);
 	    this.mLayoutManager.setRows(mIval);
 	    this.repaint();
 	    this.revalidate();
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.awt.Component#toString()
 	 * Prints the string that represents the mButtons array contained on this component
 	 */
 	@Override
 	public String toString(){
 		return this.mButtons.toString();
 	}
 	
 	/**
 	 * Load a button array from string. Allows saveing of the game
	 * an example  of a load string of a "+" in the middle of a 5x5 game is: 05:05:0000000100011100010000000
	 * an example of a random 5x4 load string is: 05:04	:11111011111100101111
 	 *
 	 * @param loadString the string from which to load should take the form of "%2iSize:%2jSize:[0|1]..."
	 *
 	 */
 	public void loadFromString(String loadString) {
 		int iVal = Integer.parseInt((String.valueOf(loadString.charAt(0))).toString()+(String.valueOf(loadString.charAt(1))).toString());
 		int jVal = Integer.parseInt((String.valueOf(loadString.charAt(3))).toString()+(String.valueOf(loadString.charAt(4))).toString());
 		//TODO throw exception if iVal*jVal != loadString legnth + HEADER_OFFSET (+1)
 		for(int j = 0; j < iVal;j++){
 			for(int i = 0; i < jVal;i++)
 			//System.out.print(loadString.charAt(i) +"["+ i + "]]");
			mButtons.setButtonAt(i,j,Integer.parseInt(String.valueOf(loadString.charAt(i*iVal+j+HEADER_OFFSET+1))));
 		}
 		this.revalidate();
 	}
 	
 }

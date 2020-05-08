 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2008 Nigel Westbury <westbury@users.sf.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.entrytable;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.jmoney.model2.DataManager;
 import net.sf.jmoney.model2.IPropertyControl;
 import net.sf.jmoney.model2.SessionChangeListener;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * This is used in rows only, not for headers.
  * (anything to be gained by using it in headers too????)
  */
 public class StackControl<T, R> extends Composite implements IPropertyControl<T> {
 
 	private RowControl rowControl;
 	
 	private R coordinator;
 	
 	private StackBlock<T, R> stackBlock;
 	
 	/**
 	 * Maps each child block to the child control of the stack composite
 	 * that shows that block in the header.
 	 */
 	private Map<Block, CellContainer<T,R>> childControls = new HashMap<Block, CellContainer<T,R>>();
 
 	private CompressedStackLayout stackLayout;
 	
 	// TODO decide if the stack control can take null, causing it to be
 	// blank.
 //	private Composite blankControl = null;
 	
 	private T entryData;
 
 	/* Listen for changes that might affect the top block.
 	 * Changes may originate from within this row control, or the
 	 * changes may come from other parts that have committed changes
 	 * and those changes are then seen in this transaction.
 	 */
 	private SessionChangeListener transactionChangeListener = null;
 
 	public StackControl(Composite parent, RowControl rowControl, R coordinator, StackBlock<T, R> stackBlock) {
 		super(parent, SWT.NONE);
 		this.rowControl = rowControl;
 		this.coordinator = coordinator;
 		this.stackBlock = stackBlock;
 		
 		stackLayout = new CompressedStackLayout();
 		setLayout(stackLayout);
 	}
 
 	/**
 	 * This method determines the top control in the stack and loads the
 	 * input only for that top control.  If another control should be bought
 	 * to the top for whatever reason then that control is loaded at that
 	 * time.
 	 * <P>
 	 * This is important not only for performance reasons but also because
 	 * controls inside a stack control can assume that they will only be loaded
 	 * if visible, thus saving those controls from needing to check that they
 	 * are applicable.
 	 *  
 	 * Derived classes should override this method and add code to
 	 * maintain change listeners.  The change listeners should??? 
 	 */
 	public void load(final T entryData) {
 		// TODO: this should be done in a 'row release' method??
 		if (this.entryData != null) {
 			DataManager dataManager = stackBlock.getDataManager(this.entryData);
 			dataManager.removeChangeListener(transactionChangeListener);
 		}
 		
 		this.entryData = entryData;
 
 		// Note that setting the top block will also load it.
 		Block<? super T, ? super R> topBlock = stackBlock.getTopBlock(entryData);
 		setTopBlock(topBlock);
 
 		DataManager dataManager = stackBlock.getDataManager(entryData);
 		transactionChangeListener = stackBlock.createListener(entryData, this);
 		dataManager.addChangeListener(transactionChangeListener);
 	}
 
 	public void save() {
 		// TODO Do we need to pass this on to the child controls?
 	}
 
 	/**
 	 * Set the control (associated with the given block) to
 	 * the top of the stack, creating it if necessary, and always
 	 * loading it.
 	 * <P>
 	 * This method may change the preferred height of the row.  It is
 	 * the caller's responsibility to resize the row to its preferred
 	 * height after calling this method.
 	 * 
 	 * @param topBlock
 	 */
 	public void setTopBlock(Block<? super T, ? super R> topBlock) {
 		// First set the top control in this row
 		CellContainer<T,R> topControl;
 		if (topBlock == null) {
 //			if (blankControl == null) {
 //				blankControl = new Composite(this, SWT.NULL);
 //			}
 //			topControl = blankControl;
 			topControl = null;  // Causes nothing to show in stacked composite
 		} else {
 			topControl = childControls.get(topBlock); 
 			if (topControl == null) {
 				topControl = new CellContainer<T,R>(this, SWT.NULL);
 				final BlockLayout<T> childLayout = new BlockLayout<T>(topBlock, false);
 				topControl.setLayout(childLayout);
 				
 				topControl.init(rowControl, coordinator, topBlock);
 				
 				final Composite finalTopControl = topControl;
 				topControl.addPaintListener(new PaintListener() {
 					public void paintControl(PaintEvent e) {
 						Color oldColor = e.gc.getBackground();
 						// TODO: move colors to single location
 						Color secondaryColor = Display.getDefault()
 						.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
 
 						try {
 							e.gc.setBackground(secondaryColor);
 							childLayout.paintRowLines(e.gc, finalTopControl);
 						} finally {
 							e.gc.setBackground(oldColor);
 						}
 					}
 				});
 
 				childControls.put(topBlock, topControl);
 			}
 			topControl.setInput(entryData);
 		}
 		stackLayout.topControl = topControl;
 		
 		// Fire event that tells header to change
 		if (rowControl instanceof BaseEntryRowControl
 				&& ((BaseEntryRowControl)rowControl).isSelected()) {
 			stackBlock.setTopHeaderBlock(topBlock, entryData);
 		}
 	}
 
 	public Control getControl() {
 		return this;
 	}
 
 	public void setFocusListener(FocusListener controlFocusListener) {
 		// TODO Auto-generated method stub
 		
 	}
 }

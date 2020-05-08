 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2007 Nigel Westbury <westbury@users.sf.net>
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
 
 import net.sf.jmoney.model2.Entry;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 /**
  * This class represents a block that is used to display the fields from the
  * other entries in the transaction.
  * 
  * If this is a simple transaction (one other entry and that entry is an
  * income/expense account) then the properties are displayed in place.
  * 
  * If this is a split transaction then the words '--split--' is displayed with
  * a drop-down button.
  * 
  * If this is a transfer then this appears the same as a simple transaction
  * except that the account is in square brackets.
  * 
  * @author Nigel Westbury
  */
 public class OtherEntriesBlock extends CellBlock<EntryData, EntryRowControl> {
 
 	final static int DROPDOWN_BUTTON_WIDTH = 15;
 	
 	private Block<Entry, SplitEntryRowControl> otherEntriesRootBlock;
 	
 	public OtherEntriesBlock(Block<Entry, SplitEntryRowControl> otherEntriesRootBlock) {
 		super(
 				otherEntriesRootBlock.minimumWidth + DROPDOWN_BUTTON_WIDTH,
 				otherEntriesRootBlock.weight
 		);
 		
 		this.otherEntriesRootBlock = otherEntriesRootBlock;
 	}
 
     @Override	
 	public ICellControl<EntryData> createCellControl(EntryRowControl parent) {
 		
 	    /*
 	     * Use a single row tracker and cell focus tracker for this
 	     * table.  This needs to be generalized for, say, the reconciliation
 	     * editor if there is to be a single row selection for both tables.
 	     */
 		// TODO: This is not right - should not be created here.
 	    RowSelectionTracker<SplitEntryRowControl> rowTracker = new RowSelectionTracker<SplitEntryRowControl>();
 	    FocusCellTracker cellTracker = new FocusCellTracker();
 
 		final OtherEntriesControl control = new OtherEntriesControl(parent, otherEntriesRootBlock, rowTracker, cellTracker);
 		
 		return new ICellControl<EntryData>() {
 			public Control getControl() {
 				return control;
 			}
 			public void load(EntryData data) {
 				control.load(data);
 			}
 			public void save() {
 				control.save();
 			}
 			public void setFocusListener(FocusListener controlFocusListener) {
 				// TODO Auto-generated method stub
 			}
 		};
 	}
 
 	@Override
 	public void createHeaderControls(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		
		// TODO: remove this..
//		otherEntriesRootBlock.buildCellList();

 		BlockLayout layout = new BlockLayout(otherEntriesRootBlock, true);
 		composite.setLayout(layout);
 
 		otherEntriesRootBlock.createHeaderControls(composite);
 	}
 	
 	@Override
 	void layout(int width) {
 		this.width = width;
 		
 		/*
 		 * This control has a drop-down button to the right of the cells in this
 		 * control. We therefore must substact the width of the button in order
 		 * to get the width into which the child cells must fit.
 		 */
 		otherEntriesRootBlock.layout(width - DROPDOWN_BUTTON_WIDTH);
 	}
 }

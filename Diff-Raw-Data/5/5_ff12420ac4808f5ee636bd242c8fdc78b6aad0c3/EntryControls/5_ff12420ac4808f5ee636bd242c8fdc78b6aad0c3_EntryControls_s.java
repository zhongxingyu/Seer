 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2005 Johann Gyger <jgyger@users.sf.net>
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
 
 package net.sf.jmoney.pages.entries;
 
 import java.util.Vector;
 
 import net.sf.jmoney.fields.EntryInfo;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.CapitalAccount;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.IPropertyControl;
 import net.sf.jmoney.model2.IncomeExpenseAccount;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 import net.sf.jmoney.model2.SessionChangeListener;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Represents the set of controls for a single entry in the
  * transaction.  These controls form one (or maybe more) rows
  * of controls in a colored row across the grid.
  * 
  * @author Nigel Westbury
  */
 class EntryControls {
 	/**
 	 * 
 	 */
 	private Color entryColor;
 	
 	/** The controls that fill each grid item in the entries grid. */
 	Composite composite1;
 	private Composite composite2;
 	private Composite composite3;
 	private Composite composite4;
 	private Composite composite5;
 	
 	private Label debitLabel;
 	private Text debitText;
 	private Label creditLabel;
 	private Text creditText;
 	
 	Vector<LabelAndEditControlPair> entryPropertyControls = new Vector<LabelAndEditControlPair>();
 	
 	abstract class LabelAndEditControlPair {
 		private Label label;
 		IPropertyControl propertyControl;
 		ScalarPropertyAccessor propertyAccessor;
 		Composite pairComposite;
 		
 		/**
 		 * Creates both a label and an edit control.
 		 * 
 		 * Adds both to the propertyControls array so that they can
 		 * later be disposed (they will be disposed if the account or
 		 * some other property is changed so that this property is
 		 * no longer applicable).
 		 * <P>
 		 * The caller must call the <code>IPropertyControl.load()</code>
 		 * method to either set the entry object or to disable the
 		 * control.
 		 * @param propertyLabel
 		 * @param propertyControl
 		 */
 		public LabelAndEditControlPair(ScalarPropertyAccessor propertyAccessor) {
 			this.propertyAccessor = propertyAccessor;
 			
 			/*
 			 * The controls for this property pair are created when an entry is set.  We know at
 			 * that time whether this property is applicable for the entry. 
 			 */
 			pairComposite = null;
 		}
 		
 		void dispose() {
 			pairComposite.dispose();
 		}
 		
 		/**
 		 * @param entry
 		 */
 		public void load(Entry entry) {
 			propertyControl.load(entry);
 		}
 		
 		/**
 		 * @param entry
 		 * @param isEntryChanging true if this method is being called because
 		 * 			a different Entry is being shown in the control, false if
 		 * 			the entry has not changed but a property in the entry has
 		 * 			changed and that property may affect whether this property
 		 * 			is applicable
 		 */
 		public void refreshState(final Entry entry, boolean isEntryChanging) {
 			Account account = entry.getAccount();
 			Session session = entry.getSession();
 			boolean isApplicable = isApplicable(account);
 			
 			// Controls with the visability set to false still
 			// take up space in the grid.  We must dispose controls
 			// if they do not apply.
 			if (isApplicable) {
 				boolean mustLoadEntry = isEntryChanging;
 				
 				if (pairComposite == null) {
 					pairComposite = new Composite(composite1, 0);
 					pairComposite.setBackground(entryColor);
 					RowLayout layout = new RowLayout();
 					layout.marginTop = 0;
 					layout.marginBottom = 0;
 					layout.marginLeft = 0;
 					layout.marginRight = 0;
 					pairComposite.setLayout(layout);
 					
 					label = new Label(pairComposite, 0);
 					label.setText(propertyAccessor.getDisplayName() + ':');
 					label.setBackground(entryColor);
 					propertyControl = propertyAccessor.createPropertyControl(pairComposite, session);
 
 					RowData controlLayoutData = new RowData();
 					controlLayoutData.width = propertyAccessor.getMinimumWidth();
 					propertyControl.getControl().setLayoutData(controlLayoutData);
 					
 					propertyControl.getControl().addFocusListener(
 							new PropertyControlFocusListener(propertyAccessor, propertyControl) {
 								ExtendableObject getExtendableObject() {
 									return entry;
 								}
 							});
 					
 					mustLoadEntry = true;
 				}
 				
 				if (mustLoadEntry) {
 					load(entry);
 				}
 				
 			} else {
 				if (pairComposite != null) {
 					pairComposite.dispose();
 					pairComposite = null;
 				}
 			}
 		}
 		
 		void setBackground(Color color) {
 			entryColor = color;
 			if (pairComposite != null) {
 				label.setBackground(color);
 			}
 		}
 		abstract class PropertyControlFocusListener extends FocusAdapter {
 			
 			private ScalarPropertyAccessor propertyAccessor;
 			private IPropertyControl propertyControl;
 			
 			// When a control gets the focus, save the old value here.
 			// This value is used in the change message.
 			private String oldValueText;
 			
 			
 			PropertyControlFocusListener(ScalarPropertyAccessor propertyAccessor, IPropertyControl propertyControl) {
 				this.propertyAccessor = propertyAccessor;
 				this.propertyControl = propertyControl;
 			}
 			
 			public void focusLost(FocusEvent e) {
 				ExtendableObject object = getExtendableObject();
 				
 				if (object.getObjectKey().getSessionManager().isSessionFiring()) {
 					return;
 				}
 				
 				propertyControl.save();
 				
 /* We need to decide at what granularity we want to support undo/redo.
  * Every individual property change may be too small, so remove this.
 
 				String newValueText = propertyAccessor.formatValueForMessage(object);
 				
 				String description = 
 					"change " + propertyAccessor.getDisplayName() + " property"
 					+ " from " + oldValueText
 					+ " to " + newValueText;
 				
 				object.getSession().registerUndoableChange(description);
 */				
 			}
 			public void focusGained(FocusEvent e) {
 				// Save the old value of this property for use in our 'undo' message.
 				ExtendableObject object = getExtendableObject();
 				oldValueText = propertyAccessor.formatValueForMessage(object);
 			}
 			
 			abstract ExtendableObject getExtendableObject();
 		};    
 
 		abstract boolean isApplicable(Account account);
 	}
 	
     /** 
      * The entry object being displayed in the set of controls.
 	 */
 	Entry entry;
 
 	private Composite entriesArea;
 	private Currency defaultCurrency;
 
 	private SessionChangeListener mySessionChangeListener = new SessionChangeAdapter() {
 		public void objectChanged(ExtendableObject changedObject, ScalarPropertyAccessor changedProperty, Object oldValue, Object newValue) {
 			if (changedObject.equals(entry)) {
 
 				/*
 				 * Ultimately we may have a mechanism whereby the determination of whether a
 				 * property is applicable may depend on any number of other properties, not
 				 * just the account property.  This may be particularly so when stock accounts
 				 * are supported.  Until then, assume the applicability of properties will change
 				 * only when the account changes.
 				 */
 				// TODO: improve this.
 				if (changedProperty == EntryInfo.getAccountAccessor()) {
 					updateSetOfEntryControls();
 				}
 
 				// Regardless of the property changed, re-load the control.
 				for (int i = 0; i < entryPropertyControls.size(); i++) {
 					LabelAndEditControlPair controlPair = (LabelAndEditControlPair)entryPropertyControls.get(i);
 					if (controlPair.propertyAccessor == changedProperty) {
 						if (controlPair.pairComposite != null) {
 							controlPair.load(entry);
 						}
 					}
 				}
 			}
 		}
 	};
 
 	
 	/**
 	 * This object represents a row of controls that allow
 	 * editing of the properties for an Entry in the transaction.
 	 * Constructing this object also constructs the controls.
      * @param defaultCurrency 
 	 */
 	EntryControls(Session session, Composite entriesArea, final Entry entry, Color entryColor, Currency defaultCurrency) {
 		this.entriesArea = entriesArea;
 		this.entryColor = entryColor;
 		this.defaultCurrency = defaultCurrency;
 		this.entry = null;
 		
 		composite1 = new Composite(entriesArea, 0);
 		composite2 = new Composite(entriesArea, 0);
 		composite3 = new Composite(entriesArea, 0);
 		composite4 = new Composite(entriesArea, 0);
 		composite5 = new Composite(entriesArea, 0);
 		
 		// First column (the one with the dynamic rows of property control pairs)
 		// grabs excess width.
 		GridData column1Data = new GridData(GridData.FILL_BOTH);
 		column1Data.grabExcessHorizontalSpace = true;
 		composite1.setLayoutData(column1Data);
 	
 		RowLayout layout1 = new RowLayout(SWT.HORIZONTAL);
 		layout1.spacing = 5;
 		composite1.setLayout(layout1);
 		
 		RowLayout layout2 = new RowLayout();
 		layout2.marginHeight = 2;
 		layout2.marginWidth = 2;
 		composite2.setLayout(layout2);
 		composite3.setLayout(layout2);
 		composite4.setLayout(layout2);
 		composite5.setLayout(layout2);
 		
 		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
 		gd.widthHint = 100;
 		composite1.setLayoutData(gd);
 		composite1.setBackground(entryColor);
 		composite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		composite2.setBackground(entryColor);
 		composite3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		composite3.setBackground(entryColor);
 		composite4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		composite4.setBackground(entryColor);
 		composite5.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		composite5.setBackground(entryColor);
 		
 		// If no account is set yet in the entry then use the "Income"
 		// and "Expense" labels, because it is more likely that the account
 		// will be an income/expense account than a capital account.
 		debitLabel = new Label(composite2, 0);
 		debitText = new Text(composite3, 0);
 		RowData debitLayoutData = new RowData();
 		debitLayoutData.width = 50;
 		debitText.setLayoutData(debitLayoutData);
 		creditLabel = new Label(composite4, 0);
 		creditText = new Text(composite5, 0);
 		RowData creditLayoutData = new RowData();
 		creditLayoutData.width = 50;
 		creditText.setLayoutData(creditLayoutData);
 		
 		debitLabel.setBackground(entryColor);
 		creditLabel.setBackground(entryColor);
 		
 		debitText.addFocusListener(
 				new FocusAdapter() {
 					public void focusLost(FocusEvent e) {
 						// We need a currency so that we can format the amount.
 						// Get the currency from this entry if possible.
 						// However, the user may not have yet entered enough information
 						// to determine the currency for this entry, in which case
 						// use the currency that was given to us as the default currency
 						// for this page.
 						Commodity commodityForFormatting = entry.getCommodity();
 						if (commodityForFormatting == null) {
 							commodityForFormatting = EntryControls.this.defaultCurrency;
 						}
 						
 						String amountString = debitText.getText();
 						long amount = commodityForFormatting.parse(amountString);
 						if (amount != 0) {
 							entry.setAmount(-amount);
 							debitText.setText(commodityForFormatting.format(amount));
 							// When a debit is entered, clear out any credit.
 							creditText.setText("");
 						} else {
 							if (creditText.getText().equals("")) { 
 								entry.setAmount(0);
 							}
 							debitText.setText("");
 						}
 					}
 				});
 		
 		creditText.addFocusListener(
 				new FocusAdapter() {
 					public void focusLost(FocusEvent e) {
 						// We need a currency so that we can format the amount.
 						// Get the currency from this entry if possible.
 						// However, the user may not have yet entered enough information
 						// to determine the currency for this entry, in which case
 						// use the currency that was given to us as the default currency
 						// for this page.
 						Commodity commodityForFormatting = entry.getCommodity();
 						if (commodityForFormatting == null) {
 							commodityForFormatting = EntryControls.this.defaultCurrency;
 						}
 						
 						String amountString = creditText.getText();
 						long amount = commodityForFormatting.parse(amountString);
 						if (amount != 0) {
 							entry.setAmount(amount);
 							creditText.setText(commodityForFormatting.format(amount));
 							// When a debit is entered, clear out any credit.
 							debitText.setText("");
 						} else {
 							if (debitText.getText().equals("")) { 
 								entry.setAmount(0);
 							}
 							creditText.setText("");
 						}
 					}
 				});
 		
 		// The account combo is always applicable, and must come
 		// first, so add this first specifically.
 		entryPropertyControls.add(
 				new LabelAndEditControlPair(EntryInfo.getAccountAccessor()) {
 					boolean isApplicable(Account account) {
 						return true;
 					}
 				});
 		
 		// The other controls depend on the type of account.
 		// This needs to be generalized in the metadata, but until
 		// that work is done, the description applies to entries in
 		// income/expense accounts and all other properties apply
 		// to capital accounts.
 		entryPropertyControls.add(
 				new LabelAndEditControlPair(EntryInfo.getDescriptionAccessor()) {
 					boolean isApplicable(Account account) {
 						return account instanceof IncomeExpenseAccount;
 					}
 				});
 		
 		
 		entryPropertyControls.add(
 				new LabelAndEditControlPair(EntryInfo.getIncomeExpenseCurrencyAccessor()) {
 					boolean isApplicable(Account account) {
 						return account instanceof IncomeExpenseAccount
 							&& ((IncomeExpenseAccount)account).isMultiCurrency();
 					}
 				});
 
    		for (ScalarPropertyAccessor propertyAccessor: EntryInfo.getPropertySet().getScalarProperties3()) {
 			if (propertyAccessor.isEditable()
					&& propertyAccessor.isScalar()
 					&& propertyAccessor != EntryInfo.getAccountAccessor() 
 					&& propertyAccessor != EntryInfo.getAmountAccessor()
 					&& propertyAccessor != EntryInfo.getDescriptionAccessor()
 					&& propertyAccessor != EntryInfo.getIncomeExpenseCurrencyAccessor()) {
 				entryPropertyControls.add(
 						new LabelAndEditControlPair(propertyAccessor) {
 							boolean isApplicable(Account account) {
 								return account instanceof CapitalAccount;
 							}
						});
 			}
 		}
 		
 		// Listen for changes to the account selection.
 		// This changes the set of properties to be shown
 		// for this entry.
 
 		// Note: composite1 is used as the containing composite,
 		// even tho there are in fact 5 composites across the row.
 		// This is ok because they are all destroyed at the same time.
 		session.getObjectKey().getSessionManager().addChangeListener(mySessionChangeListener, composite1);
 		
 		// if the entry is known at construct time.  Set it
 		// into the controls.
 		if (entry != null) {
 			setEntry(entry);
 		}
 	}
 	
 	/**
 	 * @param entry The entry to show.  Cannot be null - 
 	 * 			if no entry is selected then call the
 	 * 			showDisabledControls method.
 	 */
 	public void setEntry(final Entry entry) {
 		this.entry = entry;
 		
 		for (int i = 0; i < entryPropertyControls.size(); i++) {
 			LabelAndEditControlPair controlPair = (LabelAndEditControlPair)entryPropertyControls.get(i);
 			controlPair.refreshState(entry, true);
 		}
 		
 		updateAmountLabels();
 
 		// Set the amount in the credit and debit controls.
 		long amount = entry.getAmount();
 		
 		// We need a currency so that we can format the amount.
 		// Get the currency from this entry if possible.
 		// However, the user may not have yet entered enough information
 		// to determine the currency for this entry, in which case
 		// use the currency that was given to us as the default currency
 		// for this page.
 		Commodity commodityForFormatting = entry.getCommodity();
 		if (commodityForFormatting == null) {
 			commodityForFormatting = defaultCurrency;
 		}
 		
 		if (amount > 0) {
 			creditText.setText(commodityForFormatting.format(amount));
 		} else {
 			creditText.setText("");
 		}
 		if (amount < 0) {
 			debitText.setText(commodityForFormatting.format(-amount));
 		} else {
 			debitText.setText("");
 		}
 		
 		// Labels may have changed.
 		composite2.layout();
 		composite4.layout();
 		
 //		entrySection.layoutSection();
 		composite1.layout();
 		entriesArea.layout();
 		}
 	
 	/**
 	 * Some entry properties may be inapplicable, depending on the
 	 * value of other properties.  This method is called when the value
 	 * of a property changes and the change may make other properties
 	 * applicable or inapplicable.
 	 * 
 	 * This method is called when the row of controls for an entry are
 	 * first constructed, and also called when the user changes the
 	 * account selected in the entry.
 	 * 
 	 * The set of applicable entry properties depends on the account
 	 * set in the entry.
 	 * 
 	 * @param account If null then no controls are added beyond the
 	 * 			account list control.
 	 */
 	void updateSetOfEntryControls() {
 		for (int i = 1; i < entryPropertyControls.size(); i++) {
 			LabelAndEditControlPair controlPair = (LabelAndEditControlPair)entryPropertyControls.get(i);
 			controlPair.refreshState(entry, false);
 		}
 		
 		updateAmountLabels();
 		
 		// As controls may have been added or removed, a re-layout
 		// is required.
 		composite1.layout();
 		entriesArea.layout();
 	}
 	
 	void updateAmountLabels() {
 		Account account = entry.getAccount();
 		
 		// The choice of labels for the two amount controls also depend
 		// on the type of account that is selected.
 		String debitText = "";
 		String creditText = "";
 		if (account != null) {
 			if (account instanceof IncomeExpenseAccount) {
 				debitText = "Income:";
 				creditText = "Expense:";
 			} else {
 				debitText = "Debit:";
 				creditText = "Credit:";
 			}
 		}
 		debitLabel.setText(debitText);
 		creditLabel.setText(creditText);
 		
 		// These must be re-layed out so that the next text displays.
 		composite2.layout();
 		composite4.layout();
 	}
 	
 	/**
 	 * Set the backgroud color of the controls for this entry. 
 	 * Although the color is passed to the constructor, the color may need to
 	 * be flipped when an entry is deleted from the transaction dialog.
 	 * 
 	 * @param color
 	 */
 	public void setColor(Color entryColor) {
 		this.entryColor = entryColor;
 		
         composite1.setBackground(entryColor);
         composite2.setBackground(entryColor);
         composite3.setBackground(entryColor);
         composite4.setBackground(entryColor);
         composite5.setBackground(entryColor);
         debitLabel.setBackground(entryColor);
         creditLabel.setBackground(entryColor);
 		for (int i = 0; i < entryPropertyControls.size(); i++) {
 			LabelAndEditControlPair controlPair = (LabelAndEditControlPair)entryPropertyControls.get(i);
 			controlPair.setBackground(entryColor);
 		}
 	}
 	
 	void dispose() {
 		composite1.dispose();
 		composite2.dispose();
 		composite3.dispose();
 		composite4.dispose();
 		composite5.dispose();
 	}
 
 	// Not sure if we need this.  It may be better just to
 	// destroy the second line if it is not needed.
 	void setVisible(boolean visible) {
 		composite1.setVisible(visible);
 		composite2.setVisible(visible);
 		composite3.setVisible(visible);
 		composite4.setVisible(visible);
 		composite5.setVisible(visible);
 	}
 }

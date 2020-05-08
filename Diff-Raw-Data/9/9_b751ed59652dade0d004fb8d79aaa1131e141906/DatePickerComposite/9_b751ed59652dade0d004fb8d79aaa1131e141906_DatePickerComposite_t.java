 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.util.Util;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Composite of a DatePicker and a SWT Text widget.
  * 
  * @since 1.2
  */
 public class DatePickerComposite extends Composite {
 
 	private final static int BUTTON_WIDTH = 16;
 	private final static int BUTTON_HEIGHT = 16;
 
 	private final Text textfield;
 	private final Button pickerButton;
 	private final DatePicker datePicker;
 	private IDateConverterStrategy dateConverterStrategy;
 
 	public DatePickerComposite(Composite parent, int textStyles) {
 		super(parent, SWT.BORDER);
 
 		GridLayoutFactory.fillDefaults().numColumns(2).spacing(0, 0).applyTo(this);
 		textfield = new Text(this, checkStyle(textStyles));
 		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(textfield);
 
 		pickerButton = new Button(this, SWT.ARROW | SWT.DOWN);
 		pickerButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (!datePicker.isVisible()) {
 					Point p = textfield.toDisplay(textfield.getLocation().x, textfield.getLocation().y);
 					datePicker.setLocation(p.x, p.y + textfield.getBounds().height);
 					datePicker.open(parseDate(textfield.getText()));
 				} else {
 					datePicker.close();
 				}
 			}
 		});
 
 		GridDataFactory.fillDefaults().grab(false, false).align(SWT.RIGHT, SWT.FILL).hint(BUTTON_WIDTH, BUTTON_HEIGHT)
 				.applyTo(pickerButton);
 		datePicker = new DatePicker(this);
 		dateConverterStrategy = new RegexDateConverterStrategy(textfield);
 	}
 
 	public IDateConverterStrategy getDateConverterStrategy() {
 		return dateConverterStrategy;
 	}
 
 	public void setDateConverterStrategy(IDateConverterStrategy dateConverterStrategy) {
 		this.dateConverterStrategy = dateConverterStrategy;
 	}
 
 	public Text getTextfield() {
 		return textfield;
 	}
 
 	@Override
 	public void dispose() {
 		datePicker.dispose();
 		super.dispose();
 	}
 
 	// helping methods
 	//////////////////
 
 	/**
 	 * Removes the {@link SWT.BORDER} style, to prevent a awkward representation
 	 * 
 	 * @param style
 	 *            the SWT style bits
 	 * @return the style bits without SWT.BORDER
 	 */
 	private int checkStyle(int style) {
 		if ((style & SWT.BORDER) != 0) {
 			style &= ~SWT.BORDER;
 		}
 		return style;
 	}
 
 	private Button getPickerButton() {
 		return pickerButton;
 	}
 
 	private Calendar parseDate(String dateString) {
 		Calendar result = null;
 		Date date = getDateConverterStrategy().getDateFromTextField(dateString);
 		if (date != null) {
 			result = Calendar.getInstance();
 			result.setTime(date);
 		}
 		return result;
 	}
 
 	// helping classes
 	//////////////////
 
 	/**
 	 * Strategy for converting between {@link String} and {@link Date}
 	 */
 	public static interface IDateConverterStrategy {
 		/**
 		 * Parses the given date and sets it to the textfield
 		 * 
 		 * @param date
 		 */
 		void setDateToTextField(Date date);
 
 		/**
 		 * Converts the given dateString to a {@link Date}
 		 * 
 		 * @param dateString
 		 * @return
 		 */
 		Date getDateFromTextField(String dateString);
 	}
 
 	/**
 	 * This class shows and hides a DateTime "date picker" on request.
 	 */
 	private static final class DatePicker {
 
 		/**
 		 * Height of the widget's header on windows. Don't close when clicked
 		 * there.
 		 */
 		private static final int WIN32_CALENDAR_HEADER_HEIGHT = 45;
 
 		private Shell shell;
 		private DateTime calendar;
 		private DatePickerComposite datePicker;
 
 		/**
 		 * Create a new DatePicker instance.
 		 * <p>
 		 * You must invoke {@link #dispose()} to give up native resources held
 		 * by this class.
 		 * 
 		 * @param text
 		 *            a SWT text field that will received the picked date.
 		 * @param pickerButton
 		 *            the button that will trigger showing the date picker.
 		 */
 		protected DatePicker(final DatePickerComposite datePicker) {
 			this.datePicker = datePicker;
 			shell = new Shell(datePicker.getPickerButton().getDisplay(), SWT.NO_TRIM | SWT.ON_TOP);
 			shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
 			GridLayoutFactory.fillDefaults().margins(1, 1).applyTo(shell);
 
 			calendar = new DateTime(shell, SWT.CALENDAR | SWT.SHORT);
 			calendar.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
 			shell.pack();
 
 			calendar.addMouseListener(new MouseAdapter() {
 				/*
 				 * On windows the Calendar widget has a header that has a
 				 * zoomOut / zoomIn ability. We need to keep count of the clicks
 				 * needed until we can close the widget (i.e. last zoom in
 				 * level). See Bug 288354, comment #4, point #3.
 				 */
				int win32clicks = init();
 
 				@Override
 				public void mouseUp(MouseEvent e) {
 					if (e.button != 1) {
 						return;
 					}
 					if (Util.isWin32()) {
 						if (e.y < WIN32_CALENDAR_HEADER_HEIGHT) {
 							win32clicks = Math.min(4, win32clicks + 1);
 						} else {
 							win32clicks = Math.max(win32clicks - 1, 0);
 						}
 					}
 					if (win32clicks == 0) {
 						Calendar cal = Calendar.getInstance();
 						cal.set(Calendar.YEAR, calendar.getYear());
 						cal.set(Calendar.MONTH, calendar.getMonth());
 						cal.set(Calendar.DAY_OF_MONTH, calendar.getDay());
 
 						setDateToTextfield(cal.getTime());
						win32clicks = init();
 						close();
 					}
 				}

				private int init() {
					return Util.isWin32() ? 1 : 0;
				}
 			});
 
 			calendar.addFocusListener(new FocusAdapter() {
 				@Override
 				public void focusLost(FocusEvent e) {
 					Display display = e.widget.getDisplay();
 					Control focusControl = display.getCursorControl();
 					if (focusControl != datePicker.getPickerButton()) {
 						close();
 					}
 				}
 			});
 		}
 
 		public void dispose() {
 			if (!SwtUtilities.isDisposed(shell)) {
 				shell.dispose();
 			}
 		}
 
 		public void setLocation(int x, int y) {
 			shell.setLocation(x, y);
 		}
 
 		public boolean isVisible() {
 			return shell.isVisible();
 		}
 
 		public void close() {
 			shell.setVisible(false);
 		}
 
 		public void open(Calendar newDate) {
 			if (isVisible()) {
 				return;
 			}
 
 			shell.open();
 			shell.setVisible(true);
 
 			// if no date was supplied, use the current date
 			if (null == newDate) {
 				newDate = Calendar.getInstance();
 				newDate.setTime(new Date());
 			}
 
 			calendar.setYear(newDate.get(Calendar.YEAR));
 			calendar.setMonth(newDate.get(Calendar.MONTH));
 			calendar.setDay(newDate.get(Calendar.DAY_OF_MONTH));
 		}
 
 		private void setDateToTextfield(Date date) {
 			datePicker.getDateConverterStrategy().setDateToTextField(date);
 		}
 	}
 
 	/**
 	 * Default implementation for a {@link IDateConverterStrategy} that will be
 	 * used, when no other implementation was supplied. This implementation
 	 * tries to parse the Date with a Regular Expression, but does only support
 	 * simple DateFormats like "dd.MM.yyyy" and "dd.MM.yyyy HH:mm"
 	 */
 	private static class RegexDateConverterStrategy implements IDateConverterStrategy {
 
 		private Text textfield;
 
 		public RegexDateConverterStrategy(Text textfield) {
 			this.textfield = textfield;
 		}
 
 		public void setDateToTextField(Date date) {
 			String dateString = new SimpleDateFormat("dd.MM.yyyy").format(date); //$NON-NLS-1$
 
 			String oldText = textfield.getText();
 
 			if (oldText.contains(":")) { //$NON-NLS-1$
 				Pattern pattern = Pattern.compile("([ \\d\\.]+)\\s+(.*?:.*?)"); //$NON-NLS-1$
 				Matcher matcher = pattern.matcher(oldText);
 
 				if (matcher.matches()) {
 					String oldTime = matcher.group(2);
 					textfield.setText(dateString + " " + oldTime); //$NON-NLS-1$
 				}
 			} else {
 				textfield.setText(dateString);
 			}
 			textfield.setFocus();
 		}
 
 		public Date getDateFromTextField(String dateString) {
 			Calendar result = null;
 			Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+).*?"); //$NON-NLS-1$
 			Matcher matcher = pattern.matcher(dateString);
 
 			if (matcher.matches() && matcher.groupCount() == 3) {
 				int month = Integer.parseInt(matcher.group(2));
 				month -= 1;
 
 				int year = Integer.parseInt(matcher.group(3));
 				if (year < 100) {
 					year += 1900;
 				}
 
 				result = Calendar.getInstance();
 				result.set(Calendar.DAY_OF_MONTH, Integer.parseInt(matcher.group(1)));
 				result.set(Calendar.MONTH, month);
 				result.set(Calendar.YEAR, year);
 				return result.getTime();
 			}
 			return null;
 		}
 	}
 }

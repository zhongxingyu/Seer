 /*
  * $Id$
  *
  * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
  * Santa Clara, California 95054, U.S.A. All rights reserved.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jdesktop.swingx;
 
 import org.jdesktop.swingx.calendar.DateSpan;
 import org.jdesktop.swingx.calendar.JXMonthView;
 import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;
 import org.jdesktop.swingx.plaf.DatePickerUI;
 import org.jdesktop.swingx.plaf.JXDatePickerAddon;
 import org.jdesktop.swingx.plaf.LookAndFeelAddons;
 
 import javax.swing.*;
 import javax.swing.JFormattedTextField.AbstractFormatter;
 import javax.swing.JFormattedTextField.AbstractFormatterFactory;
 import javax.swing.text.DefaultFormatterFactory;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 /**
  * A component that combines a button, an editable field and a JXMonthView
  * component.  The user can select a date from the calendar component, which
  * appears when the button is pressed.  The selection from the calendar
  * component will be displayed in editable field.  Values may also be modified
  * manually by entering a date into the editable field using one of the
  * supported date formats.
  *
  * @author Joshua Outwater
  */
 public class JXDatePicker extends JComponent {
 
     static {
       LookAndFeelAddons.contribute(new JXDatePickerAddon());
     }
 
     /**
      * UI Class ID
      */
     public static final String uiClassID = "DatePickerUI";
 
     public static final String EDITOR = "editor";
     public static final String MONTH_VIEW = "monthView";
     public static final String DATE_IN_MILLIS ="dateInMillis";
     public static final String LINK_PANEL = "linkPanel";
 
     /** The editable date field that displays the date */
     private JFormattedTextField _dateField;
 
     /**
      * Popup that displays the month view with controls for
      * traversing/selecting dates.
      */
     private JPanel _linkPanel;
     private long _linkDate;
     private MessageFormat _linkFormat;
     private JXMonthView _monthView;
     private String _actionCommand = "selectionChanged";
     private boolean editable = true;
 
     /**
      * Create a new date picker using the current date as the initial
      * selection and the default abstract formatter
      * <code>JXDatePickerFormatter</code>.
      *
      * The date picker is configured with the default time zone and locale
      *
      * @see #setTimeZone
      * @see #getTimeZone
      */
     public JXDatePicker() {
         this(System.currentTimeMillis());
     }
 
     /**
      * Create a new date picker using the specified time as the initial
      * selection and the default abstract formatter
      * <code>JXDatePickerFormatter</code>.
      *
      * The date picker is configured with the default time zone and locale
      *
      * @param millis initial time in milliseconds
      * @see #setTimeZone
      * @see #getTimeZone
      */
     public JXDatePicker(long millis) {
         _monthView = new JXMonthView();
         _monthView.setTraversable(true);
 
         _linkFormat = new MessageFormat(UIManager.getString("JXDatePicker.linkFormat"));
 
         _linkDate = System.currentTimeMillis();
         _linkPanel = new TodayPanel();
         
         updateUI();
 
         _dateField.setValue(new Date(millis));
     }
 
     /**
      * @inheritDoc
      */
     public DatePickerUI getUI() {
         return (DatePickerUI)ui;
     }
 
     /**
      * Sets the L&F object that renders this component.
      *
      * @param ui
      */
     public void setUI(DatePickerUI ui) {
         super.setUI(ui);
     }
 
     /**
      * Resets the UI property with the value from the current look and feel.
      *
      * @see UIManager#getUI
      */
     @Override
     public void updateUI() {
         setUI((DatePickerUI)UIManager.getUI(this));
         invalidate();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public String getUIClassID() {
         return uiClassID;
     }
 
     /**
      * Replaces the currently installed formatter and factory used by the
      * editor.  These string formats are defined by the
      * <code>java.text.SimpleDateFormat</code> class.
      *
      * @param formats The string formats to use.
      * @see java.text.SimpleDateFormat
      */
     public void setFormats(String[] formats) {
         DateFormat[] dateFormats = new DateFormat[formats.length];
         for (int counter = formats.length - 1; counter >= 0; counter--) {
             dateFormats[counter] = new SimpleDateFormat(formats[counter]);
         }
         setFormats(dateFormats);
     }
 
     /**
      * Replaces the currently installed formatter and factory used by the
      * editor.
      *
      * @param formats The date formats to use.
      */
     public void setFormats(DateFormat[] formats) {
         _dateField.setFormatterFactory(new DefaultFormatterFactory(
                                 new JXDatePickerFormatter(formats)));
     }
 
     /**
      * Returns an array of the formats used by the installed formatter
      * if it is a subclass of <code>JXDatePickerFormatter<code>.
      * <code>javax.swing.JFormattedTextField.AbstractFormatter</code>
      * and <code>javax.swing.text.DefaultFormatter</code> do not have
      * support for accessing the formats used.
      *
      * @return array of formats or null if unavailable.
      */
     public DateFormat[] getFormats() {
         // Dig this out from the factory, if possible, otherwise return null.
         AbstractFormatterFactory factory = _dateField.getFormatterFactory();
         if (factory != null) {
             AbstractFormatter formatter = factory.getFormatter(_dateField);
             if (formatter instanceof JXDatePickerFormatter) {
                 return ((JXDatePickerFormatter)formatter).getFormats();
             }
         }
         return null;
     }
 
     /**
      * Set the currently selected date.
      *
      * @param date date
      */
     public void setDate(Date date) {
         _dateField.setValue(date);
     }
 
     /**
      * Set the currently selected date.
      *
      * @param millis milliseconds
      */
     public void setDateInMillis(long millis) {
         _dateField.setValue(new Date(millis));
     }
 
     /**
      * Returns the currently selected date.
      *
      * @return Date
      */
     public Date getDate() {
         return (Date)_dateField.getValue();
     }
 
     /**
      * Returns the currently selected date in milliseconds.
      *
      * @return the date in milliseconds
      */
     public long getDateInMillis() {
         return ((Date)_dateField.getValue()).getTime();
     }
 
     /**
      * Return the <code>JXMonthView</code> used in the popup to
      * select dates from.
      *
      * @return the month view component
      */
     public JXMonthView getMonthView() {
         return _monthView;
     }
 
     /**
      * Set the component to use the specified JXMonthView.  If the new JXMonthView
      * is configured to a different time zone it will affect the time zone of this
      * component.
      *
      * @param monthView month view comopnent
      * @see #setTimeZone
      * @see #getTimeZone
      */
     public void setMonthView(JXMonthView monthView) {
         JXMonthView oldMonthView = _monthView;
         _monthView = monthView;
         firePropertyChange(MONTH_VIEW, oldMonthView, _monthView);
     }
 
     /**
      * Gets the time zone.  This is a convenience method which returns the time zone
      * of the JXMonthView being used.
      *
      * @return The <code>TimeZone</code> used by the <code>JXMonthView</code>.
      */
     public TimeZone getTimeZone() {
         return _monthView.getTimeZone();
     }
 
     /**
      * Sets the time zone with the given time zone value.    This is a convenience
      * method which returns the time zone of the JXMonthView being used.
      *
      * @param tz The <code>TimeZone</code>.
      */
     public void setTimeZone(TimeZone tz) {
         _monthView.setTimeZone(tz);
 
     }
 
     public long getLinkDate() {
         return _linkDate;
     }
 
     /**
      * Set the date the link will use and the string defining a MessageFormat
      * to format the link.  If no valid date is in the editor when the popup
      * is displayed the popup will focus on the month the linkDate is in.  Calling
      * this method will replace the currently installed linkPanel and install
      * a new one with the requested date and format.
      *
      * @param linkDate Date in milliseconds
      * @param linkFormatString String used to format the link
      * @see java.text.MessageFormat
      */
     public void setLinkDate(long linkDate, String linkFormatString) {
         _linkDate = linkDate;
         _linkFormat = new MessageFormat(linkFormatString);
         setLinkPanel(new TodayPanel());
     }
     
     /**
      * Return the panel that is used at the bottom of the popup.  The default
      * implementation shows a link that displays the current month.
      *
      * @return The currently installed link panel
      */
     public JPanel getLinkPanel() {
         return _linkPanel;
     }
     
     /**
      * Set the panel that will be used at the bottom of the popup.
      *
      * @param linkPanel The new panel to install in the popup
      */
     public void setLinkPanel(JPanel linkPanel) {
         JPanel oldLinkPanel = _linkPanel;
         _linkPanel = linkPanel;
         firePropertyChange(LINK_PANEL, oldLinkPanel, _linkPanel);
     }
     
     /**
      * Returns the formatted text field used to edit the date selection.
      *
      * @return the formatted text field
      */
     public JFormattedTextField getEditor() {
         return _dateField;
     }
 
     public void setEditor(JFormattedTextField editor) {
         JFormattedTextField oldEditor = _dateField;
         _dateField = editor;
         firePropertyChange(EDITOR, oldEditor, _dateField);
     }
 
     /**
      * Returns true if the current value being edited is valid.
      *
      * @return true if the current value being edited is valid.
      */
     public boolean isEditValid() {
         return _dateField.isEditValid();
     }
 
     /**
      * Forces the current value to be taken from the AbstractFormatter and
      * set as the current value. This has no effect if there is no current
      * AbstractFormatter installed.
      */
     public void commitEdit() throws ParseException {
         _dateField.commitEdit();
     }
 
     public void setEditable(boolean value) {
         boolean oldEditable = isEditable();
         editable = value;
         firePropertyChange("editable", oldEditable, editable);
         if (editable != oldEditable) {
             repaint();
         }
     }
 
     public boolean isEditable() {
         return editable;
     }
 
     /**
 	 * Get the baseline for the specified component, or a value less
 	 * than 0 if the baseline can not be determined.  The baseline is measured
 	 * from the top of the component.
 	 *
      * @param width Width of the component to determine baseline for.
 	 * @param height Height of the component to determine baseline for.
 	 * @return baseline for the specified component
 	 */
     public int getBaseline(int width, int height) {
         return ((DatePickerUI)ui).getBaseline(width, height);
     }
     /**
      * Returns the string currently used to identiy fired ActionEvents.
      *
      * @return String The string used for identifying ActionEvents.
      */
     public String getActionCommand() {
         return _actionCommand;
     }
 
     /**
      * Sets the string used to identify fired ActionEvents.
      *
      * @param actionCommand The string used for identifying ActionEvents.
      */
     public void setActionCommand(String actionCommand) {
         _actionCommand = actionCommand;
     }
 
     /**
      * Adds an ActionListener.
      * <p>
      * The ActionListener will receive an ActionEvent when a selection has
      * been made.
      *
      * @param l The ActionListener that is to be notified
      */
     public void addActionListener(ActionListener l) {
         listenerList.add(ActionListener.class, l);
     }
 
     /**
      * Removes an ActionListener.
      *
      * @param l The action listener to remove.
      */
     public void removeActionListener(ActionListener l) {
         listenerList.remove(ActionListener.class, l);
     }
 
     /**
      * Fires an ActionEvent to all listeners.
      */
     protected void fireActionPerformed() {
         Object[] listeners = listenerList.getListenerList();
         ActionEvent e = null;
         for (int i = listeners.length - 2; i >= 0; i -=2) {
             if (listeners[i] == ActionListener.class) {
                 if (e == null) {
                     e = new ActionEvent(JXDatePicker.this,
                             ActionEvent.ACTION_PERFORMED,
                             _actionCommand);
                 }
                 ((ActionListener)listeners[i + 1]).actionPerformed(e);
             }
         }
     }
 
     public void postActionEvent() {
         fireActionPerformed();
     }
 
     private final class TodayPanel extends JXPanel {
         TodayPanel() {
             super(new FlowLayout());
             setBackgroundPainter(new BasicGradientPainter(0, 0, new Color(238, 238, 238), 0, 1, Color.WHITE));
             JXHyperlink todayLink = new JXHyperlink(new TodayAction());
             Color textColor = new Color(16, 66, 104);
             todayLink.setUnclickedColor(textColor);
             todayLink.setClickedColor(textColor);
             add(todayLink);
         }
         
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
 
             g.setColor(new Color(187, 187, 187));
             g.drawLine(0, 0, getWidth(), 0);
             g.setColor(new Color(221, 221, 221));
             g.drawLine(0, 1, getWidth(), 1);
         }
         
         private final class TodayAction extends AbstractAction {
             TodayAction() {
                 super(_linkFormat.format(new Object[] { new Date(_linkDate) }));
             }
             
             public void actionPerformed(ActionEvent ae) {
                 DateSpan span = new DateSpan(_linkDate, _linkDate);
                 _monthView.ensureDateVisible(span.getStart());
             }
         }
     }
 }

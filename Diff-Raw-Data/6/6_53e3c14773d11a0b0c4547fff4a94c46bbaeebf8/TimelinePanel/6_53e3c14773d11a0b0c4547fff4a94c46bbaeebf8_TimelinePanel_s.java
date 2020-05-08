/* $Id: TimelinePanel.java,v 1.7 2007-05-27 20:45:51 hampelratte Exp $
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its 
  *    contributors may be used to endorse or promote products derived from this 
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package lazybones.gui;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import lazybones.LazyBones;
 import lazybones.TimerManager;
 import lazybones.gui.components.timeline.Timeline;
 import lazybones.gui.components.timeline.TimelineWeekdayButton;
 import lazybones.utils.Utilities;
 
 public class TimelinePanel extends JPanel implements ActionListener, Observer {
     
     private JLabel date = new JLabel();
     private JButton nextDateButton;
     private JButton prevDateButton;
     private Timeline timeline;
     private DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
     
     private TimelineWeekdayButton[] weekdayButtons = new TimelineWeekdayButton[7];
     
     public TimelinePanel() {
         timeline = new Timeline();
         initGUI();
         TimerManager.getInstance().addObserver(this);
         enableDisableButtons();
     }
 
     private void initGUI() {
         setLayout(new BorderLayout());
         
         nextDateButton = new JButton(LazyBones.getInstance().createImageIcon("action", "go-next", 16));
         nextDateButton.addActionListener(this);
         nextDateButton.setActionCommand("NEXT_DAY");
         prevDateButton = new JButton(LazyBones.getInstance().createImageIcon("action", "go-previous", 16));
         prevDateButton.addActionListener(this);
         prevDateButton.setActionCommand("PREVIOUS_DAY");
         
         JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
         northPanel.add(prevDateButton);
         northPanel.add(nextDateButton);
         northPanel.add(date);
         northPanel.add(new JLabel("     "));
         Calendar today = Calendar.getInstance();
         for (int i = 0; i < weekdayButtons.length; i++) {
             Calendar day = (Calendar) today.clone();
             day.add(Calendar.DAY_OF_MONTH, i);
             weekdayButtons[i] = new TimelineWeekdayButton(day);
             weekdayButtons[i].addActionListener(this);
             northPanel.add(weekdayButtons[i]);
         }
         
         add(northPanel, BorderLayout.NORTH);
         
         date.setFont(new Font("SansSerif",Font.PLAIN, 18));
         setCalendar(GregorianCalendar.getInstance());
 
         add(timeline, BorderLayout.CENTER);
         timeline.getList().showTimersForCurrentDate(TimerManager.getInstance().getTimers());
     }
 
     public void actionPerformed(ActionEvent e) {
         if(e.getSource() == nextDateButton) {
             Calendar currentDay = timeline.getList().getCalendar();
             Calendar nextDay = TimerManager.getInstance().getNextDayWithEvent(currentDay);
             if(nextDay != null) {
                 setCalendar(nextDay);
             } 
         } else if(e.getSource() == prevDateButton) {
             Calendar currentDay = timeline.getList().getCalendar();
             Calendar previousDay = TimerManager.getInstance().getPreviousDayWithEvent(currentDay);
             if(previousDay != null) {
                 setCalendar(previousDay);
             } 
         } else { // one of the weekdayButtons has been hit
             TimelineWeekdayButton button = (TimelineWeekdayButton) e.getSource();
             setCalendar(button.getDay());
         }
         
         // enable disable next and prev buttons
         enableDisableButtons();
         
         // display the new timers in the timeline
         timeline.getList().showTimersForCurrentDate(TimerManager.getInstance().getTimers());
     }
 
     private void enableDisableButtons() {
         Calendar cal = timeline.getList().getCalendar();
         boolean enableNextButton = TimerManager.getInstance().hasNextDayWithEvent(cal);
         nextDateButton.setEnabled(enableNextButton);
         boolean enablePrevButton = TimerManager.getInstance().hasPreviousDayWithEvent(cal);
         prevDateButton.setEnabled(enablePrevButton);
         
         // weekday buttons
         for (int i = 0; i < weekdayButtons.length; i++) {
             TimelineWeekdayButton button = weekdayButtons[i];
             if(Utilities.sameDay(cal, button.getDay())) {
                 button.setSelected(true);
             } else {
                 button.setSelected(false);
             }
         }
     }
 
     public void update(Observable o, Object arg) {
         if(o == TimerManager.getInstance()) {
             enableDisableButtons();
         }
     }
     
     public void setCalendar(Calendar calendar) {
         // set calendar in timeline
         timeline.getList().setCalendar(calendar);
         
         // show new date
         Date d = calendar.getTime();
         SimpleDateFormat sdf = new SimpleDateFormat("EEEE ", Locale.getDefault());
         date.setText(sdf.format(d) + " " + df.format(d));
         
         // update buttons
         enableDisableButtons();
     }
 }

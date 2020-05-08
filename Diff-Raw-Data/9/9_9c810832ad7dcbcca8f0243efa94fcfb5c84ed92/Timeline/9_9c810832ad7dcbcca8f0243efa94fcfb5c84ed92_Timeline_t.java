 /*
     * JavaWebUnit, Show schoolsoft scheme.
     * Copyright (C) 2012 Richard Dahlgren
     * 
     * This program is free software; you can redistribute it and/or modify
     * it under the terms of the GNU General Public License as published by
     * the Free Software Foundation; either version 3 of the License, or
     * (at your option) any later version.
     * 
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     * GNU General Public License for more details.
     * 
     * You should have received a copy of the GNU General Public License
     * along with this program; if not, write to the Free Software Foundation,
     * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 
 package javawebunit;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 /*
  * Timeline class
  * 
  * @package javawebunit
  * @category Timeline
  * @author  Richard Dahlgren
  */
 public class Timeline extends JPanel {
     
     private final WebPage page;
     private final JFrame window = new JFrame("Timeline");
     private final Color background = new Color(93, 93, 93);
     private final Font font = new Font("Arial", Font.PLAIN, 14);
     private BufferedImage green_frame;
     private BufferedImage yellow_frame;
     private BufferedImage red_frame;
     private String daySchedule;
     
     // -------------------------------------------------------------------------
     
     /*
      * Constructor
      * 
      */
     public Timeline(final WebPage webPage) {
         this.page = webPage;
         this.loadImages();
         this.showFrame();
         new Timer(5 * 1000 * 60, new listener(this)).start();
     }
     
     /*
      * GetDayInfo
      * 
      * Retrive schedule information
      * 
      * @access  private
      * @return  ArrayList<String>
      */
     private ArrayList<String> getDayInfo() {
         this.page.setPage("https://sms7.schoolsoft.se/nti/jsp/student/right_student_startpage.jsp");
         String xml = page.htmlPage.asXml();
         xml = xml.replaceAll("  ", "");
         
         final ArrayList<String> listOfTimes = stripListOfTimes(xml);
         final HashMap<Integer, ArrayList<String>> days = getDaysSorted(listOfTimes);
 
         final Calendar cal = Calendar.getInstance();
         final int dayNo = cal.get(Calendar.DAY_OF_WEEK) - 2;
         
         if (dayNo < 5) {
             return days.get(dayNo);
         }
         return null;
     }
     
     /*
         * Day_of_week values:
         * Sunday Day of Week 1
         * Monday Day of Week 2
         * Tuesday Day of Week 3
         * Wednesday Day of Week 4
         * Thursday Day of Week 5
         * Friday Day of Week 6
         * Saturday Day of Week 7
    */
     
     /*
      * GetDaysSorted
      * 
      * Sort times into a hashmap of days
      * 
      * @access  private
      * @param   ArrayList<String>
      * @return  HashMap<Integer, ArrayList<String>>
      */
     private HashMap<Integer, ArrayList<String>> getDaysSorted(final ArrayList<String> listOfTimes) {
         final HashMap<Integer, ArrayList<String>> days = new HashMap<Integer, ArrayList<String>>();
         String[] startEnd;
         String startHour_String;
         String endHour_String;
         int startHour;
         int endHour;
         int day = 0;
         
         int startHour_PrevTask = 0;
         int endHour_PrevTask = 0;
         
         for (int task = 0; task < listOfTimes.size(); task++) {
             startEnd = listOfTimes.get(task).split("-");
             startHour_String = startEnd[0].substring(0, startEnd[0].indexOf(":"));
             endHour_String = startEnd[1].substring(0, startEnd[1].indexOf(":"));
             startHour = Integer.parseInt(startHour_String);
             endHour = Integer.parseInt(endHour_String);
             
             if (startHour < startHour_PrevTask || endHour_PrevTask > startHour) {
                 day++;
             }
             
             if (!days.containsKey(day)) {
                 days.put(day, new ArrayList<String>());
             }
             days.get(day).add(listOfTimes.get(task));
             startHour_PrevTask = startHour;
             endHour_PrevTask = endHour;
         }
         return days;
     }
     
     /*
      * LoadImages
      * 
      * Load container images
      * 
      * @access  private
      * @return  boolean
      */
     private boolean loadImages() {
         try {
             this.green_frame = ImageIO.read(this.getClass().getResource("resources/green.png"));
             this.yellow_frame = ImageIO.read(this.getClass().getResource("resources/yellow.png"));
             this.red_frame = ImageIO.read(this.getClass().getResource("resources/red.png"));
         } catch (IOException ex) {
             System.out.println("Exception caught while fetching images! "+ex.getMessage());
             JOptionPane.showMessageDialog(this.window, "Failed to load timeline, application will exit!", "Error", JOptionPane.WARNING_MESSAGE);
             System.exit(0);
         }
         return true;
     }
     
     /*
      * PaintComponent
      * 
      * Draw Scheme
      * 
      * @access  public
      * @param   Graphics
      */
     @Override
     public final void paintComponent(final Graphics g) {
         g.setColor(this.background);
         g.fillRect(0, 0, 254, 500);
         
         g.setFont(this.font);
         g.setColor(Color.BLACK);
         final String[] splittedValues = this.daySchedule.split("&");
         
         int y_current = 2;
         int yDiff = 20;
         int passedTime;
         for (int row = 0; row < splittedValues.length; row++) {
             if (row % 2 == 0) {
                 passedTime = passedTime(splittedValues[row]);
                 if (passedTime == 0) {
                     g.drawImage(this.green_frame, 10, (yDiff * y_current) - 25, null);
                 } else if (passedTime == 1) {
                     g.drawImage(this.red_frame, 10, (yDiff * y_current) - 25, null);
                 } else {
                     g.drawImage(this.yellow_frame, 10, (yDiff * y_current) - 25, null);
                 }
             } 
             
             g.drawString(splittedValues[row], 20, yDiff * y_current);
             if (row % 2 != 0) {
                 y_current += 3;
             } else {
                 y_current++;
             }
         }
         this.window.setSize(254, (y_current * yDiff));
         Toolkit.getDefaultToolkit().sync();
         g.dispose();
     }
     
     /*
      * PassedTime
      * 
      * Has the class passed?
      * 
      * @access  private
      * @param   String
      * @return  int
      */
     private int passedTime(final String time) {
         final String[] times = time.split("-");
         final String[] startTime = times[0].split(":");
         final String[] endTime = times[1].split(":");
         final int startHour = Integer.parseInt(startTime[0]);
         final int startMinute = Integer.parseInt(startTime[1]);
         final int endHour = Integer.parseInt(endTime[0]);
         final int endMinute = Integer.parseInt(endTime[1]);
         
         final Calendar cal = Calendar.getInstance();// Get Calendar Instance
         final int currentHour = cal.get(Calendar.HOUR_OF_DAY);
         final int currentMinute = cal.get(Calendar.MINUTE);
         
         if (currentHour > endHour || (currentHour == endHour && currentMinute > endMinute)) {// Passed
             return 1;
        } else if (currentHour > startHour || (currentHour == startHour && currentMinute >= startMinute)) {// Current
            if (currentHour < endHour || (currentHour == endHour && currentMinute <= endMinute)) {
                 return 2;
             }
         }
        return 0;// Remaining
     }
     
     /*
      * RefreshResults
      * 
      * Refresh schedule information
      * 
      * @access  public
      */
     public final void refreshResults() {
         try {
             this.page.htmlPage.refresh();// Refresh Html page
         } catch (IOException ex) {
             System.out.println("Failed to refresh! "+ex.getMessage());
         }
         this.updateSchedule();
         this.repaint();// Repaint the screen
     }
     
     /*
      * ShowFrame
      * 
      * Constructor - part, show frame
      * 
      * @access  public
      */
     public final void showFrame() {
         this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.window.setSize(254, 438);
         this.window.setResizable(false);
         this.window.setLocationRelativeTo(null);
         
         this.setPreferredSize(window.getSize());
         this.window.getContentPane().add(this);
         this.updateSchedule();
         
         this.window.setVisible(true);
     }
     
     /*
      * StripListOfTimes
      * 
      * Strip xml times into array
      * 
      * @access  private
      * @param   String
      * @return  Array<String>
      */
     private ArrayList<String> stripListOfTimes(final String xml) {
         final ArrayList<String> listOfTimes = new ArrayList<String>();
         int index = xml.indexOf("<div class=\"dayViewDetailedTextBox\" id=\"\">");
         String sub;
         while (index >= 0) {
             sub = xml.substring(index + 44, xml.indexOf("</div>", index) - 2);
             try {
                 Integer.parseInt(sub.substring(0, 1));
                 if (sub.contains("-")) {
                     listOfTimes.add(sub);
                 }
             } catch (NumberFormatException e) {
                 // Catch exception
             }
             index = xml.indexOf("<div class=\"dayViewDetailedTextBox\" id=\"\">", index + 1);
         }
         return listOfTimes;
     }
     
     /*
      * UpdateSchedule
      * 
      * Finalize the shedule info
      * 
      * @access  private
      */
     private void updateSchedule() {
         final ArrayList<String> todaysFeed = getDayInfo();
         String text = "";
         if (todaysFeed != null) {
             for (int row = 0; row < todaysFeed.size(); row++) {
                 final String[] splitted = todaysFeed.get(row).split(" ");
                 String additionalInfo = "";
                 boolean replaceRest = false;
                 
                 for (int split = 0; split < splitted.length; split++) {
                     if (replaceRest || splitted[split].startsWith("TE")) {
                         splitted[split] = "";
                         replaceRest = true;
                     } else if (split > 0) {
                         additionalInfo = additionalInfo + (("".equals(additionalInfo)) ? "":" ") + splitted[split];
                     }
                 }
                 text = text + splitted[0]+"&"+additionalInfo+(row + 1 < todaysFeed.size() ? "&":"");
             }
         } else {
             text = "No data to display!";
         }
         this.daySchedule = text;
     }
 }
 
 // -----------------------------------------------------------------------------
 
 /*
  * Listener class
  * 
  * @package javawebunit
  * @category Timeline
  * @author  Richard Dahlgren
  */
 final class listener implements ActionListener {
     
     private final Timeline timeline;
     
     // -------------------------------------------------------------------------
     
     /*
      * Constructor
      * 
      */
     public listener(final Timeline tl) {
         timeline = tl;
     }
 
     /*
      * ActionPerformed
      * 
      * Listen for refresh actions
      * 
      * @access  public
      * @param   ActionEvent
      */
     @Override
     public final void actionPerformed(final ActionEvent e) {
        System.out.println("Refreshed page");
         this.timeline.refreshResults();
     }
 }

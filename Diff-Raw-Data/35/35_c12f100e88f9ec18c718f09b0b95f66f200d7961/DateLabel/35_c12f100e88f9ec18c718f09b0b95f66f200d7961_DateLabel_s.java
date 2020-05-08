 package at.ac.tuwien.sepm.ui.calender.cal;
 
 import at.ac.tuwien.sepm.entity.Date;
 import at.ac.tuwien.sepm.entity.DateEntity;
 import at.ac.tuwien.sepm.entity.LvaDate;
 import at.ac.tuwien.sepm.service.TimeFrame;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import net.miginfocom.swing.MigLayout;
 import org.joda.time.DateTime;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * @author Markus MUTH
  */
 public class DateLabel extends JTextPane implements Comparable<DateLabel>{
     private static final int MAX_TEXT_LENGTH=20;
     private Date date;
     private boolean twoLine = false;
 
     public DateLabel(Date date, boolean twoLine) {
         this.date=date;
         this.twoLine = twoLine;
         setContentType("text/html");
         if(date.getName()==null || date.getName().trim().equals("")){
             this.setText("...");
         } else if (twoLine) {
             this.setText("<nobr><font size=2 face=\"Arial\"><b>"+formatStartDate(date.getTime().from())+" - "+formatStartDate(date.getTime().to()) +"</b><br>"+ date.getName()+"</font></nobr>");
         } else {
             this.setText("<nobr><font size=2 face=\"Arial\">"+cutText(date.getName(), 0)+"</font></b>");
             setBorder(BorderFactory.createEmptyBorder());
         }
         //this.setFont(new Font("Arial", Font.PLAIN, 10));
         this.addMouseListener(new PrivateMouseListener());
 
         setEditable(false);
         //setWrapStyleWord(false);
         setAutoscrolls(false);
         setFocusable(false);
 
     }
 
     public void changeColor (boolean isActualMonth) {
         if(isActualMonth) {
             this.setOpaque(true);
             if(twoLine){
                 setBorder(BorderFactory.createLineBorder(Color.WHITE,1));
             }else{
                 setBorder(BorderFactory.createEmptyBorder());
             }
             if(date instanceof LvaDate) {
                 this.setBackground(new Color(223, 233, 255));
 
 
                 //this.setForeground(new Color(117, 190, 255));
             } else if (date instanceof DateEntity) {
                 if (((DateEntity) date).getDescription().equals("Dies ist ein freier Tag, das heisst: KEINE UNI YEAAH!!!")) {
                     this.setBackground(new Color(246, 242, 122));
                 } else {
                     this.setBackground(new Color(195, 255, 194));
                     //this.setForeground(new Color(163, 255, 114));
                 }
             }
         } else {
             setBorder(BorderFactory.createEmptyBorder());
             setOpaque(false);
         }
     }
 
     public DateTime getStart() {
         return date.getTime().from();
     }
 
     public DateTime getStop() {
         return date.getTime().to();
     }
 
     public TimeFrame getTime() {
         return date.getTime();
     }
 
     private String formatStartDate(DateTime d) {
         String result = "";
         if(d.getHourOfDay()<10) {
             result = "0" + d.getHourOfDay();
         } else {
             result += d.getHourOfDay();
         }
         result += ":";
         if(d.getMinuteOfHour()<10) {
             result += "0" + d.getMinuteOfHour();
         } else {
            result += d.getHourOfDay();
         }
 
         return result;
     }
 
     private boolean consistsOf(char c, String s) {
         for(int i=0; i<s.length(); i++) {
             if(c != s.charAt(i)) {
                 return false;
             }
         }
         return true;
     }
 
     private String cutText(String text, int diff) {
         if(text.length() > (MAX_TEXT_LENGTH-diff)) {
             text = text.substring(0,MAX_TEXT_LENGTH-diff) + "...";
         }
         return text;
     }
 
     public Date getDate() {
         return date;
     }
 
     @Override
     public int compareTo(DateLabel o) {
         return date.getTime().from().compareTo(o.getTime().from());
     }
 
     class PrivateMouseListener extends MouseAdapter {
         public void mouseClicked(MouseEvent evt) {
             if (evt.getClickCount() == 2) {
                 UpdateDateFrameFactory.createFrame(getDate());
             }
         }
 
         @Override
         public void mousePressed(MouseEvent e) {}
 
         @Override
         public void mouseReleased(MouseEvent e) {
             if(e.getButton()==3) {
                 PopUpMenu menu = new PopUpMenu();
                 menu.show(e.getComponent(), e.getX(), e.getY());
             }
         }
 
         @Override
         public void mouseEntered(MouseEvent e) {}
 
         @Override
         public void mouseExited(MouseEvent e) {}
     }
 
     private class PopUpMenu extends JPopupMenu {
         private JMenuItem edit;
         private JMenuItem showRoom;
 
         public PopUpMenu(){
             edit = new JMenuItem("Bearbeiten");
             showRoom = new JMenuItem("Wegbeschreibung");
             add(edit);
             add(showRoom);
             addActionListeners();
         }
 
         private void addActionListeners() {
             edit.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseReleased(MouseEvent e) {
                     if(date instanceof LvaDate) {
                         PanelTube.backgroundPanel.viewLvaDate((LvaDate)date, null);
                     } else if(date instanceof DateEntity) {
                         PanelTube.backgroundPanel.viewDate((DateEntity)date, null);
                     }
                 }
             });
             showRoom.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseReleased(MouseEvent e) {
                     if(date instanceof LvaDate) {
                         if(!PanelTube.backgroundPanel.openRoomBrowser(((LvaDate) date).getRoom()))
                             PanelTube.backgroundPanel.viewInfoText("Keine Wegbeschreibung gefunden.", SmallInfoPanel.Info);
                     } else {
                         PanelTube.backgroundPanel.viewInfoText("Es existieren keine Wegbeschreibungen zu privaten Terminen.", SmallInfoPanel.Warning);
                     }
                 }
             });
 
         }
     }
 
     private static class UpdateDateFrameFactory extends JFrame {
         private static void createFrame(Date d) {
             if(d instanceof DateEntity) {
                 new UpdateDateFrame((DateEntity)d);
             } else if (d instanceof LvaDate) {
                 new UpdateLvaDateFrame((LvaDate)d);
             }
         }
 
         private static class UpdateDateFrame extends JFrame {
             // TODO implement this class correctly
             private DateEntity date;
             private JLabel l0=new JLabel();
             private JLabel l1=new JLabel();
 
             public UpdateDateFrame(DateEntity d) {
                 this.setLayout(new MigLayout());
                 this.date = d;
                 this.setLocation(500,500);
                 l0.setText("DateLabel.UpdateDateFrame ... ");
                 l1.setText("Name of date: " + date.getName());
                 this.setMinimumSize(new Dimension((int)(l0.getSize().getWidth() + l1.getSize().getWidth()), (int)(l0.getSize().getHeight() + l1.getSize().getHeight())));
                 this.add(l0, "wrap");
                 this.add(l1);
                 this.pack();
                 this.revalidate();
                 this.repaint();
                 this.setVisible(true);
             }
         }
 
         private static class UpdateLvaDateFrame extends JFrame {
             // TODO implement this class correctly
             private LvaDate date;
             private JLabel l0=new JLabel();
             private JLabel l1=new JLabel();
 
             public UpdateLvaDateFrame(LvaDate d) {
                 this.setLayout(new MigLayout());
                 this.date = d;
                 this.setLocation(500,500);
                 l0.setText("DateLabel.UpdateLvaDateFrame ... ");
                 l1.setText("Name of date: " + date.getName());
                 this.setMinimumSize(new Dimension((int)(l0.getSize().getWidth() + l1.getSize().getWidth()), (int)(l0.getSize().getHeight() + l1.getSize().getHeight())));
                 this.add(l0, "wrap");
                 this.add(l1);
                 this.pack();
                 this.revalidate();
                 this.repaint();
                 this.setVisible(true);
             }
         }
     }
 }

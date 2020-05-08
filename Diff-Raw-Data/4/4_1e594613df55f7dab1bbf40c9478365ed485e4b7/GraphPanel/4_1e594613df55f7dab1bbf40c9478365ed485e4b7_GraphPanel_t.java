 // Copyright distributed.net 1997-1999 - All Rights Reserved
 // For use in distributed.net projects only.
 // Any other distribution or use of this source violates copyright.
 //
 
 import java.io.*;
 import java.util.*;
 import java.text.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.FontMetrics;
 
 
 public class GraphPanel extends Panel
 implements MouseMotionListener, MouseListener,ActionListener
 {
     // Empty borders
     protected final int topBorder       = 10;
     protected final int bottomBorder    = 45;
     protected final int leftBorder      = 55;
     protected final int rightBorder     = 20;
     protected int width, height;
 
     // user-interface
     protected Color grayShade = new Color(235, 235, 235);
 
     // storage variables.
     protected Vector logdata = new Vector();
     protected long mintime, maxtime;
     protected double minrate, maxrate;
     protected double totalkeys;
     protected long rangestart, rangeend;
 
     // current graphing state.
     protected final int nologloaded     = 0;
     protected final int lognotfound     = 1;
     protected final int loadinprogress  = 2;
     protected final int logloaded       = 3;
     protected int loggerstate;
 
     // other
     public File currentLogFile;
 
     private static String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
     private int startx = -1;
     private int endx = -1;
 
 
     // constructor
     public GraphPanel()
     {
         // set the default ranges
         rangestart = -1;
         rangeend = -1;
 
         // set the flags
         loggerstate = nologloaded;
         addMouseListener(this);
         addMouseMotionListener(this);
 
         setBackground(Color.lightGray);
     }
 
     public void actionPerformed(ActionEvent e)
     {
         if (e.getActionCommand() == "complete")
         {
             setRange(-1,-1);
             startx = -1;
             endx = -1;
             repaint();
         }
 
         if (e.getActionCommand() == "today")
         {
             Date dt = new Date();
 //            System.out.println(dt);
             setRange(Date.UTC(dt.getYear(),dt.getMonth(),dt.getDate(),0,0,0)/100,Date.UTC(dt.getYear(),dt.getMonth(),dt.getDate(),0,0,0)/100+864000);
             startx = -1;
             endx = -1;
             repaint();
         }
         if (e.getActionCommand() == "yesterday")
         {
             Date dt = new Date();
 //            System.out.println(dt);
             setRange(Date.UTC(dt.getYear(),dt.getMonth(),dt.getDate(),0,0,0)/100-864000,Date.UTC(dt.getYear(),dt.getMonth(),dt.getDate(),0,0,0)/100);
 //            System.out.println(new Date(Date.UTC(dt.getYear(),dt.getMonth(),dt.getDate(),0,0,0)-86400000));
             startx = -1;
             endx = -1;
             repaint();
         }
     }
 
     public void mouseClicked(MouseEvent e)
     {
 
     }
 
     public void mousePressed(MouseEvent e)
     {
         if (e.isPopupTrigger())
         {
             PopupMenu pm = new PopupMenu("Zoom");
             pm.show(this,e.getX(),e.getY());
 //            System.out.println("popup");
         } else {
             startx = e.getX();
             endx = e.getX()+1;
         }
         repaint();
     }
 
     public void mouseReleased(MouseEvent e)
     {
        if (width <= 0 || height <= 0)
        {
          return; // don't do anything if we don't have graph data
        }
         if (e.isPopupTrigger())
         {
             PopupMenu pm = new PopupMenu("Zoom");
             MenuItem mi = new MenuItem("complete");
             mi.addActionListener(this);
             pm.add(mi);
             add(pm);
             pm.show(this,e.getX(),e.getY());
 //            System.out.println("popup");
         } else {
             if (Math.abs(startx-endx) < 10)
             {
                  startx = -1;
                  endx = -1;
             }
             if ( (startx != -1) && (endx != -1 ) )
             {
                 if (startx < endx)
                   setRange(rangestart+(startx-leftBorder)*(rangeend-rangestart)/width,rangestart+(endx-leftBorder)*(rangeend-rangestart)/width);
                 if (startx > endx)
                   setRange(rangestart+(endx-leftBorder)*(rangeend-rangestart)/width,rangestart+(startx-leftBorder)*(rangeend-rangestart)/width);
             }
 //            System.out.println(rangestart+" , "+rangeend);
             startx = -1;
             endx = -1;
         }
         repaint();
     }
 
     public void mouseEntered(MouseEvent e)
     {
 
     }
 
     public void mouseExited(MouseEvent e)
     {
 
     }
 
     public void mouseDragged(MouseEvent e)
     {
         endx = e.getX();
         repaint();
     }
 
     public void mouseMoved(MouseEvent e)
     {
 //        endx = e.getX();
     }
 
     // public interface methods.
     void getDataRange(long start, long end)
         { start = mintime; end = maxtime; }
 
     // public interface methods.
     void getRange(long start, long end)
         { start = rangestart; end = rangeend; }
 
     // public interface methods.
     void setRange(long start, long end)
         { rangestart = start; rangeend = end; }
 
     // public interface methods.
     boolean isDataAvailable()
         { return (loggerstate == logloaded) &&
             (!logdata.isEmpty()) &&
               (minrate < maxrate) && (mintime < maxtime); }
 
 
     public void repaint()
     {
         paint(getGraphics());
     }
 
     public synchronized void paint(Graphics bg)
     {
         Dimension d = getSize();
         java.awt.Image img = createImage(d.width,d.height);
         Graphics g = img.getGraphics();
 
         if (loggerstate == loadinprogress) {
             g.drawString("Please wait, currently reloading log file.",
                 leftBorder, topBorder);
             return;
         }
         else if (loggerstate == lognotfound) {
             g.drawString("Could not load any data for graphing.  This may " +
                 "indicate that there was a problem opening the log file.",
                 leftBorder, topBorder);
             return;
         }
         else if (loggerstate == nologloaded) {
             g.drawString("You must specify a log file to be used for graph visualization.",
                 leftBorder, topBorder);
             return;
         }
         else if (logdata.isEmpty() || minrate >= maxrate || mintime >= maxtime) {
             g.drawString("Could not load any data for graphing.  This may " +
                 "indicate that there was no graphable data inside of the " +
                 "specified log file",
                 leftBorder, topBorder);
             return;
         }
 
 
         // Determine dimensions of graph area.
         width = d.width - (leftBorder + rightBorder);
         height = d.height - (topBorder + bottomBorder);
 
         if ( (width < 0) || (height < 0)) return;
 
         // determine the range window that we will draw
         long timelo = (rangestart == -1 ? mintime : rangestart);
         long timehi = (rangeend == -1 ? maxtime : rangeend);
         setRange(timelo,timehi);
         if (timehi <= timelo) return;
 
         FontMetrics fm = g.getFontMetrics();
 
         // set up the graphing window structure and scaling
         double yinterval = (maxrate - minrate) / height * (fm.getHeight() * 4);
         long xinterval = (timehi - timelo) / width * (fm.charWidth('A') * 10);
 
         // Start with a white graph area.
         g.setColor(Color.white);
         g.fillRect(leftBorder, topBorder, width, height);
 
         // Add gray shading to graph area.
         g.setColor(grayShade);
         for(int i=0 ; i<width ; i+=100) {
             if((i+50)>width) {
                 g.fillRect((leftBorder+i), topBorder, (width-i), height);
             } else {
                 g.fillRect((leftBorder+i), topBorder, 50, height);
             }
         }
 
         // get the default height ...
         int string_height = fm.getHeight();
         g.setColor(Color.black);
 
         // Draw the x-Axis.
         for (int i = 0; i < width; i+= 50)
         {
             Date dt = new Date(100*(timelo+i*(timehi-timelo)/width));
             String str = months[dt.getMonth()] +" "+dt.getDate();
             int length = fm.stringWidth(str) / 2;
             g.drawString(str,(leftBorder+i)-length,topBorder+height+5+string_height);
             str = dt.getHours()+":";
             if (dt.getMinutes() < 10)
             {
                 str += "0"+dt.getMinutes();
             } else {
                 str += dt.getMinutes();
             };
             length = fm.stringWidth(str) / 2;
             g.drawString(str,(leftBorder+i)-length,topBorder+height+5+2*string_height);
             g.drawLine((leftBorder+i),topBorder+height,(leftBorder+i),topBorder+height+5);
         }
 
 
 
         NumberFormat nf = NumberFormat.getInstance();
         nf.setMinimumFractionDigits(2);
         nf.setMaximumFractionDigits(2);
 
         string_height /= 2;
 
         // draw horizontal lines & y-Axis
         for (double r = minrate + yinterval; r < maxrate; r += yinterval)
         {
             int y = topBorder + (int) ((float) height *
                 (float) (r - minrate) / (float) (maxrate - minrate));
             for (int i = 0; i < width-3; i += 6)
             {
               g.drawLine(leftBorder+i, y, leftBorder + i+3, y);
             }
 
             double temp = (maxrate -r) / 1000;
             String number = nf.format(temp);
             int length = fm.stringWidth(number);
             g.drawString(number,leftBorder - 5 - length,y+string_height);
             g.drawLine(leftBorder-5,y,leftBorder,y);
         }
 
         // draw the highest Rate.
         double temp = (maxrate) / 1000;
         String number = nf.format(temp);
         int length = fm.stringWidth(number);
         g.drawString(number,leftBorder - 5 - length,topBorder+string_height);
 
 
         // start drawing the points.
         g.setColor(Color.red);
         g.setClip(leftBorder, topBorder, width, height);
         try {
             boolean firstpoint = true;
             long lasttime = 0;
             int lastx = 0, lasty = 0;
 
             Enumeration listiter = logdata.elements();
             while (listiter.hasMoreElements())
             {
                 GraphEntry ge = (GraphEntry) listiter.nextElement();
 
                 // convert to screen coords.
                 int tmpx = leftBorder + (int) ((float) width *
                     (float) (ge.timestamp - timelo) / (float) (timehi - timelo));
                 int tmpy = topBorder + height - (int) ((float) height *
                     (float) (ge.rate - minrate) / (float) (maxrate - minrate));
 
                 if ( (tmpx < leftBorder) || (tmpx > width+leftBorder) ) continue;
 
                 // plot the point.
                 if (!firstpoint)
                 {
                     if ((ge.timestamp - lasttime) > (300 + 1.25 * ge.duration*10))
                     {
                         // There was a significant lapse in time since the last point,
                         // which probably indicates that the client was turned off for
                         // awhile, so draw a "drop" in the keyrate graph.
                         g.drawLine(lastx, lasty, lastx, topBorder + height);
                         g.drawLine(lastx, topBorder + height, tmpx, topBorder + height);
                         g.drawLine(tmpx, topBorder + height, tmpx, tmpy);
                     }
                     else
                     {
                         // otherwise just connect the line from the last one.
                         g.drawLine(lastx, lasty, tmpx, tmpy);
                     }
                 }
 
                 // remember this point for next time.
                 lastx = tmpx;
                 lasty = tmpy;
                 lasttime = ge.timestamp;
                 firstpoint = false;
             }
         }
         catch (NoSuchElementException e) { }
         g.setClip(null);
 
         if (startx > 0)
         {
             g.setXORMode(Color.black);
             if (startx < endx)
             {
                 if (endx > width+leftBorder) endx = width+leftBorder;
                 g.fillRect(startx,topBorder,endx-startx,height);
             }
             if (endx < startx)
             {
                 if (startx > width+leftBorder) startx = width+leftBorder;
                 g.fillRect(endx,topBorder,startx-endx,height);
             }
         }
 
         g.finalize();
         bg.drawImage(img,0,0,null);
         // Done!
         return;
     }
 
 
     // Load log in a separate thread.
     final class WorkerThread extends Thread
     {
         LogParser parser = null;
 
         WorkerThread(BufferedReader inbuffer) {
             parser = new LogParser(inbuffer, logdata);
             loggerstate = loadinprogress;
         }
 
         public void run()
         {
             System.out.println("load in progress");
             parser.run();
 
             mintime = maxtime = 0;
             minrate = maxrate = 0.0;
             totalkeys = 0.0;
 
             try {
                 Enumeration listiter = logdata.elements();
                 while (listiter.hasMoreElements())
                 {
                     GraphEntry ge = (GraphEntry) listiter.nextElement();
                     if (mintime == 0 || ge.timestamp < mintime) mintime = ge.timestamp;
                     if (maxtime == 0 || ge.timestamp > maxtime) maxtime = ge.timestamp;
                     if (minrate == 0.0 || ge.rate < minrate) minrate = ge.rate;
                     if (maxrate == 0.0 || ge.rate > maxrate) maxrate = ge.rate;
                     totalkeys += (float) ge.keycount;
                 }
             }
             catch (NoSuchElementException e) { }
 
             loggerstate = logloaded;
             repaint();
 //            System.out.println("load complete.  numrecords=" + logdata.size());
         }
     }
 
 
 
     void readLogData()
     {
         // reset storage.
         logdata.removeAllElements();
         mintime = maxtime = 0;
         minrate = maxrate = 0.0;
         totalkeys = 0.0;
 
         // open up the log.
         BufferedReader in;
         try {
             in = new BufferedReader(new FileReader(currentLogFile));
         }
         catch( FileNotFoundException e ) {
             System.out.println("File not found: " + e);
             loggerstate = lognotfound;
             return;
         }
 
         WorkerThread p = new WorkerThread(in);
         p.start();
         try
         {
             p.join();
         }
         catch (InterruptedException e) { }
         setRange(-1,-1);
         repaint();
     }
 }

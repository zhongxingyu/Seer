 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package visualization;
 
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 
 /**
  *
  * @author Riz
  */
 public class VisualizationCanvas extends Canvas {
 
     // basic window dimensions
     int centerY = 450, width = 5760, height = 2 * centerY;
 
     // visualization dimensions
     int startY = 64; // baseline of drawing
     int barHeight = 32; // height of a single book's bar
     int intervalWidth = 256; // width of time interval
     int whiteSpaceHeight = 8; // height of white space between bars
 
     // array of lists containing book data over time
     ArrayList<LinkedList<Book.BookStats>> buckets;
 
     public VisualizationCanvas()
     {
         super();
         setSize(new Dimension(width, height));
         buckets = new ArrayList<LinkedList<Book.BookStats>>();
         for (int i = 0; i < 22; i++)
             buckets.add(new LinkedList<Book.BookStats>());
         readSalesData();
         
         /*String s = null;
         Process p = Runtime.getRuntime().exec("pwd");
         BufferedReader stdInput = new BufferedReader(new 
                  InputStreamReader(p.getInputStream()));
 
             BufferedReader stdError = new BufferedReader(new 
                  InputStreamReader(p.getErrorStream()));
 
             // read the output from the command
             System.out.println("Here is the standard output of the command:\n");
             while ((s = stdInput.readLine()) != null) {
                 System.out.println(s);
             }
             
             // read any errors from the attempted command
             System.out.println("Here is the standard error of the command (if any):\n");
             while ((s = stdError.readLine()) != null) {
                 System.out.println(s);
             }*/
     }
 
     public void readSalesData()
     {
         for (int i = 0 ; i < 22; i++)
         {
             try
             {
                 BufferedReader input = new BufferedReader (new FileReader("src/visualization/Week " + (i+1) + ".txt"));
                             
                 int ranking = 1;
                 String line = input.readLine();
                 input.readLine();
                 while (line!=null)
                 {
                     // parse text file
                     int currentIndex = line.indexOf(", by");
                     String name = line.substring(line.indexOf(' ')+1, currentIndex);
                     line = line.substring(currentIndex+4);
                     currentIndex = line.indexOf(". (");
                     String author = line.substring (0, currentIndex);
                     line = line.substring(currentIndex+3);
                     currentIndex = line.indexOf(", $");
                     String publisher = line.substring(0, currentIndex);
                     line = line.substring (currentIndex+3);
                     currentIndex = line.indexOf (".)");
                     float price = Float.parseFloat(line.substring(0, currentIndex));
                     String description = line.substring(currentIndex+3);
 
                     // see if book already exists
                     Book owner = null;
                     if (i > 0)
                     {
                        ListIterator<Book.BookStats> it = buckets.get(i-1).listIterator();
                        while (it.hasNext())
                         {
                            Book.BookStats current = it.next();
                            if (current.owner.title.equals(name))
                             {
                                owner = current.owner;
                                break;
                             }
                         }
                     }
                     if (owner == null)
                     {
                         owner = new Book(name, author, publisher, description, price, 0);
                         
                         // calculate colour
                         Color col = null;
                         int modI = i%4;
                         int divI = i/4;
                         switch(divI)
                         {
                             case 0:
                                 col = new Color(255 - ranking%4*32, modI*32,0);
                                 break;
                             case 1:
                                 col = new Color (modI*32, 255 - ranking%4*32, 0);
                                 break;
                             case 2:
                                 col = new Color (0, 255 - ranking%4*32, modI*32);
                                 break;
                             case 3:
                                 col = new Color (0, modI*32, 255 - ranking%4*32);
                                 break;
                             case 4:
                                 col = new Color (modI*32, 0, 255 -ranking%4*32);
                                 break;
                             case 5:
                                 col = new Color (255 - ranking%4*32, 0, modI*32);
                                 break;
                         }
                         owner.colour = col;
                     }
 
                     // add book stats to bucket
                     Book.BookStats bookStats = owner.addStats(i+1, 2011, 0, ranking);
                     buckets.get(i).add(bookStats);
                     ranking++;
                     line = input.readLine();
                     input.readLine();
                     //System.out.println (owner);
                 }
             }
             catch (IOException e)
             {
                 System.out.println(e);
                 break;
             }
         }
     }
 
     @Override
     public void paint(Graphics g)
     {
         g.setColor(Color.GREEN);
         for (int i = 0; i < 22; i++)
         {
             LinkedList<Book.BookStats> currentBucket = buckets.get(i);
             int currentX = i * intervalWidth;
             Iterator<Book.BookStats> it = currentBucket.listIterator();
             while (it.hasNext())
             {
                 Book.BookStats stat = it.next();
                 int currentY = height-startY-(16 - stat.ranking) * (barHeight + whiteSpaceHeight);
                 g.setColor(stat.owner.colour);
                 g.fillRect(currentX, currentY, intervalWidth/2, barHeight);
                if (stat.next != null)
                 {
                     int nextY = height-startY-(16 - stat.next.ranking) * (barHeight + whiteSpaceHeight);
                     int y [] = {currentY, nextY, nextY+barHeight, currentY+barHeight};
                     int x [] = {currentX + intervalWidth/2, currentX + intervalWidth, currentX + intervalWidth, currentX + intervalWidth/2};
                     g.fillPolygon(x, y, 4);
                 }
                 if (stat.isFirst)
                 {
                     g.setColor(Color.WHITE);
                     if (stat.owner.title.length() < 15)
                         g.drawString(stat.owner.title, currentX + 4, currentY+20);
                     else
                         g.drawString(stat.owner.title.substring(0,15) + "...", currentX + 4, currentY+20);
                 }
             }
         }
     }
 }

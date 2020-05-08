 package ex03_03;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.awt.Point;
 import java.util.TimeZone;
 
 class DigitalClock extends Window implements Runnable, MouseMotionListener {
   private DateFormat sdf;
   private TimeZone timeZone;
   private Thread thread;
   private Font clockFont;
   private Color fontColor;
   private Color clockBackColor;
 
   private Point clockPosition;
 
   private PropertyPopupMenu proPopupMenu;
 
   DigitalClock() {
     super(null);
     setSize(100, 100);
     setLocationRelativeTo(null);
     setBackground(clockBackColor);
 
     clockFont = new Font("Arial", Font.BOLD, 60);
     fontColor = Color.BLACK;
     clockBackColor = Color.WHITE;
 
     proPopupMenu = new PropertyPopupMenu();
     proPopupMenu.init();
     add(proPopupMenu);
 
     /* add right click event */
     addMouseListener(new MouseAdapter() {
       public void mouseClicked(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON3) {
           proPopupMenu.show(e.getComponent(), e.getX(), e.getY());
         }
       }
       @Override
       public void mousePressed(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1) {
           System.out.println("mouse pressed");
          if (clockPosition == null) {
            clockPosition = e.getLocationOnScreen();
          }
         }
       }
       @Override
       public void mouseReleased(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1) {
           System.out.println("mouse released");
           clockPosition = null;
         }
       }
     });
 
     /* for mouse dragged */
     addMouseMotionListener(this);
 
     /* setting for a clock */
     sdf = new SimpleDateFormat("HH:mm:ss");
     timeZone = TimeZone.getTimeZone("Asia/Tokyo");
     sdf.setTimeZone(timeZone);
 
     setVisible(true);
     thread = new Thread(this);
     thread.start();
   }
 
   @Override
   public void paint(Graphics g) {
     Image img = createImage(getWidth(), getHeight());
     Graphics2D buffer = (Graphics2D)img.getGraphics();
 
     Calendar calendar = Calendar.getInstance();
     String time_str = sdf.format(calendar.getTime());
 
     buffer.setFont(clockFont);
     FontMetrics metrics = buffer.getFontMetrics();
     Insets insets = getInsets();
     int strWidth = metrics.stringWidth(time_str);
     int strHeight = metrics.getDescent() + metrics.getAscent();
     int width = strWidth + insets.left + insets.right;
     int height = strHeight + insets.top;
     setSize(width, height);
 
     ajustWindowLocation(getLocation());
 
     int x = 0;
     int y = metrics.getAscent();
 
     buffer.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
         RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
     buffer.setColor(fontColor);
     buffer.drawString(time_str, x, y);
     setBackground(clockBackColor);
 
     g.drawImage(img, insets.left, insets.top, this);
   }
 
   @Override
   public void run() {
     while (true){
       repaint();
       try {
         /* 250 msec 間隔で再描画 */
         Thread.sleep(250);
       } catch (InterruptedException e) {
       }
     }
   }
 
   @Override
   public void update(Graphics g) {
     paint(g);
   }
 
   void changeClockFont(String fontName) {
     setClockFont(fontName, clockFont.getStyle(), clockFont.getSize());
   }
 
   void changeClockFontSize(int fontSize) {
     setClockFont(clockFont.getFontName(), clockFont.getStyle(), fontSize);
   }
 
   void changeFontColor(String fontColorName) {
     fontColor = ColorUtil.getColorInstance(fontColorName);
   }
 
   void changeBackColor(String backColorName) {
     clockBackColor = ColorUtil.getColorInstance(backColorName);
   }
 
   private void setClockFont(String name, int style, int size) {
     clockFont = new Font(name, style, size);
   }
 
   /** set a window location in a screen */
   private void ajustWindowLocation(Point newLocation) {
     Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 
     if (newLocation.x < 0)
       newLocation.x = 0;
     if (newLocation.y < 0)
       newLocation.y = 0;
     if (newLocation.x + getSize().width > screen.width)
        newLocation.x = screen.width - getSize().width;
     if (newLocation.y + getSize().height > screen.height)
       newLocation.y = screen.height - getSize().height;
 
     setLocation(newLocation);
   }
 
   @Override
   public void mouseMoved(MouseEvent e) {}
   @Override
   public void mouseDragged(MouseEvent e) {
     if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
       int dx = e.getXOnScreen() - clockPosition.x;
       int dy = e.getYOnScreen() - clockPosition.y;
 
       clockPosition = e.getLocationOnScreen();
 
       Point newLocation = getLocation();
       newLocation.translate(dx, dy);
 
       ajustWindowLocation(newLocation);
     }
   }
 
   /** create digital clock */
   public static void main(String[] args) {
     new DigitalClock();
   }
 }

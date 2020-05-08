 package ex02_02;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.TreeSet;
 import java.util.Set;
 import java.util.TimeZone;
 import java.lang.reflect.Field;
 
 class DigitalClock extends Frame implements ActionListener, Runnable {
   private DateFormat sdf;
   private TimeZone timeZone;
   private Thread thread;
   private Font clockFont;
   private Color fontColor;
   private Color clockBackColor;
 
   private static final String[] fontSizes = {"10", "20", "40", "60", "100", "150", "200", "500"};
   private static final String[] colorsStr;
 
   /* setting for colors */
   static {
     Class<Color> c = Color.class;
     Field[] fileds = c.getFields();
     Set<String> colorStrSet = new TreeSet<String>();
     for (Field filed : fileds) {
       if (filed.getDeclaringClass() == Color.class) {
         if (Character.isUpperCase(filed.getName().charAt(0))) {
           colorStrSet.add(filed.getName().toUpperCase());
         }
       }
     }
     colorsStr = new String[colorStrSet.size()];
     int index = 0;
     for (String colorStr : colorStrSet) {
       colorsStr[index++] = colorStr;
     }
   }
 
   DigitalClock() {
     super("DigitalClock");
     setResizable(false);
     setLocationRelativeTo(null);
     setBackground(clockBackColor);
     clockFont = new Font("Arial", Font.BOLD, 60);
     fontColor = Color.BLACK;
     clockBackColor = Color.WHITE;
 
     /* close window */
     addWindowListener(new WindowAdapter() {
       public void windowClosing(WindowEvent e) {
        System.exit(0);
       }
     });
 
     /* setting for a clock */
     sdf = new SimpleDateFormat("HH:mm:ss");
     timeZone = TimeZone.getTimeZone("Asia/Tokyo");
     sdf.setTimeZone(timeZone);
 
     /* add a MenuBar & Menu */
     MenuBar menuBar = new MenuBar();
     setMenuBar(menuBar);
 
     Menu menu = new Menu("Menu");
     menu.addActionListener(this);
     menuBar.add(menu);
 
     MenuItem propMenu = new MenuItem("Properties");
     menu.add(propMenu);
 
     setVisible(true);
     thread = new Thread(this);
     thread.start();
   }
 
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
 
     int x = 0;
     int y = metrics.getAscent();
 
     buffer.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
         RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
     buffer.setColor(fontColor);
     buffer.drawString(time_str, x, y);
     setBackground(clockBackColor);
 
     g.drawImage(img, insets.left, insets.top, this);
   }
 
   public void run() {
     while (true){
       repaint();
       try {
        thread.sleep(500);
       } catch (InterruptedException e) {
       }
     }
   }
 
   public void actionPerformed(ActionEvent e) {
     String actionCommand = e.getActionCommand();
 
     if (actionCommand.equals("Properties")) {
       new PropertyDialog(this, actionCommand, 500, 400);
     } else {
       // nothing to do
     }
   }
 
   private void setClockFont(String name, int style, int size) {
     clockFont = new Font(name, style, size);
   }
 
   private void setFontColor(String fontColorName) {
     fontColor = getColorInstance(fontColorName);
   }
 
   private void setBackColor(String colorName) {
     clockBackColor = getColorInstance(colorName);
   }
 
   private Color getColorInstance(String name) {
     Color ret = null;
 
     for (String colorName : colorsStr) {
       if (colorName.equals(name)) {
         Class<Color> colorClass = Color.class;
         try {
           Field filed = colorClass.getField(colorName);
           ret = (Color)filed.get(Color.WHITE);
         } catch (NoSuchFieldException e) {
           e.printStackTrace();
         } catch (IllegalAccessException e) {
           e.printStackTrace();
         }
       }
     }
 
     return ret;
   }
 
   class PropertyDialog extends Dialog implements ActionListener, ItemListener {
     private Choice fontChoice;
     private Choice fontSizeChoice;
     private Choice fontColorChoice;
     private Choice clockBackColorChoice;
 
     private String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
 
     private Font propertyFont = new Font("Arial", Font.PLAIN, 20);
 
     PropertyDialog(Frame owner, String title, int width, int height) {
       super(owner, title, true);
       setSize(width, height);
       setResizable(false);
       setLocationRelativeTo(null);
 
       /* close window */
       addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
          setVisible(false);
         }
       });
 
       setLayout(new GridLayout(6, 1));
       Panel pTitle = new Panel();
       Panel pFont = new Panel();
       Panel pFontSize = new Panel();
       Panel pFontColor = new Panel();
       Panel pClockBackColor = new Panel();
       Panel pOKButton = new Panel();
       add(pTitle);
       add(pFont);
       add(pFontSize);
       add(pFontColor);
       add(pClockBackColor);
       add(pOKButton);
 
       pTitle.setLayout(new FlowLayout());
       Label titleLabel = new Label(title);
       titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
       pTitle.add(titleLabel);
 
       pFont.setLayout(new FlowLayout());
       Label fontLabel = new Label("Font");
       fontLabel.setFont(propertyFont);
       pFont.add(fontLabel);
       fontChoice = new Choice();
       for (String font : fonts) {
         fontChoice.add(font);
       }
       fontChoice.addItemListener(this);
       pFont.add(fontChoice);
 
       pFontSize.setLayout(new FlowLayout());
       Label fontSizeLabel = new Label("Font Size");
       fontSizeLabel.setFont(propertyFont);
       pFontSize.add(fontSizeLabel);
       fontSizeChoice = new Choice();
       for (String fontSize : fontSizes) {
         fontSizeChoice.add(fontSize);
       }
       fontSizeChoice.addItemListener(this);
       pFontSize.add(fontSizeChoice);
 
       pFontColor.setLayout(new FlowLayout());
       Label fontColorLabel = new Label("Font Color");
       fontColorLabel.setFont(propertyFont);
       pFontColor.add(fontColorLabel);
       fontColorChoice = new Choice();
       for (String fontColor : colorsStr) {
         fontColorChoice.add(fontColor);
       }
       fontColorChoice.addItemListener(this);
       pFontColor.add(fontColorChoice);
 
       pClockBackColor.setLayout(new FlowLayout());
       Label clockBackColorLabel = new Label("Clock Back Color");
       clockBackColorLabel.setFont(propertyFont);
       pClockBackColor.add(clockBackColorLabel);
       clockBackColorChoice = new Choice();
       for (String clockBackColor : colorsStr) {
         clockBackColorChoice.add(clockBackColor);
       }
       clockBackColorChoice.addItemListener(this);
       pClockBackColor.add(clockBackColorChoice);
 
       fontChoice.select(clockFont.getName());
       fontSizeChoice.select(String.valueOf(clockFont.getSize()));
       fontColorChoice.select("BLACK");
       clockBackColorChoice.select("WHITE");
 
       pOKButton.setLayout(new FlowLayout());
       Button bOK = new Button("OK");
       bOK.addActionListener(this);
       pOKButton.add(bOK);
 
       setVisible(true);
     } // end PropertyDialog()
 
     public void actionPerformed(ActionEvent e) {
       String actionCommand =  e.getActionCommand();
 
       if (actionCommand.equals("OK")) {
         setVisible(false);
       }
     }
 
     public void itemStateChanged(ItemEvent e) {
       Object source = e.getSource();
 
       if (source == fontChoice) {
         String fontName = fontChoice.getSelectedItem();
         setClockFont(fontName, clockFont.getStyle(), clockFont.getSize());
       } else if (source == fontSizeChoice) {
         int fontSize = Integer.parseInt(fontSizeChoice.getSelectedItem());
         setClockFont(clockFont.getFontName(), clockFont.getStyle(), fontSize);
       } else if (source == fontColorChoice) {
         String fontColorName = fontColorChoice.getSelectedItem();
         setFontColor(fontColorName);
       } else if (source == clockBackColorChoice) {
         String backColorName = clockBackColorChoice.getSelectedItem();
         setBackColor(backColorName);
       } else {
         // this never occurs
         throw new InternalError();
       }
     }
   }
 
   // create digital clock
   public static void main(String[] args) {
     new DigitalClock();
   }
 }

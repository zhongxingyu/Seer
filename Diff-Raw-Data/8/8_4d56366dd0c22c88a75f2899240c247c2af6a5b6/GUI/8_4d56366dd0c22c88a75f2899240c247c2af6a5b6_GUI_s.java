 package phonebook;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Insets;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 @SuppressWarnings("serial")
 public class GUI extends JFrame
 {
     PhoneBook phoneBook;
 
     JPanel control;
     JPanel buttons;
     JPanel fields;
     JTextField name;
     JTextField number;
     JPanel entry;
 
     JPanel display;
     JScrollPane pane;
     JTextArea list;
     TreeView treeView;
     StatisticsView stats;
     int view;
     int VIEW_LIST = 0;
     int VIEW_TREE = 1;
     int VIEW_STATS = 2;
 
     Log log;
 
     JMenuBar menuBar;
     JMenu menuFile;
     JMenu menuView;
 
     public GUI(PhoneBook phoneBook)
     {
         this.phoneBook = phoneBook;
 
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setVisible(true);
         setBounds(50, 50, 800, 600);
         setLayout(new BorderLayout());
 
         // *entry* - the input fields and their actions
         entry = new JPanel(new BorderLayout());
         //entry = new JPanel(new GridLayout(1,2));
 
         // add
         entry.add(new AddEntry(), BorderLayout.EAST);
 
         // name
         name = new NameField();
         entry.add(name, BorderLayout.CENTER);
 
         //// number
         //number = new NumberField();
         //entry.add(number);
 
         //// find
         //entry.add(new FindButton());
 
         entry.revalidate();
         add(entry, BorderLayout.NORTH);
 
         // *display*
         display = new JPanel(new GridLayout(1,1));
 
         // list
         list = new JTextArea();
         pane = new JScrollPane(list);
         pane.setAutoscrolls(true);
         treeView = new TreeView();
         display.add(pane);
 
         add(display, BorderLayout.CENTER);
         display.revalidate();
 
         // statistics
         stats = new StatisticsView();
 
         // *log*
         log = new Log();
         add(log, BorderLayout.SOUTH);
         log.revalidate();
         repaint();
 
         // *menu*
         menuBar = new JMenuBar();
         // file menu
         menuFile = new JMenu("File");
         menuFile.setMnemonic('F');
         menuBar.add(menuFile);
         // load
         menuFile.add(new LoadOption());
         // save
         menuFile.add(new SaveOption());
 
         // edit menu
         JMenu menuEdit = new JMenu("Edit");
         menuEdit.setMnemonic('E');
         menuEdit.add(new AddRandomEntry());
         menuEdit.add(new AddRandomEntries());
         menuEdit.add(new GenerateOption());
         menuBar.add(menuEdit);
 
         // view menu
         menuView = new JMenu("View");
         menuView.setMnemonic('V');
         menuBar.add(menuView);
         menuView.add(new LogOption());
         menuView.addSeparator();
         menuView.add(new ResultOption());
         menuView.add(new TreeOption());
         menuView.addSeparator();
         menuView.add(new AllByNameOption());
         menuView.add(new AllByNumberOption());
         menuView.add(new StatisticsOption());
 
         setJMenuBar(menuBar);
         repaint();
         menuBar.revalidate();
     }
 
     void setView(int view)
     {
         this.view = view;
         display.removeAll();
         if (view == VIEW_TREE)
             display.add(treeView);
         else if (view == VIEW_LIST)
             display.add(list);
         else if (view == VIEW_STATS)
         {
             //stats.drawSpeedTests(
             display.add(stats);
         }
         display.revalidate();
         display.repaint();
     }
 
     class FindButton extends JButton implements ActionListener
     {
         public FindButton()
         {
             setText("Find");
             addActionListener(this);
         }
 
         /**
          * {@inheritDoc}
          * @see ActionListener#actionPerformed(ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             String name = phoneBook.gui.name.getText();
             list.setText("finding '" + name + "'...");
 
             //list.append("    * By Name *\n");
             PhoneBookEntry nameEntry = phoneBook.byName.find(name);
             if (nameEntry != null)
                 list.append(nameEntry.toString()+"\n");
 
             //list.append("\n\n    * By Number *\n");
             String number = phoneBook.gui.number.getText();
             PhoneBookEntry numberEntry = phoneBook.byNumber.find(number);
             if (numberEntry != null)
                 list.append(numberEntry.toString()+"\n");
 
             setView(VIEW_LIST);
         }
     }
 
     class AddEntry extends JButton implements ActionListener
     {
         public AddEntry()
         {
             setText("Add");
             //setMaximumSize(new Dimension(100, 100));
             setMnemonic('A');
             addActionListener(this);
         }
 
         /**
          * {@inheritDoc}
          * @see ActionListener#actionPerformed(ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             String[] search = GUI.this.name.getText().split("=");
             //String name = GUI.this.name.getText();
             //String number = GUI.this.number.getText();
             phoneBook.add(new PhoneBookEntry(search[0].trim(), search[1].trim()));
             phoneBook.showAll();
 
             display.repaint();
         }
     }
 
     class NameField extends JTextField implements ActionListener, KeyListener
     {
         public NameField()
         {
             setText("<name>");
             addActionListener(this);
             addKeyListener(this);
         }
 
         /**
          * {@inheritDoc}
          * @see ActionListener#actionPerformed(ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             list.setText("");
 
             String name = phoneBook.gui.name.getText();
             list.append("    * By Name * (" + name + ")\n");
             PhoneBookEntry nameEntry = phoneBook.byName.find(name);
             if (nameEntry != null)
                 list.append(nameEntry.toString()+"\n");
 
             setView(VIEW_LIST);
         }
 
         /**
          * {@inheritDoc}
          * @see KeyListener#keyTyped(KeyEvent)
          */
         public void keyTyped(KeyEvent e)
         {
         }
 
         /**
          * {@inheritDoc}
          * @see KeyListener#keyPressed(KeyEvent)
          */
         public void keyPressed(KeyEvent e)
         {
         }
 
         /**
          * {@inheritDoc}
          * @see KeyListener#keyReleased(KeyEvent)
          */
         public void keyReleased(KeyEvent e)
         {
             String name = phoneBook.gui.name.getText();
             list.setText("finding '" + name + "'...\n\n");
 
             list.append("    * By Name *\n");
             PhoneBookEntry nameEntry = phoneBook.byName.find(name.toLowerCase());
             if (nameEntry != null)
                 list.append(nameEntry.toString()+"\n");
 
             list.append("\n\n    * By Number *\n");
             PhoneBookEntry numberEntry = phoneBook.byNumber.find(name.toLowerCase());
             if (numberEntry != null)
                 list.append(numberEntry.toString()+"\n");
 
             setView(VIEW_LIST);
         }
     }
 
     class NumberField extends JTextField implements ActionListener
     {
         public NumberField()
         {
             setText("<number>");
             addActionListener(this);
         }
 
         /**
          * {@inheritDoc}
          * @see ActionListener#actionPerformed(ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             list.setText("");
 
             String number = phoneBook.gui.number.getText();
             list.append("    * By Number * (" + number + ")\n");
             PhoneBookEntry numberEntry = phoneBook.byNumber.find(number);
             if (numberEntry != null)
                 list.append(numberEntry.toString()+"\n");
 
             setView(VIEW_LIST);
         }
     }
 
     class SaveOption extends JMenuItem implements ActionListener
     {
         public SaveOption()
         {
             setText("Save");
             setMnemonic('S');
             setEnabled(true);
             addActionListener(this);
         }
 
         public void actionPerformed(ActionEvent e)
         {
             PhoneBook.saveToDisk("blah.txt", phoneBook.byName);
         }
     }
 
     class LoadOption extends JMenuItem implements ActionListener
     {
         public LoadOption()
         {
             setText("Load");
             setMnemonic('L');
             setEnabled(true);
             addActionListener(this);
         }
 
         public void actionPerformed(ActionEvent e)
         {
             phoneBook.loadFromDisk("blah.txt");
             phoneBook.showAll();
             display.repaint();
         }
     }
 
     class AddRandomEntry extends JMenuItem implements ActionListener
     {
         public AddRandomEntry()
         {
             setText("Add Random");
             setMnemonic('R');
             setEnabled(true);
             addActionListener(this);
         }
 
         /**
          * {@inheritDoc}
          * @see ActionListener#actionPerformed(ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             phoneBook.addRandom();
             phoneBook.showAll();
             display.repaint();
         }
     }
 
     class AddRandomEntries extends JMenuItem implements ActionListener
     {
         public AddRandomEntries()
         {
             setText("Add Many Random");
             setMnemonic('M');
             setEnabled(true);
             addActionListener(this);
         }
 
         /**
          * {@inheritDoc}
          * @see ActionListener#actionPerformed(ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             int count = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter count"));
             int onePercent = count/100;
             int percent = 0;
             for (int i = 1; i <= count; i++) 
             {
                 phoneBook.addRandom();
                 if (i % onePercent == 0)
                     System.out.println(++percent + "% - " + i);
             }
             System.out.println("DONE!");
 
             phoneBook.showAll();
             display.repaint();
         }
     }
 
     class GenerateOption extends JMenuItem implements ActionListener
     {
         public GenerateOption()
         {
             setText("Generate");
             setMnemonic('G');
             setEnabled(true);
             addActionListener(this);
         }
 
         public void actionPerformed(ActionEvent e)
         {
             //String[] counts = JOptionPane.showInputDialog(this, "Enter number of entries (comma separated for many).").split(",");
             String[] counts = "1000,2000,3000,4000,5000,6000,7000,8000,9000,10000".split(",");
             ArrayList<SpeedTest> tests = new ArrayList<SpeedTest>();
             
 
             int[] intCounts = new int[counts.length];
             for (int i = 0; i < counts.length; i++) 
             {
                 intCounts[i] = Integer.parseInt(counts[i].trim());
             }
 
             Arrays.sort(intCounts);
 
             for (int count : intCounts)
             {
                 System.out.println("Generating file with '" + count + "' entries.");
                 SpeedTest test = phoneBook.generateTestFile("test" + count + ".txt", count);
                 tests.add(test);
                 System.out.println("... total time: '" + test.time + "'ms.");
             }
 
             stats.tests = tests.toArray(new SpeedTest[tests.size()]);
             setView(VIEW_STATS);
         }
     }
 
     class ResultOption extends JMenuItem implements ActionListener
     {
         public ResultOption()
         {
             setText("Result");
             setMnemonic('R');
             setEnabled(true);
             addActionListener(this);
         }
 
         /**
          * {@inheritDoc}
          * @see ActionListener#actionPerformed(ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             setView(VIEW_LIST);
         }
     }
 
     class AllByNameOption extends JMenuItem implements ActionListener
     {
         public AllByNameOption()
         {
             setText("All By Name");
             setMnemonic('N');
             setEnabled(true);
             addActionListener(this);
         }
 
         /**
          * {@inheritDoc}
          * @see ActionListener#actionPerformed(ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             phoneBook.allByName();
             setView(VIEW_LIST);
         }
     }
 
     class AllByNumberOption extends JMenuItem implements ActionListener
     {
         public AllByNumberOption()
         {
             setText("All By Number");
             setMnemonic('U');
             setEnabled(true);
             addActionListener(this);
         }
 
         /**
          * {@inheritDoc}
          * @see ActionListener#actionPerformed(ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             phoneBook.allByNumber();
             setView(VIEW_LIST);
         }
     }
 
     class StatisticsOption extends JMenuItem implements ActionListener
     {
         public StatisticsOption()
         {
             setText("Statistics");
             setMnemonic('S');
             setEnabled(true);
             addActionListener(this);
         }
 
         /**
          * {@inheritDoc}
          * @see ActionListener#actionPerformed(ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             setView(VIEW_STATS);
         }
     }
 
     class TreeOption extends JMenuItem implements ActionListener
     {
         public TreeOption()
         {
             setText("Tree");
             setMnemonic('T');
             setEnabled(true);
             addActionListener(this);
         }
 
         /**
          * {@inheritDoc}
          * @see ActionListener#actionPerformed(ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             setView(VIEW_TREE);
         }
     }
 
     class LogOption extends JMenuItem implements ActionListener
     {
         public LogOption()
         {
             setText("Log");
             setMnemonic('L');
             setEnabled(true);
             addActionListener(this);
         }
 
         public void actionPerformed(ActionEvent e)
         {
             if (log.isVisible() == true)
                 log.setVisible(false);
             else
                 log.setVisible(true);
         }
     }
 
     class TreeView extends JPanel
     {
         int width, height;
         int firstLevel = 1;
         int maxLevel = 9;
         int nodeSize = 30;
         int nodeWidth = 80;
         int nodeHeight = 20;
         int widthSpacing = 50;
         int heightSpacing = 50;
 
         int startX;
 
         Graphics g;
 
         public void paintComponent(Graphics g)
         {
             super.paintComponent(g);
 
             this.g = g;
 
             width = getWidth();
             height = getHeight();
 
             startX = getWidth()/2 - nodeWidth/2;
             removeAll();
             if (phoneBook.byName.root != null)
                 drawNode(phoneBook.byName.root, 0, startX, 10, firstLevel);
         }
 
         public void drawNode(AVLTreeNode<PhoneBookEntry> node, int parentX, int x, int y, int level)
         {
             if (level < maxLevel)
             {
                 level++;
                 int offset = Math.abs(x - (parentX + x)/2);
 
                 if (node.left != null)
                 {
                     if (node != node.left.parent)
                         g.setColor(Color.RED);
                     else
                         g.setColor(Color.BLACK);
                     g.drawLine(x + nodeWidth/2, y + nodeHeight/2, x-offset + nodeWidth/2, y + heightSpacing + nodeHeight/2);
                     drawNode(node.left, x, x-offset, y + heightSpacing, level);
                 }
 
                 if (node.right != null)
                 {
                     if (node != node.right.parent)
                         g.setColor(Color.RED);
                     else
                         g.setColor(Color.BLACK);
                     g.drawLine(x + nodeWidth/2, y + nodeHeight/2, x+offset + nodeWidth/2, y + heightSpacing + nodeHeight/2);
                     drawNode(node.right, x, x+offset, y + heightSpacing, level);
                 }
             }
 
             TreeViewNode nodeButton = new TreeViewNode(node, x, y);
             add(nodeButton);
         }
 
         class TreeViewNode extends JButton implements MouseListener, MouseWheelListener
         {
             AVLTreeNode<PhoneBookEntry> node;
 
             TreeViewNode(AVLTreeNode<PhoneBookEntry> node, int x, int y)
             {
                 this.node = node;
                 addMouseListener(this);
                 addMouseWheelListener(this);
                 setMargin(new Insets(1,1,1,1));
                 setBounds(x, y, nodeWidth, nodeHeight);
                 setText(node.value.name + " - " + node.height);
             }
 
             /**
              * {@inheritDoc}
              * @see MouseListener#mouseClicked(MouseEvent)
              */
             public void mouseClicked(MouseEvent e)
             {
                 if (e.getButton() == MouseEvent.BUTTON1)
                 {
                     log.append(node.toString());
                 }
                 else if (e.getButton() == MouseEvent.BUTTON3
                         || e.getButton() == MouseEvent.BUTTON2)
                 {
                     log.append("[remove] " + node.toString());
                     phoneBook.removeByName(node.key);
                     display.repaint();
                 }
             }
 
             /**
              * {@inheritDoc}
              * @see MouseListener#mousePressed(MouseEvent)
              */
             public void mousePressed(MouseEvent e)
             {
             }
 
             /**
              * {@inheritDoc}
              * @see MouseListener#mouseReleased(MouseEvent)
              */
             public void mouseReleased(MouseEvent e)
             {
             }
 
             /**
              * {@inheritDoc}
              * @see MouseListener#mouseEntered(MouseEvent)
              */
             public void mouseEntered(MouseEvent e)
             {
             }
 
             /**
              * {@inheritDoc}
              * @see MouseListener#mouseExited(MouseEvent)
              */
             public void mouseExited(MouseEvent e)
             {
             }
 
             /**
              * {@inheritDoc}
              * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
              */
             public void mouseWheelMoved(MouseWheelEvent e)
             {
                 if (e.getWheelRotation() == 1)
                 {
                     phoneBook.byName.rotateLeft(node);
                     display.repaint();
                 }
                 else if (e.getWheelRotation() == -1)
                 {
                     phoneBook.byName.rotateRight(node);
                     display.repaint();
                 }
             }
         }
     }
 
     class StatisticsView extends JPanel
     {
         // of plotting area
         int width, height;
         int marginLeft = 50;
         int marginBottom = 50;
         int marginRight = 50;
         int marginTop = 50;
         int dotSize = 7;
         Graphics g;
         public SpeedTest[] tests;
 
         public StatisticsView()
         {
             setBackground(Color.WHITE);
         }
 
         public void paintComponent(Graphics g)
         {
             super.paintComponent(g);
             this.g = g;
             width = getWidth();
             height = getHeight();
             System.out.println("paintComponent():");
             drawSpeedTests();
         }
 
         public void drawSpeedTests()
         {
             System.out.println("drawSpeedTests():");
 
             // find min and max values
             long maxSize = 0;
             long maxTime = 0;
             long minSize = Long.MAX_VALUE;
             long minTime = Long.MAX_VALUE;
             for (SpeedTest test : tests)
             {
                 if (test.size > maxSize)
                     maxSize = test.size;
                 if (test.time > maxTime)
                     maxTime = test.time;
                 if (test.size < minSize)
                     minSize = test.size;
                 if (test.time < minTime)
                     minTime = test.time;
             }
             System.out.println("maxSize=" + maxSize + ", minSize=" + minSize + ", maxTime=" + maxTime + ", minTime=" + minTime);
 
             //minSize -= marginLeft;
             //maxSize += marginRight;
             //minTime -= marginTop;
             //maxTime += marginBottom;
 
             // plot
             int x, y, lastX = -1, lastY = -1;
             g.setColor(Color.BLACK);
             for (SpeedTest test : tests)
             {
                x = ((int)((double)test.size / (double)(maxSize) * width));
                y = height - (int)((double)test.time / (double)maxTime * height);
                //x = marginLeft + (int)((double)test.size / (double)maxSize * (width - marginLeft - marginRight));
                //x = marginLeft + ((int)((double)test.size / (double)(maxSize-minSize) * (width - marginLeft - marginRight)));
                 //y = height - (int)((double)test.time / (double)maxTime * height);
                 System.out.print("size=" + test.size + ", time=" + test.time);
                 System.out.print(", x%=" + (double)test.size/(double)maxSize + ", y%=" + (double)test.time/(double)maxTime);
                 System.out.println(", x=" + x + ", y=" + y);
                 g.fillOval(x, y, dotSize, dotSize);
                 g.drawString("size=" + test.size + " time=" + test.time, x, y);
                 if (lastX > -1)
                     g.drawLine(lastX+dotSize/2, lastY+dotSize/2, x+dotSize/2, y+dotSize/2);
                 lastX = x;
                 lastY = y;
             }
         }
     }
 
     class Log extends JPanel
     {
         JScrollPane logPane;
         JTextArea log;
 
         Log()
         {
             setVisible(false);
             setMaximumSize(new Dimension(100, 100));
             setPreferredSize(new Dimension(100, 100));
             setMinimumSize(new Dimension(100, 100));
             setLayout(new GridLayout(1,1));
             log = new JTextArea();
             append("TehE LOOOG\n\ntest\n\n");
             logPane = new JScrollPane(log);
             add(logPane);
 
             log.repaint();
             logPane.repaint();
             repaint();
 
             log.revalidate();
             logPane.revalidate();
             revalidate();
         }
 
         public void append(String message)
         {
             log.append(message + "\n");
             log.setCaretPosition(log.getText().length());
         }
     }
 }

 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
import java.awt.Toolkit;
 import java.net.URL;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 
 public class GUI2 extends JFrame {
 
     public static JLabel makeTextLabel(String text) {
         JLabel label = new JLabel(text);
         label.setFont(new Font("Courier New", 1, 15));
         label.setForeground(new Color(0, 205, 106));
         return label;
     }
 
     ListenerHandler    listener;
     JPanel             mainPanel, innerPane1, innerPane2;
     JTabbedPane        tabbedPane;
     JCheckBox          alwaysontop;
     JCheckBox          livedecode;
 
     JTextField         tf_code;
     ColoredMenuBar     menubar;
     JButton            btn_generate;
     JButton            close, minimize, small, medium, large;
     GridBagConstraints c            = new GridBagConstraints();
 
     Font               textfont     = new Font("Courier New", 1, 16);
 
     Color              yellowColor  = new Color(0, 205, 106);
 
     Font               mono         = new Font(Font.MONOSPACED, Font.PLAIN, 14);
     boolean            islivedecode = false;
     CipherPanel        cipher1;
     CipherPanel2       cipher2;
     ConvertPanel       convert;
     AboutPanel         about;
 
     public GUI2() {
         this.listener = new ListenerHandler(this);
         this.cipher1 = new CipherPanel(this.listener);
         this.cipher2 = new CipherPanel2(this.listener);
         this.convert = new ConvertPanel(this.listener);
         this.about = new AboutPanel(this.listener);
     }
 
     public void initGUI() {
 
         // init main panel components
         this.mainPanel = new JPanel(new GridBagLayout());
         this.tabbedPane = new JTabbedPane();
 
         this.mainPanel.setBackground(Color.black);
 
         this.tabbedPane.setBackground(Color.black);
         this.tabbedPane.setForeground(this.yellowColor);
         this.tabbedPane.setFont(this.textfont);
         this.tabbedPane.setUI(new CustomTabbedPaneUI());
         this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 
         // menu bar items
         this.close = new JButton(makeImageIcon("/images/close.png"));
         this.close.setRolloverIcon(makeImageIcon("/images/close_hover.png"));
         this.close.setPressedIcon(makeImageIcon("/images/close_pressed.png"));
         this.close.setBorder(BorderFactory.createEmptyBorder());
         this.close.setContentAreaFilled(false);
         this.close.setName("close");
         this.close.addMouseListener(this.listener);
         this.minimize = new JButton(makeImageIcon("/images/minimize.png"));
         this.minimize
                 .setRolloverIcon(makeImageIcon("/images/minimize_hover.png"));
         this.minimize
                 .setPressedIcon(makeImageIcon("/images/minimize_pressed.png"));
         this.minimize.setBorder(BorderFactory.createEmptyBorder());
         this.minimize.setContentAreaFilled(false);
         this.minimize.setName("minimize");
         this.minimize.addMouseListener(this.listener);
         this.large = new JButton(makeImageIcon("/images/large.png"));
         this.large.setRolloverIcon(makeImageIcon("/images/large_hover.png"));
         this.large.setPressedIcon(makeImageIcon("/images/large_pressed.png"));
         this.large.setBorder(BorderFactory.createEmptyBorder());
         this.large.setContentAreaFilled(false);
         this.large.setName("large");
         this.large.addMouseListener(this.listener);
         this.medium = new JButton(makeImageIcon("/images/medium.png"));
         this.medium.setRolloverIcon(makeImageIcon("/images/medium_hover.png"));
         this.medium.setPressedIcon(makeImageIcon("/images/medium_pressed.png"));
         this.medium.setBorder(BorderFactory.createEmptyBorder());
         this.medium.setContentAreaFilled(false);
         this.medium.setName("medium");
         this.medium.addMouseListener(this.listener);
         this.small = new JButton(makeImageIcon("/images/small.png"));
         this.small.setRolloverIcon(makeImageIcon("/images/small_hover.png"));
         this.small.setPressedIcon(makeImageIcon("/images/small_pressed.png"));
         this.small.setBorder(BorderFactory.createEmptyBorder());
         this.small.setContentAreaFilled(false);
         this.small.setName("small");
         this.small.addMouseListener(this.listener);
 
         JLabel ingressIcon = new JLabel(
                 makeImageIcon("/images/Ingress_Logo_Middle.png"));
 
         JLabel title = new JLabel("Ingress Decoder");
         title.setFont(new Font("Courier New", 1, 25));
         title.setForeground(new Color(0, 205, 106));
 
         // init menubar
         this.menubar = new ColoredMenuBar();
         this.menubar.addMouseMotionListener(this.listener);
         this.menubar.addMouseListener(this.listener);
         this.menubar.setBorder(new EmptyBorder(0, 0, 0, 0));
 
         this.menubar.add(ingressIcon);
         this.menubar.add(Box.createHorizontalStrut(10));
         this.menubar.add(title);
         this.menubar.add(Box.createHorizontalGlue());
         this.menubar.add(this.small);
         this.menubar.add(this.medium);
         this.menubar.add(this.large);
         this.menubar.add(this.minimize);
         this.menubar.add(this.close);
 
         // adding pannels to tabbed pain
         this.tabbedPane
                 .addTab("Cipher", makeImageIcon("/images/Ingress_Logo.png"),
                         this.cipher1,
                         "Reversed, Decimal to Ascii, Pattern to Binary, Morse, Caesarian Shift");
         this.tabbedPane.addTab("Cipher 2",
                 makeImageIcon("/images/Ingress_Logo.png"), this.cipher2,
                 "Atbash, Vigenere Key, Letter2Number");
         this.tabbedPane.addTab("Converter",
                 makeImageIcon("/images/Ingress_Logo.png"), this.convert,
                 "Dec2ASCII");
         this.tabbedPane.addTab("About us",
                 makeImageIcon("/images/enlightened.png"), this.about,
                 "About us");
 
         // adding components to mainpanel
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 1.0;
         this.c.gridx = 0;
         this.c.gridy = 0;
         this.c.insets = new Insets(0, 5, 0, 5);
         this.mainPanel.add(makeTextLabel("Code"), this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.01;
         this.c.gridx = 1;
         this.c.gridy = 0;
         this.c.insets = new Insets(0, 5, 0, 5);
         this.alwaysontop = new JCheckBox("Always on top");
         this.alwaysontop.setFont(this.textfont);
         this.alwaysontop.setForeground(this.yellowColor);
         this.alwaysontop.setActionCommand("onTop");
         this.alwaysontop.addActionListener(this.listener);
         this.mainPanel.add(this.alwaysontop, this.c);
 
         this.c.fill = GridBagConstraints.HORIZONTAL;
         this.c.weightx = 1.0;
         this.c.gridx = 0;
         this.c.gridy = 1;
         // this.c.gridwidth = 2;
         this.c.insets = new Insets(0, 5, 5, 5);
         this.tf_code = new JTextField();
         this.tf_code.setBackground(Color.black);
         this.tf_code.setFont(this.mono);
         this.tf_code.setForeground(this.yellowColor);
         this.tf_code.setName("code");
         this.tf_code.addCaretListener(this.listener);
         this.mainPanel.add(this.tf_code, this.c);
 
         this.c.fill = GridBagConstraints.HORIZONTAL;
         this.c.weightx = 0.01;
         this.c.gridx = 1;
         this.c.gridy = 1;
         // this.c.gridwidth = 2;
         this.c.insets = new Insets(0, 5, 5, 5);
         this.livedecode = new JCheckBox("Live Decoding");
         this.livedecode.setFont(this.textfont);
         this.livedecode.setForeground(this.yellowColor);
         this.livedecode.setActionCommand("liveDecode");
         this.livedecode.addActionListener(this.listener);
         this.mainPanel.add(this.livedecode, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 1.0;
         this.c.gridx = 0;
         this.c.gridy = 2;
         this.c.gridwidth = 2;
         this.c.insets = new Insets(0, 0, 0, 0);
         this.mainPanel.add(this.tabbedPane, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 1.0;
         this.c.gridx = 0;
         this.c.gridy = 3;
         this.c.gridwidth = 2;
         this.c.insets = new Insets(5, 5, 5, 5);
         this.btn_generate = new JButton("Generate");
         this.btn_generate.setBackground(Color.black);
         this.btn_generate.setFont(this.textfont);
         this.btn_generate.setForeground(this.yellowColor);
         this.btn_generate.setActionCommand("generate");
         this.btn_generate.addActionListener(this.listener);
         this.mainPanel.add(this.btn_generate, this.c);
 
         // adding components to frame
         add(this.menubar, BorderLayout.NORTH);
         add(this.mainPanel, BorderLayout.CENTER);
 
         // general frame settings
        setSize(new Dimension((int) (Toolkit.getDefaultToolkit()
                .getScreenSize().width * 0.66), 500));
         setIconImage(makeImageIcon("/images/Ingress_Logo_Middle.png")
                 .getImage());
         setTitle("Ingress Decoder");
         setUndecorated(true);
         setVisible(true);
     }
 
     public ImageIcon makeImageIcon(String relative_path) {
         URL imgURL = getClass().getResource(relative_path);
         return new ImageIcon(imgURL);
     }
 }

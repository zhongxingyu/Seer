 /**
  *
  */
 package org.FriendsUnited.ConfigurationCreator.Slides;
 
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ResourceBundle;
 
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import org.FriendsUnited.ConfigurationCreator.OneNextConfigurationSlide;
 import org.FriendsUnited.Util.Option.OptionCollection;
 import org.apache.log4j.Logger;
 
 /**
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class ExpertOptionsSlide extends OneNextConfigurationSlide implements ItemListener
 {
     private final Logger log = Logger.getLogger(this.getClass().getName());
     @SuppressWarnings("unused")
     private ResourceBundle msg;
     private final OptionCollection cfg;
 
     private final JTextField LookAndFeelValue;
     private final JCheckBox CommandLineBox;
     private boolean CommandLineEnabled;
     private final JCheckBox XmlTcpBox;
     private boolean XmlTcpEnabled;
     private final JCheckBox XmlTlsBox;
     private boolean XmlTlsEnabled;
 
     /**
      * @param msg
      * @param cfg
      *
      */
     public ExpertOptionsSlide(final ResourceBundle msg, final OptionCollection cfg)
     {
         super();
         this.msg = msg;
         this.cfg = cfg;
         LookAndFeelValue = new JTextField(20);
         LookAndFeelValue.setText("System");
 
         CommandLineBox = new JCheckBox("Command Line Interface");
         CommandLineBox.setSelected(false);
         CommandLineEnabled = false;
         CommandLineBox.addItemListener(this);
 
         XmlTcpBox = new JCheckBox("XML TCP Interface");
        XmlTcpBox.setSelected(false);
         XmlTcpEnabled = true;
         XmlTcpBox.addItemListener(this);
 
         XmlTlsBox = new JCheckBox("XML TLS Interface");
        XmlTlsBox.setSelected(true);
         XmlTlsEnabled = false;
         XmlTlsBox.addItemListener(this);
     }
 
     /**
      * @see org.FriendsUnited.ConfigurationCreator.ConfigurationSlide#actionOnClose()
      */
     @Override
     public final void actionOnClose()
     {
         cfg.setStringOptionToValueCreateIfAbsent("LookAndFeel", LookAndFeelValue.getText());
 
         final OptionCollection ctrlInterfOpt = cfg.getSubSectionCreateIfAbsent("ControlInterface");
         ctrlInterfOpt.setBooleanOptionToValueCreateIfAbsent("CommandLine", CommandLineEnabled);
         ctrlInterfOpt.setBooleanOptionToValueCreateIfAbsent("XmlTcp", XmlTcpEnabled);
         ctrlInterfOpt.setBooleanOptionToValueCreateIfAbsent("XmlTls", XmlTlsEnabled);
 
         // final String Server = cfg.getString("ServerIP", "127.0.0.1");
         // final int Port = cfg.getInt("ServerPort", Defaults.SSL_CONTROL_INTERFACE_PORT);
 
         // cfg.getSubSectionCreateIfAbsent("PacketReciever")
         /*
 final OptionCollection networks = cfg.getSubSectionCreateIfAbsent("Networks");
         final Option[] nets = networks.getAllOptions();
         if(null != nets)
         {
             if(0 < nets.length)
             {
                 // loop over all Elements
                 for(int i = 0; i < nets.length; i++)
                 {
                     final Option cur = nets[i];
                     if(cur instanceof StringOption)
                     {
                         final String AddressPrefix = ((StringOption)(cur)).getValue();
                         log.info("trying Subnet " + AddressPrefix);
                         final Subnet curNet = new Subnet(AddressPrefix);
                         if(true == curNet.isValid())
                         {
                             createWorker(curNet);
                         }
          */
     }
 
     /**
      * @see org.FriendsUnited.ConfigurationCreator.ConfigurationSlide#getComponent()
      */
     @Override
     public final Component getComponent()
     {
         log.debug("getting Component for Expert Options Slide");
         final JPanel slide = new JPanel();
         // GUI Settings
         final JPanel GuiPanel = new JPanel();
         GuiPanel.setBorder(BorderFactory.createTitledBorder("GUI"));
         GuiPanel.setLayout(new GridLayout(0, 2));
         final JLabel LookAndFeelLabel = new JLabel("Look and Feel :");
         GuiPanel.add(LookAndFeelLabel);
         GuiPanel.add(LookAndFeelValue);
         slide.add(GuiPanel);
         // ControlInterface
         final JPanel ControlPanel = new JPanel();
         ControlPanel.setBorder(BorderFactory.createTitledBorder("Enabled Control Interfaces"));
         ControlPanel.setLayout(new GridLayout(0, 1));
         ControlPanel.add(CommandLineBox);
         ControlPanel.add(XmlTcpBox);
         ControlPanel.add(XmlTlsBox);
         slide.add(ControlPanel);
         // Network Settings
         // TODO
         return slide;
     }
 
     @Override
     public final void updateLanguage(final ResourceBundle newMsg)
     {
         this.msg = newMsg;
     }
 
     /**
      * @see org.FriendsUnited.ConfigurationCreator.ConfigurationSlide#getName()
      */
     @Override
     public final String getName()
     {
         return "Expert Options";
     }
 
     @Override
     public final void itemStateChanged(final ItemEvent e)
     {
         final Object source = e.getItemSelectable();
 
         boolean newValue = false;
         if (e.getStateChange() == ItemEvent.DESELECTED)
         {
             // Deselected
             newValue = false;
         }
         else
         {
             // Selected
             newValue = true;
         }
 
         if (source == CommandLineBox)
         {
             CommandLineEnabled = newValue;
         }
         else if (source == XmlTcpBox)
         {
             XmlTcpEnabled = newValue;
         }
         else if (source == XmlTlsBox)
         {
             XmlTlsEnabled = newValue;
         }
 
     }
 
     @Override
     public void actionAfterShow()
     {
     }
 
 }

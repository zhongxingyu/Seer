 package org.selfbus.sbtools.sniffer.components;
 
 import java.awt.Button;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JToolBar;
 import javax.swing.SwingUtilities;
 
 import org.selfbus.sbtools.common.Config;
 import org.selfbus.sbtools.common.gui.components.Dialogs;
 import org.selfbus.sbtools.sniffer.misc.I18n;
 import org.selfbus.sbtools.sniffer.serial.Parity;
 import org.selfbus.sbtools.sniffer.serial.SerialPortUtil;
 import org.selfbus.sbtools.sniffer.serial.Stop;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A tool bar for configuration of the serial ports, including the connect/disconnect button.
  */
 public class PortToolBar extends JToolBar
 {
    private static final Logger LOGGER = LoggerFactory.getLogger(PortToolBar.class);
    private static final long serialVersionUID = 5663016237446755211L;
 
    private final Button connectButton = new Button();
    private final JComboBox<String> sendCombo = new JComboBox<String>();
    private final JComboBox<String> recvCombo = new JComboBox<String>();
    private final JComboBox<Integer> baudCombo = new JComboBox<Integer>();
    private final JComboBox<Integer> dataBitsCombo = new JComboBox<Integer>();
    private final JComboBox<Stop> stopBitsCombo = new JComboBox<Stop>();
    private final JComboBox<Parity> parityCombo = new JComboBox<Parity>();
    private final Set<ActionListener> connectListeners = new CopyOnWriteArraySet<ActionListener>();
    private final Set<ActionListener> disconnectListeners = new CopyOnWriteArraySet<ActionListener>();
    private boolean connected;
    private int numPorts;
 
    /**
     * Create a tool bar for configuration of the serial ports, including the connect/disconnect
     * button.
     */
    public PortToolBar()
    {
       this(null);
    }
 
    /**
     * Create a tool bar for configuration of the serial ports, including the connect/disconnect
     * button.
     * 
     * @param name - the name of the tool bar
     */
    public PortToolBar(String name)
    {
       super(name);
 
       add(connectButton);
 
       add(new JLabel(" " + I18n.getMessage("PortToolBar.send")));
       add(sendCombo);
       sendCombo.addActionListener(enableConnectListener);
 
       add(new JLabel(" " + I18n.getMessage("PortToolBar.recv")));
       add(recvCombo);
       recvCombo.addActionListener(enableConnectListener);
 
       add(new JLabel(" " + I18n.getMessage("PortToolBar.baud")));
       add(baudCombo);
 
       add(new JLabel(" " + I18n.getMessage("PortToolBar.dataBits")));
       add(dataBitsCombo);
 
       add(new JLabel(" " + I18n.getMessage("PortToolBar.stopBits")));
       add(stopBitsCombo);
 
       add(new JLabel(" " + I18n.getMessage("PortToolBar.parity")));
       add(parityCombo);
 
       setupComboBoxes();
 
       initConnected(false);
       connectButton.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent e)
          {
             final boolean newConnected = !isConnected();
 
             SwingUtilities.invokeLater(new Runnable()
             {
                @Override
                public void run()
                {
                   setConnected(newConnected);
                }
             });
          }
       });
    }
 
    /**
     * Setup the combo boxes.
     */
    protected void setupComboBoxes()
    {
       String[] portNames = SerialPortUtil.getPortNames();
       numPorts = portNames.length;
 
       if (numPorts > 0)
       {
          for (String portName : portNames)
          {
             recvCombo.addItem(portName);
             sendCombo.addItem(portName);
          }
       }
       else
       {
          recvCombo.addItem(I18n.getMessage("PortToolBar.noPorts"));
          sendCombo.addItem(I18n.getMessage("PortToolBar.noPorts"));
          connectButton.setEnabled(false);
       }
 
       if (numPorts == 1)
       {
          recvCombo.removeAllItems();
          recvCombo.addItem(I18n.getMessage("PortToolBar.noPorts"));
          recvCombo.setEnabled(false);
       }
 
       for (int baudRate : SerialPortUtil.getBaudRates())
          baudCombo.addItem(baudRate);
       baudCombo.setSelectedItem(19200);
 
       dataBitsCombo.addItem(5);
       dataBitsCombo.addItem(6);
       dataBitsCombo.addItem(7);
       dataBitsCombo.addItem(8);
       dataBitsCombo.setSelectedItem(8);
 
       stopBitsCombo.addItem(Stop.BITS_1);
       stopBitsCombo.addItem(Stop.BITS_1_5);
       stopBitsCombo.addItem(Stop.BITS_2);
       stopBitsCombo.setSelectedItem(Stop.BITS_1);
 
       for (Parity parity : Parity.values())
          parityCombo.addItem(parity);
       parityCombo.setSelectedItem(Parity.NONE);
    }
 
    /**
     * Initialize the combo boxes.
     * 
     * @param sendPort - the port for sending
     * @param recvPort - the port for receiving
     * @param baudRate - the baud rate
     * @param dataBits - the number of data bits
     * @param stopBits - the number of stop bits
     * @param parity - the parity
     */
    public void init(String sendPort, String recvPort, int baudRate, int dataBits, Stop stopBits, Parity parity)
    {
       sendCombo.setSelectedItem(sendPort);
       recvCombo.setSelectedItem(recvPort);
       baudCombo.setSelectedItem(baudRate);
       dataBitsCombo.setSelectedItem(dataBits);
       stopBitsCombo.setSelectedItem(stopBits);
       parityCombo.setSelectedItem(parity);
    }
 
    /**
     * Initialize the tool bar from the configuration.
     * 
     * @param config - the configuration to read
     */
    public void readConfig(Config config)
    {
       sendCombo.setSelectedItem(config.getStringValue("sendPort"));
       recvCombo.setSelectedItem(config.getStringValue("recvPort"));
       baudCombo.setSelectedItem(config.getIntValue("baudRate", 19200));
       dataBitsCombo.setSelectedItem(config.getIntValue("dataBits", 8));
       stopBitsCombo.setSelectedItem(Stop.valueOf(config.getStringValue("stopBits", "BITS_1")));
       parityCombo.setSelectedItem(Parity.valueOf(config.getStringValue("parity", "NONE")));
    }
 
    /**
     * Write the tool bar to the configuration.
     * 
     * @param config - the configuration to read
     */
    public void writeConfig(Config config)
    {
       if (numPorts > 0)
          config.put("sendPort", getSendPort());
 
      if (numPorts > 1)
          config.put("recvPort", getRecvPort());
 
       config.put("baudRate", getBaudRate());
       config.put("dataBits", getDataBits());
       config.put("stopBits", getStopBits().name());
       config.put("parity", getParity().name());
    }
 
    /**
     * @return The selected port for sending.
     */
    public String getSendPort()
    {
       if (numPorts > 0)
          return (String) sendCombo.getSelectedItem();
       return null;
    }
 
    /**
     * @return The selected port for receiving.
     */
    public String getRecvPort()
    {
       if (numPorts > 1)
          return (String) recvCombo.getSelectedItem();
       return null;
    }
 
    /**
     * @return The selected baud rate.
     */
    public int getBaudRate()
    {
       return (Integer) baudCombo.getSelectedItem();
    }
 
    /**
     * @return The selected number of data bits.
     */
    public int getDataBits()
    {
       return (Integer) dataBitsCombo.getSelectedItem();
    }
 
    /**
     * @return The selected of stop bits.
     */
    public Stop getStopBits()
    {
       return (Stop) stopBitsCombo.getSelectedItem();
    }
 
    /**
     * @return The selected parity.
     */
    public Parity getParity()
    {
       return (Parity) parityCombo.getSelectedItem();
    }
 
    /**
     * Set the connect/disconnect button's state. Calls the connect/disconnect listeners. Does
     * nothing if the current state of the button is the same as the new state.
     * 
     * @param connected - true if connected
     */
    public void setConnected(boolean connected)
    {
       if (this.connected == connected)
          return;
 
       ActionEvent ev = new ActionEvent(connectButton, ActionEvent.ACTION_PERFORMED, null);
       if (connected)
       {
          try
          {
             for (ActionListener l : connectListeners)
                l.actionPerformed(ev);
          }
          catch (RuntimeException e)
          {
             Dialogs.showExceptionDialog(e, I18n.getMessage("Error.connect"));
             LOGGER.error("connect failed", e);
          }
       }
       else
       {
          try
          {
             for (ActionListener l : disconnectListeners)
                l.actionPerformed(ev);
          }
          catch (RuntimeException e)
          {
             LOGGER.error("disconnect failed", e);
          }
       }
 
       initConnected(connected);
    }
 
    /**
     * Initialize the state of the connect/disconnect button without calling the listeners.
     * 
     * @param connected - true if connected
     */
    public void initConnected(boolean connected)
    {
       this.connected = connected;
       setEnabled(!connected && numPorts > 0);
 
       if (connected)
          connectButton.setLabel(I18n.getMessage("PortToolBar.disconnect"));
       else connectButton.setLabel(I18n.getMessage("PortToolBar.connect"));
    }
 
    /**
     * @return True if the connect/disconnect button's state is "connected". This is only the state
     *         of the button, not the real status of the connections.
     */
    public boolean isConnected()
    {
       return connected;
    }
 
    /**
     * Enable/disable all combo boxes.
     * 
     * @param enable - true to enable
     */
    @Override
    public void setEnabled(boolean enable)
    {
       sendCombo.setEnabled(enable);
       recvCombo.setEnabled(enable && numPorts > 1);
       baudCombo.setEnabled(enable);
       dataBitsCombo.setEnabled(enable);
       stopBitsCombo.setEnabled(enable);
       parityCombo.setEnabled(enable);
    }
 
    /**
     * Add a listener for connect button events.
     * 
     * @param l - the listener
     */
    public void addConnectListener(ActionListener l)
    {
       connectListeners.add(l);
    }
 
    /**
     * Remove a listener for connect button events.
     * 
     * @param l - the listener
     */
    public void removeConnectListener(ActionListener l)
    {
       connectListeners.remove(l);
    }
 
    /**
     * Add a listener for disconnect button events.
     * 
     * @param l - the listener
     */
    public void addDisconnectListener(ActionListener l)
    {
       disconnectListeners.add(l);
    }
 
    /**
     * Remove a listener for disconnect button events.
     * 
     * @param l - the listener
     */
    public void removeDisconnectListener(ActionListener l)
    {
       connectListeners.remove(l);
    }
 
    /**
     * A listener that enables/disables the connect button, depending on
     * the selection in the tool bar.
     */
    private ActionListener enableConnectListener = new ActionListener()
    {
       @Override
       public void actionPerformed(ActionEvent e)
       {
          String sendPort = getSendPort();
          String recvPort = getRecvPort();
 
          boolean enabled = numPorts > 0;
          if (sendPort == recvPort || (sendPort != null && sendPort.equals(recvPort)))
          {
             sendCombo.setBorder(BorderFactory.createLineBorder(Color.RED));
             recvCombo.setBorder(BorderFactory.createLineBorder(Color.RED));
             enabled = false;
          }
          else
          {
             sendCombo.setBorder(null);
             recvCombo.setBorder(null);
          }
 
          connectButton.setEnabled(enabled);
       }
    };
 }

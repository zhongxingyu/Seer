 /*
  * author: Max DeLiso <maxdeliso@gmail.com>
  * purpose: graphical UDP chat program 
  */
 
 package teflon;
 
 import java.awt.BorderLayout;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.PortUnreachableException;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.nio.channels.IllegalBlockingModeException;
 import java.nio.charset.Charset;
 import java.text.DateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 class Teflon {
    public static final int TEFLON_PORT = 1337;
    public static final byte[] TEFLON_RECV_ADDRESS = new byte[] { 0, 0, 0, 0 };
    public static final byte[] TEFLON_SEND_ADDRESS = new byte[] { (byte) 0xff, (byte) 0xff,
          (byte) 0xff, (byte) 0xff };
    public static final int IO_TIMEOUT_MS = 50;
    public static final int INPUT_BUFFER_LEN = 1024;
    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
 
    private TeflonRemoteHandler remoteHandler;
    private TeflonLocalHandler localHandler;
    private boolean alive;
 
    private static void reportException(Exception ex) {
      System.err.println(ex);
       ex.printStackTrace();
    }
 
    public static void main(String args[]) {
       new Teflon();
    }
 
    public static String decodeUTF8(byte[] bytes) {
       return new String(bytes, UTF8_CHARSET);
    }
 
    public static byte[] encodeUTF8(String string) {
       return string.getBytes(UTF8_CHARSET);
    }
 
    public TeflonRemoteHandler remote() {
       return remoteHandler;
    }
 
    public TeflonLocalHandler local() {
       return localHandler;
    }
 
    public synchronized boolean alive() {
       return alive;
    }
 
    public synchronized void kill() {
       alive = false;
    }
 
    public Teflon() {
       alive = new Boolean(true);
       remoteHandler = new TeflonRemoteHandler(this);
       localHandler = new TeflonLocalHandler(this);
       Thread remoteHandlerThread = new Thread(remoteHandler);
       Thread localHandlerThread = new Thread(localHandler);
 
       remoteHandlerThread.start();
       localHandlerThread.start();
 
       try {
          remoteHandlerThread.join();
          localHandlerThread.join();
       } catch (InterruptedException ie) {
          reportException(ie);
       }
 
      System.out.println("DEBUG: clean exit");
    }
 
    private class Message {
       public String id, body;
 
       public Message(String id, String body) {
          this.id = id;
          this.body = body;
       }
 
       @Override
       public String toString() {
          return id + ": " + body;
       }
    }
 
    private interface CommDestiny {
       public void queueMessage(Message msg);
    }
 
    @SuppressWarnings("serial")
    private class TeflonLocalHandler extends JFrame implements Runnable, CommDestiny {
       private static final int TEFLON_WIDTH = 512;
       private static final int TEFLON_HEIGHT = 316;
       private static final String TEFLON_TITLE = "Teflon";
 
       private Queue<Message> sendQueue = new LinkedList<Message>();
       private JTextArea outputTextArea;
       private JTextField inputTextField;
       private JPanel headerPanel;
       private JButton helpButton;
       final private Teflon parent;
 
       public TeflonLocalHandler(Teflon parent) {
          this.parent = parent;
       }
 
       private KeyListener localKeyListener = new KeyListener() {
          @Override
          public void keyPressed(KeyEvent ke) {
          }
 
          @Override
          public void keyReleased(KeyEvent ke) {
             if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                parent.remote().queueMessage(new Message("L", inputTextField.getText()));
 
                inputTextField.setText("");
             }
          }
 
          @Override
          public void keyTyped(KeyEvent ke) {
 
          }
       };
 
       private WindowListener localWindowListener = new WindowListener() {
          @Override
          public void windowActivated(WindowEvent we) {
             inputTextField.requestFocus();
          }
 
          @Override
          public void windowClosed(WindowEvent we) {
          }
 
          @Override
          public void windowClosing(WindowEvent we) {
             parent.kill();
          }
 
          @Override
          public void windowDeactivated(WindowEvent we) {
          }
 
          @Override
          public void windowDeiconified(WindowEvent we) {
          }
 
          @Override
          public void windowIconified(WindowEvent we) {
          }
 
          @Override
          public void windowOpened(WindowEvent we) {
             inputTextField.requestFocus();
          }
       };
 
       private Runnable createInitRunnable() {
          final TeflonLocalHandler teflonLocalHandler = this;
 
          return new Runnable() {
 
             @Override
             public void run() {
                headerPanel = new JPanel();
                headerPanel.setLayout(new BorderLayout());
 
                helpButton = new JButton("Help");
                headerPanel.add(BorderLayout.LINE_END, helpButton);
 
                outputTextArea = new JTextArea();
                outputTextArea.setLineWrap(true);
                outputTextArea.setEditable(false);
 
                inputTextField = new JTextField();
                inputTextField.addKeyListener(localKeyListener);
 
                teflonLocalHandler.setSize(TEFLON_WIDTH, TEFLON_HEIGHT);
                teflonLocalHandler.setTitle(TEFLON_TITLE);
                teflonLocalHandler.setLayout(new BorderLayout());
 
                teflonLocalHandler.addWindowListener(localWindowListener);
                teflonLocalHandler.add(BorderLayout.PAGE_START, headerPanel);
                teflonLocalHandler.add(BorderLayout.CENTER, new JScrollPane(outputTextArea));
                teflonLocalHandler.add(BorderLayout.PAGE_END, new JScrollPane(inputTextField));
 
                teflonLocalHandler.setVisible(true);
             }
          };
       }
 
       @Override
       public void run() {
          try {
             SwingUtilities.invokeAndWait(createInitRunnable());
          } catch (InterruptedException ie) {
             reportException(ie);
          } catch (InvocationTargetException ite) {
             reportException(ite);
          }
 
          displayMessageWithDate(new Message("system", "started up"));
 
          while (parent.alive()) {
             try {
                synchronized (sendQueue) {
                   final Message msg = sendQueue.poll();
 
                   if (msg != null) {
                      displayMessageWithDate(msg);
                   }
                }
 
                Thread.sleep(IO_TIMEOUT_MS);
             } catch (InterruptedException ie) {
                reportException(ie);
             }
          }
 
         SwingUtilities.invokeLater(createDisposalRunnable());
       }
 
       private Runnable createDisposalRunnable() {
          final TeflonLocalHandler teflonLocalHandler = this;
 
          return new Runnable() {
 
             @Override
             public void run() {
                teflonLocalHandler.dispose();
             }
          };
       }
 
       @Override
       public void queueMessage(Message msg) {
         System.out.println("in local handler with message: " + msg);
 
          synchronized (sendQueue) {
             sendQueue.add(msg);
          }
       }
 
       private void displayMessageWithDate(final Message msg) {
          SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                outputTextArea.append(DateFormat.getInstance().format(new Date()) + " : "
                      + msg.toString() + "\n");
             }
          });
       }
    }
 
    private class TeflonRemoteHandler implements Runnable, CommDestiny {
       private InetAddress listeningAddress;
       private DatagramSocket udpSocket;
       private Teflon parent;
       private Queue<Message> sendQueue = new LinkedList<Message>();
 
       public TeflonRemoteHandler(Teflon parent) {
          this.parent = parent;
       }
 
       private boolean init() {
          try {
             listeningAddress = InetAddress.getByAddress(TEFLON_RECV_ADDRESS);
             udpSocket = new DatagramSocket(TEFLON_PORT, listeningAddress);
             udpSocket.setBroadcast(true);
             udpSocket.setSoTimeout(IO_TIMEOUT_MS);
          } catch (UnknownHostException uhe) {
             reportException(uhe);
             return false;
          } catch (SocketException soe) {
             reportException(soe);
             return false;
          } catch (SecurityException se) {
             reportException(se);
             return false;
          }
 
          return true;
       }
 
       @Override
       public void run() {
          if (!init()) {
             parent.kill();
             return;
          }
 
          byte[] inputBuffer = new byte[INPUT_BUFFER_LEN];
          DatagramPacket inputDatagram = new DatagramPacket(inputBuffer, INPUT_BUFFER_LEN);
 
          while (parent.alive()) {
             try {
                udpSocket.receive(inputDatagram);
 
               System.out.println("received block with offset/length of: "
                     + inputDatagram.getOffset() + " / " + inputDatagram.getLength());
 
                final byte[] messageBytes = Arrays.copyOfRange(inputDatagram.getData(),
                      inputDatagram.getOffset(),
                      inputDatagram.getOffset() + inputDatagram.getLength());
 
                parent.local().queueMessage(new Message("", decodeUTF8(messageBytes)));
             } catch (IllegalBlockingModeException ibme) {
                reportException(ibme);
             } catch (PortUnreachableException pue) {
                reportException(pue);
             } catch (SocketTimeoutException ste) {
 
             } catch (IOException ioe) {
                reportException(ioe);
             }
 
             synchronized (sendQueue) {
                Message msg = sendQueue.poll();
 
                if (msg != null) {
                   byte[] encodedMessage = encodeUTF8(msg.toString());
 
                   try {
                      DatagramPacket outgoingPacket = new DatagramPacket(encodedMessage,
                            encodedMessage.length, InetAddress.getByAddress(TEFLON_SEND_ADDRESS),
                            TEFLON_PORT);
 
                      udpSocket.send(outgoingPacket);
                   } catch (UnknownHostException uhe) {
                      reportException(uhe);
                   } catch (IOException ioe) {
                      reportException(ioe);
                   }
                }
             }
 
          }
 
          udpSocket.close();
       }
 
       @Override
       public void queueMessage(Message msg) {
          synchronized (sendQueue) {
             sendQueue.add(msg);
          }
       }
    }
 }

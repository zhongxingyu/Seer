 package pgu;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 
 @SuppressWarnings("serial")
 public class AppUI extends JFrame {
 
     private static final String URL_BASE     = "http://localhost:8081/wikeo-core";
 
     private final JTextField    fieldUrlBase = new JTextField();
     private final JTextField    fieldUrl     = new JTextField();
     private final JButton       btnGet       = new JButton();
     private final JButton       btnPost      = new JButton();
     private final JButton       btnPut       = new JButton();
     private final JTextArea     fieldBody    = new JTextArea(10, 10);
 
     public AppUI() {
         buildAppUI();
     }
 
     private void buildAppUI() {
         setTitle("Rest");
         setSize(320, 240);
         setLocationRelativeTo(null);
         setResizable(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         setContentPane(buildRequestUI());
     }
 
     private Container buildRequestUI() {
         final JPanel requestUI = new JPanel();
         requestUI.setLayout(new BoxLayout(requestUI, BoxLayout.PAGE_AXIS));
 
         addUIUrlBase(requestUI);
         requestUI.add(Box.createRigidArea(new Dimension(0, 5)));
 
         addUIUrl(requestUI);
         requestUI.add(Box.createRigidArea(new Dimension(0, 5)));
 
         addUIBody(requestUI);
         requestUI.add(Box.createRigidArea(new Dimension(0, 5)));
 
         final JPanel btns = new JPanel();
         btns.setLayout(new FlowLayout());
 
         addUIGet(btns);
         addUIPost(btns);
         addUIPut(btns);
         requestUI.add(btns);
 
         requestUI.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
         return requestUI;
     }
 
     private void addUIBody(final JPanel requestUI) {
         final JScrollPane scroll = new JScrollPane(fieldBody);
         requestUI.add(scroll);
     }
 
     private void addUIPut(final JPanel panel) {
         btnPut.setAction(new AbstractAction("PUT") {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 ClientRest.put(getRequestConfig());
             }
         });
         panel.add(btnPut);
     }
 
     private void addUIPost(final JPanel panel) {
         btnPost.setAction(new AbstractAction("POST") {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 ClientRest.post(getRequestConfig());
             }
         });
         panel.add(btnPost);
     }
 
     private void addUIGet(final JPanel panel) {
         btnGet.setAction(new AbstractAction("GET") {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 SwingUtilities.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         final RequestConfig config = getRequestConfig();
                         final String response = ClientRest.get(config);
 
                         // TODO PGU 
                         final JTextArea responseArea = new JTextArea(50, 50);
                         responseArea.setEditable(false);
                         responseArea.setWrapStyleWord(true);
                         responseArea.setText(response);
                         System.out.println(response);
 
                         final JPanel responseUI = new JPanel();
                         responseUI.setLayout(new BoxLayout(responseUI, BoxLayout.PAGE_AXIS));
                        responseUI.add(new JScrollPane(responseArea));
 
                         final JFrame resp = new JFrame();
                         resp.setTitle("Response for " + config.url);
                         resp.setSize(320, 240);
                         resp.setLocationRelativeTo(null);
                         resp.setResizable(true);
                         resp.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                         resp.setContentPane(responseUI);
                         resp.setVisible(true);
                     }
                 });
 
             }
         });
         panel.add(btnGet);
     }
 
     private void addUIUrl(final JPanel requestUI) {
         fieldUrl.setText("");
         requestUI.add(fieldUrl);
     }
 
     private void addUIUrlBase(final JPanel requestUI) {
         fieldUrlBase.setText(URL_BASE);
         requestUI.add(fieldUrlBase);
     }
 
     private RequestConfig getRequestConfig() {
 
         final String url = fieldUrlBase.getText() + fieldUrl.getText();
         // TODO PGU  validation des valeurs
         // TODO PGU add fields for password
         // TODO PGU add fields for content type
 
         final RequestConfig config = new RequestConfig();
         config.url = url;
         config.username = "wikeo";
         config.password = "oekiw";
         config.body = fieldBody.getText();
         return config;
     }
 
 }

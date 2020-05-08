 package ru.botland.stones;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 /**
  * Created by IntelliJ IDEA.
  * User: botdes
  * Date: 09.05.2010
  * Time: 19:33:33
  * To change this template use File | Settings | File Templates.
  */
 public class AddtionalFrame extends JFrame {
     private JTextPane wordField = new JTextPane();
     private JTextPane translateField = new JTextPane();
    private KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            System.out.println(e.getKeyCode());
            if (isVisible()) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        setVisible(false);
                        break;
                    case KeyEvent.VK_ENTER:
                        throw new UnsupportedOperationException();
                }
            }
        }
    };
 
     public AddtionalFrame() throws HeadlessException {
         setSize(400, 400);
         setTitle("Translate");
         wordField.setMargin(new Insets(5, 5, 5, 5));
         translateField.setMargin(new Insets(5, 5, 5, 5));
         wordField.setEditable(false);
         translateField.setEditable(false);
         getContentPane().add(createValues(), BorderLayout.CENTER);
         getContentPane().add(createButtons(), BorderLayout.SOUTH);
         setFocusable(true);

        addKeyListener(keyAdapter);
     }
 
     private JPanel createButtons() {
         JPanel panel = new JPanel(new GridLayout(1, 2));
         panel.add(new JButton(new AbstractAction("Send") {
             @Override
             public void actionPerformed(ActionEvent e) {
                 throw new UnsupportedOperationException();
             }
         }));
         panel.add(new JButton(new AbstractAction("No") {
             @Override
             public void actionPerformed(ActionEvent e) {
                 setVisible(false);
             }
         }));
         return panel;
     }
 
     private JComponent createValues() {
         JScrollPane wordScrollPane = new JScrollPane(wordField);
         wordScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         JScrollPane translateScrollPane = new JScrollPane(translateField);
         translateScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, wordScrollPane, translateScrollPane);
         pane.setDividerLocation(0.4);
        pane.addKeyListener(keyAdapter);
         return pane;
     }
 
     public void setValues(final String word, final String translate) {
         wordField.setText(word);
         translateField.setText(translate);
         transferFocus();
     }
 
 }

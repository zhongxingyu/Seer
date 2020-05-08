 package ru.ifmo.vizi.base.ui;
 
 import ru.ifmo.vizi.base.Configuration;
 
 import java.awt.*;
 import java.awt.event.WindowEvent;
 import javax.swing.*;
 import java.util.StringTokenizer;
 
 /**
  * AboutDialog shows information about visualizer.
  * <p>
  * Used configuration parameters:
  * <table border="1">
  *      <tr>
  *          <th>Name</th>
  *          <th>Description</th>
  *      </th>
  *      <tr>
  *          <td><b>about-title</b></td>
  *          <td>Title of this about dialog</td>
  *      </tr>
  *      <tr>
  *          <td><b>about-algorithm</b></td>
  *          <td>Algorithm name (lines can be separated by <tt>\n</tt>)</td>
  *      </tr>
  *      <tr>
  *          <td><b>about-caption</b></td>
  *          <td>Caption of "about algorithm" part</td>
  *      </tr>
  *      <tr>
  *          <td><b>about-author</b></td>
  *          <td>Author's name</td>
  *      </tr>
  *      <tr>
  *          <td><b>about-author-email</b></td>
  *          <td>Author's e-mail</td>
  *      </tr>
  *      <tr>
  *          <td><b>about-author-caption</b></td>
  *          <td>Caption for "about author" part</td>
  *      </tr>
  *      <tr>
  *          <td><b>about-supervisor-email</b></td>
  *          <td>Supervisors's e-mail</td>
  *      </tr>
  *      <tr>
  *          <td><b>about-supervisor</b></td>
  *          <td>Supervisor name and e-mail</td>
  *      </tr>
  *      <tr>
  *          <td><b>about-author-caption</b></td>
  *          <td>Caption for "about supervisor" part</td>
  *      </tr>
  *      <tr>
  *          <td><b>about-button-ok</b></td>
  *          <td>Text on close (OK) button</td>
  *      </tr>
  *      <tr>
  *          <td><b>about-font</b></td>
  *          <td>Font for simple text</td>
  *      </tr>
  *      <tr>
  *          <td><b>about-caption-font</b></td>
  *          <td>Font for captions</td>
  *      </tr>
  *      <tr>
  *          <td><b>about-copyright</b></td>
  *          <td>Copyright info</td>
  *      </tr>
  * </table>
  *
  * @author  Georgiy Korneev, Vladimir Kotov
  * @version $Id: AboutDialog.java,v 1.6 2004/06/07 14:00:04 geo Exp $
  */
 public class AboutDialog extends ModalDialog {
     /**
      * Creates a new about dialog box.
      * @param config visualizer configuration.
      * @param owner the owner of this dialog.
      */
     public AboutDialog(Configuration config, JFrame owner) {
         super(owner, config.getParameter("about-title"));
 
         String algorithm = config.getParameter("about-algorithm");
         StringTokenizer tokenizer = new  StringTokenizer(algorithm, "\n");
         JLabel lines[] = new JLabel[tokenizer.countTokens()];
         for (int i = 0; i < lines.length; i++) {
             lines[i] = new JLabel(tokenizer.nextToken());
         }
         JLabel lAlgorithm = new JLabel(config.getParameter("about-algorithm-caption") + " ", JLabel.RIGHT);
 
         JLabel labelAuthor = new JLabel(config.getParameter("about-author") + " (" + config.getParameter("about-author-email") + ")");
         JLabel lAuthor = new JLabel(config.getParameter("about-author-caption") + " ", JLabel.RIGHT);
         JLabel labelSupervisor = new JLabel(config.getParameter("about-supervisor") + " (" + config.getParameter("about-supervisor-email") + ")");
         JLabel lSupervisor = new JLabel(config.getParameter("about-supervisor-caption") + " ", JLabel.RIGHT);
         JLabel labelTechnology = new JLabel(config.getParameter("about-technology") + " (" + config.getParameter("about-technology-email") + ")");
         JLabel lTechnology = new JLabel(config.getParameter("about-technology-caption") + " ", JLabel.RIGHT);
         JLabel labelAddition = new JLabel(config.getParameter("about-copyright"), JLabel.CENTER);
 
         Hinter hinter = new Hinter(config);
         add(hinter);
 
         Container panel = hinter.getContentPane();
         panel.setLayout(new GridBagLayout());
 
         GridBagConstraints gbc = new GridBagConstraints();
 
         gbc.weightx = 0.5;
         gbc.weighty = 1;
         gbc.fill = GridBagConstraints.BOTH;
         gbc.gridwidth = 1;
         gbc.gridheight = lines.length;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.anchor = GridBagConstraints.NORTH;
         panel.add(lAlgorithm, gbc);
         gbc.anchor = GridBagConstraints.CENTER;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.BOTH;
         gbc.gridwidth = GridBagConstraints.REMAINDER;
         for (int i = 0; i < lines.length; i++) {
             panel.add(lines[i], gbc);
         }
         gbc.gridwidth = 1;
         panel.add(lAuthor, gbc);
         gbc.gridwidth = GridBagConstraints.REMAINDER;
         panel.add(labelAuthor, gbc);
         gbc.gridwidth = 1;
         panel.add(lSupervisor, gbc);
         gbc.gridwidth = GridBagConstraints.REMAINDER;
         panel.add(labelSupervisor, gbc);
         gbc.gridwidth = 1;
         panel.add(lTechnology, gbc);
         gbc.gridwidth = GridBagConstraints.REMAINDER;
         panel.add(labelTechnology, gbc);
 
         panel.add(labelAddition, gbc);
 
         gbc.fill = GridBagConstraints.NONE;
         gbc.insets = new Insets(5, 0, 5, 0);
         panel.add(new HintedButton(config, "about-button-ok") {
             protected void click() {
                 AboutDialog.this.setVisible(false);
             }
         }, gbc);
 
         Font font = config.getFont("about-font");
         setFont(font);
         Font captionFont = config.getFont("about-caption-font", font);
         lAlgorithm.setFont(captionFont);
         lAuthor.setFont(captionFont);
         lSupervisor.setFont(captionFont);
         lTechnology.setFont(captionFont);
 
         setResizable(false);
         pack();
 
         center();
     }
 }

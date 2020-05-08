 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.gui.listing;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JImage;
 import javax.swing.JLabel;
 import javax.swing.JMultilineLabel;
 import javax.swing.JPanel;
 import javax.swing.JPanelWithOverlay;
 import javax.swing.RoundedBorder;
 import javax.swing.SwingConstants;
 import javax.swing.border.Border;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 import cz.cuni.mff.peckam.java.origamist.common.BinaryImage;
 import cz.cuni.mff.peckam.java.origamist.exceptions.UnsupportedDataFormatException;
 import cz.cuni.mff.peckam.java.origamist.files.File;
 import cz.cuni.mff.peckam.java.origamist.services.ServiceLocator;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ConfigurationManager;
 
 /**
  * A renderer displaying information about a {@link cz.cuni.mff.peckam.java.origamist.files.File File} object.
  * 
  * @author Martin Pecka
  */
 public class FileRenderer extends JPanelWithOverlay
 {
 
     /** */
     private static final long       serialVersionUID       = -6710327410498511670L;
 
     /** The thumbnail that belongs to the model. */
     final protected JImage          thumbnail              = new JImage(null);
 
     /** Name of the model. */
     final JLabel                    name                   = new JLabel("");
 
     /** Label for the name of the author of the model. */
     final protected JLabel          authorLabel            = new JLabel("");
 
     /** Name of the author of the model. */
     final protected JLabel          author                 = new JLabel("");
 
     /** The component containing <code>author</code> and <code>authorLabel</code> components. */
     final protected JPanel          authorPanel            = new JPanel();
 
     /** The label to show if the origami is being loaded. */
     final protected JLabel          loading                = new JLabel("");
 
     /** Short description of the model. */
     final protected JMultilineLabel desc                   = new JMultilineLabel("");
 
     /** The rounded part of the unfocused border. */
     protected static RoundedBorder  unfocusedRoundedBorder = new RoundedBorder(20, 1, Color.LIGHT_GRAY);
 
     /** The border to be used for non-focused cell. */
     protected static Border         unfocusedBorder        = BorderFactory.createCompoundBorder(
                                                                    BorderFactory.createEmptyBorder(1, 0, 2, 0),
                                                                    unfocusedRoundedBorder);
     /** The rounded part of the focused border. */
     protected static RoundedBorder  focusedRoundedBorder   = new RoundedBorder(20, 1, Color.GRAY);
 
     /** The border to be used for focused cell. */
     protected static Border         focusedBorder          = BorderFactory.createCompoundBorder(
                                                                    BorderFactory.createEmptyBorder(1, 0, 2, 0),
                                                                    focusedRoundedBorder);
 
     public FileRenderer()
     {
         super(true);
         this.setOpaque(false);
 
         FormLayout layout = new FormLayout("$dmargin,30dlu,$lcgap,[30dlu,pref,100dlu],$dmargin",
                 "$dmargin,pref,$lgap,pref,$lgap,pref,$dmargin");
         layout.setRowGroups(new int[][] { { 2, 4 } });
 
         getContent().setLayout(layout);
         getContent().setOpaque(false);
         getContent().setBackground(Color.WHITE);
 
         name.setFont(name.getFont().deriveFont(Font.BOLD));
         author.setFont(author.getFont().deriveFont(Font.PLAIN));
         authorLabel.setFont(authorLabel.getFont().deriveFont(Font.BOLD));
         desc.setFont(author.getFont());
         thumbnail.setAlignmentX(JComponent.LEFT_ALIGNMENT);
         thumbnail.setAlignmentY(JComponent.TOP_ALIGNMENT);
 
         CellConstraints cc = new CellConstraints();
 
         authorPanel.setLayout(new FormLayout("left:pref,pref", "pref"));
         authorPanel.setOpaque(false);
         authorPanel.add(authorLabel, cc.xy(1, 1));
         authorPanel.add(author, cc.xy(2, 1));
 
         getContent().add(thumbnail, cc.xywh(2, 2, 1, 5));
         getContent().add(name, cc.xy(4, 2));
         getContent().add(authorPanel, cc.xy(4, 4));
         getContent().add(desc, cc.xy(4, 6));
 
         loading.setFont(loading.getFont().deriveFont(Font.BOLD, loading.getFont().getSize() * 1.5f));
         loading.setHorizontalAlignment(SwingConstants.CENTER);
         loading.setVerticalAlignment(SwingConstants.CENTER);
         loading.setForeground(Color.WHITE);
         // TODO: the GIF doesn't show up... odd...
         // URL spinnerURL = this.getClass().getResource("/resources/images/ajax-loader.gif");
         // ImageIcon icon = new ImageIcon(spinnerURL);
         // loading.setIcon(icon);
         // icon.setImageObserver(loading);
 
         getOverlay().setLayout(new BorderLayout());
         getOverlay().add(loading, BorderLayout.CENTER);
         getOverlay().setOpaque(false);
         getOverlay().setBackground(new Color(0, 0, 0, 128));
         getOverlay().setBorder(focusedBorder); // important!!!
     }
 
     /**
      * Configure this renderer's look based on the information if it is selected or if it has focus.
      * 
      * @param file The file to be displayed.
      * @param selected Is this cell selected?
      * @param hasFocus Has this cell focus?
      */
     public void configure(File file, boolean selected, boolean hasFocus)
     {
         Locale l = ServiceLocator.get(ConfigurationManager.class).get().getDiagramLocale();
         ResourceBundle messages = ResourceBundle.getBundle("viewer", l);
 
         if (file.isOrigamiLoading()) {
             loading.setText(messages.getString("origamiLoading"));
             getOverlay().setSize(getContent().getSize()); // maybe this is not needed, but for sure
             showOverlay();
         } else {
             hideOverlay();
         }
 
         name.setText(file.getName(l));
         name.setToolTipText(name.getText());
 
         authorLabel.setText(messages.getString("authorLabel") + ": ");
         author.setText(file.getAuthor().getName());
         authorPanel.setToolTipText(author.getText());
 
         if (file.getShortdesc().size() > 0) {
             desc.setText(file.getShortDesc(l));
         } else {
             desc.setText("");
         }
 
         if (file.getThumbnail() != null) {
             thumbnail.setImage(((BinaryImage) file.getThumbnail().getImage()).getImageIcon().getImage());
         } else if (file.isOrigamiLoaded()) {
             try {
                 if (file.getOrigami().getThumbnail() != null) {
                     thumbnail.setImage(((BinaryImage) file.getOrigami().getThumbnail().getImage()).getImageIcon()
                             .getImage());
                 }
             } catch (UnsupportedDataFormatException e) {
                 assert false;
             } catch (IOException e) {
                 assert false;
             }
         }
 
         if (selected) {
             getContent().setBackground(new Color(192, 192, 255));
         } else {
             getContent().setBackground(new Color(0, 0, 0, 0));
         }
 
         if (hasFocus) {
             getContent().setBorder(focusedBorder);
         } else {
             getContent().setBorder(unfocusedBorder);
         }
     }
 
     @Override
     public String getToolTipText(MouseEvent event)
     {
         // show tooltips for overlay components if the overlay is displayed
         Component[] comps;
         if (getOverlay().isVisible()) {
             comps = getOverlay().getComponents();
         } else {
             comps = getContent().getComponents();
         }
         for (Component c : comps) {
             int x = event.getX() - c.getX();
             int y = event.getY() - c.getY();
             if (c.contains(x, y)) {
                return ((JComponent) c).getToolTipText();
             }
         }
        return super.getToolTipText(event);
     }
 
     @Override
     public Point getToolTipLocation(MouseEvent event)
     {
         // show tooltips for overlay components if the overlay is displayed
         Component[] comps;
         if (getOverlay().isVisible()) {
             comps = getOverlay().getComponents();
         } else {
             comps = getContent().getComponents();
         }
         for (Component c : comps) {
             int x = event.getX() - c.getX();
             int y = event.getY() - c.getY();
             if (c.contains(x, y)) {
                 Point loc = c.getLocation();
                 if (c == authorPanel)
                     loc.x += author.getX();
                 return loc;
             }
         }
         return super.getToolTipLocation(event);
     }
 }

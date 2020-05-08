 package gui;
 
 import images.TaggableImage;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  * Panel to render a thumbnail image with corresponding filename shown below
  * Also displays the tag of the image
  * @author mccannjame
  * jm 080912
  */
 public class ImageThumbPanel extends JPanel {
 	
 	private static final long serialVersionUID = 1L;
 	private TaggableImage image;
 	private String fileName;
 	private boolean selected;
 
 	public ImageThumbPanel(TaggableImage ti, Dimension dim) {
 		image = ti;//new ImageThumbnail(ti, new Dimension(dim.width-10, dim.height-20));
 		//tag = ti.getTag();
 		fileName = "testing.jpg"; //TODO replace with actual filenames jm 080512
 		initLayout(dim);
 	}
 	
 	public void initLayout(Dimension dim) {
 		setPreferredSize(dim);
 		setLayout(new FlowLayout());
 		
 		//image label TODO replace with actual ImageThumbail rendered image
 		JLabel imageLabel = new JLabel(new ImageIcon(image.getImage()));
 		imageLabel.setPreferredSize(new Dimension(dim.width-10, dim.height-20)); //temporary
 		add(imageLabel);
 		
 		//add string and tag below image
 		JLabel nameLabel = new JLabel();
 		nameLabel.setText(fileName);
 		add(nameLabel);
 	}
 	
 	public void toggleSelected() { selected = !selected; }
 
 }

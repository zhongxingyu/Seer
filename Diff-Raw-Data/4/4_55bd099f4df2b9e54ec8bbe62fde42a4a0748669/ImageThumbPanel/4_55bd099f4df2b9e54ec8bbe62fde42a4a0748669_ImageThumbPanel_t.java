 package gui;
 
 import images.ImageTag;
 import images.TaggableImage;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 
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
 	JLabel imageLabel = new JLabel();
 	JLabel flagLabel;
 
 	@Override
 	public String toString() {
 		return "ImageThumbPanel [fileName=" + fileName + ", selected="
 				+ selected + "]";
 	}
 	
 	public TaggableImage getImage(){return image;}
 	
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 	
 	public JLabel imageLabel(){return imageLabel;} 
 	
 	public ImageThumbPanel(TaggableImage ti, Dimension dim) {
 		image = ti;//new ImageThumbnail(ti, new Dimension(dim.width-10, dim.height-20));
 		//tag = ti.getTag();
 		if(ti!=null)
 			fileName = ti.getFileName();
 		initLayout(dim);
 	}
 	
 	public void initLayout(Dimension dim) {
 		
 		setPreferredSize(dim); setSize(dim); setMaximumSize(dim);
 		setLayout(new BorderLayout());
 		
 		if(image!=null){
			imageLabel = new JLabel(new ImageIcon(image.getImage(dim.width-10, -1)));
 			imageLabel.setPreferredSize(new Dimension(dim.width-10, dim.height-30));
 			imageLabel.setSize(new Dimension(dim.width-10, dim.height-30));
 		}
 		add(imageLabel, BorderLayout.CENTER);
 
 		//add string and tag below image
 		JPanel labelPanel = new JPanel();
 		labelPanel.setLayout(new FlowLayout());
 		JLabel nameLabel = new JLabel(fileName);
 		nameLabel.setPreferredSize(new Dimension(getSize().width,20));
 		labelPanel.add(nameLabel);
 		flagLabel = new JLabel(new ImageIcon("lib/flag20.png"));
 		labelPanel.add(flagLabel);
 		flagLabel.setVisible(image==null?false:image.getTag()==ImageTag.INFRINGEMENT);
 		add(labelPanel, BorderLayout.SOUTH);
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		flagLabel.setVisible(image==null?false:image.getTag()==ImageTag.INFRINGEMENT);
 		super.paint(g);
 	}
 	
 	
 
 }

 package images;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 /**
  * Class containing an image and an associated tag
  * of the type ImageTag
  * Images can be tagged as UNTAGGED, NONINFRINGEMENT or INFRINGMENT
  * @author James McCann (mccannjame), 300192420
  */
 
 public class TaggableImage {
 	
 	private BufferedImage image;
 	private String fileName;
 	private ImageTag tag;
 	
 	/**
 	 * Constructs a new TaggableImage with an untagged status
 	 * @param image - image stored with this taggable
 	 */
 	public TaggableImage(File f){
 		try {
 			image = ImageIO.read(f);
 			tag = ImageTag.UNTAGGED;
			setFileName(f.getName());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void setTag(ImageTag newTag) { tag = newTag; }
 	public ImageTag getTag() { return tag; }
 	public BufferedImage getImage() { return image; }
 
 	public String getFileName() {
 		return fileName;
 	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
 }

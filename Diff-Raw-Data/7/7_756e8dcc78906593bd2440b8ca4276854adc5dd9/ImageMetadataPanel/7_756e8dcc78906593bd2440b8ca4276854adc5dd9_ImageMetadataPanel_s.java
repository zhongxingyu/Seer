 package gui;
 
 import images.TaggableImage;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 
 public class ImageMetadataPanel extends JPanel {
 	
 	private static final long serialVersionUID = 1L;
 	private Dimension size;
 	private TaggableImage currentImage;
 	private JLabel metaDataLabel;
 	private JLabel mapLabel;
 
 	private static BufferedImage PLACEHOLDER_IMAGE;
 	private static JLabel placeHolderLabel;
 
 	public ImageMetadataPanel(Dimension size) {
 		this.size = size;
 		loadPlaceholder();
 		initialise();
 	}
 
 	public TaggableImage getCurrentImage() { return currentImage; }
 	public void setCurrentImage(TaggableImage img) { currentImage = img; }
 	public void setMetaDataLabelText(String text) { metaDataLabel = new JLabel(text); }
 
 	public void initialise() {
 		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		setLayout(new BorderLayout());
 		reload();
 	}
 	
 	public void reload() {
 		removeAll();
 		setPreferredSize(size);
 		setMaximumSize(size);
 		setLayout(new BorderLayout());
 		if(currentImage ==  null || metaDataLabel.getText().equals("")) {
 			//draw the placeholder
 			addPlaceholder();
 		} else {
 			//draw image metadata
 			addMetadata();
 			addLocationMap();
 		}
 	}
 	
 	public void addPlaceholder() {
 		placeHolderLabel = new JLabel(new ImageIcon(PLACEHOLDER_IMAGE.getScaledInstance(size.width, 
 				size.height, Image.SCALE_FAST)));
 		placeHolderLabel.setSize(size);
 		add(placeHolderLabel);
 	}
 	
 	public void addMetadata() {
 		setBackground(null);
 		metaDataLabel.setSize(2*size.width/3, size.height);
 		metaDataLabel.setVerticalAlignment(JLabel.TOP);
 		add(metaDataLabel, BorderLayout.WEST);
 	}
 
 	public void addLocationMap() {
 		try {
 			String latitudeS = metaDataLabel.getText().split("GPS Latitude - ", 2)[1].split("</td>", 2)[0];
 			String longitudeS = metaDataLabel.getText().split("GPS Longitude - ")[1].split("</td>",2)[0];
 
 			Double latitudeNum1 = Double.parseDouble(latitudeS.split((char) 0x00B0+"", 2)[0]);
 			Double latitudeNum2 = Double.parseDouble(latitudeS.split((char) 0x00B0+"", 2)[1].split("' ", 2)[0]);
 			Double latitudeNum3 = Double.parseDouble(latitudeS.split("' ", 2)[1].split("\"", 2)[0]);
 
 			Double longitudeNum1 = Double.parseDouble(longitudeS.split((char) 0x00B0+"", 2)[0]);
 			Double longitudeNum2 = Double.parseDouble(longitudeS.split((char) 0x00B0+"", 2)[1].split("' ", 2)[0]);
 			Double longitudeNum3 = Double.parseDouble(longitudeS.split("' ", 2)[1].split("\"", 2)[0]);
 
 			Double latitude; 
 			Double longitude;
 
 			if(latitudeNum1>=0)
 				latitude = latitudeNum1+latitudeNum2/60+latitudeNum3/3600;
 			else 
 				latitude = latitudeNum1-latitudeNum2/60-latitudeNum3/3600;
 
 			if(longitudeNum1>=0)
 				longitude= longitudeNum1+longitudeNum2/60+longitudeNum3/3600;
 			else
 				longitude= longitudeNum1-longitudeNum2/60-longitudeNum3/3600;
 
 			URLConnection con = null;
 			if(ApplicationWindow.noConnection);
 			else if(ApplicationWindow.useProxy)
 				con = new URL("http",ApplicationWindow.proxyUrl,ApplicationWindow.proxyPort,"http://maps.google.com/maps/api/staticmap?" +
 						"center="+latitude+",%20"+longitude +
 						"&zoom=7&size="
 						+size.width/3+"x"+
 						size.height+
 						"&maptype=roadmap&sensor=false&" +
 						"markers=||"+latitude+",%20"+longitude).openConnection();
 			else
 				con = new URL("http://maps.google.com/maps/api/staticmap?" +
 						"center="+latitude+",%20"+longitude +
 						"&zoom=7&size="
 						+size.width/3+"x"+
 						size.height+
 						"&maptype=roadmap&sensor=false&" +
 						"markers=||"+latitude+",%20"+longitude).openConnection();
 			InputStream is = con.getInputStream();
 			Toolkit tk = getToolkit();
 			BufferedImage map = ImageIO.read(is);
 			tk.prepareImage(map, -1, -1, null);
 			mapLabel = new JLabel(new ImageIcon(map));
 			mapLabel.setBounds(2*size.width/3, 0, size.width/3, size.height);
 			add(mapLabel,BorderLayout.EAST);
 		} catch (IOException e1) {
 			JLabel errorLabel = new JLabel("<html><p>No Internet connection</p></html>");
 			errorLabel.setVerticalAlignment(JLabel.TOP);
 			add(errorLabel,BorderLayout.EAST);
 			ApplicationWindow.noConnection = true;
 		} catch (NullPointerException e1) {
 			JLabel errorLabel = new JLabel("<html><p>No Internet connection</p></html>");
 			errorLabel.setVerticalAlignment(JLabel.TOP);
 			add(errorLabel,BorderLayout.EAST);
 		} catch (Exception e1){
 			JLabel errorLabel = new JLabel("<html><p>No Internet connection </p><p>or no valid metadata</p></html>");
 			errorLabel.setVerticalAlignment(JLabel.TOP);
 			add(errorLabel,BorderLayout.EAST);
 		}
 	}
 
 	public static void loadPlaceholder() {
 		String metadataPlaceholderPath = "lib/metadata-panel-default.png";
 		try {
 			PLACEHOLDER_IMAGE = ImageIO.read(new File(metadataPlaceholderPath));
 		} catch (IOException e) { e.printStackTrace(); }
 	}
 
 }

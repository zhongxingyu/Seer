 package virtualboyobjects;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 public class VBGraphicsEditor extends javax.swing.JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private VBRom _myRom;
 	private BufferedImage[] allImages;
 	private JPanel panel;
 	private JScrollPane scroll;
 
 	/**
 import java.nio.ByteOrder;
 	 * @param args
 	 * @throws IOException 
 	 */
 	public VBGraphicsEditor(){
 		init();	
 		loadCharacters();
 		this.add(scroll, BorderLayout.WEST);
 		this.setVisible(true);
 	}
 	
 	public static void main(String[] args) throws IOException {
 		VBGraphicsEditor editor = new VBGraphicsEditor();
 		editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 	
 	private void init(){
 		this.setLayout(new BorderLayout());
 		this.setBounds(this.getX(), this.getY(), 800, 600);
 		panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 		scroll = new JScrollPane(panel);
 		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		_myRom = new VBRom("/home/greg/VirtualBoy/RealityBoy/ram_vip.bin");
 		//_myRom = new VBRom("/home/greg/VirtualBoy/VBProgrammingDemo/ss.vb");		
 	}
 	
 	private void loadCharacters(){
 		//Get all the bytes for characters
 		byte[] bytes = _myRom.getAllCharacters();
 		allImages = new BufferedImage[(bytes.length/16)];
 		
		int scale = 4;
 		int counter = 0;
 		for(int image=0; image<(bytes.length/16); image++){
 			int x=0;
 			int y=0;
 			allImages[image] = new BufferedImage(8*scale,8*scale,BufferedImage.TYPE_INT_RGB);
 			for(int b=(16*image); b<(16*image+16); b++){
 				if(b>(16*image) && b%2==0) {
 					y+=scale;
 					x=0;
 				}
				int cell4 = ((char)bytes[b]>>6); 
 				int cell3 = ((char)bytes[b]>>4 & 0x03);
 				int cell2 = ((char)bytes[b]>>2 & 0x03);
 				int cell1 = ((char)bytes[b] & 0x03);
 				
 				for(int loop=0; loop<scale; loop++){
 					for(int loopy=0; loopy<scale;loopy++){
 						allImages[image].setRGB(x+loop, y+loopy, setColor(cell1).getRGB());
 					}
 				}
 				x+=scale;
 				for(int loop=0; loop<scale; loop++){
 					for(int loopy=0; loopy<scale;loopy++){
 						allImages[image].setRGB(x+loop, y+loopy, setColor(cell2).getRGB());
 					}
 				}
 				x+=scale;
 				for(int loop=0; loop<scale; loop++){
 					for(int loopy=0; loopy<scale;loopy++){
 						allImages[image].setRGB(x+loop, y+loopy, setColor(cell3).getRGB());
 					}
 				}
 				x+=scale;
 				for(int loop=0; loop<scale; loop++){
 					for(int loopy=0; loopy<scale;loopy++){
 						allImages[image].setRGB(x+loop, y+loopy, setColor(cell4).getRGB());
 					}
 				}
 				x+=scale;
 			}
 			
 			javax.swing.JButton b = new javax.swing.JButton();			
 			b.setBorderPainted(false);
 			b.setContentAreaFilled(false);
 			b.setBounds(b.getX(), b.getY(), 8*scale, 8*scale);
 			b.setIcon(new ImageIcon(allImages[image]));
 			b.setSize(8*scale, 8*scale);
 			b.setVisible(true);
 			b.setToolTipText(Integer.toString(counter));
 			counter++;
 			panel.add(b);
 		}
 	}
 	
 	private Color setColor(int i){
 		Color c = Color.BLACK;
 		if(i == 3) c = new Color(255,0,0);
 		if(i == 2) c = new Color(150,0,0);
 		if(i == 1) c = new Color(75,0,0);
 		
 		return c;
 	}
 }

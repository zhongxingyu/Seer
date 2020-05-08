 import java.awt.AWTException;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Image;
 import java.awt.MouseInfo;
 import java.awt.PointerInfo;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.zip.GZIPOutputStream;
 
 import javax.imageio.ImageIO;
 
 public class ImageSender {
 
 	private int header_size = 8;
 	private int max_packets = 255;
 	private int session_start = 1;
 	private int session_end = 2;
 	private int Max_Datagram_size = 65507 - header_size;
 	private int max_session_number = 255;
 	private DatagramSocket congestionControl;
 	private volatile boolean canRun = true;
 	private String output_format = "jpeg";
 	private String IP_ADDRESS = null;
 	private int PORT = 6550;
 	private boolean SHOW_MOUSEPOINTER = true;
 	private DatagramSocket frameSocket;
 	private boolean gotMobIpAddress = false;
 	private byte[] receiveBuffer;
 	private boolean cansend = true;
 	private int congestionPort = 6551;
 	public static int COLOUR_OUTPUT = BufferedImage.TYPE_INT_RGB;
 
 	public BufferedImage getScreenshot() throws AWTException, IOException {
 
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		Dimension screenSize = toolkit.getScreenSize();
 
 		Rectangle screenRect = new Rectangle(screenSize);
 
 		Robot robot = new Robot();
 		BufferedImage image = robot.createScreenCapture(screenRect);
 
 		return image;
 	}
 
 	public byte[] bufferedImageToByteArray(BufferedImage image, String format)
 			throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ImageIO.write(image, format, baos);
 
 		return baos.toByteArray();
 	}
 
 	private void sendImage(byte[] imageData, String IpAddress, int port) {
 		InetAddress ia = null;
 
 		try {
 			ia = InetAddress.getByName(IpAddress);
 
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 
 		}
 
 		try {
 
 			DatagramPacket dp = new DatagramPacket(imageData, imageData.length,
 					ia, PORT);
 			frameSocket.send(dp);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 
 		}
 
 	}
 	
 	public static BufferedImage scale(BufferedImage source, int w, int h) {
 		Image image = source
 				.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING);
 		BufferedImage result = new BufferedImage(w, h, COLOUR_OUTPUT);
 		Graphics2D g = result.createGraphics();
 		g.drawImage(image, 0, 0, null);
 		g.dispose();
 		return result;
 		
 		
 	}
 
 	/**
 	 * Shrinks a BufferedImage
 	 * 
 	 * @param source Image to shrink
 	 * @param factor Scaling factor
 	 * @return Scaled image
 	 */
 	public static BufferedImage shrink(BufferedImage source, double factor) {
 		int w = (int) (source.getWidth() * factor);
 		int h = (int) (source.getHeight() * factor);
 		return scale(source, w, h);
 	}
 
 	public void RemoteDesktopEngine() {
 
 		int sessionNumber = 0;
 		receiveBuffer = new byte[256];
 
 		try {
 
 			congestionControl = new DatagramSocket(congestionPort);
 			frameSocket = new DatagramSocket();
 
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 
 		try {
 
 			while (!gotMobIpAddress) {
 				DatagramPacket dp = new DatagramPacket(receiveBuffer,
 						receiveBuffer.length);
 				congestionControl.receive(dp);
 
 				String s = new String(dp.getData(), 0, dp.getLength(), "UTF-8");
 
 				if (s.equals("send")) {
 					IP_ADDRESS = dp.getAddress().toString().replace("/", "");
 					// System.out.println("ip " + IP_ADDRESS);
 
 					gotMobIpAddress = true;
 				}
 			}
 
 			while (canRun) {
 				BufferedImage image;
 
 				image = getScreenshot();
 
 				if (SHOW_MOUSEPOINTER) {
 					PointerInfo p = MouseInfo.getPointerInfo();
 					int mouseX = p.getLocation().x;
 					int mouseY = p.getLocation().y;
 
 					Graphics2D g2d = image.createGraphics();
 
 					g2d.setColor(Color.black);
 					Polygon polygon1 = new Polygon(new int[] { mouseX,
 							mouseX + 10, mouseX, mouseX }, new int[] { mouseY,
 							mouseY + 10, mouseY + 15, mouseY }, 4);
 
 					Polygon polygon2 = new Polygon(new int[] { mouseX + 1,
 							mouseX + 10 + 1, mouseX + 1, mouseX + 1 },
 							new int[] { mouseY + 1, mouseY + 10 + 1,
 									mouseY + 15 + 1, mouseY + 1 }, 4);
 					g2d.setColor(Color.black);
 					g2d.fill(polygon1);
 
 					g2d.setColor(Color.black);
 					g2d.fill(polygon2);
 					g2d.dispose();
 				}
 
 				//image = shrink(image, 1);	
 				byte[] imageByteArray = bufferedImageToByteArray(image,
 						output_format);
 				
 				ByteArrayOutputStream out=new ByteArrayOutputStream();
 				GZIPOutputStream zipOut=new GZIPOutputStream(out);
 				zipOut.write(imageByteArray);
 				zipOut.close();
 				
 				imageByteArray=out.toByteArray();
 
 				int packets = (int) Math.ceil(imageByteArray.length
 						/ (float) Max_Datagram_size);
 
				 //System.out.println("total size "+imageByteArray.length);
				 //System.out.println("packets "+packets);
 
 				if (packets > max_packets) {
 					System.out.println("Image is too large to be transmitted!");
 					continue;
 				}
 
 				for (int i = 0; i <= packets; i++) {
 					int flags = 0;
 
 					flags = (i == 0) ? session_start : flags;
 					flags = (i + 1) * Max_Datagram_size > imageByteArray.length ? session_end
 							: flags;
 					int size = (flags != session_end) ? Max_Datagram_size
 							: imageByteArray.length - i * Max_Datagram_size;
 
					/*
					 * System.out.println("Size "+size);
					 * System.out.println("Flags "+flags);
					 * System.out.println("max size "+(DATAGRAM_MAX_SIZE>>8) );
					 */
 
 					byte[] data = new byte[header_size + size];
 					data[0] = (byte) flags;
 					data[1] = (byte) sessionNumber;
 					data[2] = (byte) packets;
 					data[3] = (byte) (Max_Datagram_size >> 8);
 					data[4] = (byte) Max_Datagram_size;
 					data[5] = (byte) i;
 					data[6] = (byte) (size >> 8);
 					data[7] = (byte) size;
 
 					System.arraycopy(imageByteArray, i * Max_Datagram_size,
 							data, header_size, size);
 					
 					//System.out.println("inside image send");
 
 					sendImage(data, IP_ADDRESS, PORT);
 
 					cansend = false;
 
 					while (!cansend) {
 
 						byte[] buffer1 = new byte[512];
 						DatagramPacket dp = new DatagramPacket(buffer1,
 								buffer1.length);
 						congestionControl.receive(dp);
 						String s = new String(dp.getData(), 0, dp.getLength(),
 								"UTF-8");
 
 						if (s.equals("send")) {
 
 							cansend = true;
 						}
 
 					}
 
 					if (flags == session_end)
 						break;
 
 				}
 
 				sessionNumber = sessionNumber < max_session_number ? ++sessionNumber
 						: 0;
 				Runtime.getRuntime().gc();
 
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void destroy() {
 		if (gotMobIpAddress == false)
 			gotMobIpAddress = true;
 
 		canRun = false;
 		congestionControl.close();
 		frameSocket.close();
 
 	}
 
 }

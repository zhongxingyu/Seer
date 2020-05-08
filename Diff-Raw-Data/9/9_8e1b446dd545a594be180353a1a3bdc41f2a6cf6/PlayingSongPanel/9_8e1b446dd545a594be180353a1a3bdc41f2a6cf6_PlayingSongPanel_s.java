 package com.raysmond.view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.HeadlessException;
 import java.awt.Image;
 import java.awt.Paint;
 import java.awt.Toolkit;
 import java.awt.Transparency;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FilenameFilter;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 
 import com.raysmond.song.Song;
 import com.raysmond.tag.JAudioTagger;
 import com.raysmond.util.Util;
 
 /**
  * ʾڲŵĸϢ
  * Ϣ: ,,ר
  * @author Jiankun Lei
  *
  */
 public class PlayingSongPanel extends JPanel {
 	private static final long serialVersionUID = 9210352850971415856L;
 	
 	Song song = null;
 	JLabel album = null;
 	JLabel labelTitle = new JLabel("");
 	JLabel labelArtist = new JLabel("");
 	JLabel labelAlbumName = new JLabel("ר");
 	
 	private Color infoColor = Util.getThemeDefaultColor();
 	private Color titleColor = Util.getThemeHighlightColor();
 	
 	public static int WIDTH = 300;
 	public static int HEIGHT = 120;
 	
 	public PlayingSongPanel(){
 		init();
		setSong(null);
 	}
 	
 	public PlayingSongPanel(Song song){
 		init();
		if(null==song) return;
		setSong(song);
 		showInfo(song);
 	}
 	
 	/**
 	 * ʾһڲŵĸ
 	 * @param song
 	 */
 	public void showInfo(Song song){
 		loadSongInfo();
 		loadDefaultAlbumImage();
 		loadAlbumImage();
 	}
 	
 	/**
 	 * ڲŵĸı,,ר
 	 */
 	private void loadSongInfo() {
 		labelTitle.setText(null);
 		if(song.getTitle()!=null&&!song.getTitle().isEmpty()){
 			labelTitle.setText(song.getTitle());
 		}
 		else{
 			labelTitle.setText(Song.getFileNameNoEx(song.getFileName()));
 		}
 		labelTitle.setToolTipText(Song.getFileNameNoEx(song.getFileName()));
 		String artist = song.getArtist();
 		if(artist!=null){
 			if(!artist.isEmpty()) artist = "֣" + artist;
 			else artist = "֣δ֪";
 		}
 		else artist = "֣δ֪";
 		String albumName = song.getAlbum();
 		if(albumName!=null){
 			if(!albumName.isEmpty()) albumName = "ר" + albumName;
 			else albumName = "֣δ֪";
 		}
 		else albumName = "֣δ֪";
 		labelArtist.setText(artist);
 		labelAlbumName.setText(albumName);
 		
 		//ʾϢҪǿǵʱר̫
 		labelAlbumName.setToolTipText(albumName);
 		
 	}
 
 	public void init(){
 		this.setOpaque(false);
 		setLayout(null);
 		setSize(WIDTH,HEIGHT);
 		
 		album = new JLabel();
 		JPanel albumPanel = new JPanel();
 		albumPanel.setOpaque(false);
 		
 		final JPanel titlePanel = new JPanel();
 		titlePanel.setOpaque(false);
 		albumPanel.add(album);
 		titlePanel.add(labelTitle);
 		titlePanel.add(labelArtist);
 		titlePanel.add(labelAlbumName);
 		
 		albumPanel.setBounds(10, 1, 100, 100);
 		titlePanel.setBounds(120, 0, 180, 100);
 		
 		labelAlbumName.setPreferredSize(new Dimension(180,24));
 		labelAlbumName.setFont(Util.songInfoFont);
 		
 		labelArtist.setPreferredSize(new Dimension(180,24));
 		labelArtist.setFont(Util.songInfoFont);
 		
 		labelTitle.setPreferredSize(new Dimension(180,24));
 		labelTitle.setFont(Util.songTitleFont);
 		labelTitle.setForeground(titleColor);
 		labelTitle.setVisible(true);
 		
 		labelArtist.setForeground(infoColor);
 		labelAlbumName.setForeground(infoColor);
 		
 		FlowLayout flow = new FlowLayout();
 		flow.setAlignment(FlowLayout.LEFT);
 		titlePanel.setLayout(flow);
 		
 		add(albumPanel);
 		add(titlePanel);
 		
 		album.addMouseListener(new MouseAdapter(){
 			public void mouseEntered(MouseEvent arg0) {
 				labelTitle.setForeground(Color.red);
 				}
 			public void mouseExited(MouseEvent arg0) {
 				labelTitle.setForeground(Util.songTitleColor);
 				}
 			});
 	}
 	
 	/**
 	 * רͼƬ
 	 */
 	public void loadAlbumImage(){
 		Image img = null;
 		try{
 			img = JAudioTagger.getAlbumArt(new File(song.getLocation()));
 			}catch(Exception e){
 			}
 		
 		//ܻȡרͼƬ쳣׳
 		if(img==null) {return;}
 		
 		//ıͼƬߴ
 		img = fitSize(img,Util.albumHeight);
 		album.setIcon(new ImageIcon(img));
 	}
 	
 
 	/**
 	 * ݸ߶ȰıͼƬߴ
 	 * @param src
 	 * @param newHeight
 	 * @return
 	 */
 	public static Image fitSize(Image src, int newHeight){
 		return scaleImage(src,((double)newHeight) / (double)toBufferedImage(src).getHeight());
 	}
 	
 	/**
 	 * ݱıͼƬߴ
 	 * @param src
 	 * @param scale
 	 * @return
 	 */
 	public static Image scaleImage(Image src, double scale){
 		Image des = null;
 		BufferedImage bufferImg = toBufferedImage(src);
 		int width = (int) (bufferImg.getWidth() * scale);
 		int height = (int) (bufferImg.getHeight() * scale);
 		des = src.getScaledInstance(width, height, Image.SCALE_DEFAULT);
 		return des;
 	}
 	/**
 	 * һImage͵ĶתΪBufferedImageͶ
 	 * @param image
 	 * @return
 	 */
 	public static BufferedImage toBufferedImage(Image image) {
 		if (image instanceof BufferedImage) {
 		    return (BufferedImage)image;
 		 }
 		 image = new ImageIcon(image).getImage();
 		 BufferedImage bimage = null;
 		 GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		try {
 		     int transparency = Transparency.OPAQUE;
 		     GraphicsDevice gs = ge.getDefaultScreenDevice();
 		     GraphicsConfiguration gc = gs.getDefaultConfiguration();
 		     bimage = gc.createCompatibleImage(
 			 image.getWidth(null), image.getHeight(null), transparency);
 		 } catch (HeadlessException e) {
 		 }
 
 		if (bimage == null) {
 		    int type = BufferedImage.TYPE_INT_RGB;
 		     bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
 		 }
 
 		 Graphics g = bimage.createGraphics();
 
 		 g.drawImage(image, 0, 0, null);
 		 g.dispose();
 		return bimage;
 	}
 	
 	/**
 	 * Ĭϵרļѡȡһ
 	 * @return
 	 */
 	public ImageIcon randomDefaultAlbum(){
 		//гĬרļ
 		File[] files = Util.albumDir.listFiles(new FilenameFilter(){
 			@Override
 			public boolean accept(File arg0, String arg1) {
 				return arg1.endsWith("jpg");  
 			}});
 		
 		System.out.println("Ĭϵר"+files.length);
 		if(files.length==0) return null;
 		int ramIndex = (int)(Math.random()*(files.length-1));
 		System.out.println("ѡרǣ" + files[ramIndex].getPath());
 		return new ImageIcon(files[ramIndex].getPath());
 	}
 	/**
 	 * Ĭר
 	 */
 	public void loadDefaultAlbumImage(){
 		ImageIcon defaultAlbumImage = randomDefaultAlbum();
 		album.setIcon(defaultAlbumImage);
 	}
 	
 	
 	public boolean isSetSong(){
 		if(song!=null) return true;
 		return false;
 	}
 	
 	public Song getSong() {
 		return song;
 	}
 
 	public void setSong(Song song) {
 		this.song = song;
 	}
 	
 	public void changeTheme(){
 		titleColor = Util.getThemeHighlightColor();
 		infoColor = Util.getThemeDefaultColor();
 		if(labelTitle!=null) labelTitle.setForeground(titleColor);
 		if(labelArtist!=null) labelArtist.setForeground(infoColor);
 		if(labelAlbumName!=null) labelAlbumName.setForeground(infoColor);
 	}
 }

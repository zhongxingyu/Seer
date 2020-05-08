 package com.quiltplayer.view.swing.labels;
 
 import java.awt.AlphaComposite;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.Timer;
 
 import com.quiltplayer.core.repo.spotify.JotifyRepository;
 import com.quiltplayer.external.covers.model.ImageSizes;
 import com.quiltplayer.external.covers.util.ImageUtils;
 import com.quiltplayer.model.Album;
 import com.quiltplayer.model.impl.NullAlbum;
 import com.quiltplayer.model.jotify.JotifyAlbum;
 import com.quiltplayer.properties.ImageProperties;
 import com.quiltplayer.view.swing.images.QImageIcon;
 
 /**
  * 
  * 
  * @author Vlado Palczynski
  * 
  */
 public class PlaylistImageLabel extends JLabel implements ActionListener {
     private static final long serialVersionUID = 1L;
 
     private Album album;
 
     private int imageCounter = 0;
 
     private JotifyRepository jotifyRepository;
 
     private Timer timer = new Timer(20, this);
 
     private Double alpha = 1.0;
 
     public PlaylistImageLabel(final Album album, final JotifyRepository jotifyRepository) {
         this.album = album;
         this.jotifyRepository = jotifyRepository;
 
         createImageIcon(album);
 
         addMouseListener(increaseListener);
 
         setHorizontalAlignment(JLabel.CENTER);
         setVerticalAlignment(JLabel.CENTER);
 
     }
 
     private void createImageIcon(final Album album) {
         if (album instanceof NullAlbum)
             try {
                 this.setIcon(new ImageIcon(ImageProperties.LOGO_PATH.getURL()));
             }
             catch (IOException e) {
                 e.printStackTrace();
             }
         else if (album instanceof JotifyAlbum) {
             try {
                // BUG Nullpointer when selecting album 1st time, 2:nd is ok
                Image image = jotifyRepository.getInstance().image(
                        ((JotifyAlbum) album).getSpotifyAlbum().getCover());
 
                 this.setIcon(new ImageIcon(ImageUtils.scalePicture(image, ImageSizes.LARGE
                         .getSize())));
             }
             catch (Exception e) {
                 e.printStackTrace();
                 this.setIcon(new QImageIcon("No cover", ImageSizes.LARGE.getSize()));
             }
         }
         else if (album.getImages() != null && album.getImages().size() > 0)
 
             this.setIcon(new ImageIcon(ImageUtils.scalePicture(new ImageIcon(album.getImages().get(
                     imageCounter).getLargeImage().getAbsolutePath()).getImage(), ImageSizes.LARGE
                     .getSize())));
         else
             this.setIcon(new QImageIcon("No cover", ImageSizes.LARGE.getSize()));
     }
 
     private MouseListener increaseListener = new MouseAdapter() {
         @Override
         public void mouseReleased(MouseEvent e) {
             if (e.getButton() == MouseEvent.BUTTON1) {
                 ++imageCounter;
 
                 if (imageCounter >= album.getImages().size()) {
                     imageCounter = 0;
                 }
             }
             else {
                 --imageCounter;
 
                 if (imageCounter < 0) {
                     imageCounter = album.getImages().size() - 1;
                 }
 
             }
 
             setIcon(new ImageIcon(ImageUtils.scalePicture(new ImageIcon(album.getImages().get(
                     imageCounter).getLargeImage().getAbsolutePath()).getImage(), ImageSizes.LARGE
                     .getSize())));
 
             // timer.start();
         }
     };
 
     public void update(final Album album) {
         imageCounter = 0;
         this.album = album;
 
         createImageIcon(album);
 
         // timer.start();
     }
 
     public int getCounter() {
         return imageCounter;
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         alpha += 0.10;
 
         if (alpha > 1) {
             timer.stop();
             alpha = 0.0;
         }
         else {
             this.repaint();
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.swing.JComponent#paint(java.awt.Graphics)
      */
     @Override
     public void paint(Graphics g) {
         Graphics2D g2d = (Graphics2D) g;
         g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.floatValue()));
 
         super.paint(g);
     }
 }

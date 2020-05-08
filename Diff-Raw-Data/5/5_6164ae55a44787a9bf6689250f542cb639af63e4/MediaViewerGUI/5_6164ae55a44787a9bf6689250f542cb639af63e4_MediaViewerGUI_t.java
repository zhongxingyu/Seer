 package mediastore;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import javax.swing.*;
 
 public class MediaViewerGUI extends JFrame implements ActionListener {
 
     private Database db;
     private JLabel author;
     private JLabel title;
     private JLabel duration;
     private JLabel genre;
     private JLabel rating;
     private JLabel totalReviews;
     private JLabel price;
     private JLabel ranking;
     private JLabel coverLabel;
     private ImageIcon cover;
     private JPanel infoPanel;
     private JPanel buttonPanel;
     private JPanel imagePanel;
     private JButton preview;
     private JButton buy;
     private Media media;
     private Customer customer;
 
     public MediaViewerGUI( Media m, Customer c, boolean managerMode ) throws java.io.IOException {
 
         super( "Mediastore" );
 
 
         addWindowListener( new MediaViewerGUIExitHandler( managerMode ) );
 
         JPanel contentPane = new JPanel();
 
         setContentPane( contentPane );
         contentPane.setLayout( null );
         db = MediaStoreGUI.db;
         media = m;
         customer = c;
         setLayout( new BorderLayout( 10, 10 ) );
 
 
 
         cover = db.viewCoverImage( m );
         coverLabel = new JLabel( cover );
         author = new JLabel( "Author: " + m.getAuthor() + " | " );
         title = new JLabel( "Title: " + m.getTitle() + " | " );
         duration = new JLabel( "Duration: " + m.getDuration() + " | " );
         genre = new JLabel( "Genre: " + m.getGenre() + " | " );
         rating = new JLabel( "Rating: " + m.getRating() + " | " );
         totalReviews = new JLabel( "Total Reviews: " + m.getTotalReviews() + " | " );
         price = new JLabel( "Price: $" + m.getPrice() + " | " );
         ranking = new JLabel( "Ranking: " + m.getRanking() );
         preview = new JButton( "Preview" );
         buy = new JButton( "Buy" );
 
 
 
         buttonPanel = new JPanel();
         if ( !managerMode ) {
             buttonPanel.add( buy );
         }
         buttonPanel.add( preview );
 
 
         buy.addActionListener( this );
         preview.addActionListener( this );
 
 
         infoPanel = new JPanel();
 
         JPanel titlePanel = new JPanel();
         title.setFont( new Font( "Sans", Font.PLAIN, 14 ) );
         author.setFont( new Font( "Sans", Font.PLAIN, 14 ) );
         duration.setFont( new Font( "Sans", Font.PLAIN, 14 ) );
         genre.setFont( new Font( "Sans", Font.PLAIN, 14 ) );
         rating.setFont( new Font( "Sans", Font.PLAIN, 14 ) );
         price.setFont( new Font( "Sans", Font.PLAIN, 14 ) );
         ranking.setFont( new Font( "Sans", Font.PLAIN, 14 ) );
         totalReviews.setFont( new Font( "Sans", Font.PLAIN, 14 ) );
         title.setForeground( Color.white );
         author.setForeground( Color.white );
         duration.setForeground( Color.white );
         genre.setForeground( Color.white );
         rating.setForeground( Color.white );
         price.setForeground( Color.white );
         ranking.setForeground( Color.white );
         totalReviews.setForeground( Color.white );
 
         infoPanel.add( title );
         infoPanel.add( author );
         infoPanel.add( duration );
         infoPanel.add( genre );
         infoPanel.add( rating );
         infoPanel.add( totalReviews );
         infoPanel.add( price );
         infoPanel.add( ranking );
         imagePanel = new JPanel();
         Image img = cover.getImage();
         imagePanel.add( new JLabel( scale( img, (int) ( ( (double) img.getWidth( null ) / (double) img.getHeight( null ) ) * 350.0 ), 350 ) ) );
 
 
         add( imagePanel, BorderLayout.CENTER );
         add( buttonPanel, BorderLayout.NORTH );
         add( infoPanel, BorderLayout.SOUTH );
 
 
     }
 
     @Override
     public void actionPerformed( ActionEvent e ) {
         try {
             if ( e.getSource() == preview ) {
                Media result = db.preview( media );
                if ( result == null ) {
                    JOptionPane.showMessageDialog( null, "No preview available.", "", JOptionPane.ERROR_MESSAGE );
                }
             }
             if ( e.getSource() == buy ) {
                 //customer.buy( media.getID() );
                 int result = MediaStoreGUI.loggedInCustomer.buy( media.getID() );
                 MediaStoreGUI.reloadDB();
                 if ( result == -1 ) {
                     JOptionPane.showMessageDialog( null, "Insufficient funds to buy this item.", "", JOptionPane.ERROR_MESSAGE );
                     return;
                 }
                 JOptionPane.showMessageDialog( null, "Item has been purchased!", "MEDIA PURCHASED", JOptionPane.INFORMATION_MESSAGE );
 
             }
         } catch ( IOException ex ) {
         }
     }
 
     private class MediaViewerGUIExitHandler extends WindowAdapter {
 
         private boolean managerMode;
 
         public MediaViewerGUIExitHandler( boolean managerMode ) {
             this.managerMode = managerMode;
         }
 
         @Override
         public void windowClosing( WindowEvent e ) {
 
             MediaStoreGUI.customerScreen( managerMode );
         }
     }
 
     private ImageIcon scale( Image src, int width, int height ) {
         int w = width;
         int h = height;
         int type = BufferedImage.TYPE_INT_RGB;
         BufferedImage dst = new BufferedImage( w, h, type );
         Graphics2D g2 = dst.createGraphics();
         g2.drawImage( src, 0, 0, w, h, this );
         g2.dispose();
         return new ImageIcon( dst );
     }
 }

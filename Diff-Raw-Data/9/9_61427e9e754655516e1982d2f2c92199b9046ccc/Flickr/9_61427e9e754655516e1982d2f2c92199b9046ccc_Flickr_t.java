 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package flickr;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
import java.awt.color.CMMException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.IIOException;
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.border.Border;
 import javax.xml.stream.XMLStreamException;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  *
  * @author peixoton
  */
 public class Flickr extends JFrame implements ActionListener {
     
     public static final double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
     public static final double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
     
     public String whereAmI;
     public String whereIWas;
     public ArrayList<String> historique;
     public ArrayList<String> favorites;
     
     public static int posX;
     public static int posY;
     
     public static final String RECHERCHE = "Recherche";
     public static final String RESULTATS = "Résultats";
     public static final String ZOOM = "Zoom";
     public static final String FAVORIS = "Favoris";
     
     public Container contenuFenetre;
     public JPanel panelConnexion;
     public JPanel panelSearch;
     public JPanel imagePanel;
     public JPanel panelPhotoZoomed;
     public static final int LARGEUR_FENETRE = 600;
     public static final int HAUTEUR_FENETRE = 400;
     public static final String api_key = "5ba9bb9bbac0804efaccd0f9d5b4b756";
     public Recherche recherche;
     public ParserXML parser;
     public ArrayList<Photo> lesPhotos;
     public int page = 1;
     public static final int nbParPage = 25;
 
     /* Ecran de connexion */
     public JLabel hint_identifiant;
     public JLabel hint_password;
     public JTextField identifiant;
     public JPasswordField password;
     public JButton submit_connexion;
     public JButton submit_forgot;
     /* Model connexion */
     public Connexion connexion_token;
     
     /* Ecran de recherche */
     public JLabel hint_recherche;
     public JTextField field_recheche;
     public JButton bouton_recherche;
     
     /* Ecran des photos */
     
     public int nbCol = 5;
     public int nbLig = 5;
     public int widthImage = 75;
     public int heightImage = 75;
     public JButton images[];
     public JButton retourRecherche;
     
     /* Zoom sur Photo */
     
     public JButton photoZoomed;
     public JButton redirectWeb;
     public Photo laPhoto;
     
     /* Menu */
     
     public JPanel panelMenu;
     public JButton goToSearch;
     public JButton goToResults;
     public JButton goToWeb;
     public JButton showInfos;
     public JButton showHistory;
     public JButton save;
     public JLabel bgMenuTop;
     public JLabel bgMenuBot;
     public JButton nextPage;
     public JButton previousPage;
     public JButton favoritesList;
     public JButton addToFavorites;
     public JComboBox listeHistorique;
     public static final Color menuColor = new Color(17, 16, 16);
     public int menuWidth = 41;
     
     /* Overlay chargement */
     
     public JPanel loadingPanel;
     public JLabel loadingGif;
     
 
     public Flickr() throws MalformedURLException, IOException {
         
         fetchHistorique();
         
         this.imagePanel = new JPanel();
         this.panelMenu = new JPanel();
         this.panelConnexion = new JPanel();
         this.panelSearch = new JPanel();
         this.panelPhotoZoomed = new JPanel();
         
         this.contenuFenetre = this.getContentPane();
         this.contenuFenetre.setLayout(null);
         
         this.panelMenu();
         this.ecranRecherche();
         
         this.whereAmI = Flickr.RECHERCHE;
         this.page = 1;
 
         setUndecorated(true);
         setVisible(true);
         setTitle("Flickr Project Application");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setResizable(false);
         setLayout(null);
         
     }
     
     public void loadingPanel() throws IOException{
         try {
             
             this.loadingPanel = new JPanel();
             this.loadingPanel.setLayout(null);
             this.loadingPanel.setSize( this.getWidth(), this.getHeight());
             
             this.loadingGif = new JLabel(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/ajax-loader.gif"))));
             this.loadingGif.setSize(128, 15);
             this.loadingGif.setLocation((int)(this.getWidth() / 2) - (int)(128/2), (int)(this.getHeight() /2) - (int)(15 / 2));
             
             this.loadingPanel.add(this.loadingGif);
             this.loadingPanel.setOpaque(true);
             this.loadingPanel.setBackground(new Color(0,0,0,50));
             
             this.contenuFenetre.add(this.loadingPanel, new Integer(1), 0);
             
         } catch (MalformedURLException ex) {
             Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
         }
         
     }
     
     public void reagenceLoadingPanel(){
         
         this.loadingPanel.setSize( this.getWidth(), this.getHeight());
         this.loadingGif.setLocation((int)(this.getWidth() / 2) - (int)(128/2), (int)(this.getHeight() /2) - (int)(15 / 2));
         //revalidate();
         repaint();
             
     }
 
     public void ecranConnexion() {
 
         /*int width = 400;
         int height = 75;
         int x = this.LARGEUR_FENETRE / 2 - width / 2;
         int y = this.HAUTEUR_FENETRE / 2 - height / 2;
 
         this.panelConnexion = new JPanel();
         this.panelConnexion.setLayout(new GridLayout(3, 2));
         this.panelConnexion.setBounds(x, y, width, height);
 
         this.hint_identifiant = new JLabel("Entrez votre identifiant...");
         this.hint_password = new JLabel("Entrez votre mot de passe...");
 
         this.identifiant = new JTextField();
         this.identifiant.setText("Entrez votre identifiant");
         this.password = new JPasswordField();
 
         this.submit_connexion = new JButton("Connexion");
         this.submit_forgot = new JButton("Mot de passe oublié?");
 
         this.panelConnexion.add(hint_identifiant);
         this.panelConnexion.add(identifiant);
         this.panelConnexion.add(hint_password);
         this.panelConnexion.add(password);
         this.panelConnexion.add(submit_connexion);
         this.panelConnexion.add(submit_forgot);
 
         this.submit_connexion.addActionListener(this);
         this.submit_forgot.addActionListener(this);*/
 
     }
     
     /**
      * Ajoute le panel de menu sur la gauche.
      * Ajoute également l'événement permettant de déplacer la fenêtre sur le menu.
      * @throws MalformedURLException
      * @throws IOException 
      */
     public void panelMenu() throws MalformedURLException, IOException{
         this.panelMenu = new JPanel();
         this.panelMenu.setLayout(null);
         
         this.bgMenuTop = new JLabel(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/menu_top.png"))));
         this.bgMenuBot = new JLabel(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/menu_bot.png"))));
         this.goToSearch = new JButton(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/icone_recherche.png"))));
         this.save = new JButton(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/icone_sauvegarder.png"))));
         this.showHistory = new JButton(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/icone_historique.png"))));
         this.showInfos = new JButton(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/icone_informations.png"))));
         this.goToWeb = new JButton(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/icone_web.png"))));
         this.goToResults = new JButton(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/icone_retour.png"))));
         this.nextPage = new JButton(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/icone_suivant.png"))));
         this.previousPage = new JButton(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/icone_retour.png"))));
         this.favoritesList = new JButton(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/icone_list_favoris.png"))));
         this.addToFavorites = new JButton(new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/icon_ajouter_favoris.png"))));
         
         this.panelMenu.add(bgMenuTop);
         this.panelMenu.add(bgMenuBot);
         this.panelMenu.add(goToSearch);
         this.panelMenu.add(save);
         this.panelMenu.add(showHistory);
         this.panelMenu.add(showInfos);
         this.panelMenu.add(goToWeb);
         this.panelMenu.add(goToResults);
         this.panelMenu.add(nextPage);
         this.panelMenu.add(previousPage);
         this.panelMenu.add(favoritesList);
         this.panelMenu.add(addToFavorites);
         
         this.bgMenuTop.setLocation(0,0);
         this.bgMenuTop.setSize(41, 11);
         this.bgMenuBot.setSize(41, 11);
         
         this.goToSearch.setSize(13, 10);
         this.goToSearch.setLocation(14, 12);
         this.goToSearch.setBorder(BorderFactory.createEmptyBorder());
         this.goToSearch.setBackground(menuColor);
         this.goToSearch.setForeground(menuColor);
         
         this.favoritesList.setSize(16, 16);
         this.favoritesList.setLocation(12, 32);
         this.favoritesList.setBorder(BorderFactory.createEmptyBorder());
         this.favoritesList.setBackground(menuColor);
         
         this.showHistory.setSize(13, 13);
         this.showHistory.setLocation(14, 32);
         this.showHistory.setBorder(BorderFactory.createEmptyBorder());
         this.showHistory.setBackground(menuColor);
         
         this.save.setSize(13, 14);
         this.save.setLocation(14, 55);
         this.save.setBorder(BorderFactory.createEmptyBorder());
         this.save.setBackground(menuColor);
         
         this.goToWeb.setSize(13, 13);
         this.goToWeb.setLocation(12, 79);
         this.goToWeb.setBorder(BorderFactory.createEmptyBorder());
         this.goToWeb.setBackground(menuColor);
         
         this.showInfos.setSize(5, 10);
         this.showInfos.setLocation(16, 102);
         this.showInfos.setBorder(BorderFactory.createEmptyBorder());
         this.showInfos.setBackground(menuColor);
         
         this.goToResults.setSize(13, 11);
         this.goToResults.setLocation(12, 122);
         this.goToResults.setBorder(BorderFactory.createEmptyBorder());
         this.goToResults.setBackground(menuColor);
         
         this.addToFavorites.setSize(16, 16);
         this.addToFavorites.setLocation(12, 143);
         this.addToFavorites.setBorder(BorderFactory.createEmptyBorder());
         this.addToFavorites.setBackground(menuColor);
         
         this.nextPage.setSize(13, 11);
         this.nextPage.setLocation(12, 55);
         this.nextPage.setBorder(BorderFactory.createEmptyBorder());
         this.nextPage.setBackground(menuColor);
         
         this.previousPage.setSize(13, 11);
         this.previousPage.setLocation(12, 76);
         this.previousPage.setBorder(BorderFactory.createEmptyBorder());
         this.previousPage.setBackground(menuColor);
         
         this.contenuFenetre.add(this.panelMenu);
         
         this.panelMenu.setSize(41, 11);
         this.panelMenu.setBackground(menuColor);
         this.panelMenu.setVisible(true);
         
         this.goToSearch.addActionListener(this);
         this.goToWeb.addActionListener(this);
         this.goToResults.addActionListener(this);
         this.nextPage.addActionListener(this);
         this.previousPage.addActionListener(this);
         this.save.addActionListener(this);
         this.showInfos.addActionListener(this);
         this.showHistory.addActionListener(this);
         this.favoritesList.addActionListener(this);
         this.addToFavorites.addActionListener(this);
         
         this.panelMenu.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e){
               Flickr.posX=e.getX();
               Flickr.posY=e.getY();
            }
         });
         
         this.panelMenu.addMouseMotionListener(new MouseAdapter(){
              @Override
              public void mouseDragged(MouseEvent evt){
                 //sets frame position when mouse dragged			
                 setLocation (evt.getXOnScreen()-Flickr.posX,evt.getYOnScreen()-Flickr.posY);
              }
         });
         
     }
     
     /**
      * Adapte la hauteur du menu à la fenêtre.
      * @param height: Hauteur de la fenêtre. 
      */
     public void reagenceMenu(int height){
         this.bgMenuBot.setLocation( 0, (height - 11));
         this.panelMenu.setSize( this.menuWidth, height );
         
         System.err.println(whereAmI);
         
         if(this.whereAmI == Flickr.RECHERCHE){
             this.goToSearch.setVisible(false);
             this.save.setVisible(false);
             this.goToWeb.setVisible(false);
             this.showInfos.setVisible(false);
             this.goToResults.setVisible(false);
             this.favoritesList.setVisible(true);
             this.nextPage.setVisible(false);
             this.addToFavorites.setVisible(false);
             this.previousPage.setVisible(false);
             this.showHistory.setLocation( 14, 12 );
             this.favoritesList.setLocation(12, 32);
         }else if(this.whereAmI == Flickr.RESULTATS || this.whereAmI == Flickr.FAVORIS){
             this.addToFavorites.setVisible(false);
             this.goToResults.setVisible(false);
             this.favoritesList.setVisible(true);
             this.goToSearch.setVisible(true);
             this.favoritesList.setLocation(12, 97);
             if(this.lesPhotos.size() == Flickr.nbParPage && this.whereAmI != Flickr.FAVORIS)
                 this.nextPage.setVisible(true);
             if(this.page > 1 && this.whereAmI != Flickr.FAVORIS){
                 this.previousPage.setVisible(true);
             }
             this.goToWeb.setVisible(false);
             this.save.setVisible(false);
             this.showInfos.setVisible(false);
             this.showHistory.setLocation(14, 32);
             
             if(this.whereAmI == Flickr.FAVORIS){
                 this.favoritesList.setLocation(12, 55);
             }
             
         }else if(this.whereAmI == Flickr.ZOOM){
             if(this.whereIWas != Flickr.FAVORIS)
                 this.addToFavorites.setVisible(true);
             this.favoritesList.setVisible(true);
             if(this.whereIWas == Flickr.FAVORIS){
                 this.favoritesList.setLocation(12,143);
             }else{
                 this.favoritesList.setLocation(12,166);
             }
             this.goToSearch.setVisible(true);
             this.save.setVisible(true);
             this.goToWeb.setVisible(true);
             this.showInfos.setVisible(true);
             this.goToResults.setVisible(true);
             this.nextPage.setVisible(false);
             this.previousPage.setVisible(false);
         }
     }
     
     public void ecranRecherche() throws MalformedURLException, IOException{
         /* Ecran d'accueil */
         int width = 400;
         int height = 200;
         
         this.page = 1;
         
         this.whereAmI = Flickr.RECHERCHE;
         
         Border emptyBorder = BorderFactory.createEmptyBorder();
         
         this.panelSearch = new JPanel();
         this.panelSearch.setLayout(null);
         this.panelSearch.setBounds(0, 0, width, height);
         this.panelSearch.setBackground( Flickr.menuColor );
         
         /*JLabel test = new JLabel( "Chargement...", new ImageIcon(ImageIO.read(new URL("http://www.howdoyousay.fr/FlickrAPI/Images/ajax-loader.gif"))), JLabel.CENTER);
         this.panelSearch.add(test);
         test.setSize(400, 200);
         test.setBackground(new Color(0, 0, 0, 50));*/
         
         this.hint_recherche = new JLabel("RECHERCHER SUR FLICKR", JLabel.CENTER);
         this.field_recheche = new JTextField();
         this.bouton_recherche = new JButton("RECHERCHER");
         
         this.field_recheche.setColumns(20);
         this.panelSearch.add(this.hint_recherche);
         this.panelSearch.add(this.field_recheche);
         this.panelSearch.add(this.bouton_recherche);
         this.hint_recherche.setBounds(60, 40, 280, 16);
         this.hint_recherche.setAlignmentX(JLabel.CENTER);
         this.hint_recherche.setFont(new Font("Verdana", Font.PLAIN, 16));
         this.hint_recherche.setForeground(Color.white);
         this.field_recheche.setBounds(60, 85, 280, 33);
         this.field_recheche.setBorder(emptyBorder);
         this.field_recheche.setBorder(
             BorderFactory.createCompoundBorder(
                 this.field_recheche.getBorder(), 
                 BorderFactory.createEmptyBorder(5, 5, 5, 5)
             )
         );
         this.bouton_recherche.setBounds(60, 120, 280, 33);
         this.bouton_recherche.setFont(new Font("Verdana", Font.PLAIN, 16));
         this.bouton_recherche.setForeground(Color.white);
         this.bouton_recherche.setBackground(new Color(255, 0, 128));
         this.bouton_recherche.setBorder(emptyBorder);
         
         this.bouton_recherche.addActionListener(this);
         
         this.bouton_recherche.setFocusPainted(false);
         
         this.contenuFenetre.add(this.panelSearch);
         
         String path = "http://www.howdoyousay.fr/FlickrAPI/Images/bg.png";
         URL url = new URL(path);
         BufferedImage image = ImageIO.read(url);
         JLabel fond = new JLabel(new ImageIcon(image));
         this.retourRecherche = new JButton(new ImageIcon(image));
         this.panelSearch.add(fond);
         fond.setBounds(0, 0, width, height);
         this.panelSearch.setLocation( this.menuWidth, 0 );
         
         reagenceMenu(height);
         
         setLocation((int)(Flickr.screenWidth/2) - ((width + this.menuWidth) / 2), (int)(Flickr.screenHeight/2) - (height / 2));
         setSize(width + this.menuWidth, height);
     }
     
     public void ecranImages(){
         /* Liste des résultats */
         try {
             
             if(this.whereAmI != Flickr.FAVORIS)
                 this.whereAmI = Flickr.RESULTATS;
             
             int width = this.nbCol * this.widthImage;
             int height = this.nbLig * this.heightImage;
             
             this.imagePanel = new JPanel();
             this.imagePanel.setBackground(Color.BLACK);
             this.imagePanel.setLayout(null);
             this.imagePanel.setBounds(0, 0, width + this.nbCol + 1 + this.menuWidth, height + this.nbLig + 1);
             
             this.images = new JButton[this.nbCol * this.nbLig];
             
             Border emptyBorder = BorderFactory.createEmptyBorder();
             
             int compteurImages = 0;
             
             for(int i = 0; i < this.nbLig; i++){
                 
                 for(int j = 0; j < this.nbCol; j++){
                     
                     if(compteurImages < this.lesPhotos.size() ){
                     
                         String extension = this.lesPhotos.get(compteurImages).photo_url_thumb.substring(this.lesPhotos.get(compteurImages).photo_url_thumb.lastIndexOf(".") + 1, this.lesPhotos.get(compteurImages).photo_url_thumb.length());
                         System.err.println(extension);
                         String path = this.lesPhotos.get(compteurImages).photo_url_thumb;
                         URL url = new URL(path);
                         System.err.println(url);
                         try{
                             BufferedImage image = ImageIO.read(url);
                             JButton button = new JButton(new ImageIcon(image));
                             this.images[compteurImages] = button;
                             this.images[compteurImages].setBorder(emptyBorder);
                             this.imagePanel.add(this.images[compteurImages]);
                             this.images[compteurImages].setLocation( (this.widthImage * i + ( 1 + i) ), this.heightImage * j + (1 + j) );
                             this.images[compteurImages].setSize(this.widthImage, this.heightImage);
                             this.images[compteurImages].addActionListener(this);
                         }catch(IllegalArgumentException ex){
                            System.err.println("Erreur bizarre.");
                         }catch(IIOException test){
                            System.err.println("Bad URL.");
                        }catch(CMMException test2){
                            System.err.println("Format de l'image invalide.");
                         }finally{
 
                             compteurImages++;
                         
                         }
                     
                     }else{
                         
                         break;
                         
                     }
                     
                 }
                 
                 if(compteurImages < this.lesPhotos.size()){
                     
                 }else{
                     
                     break;
                     
                 }
                 
             }
             
             this.imagePanel.setLocation( this.menuWidth, 0);
             reagenceMenu(height + this.nbLig + 1);
             
             setLocation((int)(Flickr.screenWidth/2) - ((width + this.menuWidth) / 2), (int)(Flickr.screenHeight/2) - (height / 2));
             setSize(width + this.nbCol + 1 + this.menuWidth, height + this.nbLig + 1);
             
         } catch (Exception exp) {
             exp.printStackTrace();
         }
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == submit_connexion) {
             
         } else if (e.getSource() == submit_forgot) {
             
             try {
                 
                 /* J'ai oublié mon mot de passe */
                 this.actionForgottenPassword();
                         
             } catch (URISyntaxException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             }
                     
         }else if(e.getSource() == this.save){
             
             this.actionSaveImage();
             
         }else if(e.getSource() == this.addToFavorites){
             
             actionAddToFavorites();
             
         }else if(e.getSource() == this.showHistory){
             
             try {
                 this.searchFromHistory();
             } catch (IOException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             }
             
         }else if(e.getSource() == this.favoritesList){
             
             try {
                 
                 this.whereAmI = Flickr.FAVORIS;
                 this.fetchFavorites();
                 this.loadFromFavorites();
                 this.imagePanel.setVisible(false);
                 this.panelPhotoZoomed.setVisible(false);
 
                 if(this.lesPhotos.size() > 0){
                     this.panelSearch.setVisible(false);
                     this.ecranImages();
                     this.imagePanel.setVisible(true);
                     this.contenuFenetre.add(this.imagePanel);
                     repaint();
                 }
                 
             } catch (FileNotFoundException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IOException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             }
             
         }else if(e.getSource() == this.bouton_recherche){
             
             /* Soumission d'une recherche */
             this.performSearch(this.field_recheche.getText(), true);
             
         }else if(e.getSource() == this.showInfos){
             
             this.actionDisplayInfos();
             
         }else if(e.getSource() == this.goToSearch){
             
             this.goBackToSearch();
             
         }else if( e.getSource() == this.nextPage ){
             
             System.err.println(this.field_recheche.getText());
             System.err.println("Prev page:" + this.page);
             this.page++;
             System.err.println("" + this.page);
             this.performSearch(this.field_recheche.getText(), false);
             
         }else if( e.getSource() == this.previousPage ){
             
             this.page--;
             this.performSearch(this.field_recheche.getText(), false);
             
         }else if( e.getSource() == this.goToResults){
             
             /* Retour à la liste des photos */
             this.whereAmI = this.whereIWas;
 
             int width = this.nbCol * this.widthImage + 1 + this.nbCol;
             int height = this.nbLig * this.heightImage + 1 + this.nbLig;
             setLocation((int)(Flickr.screenWidth/2) - ((width + this.menuWidth) / 2), (int)(Flickr.screenHeight/2) - (height / 2));
             setSize(width + this.menuWidth, height);
             this.panelPhotoZoomed.setVisible(false);
             this.imagePanel.setVisible(true);
             this.reagenceMenu(height);
                 
         }else if(e.getSource() == this.goToWeb){
             
             try {
                 this.actionDisplayInBrowser();
             } catch (URISyntaxException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             }
             
         }else if(e.getSource() instanceof JButton ){
             /* Clique sur une photo de la liste ou une photo zoomée */
             JButton image = new JButton();
             Photo photo = new Photo();
             int found = 0;
             
             for(int i = 0; i < this.nbCol * this.nbLig; i++){
                 if(e.getSource() == this.images[i]){
                     image = this.images[i];
                     this.laPhoto = this.lesPhotos.get(i);
                     found = 1;
                 }
             }
             
             if(found == 1){
                 try {
                     /* Zoom sur une photo */
                     
                     this.whereIWas = this.whereAmI;
                     this.whereAmI = Flickr.ZOOM;
                     String path;
                     if(this.screenWidth >= 1024 && this.screenHeight >= 1024 )
                         path = this.laPhoto.photo_url_big;
                     else
                         path = this.laPhoto.photo_url;
                     System.out.println("Get Image from " + path);
                     URL url = new URL(path);
                     BufferedImage current_image = ImageIO.read(url);
                     ImageIcon theImage = new ImageIcon(current_image);
                     this.photoZoomed = new JButton(theImage);
                     Border emptyBorder = BorderFactory.createEmptyBorder();
                     this.photoZoomed.setBorder(emptyBorder);
                     this.imagePanel.setVisible(false);
                     this.panelPhotoZoomed = new JPanel();
                     this.panelPhotoZoomed.setLayout(null);
                     this.panelPhotoZoomed.add(this.photoZoomed);
                     this.photoZoomed.addActionListener(this);
                     this.photoZoomed.setBounds(0, 0, theImage.getIconWidth(), theImage.getIconHeight());
                     
                     /*this.redirectWeb = new JButton("Afficher dans mon navigateur");
                     this.panelPhotoZoomed.add( this.redirectWeb );
                     this.redirectWeb.setBounds(0, theImage.getIconHeight(), theImage.getIconWidth(), 33 );
                     this.redirectWeb.setBackground(new Color(255, 0, 128));
                     this.redirectWeb.setForeground(Color.white);
                     this.redirectWeb.setBorder(emptyBorder);
                     this.redirectWeb.setFont(new Font("Verdana", Font.PLAIN, 16));
                     this.redirectWeb.addActionListener(this);*/
 
                     this.panelPhotoZoomed.setVisible(true);
                     this.panelPhotoZoomed.setBounds(0, 0, theImage.getIconWidth() + this.menuWidth, theImage.getIconHeight());
                     this.contenuFenetre.add(this.panelPhotoZoomed);
                     
                     this.panelPhotoZoomed.setLocation( this.menuWidth, 0 );
                     
                     reagenceMenu(theImage.getIconHeight());
                     
                     setLocation((int)(Flickr.screenWidth/2) - ((theImage.getIconWidth() + this.menuWidth) / 2), (int)(Flickr.screenHeight/2) - ((theImage.getIconHeight()) / 2));
                     setSize( theImage.getIconWidth() + this.menuWidth, theImage.getIconHeight() );
                 } catch (MalformedURLException ex) {
                     Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (IOException ex) {
                     Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 
             }else{
                 /* Retour à la liste des photos */
                 
                 this.whereAmI = this.whereIWas;
                 
                 int width = this.nbCol * this.widthImage + 1 + this.nbCol;
                 int height = this.nbLig * this.heightImage + 1 + this.nbLig;
                 setLocation((int)(Flickr.screenWidth/2) - ((width + this.menuWidth) / 2), (int)(Flickr.screenHeight/2) - (height / 2));
                 setSize(width + this.menuWidth, height);
                 this.panelPhotoZoomed.setVisible(false);
                 this.imagePanel.setVisible(true);
                 this.reagenceMenu(height);
             }
             
             
         }
     }
 
     public static void main(String[] args) throws MalformedURLException, IOException, SAXException, URISyntaxException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
         Flickr application = new Flickr();
         Connexion test = new Connexion("Bob", "Marley");
     }
     
     public void fetchHistorique() throws FileNotFoundException, IOException{
         
         this.historique = null;
         this.historique = new ArrayList<String>();
         FileInputStream fStream = null;
         fStream = new FileInputStream("historique.txt");
         DataInputStream in = new DataInputStream(fStream);
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         String strLine;
         while ((strLine = br.readLine()) != null){
             this.historique.add(strLine);
         }
         Collections.reverse(this.historique);
         
     }
     
     public void writeInFile(String motClef, String fichier){
         
         /*DateFormat dateFormat = new SimpleDateFormat("dd/MM/YY HH:mm");
         Calendar cal = Calendar.getInstance();*/
         
         FileWriter fStream = null;
         try {
             fStream = new FileWriter(fichier, true);
             fStream.append( motClef );
             fStream.append(System.getProperty("line.separator"));
             fStream.flush();
             fStream.close();
         } catch (IOException ex) {
             Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 fStream.close();
             } catch (IOException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
     
     public void saveImage(String imageUrl, String destinationFile) throws IOException {
         URL url = new URL(imageUrl);
         InputStream is = url.openStream();
         OutputStream os = new FileOutputStream(destinationFile);
 
         byte[] b = new byte[2048];
         int length;
 
         while ((length = is.read(b)) != -1) {
                 os.write(b, 0, length);
         }
 
         is.close();
         os.close();
     }
     
     public void performSearch(String motClef, boolean saveSearch){
         try {
             
             /*reagenceLoadingPanel();*/
             this.lesPhotos = null;
             this.images = null;
             this.imagePanel.setVisible(false);
             this.panelPhotoZoomed.setVisible(false);
             
             recherche = new Recherche(motClef, this.page, this.nbParPage);
             this.lesPhotos = Recherche.lesPhotos;
             if(this.lesPhotos.size() > 0){
                 this.panelSearch.setVisible(false);
                 this.ecranImages();
                 this.imagePanel.setVisible(true);
                 this.contenuFenetre.add(this.imagePanel);
                 if(saveSearch == true)
                     writeInFile(this.field_recheche.getText(), "historique.txt");
                 //revalidate();
                 repaint();
             }else{
                 this.page = 1;
                 this.panelSearch.setVisible(false);
                 remove(this.panelSearch);
                 this.ecranRecherche();
                 this.panelSearch.setVisible(true);
                 //revalidate();
                 repaint();
                 String choixPossibles[] = {"Réessayer", "Nouvelle recherche"};
                 int retour = JOptionPane.showOptionDialog(this, "La recherche n'a donné aucun résultat. Veuillez réessayer en changeant les mots-clefs (cela peut provenir d'une surcharge des serveurs).", "Recherche", 0, 0, null, choixPossibles, choixPossibles[1]); 
                 if(retour == 0){
                     this.performSearch(motClef, false);
                 }
                 /*JOptionPane.showMessageDialog(panelConnexion, "La recherche n'a donné aucun résultat. Veuillez réessayer en changeant les mots-clefs (cela peut provenir d'une surcharge des serveurs).");*/
             }
 
 
         } catch (URISyntaxException ex) {
             Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SAXException ex) {
             Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
         } catch (XMLStreamException ex) {
             Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public void searchFromHistory() throws IOException{
         
         this.fetchHistorique();
         
         String s = (String)JOptionPane.showInputDialog(
                     this, 
                     "Voici l'historique de vos recherches récentes", "Historique", 1, null, this.historique.toArray(), hint_identifiant
         );
         
         if( s != null ){
             
             this.performSearch(s, false);
             
         }
         
     }
     
     public void openUrl(String url) throws URISyntaxException{
         URI uri = new URI(url);
         Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
         if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
             try {
                 desktop.browse(uri);
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
     }
     
     public void actionForgottenPassword() throws URISyntaxException{
         
         openUrl("https://edit.europe.yahoo.com/forgotroot?done=https%3A%2F%2Flogin.yahoo.com%2Fconfig%2Fvalidate%3F.src%3Dflickrsignin%26.pc%3D8190%26.scrumb%3D0%26.pd%3Dc%253DJvVF95K62e6PzdPu7MBv2V8-%26.intl%3Dfr%26.done%3Dhttp%253A%252F%252Fwww.flickr.com%252Fsignin%252Fyahoo%252F%253Fredir%253Dhttp%25253A%25252F%25252Fwww.flickr.com%25252F&src=flickrsignin&partner=&intl=fr&lang=fr-FR");
             
     }
     
     public void actionSaveImage(){
         try {
             String s = (String)JOptionPane.showInputDialog(this, "Choisissez un nom pour le fichier à enregistrer");
             saveImage(this.laPhoto.photo_url_big, s + ".jpg");
             JOptionPane.showMessageDialog(this, "Image enregistrée.");
         } catch (IOException ex) {
             Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public void actionDisplayInfos(){
         
         int imageWidth = this.photoZoomed.getIcon().getIconWidth();
         int imageHeight = this.photoZoomed.getIcon().getIconHeight();
         JOptionPane.showMessageDialog(this, 
                 "Résolution de l'image: " + imageWidth + "x" + imageHeight + "\n" +
                 "URL de l'image: " + this.laPhoto.photo_url
         );
             
     }
     
     public void goBackToSearch(){
         
         /* Retour à la recherche */
         this.page = 1;
         remove(this.panelSearch); 
         this.imagePanel.setVisible(false);
         this.panelPhotoZoomed.setVisible(false);
 
         try {
             ecranRecherche();
         } catch (MalformedURLException ex) {
             Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
         }
         this.contenuFenetre.add(this.panelSearch);
         this.panelSearch.setVisible(true);
             
     }
     
     public void actionDisplayInBrowser() throws URISyntaxException{
         
         openUrl(this.laPhoto.page_url);
         
     }
     
     public void fetchFavorites() throws FileNotFoundException, IOException{
         this.favorites = null;
         this.favorites = new ArrayList<String>();
         FileInputStream fStream = null;
         fStream = new FileInputStream("favorites.txt");
         DataInputStream in = new DataInputStream(fStream);
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         String strLine;
         while ((strLine = br.readLine()) != null){
             this.favorites.add(strLine);
         }
         Collections.reverse(this.favorites);
     }
     
     public void loadFromFavorites(){
         this.lesPhotos = null;
         this.lesPhotos = new ArrayList<Photo>();
         for(String s: this.favorites){
             String[] images = s.split("XXX");
             Photo p = new Photo(images[1], images[0]);
             this.lesPhotos.add(p);
         }
     }
     
     public void actionAddToFavorites(){
         String favoris =
                 this.laPhoto.photo_url_thumb + "XXX" +
                 this.laPhoto.photo_url_big;
         writeInFile(favoris, "favorites.txt");
         
         JOptionPane.showMessageDialog(this, "L'image a bien été ajoutée aux favoris");
     }
     
 }

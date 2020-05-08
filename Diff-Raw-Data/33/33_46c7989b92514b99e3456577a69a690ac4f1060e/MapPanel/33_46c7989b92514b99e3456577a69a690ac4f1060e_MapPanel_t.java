 package risk.view.client;
 
 import java.awt.BorderLayout;
import java.awt.Color;
 import java.awt.GridBagLayout;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 import risk.common.ImagePanel;
import risk.common.Logger;
 import risk.game.Controller;
 import risk.game.Country;
 import risk.game.GameView;
 //import net.miginfocom.swing.MigLayout;
 import javax.swing.JLayeredPane;
 import java.awt.Dimension;
 import java.util.HashMap;
 import javax.swing.AbstractAction;
 import java.awt.event.ActionEvent;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 
 import javax.swing.Action;
 
 public class MapPanel extends JPanel {
     private ClientPanel parent;
     private ImageIcon image = new ImageIcon(getClass().getResource(
             "/risk/view/client/resource/Risk_small_names.jpg"));
     private ImagePanel backGround;
     private JPanel buttonPanel;
     private JLayeredPane map = new JLayeredPane();
     private HashMap<String, CountryButton> countryButtons = new HashMap<String, CountryButton>();
     private Controller controller;
     public void setController(Controller controller) {
         this.controller = controller;
     }
 
     private final Action countryAction = new SwingAction();
 
     /**
      * Create the panel.
      */
     public MapPanel(ClientPanel cp) {
         parent=cp;
         backGround = new ImagePanel(image.getImage());
         setPreferredSize(backGround.getOriginalSize());
 
         setLayout(new BorderLayout());
         map.setPreferredSize(backGround.getOriginalSize());
         add(map, BorderLayout.CENTER);
         
         map.add(backGround);
 
         buttonPanel = new JPanel();
         buttonPanel.setOpaque(false);
         map.setLayer(buttonPanel, 1);
         buttonPanel.setBounds(0, 0, 1152, 648);
         map.add(buttonPanel);
         buttonPanel.setLayout(null);
         
         CountryButton btnAlaska = new CountryButton(Country.ALASKA);
         btnAlaska.setAction(countryAction);
         btnAlaska.setBounds(86, 95, 50, 20);
         buttonPanel.add(btnAlaska);
         countryButtons.put(Country.ALASKA, btnAlaska);
 
         CountryButton btnNW_Ter = new CountryButton(Country.NORTHWESTTERRITORY);
         btnNW_Ter.setAction(countryAction);
         btnNW_Ter.setBounds(160, 82, 50, 20);
         buttonPanel.add(btnNW_Ter);
         countryButtons.put(Country.NORTHWESTTERRITORY, btnNW_Ter);
         
         CountryButton btnGreenLand = new CountryButton(Country.GREENLAND);
         btnGreenLand.setAction(countryAction);
         btnGreenLand.setBounds(416, 48, 50, 20);
         buttonPanel.add(btnGreenLand);
         countryButtons.put(Country.GREENLAND, btnGreenLand);
         
         CountryButton btnAlberta = new CountryButton(Country.ALBERTA);
         btnAlberta.setAction(countryAction);
         btnAlberta.setBounds(185, 147, 50, 20);
         buttonPanel.add(btnAlberta);
         countryButtons.put(Country.ALBERTA, btnAlberta);
         
         CountryButton btnOntario = new CountryButton(Country.ONTARIO);
         btnOntario.setAction(countryAction);
         btnOntario.setBounds(258, 136, 50, 20);
         buttonPanel.add(btnOntario);
         countryButtons.put(Country.ONTARIO, btnOntario);
         
         CountryButton btnE_Canada = new CountryButton(Country.EASTERNCANADA);
         btnE_Canada.setAction(countryAction);
         btnE_Canada.setBounds(330, 166, 50, 20);
         buttonPanel.add(btnE_Canada);
         countryButtons.put(Country.EASTERNCANADA, btnE_Canada);
         
         CountryButton btnW_USA = new CountryButton(Country.WESTERNUNITEDSTATES);
         btnW_USA.setAction(countryAction);
         btnW_USA.setBounds(185, 214, 50, 20);
         buttonPanel.add(btnW_USA);
         countryButtons.put(Country.WESTERNUNITEDSTATES, btnW_USA);
         
         CountryButton btnE_USA = new CountryButton(Country.EASTERNUNITEDSTATES);
         btnE_USA.setAction(countryAction);
         btnE_USA.setBounds(279, 204, 50, 20);
         buttonPanel.add(btnE_USA);
         countryButtons.put(Country.EASTERNUNITEDSTATES, btnE_USA);
         
         CountryButton btnCentral_America = new CountryButton(Country.CENTRALAMERICA);
         btnCentral_America.setAction(countryAction);
         btnCentral_America.setBounds(219, 291, 50, 20);
         buttonPanel.add(btnCentral_America);
         countryButtons.put(Country.CENTRALAMERICA, btnCentral_America);
         
         CountryButton btnVenezuela = new CountryButton(Country.VENEZUELA);
         btnVenezuela.setAction(countryAction);
         btnVenezuela.setBounds(242, 323, 50, 20);
         buttonPanel.add(btnVenezuela);
         countryButtons.put(Country.VENEZUELA, btnVenezuela);
         
         CountryButton btnBrazil = new CountryButton(Country.BRAZIL);
         btnBrazil.setAction(countryAction);
         btnBrazil.setBounds(331, 401, 50, 20);
         buttonPanel.add(btnBrazil);
         countryButtons.put(Country.BRAZIL, btnBrazil);
         
         CountryButton btnPeru = new CountryButton(Country.PERU);
         btnPeru.setAction(countryAction);
         btnPeru.setBounds(267, 438, 50, 20);
         buttonPanel.add(btnPeru);
         countryButtons.put(Country.PERU, btnPeru);
         
         CountryButton btnArgentina = new CountryButton(Country.ARGENTINA);
         btnArgentina.setAction(countryAction);
         btnArgentina.setBounds(272, 506, 50, 20);
         buttonPanel.add(btnArgentina);
         countryButtons.put(Country.ARGENTINA, btnArgentina);
         
         CountryButton btnNorth_Africa = new CountryButton(Country.NORTHAFRICA);
         btnNorth_Africa.setAction(countryAction);
         btnNorth_Africa.setBounds(466, 370, 50, 20);
         buttonPanel.add(btnNorth_Africa);
         countryButtons.put(Country.NORTHAFRICA, btnNorth_Africa);
         
         CountryButton btnSouth_Africa = new CountryButton(Country.SOUTHAFRICA);
         btnSouth_Africa.setAction(countryAction);
         btnSouth_Africa.setBounds(526, 528, 50, 20);
         buttonPanel.add(btnSouth_Africa);
         countryButtons.put(Country.SOUTHAFRICA, btnSouth_Africa);
         
         CountryButton btnEast_Africa = new CountryButton(Country.EASTAFRICA);
         btnEast_Africa.setAction(countryAction);
         btnEast_Africa.setBounds(583, 404, 50, 20);
         buttonPanel.add(btnEast_Africa);
         countryButtons.put(Country.EASTAFRICA, btnEast_Africa);
         
         CountryButton btnEgypt = new CountryButton(Country.EGYPT);
         btnEgypt.setAction(countryAction);
         btnEgypt.setBounds(540, 328, 50, 20);
         buttonPanel.add(btnEgypt);
         countryButtons.put(Country.EGYPT, btnEgypt);
         
         CountryButton btnCentral_Africa = new CountryButton(Country.CENTRALAFRICA);
         btnCentral_Africa.setAction(countryAction);
         btnCentral_Africa.setBounds(523, 420, 50, 20);
         buttonPanel.add(btnCentral_Africa);
         countryButtons.put(Country.CENTRALAFRICA, btnCentral_Africa);
         
         CountryButton btnMadagascar = new CountryButton(Country.MADAGASCAR);
         btnMadagascar.setAction(countryAction);
         btnMadagascar.setBounds(624, 495, 50, 20);
         buttonPanel.add(btnMadagascar);
         countryButtons.put(Country.MADAGASCAR, btnMadagascar);
         
         CountryButton btnScandinavia = new CountryButton(Country.SCANDINAVIA);
         btnScandinavia.setAction(countryAction);
         btnScandinavia.setBounds(523, 95, 50, 20);
         buttonPanel.add(btnScandinavia);
         countryButtons.put(Country.SCANDINAVIA, btnScandinavia);
         
         CountryButton btnIceland = new CountryButton(Country.ICELAND);
         btnIceland.setAction(countryAction);
         btnIceland.setBounds(451, 116, 50, 20);
         buttonPanel.add(btnIceland);
         countryButtons.put(Country.ICELAND, btnIceland);
         
         CountryButton btnGreat_Britain = new CountryButton(Country.GREATBRITAIN);
         btnGreat_Britain.setAction(countryAction);
         btnGreat_Britain.setBounds(439, 177, 50, 20);
         buttonPanel.add(btnGreat_Britain);
         countryButtons.put(Country.GREATBRITAIN, btnGreat_Britain);
         
         CountryButton btnW_Europe = new CountryButton(Country.WESTERNEUROPE);
         btnW_Europe.setAction(countryAction);
         btnW_Europe.setBounds(466, 214, 50, 20);
         buttonPanel.add(btnW_Europe);
         countryButtons.put(Country.WESTERNEUROPE, btnW_Europe);
         
         CountryButton btnS_Europe = new CountryButton(Country.SOUTHERNEUROPE);
         btnS_Europe.setAction(countryAction);
         btnS_Europe.setBounds(526, 224, 50, 20);
         buttonPanel.add(btnS_Europe);
         countryButtons.put(Country.SOUTHERNEUROPE, btnS_Europe);
         
         CountryButton btnN_Europe = new CountryButton(Country.NORTHERNEUROPE);
         btnN_Europe.setAction(countryAction);
         btnN_Europe.setBounds(508, 163, 50, 20);
         buttonPanel.add(btnN_Europe);
         countryButtons.put(Country.NORTHERNEUROPE, btnN_Europe);
         
         CountryButton btnRussia = new CountryButton(Country.RUSSIA);
         btnRussia.setAction(countryAction);
         btnRussia.setBounds(595, 163, 50, 20);
         buttonPanel.add(btnRussia);
         countryButtons.put(Country.RUSSIA, btnRussia);
         
         CountryButton btnMiddle_East = new CountryButton(Country.MIDDLEEAST);
         btnMiddle_East.setAction(countryAction);
         btnMiddle_East.setBounds(609, 285, 50, 20);
         buttonPanel.add(btnMiddle_East);
         countryButtons.put(Country.MIDDLEEAST, btnMiddle_East);
         
         CountryButton btnAfghanistan = new CountryButton(Country.AFGHANISTAN);
         btnAfghanistan.setAction(countryAction);
         btnAfghanistan.setBounds(670, 214, 50, 20);
         buttonPanel.add(btnAfghanistan);
         countryButtons.put(Country.AFGHANISTAN, btnAfghanistan);
         
         CountryButton btnIndia = new CountryButton(Country.INDIA);
         btnIndia.setAction(countryAction);
         btnIndia.setBounds(725, 316, 50, 20);
         buttonPanel.add(btnIndia);
         countryButtons.put(Country.INDIA, btnIndia);
         
         CountryButton btnChina = new CountryButton(Country.CHINA);
         btnChina.setAction(countryAction);
         btnChina.setBounds(776, 256, 50, 20);
         buttonPanel.add(btnChina);
         countryButtons.put(Country.CHINA, btnChina);
         
         CountryButton btnSiam = new CountryButton(Country.SIAM);
         btnSiam.setAction(countryAction);
         btnSiam.setBounds(806, 316, 50, 20);
         buttonPanel.add(btnSiam);
         countryButtons.put(Country.SIAM, btnSiam);
         
         CountryButton btnUral = new CountryButton(Country.URAL);
         btnUral.setAction(countryAction);
         btnUral.setBounds(698, 126, 50, 20);
         buttonPanel.add(btnUral);
         countryButtons.put(Country.URAL, btnUral);
         
         CountryButton btnSiberia = new CountryButton(Country.SIBERIA);
         btnSiberia.setAction(countryAction);
         btnSiberia.setBounds(776, 116, 50, 20);
         buttonPanel.add(btnSiberia);
         countryButtons.put(Country.SIBERIA, btnSiberia);
         
         CountryButton btnYakutsk = new CountryButton(Country.YAKUTSK);
         btnYakutsk.setAction(countryAction);
         btnYakutsk.setBounds(856, 94, 50, 20);
         buttonPanel.add(btnYakutsk);
         countryButtons.put(Country.YAKUTSK, btnYakutsk);
         
         CountryButton btnIrkutsk = new CountryButton(Country.IRKUTSK);
         btnIrkutsk.setAction(countryAction);
         btnIrkutsk.setBounds(827, 163, 50, 20);
         buttonPanel.add(btnIrkutsk);
         countryButtons.put(Country.IRKUTSK, btnIrkutsk);
         
         CountryButton btnMongolia = new CountryButton(Country.MONGOLIA);
         btnMongolia.setAction(countryAction);
         btnMongolia.setBounds(858, 211, 50, 20);
         buttonPanel.add(btnMongolia);
         countryButtons.put(Country.MONGOLIA, btnMongolia);
         
         CountryButton btnKamchatka = new CountryButton(Country.KAMCHATKA);
         btnKamchatka.setAction(countryAction);
         btnKamchatka.setBounds(965, 89, 50, 20);
         buttonPanel.add(btnKamchatka);
         countryButtons.put(Country.KAMCHATKA, btnKamchatka);
         
         CountryButton btnJapan = new CountryButton(Country.JAPAN);
         btnJapan.setAction(countryAction);
         btnJapan.setBounds(929, 240, 50, 20);
         buttonPanel.add(btnJapan);
         countryButtons.put(Country.JAPAN, btnJapan);
         
         CountryButton btnIndonesia = new CountryButton(Country.INDONESIA);
         btnIndonesia.setAction(countryAction);
         btnIndonesia.setBounds(812, 406, 50, 20);
         buttonPanel.add(btnIndonesia);
         countryButtons.put(Country.INDONESIA, btnIndonesia);
         
         CountryButton btnNew_Guinea = new CountryButton(Country.NEWGUINEA);
         btnNew_Guinea.setAction(countryAction);
         btnNew_Guinea.setBounds(929, 420, 50, 20);
         buttonPanel.add(btnNew_Guinea);
         countryButtons.put(Country.NEWGUINEA, btnNew_Guinea);
         
         CountryButton btnW_Australia = new CountryButton(Country.WESTERNAUSTRALIA);
         btnW_Australia.setAction(countryAction);
         btnW_Australia.setBounds(815, 495, 50, 20);
         buttonPanel.add(btnW_Australia);
         countryButtons.put(Country.WESTERNAUSTRALIA, btnW_Australia);
         
         CountryButton btnE_Australia = new CountryButton(Country.EASTERNAUSTRALIA);
         btnE_Australia.setAction(countryAction);
         btnE_Australia.setBounds(920, 506, 50, 20);
         buttonPanel.add(btnE_Australia);
         countryButtons.put(Country.EASTERNAUSTRALIA, btnE_Australia);
         for(CountryButton c: countryButtons.values()){
             c.setRelativePosition();
         }
     }
 
     public void refresh(GameView view) {
         for (CountryButton cb : countryButtons.values()) {
             cb.refresh(view);
         }
     }
     public Dimension resizeRiskBoard(int height, int width){
         Dimension d=backGround.resizeImage(height, width);
         this.setPreferredSize(d);
        this.setMinimumSize(d);
        this.setMaximumSize(d);
        this.setBounds(0, 0, (int)d.getWidth(), (int)d.getHeight());
         map.setPreferredSize(d);
        map.setMinimumSize(d);
        map.setMaximumSize(d);
        map.setBounds(0, 0, (int)d.getWidth(), (int)d.getHeight());

         buttonPanel.setPreferredSize(d);
        buttonPanel.setMinimumSize(d);
        buttonPanel.setMaximumSize(d);
        buttonPanel.setBounds(0, 0, (int)d.getWidth(), (int)d.getHeight());
         backGround.setPreferredSize(d);
        backGround.setMinimumSize(d);
        backGround.setMaximumSize(d);
        backGround.setBounds(0, 0, (int)d.getWidth(), (int)d.getHeight());
        
         repaint();
         for(CountryButton c: countryButtons.values()){
             c.setCurrentPosition(d.width, d.height);
         }
         return d;
     }
 
     private class SwingAction extends AbstractAction {
         public SwingAction() {
             putValue(NAME, "");
             putValue(SHORT_DESCRIPTION, "");
         }
 
         public void actionPerformed(ActionEvent e) {
             if(controller!=null){
                 controller.onCountryClick(((CountryButton)e.getSource()).getCountry());
             }
         }
     }
 }

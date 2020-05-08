 package View;
 
 import Controller.FilmController;
 import Controller.GenreController;
 import Model.Genre;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Package: View
  * User: Mathias
  * Date: 23-10-12
  * Time: 16:01
  */
 public class FilmOpslaan extends JFrame {
 
     private static FilmOpslaan filmOpslaan;
 
     private JPanel panel;
     private JTextField titel;
     private JTextField speelduur;
     private JTextField land;
     private JCheckBox kinderenToegelaten;
     private JTextArea korteInhoud;
     private JTextField filmmaatschappij;
     private JTextField datumVanUItgave;
     private JCheckBox film3D;
     private JButton voegToe;
     private JButton annuleer;
 
     private JLabel lbltitel;
     private JLabel lblspeelduur;
     private JLabel lblland;
     private JLabel lblkinderenToegelaten;
     private JLabel lblkorteInhoud;
     private JLabel lblfilmmaatschappij;
     private JLabel lbldatumVanUItgave;
     private JLabel lblfilm3D;
     private JLabel lblgenre;
 
     private JScrollPane js;
 
     private JComboBox genres = new JComboBox();
 
     private FilmController fc;
 
     private FilmOpslaan() throws HeadlessException {
             maakComponenten();
             maakLayout();
             voegListenersToe();
             toonFrame();
     }
 
     public static synchronized FilmOpslaan getFilmOpslaan(){
         if (filmOpslaan == null){
             filmOpslaan = new FilmOpslaan();
         }
         return filmOpslaan;
     }
 
     private void maakComponenten() {
         titel = new JTextField();
         speelduur = new JTextField();
         land = new JTextField();
         kinderenToegelaten = new JCheckBox();
         korteInhoud = new JTextArea();
         korteInhoud.setLineWrap(true);
         korteInhoud.setWrapStyleWord(true);
         js = new JScrollPane(korteInhoud);
         filmmaatschappij = new JTextField();
         datumVanUItgave = new JTextField();
         film3D = new JCheckBox();
 
         lbltitel = new JLabel("Titel");
         lblspeelduur = new JLabel("Speelduur");
         lblland = new JLabel("Land van herkomst");
         lblkinderenToegelaten = new JLabel("Kinderen toegelaten?");
         lblkorteInhoud = new JLabel("Korte Inhoud");
         lblfilmmaatschappij = new JLabel("Filmmaatschappij");
         lbldatumVanUItgave = new JLabel("Datum van uitgave (dd/mm/yyyy");
         lblfilm3D = new JLabel("3D film?");
         lblgenre = new JLabel("Genre");
 
         genres = new JComboBox();
         GenreController gc = new GenreController();
         for (Genre g : gc.getGenres()){
             genres.addItem(g);
         }
 
         voegToe = new JButton("Voeg Toe");
         annuleer = new JButton("Annuleer");
     }
 
     private void maakLayout() {
         panel = new JPanel(new GridLayout(0, 2));
         panel.add(lbltitel);
         panel.add(titel);
         panel.add(lblspeelduur);
         panel.add(speelduur);
         panel.add(lblkinderenToegelaten);
         panel.add(kinderenToegelaten);
         panel.add(lblkorteInhoud);
         panel.add(js);
         panel.add(lblfilmmaatschappij);
         panel.add(filmmaatschappij);
         panel.add(lbldatumVanUItgave);
         panel.add(datumVanUItgave);
         panel.add(lblfilm3D);
         panel.add(film3D);
         panel.add(lblgenre);
         panel.add(genres);
         panel.add(voegToe);
         panel.add(annuleer);
         add(panel);
 
     }
 
     private void voegListenersToe() {
         voegToe.addMouseListener(new MouseAdapter() {
             @Override
             public void mousePressed(MouseEvent e) {
                 try {
                     fc = new FilmController();
                     DateFormat formatter ;
                     Date date = null;
                     formatter = new SimpleDateFormat("dd/mm/yyyy");
                     if (!datumVanUItgave.getText().isEmpty()){
                         date = (Date)formatter.parse(datumVanUItgave.getText());
                     }
                     Integer lengte = null;
                     if (!speelduur.getText().isEmpty())
                         lengte =  Integer.parseInt(speelduur.getText());
                     Boolean succesful = fc.voegFilmToe(titel.getText(),lengte, land.getText(),
                             kinderenToegelaten.isSelected(), korteInhoud.getText(), filmmaatschappij.getText(), date, film3D.isSelected());
                     if (succesful){
                         JOptionPane.showMessageDialog(FilmOpslaan.this,
                                 "Film succesvol toegevoegd!",
                                 "Gelukt!",
                                 JOptionPane.INFORMATION_MESSAGE);
                     }
                     else{
                         JOptionPane.showMessageDialog(FilmOpslaan.this,
                                 "Kon de film niet toevoegen..",
                                 "Oh nee",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                 }
                 catch (ParseException e1) {
                     JOptionPane.showMessageDialog(FilmOpslaan.this,
                             "Fout formaat voor de datum, gebruik dd/mm/yyyy",
                             "Datum in fout formaat",
                             JOptionPane.ERROR_MESSAGE);
                 }
                catch (NumberFormatException a){
                    JOptionPane.showMessageDialog(FilmOpslaan.this,
                            "Speelduur moet een getal zijn (in minuten)",
                            "Speelduur fout",
                            JOptionPane.ERROR_MESSAGE);
                }
             }
         });
 
         annuleer.addMouseListener(new MouseAdapter() {
             @Override
             public void mousePressed(MouseEvent e) {
                 FilmOpslaan.this.setVisible(false);
             }
         });
 
     }
 
     private void toonFrame() {
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setBounds(200, 200, 400, 280);
         pack();
     }
 
 
 
 
 }
 
 

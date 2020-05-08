 import javax.swing.*;
 import java.awt.Dimension;
 import java.awt.Color; //TEMPORAIRE, juste pour le positionnement
 import java.awt.BorderLayout;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 public class Terminal extends JPanel implements KeyListener{
 
     //private static ArrayList<JTextArea> affichage_historique = new ArrayList<JTextArea>();
     private static JTextArea histo = new JTextArea("Bienvenue sur Carapuce ! Le logiciel fait pour les tortues !");
     private static JScrollPane pane = new JScrollPane(histo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
     private static ArrayList<String> all_cmd = new ArrayList<String>();
     private int compteur_commandes = -1;
     private JTextField champ_de_commande;
     private Controleur controleur;
 
     /**
      *  Constructeur du Terminal
      */
 	Terminal()
     {
 
 		this.setBackground(Color.BLACK);//TEMPORAIRE, juste pour le positionnement
         addTerminal();
         this.setLayout(new BorderLayout());
         
         this.add(this.champ_de_commande, BorderLayout.SOUTH);
         this.add(this.pane, BorderLayout.CENTER);
         
         this.champ_de_commande.addKeyListener(this);
 
     }
     
 
     /**
      *  Evenement lors de la pression des touches
      *  @param keyEvent
      */
     public void keyPressed(KeyEvent keyEvent)
     {
         if ( keyEvent.getKeyCode() == KeyEvent.VK_ENTER
                 && !this.champ_de_commande.getText().equals(""))
         {
             controleur.commande(this.champ_de_commande.getText());
            this.histo.append("\n"+this.champ_de_commande.getText());
             this.histo.setCaretPosition(this.histo.getDocument().getLength());
             this.all_cmd.add(this.champ_de_commande.getText());
             
             /*
             JTextArea j = new JTextArea(this.champ_de_commande.getText());
 
             for (int i = affichage_historique.size()-1; i >= 0; i--)
               this.add(this.affichage_historique.get(i));
             */
 
             this.champ_de_commande.setText("");
             this.compteur_commandes = all_cmd.size();
         }
 
         else if ( keyEvent.getKeyCode() == KeyEvent.VK_UP)
         {
             
             if ( compteur_commandes - 1 >= 0 )
             {
                 compteur_commandes--;
                System.out.println(" UP :: " + compteur_commandes);
                 this.champ_de_commande.setText(all_cmd.get(compteur_commandes));
             }        
             
         }
 
         else if ( keyEvent.getKeyCode() == KeyEvent.VK_DOWN)
         {
 
             if ( compteur_commandes + 1 < all_cmd.size() )
             {
                 compteur_commandes++;
                System.out.println(" DOWN :: " + compteur_commandes);
                 this.champ_de_commande.setText(all_cmd.get(compteur_commandes));
             }
 
         }
 
         else;
 
     }
 
     /**
      *  (IGNORE)
      *  @param keyEvent
      */
     public void keyReleased(KeyEvent keyEvent)
     {
         /*  IGNORE  */
     }
 
     /**
      *  (IGNORE)
      *  @param keyEvent
      */
     public void keyTyped(KeyEvent keyEvent)
     {
         /*  IGNORE  */
     }
 
     /**
      *  Ajoute le terminal, et ajoute les valeurs par défaut
      */
     private void addTerminal()
     {
 
         this.champ_de_commande = new JTextField(50);
         this.champ_de_commande.setBackground(Color.black);
         this.champ_de_commande.setForeground(Color.white);
         this.champ_de_commande.setSize( this.getWidth(), 20 );
         this.champ_de_commande.setFocusable(true);
         this.champ_de_commande.requestFocus();
 
         this.histo.setBackground(Color.black);
         this.histo.setForeground(Color.white);
         this.histo.setEnabled(false);
 
     }
 
     /**
      *  Modifie le controleur
      *  @param c nouveau controleur
      */
     public void setControleur(Controleur c)
     {
         this.controleur = c;
     }
 
 }

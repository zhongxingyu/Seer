 package IHM;
 
 
 import javax.swing.*;
 import javax.swing.border.LineBorder;
 
 import Others.Outils;
 
 import devintAPI.FenetreAbstraite;
 import devintAPI.Preferences;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 
 /** Cette classe est un exemple d'interface de jeu.
  * Elle tend DevintFrame pour avoir un Frame et ragir aux vnements claviers
  * Implmente ActionListener pour ragir au clic souris sur le bouton.
  * On surchage la mthode "keyPressed" pour associer une action  la touche F3
  *
  * @author helene
  */
 public class IHMJeu extends FenetreAbstraite implements ActionListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3618784748797233087L;
 	private JButton question;
 	private Outils outils;
 	private JTextArea lb1;
 	String mot="";
 	/*
 	 * Constructeur
 	 */
 	public IHMJeu(String title) {
 		super(title);
 	}
 
 
 	/*
 	 * Initialisation du frame.
 	 */
 	protected void init() {
 		
 		/*
 		String text = outils.laQuestionEst(outils.choisirListe());
 		voix.playText(text);
 		lb1 = new JTextArea (text); 
     	lb1.setLineWrap(true);
     	lb1.setEditable(false);
     	lb1.setFont(new Font("Georgia",1,30));
     	
     	// on place le premier composant en haut
     	this.add(lb1);
     	*/
 		
 		// bouton pour poser une questin
 		
    	question = new JButton();
     	Outils o = new Outils();
     	String question = o.laQuestionEst();
     	lb1 = new JTextArea (question);
     	lb1.setLineWrap(true);
     	lb1.setEditable(false);
     	lb1.setFont(new Font("Georgia",1,30));
     	this.add(lb1,BorderLayout.NORTH);
     	/*
     	question.setText(texte);
     	question.setBackground(new Color(50,50,255));
     	question.setBorder(new LineBorder(Color.BLACK,10));
      	question.setFont(new Font("Georgia",1,40));
      	// c'est l'objet Jeu lui-mme qui ragit au clic souris
        	question.addActionListener(this);
     	// on met le bouton  droite
      	this.add(question);
      	*/
 	}
 
 
 	/*
 	 * Fonctions permettant de grer les vnements et les actions
 	 */
 
 	//Action performed: Dfini les actions  effectuer lors de dtection des vnements
 	public void actionPerformed(ActionEvent ae){
 		
 		voix.stop();	// toujours stopper la voix avant de parler
 
 		Object source = ae.getSource(); // on rcupre la source de l'vnement
 
 		if (source.equals(question)) { // si c'est le bouton "question" on lit la question
 			String text = outils.laQuestionEst();
 			voix.playText(text); // le contenu des questions est variable donc on les lit avec SI_VOX
 			
 			
 			
 		}
 
 		this.requestFocus(); // on redonne le focus au JFrame principal (aprs un clic, le focus est sur le bouton)
 	
 	}
 	
 
 
 	//Keyboard event listener: dtecte les lments clavier.
 	@Override
 	public void keyPressed(KeyEvent e) {
 		super.keyPressed(e);	// appel  la mthode mre qui gre les vnements ESC, F1, F3, F4
 		switch(e.getKeyCode()){
 		case KeyEvent.VK_ENTER:
 			break;
 			
 		default:
 			mot=mot+e.getKeyChar();
 
 		}
 	}
 
 
 	/*
 	 * Fonctions qui renvoient les sons d'accueil et d'aide.
 	 * Pour plus d'informations, regarder la classe DevintFrameListener (dans le package devintAPI)
 	 */
 
 	protected String wavAccueil() {	// renvoie le fichier wave contenant le message d'accueil
 		return "../ressources/sons/accueilJeu.wav";
 	}
 
 	protected String wavRegleJeu() {	// renvoie le fichier wave contenant la rgle du jeu
 		return "../ressources/sons/aideF1.wav";
 	}
 
 	protected String wavAide() {	// renvoie le fichier wave contenant la rgle du jeu
 		return "../ressources/sons/aide.wav";
 	}
 
 
 	@Override
 	public void changeColor() {
 		// TODO Auto-generated method stub
 
 	}
 
 
 
 
 }

 package com.carte.panels;
 import java.awt.AWTException;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
 import com.carte.sprites.CaseNiveaux;
 import com.carte.sprites.Sprite;
 import com.carte.utils.Observable;
 import com.carte.utils.Observateur;
 
 
 @SuppressWarnings("serial")
 public class Carte extends JPanel implements Observateur, MouseListener, Serializable, Observable
 {
 	private int largeur; // Nombre de cases en largeur
 	private int hauteur; // Nombre de cases en hauteur
 	private int nbrNiveaux; // Nombre max de niveaux
 	private int largeurCase; // Nombre de pixels par case en largeur
 	private int hauteurCase; // Nombre de pixels par case en hauteur
 	private int nivSelected; // Niveaux sélectionné
 	private JPanel pCases; // Panel contenant les cases
 	
 	private ArrayList<Observateur> listeObservateur =
         new ArrayList<Observateur>(); // liste des observateurs
 	// ArrayList à envoyer aux observateurs le nécessitant
 	private ArrayList<Sprite> listeSpritesCase = new ArrayList<Sprite>(); 
 
 	private ArrayList<Sprite> spritesCurseur; // Image du curseur
 	private CaseNiveaux[][] cases; // La carte est constituée de ces cases
 	private CaseNiveaux[][] casesOld; // Copie de la carte visible pour un
 									  // éventuel retour en arrière
 	private Toolkit tk = Toolkit.getDefaultToolkit(); // Permet de gérer le
 													  // curseur
 
 	private int[] coordSelection = new int[4]; // coordonnées d'une sélection en
 											   // i,j
 	private boolean modeSelection = false; // indique si on est en train de
 										   // faire une selection
 	// Contient les cases de la sélection
 	private ArrayList<CaseNiveaux> selection = new ArrayList<CaseNiveaux>();
 
 	private boolean aleatoire = true; // indique si le mode aléatoire de pose
 									  // est activé
 	private int perctAlea = 100; // le pourcentage de vide dans l'aléatoire
 
 	/**
 	 * @param l
 	 *        Largeur de la carte en nombre de cases
 	 * @param h
 	 *        Hauteur de la carte en nombre de cases
 	 * @param lc
 	 *        Largeur d'une case en pixels
 	 * @param hc
 	 *        Hauteur d'une case en pixels
 	 * @param nbrNiv
 	 *        Nombre de niveaux gérés
 	 */
 	public Carte(int l, int h, int lc, int hc, int nbrNiv)
 	{
 		largeur = l;
 		hauteur = h;
 		largeurCase = lc;
 		hauteurCase = hc;
 		nbrNiveaux = nbrNiv;
 		pCases = new JPanel();
 
 		pCases.setMaximumSize(new Dimension(l * lc, h * hc));
 		pCases.setMinimumSize(new Dimension(l * lc, h * hc));
 
 		// Mise en place d'un layout en grille
 		pCases.setLayout(new GridLayout(h, l, 0, 0));
 		this.setLayout(new GridBagLayout());
 
 		// Initialisation des cases
 		cases = new CaseNiveaux[h][l];
 		for (int i = 0; i < h; i++)
 		{
 			for (int j = 0; j < l; j++)
 			{
 				cases[i][j] =
 				        new CaseNiveaux(largeurCase, hauteurCase, Color.blue,
 				                Color.black, nbrNiveaux);
 				pCases.add(cases[i][j]);
 				cases[i][j].addMouseListener(this);
 			}
 		}
 		
 		// Initialisation du tableau de copie des cases
 		casesOld = new CaseNiveaux[h][l];
 		for (int i = 0; i < h; i++)
 		{
 			for (int j = 0; j < l; j++)
 			{
 				casesOld[i][j] =
 				        new CaseNiveaux(largeurCase, hauteurCase, Color.blue,
 				                Color.black, nbrNiveaux);
 			}
 		}
 		copieCases(cases, casesOld);
 
 		nivSelected = 1;
 		this.setBorder(BorderFactory.createRaisedBevelBorder());
 		this.add(pCases, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
 		        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
 		                20, 20, 20, 20), 0, 0));
 
 		repaint();
 	}
 
 	@Override
 	public void paintComponent(Graphics g)
 	{
 		// Dessine un rectangle noir englobant tout
 		g.setColor(Color.DARK_GRAY);
 
 		/*
 		 * g.fillRect(0, 0, largeur * largeurCase, hauteur * hauteurCase);
 		 */
 
 		g.fillRect(0, 0, this.getWidth(), this.getHeight());
 	}
 
 	/**
 	 * @param j
 	 *        Abscisse de la case voulue
 	 * @param i
 	 *        Ordonnée de la case voulue
 	 * @return La case voulue
 	 */
 	public CaseNiveaux getCase(int j, int i)
 	{
 		if (j >= 0 && j < largeur && i >= 0 && i < hauteur)
 		{
 			return cases[i][j];
 		}
 		else
 		{
 			System.err.println("Erreur dans Carte.getCase(int j, int i) : "
 			        + "index hors des limites.");
 			return null;
 		}
 	}
 	
 
 	@Override
 	public void mouseClicked(MouseEvent arg0)
 	{
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0)
 	{
 		int[] coordArr = new int[2]; // coordonnées d'arrivée
 		coordArr = coordCase((CaseNiveaux) arg0.getSource());
 
 		// Si on est dans la sélection : on hover toute la sélection
 		if (((CaseNiveaux) arg0.getSource()).isChosen() && !modeSelection)
 		{
 			for (int i = 0; i < selection.size(); i++)
 			{
 				selection.get(i).setHovered(true);
 			}
 		}
 
 		// De toute façon : on hover seulement la case actuelle
 		((CaseNiveaux) arg0.getSource()).setHovered(true);
 
 		// Si on est en train de faire une sélection click-deplace : on rajoute
 		// la sélection en hover
 		if (modeSelection)
 		{
 			for (int i = Math.min(coordSelection[0], coordArr[0]); i <= Math
 			        .max(coordSelection[0], coordArr[0]); i++)
 			{
 				for (int j = Math.min(coordSelection[1], coordArr[1]); j <= Math
 				        .max(coordSelection[1], coordArr[1]); j++)
 				{
 					if (arg0.isControlDown())
 						toggleHover(cases[i][j]);
 					else
 						cases[i][j].setHovered(true);
 				}
 			}
 		}
 		repaint();
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0)
 	{
 		int[] coordArr = new int[2]; // coordonnées d'arrivée
 		coordArr = coordCase((CaseNiveaux) arg0.getSource());
 
 		if (((CaseNiveaux) arg0.getSource()).isChosen() && !modeSelection)
 		{
 			for (int i = 0; i < selection.size(); i++)
 			{
 				toggleHover(selection.get(i));
 			}
 		}
 		((CaseNiveaux) arg0.getSource()).setHovered(false);
 
 		// Si on est en train de faire une sélection click-deplace
 		if (modeSelection)
 		{
 			for (int i = Math.min(coordSelection[0], coordArr[0]); i <= Math
 			        .max(coordSelection[0], coordArr[0]); i++)
 			{
 				for (int j = Math.min(coordSelection[1], coordArr[1]); j <= Math
 				        .max(coordSelection[1], coordArr[1]); j++)
 				{
 					cases[i][j].setHovered(false);
 				}
 			}
 		}
 
 		repaint();
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0)
 	{
 		int[] coordDep = new int[2];
 		coordDep = coordCase((CaseNiveaux) arg0.getSource());
 		coordSelection[0] = coordDep[0];
 		coordSelection[1] = coordDep[1];
 
 		// Permet de rentrer en mode sélection avec le clic gauche
 		if (arg0.getButton() == MouseEvent.BUTTON1 && !modeSelection)
 		{
 			modeSelection = true;
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0)
 	{
 		// On récupère les coordonnées de release
 		int[] coordDeplacement = new int[2];
 		coordDeplacement[0] = arg0.getY() / hauteurCase;
 		coordDeplacement[1] = arg0.getX() / largeurCase;
 		if (arg0.getY() < 0)
 			coordDeplacement[0]--;
 		if (arg0.getX() < 0)
 			coordDeplacement[1]--;
 		coordSelection[2] = coordSelection[0] + coordDeplacement[0];
 		coordSelection[3] = coordSelection[1] + coordDeplacement[1];
 
 		// Si le bouton est relaché dans de la carte
 		if ((coordSelection[2] >= 0) && (coordSelection[3] >= 0)
 		        && (coordSelection[2] < hauteur)
 		        && (coordSelection[3] < largeur))
 		{
 			if (coordSelection[0] == coordSelection[2]
 			        && coordSelection[1] == coordSelection[3]) // Clic simple
 			{
 				if (arg0.isControlDown()) // Avec ctrl : on ajoute/enlève la
 										  // case à la sélection
 				{
 					toggleSelection(cases[coordSelection[0]][coordSelection[1]]);
 				}
 				else if (arg0.getButton() == MouseEvent.BUTTON2) // Clic molette
 																 // : on annule
 																 // toute la
 																 // sélection
 				{
 					if(selection.isEmpty())
 					{
 						try
                         {
 	                        Robot rb = new Robot();
 	                        rb.keyPress(KeyEvent.VK_ESCAPE);
 	                        rb.keyRelease(KeyEvent.VK_ESCAPE);
                         }
                         catch (AWTException e)
                         {
 	                        System.err.println("Erreur dans Carte::MouseReleased : " + e.getMessage());
                         }
 					}else
 					{
 						clearSelection();
 					}
 				}
 				else if(arg0.getButton() == MouseEvent.BUTTON1
 				        && this.getCursor() == Cursor
 		                .getDefaultCursor())
 				// Si on n'a rien de sélectionné, on peut sélectionner une case pour afficher ses propriétés avec le clic gauche
 				{
 					
 					selectionnerCase();
 				}
 				else
 				// Sinon : ajout/suppression image
 				{
 					// On sauvegarde ds old avant de faire une modif
 					copieCases(cases, casesOld);
 
 					if (cases[coordSelection[0]][coordSelection[1]].isChosen()) // Si
 																				// on
 																				// est
 																				// sur
 																				// une
 																				// case
 																				// de
 																				// la
 																				// sélection
 					{
 						for (int i = 0; i < selection.size(); i++)
 						{
 							// click gauche : on place
 							if (arg0.getButton() == MouseEvent.BUTTON1
 							        && this.getCursor() != Cursor
 							                .getDefaultCursor()
 							        && !spritesCurseur.isEmpty())
 							{
 								if (!aleatoire)
 								{
 									selection.get(i).setSprite(spritesCurseur.get(0),
 									        nivSelected);
 								}
 								else
 								{
 									selection.get(i).setSprite(getSpriteAlea(true),
 									        nivSelected);
 								}
 
 							}
 							// click droit : on efface
 							else if (arg0.getButton() == MouseEvent.BUTTON3)
 							{
 								selection.get(i).clear(nivSelected);
 							}
 
 						}
 					}
 					else
 					// Sinon on agit seulement sur la case
 					{
 						// click gauche : on place
 						if (arg0.getButton() == MouseEvent.BUTTON1
 						        && this.getCursor() != Cursor
 						                .getDefaultCursor() && !spritesCurseur.isEmpty())
 						{
 							if (!aleatoire)
 							{
 								cases[coordSelection[0]][coordSelection[1]]
 													        .setSprite(spritesCurseur.get(0), nivSelected);
 							}
 							else
 							{
 								cases[coordSelection[0]][coordSelection[1]]
 													        .setSprite(getSpriteAlea(false), nivSelected);
 							}
 						}
 						// click droit : on efface
 						else if (arg0.getButton() == MouseEvent.BUTTON3)
 						{
 							cases[coordSelection[0]][coordSelection[1]]
 							        .clear(nivSelected);
 						}
 
 					}
 				}
 			}
 			else if (arg0.getButton() == MouseEvent.BUTTON1) // Clic spécial :
 															 // avec glissement
 															 // souris
 			{
 				if(this.getCursor() == Cursor
 		                .getDefaultCursor())
 				{
 					listeSpritesCase.clear();
 					updateObservateur();
 				}
 				if (!arg0.isControlDown())
 				{
 					clearSelection();
 				}
 				for (int i = Math.min(coordSelection[0], coordSelection[2]); i <= Math
 				        .max(coordSelection[0], coordSelection[2]); i++)
 				{
 					for (int j = Math.min(coordSelection[1], coordSelection[3]); j <= Math
 					        .max(coordSelection[1], coordSelection[3]); j++)
 					{
 						if (arg0.isControlDown()) // Si ctrl : on alterne la
 												  // partie sélectionnée
 						{
 							toggleSelection(cases[i][j]);
 						}
 						else
 						// Sinon : on crée une nouvelle sélection
 						{
 							addToSelection(cases[i][j]);
 						}
 					}
 				}
 			}
 		}
 		modeSelection = false;
 		repaint();
 
 		// permet d'afficher les 2 cartes pour le débuggage
 		// repaint();
 		// System.out.println("CasesOld :");
 		// printCarte(casesOld);
 		// System.out.println("Cases :");
 		// printCarte(cases);
 	}
 
 	/**
 	 * Copie un tableau de case de même hauteur et largeur (attributs de Carte)
 	 * dans un autre
 	 * 
 	 * @param casesACopier
 	 *        Le tableau de case à copier
 	 * @param casesOuCopier
 	 *        Le tableau de case où copier
 	 */
 	public void copieCases(CaseNiveaux[][] casesACopier,
 	        CaseNiveaux[][] casesOuCopier)
 	{
 		for (int i = 0; i < hauteur; i++)
 		{
 			for (int j = 0; j < largeur; j++)
 			{
 				for (int k = 1; k <= nbrNiveaux; k++)
 				{
 					casesOuCopier[i][j].setSprite(
 					        casesACopier[i][j].getSprite(k), k);
 				}
 			}
 		}
 		
 		revalidate();
 	}
 
 	/**
 	 * Permet d'annuler une action : copie le tableau de sauvegarde dans le
 	 * tableau visible
 	 */
 	public void undoCases()
 	{
 		copieCases(casesOld, cases);
 		repaint();
 		// System.out.println("CasesOld :");
 		// printCarte(casesOld);
 		// System.out.println("Cases :");
 		// printCarte(cases);
 	}
 
 	/**
 	 * Affiche textuellement le contenu d'une carte : affiche i si il y a une
 	 * image dans la case (i,j) au niveau k
 	 * 
 	 * @param casesNiv
 	 *        Le tableau de case de hauteur et largeur (attributs de Carte) à
 	 *        afficher
 	 */
 	public void printCarte(CaseNiveaux[][] casesNiv)
 	{
 		for (int i = 0; i < hauteur; i++)
 		{
 			System.out.println(">>> " + i);
 			for (int k = 0; k < nbrNiveaux; k++)
 			{
 				for (int j = 0; j < largeur; j++)
 				{
 					if (casesNiv[i][j].getImage(k + 1) != null)
 						System.out.print(" i |");
 					else
 						System.out.print("   |");
 				}
 				System.out.println();
 			}
 		}
 		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
 	}
 
 	/**
 	 * Récupére les coordonnées d'une case en (i,j)
 	 * 
 	 * @param caseNiv
 	 *        La case dont on veut récupérer les coordonnées
 	 * @return coord Les coordonnées de la case : coord[0] = i et coord[1] = j
 	 */
 	public int[] coordCase(CaseNiveaux caseNiv)
 	{
 		int[] coord = new int[2];
 
 		for (int i = 0; i < hauteur; i++)
 		{
 			for (int j = 0; j < largeur; j++)
 			{
 				if (cases[i][j] == caseNiv)
 				{
 					coord[0] = i;
 					coord[1] = j;
 				}
 			}
 		}
 
 		return coord;
 	}
 
 	/**
 	 * Sélectionne aléatoirement une image parmis celles contenues dans
 	 * l'attribut image
 	 * 
 	 * @return img L'image sélectionnée aléatoirement
 	 */
 	public Sprite getSpriteAlea(boolean perct)
 	{
 		Sprite sprite = null;
 
 		// On détermine si on affiche une image ou non en fonction du
 		// pourcentage de remplissage
 		int numRempl = (int) (Math.random() * 100);
 		boolean vide = (numRempl > perctAlea && perct) ? true : false;
 
 		// On choisit une image au hasard si on peut
 		int num = (int) (Math.random() * spritesCurseur.size());
 		if (!spritesCurseur.isEmpty() && !vide)
 			sprite = spritesCurseur.get(num);
 
 		return sprite;
 	}
 
 	/**
 	 * Ajoute une case à la sélection si elle n'y est pas ou la retire si elle y
 	 * est déjà
 	 * 
 	 * @param caseNiv
 	 *        La case à modifier
 	 */
 	public void toggleSelection(CaseNiveaux caseNiv)
 	{
 		if (!selection.contains(caseNiv))
 		{
 			addToSelection(caseNiv);
 		}
 		else
 		{
 			removeFromSelection(caseNiv);
 		}
 	}
 
 	/**
 	 * Efface la sélection
 	 */
 	public void clearSelection()
 	{
 		for (int i = 0; i < selection.size(); i++)
 		{
 			selection.get(i).choose(false);
 			selection.get(i).setHovered(false);
 		}
 		selection.clear();
 	}
 
 	/**
 	 * Modifie l'attribut hover de la case selon le fait que la case soit
 	 * sélectionnée ou non
 	 * 
 	 * @param caseNiv
 	 *        La case à modifier
 	 */
 	public void toggleHover(CaseNiveaux caseNiv)
 	{
 		if (caseNiv.isChosen())
 		{
 			caseNiv.setHovered(false);
 		}
 		else
 		{
 			caseNiv.setHovered(true);
 		}
 	}
 
 	/**
 	 * Ajoute une case à la sélection
 	 * 
 	 * @param caseNiv
 	 *        La case à ajouter
 	 */
 	public void addToSelection(CaseNiveaux caseNiv)
 	{
 		selection.add(caseNiv);
 		caseNiv.choose(true);
 		for (int i = 0; i < selection.size(); i++)
 		{
 			selection.get(i).setHovered(true);
 		}
 	}
 
 	/**
 	 * Enlève une case à la sélection
 	 * 
 	 * @param caseNiv
 	 *        La case à enlever
 	 */
 	public void removeFromSelection(CaseNiveaux caseNiv)
 	{
 		caseNiv.choose(false);
 		for (int i = 0; i < selection.size(); i++)
 		{
 			selection.get(i).setHovered(false);
 		}
 		selection.remove(caseNiv);
 	}
 	
 	public CaseNiveaux[][] getCases()
 	{
 		cases[0][0].getSprite(1);
 		return cases;
 	}
 
 	/**
      * Remet le curseur par défaut : l'utilisateur est à nouveau libre de
      * sélectionner un sprite ou une case
      */
     public void deselectionneCurseur()
     {
     	if(spritesCurseur != null)
     	{
     		this.spritesCurseur.clear();			
     	}
     	this.setCursor(Cursor.getDefaultCursor());
     }
 
 	private void selectionnerCase()
     {
     	clearSelection();
     	listeSpritesCase.clear();
     	addToSelection(cases[coordSelection[0]][coordSelection[1]]);
     	for(int k = 1 ; k <= nbrNiveaux ; k++)
     	{
     		listeSpritesCase.add(cases[coordSelection[0]][coordSelection[1]].getSprite(k));
     	}
     	updateObservateur();
     }
 
 	// fonction du DP observer
 	
 	@Override
 	public void addObservateur(Observateur obs)
 	{
 		listeObservateur.add(obs);
 
 	}
 
 	@Override
 	public void rmvObservateur(Observateur obs)
 	{
 		listeObservateur.remove(obs);
 	}
 
 	@Override
 	public void updateObservateur()
 	{
 		for (Observateur obs : listeObservateur)
 		{
 			obs.update(listeSpritesCase, "Carte");
 		}
 	}
 
 	@Override
     public void update(ArrayList<Sprite> sprites, String source)
     {
     	// Maybe use switch ans Constants instead but I don't know how to do know and I don't have internet...
     	if(source.equalsIgnoreCase("selection"))
     	{
     		if (!sprites.isEmpty())
     		{
     			this.spritesCurseur = sprites;
     			Image img = spritesCurseur.get(0).getImage();
     			Cursor monCurseur;
     			BufferedImage bufImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
     			if(bufImage.getGraphics().drawImage(img, 0, 0, null))
     			{
     				monCurseur =
     				tk.createCustomCursor(bufImage, new Point(15, 15),
     				"sprite");
     			}else
     			{
     				monCurseur =
     					tk.createCustomCursor(img, new Point(15, 15),
     					"sprite");
     			}
     			this.setCursor(monCurseur);
     			if(selection.size() == 1)
     			{
     				clearSelection();
     			}
     		}
     		else
     		{
     			deselectionneCurseur();
     		}
     	}
     		else if (source.equalsIgnoreCase("pancaseproperties"))
     	{
     		for(int i = 0 ; i < sprites.size() ; i ++)
     		{
     			selection.get(0).setSprite(sprites.get(i),i+1 );
     		}
     		selectionnerCase();
     	}
     	repaint();
     
     }
 
 	@Override
     public void update(String[] string)
     {
 		if(string[0].equalsIgnoreCase("repaint"))
 		{
 			System.out.println(string[0]);
 			repaint();
 		}
     }
 
 	@Override
     public void update(int[] infoInt)
     {
     	nivSelected = infoInt[0];
     	perctAlea = infoInt[1];
     	repaint();
     }
 
 	@Override
     public void update(boolean[] bool)
     {
     	for (int i = 0; i < hauteur; i++)
     	{
     		for (int j = 0; j < largeur; j++)
     		{
     			cases[i][j].setBordure(bool[0]);
     			for (int k = 1; k <= nbrNiveaux; k++)
     				cases[i][j].setNivVisible(k, bool[k]);
     		}
     	}
     	aleatoire = bool[nbrNiveaux + 1];
     	repaint();
     }
 
 	@Override
     public void update()
     {
 	    // TODO Auto-generated method stub
 	    
     }
 }

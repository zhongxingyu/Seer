 package autoquery;
 
 /* GUI */
 import javax.swing.JComponent;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 import javax.swing.JTree;
 import javax.swing.JScrollPane;
 import javax.swing.JEditorPane;
 import javax.swing.JSplitPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingConstants;
 import javax.swing.KeyStroke;
 import java.awt.Dimension;
 import java.awt.Component;
 import java.awt.BorderLayout;
 import java.util.EventObject;
 import java.util.Vector;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.JTable;
 import javax.swing.JList;
 import javax.swing.DefaultListModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.TableModel;
 import javax.swing.event.TableModelListener;
 import javax.swing.event.TableModelEvent;
 import java.awt.GridLayout;
 import javax.swing.SwingWorker;
 import java.util.List;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseEvent;
 import javax.swing.tree.TreePath;
 import javax.swing.JOptionPane;
 import javax.swing.JFileChooser;
 import java.io.File;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import java.io.IOException;
 import java.util.HashMap;
 
 /* SQL */
 import java.sql.ResultSet;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.ResultSetMetaData;
 
 /* Arbre */
 import java.util.TreeMap;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import javax.swing.tree.DefaultMutableTreeNode;
 import java.util.Map;
 import java.awt.event.KeyListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 
 /* Classes liées à JEditTextArea */
 import autoquery.jedittextarea.*;
 
 
 public class AutoQuery extends JFrame implements ActionListener, TableModelListener{
     public static void main(String args[]){
 
 	/* Dessine l'interface en suivant les précepts de l'OS hôte */
 	try {
 	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 	} 
 	catch (UnsupportedLookAndFeelException e) {
 	    // handle exception
 	}
 	catch (ClassNotFoundException e) {
 	    // handle exception
 	}
 	catch (InstantiationException e) {
 	    // handle exception
 	}
 	catch (IllegalAccessException e) {
 	    // handle exception
 	}
 
 	AutoQuery app = new AutoQuery(args);
     }
 
     /* Variables */
     private int keyTABCount = 0;
     private HashMap<Component, InfosOnglet> infosOnglets = new HashMap<Component, InfosOnglet>();
     private String host; /* Serveur auquel se connecter pour exécuter la requête*/
     private String port; /* Le port sur le serveur */
     private String username; /* Le user pour se connecter */
     private String password; /* Le mot de passe pour se connecter */
     private String nomConnexion; /* Le nom donné à cette connexion */
 
     /* Arborescence BDD */
     private JTree jt_arborescence_BDD;
     private TreeMap <String, TreeMap<String, TreeSet<String>> >  tm_arborescence_BDD;
     private DefaultMutableTreeNode dmtn_root;
     private TreeMap<String, TreeSet> tm_alias;
 
 
     /* Composants */
     StatusBar status_bar;
     JPopupMenu popup;
     DnDTabbedPane jtp_onglets;
     JTabbedPane jtp_droite;
     DefaultListModel lm_historique;
     JButton jb_historique;
     final JList jlist_historique;
     JScrollPane scrollPaneResultSet;
     
     public void increaseKeyTABCount(){
 	keyTABCount++;
     }
 
     public void resetKeyTABCount(){
 	keyTABCount = 0;
     }
 
     public int getKeyTABCount(){
 	return keyTABCount;
     }
 
     public void actionOnKey(KeyEvent evt){
 	if (evt.getKeyCode() == KeyEvent.VK_TAB){
 	    this.actionOnKeyTAB();
 	} else if (evt.getKeyCode() == KeyEvent.VK_R && evt.getKeyModifiersText(evt.getModifiers()).equals("Ctrl") ) {	
 	    resetKeyTABCount();
 	    this.looksForAnAliasAndCreate();
 	} else {
 	    resetKeyTABCount();
 	    status_bar.showStatus("");
 	}
     }
 
     private void looksForAnAliasAndCreate(){
 	JEditTextArea jeta_query = getActiveJEditTextArea();
 	    int i_caret = jeta_query.getCaretPosition();
 	    String s_ta = jeta_query.getText().replace("\n", " ").replace("\r", " ");
 	    String[] sa_words = s_ta.substring(0, i_caret).split(" ");
 	    System.out.println("Il y a " + sa_words.length + " mots.");
 	    /* Trouve les deux (trois s'il y a un "AS") derniers mots */ 
 	    boolean b_alias_trouve = false;
 	    boolean b_chemin_trouve = false;
 	    int i_alias = -1;
 	    int i_chemin = -1;
 	    int i = sa_words.length - 1;
 	    while (i >= 0 && (!b_alias_trouve || !b_chemin_trouve)){
 		if (sa_words[i].equals("") || sa_words[i].equalsIgnoreCase("as")){
 		    i--;
 		    continue;
 		}
 
 		if (!b_alias_trouve){
 		    b_alias_trouve = true;
 		    i_alias = i;
 		    i--;
 		} else {
 		    b_chemin_trouve = true;
 		    i_chemin = i;
 		    i--;
 		}
 	    }
 
 	    if (b_alias_trouve && b_chemin_trouve){
 	       	status_bar.showStatus("Ajout alias : " + sa_words[i_alias] + " pour " + sa_words[i_chemin]);
 		System.out.println("Ajout alias : " + sa_words[i_alias] + " pour " + sa_words[i_chemin]);
 		addAlias(sa_words[i_alias], sa_words[i_chemin]);
 	    }
 
 	    
 	    System.out.println("Alias : " + sa_words[i_alias] + ", chemin : " + sa_words[i_chemin]);
     }
 	    
     public void addAlias(String alias, String path){
 	System.out.println("Ajout de l'alias " + alias + " pour " + path + ".");
 	
 	String[] sa_path = path.split("\\.");
 	if (sa_path.length == 2){
 	    if (tm_arborescence_BDD.containsKey(sa_path[0]) && tm_arborescence_BDD.get(sa_path[0]).containsKey(sa_path[1])){
 		tm_alias.put(alias, (TreeSet) tm_arborescence_BDD.get(sa_path[0]).get(sa_path[1])); // Ajoute un lien vers les champs
 		System.out.println("Alias ajouté");
 	    }
 	}
     }
 
     private void actionOnKeyTAB(){
 	JEditTextArea jeta_query = getActiveJEditTextArea();
 	increaseKeyTABCount();
 	if (getKeyTABCount() <= 1){ /* Une fois TAB appuyé */
 	    String s_query = jeta_query.getText().replace("\n", " ").replace("\r", " ");
 	    
 
 	    /* Récupère le mot entre le dernier espace précédant le curseur et le curseur. S'il n'y a pas d'espace, ça prend toute la sous-chaîne gauche jusqu'au curseur. */
 	    int i_caret = jeta_query.getCaretPosition(); /* Enregistre la position du curseur */
 	    String s_pref = s_query.substring(0, i_caret); /* Chaîne a */
 	    String s_suff = s_query.substring(i_caret); /* Chaîne b */
 	    int i_debut = Math.max(s_pref.lastIndexOf(" ")+1, 0);
 	    String s_extract = s_pref.substring(i_debut, i_caret);
 	    System.out.println("Tentative de complétion à partir de '" + s_extract + "'");	    
 	    System.out.println("s_pref vaut '" + s_pref + "'");
 	    System.out.println("s_suff vaut '" + s_suff + "'");
 
 
 		/* Compte le nombre de points pour savoir s'il faut compléter un nom de schéma, de table ou de champ */
 		int dotCount = s_extract.replaceAll("[^.]", "").length();
 		System.out.println("Nombre de points : " + dotCount);
 	    
 		String s_insert = null;
 		String s_schema = null;
 		String s_to_complete = null;
 		String s_table = null;
 
 		switch(dotCount){
 		case 0: 
 		    System.out.println("Complétion de schéma");
 		    s_to_complete = s_extract;
 		    s_insert = complete(tm_arborescence_BDD, s_to_complete);
 		    if (s_insert == null){ // Si ce n'est pas un schéma qu'il faut chercher mais un alias
 			System.out.println("Pas de schéma trouvé commençant par " + s_to_complete);
 			System.out.println("Recherche d'un alias correspondant");
 			s_insert = complete( (SortedMap) tm_alias, s_to_complete);
 			if (s_insert == null){ // Si ce n'est pas non plus un début d'alias, on empeche la complétion
 			    this.resetKeyTABCount();
 			}
 			    
 		    }
 
 		    System.out.println("Cherche à insérer : " + s_insert);
 		    break;
 		case 1:
 		    System.out.println("Complétion de table");
 		    s_schema = extract(s_extract, 1);
 		    s_to_complete = extract(s_extract, 2);
 		    if (s_to_complete != null){
 			s_insert = (tm_arborescence_BDD.containsKey(s_schema)) ? complete(tm_arborescence_BDD.get(s_schema), s_to_complete) : null;
 			if (s_insert == null && tm_alias.containsKey(s_schema)){ // Si ce n'est pas une table qu'il faut chercher mais un champ
 			    s_insert = complete( (SortedSet) tm_alias.get(s_schema), s_to_complete);
 			}
 		    
 		    }
 		    System.out.println("Cherche à insérer : " + s_insert);
 		    break;
 		case 2:
 		    System.out.println("Complétion du champ");
 		    s_schema = extract(s_extract, 1);
 		    s_table = extract(s_extract, 2);
 		    s_to_complete = extract(s_extract, 3);
 		    s_insert = complete((SortedSet) tm_arborescence_BDD.get(s_schema).get(s_table), s_to_complete);
 		    System.out.println("Cherche à insérer : " + s_insert + " à partir de " + s_to_complete);
 		    break;		
 		}
 
 		if (s_insert != null){ /* Si s_insert est bien le préfixe d'un schéma, d'un alias, d'une table ou d'un champ*/
 		    jeta_query.setSelectedText(s_insert.substring(s_to_complete.length()));
 		    jeta_query.setCaretPosition( jeta_query.getText().length() - (s_query.length() - i_caret)); // Remet le curseur où il était avant la complétion
 		    if (s_insert.length() == s_to_complete.length()) { // Si on n'insère rien parce qu'il y a plusieurs possibilités
 			resetKeyTABCount(); // Remet le compteur de TAB à zéro puisqu'il y a eu une complétion
 		    }
 		}	    
 		System.out.println("TAB appuyé");
 
 
 	} else { /* Plus d'une fois TAB appuyé */
 	    System.out.println("TAB appuyé deux fois");
 	    if (popup != null){
 		add(popup);
 		System.out.println("Affichage d'un popup des choix possibles");
 		popup.show(jeta_query, 12, 20); // Position pifométrique
 	    }
 	    this.resetKeyTABCount();
 	    
 	}
     }
 
 
     /* Renvoie le i-ème élément dans une série d'éléments séparés par '.' */
     private String extract(String s, int i){
 	String[] composants = s.split("\\.");
 	if (i > composants.length){
 	    return("");
 	} else {
 	    return(composants[i-1]);
 	}
     }
 
     /* Si plus d'une solution ou pas de solution, renvoie NULL */
     private String complete(SortedMap tm, String s_pref){
 	Object[] sa_suff = filterPrefix(tm, s_pref).keySet().toArray();	
 	int nb_comp = sa_suff.length; // nombre de complétion(s) possible(s)
 	status_bar.showStatus(nb_comp + " complétion(s) possible(s)");
 	if (nb_comp == 1){
 	    return ((String) sa_suff[0]);
 	} else if (nb_comp > 1){
 	    String s_premier = (String)sa_suff[0];
 	    int i = s_pref.length();
 	    StringBuffer sb_plus_long_prefixe = new StringBuffer(s_pref);
 	    System.out.println("Préfixe le plus long à partir de " + sb_plus_long_prefixe.toString());
 	    
 	    while (i < s_premier.length()){
 		System.out.println(sb_plus_long_prefixe.toString() + s_premier.charAt(i) + " est un préfixe commun ?");
 		
 		Object[] sa_comp = filterPrefix(tm, sb_plus_long_prefixe.toString() + s_premier.charAt(i)).keySet().toArray();
 		if (sa_comp.length == sa_suff.length){
 		    sb_plus_long_prefixe.append(s_premier.charAt(i));
 		    i++;
 		} else {
 		    break;
 		}
 	    }
 	    /* Remplissage du menu contextuel */
 	    popup = new JPopupMenu("Complétions possibles");
 	    JMenuItem mi_completion;
 	    for (int j=0 ; j < sa_suff.length ; j++){
 		mi_completion = new JMenuItem((String)sa_suff[j]);
 		popup.add(mi_completion);
 		mi_completion.addActionListener(this);
 		}
 	    return sb_plus_long_prefixe.toString();	    
 	    
 	}
 
 	/* Assert: le tableau est vide */
 	return null;
     }
 
     /* Si plus d'une solution ou pas de solution, renvoie NULL */
     /* Remarque : cette méthode est en tout point identique à celle concernant les SortedMaps. 
        Comment n'en faire qu'une pour les deux ? */
     private String complete(SortedSet ss, String s_pref){
 	Object[] sa_suff = filterPrefix(ss, s_pref).toArray();	
 	int nb_comp = sa_suff.length; // nombre de complétion(s) possible(s)
 	status_bar.showStatus(jtp_onglets.getTitleAt(jtp_onglets.getSelectedIndex()), 
 			      nb_comp + " complétion(s) possible(s)");
 	if (nb_comp == 1){
 	    return ((String) sa_suff[0]);
 	} else if (nb_comp > 1){
 	    String s_premier = (String)sa_suff[0];
 	    int i = s_pref.length();
 	    StringBuffer sb_plus_long_prefixe = new StringBuffer(s_pref);
 	    System.out.println("Préfixe le plus long à partir de " + sb_plus_long_prefixe.toString());
 	    while (i < s_premier.length()){
 		System.out.println(s_pref + s_premier.charAt(i) + " est un préfixe commun ?");
 		
 		Object[] sa_comp = filterPrefix(ss, sb_plus_long_prefixe.toString() + s_premier.charAt(i)).toArray();
 		if (sa_comp.length == sa_suff.length){
 		    sb_plus_long_prefixe.append(s_premier.charAt(i));
 		    i++;
 		} else {
 		    break;
 		}
 	    }
 	    /* Remplissage du menu contextuel */
 	    popup = new JPopupMenu("Complétions possibles");
 	    JMenuItem mi_completion;
 	    for (int j=0 ; j < sa_suff.length ; j++){
 		mi_completion = new JMenuItem((String)sa_suff[j]);
 		popup.add(mi_completion);
 		mi_completion.addActionListener(this);
 		}
 
 	    return sb_plus_long_prefixe.toString();	    
 	}
 
 	/* Assert: le tableau est vide */
 	return null;
 
     }
 
     /* Renvoie le sous-arbre dont les clefs dont le préfixe est prefix */
     private <V> SortedMap<String, V> filterPrefix(SortedMap<String, V> baseMap, String prefix){
 	if(prefix.length() > 0){
 	    char nextLetter = (char) (prefix.charAt(prefix.length()-1)+1);
 	    String end = prefix.substring(0, prefix.length()-1) + nextLetter;
 	    return baseMap.subMap(prefix, end);
 	}
 	return baseMap;
     }
 
 
     /* Renvoie le sous-ensemble des éléments dont le préfixe est prefix */
     private SortedSet<String> filterPrefix(SortedSet<String> baseSet, String prefix){
 	if(prefix.length() > 0){
 	    char nextLetter = (char) (prefix.charAt(prefix.length()-1)+1);
 	    String end = prefix.substring(0, prefix.length()-1) + nextLetter;
 	    System.out.println("Éléments entre " + prefix + " et " + end);
 	    return baseSet.subSet(prefix, end);
 	}
 	return baseSet;
     }
 
 
     /* On ne peut pas renvoyer un ResultSet car le ResultSet est fermé au moment
        où l'instance de Statement est fermée. On est donc obligé de parser le résultat. 
        L'idéal, plutôt que de le faire à la main serait d'avoir une espèce d'ORM. */
     private Vector<String[]> executeRequete(String query){
  	Connection con = null;
  	ResultSet rs = null;
  	Statement st = null;
  	Vector<String[]> cache = new Vector<String[]>();
 	try {
 	    loadDriver();
 	    System.out.println("Connexion...");
 	    con = newConnection();
 	    st = con.createStatement();
 	    rs = st.executeQuery(query);
 	    			
 	    while (rs.next()) {
 	    	    int colCount = rs.getMetaData().getColumnCount();
 	    	    String[] record = new String[colCount];
 	    	    for (int i = 0; i < colCount; i++) {
 			record[i] = rs.getString(i + 1);
 		    }
 		    cache.addElement(record);
 	    }
 	    return cache;
 
 	    
 	} catch (Exception e){
 	    System.err.println("Exception : " + e.getMessage());
 	} finally {
             if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
             if (st != null) try { st.close(); } catch (SQLException ignore) {}
             if (con != null) try { con.close(); } catch (SQLException ignore) {}
 	}
 	return null;
     }
 
     public AutoQuery(String args[]){
 	super("AutoQuery");
 	
 	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	
 	
 	LoginDialog loginDialog = new LoginDialog(this);
 	loginDialog.setVisible(true);
 	
 	/* Test que les identifiants sont les bons */
 	if (!loginDialog.loginSucceeded()){
 		System.exit(0);
 	}
 	
 	/* Chargement des paramètres */
 	host = loginDialog.getHost();
 	port = loginDialog.getPort();
 	username = loginDialog.getUsername();
 	password = loginDialog.getPassword();
 	nomConnexion = loginDialog.getNomConnexion();
 	
 	/* Sauvegarde des alias */
 	tm_alias = new TreeMap();
 
 
 	/* Crée le TreeMap à partir de information_schema */
 	Connection con = null;
 	try {
 	    loadDriver();
 	    con = newConnection();
 	    Statement st = con.createStatement();
 	    String query = "SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME FROM information_schema.COLUMNS ORDER BY TABLE_SCHEMA, TABLE_NAME, ORDINAL_POSITION";
 	    ResultSet rs = st.executeQuery(query);
 	    tm_arborescence_BDD = makeTree(rs);
 	} catch (Exception e){
 	    System.err.println("Exception : " + e.getMessage());
 	} finally {
 	    try {
 		if (con != null){
 		    System.out.println("Fermeture de la connexion à la BDD");
 		    con.close();
 		}
 	    } catch (SQLException e) {}
 	}
 	
 
 	/* Crée la réprésentation graphique de la BDD */
 	dmtn_root = new DefaultMutableTreeNode("MOMACQ_V3");
 	if (tm_arborescence_BDD != null) {
 		createTree(dmtn_root, tm_arborescence_BDD);
 		jt_arborescence_BDD = new JTree(dmtn_root);
 
 		MouseListener ml = new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				if (e.getButton() == MouseEvent.BUTTON3){
 					TreePath selPath = jt_arborescence_BDD.getPathForLocation(e.getX(), e.getY());
 					int type = selPath.getPathCount();
 					switch (type){
 						case 2: 
 							System.out.println("Pas d'action sur les schémas.");
 							break;
 						case 3: 
 							System.out.println("Affichage menu contextuel.");
 							final String s_schema = (String) ((DefaultMutableTreeNode) selPath.getPathComponent(type-2)).getUserObject();
 							final String s_table = (String) ((DefaultMutableTreeNode) selPath.getPathComponent(type-1)).getUserObject();
 							/* Remplissage du menu contextuel */
 							JPopupMenu popup = new JPopupMenu();
 							
 							/* Infos sur la table */
 							JMenuItem item = new JMenuItem("Informations...");
 							popup.add(item);	    
 							item.addActionListener(new ActionListener(){
 								public void actionPerformed(ActionEvent ae){
 									Vector<String[]> rs = executeRequete("SELECT UPDATE_TIME, TABLE_ROWS FROM information_schema.TABLES WHERE TABLE_SCHEMA = '" + s_schema + "' AND TABLE_NAME = '" + s_table + "'");
 									if (rs != null){
 										final byte UPDATE_TIME = 0;
 										final byte TABLE_ROWS = 1;
 										String date = rs.elementAt(0)[UPDATE_TIME];
 										String rows = rs.elementAt(0)[TABLE_ROWS];
 										JOptionPane.showMessageDialog((Component) AutoQuery.this, 
 											"Nombre de lignes : " + rows + "\n" +
 											"Date de dernière MÀJ : " + date,
 											"Détails de la table " + s_table,
 											JOptionPane.INFORMATION_MESSAGE);
 									}
 								}});
 							
 							/* CREATE statement */
 							item = new JMenuItem("Syntaxe du CREATE...");
 							popup.add(item);	    
 							item.addActionListener(new ActionListener(){
 								public void actionPerformed(ActionEvent ae){
 									Vector<String[]> rs = executeRequete("SHOW CREATE TABLE " + s_schema + "." + s_table + ";");
 									if (rs != null){
 										final byte CREATE_STATEMENT = 1;
 										String create_statement = rs.elementAt(0)[CREATE_STATEMENT];
 										JOptionPane.showMessageDialog((Component) AutoQuery.this, 
 											"La syntaxe de création de la table est :\n" + create_statement,
 											"CREATE statement de " + s_schema + "." + s_table,
 											JOptionPane.INFORMATION_MESSAGE);
 									}
 								}});	
 							
 							/* 1000 premières lignes */
 							item = new JMenuItem("SELECT... LIMIT 1000");
 							popup.add(item);	    
 							item.addActionListener(new ActionListener(){
 								public void actionPerformed(ActionEvent ae){
 								    makeTabFromQuery("SELECT * FROM " + s_schema + "." + s_table + " LIMIT 1000;",
 										     s_table);
 								}});	
 
 
 							popup.show(jt_arborescence_BDD, e.getX(), e.getY());
 							popup.setVisible(true);
 							break;
 						case 4: 
 							System.out.println("Vous avez sélectionné le champ : " + (String) ((DefaultMutableTreeNode) selPath.getPathComponent(type-1)).getUserObject());
 							break;
 					}
 				}
 			}
 		};
 		jt_arborescence_BDD.addMouseListener(ml);
 	} 
 
 
 
 
 
 	/* Crée la fenêtre avec ses composants */
 	JScrollPane jsp_treeView = new JScrollPane(jt_arborescence_BDD); /* Crée la hiérarchie de la BDD */
 
 	
 	/* Crée les onglets BDD, Signets, Historique */
 	jtp_droite = new JTabbedPane(SwingConstants.TOP);
 	jtp_droite.addTab("BDD", jsp_treeView);
 	JScrollPane jsp_signets = new JScrollPane();
 	jtp_droite.addTab("Signets", jsp_signets);
 	
 	/* Onglet HISTORIQUE */
 	lm_historique = new DefaultListModel();
 	jlist_historique = new JList(lm_historique);
 	jlist_historique.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 	JPanel jp_historique = new JPanel(new BorderLayout());
 	jb_historique = new JButton("Créer onglet avec cette requête");
 	jb_historique.setEnabled(false);
 	jb_historique.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 		    String requete = (String) jlist_historique.getSelectedValue();
 		    System.out.println("Requête sélectionnée : " + (String) jlist_historique.getSelectedValue());
 		    if (!requete.isEmpty()){
 			AutoQuery.this.makeTabFromQuery(requete);
 		    }
 		}
 	    });
 	jp_historique.add(jb_historique, BorderLayout.NORTH);
 	jp_historique.add(jlist_historique, BorderLayout.CENTER);
 	jtp_droite.addTab("Historique", jp_historique);	
 	
 	/* Crée le composant qui accueille les onglets */
 	jtp_onglets = new DnDTabbedPane();//new DnDTabbedPane(SwingConstants.TOP);
 	jtp_onglets.setOpaque(true);
 
 	/* Ajoute un onglet aux composants gérant les onglets en lui passant les paramètres de connexion */
 	Component premier_onglet = makeTab();
 
 	/* Crée la status bar */
 	status_bar = new StatusBar();
 
 
 	/* Crée la partie de gauche */
 	/** Accueil des composants **/
 	scrollPaneResultSet = new JScrollPane(infosOnglets.get(premier_onglet).getTable());
 	JPanel panel_bas = new JPanel();
 	panel_bas.setLayout(new BorderLayout());
 	panel_bas.add(scrollPaneResultSet, BorderLayout.CENTER);
 	panel_bas.add(status_bar, BorderLayout.SOUTH);
 
 
 
 	JSplitPane jsp_gauche = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
 					       jtp_onglets,
 					       panel_bas);
 
 
 	/* Crée le JSplitPane qui accueille les onglets à gauche et l'arbre à droite */
 	JSplitPane splitPaneGaucheDroite = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 	splitPaneGaucheDroite.setLeftComponent(jsp_gauche);
 	splitPaneGaucheDroite.setRightComponent(jtp_droite);
 	
 
 	jtp_droite.setPreferredSize(new Dimension(400, 500));
 	splitPaneGaucheDroite.setDividerLocation(600); 
 	splitPaneGaucheDroite.setPreferredSize(new Dimension(1000, 500));
 
 	add(splitPaneGaucheDroite);
 
 
 	/* Lorsqu'il y a changement du JTabbedPane (notamment lorsqu'on change d'onglet)*/
 	jtp_onglets.addChangeListener(new ChangeListener()
 	    {
 		public void stateChanged(ChangeEvent e){
 		    Component onglet = jtp_onglets.getTabComponentAt(jtp_onglets.getSelectedIndex());
 		    InfosOnglet infos = infosOnglets.get(onglet);
 		    setActiveTable(infos.getTable());
 		}
 	    });
 
 	
 	JMenuBar menu_bar = new JMenuBar();
 	
 	/** Menu Onglets **/
 	JMenu menu_onglets = new JMenu("Onglets");
 	JMenuItem menu_onglets_ajout = new JMenuItem("Ajouter", KeyEvent.VK_A);
 	menu_onglets_ajout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
 	menu_onglets_ajout.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 		    /* Sélectionne l'onglet nouvellement créé */
 		    Component onglet = makeTab();
 		    jtp_onglets.setSelectedIndex(jtp_onglets.indexOfTabComponent(onglet)); 
 		}
 	    });
 	menu_onglets.add(menu_onglets_ajout);
 	menu_bar.add(menu_onglets);
 	
 	/** Menu Requête **/
 	JMenu menu_requete = new JMenu("Requête");
 	
 	/* Ouvrir un fichier */
 	final JMenuItem menu_requete_ouvrir = new JMenuItem("Ouvrir...");
 	menu_requete_ouvrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
 	menu_requete_ouvrir.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 		    
 		    /** Ouverture d'une boîte de dialogue de choix de fichier **/
 		    final JFileChooser fc = new JFileChooser();
 		    int returnVal = fc.showOpenDialog(menu_requete_ouvrir);
 		    
 		    if (returnVal == JFileChooser.APPROVE_OPTION) {
 			File inFile = fc.getSelectedFile();
 			try {
 			    FileReader fr = new FileReader(inFile);
 			    BufferedReader bufRdr = new BufferedReader(fr);
                     
 			    String line = null;
 			    StringBuffer buf = new StringBuffer("/* Fichier " + inFile.getName() + " */\n");
 			    while ((line = bufRdr.readLine()) != null){
 				buf.append(line + "\n");
 			    }
 			    bufRdr.close();
 			    getActiveJEditTextArea().setText(buf.toString());
                     
 			} catch (IOException ioex) {
 			    System.err.println("Erreur lors du chargement du fichier.");
 			    System.err.println(ioex);
 			}
 		    }
 		}
 	    });
 	menu_requete.add(menu_requete_ouvrir);
 
 
 	/* Exécuter une requête */
 	JMenuItem menu_requete_executer = new JMenuItem("Exécuter", KeyEvent.VK_W);
 	menu_requete_executer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
 	menu_requete_executer.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 		    lm_historique.addElement(getActiveJEditTextArea().getText());
 		    if (!jb_historique.isEnabled()){
 			jb_historique.setEnabled(true);
 		    }
 		    executeRequeteOngletActif();
 		}
 	    });
 	menu_requete.add(menu_requete_executer);
 
 
 	/* Ajout du menu à la fenêtre */
 	menu_bar.add(menu_requete);
        	add("North", menu_bar);
 
 	setTitle(makeFrameTitle());
 	pack();
 	setVisible(true);
     }
 
     /** Donne un titre à l'application **/
     private String makeFrameTitle(){
 	return "AutoQuery   ---  " + username + " @ " + host + ((nomConnexion == null) ? "" : " (" + nomConnexion + ")");
     }
 
 
     public void tableChanged(TableModelEvent e){
 	System.out.println("Y a eu du mouvement dans la table !");
     }
 
     /* Charge le driver pour communiquer avec la base de données */
     private static void loadDriver() throws ClassNotFoundException {
 	System.out.println("Chargement du pilote...");
 	Class.forName("com.mysql.jdbc.Driver");
 	System.out.println("Pilote chargé...");
     }
     
     /* Obtient une connexion avec le moteur de gestion de BDD */
     private Connection newConnection() throws SQLException {
 	final String url = "jdbc:mysql://" + host + ":" + port;
 	Connection con = DriverManager.getConnection(url, username, password);
 	return con;
     }
 
     /* Ouvre une connexion vers le serveur lié à un onglet */
     /* Pour l'instant chaque onglet possède les mêmes paramètres de connexion */
     private Connection newConnectionFromTab(int currentTab) throws SQLException{
 	return newConnection();
     }
     
     /* Ouvre une connexion vers le serveur lié à l'onglet sélectionné */
     private Connection newConnectionFromCurrentTab() throws SQLException{
 	return newConnectionFromTab(jtp_onglets.getSelectedIndex());
     }
 
     private void executeRequeteOngletActif(){
 	System.out.println("Exécution de la requête...");
 	Connection con = null;
 	try{
 	    con = newConnectionFromCurrentTab();
 	    String query = getActiveJEditTextArea().getText();
 	    System.out.println("Requête exécutée : " + query);
 	    getActiveQueryTableModel().runQuery(con, query); // Le modèle de données contient toutes les données à afficher dans une JTable
 	    
 	    
 	}  catch (Exception e){
 	    System.err.println("Exception lors de la connexion : " + e.getMessage());
 	}
 
 
 
     }
 
     /** Renvoie le JEditTextArea sur l'onglet sélectionné **/
     private JEditTextArea getActiveJEditTextArea(){
	return infosOnglets.get(jtp_onglets.getSelectedComponent()).getJETA();
     }
 
     private void setActiveTable(JTable newJTable){
     	scrollPaneResultSet.setViewportView(newJTable);
     }
 
 
 
     /** Renvoie le QueryTableModel correspondant à l'onglet sélectionné **/
     private QueryTableModel getActiveQueryTableModel(){
	return infosOnglets.get(jtp_onglets.getSelectedComponent()).getModele();
     }
 
     public JTabbedPane getQueryTabs() {
 	return jtp_onglets;
     }
 
 
     /* Crée de quoi accueillir les résultats de requêtes :
      JTable, QueryTableModel */
     private void makeComponentsForResultSet(Component onglet){
 	infosOnglets.get(onglet).setModele(new QueryTableModel(infosOnglets.get(onglet)));
 	JTable table = new JTable( infosOnglets.get(onglet).getModele());
 	infosOnglets.get(onglet).setTable(table);
 	table.setAutoCreateRowSorter(true);
 	table.getModel().addTableModelListener(this);
 	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 	TableColumnAdjuster tca = new TableColumnAdjuster(table);
 	tca.adjustColumns();
     }
 
     /* Crée un onglet dans le composant gérant les requêtes. 
        Renvoie l'index de l'onglet créé. */
     private Component makeTab(){
 	// Composant de l'onglet. Sert ensuite à traquer cet onglet, même s'il change d'index (suppression, mouvement, etc.)
 	ButtonTabComponent btc = new ButtonTabComponent(jtp_onglets, this);
 	infosOnglets.put(btc, new InfosOnglet(this, btc));
 
 	jtp_onglets.addTab("Onglet " + (jtp_onglets.getTabCount()+1), makePanelForTab(btc));
 
 	jtp_onglets.setTabComponentAt(jtp_onglets.getTabCount()-1, btc);
 
 	/* De quoi accueillir le résultat de la requête qui sera exécutée dans ce nouvel onglet */
 	makeComponentsForResultSet(btc); //jtp_onglets.getTabCount()-1);
 	return btc;//jtp_onglets.getTabCount()-1;
     }
 
     
     private void makeTabFromQuery(String query){
 	Component onglet = makeTab();
 	infosOnglets.get(onglet).getJETA().setText(query);	
     }
 
     private void makeTabFromQuery(String query, String title){
 	Component onglet = makeTab();
 	infosOnglets.get(onglet).getJETA().setText(query);	
 	getQueryTabs().setTitleAt(getQueryTabs().indexOfTabComponent(onglet), title);
     }
 
 
     private JPanel makePanelForTab(Component component){
 
 	/** Crée le contenu d'un onglet
 	    Crée le composant pour écrire les requêtes **/
 	JEditTextArea jeta_query = new JEditTextArea(); 
 	jeta_query.setFocusTraversalKeysEnabled(false);
 	jeta_query.setEditable(true);
 	jeta_query.setTokenMarker(new SQLTokenMarker());
 	jeta_query.setMinimumSize(new Dimension(300, 150));
 
 	/** Enregistre le JEditTextArea pour pouvoir faire des opérations plus tard dessus **/
 	/* Pas nécessaire de faire nbTabs - 1 puisque l'onglet dont on stocke les infos n'est pas encore créé*/
 	infosOnglets.get(component).setJETA(jeta_query);
 
 	
 	/** Crée le JPanel dans lequel on met les composants de chaque onglet **/
 	JPanel panel = new JPanel(new BorderLayout());
 	panel.add("Center", jeta_query);
 	return panel;
     }
        
     public void deleteTab(int tab){
 	jtp_onglets.remove(tab);
 	infosOnglets.remove(tab);
     }
 
     public HashMap<Component, InfosOnglet> getInfosOnglets(){
 	return infosOnglets;
     }
 
     /* Crée l'arbre de description de la BDD */
     private void createTree(DefaultMutableTreeNode top, TreeMap <String, TreeMap<String, TreeSet<String>> > tm_arbre){
 	DefaultMutableTreeNode dmtn_schema;
 	DefaultMutableTreeNode dmtn_table;
 	DefaultMutableTreeNode dmtn_column;
 	
 	/* Parcours sur les schémas */
 	for (Map.Entry <String, TreeMap<String, TreeSet<String>> > schema : tm_arbre.entrySet()){
 	    dmtn_schema = new DefaultMutableTreeNode(schema.getKey());
 	    
 	    /* Parcours sur les tables de ce schéma */
 	    for (Map.Entry <String, TreeSet<String> > table : schema.getValue().entrySet()){
 		dmtn_table = new DefaultMutableTreeNode(table.getKey());
 
 		/* Parcours sur les colonnes de cette table */
 		for (String column : table.getValue()){
 		    dmtn_column = new DefaultMutableTreeNode(new String(column));
 		    dmtn_table.add(dmtn_column);
 		}		
 		dmtn_schema.add(dmtn_table);
 	    }	    
 	    top.add(dmtn_schema);
 	}
 	
     }
 
 
 
 
     /* Crée l'arbre pour la complétion */
     private static TreeMap makeTree(ResultSet rs) throws SQLException{
 	TreeMap <String, TreeMap<String, TreeSet<String>> > tm_schemas = new TreeMap();
 	while (rs.next()) {
 	    String s_schema = rs.getString("TABLE_SCHEMA");
 	    String s_table = rs.getString("TABLE_NAME");
 	    String s_champ = rs.getString("COLUMN_NAME");
 	    if ( ! tm_schemas.containsKey(s_schema)){
 		/* schéma pas encore répertorié */
 		tm_schemas.put(s_schema, new TreeMap<String, TreeSet<String>>());
 		System.out.println("Ajout du schéma : " + s_schema);
 	    }
 	    
     	    if ( ! tm_schemas.get(s_schema).containsKey(s_table)){
 		/* table pas encore répertoriée */
 		tm_schemas.get(s_schema).put(s_table, new TreeSet<String>());
 		System.out.println("\t Ajout de la table : " + s_table);
 	    }
 	    tm_schemas.get(s_schema).get(s_table).add(s_champ);
 	    System.out.println("\t\t Ajout du champ : " + s_champ);
       	}
 	return tm_schemas;
     }
 
     /* Retrouve l'instance de AutoQuery qui contrôle l'objet lié à l'événement */
     public static AutoQuery getAutoQueryParent(EventObject evt){
 	if(evt != null)
 	    {
 		Object o = evt.getSource();
 		if(o instanceof Component)
 		    {
 			// find the parent AutoQuery
 			Component c = (Component)o;
 			for(;;)
 			{
 				if(c instanceof AutoQuery)
 					return (AutoQuery)c;
 				else if(c == null)
 					break;
 				if(c instanceof JPopupMenu)
 					c = ((JPopupMenu)c)
 						.getInvoker();
 				else
 					c = c.getParent();
 			}
 		}
 	}
 	
 	return null; /* Cas pas possible */
 	
     }
 
     /* Lors d'un choix dans le menu contextuel de complétion */
     public final void actionPerformed(final ActionEvent e) {
 	JEditTextArea jeta_query = getActiveJEditTextArea();
 	// Dans le cas d'un clic ou ENTER dans le menu contextuel :
 	// Insérer la complétion choisie
 	System.out.println("String lié au clic : " + e.getActionCommand());
 	int i_LastDotPosition = jeta_query.getText().substring(0, jeta_query.getCaretPosition()).lastIndexOf(".");
 	String s_pref = jeta_query.getText().substring(0, i_LastDotPosition+1);
 	String s_suff = jeta_query.getText().substring(jeta_query.getCaretPosition());
 	jeta_query.setText(s_pref + e.getActionCommand() + s_suff);
 	jeta_query.setCaretPosition( s_pref.length() + e.getActionCommand().length());
 	// Supprimer ce menu
 	remove(popup);
 	popup = null;
     }
 
     public void showStatus(String message){
 	status_bar.showStatus(message);
     }
 
     public void showStatus(String from, String message){
 	status_bar.showStatus(from, message);
     }
 
 
 }
 
 /**
  * Describe class <code>StatusBar</code> here.
  *
  * @author <a href="mailto:mad@portable-MAD">Marc Autord</a>
  * @version 1.0
  */
 class StatusBar extends JPanel
 {
 	private JLabel info;
 
 	// The constructor 
 	public StatusBar()
 	{
 		setLayout(new BorderLayout());
 
 		// Je crée un Label bourré d'espaces parce que je ne sais pas
 		// pas faire en sorte que le Label puisse accueillir un grand
 		// texte sinon.
 		add("West", info = new JLabel("                                                                                                                       ", JLabel.LEFT));
 		info.setMinimumSize(this.getSize());
        	}
 
 	public void showStatus(String status)
 	{
 		info.setText(status);
 	}
 
     public void showStatus(String from, String status) {
 	showStatus(from + " : " + status);
     }
 
 }
 
 
 /** À chaque onglet on attache différentes informations **/
 class InfosOnglet {
 
     private QueryTableModel modele;
     private JTable table;
     private AutoQuery app;
 
     /**
      * Describe onglet here.
      */
     private Component onglet;
 
     /**
      * Get the <code>Onglet</code> value.
      *
      * @return a <code>Component</code> value
      */
     public final Component getOnglet() {
 	return onglet;
     }
 
     /**
      * Set the <code>Onglet</code> value.
      *
      * @param newOnglet The new Onglet value.
      */
     public final void setOnglet(final Component newOnglet) {
 	this.onglet = newOnglet;
     }
     /**
      * Le composant dans l'onglet sert de pointeur vers l'onglet
      */
     JEditTextArea jeta; /* Quel est le JEditTextArea contenant la requête à exécuter */
 
 
 
     /**
      * Get the <code>App</code> value.
      *
      * @return a <code>AutoQuery</code> value
      */
     public final AutoQuery getApp() {
 	return app;
     }
 
     /**
      * Set the <code>App</code> value.
      *
      * @param newApp The new App value.
      */
     public final void setApp(final AutoQuery newApp) {
 	this.app = newApp;
     }
     public InfosOnglet(AutoQuery app, Component onglet) {
 	this.app = app;
 	this.onglet = onglet;
     }
 
     public String getTabTitle(){
 	JTabbedPane onglets = app.getQueryTabs();
 	return onglets.getTitleAt(onglets.indexOfTabComponent(onglet));
     }
 
     /**
      * Get the <code>Table</code> value.
      *
      * @return a <code>JTable</code> value
      */
     public final JTable getTable() {
 	return table;
     }
 
     /**
      * Set the <code>Table</code> value.
      *
      * @param newTable The new Table value.
      */
     public final void setTable(final JTable newTable) {
 	this.table = newTable;
     }
     /**
      * Get the <code>Modele</code> value.
      *
      * @return a <code>QueryTableModel</code> value
      */
     public final QueryTableModel getModele() {
 	return modele;
     }
 
     /**
      * Set the <code>Modele</code> value.
      *
      * @param newModele The new Modele value.
      */
     public final void setModele(final QueryTableModel newModele) {
 	this.modele = newModele;
     }
 
 
     public void setJETA(JEditTextArea jeta){
 	this.jeta = jeta; 
     }
     
     public JEditTextArea getJETA(){
 	return jeta;
     }
     
 }
 
 
 
 class QueryTableModel extends AbstractTableModel {
     InfosOnglet infosOnglet;
     Vector cache; // will hold String[] objects . . .
     int colCount;
     String[] headers;
 
     
     public QueryTableModel(InfosOnglet infos){
 	this.infosOnglet = infos;
 	cache = new Vector();
     }
 
     public String getColumnName(int i) {
 	return headers[i];
     }
 
     public int getColumnCount() {
 	return colCount;
     }
 
     public void setColCount(int col){
 	colCount = col;
     }
 
     public void setHeaders(String[] headers){
 	this.headers = headers;
     }
 
     public void setHeader(int i, String s){
 	this.headers[i] = s;
     }
 
     public int getRowCount() {
 	return cache.size();
     }
     
     public Object getValueAt(int row, int col) {
 	return ((String[])cache.elementAt(row))[col];
     }
 
   // All the real work happens here; in a real application,
   // we'd probably perform the query in a separate thread.
     public void runQuery(final Connection con, final String query) {
 	cache = new Vector();
 	try {
 	    SwingWorker worker = new SwingWorker <Void, Integer>(){
 
 		@Override
 		protected void process(List<Integer> chunks){
 		    int dernier_element = chunks.get(chunks.size() -1).intValue();
 		    if ( dernier_element % 10 == 0){
 			infosOnglet.getApp().showStatus(infosOnglet.getTabTitle(), 
 							"Traitement local : " + dernier_element + " %");
 		    }
 		}
 
 		@Override
 		public Void doInBackground() throws SQLException{
 		    try{
 			Statement st = con.createStatement();
 
 			System.out.println("Avant requête");			
 			ResultSet rs = st.executeQuery(query);
 			System.out.println("Après requête");
 			ResultSetMetaData meta = rs.getMetaData();
 			setColCount(meta.getColumnCount());
 			// Now we must rebuild the headers array with the new column names
 			setHeaders(new String[colCount]);
 			for (int h = 1; h <= colCount; h++) {
 			    setHeader(h - 1, meta.getColumnName(h));
 			}
 
 			rs.last();
 			int nlignes = rs.getRow();
 			rs.beforeFirst();
 			int ligne = 0;
 
 			while (rs.next()) {
 			    int colCount = getColumnCount();
 			    String[] record = new String[colCount];
 			    for (int i = 0; i < colCount; i++) {
 				record[i] = rs.getString(i + 1);
 			    }
 			    cache.addElement(record);
 			    publish(new Integer(100 * ++ligne / nlignes));
 			}
 
 		    } catch (SQLException e) {
 		    } catch (Exception e){
 			System.out.println("Exception dans le worker !");
 			System.out.println("Message de l'exception : " + e.getMessage());
 			
 		    } finally {
 			if (con != null){
 			    System.out.println("runQuery : Fermeture de la connexion à la BDD");
 			    con.close();
 			}
 		    }
 
 		    return null;
 
 		}
 
 		@Override
 		public void done(){
 		    System.out.println("Le résultat contient " + getRowCount() + " lignes et " + getColumnCount() + " colonnes.");
 		    infosOnglet.getApp().showStatus(infosOnglet.getTabTitle(),
 						    getRowCount() + " ligne(s) extraite(s).");
 		    fireTableChanged(null);
 		}
 	    };
 
 
 
 	    worker.execute();
 	} catch (Exception e){
 	    cache = new Vector(); // blank it out and keep going.
 	    e.printStackTrace();
 	    System.err.println("Exception lors de l'exécution de la requête : " + e.getMessage());
    	}
     }
 }

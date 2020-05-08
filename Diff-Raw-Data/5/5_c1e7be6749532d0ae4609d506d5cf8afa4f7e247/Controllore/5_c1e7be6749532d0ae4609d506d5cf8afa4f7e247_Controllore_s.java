 package gestionale.magazzino;
 
 import gestionale.magazzino.grafica.cancelleria.GraficaLogin;
 import gestionale.magazzino.grafica.cancelleria.GraficaRegistrazione;
 import gestionale.magazzino.grafica.cancelleria.MyModel;
 import gestionale.magazzino.grafica.dipendente.finestre.GraficaDipendente;
 import gestionale.magazzino.grafica.dipendente.finestre.ModificaProdotto;
 import gestionale.magazzino.grafica.dipendente.finestre.VisualizzaProdotto;
 import gestionale.magazzino.grafica.dipendente.pannelli.GraficaAccount;
 import gestionale.magazzino.grafica.dipendente.pannelli.GraficaCarrello;
 import gestionale.magazzino.grafica.dipendente.pannelli.GraficaProdotti;
 import gestionale.magazzino.grafica.responsabile.finestre.GraficaDipendenteSlezionato;
 import gestionale.magazzino.grafica.responsabile.finestre.GraficaInsProdotto;
 import gestionale.magazzino.grafica.responsabile.finestre.GraficaModificaProdotto;
 import gestionale.magazzino.grafica.responsabile.finestre.GraficaNotificaSelezionata;
 import gestionale.magazzino.grafica.responsabile.finestre.GraficaResponsabile;
 import gestionale.magazzino.grafica.responsabile.pannelli.GraficaAccountResponsabile;
 import gestionale.magazzino.grafica.responsabile.pannelli.GraficaMagazzino;
 
 import java.awt.event.WindowEvent;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 
 public class Controllore {
 	
 	
 	
 	/** 
 	 * Controlla campo vuoto, lunghezza e uguaglianza con la seconda password in fase di registrazione
 	 * @return il numero dell'errore, in modo d poterlo gestire singolarmente
 	 * @return 1 se la password  vuota
 	 * @return 2 se la password  maggiore di 12
 	 * @return 3 se le password non sono uguali
 	 */
 	public boolean checkPassword(String pass, String pass2){
 		int noErr = 0;
 		boolean b = false;
 		if(pass.isEmpty()){
 			noErr = 1;
 			b = false;
 		}else if(pass.length() >12){
 			noErr = 2;
 			b = false;
 		}else if(!pass.equals(pass2)){
 			noErr = 3;
 			b = false;
 		}else if(pass.equals(pass2)){
 			b = true;
 		}
 		
 		return b;
 	}
 	
 	/**
 	 * Controlla la validit sintattica della email
 	 * @param email l'email da prendere in esame
 	 * @return true o false in base alla validit della formattazione
 	 */
 	public boolean validateSintassiEmail(String email){
 		EmailValidator ev = new EmailValidator();
 		boolean emailValida = ev.validate(email);
 		
 		return emailValida;
 	}
 
 	/////////////////////////////////////////////////////////////////////////////////
 	// metodi pendy
 	
 	private GraficaLogin gl;
 	private GraficaDipendente gd;
 	private GraficaRegistrazione gr;
 	private GraficaResponsabile gresp;
 	private GraficaProdotti gp;
 	private GraficaAccount ga;
 	private VisualizzaProdotto vp;
 	private ModificaProdotto mp;
 	private MyModel modelloCatalogo;
 	private MyModel modelloCarrello;
 	private MyModel modelloNotifiche;
 	private MyModel modelloDipendenti;
 	private GraficaCarrello gc;
 	private ArrayList<Prodotto> prodotti;
 	private ArrayList<Acquisto> carrello;
 	private ArrayList<Notifica> notifiche;
 	private ArrayList<Dipendente> dipendenti;
 	private GraficaAccountResponsabile gar;
 	private GraficaMagazzino gm;
 	private gestionale.magazzino.Dipendente dip;
 	private MyListener m;
 	private Prodotto prod;
 	private Notifica not;
 	private GraficaInsProdotto gins;
 	private GraficaModificaProdotto gmp;
 	private GraficaNotificaSelezionata gns;
 	private GraficaDipendenteSlezionato gdp;
 	private Dipendente dipSel;
 	DateFormat dateFormat;
 	Date date;
	private int index = 0;
 	/**
 	 * Costruttore controllore
 	 * inizializza tutte le finestre grafiche,senza pero caricarne i componenti
 	 */
 	public Controllore()
 	{
 		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		date = new Date();
 		gdp = new GraficaDipendenteSlezionato();
 		gns = new GraficaNotificaSelezionata();
 		gmp = new GraficaModificaProdotto();
 		gins = new GraficaInsProdotto();
 		m = new MyListener();
 		modelloCatalogo = new MyModel();
 		modelloCarrello = new MyModel();
 		modelloNotifiche = new MyModel();
 		gar = new GraficaAccountResponsabile();
 		gm = new GraficaMagazzino();
 		gresp = new GraficaResponsabile();
 		gl = new GraficaLogin();
 		gp = new GraficaProdotti();
 		gr = new GraficaRegistrazione();
 		gd = new GraficaDipendente();
 		ga = new GraficaAccount();
 		vp = new VisualizzaProdotto();
 		gc = new GraficaCarrello();
 		mp = new ModificaProdotto();
 	}
 	
 	/**
 	 * mostra la finestra iniziale del programma
 	 */
 	public void start()
 	{
 		gl.init();
 	}
 	
 	/**
 	 * raccoglie i dati dai campi di login e controlla se l'utente puo effettuare l'accesso interrogando il database.
 	 * in caso negativo mostra gli errori commessi dall'utente nel inserire le credenziali
 	 * in caso positivo mostra l'avvenuto accesso al sistema e mostra la finestra del catalogo
 	 */
 	public int isConnected()
 	{
 		int stato = 0;
 		gl.pulisciErrori();
 		String email = gl.getEmail();
 		String password = gl.getPassword();
 		boolean b1 = gestionale.magazzino.models.Dipendente.validateEmail(email);
 		boolean b2 = gestionale.magazzino.models.Dipendente.validatePassword(email, password);
 		boolean b3 = gestionale.magazzino.models.Dipendente.validateResponsabile(email, password);
 		boolean b4 = gestionale.magazzino.models.Dipendente.isActive(email);
 		if(b2)
 		{
 			if(b3)
 			{
 				if(b4)
 				{
 					stato = 3;
 					dip = new Dipendente();
 					dip = gestionale.magazzino.models.Dipendente.visualizzaDipendente(email, password);
 					JOptionPane.showMessageDialog(gl, "Login Effetuato");
 					gresp.init();
 					m.setTable(gresp.getTableNotifiche());
 					gl.disposeF();
 				}
 				if(!b4)
 				{
 					JOptionPane.showMessageDialog(gl, "Utente Disabilitato");
 				}
 				
 			}
 			else
 			{
 				if(b4)
 				{
 					stato = 1;
 					dip = new Dipendente();
 					dip = gestionale.magazzino.models.Dipendente.visualizzaDipendente(email, password);
 					JOptionPane.showMessageDialog(gl, "Login Effetuato");
 					gd.init();
 					m.setTable(gd.getTableProdotti());
 					gl.disposeF();
 				}
 				if(!b4)
 				{
 					JOptionPane.showMessageDialog(gl, "Utente Disabilitato");
 				}
 				
 			}
 
 		}
 		else
 		{
 			stato = 0;
 			if(!b1 && !b2)
 			{
 				gl.setErroreEmail("Email errata");
 				gl.setErrorePass("Password errata");
 			}
 			if(!b1)
 			{
 				gl.setErroreEmail("Email errata");
 			}
 			else
 			{
 				gl.setErrorePass("Password errata");
 			}
 		}
 		
 		return stato;
 		
 	}
 	
 	/**
 	 * disalloca le componenti grafiche alla disconnessione dell'utente
 	 */
 	public void disconnect()
 	{
 		gl.disposeF();
 	}
 	
 	/**
 	 * mostra la finestra grafica di registrazione
 	 */
 	public void registering()
 	{
 		gr.init();
 		gl.disposeF();
 	}
 	
 	/**
 	 * mostra la finestra grafica di login dopo che l'utente si  registrato 
 	 */
 	public void logging()
 	{
 		gl.init();
 		gr.disposeF();
 	}
 	
 	/**
 	 * pulisce i campi della finestra di registrazione e gli eventuali errori
 	 */
 	public void resetRegistrazione()
 	{
 		gr.pulisciErrori();
 		gr.pulisci();
 	}
 	
 	/**
 	 * controlla se i campi inseriti dall'utente al momento della registrazione sono corretti.
 	 * in caso negativo mostra quali campi sono errati
 	 * in caso positivo invia i dati al database e notifica l'utente dell'avvenuta registrazione
 	 */
 	public void registered()
 	{
 		gr.pulisciErrori();
 		boolean b = false;
 		String nome = gr.getNome();
 		String cognome = gr.getCognome();
 		String email = gr.getEmail();
 		String password = gr.getPassword();
 		String password2 = gr.getPassword2();
 		b = this.validateSintassiEmail(email);
 		if(b)
 		{
 			b = checkPassword(password, password2);
 			if(!b)
 			{
 				gr.setErrorePassword2("Le password non coincidono");
 				b = false;
 			}
 			if(nome.isEmpty())
 			{
 				gr.setErroreNome("Nome non valido");
 				b = false;
 			}
 			if(cognome.isEmpty())
 			{
 				gr.setErroreCognome("Cognome non valid");
 				b = false;
 			}
 			if(b)
 			{
 				b = gestionale.magazzino.models.Dipendente.validateEmail(email);
 				if(!b)
 				{
 					this.isRegistered(nome,cognome,password,email);
 					JOptionPane.showMessageDialog(gr, "Registrazione effetuata");
 					gl.init();
 					gr.disposeF();
 					
 				}
 				else
 				{
 					b = false;
 					gr.setErroreEmail("Email gia presente nell'archivio");
 				}
 			}
 		}
 		else 
 		{
 			b = false;
 			gr.setErroreEmail("Email non valida");
 			b = checkPassword(password, password2);
 			if(!b)
 			{
 				gr.setErrorePassword2("Le password non coincidono");
 				b = false;
 			}
 			if(nome.isEmpty())
 			{
 				gr.setErroreNome("Nome non valido");
 				b = false;
 			}
 			if(cognome.isEmpty())
 			{
 				gr.setErroreCognome("Cognome non valid");
 				b = false;
 			}
 		}	
 		
 		
 	}
 	
 	/**
 	 * controlla se l'utente puo effettuare la registrazione oppure se  gia registrato al sistema
 	 * 
 	 * @param nome
 	 * @param cognome
 	 * @param password
 	 * @param email
 	 */
 	public void isRegistered(String nome,String cognome,String password,String email)
 	{
 		String tipo = "dipendente";
 		gestionale.magazzino.models.Dipendente.inserisciDipendente(nome, cognome, password, email, tipo);
 	}
 	
 	/**
 	 * mostra la tabella dell'account con i dati dell'utente
 	 */
 	public void showAccount()
 	{
 		gd.setAccount(dip.getEmail(), dip.getNome(), dip.getCognome(), dip.getTipo());
 		gd.setPannelloSelezionato("account");
 	}
 
 	/**
 	 * inizializza il catalogo prendendo i dati dal database e caricandoni in un modelloCatalogo astratto per una tabella
 	 */
 	public void initCatalogo()
 	{
 		int ID;
 		String nome;
 		int qta;
 		float prezzo;
 		prodotti = new ArrayList<gestionale.magazzino.Prodotto>();
 		prodotti = gestionale.magazzino.models.Prodotto.visualizzaProdotti();
 		String[] colonne = {"ID","nome","quantita","prezzo","acquista"};
 		MyModel model = new MyModel(prodotti.size(),5,colonne);
 		for(int i = 0;i < prodotti.size(); i++)
 		{
 			ID = prodotti.get(i).getId_Prodotto();
 			nome = prodotti.get(i).getNome();
 			qta = prodotti.get(i).getQuantit();
 			prezzo = prodotti.get(i).getPrezzo();
 			model.setValueAt(ID, i, 0);
 			model.setValueAt(nome, i, 1);
 			model.setValueAt(qta, i, 2);
 			model.setValueAt(prezzo, i, 3);
 			model.setValueAt(Boolean.FALSE, i, 4);
 		}
 		modelloCatalogo = model;
 	}
 	
 	/**
 	 * funzione che restituisce il numero di record presenti nel modelloCatalogo della tabella
 	 * @return
 	 */
 	public int getRowCount()
 	{
 		return modelloCatalogo.getRowCount();
 	}
 	/**
 	 * funzione che restituisce il numero di campi presenti nel modelloCatalogo della tabella
 	 * @return
 	 */
 	public int getColumnCount()
 	{
 		return modelloCatalogo.getColumnCount();
 	}
 	/**
 	 * funzione che restituisce il modelloCatalogo della tabella
 	 * @return
 	 */
 	public MyModel getCatalogo()
 	{
 		return modelloCatalogo;
 	}
 	/**
 	 * funzione che restituisce il nome dei campi del modelloCatalogo della tabella
 	 * @return
 	 */
 	public String[] getColumnNames()
 	{
 		String[] s = new String[5];
 		for(int i = 0;i <5;i++)
 		{
 			s[i] = modelloCatalogo.getColumnName(i);
 		}
 		return s;
 	}
 	/**
 	 * riabilita la finestra dipendente in caso fosse stata disabilitata
 	 * mostra la tabella del catalogo
 	 */
 	public void showCatalogo()
 	{
 		gd.setState(true);
 		m.setTable(gd.getTableProdotti());
 		gd.setPannelloSelezionato("prodotti");
 	}
 	/**
 	 * reinizializza la finestra del catalogo (da ottimizzare)
 	 */
 	public void updateCatalogo(int x)
 	{
 		gd.setState(true);
 		modelloCatalogo = (MyModel) gd.getTableProdotti().getModel();
 		if(modelloCatalogo.getRowCount() > 0)
 		{
 			modelloCatalogo.setValueAt(Boolean.FALSE, x, 4);
 		}
 		gd.setPannelloSelezionato("prodotti");
 	}
 	/**
 	 * riabilita la finestra dipendente in caso fosse stata disabilitata
 	 * mostra la tabella del carrello
 	 */
 	public void showCarrello()
 	{
 		gd.setState(true);
 		m.setTable(gd.getTableCarrello());
 		gd.setPannelloSelezionato("carrello");
 	}
 	/**
 	 * reinizializza la finestra del carrello (da ottimizzare)
 	 */
 	public void updateCarrello(int x)
 	{
 		gd.setState(true);
 		modelloCarrello = (MyModel) gd.getTableProdotti().getModel();
 		if(modelloCarrello.getRowCount() > 0)
 		{
 			modelloCarrello.setValueAt(Boolean.FALSE, x, 4);
 		}
 		gd.setPannelloSelezionato("carrello");
 	}
 	/**
 	 * restituisce il modelloCatalogo della tabella
 	 * @return
 	 */
 	public AbstractTableModel getCarrrello()
 	{
 		return modelloCarrello;
 	}
 	/**
 	 * carica dal database i prodotti scelti da un utente
 	 */
 	////////// modificare la query per la raccolta dei dati
 	public void initCarrello()
 	{
 		int ID;
 		String nome;
 		int qta;
 		float spesa;
 		carrello = new ArrayList<Acquisto>();
 		//carrello = gestionale.magazzino.models.Acquisto.visualizzaAcquistiDipendente(dip.getId_Dipendente());
 		String[] colonne = {"ID","Prodotto","Quantita","Spesa","Seleziona"};
 		MyModel model = new MyModel(carrello.size(),5,colonne);
 		for(int i = 0;i < carrello.size(); i++)
 		{
 			ID = carrello.get(i).getIdAcquisto();
 			nome = carrello.get(i).getNomeProdotto();
 			qta = carrello.get(i).getQta();
 			spesa = carrello.get(i).getSpesa();
 			model.setValueAt(ID, i, 0);
 			model.setValueAt(nome, i, 1);
 			model.setValueAt(qta, i, 2);
 			model.setValueAt(spesa, i, 3);
 			model.setValueAt(Boolean.FALSE, i, 4);
 		}
 		modelloCarrello = model;
 	}
 	/**
 	 * disalloca tutte le risorse create all'accesso di un dipendente al sistema
 	 */
 	public void disposeDipendente()
 	{
 		gl.dispose();
 		gp.dispose();
 		gr.dispose();
 		vp.dispose();
 		mp.dispose();
 		gd.disposeF();
 	}
 	/**
 	 * effettua il logout dall'account del dipendente mostrando di nuovo la finestra del login
 	 */
 	public void doLogout()
 	{
 		gd.disposeF();
 		this.disposeDipendente();
 		gl.init();
 	}
 	
 	/**
 	 * visualizza il prodotto selezionato dal catalogo
 	 * @param p
 	 */
 
 	public void showProdotto(int p)
 	{
 		JTable tabella = gd.getTableProdotti();
 		int id = Integer.parseInt(tabella.getValueAt(p, 0).toString());
 		String nome = tabella.getValueAt(p, 1).toString();
 		float prezzo = Float.parseFloat(tabella.getValueAt(p,3).toString());
 		int quantita = Integer.parseInt(tabella.getValueAt(p,2).toString());
 		prod = new Prodotto(id,nome,quantita,prezzo);
 		vp.init();
 		vp.setIDProdotto(""+id);
 		vp.setNomeProdotto(nome);
 		vp.setPrezzoProdotto(""+prezzo);
 		vp.setQuantitaProdotto(""+quantita);
 		gd.setState(false);
 	}
 	
 	/**
 	 * visualizza il prodotto selezionato dagli ordini
 	 * @param p
 	 */
 	// inserire query di controllo su ID prodotto
 	public void showOrdinato(int p)
 	{
 		mp.init();
 		gd.setState(false);
 	}
 	
 	
 	public void logoutResp()
 	{
 		gresp.disposeF();
 		this.disposeResp();
 		gl.init();
 	}
 	
 	public void disposeResp()
 	{
 		gm.dispose();
 		gl.dispose();
 		gp.dispose();
 		gr.dispose();
 		vp.dispose();
 		mp.dispose();
 		gresp.disposeF();
 		
 	}
 	
 	public void showAccountResp()
 	{
 		gresp.setAccount(dip.getEmail(),dip.getNome(),dip.getCognome(),dip.getTipo());
 		gresp.setPannelloSelezionato("account");
 	}
 	
 	public void showMagazzino()
 	{
 		gresp.setState(true);
 		m.setTable(gresp.getTableProdotti());
 		gresp.setPannelloSelezionato("magazzino");
 	}
 	
 	public void initNotifiche()
 	{
 		int IdNotifica;
 		int IdDipNotif;
 		String data;
 		notifiche = new ArrayList<Notifica>();
 		notifiche = gestionale.magazzino.models.Notifica.visualizzaNotificheValide();
 		String[] colonne = {"ID","Nome Dipendente","Data","Mostra"};
 		MyModel model = new MyModel(notifiche.size(),4,colonne);
 		Dipendente dipendente = new Dipendente();
 		for(int i = 0;i < notifiche.size(); i++)
 		{
 			IdNotifica = notifiche.get(i).getIdNotifica();
 			IdDipNotif = notifiche.get(i).getIdDipendenteNotificato();
 			dipendente = gestionale.magazzino.models.Dipendente.visualizzaDipendente(IdDipNotif);
 			String nome = dipendente.getEmail();
 			data = notifiche.get(i).getData();
 			model.setValueAt(IdNotifica, i, 0);
 			model.setValueAt(nome, i, 1);
 			model.setValueAt(data, i, 2);
 			model.setValueAt(Boolean.FALSE, i, 3);
 		}
 		modelloNotifiche = model;
 	}
 	
 	public void showNotifiche()
 	{
 		gresp.setState(true);
 		m.setTable(gresp.getTableNotifiche());
 		gresp.setPannelloSelezionato("notifiche");
 	}
 	
 	public void showListaDip()
 	{
 		gresp.setState(true);
 		m.setTable(gresp.getTableDipendenti());
 		gresp.setPannelloSelezionato("listaDip");
 	}
 	
 	public void gotoCatalogo(int x)
 	{
 		vp.doClose();
 		updateCatalogo(x);
 		
 	}
 	
 	public void controlloOrdine(int x)
 	{
		vp.setIndex(index);
 		int q = vp.getQuantitaProdotto();
 		int qins = vp.getQuantita();
 		System.out.println(q);
 		if(qins > 0)
 		{
 			float y = vp.getPrezzoProdotto();
 			float z = y*q;
 			String b = vp.getFondoScelto();
 			Fondo f = new Fondo();
 			f = gestionale.magazzino.models.Fondo.visualizzaFondo(b);
 			float v = f.getImporto();
 			if(z > v)
 			{
 				JOptionPane.showMessageDialog(vp, "Fondo non sufficiente");
 			}
 			else
 			{
				index = vp.getIndex();
 				vp.doClose();
 				f.setImporto(f.getImporto()-z);
 				gestionale.magazzino.models.Fondo.cancellaFondo(f.getNome());
 				gestionale.magazzino.models.Fondo.inserisciFondo(f.getNome(),f.getImporto());
 				prod = gestionale.magazzino.models.Prodotto.visualizzaProdotto(vp.getIDProdotto());
 				Acquisto acq = new Acquisto();
 				acq.setIdDipendente(dip.getId_Dipendente());
 				acq.setNomeDipendente(dip.getNome());
 				acq.setIdProdotto(vp.getIDProdotto());
 				acq.setNomeProdotto(vp.getNomeProdotto());
 				acq.setIdFondo(f.getId_Fondo());
 				acq.setNomeFondo(f.getNome());
 				acq.setQta(qins);
 				acq.setSpesa(z);
 				date = new Date();
 				String data = dateFormat.format(date);
 				System.out.println(data);
 				acq.setDataAcquisto(data);
 				//gestionale.magazzino.models.Acquisto.inserisciAcquisto(acq.getIdDipendente(), acq.getIdProdotto(), acq.getIdFondo(), acq.getQta());
 				JOptionPane.showMessageDialog(vp, "Prodotto aggiunto al carrello");
 				updateCatalogo(x);
 			}
 		}
 		if(q <= 0)
 		{
 			int i =JOptionPane.showConfirmDialog(vp,"Inviare una notifica al responsabile?",null,JOptionPane.YES_NO_OPTION);
 			if(i == 0)
 			{
 				String msg = "L'oggetto: "+vp.getNomeProdotto()+" non  disponibile in magazzino";
 				gestionale.magazzino.models.Notifica.inserisciNotifica(1, 1, msg);
 				JOptionPane.showMessageDialog(vp, "Notifica Inviata");
 			}
 		}
 	}
 	
 	public void showOption()
 	{
 		gins.init();
 		gresp.setState(false);
 		updateMagazzino(0);
 	}
 	
 	public void updateMagazzino(int x)
 	{
 		gresp.setState(true);
 		modelloCatalogo = (MyModel) gresp.getTableProdotti().getModel();
 		if(modelloCatalogo.getRowCount() > 0)
 		{
 			modelloCatalogo.setValueAt(Boolean.FALSE, x, 4);
 		}
 		gresp.setPannelloSelezionato("magazzino");
 	}
 	
 	public void initMagazzino()
 	{
 		int ID;
 		String nome;
 		int qta;
 		float prezzo;
 		prodotti = new ArrayList<gestionale.magazzino.Prodotto>();
 		prodotti = gestionale.magazzino.models.Prodotto.visualizzaProdotti();
 		String[] colonne = {"ID","Nome","Quantita","Prezzo","Modifica"};
 		MyModel model = new MyModel(prodotti.size(),5,colonne);
 		for(int i = 0;i < prodotti.size(); i++)
 		{
 			ID = prodotti.get(i).getId_Prodotto();
 			nome = prodotti.get(i).getNome();
 			qta = prodotti.get(i).getQuantit();
 			prezzo = prodotti.get(i).getPrezzo();
 			model.setValueAt(ID, i, 0);
 			model.setValueAt(nome, i, 1);
 			model.setValueAt(qta, i, 2);
 			model.setValueAt(prezzo, i, 3);
 			model.setValueAt(Boolean.FALSE, i, 4);
 		}
 		modelloCatalogo = model;
 	}
 	
 	public void showModificaProdotto(int p)
 	{
 		gresp.setState(false);
 		JTable tabella = gresp.getTableProdotti();
 		int id = Integer.parseInt(tabella.getValueAt(p,0).toString());
 		String nome = tabella.getValueAt(p, 1).toString();
 		float prezzo = Float.parseFloat(tabella.getValueAt(p,3).toString());
 		int quantita = Integer.parseInt(tabella.getValueAt(p,2).toString());
 		prod = new Prodotto(id,nome,quantita,prezzo);
 		gmp.init();
 		gmp.setNome(nome);
 		gmp.setQuantita(quantita);
 		gmp.setPrezzo(prezzo);
 	}
 	
 	public void inserisciProdotto()
 	{
 		String nome = gins.getNome();
 		int qta = gins.getQuantita();
 		float prezzo = gins.getPrezzo();
 		gestionale.magazzino.models.Prodotto.inserisciProdotto(nome, qta, prezzo);
 		initMagazzino();
 		gresp.updateMagazzino(modelloCatalogo);
 		gins.doClose();
 		
 		
 	}
 	
 	public void gotoMagazzino(int x)
 	{
 		gins.disposeF();
 		updateMagazzino(x);
 	}
 	
 	public void modificaProdottoResponsabile()
 	{
 		gestionale.magazzino.models.Prodotto.cancellaProdotto(prod.getNome());
 		prod.setNome(gmp.getNome());
 		prod.setQuantit(gmp.getQuantita());
 		prod.setPrezzo(gmp.getPrezzo());
 		gestionale.magazzino.models.Prodotto.inserisciProdotto(prod.getNome(), prod.getQuantit(), prod.getPrezzo());
 		prod = null;
 		gmp.doClose();
 		initMagazzino();
 		gresp.updateMagazzino(modelloCatalogo);
 		updateMagazzino(0);
 	}
 	
 	public void rimuoviProdottoResponsabile()
 	{
 		prod.setNome(gmp.getNome());
 		gestionale.magazzino.models.Prodotto.cancellaProdotto(prod.getNome());
 		prod = null;
 		gmp.doClose();
 		initMagazzino();
 		gresp.updateMagazzino(modelloCatalogo);
 		updateMagazzino(0);
 	}
 	
 	public void gotoMagazzino2()
 	{
 		gmp.doClose();
 		updateMagazzino(0);
 	}
 
 	
 	public AbstractTableModel getNotifiche() {
 		return modelloNotifiche;
 	}
 
 	
 	public void showNotifica(int x) 
 	{
 		gresp.setState(false);
 		JTable tabella = gresp.getTableNotifiche();
 		int idNotifica = (int) tabella.getValueAt(x, 0);
 		Notifica notifica = new Notifica();
 		notifica = gestionale.magazzino.models.Notifica.visualizzaNotifica(idNotifica);
 		not = new Notifica(notifica.getIdNotifica(),notifica.getIdDipendente(),notifica.getIdDipendenteNotificato(),notifica.getNotifica(),notifica.getData(),true);
 		gns.init();
 		gns.setTesto(notifica.getNotifica());
 		gns.setData(notifica.getData());
 		
 	}
 	
 	public void updateNotifiche(int x)
 	{
 		gresp.setState(true);
 		modelloNotifiche = (MyModel) gresp.getTableNotifiche().getModel();
 		if(modelloNotifiche.getRowCount() > 0)
 		{
 			modelloNotifiche.setValueAt(Boolean.FALSE, x, 3);
 		}
 		gresp.setPannelloSelezionato("notifiche");
 	}
 	
 	public void gotoNotifiche(int x)
 	{
 		gns.doClose();
 		updateNotifiche(0);
 	}
 
 	public void eliminaNotifica() 
 	{
 		int i =JOptionPane.showConfirmDialog(gns,"Cancellare notifica?",null,JOptionPane.YES_NO_OPTION);
 		if(i == 0)
 		{
 			gestionale.magazzino.models.Notifica.cancellaNotifica(not.getIdNotifica());
 			not = null;
 			gns.doClose();
 			initNotifiche();
 			gresp.updateNotifiche(modelloNotifiche);
 			updateNotifiche(0);
 		}
 		
 	}
 
 	public void initListaDip() {
 		int idDipendente;
 		String tipo;
 		String email;
 		String s;
 		boolean isActive;
 		dipendenti = new ArrayList<gestionale.magazzino.Dipendente>();
 		dipendenti = gestionale.magazzino.models.Dipendente.visualizzaDipendenti();
 		String[] colonne = {"ID","Tipo","Email","Stato","Seleziona"};
 		MyModel model = new MyModel(dipendenti.size(),5,colonne);
 		for(int i = 0;i < dipendenti.size(); i++)
 		{
 			idDipendente = dipendenti.get(i).getId_Dipendente();
 			tipo = dipendenti.get(i).getTipo();
 			email = dipendenti.get(i).getEmail();
 			isActive = dipendenti.get(i).isActive();
 			model.setValueAt(idDipendente, i, 0);
 			model.setValueAt(""+tipo, i, 1);
 			model.setValueAt(email, i, 2);
 			model.setValueAt(isActive, i, 3);
 			model.setValueAt(Boolean.FALSE, i, 4);
 		}
 		
 		modelloDipendenti = model;
 	}
 	
 	public MyModel getListaDip()
 	{
 		return modelloDipendenti;
 	}
 	
 	public void updateDipendenti(int x)
 	{
 		gresp.setState(true);
 		modelloDipendenti = (MyModel) gresp.getTableDipendenti().getModel();
 		if(modelloDipendenti.getRowCount() > 0)
 		{
 			modelloDipendenti.setValueAt(Boolean.FALSE, x, 4);
 		}
 		gresp.setPannelloSelezionato("listaDip");
 	}
 	
 	public void gotoDipendenti()
 	{
 		gdp.doClose();
 		updateDipendenti(0);
 	}
 	
 	public void showDipendente(int x)
 	{
 		gresp.setState(false);
 		JTable tabella = gresp.getTableDipendenti();
 		int IdDipendente = Integer.parseInt(tabella.getValueAt(x, 0).toString());
 		Dipendente dip = new Dipendente();
 		dip = gestionale.magazzino.models.Dipendente.visualizzaDipendente(IdDipendente);
 		dipSel = new Dipendente(dip.getId_Dipendente(),dip.getNome(),dip.getCognome(),dip.getEmail(),dip.getPassword(),dip.getTipo(),dip.isActive());
 		gdp.init();
 		gdp.setId(""+dipSel.getId_Dipendente());
 		gdp.setNome(""+dipSel.getNome());
 		gdp.setCognome(""+dipSel.getCognome());
 		gdp.setEmail(""+ dipSel.getEmail());
 		gdp.setTipo(""+dipSel.getTipo().substring(0, 3));
 		String s;
 		if(dipSel.isActive())
 		{
 			s = "Attivo";
 		}
 		else
 		{
 			s = "Disabilitato";
 		}
 		gdp.setStato(s);
 	}
 
 	public void modificaDipendenteResp() {
 		Dipendente dip = new Dipendente();
 		int i = Integer.parseInt(gdp.getID());
 		String pass = (gestionale.magazzino.models.Dipendente.visualizzaDipendente(i).getPassword());
 		gestionale.magazzino.models.Dipendente.cancellaDipendente(i);
 		String t = gdp.getTipoScelto();
 		if(t.equals("res"))
 		{
 			t = "";
 			t = "responsabile";
 		}
 		else
 		{
 			t = "";
 			t = "dipendente";
 		}
 		dip.setTipo(t);
 		gestionale.magazzino.models.Dipendente.inserisciDipendente(gdp.getNome(), gdp.getCognome(), pass, gdp.getEmail(), t);
 		String s = gdp.getStatoScelto();
 		if(s.equals("Attivo"))
 		{
 			gestionale.magazzino.models.Dipendente.attivaDipendente(gdp.getEmail());
 		}
 		else
 		{
 			gestionale.magazzino.models.Dipendente.disattivaDipendente(gdp.getEmail());
 		}
 		
 		dip = null;
 		gdp.doClose();
 		initListaDip();
 		gresp.updateDipendenti(modelloDipendenti);
 		updateDipendenti(0);
 	}
 
 	public void rimuoviDipendenteResp() {
 		
 		int i =JOptionPane.showConfirmDialog(gns,"Cancellare Dipendente?",null,JOptionPane.YES_NO_OPTION);
 		if(i == 0)
 		{
 			gestionale.magazzino.models.Dipendente.cancellaDipendente(Integer.parseInt(gdp.getID()));
 			dipSel = null;
 			gdp.doClose();
 			initListaDip();
 			gresp.updateDipendenti(modelloDipendenti);
 			updateDipendenti(0);
 		}
 	}
 	
 }

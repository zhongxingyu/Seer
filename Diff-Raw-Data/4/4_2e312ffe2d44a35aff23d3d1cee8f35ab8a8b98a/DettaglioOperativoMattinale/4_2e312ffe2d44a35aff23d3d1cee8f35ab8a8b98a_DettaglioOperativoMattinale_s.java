 package it.ivncr.erp.jsf.managedbean.operativo.mattinale;
 
 import it.ivncr.erp.jsf.managedbean.accesso.session.LoginInfo;
 import it.ivncr.erp.model.commerciale.ods.OrdineServizio;
 import it.ivncr.erp.model.operativo.Servizio;
 import it.ivncr.erp.model.personale.Addetto;
 import it.ivncr.erp.model.personale.Reparto;
 import it.ivncr.erp.model.personale.SistemaLavoro;
 import it.ivncr.erp.service.ServiceFactory;
 import it.ivncr.erp.service.addetto.AddettoService;
 import it.ivncr.erp.service.lut.LUTService;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 
 import org.apache.commons.lang3.time.DurationFormatUtils;
 import org.primefaces.model.DefaultTreeNode;
 import org.primefaces.model.TreeNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @ManagedBean
 @ViewScoped
 public class DettaglioOperativoMattinale implements Serializable {
 
 	private static final Logger logger = LoggerFactory.getLogger(DettaglioOperativoMattinale.class);
 
 	private static final long serialVersionUID = 1L;
 
 	@ManagedProperty("#{loginInfo}")
     private LoginInfo loginInfo;
 
 	private TreeNode reparti;
 	private TreeNode selectedReparto;
 
 	private Date dataMattinale;
 
 	private List<AddettoRow> addetti;
 
 	private List<Object> serviziAddetto;
 
 	private List<LargeGridRow> servizi;
 	private List<String> serviziColumns;
 
 
 	@PostConstruct
 	public void init() {
 
 		LUTService lutService = ServiceFactory.createService("LUT");
 		List<Reparto> list = lutService.listItems("Reparto", "descrizione", "azienda.id", loginInfo.getCodiceAzienda());
 
 		reparti = new DefaultTreeNode("root", null);
 		reparti.setExpanded(true);
 		reparti.setSelectable(false);
 
 		TreeNode azienda = new DefaultTreeNode("azienda", loginInfo.getAzienda(), reparti);
 		azienda.setExpanded(true);
 		azienda.setSelectable(false);
 
 		for(Reparto reparto : list) {
 			new DefaultTreeNode("reparto", reparto, azienda);
 		}
 
 		logger.debug("Initialization performed.");
 	}
 
 	@SuppressWarnings("unchecked")
 	public void riempiMattinale() {
 
 		logger.debug("Entering riempiMattinale method.");
 
 		// Check conditions. In order to be able to complete the query, both reparto and
 		// data mattinale should have been specified.
 		//
 		if(selectedReparto == null) {
 
 			// Set the error message for missing selection in reparto tree.
 			//
 			FacesMessage message = new FacesMessage(
 					FacesMessage.SEVERITY_ERROR,
 					"Reparto non selezionato",
 					"E' necessario specificare un reparto.");
 			FacesContext.getCurrentInstance().addMessage(null, message);
 			return;
 		}
 		if(dataMattinale == null) {
 
 			// Set the error message for missing data mattinale.
 			//
 			FacesMessage message = new FacesMessage(
 					FacesMessage.SEVERITY_ERROR,
 					"Data mattinale non inserita",
 					"E' necessario immettere la data del mattinale.");
 			FacesContext.getCurrentInstance().addMessage(null, message);
 			return;
 		}
 
 		// Extract selected reparto id.
 		//
 		Reparto reparto = (Reparto)selectedReparto.getData();
 		Integer codiceReparto = reparto.getId();
 
 
 		AddettoService as = ServiceFactory.createService("Addetto");
 		List<Object[]> listAddetti = as.listAddettiAndServizi(codiceReparto, dataMattinale);
 		addetti = new ArrayList<AddettoRow>();
 		for(Object[] o : listAddetti) {
 
 			Addetto addetto = (Addetto)o[0];
 			List<Servizio> servizi = (List<Servizio>)o[1];
 			Long workedMillis = (Long)o[2];
 			SistemaLavoro sistemaLavoro = (SistemaLavoro)o[3];
 
 			AddettoRow row = new AddettoRow();
 			row.setMatricola(addetto.getMatricola());
 			row.setCognome(addetto.getCognome());
 			row.setNome(addetto.getNome());
 
 			if(workedMillis > 0) {
 				row.setOreLavorate(DurationFormatUtils.formatDuration(workedMillis, "HH:mm"));
 			} else {
				row.setOreLavorate(servizi.get(0).getCausaleOds().getDescrizione());
 			}
 			row.setOreDisponibili("00:00");
 			row.setSistemaLavoro(sistemaLavoro.getTipoSistemaLavoro().getDescrizione());
 
 			addetti.add(row);
 		}
 	}
 
 	public void onServizioClick() {
 
 		System.out.println("onServizioClick");
 	}
 
 	public LoginInfo getLoginInfo() {
 		return loginInfo;
 	}
 
 	public void setLoginInfo(LoginInfo loginInfo) {
 		this.loginInfo = loginInfo;
 	}
 
 	public TreeNode getReparti() {
 		return reparti;
 	}
 
 	public void setReparti(TreeNode reparti) {
 		this.reparti = reparti;
 	}
 
 	public TreeNode getSelectedReparto() {
 		return selectedReparto;
 	}
 
 	public void setSelectedReparto(TreeNode selectedReparto) {
 		this.selectedReparto = selectedReparto;
 	}
 
 	public Date getDataMattinale() {
 		return dataMattinale;
 	}
 
 	public void setDataMattinale(Date dataMattinale) {
 		this.dataMattinale = dataMattinale;
 	}
 
 	public List<AddettoRow> getAddetti() {
 		return addetti;
 	}
 
 	public void setAddetti(List<AddettoRow> addetti) {
 		this.addetti = addetti;
 	}
 
 	public List<Object> getServiziAddetto() {
 		return serviziAddetto;
 	}
 
 	public void setServiziAddetto(List<Object> serviziAddetto) {
 		this.serviziAddetto = serviziAddetto;
 	}
 
 	public List<LargeGridRow> getServizi() {
 		return servizi;
 	}
 
 	public void setServizi(List<LargeGridRow> servizi) {
 		this.servizi = servizi;
 	}
 
 	public List<String> getServiziColumns() {
 		return serviziColumns;
 	}
 
 	public void setServiziColumns(List<String> serviziColumns) {
 		this.serviziColumns = serviziColumns;
 	}
 
 
 	public class AddettoRow {
 
 		private String matricola;
 		private String cognome;
 		private String nome;
 		private String sistemaLavoro;
 		private String oreLavorate;
 		private String oreDisponibili;
 
 
 		public String getMatricola() {
 			return matricola;
 		}
 
 		public void setMatricola(String matricola) {
 			this.matricola = matricola;
 		}
 
 		public String getCognome() {
 			return cognome;
 		}
 
 		public void setCognome(String cognome) {
 			this.cognome = cognome;
 		}
 
 		public String getNome() {
 			return nome;
 		}
 
 		public void setNome(String nome) {
 			this.nome = nome;
 		}
 
 		public String getSistemaLavoro() {
 			return sistemaLavoro;
 		}
 
 		public void setSistemaLavoro(String sistemaLavoro) {
 			this.sistemaLavoro = sistemaLavoro;
 		}
 
 		public String getOreLavorate() {
 			return oreLavorate;
 		}
 
 		public void setOreLavorate(String oreLavorate) {
 			this.oreLavorate = oreLavorate;
 		}
 
 		public String getOreDisponibili() {
 			return oreDisponibili;
 		}
 
 		public void setOreDisponibili(String oreDisponibili) {
 			this.oreDisponibili = oreDisponibili;
 		}
 	}
 
 
 	public class LargeGridRow {
 
 		private OrdineServizio ods;
 		private Orario orario;
 		private List<AddettoCell> addetti;
 
 		public LargeGridRow() {
 			addetti = new ArrayList<AddettoCell>();
 		}
 
 		public OrdineServizio getOds() {
 			return ods;
 		}
 
 		public void setOds(OrdineServizio ods) {
 			this.ods = ods;
 		}
 
 		public Orario getOrario() {
 			return orario;
 		}
 
 		public void setOrario(Orario orario) {
 			this.orario = orario;
 		}
 
 		public List<AddettoCell> getAddetti() {
 			return addetti;
 		}
 
 		public void setAddetti(List<AddettoCell> addetti) {
 			this.addetti = addetti;
 		}
 	}
 
 
 	public class AddettoCell {
 
 		private Addetto addetto;
 		private List<Servizio> servizi;
 
 		public AddettoCell() {
 
 			servizi = new ArrayList<Servizio>();
 		}
 
 		public Addetto getAddetto() {
 			return addetto;
 		}
 
 		public void setAddetto(Addetto addetto) {
 			this.addetto = addetto;
 		}
 
 		public List<Servizio> getServizi() {
 			return servizi;
 		}
 
 		public void setServizi(List<Servizio> servizi) {
 			this.servizi = servizi;
 		}
 	}
 
 
 	public class Orario {
 
 		private Integer quantita1;
 		private Date orarioInizio1;
 		private Date orarioFine1;
 
 		private Integer quantita2;
 		private Date orarioInizio2;
 		private Date orarioFine2;
 
 		private Integer quantita3;
 		private Date orarioInizio3;
 		private Date orarioFine3;
 
 		public Integer getQuantita1() {
 			return quantita1;
 		}
 
 		public void setQuantita1(Integer quantita1) {
 			this.quantita1 = quantita1;
 		}
 
 		public Date getOrarioInizio1() {
 			return orarioInizio1;
 		}
 
 		public void setOrarioInizio1(Date orarioInizio1) {
 			this.orarioInizio1 = orarioInizio1;
 		}
 
 		public Date getOrarioFine1() {
 			return orarioFine1;
 		}
 
 		public void setOrarioFine1(Date orarioFine1) {
 			this.orarioFine1 = orarioFine1;
 		}
 
 		public Integer getQuantita2() {
 			return quantita2;
 		}
 
 		public void setQuantita2(Integer quantita2) {
 			this.quantita2 = quantita2;
 		}
 
 		public Date getOrarioInizio2() {
 			return orarioInizio2;
 		}
 
 		public void setOrarioInizio2(Date orarioInizio2) {
 			this.orarioInizio2 = orarioInizio2;
 		}
 
 		public Date getOrarioFine2() {
 			return orarioFine2;
 		}
 
 		public void setOrarioFine2(Date orarioFine2) {
 			this.orarioFine2 = orarioFine2;
 		}
 
 		public Integer getQuantita3() {
 			return quantita3;
 		}
 
 		public void setQuantita3(Integer quantita3) {
 			this.quantita3 = quantita3;
 		}
 
 		public Date getOrarioInizio3() {
 			return orarioInizio3;
 		}
 
 		public void setOrarioInizio3(Date orarioInizio3) {
 			this.orarioInizio3 = orarioInizio3;
 		}
 
 		public Date getOrarioFine3() {
 			return orarioFine3;
 		}
 
 		public void setOrarioFine3(Date orarioFine3) {
 			this.orarioFine3 = orarioFine3;
 		}
 	}
 }

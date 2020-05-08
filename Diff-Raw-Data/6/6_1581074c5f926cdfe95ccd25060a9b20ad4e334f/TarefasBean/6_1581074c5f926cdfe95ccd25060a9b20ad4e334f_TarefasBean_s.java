 package Bean;
 
 import java.io.Serializable;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 import Controller.Controller;
 import Exceptions.AnoInvalidoException;
 import Exceptions.DiaInvalidoException;
 import Exceptions.HoraInvalidaException;
 import Exceptions.MesInvalidoException;
 import Exceptions.MinutoInvalidoException;
 import Model.ComparadorDataConclusao;
 import Model.ComparadorDataCriacao;
 import Model.Data;
 import Model.Hora;
 import Model.Tarefa;
 
 @ManagedBean(name = "cadastraTarefa")
 @SessionScoped
 public class TarefasBean implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7803563315491022010L;
 	private String nome;
 	private String descricao;
 	private String dataConclusao;
 	private Controller controller;
 	private String horaConclusao;
 	private Comparator<Tarefa> comparador;
 	private String ordenacao;
 	private boolean status;
 	private Tarefa tarefa;
 	
 	public TarefasBean() throws NumberFormatException, HoraInvalidaException, MinutoInvalidoException, DiaInvalidoException, MesInvalidoException, AnoInvalidoException {
 		this.controller = new Controller();
 	}
 
 	public String voltar() {
 		return "index.seam";
 	}
 	
 	public String addTarefa() {
 		return "addTarefa.seam";
 	}
 	
 	public void cadastraTarefa() throws NumberFormatException,
 			DiaInvalidoException, MesInvalidoException, AnoInvalidoException,
 			HoraInvalidaException, MinutoInvalidoException {
 
 		if (!validaNome()) {
 			msgUsuario("Nome requerido", "O campo nome  obrigatrio");
 			return;
 		}
 		// Criando nova tarefa q ser armazenada;
 		Tarefa tarefa = new Tarefa(this.getNome());
 		tarefa.setDescricao(this.getDescricao());
 
 		if (!this.getDataConclusao().isEmpty()) {
 			try {
 				Data data = getDataConclusao(this.getDataConclusao());
 				tarefa.setDataConclusao(data);
 			} catch (Exception e) {
 				msgUsuario("Data invlida", "Informe uma data vlida");
 				return;
 			}
 		}
 
 		if (!this.getHoraConclusao().isEmpty()) {
 			try {
 				Hora hora = getHoraConclusao(this.getHoraConclusao());
 				tarefa.setHoraConclusao(hora);
 			} catch (Exception e) {
 				msgUsuario("Hora invlida", "Informe uma hora vlida");
 				return;
 			}
 		}
 		if (!this.controller.getTarefas().contains(tarefa)) {
 			this.controller.adicionaTarefa(tarefa);
 		} else {
 			msgUsuario("Tarefa Invlida",
 					"Informe um outro nome para a tarefa.");
 			return;
 		}
 		limpaCampos();
 	}
 
 	public void mudaStatus() {
 		if (this.tarefa == null) {
 			msgUsuario("Seleo invlida", "Selecione uma tarefa.");
 			return;
 		} else if (this.tarefa != null && this.getTarefa().getStatus() == false) {
 			this.tarefa.setStatus(true);
 			this.controller.addTarefaCompleta(this.getTarefa());
 			this.controller.removeTarefaIncompleta(this.getTarefa());
 		} else if (this.getTarefa().getStatus() == true) {
 			msgUsuario("Seleo invlida", "Tarefa j foi concluda.");
 			return;
 		} else {
 			msgUsuario("Seleo invlida", "Selecione uma opo vlida.");
 			return;
 		}
 	}
 	
 	public void removeTarefa() {
 		if (this.tarefa == null) {
 			msgUsuario("Seleo invlida", "Selecione uma opo vlida.");
 			return;
 		} else {
 			this.getController().removeTarefa(tarefa);
 		}
 	}
 
 	public String editarTarefa() {
 		if (this.getTarefa() == null) {
 			msgUsuario("Seleo invlida", "Selecione uma tarefa.");
 			return "";
 		}
 		return "editTarefa.seam";
 	}
 
 	public void salvarTarefa() {
 
 		if(!validaNome()){
 			msgUsuario("Nome Invlido", "Digite um nome vlido para edio");
 			return;
 		}
 		try {
 			Tarefa newTarefa = new Tarefa(this.getNome());
 			newTarefa.setDataCriacao(this.getTarefa().getDataCriacao());
 			newTarefa.setStatus(this.getTarefa().getStatus());
 			if (!this.getDataConclusao().isEmpty()) {
 				try {
 					Data data = new Data(Integer.parseInt(this
 							.getDataConclusao().substring(0, 2)),
 							Integer.parseInt(this.getDataConclusao().substring(
 									3, 5)), Integer.parseInt(this
 									.getDataConclusao().substring(6, 10)));
 					newTarefa.setDataConclusao(data);
 				} catch (Exception e) {
 					msgUsuario("Data invlida", "Informe uma data vlida");
 					return;
 				}
 			} else {
 				Data data = new Data();
 				newTarefa.setDataConclusao(data);
 			}
 			if (!this.getHoraConclusao().isEmpty()) {
 				try {
 					Hora hora = new Hora(Integer.parseInt(this
 							.getHoraConclusao().substring(0, 2)),
 							Integer.parseInt(this.getHoraConclusao().substring(
 									3, 5)));
					tarefa.setHoraConclusao(hora);
 				} catch (Exception e) {
 					msgUsuario("Hora invlida", "Informe uma hora vlida");
 					return;
 
 				}
 			} else {
 				Hora hora = new Hora();
 				newTarefa.setHoraConclusao(hora);
 			}
 			newTarefa.setDescricao(this.getDescricao());
 			this.getController().editTarefa(this.getTarefa(), newTarefa);
 		} catch (Exception e) {
 			msgUsuario("Nome Invlido", "Digite um nome vlido.");
 			return;
 		}
 		limpaCampos();	
 	}
 
 	public void ordena() {
 		if (this.getOrdenacao().equals("")) {
 			msgUsuario("Seleo invlida",
 					"Selecione uma opo vlida pra ordenao.");
 			return;
 		} else if (this.getOrdenacao().equals("dataCriacao")) {
 			this.setComparador(new ComparadorDataCriacao());
 		} else {
 			this.setComparador(new ComparadorDataConclusao());
 		}
 		this.controller.ordena(this.getComparador());
 	}
 
 	public void ordenaCompletas() {
 		if (this.getOrdenacao().equals("")) {
 			msgUsuario("Seleo invlida",
 					"Selecione uma opo vlida pra ordenao.");
 			return;
 		} else if (this.getOrdenacao().equals("dataCriacao")) {
 			this.setComparador(new ComparadorDataCriacao());
 		} else {
 			this.setComparador(new ComparadorDataConclusao());
 		}
 		this.controller.ordenaCompletas(this.getComparador());
 	}
 
 	public void ordenaIncompletas() {
 		if (this.getOrdenacao().equals("")) {
 			msgUsuario("Seleo invlida",
 					"Selecione uma opo vlida pra ordenao.");
 			return;
 		} else if (this.getOrdenacao().equals("dataCriacao")) {
 			this.setComparador(new ComparadorDataCriacao());
 		} else {
 			this.setComparador(new ComparadorDataConclusao());
 		}
 		this.controller.ordenaIncompletas(this.getComparador());
 	}
 
 	private Data getDataConclusao(String dataConclusao)
 			throws NumberFormatException, DiaInvalidoException,
 			MesInvalidoException, AnoInvalidoException {
 		return new Data(Integer.parseInt(dataConclusao.substring(0, 2)),
 				Integer.parseInt(dataConclusao.substring(3, 5)),
 				Integer.parseInt(dataConclusao.substring(6, 10)));
 	}
 
 	private Hora getHoraConclusao(String horaConclusao)
 			throws NumberFormatException, HoraInvalidaException,
 			MinutoInvalidoException {
 		return new Hora(Integer.parseInt(horaConclusao.substring(0, 2)),
 				Integer.parseInt(horaConclusao.substring(3, 5)));
 	}
 
 	private void limpaCampos() {
 		this.setNome("");
 		this.setDescricao("");
 		this.setDataConclusao("");
 		this.setHoraConclusao("");
 	}
 
 	private boolean validaNome() {
 		return !this.getNome().isEmpty();
 	}
 
 	private void msgUsuario(String string1, String string2) {
 		FacesContext context = FacesContext.getCurrentInstance();
 		context.addMessage(null, new FacesMessage(string1, string2));
 	}
 
 	public boolean getStatus() {
 		return status;
 	}
 
 	public Tarefa getTarefa() {
 		return tarefa;
 	}
 
 	public void setTarefa(Tarefa tarefa) {
 		this.tarefa = tarefa;
 	}
 
 	public String getOrdenacao() {
 		return ordenacao;
 	}
 
 	public void setOrdenacao(String ordenacao) {
 		this.ordenacao = ordenacao;
 	}
 
 	public String getHoraConclusao() {
 		return horaConclusao;
 	}
 
 	public void setHoraConclusao(String horaConclusao) {
 		this.horaConclusao = horaConclusao;
 	}
 
 	public Comparator<Tarefa> getComparador() {
 		return comparador;
 	}
 
 	public void setComparador(Comparator<Tarefa> comparador) {
 		this.comparador = comparador;
 	}
 
 	public Controller getController() {
 		return controller;
 	}
 
 	public void setController(Controller controller) {
 		this.controller = controller;
 	}
 
 	public String getDataConclusao() {
 		return dataConclusao;
 	}
 
 	public void setDataConclusao(String dataConclusao) {
 		this.dataConclusao = dataConclusao;
 	}
 
 	public String getNome() {
 		return nome;
 	}
 
 	public void setNome(String nome) {
 		this.nome = nome;
 	}
 
 	public String getDescricao() {
 		return descricao;
 	}
 
 	public void setDescricao(String descricao) {
 		this.descricao = descricao;
 	}
 
 	public List<Tarefa> getTarefas() {
 		return controller.getTarefas();
 	}
 
 }

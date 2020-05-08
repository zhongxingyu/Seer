 package org.uqbar.edu.paiu.examples.celulares.ui.arena;
 
 import org.uqbar.arena.actions.MessageSend;
 import org.uqbar.arena.aop.windows.TransactionalDialog;
 import org.uqbar.arena.layout.ColumnLayout;
 import org.uqbar.arena.widgets.Button;
 import org.uqbar.arena.widgets.CheckBox;
 import org.uqbar.arena.widgets.Label;
 import org.uqbar.arena.widgets.Panel;
 import org.uqbar.arena.widgets.Selector;
 import org.uqbar.arena.widgets.TextBox;
 import org.uqbar.arena.windows.WindowOwner;
 import org.uqbar.commons.model.Home;
 import org.uqbar.edu.paiu.examples.celulares.dao.RepositorioCelulares;
 import org.uqbar.edu.paiu.examples.celulares.dao.RepositorioModelos;
 import org.uqbar.edu.paiu.examples.celulares.domain.Celular;
 
 public class EditarCelularWindow extends TransactionalDialog<Celular> {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private final Home<Celular> home;
 
 	public EditarCelularWindow(WindowOwner owner, Celular model) {
 		super(owner, model);
 		this.home = RepositorioCelulares.getInstance();
 	}
 
 	@Override
 	protected void createFormPanel(Panel mainPanel) {
 		Panel form = new Panel(mainPanel);
 		form.setLayout(new ColumnLayout(2));
 
 		new Label(form).setText("Número");
 		new TextBox(form).bindValueToProperty(Celular.NUMERO);
 
 		new Label(form).setText("Nombre del cliente");
 		new TextBox(form).bindValueToProperty(Celular.NOMBRE);
 
 		new Label(form).setText("Modelo del aparato");
 		new Selector(form)
 				.allowNull(false)
 				.setContents(RepositorioModelos.getInstance().allInstances(),
 						"descripcionEntera")
 				.bindValueToProperty(Celular.MODELO_CELULAR);
 
 		new Label(form).setText("Recibe resumen cuenta en domicilio");
 		new CheckBox(form).bindValueToProperty(Celular.RECIBE_CUENTA_DOMICILIO);
 
 	}
 
 	@Override
 	protected void addActions(Panel actions) {
 		new Button(actions).setCaption("Aceptar")
 				.onClick(new MessageSend(this, ACCEPT)).setAsDefault()
 				.disableOnError();
 
 		new Button(actions) //
 				.setCaption("Cancelar").onClick(new MessageSend(this, CANCEL));
 	}
 
 	/**
 	 * Errores de validación son capturados por Window
 	 */
 	@Override
 	protected void executeTask() {
		super.executeTask();
 		Celular celular = this.getModelObject();
//		celular.validar();
 		if (celular.isNew()) {
 			this.home.create(celular);
 		} else {
 			this.home.update(celular);
 		}
 	}
 }

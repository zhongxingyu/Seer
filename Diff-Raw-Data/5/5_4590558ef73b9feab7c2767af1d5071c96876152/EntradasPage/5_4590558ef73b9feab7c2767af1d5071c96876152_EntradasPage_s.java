 package com.odea;
 
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.subject.Subject;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.ajax.AjaxEventBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.markup.html.form.Radio;
 import org.apache.wicket.markup.html.form.RadioChoice;
 import org.apache.wicket.markup.html.form.RadioGroup;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.StringValidator;
 import org.joda.time.LocalDate;
 
 import com.odea.behavior.numberComma.NumberCommaBehavior;
 import com.odea.behavior.onlyNumber.OnlyNumberBehavior;
 import com.odea.components.datepicker.DatePickerDTO;
 import com.odea.components.datepicker.HorasCargadasPorDia;
 import com.odea.components.slickGrid.Columna;
 import com.odea.components.slickGrid.Data;
 import com.odea.components.slickGrid.SlickGrid;
 import com.odea.components.yuidatepicker.YuiDatePicker;
 import com.odea.domain.Actividad;
 import com.odea.domain.Entrada;
 import com.odea.domain.Feriado;
 import com.odea.domain.Proyecto;
 import com.odea.domain.Usuario;
 import com.odea.services.DAOService;
 import com.odea.validators.duracion.DurationValidator;
 import com.odea.validators.ticketExterno.OnRelatedFieldsNullValidator;
 
 public class EntradasPage extends BasePage {
 
 	private static final long serialVersionUID = 1088210443697851501L;
 
 	@SpringBean
 	private transient DAOService daoService;
 
 	private Usuario usuario;
 	private EntradaForm form;
 	private RadioChoice<String> selectorTiempo;
 	private LocalDate fechaActual = new LocalDate();
 	private String radioSeleccionado = "dia";
 	private IModel<String> lstDataModel;
 	private IModel<Integer> horasSemanalesModel;
 	private IModel<Integer> horasMesModel;
 	private IModel<Integer> horasDiaModel;
 	private IModel<String> slickGridJsonCols;
 	private Label mensajeProyecto;
 	private Label mensajeActividad;
 	private Label horasAcumuladasDia;
 	private Label horasAcumuladasSemana;
 	private Label horasAcumuladasMes;
 	private WebMarkupContainer listViewContainer;
 	private WebMarkupContainer radioContainer;
 	private WebMarkupContainer labelContainer;
 	private WebMarkupContainer selectorUsuarioContainer;
 	
 
 	public EntradasPage() {
 
 		final Subject subject = SecurityUtils.getSubject();
 		this.slickGridJsonCols = Model.of(this.getColumns());
 
 		this.usuario = this.daoService.getUsuario(subject.getPrincipal()
 				.toString());
 
 		this.lstDataModel = new LoadableDetachableModel<String>() {
 			@Override
 			protected String load() {
 				if (radioSeleccionado == "dia"){
 				return daoService.toJson(daoService.getEntradasDia(
 						EntradasPage.this.usuario,
 						EntradasPage.this.fechaActual));
 				}
 				if (radioSeleccionado == "mes"){
 					return daoService.toJson(daoService.getEntradasMensuales(
 							EntradasPage.this.usuario,
 							EntradasPage.this.fechaActual));
 				}
 				if (radioSeleccionado == "semana"){
 					return daoService.toJson(daoService.getEntradasSemanales(
 							EntradasPage.this.usuario,
 							EntradasPage.this.fechaActual));
 				}
 				else {
 					throw new RuntimeException("radio seleccionado erroneo o loqueseaquepaso");
 				}
 			}
 		};
 
 		this.horasSemanalesModel = new LoadableDetachableModel<Integer>() {
 			@Override
 			protected Integer load() {
 				return daoService.getHorasSemanales(usuario, fechaActual);
 			}
 
 		};
 		this.horasMesModel = new LoadableDetachableModel<Integer>() {
 			@Override
 			protected Integer load() {
 				return daoService.getHorasMensuales(usuario, fechaActual);
 			}
 
 		};
 
 		this.horasDiaModel = new LoadableDetachableModel<Integer>() {
 			@Override
 			protected Integer load() {
 				return daoService.getHorasDiarias(usuario, fechaActual);
 			}
 
 		};
 
 		this.listViewContainer = new WebMarkupContainer("listViewContainer");
 		this.listViewContainer.setOutputMarkupId(true);
 
 		this.selectorUsuarioContainer = new WebMarkupContainer("selectorUsuarioContainer");
 		this.selectorUsuarioContainer.setOutputMarkupId(true);
 		
 		
 		this.form = new EntradaForm("form") {
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, EntradaForm form) {
 				daoService.agregarEntrada(form.getModelObject(), usuario);
 				EntradasPage.this.lstDataModel.detach();
 				
 				target.add(listViewContainer);
 				target.add(labelContainer);
 			}
 		};
 		form.setMarkupId("formEntradas");
 		
 		
 		
 		final SlickGrid slickGrid = new SlickGrid("slickGrid", this.lstDataModel, this.slickGridJsonCols) {
 
 			@Override
 			protected void onInfoSend(AjaxRequestTarget target, String realizar, Data data) {
 				try {
 					
 					if (realizar == "borrar") {
 						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 						Date parsedDate = dateFormat.parse(data.getId());
 						Timestamp timestamp = new Timestamp(parsedDate.getTime());
 						daoService.borrarEntrada(timestamp);
 					} else {
 
 						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 						Date parsedDate = dateFormat.parse(data.getId());
 						Timestamp timestamp = new Timestamp(parsedDate.getTime());
 						dateFormat = new SimpleDateFormat("dd/MM/yyyy");
 						Date fecha = dateFormat.parse(data.getFecha());
 						
 						Actividad actividad = daoService.getActividad(data.getActividad());
 						Proyecto proyecto = daoService.getProyecto(data.getProyecto());
 						
 						Integer ticket;
 						
 						if (data.getTicket().isEmpty()) {
 							ticket = 0;
 						} else {
 							ticket = Integer.parseInt(data.getTicket());
 						}
 
 						
 						String sistemaExterno = null;
 						
 						if (data.getSistExt()!= null) {
 							if (data.getSistExt().equals("Sistema de Incidencias de YPF")) {
 								sistemaExterno = "SIY";
 							} else if (data.getSistExt().equals(" Sistema Geminis de YPF")) {
 								sistemaExterno = "SGY";
 							} else if (data.getSistExt().equals(" Ninguno")) {
 								sistemaExterno = null;
 							} else {
 							sistemaExterno = data.getSistExt();
 							}
 						}
 						
 						Entrada entrada = new Entrada(timestamp, proyecto, actividad, data.getDuration(),
 								data.getDescripcion(), ticket, data.getTicketExt(), sistemaExterno,
 								EntradasPage.this.usuario, fecha);
 						
 						daoService.modificarEntrada(entrada);
 					}
 					EntradasPage.this.lstDataModel.detach();
 					
 					
 					target.add(listViewContainer);
 					target.add(labelContainer);
 					
 					EntradasPage.this.form.setModelObject(new Entrada());
 					target.add(form);
 					
 					// poner target.add separados, o no ponerlos directamente
 					
 				} catch (Exception e) {
 					throw new RuntimeException(e);
 				}
 
 			}
 			
 		};
 
 		slickGrid.setOutputMarkupId(true);
 
 		
 		
 		this.horasAcumuladasDia = new Label("horasAcumuladasDia", this.horasDiaModel);
 		this.horasAcumuladasDia.setOutputMarkupId(true);
 
 		this.horasAcumuladasSemana = new Label("horasAcumuladasSemana", this.horasSemanalesModel);
 		this.horasAcumuladasSemana.setOutputMarkupId(true);
 
 		this.horasAcumuladasMes = new Label("horasAcumuladasMes", this.horasMesModel);
 		this.horasAcumuladasMes.setOutputMarkupId(true);
 
 		
 		
 		
 		final RadioGroup<String> radiog = new RadioGroup<String>("selectorTiempo", Model.of(new String()));
 
 		Radio<String> dia = new Radio<String>("dia", Model.of("Dia"));
 		Radio<String> semana = new Radio<String>("semana", Model.of("Semana"));
 		Radio<String> mes = new Radio<String>("mes", Model.of("Mes"));
 
 		this.radioContainer = new WebMarkupContainer("radioContainer");
 		this.radioContainer.setOutputMarkupId(true);
 		this.radioContainer.add(radiog);
 		this.radioContainer.setMarkupId("radioSelector");
 
 		radiog.add(dia.add(new AjaxEventBehavior("onchange") {
 			
 			protected void onEvent(AjaxRequestTarget target) {
 				EntradasPage.this.radioSeleccionado = "dia";
 				EntradasPage.this.lstDataModel.detach();
 				target.add(listViewContainer);
 				target.add(labelContainer);
 			}
 			
 		}));
 
 		radiog.add(semana.add(new AjaxEventBehavior("onchange") {
 			
 			protected void onEvent(AjaxRequestTarget target) {
 				EntradasPage.this.radioSeleccionado = "semana";
 //				List<Data> entradas = daoService.getEntradasSemanales(EntradasPage.this.usuario, EntradasPage.this.fechaActual);
 //				lstDataModel.setObject(daoService.toJson(entradas));
 				EntradasPage.this.lstDataModel.detach();
 				target.add(listViewContainer);
 				target.add(labelContainer);
 			}
 			
 		}));
 		
 		
 		radiog.add(mes.add(new AjaxEventBehavior("onchange") {
 			
 			protected void onEvent(AjaxRequestTarget target) {
 				EntradasPage.this.radioSeleccionado = "mes";
 //				List<Data> entradas = daoService.getEntradasSemanales(EntradasPage.this.usuario, EntradasPage.this.fechaActual);
 //				lstDataModel.setObject(daoService.toJson(entradas));
 				EntradasPage.this.lstDataModel.detach();
 				target.add(listViewContainer);
 				target.add(labelContainer);
 
 			}
 		}));
 
 		this.listViewContainer.add(slickGrid);
 		this.listViewContainer.setMarkupId("containerSlickGrid");
 
 
 		this.labelContainer = new WebMarkupContainer("labelContainer");
 		this.labelContainer.add(horasAcumuladasDia);
 		this.labelContainer.add(horasAcumuladasSemana);
 		this.labelContainer.add(horasAcumuladasMes);
 		this.labelContainer.setOutputMarkupId(true);
 		this.labelContainer.setMarkupId("labelHoras");
 		
 		
 		final DropDownChoice<Usuario> selectorUsuario = new DropDownChoice<Usuario>("selectorUsuario",daoService.getUsuarios(),new IChoiceRenderer<Usuario>() {
 			@Override
 			public Object getDisplayValue(Usuario object) {
 				return object.getNombre();
 			}
 
 			@Override
 			public String getIdValue(Usuario object, int index) {
 				return Integer.toString(object.getIdUsuario());
 			}
 			
 		});
 
 		selectorUsuario.setModel(new Model<Usuario>(this.usuario));
 		selectorUsuario.setOutputMarkupId(true);
 		
 		selectorUsuario.add(new AjaxFormComponentUpdatingBehavior("onchange") {
 			@Override
 			protected void onUpdate(AjaxRequestTarget target) {
 				
 				EntradasPage.this.usuario = selectorUsuario.getModelObject();
 				
 				List<Data> entradas = daoService.getEntradasDia(EntradasPage.this.usuario, fechaActual);
 				lstDataModel.setObject(daoService.toJson(entradas));
 				
 				target.add(EntradasPage.this.form);
 				target.add(EntradasPage.this.listViewContainer);
 				target.add(EntradasPage.this.labelContainer);
 				target.add(EntradasPage.this.radioContainer);
 			}
 			
 		});
 		
 		this.selectorUsuarioContainer.add(selectorUsuario);
 		this.selectorUsuarioContainer.setMarkupId("selectorUsuario");
 		
 		
 		
 		
 		add(selectorUsuarioContainer);
 		add(radioContainer);
 		add(listViewContainer);		
 		add(labelContainer);
 		add(form);
 
 	}
 
 
 	
 	public abstract class EntradaForm extends Form<Entrada> {
 		
 		public IModel<Entrada> entradaModel = new CompoundPropertyModel<Entrada>(new Entrada());
 		public DropDownChoice<Actividad> comboActividad;
 		public DropDownChoice<Proyecto> comboProyecto;
 		public TextField<String> ticketExt;
 		public DropDownChoice<String> sistemaExterno;
 		public TextField<String> duracion;
 		public TextField<Integer> ticketBZ;
 		public YuiDatePicker fecha;
 		public TextArea<String> nota;
 		
 
 		public EntradaForm(String id) {
 			super(id);
 			this.setDefaultModel(this.entradaModel);
 			this.setOutputMarkupId(true);
 
 			//this.delegateSubmit(this.findSubmittingButton());
 			
 			this.comboProyecto = new DropDownChoice<Proyecto>("proyecto",
 					daoService.getProyectosHabilitados(), new IChoiceRenderer<Proyecto>() {
 						@Override
 						public Object getDisplayValue(Proyecto object) {
 							return object.getNombre();
 						}
 
 						@Override
 						public String getIdValue(Proyecto object, int index) {
 							return Integer.toString(object.getIdProyecto());
 						}
 
 					});
 
 			this.comboProyecto.setOutputMarkupId(true);
 			this.comboProyecto.setRequired(true);
 			this.comboProyecto.setLabel(Model.of("Proyecto"));
 
 			this.comboProyecto.add(new AjaxFormComponentUpdatingBehavior(
 					"onchange") {
 				@Override
 				protected void onUpdate(AjaxRequestTarget target) {
 					EntradaForm.this.comboActividad.setChoices(daoService
 							.getActividadesHabilitadas(EntradaForm.this.comboProyecto
 									.getModelObject()));
 					target.add(EntradaForm.this.comboActividad);
 				}
 			});
 			
			EntradasPage.this.mensajeProyecto = new Label("mensajeProyecto", "Campo obligatorio");
 			EntradasPage.this.mensajeProyecto.add(new AttributeModifier("style", Model.of("display:none")));
 			EntradasPage.this.mensajeProyecto.setOutputMarkupId(true);
 
 			
 			
 			
 			this.comboActividad = new DropDownChoice<Actividad>("actividad",
 					new ArrayList<Actividad>(),
 					new IChoiceRenderer<Actividad>() {
 						@Override
 						public Object getDisplayValue(Actividad object) {
 							return object.getNombre();
 						}
 
 						@Override
 						public String getIdValue(Actividad object, int index) {
 							return Integer.toString(object.getIdActividad());
 						}
 
 					});
 
 			
 			this.comboActividad.setOutputMarkupId(true);
 			this.comboActividad.setRequired(true);
 			this.comboActividad.setLabel(Model.of("Actividad"));
 
			EntradasPage.this.mensajeActividad = new Label("mensajeActividad", "Campo obligatorio");
 			EntradasPage.this.mensajeActividad.add(new AttributeModifier("style", Model.of("display:none")));
 			EntradasPage.this.mensajeActividad.setOutputMarkupId(true);
 			
 			
 			
 			List<String> sistemasExternos = daoService.getSistemasExternos();
 			sistemasExternos.remove("Ninguno");
 			
 			this.sistemaExterno = new DropDownChoice<String>("sistemaExterno", sistemasExternos);
 			this.sistemaExterno.setLabel(Model.of("Sistema Externo"));
 			this.sistemaExterno.setOutputMarkupId(true);
 
 			
 			this.nota = new TextArea<String>("nota");
 			this.nota.add(new StringValidator(0, 5000));
 			this.nota.setLabel(Model.of("Nota"));
 			this.nota.setOutputMarkupId(true);
 
 			
 			this.duracion = new TextField<String>("duracion");
 			this.duracion.setRequired(true);
 			this.duracion.setOutputMarkupId(true);
 			this.duracion.setLabel(Model.of("Duracion"));
 			this.duracion.add(new NumberCommaBehavior(duracion.getMarkupId()));
 			this.duracion.add(new DurationValidator());
 
 			
 			this.ticketBZ = new TextField<Integer>("ticketBZ");
 			this.ticketBZ.setLabel(Model.of("Ticket Bugzilla"));
 			this.ticketBZ.add(new OnlyNumberBehavior(ticketBZ.getMarkupId()));
 			this.ticketBZ.setOutputMarkupId(true);
 
 			
 			this.ticketExt = new TextField<String>("ticketExterno");
 			this.ticketExt.setLabel(Model.of("ID Ticket Externo"));
 			this.ticketExt.add(new StringValidator(0, 15));
 			this.ticketExt.setOutputMarkupId(true);
 
 			final YuiDatePicker fecha = new YuiDatePicker("fecha") {
 
 				@Override
 				protected void onDateSelect(AjaxRequestTarget target, String selectedDate) {
 					String json = selectedDate;
 					List<String> campos = Arrays.asList(json.split("/"));
 					int dia = Integer.parseInt(campos.get(0));
 					int mes = Integer.parseInt(campos.get(1));
 					int anio = Integer.parseInt(campos.get(2));
 
 					EntradasPage.this.fechaActual = new LocalDate(anio, mes, dia);
 					radioSeleccionado = "dia";
 					lstDataModel.detach();
 					target.add(listViewContainer);
 					target.add(labelContainer);
 					target.add(radioContainer);
 				}
 
 				@Override
 				public DatePickerDTO getDatePickerData() {
 					DatePickerDTO dto = new DatePickerDTO();
 					dto.setDedicacion(daoService.getDedicacion(usuario));
 					dto.setUsuario(usuario.getNombre());
 					
 					Collection<HorasCargadasPorDia> c = daoService.getHorasDiaras(usuario);
 					dto.setHorasDia(c);
 					
 					List<Feriado> feriados = daoService.getFeriados();
 					dto.setFeriados(feriados);
 					
 					return dto;
 				}
 			};
 
 			fecha.setRequired(true);
 			fecha.setLabel(Model.of("Fecha"));
 			fecha.setMarkupId("fechaDatepicker");
 
 			final FeedbackPanel feedBackPanel = new FeedbackPanel("feedBackPanel");
 			feedBackPanel.setOutputMarkupId(true);
 			feedBackPanel.setMarkupId("feedBackPanel");
 
 			
 			
 			AjaxButton submit = new AjaxButton("submit", this) {
 
 				private static final long serialVersionUID = 1L;
 				
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 					
 					EntradaForm.this.onSubmit(target, (EntradaForm) form);
 					EntradaForm.this.setModelObject(new Entrada());
 					
 					EntradasPage.this.lstDataModel.detach();
 					target.add(feedBackPanel);
 					target.add(EntradasPage.this.listViewContainer);
 					target.add(EntradasPage.this.labelContainer);
 					
 
 					if (duracion.isValid()) {
 						duracion.add(new AttributeModifier("style", Model.of("border-color:none")));
 					} else {
 						duracion.add(new AttributeModifier("style", Model.of("border-style:solid; border-color:red;")));
 					}
 
 					if (ticketExt.isValid()) {
 						ticketExt.add(new AttributeModifier("style", Model.of("border-color:none")));
 					} else {
 						ticketExt.add(new AttributeModifier("style", Model.of("border-style:solid; border-color:red;")));
 					}
 
 					if (fecha.isValid()) {
 						fecha.add(new AttributeModifier("style", Model.of("border-color:none")));
 					} else {
 						fecha.add(new AttributeModifier("style", Model.of("border-style:solid; border-color:red;")));
 					}
 
 					if (comboProyecto.isValid()) {
 						mensajeProyecto.add(new AttributeModifier("style", Model.of("display:none")));
 					}
 
 					if (comboActividad.isValid()) {
 						mensajeActividad.add(new AttributeModifier("style", Model.of("display:none")));
 					}
 
 					
 					target.add(comboProyecto);
 					target.add(comboActividad);
 					target.add(comboProyecto);
 					target.add(sistemaExterno);
 					target.add(duracion);
 					target.add(ticketBZ);
 					target.add(nota);
 					target.add(ticketExt);
 					target.add(feedBackPanel);
 					target.add(mensajeProyecto);
 					target.add(mensajeActividad);
 					target.add(fecha);
 				}
 
 				@Override
 				protected void onError(AjaxRequestTarget target, Form<?> form) {
 
 					if (!duracion.isValid()) {
 						duracion.add(new AttributeModifier("style", Model.of("border-style:solid; border-color:red;")));
 					} else {
 						duracion.add(new AttributeModifier("style", Model.of("border-color:none")));
 					}
 
 					if (!ticketExt.isValid()) {
 						ticketExt.add(new AttributeModifier("style", Model.of("border-style:solid; border-color:red;")));
 					} else {
 						ticketExt.add(new AttributeModifier("style", Model.of("border-color:none")));
 					}
 
 					if (!fecha.isValid()) {
 						fecha.add(new AttributeModifier("style", Model.of("border-style:solid; border-color:white;")));
 					} else {
 						fecha.add(new AttributeModifier("style", Model.of("border-color:none")));
 					}
 
 					if (comboProyecto.isValid()) {
 						mensajeProyecto.add(new AttributeModifier("style", Model.of("display:none")));
 					} else {
 						mensajeProyecto.add(new AttributeModifier("style", Model.of("font-weight:bold;color:red")));
 					}
 
 					if (comboActividad.isValid()) {
 						mensajeActividad.add(new AttributeModifier("style",	Model.of("display:none")));
 					} else {
 						mensajeActividad.add(new AttributeModifier("style",	Model.of("font-weight:bold;color:red")));
 					}
 
 					target.add(feedBackPanel);
 					target.add(duracion);
 					target.add(ticketExt);
 					target.add(mensajeProyecto);
 					target.add(mensajeActividad);
 				}
 
 			};
 
 			AjaxButton limpiar = new AjaxButton("limpiar", this) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 					EntradaForm.this.setModelObject(new Entrada());
 					
 					mensajeProyecto.add(new AttributeModifier("style", Model.of("display:none")));
 					mensajeActividad.add(new AttributeModifier("style", Model.of("display:none")));
 					ticketExt.add(new AttributeModifier("style", Model.of("border-color:none")));
 					duracion.add(new AttributeModifier("style", Model.of("border-color:none")));
 					
 					target.add(form);
 					target.add(feedBackPanel);
 				}
 
 			};
 
 			limpiar.setDefaultFormProcessing(false);
 
 			
 			
 			add(mensajeProyecto);
 			add(mensajeActividad);
 			add(comboProyecto);
 			add(comboActividad);
 			add(duracion);
 			add(fecha);
 			add(nota);
 			add(ticketBZ);
 			add(sistemaExterno);
 			add(ticketExt);
 			add(feedBackPanel);
 			add(submit);
 			add(limpiar);
 			add(new OnRelatedFieldsNullValidator(sistemaExterno, ticketExt, "Debe seleccionar un Sistema Externo si quiere elegir un ID Ticket Externo"));
 			add(new OnRelatedFieldsNullValidator(ticketExt, sistemaExterno, "Debe ingresar un ID Ticket Externo con el Sistema Externo elegido"));
 		}
 
 		
 		protected abstract void onSubmit(AjaxRequestTarget target, EntradaForm form);
 
 	}
 	
 	
 	
 
 	private String getColumns() {
 		
 		List<Proyecto> list = this.daoService.getProyectosHabilitados();
 		String actividades = "";
 		for (Proyecto proyecto : list) {
 			actividades += proyecto.toString();
 			actividades += this.daoService.getActividadesHabilitadas(proyecto).toString();
 		}
 
 		
 		String listaProyectos = daoService.getProyectosHabilitados().toString();
 		String proyectos = listaProyectos.subSequence(1, listaProyectos.length() - 1).toString();
 		
 		String listaSistemaExterno = daoService.getSistemasExternos().toString();
 		String sistemasExternos = listaSistemaExterno.subSequence(1, listaSistemaExterno.length() - 1).toString();
 		
 		Columna columna = new Columna("delCol", "Delete", 60, 60, 60, null, "del", "Slick.Formatters.DeleteButton", null, null, null);
 		Columna columna2 = new Columna("duration", "Duracion", 60, 60, 60, "cell-title", "duration", null, "Slick.Editors.Text", "requiredDurationValidator", null);
 		Columna columna3 = new Columna("actividad", "Actividad", 125, 100, 200, "cell-title", "actividad", null, "Slick.Editors.SelectRelatedEditor", "requiredFieldValidator",	actividades);
 		Columna columna4 = new Columna("proyecto", "Proyecto", 135, 100, 200, "cell-title", "proyecto", null, "Slick.Editors.SelectEditor",	"requiredFieldValidator", proyectos);
 		Columna columna5 = new Columna("fecha", "Fecha", 60, 60, 60, null, "fecha", null, "Slick.Editors.Date", "requiredFieldValidator", null);
 		Columna columna6 = new Columna("ticket", "Ticket", 50, 50, 50, "cell-title", "ticket", null, "Slick.Editors.Text", "ticketBugzillaValidator", null);
 		Columna columna7 = new Columna("ticketExt", "Ticket Externo", 80, 80, 100, "cell-title", "ticketExt", null, "Slick.Editors.TextTicketExt", "ticketExternoValidator", null);
 		Columna columna8 = new Columna("sistExt", "Sistema Externo", 80, 80, 80, "cell-title", "sistExt", null, "Slick.Editors.SelectEditor", null, sistemasExternos);
 		Columna columna9 = new Columna("descripcion", "Descripcion", 80, 80, 80, null, "descripcion", null, "Slick.Editors.LongText", "descripcionValidator", null);
 		
 		ArrayList<Columna> columnas = new ArrayList<Columna>();
 		columnas.add(columna);
 		columnas.add(columna5);
 		columnas.add(columna4);
 		columnas.add(columna3);
 		columnas.add(columna2);
 		columnas.add(columna9);
 		columnas.add(columna6);
 		columnas.add(columna8);
 		columnas.add(columna7);
 		
 		String texto = "[";
 		for (Columna col : columnas) {
 			texto += "{id:\"" + col.getId() + "\", name: \"" + col.getName()
 					+ "\", width: " + col.getWidth() + ", minWidth: "
 					+ col.getMinWidth() + ", maxWidth: " + col.getMaxWidth()
 					+ ", cssClass: \"" + col.getCssClass() + "\", field: \""
 					+ col.getField() + "\",formatter: " + col.getFormatter()
 					+ ", editor: " + col.getEditor() + ", validator: "
 					+ col.getValidator() + ", options: \"" + col.getOptions()
 					+ "\"},";
 		}
 		texto += "]";
 		return texto;
 	}
 
 }

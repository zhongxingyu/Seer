 package com.odea;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.text.DateFormatter;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.subject.Subject;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.ajax.AjaxEventBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.WebComponent;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.Radio;
 import org.apache.wicket.markup.html.form.RadioGroup;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.PageableListView;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.Response;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormatter;
 
 import com.odea.components.datepicker.DatePickerDTO;
 import com.odea.components.yuidatepicker.YuiDatePicker;
 import com.odea.domain.Feriado;
 import com.odea.domain.FormHoras;
 import com.odea.domain.Usuario;
 import com.odea.domain.UsuarioListaHoras;
 import com.odea.services.DAOService;
 
 public class VistaHorasPage extends BasePage{
 	
 	private static final long serialVersionUID = 1L;
 
 	@SpringBean
 	private transient DAOService daoService;
 	
 	public String sectorGlobal= "Todos";
 	public Usuario usuario;
 	public IModel<List<UsuarioListaHoras>> lstUsuariosModel;
 	public IModel<List<UsuarioListaHoras>> lstUsuariosEnRojoModel;
 	public WebComponent titulos;
 	public WebMarkupContainer listViewContainer;
 	public WebMarkupContainer radioContainer;
 	public IModel<FormHoras> horasUsuarioModel;
 	public LocalDate fechaActual = new LocalDate();
 	public Date desde = fechaActual.withDayOfMonth(1).toDateTimeAtStartOfDay().toDate();
 	public Date hasta = fechaActual.plusMonths(1).withDayOfMonth(1).minusDays(1).toDateTimeAtStartOfDay().toDate();
 	
 	public VistaHorasPage(){
 		
 		final Subject subject = SecurityUtils.getSubject();
 		this.usuario = this.daoService.getUsuario(subject.getPrincipal().toString());
 
 		VistaHorasForm form = new VistaHorasForm("form");
         form.setOutputMarkupId(true);
         add(form);
 				
 		this.lstUsuariosModel = new LoadableDetachableModel<List<UsuarioListaHoras>>() { 
 	        @Override
 	        protected List<UsuarioListaHoras> load() {
 	          	return daoService.obtenerHorasUsuarios(desde, hasta, VistaHorasPage.this.sectorGlobal);
 	        }
 	    };
 	    
 	    this.lstUsuariosEnRojoModel = new LoadableDetachableModel<List<UsuarioListaHoras>>() {
 
 			@Override
 			protected List<UsuarioListaHoras> load() {
 				List<UsuarioListaHoras> devolver = daoService.obtenerHorasUsuarios(desde, hasta,VistaHorasPage.this.sectorGlobal);
 				List<UsuarioListaHoras> itemsToRemove = new ArrayList<UsuarioListaHoras>();
 				for (UsuarioListaHoras usuarioHoras : devolver) {
					if(usuarioHoras.tieneDiaMenorDedicacion()){
 						itemsToRemove.add(usuarioHoras);
 					}
 				}
 				devolver.removeAll(itemsToRemove);
 				return devolver;
 			}
 	    	
 		};
         
 		this.listViewContainer = new WebMarkupContainer("listViewContainer");
 		this.listViewContainer.setOutputMarkupId(true);
 	    final PageableListView<UsuarioListaHoras> usuariosHorasListView = new PageableListView<UsuarioListaHoras>("tabla", this.lstUsuariosModel, 1000) {
 
 			private static final long serialVersionUID = 1L;
 			
 	
 			@Override
 	        protected void populateItem(ListItem<UsuarioListaHoras> item) {
 	           	final UsuarioListaHoras usuarioHoras = item.getModel().getObject();   
 //	           	
 //	           	if((item.getIndex() % 2) == 0){
 //	           		item.add(new AttributeModifier("class","odd"));
 //	           	}
             	String nombreCorregido= daoService.getNombreApellido(usuarioHoras.getUsuario()).replaceAll("Ã³","ó").replaceAll("Ã©","é").replaceAll("Ã±","ñ").replaceAll("Ã¡","á").replaceAll("Ã­","í");
 	           	
             	Label nombre = new Label("apellidoNombre",nombreCorregido);
             	item.add(nombre);
             	Map<Date, Double> colHoras = new HashMap<Date, Double>();
             	colHoras.putAll(usuarioHoras.getDiaHoras());
             	
             	List<Feriado> feriados = daoService.getFeriados();
             	List<Date> fechaFeriados = new ArrayList<Date>();
 				for (Feriado feriado : feriados) {
 					fechaFeriados.add(feriado.getFecha());
 				}
             	LocalDate diaActual = new LocalDate(VistaHorasPage.this.desde);
             	
             	for (int j = 1; j <= 31; j++) {
             		Label lbHoras;
             		Double horasDia = new Double(0);
             		
             		horasDia = colHoras.get(diaActual.toDate());
             		
             		if(horasDia != null) {    			
             			lbHoras = new Label("contenidoDia" + j, Model.of(horasDia));
             			lbHoras.add(new AttributeModifier("style", Model.of("display: block; ")));
             			if(fechaFeriados.contains(diaActual.toDate()) || diaActual.getDayOfWeek()==DateTimeConstants.SATURDAY || diaActual.getDayOfWeek()==DateTimeConstants.SUNDAY ){
 //            				lbHoras.add(new AttributeAppender("style", Model.of("background-color:gray;")));
             				//background-clip: border-box; background-color: blue; color: white; border: 5px solid blue; border-radius: 5px;
             			}
             			if (horasDia < usuarioHoras.getDedicacion()) {
             				lbHoras.add(new AttributeAppender("style", Model.of("color:red;")));
             			}
             		}else if(fechaFeriados.contains(diaActual.toDate()) || diaActual.getDayOfWeek()==DateTimeConstants.SATURDAY || diaActual.getDayOfWeek()==DateTimeConstants.SUNDAY ){
             			lbHoras = new Label("contenidoDia" + j, "0");
 //            			lbHoras.add(new AttributeModifier("style", Model.of("background-color:gray;")));
             		}else{
             			lbHoras = new Label("contenidoDia" + j, "0");
             		}
             		
             		item.add(lbHoras);
             		diaActual = diaActual.plusDays(1);
             	}
             	
             };
             
             	
 		};
 		//html dinamico
 	    this.titulos = new WebComponent("tituloHtml"){
 			@Override
 			public void onComponentTagBody(MarkupStream markupStream,ComponentTag openTag) {
 				Response response = getRequestCycle().getResponse();
 				String respuesta= "";
 				respuesta+="<th class='skinnyTable' scope='col'>Usuarios</th>";
 				LocalDate diaActual = new LocalDate(VistaHorasPage.this.desde);
 				for(int i = 1;i<32;i++){
 					
 					respuesta+="<th class='skinnyTable' scope='col' wicket:id='dia1'>"+ diaActual.getDayOfMonth()+"/"+diaActual.getMonthOfYear() +"</th>";
 					diaActual = diaActual.plusDays(1);
 				}
                 response.write(respuesta);
 			}
 	    	
         };
         this.listViewContainer.add(titulos);
 		this.listViewContainer.add(usuariosHorasListView);
 		this.listViewContainer.add(new AjaxPagingNavigator("navigator", usuariosHorasListView));
 		add(listViewContainer);
 		
 		radioContainer = new WebMarkupContainer("radioContainerUsuarios");
 		radioContainer.setOutputMarkupId(true);
 		
 		RadioGroup<String> radiog = new RadioGroup<String>("radioGroup", new Model<String>());
 		
 		Radio<String> mostrarTodas = new Radio<String>("mostrarTodas", Model.of("Todos"));
 		Radio<String> mostrarEnRojo = new Radio<String>("mostrarEnRojo", Model.of("Menor que dedicación"));
 		
 		mostrarTodas.add(new AjaxEventBehavior("onchange") {
            
             protected void onEvent(AjaxRequestTarget target) {
             	VistaHorasPage.this.lstUsuariosModel.setObject(VistaHorasPage.this.lstUsuariosModel.getObject());
                 usuariosHorasListView.setModel(VistaHorasPage.this.lstUsuariosModel);
                 target.add(listViewContainer);
             }
            
         });
 		
 		mostrarEnRojo.add(new AjaxEventBehavior("onchange") {
 	           
             protected void onEvent(AjaxRequestTarget target) {
                 usuariosHorasListView.setModel(VistaHorasPage.this.lstUsuariosEnRojoModel);
                 target.add(listViewContainer);
             }
            
         });
 		
 		radiog.add(mostrarTodas);
 		radiog.add(mostrarEnRojo);
 		radioContainer.add(radiog);
 		
 		add(radioContainer);
 		
 	}
 	
 	public class VistaHorasForm extends Form<FormHoras>{
 		
 		public IModel<FormHoras> modeloHoras = new CompoundPropertyModel<FormHoras>(new FormHoras());
 		public LocalDate fechaActual = new LocalDate();
 		public YuiDatePicker fechaDesde;
 		public YuiDatePicker fechaHasta;
 		public DropDownChoice<String> sector;
 		
 		
 		public VistaHorasForm(String id) {
 			super(id);
 			this.setDefaultModel(modeloHoras);
 			this.setOutputMarkupId(true);
 			fechaDesde= new YuiDatePicker("fechaDesde") {
 				
 				@Override
 				protected void onDateSelect(AjaxRequestTarget target, String selectedDate) {
 					String json = selectedDate;
 					List<String> campos = Arrays.asList(json.split("/"));
 					int dia = Integer.parseInt(campos.get(0));
 					int mes = Integer.parseInt(campos.get(1));
 					int anio = Integer.parseInt(campos.get(2));
 					desde = new LocalDate(anio,mes,dia).toDate();
 					System.out.println("hola");
 					target.add(listViewContainer);
 					
 				}
 				
 				@Override
 				public DatePickerDTO getDatePickerData() {
 					return new DatePickerDTO();
 				}
 			};
 			fechaDesde.setOutputMarkupId(true);
 			List<String> sectores = new ArrayList<String>();
 			sectores.add("TI");
 			sectores.add("E&P");
 			sectores.add("Administracion");
 			sectores.add("ExOdea");
 			sectores.add("Todos");
 			sector = new DropDownChoice<String>("sector",Model.of("Todos"),sectores);
 			sector.add(new AjaxFormComponentUpdatingBehavior("onchange"){
 
 				@Override
 				protected void onUpdate(AjaxRequestTarget target) {
 					System.out.println("changed!");
 					VistaHorasPage.this.sectorGlobal= sector.getModelObject();
 					target.add(listViewContainer);
 				}
 				
 			});
 			
 			AjaxButton submit = new AjaxButton("submit") {
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 					System.out.println("llega");
 					lstUsuariosModel.setObject(daoService.obtenerHorasUsuarios(fechaDesde.getModelObject(),fechaHasta.getModelObject(),sector.getModelObject()));
 					target.add(listViewContainer);
 				}
 				
 			};
 			submit.setOutputMarkupId(true);
 			add(fechaDesde);
 			add(sector);
 			add(submit);
 			LocalDate ld = new LocalDate(VistaHorasPage.this.desde).plusDays(30);
 			add(new Label("fechaHasta",ld.getDayOfMonth()+"/"+ld.getMonthOfYear()+"/"+ld.getYear()));
 		}
 		
 		
 	}
 	
 }

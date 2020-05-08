 package model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import model.evento.Actividad;
 import model.evento.Evento;
 import model.evento.Reunion;
 import model.recurso.Empleado;
 import model.recurso.PerfilRecurso;
 import model.recurso.Recurso;
 import model.recurso.Rol;
 
 import org.joda.time.DateTime;
 import org.joda.time.Hours;
 import org.joda.time.MutableInterval;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 
 public class EmpresaTest {
 	
 	@Mock private Empleado empleado1;
 	@Mock private Empleado empleado2;
 	@Mock private Empleado empleado3;
 	@Mock private Reunion reunion;
 	@Mock private PerfilRecurso perfil1;
 	@Mock private PerfilRecurso perfil2;
 	
 	private Set<Recurso> recursos;
 	private List<PerfilRecurso> perfiles;
 	private Empresa empresa;
 	private Actividad actividad;
 	private Actividad actividad2;
 	
 	@Before
 	public void setUp() throws Exception {
 		MockitoAnnotations.initMocks(this);
 		
 		this.actividad = new Actividad();
 		DateTime inicioActividad = new DateTime(2012, 1, 1, 10, 0, 0, 0);
 		DateTime finActividad = new DateTime(2012, 1, 1, 11, 0, 0, 0);
 		actividad.setHorario(new MutableInterval(inicioActividad, finActividad));
 		this.actividad2 = new Actividad();
 		DateTime inicioActividad2 = new DateTime(2012, 1, 1, 11, 0, 0, 0);
 		DateTime finActividad2 = new DateTime(2012, 1, 1, 12, 0, 0, 0);
 		actividad2.setHorario(new MutableInterval(inicioActividad2, finActividad2));
 		
 		this.empresa = new Empresa();
 		this.empresa.agregarEmpleado(empleado1);
 		this.empresa.agregarEmpleado(empleado2);
 		this.empresa.agregarEmpleado(empleado3);
 		
 		this.perfiles = new ArrayList<PerfilRecurso>();
 		this.perfiles.add(perfil1);
 		this.perfiles.add(perfil2);
 		
 		this.recursos = new HashSet<Recurso>();
 		this.recursos.add(empleado1);
//		this.recursos.add(empleado2);
 	}
 	
 	@Test
 	public void testRecursosParaReunionConSoloPerfilesDeEmpleado(){
		Mockito.when(this.empresa.obtenerRecursoParaReunionConPerfil(reunion, perfil1)).thenReturn(empleado1);
//		Mockito.when(this.empresa.obtenerRecursoParaReunionConPerfil(reunion, perfil2)).thenReturn(empleado2);
 		assertEquals(recursos, this.empresa.recursosParaReunionConPerfiles(perfiles, reunion));
 		
 	}
 	
 	
 	@Test
 	public void testEmpresaDameTiempoDedicadoAlEventoParaUnGrupoDeEmpleados(){
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		assertEquals(this.empresa.dameTiempoDedicadoAlEventoPorEmpleados(empleados,this.actividad),Hours.THREE.toStandardDuration());
 	}
 	@Test
 	public void testEmpresaDameTiempoDedicadoAlEventoParaUnGrupoDeEmpleadosDeUnSector(){
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn("Ventas").when(empleado1).getSector();
 		Mockito.doReturn("Ventas").when(empleado2).getSector();
 		Mockito.doReturn("Compras").when(empleado3).getSector();
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		assertEquals(this.empresa.dameTiempoDedicadoAlEventoPorSector(empleados,"Ventas",this.actividad),Hours.TWO.toStandardDuration());
 	
 	}
 	@Test
 	public void testEmpresaDameTiempoDedicadoAlEventoParaUnGrupoDeEmpleadosConUnRol(){
 		Mockito.doReturn(Hours.TWO.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(Rol.PROGRAMADOR).when(empleado1).getRol();
 		Mockito.doReturn(Rol.ARQUITECTO).when(empleado2).getRol();
 		Mockito.doReturn(Rol.DISENIADOR_DE_SISTEMAS).when(empleado3).getRol();
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		assertEquals(this.empresa.dameTiempoDedicadoAlEventoPorRol(empleados,Rol.PROGRAMADOR,this.actividad),Hours.TWO.toStandardDuration());
 	
 	}
 	
 	@Test
 	public void testEmpresaDamePorcentajeDeTiempoDedicadoAlEventoParaUnGrupoDeEmpleados(){
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(8.0).when(empleado1).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado2).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado3).duracionDeTiempoDeTrabajo();
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		assertEquals(12.5,this.empresa.damePorcentajeDeTiempoDedicadoAlEventoPorEmpleados(empleados,this.actividad),12.5);
 	}
 	@Test
 	public void testEmpresaDamePorcentajeDeTiempoDedicadoAlEventoParaUnGrupoDeEmpleadosDeUnSector(){
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.TWO.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(8.0).when(empleado1).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado2).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado3).duracionDeTiempoDeTrabajo();
 		
 		Mockito.doReturn("Ventas").when(empleado1).getSector();
 		Mockito.doReturn("Ventas").when(empleado2).getSector();
 		Mockito.doReturn("Compras").when(empleado3).getSector();
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		assertEquals(18.75,this.empresa.damePorcentajeDeTiempoDedicadoAlEventoPorSector(empleados,"Ventas",this.actividad),18.75);
 	
 	}
 	@Test
 	public void testEmpresaDamePorcentajeDeTiempoDedicadoAlEventoParaUnGrupoDeEmpleadosConUnRol(){
 		Mockito.doReturn(Hours.TWO.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.THREE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(8.0).when(empleado1).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado2).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado3).duracionDeTiempoDeTrabajo();
 		
 		Mockito.doReturn(Rol.PROGRAMADOR).when(empleado1).getRol();
 		Mockito.doReturn(Rol.ARQUITECTO).when(empleado2).getRol();
 		Mockito.doReturn(Rol.PROGRAMADOR).when(empleado3).getRol();
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		assertEquals(43.75,this.empresa.damePorcentajeDeTiempoDedicadoAlEventoPorRol(empleados,Rol.PROGRAMADOR,this.actividad),43.75);
 	
 	}
 	@Test
 	public void testEmpresaDameDineroInvertidoEnUnEventoParaUnGrupoDeEmpleados(){
 		Mockito.doReturn(Hours.TWO.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.THREE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(8.0).when(empleado1).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado2).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado3).duracionDeTiempoDeTrabajo();
 								
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		assertEquals(120.0,this.empresa.dameDineroInvertidoEnUnEvento(empleados, this.actividad),120.0);
 	
 	}
 	
 	@Test
 	public void testEmpresaDameDineroInvertidoEnUnEventoParaUnGrupoDeEmpleadosDeUnSector(){
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(20.0).when(empleado1).getPrecio();
 		Mockito.doReturn(20.0).when(empleado2).getPrecio();
 		Mockito.doReturn(20.0).when(empleado3).getPrecio();
 		
 		Mockito.doReturn("Ventas").when(empleado1).getSector();
 		Mockito.doReturn("Ventas").when(empleado2).getSector();
 		Mockito.doReturn("Compras").when(empleado3).getSector();
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		assertEquals(40.0,this.empresa.dameDineroInvertidoEnUnEventoPorSector(empleados,"Ventas",this.actividad),40.0);
 	}
 	@Test
 	public void testEmpresaDameDineroInvertidoEnUnEventoParaUnGrupoDeEmpleadosPorRol(){
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(30.0).when(empleado1).getPrecio();
 		Mockito.doReturn(20.0).when(empleado2).getPrecio();
 		Mockito.doReturn(20.0).when(empleado3).getPrecio();
 		
 		Mockito.doReturn(Rol.PROGRAMADOR).when(empleado1).getRol();
 		Mockito.doReturn(Rol.ARQUITECTO).when(empleado2).getRol();
 		Mockito.doReturn(Rol.PROGRAMADOR).when(empleado3).getRol();
 		
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		assertEquals(50.0,this.empresa.dameDineroInvertidoEnUnEventoPorRol(empleados,Rol.PROGRAMADOR,this.actividad),50.0);
 	}
 //*************************************
 	
 	
 	@Test
 	public void testEmpresaDameTiempoDedicadoAUnGrupoDeEventosParaUnGrupoDeEmpleados(){
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad2);
 		
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		Set<Evento> eventos = new HashSet<Evento>();
 		eventos.add(this.actividad);
 		eventos.add(this.actividad2);
 		
 		assertEquals(this.empresa.dameTiempoDedicadoAEventosPorEmpleados(empleados,eventos),Hours.SIX.toStandardDuration());
 	}
 	@Test
 	public void testEmpresaDameTiempoDedicadoAUnGrupoDeEventosParaUnGrupoDeEmpleadosDeUnSector(){
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad2);
 		
 		Mockito.doReturn("Ventas").when(empleado1).getSector();
 		Mockito.doReturn("Ventas").when(empleado2).getSector();
 		Mockito.doReturn("Compras").when(empleado3).getSector();
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		Set<Evento> eventos = new HashSet<Evento>();
 		eventos.add(this.actividad);
 		eventos.add(this.actividad2);
 		
 		assertEquals(this.empresa.dameTiempoDedicadoAEventosPorSector(empleados,"Ventas",eventos),Hours.FOUR.toStandardDuration());
 	
 	}
 	@Test
 	public void testEmpresaDameTiempoDedicadoAUnGrupoDeEventosParaUnGrupoDeEmpleadosConUnRol(){
 		Mockito.doReturn(Hours.TWO.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad2);
 		
 		Mockito.doReturn(Rol.PROGRAMADOR).when(empleado1).getRol();
 		Mockito.doReturn(Rol.ARQUITECTO).when(empleado2).getRol();
 		Mockito.doReturn(Rol.DISENIADOR_DE_SISTEMAS).when(empleado3).getRol();
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		Set<Evento> eventos = new HashSet<Evento>();
 		eventos.add(this.actividad);
 		eventos.add(this.actividad2);
 		
 		assertEquals(this.empresa.dameTiempoDedicadoAEventosPorRol(empleados,Rol.PROGRAMADOR,eventos),Hours.THREE.toStandardDuration());
 	
 	}
 	
 	@Test
 	public void testEmpresaDamePorcentajeDeTiempoDedicadoAUnGrupoDeEventosParaUnGrupoDeEmpleados(){
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad2);
 		
 		Mockito.doReturn(8.0).when(empleado1).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado2).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado3).duracionDeTiempoDeTrabajo();
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		Set<Evento> eventos = new HashSet<Evento>();
 		eventos.add(this.actividad);
 		eventos.add(this.actividad2);
 		
 		assertEquals(25,this.empresa.damePorcentajeDeTiempoDedicadoAEventosPorEmpleados(empleados,eventos),25);
 	}
 	@Test
 	public void testEmpresaDamePorcentajeDeTiempoDedicadoAUnGrupoDeEventosParaUnGrupoDeEmpleadosDeUnSector(){
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.TWO.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad2);
 		
 		Mockito.doReturn(8.0).when(empleado1).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado2).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado3).duracionDeTiempoDeTrabajo();
 		
 		Mockito.doReturn("Ventas").when(empleado1).getSector();
 		Mockito.doReturn("Ventas").when(empleado2).getSector();
 		Mockito.doReturn("Compras").when(empleado3).getSector();
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		Set<Evento> eventos = new HashSet<Evento>();
 		eventos.add(this.actividad);
 		eventos.add(this.actividad2);
 		
 		assertEquals(25.0,this.empresa.damePorcentajeDeTiempoDedicadoAEventosPorSector(empleados,"Ventas",eventos),25.0);
 	
 	}
 	@Test
 	public void testEmpresaDamePorcentajeDeTiempoDedicadoAUnGrupoDeEventosParaUnGrupoDeEmpleadosConUnRol(){
 		Mockito.doReturn(Hours.TWO.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.THREE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad2);
 		
 		Mockito.doReturn(8.0).when(empleado1).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado2).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado3).duracionDeTiempoDeTrabajo();
 		
 		Mockito.doReturn(Rol.PROGRAMADOR).when(empleado1).getRol();
 		Mockito.doReturn(Rol.ARQUITECTO).when(empleado2).getRol();
 		Mockito.doReturn(Rol.PROGRAMADOR).when(empleado3).getRol();
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		Set<Evento> eventos = new HashSet<Evento>();
 		eventos.add(this.actividad);
 		eventos.add(this.actividad2);
 		
 		assertEquals(43.75,this.empresa.damePorcentajeDeTiempoDedicadoAEventosPorRol(empleados,Rol.PROGRAMADOR,eventos),43.75);
 	
 	}
 	@Test
 	public void testEmpresaDameDineroInvertidoEnUnGrupoDeEventosParaUnGrupoDeEmpleados(){
 		Mockito.doReturn(Hours.TWO.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.THREE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad2);
 		
 		Mockito.doReturn(8.0).when(empleado1).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado2).duracionDeTiempoDeTrabajo();
 		Mockito.doReturn(8.0).when(empleado3).duracionDeTiempoDeTrabajo();
 								
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		Set<Evento> eventos = new HashSet<Evento>();
 		eventos.add(this.actividad);
 		eventos.add(this.actividad2);
 		
 		assertEquals(180.0,this.empresa.dameDineroInvertidoEnEventos(empleados, eventos),180.0);
 	
 	}	
 	@Test
 	public void testEmpresaDameDineroInvertidoEnUnGrupoDeEventosParaUnGrupoDeEmpleadosDeUnSector(){
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad2);
 		
 		Mockito.doReturn(20.0).when(empleado1).getPrecio();
 		Mockito.doReturn(20.0).when(empleado2).getPrecio();
 		Mockito.doReturn(20.0).when(empleado3).getPrecio();
 		
 		Mockito.doReturn("Ventas").when(empleado1).getSector();
 		Mockito.doReturn("Ventas").when(empleado2).getSector();
 		Mockito.doReturn("Compras").when(empleado3).getSector();
 						
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		Set<Evento> eventos = new HashSet<Evento>();
 		eventos.add(this.actividad);
 		eventos.add(this.actividad2);
 		
 		assertEquals(80.0,this.empresa.dameDineroInvertidoEnEventosPorSector(empleados,"Ventas",eventos),80.0);
 	}
 	@Test
 	public void testEmpresaDameDineroInvertidoEnUnGrupoDeEventosParaUnGrupoDeEmpleadosDeUnRol(){
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad);
 		
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado1).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado2).dameTiempoDedicadoPara(this.actividad2);
 		Mockito.doReturn(Hours.ONE.toStandardDuration()).when(empleado3).dameTiempoDedicadoPara(this.actividad2);
 		
 		Mockito.doReturn(30.0).when(empleado1).getPrecio();
 		Mockito.doReturn(20.0).when(empleado2).getPrecio();
 		Mockito.doReturn(20.0).when(empleado3).getPrecio();
 		
 		Mockito.doReturn(Rol.PROGRAMADOR).when(empleado1).getRol();
 		Mockito.doReturn(Rol.ARQUITECTO).when(empleado2).getRol();
 		Mockito.doReturn(Rol.PROGRAMADOR).when(empleado3).getRol();
 		
 		Set<Empleado> empleados = new HashSet<Empleado>();
 		empleados.add(this.empleado1);
 		empleados.add(this.empleado2);
 		empleados.add(this.empleado3);
 		
 		Set<Evento> eventos = new HashSet<Evento>();
 		eventos.add(this.actividad);
 		eventos.add(this.actividad2);
 		
 		assertEquals(80.0,this.empresa.dameDineroInvertidoEnEventosPorRol(empleados,Rol.PROGRAMADOR,eventos),80.0);
 	}
 }

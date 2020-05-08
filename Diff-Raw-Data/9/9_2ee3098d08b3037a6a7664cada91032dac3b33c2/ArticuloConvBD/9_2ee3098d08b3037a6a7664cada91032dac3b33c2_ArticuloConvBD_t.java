 package cidc.articulos_Conv.db;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 
 import cidc.articulos_Conv.obj.InfoGeneral;
 import cidc.convMovilidad.obj.Requisitos;
 import cidc.general.db.BaseDB;
 import cidc.general.db.CursorDB;
 import cidc.general.mails.EnvioMail2;
 import cidc.general.mails.Reporte;
 import cidc.general.obj.Globales;
 import cidc.inscripSistema.obj.Persona;
 import cidc.planAccion.obj.Actividades;
 
 public class ArticuloConvBD extends BaseDB {
 	private String consec=null;
 	//constructor
 		public ArticuloConvBD(CursorDB c, int perfil) {
 			super(c, perfil);
 			// TODO Auto-generated constructor stub
 			//archivo que almacena sentencias sql
 			rb=ResourceBundle.getBundle("cidc.articulos_Conv.consultas");
 		}
 
 	
 	//Lista las propuestas que ha realizado el investigador
 	public List consultaLista(long idPersona){
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		InfoGeneral info=null;
 		List lista=new ArrayList();
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("consultaLista"));
 			ps.setLong(1,idPersona);
 			ps.setString(2,"%"+2012+"%");
 			rs=ps.executeQuery();
 		//	//System.out.println("idPersona="+idPersona);
 			while(rs.next()){
 				i=1;
 				info=new InfoGeneral();
 				info.setIdPersona(idPersona);
 				info.setIdPropuesta(rs.getLong(i++));
 				info.setConvid(rs.getInt(i++));
 				info.setIdGrupo(rs.getInt(i++));
 				info.setNombreart(rs.getString(i++));				
 				info.setRevista(rs.getString(i++));
 				info.setIssnrevista(rs.getString(i++));
 				info.setAreacon(rs.getString(i++));
 				info.setSubareacon(rs.getString(i++));
 				info.setPalabrasnum(rs.getString(i++));
 				info.setProyinv(rs.getString(i++));
 				info.setProypa(rs.getString(i++));
 				info.setResumen(rs.getString(i++));
 				info.setFechains(rs.getString(i++));
 				info.setArchivoart(rs.getString(i++));
 				info.setArchradicado(rs.getString(i++));				
 				lista.add(info);				
 			}
 			System.out.println(rb);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return lista;
 	}
 
 	//info del evento
 	public boolean insertaPropuesta(InfoGeneral info){
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			//con false no es automatico el proceso de consultas
 			cn.setAutoCommit(false);
 			ps=cn.prepareStatement(rb.getString("insertaArticulo"));
 			ps.setLong(i++, info.getIdPersona());
 			
 			//cvlac
 			ps.setInt(i++, info.getConvid());
 			ps.setInt(i++, info.getIdGrupo());
 			ps.setString(i++, info.getNombreart());
 			ps.setString(i++, info.getArchivoart());
 			ps.setString(i++, info.getArchradicado());
 			ps.setString(i++, info.getProyinv());
 			ps.setString(i++, info.getProypa());
 			ps.setString(i++, info.getRevista());
 			ps.setString(i++, info.getIssnrevista());
 			ps.setString(i++, info.getResumen());
 			ps.setString(i++, info.getPalabrasnum());
 			ps.setString(i++, info.getAreacon());
 			ps.setString(i++, info.getSubareacon());
 			ps.executeUpdate();			
 			//proporciona el ID de la propuesta
 			ps=cn.prepareStatement(rb.getString("getIdProp"));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				this.consec=rs.getString(1);
 			}
 			i=1;
 			cn.commit();
 			retorno=true;
 			
 		}catch (SQLException e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}
 		catch (Exception e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}finally{
 			//obligatorio en todas las conexiones con la base de datos 
 			cerrar(ps);
 			cerrar(cn);
 		}
 		//retorno es booleana y retorna false si algo fallo
 		return retorno;
 	}
 	
 	public String getConsec() {
 		return consec;
 	}
 	
 	public InfoGeneral consultaIndividual(String idPropuesta){
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		InfoGeneral info=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("consultaIndividual"));
 			ps.setLong(1,Long.parseLong(idPropuesta));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				i=1;
 				info=new InfoGeneral();
 				info.setIdPropuesta(Long.parseLong(idPropuesta));
 				info.setConvid(rs.getInt(i++));
 				info.setIdGrupo(rs.getInt(i++));
 				info.setNombreart(rs.getString(i++));
 				info.setArchivoart(rs.getString(i++));
 				info.setArchradicado(rs.getString(i++));
 				info.setProyinv(rs.getString(i++));
 				//info.setProypa(rs.getString(i++));
 				info.setRevista(rs.getString(i++));
 				info.setIssnrevista(rs.getString(i++));
 				info.setResumen(rs.getString(i++));
 				info.setPalabrasnum(rs.getString(i++));
 				info.setAreacon(rs.getString(i++));
 				info.setSubareacon(rs.getString(i++));
 				info.setFechains(rs.getString(i++));
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return info;
 	}
 	
 	public boolean enviarMail(String idPropuesta,Persona persona){
 		boolean retorno=false;
 		String []destino={persona.getMail()};
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		String consMail=null;
 		boolean x=false;
 		//System.out.println("bandera 1");
 		try {
 			cn = cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("getConsecutivo"));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				consMail=rs.getString(1);
 			}
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e1);
 		}
 		//System.out.println("bandera 2");
 		ResourceBundle rb1= ResourceBundle.getBundle("cidc.general.mails.soporteInscripcion");
 		InfoGeneral general=consultaIndividual(idPropuesta);	
 		Globales gl=new Globales();
 		//System.out.println("bandera 3");
 		StringBuffer texto=new StringBuffer();
 		texto.append("<b>CIDC "+consMail+"-"+gl.getAnoCortoHoy()+"</b><br><br>");
 		texto.append(rb1.getString("rp1")+"  <b>"+persona.getNombre()+"</b>");
 		texto.append(rb1.getString("rp2"));
 		texto.append(rb1.getString("e1"));
 		texto.append(persona.getNombre());
 		texto.append(rb1.getString("e2"));
 		texto.append(persona.getNombGrupo());
 		texto.append(rb1.getString("e3"));
 		texto.append(rb1.getString("e15"));
 		texto.append(rb1.getString("e16"));
 		//System.out.println("bandera 5");
 		if(!x)
 			texto.append(rb1.getString("e221"));
 		else
 			texto.append(rb1.getString("e222"));
 		texto.append(rb1.getString("e23"));
 		EnvioMail2 mail=new EnvioMail2("siciud");
 		////System.out.println("bandera 6");
 		try {
 			//destino= nombre correo electronico, asunto, cuerpo del mensaje
 			mail.enviar(destino,"Inscripcin Convocatoria Movilidad",""+texto);
 			////System.out.println("bandera 7");
 			Reporte reporteMail=new Reporte(cursor,super.perfil);
 			reporteMail.reportar(persona.getNombre(),"Inscripcin Convocatoria 2012",destino[0],consMail);
 			retorno=true;
 			//System.out.println("bandera 8");
 		} catch (AddressException e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		} catch (MessagingException e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}
 		return retorno;
 	}
 	
 	public List AjaxProyectosInvestigacion(int idGrupo){
 		List l=new ArrayList();
 		Connection cn=null;
 		PreparedStatement ps=null; 
 		ResultSet rs=null;
 		System.out.println("En la funcion Ajax");
 		int i=0;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("ajaxProyectos"));
 			ps.setLong(1,idGrupo);
 			ps.setString(2, ""+Calendar.getInstance().get(Calendar.YEAR));
 			ps.setString(3, ""+Calendar.getInstance().get(Calendar.YEAR));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				i=1;
 				Actividades actividadOBJ=new Actividades();
 				actividadOBJ.setIdActividad((rs.getInt(i++)));
 				System.out.println("IdActividad "+rs.getInt(1));
				actividadOBJ.setActividad(rs.getString(i++).replaceAll("[\n\r]", ""));
 				System.out.println("Descripcion "+rs.getString(2));
 				l.add(actividadOBJ);
 	//			System.out.println(gruposOBJ.getNombre());
 			}
 		//	System.out.println("la cantidad de grupos encontrados es:"+l.size());
 		}catch (SQLException e) {
 			lanzaExcepcion(e);
 		}catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		System.out.println("La longitud de la lista es ***** "+ l.size());		
 		return l;
 	}
 }

 package cidc.convMovilidad.db;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 
 import cidc.convMovilidad.obj.InfoGeneral;
 import cidc.convMovilidad.obj.Requisitos;
 import cidc.evalMovilidad.db.EvalMovilidadDB;
 import cidc.general.db.BaseDB;
 import cidc.general.db.CursorDB;
 import cidc.general.mails.EnvioMail2;
 import cidc.general.mails.Reporte;
 import cidc.general.obj.Globales;
 import cidc.inscripSistema.obj.Persona;
 import cidc.planAccion.obj.Actividades;
 
 public class MovilidadDB extends BaseDB{
 
 	private String consec=null;
 //constructor
 	public MovilidadDB(CursorDB c, int perfil) {
 		super(c, perfil);
 		// TODO Auto-generated constructor stub
 		//archivo que almacena sentencias sql
 		rb=ResourceBundle.getBundle("cidc.convMovilidad.consultas");
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
 			ps=cn.prepareStatement(rb.getString("insertaMovilidad"));
 			ps.setLong(i++, info.getIdPersona());
 			
 			//cvlac
 			ps.setInt(i++, info.getTipo());
 			ps.setString(i++, info.getPais());
 			ps.setString(i++, info.getCiudad());
 			ps.setString(i++, info.getNombreEvento());
 			ps.setString(i++, info.getInstitucion());
 			ps.setString(i++, info.getSiglasInstitu());
 			ps.setString(i++, info.getFechaInicio());
 			ps.setString(i++, info.getFechaFin());
 			ps.setString(i++, info.getPagEvento());
 			ps.setString(i++, info.getNombrePonencia());
 			ps.setString(i++, info.getNombreAutores());
 			ps.setString(i++, info.getValorInsc());
 			ps.setInt(i++, info.getMoneda());
 			ps.setString(i++, info.getTrayectoria());
 			ps.setInt(i++, info.getGrupo());
 			//Esta variable captura el valor de la lista de arbitraje
 			ps.setString(i++, info.getLista_arbitraje());
 			ps.setString(i++, info.getProyectoinv());
 			System.out.println("Consulta: "+ps);
 			ps.executeUpdate();			
 			//proporciona el ID de la propuesta
 			ps=cn.prepareStatement(rb.getString("getIdProp"));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				this.consec=rs.getString(1);
 			}
 			i=1;
 			ps=cn.prepareStatement(rb.getString("insertaRegInforme"));
 			ps.setLong(i++, Long.parseLong(consec));
 			ps.setLong(i++, info.getIdPersona());
 			
 			ps.executeUpdate();
 			//ejecuta las sentencias anteriores
 			// hace un rollback si algo falla
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
 
 	
 	public boolean actualizarPropuesta(InfoGeneral info){
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			//con false no es automatico el proceso de consultas
 			cn.setAutoCommit(false);		
 			
 			ps=cn.prepareStatement(rb.getString("actualizacionPaso1"));		
 			
 			//cvlac
 			
 			ps.setInt(i++, info.getTipo());
 			ps.setString(i++, info.getPais());
 			ps.setString(i++, info.getCiudad());
 			ps.setString(i++, info.getNombreEvento());
 			ps.setString(i++, info.getInstitucion());
 			ps.setString(i++, info.getSiglasInstitu());
 			ps.setString(i++, info.getFechaInicio());
 			ps.setString(i++, info.getFechaFin());
 			ps.setString(i++, info.getPagEvento());
 			ps.setString(i++, info.getNombrePonencia());
 			ps.setString(i++, info.getNombreAutores());
 			ps.setString(i++, info.getValorInsc());
 			ps.setInt(i++, info.getMoneda());
 			ps.setString(i++, info.getTrayectoria());
 			ps.setInt(i++, info.getGrupo());
 			//Esta variable captura el valor de la lista de arbitraje
 			ps.setString(i++, info.getLista_arbitraje());
 			ps.setString(i++, info.getProyectoinv());
 			ps.setLong(i++, info.getIdPropuesta());
 			System.out.println("Consulta: "+ps);
 			ps.executeUpdate();			
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
 	//insetar la segunta Agenda de cooperación
 	public boolean insertaAgenda(Requisitos requis){
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			cn.setAutoCommit(false);
 			i=1;
 			ps=cn.prepareStatement(rb.getString("insertaRequisito"));
 			ps.setLong(i++, requis.getIdProp());
 			//almacena la información de la Justificación del investigador
 			ps.setString(i++, requis.getCompromisos1());
 			ps.setString(i++, requis.getCompromisos2());
 			//almacena la información de la Agenda de actividades del investigador
 			ps.setString(i++, requis.getCompromisos3());
 			ps.setString(i++, requis.getCompromisos4());
 			ps.setString(i++, requis.getBeneficiosGrupo1());
 			ps.setString(i++, requis.getBeneficiosGrupo2());
 			ps.setString(i++, requis.getBeneficiosGrupo3());
 			ps.setString(i++, requis.getBeneficiosGrupo4());
 			ps.setString(i++, requis.getBeneficiosGrupo5());
 			ps.setString(i++, requis.getInteresInsti1());
 			ps.setString(i++, requis.getInteresInsti2());
 			ps.setString(i++, requis.getInteresInsti3());
 			ps.setString(i++, requis.getInteresInsti4());
 			ps.executeUpdate();
 
 			ps=cn.prepareStatement(rb.getString("eliminaEventos"));
 			ps.setLong(1, requis.getIdProp());
 			ps.executeUpdate();
 			
 			ps=cn.prepareStatement(rb.getString("insertaEventoMovilidad"));
 			if(requis.getPartiEvent()!=null){
 				for(int j=0;j<requis.getPartiEvent().length;j++){
 					if(!requis.getPartiEvent()[j].equals("")){
 						ps.setLong(1, requis.getIdProp());
 						ps.setString(2, requis.getPartiEvent()[j]);
 						ps.addBatch();
 					}
 				}
 			}
 			ps.executeBatch();
 			cn.commit();
 			ps=cn.prepareStatement(rb.getString("getIdProp"));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				this.consec=rs.getString(1);
 			}
 			retorno=true;
 		}catch (SQLException e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}
 		catch (Exception e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}finally{
 			
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
 	}
 
 	/*metodo para actualizar la información del archivo que se ha cargado en el sistema*/
 	public boolean actualizaArchivo(InfoGeneral info, String nombreArchivo,String item){
 		boolean retorno=false;
 		Connection cn=null;
 		PreparedStatement ps=null;
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("actualizaArchivo"+item));
 			ps.setString(i++,nombreArchivo);
 			ps.setLong(i++, info.getIdPropuesta());
 			ps.executeUpdate();
 			retorno=true;
 		}catch (SQLException e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}
 		catch (Exception e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return retorno;
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
 			ps.setString(2,"%"+2013+"%");
 			rs=ps.executeQuery();
 		//	//System.out.println("idPersona="+idPersona);
 			while(rs.next()){
 				i=1;
 				info=new InfoGeneral();
 				info.setIdPersona(idPersona);
 				info.setIdPropuesta(rs.getLong(i++));
 				info.setTipo(rs.getInt(i++));
 				info.setCorte(rs.getInt(i++));
 				info.setNombrePonencia(rs.getString(i++));
 				info.setPais(rs.getString(i++));
 				info.setCiudad(rs.getString(i++));
 				info.setArchivoAval(rs.getString(i++));
 				info.setArchivoAceptacion(rs.getString(i++));
 				info.setArchivoResumen(rs.getString(i++));
 				info.setArchivoProyCurri(rs.getString(i++));
 				info.setArchivoDecanatura(rs.getString(i++));
 				info.setArchivoConsFac(rs.getString(i++));
 				info.setArchivoConsAcade(rs.getString(i++));
 				info.setArchivoExcelencia(rs.getString(i++));
 				//Esta variale almacenara la informacion del archivo de ceritificaciones cargado por el investigador
 				info.setArchivoCertificaciones(rs.getString(i++));
 				info.setArchivoCertificacionesCIDC(rs.getString(i++));
 				info.setArchivoResultados(rs.getString(i++));
 				info.setArchivoApoyos(rs.getString(i++));
 				info.setEstado(estadoPropuesta(cn, info.getIdPropuesta()));
 				lista.add(info);
 			}
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
 	
 	//Determina si la agenda de cooperación fue almacenada o no
 	public int estadoPropuesta(Connection cn,long idProp){
 		int estado=1;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		try {
 			ps=cn.prepareStatement(rb.getString("agendaEstadoProp"));
 			ps.setLong(1,idProp);
 			rs=ps.executeQuery();
 			if(rs.next()){
 				estado=rs.getInt(1)+1;
 			}
 	//		//System.out.println("id= "+idProp+" estado= "+estado);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 		}
 		return estado;
 	}
 
 	//Consulta los datos generales de la inscripción (para el envio el mail)
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
 				info.setCorte(rs.getInt(i++));
 				info.setGrupo(rs.getInt(i++));
 				info.setSiglasInstitu(rs.getString(i++));
 				info.setLista_arbitraje(rs.getString(i++));
 				info.setMoneda(rs.getInt(i++));
 				info.setProyectoinv(rs.getString(i++));
 				info.setTipo(rs.getInt(i++));
 				info.setTipoLetra(rs.getString(i++));
 				info.setPais(rs.getString(i++));
 				info.setCiudad(rs.getString(i++));
 				info.setInstitucion(rs.getString(i++));
 				info.setFechaInicio(rs.getString(i++));
 				info.setFechaFin(rs.getString(i++));
 				info.setNombrePonencia(rs.getString(i++));
 				info.setNombreAutores(rs.getString(i++));
 				info.setNombreEvento(rs.getString(i++));
 				info.setPagEvento(rs.getString(i++));
 				info.setTrayectoria(rs.getString(i++));
 				info.setValorInsc(rs.getString(i++));
 				info.setMonedaTxt(rs.getString(i++));
 				info.setArchivoAval(rs.getString(i++));
 				info.setArchivoAceptacion(rs.getString(i++));
 				info.setArchivoResumen(rs.getString(i++));
 				info.setArchivoProyCurri(rs.getString(i++));
 				info.setArchivoDecanatura(rs.getString(i++));
 				info.setArchivoConsFac(rs.getString(i++));
 				info.setArchivoConsAcade(rs.getString(i++));
 				info.setArchivoExcelencia(rs.getString(i++));
 				info.setArchivoCertificaciones(rs.getString(i++));
 				info.setArchivoCertificacionesCIDC(rs.getString(i++));
 				info.setArchivoResultados(rs.getString(i++));
 				info.setArchivoApoyos(rs.getString(i++));
 				info.setProyectoinv(rs.getString(i++));
 				info.setArchivoProduccion(rs.getString(i++));
 				info.setEstado(estadoPropuesta(cn, info.getIdPropuesta()));
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
 	//Para el correo pero especifica los datos de la Agenda de COoperacion
 	public Requisitos consultaRequisitos(String idPropuesta){
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		Requisitos req=null;
 		String []eventos=null;
 		int i=1,conteo=0;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("consultaRequisitos"));
 			ps.setLong(1,Long.parseLong(idPropuesta));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				i=1;
 				req=new Requisitos();
 				req.setCompromisos1(rs.getString(i++));
 				req.setCompromisos2(rs.getString(i++));
 				req.setCompromisos3(rs.getString(i++));
 				req.setCompromisos4(rs.getString(i++));
 				req.setBeneficiosGrupo1(rs.getString(i++));
 				req.setBeneficiosGrupo2(rs.getString(i++));
 				req.setBeneficiosGrupo3(rs.getString(i++));
 				req.setBeneficiosGrupo4(rs.getString(i++));
 				req.setBeneficiosGrupo5(rs.getString(i++));
 				req.setInteresInsti1(rs.getString(i++));
 				req.setInteresInsti2(rs.getString(i++));
 				req.setInteresInsti3(rs.getString(i++));
 				req.setInteresInsti4(rs.getString(i++));
 			}
 			rs=null;
 			if(req!=null){
 				//Consulta la cantidad de eventos que el investigador agreso en la lista.
 				ps=cn.prepareStatement(rb.getString("consultaEventosCont"));
 				ps.setLong(1,Long.parseLong(idPropuesta));
 				rs=ps.executeQuery();
 				while(rs.next()){
 					conteo=rs.getInt(1);
 				}
 				if(conteo>0){
 				ps=cn.prepareStatement(rb.getString("consultaEventos"));
 				ps.setLong(1,Long.parseLong(idPropuesta));
 				rs=ps.executeQuery();
 			//	Result paraContar = ResultSupport.toResult(rs);
 			//	//System.out.println("encuentra eventos "+paraContar.getRowCount());
 				eventos=new String [conteo];
 					i=0;
 					while(rs.next()){
 						eventos[i++]=rs.getString(1);
 					}
 					req.setPartiEvent(eventos);
 				}
 			}
 		} catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return req;
 	}
 
 	public String getConsec() {
 		return consec;
 	}
 
 	public void setConsec(String consec) {
 		this.consec = consec;
 	}
 	
 	//Envia la agenda de cooperación el mail al correo del investigador
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
 		Requisitos req=consultaRequisitos(idPropuesta);
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
 		texto.append(general.getTipoLetra());
 		texto.append(rb1.getString("e4"));
 		texto.append(general.getCiudad()+" - "+general.getPais());
 		texto.append(rb1.getString("e5"));
 		texto.append(general.getNombreEvento());
 		texto.append(rb1.getString("e6"));
 		texto.append(general.getInstitucion());
 		texto.append(rb1.getString("e7"));
 		texto.append(general.getNombrePonencia());
 		texto.append(rb1.getString("e8"));
 		texto.append(general.getNombreAutores());
 		texto.append(rb1.getString("e9"));
 		texto.append(general.getTrayectoria());
 		texto.append(rb1.getString("e10"));
 		texto.append(general.getValorInsc());
 		texto.append(rb1.getString("e11"));
 		texto.append(general.getMonedaTxt());
 		texto.append(rb1.getString("e12"));
 		texto.append(general.getPagEvento());
 		texto.append(rb1.getString("e13"));
 		texto.append(general.getFechaInicio());
 		texto.append(rb1.getString("e14"));
 		texto.append(general.getFechaFin());
 		texto.append(rb1.getString("e15"));
 		texto.append(rb1.getString("e16"));
 		//texto.append(req.getCompromisos1()+"<br>"+req.getCompromisos2());
 		texto.append(req.getCompromisos1()+"<br>");
 		texto.append(rb1.getString("e17"));
 		//texto.append(req.getCompromisos3()+"<br>"+req.getCompromisos4());
 		texto.append(req.getCompromisos3()+"<br>");
 		/*texto.append(rb1.getString("e18"));
 		texto.append(req.getBeneficiosGrupo1());
 		texto.append(rb1.getString("e19"));
 		texto.append(req.getBeneficiosGrupo2()+"<br>"+req.getBeneficiosGrupo3());
 		texto.append(rb1.getString("e20"));
 		texto.append(req.getBeneficiosGrupo4()+"<br>"+req.getBeneficiosGrupo5());
 		texto.append(rb1.getString("e118"));
 		texto.append(req.getInteresInsti1());
 		texto.append(rb1.getString("e119"));
 		texto.append(req.getInteresInsti2());
 		texto.append(rb1.getString("e120"));
 		texto.append(req.getInteresInsti3());
 		texto.append(rb1.getString("e121"));
 		texto.append(req.getInteresInsti4());*/
 		texto.append(rb1.getString("e21"));
 		//System.out.println("bandera 4");
 		if(req.getPartiEvent()!=null)
 			if(req.getPartiEvent().length>0){
 				texto.append(rb1.getString("e212"));
 				for(int j=0;j<req.getPartiEvent().length;j++){
 					texto.append("<tr><td>"+req.getPartiEvent()[j]+"</td></tr>");
 				}
 				x=true;
 			}
 			else{
 				texto.append(rb1.getString("e211"));
 			}
 		else{
 			texto.append(rb1.getString("e211"));
 		}
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
 
 	/*public boolean grupoHabilitado(Persona persona){
 		boolean retorno=false;
 		if(persona.getDocumento()!=null){
 			if(persona.getDocumento().trim().equals("")){
 				setMensaje("Es necesario completar la informaci�n de datos personales para poder continuar con la inscripci�n");
 			}else{
 				Connection cn=null;
 				PreparedStatement ps=null;
 				ResultSet rs=null;
 				int i=1;
 				try {
 					cn=cursor.getConnection(super.perfil);
 					ps=cn.prepareStatement(rb.getString("consultaEstadoGrupo"));
 					ps.setLong(i++,persona.getGrupo());
 					rs=ps.executeQuery();
 					while(rs.next()){
 						retorno=rs.getBoolean(1); // se debe retornar el estado del gruop segun la tabla
 					}
 					if(!retorno)
 						setMensaje("El grupo se encuentra Inhabilitado para poder hacer inscripciones en la convocatoria de movilidad");
 				}catch (SQLException e) {
 					// TODO Auto-generated catch block
 					lanzaExcepcion(e);
 				}
 				catch (Exception e) {
 					// TODO Auto-generated catch block
 					lanzaExcepcion(e);
 				}finally{
 					cerrar(ps);
 					cerrar(cn);
 				}
 			}
 		}
 		else{
 			setMensaje("Es necesario completar la informaci�n de datos personales para poder continuar con la inscripci�n");
 		}
 		return retorno;
 	}
 */
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
 	public Requisitos getInfoB(String idPropuesta){
 		Connection cn=null;
 		PreparedStatement ps=null;
 		ResultSet rs=null;
 		Requisitos req=null;
 		EvalMovilidadDB evaluacion= new EvalMovilidadDB(super.cursor,super.perfil); 
 		int i=1;
 		try {
 			cn=cursor.getConnection(super.perfil);
 			ps=cn.prepareStatement(rb.getString("consultaRequisitos"));
 			ps.setLong(1,Long.parseLong(idPropuesta));
 			rs=ps.executeQuery();
 			while(rs.next()){
 				i=1;
 				req=new Requisitos();
 				//Justificacion
 				req.setCompromisos1(rs.getString(i++));
 				req.setCompromisos2(rs.getString(i++));
 				//Agenda de trabajo durante el evento
 				req.setCompromisos3(rs.getString(i++));
 				req.setCompromisos4(rs.getString(i++));
 				req.setBeneficiosGrupo1(rs.getString(i++));
 				req.setBeneficiosGrupo2(rs.getString(i++));
 				req.setBeneficiosGrupo3(rs.getString(i++));
 				req.setBeneficiosGrupo4(rs.getString(i++));
 				req.setBeneficiosGrupo5(rs.getString(i++));
 				req.setInteresInsti1(rs.getString(i++));
 				req.setInteresInsti2(rs.getString(i++));
 				req.setInteresInsti3(rs.getString(i++));
 				req.setInteresInsti4(rs.getString(i++));
 				
 				req.setPartiEvent(evaluacion.getEventos(Long.parseLong(idPropuesta)));
 			}
 			
 			
 		} catch (Exception e) {
 			lanzaExcepcion(e);
 		}finally{
 			cerrar(rs);
 			cerrar(ps);
 			cerrar(cn);
 		}
 		return req;
 	}
 }

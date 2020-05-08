 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package mx.gearsofcode.proyservsocial.logico.impl;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.PreparedStatement;
 import java.util.LinkedList;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import mx.gearsofcode.proyservsocial.logico.ConectaDb;
 
 //import mx.gearsofcode.proyservsocial.logico.LogicoPackage;
 
 import mx.gearsofcode.proyservsocial.logico.proyectos.Proyecto;
 
 import mx.gearsofcode.proyservsocial.logico.usuarios.Responsable;
 import mx.gearsofcode.proyservsocial.logico.usuarios.UsuarioRegistrado;
 import mx.gearsofcode.proyservsocial.logico.inicioDeSesion.TipoUsuario;
 
 import mx.gearsofcode.proyservsocial.logico.util.DBConsultException;
 import mx.gearsofcode.proyservsocial.logico.util.DBCreationException;
 import mx.gearsofcode.proyservsocial.logico.util.DBModificationException;
 
 
 //import org.eclipse.emf.ecore.EClass;
 //import org.eclipse.emf.ecore.EObject;
 //
 //import org.eclipse.emf.ecore.impl.EObjectImpl;
 //
 //import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * Clase que se encarga de manejar las conexiones a la base de
  * datos del proyecto, aqui se hacen todas las sentencias SQL
  * y se pasan al servidor de la base de datos para procesarlas.
  *
  */
 //public class ConectaDbImpl extends EObjectImpl implements ConectaDb {
 public class ConectaDbImpl implements ConectaDb {
 
     // Variables para conexion.
     private final static String driver = "com.mysql.jdbc.Driver";
     private final static String url = "jdbc:mysql://localhost:3306/gearsofcode";
 //    private static String dbName = "gearsofcode";
     private final static String user = "root";
     private final static String password = "goldsun627";
     // Variables para estados de los proyectos y usuarios.
     final private int NO_AUTORIZADO = 0;
     final private int AUTORIZADO = 1;
 
     // TODO: LIMPIAR LA BASURA
 
     /**
      * 
      */
     public ConectaDbImpl() throws DBCreationException {
     }
 
     /**
      * Metodo que abre la conexion a la Base de Datos. Utiliza los atributos
      * de la clase como datos para conectarse.
      * @throws DBCreationException 
      */
     private Connection cargarBase() throws DBCreationException {
         Connection connect;
 
         try {
             try {
                 Class.forName("com.mysql.jdbc.Driver").newInstance();
             } catch (InstantiationException ex) {
                 Logger.getLogger(ConectaDbImpl.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(ConectaDbImpl.class.getName()).log(Level.SEVERE, null, ex);
             }
             connect = DriverManager.getConnection(url, user, password);
 
 //            Class.forName(driver);
 //            connect = DriverManager.getConnection(url + dbName, user,                    password);
 
             if (connect == null) {
                 throw new DBCreationException("NOTHING TO SAY");
             }
         } catch (ClassNotFoundException ex) {
             throw new DBCreationException("Error cargando el Driver MySQL JDBC");
         } catch (SQLException ex) {
             String errorMsg = ex.getSQLState();
             throw new DBCreationException("Imposible realizar conexion con " + url + "\nSQL reporta:" + errorMsg);
         }
         return connect;
     }
 
     /**
      * Metodo que cierra la conexion a la base de datos-
      *
      * @param conecta Coneccion a la base de datos.
      * @param sentencia Statement para realizar consultas a la base de datos.
      */
     private static void cerrarBase(final Connection conecta,
             final Statement sentencia) throws DBCreationException {
         try {
             if (sentencia != null) {
                 sentencia.close();
             }
             if (conecta != null) {
                 conecta.close();
             }
         } catch (SQLException sqlex) {
             throw new DBCreationException();
         }
     }
 
     /**
      * Metodo que muestra proyectos autorizados dependiendo del tipo de usuario.
      * @param tipoUsuario Una de las 3 constantes definidas en la interfaz UsuarioRegistrado.
      * Cualquier otro valor, provocara una lista vacia.
      * @param idUsuario Identificador de un usuario en la base de datos.
      * @return Regresa una lista ligada de arreglos de Cadenas con el siguiente formato:<br>
      * { [id_del_proyecto1,nombre_del_proyecto1], ...}
      */
     @Override
     public LinkedList<String[]> verProyectosDb(
             final int tipoUsuario,
             final int idUsuario)
             throws DBConsultException, DBCreationException {
 
         LinkedList<String[]> listaDeProyectos = new LinkedList<String[]>();
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
             String query = "";
 
             switch (tipoUsuario) {
 
                 case TipoUsuario.ADMINISTRADOR_VALUE:
                     query = "SELECT nombre, id_p "
                             + "FROM  proyectos "
                             + "WHERE estado = 0";
                     resultset = statement.executeQuery(query);
                     break;
 
                 case TipoUsuario.RESPONSABLE_VALUE:
                     query = "SELECT nombre, id_p "
                             + "FROM  proyectos "
                             + "WHERE estado = 1 AND"
                             + " id_u = " + idUsuario;
                     resultset = statement.executeQuery(query);
                     break;
 
                 case TipoUsuario.ALUMNO_VALUE:
                     query = "SELECT nombre, id_p "
                             + "FROM  proyectos "
                             + "WHERE estado = 1";
                     resultset = statement.executeQuery(query);
                     break;
 
                 default:
                     System.out.println("Tipo de usuario no valido");
                     break;
             }
 
             listaDeProyectos = getIdxNombre(resultset);
         } catch (SQLException e) {
             throw new DBConsultException(e.getMessage() + "    " + e.getSQLState());
         } finally {
             cerrarBase(connect, statement);
         }
         return listaDeProyectos;
     }
 
     /**
      * Este método tiene codigo redundate con verProyectos. Corregir
      */
     @Override
     public LinkedList<String[]> verMisProyectosDb(final int tipoUsuario, final int idUsuario, boolean studentStatus)
             throws DBConsultException, DBCreationException {
 
         LinkedList<String[]> listaDeProyectos = new LinkedList<String[]>();
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
             String query = "";
 
             switch (tipoUsuario) {
                 case TipoUsuario.ADMINISTRADOR_VALUE:
                     query = "SELECT nombre, id_p FROM  proyectos WHERE estado = \"0\";";
                     resultset = statement.executeQuery(query);
                     break;
 
                 case TipoUsuario.RESPONSABLE_VALUE:
                     query = "SELECT nombre, id_p FROM  proyectos "
                             + "WHERE estado = \"0\" AND id_u =\"" + idUsuario + "\";";
                     resultset = statement.executeQuery(query);
                     break;
 
                 case TipoUsuario.ALUMNO_VALUE:
                     if (studentStatus) {
                         query = "SELECT DISTINCT nombre, postulados.id_p FROM proyectos, postulados WHERE postulados.estadoa = \"0\" AND postulados.id_u = " + idUsuario + " AND proyectos.id_p = postulados.id_p;";
                     } else {
                         query = "SELECT DISTINCT nombre, postulados.id_p FROM proyectos, postulados WHERE postulados.id_u = " + idUsuario + " AND proyectos.id_p = postulados.id_p;";
 
                     }
                     resultset = statement.executeQuery(query);
                     break;
             }
             listaDeProyectos = getIdxNombre(resultset);
         } catch (SQLException e) {
             throw new DBConsultException(e.getSQLState() + "  " + e.getMessage());
         } finally {
             cerrarBase(connect, statement);
         }
         return listaDeProyectos;
     }
 
     /**
      * Se puede agregar una columna tipo BLOB (binary large object b) a la
      * base de datos para guardar un Proyecto completo. De esta manera
      * podemos regresar un objeto Proyecto, sin hacer el codigo ilegible.
      * Solo hay que cuidar la integredida de la base de datos, a la hora
      * de actualizar el un proyecto. Sin embargo ningun caso de uso,
      * realiza modificaciones a los proyectos.
      */
     public ResultSet verDetallesProyectoDb(final int idProyecto)
             throws DBCreationException {
 
         String query = "SELECT * FROM proyectos NATURAL JOIN proyac "
                 + "NATURAL JOIN proycarr WHERE id_p = " + idProyecto;
 
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
             connect = cargarBase();
             statement = connect.createStatement();
             resultset = statement.executeQuery(query);
         } catch (SQLException e) {
             System.out.println(e.getMessage());
         }
 
         return resultset;
     }
 
     /**
      * Agrega un proyecto a la tabla Proyectos con un estado NO_AUTORIZADO
      * Tambien agrega entradas a las tablas proycarr y proyac.
      *
      * @param proy Proyecto que se quiere agregar a la base de datos.
      * @throws Se lanza una excepcion si ninguna fila en la base de datos es
      * afectada, es decir, no se pudo agregar el proyecto.
      */
     public void proponerProyectoDb(final Proyecto proy)
             throws DBCreationException {
 
         Connection connect = null;
         Statement statement = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             int id_u = proy.getResponsable();
             int telefono = proy.getTelefono();
             int maxParticipantes = proy.getMaxParticipantes();
             int[] id_ac = proy.getAreaConocimiento();
             int[] carreras = proy.getCarreras();
             String descripcion = proy.getDescripcion();
             int estado = NO_AUTORIZADO;
             String direccion = proy.getDireccion();
             String nombre = proy.getNombre();
             String email = proy.getEmail();
 
             String update = "";
 
             update = "INSERT INTO proyectos (id_u,nombre,email,telefono,direccion,"
                     + "maxParticipantes,descripcion,estado) VALUES ('"
                     + id_u + "','"
                     + nombre + "','"
                     + email + "','"
                     + telefono + "',\""
                     + direccion + "\",'"
                     + maxParticipantes + "',\""
                     + descripcion + "\",'"
                     + estado + "');";
 
             if (statement.executeUpdate(update, Statement.RETURN_GENERATED_KEYS) == 0) {
                 throw new DBCreationException("Cannot execute proyect insertionupdate.");
             } else {
                 int id_p = -1;
                 ResultSet rs = statement.getGeneratedKeys();
                 if (rs.next()) {
                     id_p = rs.getInt(1);
                 } else {
                     throw new DBCreationException("PIB, can't find most recent insert we just entered");
                 }
 
                 // Areas
                 update = "INSERT INTO proyac (id_p,id_ac) VALUES";
                 update += getPairValues(id_p, id_ac);
                 // Regresa el par de valores a agregar a la tabla proyac.
 
                 if (statement.executeUpdate(update) == 0) {
                     throw new DBCreationException("Dumb error.");
                 }
                 // Careers
                 update = "INSERT INTO proycarr (id_p,id_c) VALUES ";
                 update += getPairValues(id_p, carreras);
 
                 if (statement.executeUpdate(update) == 0) {
                     throw new DBCreationException("Cannot execute proyect-related update.");
                 }
             }
 
         } catch (SQLException sqlex) {
             throw new DBCreationException(sqlex.getMessage() + "   " + sqlex.getSQLState() + " id_u " + proy.getResponsable());
         } catch (NullPointerException nuly) {
             throw new DBCreationException(nuly.getMessage());
         } finally {
             cerrarBase(connect, statement);
         }
     }
 
     /**
      * El administrador autoriza un proyecto. Esto significa que la columna
      * "estado" en la tabla "proyectos" correspondiente al proyecto a autorizar
      * sera cambiado a 1.
      *
      * @param idProyecto id del proyecto que el administrador desea autorizar.
      * @throws Se lanza una excepcion si no se puede modificar el
      * estado del proyecto.
      */
     public void autorizarProyectoDb(final int idProyecto)
             throws DBModificationException, DBCreationException {
 
         String query = "UPDATE proyectos SET estado = " + AUTORIZADO
                 + " WHERE id_p = " + idProyecto;
 
         Connection connect = null;
         Statement statement = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             if (statement.executeUpdate(query) == 0) {
                 throw new DBModificationException();
             }
         } catch (SQLException sqlex) {
             throw new DBModificationException(sqlex.getMessage() + "   " + sqlex.getSQLState());
 
         } finally {
             cerrarBase(connect, statement);
         }
     }
 
     /**
      * El responsable rechaza (cambia su estado a rechazado) un alumno de su proycto.
      *
      * @param idProyecto id del proyecto del cual se quiere rechazar un alumno.
      * @param idAlumno id del alumno que se va a rechazar.
      * @throws Se lanza una excepcion si no se puede eliminar al alumno del proyecto.
      */
     public void rechazaAlumnoProyectoDb(final int idProyecto, final int idAlumno)
             throws DBModificationException, DBCreationException {
 
         String query = "UPDATE postulados SET estador = 'rechazado' " +
         		" WHERE id_p = " + idProyecto + 
         		" AND id_u = " + idAlumno + ";";     
         
         
         Connection connect = null;
         Statement statement = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             if (statement.executeUpdate(query) == 0) {
                 throw new DBModificationException("Cannot turn back. " + idProyecto + " alum:" + idAlumno);
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             throw new DBModificationException(e.getMessage() + "   " + e.getSQLState());
         }
     }
 
     /**
      * El administrador rechaza un proyecto y es eliminado de la db.
      *
      * @param idProyecto id del proyecto que se eliminara de la base de datos.
      * @throws Se lanza una excepcion si no se pudo elimiar el proyecto.
      */
     public void rechazarProyectoDb(final int idProyecto)
             throws DBModificationException, DBCreationException {
 
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
             String query = "DELETE FROM proyac WHERE id_p = " + idProyecto;
             statement.executeUpdate(query);
 
             query = "DELETE FROM proycarr WHERE id_p = " + idProyecto;
             statement.executeUpdate(query);
 
             query = "DELETE FROM proyectos WHERE id_p = " + idProyecto;
 
             if (statement.executeUpdate(query) == 0) {
                 throw new DBModificationException("Proyect not found. " + idProyecto);
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             throw new DBModificationException(e.getMessage() + "   " + e.getSQLState());
         }
     }
 
     /**
      * El administrador rechaza un responsable, por lo cual es eliminado de la
      * base de datos.
      *
      * @param idResponsable id del responsable que se quiere eliminar.
      * @throws Se lanza una excepcion si no se puede eliminar al responsable.
      */
     public void rechazaResponsableDb(final int idResponsable)
             throws DBCreationException, DBModificationException {
 
         String query = "DELETE FROM responsables WHERE id_u  = "
                 + idResponsable;
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             if ((statement.executeUpdate(query)) == 0) {
                 throw new DBModificationException("Guy not found. " + idResponsable);
             }
         } catch (SQLException sqlex) {
             throw new DBModificationException(sqlex.getMessage() + "   " + sqlex.getSQLState());
         }
     }
 
     /**
      * Agrega un alumno a la lista (tabla) de postulados de un proyecto.
      * Los estados, estadoa (administrador) y estadr (responsable), de un alumno
      * postulado se inicializan en NO_AUOT = 0;
      *
      * @param idProyecto id del proyecto al cual se quiere postular un alumno.
      * @param idAlumno id del alumno que se quiere postular a un proyecto.
      * @throws Se lanza un excepcion si no se puede agregar al usuario
      * a la lista de postulados.
      */
     public void postularAProyectoDb(final int idProyecto, final int idAlumno)
             throws DBCreationException, DBModificationException {
 
         String query = "INSERT INTO postulados (id_p, id_u, estadoa, estador) " +
         		" VALUES ( " + idProyecto + "," + idAlumno + ",'pendiente','pendiente');";
 
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             if (statement.executeUpdate(query) == 0) {
                 throw new DBModificationException();
             }
         } catch (SQLException e) {
             throw new DBModificationException(e.getMessage() + "   " + e.getSQLState());
         }
     }
 
     /**
      * Regresa una lista de alumnos postulados a un proyecto.
      *
      * @param idProyecto id del proyecto del cual se quiere cononcer
      * la lista de postulados.
      * @return Regresa una lista ligada cuyos elemenos son el par ordenado
      * de id del usuario y su nombre.
      * @throws Lanza una excepcion
      */
     public LinkedList<String[]> verPostuladosDb(final int idProyecto)
             throws DBCreationException, DBConsultException {
 
         String query = "SELECT postulados.id_u,usuarios.nombre"
                 + " FROM  postulados, usuarios"
                 + " WHERE postulados.id_p = " + idProyecto
                 + " AND postulados.id_u = usuarios.id_u";
 
         LinkedList<String[]> listaPos = new LinkedList<String[]>();
 
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             resultset = statement.executeQuery(query);
             listaPos = getIdxNombre(resultset);
 
         } catch (SQLException e) {
             throw new DBConsultException(e.getMessage() + "    " + e.getSQLState());
         } finally {
             cerrarBase(connect, statement);
         }
         return listaPos;
     }
 
     /**
      * El reaponsable de un proyecto acepta un alumno en su proyecto. Por lo cual
      * se cambia el contenido de la columna "estador" a "aceptado" donde el id del usuario
      * y el id de proyecto coicidan con los pasados como parametros.
      *
      * @param idProyecto id del proyecto en el cual se aceptara al alumno.
      * @param idAlumno id el alumno al cual se acepto en el proyecto.
      * @throws Se lanza una excepcion si no se puede actualizar el estado del
      * alumno en la lista (tabla) de ponstulados.
      */
     public void aceptarAlumnoProyectoDb(final int idProyecto, final int idAlumno)
             throws DBCreationException, DBModificationException {
 
         String query = "UPDATE postulados " + "SET estador = 'aceptado' "
                 + "WHERE id_u = " + idAlumno
                 + " AND id_p = " + idProyecto;
 
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             if (statement.executeUpdate(query) == 0) {
                 throw new DBModificationException("Malformed update. Are you sure student " + idAlumno + " wants to be in " + idProyecto + "?");
             }
             if (estadoA(idAlumno) == 1) {
                 borrarPostulaciones(idAlumno, idProyecto);
             }
         } catch (SQLException e) {
             throw new DBModificationException(e.getMessage() + "   " + e.getSQLState());
         }
     }
 
     /**
      * El administrador autoriza a un alumno en un proyecto. Por lo cual
      * se cambia el contenido de la columna "estadoa" a aceptado donde el id del usuario
      * y el id de proyecto coicidan con los pasados como parametros.
      *
      * @param idProyecto id del proyecto en el cual se autorizara al alumno.
      * @param idAlumno id el alumno al cual se autorizara en el proyecto.
      * @throws Se lanza una excepcion si no se puede actualizar el estado del
      * alumno en la lista (tabla) de ponstulados.
      */
     public void autorizarAlumnoProyecto(final int idAlumno, final int idProyecto)
             throws DBCreationException, DBModificationException {
 
         String query = "UPDATE postulados  SET estadoa = 'aceptado' " 
                 + "WHERE id_u = " + idAlumno
                 + " AND id_p = " + idProyecto;
 
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             if (statement.executeUpdate(query) == 0) {
                 throw new DBModificationException("Malformed update. Are you sure student " + idAlumno + " wants to be in " + idProyecto + "?");
             }
             if (estadoR(idAlumno) == 1) {
                 borrarPostulaciones(idAlumno, idProyecto);
             }
         } catch (SQLException e) {
             throw new DBModificationException(e.getMessage() + "   " + e.getSQLState());
         }
     }
 
     /**
      * En este punto, tanto el administrador como el responsable
      * han autorizado y aceptado respectivamente a un alumno. De aqui
      * que se proceda a borrar el todas las postulaciones del alumno
      * excepto en este proyecto cuyo id se pasa como parametro.
      */
     private void borrarPostulaciones(final int idAlumno, final int idProyecto)
             throws DBCreationException, DBModificationException, SQLException {
 
         Connection connect = null;
         Statement statement = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             String query = "DELETE postulados WHERE id_u = " + idAlumno
                     + " AND id_p != " + idProyecto;
             if (statement.executeUpdate(query) == 0) {
                 throw new DBModificationException();
             }
         } catch (Exception e) {
         }
     }
 
     /**
      * Agrega un nuevo usuario y un nuevo responsalbe a a la base de datos.
      *
      * @param repo un objeto Responsble.
      * @throws Se lanza una excepcion si no se puede agregar al responsable.
      */
     public void registrarDb(final Responsable repo)
             throws DBCreationException, DBConsultException {
 
         int id_u = -1;
 
         Connection connect = null;
         Statement statement = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
             String update = insertaUsuario(repo);
 
             if (statement.executeUpdate(update, statement.RETURN_GENERATED_KEYS) == 0) {
                 throw new DBCreationException("No se pudo insertar al usuario:" + repo.getUsername() + "en fase 1.");
 
             } else {
 
                 ResultSet rs = statement.getGeneratedKeys();
                 if (rs.next()) {
                     id_u = rs.getInt(1);
                 } else {
                     throw new DBConsultException("PIB, can't find most recent insert we just entered");
                 }
 
                 update = insertaResponsable(repo, id_u);
 
                 if (statement.executeUpdate(update) == 0) {
                     throw new DBCreationException("No se pudo insertar al usuario:" + repo.getUsername() + "en fase 2.");
                 }
             }
         } catch (SQLException sqlex) {
             throw new DBCreationException(sqlex.getMessage() + "  " + sqlex.getSQLState());
         } finally {
             cerrarBase(connect, statement);
         }
     }
 
     /**
      * El adminstrador acepta un responsable. Es decir, cambia el valor de la
      * columna estado en al tabla responsable a 1 (Autorizado).
      *
      * @param idResponsable id del responsable que sera aceptado.
      * @throws DBCreationException, Se lanza un excepcion si no se puede cambiar el estado del
      * responsable.
      */
     public void aceptarResponsableDb(final int idResponsable)
             throws DBCreationException, DBModificationException {
 
         String query = "UPDATE responsables SET estado = 1"
                 + " WHERE id_u = " + idResponsable;
 
         Connection connect = null;
         Statement statement = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
             if (statement.executeUpdate(query) == 0) {
                 throw new DBModificationException();
             }
         } catch (SQLException sqlex) {
             throw new DBModificationException(sqlex.getMessage() + "   " + sqlex.getSQLState());
         } finally {
             cerrarBase(connect, statement);
         }
     }
 
     /**
      * regresa arreglo de tres enteros 
      * |tipoUsu|id_u|estado| de no existir tipo Usu = "-1"
      *
      */
     public ResultSet validaUsuarioDb(final String nombreUsuario,
             final String passwd) throws DBCreationException {
         String query = "SELECT * FROM usuarios "
                 + "WHERE username = '" + nombreUsuario + "' "
                 + "AND contrasena = '" + passwd + "';";
 
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             resultset = statement.executeQuery(query);
 
         } catch (SQLException sqlex) {
             System.out.println(sqlex.getMessage());
             throw new DBCreationException(sqlex.getMessage() + "    " + sqlex.getSQLState());
 
         }
         return resultset;
     }
 
     public void modificarEstadoAlumno(final int idAlumno, final boolean estadoAlumno)
             throws DBCreationException, DBModificationException {
 
         int estado = (estadoAlumno) ? AUTORIZADO : NO_AUTORIZADO;
 
         String query = "UPDATE alumnos SET estado =  " + estado
                 + " WHERE id_u = " + idAlumno;
 
         Connection connect = null;
         Statement statement = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             if (statement.executeUpdate(query) == 0) {
                 throw new DBModificationException();
             }
         } catch (SQLException e) {
             throw new DBModificationException(e.getMessage() + "    " + e.getSQLState());
         } finally {
             cerrarBase(connect, statement);
         }
     }
 
     /**
      *
      */
     private String getPairValues(final int id, final int[] dataArray) {
 
         String coma = ",";
         String result = "";
         for (int i = 0; i < dataArray.length; i++) {
             if ((i + 1) == dataArray.length) {
                 coma = "";
             }
             result += "(" + id + "," + dataArray[i] + ")" + coma;
         }
 
         return result;
     }
 
     private String insertaUsuario(final UsuarioRegistrado usuario) {
         String nombreUsuario = usuario.getUsername();
         String contrasena = usuario.getContraseña();
         int tipo = usuario.getTipo();
         String nombre = usuario.getNombre();
         String telefono = usuario.getTelefono();
         String email = usuario.getEmail();
         int activado = NO_AUTORIZADO;
         String update = "INSERT INTO usuarios (username,contrasena,tipo,nombre,"
                 + "telefono,email,activado) VALUES ("
                 + "'" + nombreUsuario + "','" + contrasena + "',"
                 + tipo + ",'" + nombre + "',"
                 + telefono + ",'" + email + "',"
                 + activado + ")";
 
         return update;
     }
 
     private String insertaResponsable(final Responsable resp,
             final int idUsuario) {
         String descripcion = resp.getDescripcion();
         String sitioweb = resp.getSitioweb();
         int estado = NO_AUTORIZADO;
 
         String update = "INSERT INTO responsables (id_u,descripcion,sitioweb,"
                 + "estado) VALUES ("
                 + idUsuario + ",'" + descripcion + "','"
                 + sitioweb + "'," + estado + ")";
         return update;
     }
 
     /**
      * Este metodo se utiliza para conseguir una lista de arreglos,
      * cada uno con dos entradas. Cada uno de estos arreglos,
      * tienen como primer elemento un entero que representa un id y una
      * cadena que representa, generalmente, un nombre.
      * @param rs
      * @return Lista ligada de arreglos de cadenas de tamaño 2.
      * @throws SQLException
      */
     private LinkedList<String[]> getIdxNombre(final ResultSet rs) throws SQLException {
 
         LinkedList<String[]> lista = new LinkedList<String[]>();
         String[] idxnombre = null;
 
         while (rs.next()) {
             idxnombre = new String[2];
             idxnombre[0] = rs.getString(1);
             idxnombre[1] = rs.getString(2);
             lista.add(idxnombre);
         }
 
         return lista;
     }
 
     private int estadoA(final int idAlumno) throws DBCreationException {
         return estadoAR(idAlumno, "estadoa");
     }
 
     private int estadoR(final int idAlumno) throws DBCreationException {
         return estadoAR(idAlumno, "estador");
     }
 
     private int estadoAR(final int idAlumno, final String AR)
             throws DBCreationException {
         String query = "SELECT postulados (" + AR + ") "
                 + "WHERE id_u=" + idAlumno + ")";
         int estado = -1;
 
         ResultSet resultset = null;
         Connection connect = null;
         Statement statement = null;
 
         try {
             statement = connect.createStatement();
             resultset = statement.executeQuery(query);
             estado = resultset.getInt(1);
         } catch (SQLException e) {
             System.out.println(e.getMessage());
         }
         return estado;
 
     }
 
     @Override
     public ResultSet fetchUserInfo(int userId, int userTIpo)
             throws DBConsultException, DBCreationException {
         ResultSet res = null;
         String query = "";
         Connection connect = null;
         Statement statement = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
             switch (userTIpo) {
                 case 0:
                     query = "Select *, nombre as nombreUsuario FROM usuarios WHERE id_u = " + userId;
                     res = statement.executeQuery(query);
                     break;
                 case 1:
                     query = "SELECT *, nombre as nombreUsuario FROM usuarios NATURAL JOIN responsables WHERE id_u = " + userId;
                     res = statement.executeQuery(query);
                     break;
                 case 2:
                     query = "SELECT *, c.nombre AS nombreCarrera, u.nombre AS nombreUsuario FROM usuarios u NATURAL JOIN alumnos, carreras c  WHERE id_u = " + userId + " AND id_c = carrera";
                     res = statement.executeQuery(query);
                     break;
                 default:
                     DBConsultException cons = new DBConsultException("User type not supported.");
                     cons.setErrorCode(8);
                     throw cons;
             }
         } catch (SQLException ex) {
             DBConsultException cons = new DBConsultException(ex.getMessage() + "," + ex.getSQLState());
             cons.setErrorCode(0);
             throw cons;
         }
         return res;
     }
 
     @Override
     public int getUserType(int userID)
             throws DBConsultException, DBCreationException {
 
         ResultSet res = null;
         String query;
         String goal = "";
 
         Connection connect = null;
         Statement statement = null;
 
         try {
             connect = cargarBase();
             statement = connect.createStatement();
             query = "SELECT * FROM usuarios WHERE id_u = " + userID + ";";
             res = statement.executeQuery(query);
             res.next();
             goal = res.getString("tipo");
             goal = goal.toUpperCase();
         } catch (SQLException ex) {
             DBConsultException cons = new DBConsultException(ex.getMessage() + "," + ex.getSQLState());
             cons.setErrorCode(0);
             throw cons;
         } finally {
             cerrarBase(connect, statement);
         }
 
         return TipoUsuario.valueOf(goal).getValue();
     }
 
     @Override
     public String[][] pendingResp() throws DBCreationException {
         ResultSet res = null;
         String[][] foo = null;
         LinkedList<String> tmp = new LinkedList();
         String query = "SELECT * FROM responsables NATURAL JOIN usuarios WHERE estado = \"0\";";
 
         Connection connect = null;
         Statement statement = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
             res = statement.executeQuery(query);
 
             while (res.next()) {
                 tmp.add(res.getString("id_u"));
                 tmp.add(res.getString("nombre"));
             }
             foo = new String[tmp.size()/2][2];
             int index = 0;
             while (!tmp.isEmpty()) {
                 foo[index][0] = tmp.getFirst();
                 tmp.removeFirst();
                 foo[index][1] = tmp.getFirst();
                 tmp.removeFirst();
                 index++;
             }
 
         } catch (SQLException ex) {
             Logger.getLogger(ConectaDbImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
         return foo;
     }
 
     @Override
     public String[][] pendingAlum() throws DBCreationException {
         ResultSet res = null;
         String[][] foo = null;
         LinkedList<String> tmp = new LinkedList();
         String query = "SELECT id_u, nombre, id_p FROM usuarios NATURAL JOIN postulados WHERE estador <> '1' ORDER BY id_p;";
 
         Connection connect = null;
         Statement statement = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
             res = statement.executeQuery(query);
 
             while (res.next()) {
                 tmp.add(res.getString("id_u"));
                 tmp.add(res.getString("nombre"));
             }
             foo = new String[tmp.size()/2][2];
             int index = 0;
             while (!tmp.isEmpty()) {
                 foo[index][0] = tmp.getFirst();
                 tmp.removeFirst();
                 foo[index][1] = tmp.getFirst();
                 tmp.removeFirst();
                 index++;
             }
 
         } catch (SQLException ex) {
             Logger.getLogger(ConectaDbImpl.class.getName()).log(Level.SEVERE, null, ex);
             throw new DBCreationException(ex.getMessage() + "   " + ex.getSQLState());
         }
         return foo;
     }
 
     /**
      * Creates a new instance of the specified Ecore class.
      *
      * @param eClass The Ecore class of the instance to create.     
      * @return The new instance.
      * @generated
      */
 //    protected EObject create(EClass eClass) {
 //        return EcoreUtil.create(eClass);
 //    }
 
     public String[][] fetchAC() throws DBConsultException, DBCreationException {
         String[][] res = null;
         Connection connect = null;
         Statement statement = null;
         ResultSet rs = null;
         try {
             connect = cargarBase();
             statement = connect.createStatement();
             String query = "select * from areasconocimiento;";
             rs = statement.executeQuery(query);
             res = new String[rs.getFetchSize()][2];
             int index = 0;
             while (rs.next()) {
                 res[index][0] = rs.getString("id_ac");
                 res[index][1] = rs.getString("nombre");
                 index++;
             }
         } catch (SQLException ex) {
             Logger.getLogger(ConectaDbImpl.class.getName()).log(Level.SEVERE, null, ex);
             throw new DBCreationException(ex.getMessage() + "   " + ex.getSQLState());
         }
         return res;
     }
 
     public String[][] fetchCAR() throws DBConsultException, DBCreationException {
         String[][] res = null;
         Connection connect = null;
         Statement statement = null;
         ResultSet rs = null;
         try {
             connect = cargarBase();
             statement = connect.createStatement();
             String query = "select * from carreras;";
             rs = statement.executeQuery(query);
             res = new String[rs.getFetchSize()][2];
             int index = 0;
             while (rs.next()) {
                 res[index][0] = rs.getString("id_c");
                 res[index][1] = rs.getString("nombre");
                 index++;
             }
         } catch (SQLException ex) {
             Logger.getLogger(ConectaDbImpl.class.getName()).log(Level.SEVERE, null, ex);
             throw new DBCreationException(ex.getMessage() + "   " + ex.getSQLState());
         }
         return res;
     }
 
     public String[] translateAC(int[] array) throws DBConsultException, DBCreationException {
         String[] resultArray = new String[array.length];
         Connection connect = null;
         Statement statement = null;
         ResultSet rs = null;
         try {
             connect = cargarBase();
             statement = connect.createStatement();
             String query = "select * from areasconocimiento;";
             rs = statement.executeQuery(query);
             int index = 0;
             LinkedList<Integer> tmp = new LinkedList<Integer>();
             for (int i = 0; i < array.length; i++) {
                 tmp.add(new Integer(array[i]));
             }
             while (rs.next()) {
                 if (tmp.contains(new Integer(rs.getInt("id_ac")))) {
                     resultArray[index] = rs.getString("nombre");
                     index++;
                 }
             }
         } catch (SQLException ex) {
             Logger.getLogger(ConectaDbImpl.class.getName()).log(Level.SEVERE, null, ex);
             throw new DBCreationException(ex.getMessage() + "   " + ex.getSQLState());
         }
         return resultArray;
     }
 
     public String[] translateCAR(int[] array) throws DBConsultException, DBCreationException {
         String[] resultArray = new String[array.length];
         Connection connect = null;
         Statement statement = null;
         ResultSet rs = null;
         try {
             connect = cargarBase();
             statement = connect.createStatement();
             String query = "select * from carreras;";
             rs = statement.executeQuery(query);
             int index = 0;
             LinkedList<Integer> tmp = new LinkedList<Integer>();
             for (int i = 0; i < array.length; i++) {
                 tmp.add(new Integer(array[i]));
             }
             while (rs.next()) {
                 if (tmp.contains(new Integer(rs.getInt("id_c")))) {
                     resultArray[index] = rs.getString("nombre");
                     index++;
                 }
             }
         } catch (SQLException ex) {
             Logger.getLogger(ConectaDbImpl.class.getName()).log(Level.SEVERE, null, ex);
             throw new DBCreationException(ex.getMessage() + "   " + ex.getSQLState());
         }
         return resultArray;
     }
     
  
     /**
      * Nuevos metodos 
      */
     
     /**
      * Este metodo se utiliza para conseguir una lista de arreglos,
      * cada uno con dos entradas. Cada uno de estos arreglos,
      * tienen como primer elemento una cadena  que representa el
      *  nombre (area de conocimiento,carrera o proyecto ) y entero
      * que representa el numero de elementos.
      * @param rs
      * @return Lista ligada de arreglos de cadenas de tamaño 2.
      * @throws SQLException
      */
     private LinkedList<String[]> getNombrexNum(final ResultSet rs) throws SQLException {
 
         LinkedList<String[]> lista = new LinkedList<String[]>();
         String[] nombrexnum = null;
 
         while (rs.next()) {
             nombrexnum = new String[2];
             nombrexnum[0] = rs.getString(1);
             nombrexnum[1] = rs.getString(2);
             lista.add(nombrexnum);
         }
 
         return lista;
 }
     
     /**
      * El Alumno se despostula de un proycto (se elimina de db) .
      * @param idProyecto id del proyecto del cual se quiere despostular un alumno.
      * @param idAlumno id del alumno que se va a despostular.
      * @throws Se lanza una excepcion si no se puede eliminar al alumno del proyecto.
      */
     public void despostularAlumnoProyectoDb(final int idProyecto, final int idAlumno)
             throws DBModificationException, DBCreationException {
 
         String query = "DELETE FROM postulados WHERE id_p = " + idProyecto
                 + " AND id_u = " + idAlumno;
 
         Connection connect = null;
         Statement statement = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             if (statement.executeUpdate(query) == 0) {
                 throw new DBModificationException("No se puedo despostular al alumno");
             }
         } catch (SQLException e) {
             throw new DBModificationException(e.getMessage() + "   " + e.getSQLState());
         }
     }
       
     /**
      * Regresa una lista con  el numero de proyectos por areas  de conocimiento.
      *areaNumProy
     * @return Regresa una lista ligada cuyos elemenos son el par ordenado
      * del nombre del Area C. y  el numero de proyectos en esta.
      * @throws Lanza una excepcion
      */
     public LinkedList<String[]> proyectosPorAreaDb () throws DBCreationException, DBConsultException {
 
         String query = "select areasconocimiento.nombre, count(proyac.id_p) "+
                 "as NumProyectos from areasconocimiento left join proyac "+
                 "on areasconocimiento.id_ac = proyac.id_ac "+
                 "group by areasconocimiento.nombre";
 
         LinkedList<String[]> listaAreas = new LinkedList<String[]>();
 
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             resultset = statement.executeQuery(query);
             listaAreas = getNombrexNum(resultset);
 
         } catch (SQLException e) {
             throw new DBConsultException(e.getMessage() + "    " + e.getSQLState());
         } finally {
             cerrarBase(connect, statement);
         }
         return listaAreas ;
     }
     
     /**
      * Regresa una lista con  el numero de proyectos por carreras.
      * carrerasNumAlumnosDb carrerasNumProyDb
      * @return Regresa una lista ligada cuyos elemenos son el par ordenado
      * de nombre del carrera y  el numero de proyectos en esta.
      * @throws Lanza una excepcion
      */  
     public LinkedList<String[]> proyectosPorCarrerasDb () 
             throws DBCreationException, DBConsultException {
 
         String query = "select carreras.nombre, count(proycarr.id_p) as NumProyectos " +
                 "from carreras left join proycarr " +
                 "on carreras.id_c = proycarr.id_c " +
                 "group by carreras.nombre";
 
         LinkedList<String[]> listaCarreras = new LinkedList<String[]>();
 
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             resultset = statement.executeQuery(query);
             listaCarreras = getNombrexNum(resultset);
 
         } catch (SQLException e) {
             throw new DBConsultException(e.getMessage() + "    " + e.getSQLState());
         } finally {
             cerrarBase(connect, statement);
         }
         return listaCarreras ;
     }
     
     /**
      * Regresa una lista con  el numero de alumnos por carreras.
      *
      * @param null
      * @return Regresa una lista ligada cuyos elemenos son el par ordenado
      * del nombre del carrera y  el numero de alumnos en esta.
      * @throws Lanza una excepcion
      */
     public LinkedList<String[]> alumnosPorCarreraDb () throws DBCreationException, DBConsultException {
 
         String query = "select carreras.nombre, count(alumnos.id_u) as NumAlumnos " +
                 "from carreras left join alumnos " +
                 "on carreras.id_c = alumnos.carrera " +
                 "group by carreras.nombre;";
 
         LinkedList<String[]> listaAlumnos = new LinkedList<String[]>();
 
         Connection connect = null;
         Statement statement = null;
         ResultSet resultset = null;
 
         try {
 
             connect = cargarBase();
             statement = connect.createStatement();
 
             resultset = statement.executeQuery(query);
             listaAlumnos = getNombrexNum(resultset);
 
         } catch (SQLException e) {
             throw new DBConsultException(e.getMessage() + "    " + e.getSQLState());
         } finally {
             cerrarBase(connect, statement);
         }
         return listaAlumnos ;
     }
     
     /**
      * Regresa una lista de arreglas de cadenas.
      * Cada arreglo de cadenas contienes los siguientes datos en ese orden:
      * nombre_del_proyecto, num_aceptados, num_rechazados, num_pendientes.
      * @return Regresa una lista ligada cuyos elemenos son arraglos de cadenas
      * que 
      * @throws Lanza una excepcion
     */
     public LinkedList<String[]> estadosAlumnosProyDb () 
             throws DBCreationException, DBConsultException {		
 			
         String query = "select nombre, count(estador) as estado " +
                 "from proyectos left join postulados "+
                 "on proyectos.id_p = postulados.id_p and estador = ?" + 
                 "group by nombre";
 
         LinkedList<String[]> listaProy = new LinkedList<String[]>();
         LinkedList<String> nproy = new LinkedList<String>();
         LinkedList<String> aceptados = new LinkedList<String>();
         LinkedList<String> rechazados = new LinkedList<String>();
         LinkedList<String> pendientes = new LinkedList<String>();
         String[] proy = null;
         ResultSet rs = null;
 
         Connection connect = null;
         PreparedStatement statement = null;
         
         try {
 
             connect = cargarBase();
             statement = connect.prepareStatement(query);
             
             statement.setString(1, "aceptado");
             rs = statement.executeQuery();
             
             while (rs.next()){
                 nproy.add(rs.getString("nombre"));
                 aceptados.add(rs.getString("estado"));
             }
             
             statement.setString(1,"rechazado");
             rs = statement.executeQuery();
             
             while (rs.next()){
                 rechazados.add(rs.getString("estado"));
             }
             
             statement.setString(1,"pendiente");
             rs = statement.executeQuery(); 
             
             while (rs.next()){
                 pendientes.add(rs.getString("estado"));
             }
             
             
             for (int i = 0; i< nproy.size(); i++){
                 proy = new String[4];
                 proy[0] = nproy.get(i);
                 proy[1] = aceptados.get(i);
                 proy[2] = rechazados.get(i);
                 proy[3] = pendientes.get(i);
                 listaProy.add(proy);
             }
             
 
         } catch (SQLException e) {
             throw new DBConsultException(e.getMessage() + "    " + e.getSQLState());
         } finally {
             cerrarBase(connect, statement);
         }
         return listaProy ;
     }
     
     // TODO: Eliminar esto para la version final.
     public static void main(String[] args){
         try{
         ConectaDbImpl d = new ConectaDbImpl();
         System.out.println(toStringLinkedList(d.estadosAlumnosProyDb()));
         }catch(Exception e){
             System.out.println(e.getMessage());
         }
         
     }
     
     private static String toStringLinkedList(LinkedList<String[]> lista){
         String result = "";
         String[] elem = null;
         for (int i = 0; i< lista.size(); i++){
             elem = lista.get(i);
             for (int j = 0; j<elem.length; j++){
                 result += elem[j] + " ";
             }
             result += "\n";
         }
         return  result;
     }  
     
 } // ConectaDbImpl

 package DBMS;
 
 import Clases.Carrera;
 import Clases.Coordinacion;
 import Forms.CreateUserForm;
 import Clases.Decanato;
 import Forms.EliminarUserForm;
 import Clases.NucleoUniversitario;
 import Clases.Profesor;
 import Clases.Usuario;
 import Sistemas.CCT;
 import Sistemas.DACE;
 import Sistemas.SINAI;
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Langtech
  */
 public class DBMS {
 
     static private Connection conexion;
 
     protected DBMS() {
     }
     static private DBMS instance = null;
 
     static public DBMS getInstance() {
         if (null == DBMS.instance) {
             DBMS.instance = new DBMS();
         }
         conectar();
         return DBMS.instance;
     }
 
     public static boolean conectar() {
         try {
             Class.forName("org.postgresql.Driver");
             conexion = DriverManager.getConnection(
                     "jdbc:postgresql://localhost:5432/evalprof",
                     "langtech",
                     "evalprof");
             return true;
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }
         return false;
     }
 
     /* Esta funcion se utiliza cuando un usuario inicia sesion
      * para verificar que su nombre y contrasena sean correctos
      * 
      */
     public Usuario verificarCas(Usuario u) {
 
         PreparedStatement psConsultar = null;
         try {
 
             psConsultar = conexion.prepareStatement("SELECT * FROM usuario"
                     + " WHERE usbid = ? AND contrasena = ?");
 
             psConsultar.setString(1, u.getUsbid());
             psConsultar.setString(2, u.getContrasena());
             System.out.println(psConsultar.toString());
             ResultSet rs = psConsultar.executeQuery();
 
             while (rs.next()) {
                 u.setUsbid(rs.getString("usbid"));
                 u.setTipousuario(rs.getString("tipousuario"));
 
                 return u;
             }
 
         } catch (SQLException ex) {
             Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
         }
         u.setUsbid(null);
 
         return u;
     }
 
     public boolean agregarUsuario(CreateUserForm u) {
 
         PreparedStatement psAgregar1 = null;
         PreparedStatement psAgregar2 = null;
 
         try {
             psAgregar1 = conexion.prepareStatement("INSERT INTO usuario(usbid, contrasena) VALUES (?, ?);");
             psAgregar1.setString(1, u.getUsbid());
             psAgregar1.setString(2, u.getContrasena1());
             System.out.println(psAgregar1.toString());
             Integer i = psAgregar1.executeUpdate();
 
             psAgregar2 = conexion.prepareStatement("INSERT INTO profesor(usbid,email) VALUES (?, ?);");
             psAgregar2.setString(1, u.getUsbid());
             psAgregar2.setString(2, u.getUsbid() + "@usb.ve");
             Integer j = psAgregar2.executeUpdate();
 
             return i > 0 && j > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public ArrayList<Usuario> listarUsuarios() {
 
         ArrayList<Usuario> usrs = new ArrayList<Usuario>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM usuario ORDER BY usbid");
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Usuario u = new Usuario();
                 u.setUsbid(rs.getString("usbid"));
                 u.setTipousuario(rs.getString("tipousuario"));
                 u.setDepartamento(rs.getString("departamento"));
                 usrs.add(u);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return usrs;
     }
 
     public ArrayList[] mostrarUsuarios() {
 
         ArrayList[] total = new ArrayList[4];
         ArrayList<Usuario> profesores = new ArrayList<Usuario>(0);
         ArrayList<Usuario> decanatos = new ArrayList<Usuario>(0);
         ArrayList<Usuario> departamentos = new ArrayList<Usuario>(0);
         ArrayList<Usuario> coordinaciones = new ArrayList<Usuario>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM usuario ORDER BY usbid");
             ResultSet rs = ps.executeQuery();
             String tipousuario = null;
             while (rs.next()) {
                 Usuario u = new Usuario();
                 u.setUsbid(rs.getString("usbid"));
                 u.setDepartamento(rs.getString("departamento"));
                 tipousuario = rs.getString("tipousuario");
                 if (tipousuario.equals("profesor")){
                     profesores.add(u);
                 } else if (tipousuario.equals("decanato")) {
                     decanatos.add(u);
                 } else if (tipousuario.equals("departamento")) {
                     departamentos.add(u);
                 } else if (tipousuario.equals("coordinacion")) {
                     coordinaciones.add(u);
                 }
             }
             total[0] = profesores;
             total[1] = decanatos;
             total[2] = departamentos;
             total[3] = coordinaciones;
             
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return total;
     }
     
     public ArrayList<Usuario> listarProfesores() {
 
         ArrayList<Usuario> usrs = new ArrayList<Usuario>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM usuario WHERE TIPOUSUARIO = 'profesor' ORDER BY usbid");
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Usuario u = new Usuario();
                 u.setUsbid(rs.getString("usbid"));
                 u.setTipousuario(rs.getString("tipousuario"));
                 u.setDepartamento(rs.getString("departamento"));
                 usrs.add(u);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return usrs;
     }
 
     public ArrayList<Carrera> listarCarreras() {
 
         ArrayList<Carrera> carreras = new ArrayList<Carrera>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM carrera ORDER BY codigo");
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Carrera u = new Carrera();
                 u.setCodigo(rs.getString("codigo"));
                 u.setNombre(rs.getString("nombre"));
                 u.setEstado(rs.getString("Estado"));
                 carreras.add(u);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return carreras;
     }
     
     public ArrayList<Carrera> [] listarCarrerasDecanato(String d) {
     
         ArrayList<Carrera> [] carreras = new ArrayList[2];
         ArrayList<Carrera> visibles = new ArrayList(0);
         ArrayList<Carrera> ocultas = new ArrayList(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT codigo, nombre, estado FROM incluye, CARRERA "
                     + "WHERE codigo_decanato = ? AND codigo_carrera = codigo "
                     + "ORDER BY codigo_carrera");
             ps.setString(1,d);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Carrera u = new Carrera();
                 u.setCodigo(rs.getString("codigo"));
                 u.setNombre(rs.getString("nombre"));
                 u.setEstado(rs.getString("Estado"));
                 if (u.getEstado().equals("visible")) {
                     visibles.add(u);
                 } else {
                     ocultas.add(u);
                 }
             }
             carreras[0] = visibles;
             carreras[1] = ocultas;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return carreras;
     }
 
     public ArrayList<Decanato> [] listarDecanatos() {
 
         ArrayList<Decanato> [] dcnto = new ArrayList[2];
         ArrayList<Decanato> visibles = new ArrayList(0);
         ArrayList<Decanato> ocultos = new ArrayList(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM decanato ORDER BY codigo");
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Decanato u = new Decanato();
                 u.setCodigo(rs.getString("codigo"));
                 u.setNombre(rs.getString("nombre"));
                 u.setEstado(rs.getString("Estado"));
                 if (u.getEstado().equals("visible")) {
                     visibles.add(u);
                 } else {
                     ocultos.add(u);
                 }
             }
             dcnto[0] = visibles;
             dcnto[1] = ocultos;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return dcnto;
     }
 
     public ArrayList<Coordinacion> [] listarCoordinaciones() {
 
         ArrayList<Coordinacion> [] coords = new ArrayList[2];
         ArrayList<Coordinacion> visibles = new ArrayList<Coordinacion>(0);
         ArrayList<Coordinacion> ocultos = new ArrayList<Coordinacion>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM COORDINACION ORDER BY CODIGO");
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Coordinacion u = new Coordinacion();
                 u.setCodigo(rs.getString("codigo"));
                 u.setNombre(rs.getString("nombre"));
                 u.setEstado(rs.getString("Estado"));
                 if (u.getEstado().equals("visible")) {
                     visibles.add(u);
                 } else {
                     ocultos.add(u);
                 }
             }
             coords[0] = visibles;
             coords[1] = ocultos;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return coords;
     }
 
     public ArrayList<NucleoUniversitario> [] listarNucleosUniversitarios() {
 
         ArrayList<NucleoUniversitario> [] nucleos = new ArrayList[2];
         ArrayList<NucleoUniversitario> visibles = new ArrayList<NucleoUniversitario>(0);
         ArrayList<NucleoUniversitario> ocultos = new ArrayList<NucleoUniversitario>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM NUCLEOUNIV ORDER BY CODIGO");
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 NucleoUniversitario u = new NucleoUniversitario();
                 u.setCodigo(rs.getString("codigo"));
                 u.setNombre(rs.getString("nombre"));
                 u.setEstado(rs.getString("Estado"));
                 if (u.getEstado().equals("visible")) {
                     visibles.add(u);
                 } else {
                     ocultos.add(u);
                 }
             }
             nucleos[0] = visibles;
             nucleos[1] = ocultos;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return nucleos;
     }
 
     public Profesor obtenerInfoProfesor(String usbid) {
 
         Profesor u = new Profesor();
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM profesor WHERE usbid = ?");
             ps.setString(1, usbid);
 
             ResultSet rs = ps.executeQuery();
 
 
 
             while (rs.next()) {
                 u.setUsbid(rs.getString("usbid"));
                 u.setNombre(rs.getString("nombre"));
                 u.setApellido(rs.getString("apellido"));
                 u.setCedula(rs.getString("cedula"));
                 u.setGenero(rs.getString("genero"));
                 u.setEmail(rs.getString("email"));
                 u.setEmail_personal(rs.getString("email_personal"));
                 u.setNivel(rs.getString("nivel"));
                 u.setJubilado(rs.getString("jubilado"));
                 u.setLapso_contractual_inicio(rs.getString("lapso_contractual_inicio"));
                 u.setLapso_contractual_fin(rs.getString("lapso_contractual_fin"));
             }
 
             return u;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return u;
     }
 
     public boolean eliminarUsuario(EliminarUserForm u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("DELETE FROM usuario WHERE ( usbid = ? )");
 
             ps.setString(1, u.getUsbid());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
 
     }
 
     public boolean actualizarInfoProfesor(Profesor u) {
 
         PreparedStatement psAgregar1 = null;
 
         try {
             psAgregar1 = conexion.prepareStatement("UPDATE profesor SET email = ?, email_personal = ?, nivel = ?, jubilado = ?, lapso_contractual_inicio = ?, lapso_contractual_fin = ? WHERE usbid = ? ");
             psAgregar1.setString(1, u.getEmail());
             psAgregar1.setString(2, u.getEmail_personal());
             psAgregar1.setString(3, u.getNivel());
             psAgregar1.setString(4, u.getJubilado());
             psAgregar1.setString(5, u.getLapso_contractual_inicio());
             psAgregar1.setString(6, u.getLapso_contractual_fin());
             psAgregar1.setString(7, u.getUsbid());
 
 
             Integer i = psAgregar1.executeUpdate();
 
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean resetInfoProfesor(Profesor u) {
 
         PreparedStatement psAgregar1 = null;
 
         try {
            psAgregar1 = conexion.prepareStatement("UPDATE profesor SET email_personal = ?, nivel = ?, jubilado = ?, lapso_contractual_inicio = ?, lapso_contractual_fin = ? WHERE usbid = ? ");
             psAgregar1.setString(1, "");
             psAgregar1.setString(2, "");
             psAgregar1.setString(3, "");
             psAgregar1.setString(4, "");
            psAgregar1.setString(5, "");
            psAgregar1.setString(6, u.getUsbid());
 
 
             Integer i = psAgregar1.executeUpdate();
 
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean actualizarRolUsuario(EliminarUserForm u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE usuario SET tipousuario = ?, departamento = ? WHERE ( usbid = ? )");
 
             ps.setString(1, u.getTipousuario());
             ps.setString(2, u.getDepartamento());
             ps.setString(3, u.getUsbid());
 
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
 
     }
 
     public boolean registrarCarrera(Carrera u) {
 
         PreparedStatement psAgregar1 = null;
 
         try {
             psAgregar1 = conexion.prepareStatement("INSERT INTO carrera(codigo, nombre) VALUES (?, ?);");
             psAgregar1.setString(1, u.getCodigo());
             psAgregar1.setString(2, u.getNombre());
 
             Integer i = psAgregar1.executeUpdate();
 
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean registrarCarreraDec(Carrera u,String decanato) {
 
         PreparedStatement psAgregar1 = null;
         PreparedStatement psAgregar2 = null;
         
         try {
             psAgregar1 = conexion.prepareStatement("INSERT INTO carrera(codigo, nombre) VALUES (?, ?);");
             psAgregar1.setString(1, u.getCodigo());
             psAgregar1.setString(2, u.getNombre());
             Integer i = psAgregar1.executeUpdate();
             
             psAgregar2 = conexion.prepareStatement("INSERT INTO incluye VALUES (?,?);");
             psAgregar2.setString(2, u.getCodigo());
             psAgregar2.setString(1, decanato);
             Integer j = psAgregar2.executeUpdate();
             
             return i > 0 && j > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
     
     public boolean eliminarCarrera(Carrera u){
     
         PreparedStatement psEliminar1 = null;
         try{
             psEliminar1 = conexion.prepareStatement("DELETE FROM carrera AS c WHERE (c.codigo = ?);");
             psEliminar1.setString(1,u.getCodigo());
             
             Integer i = psEliminar1.executeUpdate();
             
             System.out.println("retorna: " + (i>0));
             return i > 0;
             
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
     
     public boolean eliminarCoordinacion(Coordinacion u){
     
         PreparedStatement psEliminar1 = null;
         try{
             psEliminar1 = conexion.prepareStatement("DELETE FROM coordinacion AS c WHERE (c.codigo = ?);");
             psEliminar1.setString(1,u.getCodigo());
             
             Integer i = psEliminar1.executeUpdate();
             
             System.out.println("retorna: " + (i>0));
             return i > 0;
             
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
     
     public boolean registrarDecanato(Decanato u) {
 
         PreparedStatement psAgregar1 = null;
 
         try {
             psAgregar1 = conexion.prepareStatement("INSERT INTO decanato(codigo, nombre) VALUES (?, ?);");
             psAgregar1.setString(1, u.getCodigo());
             psAgregar1.setString(2, u.getNombre());
 
             Integer i = psAgregar1.executeUpdate();
 
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean registrarCoordinacion(Coordinacion c) {
         PreparedStatement psAgregar = null;
 
         try {
             psAgregar = conexion.prepareStatement("INSERT INTO COORDINACION(codigo, nombre) VALUES (?, ?);");
             psAgregar.setString(1, c.getCodigo());
             psAgregar.setString(2, c.getNombre());
 
             Integer i = psAgregar.executeUpdate();
 
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean registrarNucleoUniv(NucleoUniversitario n) {
         PreparedStatement psAgregar = null;
 
         try {
             psAgregar = conexion.prepareStatement("INSERT INTO NUCLEOUNIV(codigo, nombre) VALUES (?, ?);");
             psAgregar.setString(1, n.getCodigo());
             psAgregar.setString(2, n.getNombre());
 
             Integer i = psAgregar.executeUpdate();
 
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean actualizarEstadoCoordinacion(Coordinacion u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE COORDINACION SET Estado = ? WHERE ( codigo = ? )");
 
             ps.setString(1, u.getEstado());
             ps.setString(2, u.getCodigo());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
 
     }
 
     public boolean actualizarEstadoCarrera(Carrera u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE carrera SET Estado = ? WHERE ( codigo = ? )");
 
             ps.setString(1, u.getEstado());
             ps.setString(2, u.getCodigo());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
 
     }
 
     public boolean actualizarEstadoDecanato(Decanato u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE decanato SET Estado = ? WHERE ( codigo = ? )");
 
             ps.setString(1, u.getEstado());
             ps.setString(2, u.getCodigo());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
 
     }
 
     public boolean actualizarEstadoNucleoUniversitario(NucleoUniversitario u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE NUCLEOUNIV SET Estado = ? WHERE ( codigo = ? )");
 
             ps.setString(1, u.getEstado());
             ps.setString(2, u.getCodigo());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
 
     }
 
     public boolean actualizarNombreCarrera(Carrera u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE carrera SET nombre = ? WHERE ( codigo = ? )");
 
             ps.setString(1, u.getNombre());
             ps.setString(2, u.getCodigo());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
 
     }
 
     public boolean actualizarNombreDecanato(Decanato u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE decanato SET nombre = ? WHERE ( codigo = ? )");
 
             ps.setString(1, u.getNombre());
             ps.setString(2, u.getCodigo());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean actualizarNombreCoordinacion(Coordinacion u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE COORDINACION SET nombre = ? WHERE ( codigo = ? )");
 
             ps.setString(1, u.getNombre());
             ps.setString(2, u.getCodigo());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean actualizarNombreNucleoUniversitario(NucleoUniversitario u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE NUCLEOUNIV SET nombre = ? WHERE ( codigo = ? )");
 
             ps.setString(1, u.getNombre());
             ps.setString(2, u.getCodigo());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public Carrera obtenerNombreCarrera(Carrera u) {
 
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM carrera WHERE codigo = ?");
             ps.setString(1, u.getCodigo());
 
             ResultSet rs = ps.executeQuery();
 
 
 
             while (rs.next()) {
                 u.setNombre(rs.getString("nombre"));
             }
 
             return u;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return u;
     }
 
     public Decanato obtenerNombreDecanato(Decanato u) {
 
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM decanato WHERE codigo = ?");
             ps.setString(1, u.getCodigo());
 
             ResultSet rs = ps.executeQuery();
 
 
 
             while (rs.next()) {
                 u.setNombre(rs.getString("nombre"));
             }
 
             return u;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return u;
     }
 
     public Coordinacion obtenerNombreCoordinacion(Coordinacion u) {
 
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM COORDINACION WHERE codigo = ?");
             ps.setString(1, u.getCodigo());
 
             ResultSet rs = ps.executeQuery();
 
 
 
             while (rs.next()) {
                 u.setNombre(rs.getString("nombre"));
             }
 
             return u;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return u;
     }
 
     public NucleoUniversitario obtenerNombreNucleoUniversitario(NucleoUniversitario u) {
 
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM NUCLEOUNIV WHERE codigo = ?");
             ps.setString(1, u.getCodigo());
 
             ResultSet rs = ps.executeQuery();
 
 
 
             while (rs.next()) {
                 u.setNombre(rs.getString("nombre"));
             }
 
             return u;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return u;
     }
 
     public ArrayList<SINAI> listarSINAI(String usbid) {
 
         ArrayList<SINAI> sinai = new ArrayList<SINAI>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM SINAI WHERE usbid = ?");
             ps.setString(1, usbid);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 SINAI u = new SINAI();
                 u.setNombre(rs.getString("nombre"));
                 u.setFecha_inic(rs.getString("Fecha_inic"));
                 u.setFecha_fin(rs.getString("Fecha_fin"));
                 sinai.add(u);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return sinai;
     }
 
     public ArrayList<CCT> listarCCT(String usbid) {
 
         ArrayList<CCT> cct = new ArrayList<CCT>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM CCT WHERE usbid = ?");
             ps.setString(1, usbid);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 CCT u = new CCT();
                 u.setIdent(rs.getString("ident"));
                 u.setTitulo(rs.getString("titulo"));
                 u.setFecha_inic(rs.getString("Fecha_inic"));
                 u.setFecha_fin(rs.getString("Fecha_fin"));
                 u.setCarrera(rs.getString("carrera"));
                 u.setTipo(rs.getString("tipo"));
                 cct.add(u);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return cct;
     }
         
         public ArrayList<DACE> listarDACE(String usbid, int ano, String trim) {
 
         ArrayList<DACE> dace = new ArrayList<DACE>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM DACE WHERE usbid = ? AND ano = ? AND trimestre = ?");
             ps.setString(1, usbid);
             ps.setInt(2, ano);
             ps.setString(3, trim);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 DACE u = new DACE();
                 u.setCodigo(rs.getString("codigo"));
                 u.setNombre(rs.getString("nombre"));
                 u.setUno(rs.getInt("uno"));
                 u.setDos(rs.getInt("dos"));
                 u.setTres(rs.getInt("tres"));
                 u.setCuatro(rs.getInt("cuatro"));
                 u.setCinco(rs.getInt("cinco"));
                 u.setRetirados(rs.getInt("retirados"));
                 dace.add(u);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return dace;
     }
 }

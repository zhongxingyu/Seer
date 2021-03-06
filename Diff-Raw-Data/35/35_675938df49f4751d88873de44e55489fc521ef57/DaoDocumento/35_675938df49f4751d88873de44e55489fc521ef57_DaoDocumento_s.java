 /*
  * This file is part of Biblioteca-Digital de Univalle.
  *
  * Biblioteca-Digital de Univalle is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Biblioteca-Digital de Univalle is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with Biblioteca-Digital de Univalle.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package biblioteca.database2.accesoDatos;
 
 import biblioteca.database2.fachada.Fachada;
 import biblioteca.database2.beans.*;
 import java.sql.Statement;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 /**
  *  Esta clase forma parte de los controladores creados para cumplir con el Patrón
  * de diseño DAO.
  * 
  * <br>DaoDocumento crea el sql que se ejecutará a través de una conexión de una Fachada,
  * este dao tiene que ver con todo lo relacionado con la administración de documentos
  * en la aplicación
  * 
  * @see <a href="http://www.proactiva-calidad.com/java/patrones/DAO.html">Patrón "Data Access Object"</a>
  *
  * @author María Cristina Bustos Rodríguez
  * @author Alejandro Valdés Villada
  */
 public class DaoDocumento {
 
     Fachada Fachada;
 
     /**
      * Crea un nuevo DaoDocumento, inicializando la Fachada
      */
     public DaoDocumento() {
         Fachada = new Fachada();
     }
 
     /**
      * Inserta un nuevo documento a la base de datos
      * @param documento Documento con los datos del documento a insertar
      * @param usuario El username del catalogador
      * @return -1 si hubo algún error en la ejecución de la consulta
      */
     public int insertarDocumento(Documento documento, String usuario) {
         String sql_agregar;
         sql_agregar = "INSERT INTO documentos(activo, titulo_principal, "
                 + "titulo_secundario, descripcion, idioma, editorial, fecha_publicacion, "
                + "derechos_autor, tipo_documento, catalogador)"
                 + "VALUES ('" + "true" + "','" + documento.getTituloPrincipal() + "','"
                 + documento.getTituloSecundario() + "','" + documento.getDescripcion() + "','"
                 + documento.getIdioma() + "','" + documento.getEditorial() + "','"
                 + documento.getFechaPublicacion() + "','" + documento.getDerechosAutor() + "','"
                + documento.getTipoMaterial() + "','" + usuario + "');";
 
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             int num_filas = sentencia.executeUpdate(sql_agregar);
             conn.close();
             return num_filas;
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
 
         return -1;
     }
 
     /**
      * Consulta en la base de datos el documento con el identificador especificado
      * @param id_documento String con el identificador del documento a consultar
      * @return Documento con los datos relacionados al identificador
      */
     public Documento consultarDocumento(String id_documento) {
         String sql_consultar;
         sql_consultar = "SELECT doc_id, activo, titulo_principal, titulo_secundario,"
                 + "descripcion, tipo_documento, idioma, editorial, fecha_publicacion, derechos_autor,"
                 + "ubicacion, fecha_catalogacion FROM documentos WHERE doc_id='" + id_documento + "';";
         Documento documento = null;
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             ResultSet salida = sentencia.executeQuery(sql_consultar);
             documento = new Documento();
             if(salida.next()) {
                 documento.setID_documento(salida.getString(1));
                 documento.setActivo(salida.getString(2).equals("t") ? true : false);
                 documento.setTituloPrincipal(salida.getString(3));
                 documento.setTituloSecundario(salida.getString(4));
                 documento.setDescripcion(salida.getString(5));
                 documento.setTipoMaterial(salida.getString(6));
                 documento.setIdioma(salida.getString(7));
                 documento.setEditorial(salida.getString(8));
                 documento.setFechaPublicacion(salida.getString(9));
                 documento.setDerechosAutor(salida.getString(10));
                 documento.setUbicacion(salida.getString(11));
                 documento.setFechaCatalogacion(salida.getString(12));
             }
             conn.close();
 
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
         return documento;
     }
 
     /**
      * Obtiene el identificador de un documento especificando varios datos de este
      * @param documento Documento con los datos conocidos del documento
      * @param catalogador String con el username del catalogador del documento
      * @return String indicando el identificador del documento
      */
     public String obtenerDocumentoID(Documento documento, String catalogador) {
         String sql_consultar;
         sql_consultar = "SELECT doc_id FROM documentos WHERE titulo_principal='"
                 + documento.getTituloPrincipal() + "'AND titulo_secundario='"
                 + documento.getTituloSecundario() + "'AND descripcion='"
                 + documento.getDescripcion() + "'AND editorial='"
                 + documento.getEditorial() + "'AND idioma='"
                 + documento.getIdioma() + "'AND fecha_publicacion='"
                 + documento.getFechaPublicacion() + "'AND derechos_autor='"
                 + documento.getDerechosAutor() + "'AND catalogador='" + catalogador + "'"
                + "AND ubicacion=null";
         String id = null;
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             ResultSet salida = sentencia.executeQuery(sql_consultar);
             if (salida.next()) {
                 id = salida.getString(1);
             }
             conn.close();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
         return id;
     }
 
     /**
      * Inserta la ubicación (el path del archivo del documento) a un documento especificado por el identificador
      * @param id_documento String con el identificador del documento a modificar
      * @param ubicacion String con la ubicación(path) del documento
      * @return -1 si la operación no se pudo realizar
      */
     public int insertarUbicacion(String id_documento, String ubicacion) {
         String sql_modificar;
         sql_modificar = "UPDATE documentos SET ubicacion ='" + ubicacion + "' WHERE doc_id = '"
                 + id_documento + "';";
         try {
             Connection conn = Fachada.conectar();
             Statement sentencia = conn.createStatement();
             int num_filas = sentencia.executeUpdate(sql_modificar);
             conn.close();
             return num_filas;
 
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
         return -1;
     }
 
     /**
      * Deshabilita un documento de la base de datos
      * @param id_documento String con el identificador del documento
      * @return -1 si la operación de deshabilitación no se pudo realizar
      */
     public int deshabilitarDocumento(String id_documento) {
         String sql_modificar;
         sql_modificar = "UPDATE documentos SET activo = 'false' WHERE doc_id = '"
                 + id_documento + "';";
         try {
             Connection conn = Fachada.conectar();
             Statement sentencia = conn.createStatement();
             int num_filas = sentencia.executeUpdate(sql_modificar);
             conn.close();
             return num_filas;
 
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
         return -1;
     }
 
     /**
      * Modifica un documento en la base de datos, este se selecciona por medio del 
      * identificador que viene incluido en el parametro
      * @param documento Documento con los datos a modificar
      * @return -1 si la operación no se pudo realizar
      */
     public int modificarDocumento(Documento documento) {
         String sql_modificar;
         sql_modificar = "UPDATE documentos SET activo ='" + documento.getActivo() + "',"
                 + "titulo_principal='" + documento.getTituloPrincipal() + "',"
                 + "titulo_secundario='" + documento.getTituloSecundario() + "',"
                 + "descripcion='" + documento.getDescripcion() + "',"
                 + "idioma='" + documento.getIdioma() + "',"
                 + "editorial='" + documento.getEditorial() + "',"
                 + "fecha_publicacion='" + documento.getFechaPublicacion() + "',"
                 + "derechos_autor='" + documento.getDerechosAutor() + "',"
                 + "ubicacion='" + documento.getUbicacion() + "',"
                 + "tipo_documento='" + documento.getTipoMaterial() + "'"
                 + "WHERE doc_id='" + documento.getID_documento() + "'";
 
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             int num_filas = sentencia.executeUpdate(sql_modificar);
             conn.close();
             return num_filas;
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
 
         return -1;
     }
 
     /**
      * Verifica si un documento existe en la base de datos a través de su identificador unico
      * @param id_documento String con el identificador del documento
      * @return boolean indicando si el documento existe o no
      * @deprecated Use consultarDocumento, y comprobar si lo que retorna es null
      */
     @Deprecated
     public boolean verificarExistencia(String id_documento) {
         String sql_verificar;
         sql_verificar = "SELECT * FROM documentos WHERE doc_id='" + id_documento + "';";
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             ResultSet salida = sentencia.executeQuery(sql_verificar);
             conn.close();
             if (salida.next()) {
                 if (salida.wasNull()) {
                     return false;
                 } else {
                     return true;
                 }
             }
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
         return false;
     }
     
     /**
      * Modifica un documento en la base de datos, este se selecciona por medio del 
      * identificador que viene incluido en el parametro
      * @param documento Documento con los datos a modificar
      * @return -1 si la operación no se pudo realizar
      * @deprecated Use modificarDocumento(Documento) en vez de esta
      */
     @Deprecated
     public int actualizarDocumento(Documento documento) {
         String sql_modificar;
         sql_modificar = "UPDATE documentos SET activo ='" + documento.getActivo() + "',"
                 + "titulo_principal='" + documento.getTituloPrincipal() + "',"
                 + "titulo_secundario='" + documento.getTituloSecundario() + "',"
                 + "descripcion='" + documento.getDescripcion() + "',"
                 + "idioma='" + documento.getIdioma() + "',"
                 + "editorial='" + documento.getEditorial() + "',"
                 + "fecha_publicacion='" + documento.getFechaPublicacion() + "',"
                 + "derechos_autor='" + documento.getDerechosAutor() + "',"
                 + "ubicacion='" + documento.getUbicacion() + "',"
                 + "tipo_documento='" + documento.getTipoMaterial() + "'"
                 + "WHERE doc_id='" + documento.getID_documento() + "'";
 
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             int num_filas = sentencia.executeUpdate(sql_modificar);
             conn.close();
             return num_filas;
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
 
         return -1;
     }
 
     /**
      * Inserta un conjunto de áreas al documento seleccionado
      * @param areas ArrayList de Area con las áreas a insertar
      * @param id_documento String con el identificador del documento a modificar
      */
     public void insertarAreas(ArrayList<Area> areas, String id_documento) {
         try {
             Connection conn = Fachada.conectar();
             Statement sentencia = conn.createStatement();
             String sql_insertar;
             for (int i = 0; i < areas.size(); i++) {
                 sql_insertar = "INSERT INTO documento_areas_computacion VALUES ('"
                         + areas.get(i).getID() + "','" + id_documento + "');";
                 sentencia.addBatch(sql_insertar);
             }
             sentencia.executeBatch();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
     }
 
     /**
      * Inserta un conjunto de palabras clave al documento seleccionado
      * @param PC ArrayList de PalabraClave con las palabras claves a insertar
      * @param id_documento String con el identificador del documento a modificar
      */
     public void insertarPalabrasClave(ArrayList<PalabraClave> PC, String id_documento) {
         try {
             Connection conn = Fachada.conectar();
             Statement sentencia = conn.createStatement();
             String sql_insertar;
             for (int i = 0; i < PC.size(); i++) {
                 sql_insertar = "INSERT INTO documento_palabras_clave VALUES ('"
                         + id_documento + "','" + PC.get(i).getNombre() + "');";
                 sentencia.addBatch(sql_insertar);
             }
             sentencia.executeBatch();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
     }
 
     /**
      * Inserta un conjunto de autores al documento seleccionado
      * @param autores ArrayList de Autor con los autores a insertar al documento
      * @param id_documento String con el identificador del documento a modificar
      */
     public void insertarAutores(ArrayList<Autor> autores, String id_documento) {
         try {
             Connection conn = Fachada.conectar();
             Statement sentencia = conn.createStatement();
             String sql_insertar;
             for (int i = 0; i < autores.size(); i++) {
                 sql_insertar = "INSERT INTO documento_autor VALUES ('"
                         + id_documento + "','" + autores.get(i).getCorreo() + "');";
                 sentencia.addBatch(sql_insertar);
             }
             sentencia.executeBatch();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
     }
 
     /**
      * Elimina un conjunto de áreas del documento seleccionado
      * @param areas ArrayList de Area con las áreas a eliminar
      * @param id_documento String con el identificador del documento a modificar
      */
     public void eliminarAreas(ArrayList<Area> areas, String id_documento) {
         try {
             Connection conn = Fachada.conectar();
             Statement sentencia = conn.createStatement();
             String sql_eliminar;
             for (int i = 0; i < areas.size(); i++) {
                 sql_eliminar = "DELETE FROM documento_areas_computacion WHERE "
                         + "area_id='" + areas.get(i).getID() + "' AND doc_id='" + id_documento + "';";
                 sentencia.addBatch(sql_eliminar);
             }
             sentencia.executeBatch();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
     }
 
     /**
      * Elimina un conjunto de palabras clave del documento seleccionado
      * @param PC ArrayList de PalabraClave con las palabras claves a eliminar
      * @param id_documento String con el identificador del documento a modificar
      */
     public void eliminarPalabrasClave(ArrayList<PalabraClave> PC, String id_documento) {
         try {
             Connection conn = Fachada.conectar();
             Statement sentencia = conn.createStatement();
             String sql_eliminar;
             for (int i = 0; i < PC.size(); i++) {
                 sql_eliminar = "DELETE FROM documento_palabras_clave WHERE doc_id='"
                         + id_documento + "' AND nombre='" + PC.get(i).getNombre() + "';";
                 sentencia.addBatch(sql_eliminar);
             }
             sentencia.executeBatch();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
     }
 
     /**
      * Elimina un conjunto de autores del documento seleccionado
      * @param autores ArrayList de Autor con los autores a eliminar
      * @param id_documento String con el identificador del documento a modificar
      */
     public void eliminarAutores(ArrayList<Autor> autores, String id_documento) {
         try {
             Connection conn = Fachada.conectar();
             Statement sentencia = conn.createStatement();
             String sql_eliminar;
             for (int i = 0; i < autores.size(); i++) {
                 sql_eliminar = "DELETE FROM documento_autor WHERE doc_id='"
                         + id_documento + "' AND autor_correo='" + autores.get(i).getCorreo() + "';";
                 sentencia.addBatch(sql_eliminar);
             }
             sentencia.executeBatch();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
     }
 
     /**
      * Consulta las palabras claves relacionadas con el documento seleccionado
      * @param id_documento String con el identificador del documento a consultar
      * @return ArrayList de PalabraClave con las palabras relacionadas al documento
      */
     public ArrayList<PalabraClave> consultarPalabrasClaveDocumento(String id_documento) {
         String sql_consultar;
         sql_consultar = "SELECT nombre, descripcion FROM documento_palabras_clave"
                 + " NATURAL JOIN palabras_clave WHERE doc_id='" + id_documento + "';";
         ArrayList<PalabraClave> palabrasClave = null;
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             ResultSet salida = sentencia.executeQuery(sql_consultar);
             palabrasClave = new ArrayList<PalabraClave>();
             while (salida.next()) {
                 PalabraClave palabraClave = new PalabraClave();
                 palabraClave.setNombre(salida.getString(1));
                 palabraClave.setDescripcion(salida.getString(2));
                 palabrasClave.add(palabraClave);
             }
             conn.close();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
         return palabrasClave;
     }
 
     /**
      * Consulta los autores relacionados con el documento especificado
      * @param id_documento String con el identificador del documento a consultar
      * @return ArrayList de Autor con los autores relacionados
      */
     public ArrayList<Autor> consultarAutoresDocumento(String id_documento) {
         String sql_consultar;
         sql_consultar = "SELECT autor_correo, acronimo, nombre, apellido FROM "
                 + "documento_autor NATURAL JOIN autor WHERE "
                 + "doc_id='" + id_documento + "';";
         ArrayList<Autor> Autores = null;
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             ResultSet salida = sentencia.executeQuery(sql_consultar);
             Autores = new ArrayList<Autor>();
             while (salida.next()) {
                 Autor autor = new Autor();
                 autor.setCorreo(salida.getString(1));
                 autor.setAcronimo(salida.getString(2));
                 autor.setNombre(salida.getString(3));
                 autor.setApellido(salida.getString(4));
                 Autores.add(autor);
             }
             conn.close();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
         return Autores;
     }
 
     /**
      * Consulta las áreas relacionadas con el documento seleccionado
      String con el identificador del documento a consultar
      * @return ArrayList de Area con las áreas relacionadas
      */
     public ArrayList<Area> consultarAreasDocumento(String id_documento) {
         String sql_consultar;
         sql_consultar = "SELECT area_id, nombre, descripcion, area_padre FROM "
                 + "documento_areas_computacion NATURAL JOIN areas_computacion"
                 + " WHERE doc_id='" + id_documento + "';";
         ArrayList<Area> Areas = null;
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             ResultSet salida = sentencia.executeQuery(sql_consultar);
             Areas = new ArrayList<Area>();
             while (salida.next()) {
                 Area area = new Area();
                 area.setID(salida.getString(1));
                 area.setNombre(salida.getString(2));
                 area.setDescripcion(salida.getString(3));
                 area.setAreaPadre(salida.getString(4));
                 Areas.add(area);
             }
             conn.close();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
         return Areas;
     }
     
     /**
      * Consulta en la base de datos si un documento contiene alguno de los metadatos
      * especificados en la entrada. Esta función consulta en los campos de:
      * <br> Áreas
      * <br> Autores
      * <br> Palabras Clave
      * <br> Titulo Principal
      * <br> Titulo Secundario
      * <br><br> La salida de esta función es el identificador del documento y el título principal de este
      * @param metadatos ArrayList de String con las palabras (metadatos) a consultar
      * @return ArrayList de String con los datos de los documentos encontrados 
      */
     public ArrayList<String> consultarDocumento(ArrayList<String> metadatos) {
         String sql_consultar = "SELECT DISTINCT documentos.doc_id, titulo_principal"
                 + " FROM ((((areas_computacion NATURAL JOIN "
                 + "documento_areas_computacion) JOIN documentos ON "
                 + "documento_areas_computacion.doc_id=documentos.doc_id) "
                 + "JOIN documento_autor ON documentos.doc_id=documento_autor.doc_id "
                 + "JOIN autor ON documento_autor.autor_correo=autor.autor_correo) "
                 + "JOIN documento_palabras_clave ON documentos.doc_id="
                 + "documento_palabras_clave.doc_id) WHERE ";
         sql_consultar += "titulo_principal ILIKE '%" + metadatos.get(0) + "%' OR ";
         sql_consultar += "titulo_secundario ILIKE '%" + metadatos.get(0) + "%' OR ";
         sql_consultar += "autor.nombre ILIKE '%" + metadatos.get(0) + "%' OR ";
         sql_consultar += "autor.apellido ILIKE '%" + metadatos.get(0) + "%' OR ";
         sql_consultar += "areas_computacion.nombre ILIKE '%" + metadatos.get(0) + "%' OR ";
         sql_consultar += "documento_palabras_clave.nombre ILIKE '%" + metadatos.get(0) + "%'";
 
         for (int i = 1; i < metadatos.size(); i++) {
             String temporal="";
             temporal += "OR titulo_principal ILIKE '%" + metadatos.get(i) + "%' OR ";
             temporal += "titulo_secundario ILIKE '%" + metadatos.get(i) + "%' OR ";
             temporal += "autor.nombre ILIKE '%" + metadatos.get(i) + "%' OR ";
             temporal += "autor.apellido ILIKE '%" + metadatos.get(i) + "%' OR ";
             temporal += "areas_computacion.nombre ILIKE '%" + metadatos.get(i) + "%' OR ";
             temporal += "documento_palabras_clave.nombre ILIKE '%" + metadatos.get(i) + "%'";
             sql_consultar+=temporal;
         }
         sql_consultar+=";";
         ArrayList<String> resultados = null;
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             ResultSet salida = sentencia.executeQuery(sql_consultar);
             resultados = new ArrayList<String>();
             while (salida.next()) {
                 resultados.add(salida.getString(1));
                 resultados.add(salida.getString(2));
             }
             conn.close();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
         return resultados;
         
     }
 
     /**
      * Inserta en la base de datos una nueva tupla con la información referente
      * a la consulta de un documento por parte de un usuario
      * @param id_documento String con el identificador del documento a consultar
      * @param usuario String con el login(username) del usuario que consulta
      */
     public void UsuarioConsultaDocumento(String id_documento, String usuario) {
         String sql_insertar;
         sql_insertar = "INSERT INTO usuario_consulta_documento (doc_id, username)"
                 + "VALUES ('" + id_documento + "','" + usuario + "');";
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             sentencia.execute(sql_insertar);
             conn.close();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
     }
 
     /**
      * Inserta en la base de datos una nueva tupla con la información referente a
      * la descarga de un documento por parte de un usuario <b>registrado</b>
      * @param id_documento String con el identificador del documento a descargar
      * @param usuario String con el login(username) del usuario que va a descargar el documento
      */
     public void UsuarioDescargaDocumento(String id_documento, String usuario) {
         String sql_insertar;
         sql_insertar = "INSERT INTO usuario_descarga_documento (doc_id, username)"
                 + "VALUES ('" + id_documento + "','" + usuario + "');";
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             sentencia.executeUpdate(sql_insertar);
             conn.close();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
     }
 
     /**
      * Consulta en la base de datos si existen documentos con los parametros espeficados
      * <br><br> La salida de esta función es el identificador del documento y el título principal de este
      * @param titulo
      * @param autor
      * @param pc
      * @param tituloopcion
      * @param autoropcion
      * @param pcopcion
      * @param area
      * @param editorial
      * @param tipo_material
      * @param idioma
      * @param fecha
      * @return 
      */
     public ArrayList<String>  consultaAvanzada(ArrayList<String> titulo, ArrayList<String> autor, ArrayList<String> pc, int tituloopcion, int autoropcion, int pcopcion, String area, String editorial, String tipo_material, String idioma, int fecha) {
 
       ArrayList<String> resultados = new ArrayList<String>();
       String Restricciones=ConsultaRestricciones(editorial, tipo_material, idioma, fecha);
       String ConsultaAreas=ConsultaAvanzadaAreas(area);
       String SQL_Avanzado="";
       if(!titulo.isEmpty()) {
           SQL_Avanzado+="("+ConsultaAvanzadaTitulo(titulo,tituloopcion)+")";
 
       }
       if(!autor.isEmpty()){
           if(!titulo.isEmpty()) SQL_Avanzado+=" INTERSECT ";
            SQL_Avanzado+="("+ConsultaAvanzadaAutor(autor,autoropcion)+")";
       }
       if(!pc.isEmpty()){
           if(!titulo.isEmpty() || !autor.isEmpty()) SQL_Avanzado+=" INTERSECT ";
           SQL_Avanzado+="("+ConsultaAvanzadaPalabraClave(pc, pcopcion)+")";
       }
 
       if(!Restricciones.equals("")){
           if(!titulo.isEmpty() || !autor.isEmpty() || !titulo.isEmpty() || !pc.isEmpty()) SQL_Avanzado+=" INTERSECT ";
           SQL_Avanzado+="("+Restricciones+")";
       }
 
       if(!ConsultaAreas.equals("")){
           if(!titulo.isEmpty() || !autor.isEmpty() || !titulo.isEmpty() || !pc.isEmpty() || !Restricciones.equals(""))
               SQL_Avanzado+=" INTERSECT ";
           SQL_Avanzado+="("+ConsultaAreas+")";
       }
       
       SQL_Avanzado+=";";
       
       try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             ResultSet salida = sentencia.executeQuery(SQL_Avanzado);
              while (salida.next()) {
                 resultados.add(salida.getString(1));
                 resultados.add(salida.getString(2));
             }
             conn.close();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
 
         return resultados;
     }
 
    private String ConsultaAvanzadaTitulo(ArrayList<String> titulo, int tituloopcion){
        String SQL_Avanzado="SELECT  DISTINCT documentos.doc_id, titulo_principal FROM documentos WHERE";
        if(tituloopcion==0){
           for(int i=0;i<titulo.size();i++){
              if(i==0)
                 SQL_Avanzado+=" titulo_principal ilike '%"+titulo.get(i)+"%' ";
              else SQL_Avanzado+=" and titulo_principal ilike '%"+titulo.get(i)+"%' ";
           }
        }
        else if(tituloopcion == 1)
            {
            SQL_Avanzado+=" titulo_principal='"+titulo.get(0)+"' ";
        }
        else if(tituloopcion == 2)
            {
             for(int i=0;i<titulo.size();i++){
              if(i==0)
                 SQL_Avanzado+=" titulo_principal ilike '%"+titulo.get(i)+"%' ";
              else SQL_Avanzado+=" or titulo_principal ilike '%"+titulo.get(i)+"%' ";
           }
        }
        else if(tituloopcion == 3)
            {
            for(int i=0;i<titulo.size();i++){
              if(i==0)
                 SQL_Avanzado+=" titulo_principal not ilike '%"+titulo.get(i)+"%' ";
              else SQL_Avanzado+=" and titulo_principal not ilike '%"+titulo.get(i)+"%' ";
           }
        
        }
        return SQL_Avanzado;
    }
 
    private String ConsultaAvanzadaAutor(ArrayList<String> autor, int autoropcion){
        String SQL_Avanzado="(SELECT  DISTINCT documentos.doc_id, titulo_principal FROM documentos NATURAL JOIN documento_autor "
                + "INNER JOIN autor ON documento_autor.autor_correo=autor.autor_correo WHERE";
        String SQL_AvanzadoUnion=" UNION (SELECT  DISTINCT documentos.doc_id, titulo_principal FROM documentos NATURAL JOIN documento_autor "
                + "INNER JOIN autor ON documento_autor.autor_correo=autor.autor_correo WHERE";
          if(autoropcion==0){
           for(int i=0;i<autor.size();i++){
              if(autor.size()==1){
                 SQL_Avanzado+=" nombre ilike '%"+autor.get(i)+"%' )";
                 SQL_AvanzadoUnion+=" apellido ilike '%"+autor.get(i)+"%' )";
              }
              else if (i == 0) {
                 SQL_Avanzado+=" nombre ilike '%"+autor.get(i)+"%' ";
                 SQL_AvanzadoUnion+=" apellido ilike '%"+autor.get(i)+"%' ";
               }
              else if(i == autor.size()-1){
                  SQL_Avanzado+=" and nombre ilike '%"+autor.get(i)+"%') ";
                  SQL_AvanzadoUnion+=" and apellido ilike '%"+autor.get(i)+"%') ";
                 }
              else {
                  SQL_Avanzado+=" and nombre ilike '%"+autor.get(i)+"%' ";
                  SQL_AvanzadoUnion+=" and apellido ilike '%"+autor.get(i)+"%' ";
              }
           }
        }
        else if(autoropcion == 1)
            {
            SQL_Avanzado+=" nombre='"+autor.get(0)+"' )";
            SQL_AvanzadoUnion+=" apellido='"+autor.get(0)+"' )";
        }
        else if(autoropcion == 2)
            {
             for(int i=0;i<autor.size();i++){
              if(autor.size()==1){
                 SQL_Avanzado+=" nombre ilike '%"+autor.get(i)+"%' )";
                 SQL_AvanzadoUnion+=" apellido ilike '%"+autor.get(i)+"%' )";
              }
              else if(i == 0)
                 {
                 SQL_Avanzado+=" nombre ilike '%"+autor.get(i)+"%' ";
                 SQL_AvanzadoUnion+=" apellido ilike '%"+autor.get(i)+"%' ";
                 }
              else if(i == autor.size()-1) {
                  SQL_Avanzado+=" or nombre ilike '%"+autor.get(i)+"%') ";
                  SQL_AvanzadoUnion+=" or apellido ilike '%"+autor.get(i)+"%') ";
              }
             else{
                  SQL_Avanzado+=" or nombre ilike '%"+autor.get(i)+"%' ";
                  SQL_AvanzadoUnion+=" or apellido ilike '%"+autor.get(i)+"%' ";
              }
 
           }
        }
        else if(autoropcion == 3)
            {
            for(int i=0;i<autor.size();i++){
              if(autor.size()==1){
                  SQL_Avanzado+=" nombre not ilike '%"+autor.get(i)+"%' )";
                 SQL_AvanzadoUnion+=" apellido not ilike '%"+autor.get(i)+"%' )";
              }
                else if(i == 0)
                {
                 SQL_Avanzado+=" nombre not ilike '%"+autor.get(i)+"%' ";
                 SQL_AvanzadoUnion+=" apellido not ilike '%"+autor.get(i)+"%' ";
                }
             else if(i == autor.size()-1) {
                  SQL_Avanzado+=" and nombre not ilike '%"+autor.get(i)+"%') ";
                  SQL_AvanzadoUnion+=" and apellido not ilike '%"+autor.get(i)+"%') ";
                 }
              else {
                  SQL_Avanzado+=" and nombre not ilike '%"+autor.get(i)+"%' ";
                  SQL_AvanzadoUnion+=" and apellido not ilike '%"+autor.get(i)+"%' ";
              }
           }
 
        }
 
        return SQL_Avanzado+SQL_AvanzadoUnion;
    }
 
    private String ConsultaAvanzadaPalabraClave(ArrayList<String> pc, int pcopcion){
         String SQL_Avanzado="SELECT  DISTINCT documentos.doc_id, titulo_principal FROM documentos NATURAL JOIN documento_palabras_clave "
                 + "INNER JOIN palabras_clave ON documento_palabras_clave.nombre=palabras_clave.nombre WHERE";
        if(pcopcion==0){
           for(int i=0;i<pc.size();i++){
              if(i==0)
                 SQL_Avanzado+=" palabras_clave.nombre ilike '%"+pc.get(i)+"%' ";
              else SQL_Avanzado+=" and palabras_clave.nombre ilike '%"+pc.get(i)+"%' ";
           }
        }
        else if(pcopcion == 1)
            {
            SQL_Avanzado+=" palabras_clave.nombre='"+pc.get(0)+"' ";
        }
        else if(pcopcion == 2)
            {
             for(int i=0;i<pc.size();i++){
              if(i==0)
                 SQL_Avanzado+=" palabras_clave.nombre ilike '%"+pc.get(i)+"%' ";
              else SQL_Avanzado+=" or palabras_clave.nombre ilike '%"+pc.get(i)+"%' ";
           }
        }
        else if(pcopcion == 3)
            {
            for(int i=0;i<pc.size();i++){
              if(i==0)
                 SQL_Avanzado+=" palabras_clave.nombre not ilike '%"+pc.get(i)+"%' ";
              else SQL_Avanzado+=" and palabras_clave.nombre not ilike '%"+pc.get(i)+"%' ";
           }
 
        }
        return SQL_Avanzado;
    }
 
    private String ConsultaRestricciones(String editorial, String tipo_material, String idioma, int fecha){
      String SQL_Avanzado="";
      if(editorial.equals("") && tipo_material.equals("Cualquiera") && idioma.equals("Cualquiera") && fecha==0)
          return SQL_Avanzado;
      else{
      SQL_Avanzado="SELECT  DISTINCT documentos.doc_id, titulo_principal FROM documentos WHERE";
      if(!editorial.equals("")){
          SQL_Avanzado+=" editorial ilike '"+editorial+"' ";
      }
      if(!tipo_material.equals("Cualquiera")){
          if(!editorial.equals("")) SQL_Avanzado+=" and ";
          SQL_Avanzado+=" tipo_documento='"+tipo_material+"' ";
      }
      if(!idioma.equals("Cualquiera")){
           if(!editorial.equals("") || !tipo_material.equals("Cualquiera")) SQL_Avanzado+=" and ";
          SQL_Avanzado+=" idioma='"+idioma+"' ";
      }
      if(fecha!=0){
         Calendar cal = Calendar.getInstance();
         String fecha_actual=(cal.get(Calendar.YEAR))+"-"+(cal.get(Calendar.MONTH)+1)+"-"+(cal.get(Calendar.DAY_OF_MONTH));
         if(fecha==1){
             String mes_pasado=(cal.get(Calendar.YEAR))+"-"+(cal.get(Calendar.MONTH))+"-"+(cal.get(Calendar.DAY_OF_MONTH));
             SQL_Avanzado+=" and fecha_publicacion BETWEEN '"+mes_pasado+"' and '"+fecha_actual+"' ";
         }
          else if(fecha == 2){
            String ano_pasado=(cal.get(Calendar.YEAR)-1)+"-"+(cal.get(Calendar.MONTH)+1)+"-"+(cal.get(Calendar.DAY_OF_MONTH));
             SQL_Avanzado+=" and fecha_publicacion BETWEEN '"+ano_pasado+"' and '"+fecha_actual+"' ";
         }
         else if(fecha == 3){
             String hace_dos_anos=(cal.get(Calendar.YEAR)-2)+"-"+(cal.get(Calendar.MONTH)+1)+"-"+(cal.get(Calendar.DAY_OF_MONTH));
             SQL_Avanzado+=" and fecha_publicacion BETWEEN '"+hace_dos_anos+"' and '"+fecha_actual+"' ";
         }
         else if(fecha == 4){
             String hace_cinco_anos=(cal.get(Calendar.YEAR)-5)+"-"+(cal.get(Calendar.MONTH)+1)+"-"+(cal.get(Calendar.DAY_OF_MONTH));
             SQL_Avanzado+=" and fecha_publicacion BETWEEN '"+hace_cinco_anos+"' and '"+fecha_actual+"' ";
         }
      }
 
      return SQL_Avanzado;
        }
     }
 
    private String ConsultaAvanzadaAreas(String area){
        String SQL_Avanzado="";
        if(area.equals("Cualquiera")){
            return SQL_Avanzado;
        }
        else{
          SQL_Avanzado="SELECT DISTINCT documentos.doc_id, titulo_principal FROM documentos NATURAL JOIN documento_areas_computacion"
                  + " INNER JOIN areas_computacion ON documento_areas_computacion.area_id=areas_computacion.area_id "
                  + " WHERE documento_areas_computacion.area_id='"+area+"'";
 
          return SQL_Avanzado;
         }
     }
    
    /**
     * Consulta las recomendaciones de documento para un usuario registrado, estas
     * se basan en la ultima fecha de entrada del usuario, se entregan todos los
     * documentos que han sido catalogados después de esa fecha del usuario
     * <br><br> La salida de esta función es el identificador del documento y el título principal de este
     * @param fecha_registro String con la fecha de ultimo acceso del usuario, debe estar en formato SQL
     * @param Areas ArrayList de Area con las areas relacionadas al usuario que esta buscando las recomendaciones
     * @return ArrayList de String con los datos relacionados de los documentos encontrados
     */
    public ArrayList<String> consultarRecomendacionesDocumentos(String username, String fecha_registro, ArrayList<String> Areas){
        String sql_consultar = "SELECT DISTINCT documentos.doc_id, titulo_principal"
                 + " FROM documento_areas_computacion NATURAL JOIN documentos WHERE ";
         sql_consultar += "fecha_catalogacion > " + fecha_registro+ " AND ( ";
 
         for (int i = 0; i < Areas.size(); i++) {
             sql_consultar+="area_id='"+Areas.get(i)+"' ";
             if(i!=(Areas.size()-1))
                 sql_consultar+="OR ";
         }
         sql_consultar+=");";
         ArrayList<String> resultados = null;
         System.err.println(sql_consultar);
         try {
             Connection conn = Fachada.conectar();
             java.sql.Statement sentencia = conn.createStatement();
             ResultSet salida = sentencia.executeQuery(sql_consultar);
             resultados = new ArrayList<String>();
             while (salida.next()) {
                 resultados.add(salida.getString(1));
                 resultados.add(salida.getString(2));
             }
             conn.close();
         } catch (SQLException e) {
             System.err.println(e);
         } catch (Exception e) {
             System.err.println(e);
         }
         return resultados;
    }
 
 }
 
 

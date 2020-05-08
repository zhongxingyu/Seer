 package doo.daba.java.servicio;
 
 import doo.daba.java.beans.UsuarioBean;
 import doo.daba.java.servicio.interfaces.UsuarioServicio;
 import doo.daba.java.persistencia.UsuarioInterfaceDao;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: Gerardo Aquino
  * Date: 14/05/13
  */
 @Service
 @Transactional
 public class UsuarioServicioImpl implements UsuarioServicio {
 
     @Autowired
     private UsuarioInterfaceDao usuarioDao;
 
 
 
     @Override
     public int registraUsuario(UsuarioBean usuario) {
         return this.usuarioDao.insert(usuario);
     }
 
 
 
     @Override
     public UsuarioBean consultarUsuario(int idUsuario) {
         return this.usuarioDao.select(idUsuario);
     }
 
 
 
     @Override
     public UsuarioBean consultarUsuario(String alias) {
        return this.usuarioDao.select(alias);
     }
 
 
 
     @Override
     public List<String> consultarRoles(int idUsuario) {
         return this.usuarioDao.obtenerListaDeRoles(idUsuario);
     }
 
 
 
     @Override
     public int registrarImagenDePerfil(int idUsuario, int idImagen) {
         return this.registrarImagenDePerfil(idUsuario, idImagen);
     }
 
 }

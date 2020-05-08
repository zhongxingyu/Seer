 package doo.daba.java.servicio;
 
 import doo.daba.java.beans.ImagenBean;
 import doo.daba.java.persistencia.ImagenInterfaceDao;
 import doo.daba.java.servicio.interfaces.ImagenServicio;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Gerardo Aquino
  * Date: 14/05/13
  */
 @Service
 @Transactional
 public class ImagenServicioImpl implements ImagenServicio {
 
     @Autowired
     ImagenInterfaceDao imagenDao;
 
 
 
     @Override
     public int registrarImagen(ImagenBean imagen) {
         return this.imagenDao.insert(imagen);
     }
 
 
 
     @Override
     public ImagenBean consultarImagen(int idImagen) {
         return this.imagenDao.select(idImagen);
     }
 
 
 
     @Override
     public ImagenBean consultarImagenPerfilUsuario(int idUsuario) {
        return this.consultarImagenPerfilUsuario(idUsuario);
     }
 }

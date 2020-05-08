 package despacho.backend.administradores;
 
 import java.util.*;
 
 import javax.ejb.Stateless;
 import javax.persistence.*;
 
 import despacho.backend.entities.*;
 
 @Stateless
 public class AdministradorUsuariosBean implements AdministradorUsuarios {
 	
 	@PersistenceContext(unitName="portalweb.despacho")
 	private EntityManager em;
 
 	@Override
 	public void agregar(Usuario usuario) {	
 		this.em.persist(usuario);
 	}
 	
 	@Override
 	public List<Usuario> listar() {
 		@SuppressWarnings("unchecked")
		List<Usuario> usuarios = this.em.createQuery("FROM USUARIOS").getResultList();
 		return usuarios;
 	}
 	
 	@Override
 	public void actualizar(Usuario usuario) {
 		this.em.merge(usuario);
 	}
 }

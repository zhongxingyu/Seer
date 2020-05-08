 package com.worktogether.buisiness;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import com.worktogether.dto.EventoDTO;
 import com.worktogether.dto.UsuarioDTO;
 import com.worktogether.entities.DominioTipoPontuacao;
 import com.worktogether.entities.Evento;
 import com.worktogether.entities.Usuario;
 
 @Stateless
 public class GerenciaRanking {
 	
 	@PersistenceContext
 	private EntityManager em;
 	
 	//TODO MODELAR
 	public BigDecimal calcularPontuacaoEvento(Long idEvento, DominioTipoPontuacao origem){
 		try{
 			if(DominioTipoPontuacao.PRESENCA.toString().equalsIgnoreCase(origem.toString())){
 				return new BigDecimal(1);
 				
 			}else if(DominioTipoPontuacao.PUBLICACAO.toString().equalsIgnoreCase(origem.toString())){
 				return new BigDecimal(10);
 			}
 			
 			return new BigDecimal(0);
 			
 		}catch(Throwable e){
 			e.printStackTrace();
 			return new BigDecimal(0);
 		}
 	}
 	
 	//TODO MODELAR
 	public BigDecimal calcularPontuacaoUsuarioUsuario(Long idUsuario, DominioTipoPontuacao origem){
 		try{
 			if(DominioTipoPontuacao.INDICAR_PRESENCA.toString().equals(origem.toString())){
 				return new BigDecimal(1);
 				
 			}else if(DominioTipoPontuacao.PRESENCA.toString().equalsIgnoreCase(origem.toString())){
 				return new BigDecimal(10);
 			}
 			
 			return new BigDecimal(0);
 			
 		}catch(Throwable e){
 			e.printStackTrace();
 			return new BigDecimal(0);
 		}
 	} 
 	
 	//TODO MODELAR
 	public void atualizarPontuacaoUsuario(Long idUsuario, DominioTipoPontuacao origem){
 		try{
 			TypedQuery<Usuario> qr = em.createQuery("select u from Usuario u where u.id = ?1",  Usuario.class);
 			qr.setParameter(1, idUsuario);
 			Usuario usuario = qr.getSingleResult();
 			
 			BigDecimal pu = this.calcularPontuacaoUsuarioUsuario(idUsuario, origem);
 			usuario.setPontuacao(pu.add(usuario.getPontuacao()));
 			em.persist(usuario);
 			
 		}catch(Throwable e){
 			e.printStackTrace();
 		}
 	}
 	
 	//TODO MODELAR
 	public void atualizarPontuacaoEvento(Long idEvento, DominioTipoPontuacao origem){
 		try{
 			TypedQuery<Evento> q = em.createQuery("select e from Evento e where e.id = ?1",  Evento.class);
 			q.setParameter(1, idEvento);
 			Evento evento = q.getSingleResult();
 			
 			BigDecimal pe = this.calcularPontuacaoEvento(idEvento, origem);
 			evento.setPontuacao(pe.add(evento.getPontuacao()));
 			em.persist(evento);
 			
 		}catch(Throwable e){
 			e.printStackTrace();
 		}
 	} 
 	
 	//TODO MODELAR
 	public List<UsuarioDTO> buscarRankingUsuario(BigDecimal  pontuacaoUltimo){
 		try{
 			boolean isPrimeiraConsulta = (pontuacaoUltimo == null);
 			
 			StringBuilder sql = new StringBuilder("select u from Usuario u");
 			
			if(!isPrimeiraConsulta){
 				sql.append(" where u.pontuacao > ?1");
 			}
 			
			sql.append(" order by u.pontuacao desc");
 			
 			TypedQuery<Usuario> qry = em.createQuery(sql.toString(), Usuario.class);
 			qry.setMaxResults(10);
 			
			if(!isPrimeiraConsulta){
 				qry.setParameter(1, pontuacaoUltimo);
 			}
 			
 			List<Usuario> ranking = qry.getResultList();
 			List<UsuarioDTO> usuarioList = new LinkedList<UsuarioDTO>();
 			
 			for (int i= 0, size = ranking.size(); i < size; i++) {
 				Usuario usuario =  ranking.get(i);
 				
 				UsuarioDTO usu = new UsuarioDTO();
 				
 				usu.setId(usuario.getId());
 				usu.setApelido(usuario.getApelido());
 				usu.setNome(usuario.getNome());
 				usu.setColocacaoRanking(new Long(i + 1));
 				usu.setImagem(usuario.getImagem());
				usu.setPontuacao(usuario.getPontuacao());
 				
 				usuarioList.add(usu);
 				
 			}
 				
 			return usuarioList;
 			 
 		}catch(NoResultException e){
 			return null;
 			
 		}catch(Throwable e){
 			return null;
 			
 		}
 	}
 
 	//TODO MODELAR
 	public List<EventoDTO> buscarRankingEvento(BigDecimal pontuacaoUltimo){
 		try{
 			boolean isPrimeiraConsulta = (pontuacaoUltimo == null);
 			
			if(!isPrimeiraConsulta){
 				return new ArrayList<EventoDTO>();
 			}
 			
 			StringBuilder sql = new StringBuilder("select e from Evento e ");
 			
			if(!isPrimeiraConsulta){
 				sql.append("where e.pontuacao > ?1 ");
 			}
 			
			sql.append("order by e.pontuacao desc");
 			
 			
 			TypedQuery<Evento> qry = em.createQuery(sql.toString(), Evento.class);
 			qry.setMaxResults(10);
 			
			if(!isPrimeiraConsulta){
 				qry.setParameter(1, pontuacaoUltimo);
 			}
 			
 			List<Evento> ranking = qry.getResultList();
 			List<EventoDTO> eventoList = new LinkedList<EventoDTO>();
 			
 			for (int i= 0, size = ranking.size(); i < size; i++) {
 				Evento evento =  ranking.get(i);
 				
 				EventoDTO eve = new EventoDTO();
 				
 				eve.setId(evento.getId());
 				eve.setNome(evento.getNome());
 				eve.setColocacao(new Long(i+1));
 				eve.setDescricao(evento.getDescricao());
 				eve.setImagem(evento.getImagem());
				eve.setPontuacao(evento.getPontuacao());
 				
 				eventoList.add(eve);
 				
 			}
 			return eventoList;
 			 
 		}catch(NoResultException e){
 			return null;
 			
 		}catch(Throwable e){
 			return null;
 			
 		}
 	}
 	
 	
 }

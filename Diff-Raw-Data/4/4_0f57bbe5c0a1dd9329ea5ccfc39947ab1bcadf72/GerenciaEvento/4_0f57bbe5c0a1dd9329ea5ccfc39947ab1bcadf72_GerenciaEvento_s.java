 package com.worktogether.buisiness;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import com.worktogether.dto.EventoDTO;
 import com.worktogether.dto.GeolocalizacaoDTO;
 import com.worktogether.dto.HabilidadeDTO;
 import com.worktogether.entities.Convite;
 import com.worktogether.entities.DominioTipoGeolocalizacao;
 import com.worktogether.entities.DominioTipoPresenca;
 import com.worktogether.entities.Evento;
 import com.worktogether.entities.Geolocalizacao;
 import com.worktogether.entities.Habilidade;
 import com.worktogether.entities.Presenca;
 import com.worktogether.entities.Usuario;
 import com.worktogether.entities.UsuarioEvento;
 import com.worktogether.gcm.GCMController;
 
 @Stateless
 public class GerenciaEvento {
 	
 	@PersistenceContext
 	private EntityManager em;
 	
 	public Long cadastrarEvento(Evento evento){
 		
 		List<Geolocalizacao> listGeo = evento.getGeolocalizacoes();
 		
 		Evento e = em.merge(evento);
 		Long id = e.getId();
 		
 		for (Geolocalizacao geolocalizacao : listGeo) {
 			geolocalizacao.setIdEvento(id);
 			em.persist(geolocalizacao);
 		}
 		
 		em.persist(e);
 		
 		//VINCULAR USUARIO AO EVENTO
 		UsuarioEvento ue = new UsuarioEvento();
 		ue.setIdUsuario(evento.getUsuario().getId());
 		ue.setIdEvento(id);
 		
 		em.persist(ue);
 		
 		//INDICA A PRESENCA DO USUARIO
 		Presenca p = new Presenca();
 		p.setDataHora(new Date());
 		p.setIdEvento(id);
 		p.setIdUsuario(evento.getUsuario().getId());
 		p.setTipoPresenca(DominioTipoPresenca.INDICADA);
 		
 		em.persist(p);
 		
 		return id;
 	}
 	
 	public List<HabilidadeDTO> buscarHabilidadeEvento(Long idEvento){
 		try{
 			if(idEvento == null || idEvento == 0){
 				throw new Exception("Parmetros invlidos para busca das habilidades do evento.");
 			}
 			
 			javax.persistence.Query qh = em.createNativeQuery("select h.id, h.descricao, h.tipo, h.dataHora from habilidade h, evento_habilidade eh where h.id = eh.id_habilidade and eh.id_evento = ?1", Habilidade.class);
 			qh.setParameter(1, idEvento);
 			List<Habilidade> habilidadeList = qh.getResultList();
 			
 			List<HabilidadeDTO> hlistDTO = new ArrayList<HabilidadeDTO>();
 			
 			for (Habilidade habilidade : habilidadeList) {
 				HabilidadeDTO hDTO = new HabilidadeDTO();
 				
 				hDTO.setDescricao(habilidade.getDescricao());
 				hDTO.setId(habilidade.getId());
 				hDTO.setTipo(habilidade.getTipo());
 				
 				hlistDTO.add(hDTO);
 			}
 			return hlistDTO;
 			
 		}catch(Throwable e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<GeolocalizacaoDTO> buscarGeolocalizacaoEvento(Long idEvento){
 		try{
 			if(idEvento == null || idEvento == 0){
 				throw new Exception("Parmetros invlidos para busca da geolocalizao.");
 			}
 			//GEOLOCALIZACAO
 			TypedQuery<Geolocalizacao> qgeo = em.createQuery("select g from Geolocalizacao g where g.idEvento = ?1",  Geolocalizacao.class);
 			qgeo.setParameter(1, idEvento);
 			List<Geolocalizacao> geoList = qgeo.getResultList();
 			List<GeolocalizacaoDTO> geoDTOList = new ArrayList<GeolocalizacaoDTO>();
 			
 			for (Geolocalizacao geolocalizacao : geoList) {
 				GeolocalizacaoDTO geoDTO = new GeolocalizacaoDTO();
 				geoDTO.setDataHora(geolocalizacao.getDataHora());
 				geoDTO.setId(geolocalizacao.getId());
 				geoDTO.setIdEvento(geolocalizacao.getIdEvento());
 				geoDTO.setLatitude(geolocalizacao.getLatitude().doubleValue());//TODO verificar precisao
 				geoDTO.setLongitude(geolocalizacao.getLongitude().doubleValue());
 				geoDTO.setRaio(geolocalizacao.getRaio());
 				geoDTO.setTipo(geolocalizacao.getTipo().toString());
 				geoDTOList.add(geoDTO);
 			}
 			return geoDTOList;
 			
 		}catch(Throwable e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<EventoDTO> buscarEventos(Usuario usuario){
 		//PEGA OS EVENTOS QUE O USUARIO ESTA VISUALIZANDO E CRIA UMA STRING
 		//COM OS IDS PARA SER ENVIADO COM PARAMETRO A PROCEDURE
 		List<Evento> list = usuario.getEventos();
 		StringBuilder eventoIds = new StringBuilder();
 		
 		if(list != null && list.size() > 0){
 			int size = list.size();
 			
 			for (int i=0, s = size; i < size; i++) {
 				eventoIds.append(list.get(i).getId());
 				
 				if(i != size - 1){
 					eventoIds.append(";");
 				}
 				
 			}
 		}
 		
 		//CHAMADA DA PROCEDURE SP_SUGERIR_EVENTOS PARA TODOS OS EVENTOS
 		Session session = (Session) em.getDelegate();
 		Query query = session.getNamedQuery("callStoreProcedureEventosSugeridos");
 		query.setParameter("id_evento_list", eventoIds.toString());
 		query.setParameter("id_usuario", usuario.getId());
 		query.setParameter("id_evento_ext", null);
 		
 		List<Evento> eventoList = (List<Evento>) query.list();
 		List<EventoDTO> retornoList = new ArrayList<EventoDTO>();
 		
 		for (int i=0, t = eventoList.size(); i < t; i++) {
 			Evento entity = (Evento) eventoList.get(i);
 			EventoDTO dto = new EventoDTO();
 			
 			dto.setId(entity.getId());
 			dto.setNome(entity.getNome());
 			dto.setDataHora(entity.getDataHora());
 			dto.setColocacao(entity.getColocacao());
 			dto.setDescricao(entity.getDescricao());
 			dto.setObjetivo(entity.getObjetivo());
 			dto.setImagem(entity.getImagem());
 			dto.setSugerido(entity.getSugerido());
 			dto.setPontuacao(entity.getPontuacao());
 			dto.setTipo(entity.getTipo().toString());
 			
 			retornoList.add(dto);
 		}
 		return retornoList;
 	} 
 	
 	//TODO Verificar modelagem
 	public EventoDTO buscarEvento(Long idEvento, Long idUsuario) {
 		try{
 			
 			if(idEvento == null || idUsuario == null){
 				return null;
 			}
 			
 			//CHAMADA DA PROCEDURE SP_SUGERIR_EVENTOS PARA BUSCAR O EVENTO DA NOTIFICACAO
 			Session session = (Session) em.getDelegate();
 			Query query = session.getNamedQuery("callStoreProcedureEventosSugeridos");
 			query.setParameter("id_evento_list", null);
 			query.setParameter("id_usuario", idUsuario);
 			query.setParameter("id_evento_ext", idEvento);
 			
 			List<Evento> eventoList = (List<Evento>) query.list();
 			List<EventoDTO> retornoList = new ArrayList<EventoDTO>();
 			
 			for (int i=0, t = eventoList.size(); i < t; i++) {
 				Evento entity = (Evento) eventoList.get(i);
 				EventoDTO dto = new EventoDTO();
 				
 				dto.setId(entity.getId());
 				dto.setNome(entity.getNome());
 				dto.setDataHora(entity.getDataHora());
 				dto.setColocacao(entity.getColocacao());
 				dto.setDescricao(entity.getDescricao());
 				dto.setObjetivo(entity.getObjetivo());
 				dto.setImagem(entity.getImagem());
 				dto.setSugerido(entity.getSugerido());
 				dto.setTipo(entity.getTipo().toString());
 				
 				return dto;
 			}
 			
 			return null;
 			
 		}catch(Throwable e){
 			return null;
 		}
 	} 
 	
 	//TODO MODELAR
 	public void enviarConviteAutomatico(String usuarioInfo, String localizacao){
 		
 		if(usuarioInfo != null && !"".equalsIgnoreCase(usuarioInfo) && 
 		   localizacao != null && !"".equalsIgnoreCase(localizacao)){
 			
 			//ID USUARIO;ID GCM
 			String[] latiLong = localizacao.split(";");
 			String[] ids = usuarioInfo.split(";");
 			
 			//TODO REMOVER COMENTARIO
 			//CHAMADA DA PROCEDURE sp_localizar_eventos_sugeridos PARA APENAS EVENTOS SUGERIDOS
 			Session session = (Session) em.getDelegate();
 			Query query = session.getNamedQuery("callStoreProcedureEventosSugeridosGeo");
 			query.setParameter("id_usuario", ids[0]);
 			query.setParameter("latitudeA", latiLong[0]);
 			query.setParameter("longitudeA", latiLong[1]);
 			
 			List<Evento> eventoList = (List<Evento>) query.list();
 			for (Evento evento : eventoList) {
 				new Thread(new GCMController(ids[1], evento.getId(), evento.getNome())).start();
 			}
 		}
 	} 
 	
 	public List<EventoDTO> buscarEventosUsuario(Long idUsuario){
 		try{
 			if(idUsuario == null || idUsuario == 0){
 				throw new Exception("Parmetros invlidos para busca dos eventos do usurio.");
 			}
 			
 			javax.persistence.Query qh = em.createNativeQuery("select * from evento e where e.id in (select ue.id_evento from usuario_evento ue where ue.id_usuario = ?1)", Evento.class);
 			qh.setParameter(1, idUsuario);
 			List<Evento> eventoList = qh.getResultList();
 			List<EventoDTO> eventoDTOList = new ArrayList<EventoDTO>();
 			
 			for (Evento entity : eventoList) {
 				EventoDTO dto = new EventoDTO();
 				
 				dto.setId(entity.getId());
 				dto.setNome(entity.getNome());
 				dto.setDataHora(entity.getDataHora());
 				dto.setColocacao(entity.getColocacao());
 				dto.setDescricao(entity.getDescricao());
 				dto.setObjetivo(entity.getObjetivo());
 				dto.setImagem(entity.getImagem());
 				dto.setSugerido(entity.getSugerido());
 				dto.setPontuacao(entity.getPontuacao());
 				dto.setTipo(entity.getTipo().toString());
 				
 				eventoDTOList.add(dto);
 			}
 			return eventoDTOList;
 			
 		}catch(Throwable e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public EventoDTO buscarEvento(Long idEvento){
 		try{
 			if(idEvento == null || idEvento == 0){
 				throw new Exception("Parmetros invlidos para busca do evento.");
 			}
 			
 			TypedQuery<Evento> qry = em.createQuery("select e from Evento e where id = ?1", Evento.class);
 			qry.setParameter(1, idEvento);
 			Evento entity = qry.getSingleResult();
 			
 			EventoDTO dto = new EventoDTO();
 			dto.setId(entity.getId());
 			dto.setNome(entity.getNome());
 			dto.setDataHora(entity.getDataHora());
 			dto.setColocacao(entity.getColocacao());
 			dto.setDescricao(entity.getDescricao());
 			dto.setObjetivo(entity.getObjetivo());
 			dto.setImagem(entity.getImagem());
 			dto.setSugerido(entity.getSugerido());
 			dto.setPontuacao(entity.getPontuacao());
 				
 			return dto;
 			
 		}catch(Throwable e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public String removerPresenca(Long idEvento, Long idUsuario){
 		try{
 			if(idEvento == null || idUsuario == null || idEvento == 0 || idUsuario == 0){
 				throw new Exception("Parmetros invlidos para indicao de presena.");
 			}
 			TypedQuery<Presenca> qry = em.createQuery("select p from Presenca p where p.idEvento = ?1 and p.idUsuario = ?2", Presenca.class);
 			qry.setParameter(1, idEvento);
 			qry.setParameter(2, idUsuario);
 			
 			List<Presenca> list = qry.getResultList();
 			
 			for (Presenca presenca : list) {
 				em.remove(presenca);
 			}
 			
 			//DESVINCULA USUARIO AO EVENTO
 			UsuarioEvento ue = new UsuarioEvento();
 			ue.setIdUsuario(idUsuario);
 			ue.setIdEvento(idEvento);
 			
 			em.remove(ue);
 			
 			return DominioStatusRequsicao.SUCESS.toString();
 			
 		}catch(Throwable e){
 			e.printStackTrace();
 			return DominioStatusRequsicao.SERVER_ERROR.toString();
 		}
 	}
 	
 	
 	public String indicarPresenca(Long idEvento, Long idUsuario){
 		try{
 			if(idEvento == null || idUsuario == null || idEvento == 0 || idUsuario == 0){
 				throw new Exception("Parmetros invlidos para indicao de presena.");
 			}
 			
 			try{
 				TypedQuery<Presenca> qry = em.createQuery("select p from Presenca p where p.idEvento = ?1 and p.idUsuario = ?2", Presenca.class);
 				qry.setParameter(1, idEvento);
 				qry.setParameter(2, idUsuario);
 				
 				List<Presenca> list = qry.getResultList();
 				
 				if(list.size() != 0){
 					return DominioStatusRequsicao.FOUND.toString();
 				}
 				
 			}catch(Throwable e){
 				e.printStackTrace();
 			}
 			
 			TypedQuery<Evento> qry = em.createQuery("select e from Evento e where id = ?1", Evento.class);
 			qry.setParameter(1, idEvento);
 			Evento evento = qry.getSingleResult();
 			
 			if(new Date().compareTo(evento.getDataHora()) > 0){
 				return DominioStatusRequsicao.DENIED.toString();
 			}
 			
 			Presenca p = new Presenca();
 			p.setDataHora(new Date());
 			p.setIdEvento(idEvento);
 			p.setIdUsuario(idUsuario);
 			p.setTipoPresenca(DominioTipoPresenca.INDICADA);
 			
 			em.persist(p); 
 			
 			//VINCULA USUARIO AO EVENTO
 			UsuarioEvento ue = new UsuarioEvento();
 			ue.setIdUsuario(idUsuario);
 			ue.setIdEvento(idEvento);
 			
 			em.persist(ue);
 			
 			return DominioStatusRequsicao.SUCESS.toString();
 			
 		}catch(Throwable e){
 			e.printStackTrace();
 			return DominioStatusRequsicao.SERVER_ERROR.toString();
 		}
 	}
 	
 	//TODO MODELAR
 	public String confirmarPresenca(Long idEvento, Long idUsuario, BigDecimal latitude, BigDecimal longitude){
 		try{
 			if(idEvento == null || idUsuario == null || latitude == null || longitude == null){
 				throw new Exception("Parmetros invlidos para confirmao de presena.");
 			}
 			
 			TypedQuery<Geolocalizacao> qGeo = em.createQuery("select g from Geolocalizacao g where g.idEvento = ?1 and g.tipo = ?2",  Geolocalizacao.class);
 			qGeo.setParameter(1, idEvento);
 			qGeo.setParameter(2, DominioTipoGeolocalizacao.EVENTO);
 			Geolocalizacao geo = qGeo.getSingleResult();
 			
 			TypedQuery<Presenca> qry = em.createQuery("select p from Presenca p where p.idEvento = ?1 and p.idUsuario = ?2", Presenca.class);
 			qry.setParameter(1, idEvento);
 			qry.setParameter(2, idUsuario);
 			Presenca presenca = qry.getSingleResult();
 			
 			Session session = (Session) em.getDelegate();
 			Distancia work = new Distancia(latitude, longitude, geo.getLatitude(), geo.getLongitude());
 			session.doWork(work);
 			
 			double distancia = work.getDistancia();
 			
 			if(distancia >= 0){
 				if(distancia <=  geo.getRaio()){
 					
 					presenca.setTipoPresenca(DominioTipoPresenca.CONFIRMADA);
 					em.persist(presenca);
 				}
 			}else{
 				throw new Exception("Erro ao calcular distncia.");
 			}
 			
 			return DominioStatusRequsicao.SUCESS.toString();
 			
 		}catch(Throwable e){
 			e.printStackTrace();
 			return DominioStatusRequsicao.SERVER_ERROR.toString();
 		}
 	} 
 	
 	
 	private void vincularUsuarioEvento(Usuario usuario, List<Evento> eventos){} 
 	private void validarRaio(Geolocalizacao geoLocalizacao, String localizacaoAtual){} 
 	private void validarConfirmacaoPresenca(Evento evento, String localizacaoAtual){} 
 	private void salvarPresenca(Presenca presenca){} 
 	
 	
 	private String atualizarListaEventosUsuairo(Usuario usuario, List<Presenca> eventos){
 		return null;
 	} 
 	
 	private List<Evento> verificarAtualizacaoEventos(Long idUltimoEvento){
 		return null;
 	} 
 	private List<Evento> marcarEventosSugeridos(List<Evento> eventos, List<Evento> eventosSugeridos){
 		return null;
 	} 
 	private List<Convite> buscarEventosPontosProximos(String localizacao, Long idUsuario){
 		return null;
 	} 
 	private void armazenarConvitesEnviados(Long idUsuario, List<Convite> convites){} 
 }

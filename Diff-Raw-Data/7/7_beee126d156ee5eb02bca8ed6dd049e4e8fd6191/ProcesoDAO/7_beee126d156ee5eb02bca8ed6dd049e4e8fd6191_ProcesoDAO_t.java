 package py.com.ait.gestion.persistence;
 
 import java.util.Calendar;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.slf4j.Logger;
 import org.ticpy.tekoporu.stereotype.PersistenceController;
 import org.ticpy.tekoporu.template.JPACrud;
 
 import py.com.ait.gestion.constant.Definiciones;
 import py.com.ait.gestion.domain.Actividad;
 import py.com.ait.gestion.domain.Proceso;
 
 @PersistenceController
 public class ProcesoDAO extends JPACrud<Proceso, Long> {
 
 	private static final long serialVersionUID = 1L;
 
 	@Inject
 	private EntityManager em;
 
 	@Inject
 	private Logger logger;
 
 	public Long getMaxId() {
 
 		Query q = this.em.createQuery("select max(p.id) from Proceso p");
 		return ((Long) q.getSingleResult());
 	}
 
 	public String getLastSequence(Long cronogramaId) {
 
 		int year = Calendar.getInstance().get(Calendar.YEAR);
 		/*
 		 * String nroProceso =
 		 * "substring(p.nroProceso,1,locate('/',p.nroProceso)-1)"; String query
 		 * = "select cast(max(cast(" + nroProceso + " as int)) as string)" +
 		 * " from Proceso p " +
 		 * "where substring(p.nroProceso,locate('/',p.nroProceso)+1) = '" + year
 		 * + "'";
 		 */
 		String query = "select cast(max(cast( "
 				+ "substring(p.nroProceso,locate('/', p.nroProceso)+1) "
 				+ "as int)) as string) "
 				+ "from Proceso p  "
 				+ "where substring(p.nroProceso,locate('_', p.nroProceso)+1,4) = :year "
 				+ "and p.cronograma.cronogramaId = :cronogramaId";
 
 		this.logger.info("ProcesoDAO.getLastSequence() query: " + query);
 		Query q = this.em.createQuery(query);
 		q.setParameter("year", year + "");
 		q.setParameter("cronogramaId", cronogramaId);
 		String result = ((String) q.getSingleResult());
 		if (result == null) {
 			result = "0";
 		}
 		this.logger.info("ProcesoDAO.getLastSequence() result: " + result);
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Actividad> getActividadesByProceso(Proceso procesoSeleccionado,
 			boolean isAdminUser, Long currentUserId) {
 
 		String filtro = "where a.master.procesoId = :proceso";
 		// si no es admin, agregar filtro por el usuario actual
 		if (!isAdminUser
 				&& procesoSeleccionado.getResponsable().getUsuarioId() != currentUserId) {
 
 			// De acuerdo a lo comentado por ggil en reunión de 09/10/2013, esto
 			// no aplica
 			// filtro += " and a.responsable.usuarioId = " + currentUserId;
 
 		}
 		Query q = this.em.createQuery("select a from Actividad a " + filtro
 				+ " order by a.fechaCreacion");
 		q.setParameter("proceso", procesoSeleccionado.getProcesoId());
 
 		return (q.getResultList());
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Proceso> getProcesos(String filtroEstadoProceso,
 			boolean isAdminUser, Long currentUserId) {
 
 		String filtro = "where p.procesoId = a.master.procesoId";
 		if (filtroEstadoProceso.equals("C")) {
 
 			filtro += " and p.estado in ("
 					+ Definiciones.EstadoProceso.getEstadosCerrados() + ")";
 		} else if (filtroEstadoProceso.equals("A")) {
 
 			filtro += " and p.estado not in ("
 					+ Definiciones.EstadoProceso.getEstadosCerrados() + ")";
 			filtro += " and a.estado not in ("
 					+ Definiciones.EstadoActividad.getEstadosCerrados() + ")";
 		}
 		// si no soy admin agregar filtros por usuario actual
 		if (!isAdminUser) {
 
 			filtro += " and (p.responsable.usuarioId = " + currentUserId
 					+ " or a.responsable.usuarioId = " + currentUserId + ")";
 		}
 
 		Query q = this.em
 				.createQuery("select distinct p from Proceso p, Actividad a "
 						+ filtro + " order by p.nroProceso");
 		return (q.getResultList());
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Proceso> getProcesosSinActividades(boolean isAdminUser,
 			Long currentUserId) {
 
 		String filtro = "";
 		if (!isAdminUser) {
 
 			filtro += " and (p.responsable.usuarioId = " + currentUserId + ")";
 		}
 		Query q = this.em
 				.createQuery("select distinct p from Proceso p "
 						+ "where p.id not in (select distinct a.master.procesoId from Actividad a) "
 						+ filtro + "order by p.nroProceso");
 
 		return (q.getResultList());
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Long> getProcesoIdsForUser(String filtroEstado, boolean isAdminUser, Long usuarioId) {
 
 		List<Long> result = null;
 		String filtro = "where p.procesoId = a.master.procesoId";
 		if (filtroEstado.equals("C")) {
 
 			filtro += " and p.estado in ("
 					+ Definiciones.EstadoProceso.getEstadosCerrados() + ")";
 		} else if (filtroEstado.equals("A")) {
 
 			filtro += " and p.estado not in ("
 					+ Definiciones.EstadoProceso.getEstadosCerrados() + ")";
 			filtro += " and a.estado not in ("
 					+ Definiciones.EstadoActividad.getEstadosCerrados() + ")";
 		}
 
 		// si no soy admin agregar filtros por usuario actual
 		if (!isAdminUser) {
 
 			filtro += " and (p.responsable.usuarioId = " + usuarioId
 					+ " or a.responsable.usuarioId = " + usuarioId + ")";
 		}
 
 		Query q = this.em
 				.createQuery("select distinct p.procesoId from Proceso p, Actividad a "
						+ filtro + " order by p.procesoId");
 
 		result = q.getResultList();
 
 		if (!"C".equals(filtroEstado)){
 			Query q2 = this.em
 					.createQuery("select distinct p.procesoId from Proceso p" +
 							" where p.id not in (select a.master.procesoId from Actividad a)" +
							" and p.responsable.usuarioId = " + usuarioId+ " order by p.procesoId");
 
 			result.addAll(q2.getResultList());
 		}
 			return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Proceso> getProcesosByCronogramaAndProcesosId(
 			Long cronogramaId, List<Long> procesosId) {
 
 		String filtro = "where p.cronograma.cronogramaId = :cronogramaId and p.procesoId IN (:procesosId)";
 
 		Query q = this.em
 				.createQuery("select p from Proceso p "
 						+ filtro + " order by p.procesoId");
 		q.setParameter("cronogramaId", cronogramaId);
 
 		q.setParameter("procesosId", procesosId);
 
 		return (q.getResultList());
 	}
 
 	public String getCountProcesosByCronogramaAndProcesosId(
 			Long cronogramaId, List<Long> procesosId) {
 
 		String filtro = "where p.cronograma.cronogramaId = :cronogramaId and p.procesoId IN (:procesosId)";
 
 		Query q = em.createQuery("select count(p.id) from Proceso p " + filtro);
 
 		q.setParameter("cronogramaId", cronogramaId);
 
 		q.setParameter("procesosId", procesosId);
 		Long cantProcesos = (Long) q.getSingleResult(); 
 
 		return cantProcesos.toString();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Proceso> getProcesosByCronogramaClienteAndProcesosId(
 			Long cronogramaId, Long clienteId,
 			List<Long> procesosId) {
 
 		String filtro = "where p.procesoId IN (:procesosId)";
 
 		if (cronogramaId!=null)
 			filtro += " and p.cronograma.cronogramaId = :cronogramaId";
 
 		if (clienteId!=null)
 			filtro += " and p.cliente.clienteId = :clienteId";
 
 		Query q = this.em
 				.createQuery("select p from Proceso p "
 						+ filtro + " order by p.procesoId");
 
 		q.setParameter("procesosId", procesosId);
 
 		if (cronogramaId != null)
 			q.setParameter("cronogramaId", cronogramaId);
 
 		if (clienteId != null)
 			q.setParameter("clienteId", clienteId);
 
 		return (q.getResultList());
 	}
 
 }

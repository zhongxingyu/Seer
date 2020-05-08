 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.carreras.servicios.impl;
 
 import com.carreras.config.HibernateUtil;
 import com.carreras.dominio.modelo.Tiempo;
import com.carreras.dominio.modelo.TipoTiempo;
 import com.carreras.servicios.TiempoManager;
 import java.util.List;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 
 /**
  *
  * @author Fanky10 <fanky10@gmail.com>
  */
 public class TiempoManagerImpl implements TiempoManager {
 
     @Override
     public Integer save(Tiempo Tiempo) {
         Session session = null;
         try {
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             return (Integer) session.save(Tiempo);
 
         } catch (HibernateException he) {
             he.printStackTrace();
         } finally {
             session.getTransaction().commit();
         }
         return null;
     }
 
     @Override
     public Tiempo getOne(Integer id) {
         Session session = null;
         try {
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             org.hibernate.Query q = session.createQuery(" from " + Tiempo.class.getName() + " where id=" + id);
             return (Tiempo) q.list().get(0);
 
         } catch (HibernateException he) {
             he.printStackTrace();
         } finally {
             session.getTransaction().commit();
         }
         return null;
     }
 
     @Override
     public List<Tiempo> getAll() {
         Session session = null;
         try {
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             org.hibernate.Query q = session.createQuery(" from " + Tiempo.class.getName());
             return q.list();
 
         } catch (HibernateException he) {
             he.printStackTrace();
         } finally {
             session.getTransaction().commit();
         }
         return null;
     }
 
     @Override
     public List<Tiempo> getTiemposCarril(Integer idCarril) {
         Session session = null;
         try {
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             org.hibernate.Query q = session.createQuery(" from " + Tiempo.class.getName()+" t WHERE t.carril.id = "+idCarril);
             return q.list();
 
         } catch (HibernateException he) {
             he.printStackTrace();
         } finally {
             session.getTransaction().commit();
         }
         return null;
     }
 
     @Override
     public Tiempo getTiempo(Integer idCarril, Integer idTipoTiempo) {
         Session session = null;
         try {
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             org.hibernate.Query q = session.createQuery(" from " + Tiempo.class.getName()+" t"
                     + " WHERE t.carril.id = "+idCarril
                     + " AND t.tipoTiempo.id ="+idTipoTiempo);
             return (Tiempo)q.list().get(0);
 
         } catch (HibernateException he) {
             he.printStackTrace();
         } finally {
             session.getTransaction().commit();
         }
         return null;
     }
 
     @Override
     public Tiempo getMejorTiempo(Integer idTorneo, Integer idInscripto) {
         Session session = null;
         try {
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             StringBuilder sbQuery = new StringBuilder();
             sbQuery.append(" FROM " + Tiempo.class.getName()+" t");
             sbQuery.append(" WHERE t.carril.inscriptoCompetencia.inscripto.id = "+idInscripto);
             sbQuery.append(" AND  t.carril.inscriptoCompetencia.competencia.torneo.id = "+idTorneo);
            sbQuery.append(" AND  t.tipoTiempo.id = "+TipoTiempo.ID_TIEMPO_FIN);
            sbQuery.append(" ORDER BY t.tiempo asc");
             org.hibernate.Query q = session.createQuery(sbQuery.toString());
             q.setMaxResults(1);
             List<Tiempo> tiempos = q.list();
             if(tiempos.isEmpty()){
                 return null;
             }
             return (Tiempo)q.list().get(0);
 
         } catch (HibernateException he) {
             he.printStackTrace();
             session.getTransaction().rollback();
         } finally {
             session.getTransaction().commit();
         }
         return null;
     }
 }

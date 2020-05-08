 package ar.noxit.ehockey.service.impl;
 
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

 import ar.noxit.ehockey.dao.IClubDao;
 import ar.noxit.ehockey.dao.IDivisionDao;
 import ar.noxit.ehockey.dao.IJugadorDao;
 import ar.noxit.ehockey.dao.ISectorDao;
 import ar.noxit.ehockey.exception.SinClubException;
 import ar.noxit.ehockey.model.Jugador;
 import ar.noxit.ehockey.service.IJugadorService;
 import ar.noxit.ehockey.web.pages.jugadores.JugadorPlano;
 import ar.noxit.exceptions.NoxitException;
 import ar.noxit.exceptions.persistence.PersistenceException;
 
 public class JugadorService implements IJugadorService {
 
     private IJugadorDao jugadorDao;
     private IClubDao clubDao;
     private IDivisionDao divisionDao;
     private ISectorDao sectorDao;
 
     @Override
     @Transactional
     public void add(JugadorPlano jugadorPlano) throws NoxitException {
         jugadorDao.save(ensamblar(jugadorPlano));
     }
 
     @Override
     @Transactional
     public void update(JugadorPlano jugadorPlano) throws NoxitException {
         Jugador jugador = jugadorDao.get(jugadorPlano.getFicha());
         jugador.setClub(clubDao.get(jugadorPlano.getClubId()));
         jugador.setDivision(divisionDao.get(jugadorPlano.getDivisionId()));
         jugador.setSector(sectorDao.get(jugadorPlano.getSectorId()));
         jugador.setApellido(jugadorPlano.getApellido());
         jugador.setNombre(jugadorPlano.getNombre());
         setearDatos(jugadorPlano, jugador);
     }
 
     @Override
     @Transactional
     public void remove(Jugador jugador) throws NoxitException {
         if (jugador == null) {
             throw new IllegalArgumentException();
         }
         // No se elimina el jugador, solo se deja en estado inactivo.
         jugador.bajaJugador();
     }
 
     @Override
     @Transactional(readOnly = true)
     public Jugador get(Integer id) throws NoxitException {
         return jugadorDao.get(id);
     }
 
     @Override
     @Transactional(readOnly = true)
     public Jugador getActive(Integer id) throws NoxitException {
         return jugadorDao.getActiveJugadorById(id);
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<Jugador> getAll() throws NoxitException {
         return jugadorDao.getAll();
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<Jugador> getAllActive() throws NoxitException {
         return jugadorDao.getAllActive();
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<Jugador> getAllByClubDivisionSector(Integer clubid,
             Integer divisionid, Integer sectorid) throws NoxitException {
         return jugadorDao.getJugadoresFromClubDivisionSector(clubid,
                 divisionid, sectorid);
     }
 
     public void setJugadorDao(IJugadorDao jugadorDao) {
         this.jugadorDao = jugadorDao;
     }
 
     public void setClubDao(IClubDao clubDao) {
         this.clubDao = clubDao;
     }
 
     public void setDivisionDao(IDivisionDao divisionDao) {
         this.divisionDao = divisionDao;
     }
 
     public void setSectorDao(ISectorDao sectorDao) {
         this.sectorDao = sectorDao;
     }
 
     private Jugador ensamblar(JugadorPlano jugadorPlano)
             throws PersistenceException {
         Jugador jugador = clubDao.get(jugadorPlano.getClubId())
                 .crearNuevoJugador(jugadorPlano.getApellido(),
                         jugadorPlano.getNombre(),
                         sectorDao.get(jugadorPlano.getSectorId()),
                         divisionDao.get(jugadorPlano.getDivisionId()));
         setearDatos(jugadorPlano, jugador);
         return jugador;
     }
 
     private void setearDatos(JugadorPlano jugadorPlano, Jugador jugador) {
         jugador.setFechaNacimiento(jugadorPlano.getFechaNacimiento());
         jugador.setLetraJugador(jugadorPlano.getLetraJugador());
         jugador.setNumeroDocumento(jugadorPlano.getNumeroDocumento());
         jugador.setTelefono(jugadorPlano.getTelefono());
         jugador.setTipoDocumento(jugadorPlano.getTipoDocumento());
     }
 
     public JugadorPlano aplanar(Jugador jugador) {
         JugadorPlano jugadorPlano = new JugadorPlano();
         jugadorPlano.setApellido(jugador.getApellido());
         try {
             jugadorPlano.setClubId(jugador.getClub().getId());
         } catch (SinClubException e) {
             jugadorPlano.setClubId(null);
         }
         jugadorPlano.setDivisionId(jugador.getDivision().getId());
         jugadorPlano.setFechaAlta(jugador.getFechaAlta());
         jugadorPlano.setFechaNacimiento(jugador.getFechaNacimiento());
         jugadorPlano.setFicha(jugador.getFicha());
         jugadorPlano.setLetraJugador(jugador.getLetraJugador());
         jugadorPlano.setNombre(jugador.getNombre());
         jugadorPlano.setNumeroDocumento(jugador.getDocumento());
         jugadorPlano.setSectorId(jugador.getSector().getId());
         jugadorPlano.setTelefono(jugador.getTelefono());
         jugadorPlano.setTipoDocumento(jugador.getTipoDocumento());
         return jugadorPlano;
     }
 }

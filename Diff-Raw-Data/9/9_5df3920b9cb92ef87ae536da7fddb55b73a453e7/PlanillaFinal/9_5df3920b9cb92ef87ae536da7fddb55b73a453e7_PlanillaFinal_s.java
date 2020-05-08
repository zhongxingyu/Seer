 package ar.noxit.ehockey.model;
 
 import ar.noxit.ehockey.exception.JugadorSinTarjetasException;
 import ar.noxit.ehockey.exception.JugadorYaPerteneceAListaException;
 import ar.noxit.ehockey.exception.PlanillaNoModificableException;
 import ar.noxit.ehockey.exception.PlanillaNoVencidaException;
 import ar.noxit.ehockey.exception.ReglaNegocioException;
 import ar.noxit.ehockey.exception.ViolacionReglaNegocioException;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 import org.apache.commons.lang.Validate;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.LocalDateTime;
 
 public class PlanillaFinal implements Planilla {
 
     private int id;
 
     private DatosEquipoPlanilla datosLocal = new DatosEquipoPlanilla();
     private DatosEquipoPlanilla datosVisitante = new DatosEquipoPlanilla();
 
     private String observaciones;
 
     private Partido partido;
     private String comentario;
     private EstadoPlanilla estado;
 
     protected PlanillaFinal() {
     }
 
     public PlanillaFinal(PlanillaPrecargada original, LocalDateTime now) {
         Validate.notNull(original);
         Validate.notNull(now, "now no puede ser null");
 
         // partido
         this.partido = original.getPartido();
 
         // estado inicial
         this.estado = new EstadoPlanillaCargada();
 
         // jugadores en la planilla
         this.datosLocal.asignarJugadores(original.getJugadoresLocales());
         this.datosVisitante.asignarJugadores(original.getJugadoresVisitantes());
 
         // verificar que no este vencida
         this.estado = this.estado.verificarVencimiento(new PlanillaFinalVencible(), now);
     }
 
     private void agregarJugador(Jugador jugador, Set<Jugador> jugadores) throws JugadorYaPerteneceAListaException {
         Validate.notNull(jugador, "jugador no puede ser null");
 
         if (jugadores.contains(jugador)) {
             throw new JugadorYaPerteneceAListaException("el jugador ya está en la lista de buena fe");
         }
         jugadores.add(jugador);
     }
 
     private void agregarJugadores(Collection<Jugador> jugadoresNuevos, Set<Jugador> jugadores) {
         jugadores.clear();
         for (Jugador j : jugadoresNuevos) {
             jugadores.add(j);
         }
     }
 
     // VALIDADORES
     private void validatePlanillaCerrada() throws PlanillaNoModificableException {
         if (this.estado.estaFinalizada())
             throw new PlanillaNoModificableException();
     }
 
     private Equipo getEquipoDeJugador(Jugador jugador) {
         Validate.notNull(jugador);
 
         boolean jugoLocal = datosLocal.jugo(jugador);
         boolean jugoVisitante = datosVisitante.jugo(jugador);
 
         if (jugoLocal && jugoVisitante) {
             throw new ViolacionReglaNegocioException("el jugador jugo en los dos equipos");
         }
 
         if (jugoLocal) {
             return getLocal();
         }
         if (jugoVisitante) {
             return getVisitante();
         }
 
         throw new ViolacionReglaNegocioException("el jugador no jugo en ningun lado");
     }
 
     private void amonestar(Map<Jugador, TarjetasPartido> tarjetasLocal) {
         for (Map.Entry<Jugador, TarjetasPartido> entry : tarjetasLocal.entrySet()) {
             Jugador jugador = entry.getKey();
             TarjetasPartido tarjetasPartido = entry.getValue();
             Equipo equipoJugador = getEquipoDeJugador(jugador);
 
             jugador.amonestar(partido, equipoJugador, tarjetasPartido);
         }
     }
 
     private boolean isVencida(LocalDateTime now) {
         Validate.notNull(now, "now no puede ser null");
 
         LocalDateTime sigLunes = this.partido.getInicio().plusDays(1);
         while (sigLunes.getDayOfWeek() != DateTimeConstants.MONDAY) {
             sigLunes = sigLunes.plusDays(1);
         }
        sigLunes = sigLunes.withHourOfDay(18).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
         return now.isAfter(sigLunes) || now.isEqual(sigLunes);
     }
 
     private class PlanillaFinalVencible implements PlanillaVencible {
 
         @Override
         public void checkVencimiento(LocalDateTime now) throws PlanillaNoVencidaException {
             if (!isVencida(now)) {
                 throw new PlanillaNoVencidaException("la planilla no esta para vencer");
             }
         }
     }
 
     private class PlanillaFinalComentable implements Comentable {
 
         @Override
         public void comentar(String comentario) {
             PlanillaFinal.this.comentario = comentario;
         }
     }
 
     private class PlanillaFinalPublicable implements PlanillaPublicable {
 
         @Override
         public void checkPublicable() throws ReglaNegocioException {
             CompositeReglaDeNegocioException composite = new CompositeReglaDeNegocioException();
 
             String equipo[] = { "Local", "Visitante" };
             DatosEquipoPlanilla datos[] = { datosLocal, datosVisitante };
             int i = 0;
             for (DatosEquipoPlanilla dep : datos) {
                 try {
                     dep.checkCompleta(equipo[i]);
                 } catch (ReglaNegocioException e) {
                     composite.add(e);
                 }
                 i++;
             }
 
             composite.throwsIfNotEmpty();
         }
     }
 
     private class PlanillaFinalFinalizable implements PlanillaFinalizable {
 
         @Override
         public void finalizarPlanilla() throws ReglaNegocioException {
             amonestar(datosLocal.getTarjetas());
             amonestar(datosVisitante.getTarjetas());
         }
     }
 
     public void publicar() throws ReglaNegocioException {
         estado = estado.publicar(new PlanillaFinalPublicable());
     }
 
     public void validar() throws ReglaNegocioException {
         estado = estado.validar(new PlanillaFinalFinalizable());
     }
 
     public void rechazar(String comentario) throws ReglaNegocioException {
         estado = estado.rechazar(new PlanillaFinalComentable(), comentario);
     }
 
     public void verificarVencimiento(LocalDateTime now) {
         estado = estado.verificarVencimiento(new PlanillaFinalVencible(), now);
     }
 
     public void finalizar() throws ReglaNegocioException {
         estado = estado.cerrarPlanillaVencida(new PlanillaFinalPublicable(), new PlanillaFinalFinalizable());
     }
 
     public TarjetasPartido getTarjetasDe(Jugador object) throws JugadorSinTarjetasException {
         try {
             return datosLocal.buscarJugador(object);
         } catch (JugadorSinTarjetasException e) {
             return datosVisitante.buscarJugador(object);
             // TODO validar que no juegue en los dos equipos
         }
     }
 
     public void agregarJugadorLocal(Jugador jugador) throws PlanillaNoModificableException,
             JugadorYaPerteneceAListaException {
 
         validatePlanillaCerrada();
         agregarJugador(jugador, this.datosLocal.getJugadores());
     }
 
     public void agregarJugadorVisitante(Jugador jugador) throws PlanillaNoModificableException,
             JugadorYaPerteneceAListaException {
 
         validatePlanillaCerrada();
         agregarJugador(jugador, this.datosVisitante.getJugadores());
     }
 
     public void setJugadoresLocal(Collection<Jugador> jugadores) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         agregarJugadores(jugadores, this.datosLocal.getJugadores());
     }
 
     public void setJugadoresVisitante(Collection<Jugador> jugadores) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         agregarJugadores(jugadores, this.datosVisitante.getJugadores());
     }
 
     public void setGolesLocal(Integer goles) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosLocal.setGoles(goles);
     }
 
     public void setGolesVisitante(Integer goles) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosVisitante.setGoles(goles);
     }
 
     public void setArbitroL(String arbitro) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosLocal.setArbitro(arbitro);
     }
 
     public void setDtL(String dt) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosLocal.setdT(dt);
     }
 
     public void setGoleadoresL(String goleadores) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosLocal.setGoleadores(goleadores);
     }
 
     public void setJuezMesaL(String juezMesa) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosLocal.setJuezDeMesa(juezMesa);
     }
 
     public void setMedicoL(String medico) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosLocal.setMedico(medico);
     }
 
     public void setCapitanL(String capitan) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosLocal.setCapitan(capitan);
     }
 
     public void setCapitanV(String capitan) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosVisitante.setCapitan(capitan);
     }
 
     public void setPfL(String pf) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosLocal.setpFisico(pf);
     };
 
     public void setArbitroV(String arbitro) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosVisitante.setArbitro(arbitro);
     }
 
     public void setDtV(String dt) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosVisitante.setdT(dt);
     }
 
     public void setGoleadoresV(String goleadores) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosVisitante.setGoleadores(goleadores);
     }
 
     public void setJuezMesaV(String juezMesa) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosVisitante.setJuezDeMesa(juezMesa);
     }
 
     public void setMedicoV(String medico) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosVisitante.setMedico(medico);
     }
 
     public void setPfV(String pf) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.datosVisitante.setpFisico(pf);
     }
 
     public void setObservaciones(String observaciones) throws PlanillaNoModificableException {
         validatePlanillaCerrada();
 
         this.observaciones = observaciones;
     }
 
     public String getComentario() {
         return comentario;
     }
 
     public Equipo getLocal() {
         return partido.getLocal();
     }
 
     public Equipo getVisitante() {
         return partido.getVisitante();
     }
 
     public Set<Jugador> getJugadoresL() {
         return this.datosLocal.getJugadores();
     }
 
     public Set<Jugador> getJugadoresV() {
         return this.datosVisitante.getJugadores();
     }
 
     public DatosEquipoPlanilla getDatosLocal() {
         return this.datosLocal;
     }
 
     public DatosEquipoPlanilla getDatosVisitante() {
         return this.datosVisitante;
     }
 
     public int getId() {
         return id;
     }
 
     public boolean isFinalizada() {
         return estado.estaFinalizada();
     }
 
     public Integer getGolesLocal() {
         return this.datosLocal.getGoles();
     }
 
     public Integer getGolesVisitante() {
         return this.datosVisitante.getGoles();
     }
 
     public String getObservaciones() {
         return observaciones;
     }
 
     @Override
     public Partido getPartido() {
         return this.partido;
     }
 
     public String getEstado() {
         return this.estado.toString();
     }
 
     public boolean isRechazada() {
         return this.estado.estaRechazada();
     }
 
     public boolean isEditable() {
         return this.estado.esEditable();
     }
 
     public boolean isVencida() {
         return this.estado.estaVencida();
     }
 
     public boolean isPublicada() {
         return this.estado.estaPublicada();
     }
 
     public String getEstadoReducido() {
         return this.estado.toStringReducido();
     }
 }

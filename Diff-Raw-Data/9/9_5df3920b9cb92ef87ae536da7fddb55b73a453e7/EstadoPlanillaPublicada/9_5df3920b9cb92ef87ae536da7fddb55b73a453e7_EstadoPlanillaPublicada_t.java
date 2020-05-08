 package ar.noxit.ehockey.model;
 
 import ar.noxit.ehockey.exception.PlanillaNoVencidaException;
 import ar.noxit.ehockey.exception.ReglaNegocioException;
 import ar.noxit.ehockey.exception.TransicionEstadoInvalidaException;
 import org.apache.commons.lang.Validate;
 import org.joda.time.LocalDateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class EstadoPlanillaPublicada extends EstadoPlanilla {
 
     private static final Logger logger = LoggerFactory.getLogger(EstadoPlanillaPublicada.class);
 
     @Override
     public EstadoPlanilla publicar(PlanillaPublicable publicable) throws TransicionEstadoInvalidaException {
         throw new TransicionEstadoInvalidaException("la planilla ya está publicada");
     }
 
     @Override
     public EstadoPlanilla rechazar(Comentable comentable, String mensaje) throws TransicionEstadoInvalidaException {
         Validate.notNull(comentable);
         Validate.notNull(mensaje);
 
         comentable.comentar(mensaje);
         return new EstadoPlanillaRechazada();
     }
 
     @Override
     public EstadoPlanilla validar(PlanillaFinalizable finalizable) throws ReglaNegocioException {
         Validate.notNull(finalizable);
 
         finalizable.finalizarPlanilla();
         return new EstadoPlanillaFinalizada();
     }
 
     @Override
     public String toString() {
         return "Publicada (esperando validación)";
     }
 
     @Override
     public String toStringReducido() {
         return "Publicada";
     }
 
     @Override
     public EstadoPlanilla verificarVencimiento(PlanillaVencible vencible, LocalDateTime now) {
         try {
            vencible.checkVencimiento(now.minusHours(6));
             // TODO
             // aca colocar sancion al visitante por que no valido la planilla
             return new EstadoPlanillaFinalizada();
         } catch (PlanillaNoVencidaException e) {
             logger.debug("planilla no vencida", e);
             return this;
         }
     }
 
     @Override
     public boolean estaPublicada() {
         return true;
     }
 }

 package ar.noxit.ehockey.model;
 
 import org.apache.commons.lang.Validate;
 
import ar.noxit.ehockey.exception.PlanillaIncompletaException;
 import ar.noxit.ehockey.exception.ReglaNegocioException;
 import ar.noxit.ehockey.exception.TransicionEstadoInvalidaException;
 
 public class EstadoPlanillaCargada extends EstadoPlanilla {
 
     @Override
     public EstadoPlanilla publicar(PlanillaPublicable publicable) throws ReglaNegocioException {
         Validate.notNull(publicable);
 
        if (publicable.checkPublicable()) {
            return new EstadoPlanillaPublicada();
        }
        throw new PlanillaIncompletaException();
     }
 
     @Override
     public EstadoPlanilla rechazar(Comentable comentable, String mensaje) throws TransicionEstadoInvalidaException {
         throw new TransicionEstadoInvalidaException("la planilla no está publicada");
     }
 
     @Override
     public EstadoPlanilla validar(PlanillaFinalizable finalizable) throws TransicionEstadoInvalidaException {
         throw new TransicionEstadoInvalidaException("la planilla no está publicada");
     }
 
     @Override
     public boolean estaRechazada() {
         return false;
     }
 }

 package core.schedule;
 
 import core.schedule.task.Task;
 import core.utils.Hour;
 import java.util.Iterator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * La clase {@code DayliSchedule} es un schedule para un día completo. 
  * Se pueden schedulear multiples {@code Task}s durante el día, pero solo puede
  * existir una(1) task por momento del día.
  * @author kiira
  * @version 1.0
  */
 public class DailySchedule extends Schedule {
 
     /** Estructura de datos que mantiene las {@code Task}s scheduleadas. */
     private SortedSet<Task> tasks;
 
     /**
      * Inicializa el schedule.
      */
     public DailySchedule() {
         this.tasks = new TreeSet<Task>();
     }
 
     /**
      * Permite crear un tarea básica y asignarla al {@code DailySchedule}.
      * @param init horario de inicio de la tarea.
      * @param end horario de fin de la tarea.
      * @param desc descripcion de la tarea.
      * @return {@code true} si la tarea se pudo crear y asignar correctamente
      * (si el período indicado por la tarea no esta ocupado); 
      * {@code false} si la tarea no se pudo crear y asignar correctamente.
      */
     public boolean createTask( Hour init, Hour end, String desc ) {
         Task newTask = new Task( init, end, desc );
 
         return this.addTask( newTask );
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean addTask( Task task ) {
         boolean res = this.isPeriodAvailable( task.getInitHour(),
                                               task.getEndHour() );
 
         if ( res )
             this.tasks.add( task );
 
         return res;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean removeTask( Task task ) {
         boolean res = this.tasks.remove( task );
 
         if ( res )
             task.delete();
 
         return res;
     }
 
     /**
      * Indica si el período de tiempo establecido se encuentra libre en el
      * schedule.
      * @param init horario de inicio del período.
      * @param end horario de fin del período.
      * @return {@code true} si el período de tiempo esta disponible.;
      * {@code false} si el periodo de tiempo no esta disponible.
      */
     private boolean isPeriodAvailable( Hour init, Hour end ) {
         boolean res = true;
 
         //recorremos las tasks 
         for ( Task t : this.tasks ) {
             //comprobamos si la task transcurre DURANTE el horario inicial
             if ( t.isTranscurringAt( init ) )
                 res = false;
             //sino, comprobamos si la task transcurre después del horario 
             //inicial            
             else if ( t.isAfter( init ) ) {
                 //y si es así, comprobamos si la tarea termina antes del horario
                 //final, o si incluye el horario final
                 if ( t.isTranscurringAt( end ) || t.isBefore( end ) )
                     res = false;
            } //si llegamos a esta instancia, la tarea transcurre ANTES del 
            //horario inicial, por lo tanto no tiene sentido seguir analizando
            else
                 break;
 
             //finalmente, salimos del ciclo si ya comprobamos que el periodo
             //no esta disponible
             if ( !res )
                 break;
         }
 
         return res;
     }
 
     /**
      * Devuelve la tarea específica que está scheduleada para el horario 
      * indicado, o {@code null} en caso de no existir.
      * @param hour horario a comprobar.
      * @return la {@code Task} correspondiente al horario o {@code null} en 
      * caso de no existir una asignada.
      */
     public Task taskAtTime( Hour hour ) {
         Task finded = null;
 
         for ( Task t : this.tasks )
             if ( t.isTranscurringAt( hour ) ) {
                 finded = t;
                 break;
             } else if ( t.isBefore( hour ) )
                 break;
 
         return finded;
     }
 
     /**
      * {@inheritDoc} 
      */
     @Override
     public Iterator<Task> iterator() {
         return this.tasks.iterator();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int size() {
         return this.tasks.size();
     }
     
     /**
      * Representa este schedule en forma de una lista simple y ordenada de las
      * tareas scheduleadas, en orden temporal.
      * @return representación en forma de {@code String} de este schedule.
      */
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder( "Schedule:\n" );
 
         for ( Task t : this.tasks )
             builder.append( '*' ).append( t.toString() ).append( "\n" );
 
         return builder.toString();
     }
 
 }

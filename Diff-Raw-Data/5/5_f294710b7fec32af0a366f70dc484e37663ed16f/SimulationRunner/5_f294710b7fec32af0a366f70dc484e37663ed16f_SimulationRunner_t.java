 package kembe.sim.runner;
 
 import fj.Effect;
 import fj.F;
 import fj.data.List;
 import kembe.EventStream;
 import kembe.OpenEventStream;
 import kembe.StreamEvent;
 import kembe.Time;
 import kembe.sim.*;
 import kembe.sim.rand.Rand;
 import org.joda.time.Instant;
 
 import java.util.HashMap;
 import java.util.Random;
 
 
 public class SimulationRunner {
 
     private final Instant startTime;
 
     private final Instant endTime;
 
     private final Random random;
 
     private final Scheduler scheduler;
 
     private HashMap<AgentId, SimAgent> agents;
 
     public SimulationRunner(Instant startTime, Instant endTime, Random random, HashMap<AgentId, SimAgent> agents, Scheduler scheduler) {
         this.random = random;
         this.agents = agents;
         this.startTime = startTime;
         this.endTime = endTime;
         this.scheduler = scheduler;
     }
 
     public EventStream<Timed<SimEvent>> eventStream(final List<Signal> startSignals) {
         return new EventStream<Timed<SimEvent>>() {
             @Override public OpenEventStream<Timed<SimEvent>> open(final Effect<StreamEvent<Timed<SimEvent>>> effect) {
 
                 scheduler.scheduleAt( startTime, new SchedulerTask() {
                     @Override public void run(Instant time) {
                         startSignals
                                 .map( Timed.<Signal>timed( startTime ) )
                                 .map( scheduleTask( effect ) )
                                 .foreach( scheduler.toEffect() );
                     }
                 } );
 
 
                 scheduler.scheduleAt( Time.quantumIncrement( endTime ), new SchedulerTask() {
                     @Override public void run(Instant time) {
                         effect.e( StreamEvent.<Timed<SimEvent>>done() );
                     }
                 } );
 
                 final EventStream<Timed<SimEvent>> self = this;
                 return new OpenEventStream<Timed<SimEvent>>() {
                     @Override public EventStream<Timed<SimEvent>> close() {
                         return self;
                     }
                 };
             }
         };
     }
 
     private Step invokeAgent(final SimAgentContext context, Timed<Signal> timedSignal) {
         final Signal signal =
                 timedSignal.value;
 
         final AgentId id =
                 signal.to;
 
         final SimAgent agent =
                 agents.get( id );
 
         final Rand<Step> steps =
                 agent.act( signal, context );
 
         final Step step =
                 steps.next( random );
 
         return step;
     }
 
     private List<Timed<Signal>> getNextInvocations(final SimAgentContext ctx, Step step) {
         return step.action
                 .either(
                         new F<SignalSchedule, List<Timed<Signal>>>() {
                             @Override public List<Timed<Signal>> f(SignalSchedule signalOccurring) {
                                 return List.single(
                                         new Timed<>(
                                                 signalOccurring.randomSleep.after( ctx.currentTime ).next( random ),
                                                 signalOccurring.value.f(ctx) ) );
                             }
                         }, new F<List<Signal>, List<Timed<Signal>>>() {
                             @Override public List<Timed<Signal>> f(List<Signal> messages) {
                                 return messages.map( new F<Signal, Timed<Signal>>() {
                                     @Override public Timed<Signal> f(Signal signal) {
                                         return new Timed<>(
                                                 Time.quantumIncrement( ctx.currentTime ),
                                                  signal );
                                     }
                                 } );
                             }
                         }
                 );
     }
 
     private F<Timed<Signal>, Timed<SchedulerTask>> scheduleTask(final Effect<StreamEvent<Timed<SimEvent>>> listener) {
         return new F<Timed<Signal>, Timed<SchedulerTask>>() {
             @Override public Timed<SchedulerTask> f(Timed<Signal> agentInvocationTimed) {
                 return new Timed<SchedulerTask>( agentInvocationTimed.time, new ScheduleInvocation( listener, agentInvocationTimed ) );
             }
         };
     }
 
     class ScheduleInvocation implements SchedulerTask {
 
         final Effect<StreamEvent<Timed<SimEvent>>> listener;
 
         final Timed<Signal> signal;
 
         ScheduleInvocation(Effect<StreamEvent<Timed<SimEvent>>> listener, Timed<Signal> signal) {
             this.listener = listener;
             this.signal = signal;
         }
 
         @Override public void run(final Instant time) {
             final SimAgentContext context =
                     new SimAgentContext( signal.value.to,signal.time );
             Step step = invokeAgent(context, signal );
            List<Timed<Signal>> invocations = getNextInvocations( context, step );

            agents.put(context.id,step.nextHandler);
 
             step.emittedEvents.foreach( new Effect<SimEvent.SimEventF>() {
                 @Override public void e(SimEvent.SimEventF simEvent) {
                     listener.e( StreamEvent.next( new Timed<>( time, simEvent.f( context ) ) ) );
                 }
             } );
 
             invocations
                     .filter( Timed.<Signal>isBeforeOrEqual( endTime ) )
                     .map( scheduleTask( listener ) )
                     .foreach( scheduler.toEffect() );
         }
 
         public String toString() {
             return "ScheduledInvocation of "+ signal.value.id+" "+  Signal.detailShow.showS( signal.value );
         }
     }
 
 
 }

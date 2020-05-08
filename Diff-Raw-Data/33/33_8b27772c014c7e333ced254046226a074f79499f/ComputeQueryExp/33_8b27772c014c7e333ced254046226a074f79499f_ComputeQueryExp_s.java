 package org.scriptbox.metrics.query.exp;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.scriptbox.metrics.compute.MetricCollator;
 import org.scriptbox.metrics.model.Metric;
 import org.scriptbox.metrics.model.MetricRange;
 import org.scriptbox.metrics.query.main.MetricProvider;
 import org.scriptbox.metrics.query.main.MetricQueries;
 import org.scriptbox.metrics.query.main.MetricQueryContext;
 import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
 
 public abstract class ComputeQueryExp implements MetricQueryExp {
 
 	  private static final Logger LOGGER = LoggerFactory.getLogger( ComputeQueryExp.class );
 	  
 	  protected String name;
 	  protected String operator;
 	  protected int chunk;
 	  protected MetricQueryExp child1; 
 	  protected MetricQueryExp child2; 
 	  
 	  public ComputeQueryExp( String name, String operator, int chunk, MetricQueryExp child1, MetricQueryExp child2 ) {
 	    this.name = name;
 	    this.operator = operator;
 	    this.chunk = chunk;
 	    this.child1 = child1;
 	    this.child2 = child2;
 	  }
 	  public Object evaluate( final MetricQueryContext ctx ) throws Exception {
 	      MetricProvider provider1 = MetricQueries.provider(ctx, child1 );
 	      MetricProvider provider2 = MetricQueries.provider(ctx, child2 );
 
 	      MetricRange range1 = provider1.getMetrics( ctx );
 	      MetricRange range2 = provider2.getMetrics( ctx );
 	     
 	      if( LOGGER.isTraceEnabled() ) {
	    	  LOGGER.trace( "evaluate: range - name: " + name + ", range[1]: " + range1 + ", metrics: " + range1.getMetrics(ctx.getChunk()) );
	    	  LOGGER.trace( "evaluate: range - name: " + name + ", range[2]: " + range2 + ", metrics: " + range2.getMetrics(ctx.getChunk()) );
 	      }
 	      List<MetricRange> metrics = new ArrayList<MetricRange>(2);
 	      metrics.add( range1 );
 	      metrics.add( range2 );
 	      
 			MetricCollator collator = new MetricCollator(name, name, chunk, metrics);
 			return collator.group(new ParameterizedRunnableWithResult<Metric, MetricRange[]>() {
 				public Metric run(MetricRange[] ranges) {
 					float val1 = compute( ranges[0] );
 					float val2 = compute( ranges[1] );
 					float result = compute( val1, val2 );;
 					long millis = ranges[0].getStart();
 					if( LOGGER.isTraceEnabled() ) {
 						Date dt = new Date(millis);
 						LOGGER.trace( "evaluate: group - name: " + name + " " + dt + "(" + millis + ") - " + val1 + " " + operator + " " + val2 + " = " + result );
 					}
 					return new Metric( millis, result );
 				}
 			});
 	  }
 
 	  protected abstract float compute( MetricRange range );
 	  protected abstract float compute( float val1, float val2 );
 	  
 	  public String toString() {
 	    return name + "(child1=" + child1 + ", child2=" + child2 + ")";
 	  }
 }

 package edu.rivfader.profiling;
 
 import edu.rivfader.data.Database;
 
 import edu.rivfader.relalg.IRelAlgExpr;
 import edu.rivfader.relalg.Evaluator;
 import edu.rivfader.relalg.Selection;
 import edu.rivfader.relalg.Projection;
 import edu.rivfader.relalg.Product;
 import edu.rivfader.relalg.IQualifiedNameRow;
 
 import java.util.Iterator;
 
 public class ProfilingEvaluator extends Evaluator {
     private ICostAccumulator statisticsDestination;
     public ProfilingEvaluator(Database context,
                               ICostAccumulator pStatisticsDestination) {
         super(context);
         statisticsDestination = pStatisticsDestination;
     }
 
     @Override
     public ICountingIterator<IQualifiedNameRow> transform(IRelAlgExpr e) {
         return new CountingIterator<IQualifiedNameRow>(super.transform(e));
     }
 
     @Override
     public Iterator<IQualifiedNameRow> transformSelection(Selection s) {
        ICountingIterator<IQualifiedNameRow> inputSet =
            transform(s.getSubExpression());
         return new SelectionStatisticsIterator(s, inputSet,
                                                statisticsDestination,
                                               inputSet);
     }
 
     @Override
     public Iterator<IQualifiedNameRow> transformProjection(Projection p) {
         return new ProjectionStatisticsIterator(statisticsDestination,
                                                transform(p.getSubExpression()),
                                                 p);
     }
 
     @Override
     public Iterator<IQualifiedNameRow> transformProduct(Product p) {
         return new ProductStatisticsIterator(p, statisticsDestination,
                                              transform(p));
     }
 }

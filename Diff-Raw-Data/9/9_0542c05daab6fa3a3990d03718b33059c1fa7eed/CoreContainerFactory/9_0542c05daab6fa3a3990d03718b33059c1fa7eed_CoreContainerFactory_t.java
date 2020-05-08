 package mpa.core.container;
 
 import mpa.mad.MadHomeImpl;
 import mpa.pair.PairHomeImpl;
 import mpa.scenary.LastOcurrenceCache;
 import mpa.scenary.ScenaryHome;
 import mpa.scenary.ScenaryHomeImpl;
 import mpa.search.ScenaryEvaluator;
 import mpa.search.evaluator.CompoundScenaryEvaluator;
 import mpa.search.evaluator.MeanEvaluator;
 import mpa.search.evaluator.MostRecentPairEvaluator;
 import mpa.search.evaluator.PredefinedPairs;
 import mpa.search.exhaustive.ExhaustiveSearch;
 
 import org.picocontainer.DefaultPicoContainer;
 import org.picocontainer.MutablePicoContainer;
 import org.picocontainer.behaviors.Caching;
import org.picocontainer.parameters.CollectionComponentParameter;
 
 public class CoreContainerFactory {
 
     public static MutablePicoContainer buildContainer() {
         DefaultPicoContainer container = new DefaultPicoContainer(new Caching());
 
         container.addComponent(MadHomeImpl.class);
         container.addComponent(PairHomeImpl.class);
         container.addComponent(LastOcurrenceCache.class);
         container.addComponent(ScenaryHome.class, ScenaryHomeImpl.class);
 
         container.addComponent(PredefinedPairs.class);
 
        container.addComponent(MostRecentPairEvaluator.class);        
        container.addComponent(MeanEvaluator.class);        
        CollectionComponentParameter allEvaluators = new CollectionComponentParameter(ScenaryEvaluator.class, false);
		container.addComponent(ScenaryEvaluator.class, CompoundScenaryEvaluator.class, allEvaluators);
         
         container.addComponent(ExhaustiveSearch.class);
         return container;
     }
 
 }

 package ske.mag.jenkins.buildscreen.collections;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import hudson.model.AbstractBuild;
 import hudson.model.Result;
 import hudson.model.Run;
 import java.util.Collection;
 import static com.google.common.collect.Collections2.*;
 import java.util.AbstractCollection;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public class CollectionFilter extends AbstractCollection<AbstractBuild>{
 	private final Collection<AbstractBuild> backingCollection;
 	private final static Function<Run, AbstractBuild> castRunToAbstractBuild = new Function<Run, AbstractBuild>(){
 			public AbstractBuild apply(Run input) {
 				return AbstractBuild.class.cast(input);
 			}
 		};
 
 	private CollectionFilter(Collection<AbstractBuild> backingCollection) {
 		this.backingCollection = new ArrayList<AbstractBuild>(backingCollection);
 	}
 
 	public static CollectionFilter filter(Collection<Run> collectionToFilter) {
 		return new CollectionFilter(transform(collectionToFilter, castRunToAbstractBuild));
 	}
 
 	public CollectionFilter byResult(Result result) {
 		return new CollectionFilter(Collections2.filter(backingCollection, new IncludeResultPredicate(result)));
 	}
 
 	public CollectionFilter byLastCompletedBuilds() {
 		return new CollectionFilter(Collections2.filter(backingCollection, new IncludeOnlyLatestBuildPredicate()));
 	}
 
	public CollectionFilter byRootBuilds() {
		return new CollectionFilter(Collections2.filter(backingCollection, new IncludeOnlyRootBuildsPredicate()));
	}

 	@Override
 	public Iterator<AbstractBuild> iterator() {
 		return backingCollection.iterator();
 	}
 
 	@Override
 	public int size() {
 		return backingCollection.size();
 	}
 
 	private class IncludeOnlyLatestBuildPredicate implements Predicate<AbstractBuild> {
 
 		public boolean apply(AbstractBuild input) {
 			return noneOfTheNextBuildsMatters(input.getNextBuild());
 		}
 
 		private boolean noneOfTheNextBuildsMatters(AbstractBuild nextBuild) {
 			if(nextBuild == null || nextBuild.isBuilding())
 				return true;
 			if(nextBuild.getResult().isWorseOrEqualTo(Result.ABORTED)) {
 				return noneOfTheNextBuildsMatters(nextBuild.getNextBuild());
 			}
 			return false;
 		}
 	}
 
 	private class IncludeResultPredicate implements Predicate<AbstractBuild> {
 		private final Result includeResult;
 
 		public IncludeResultPredicate(Result result) {
 			this.includeResult = result;
 		}
 
 		public boolean apply(AbstractBuild input) {
 			return includeResult.equals(input.getResult());
 		}
 	}

	private class IncludeOnlyRootBuildsPredicate implements Predicate<AbstractBuild> {

		public boolean apply(AbstractBuild input) {
			return input.getRootBuild() == input;
		}
	}
 }

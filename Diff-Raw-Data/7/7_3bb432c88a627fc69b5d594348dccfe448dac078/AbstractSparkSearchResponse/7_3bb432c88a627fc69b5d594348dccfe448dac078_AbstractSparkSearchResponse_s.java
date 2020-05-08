 package net.sparkmuse.discussion;
 
 import net.sparkmuse.data.entity.SparkVO;
 
 import java.util.TreeSet;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Iterables;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author neteller
  * @created: Nov 25, 2010
  */
 public abstract class AbstractSparkSearchResponse {
 
   static final int MAX_SIZE = 60;
 
   private final TreeSet<SparkVO> sparks;
   private SparkSearchRequest.Filter filter;
 
   public AbstractSparkSearchResponse(final TreeSet<SparkVO> sparks, final SparkSearchRequest.Filter filter) {
     Preconditions.checkNotNull(sparks);
     this.sparks = sparks;
     this.filter = filter;
   }
 
   public TreeSet<SparkVO> getSparks() {
     return sparks;
   }
 
   public SparkSearchRequest.Filter getFilter() {
     return filter;
   }
 
   public void update(SparkVO spark) {
     this.sparks.add(spark);
     if (this.sparks.size() > MAX_SIZE) this.sparks.remove(Iterables.getLast(this.sparks));
   }
 
   protected static int tiebreak(SparkVO a, SparkVO b, int compareTo) {
     if (0 != compareTo) return compareTo;
     else return a.getId().compareTo(b.getId());
   }
   
 }

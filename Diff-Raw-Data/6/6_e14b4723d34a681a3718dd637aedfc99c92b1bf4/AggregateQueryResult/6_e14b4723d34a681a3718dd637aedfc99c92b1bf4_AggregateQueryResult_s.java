 package com.psddev.dari.db;
 
 import com.psddev.dari.util.PaginatedResult;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Aggregates multiple queries into a single paginated result.
  *
  * <p>For example:</p>
  *
  * <blockquote><pre>{@literal
 Query<Author> authorQuery = Query.from(Author.class).where(...);
 Query<Article> articleQuery = Query.from(Article.class).where(...);
 PaginatedResult<Object> result = new AggregateQueryResult<Object>(10L, 10, authorQuery, articleQuery);
  * }</pre></blockquote>
  *
  * <p>If these queries return 3 and 17 items respectively, the {@code result}
  * would contain 3 authors and 7 articles.</p>
  */
 public class AggregateQueryResult<E> extends PaginatedResult<E> {
 
     private final int length;
     private final Query<? extends E>[] queries;
     private final Long[] counts;
 
     private Long aggregateCount;
     private List<E> items;
     private Boolean hasNext;
 
     /**
      * Creates an instance with the given {@code offset}, {@code limit},
      * and {@code queries}.
      *
      * @param queries {@code null} is equivalent to an empty array.
      */
     @SuppressWarnings("unchecked")
     public AggregateQueryResult(long offset, int limit, Query<? extends E>... queries) {
         super(offset, limit, -1L, null);
 
         if (queries != null) {
             this.length = queries.length;
             this.queries = Arrays.copyOf(queries, length);
             this.counts = new Long[length];
 
         } else {
             this.length = 0;
             this.queries = new Query[0];
             this.counts = new Long[0];
         }
     }
 
     // --- PaginatedResult support ---
 
     private long getCountFor(int index) {
         Long count = counts[index];
         if (count == null) {
             count = queries[index].count();
             counts[index] = count;
         }
         return count;
     }
 
     @Override
     public long getCount() {
         if (aggregateCount == null) {
             aggregateCount = 0L;
             for (int i = 0; i < length; ++ i) {
                 aggregateCount += getCountFor(i);
             }
         }
         return aggregateCount;
     }
 
     @Override
     public List<E> getItems() {
         if (items == null) {
             items = new ArrayList<E>();
 
             long start = getOffset();
             long end = start + getLimit();
             long current = 0;
 
             for (int i = 0; i < length; ++ i) {
                 long next = current + getCountFor(i);
 
                 if (current < end && next > start) {
                     items.addAll(queries[i].select(
                             start <= current ? 0L : (start - current),
                             (int) (end - current)).getItems());
 
                     if (next >= end) {
                         break;
                     }
                 }
 
                 current = next;
             }
         }
 
         return items;
     }
 
     @Override
     public boolean hasNext() {
         if (hasNext == null) {
             hasNext = false;
 
             long count = 0L;
            long offset = getOffset();
 
             for (int i = 0; i < length; ++ i) {
                 count += getCountFor(i);
                if (count >= offset) {
                     hasNext = true;
                     break;
                 }
             }
         }
 
         return hasNext;
     }
 }

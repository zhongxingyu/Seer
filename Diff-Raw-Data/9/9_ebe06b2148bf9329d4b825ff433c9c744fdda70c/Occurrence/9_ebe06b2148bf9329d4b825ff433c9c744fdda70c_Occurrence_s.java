 package net.personaltt.model;
 
 import net.personaltt.utils.BaseInterval;
 import net.personaltt.utils.BaseIntervalsSet;
 
 /**
  * Occurrence represents definition of one time quantum in the time axis. It means its 
  * time domain and ID. Current allocation will be held separately
  * @author docx
  */
 public class Occurrence {
     
     /**
      * Intervals set representing occurrence's domain of what area on time axis it
      * can cover
      */
     BaseIntervalsSet<Integer> domain;
     
     /*
      * Lower bound of duration interval
      */
     int minDuration;
     
     /**
      * Upper bound of duration interval
      */
     int maxDuration;
     
     /**
      * ID of occurrence used to associate with other parts of algorithm and client
      */
     int id;
     
     /**
      * Reference to assigned allocation
      */
     OccurrenceAllocation allocation;
     
     /**
      * Preferred starting point
      */
     int preferredStart;
     
     /**
      * Preferred duration for preferrence priority. Without preferrence priority,
      * default preferred duration is to maximize it 
      */
     int preferredDuration;
     
     /**
      * Priority of preferred allocation start
      */
     int preferrencePriority = 0;
     
     long domainLowerBound = 0;
     
     long domainUpperBound = 0;
     
     private long maximalPreferredStartDiff = 0;
     
     /**
      * 
      * @param domain
      * @param minDuration
      * @param maxDuration
      * @param id 
      */
     public Occurrence(BaseIntervalsSet<Integer> domain, int minDuration, int maxDuration, int id) {
         this.domain = domain;
         this.minDuration = minDuration;
         this.maxDuration = maxDuration;
         this.id = id;
         
         // get domain bounds to compute maximal difference from preferredStart
         initDomainBounds();
     }
 
     private void initDomainBounds() {
         if (domain == null) {
             return;
         }
         domainLowerBound = domain.getLowerBound();
         domainUpperBound = domain.getUpperBound();
     }
     
     /**
      * Occurrences are equal iff their ids are equal
      * @param obj
      * @return 
      */
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof Occurrence) {
             return this.id == ((Occurrence)(obj)).id;
         }
         
         return super.equals(obj);
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 53 * hash + this.id;
         return hash;
     }
     
     /**
      * Domain of occurrence. Set of intervals where can be occurrence allocated
      * @return 
      */
     public BaseIntervalsSet<Integer> getDomain() {
         return domain;
     }
 
     /**
      * Maximum duration of allocation of occurrence.
      * @return 
      */
     public int getMaxDuration() {
         return maxDuration;
     }
 
     /**
      * Minimum duration of allocation of occurrence
      * @return 
      */
     public int getMinDuration() {
         return minDuration;
     }
 
     @Override
     public String toString() {
         return String.valueOf(id);
     }
     
     /**
      * ID of occurrence.
      * @return 
      */
     public int getId() {
         return id;
     }
 
     /**
      * Shallow clone of occurrence. Since all fields of occurrence are atomic 
      * their are copied. Only domain is referenced to source. Allocation is not copied.
      * @return 
      */
     @Override
     public Object clone() {
         Occurrence o = new Occurrence(domain, minDuration, maxDuration, id);
         o.preferrencePriority = this.preferrencePriority;
         o.preferredStart = this.preferredStart;
         o.maximalPreferredStartDiff = this.maximalPreferredStartDiff;
         return o;
     }
 
     public OccurrenceAllocation getAllocation() {
         return allocation;
     }
 
     public void setAllocation(OccurrenceAllocation allocation) {
         this.allocation = allocation;
     }
     
     public void setInitialAllocation(OccurrenceAllocation allocation) {
         this.allocation = allocation;
         this.setPreferredStart(allocation.start);
         this.setPreferredDuration(allocation.duration);
     }
     
     private void setPreferredStart(int start) {
         this.preferredStart = start;
         this.maximalPreferredStartDiff = Math.max(preferredStart - domainLowerBound, domainUpperBound - preferredStart);
     }
 
     public int getPreferrencePriority() {
         return preferrencePriority;
     }
 
     public int getPreferredStart() {
         return preferredStart;
     }
     
     /**
      * Encoded preferrence cost of current allocation of this occurrence.
      * @return 
      */
     public long getPreferrenceCost() {
         if (allocation == null) {
             return 0;
         }
         return getPreferrenceCost(allocation.start, allocation.duration);
     }
     
     /**
      * Encoded preferrence cost of given allocation
      * @param inAllocation
      * @return 
      */
     public long getPreferrenceCost(OccurrenceAllocation inAllocation) {
         if (inAllocation == null) {
             return 0;
         }
         return getPreferrenceCost(inAllocation.start, inAllocation.duration);
     }
     
     /**
      * Encoded preferrence cost of given allocation.
      * @param allocationInterval
      * @return 
      */
     public long getPreferrenceCost(BaseInterval<Integer> allocationInterval) {
         if (allocationInterval == null) {
             return 0;
         }
         return getPreferrenceCost(allocationInterval.getStart(), allocationInterval.getEnd() - allocationInterval.getStart());
     }
     
     int PRIORTY_OFFSET = 48;
     int DURATIONPREF_OFFSET = 20;
     
     /**
      * Encoded preference cost of given allocation. It consist
      * of prefference priority cost, duration cost and preferred start cost
      * @param start
      * @param duration
      * @return 
      */
     public long getPreferrenceCost(int start, int duration) {
         long diffFromPreferredStart = (long)Math.abs(start - preferredStart);
         
         long cost = 
                 ((long)(maxDuration - duration) << DURATIONPREF_OFFSET) +  
                 diffFromPreferredStart;
         
         // if priority is applied and  preferred allocation is not met
         if (preferrencePriority != 0 && (start != preferredStart || duration != preferredDuration)) {
            cost = ((long)preferrencePriority << PRIORTY_OFFSET);
         }
         
         return cost;
                 
     }
     
     public long getDurationCost() {
         if (allocation == null) {
             return 0;
         }
         return getDurationCost(allocation.duration);
     }
        
     public long getDurationCost(int duration) {
         return maxDuration - duration;
     }
     
     public long getPreferredStartCost() {
         if (allocation == null) {
             return 0;
         }
         return getPreferredStartCost(allocation.start);
     }
     
     public long getPreferredStartCost(long start) {
         return Math.abs(start - preferredStart) ; 
     }
 
     public void setDomain(BaseIntervalsSet<Integer> domain) {
         this.domain = domain;
     }
     
     public void setPreferrencePriority(int weight) {
         this.preferrencePriority = weight;
     }
 
     /**
      * Return new instance of allocation that is best preferred. It is 
      * allocation with preferred start and preferred duration, bud duration
      * can be cropped by domain of allocation.
      * @return 
      */
     public OccurrenceAllocation getBestPreferredAllocation() {
         // get allocation in preferred start with maximal duration that is allowed
         // due to domain
         
         BaseInterval<Integer> d = this.domain.getIntervalContaining(preferredStart);
         
         return new OccurrenceAllocation(preferredStart, Math.min(d.getEnd() - preferredStart, maxDuration));
     }
 
     private void setPreferredDuration(int duration) {
         this.preferredDuration = duration;
     }
 }

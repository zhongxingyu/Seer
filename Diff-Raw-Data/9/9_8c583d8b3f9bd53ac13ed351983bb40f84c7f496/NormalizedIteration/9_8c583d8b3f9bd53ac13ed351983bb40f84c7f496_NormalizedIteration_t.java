 package blast.normal.iteration;
 
 
 import blast.ncbi.output.Hit;
 import blast.ncbi.output.Iteration;
 import blast.specification.BLASTIdentifier;
 import blast.normal.hit.NormalizedHit;
 import format.BadFromatException;
 import format.fasta.nucleotide.NucleotideFasta;
 import logger.Log;
 import taxonomy.Ranks;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * As wrapper class for the {@link Iteration} that allows to assign the taxonomy basing on the
  * {@link Iteration} {@link Hit} list
  */
 public class NormalizedIteration<I extends Iteration> {
 
     /**
      * A parent {@link blast.specification.BLASTIdentifier} that will perform database-related tasks
      */
     protected final BLASTIdentifier blastIdentifier;
     /**
      * I extends {@link Iteration}
      */
     protected final I iteration;
     /**
      * A list that stores {@link blast.normal.hit.NormalizedHit}s for further taxonomic specification
      */
     protected List<NormalizedHit> normalizedHits;
     /**
      * The length of the query sequence in a numeric representation
      */
     protected final int queryLength;
 
     /**
      * Current level of specification
      */
     protected Ranks currentRank;
     /**
      * A current candidate NormalizedHit that may become the pivotal one
      */
     protected NormalizedHit pivotalHit;
     /**
      * A nucleotide fasta used as a query for the given BLAST iteration
      */
     protected NucleotideFasta query;
 
     /**
      * A protected constructor to use with static factories
      *
      * @param query           a {@link NucleotideFasta} used as a query for the given BLAST iteration
      * @param iteration       I extends {@link Iteration} that will be used to perform taxonomic specification
      * @param blastIdentifier {@link blast.specification.BLASTIdentifier} that will perform cutoff checks and database communication and cutoff checks
      */
     protected NormalizedIteration(NucleotideFasta query, I iteration, BLASTIdentifier blastIdentifier) {
         this.query = query;
         this.iteration = iteration;
         this.blastIdentifier = blastIdentifier;
         this.queryLength = Integer.parseInt(this.iteration.getIterationQueryLen());
     }
 
     /**
      * A getter for the current pivotal hit
      *
      * @return current pivotal {@link NormalizedHit}
      */
     public NormalizedHit getPivotalHit() {
         return pivotalHit;
     }
 
     /**
      * Normalizes the {@link Hit}s from the I extends {@link Iteration} hit list
      *
      * @throws SQLException       in case a database communicaton error occurs
      * @throws BadFromatException in case formatting the {@link Hit} GI fails
      */
     protected void normalyzeHits() throws SQLException, BadFromatException {
         //Check if the costly procedure of Hits normalization has already been performed
         if (this.normalizedHits == null) {
             //If not yet - create a new list of the size of the list of hits
             this.normalizedHits = new ArrayList<NormalizedHit>(this.iteration.getIterationHits().getHit().size());
             //For every hit on the hit list
             for (Hit hit : this.iteration.getIterationHits().getHit()) {
                 //Create a normalized version and store in the newly created list
                 NormalizedHit normalizedHit = this.blastIdentifier.assignTaxonomy(NormalizedHit.newDefaultInstanceFromHit(hit, this.queryLength));
                 //The hit may be returned as null upon errors and inability of the blastIdentifier module to process the request
                 if (normalizedHit != null) {
                     this.normalizedHits.add(normalizedHit);
                 }
             }
         } else {
             this.normalizedHits.clear();
         }
         //For every hit on the hit list
         for (Hit hit : this.iteration.getIterationHits().getHit()) {
             //Create a normalized version and store in the newly created list
             NormalizedHit normalizedHit = this.blastIdentifier.assignTaxonomy(NormalizedHit.newDefaultInstanceFromHit(hit, this.queryLength));
             //The hit may be returned as null upon errors and inability of the blastIdentifier module to process the request
             if (normalizedHit != null) {
                 this.normalizedHits.add(normalizedHit);
             }
         }
     }
 
     /**
      * Looks for the lowest {@link Ranks} amont ghe {@link Ranks} of the {@link NormalizedHit}s and sets currentHit field to the lowest that was found
      */
     protected void findLowestRank() throws SQLException {
         //Go down starting with the root of life
         //The algorithm is only interested in real ranks, so the "no rank" is of no interest
         this.reduceNoRanks();
         Ranks lowestRank = Ranks.root_of_life;
         for (NormalizedHit normalizedHit : this.normalizedHits) {
             if (normalizedHit.getAssignedRank().ordinal() > lowestRank.ordinal()) {
                 lowestRank = normalizedHit.getAssignedRank();
             }
         }
         this.currentRank = lowestRank;
     }
 
     /**
      * Tries to lift the current {@link Ranks} of specification,
      *
      * @return {@code true} if succeeds, {@code false} it the {@link Ranks} is already {@link Ranks}.root_of_life, and no further rising is possible
      */
     protected boolean couldLiftCurrentRank() {
         if (this.currentRank != Ranks.superkingdom) {
             this.currentRank = Ranks.previous(this.currentRank);
             return true;
         } else {
             Log.getInstance().getLogger().info("Was not able to set the rank any higher");
             return false;
         }
     }
 
     /**
      * Creates a list of hits that display GIs that point to the current taxonomic rank
      *
      * @return {@link List} of normalized hits at current taxonomic rank
      */
     protected List<NormalizedHit> gatherHitsAtCurrentRank() {
         //First - count how many hits with this rank exist
         Log.getInstance().getLogger().fine("Attempting to gather hits at current rank of " + this.currentRank);
         int numberOfHitsThatQualify = 0;
         for (NormalizedHit normalizedHit : this.normalizedHits) {
             if (normalizedHit.getAssignedRank().equals(this.currentRank)) {
                 numberOfHitsThatQualify++;
             }
         }
         //Knowing the exact number - create a list of exactly the needed size
         if (numberOfHitsThatQualify > 0) {
             List<NormalizedHit> normalizedHitsAtCurrentRank = new ArrayList<NormalizedHit>(numberOfHitsThatQualify);
             Log.getInstance().getLogger().fine("At current rank of " + this.currentRank + " " + numberOfHitsThatQualify + " hits were found.");
             for (NormalizedHit normalizedHit : this.normalizedHits) {
                 //and then put all matching normalized hits into the list
                 if (normalizedHit.getAssignedRank() == this.currentRank) {
                     normalizedHitsAtCurrentRank.add(normalizedHit);
                 }
             }
             return normalizedHitsAtCurrentRank;
         } else {
             return null;
         }
     }
 
     /**
      * Ensures that all of the hits from a given {@link List} pass the cutoffs threshold at the current level of taxonomic identification.
      *
      * @param normalizedHitsUnderTest {@link List} of normalized hits to test
      * @return {@link List} of only those normalized hits that have passed, {@code null} in case none of the normalized hits passed the cutoffs,
      *         if a {@code null} or an empty list was passed as a parameter.
      * @throws SQLException in case a communication error occurs during the database interaction
      */
     protected List<NormalizedHit> ensureNormalyzedHitsPassCutoffsAtCurrentRank(List<NormalizedHit> normalizedHitsUnderTest) throws SQLException {
         //First check the incoming list for null and for that at least one normalized hit exists there
         if (normalizedHitsUnderTest != null && normalizedHitsUnderTest.size() > 0) {
             //Create a new list to hold those hits that have passed the cutoffs assuming in an optimistic way that all of the
             //hits will pass and a list of the same size will be needed
             List<NormalizedHit> esuredNormalizedHits = new ArrayList<NormalizedHit>(normalizedHitsUnderTest.size());
             for (NormalizedHit normalizedHit : normalizedHitsUnderTest) {
                 if (this.blastIdentifier.normalyzedHitChecksAgainstParametersForRank(normalizedHit, this.currentRank)) {
                     esuredNormalizedHits.add(normalizedHit);
                 } else {
                     //If the hit does not check, it should be identified at a higher taxonomic level in the next round (if such occurs)
                     this.blastIdentifier.liftRankForNormalyzedHit(normalizedHit);
                 }
             }
             if (esuredNormalizedHits.size() > 0) {
                 return esuredNormalizedHits;
             } else {
                 return null;
             }
 
         } else {
             return null;
         }
     }
 
     /**
      * Assembles a list of normalized hits that have better E-values than the potential pivotal hit.
      *
      * @return {@link List} of normalized hits that have better E-values than the potential pivotal hit.
      *         {@code null} in case the current potential pivotal hit was the first one on the list, or if the potential pivotal hit
      *         has not been assigned yet
      */
     protected List<NormalizedHit> getNormalyzedHitsWithBetterEvalue() {
         //Check if any potential pivotal hit has been assigned
         if (this.pivotalHit != null) {
             //Prepare a new list to store the normalized hits that have better E-values than the potential pivotal hit.
             List<NormalizedHit> normalizedHitsWithBetterEvalue = new ArrayList<NormalizedHit>();
             //Add hits until the potential pivotal has been found
             for (NormalizedHit normalizedHit : this.normalizedHits) {
                 if (!normalizedHit.equals(this.pivotalHit)) {
                     normalizedHitsWithBetterEvalue.add(normalizedHit);
                 } else {
                     break;
                 }
             }
             //If the potential pivotal hit was the first one on the list - just return null
             if (normalizedHitsWithBetterEvalue.size() > 0) {
                 Log.getInstance().getLogger().fine("Have found " + normalizedHitsWithBetterEvalue.size() + " hits with better E-value.");
                 return normalizedHitsWithBetterEvalue;
             } else {
                 Log.getInstance().getLogger().fine("There are no hits with better E-value.");
                 return null;
             }
 
         } else {
             return null;
         }
 
     }
 
     /**
      * Checks whether a sublist of normalized hits that possess a better E-value than the current potential pivotal hit
      * allow the assignment of the pivotal hit. If any of the hits with better E-value point to a taxonomic group that does not
      * contain a subnode of the potential pivotal hit, than such a normalized hit "contradicts" the assignment of the potential
      * pivotal hit
      *
      * @return {@code true} if the normalized hits with better E-value allow the assignment of the pivotal,
      *         {@code false} otherwise
      * @throws SQLException in case something goes wrong during the database communication
      */
     protected boolean normalyzedHitsWithBetterEvalueAllowPivotal() throws SQLException {
         //Prepare a list of hits with better E-value than the current potential pivotal hit
         Log.getInstance().getLogger().fine("Looking for hits with better E-value..");
         List<NormalizedHit> normalizedHitsWithBetterEvalue = this.getNormalyzedHitsWithBetterEvalue();
         if (normalizedHitsWithBetterEvalue != null) {
             for (NormalizedHit normalizedHit : normalizedHitsWithBetterEvalue) {
                 //Assign taxonomy down to the leaves for each hit on the list
                if(!this.blastIdentifier.isParentOf(normalizedHit.getAssignedTaxid(), this.pivotalHit.getAssignedTaxid())){
                     Log.getInstance().getLogger().fine("Hit with " + normalizedHit.getGI() + " and taxid " + normalizedHit.getAssignedTaxid() + " did not allow the current potential pivotal because \n" +
                             " it points to a taxid, which is not a parent to the current potential pivotal taxid of " + this.pivotalHit.getAssignedTaxid() + ".");
                     return false;
                 }
             }
         } else {
            Log.getInstance().getLogger().fine("Hits with better E-value allow current pivotal hit");
             return true;
         }
        Log.getInstance().getLogger().fine("Hits with better E-value allow current pivotal hit");
         return true;
     }
 
     /**
      * Attempts to set a potential pivotal hit at current taxonomic level.
      *
      * @return {@code true} in case succeeds, {@code false} in case was not able to
      *         set any hit as pivotal at the current level of specification
      * @throws SQLException in case something goes wrong during the database communication
      */
     protected boolean couldSetPivotalHitAtCurrentRank() throws SQLException {
         //Prepare a list of normalized hits that have been checked against the cutoffs at current rank
         Log.getInstance().getLogger().fine("Attempting to set current potential pivotal hit");
         List<NormalizedHit> normalizedHitsAtCurrentRank = this.ensureNormalyzedHitsPassCutoffsAtCurrentRank(this.gatherHitsAtCurrentRank());
         if (normalizedHitsAtCurrentRank != null) {
             Log.getInstance().getLogger().fine("A subset of hits at current rank of " + this.currentRank + " contains " + normalizedHitsAtCurrentRank.size() + " hits (that satisfy cutoffs)");
             //If any normalized hits exist on the list - set the firs one as pivotal
             this.pivotalHit = normalizedHitsAtCurrentRank.get(0);
             Log.getInstance().getLogger().fine("Current pivotal hit was set to: " + this.pivotalHit.getGI());
             return true;
         } else {
             //Try lifting one step the current rank
             this.liftCurrentRankOfSpecificationForHits();
             Log.getInstance().getLogger().fine("A subset of hits at current rank of " + this.currentRank + " is empty, lifting current rank and attempting once again.");
             if (this.couldLiftCurrentRank()) {
                 //Retry to set potential pivotal hit recursively
                 return this.couldSetPivotalHitAtCurrentRank();
             }
             return false;
         }
     }
 
     /**
      * As the NCBI taxonomy database is not perfect, some of the taxonomic nodes contain a "no rank" taxonomy. As long as
      * "no rank" is not helpful or comparable to the normal ranks, such should be reduces to the higher taxonomic levels
      * at which some real taxonomic rank exists.
      * <i> Application of this function prior to setting a pitovat hit a current level.</i>
      * @throws SQLException in case an error occurs during the database communication
      */
     protected void reduceNoRanks() throws SQLException {
         for (NormalizedHit normalizedHit : this.normalizedHits) {
             while (normalizedHit.getAssignedRank() == Ranks.no_rank&&normalizedHit.getAssignedTaxid()!=1) {
                 this.blastIdentifier.liftRankForNormalyzedHit(normalizedHit);
             }
         }
     }
 
     /**
      * Attempts to lift the current {@link Ranks} of specificaton one step higher for all those {@link NormalizedHit}s that
      * are set to the current {@link Ranks}.
      *
      * @throws SQLException in case an error occurs during the database communication
      */
     protected void liftCurrentRankOfSpecificationForHits() throws SQLException {
         for (NormalizedHit normalizedHit : this.normalizedHits) {
             if (normalizedHit.getAssignedRank() == this.currentRank) {
                 this.blastIdentifier.liftRankForNormalyzedHit(normalizedHit);
             }
         }
        this.reduceNoRanks();
     }
 
     /**
      * Check whether the {@link NormalizedHit}s that have worse (higher) E-value point to the same taxid as the potential pivotal hit, and if so -
      * whether the E-value difference (in folds, ratio) is more than the cutoff value at the current {@link Ranks} of specification
      *
      * @return {@code false} in two cases - 1. if current potential pivotal hit is {@code null}
      *         2. if there was a {@link NormalizedHit} that pointed to a different taxid at the current {@link Ranks} and the E-value ratio was less than the cutoff
      *         otherwise - returns true
      * @throws SQLException in case an error occurs during the database communication
      */
     protected boolean normalyzedHitsWithWorseEvalueAllowPivotal() throws SQLException {
 
         //Go through the hits that have worse E-value than the pivotal hit
         if (this.pivotalHit != null) {
             for (int i = this.normalizedHits.indexOf(this.pivotalHit) + 1; i < this.normalizedHits.size(); i++) {
                 NormalizedHit normalizedHit = this.normalizedHits.get(i);
                 //If the next hit has current rank and points to a different taxonomic node
                 if (normalizedHit.getAssignedRank() == this.currentRank && normalizedHit.getAssignedTaxid() != this.pivotalHit.getAssignedTaxid()) {
                     Log.getInstance().getLogger().fine("A hit with worse E-value was from the same rank of \'" + this.currentRank +
                             "\", but from different taxonomic group with taxid: " + normalizedHit.getAssignedTaxid()
                             + " (while the current pivotal hit has: " + this.pivotalHit.getAssignedTaxid() + ").");
                     //if the E-value difference (in folds) between the next hit and the current pivotal
                     //is less then the threshold cutoff - do not allow the pivotal hit
                     Log.getInstance().getLogger().fine("Checking whether the hits are far enough by the E-value in folds..");
                     if (this.blastIdentifier.hitsAreFarEnoughByEvalueAtRank(normalizedHit, this.pivotalHit, this.currentRank)) {
                         Log.getInstance().getLogger().fine("The hits are far enough.");
                         return true;
                     } else {
                         Log.getInstance().getLogger().fine("The hits are not far enough.");
                         return false;
                     }
                 }
             }
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Performs the taxonomic specification. Tries to find a pivotal hit at the lowest rank possible,
      * looks if the hits with higher ranks and better E-values allow the pivotal hit, if the hits with
      * the same rank, but worse E-values are less likely to affect the pivotal hit selection.
      *
      * @throws SQLException       in case an error occurs during the database communication
      * @throws BadFromatException in case formatting the {@link Hit} GI fails
      */
     public void specify() throws SQLException, BadFromatException {
         if (!this.iteration.getIterationHits().getHit().isEmpty()) {
             this.normalyzeHits();
             Log.getInstance().getLogger().fine("Current number of normalized hits is: " + this.normalizedHits.size());
             Log.getInstance().getLogger().fine("Attempting to find the lowest rank..");
             this.findLowestRank();
             Log.getInstance().getLogger().fine("The lowest rank is: " + this.currentRank);
             //Moving up the taxonomic ranks
             while (couldSetPivotalHitAtCurrentRank()) {
                 //Try finding such a hit that is supported as a pivotal one for the taxonomic specification by both
                 //hits with better and worse E-values (belongs to the same taxon as those with better E-value, but allows
                 //deeper specification, and has no compatitors among those that have worse E-values
                 if (this.normalyzedHitsWithBetterEvalueAllowPivotal() && this.normalyzedHitsWithWorseEvalueAllowPivotal()) {
                     //success
                     Log.getInstance().getLogger().finest("Success");
                     this.blastIdentifier.attachFullDirectLineage(this.pivotalHit.getFocusNode());
                     break;
                 } else {
                     Log.getInstance().getLogger().fine("Lifting up current rank of specification for those hits that has " + this.currentRank);
                     this.liftCurrentRankOfSpecificationForHits();
                     if (this.couldLiftCurrentRank()) {
                         Log.getInstance().getLogger().fine("Trying a higher rank of rank \"" + this.currentRank + "\"");
                     } else {
                         break;
                     }
                 }
             }
             //Save results whatever they are
             try {
                 this.blastIdentifier.acceptResults(this.query, this);
             } catch (Exception e) {
                 Log.getInstance().getLogger().severe("Unable to save results! The error was:");
                 e.printStackTrace();
             }
         } else {
             try {
             this.blastIdentifier.acceptResults(this.query, this);
             } catch (Exception e) {
                 e.printStackTrace();
             }
             Log.getInstance().getLogger().severe("No hits returned from BLASTN. Suggestion: please check the entrez_query field within the io.properties configuration file.");
         }
         //fail
     }
 
     /**
      * A static factory to create a new instance of {@link NormalizedIteration} from a given set of parameters
      *
      * @param iteration       I extends {@link Iteration} that will be used to perform taxonomic specification
      * @param blastIdentifier {@link blast.specification.BLASTIdentifier} that will perform cutoff checks and database communication and cutoff checks
      * @return a new instance of {@link NormalizedIteration} from a given set of parameters
      */
     public static <I extends Iteration> NormalizedIteration newDefaultInstanceFromIteration(NucleotideFasta query, I iteration, BLASTIdentifier blastIdentifier) {
         return new NormalizedIteration<I>(query, iteration, blastIdentifier);
     }
 }

 package net.cscott.sdr.calls;
 
 import static net.cscott.sdr.util.Tools.m;
 import static net.cscott.sdr.util.Tools.p;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.jutil.BitSetFactory;
 import net.cscott.jutil.GenericMultiMap;
 import net.cscott.jutil.Indexer;
 import net.cscott.jutil.MultiMap;
 import net.cscott.jutil.PersistentSet;
 import net.cscott.jutil.SetFactory;
 import net.cscott.sdr.calls.Breather.FormationPiece;
 import net.cscott.sdr.calls.Position.Flag;
 import net.cscott.sdr.calls.TaggedFormation.Tag;
 import net.cscott.sdr.calls.TaggedFormation.TaggedDancerInfo;
 import net.cscott.sdr.util.Fraction;
 
 import org.junit.runner.RunWith;
 
 /**
  * {@link GeneralFormationMatcher} produces a {@link FormationMatch}
  * given an input {@link Formation} and a goal {@link TaggedFormation}.
  * This can be used to make {@link Matcher}s out of {@link TaggedFormation}s,
  * via the {@link #makeMatcher} method.
  *
  * @author C. Scott Ananian
  */
 @RunWith(value=JDoctestRunner.class)
 public class GeneralFormationMatcher {
     private GeneralFormationMatcher() {}
     // currying, oh, my
     public static Matcher makeMatcher(TaggedFormation... goals) {
         return makeMatcher(Arrays.asList(goals));
     }
     public static Matcher makeMatcher(List<TaggedFormation> goals) {
         String name = targetName(goals);
         return makeMatcher(name, goals);
     }
     public static Matcher makeMatcher(final String name, final List<TaggedFormation> goals) {
         return new Matcher() {
             @Override
             public FormationMatch match(Formation f) throws NoMatchException {
                 return doMatch(f, goals, false, false);
             }
             @Override
             public String getName() { return name; }
         };
     }
 
     /**
      * Attempt to match the input formation against the goal formation; you can
      * have multiple rotated copies of the goal formation in the input.
      * Allow dancers who are not part of copies of the goal formation if
      * allowUnmatchedDancers is true; allow copies of the goal formation
      * with phantoms in them if usePhantoms is true.  Returns the best
      * such match (ie, most copies of the goal formation).
      * @param input An untagged formation to match against.
      * @param goal A tagged goal formation
      * @param allowUnmatchedDancers allow dancers in the input formation not to
      *        match dancers in (copies of) the goal
      * @param usePhantoms allow dancers in the goal formation not to match
      *        dancers in the input
      * @return the match result
      * @throws NoMatchException if there is no way to match the goal formation
      *   with the given input
      * @doc.test A successful match with no phantoms or unmatched dancers:
      *  js> FormationList = FormationListJS.initJS(this); undefined;
      *  js> GeneralFormationMatcher.doMatch(Formation.SQUARED_SET,
      *    >                                 FormationList.COUPLE,
      *    >                                 false, false)
      *       AAv
      *  
      *  BB>       CC<
      *  
      *       DD^
      *  AA:
      *     3B^  3G^
      *   [3B: BEAU; 3G: BELLE]
      *  BB:
      *     4B^  4G^
      *   [4B: BEAU; 4G: BELLE]
      *  CC:
      *     2B^  2G^
      *   [2B: BEAU; 2G: BELLE]
      *  DD:
      *     1B^  1G^
      *   [1B: BEAU; 1G: BELLE]
      * @doc.test A successful match with some unmatched dancers:
      *  js> FormationList = FormationListJS.initJS(this); undefined;
      *  js> GeneralFormationMatcher.doMatch(FormationList.RH_TWIN_DIAMONDS,
      *    >                                 FormationList.RH_MINIWAVE,
      *    >                                 true, false)
      *  AA>  BB>
      *  
      *  CC^  DDv
      *  
      *  EE<  FF<
      *  AA: (unmatched)
      *     ^
      *  BB: (unmatched)
      *     ^
      *  CC:
      *     ^    v
      *   [ph: BEAU; ph: BEAU]
      *  DD:
      *     ^    v
      *   [ph: BEAU; ph: BEAU]
      *  EE: (unmatched)
      *     ^
      *  FF: (unmatched)
      *     ^
      * @doc.test When possible, symmetry is preserved in the result:
      *  js> FormationList = FormationListJS.initJS(this); undefined;
      *  js> GeneralFormationMatcher.doMatch(FormationList.PARALLEL_RH_WAVES,
      *    >                                 FormationList.RH_MINIWAVE,
      *    >                                 false, false)
      *  AA^  BBv
      *  
      *  CC^  DDv
      *  AA:
      *     ^    v
      *   [ph: BEAU; ph: BEAU]
      *  BB:
      *     ^    v
      *   [ph: BEAU; ph: BEAU]
      *  CC:
      *     ^    v
      *   [ph: BEAU; ph: BEAU]
      *  DD:
      *     ^    v
      *   [ph: BEAU; ph: BEAU]
      */
     // booleans for 'allow unmatched dancers' and
     // 'use phantoms' allow dancers in the input and result formations,
     // respectively, not to match up.
     // XXX: implement usePhantoms
     // NOTE THAT result will include 1-dancer formations if
     // allowUnmatchedDancers is true; see the contract of FormationMatch.
     public static FormationMatch doMatch(
             final Formation input, 
             final TaggedFormation goal,
             boolean allowUnmatchedDancers,
             boolean usePhantoms)
     throws NoMatchException {
         return doMatch(input, Collections.singletonList(goal),
                        allowUnmatchedDancers, usePhantoms);
     }
     private static String targetName(List<TaggedFormation> goals) {
         StringBuilder sb = new StringBuilder();
         assert goals.size() > 0;
         for (TaggedFormation goal : goals) {
             String name = (goal instanceof NamedTaggedFormation) ?
                     ((NamedTaggedFormation) goal).getName() : goal.toString();
             if (sb.length()>0) sb.append(',');
             sb.append(name);
         }
         if (goals.size()>1)
             return "MIXED("+sb.toString()+")";
         return sb.toString();
     }
     /** sort so that first dancers' target rotations are most constrained,
      *  for efficiency. (ie largest rotation moduli are first) */
     private static Comparator<Position> PCOMP = new Comparator<Position>() {
         public int compare(Position p1, Position p2) {
             int c = -p1.facing.modulus.compareTo(p2.facing.modulus);
             if (c!=0) return c;
             return p1.compareTo(p2);
         }
     };
     /** Allow multiple simultaneous goal formations.
      * @doc.test A successful match on a Siamese Diamond.
      *  js> importPackage(net.cscott.sdr.util); // for Fraction
      *  js> FormationList = FormationListJS.initJS(this); undefined;
      *  js> f = Formation.SQUARED_SET; undefined
      *  js> for each (d in StandardDancer.values()) {
      *    >   if (d.isHead()) continue;
      *    >   p = f.location(d).forwardStep(Fraction.valueOf(2), true).
      *    >                     turn(Fraction.valueOf(1,4), false);
      *    >   f = f.move(d, p);
      *    > }; f.toStringDiagram('|');
      *  |3Gv  3Bv
      *  |
      *  |4Bv  2G^
      *  |
      *  |4Gv  2B^
      *  |
      *  |1B^  1G^
      *  js> goals = java.util.Arrays.asList(FormationList.TANDEM,
      *    >                                 FormationList.COUPLE); undefined
      *  js> GeneralFormationMatcher.doMatch(f, goals, false, false)
      *    AAv
      *  
      *  BBv  CC^
      *  
      *    DD^
      *  AA:
      *     3B^  3G^
      *   [3B: BEAU; 3G: BELLE]
      *  BB:
      *     4G^
      *     
      *     4B^
      *   [4G: LEADER; 4B: TRAILER]
      *  CC:
      *     2G^
      *     
      *     2B^
      *   [2G: LEADER; 2B: TRAILER]
      *  DD:
      *     1B^  1G^
      *   [1B: BEAU; 1G: BELLE]
      */
     public static FormationMatch doMatch(
                 final Formation input,
                 final List<TaggedFormation> goals,
                 boolean allowUnmatchedDancers,
                 boolean usePhantoms)
         throws NoMatchException {
         assert !usePhantoms : "matching with phantoms is not implemented";
         // get an appropriate formation name
         String target = targetName(goals);
 
         // okay, try to perform match by trying to use each dancer in turn
         // as dancer #1 in the goal formation.  We then validate the match:
         // make sure that there is a dancer in each position, that no dancer
         // in the match is already in another match, and that the state isn't
         // redundant due to symmetry.  (To determine this last, we identify
         // those 'other dancers' in the goal formation which are rotationally
         // symmetric to dancer #1, and make sure that the proposed match
         // doesn't assign any of these positions to dancers we've already
         // tried as #1.)  Finally, we'll have a list of matches.  We
         // identify the ones with a maximum number of the goal formation,
         // and assert that this maximal match is unique; otherwise the
         // match is ambiguous and we throw NoMatchException.
         // (note that we ignore unselected dancers in formation f)
         // (note that 'dancer #1' is really 'dancer #0' below)
         assert goals.size() > 0;
         int minGoalDancers = goals.get(0).dancers().size();
         List<GoalInfo> goalInfo = new ArrayList<GoalInfo>(goals.size());
         for (TaggedFormation goal : goals) {
             // create GoalInfo
             goalInfo.add(new GoalInfo(goal));
             minGoalDancers = Math.min(minGoalDancers, goal.dancers().size());
         }
         if (minGoalDancers > input.dancers().size())
             throw new NoMatchException(target, "goal is too large");
 
         // sort the input dancers the same as the goal dancers: real dancers
         // before phantoms.
         // there must be at least one non-phantom dancer in the formation.
         // in addition, group symmetric dancers together in the order, so
         // that the resulting matches tend to symmetry.
         final List<Dancer> inputDancers=new ArrayList<Dancer>(input.dancers());
         Collections.sort(inputDancers, new Comparator<Dancer>() {
             /** minimum of position rotated through 4 quarter rotations */
             private Position qtrMin(Position p) {
                 if (!qtrMinCache.containsKey(p))
                     qtrMinCache.put(p, Collections.min(rotated(p), PCOMP));
                 return qtrMinCache.get(p);
             }
             /** minimum of position rotated by 180 degrees */
             private Position halfMin(Position p) {
                 if (!halfMinCache.containsKey(p)) {
                     Position pprime = p.rotateAroundOrigin(ExactRotation.ONE_HALF);
                     Position r = Collections.min(Arrays.asList(p,pprime), PCOMP);
                     halfMinCache.put(p, r);
                 }
                 return halfMinCache.get(p);
             }
             public int compare(Dancer d1, Dancer d2) {
                 Position p1 = input.location(d1), p2 = input.location(d2);
                 // first comparison is against min of quarter-rotated versions
                 int c = PCOMP.compare(qtrMin(p1), qtrMin(p2));
                 if (c!=0) return c;
                 // now, compare against min of half-rotated versions
                 c = PCOMP.compare(halfMin(p1), halfMin(p2));
                 if (c!=0) return c;
                 // finally, break ties by comparing against "real" position
                 return PCOMP.compare(p1, p2);
             }
             private Map<Position,Position> halfMinCache = new HashMap<Position,Position>();
             private Map<Position,Position> qtrMinCache = new HashMap<Position,Position>();
         });
         final Indexer<Dancer> inputIndex = new Indexer<Dancer>() {
             Map<Dancer,Integer> index = new HashMap<Dancer,Integer>();
             { int i=0; for (Dancer d: inputDancers) index.put(d, i++); }
             @Override
             public int getID(Dancer d) { return index.get(d); }
             @Override
             public Dancer getByID(int id) { return inputDancers.get(id); }
             @Override
             public boolean implementsReverseMapping() { return true; }
         };
         final PersistentSet<Dancer> inputEmpty = new PersistentSet<Dancer>
         (new Comparator<Dancer>() {
            public int compare(Dancer d1, Dancer d2) {
                return inputIndex.getID(d1) - inputIndex.getID(d2);
             } 
         });
         
         // now try setting each dancer in 'f' to d0 in the goal formation.
 
         // Construct MatchInfo & initial (empty) assignment
         MatchInfo mi = new MatchInfo(input, goalInfo, minGoalDancers,
                                      inputDancers, inputIndex, inputEmpty);
         PersistentSet<OneMatch> initialAssignment = new PersistentSet<OneMatch>
         (new Comparator<OneMatch>(){
             public int compare(OneMatch o1, OneMatch o2) {
                 int c=inputIndex.getID(o1.dancer)-inputIndex.getID(o2.dancer);
                 if (c!=0) return c;
                 return o1.extraRot.compareTo(o2.extraRot);
             }
         }); 
         // Do the match
         tryOne(mi, 0, initialAssignment, inputEmpty, allowUnmatchedDancers);
         if (mi.matches.isEmpty())
             throw new NoMatchException(target, "no matches");
         
         // Filter out the max
         int max = 0;
         for (PersistentSet<OneMatch> match: mi.matches)
             max = Math.max(max,match.size());
         assert max > 0;
         // Is it unique?
         PersistentSet<OneMatch> bestMatch=null; boolean found = false;
         for (PersistentSet<OneMatch> match: mi.matches)
             if (match.size()==max)
                 if (found) // ambiguous match.
                     throw new NoMatchException(target, "ambiguous");
                 else {
                     bestMatch = match;
                     found = true;
                 }
         assert found;
         // track the input dancers who aren't involved in matches
         Set<Dancer> unmappedInputDancers = new LinkedHashSet<Dancer>(inputDancers);
         // Create a FormationMatch object from FormationPieces.
 	List<FormationPiece> pieces = new ArrayList<FormationPiece>(max);
         Map<Dancer,TaggedFormation> canonical=new LinkedHashMap<Dancer,TaggedFormation>();
         for (OneMatch om : bestMatch) {
             Dancer id0 = om.dancer;//input dancer who's #1 in the goal formation
             int dn0 = inputIndex.getID(id0);
             Position inP = mi.inputPositions.get(dn0);
             assert inP.facing instanceof ExactRotation :
                 "at least one real dancer must be in formation";
             // make an ExactRotation for pGoal, given the extraRot
             Position goP = makeExact(om.gi.goalPositions.get(0), om.extraRot);
             Warp warpF = Warp.rotateAndMove(goP, inP);
             Warp warpB = Warp.rotateAndMove(inP, goP);
             ExactRotation rr = (ExactRotation) inP.facing.subtract(goP.facing.amount);
             Map<Dancer,Position> subPos = new LinkedHashMap<Dancer,Position>();
             MultiMap<Dancer,Tag> subTag = new GenericMultiMap<Dancer,Tag>();
             for (Dancer goD : om.gi.goalDancers) {
                 goP = om.gi.goal.location(goD);
                 // warp to find which input dancer corresponds to this one
                 inP = warpF.warp(goP, Fraction.ZERO);
                 Dancer inD = mi.inputPositionMap.get(zeroRotation(inP));
                 // warp back to get an exact rotation for this version of goal
                Position goPr = warpB.warp(input.location(inD), Fraction.ZERO);
                // to avoid distortion for 1/8 off formations, take only the
                // rotation (and flags) from this new goP
                goP = goPr.relocate(goP.x, goP.y, goPr.facing);
                 // add to this subformation.
                 subPos.put(inD, goP);
                 subTag.addAll(inD, om.gi.goal.tags(goD));
                 unmappedInputDancers.remove(inD);
             }
             TaggedFormation tf =
                 new TaggedFormation(new Formation(subPos), subTag);
 	    Dancer dd = new PhantomDancer();
 	    canonical.put(dd, tf);
 
             Formation pieceI = input.select(tf.dancers()).onlySelected();
             Formation pieceO = new Formation(m(p(dd, new Position(0,0,rr))));
             pieces.add(new FormationPiece(pieceI, pieceO));
         }
         // add pieces for unmapped dancers (see spec for FormationMatch.meta)
         Set<Dancer> unmatchedMetaDancers = new LinkedHashSet<Dancer>();
         for (Dancer d : unmappedInputDancers) {
 	    // these clauses are parallel to the ones above for matched dancers
             Position inP = input.location(d);
             Position goP = Position.getGrid(0,0,"n");
             ExactRotation rr = (ExactRotation) // i know this is a no-op.
 		inP.facing.subtract(goP.facing.amount);
 
             Dancer dd = new PhantomDancer();
             TaggedFormation tf = new TaggedFormation
 		(new TaggedDancerInfo(d, goP));
             canonical.put(dd, tf);
             unmatchedMetaDancers.add(dd);
 
             Formation pieceI = input.select(tf.dancers()).onlySelected();
             Formation pieceO = new Formation(m(p(dd, new Position(0,0,rr))));
             pieces.add(new FormationPiece(pieceI, pieceO));
         }
         // the components formations are the warped & rotated version.
         // the rotation in 'components' tells how much they were rotated.
         // the canonical formations have the input dancers, and the formations
         // are unwarped and unrotated.  The key dancers in the canonical map
         // are the phantoms from the meta formation.
         return new FormationMatch(Breather.breathe(pieces), canonical,
                                   unmatchedMetaDancers);
     }
     private static class OneMatch {
         /** Which goal formation. */
         public final GoalInfo gi;
         /** This input dancer is #1 in the goal formation. */
         public final Dancer dancer;
         /** This is the 'extra rotation' needed to align the goal formation,
          * if dancer #1 in the goal formation allows multiple orientations. */
         public final Fraction extraRot;
         OneMatch(GoalInfo gi, Dancer dancer, Fraction extraRot) {
             this.gi = gi; this.dancer = dancer; this.extraRot = extraRot;
         }
     }
     private static class GoalInfo {
         final List<Dancer> goalDancers;
         final List<Position> goalPositions;
         final TaggedFormation goal;
         final Set<Dancer> eq0; // goal dancers who are symmetric to goal dancer #0
         final int numExtra; // number of 'extra' rotations we'll try to match
         GoalInfo(final TaggedFormation goal) {
             this.goal = goal;
             // make a canonical ordering for the goal dancers
             this.goalDancers=new ArrayList<Dancer>(goal.dancers());
             Collections.sort(this.goalDancers, new Comparator<Dancer>() {
                 public int compare(Dancer d1, Dancer d2) {
                     return PCOMP.compare(goal.location(d1), goal.location(d2));
                 }
             });
             SetFactory<Dancer> gsf = new BitSetFactory<Dancer>(goalDancers);
             // Identify dancers who are symmetric to dancer #0
             assert goal.isCentered(); // Assumes center of goal formation is 0,0
             Dancer gd0 = goalDancers.get(0);
             this.eq0 = gsf.makeSet();
             Position p0 = goal.location(gd0).normalize(); // remember p0.facing is most exact
             for (Dancer gd : goalDancers)
                 for (Position rp: rotated(goal.location(gd)))
                         if (rp.x.equals(p0.x) && rp.y.equals(p0.y)&&
                                 rp.facing.includes(p0.facing))
                             eq0.add(gd);
             assert eq0.contains(gd0);//at the very least, gd0 is symmetric to itself
             // map dancer # to position
             this.goalPositions = new ArrayList<Position>(goalDancers.size());
             for (Dancer d : goalDancers) {
                 Position p = goal.location(d);
                 this.goalPositions.add(p);
             }
             // first goal dancer has a rotation modulus which is 1/N for some
             // N.  This means we need to try N other rotations for matches.
             this.numExtra = goal.location
                 (goalDancers.get(0)).facing.modulus.getDenominator();
         }
     }
     private static class MatchInfo {
         final List<PersistentSet<OneMatch>> matches = new ArrayList<PersistentSet<OneMatch>>();
         final Indexer<Dancer> inputIndex;
         final Map<Position,Dancer> inputPositionMap = new HashMap<Position,Dancer>();
         final List<Position> inputPositions = new ArrayList<Position>();
         final List<GoalInfo> goals;
         final int minGoalDancers;
         final int numInput;
         final Set<Dancer> sel; // input dancers who are selected
         // these next one is used to pass info into validate & back:
         /** Input dancers who are already assigned to a formation. */
         PersistentSet<Dancer> inFormation;
         /** Size of the current best match. */
         int bestMatchSize = 0;
         MatchInfo(Formation f, List<GoalInfo> goals, int minGoalDancers,
                   List<Dancer> inputDancers, Indexer<Dancer> inputIndex,
                   PersistentSet<Dancer> inputEmpty) {
             for (Dancer d : inputDancers) {
                 Position p = f.location(d);
                 this.inputPositions.add(p);
                 this.inputPositionMap.put(zeroRotation(p), d);
             }
             this.numInput = inputDancers.size();
             this.inputIndex = inputIndex;
             this.sel = f.selected;
             this.inFormation = inputEmpty;
             this.goals = goals;
             this.minGoalDancers = minGoalDancers;
         }
     }
     private static boolean validate(MatchInfo mi, GoalInfo goal, int dancerNum, Fraction extraRot) {
         PersistentSet<Dancer> inFormation = mi.inFormation;
         Set<Dancer> eq0 = goal.eq0;
         // find some Dancer in the input formation to correspond to each
         // Dancer in the goal formation.  Each such dancer must not already
         // be assigned.
         Position pIn = mi.inputPositions.get(dancerNum);
         assert pIn.facing instanceof ExactRotation :
             "at least one dancer in the input formation must be non-phantom";
         Position pGoal = makeExact(goal.goalPositions.get(0), extraRot);
         Warp warp = Warp.rotateAndMove(pGoal, pIn);
         int gNum = 0;
         for (Position gp : goal.goalPositions) {
             // compute warped position.
             gp = warp.warp(gp, Fraction.ZERO);
             Position key = zeroRotation(gp);
             if (!mi.inputPositionMap.containsKey(key))
                 return false; // no input dancer at this goal position.
             // okay, there is an input dancer:
             Dancer iDan = mi.inputPositionMap.get(key);
             int iNum = mi.inputIndex.getID(iDan);
             // if this dancer selected?
             if (!mi.sel.contains(iDan))
                 return false; // this dancer isn't selected.
             // is he free to be assigned to this formation?
             if (inFormation.contains(iDan))
                 return false; // this dancer is already in some match
             // is his facing direction consistent?
             Position ip = mi.inputPositions.get(iNum);
             assert ip.x.equals(gp.x) && ip.y.equals(gp.y);
             if (!gp.facing.includes(ip.facing))
                 return false; // rotations aren't correct.
             // check for symmetry: if this goal position is 'eq0' (ie,
             // symmetric with the 0 dancer's position), then this dancer #
             // must be >= the 0 dancer's input # (ie, dancerNum)
             if (eq0.contains(goal.goalDancers.get(gNum)))
                 if (iNum < dancerNum) {
                     // check that our matching rotation is really symmetric,
                     // since the goal dancer may have a vague direction which
                     // includes an asymmetric alternative (ie, "n|" as a target)
                     for (Position gp0 : rotated(goal.goalPositions.get(0))) {
                         gp0 = warp.warp(gp0, Fraction.ZERO);
                         if (ip.x.equals(gp0.x) &&
                             ip.y.equals(gp0.y) &&
                             gp0.facing.includes(ip.facing))
                             return false; // symmetric to some other canonical formation
                     }
                 }
             // update 'in formation' and 'gNum'
             inFormation = inFormation.add(iDan);
             gNum++;
         }
         // return updates to inFormation
         mi.inFormation = inFormation;
         return true; // this is a valid match.
     }
         
     private static void tryOne(MatchInfo mi, int dancerNum,
             PersistentSet<OneMatch> currentAssignment,
             PersistentSet<Dancer> inFormation,
             boolean allowUnmatchedDancers) {
         if (dancerNum >= mi.numInput) {
             if (inFormation.size() != mi.numInput)
                 if (!allowUnmatchedDancers)
                     return; // not a good assignment
             // we've got a complete assignment; save it.
             if (!currentAssignment.isEmpty() &&
                 currentAssignment.size() >= mi.bestMatchSize) {
                 mi.bestMatchSize = currentAssignment.size();
                 mi.matches.add(currentAssignment);
             }
             return;
         }
 
         // is there any way we can still match the bestMatchSize?
         int dancer0sLeftToAssign = mi.numInput - dancerNum;
         if (currentAssignment.size() + dancer0sLeftToAssign < mi.bestMatchSize)
             return; // not enough dancer 0's to beat the current best match
         int dancersLeftToAssign = mi.numInput - inFormation.size();
         int goalsLeftToAssign = mi.bestMatchSize - currentAssignment.size();
         if (mi.minGoalDancers*goalsLeftToAssign > dancersLeftToAssign)
             return; // not enough unassigned dancers to beat the best match
 
         // is this dancer available to be assigned?
         Dancer thisDancer = mi.inputIndex.getByID(dancerNum);
         if (!inFormation.contains(thisDancer)) {
             // okay, try to assign the thisDancer, possibly w/ some extra rotation
             for (GoalInfo gi : mi.goals) {
                 for (int i=0; i < gi.numExtra; i++) {
                     Fraction extraRot = Fraction.valueOf(i, gi.numExtra);
                     PersistentSet<OneMatch> newAssignment =
                         currentAssignment.add(new OneMatch(gi, thisDancer, extraRot));
                     mi.inFormation = inFormation;
                     if (validate(mi, gi, dancerNum, extraRot)) // sets mi.inFormation
                         // try to extend this match!
                         tryOne(mi, dancerNum+1, newAssignment, mi.inFormation,
                                 allowUnmatchedDancers);
                 }
             }
         }
 
         // try NOT assigning this dancer
         tryOne(mi, dancerNum+1, currentAssignment, inFormation,
                allowUnmatchedDancers);
     }
 
     /** Make a position with an ExactRotation from the given position with a
      * general rotation and an 'extra rotation' amount. */
     private static Position makeExact(Position p, Fraction extraRot) {
         return new Position(p.x, p.y, 
                 new ExactRotation(p.facing.amount.add(extraRot)));
     }
     /** Make a position with exact zero rotation from the given position. */
     private static Position zeroRotation(Position p) {
         return new Position(p.x, p.y, ExactRotation.ZERO);
     }
     private static Set<Position> rotated(Position p) {
         Set<Position> s = new LinkedHashSet<Position>(4);
         for (int i=0; i<4; i++) {
             s.add(p);
             p = p.rotateAroundOrigin(ExactRotation.ONE_QUARTER);
         }
         return s;
     }
     /** @deprecated XXX: rewrite to remove dependency on old Warp class */
     private static abstract class Warp {
         public abstract Position warp(Position p, Fraction time);
         /** A <code>Warp</code> which returns points unchanged. */
         public static final Warp NONE = new Warp() {
             public Position warp(Position p, Fraction time) { return p; }
         };
 	/** Returns a <code>Warp</code> which will rotate and translate
 	 * points such that <code>from</code> is warped to <code>to</code>.
 	 * Requires that both {@code from.facing} and {@code to.facing} are
 	 * {@link ExactRotation}s.
 	 */
 	// XXX is this the right spec?  Should we allow general Rotations?
 	public static Warp rotateAndMove(Position from, Position to) {
 	    assert from.facing instanceof ExactRotation;
 	    assert to.facing instanceof ExactRotation;
 	    if (from.equals(to)) return NONE;
 	    ExactRotation rot = (ExactRotation) to.facing.add(from.facing.amount.negate());
 	    Position nFrom = rotateCWAroundOrigin(from,rot);
 	    final Position warp = new Position
 		(to.x.subtract(nFrom.x), to.y.subtract(nFrom.y),
 		 rot);
 	    Warp w = new Warp() {
 	        public Position warp(Position p, Fraction time) {
 		    p = rotateCWAroundOrigin(p, (ExactRotation) warp.facing);
 		    return p.relocate
                     (p.x.add(warp.x), p.y.add(warp.y), p.facing);
 		}
 	    };
 	    assert to.setFlags(from.flags.toArray(new Flag[0])).equals(w.warp(from,Fraction.ZERO)) : "bad warp "+to+" vs "+w.warp(from, Fraction.ZERO);
 	    return w;
 	}
 	// helper method for rotateAndMove
 	private static Position rotateCWAroundOrigin(Position p, ExactRotation amt) {
 	    Fraction x = p.x.multiply(amt.toY()).add(p.y.multiply(amt.toX()));
 	    Fraction y = p.y.multiply(amt.toY()).subtract(p.x.multiply(amt.toX()));
 	    return p.relocate(x, y, p.facing.add(amt.amount));
 	}
     }
 }

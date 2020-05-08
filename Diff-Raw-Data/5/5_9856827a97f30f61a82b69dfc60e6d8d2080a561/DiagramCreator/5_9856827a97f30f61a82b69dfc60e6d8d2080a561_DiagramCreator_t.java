 package icircles.concreteDiagram;
 
 import java.awt.Rectangle;
 import java.awt.geom.Area;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import icircles.decomposition.Decomposer;
 import icircles.decomposition.DecompositionStep;
 import icircles.decomposition.DecompositionStrategy;
 import icircles.gui.CirclesPanel;
 
 import icircles.abstractDescription.AbstractBasicRegion;
 import icircles.abstractDescription.AbstractCurve;
 import icircles.abstractDescription.AbstractDescription;
 import icircles.abstractDescription.AbstractSpider;
 import icircles.recomposition.RecompData;
 import icircles.recomposition.Recomposer;
 import icircles.recomposition.RecompositionStep;
 import icircles.recomposition.RecompositionStrategy;
 import icircles.util.CannotDrawException;
 import icircles.util.DEB;
 
 class AngleIterator
 {
 	private int[] ints = {0, 8, 4, 12, 2, 6, 10, 14, 1, 3, 5, 7, 9, 11, 13, 15};
 	private int index = -1;
 
 	public AngleIterator(){}
 	
 	public boolean hasNext()
 	{
 		return index < ints.length - 1;
 	}
 	public double next_angle()
 	{
 	   index++;
 	   int mod_index = (index%ints.length);
        double angle = Math.PI * 2 * ints[mod_index] / (1.0 * ints.length);
 	   return angle;		
 	}	
 }
 
 public class DiagramCreator {
 
 	AbstractDescription m_initial_diagram;
 	
     final static int smallest_rad = 3;
     ArrayList<DecompositionStep> d_steps;
     ArrayList<RecompositionStep> r_steps;
     HashMap<AbstractBasicRegion, Double> zoneScores;
     HashMap<AbstractCurve, Double> contScores;
     HashMap<AbstractCurve, Double> guide_sizes;
     HashMap<AbstractCurve, CircleContour> map;
     ArrayList<CircleContour> circles;
     
     int debug_image_number = 0;
     int debug_size = 50;
 
     public DiagramCreator( AbstractDescription ad )
     {
     	m_initial_diagram = ad;
         d_steps = new ArrayList<DecompositionStep>();
         r_steps = new ArrayList<RecompositionStep>();
         Decomposer d = new Decomposer();
         d_steps.addAll(d.decompose(ad)); 
         Recomposer r = new Recomposer();
         r_steps.addAll(r.recompose(d_steps));
         map = new HashMap<AbstractCurve, CircleContour>();
     }
 
     public DiagramCreator( AbstractDescription ad, 
     		DecompositionStrategy decomp_strategy,
     		RecompositionStrategy recomp_strategy )
     {
     	m_initial_diagram = ad;
         d_steps = new ArrayList<DecompositionStep>();
         r_steps = new ArrayList<RecompositionStep>();
         Decomposer d = new Decomposer(decomp_strategy);
         d_steps.addAll(d.decompose(ad)); 
         Recomposer r = new Recomposer(recomp_strategy);
         r_steps.addAll(r.recompose(d_steps));
         map = new HashMap<AbstractCurve, CircleContour>();
     }
     
     public ConcreteDiagram createDiagram(int size) throws CannotDrawException {
         make_guide_sizes(); // scores zones too
         /*
         Rectangle2D.Double box = null;
         if (guide_sizes.size() < 1) {
             box = new Rectangle2D.Double(0, 0, 1000, 1000);
         } else {
             box = new Rectangle2D.Double(0, 0, 1000 * guide_sizes.size(), 1000 * guide_sizes.size());
         }
         */
         circles = new ArrayList<CircleContour>();
         boolean ok = createCircles(/*box, */size);
         		
         if (!ok) {
             circles = null;
             return null;
         }
 
         CircleContour.fitCirclesToSize(circles, size);
 
         ArrayList<ConcreteZone> shadedZones = createShadedZones();
         
         ArrayList<ConcreteSpider> spiders = createSpiders();
         
         ConcreteDiagram result = new ConcreteDiagram(new Rectangle2D.Double(0, 0, size, size),
                 circles, shadedZones, spiders);
         return result;
     }
 
     private ArrayList<ConcreteSpider> createSpiders() throws CannotDrawException{
     	ArrayList<ConcreteSpider> result = new ArrayList<ConcreteSpider>();
 
     	HashMap<AbstractBasicRegion, Integer> footCount = new HashMap<AbstractBasicRegion, Integer>();    	
     	Iterator<AbstractSpider> it = m_initial_diagram.getSpiderIterator();
     	while(it.hasNext())
     	{
     		AbstractSpider as = it.next();
     		for(AbstractBasicRegion abr : as.get_feet())
     		{
     			Integer oldCount = footCount.get(abr);
     			Integer newCount = null;
     			if(oldCount != null)
     			{
     				newCount = new Integer(oldCount.intValue() + 1);
     			}
     			else
     			{
     				newCount = new Integer(1);
     			}
 				footCount.put(abr, newCount);
     		}
     	}
     	// now, for each zone, I know how many feet are in that zone
     	// build some feet
     	HashMap<AbstractBasicRegion, ArrayList<ConcreteSpiderFoot>> drawnFeet = 
     			new HashMap<AbstractBasicRegion, ArrayList<ConcreteSpiderFoot>>();
 		for(AbstractBasicRegion abr : footCount.keySet())
 		{
 			ArrayList<ConcreteSpiderFoot> footList = new ArrayList<ConcreteSpiderFoot>();
 			drawnFeet.put(abr, footList);
 			Integer num_required = footCount.get(abr);
 	        ArrayList<AbstractCurve> acs = new ArrayList<AbstractCurve>();
 	        for(int i = 0; i < num_required.intValue(); i++)
 	        {
 	        acs.add(new AbstractCurve(null));
 	        }
 	        Rectangle2D.Double box = CircleContour.makeBigOuterBox(circles);
 	        RecompositionStep last_step = r_steps.get(r_steps.size() - 1);
 	        AbstractDescription last_diag = last_step.to();
 	        
 	        AbstractBasicRegion zone_in_last_diag = last_diag.getLabelEquivalentZone(abr);
 	        if(zone_in_last_diag == null)
 	        	throw new CannotDrawException("problem with spider habitat");
 	        
             ArrayList<CircleContour> cs = findCircleContours(box, smallest_rad, 3,
             		zone_in_last_diag, last_diag, acs, 3);
             for(CircleContour cc : cs)
             {
             	ConcreteSpiderFoot foot = new ConcreteSpiderFoot();
             	foot.x = cc.cx;
             	foot.y = cc.cy;
             	footList.add(foot);
             }
 		}
 		
 		// TODO collect good choices of feet into spiders
 		// for now, we just pick feet which are in the right zones.		
 		it = m_initial_diagram.getSpiderIterator();
     	while(it.hasNext())
     	{
     		AbstractSpider as = it.next();
     		ConcreteSpider cs = new ConcreteSpider();
     		for(AbstractBasicRegion abr : as.get_feet())
     		{
     			ArrayList<ConcreteSpiderFoot> footList = drawnFeet.get(abr);
     			if(footList == null || footList.size() == 0)
     				throw new CannotDrawException("spider foot problem");
     			
     			ConcreteSpiderFoot foot = footList.get(0);
     			footList.remove(0);
     			cs.feet.add(foot);
     		}
     		// choose a foot as "most central" - shortest leg length sum
     		ConcreteSpiderFoot centralFoot = null;
     		double best_dist_sum = Double.MAX_VALUE;
     		for(ConcreteSpiderFoot centreCandidate : cs.feet)
     		{
     			double distSum = 0;
         		for(ConcreteSpiderFoot other : cs.feet)
         		{
         			if(other == centreCandidate)
         				continue;
         			distSum += Math.sqrt((centreCandidate.x - other.x)*(centreCandidate.x - other.x) +
         								 (centreCandidate.y - other.y)*(centreCandidate.y - other.y));
         		}
         		if(distSum < best_dist_sum)
         		{
         			best_dist_sum = distSum;
         			centralFoot = centreCandidate;
         		}    			
     		}
     		for(ConcreteSpiderFoot other : cs.feet)
     		{
     			if(other == centralFoot)
     				continue;
 				ConcreteSpiderLeg leg = new ConcreteSpiderLeg();
 				leg.from = centralFoot;
 				leg.to = other;
 				cs.legs.add(leg);    			
     		}
     		
     		result.add(cs);
     	}
     	
    	// Now we want to avoid spiders that overlap - especially 
    	// those with a leg passing through the foot of another spider.
    	// For now, just nudge the spider foot off the offending leg.
    	// Check that the new foot is still in its relevant abstract basic region.
    	
     	return result;
 	}
 
 	private void make_guide_sizes() {
         guide_sizes = new HashMap<AbstractCurve, Double>();
         if (r_steps.size() == 0) {
             return;
         }
 
         RecompositionStep last_step = r_steps.get(r_steps.size() - 1);
         AbstractDescription last_diag = last_step.to();
 
         zoneScores = new HashMap<AbstractBasicRegion, Double>();
         double total_score = 0.0;
         {
             Iterator<AbstractBasicRegion> zIt = last_diag.getZoneIterator();
             while (zIt.hasNext()) {
                 AbstractBasicRegion abr = zIt.next();
                 double score = scoreZone(abr, last_diag);
                 total_score += score;
                 zoneScores.put(abr, score);
             }
         }
 
         contScores = new HashMap<AbstractCurve, Double>();
         Iterator<AbstractCurve> cIt = last_diag.getContourIterator();
         while (cIt.hasNext()) {
             AbstractCurve ac = cIt.next();
             double cScore = 0;
             Iterator<AbstractBasicRegion> zIt = last_diag.getZoneIterator();
             while (zIt.hasNext()) {
                 AbstractBasicRegion abr = zIt.next();
                 if (abr.is_in(ac)) {
                     cScore += zoneScores.get(abr);
                 }
             }
             contScores.put(ac, cScore);
             double guide_size = Math.exp(0.75 * Math.log(cScore / total_score)) * 200;
             guide_sizes.put(ac, guide_size);
         }
     }
 
     private double scoreZone(AbstractBasicRegion abr, AbstractDescription context) {
         return 1.0;
     }
 
     private ArrayList<ConcreteZone> createShadedZones() {
         ArrayList<ConcreteZone> result = new ArrayList<ConcreteZone>();
         AbstractDescription final_diagram = null;
         if (d_steps.size() == 0) {
             final_diagram = m_initial_diagram;
         }
         else
         {
         	final_diagram = r_steps.get(r_steps.size() - 1).to();
         }
         // which zones in final_diagram were shaded in initial_diagram?
         // which zones in final_diagram were not in initial_diagram, or specified shaded in initial_diagram?
 
         if (DEB.level > 2) {
             Iterator<AbstractBasicRegion> it = m_initial_diagram.getZoneIterator();
             while (it.hasNext()) {
                 System.out.println("initial zone " + it.next().debug());
             }
             it = final_diagram.getZoneIterator();
             while (it.hasNext()) {
                 System.out.println("final zone " + it.next().debug());
             }
         }
 
         Iterator<AbstractBasicRegion> it = final_diagram.getZoneIterator();
         while (it.hasNext()) {
             AbstractBasicRegion z = it.next();
             AbstractBasicRegion matched_z = m_initial_diagram.getLabelEquivalentZone(z);
             if (matched_z == null || m_initial_diagram.hasShadedZone(matched_z)) {
                 if (DEB.level > 2) {
                     System.out.println("extra zone " + z.debug());
                 }
                 ConcreteZone cz = makeConcreteZone(z);
                 result.add(cz);
             }
         }
         return result;
     }
 
     private ConcreteZone makeConcreteZone(AbstractBasicRegion z) {
         ArrayList<CircleContour> includingCircles = new ArrayList<CircleContour>();
         ArrayList<CircleContour> excludingCircles = new ArrayList<CircleContour>(circles);
         Iterator<AbstractCurve> acIt = z.getContourIterator();
         while (acIt.hasNext()) {
             AbstractCurve ac = acIt.next();
             CircleContour containingCC = map.get(ac);
             excludingCircles.remove(containingCC);
             includingCircles.add(containingCC);
         }
         ConcreteZone cz = new ConcreteZone(z, includingCircles, excludingCircles);
         return cz;
     }
 
     private boolean createCircles(int deb_size) throws CannotDrawException {
     	debug_size = deb_size;
     	debug_image_number = 0;
         BuildStep bs = null;
         BuildStep tail = null;
         for (RecompositionStep rs : r_steps) {
             // we need to add the new curves with regard to their placement
             // relative to the existing ones in the map
             Iterator<RecompData> it = rs.getRecompIterator();
             while (it.hasNext()) {
                 RecompData rd = it.next();
                 BuildStep newOne = new BuildStep(rd);
                 if (bs == null) {
                     bs = newOne;
                     tail = newOne;
                 } else {
                     tail.next = newOne;
                     tail = newOne;
                 }
             }
         }
 
         shuffle_and_combine(bs);
 
         BuildStep step = bs;
         stepLoop:
         while (step != null) {
             DEB.out(2, "new build step");
             Rectangle2D.Double outerBox = CircleContour.makeBigOuterBox(circles);
                         
             // we need to add the new curves with regard to their placement
             // relative to the existing ones in the map
             if (step.recomp_data.size() > 1) {
                 if (step.recomp_data.get(0).split_zones.size() == 1) {
                     // we have a symmetry of nested contours.
                     // try to add them together
                     RecompData rd = step.recomp_data.get(0);
                     AbstractBasicRegion zone = rd.split_zones.get(0);
 
                     RecompositionStep last_step = r_steps.get(r_steps.size() - 1);
                     AbstractDescription last_diag = last_step.to();
 
                     AbstractCurve ac = rd.added_curve;
                     double suggested_rad = guide_sizes.get(ac);
 
                     ArrayList<AbstractCurve> acs = new ArrayList<AbstractCurve>();
                     for (RecompData rd2 : step.recomp_data) {
                         ac = rd2.added_curve;
                         acs.add(ac);
                     }
 
                     // put contours into a zone
                     ArrayList<CircleContour> cs = findCircleContours(outerBox, smallest_rad, suggested_rad,
                             zone, last_diag, acs, debug_image_number);
                     
                     if (cs != null && cs.size() > 0) {
                         DEB.assertCondition(cs.size() == step.recomp_data.size(), "not enough circles for rds");
                         for (int i = 0; i < cs.size(); i++) {
                             CircleContour c = cs.get(i);
                             ac = step.recomp_data.get(i).added_curve;
                             DEB.assertCondition(
                                     c.ac.getLabel() == ac.getLabel(), "mismatched labels");
                             map.put(ac, c);
                             addCircle(c);
                         }
                         step = step.next;
                         continue stepLoop;
                     }
                 } else if (step.recomp_data.get(0).split_zones.size() == 2) {
                     // we have a symmetry of 1-piercings.
                     // try to add them together
                 	
                 	// Look at the 1st 1-piercing
                 	RecompData rd0 = step.recomp_data.get(0);
                     AbstractBasicRegion abr0 = rd0.split_zones.get(0);
                     AbstractBasicRegion abr1 = rd0.split_zones.get(1);
                     AbstractCurve piercingCurve = rd0.added_curve;
                     
                     AbstractCurve pierced_ac = abr0.getStraddledContour(abr1);
                     CircleContour pierced_cc = map.get(pierced_ac);
                     ConcreteZone cz0 = makeConcreteZone(abr0);
                     ConcreteZone cz1 = makeConcreteZone(abr1);
                     Area a = new Area(cz0.getShape(outerBox));
                     a.add(cz1.getShape(outerBox));
 
                     double suggested_rad = guide_sizes.get(piercingCurve);
 
                     DEB.show(4, a, "a for 1-piercings "+debug_image_number);
                     
                 	// We have made a piercing which is centred on the circumference of circle c.
                 	// but if the contents of rd.addedCurve are not equally balanced between
                 	// things inside c and things outside, we may end up squashing lots
                 	// into half of rd.addedCurve, leaving the other half looking empty.
                 	// See if we can nudge c outwards or inwards to accommodate
                 	// its contents.
                 	
                 	// iterate through zoneScores, looking for zones inside c,
                 	// then ask whether they are inside or outside cc.  If we
                 	// get a big score outside, then try to move c outwards.
                 	
                 	//  HashMap<AbstractBasicRegion, Double> zoneScores;
                 	double score_in_c = 0.0;
                 	double score_out_of_c = 0.0;
                 	
                 	double center_of_circle_lies_on_rad = pierced_cc.radius;
                 	                	
                 	Set<AbstractBasicRegion> allZones = zoneScores.keySet();
                 	for(AbstractBasicRegion abr : allZones)
                 	{
                 		DEB.out(1, "compare "+abr.debug()+" against "+piercingCurve.debug());
                 		if(!abr.is_in(piercingCurve))
                 			continue;
                 		DEB.out(1, "OK "+abr.debug()+" is in "+piercingCurve.debug()+", so compare against "+pierced_ac.debug());
                 		if(abr.is_in(pierced_ac))
                 			score_in_c += zoneScores.get(abr).doubleValue();
                 		else
                 			score_out_of_c += zoneScores.get(abr).doubleValue();                    		
                 	}
             		DEB.out(3, "scores for "+piercingCurve+" are inside="+score_in_c+" and outside="+score_out_of_c);
             		
             		if(score_out_of_c > score_in_c)
             		{
             			double nudge = suggested_rad * 0.3;
             			center_of_circle_lies_on_rad += nudge;
             		}
             		else if(score_out_of_c < score_in_c)
             		{
             			double nudge = Math.min(suggested_rad * 0.3, (pierced_cc.radius * 2 - suggested_rad) * 0.5) ;
             			center_of_circle_lies_on_rad -= nudge;
             		}
                     
                     double guide_rad = guide_sizes.get(step.recomp_data.get(0).added_curve);
                     int sampleSize = (int) (Math.PI / Math.asin(guide_rad / pierced_cc.radius));
                     if (sampleSize >= step.recomp_data.size()) {
                         int num_ok = 0;
                         for (int i = 0; i < sampleSize; i++) {
                             double angle = i * Math.PI * 2.0 / sampleSize;
                             double x = pierced_cc.cx + Math.cos(angle) * center_of_circle_lies_on_rad;
                             double y = pierced_cc.cy + Math.sin(angle) * center_of_circle_lies_on_rad;
                             if (a.contains(x, y)) {
                                 CircleContour sample = new CircleContour(x, y, guide_rad,
                                         step.recomp_data.get(0).added_curve);
                                 if (containedIn(sample, a)) {
                                     num_ok++;
                                 }
                             }
                         }
                         if (num_ok >= step.recomp_data.size()) {
                             if (num_ok == sampleSize) {
                                 // all OK.
                                 for (int i = 0; i < step.recomp_data.size(); i++) {
                                     double angle = 0.0 + i * Math.PI * 2.0 / step.recomp_data.size();
                                     double x = pierced_cc.cx + Math.cos(angle) * center_of_circle_lies_on_rad;
                                     double y = pierced_cc.cy + Math.sin(angle) * center_of_circle_lies_on_rad;
                                     if (a.contains(x, y)) {
                                         AbstractCurve added_curve = step.recomp_data.get(i).added_curve;
                                         CircleContour c = new CircleContour(x, y, guide_rad, added_curve);
                                         abr0 = step.recomp_data.get(i).split_zones.get(0);
                                         abr1 = step.recomp_data.get(i).split_zones.get(1);
                                         
                                         map.put(added_curve, c);
                                         addCircle(c);
                                     }
                                 }
                                 step = step.next;
                                 continue stepLoop;
                             } else if (num_ok > sampleSize) {  // BUG?  Doesn't make sense
                                 num_ok = 0;
                                 for (int i = 0; i < sampleSize; i++) {
                                     double angle = 0.0 + i * Math.PI * 2.0 / sampleSize;
                                     double x = pierced_cc.cx + Math.cos(angle) * center_of_circle_lies_on_rad;
                                     double y = pierced_cc.cy + Math.sin(angle) * center_of_circle_lies_on_rad;
                                     if (a.contains(x, y)) {
                                         AbstractCurve added_curve = step.recomp_data.get(i).added_curve;
                                         CircleContour c = new CircleContour(x, y, guide_rad, added_curve);
                                         if (containedIn(c, a)) {
                                             abr0 = step.recomp_data.get(num_ok).split_zones.get(0);
                                             abr1 = step.recomp_data.get(num_ok).split_zones.get(1);
                                             map.put(added_curve, c);
                                             addCircle(c);
                                             num_ok++;
                                             if (num_ok == step.recomp_data.size()) {
                                                 break;
                                             }
                                         }
                                     }
                                 }
                                 step = step.next;
                                 continue stepLoop;
                             }
                         }
                     }
                 }
             }
             
             for (RecompData rd : step.recomp_data) {
                 AbstractCurve ac = rd.added_curve;
                 double suggested_rad = guide_sizes.get(ac);
                 if (rd.split_zones.size() == 1) {
                     // add a nested contour---------------------------------------------------
                     // add a nested contour---------------------------------------------------
                     // add a nested contour---------------------------------------------------
 
                     // look ahead - are we going to add a piercing to this?
                     // if so, push it to one side to make space
                     boolean will_pierce = false;
                     BuildStep future_bs = bs.next;
                     while (future_bs != null) {
                         if (future_bs.recomp_data.get(0).split_zones.size() == 2) {
                             AbstractBasicRegion abr0 = future_bs.recomp_data.get(0).split_zones.get(0);
                             AbstractBasicRegion abr1 = future_bs.recomp_data.get(0).split_zones.get(1);
                             AbstractCurve ac_future = abr0.getStraddledContour(abr1);
                             if (ac_future == ac) {
                                 will_pierce = true;
                                 break;
                             }
                         }
                         future_bs = future_bs.next;
                     }
 
                     if (DEB.level > 3) {
                         System.out.println("make a nested contour");
                     }
                     // make a circle inside containingCircles, outside excludingCirles.
 
                     AbstractBasicRegion zone = rd.split_zones.get(0);
 
                     RecompositionStep last_step = r_steps.get(r_steps.size() - 1);
                     AbstractDescription last_diag = last_step.to();
 
                     // put contour into a zone
                     CircleContour c = findCircleContour(outerBox, smallest_rad, suggested_rad,
                             zone, last_diag, ac, debug_image_number);
 
                     if (c == null) {
                         throw new CannotDrawException("cannot place nested contour");
                     }
 
                     if (will_pierce && rd.split_zones.get(0).getNumContours() > 0) {
                         // nudge to the left
                         c.cx -= c.radius * 0.5;
 
                         ConcreteZone cz = makeConcreteZone(rd.split_zones.get(0));
                         Area a = new Area(cz.getShape(outerBox));
                         if (!containedIn(c, a)) {
                             c.cx += c.radius * 0.25;
                             c.radius *= 0.75;
                         }
                     }
                     map.put(ac, c);
                     addCircle(c);
                 } else if (rd.split_zones.size() == 2) {
                     // add a single piercing---------------------------------------------------
                     // add a single piercing---------------------------------------------------
                     // add a single piercing---------------------------------------------------
 
                     if (DEB.level > 3) {
                         System.out.println("make a single-piercing contour");
                     }
                     AbstractBasicRegion abr0 = rd.split_zones.get(0);
                     AbstractBasicRegion abr1 = rd.split_zones.get(1);
                     AbstractCurve c = abr0.getStraddledContour(abr1);
                     CircleContour cc = map.get(c);
                     ConcreteZone cz0 = makeConcreteZone(abr0);
                     ConcreteZone cz1 = makeConcreteZone(abr1);
                     Area a = new Area(cz0.getShape(outerBox));
 
                     DEB.show(4, a, "for single piercing first half "+debug_image_number);
                     DEB.show(4, new Area(cz1.getShape(outerBox)), "for single piercing second half "+debug_image_number);
                     a.add(cz1.getShape(outerBox));
                     
                     DEB.show(4, a, "for single piercing "+debug_image_number);
 
                 	// We have made a piercing which is centred on the circumference of circle c.
                 	// but if the contents of rd.addedCurve are not equally balanced between
                 	// things inside c and things outside, we may end up squashing lots
                 	// into half of rd.addedCurve, leaving the other half looking empty.
                 	// See if we can nudge c outwards or inwards to accommodate
                 	// its contents.
                 	
                 	// iterate through zoneScores, looking for zones inside c,
                 	// then ask whether they are inside or outside cc.  If we
                 	// get a big score outside, then try to move c outwards.
                 	
                 	//  HashMap<AbstractBasicRegion, Double> zoneScores;
                 	double score_in_c = 0.0;
                 	double score_out_of_c = 0.0;
                 	
                 	double center_of_circle_lies_on_rad = cc.radius;
                 	double smallest_allowed_rad = smallest_rad;
                 	
                 	Set<AbstractBasicRegion> allZones = zoneScores.keySet();
                 	for(AbstractBasicRegion abr : allZones)
                 	{
                 		DEB.out(1, "compare "+abr.debug()+" against "+c.debug());
                 		if(!abr.is_in(rd.added_curve))
                 			continue;
                 		DEB.out(1, "OK "+abr.debug()+" is in "+c.debug()+", so compare against "+cc.debug());
                 		if(abr.is_in(c))
                 			score_in_c += zoneScores.get(abr).doubleValue();
                 		else
                 			score_out_of_c += zoneScores.get(abr).doubleValue();                    		
                 	}
             		DEB.out(3, "scores for "+c+" are inside="+score_in_c+" and outside="+score_out_of_c);
             		
             		if(score_out_of_c > score_in_c)
             		{
             			double nudge = suggested_rad * 0.3;
             			smallest_allowed_rad += nudge;
             			center_of_circle_lies_on_rad += nudge;
             		}
             		else if(score_out_of_c < score_in_c)
             		{
             			double nudge = Math.min(suggested_rad * 0.3, (cc.radius * 2 - suggested_rad) * 0.5) ;
             			smallest_allowed_rad += nudge;
             			center_of_circle_lies_on_rad -= nudge;
             		}
                     
                     // now place circles around cc, checking whether they fit into a
                     CircleContour solution = null;
                     for (AngleIterator ai = new AngleIterator(); ai.hasNext(); )
                     	{
                         double angle = ai.next_angle();
                         double x = cc.cx + Math.cos(angle) * center_of_circle_lies_on_rad;
                         double y = cc.cy + Math.sin(angle) * center_of_circle_lies_on_rad;
                         if (a.contains(x, y)) {
                             // how big a circle can we make?
                             double start_rad;
                             if (solution != null) {
                                 start_rad = solution.radius + smallest_rad;
                             } else {
                                 start_rad = smallest_rad;
                             }
                             CircleContour attempt = growCircleContour(a, rd.added_curve, 
                             		x, y, suggested_rad, 
                             		start_rad, 
                             		smallest_allowed_rad);
                             if (attempt != null) {
                                 solution = attempt;
                                 if (solution.radius == guide_sizes.get(ac)) {
                                     break; // no need to try any more
                                 }
                             }
 
                         }//check that the centre is ok
                     }// loop for different centre placement
                     if (solution == null) // no single piercing found which was OK
                     {
                         throw new CannotDrawException("1-peircing no fit");
                     }
                     else 
                     {
                         DEB.out(2, "added a single piercing labelled " + solution.ac.getLabel());
                         map.put(rd.added_curve, solution);
                         addCircle(solution);
                     }
                 } else {
                     //double piercing
                     AbstractBasicRegion abr0 = rd.split_zones.get(0);
                     AbstractBasicRegion abr1 = rd.split_zones.get(1);
                     AbstractBasicRegion abr2 = rd.split_zones.get(2);
                     AbstractBasicRegion abr3 = rd.split_zones.get(3);
                     AbstractCurve c1 = abr0.getStraddledContour(abr1);
                     AbstractCurve c2 = abr0.getStraddledContour(abr2);
                     CircleContour cc1 = map.get(c1);
                     CircleContour cc2 = map.get(c2);
 
                     double[][] intn_coords = intersect(cc1.cx, cc1.cy, cc1.radius,
                             cc2.cx, cc2.cy, cc2.radius);
                     if (intn_coords == null) {
                         System.out.println("double piercing on non-intersecting circles");
                         return false;
                     }
 
                     ConcreteZone cz0 = makeConcreteZone(abr0);
                     ConcreteZone cz1 = makeConcreteZone(abr1);
                     ConcreteZone cz2 = makeConcreteZone(abr2);
                     ConcreteZone cz3 = makeConcreteZone(abr3);
                     Area a = new Area(cz0.getShape(outerBox));
                     a.add(cz1.getShape(outerBox));
                     a.add(cz2.getShape(outerBox));
                     a.add(cz3.getShape(outerBox));
 
                     DEB.show(4, a, "for double piercing "+debug_image_number);
                     
                     double cx, cy;
                     if (a.contains(intn_coords[0][0], intn_coords[0][1])) {
                         if (DEB.level > 2) {
                             System.out.println("intn at (" + intn_coords[0][0] + "," + intn_coords[0][1] + ")");
                         }
                         cx = intn_coords[0][0];
                         cy = intn_coords[0][1];
                     } else if (a.contains(intn_coords[1][0], intn_coords[1][1])) {
                         if (DEB.level > 2) {
                             System.out.println("intn at (" + intn_coords[1][0] + "," + intn_coords[1][1] + ")");
                         }
                         cx = intn_coords[1][0];
                         cy = intn_coords[1][1];
                     } else {
                         if (DEB.level > 2) {
                             System.out.println("no suitable intn for double piercing");
                         }
                         throw new CannotDrawException("2peircing + disjoint");
                     }
 
                     CircleContour solution = growCircleContour(a, rd.added_curve, cx, cy,
                             suggested_rad, smallest_rad, smallest_rad);
                     if (solution == null) // no double piercing found which was OK
                     {
                         throw new CannotDrawException("2peircing no fit");
                     } else {
                         DEB.out(2, "added a double piercing labelled " + solution.ac.getLabel());
                         map.put(rd.added_curve, solution);
                         addCircle(solution);
                     }
                 }// if/else/else about piercing type
             }// next RecompData in the BuildStep
             step = step.next;
         }// go to next BuildStep
         
         DEB.showFilmStrip();
         
         return true;
     }
 
     private void shuffle_and_combine(BuildStep steplist) {
         // collect together additions which are
         //  (i) nested in the same zone
         //  (ii) single-piercings with the same zones
         //  (iii) will have the same radius (have the same "score")
 
         BuildStep bs = steplist;
         while (bs != null) {
             DEB.assertCondition(bs.recomp_data.size() == 1, "not ready for multistep");
             if (bs.recomp_data.get(0).split_zones.size() == 1) {
                 RecompData rd = bs.recomp_data.get(0);
                 AbstractBasicRegion abr = rd.split_zones.get(0);
                 // look ahead - are there other similar nested additions?
                 BuildStep beforefuturebs = bs;
                 while (beforefuturebs != null && beforefuturebs.next != null) {
                     RecompData rd2 = beforefuturebs.next.recomp_data.get(0);
                     if (rd2.split_zones.size() == 1) {
                         AbstractBasicRegion abr2 = rd2.split_zones.get(0);
                         if (abr.isLabelEquivalent(abr2)) {
                             DEB.out(2, "found matching abrs " + abr.debug() + ", " + abr2.debug());
                             // check scores match
 
                             double abrScore = contScores.get(rd.added_curve);
                             double abrScore2 = contScores.get(rd2.added_curve);
                             DEB.assertCondition(abrScore > 0 && abrScore2 > 0, "zones must have score");
                             DEB.out(2, "matched nestings " + abr.debug() + " and " + abr2.debug()
                                     + "\n with scores " + abrScore + " and " + abrScore2);
                             if (abrScore == abrScore2) {
                                 // unhook futurebs and insert into list after bs
                                 BuildStep to_move = beforefuturebs.next;
                                 beforefuturebs.next = to_move.next;
 
                                 bs.recomp_data.add(to_move.recomp_data.get(0));
                             }
                         }
                     }
                     beforefuturebs = beforefuturebs.next;
                 }// loop through futurebs's to see if we insert another
             }// check - are we adding a nested contour?
             else if (bs.recomp_data.get(0).split_zones.size() == 2) {// we are adding a 1-piercing
                 RecompData rd = bs.recomp_data.get(0);
                 AbstractBasicRegion abr1 = rd.split_zones.get(0);
                 AbstractBasicRegion abr2 = rd.split_zones.get(1);
                 // look ahead - are there other similar 1-piercings?
                 BuildStep beforefuturebs = bs;
                 while (beforefuturebs != null && beforefuturebs.next != null) {
                     RecompData rd2 = beforefuturebs.next.recomp_data.get(0);
                     if (rd2.split_zones.size() == 2) {
                         AbstractBasicRegion abr3 = rd2.split_zones.get(0);
                         AbstractBasicRegion abr4 = rd2.split_zones.get(1);
                         if ((abr1.isLabelEquivalent(abr3) && abr2.isLabelEquivalent(abr4))
                                 || (abr1.isLabelEquivalent(abr4) && abr2.isLabelEquivalent(abr3))) {
 
                             DEB.out(2, "found matching abrs " + abr1.debug() + ", " + abr2.debug());
                             // check scores match
                             double abrScore = contScores.get(rd.added_curve);
                             double abrScore2 = contScores.get(rd2.added_curve);
                             DEB.assertCondition(abrScore > 0 && abrScore2 > 0, "zones must have score");
                             DEB.out(2, "matched piercings " + abr1.debug() + " and " + abr2.debug()
                                     + "\n with scores " + abrScore + " and " + abrScore2);
                             if (abrScore == abrScore2) {
                                 // unhook futurebs and insert into list after bs
                                 BuildStep to_move = beforefuturebs.next;
                                 beforefuturebs.next = to_move.next;
 
                                 bs.recomp_data.add(to_move.recomp_data.get(0));
                                 continue;
                             }
                         }
                     }
                     beforefuturebs = beforefuturebs.next;
                 }// loop through futurebs's to see if we insert another
             }
 
             bs = bs.next;
         }// bsloop
     }
 
     void addCircle(CircleContour c) {
         if (DEB.level > 2) {
             System.out.println("adding " + c.debug());
         }
         circles.add(c);
         
         DEB_show_frame(3, debug_image_number, debug_size);
         debug_image_number++;
     }
 
     private CircleContour growCircleContour(Area a, AbstractCurve ac,
             double cx, double cy,
             double suggested_rad, double start_rad,
             double smallest_rad) {
         CircleContour attempt = new CircleContour(cx, cy, suggested_rad, ac);
         if (containedIn(attempt, a)) {
             return new CircleContour(cx, cy, suggested_rad, ac);
         }
 
         boolean ok = true;
         double good_rad = -1.0;
         double rad = start_rad;
         while (ok) {
             attempt = new CircleContour(cx, cy, rad, ac);
             if (containedIn(attempt, a)) {
                 good_rad = rad;
                 rad *= 1.5;
             } else {
                 break;
             }
         }// loop for increasing radii
         if (good_rad < 0.0) {
             return null;
         }
         CircleContour sol = new CircleContour(cx, cy, good_rad, ac);
         return sol;
     }
 
     private CircleContour findCircleContour(Rectangle2D.Double outerBox,
             int smallest_rad,
             double guide_rad,
             AbstractBasicRegion zone,
             AbstractDescription last_diag,
             AbstractCurve ac,
             int debug_index) throws CannotDrawException {
         ArrayList<AbstractCurve> acs = new ArrayList<AbstractCurve>();
         acs.add(ac);
         ArrayList<CircleContour> result = findCircleContours(outerBox,
                 smallest_rad, guide_rad, zone, last_diag, acs,
                 debug_index);
         if (result == null || result.size() == 0) {
             return null;
         } else {
             return result.get(0);
         }
     }
 
     private boolean all_ok_in(int lowi, int highi, int lowj, int highj,
             PotentialCentre[][] ok_array, int Ni, int Nj) {
         boolean all_ok = true;
         for (int i = lowi; all_ok && i < highi + 1; i++) {
             for (int j = lowj; all_ok && j < highj + 1; j++) {
                 if (i >= Ni || j >= Nj || !ok_array[i][j].ok) {
                     all_ok = false;
                 }
             }
         }
         return all_ok;
     }
 
     private ArrayList<CircleContour> findCircleContours(Rectangle2D.Double outerBox,
             int smallest_rad,
             double guide_rad,
             AbstractBasicRegion zone,
             AbstractDescription last_diag,
             ArrayList<AbstractCurve> acs,
             int debug_index) throws CannotDrawException {
         ArrayList<CircleContour> result = new ArrayList<CircleContour>();
 
         // special case : our first contour
         boolean is_first_contour = !map.keySet().iterator().hasNext();
         if (is_first_contour) {
             int label_index = 0;
             for (AbstractCurve ac : acs) {
                 result.add(new CircleContour(
                 		outerBox.getCenterX() - 0.5 * (guide_rad * 3 * acs.size()) + 1.5 * guide_rad
                         + guide_rad * 3 * label_index,
                         outerBox.getCenterY(),
                         guide_rad, ac));
                 label_index++;
             }
             DEB.out(2, "added first contours into diagram, labelled " + acs.get(0).getLabel());
             return result;
         }
 
         if (zone.getNumContours() == 0) {
             // adding a contour outside everything else
             double minx = Double.MAX_VALUE;
             double maxx = Double.MIN_VALUE;
             double miny = Double.MAX_VALUE;
             double maxy = Double.MIN_VALUE;
 
             for (CircleContour c : circles) {
                 if (c.getMinX() < minx) {
                     minx = c.getMinX();
                 }
                 if (c.getMaxX() > maxx) {
                     maxx = c.getMaxX();
                 }
                 if (c.getMinY() < miny) {
                     miny = c.getMinY();
                 }
                 if (c.getMaxY() > maxy) {
                     maxy = c.getMaxY();
                 }
             }
             if (acs.size() == 1) {
                 if (maxx - minx < maxy - miny) {// R
                     result.add(new CircleContour(
                             maxx + guide_rad * 1.5,
                             (miny + maxy) * 0.5,
                             guide_rad, acs.get(0)));
                 } else {// B
                     result.add(new CircleContour(
                             (minx + maxx) * 0.5,
                             maxy + guide_rad * 1.5,
                             guide_rad, acs.get(0)));
                 }
             } else if (acs.size() == 2) {
                 if (maxx - minx < maxy - miny) {// R
                     result.add(new CircleContour(
                             maxx + guide_rad * 1.5,
                             (miny + maxy) * 0.5,
                             guide_rad, acs.get(0)));
                     result.add(new CircleContour(
                             minx - guide_rad * 1.5,
                             (miny + maxy) * 0.5,
                             guide_rad, acs.get(1)));
                 } else {// T
                     result.add(new CircleContour(
                             (minx + maxx) * 0.5,
                             maxy + guide_rad * 1.5,
                             guide_rad, acs.get(0)));
                     result.add(new CircleContour(
                             (minx + maxx) * 0.5,
                             miny - guide_rad * 1.5,
                             guide_rad, acs.get(1)));
                 }
             } else {
                 if (maxx - minx < maxy - miny) {// R
                     double lowy = (miny + maxy) * 0.5 - 0.5 * acs.size() * guide_rad * 3 + guide_rad * 1.5;
                     for (int i = 0; i < acs.size(); i++) {
                         result.add(new CircleContour(
                                 maxx + guide_rad * 1.5,
                                 lowy + i * 3 * guide_rad,
                                 guide_rad, acs.get(i)));
                     }
                 } else {
                     double lowx = (minx + maxx) * 0.5 - 0.5 * acs.size() * guide_rad * 3 + guide_rad * 1.5;
                     for (int i = 0; i < acs.size(); i++) {
                         result.add(new CircleContour(
                                 lowx + i * 3 * guide_rad,
                                 maxy + guide_rad * 1.5,
                                 guide_rad, acs.get(i)));
                     }
                 }
             }
             return result;
         }
 
         ConcreteZone cz = makeConcreteZone(zone);
         Area a = new Area(cz.getShape(outerBox));
         if (a.isEmpty()) {
             throw new CannotDrawException("cannot put a nested contour into an empty region");
         }
         
         DEB.show(4, a, "area for "+debug_index);
 
         // special case : one contour inside another with no other interference between
         // look at the final diagram - find the corresponding zone
         DEB.out(2, "");
         if (zone.getNumContours() > 0 && acs.size() == 1) {
             //System.out.println("look for "+zone.debug()+" in "+last_diag.debug());
             // not the outside zone - locate the zone in the last diag
             AbstractBasicRegion zoneInLast = null;
             Iterator<AbstractBasicRegion> abrIt = last_diag.getZoneIterator();
             while (abrIt.hasNext() && zoneInLast == null) {
                 AbstractBasicRegion abrInLast = abrIt.next();
                 if (abrInLast.isLabelEquivalent(zone)) {
                     zoneInLast = abrInLast;
                 }
             }
             DEB.assertCondition(zoneInLast != null, "failed to locate zone in final diagram");
 
             // how many neighbouring abrs?
             abrIt = last_diag.getZoneIterator();
             ArrayList<AbstractCurve> nbring_curves = new ArrayList<AbstractCurve>();
             while (abrIt.hasNext()) {
                 AbstractBasicRegion abrInLast = abrIt.next();
                 AbstractCurve ac = zoneInLast.getStraddledContour(abrInLast);
                 if (ac != null) {
                     if (ac.getLabel() != acs.get(0).getLabel()) {
                         nbring_curves.add(ac);
                     }
                 }
             }
             if (nbring_curves.size() == 1) {
                 //  we should use concentric circles
 
                 AbstractCurve acOutside = nbring_curves.get(0);
                 // use the centre of the relevant contour
                 DEB.assertCondition(acOutside != null, "did not find containing contour");
                 CircleContour ccOutside = map.get(acOutside);
                 DEB.assertCondition(ccOutside != null, "did not find containing circle");
                 if (ccOutside != null) {
                     DEB.out(2, "putting contour " + acs.get(0) + " inside " + acOutside.getLabel());
                     double rad = Math.min(guide_rad, ccOutside.radius - smallest_rad);
                     if (rad > 0.99 * smallest_rad) {
                         // build a co-centric contour
                         CircleContour attempt = new CircleContour(
                                 ccOutside.cx, ccOutside.cy, rad, acs.get(0));
                         if (containedIn(attempt, a)) {
                             if (rad > 2 * smallest_rad) // shrink the co-centric contour a bit
                             {
                                 attempt = new CircleContour(
                                         ccOutside.cx, ccOutside.cy, rad - smallest_rad, acs.get(0));
                             }
                             result.add(attempt);
                             return result;
                         }
                     }
                 } else {
                     System.out.println("warning : did not find expected containing circle...");
                 }
             } else if (nbring_curves.size() == 2) {
                 //  we should put a circle along the line between two existing centres
                 AbstractCurve ac1 = nbring_curves.get(0);
                 AbstractCurve ac2 = nbring_curves.get(1);
 
                 CircleContour cc1 = map.get(ac1);
                 CircleContour cc2 = map.get(ac2);
 
                 if (cc1 != null && cc2 != null) {
                     boolean in1 = zone.is_in(ac1);
                     boolean in2 = zone.is_in(ac2);
 
                     double step_c1_c2_x = cc2.cx - cc1.cx;
                     double step_c1_c2_y = cc2.cy - cc1.cy;
 
                     double step_c1_c2_len = Math.sqrt(step_c1_c2_x * step_c1_c2_x
                             + step_c1_c2_y * step_c1_c2_y);
                     double unit_c1_c2_x = 1.0;
                     double unit_c1_c2_y = 0.0;
                     if (step_c1_c2_len != 0.0) {
                         unit_c1_c2_x = step_c1_c2_x / step_c1_c2_len;
                         unit_c1_c2_y = step_c1_c2_y / step_c1_c2_len;
                     }
 
                     double p1x = cc1.cx + unit_c1_c2_x * cc1.radius * (in2 ? 1.0 : -1.0);
                     double p2x = cc2.cx + unit_c1_c2_x * cc2.radius * (in1 ? -1.0 : +1.0);
                     double cx = (p1x + p2x) * 0.5;
                     double max_radx = (p2x - p1x) * 0.5;
                     double p1y = cc1.cy + unit_c1_c2_y * cc1.radius * (in2 ? 1.0 : -1.0);
                     double p2y = cc2.cy + unit_c1_c2_y * cc2.radius * (in1 ? -1.0 : +1.0);
                     double cy = (p1y + p2y) * 0.5;
                     double max_rady = (p2y - p1y) * 0.5;
                     double max_rad = Math.sqrt(max_radx * max_radx + max_rady * max_rady);
 
                     // build a contour
                     CircleContour attempt = new CircleContour(
                             cx, cy, max_rad - smallest_rad, acs.get(0));
                     //DEB.show(3, attempt.getBigInterior());
                     if (containedIn(attempt, a)) {
                         if (max_rad > 3 * smallest_rad) // shrink the co-centric contour a bit
                         {
                             attempt = new CircleContour(
                                     cx, cy, max_rad - 2 * smallest_rad, acs.get(0));
                         } else if (max_rad > 2 * smallest_rad) // shrink the co-centric contour a bit
                         {
                             attempt = new CircleContour(
                                     cx, cy, max_rad - smallest_rad, acs.get(0));
                         }
                         result.add(attempt);
                         return result;
                     }
                 }
             }
         }
 
         // special case - inserting a nested contour into a part of a Venn2
 
 
         Rectangle bounds = a.getBounds();
         /*
         // try from the middle of the bounds.
         double cx = bounds.getCenterX();
         double cy = bounds.getCenterX();
         if(a.contains(cx, cy))
         {
         if(labels.size() == 1)
         {
         // go for a circle of the suggested size
         CircleContour attempt = new CircleContour(cx, cy, guide_rad, labels.get(0));
         if(containedIn(attempt, a))
         {
         result.add(attempt);
         return result;
         }
         }
         else
         {
         Rectangle box = new Rectangle(cx - guide_rad/2)
         }
         }
          */
         if(acs.get(0) == null)
         	DEB.out(2, "putting unlabelled contour inside a zone - grid-style");
         else
         	DEB.out(2, "putting contour " + acs.get(0).getLabel() + " inside a zone - grid-style");
 
         // Use a grid approach to search for a space for the contour(s)
         int ni = (int) (bounds.getWidth() / smallest_rad) + 1;
         int nj = (int) (bounds.getHeight() / smallest_rad) + 1;
         PotentialCentre contained[][] = new PotentialCentre[ni][nj];
         double basex = bounds.getMinX();
         double basey = bounds.getMinY();
         if (DEB.level > 3) {
             System.out.println("--------");
         }
         for (int i = 0; i < ni; i++) {
             double cx = basex + i * smallest_rad;
 
             for (int j = 0; j < nj; j++) {
                 double cy = basey + j * smallest_rad;
                 //System.out.println("check for ("+cx+","+cy+") in region");
                 contained[i][j] = new PotentialCentre(cx, cy, a.contains(cx, cy));
                 if (DEB.level > 3) {
                     if (contained[i][j].ok) {
                         System.out.print("o");
                     } else {
                         System.out.print("x");
                     }
                 }
             }
             if (DEB.level > 3) {
                 System.out.println("");
             }
         }
         if (DEB.level > 3) {
             System.out.println("--------");
         }
         // look in contained[] for a large square
 
         int corneri = -1, cornerj = -1, size = -1;
         boolean isTall = true; // or isWide
         for (int i = 0; i < ni; i++) {
             for (int j = 0; j < nj; j++) {
                 // biggest possible square?
                 int max_sq = Math.min(ni - i, nj - j);
                 for (int sq = size + 1; sq < max_sq + 1; sq++) {
                     // scan a square from i, j
                     DEB.out(2, "look for a box from (" + i + "," + j + ") size " + sq);
 
                     if (all_ok_in(i, i + (sq * acs.size()) + 1, j, j + sq + 1, contained, ni, nj)) {
                         DEB.out(2, "found a wide box, corner at (" + i + "," + j + "), size " + sq);
                         corneri = i;
                         cornerj = j;
                         size = sq;
                         isTall = false;
                     } else if (acs.size() > 1
                             && all_ok_in(i, i + sq + 1, j, j + (sq * acs.size()) + 1, contained, ni, nj)) {
                         DEB.out(2, "found a tall box, corner at (" + i + "," + j + "), size " + sq);
                         corneri = i;
                         cornerj = j;
                         size = sq;
                         isTall = true;
                     } else {
                         break; // neither wide nor tall worked - move onto next (x, y)
                     }
                 }// loop for increasing sizes
             }// loop for j corner
         }// loop for i corner
         //System.out.println("best square is at corner ("+corneri+","+cornerj+"), of size "+size);
         if (size > 0) {
             PotentialCentre pc = contained[corneri][cornerj];
             double radius = size * smallest_rad * 0.5;
             double actualRad = radius;
             if (actualRad > 2 * smallest_rad) {
                 actualRad -= smallest_rad;
             } else if (actualRad > smallest_rad) {
                 actualRad = smallest_rad;
             }
 
             // have size, cx, cy
             DEB.out(2, "corner at " + pc.x + "," + pc.y + ", size " + size);
 
             ArrayList<CircleContour> centredCircles = new ArrayList<CircleContour>();
 
             double bx = bounds.getCenterX();
             double by = bounds.getCenterY();
             if (isTall) {
                 by -= radius * (acs.size() - 1);
             } else {
                 bx -= radius * (acs.size() - 1);
             }
             for (int labelIndex = 0;
                     centredCircles != null && labelIndex < acs.size();
                     labelIndex++) {
                 AbstractCurve ac = acs.get(labelIndex);
                 double x = bx;
                 double y = by;
                 if (isTall) {
                     y += 2 * radius * labelIndex;
                 } else {
                     x += 2 * radius * labelIndex;
                 }
 
                 CircleContour attempt = new CircleContour(x, y,
                         Math.min(guide_rad, actualRad), ac);
                 //DEB.show(3, attempt.getBigInterior());
                 if (containedIn(attempt, a)) {
                     centredCircles.add(attempt);
                 } else {
                     centredCircles = null;
                     //Debug.show(a);
                 }
             }
             if (centredCircles != null) {
                 result.addAll(centredCircles);
                 return result;
             }
 
             for (int labelIndex = 0; labelIndex < acs.size(); labelIndex++) {
                 AbstractCurve ac = acs.get(labelIndex);
                 double x = pc.x + radius;
                 double y = pc.y + radius;
                 if (isTall) {
                     y += 2 * radius * labelIndex;
                 } else {
                     x += 2 * radius * labelIndex;
                 }
 
                 CircleContour attempt = new CircleContour(x, y,
                         Math.min(guide_rad, actualRad + smallest_rad), ac);
                 if (containedIn(attempt, a)) {
                     result.add(attempt);
                 } else {
                     result.add(new CircleContour(x, y, actualRad, ac));
                 }
             }
             return result;
         } else {
             throw new CannotDrawException("cannot fit nested contour into region");
         }
     }
 
     private double[][] intersect(double c1x, double c1y, double rad1,
             double c2x, double c2y, double rad2) {
 
         double ret[][] = new double[2][2];
         double dx = c1x - c2x;
         double dy = c1y - c2y;
         double d2 = dx * dx + dy * dy;
         double d = Math.sqrt(d2);
 
         if (d > rad1 + rad2 || d < Math.abs(rad1 - rad2)) {
             return null; // no solution
         }
 
         double a = (rad1 * rad1 - rad2 * rad2 + d2) / (2 * d);
         double h = Math.sqrt(rad1 * rad1 - a * a);
         double x2 = c1x + a * (c2x - c1x) / d;
         double y2 = c1y + a * (c2y - c1y) / d;
 
 
         double paX = x2 + h * (c2y - c1y) / d;
         double paY = y2 - h * (c2x - c1x) / d;
         double pbX = x2 - h * (c2y - c1y) / d;
         double pbY = y2 + h * (c2x - c1x) / d;
 
         ret[0][0] = paX;
         ret[0][1] = paY;
         ret[1][0] = pbX;
         ret[1][1] = pbY;
 
         return ret;
     }
 
     private boolean containedIn(CircleContour c, Area a) {
         Area test = new Area(c.getFatInterior(smallest_rad));
         test.subtract(a);
         return test.isEmpty();
     }
     private void DEB_show_frame(int deb_level,// only show if deb_level >= global debug level
     		int debug_frame_index,
     		int size)
     {
 		if(deb_level < DEB.level)
 			return;
 		
 		// build a ConcreteDiagram for the current collection of circles
 		ArrayList<ConcreteZone> shadedZones = new ArrayList<ConcreteZone>();
 		ArrayList<ConcreteSpider> spiders = new ArrayList<ConcreteSpider>();
 		
 		ArrayList<CircleContour> circles_copy = new ArrayList<CircleContour>();
 		for(CircleContour c : circles)
 		{
 			circles_copy.add(new CircleContour(c));
 		}
         CircleContour.fitCirclesToSize(circles_copy, size);
 		ConcreteDiagram cd = new ConcreteDiagram(new Rectangle2D.Double(0, 0, size, size),
 	            circles_copy, shadedZones, spiders );
 	    CirclesPanel cp = new CirclesPanel("debug frame "+debug_frame_index, "no failure",
 	    		cd, size, true);
 	    DEB.addFilmStripShot(cp);
     }
 }
 
 class PotentialCentre {
 
     double x;
     double y;
     boolean ok;
 
     PotentialCentre(double x, double y, boolean ok) {
         this.x = x;
         this.y = y;
         this.ok = ok;
     }
 }

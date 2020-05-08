 package generalLWRNetwork;
 
 import generalNetwork.data.demand.Demands;
 import generalNetwork.state.CellInfo;
 import generalNetwork.state.Profile;
 import generalNetwork.state.externalSplitRatios.IntertemporalOriginsSplitRatios;
 import generalNetwork.state.internalSplitRatios.IntertemporalSplitRatios;
 import generalNetwork.state.internalSplitRatios.JunctionSplitRatios;
 
 import java.util.LinkedHashMap;
 import java.util.ListIterator;
 
 public class LWR_network {
 
   private Cell[] cells;
   private Junction[] junctions;
   private Origin[] sources;
   private Destination[] sinks;
 
   private IntertemporalSplitRatios internal_split_ratios;
   private int nb_compliant_commodities;
 
   /**
    * @brief Take a DiscretizedGraph and create the LWR_network compact
    *        representation of it.
    * @details It does not create any cells but uses the ones in the graph
    */
   public LWR_network(DiscretizedGraph g) {
     int total_nb_junctions = g.total_nb_junctions;
 
     if (g.junctions.length + g.new_junctions.size() != total_nb_junctions) {
       System.out.println("[LWR_network]Different number of junctions: "
           + (g.junctions.length + g.new_junctions.size())
           + " vs " + total_nb_junctions);
       System.exit(1);
     }
 
     int total_nb_cells = g.total_nb_cells;
     if (total_nb_cells != g.new_cells.size()) {
       System.out.println("Different number of cells: " + total_nb_cells
           + " vs "
           + g.new_cells.size());
       System.exit(1);
     }
 
     /* We register all the cells */
     cells = new Cell[total_nb_cells];
     ListIterator<Cell> iterator = g.new_cells.listIterator();
     Cell tmp;
     while (iterator.hasNext()) {
       tmp = iterator.next();
       cells[tmp.getUniqueId()] = tmp;
     }
 
     /* We register all the junctions */
     junctions = new Junction[total_nb_junctions];
     ListIterator<Junction> iterator2 = g.new_junctions.listIterator();
     Junction tmp2;
     while (iterator2.hasNext()) {
       tmp2 = iterator2.next();
       junctions[tmp2.getUniqueId()] = tmp2;
     }
     for (int i = 0; i < g.junctions.length; i++) {
       junctions[g.junctions[i].getUniqueId()] = g.junctions[i];
     }
 
     /* We register all the origins */
     sources = g.sources.clone();
     /* We register all the destinations */
     sinks = g.destinations.clone();
 
     /* We register all the internal_split_ratios */
     internal_split_ratios = g.split_ratios;
 
     nb_compliant_commodities = g.nb_paths;
 
     check();
   }
 
   private void check() {
     /* We check that all registered cells and junctions are not null */
     for (int i = 0; i < cells.length; i++) {
       assert cells[i] != null : "Null cell found !";
     }
     /* We also check that all junctions have an outgoing cell */
     for (int i = 0; i < junctions.length; i++) {
       assert junctions[i] != null : "Null junction found !";
       assert junctions[i].getNext() != null;
       assert junctions[i].getNext().length > 0 : "A junction has no outgoing cell !";
     }
 
     /* We check that the internal_split_ratios is defined */
     assert (internal_split_ratios != null);
 
     assert sources != null;
     assert sinks != null;
   }
 
   public void print() {
     for (int c = 0; c < cells.length; c++)
       cells[c].print();
     for (int j = 0; j < junctions.length; j++)
       junctions[j].print();
   }
 
   public void printInternalSplitRatios() {
     System.out.println("Printing the internal split ratios:");
     System.out.println(internal_split_ratios.toString());
   }
 
   public int getNb_Cells() {
     return cells.length;
   }
 
   public int getNb_Junctions() {
     return junctions.length;
   }
 
   public void addCell(Cell c) {
     cells[c.getUniqueId()] = c;
   }
 
   public Cell getCell(int i) {
     return cells[i];
   }
 
   public Origin[] getSources() {
     return sources;
   }
 
   public Destination[] getSinks() {
     return sinks;
   }
 
   public Destination getSink(int path) {
     return sinks[path];
   }
 
   public IntertemporalSplitRatios getInternal_split_ratios() {
     return internal_split_ratios;
   }
 
   public void addJunction(Junction j) {
     junctions[j.getUniqueId()] = j;
   }
 
   public Junction getJunction(int i) {
     return junctions[i];
   }
 
   public Cell[] getCells() {
     return cells;
   }
 
   public Junction[] getJunctions() {
     return junctions;
   }
 
   private int getNumber_paths() {
     int result = 0;
     for (int i = 0; i < sources.length; i++) {
       result += sources[i].size();
     }
     return result;
   }
 
   public int getNb_compliantCommodities() {
     return nb_compliant_commodities;
   }
 
   public void checkConstraints(double delta_t) {
     for (int c = 0; c < cells.length; c++)
       cells[c].checkConstraints(delta_t);
 
     // for (int j = 0; j < junctions.length; j++)
     // junctions[j].checkConstraints();
   }
 
   /**
    * @details - Add the demand in the buffer of the previous profile to get
    *          the buffer of the profile p
    *          - Computes the demand and supply of profile p
    *          - Computes the in- and out-flows for profile p
    *          - Create a new profile containing the new densities
    * @param previous_profile
    *          This is NOT modified
    * @param p
    *          Only the buffers of this profile are modified
    * @param demands
    *          demands[i] is the demand {(commodity, value)} we have to put
    *          in entries[i]
    * 
    */
   public Profile simulateProfileFrom(Profile previous_profile, Profile p,
       double delta_t, Demands origin_demand,
       IntertemporalOriginsSplitRatios splits,
       int time_step) {
 
     assert p.CellInfoSize() == cells.length : "The profile size must correspond to the size of the network";
     assert origin_demand.size() == sources.length : " The demands should correspond to the number of entries";
 
     Profile next_profile = new Profile(cells.length, junctions.length);
 
     /* We inject the demand in the buffers of the profile p */
     for (int b = 0; b < sources.length; b++) {
       sources[b].injectDemand(previous_profile,
           p,
           origin_demand.get(sources[b], time_step),
           splits.get(sources[b], time_step),
           delta_t);
     }
 
     /* Computation of the demand and supply */
     double density, demand, supply;
     for (int cell_id = 0; cell_id < cells.length; cell_id++) {
       density = p.getCell(cell_id).total_density;
 
       /*
        * The demand and the supply depend on the network and the density
        */
       demand = getCell(cell_id).getDemand(density, delta_t);
       supply = getCell(cell_id).getSupply(density);
      assert demand >= 0 : "Demand should be positive (" + demand + ")";
      assert supply >= 0 : "Supply should be positive (" + supply + ")*";
 
       p.getCell(cell_id).demand = demand;
       p.getCell(cell_id).supply = supply;
 
       // We clear the old flows
       p.getCell(cell_id).clearFlow();
     }
 
     /*
      * Computation of the flows. The flows should have been cleared BEFORE doing
      * this operation
      */
     for (int j_id = 0; j_id < junctions.length; j_id++) {
       /* The JunctionInfo is created in the solveJunction */
       /* We get the split-ratios for this junction at this time-step */
       JunctionSplitRatios junction_sr =
           internal_split_ratios.get(time_step, j_id);
       junctions[j_id].solveJunction(p, time_step, junction_sr, cells);
     }
 
     /* Creation of the new profile with the new densities */
     LinkedHashMap<Integer, Double> new_densities, densities, in_flows, out_flows;
     CellInfo cell_info;
     for (int cell_id = 0; cell_id < cells.length; cell_id++) {
       cell_info = p.getCell(cell_id);
       densities = cell_info.partial_densities;
 
       in_flows = cell_info.in_flows;
       out_flows = cell_info.out_flows;
 
       new_densities = getCell(cell_id).getUpdatedDensity(densities,
           in_flows, out_flows, delta_t);
 
       next_profile.putCell(cell_id, new CellInfo(new_densities));
     }
 
     return next_profile;
   }
 
   public Profile emptyProfile() {
     Profile initial_profile = new Profile(cells.length, junctions.length);
     for (int cell_id = 0; cell_id < cells.length; cell_id++) {
       initial_profile.putCell(cell_id, new CellInfo());
     }
     return initial_profile;
   }
 
   // This is false now
   /*
    * private double[] initialSplitRatios() {
    * double[] res = new double[sources.length];
    * for (int i = 0; i < res.length; i++) {
    * res[i] = 1.0 / (double) res.length;
    * }
    * return res;
    * }
    */
 }

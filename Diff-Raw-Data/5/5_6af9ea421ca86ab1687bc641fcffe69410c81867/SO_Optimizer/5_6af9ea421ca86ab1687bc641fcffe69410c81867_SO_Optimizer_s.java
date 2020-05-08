 package dta_solver;
 
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import generalLWRNetwork.Cell;
 import generalLWRNetwork.Destination;
 import generalLWRNetwork.Junction;
 import generalLWRNetwork.Origin;
 import generalNetwork.state.CellInfo;
 import generalNetwork.state.JunctionInfo;
 import generalNetwork.state.Profile;
 import generalNetwork.state.State;
 import generalNetwork.state.externalSplitRatios.IntertemporalOriginsSplitRatios;
 import generalNetwork.state.internalSplitRatios.IntertemporalJunctionSplitRatios;
 import generalNetwork.state.internalSplitRatios.IntertemporalSplitRatios;
 import generalNetwork.state.internalSplitRatios.JunctionSplitRatios;
 
 import org.apache.commons.math3.optimization.DifferentiableMultivariateOptimizer;
 import org.wsj.AdjointForJava;
 
import com.sun.org.apache.xml.internal.security.encryption.AgreementMethod;

 import scala.Option;
 import scala.Some;
 
 import cern.colt.matrix.tdouble.DoubleMatrix1D;
 import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
 import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
 import cern.colt.matrix.tdouble.DoubleFactory1D;
 import dataStructures.Numerical;
 import dataStructures.Triplet;
 
 public class SO_Optimizer extends AdjointForJava<State> {
 
   private Simulator simulation;
 
   private double epsilon = 0.02;
 
   /* Total number of time steps */
   private int T;
   /* Number of compliant commodities */
   private int C;
   private Cell[] cells;
   private Junction[] junctions;
   private Origin[] sources;
   private Destination[] destinations;
   /* Number of origins */
   private int O;
   /* Number of destinations */
   private int S;
 
   /* Control Vector U */
   /* Total size of a block for one time step of the control */
   private int temporal_control_block_size;
 
   /* State Vector X */
   /* Size of a block describing all the densities for a given time step */
   private int size_density_block;
   /* Size of a block describing all the supply/demand at one time step */
   private int size_demand_suply_block;
   /* Size of the block describing all the Aggregate SR at one time sate */
   private int size_aggregate_split_ratios;
   /* Size of a block describing out-flows */
   private int size_f_out_block;
   /* Total size of the description of a profile for a given time step */
   private int x_block_size;
 
   private int f_out_position;
   private int f_in_position;
 
   /* Constraints Vector H */
   /* Size of a block describing the Mass Conversation constraints */
   private int mass_conservation_size;
   /* Size of a block describing the Flow Propagation constraints */
   private int flow_propagation_size;
   /* Size of the block describing the Aggregate SR constraints */
   // size_aggregate_split_ratios;
   /* Size of the block describing the out-flows constraints : */
   // mass_conservation_size
   /* Size of the block describing the in-flows constraints : */
   // mass_conservation_size
   /* Total size of a block of constraints for a given time step */
   private int H_block_size;
 
   public SO_Optimizer(DifferentiableMultivariateOptimizer op, int maxIter,
       Simulator simu) {
     super(op, maxIter);
     simulation = simu;
 
     T = simulation.time_discretization.getNb_steps();
     C = simulation.lwr_network.getNb_compliantCommodities();
     cells = simulation.lwr_network.getCells();
 
     sources = simulation.lwr_network.getSources();
     O = sources.length;
     destinations = simulation.lwr_network.getSinks();
     S = destinations.length;
 
     junctions = simulation.lwr_network.getJunctions();
 
     /* For every time steps there are C compliant flows, and O non compliant */
     temporal_control_block_size = (C + O);
 
     /* State Vector X */
     /* Size of a block describing all the densities for a given time step */
     size_density_block = cells.length * (C + 1);
     /* Size of a block describing all the supply/demand at one time step */
     size_demand_suply_block = 2 * cells.length;
     /* Size of the block describing all the Aggregate SR at one time sate */
     Junction junction;
     int tmp_nb_aggregate_split_ratios = 0;
     for (int j = 0; j < junctions.length; j++) {
       junction = junctions[j];
       tmp_nb_aggregate_split_ratios +=
           junction.getPrev().length * junction.getNext().length;
     }
     size_aggregate_split_ratios = tmp_nb_aggregate_split_ratios;
     /* Size of a block describing out-flows or in-flows */
     size_f_out_block = size_density_block;
     /* Total size of the description of a profile for a given time step */
     x_block_size = (3 * (C + 1) + 2) * cells.length
         + size_aggregate_split_ratios;
 
     f_out_position = size_density_block + size_demand_suply_block
         + size_aggregate_split_ratios;
     f_in_position = f_out_position + size_f_out_block;
 
     /* Constraints Vector H */
     /* Size of a block describing the Mass Conversation constraints */
     mass_conservation_size = size_density_block;
     /* Size of a block describing the Flow Propagation constraints */
     flow_propagation_size = size_demand_suply_block;
     /* Size of the block describing the Aggregate SR constraints */
     // size_aggregate_split_ratios;
     /* Size of the block describing the out-flows constraints : */
     // mass_conservation_size
     /* Size of the block describing the in-flows constraints : */
     // mass_conservation_size
     /* Total size of a block of constraints for a given time step */
     H_block_size = 3 * mass_conservation_size + flow_propagation_size
         + size_aggregate_split_ratios;
   }
 
   /**
    * @brief Return the 1x(C+O) matrix representing the control where
    *        C is the number of compliant commodities and O the number of origins
    * @details There are T blocks of size (C+O). The i-th block contains the
    *          control at time step i.
    */
   public double[] getControl() {
 
     IntertemporalOriginsSplitRatios splits = simulation.splits;
 
     /*
      * For every time steps there are C compliant flows, and O non compliant
      * There is also the sum of the split ratios for every origins
      */
     double[] control = new double[T * temporal_control_block_size];
 
     int index_in_control = 0;
     int commodity;
     Double split_ratio;
     for (int orig = 0; orig < O; orig++) {
       double[] sum_of_split_ratios = new double[T];
       for (int k = 0; k < T; k++) {
         /*
          * Mapping between
          * splits.get(sources[orig], k).get(0)
          * and U[k*(C + sources.length + index_in_control)]
          */
         split_ratio = splits.get(sources[orig], k).get(0);
         if (split_ratio != null) {
           control[k * temporal_control_block_size + index_in_control] = split_ratio;
           sum_of_split_ratios[k] += split_ratio;
         }
       }
       index_in_control++;
 
       Iterator<Integer> it = sources[orig]
           .getCompliant_commodities()
           .iterator();
       while (it.hasNext()) {
         commodity = it.next();
         for (int k = 0; k < T; k++) {
           /*
            * Mapping between
            * splits.get(sources[orig], k).get(commodity) and
            * U[k*(C +sources.length) + index_in_control]
            */
           split_ratio = splits.get(sources[orig], k).get(commodity);
           if (split_ratio != null) {
             control[k * temporal_control_block_size + index_in_control] = split_ratio;
             sum_of_split_ratios[k] += split_ratio;
           }
         }
         index_in_control++;
 
       }
       /* At the end we add the sum of the split ratios at that origin */
       for (int k = 0; k < T; k++)
         sources[orig].sum_split_ratios = sum_of_split_ratios;
     }
 
     return control;
   }
 
   private void parseStateVector(Profile p) {
 
     int block_id, sub_block_id;
     int commodity;
     int index_in_state = 0;
     double value;
     CellInfo cell_info;
     for (int k = 0; k < T; k++) {
       /* Id of the first data of time step k */
       block_id = k * x_block_size;
 
       for (int cell_id = 0; cell_id < cells.length; cell_id++) {
 
         cell_info = p.getCell(cells[cell_id]);
         /* Id of the first index containing data from cells[cell_id] */
         sub_block_id = block_id + cell_id * C;
 
         // Operations on densities
         Iterator<Entry<Integer, Double>> it =
             cell_info.partial_densities.entrySet().iterator();
         Entry<Integer, Double> entry;
         while (it.hasNext()) {
           entry = it.next();
           commodity = entry.getKey();
           // density (cell_id, commodity)(k)
           index_in_state = sub_block_id + commodity;
           value = entry.getValue();
         }
 
         // Operations on demand and supply
         index_in_state = sub_block_id + size_density_block;
         value = cell_info.demand;
         index_in_state++;
         value = cell_info.supply;
 
         // Operations on aggregate split ratios
         index_in_state += size_demand_suply_block;
         JunctionInfo junction_info;
         Junction junction;
         for (int j = 0; j < junctions.length; j++) {
           junction = junctions[j];
           junction_info = p.getJunction(j);
           for (int in = 0; in < junction.getPrev().length; in++) {
             for (int out = 0; out < junction.getNext().length; out++) {
               // Mapping between junctions_info.get(new PairCell(in, out));
               index_in_state++;
             }
           }
         }
 
         // Operation on out-flows
         sub_block_id += size_aggregate_split_ratios;
         it = cell_info.out_flows.entrySet().iterator();
         while (it.hasNext()) {
           entry = it.next();
           commodity = entry.getKey();
           // flow_out (cell_id, commodity)(k)
           index_in_state = sub_block_id + commodity;
           value = entry.getValue();
         }
 
         // Operations on in-flows
         index_in_state += size_f_out_block;
         it = cell_info.in_flows.entrySet().iterator();
         while (it.hasNext()) {
           entry = it.next();
           commodity = entry.getKey();
           // flow_in (cell_id, commodity)(k)
           index_in_state = sub_block_id + commodity;
           value = entry.getValue();
         }
       }
     }
   }
 
   @Override
   public Option<SparseCCDoubleMatrix2D> dhdu(State state, double[] control) {
 
     SparseCCDoubleMatrix2D result = new SparseCCDoubleMatrix2D(
         H_block_size * T,
         temporal_control_block_size * T);
 
     int size, i, j, index_in_control = 0;
     double[] origin_demands;
     for (int orig = 0; orig < O; orig++) {
       size = sources[orig].getCompliant_commodities().size();
       origin_demands = simulation.origin_demands.get(sources[orig]);
       for (int c = 0; c < size + 1; c++) {
         for (int k = 0; k < T; k++) {
           i = k * H_block_size + sources[orig].getUniqueId();
           j = k * temporal_control_block_size + index_in_control;
 
           result.setQuick(i, j, origin_demands[k]);
         }
         index_in_control++;
       }
     }
 
     return new Some<SparseCCDoubleMatrix2D>(result);
   }
 
   @Override
   public Option<SparseCCDoubleMatrix2D> dhdx(State state, double[] control) {
 
     IntertemporalSplitRatios internal_SR =
         simulation.lwr_network.getInternal_split_ratios();
 
     SparseCCDoubleMatrix2D result = new SparseCCDoubleMatrix2D(
         H_block_size * T,
         x_block_size * T);
 
     /*********************************************************
      * Derivative terms for the Mass Conservation constraints
      *********************************************************/
 
     /*
      * Derivative of the initial conditions : \rho(i,c)(0) - \rho(i,c,0) = 0
      * The derivative is always 1
      */
     for (int partial_density = 0; partial_density < mass_conservation_size; partial_density++) {
       result.setQuick(partial_density,
           partial_density,
           1.0);
     }
 
     // Position of a block of constraints in H indexed by k
     int block_upper_position;
     // Position of a block in the Mass Conservation block indexed by the cell_id
     int sub_block_position;
     double delta_t_over_l;
     int i, j;
     for (int k = 1; k < T; k++) {
       block_upper_position = k * H_block_size;
       for (int cell_id = 0; cell_id < cells.length; cell_id++) {
         sub_block_position = cell_id * (C + 1);
         for (int c = 0; c < C + 1; c++) {
 
           // Line of interest in the H matrix
           i = block_upper_position + sub_block_position + c;
           // Column of interest in the H matrix
           j = x_block_size * (k - 1) + sub_block_position + c;
 
           /*
            * We put 1 for the derivative terms of the mass conversation
            * equations (i,c,k) with respect to \beta(i,c,k) for k \in [1, T]
            */
          result.setQuick(i, j, 1.0);
 
           /*
            * Derivative terms with respect to flow-out(i,c,k-1) and
            * flow-in(i,c,k-1)
            */
           delta_t_over_l = simulation.time_discretization.getDelta_t() /
               simulation.lwr_network.getCell(cell_id).getLength();
 
           assert Numerical.validNumber(delta_t_over_l);
 
           // d \density(i, k) / d f_out(i,k-1) = - delta_t / l
           result.setQuick(i, j + f_out_position, -delta_t_over_l);
 
           // d \density(i, k) / d f_in(i,k-1) = delta_t / l
           result.setQuick(i, j + f_in_position, delta_t_over_l);
         }
       }
       // For the buffers and sinks we have put a derivative term that should
       // have been zero
       for (int o = 0; o < O; o++) {
         for (int c = 0; c < C + 1; c++) {
           i = block_upper_position + sources[o].getUniqueId() * (C + 1) + c;
           j = x_block_size * (k - 1) + sources[o].getUniqueId() * (C + 1) + c;
           // flow-in
           result.setQuick(i, j + f_in_position, 0.0);
         }
       }
       for (int s = 0; s < S; s++) {
         for (int c = 0; c < C + 1; c++) {
           i = block_upper_position + destinations[s].getUniqueId() * (C + 1)
               + c;
           j = x_block_size * (k - 1) + destinations[s].getUniqueId() * (C + 1)
               + c;
           // flow-in
           result.setQuick(i, j + f_out_position, 0.0);
         }
       }
     }
 
     /*********************************************************
      * Derivative terms for the Flow propagation
      *********************************************************/
     double total_density;
     for (int k = 0; k < T; k++) {
       // Position of the first constraint in H dealing with supply/demand at
       // time step k
       block_upper_position = k * H_block_size + mass_conservation_size;
       for (int cell_id = 0; cell_id < cells.length; cell_id++) {
         sub_block_position = block_upper_position + cell_id * 2;
         total_density = state.profiles[k].getCell(cell_id).total_density;
 
         for (int c = 0; c < C + 1; c++) {
           // Demand first
           result.setQuick(sub_block_position,
               x_block_size * k + cell_id * (C + 1) + c,
               cells[cell_id].getDerivativeDemand(total_density));
 
           // Then supply
           result.setQuick(sub_block_position + 1,
               x_block_size * k + cell_id * (C + 1) + c,
               cells[cell_id].getDerivativeSupply(total_density));
         }
       }
     }
 
     /*********************************************************
      * Derivative terms for the Aggregate Split Ratios
      *********************************************************/
     /*
      * This part is not effective because we have to do
      * Nb_Aggregate_SR * T * (C+1) computation of derivative terms
      */
     Junction junction;
     int aggregate_SR_index = 0;
     Double partial_density;
     Double i_j_c_SR;
     JunctionSplitRatios junction_SR;
     CellInfo in_cell_info;
     int prev_length, next_length;
 
     for (int j_id = 0; j_id < junctions.length; j_id++) {
       junction = junctions[j_id];
       IntertemporalJunctionSplitRatios intert_junction_SR =
           internal_SR.get(j_id);
 
       prev_length = junction.getPrev().length;
       for (int in = 0; in < prev_length; in++) {
 
         next_length = junction.getNext().length;
         for (int out = 0; out < next_length; out++) {
           for (int k = 0; k < T; k++) {
             block_upper_position = k * H_block_size
                 + mass_conservation_size + flow_propagation_size;
 
             /* There is no intertemporal split ratios for Nx1 junctions */
 
             if (intert_junction_SR == null)
               junction_SR = null;
             else {
               junction_SR = intert_junction_SR.get(k);
               assert junction_SR != null;
             }
             in_cell_info = state.profiles[k].getCell(in);
 
             i = block_upper_position + aggregate_SR_index;
             j = k * x_block_size + (C + 1) * in;
 
             for (int c = 0; c < C + 1; c++) {
               /*
                * If there is no intertemporal_split ratios this means we are at
                * a Nx1
                * junction and we always have beta = 1
                */
               if (junction_SR == null)
                 i_j_c_SR = 1.0;
               else
                 i_j_c_SR = junction_SR.get(in, out, c);
 
               /*
                * If the split ratio for this commodity is zero, then the
                * aggregate split ratio is independent of this split ratio
                */
               if (i_j_c_SR == null || i_j_c_SR == 0)
                 continue;
 
               partial_density = in_cell_info.partial_densities.get(c);
               if (partial_density == null)
                 partial_density = 0.0;
               total_density = in_cell_info.total_density;
 
               if (total_density != 0) {
                 double derivative_term = i_j_c_SR
                     * (total_density - partial_density)
                     / (total_density * total_density);
                 assert Numerical.validNumber(derivative_term);
                 result.setQuick(i, j, derivative_term);
               }
 
             }
             aggregate_SR_index++;
           }
         }
 
       }
     }
 
     /*********************************************************
      * Derivative terms for the out-flows
      *********************************************************/
 
     int nb_prev, nb_next;
     double value;
     for (int j_id = 0; j_id < junctions.length; j_id++) {
       junction = junctions[j_id];
       nb_prev = junction.getPrev().length;
       nb_next = junction.getNext().length;
 
       // Derivative terms for 1x1 junctions
       if (nb_prev == 1 && nb_next == 1) {
         double demand, supply, f_out;
         CellInfo cell_info;
         int prev_id = junction.getPrev()[0].getUniqueId();
         int next_id = junction.getPrev()[0].getUniqueId();
         for (int k = 0; k < T; k++) {
           cell_info = state.profiles[k].getCell(prev_id);
           total_density = cell_info.total_density;
 
           if (total_density == 0)
             continue;
 
           demand = cell_info.demand;
           supply = state.profiles[k].getCell(next_id).supply;
 
           f_out = Math.min(demand, supply);
           /* Derivative terms with respect to the partial densities */
           for (int c = 0; c < C + 1; c++) {
             partial_density = cell_info.partial_densities.get(c);
             if (partial_density == null)
               partial_density = 0.0;
 
             i = H_block_size * k + mass_conservation_size
                 + flow_propagation_size + size_aggregate_split_ratios + prev_id
                 * (C + 1) + c;
             j = x_block_size * k + prev_id * (C + 1) + c;
             value = f_out * (total_density - partial_density)
                 / (total_density * total_density);
             assert Numerical.validNumber(value);
             result.setQuick(i, j, value);
 
             /* Derivative terms with respect to supply/demand */
             if (partial_density == 0)
               continue;
 
             value = partial_density / total_density;
             assert Numerical.validNumber(value);
             if (demand < supply) {
               j = x_block_size * k + size_density_block + 2 * prev_id;
               result.setQuick(i, j, value);
             } else if (supply < demand) {
               value = partial_density / total_density;
               j = x_block_size * k + size_density_block + 2 * prev_id + 1;
               result.setQuick(i, j, value);
             }
           }
         }
         // Derivative terms for 1x2 junctions
       } else if (nb_prev == 2 && nb_next == 1) {
         int in_1 = junction.getPrev()[0].getUniqueId();
         int in_2 = junction.getPrev()[1].getUniqueId();
         int out = junction.getNext()[0].getUniqueId();
         double P1 = junction.getPriority(in_1);
         double P2 = junction.getPriority(in_2);
 
         double demand1, demand2, supply, f_in;
         for (int k = 0; k < T; k++) {
           demand1 = state.profiles[k].getCell(in_1).demand;
           demand2 = state.profiles[k].getCell(in_2).demand;
           supply = state.profiles[k].getCell(out).supply;
           f_in = Math.min(demand1 + demand2, supply);
 
           double total_density1 = state.profiles[k].getCell(in_1).total_density;
           double total_density2 = state.profiles[k].getCell(in_2).total_density;
 
           for (int c = 0; c < C + 1; c++) {
 
             double Df_inDdemand1 = 0, Df_inDdemand2 = 0, Df_inDsupply = 0;
             if (demand1 + demand2 < supply) {
               Df_inDdemand1 = 1;
               Df_inDdemand2 = 1;
             }
             if (supply < demand1 + demand2) {
               Df_inDsupply = 1;
             }
 
             /* For the first incoming road in_1 */
             /* Derivative terms with respect to the partial densities */
             partial_density = state.profiles[k].getCell(in_1).partial_densities
                 .get(c);
             if (partial_density == null)
               partial_density = 0.0;
 
             if (total_density1 != 0) {
 
               double f_in_1_out;
               double DfDdemand1 = 0, DfDdemand2 = 0, DfDsupply = 0;
               if (P1 * (f_in - demand1) > P2 * demand1) {
                 f_in_1_out = demand1;
                 DfDdemand1 = 1;
               } else if (P1 * demand2 < P2 * (f_in - demand2)) {
                 f_in_1_out = f_in - demand2;
                 DfDdemand1 = Df_inDdemand1;
                 DfDdemand2 = Df_inDdemand2 - 1;
                 DfDsupply = Df_inDsupply;
               } else {
                 f_in_1_out = P1 * f_in;
                 DfDdemand1 = P1 * Df_inDdemand1;
                 DfDdemand2 = P1 * Df_inDdemand2;
                 DfDsupply = P1 * Df_inDsupply;
               }
 
               i = H_block_size * k + mass_conservation_size
                   + flow_propagation_size + size_aggregate_split_ratios + in_1
                   * (C + 1) + c;
               j = x_block_size * k + in_1 * (C + 1) + c;
 
               value = f_in_1_out * (total_density1 - partial_density)
                   / (total_density1 * total_density1);
               result.setQuick(i, j, value);
 
               /* Derivative terms with respect to supply/demand */
               if (partial_density != 0) {
                 if (DfDdemand1 != 0) {
                   j = x_block_size * k + size_demand_suply_block + in_1 * 2;
                   assert Numerical.validNumber(DfDdemand1);
                   result.setQuick(i, j, DfDdemand1);
                 }
                 if (DfDdemand2 != 0) {
                   j = x_block_size * k + size_demand_suply_block + in_2 * 2;
                   assert Numerical.validNumber(DfDdemand2);
                   result.setQuick(i, j, DfDdemand2);
                 }
                 if (DfDsupply != 0) {
                   j = x_block_size * k + size_demand_suply_block + out * 2 + 1;
                   assert Numerical.validNumber(DfDsupply);
                   result.setQuick(i, j, DfDsupply);
                 }
               }
             }
 
             /* For the second incoming road in_2 */
             /* Derivative terms with respect to the partial densities */
             partial_density = state.profiles[k].getCell(in_2).partial_densities
                 .get(c);
             if (partial_density == null)
               partial_density = 0.0;
 
             if (total_density2 != 0) {
 
               double f_in_2_out;
               double DfDdemand1 = 0, DfDdemand2 = 0, DfDsupply = 0;
               if (P2 * (f_in - demand2) > P1 * demand2) {
                 f_in_2_out = demand1;
                 DfDdemand2 = 1;
               } else if (P2 * demand2 < P1 * (f_in - demand1)) {
                 f_in_2_out = f_in - demand1;
                 DfDdemand2 = Df_inDdemand2;
                 DfDdemand1 = Df_inDdemand1 - 1;
                 DfDsupply = Df_inDsupply;
               } else {
                 f_in_2_out = P2 * f_in;
                 DfDdemand2 = P2 * Df_inDdemand2;
                 DfDdemand1 = P2 * Df_inDdemand1;
                 DfDsupply = P2 * Df_inDsupply;
               }
 
               i = H_block_size * k + mass_conservation_size
                   + flow_propagation_size + size_aggregate_split_ratios + in_2
                   * (C + 1) + c;
               j = x_block_size * k + in_2 * (C + 1) + c;
 
               value = f_in_2_out * (total_density2 - partial_density)
                   / (total_density2 * total_density2);
               result.setQuick(i, j, value);
 
               /* Derivative terms with respect to supply/demand */
               if (partial_density != 0) {
                 if (DfDdemand1 != 0) {
                   j = x_block_size * k + size_demand_suply_block + in_1 * 2;
                   assert Numerical.validNumber(DfDdemand1);
                   result.setQuick(i, j, DfDdemand1);
                 }
                 if (DfDdemand2 != 0) {
                   j = x_block_size * k + size_demand_suply_block + in_2 * 2;
                   assert Numerical.validNumber(DfDdemand2);
                   result.setQuick(i, j, DfDdemand2);
                 }
                 if (DfDsupply != 0) {
                   j = x_block_size * k + size_demand_suply_block + out * 2 + 1;
                   assert Numerical.validNumber(DfDsupply);
                   result.setQuick(i, j, DfDsupply);
                 }
               }
             }
           }
 
         }
 
         // Derivative terms for 1xN junctions
       } else if (nb_prev == 1) {
         CellInfo cell_info;
         for (int k = 0; k < T; k++) {
           int in_id = junction.getPrev()[0].getUniqueId();
           cell_info = state.profiles[k].getCell(in_id);
           Cell[] next_cells = junction.getNext();
           total_density = cell_info.total_density;
           double demand = cell_info.demand;
 
           if (total_density != 0) {
             /*
              * We find j such that f_(in_id)_out = min (supply_j /
              * \beta_(in_id)_j)
              */
             double minimum = Double.MAX_VALUE;
             int minimum_id_cell = 0;
             double min_supply = -1, beta, supply, beta_at_minimum = 0;
             for (int out = 0; out < next_cells.length; out++) {
               beta = state.profiles[k]
                   .getJunction(junction)
                   .getAggregateSR(in_id, next_cells[out].getUniqueId());
               if (beta != 0) {
                 supply = state.profiles[k].getCell(next_cells[out]).supply;
                 if (supply / beta < minimum) {
                   min_supply = supply / beta;
                   minimum_id_cell = next_cells[out].getUniqueId();
                   beta_at_minimum = beta;
                 }
               }
             }
             assert min_supply != -1;
 
             double flow_out;
             Double tmp;
             if (demand < min_supply) {
               flow_out = demand;
               /* Derivative with respect to partial densities */
               for (int c = 0; c < C + 1; c++) {
                 partial_density = cell_info.partial_densities.get(c);
                 if (partial_density == null)
                   partial_density = 0.0;
 
                 i = H_block_size * k + mass_conservation_size
                     + flow_propagation_size + size_aggregate_split_ratios
                     + in_id * (C + 1) + c;
                 j = x_block_size * k + minimum_id_cell * (C + 1) + c;
 
                 tmp = internal_SR.get(k, j_id).get(in_id, minimum_id_cell, c);
                 if (tmp != null && tmp != 0) {
                   value = tmp
                       * flow_out *
                       (total_density - partial_density)
                       / (total_density * total_density);
                   assert Numerical.validNumber(value);
                   result.setQuick(i, j, value);
 
                   /* Derivative with respect to supply and demand */
                   j = x_block_size * k + size_demand_suply_block + in_id * 2;
                   value = tmp
                       * partial_density / total_density;
                   assert Numerical.validNumber(value);
                   result.setQuick(i, j, value);
                 }
               }
 
             } else if (min_supply < demand) {
               flow_out = min_supply;
 
               /* Derivative with respect to partial densities */
               for (int c = 0; c < C + 1; c++) {
                 partial_density = cell_info.partial_densities.get(c);
                 if (partial_density == null)
                   partial_density = 0.0;
 
                 i = H_block_size * k + mass_conservation_size
                     + flow_propagation_size + size_aggregate_split_ratios
                     + in_id * (C + 1) + c;
                 j = x_block_size * k + minimum_id_cell * (C + 1) + c;
 
                 tmp = internal_SR.get(k, j_id).get(in_id, minimum_id_cell, c);
                 if (tmp != null && tmp != 0) {
                   value = tmp
                       * flow_out *
                       (total_density - partial_density)
                       / (total_density * total_density);
                   assert Numerical.validNumber(value);
                   result.setQuick(i, j, value);
 
                   /* Derivative with respect to supply and demand */
                   j = x_block_size * k + size_demand_suply_block
                       + minimum_id_cell * 2 + 1;
                   value = tmp
                       * partial_density / total_density / beta_at_minimum;
                   assert Numerical.validNumber(value);
                   result.setQuick(i, j, value);
                 }
               }
 
             }
 
           }
         }
       } else {
         assert false : "[dhdx] Only 1x1, 1x2, and Nx1 junctions are possible";
       }
     }
     /*********************************************************
      * Derivative terms for the in-flows
      *********************************************************/
     int commodity, in_id, out_id;
     for (int j_id = 0; j_id < junctions.length; j_id++) {
       for (int k = 0; k < T; k++) {
         junction_SR = internal_SR.get(k, j_id);
         /* If there is no split ratios, it means it is a Nx1 junction */
 
         if (junction_SR == null) {
           for (in_id = junctions[j_id].getPrev()[0].getUniqueId(); in_id < junctions[j_id]
               .getPrev().length; in_id++) {
             // We don't know the split ratios so we suppose it is 1 for all
             assert junctions[j_id].getNext().length <= 1;
             for (int c = 0; c < C + 1; c++) {
               i = H_block_size * k + 2 * mass_conservation_size
                   + flow_propagation_size + aggregate_SR_index
                   + junctions[j_id].getNext()[0].getUniqueId() * (C + 1)
                   + c;
               j = x_block_size * k + f_out_position + in_id * (C + 1) + c;
 
               result.setQuick(i, j, 1.0);
             }
           }
 
         } else {
           Iterator<Entry<Triplet, Double>> iterator =
               junction_SR.non_compliant_split_ratios
                   .entrySet()
                   .iterator();
           Entry<Triplet, Double> entry;
           while (iterator.hasNext()) {
             entry = iterator.next();
             in_id = entry.getKey().incoming;
             out_id = entry.getKey().outgoing;
             commodity = entry.getKey().commodity;
 
             i = H_block_size * k + 2 * mass_conservation_size
                 + flow_propagation_size + aggregate_SR_index + out_id * (C + 1)
                 + commodity;
             j = x_block_size * k + f_out_position + in_id * (C + 1) + commodity;
 
             assert Numerical.validNumber(entry.getValue());
 
             result.setQuick(i, j, entry.getValue());
           }
         }
       }
     }
     for (int index = 0; index < H_block_size * T; index++)
       result.setQuick(index, index, -1.0);
 
     return new Some<SparseCCDoubleMatrix2D>(result);
   }
 
   /**
    * @brief Computes the derivative dJ/dU
    */
   @Override
   public DoubleMatrix1D djdu(State state, double[] control) {
 
     DoubleMatrix1D result =
         DoubleFactory1D.dense.make(T * temporal_control_block_size);
 
     int index_in_control = 0;
     double split_ratio;
     double sum_of_split_ratios;
     for (int orig = 0; orig < O; orig++) {
 
       for (int k = 0; k < T; k++) {
         /*
          * Mapping between
          * splits.get(sources[orig], k).get(0) and U[k*(C + sources.length)]
          */
         /* In case of full System Optimal computation we skip the NC flows */
         split_ratio = control[k * (C + sources.length)];
         if (split_ratio == 0) {
           continue;
         }
 
         double derivative_term = 0;
         if (control[k * temporal_control_block_size] == 0) {
           System.out.println("!FAILURE! A non compliant split ratio is ZERO !");
           assert false;
         } else {
           /*
            * The condition \beta >= 0 is already put in the solver (in
            * AdjointJVM/org.wsj/Optimizers.scala)
            */
         }
 
         sum_of_split_ratios = sources[orig].sum_split_ratios[k];
         if (sum_of_split_ratios == 0) {
           System.out
               .println("!Warning! Sum of the split ratios for an origin is Zero !");
           assert false;
         } else {
           // TODO : For now we imposes \sum \beta > 0.999
           assert sum_of_split_ratios >= 0.999;
           // To skip one operation we do 1 / (a-b) instead of - 1 / (b-a)
           derivative_term += epsilon / (0.999 - sum_of_split_ratios);
         }
 
         assert Numerical.validNumber(derivative_term);
 
         result.set(k * temporal_control_block_size, derivative_term);
       }
       index_in_control++;
 
       Iterator<Integer> it = sources[orig]
           .getCompliant_commodities()
           .iterator();
       while (it.hasNext()) {
         it.next(); // Needed to empty the iterator
         for (int k = 0; k < T; k++) {
           /*
            * Mapping between
            * splits.get(sources[orig], k).get(commodity) and
            * U[k*(C +sources.length) + index_in_control]
            */
           double derivative_term = 0;
 
           // The >= 0 constraint is already in the solver
           /*
            * if (c == 0) {
            * System.out.println("!FAILURE! A non compliant split ratio is ZERO !"
            * );
            * assert false;
            * } else {
            * 
            * }
            */
           sum_of_split_ratios = sources[orig].sum_split_ratios[k];
           if (sum_of_split_ratios == 0) {
             System.out
                 .println("!Warning! Sum of the split ratios for an origin is Zero !");
             assert false;
           } else {
             // TODO : For now we imposes \sum \beta > 0.999
             assert sum_of_split_ratios >= 0.999;
             derivative_term += epsilon / (0.999 - sum_of_split_ratios);
           }
 
           assert Numerical.validNumber(derivative_term);
 
           result.set(k * temporal_control_block_size
               + index_in_control, derivative_term);
         }
         index_in_control++;
       }
     }
 
     return result;
   }
 
   /**
    * @brief Computes the dJ/dX matrix
    * @details All terms are zero except the ones which are partial derivative in
    *          a partial density
    */
   @Override
   public SparseDoubleMatrix1D djdx(State state, double[] control) {
 
     SparseDoubleMatrix1D result = new SparseDoubleMatrix1D(T * x_block_size);
 
     /* We put 1 when we derivate along a partial density */
     int block_position;
     for (int k = 0; k < T; k++) {
       block_position = k * x_block_size;
       for (int partial_density_id = 0; partial_density_id < size_density_block; partial_density_id++) {
         result.setQuick(block_position + partial_density_id, 1.0);
       }
     }
 
     return result;
   }
 
   /**
    * @brief Forward simulate after having loaded the external split ratios
    * @details For now we even put the null split ratios because we never clear
    *          the split ratios
    */
   @Override
   public State forwardSimulate(double[] control) {
 
     IntertemporalOriginsSplitRatios splits = simulation.splits;
 
     int index_in_control = 0;
     int commodity;
     for (int orig = 0; orig < O; orig++) {
       double[] sum_of_split_ratios = new double[T];
       for (int k = 0; k < T; k++) {
         /*
          * Mapping between
          * splits.get(sources[orig], k).get(0)
          * and U[k*(C + sources.length + index_in_control)]
          */
         splits.get(sources[orig], k)
             .put(0,
                 control[k * temporal_control_block_size + index_in_control]);
         sum_of_split_ratios[k] +=
             control[k * temporal_control_block_size + index_in_control];
 
       }
       index_in_control++;
 
       Iterator<Integer> it = sources[orig]
           .getCompliant_commodities()
           .iterator();
       while (it.hasNext()) {
         commodity = it.next();
         for (int k = 0; k < T; k++) {
           /*
            * Mapping between
            * splits.get(sources[orig], k).get(commodity) and
            * U[k*(C +sources.length) + index_in_control]
            */
           splits.get(sources[orig], k).
               put(commodity,
                   control[k * temporal_control_block_size + index_in_control]);
           sum_of_split_ratios[k] +=
               control[k * temporal_control_block_size + index_in_control];
         }
         index_in_control++;
       }
       /* At the end we add the sum of the split ratios at that origin */
       for (int k = 0; k < T; k++)
         sources[orig].sum_split_ratios = sum_of_split_ratios;
     }
 
     return simulation.run(false);
   }
 
   /**
    * @brief Computes the objective function:
    *        \sum_(i,c,k) \rho(i,c,k)
    *        - \sum_{origin o} epsilon2 * ln(\sum \rho(o,c,k) - 1)
    */
   @Override
   public double objective(State state, double[] control) {
     double objective = 0;
 
     for (int k = 0; k < T; k++)
       for (int cell_id = 0; cell_id < cells.length; cell_id++)
         objective += state.profiles[k].getCell(cell_id).total_density;
 
     for (int orig = 0; orig < O; orig++)
       for (int k = 0; k < T; k++)
         objective -=
             epsilon * Math.log(sources[orig].sum_split_ratios[k] - 0.999);
 
     assert Numerical.validNumber(objective);
 
     return objective;
   }
 }

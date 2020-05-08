 /**
  * MolecularHashFactory.java
  *
  * 2011.11.09
  *
  * This file is part of the CheMet library
  *
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CheMet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.mdk.prototype.hash;
 
 import org.apache.log4j.Logger;
 import org.openscience.cdk.graph.SpanningTree;
 import org.openscience.cdk.interfaces.IAtom;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.interfaces.IBond;
 import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
 import uk.ac.ebi.mdk.prototype.hash.seed.AtomSeed;
 import uk.ac.ebi.mdk.prototype.hash.seed.ConnectedAtomSeed;
 import uk.ac.ebi.mdk.prototype.hash.seed.NonNullAtomicNumberSeed;
 import uk.ac.ebi.mdk.prototype.hash.seed.SeedFactory;
 import uk.ac.ebi.mdk.prototype.hash.util.ConnectionMatrixFactory;
 import uk.ac.ebi.mdk.prototype.hash.util.MutableInt;
 import uk.ac.ebi.mdk.prototype.hash.util.OccurrenceCounter;
 import uk.ac.ebi.mdk.prototype.hash.util.ParityCalculator;
 
 import java.util.Arrays;
 import java.util.BitSet;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.openscience.cdk.interfaces.IAtomType.Hybridization.SP3;
 import static org.openscience.cdk.interfaces.IBond.Order.DOUBLE;
 import static org.openscience.cdk.interfaces.IBond.Order.TRIPLE;
 import static org.openscience.cdk.interfaces.IBond.Stereo.DOWN;
 import static org.openscience.cdk.interfaces.IBond.Stereo.DOWN_INVERTED;
 import static org.openscience.cdk.interfaces.IBond.Stereo.UP;
 import static org.openscience.cdk.interfaces.IBond.Stereo.UP_INVERTED;
 import static org.openscience.cdk.interfaces.IBond.Stereo.UP_OR_DOWN;
 import static org.openscience.cdk.interfaces.IBond.Stereo.UP_OR_DOWN_INVERTED;
 
 /**
  * MolecularHashFactory - 2011.11.09 <br> Factory of generating MolecularHash
  * objects. The main method here is {@see getHash(IAtomContainer)} that can be
  * tuned with different {@see AtomSeed}s. The default {@see AtomSeed}s can be
  * altered using {@see addSeedMethod(AtomSeed)} and {@see setSeedMethods(Set)}.
  * Seeds are generated in the {@see SeedFactory}.
  *
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$ : Last Changed $Date$
  */
 public class MolecularHashFactory implements HashGenerator<Integer> {
 
     private static final Logger logger = Logger.getLogger(MolecularHashFactory.class);
     public static final Collection<AtomSeed> DEFAULT_SEEDS = SeedFactory.getInstance().getSeeds(NonNullAtomicNumberSeed.class,
                                                                                                 ConnectedAtomSeed.class);
     private final Collection<AtomSeed> seedMethods;
     private final ConnectionMatrixFactory matrixFactory = ConnectionMatrixFactory.getInstance();
 
     private boolean deprotonate = false;
     private boolean seedWithMoleculeSize = true;
     private int depth = 1;
 
     public MolecularHashFactory() {
         this(DEFAULT_SEEDS, 1, false);
     }
 
     public MolecularHashFactory(int depth) {
         this(DEFAULT_SEEDS, depth, false);
     }
 
     public MolecularHashFactory(Class<? extends AtomSeed>... seeds) {
         this(SeedFactory.getInstance().getSeeds(seeds), 1, false);
     }
 
     public MolecularHashFactory(Collection<AtomSeed> seeds, int depth, boolean deprotonate) {
         this.seedMethods = Collections.unmodifiableCollection(seeds);
         this.depth = depth;
         this.deprotonate = deprotonate;
     }
 
     public static MolecularHashFactory getInstance() {
         return MolecularHashFactoryHolder.INSTANCE;
     }
 
     private static class MolecularHashFactoryHolder {
         private static final MolecularHashFactory INSTANCE = new MolecularHashFactory();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Integer generate(IAtomContainer container) {
         return getHash(container).hash;
     }
 
     /**
      * Generates a hash code for the molecule with default seeds. The default
      * seeds are currently {@see NonNullAtomicNumberSeed} and {@see
      * ConnectedAtomSeed}. These can be modified using the {@see
      * setSeedMethods(Set)} and {@see addSeedMethod(AtomSeed)} methods.
      *
      * @param molecule the molecule to generate the hash for
      * @return The hash for this molecule
      */
     public MolecularHash getHash(IAtomContainer molecule) {
         return getHash(molecule, seedMethods);
     }
 
     /**
      * Generate a hash code with specified seeds. This method allows overriding
      * of the default seeds for selected entries.
      *
      * @param molecule The molecule to generate the hash for
      * @param methods  The methods that will be used to generate the hash
      * @return The hash for this molecule
      */
     public MolecularHash getHash(IAtomContainer molecule, Collection<AtomSeed> methods) {
 
         if (molecule.getAtomCount() == 0)
             return new MolecularHash(0, new int[0]);
 
         BitSet hydrogens = deprotonate ? getHydrogenMask(molecule) : new BitSet(molecule.getAtomCount());
         int[] precursorSeeds = getAtomSeeds(molecule, methods, hydrogens);
         byte[][] table = matrixFactory.getConnectionMatrix(molecule);
 
         BitSet stereoatoms = getTetrahedralCentres(molecule);
         BitSet dbStereoatoms = new BitSet(molecule.getAtomCount());
         int[] groups = getDoubleBondGroups(molecule, dbStereoatoms);
         int[] parities = getDoubleBondParities(molecule, dbStereoatoms, groups);
 
 
         return getHash(table, precursorSeeds, stereoatoms, hydrogens, molecule, true,
                        dbStereoatoms, groups, parities);
 
     }
 
     /**
      * Sets that explicit hydrogen atoms should not be included. Note if this
      * set you should avoid seeds that may include the value (e.g.
      * ConnectedAtomSeed).
      *
      * @param deprotonate whether to ignore hydrogens
      * @deprecated option will be immutable in future and must be set via the
      *             constructor
      */
     @Deprecated
     public void setDeprotonate(boolean deprotonate) {
         this.deprotonate = deprotonate;
     }
 
     public String toString(int[] seeds) {
         StringBuilder sb = new StringBuilder("[");
         int n = seeds.length - 1;
         for (int i = 0; i <= n; i++) {
             sb.append("0x").append(Integer.toHexString(seeds[i]));
             if (i == n) {
                 sb.append("]");
                 return sb.toString();
             }
             sb.append(", ");
         }
         throw new IllegalStateException("Unexpected state - could not convert int array");
     }
 
     public MolecularHash getHash(byte[][] table, int[] precursorSeeds,
                                  BitSet stereoatoms, BitSet hydrogens, IAtomContainer molecule, boolean shallow,
                                  BitSet dbAtoms, int[] dbGroups, int[] dbParities) {
 
         // System.out.println("seeds: " + toString(precursorSeeds));
         int n = precursorSeeds.length;
 
         int[] parities = new int[n];
 
         int[] previous = new int[n];
         int[] current = new int[n];
 
         IAtom[] atoms = AtomContainerManipulator.getAtomArray(molecule);
 
         copy(precursorSeeds, previous);
         copy(precursorSeeds, current);
 
         HashCounter globalCount = new HashCounter();
 
 //        System.out.println("starting:");
 //        for (int i = 0; i < n; i++) {
 //            System.out.printf("%4s: %s\n", atoms[i].getSymbol() + (i + 1), Integer.toHexString(current[i]));
 //        }
 
 
         current = setParity(stereoatoms, table, current, previous, molecule, dbAtoms, dbGroups, dbParities);
         copy(current, previous); // set the current values to the previous and repeat until depth
 //        System.out.println("after parity");
 //        for (int i = 0; i < n; i++) {
 //            System.out.printf("%4s: %s\n", atoms[i].getSymbol() + (i + 1), Integer.toHexString(current[i]));
 //        }
 
 
         HashCounter[] counters = new HashCounter[n];
         for (int i = 0; i < n; i++)
             counters[i] = new HashCounter();
 
 
         for (int d = 0; d < depth; d++) {
 
             for (int i = 0; i < previous.length; i++) {
                 current[i] = neighbourHash(i, previous[i], table, previous, counters[i], hydrogens);
             }
 
 //            for (int i = 0; i < n; i++) {
 //                System.out.printf("%2s %4s: %s\n", d, atoms[i].getSymbol() + (i + 1), Integer.toHexString(current[i]));
 //            }
 
             current = setParity(stereoatoms, table, current, previous, molecule, dbAtoms, dbGroups, dbParities);
             copy(current, previous); // set the current values to the previous and repeat until depth
 
         }
 
 
         for (HashCounter counter : counters)
             globalCount.addAll(counter);
 
 
         int hash = 49157;
         for (int i = 0; i < current.length; i++) {
 
             // if ignore (explicit) hydrogens we check that this atom isn't a hydrogen
             if (!hydrogens.get(i))
                 hash ^= rotate(current[i], globalCount.register(current[i]));
 
         }
 
 
         globalCount.clear();
         // un handled stereo centres need to do ensemble hash -
         if (shallow && !stereoatoms.isEmpty()) {
 
             int[] individual = new int[n];
 
             Map<List<Integer>, MutableInt> count = new HashMap<List<Integer>, MutableInt>();
 
             for (int i = 0; i < n; i++) {
                 int[] preturbed = Arrays.copyOf(precursorSeeds, n);
                 preturbed[i] = precursorSeeds[i] * 105341;
                 individual[i] = getHash(table, preturbed, (BitSet) stereoatoms.clone(), hydrogens, molecule, false, dbAtoms, dbGroups, dbParities).hash;
                 globalCount.register(hash);
                 hash ^= rotate(individual[i], globalCount.register(individual[i]));
             }
 
             return new MolecularHash(hash, individual, null);
         }
 
 
         return new MolecularHash(hash, precursorSeeds, null);
 
     }
 
     private BitSet getHydrogenMask(IAtomContainer container) {
 
         int n = container.getAtomCount();
 
         BitSet mask = new BitSet(n);
         for (int i = 0; i < n; i++) {
             mask.set(i, isHydrogen(container.getAtom(i)));
         }
 
         return mask;
 
     }
 
     private int getNonHydrogenAtomCount(IAtomContainer container, BitSet hydrogens) {
         return container.getAtomCount() - hydrogens.cardinality();
     }
 
     private boolean isHydrogen(IAtom atom) {
         return "H".equals(atom.getSymbol());
     }
 
     public int[] setParity(BitSet stereoatoms,
                            byte[][] table,
                            int[] current,
                            int[] previous,
                            IAtomContainer molecule,
                            BitSet dbAtoms,
                            int[] dbGroups,
                            int[] dbParities) {
 
         boolean found = false;
 
         // double bonds
         for (int i = dbAtoms.nextSetBit(0); i >= 0; i = dbAtoms.nextSetBit(i + 1)) {
 
             int j = dbGroups[i];
 
             int parity = dbParities[i] * getParity(table, i, previous) *
                     dbParities[j] * getParity(table, j, previous);
 
 //            System.out.println(toString(table));
 //            System.out.println(Arrays.toString(current));
 //            System.out.println(dbParities[i] + ", " + getParity(table, i, previous));
 //            System.out.println(dbParities[j] + ", " + getParity(table, j, previous));
 
             // can't set to previous... as this would alter other centres
             if (parity != 0) {
                 current[i] *= (parity == -1 ? 1309093 : 1316717); // bigprimes.net
                 current[j] *= (parity == -1 ? 1309093 : 1316717);
                 //System.out.println((i + 1) + " set to " + Integer.toHexString(current[i]));
                 found = true;
                 dbAtoms.clear(i);
                 dbAtoms.clear(j);
             }
 
         }
 
         // tetrahedral atoms
         for (int i = stereoatoms.nextSetBit(0); i >= 0; i = stereoatoms.nextSetBit(i + 1)) {
 
             Integer storage = molecule.getAtom(i).getStereoParity();
 
             if (storage == null)
                 throw new IllegalStateException("tried to set parities with null storage parity");
 
             if (storage == 2)
                 storage = -1;
 
             int order = getParity(table, i, previous);
 
             int parity = storage * order;
 
             //System.out.println((i + 1) + ": " + storage + " -> " + order + " -> " + parity);
             // can't set to previous... as this would alter other centres
             if (parity != 0) {
                 current[i] *= (parity == -1 ? 1300141 : 105913);
                 //System.out.println((i + 1) + " set to " + Integer.toHexString(current[i]));
                 found = true;
                 stereoatoms.clear(i);
             }
 
         }
 
 
         if (found && !stereoatoms.isEmpty()) {
             copy(current, previous);
             current = setParity(stereoatoms, table, current, previous, molecule, dbAtoms, dbGroups, dbParities);
             return current;
         }
 
         return current;
 
     }
 
     private int[] getDoubleBondParities(IAtomContainer container, BitSet bonds, int[] groups) {
 
         int[] parities = new int[container.getAtomCount()];
 
         for (int i = bonds.nextSetBit(0); i >= 0; i = bonds.nextSetBit(i + 1)) {
             parities[i] = ParityCalculator.getSP2Parity(container.getAtom(i), container);
             parities[groups[i]] = ParityCalculator.getSP2Parity(container.getAtom(groups[i]), container);
         }
 
         return parities;
 
     }
 
     private int[] getDoubleBondGroups(IAtomContainer container, BitSet atoms) {
 
         int n = container.getAtomCount();
 
         int[] groups = new int[n];
 
         SpanningTree tree = new SpanningTree(container);
         IAtomContainer cyclic = tree.getCyclicFragmentsContainer();
 
         for (IBond bond : container.bonds()) {
 
             if (DOUBLE.equals(bond.getOrder())) {
 
                 IAtom first = bond.getAtom(0);
                 IAtom second = bond.getAtom(1);
 
 
                 if (first.getFormalNeighbourCount() != null
                         && first.getFormalNeighbourCount() >= 2
                         && second.getFormalNeighbourCount() != null
                         && second.getFormalNeighbourCount() >= 2) {
 
 
                    // if one is not in a cycle we can calculate E/Z
                    boolean isCyclic = cyclic.contains(first) && cyclic.contains(second);
                    if (!isCyclic) {
 
                         if (validEZBond(container, first)
                                 && validEZBond(container, second)) {
 
 
                             int i = container.getAtomNumber(first);
                             int j = container.getAtomNumber(second);
 
                             atoms.set(i);
                             // atoms.set(j); // only set one we can get the other from the groups
 
                             // point to each other
                             groups[i] = j;
                             groups[j] = i;
 
                         }
                     }
                 }
 
 
             }
 
         }
 
         return groups;
 
     }
 
     /**
      * Checks whether an atom is connected to two double bonds and where an UP
      * and DOWN (wiggly) bond (indicating unspecified stereochemistry is
      * present)
      *
      * @param container the container
      * @param atom      the atom to test
      * @return whether it is connect to two double bonds
      */
     private boolean validEZBond(IAtomContainer container, IAtom atom) {
 
         int n = 0; // number of double bonds
 
         for (IBond bond : container.getConnectedBondsList(atom)) {
 
             if (DOUBLE.equals(bond.getOrder())
                     || TRIPLE.equals(bond.getOrder())) {
                 n++;
             }
 
 
             if (UP_OR_DOWN.equals(bond.getStereo()) ||
                     UP_OR_DOWN_INVERTED.equals(bond.getStereo())) {
                 return false;
             }
 
         }
 
         return n == 1;
 
     }
 
 
     /**
      * Returns the hash value xor'd with that of the atom's neighbours. The
      * method is recursive thus the depth indicates the current depth of the
      * method
      * <p/>
      * The max depth is set with the {@see setDepth(int depth)} method.
      *
      * @param index Atom index to add the neighbour values too
      * @param value The current value of the above atom
      * @return Computed value
      */
     private int neighbourHash(int index, int value,
                               byte[][] connectionTable, int[] precursorSeeds,
                               HashCounter counter, BitSet hydrogens) {
 
         if (hydrogens.get(index))
             return value;
 
 
         //System.out.print(Integer.toHexString(value) + " -> " + (value & 0x7) + " = ");
         value = rotate(value, (value & 0x7) + 1); // rotate using the low order bits 1..8 times
         //System.out.println(Integer.toHexString(value));
 
         for (int j = 0; j < connectionTable[index].length; j++) {
 
             if (hydrogens.get(j))
                 continue;
 
             if (connectionTable[index][j] != 0) {
                 //counter.register(value); // avoid self xor'ing - don't do this it can cause errors due to different order
 
 
                 int count = counter.register(precursorSeeds[j]);
                 int neighbour = rotate(precursorSeeds[j], count);
                 //System.out.print("\t" + "[" + j + "]0x" + Integer.toHexString(value) + "^0x" + Integer.toHexString(precursorSeeds[j]) + " ~ " + count + " => ");
                 //System.out.print(Integer.toHexString(neighbour) + " = ");
                 value ^= neighbour;
                 //System.out.println(Integer.toHexString(value));
             }
 
         }
 
         return value;
 
     }
 
 
     private static String toString(byte[][] table) {
         StringBuilder sb = new StringBuilder();
         for (byte[] aTable : table)
             sb.append(Arrays.toString(aTable)).append("\n");
         return sb.toString();
     }
 
     private int getParity(byte[][] table, int i, int[] hashes) {
 
         byte[] row = table[i];
 
         int count = 0;
         int n = row.length;
 
 
         for (int j = 0; j < n; j++) {
 
             // if we have a single connection
             if (row[j] == 1) {
 
                 int h = hashes[j];
                 //System.out.println(Integer.toHexString(h) + ": ");
 
                 for (int k = j + 1; k < n; k++) {
 
                     if (row[k] == 1) {
 
                         int cmp = hashes[k] > h ? 1 : hashes[k] < h ? -1 : 0;
                         //System.out.println("\t" + "[" + j + "] " +Integer.toHexString(h) + " - "  + "[" + k + "] " + Integer.toHexString(hashes[k]) + ": " + cmp);
 
                         // if this value is larger then the last value
                         // and that the last value isn't a hydrogen... note we still
                         // need to check for duplicate hydrogens
                         if (cmp > 0)
                             count++;
                         else if (cmp == 0)
                             return 0;
 
                     }
 
                 }
 
             }
 
         }
 
         // System.out.println(count);
 
         // number of swaps for atom numbers and the hashes
         return (count & 0x1) == 1 ? -1 : +1;
 
     }
 
     /**
      * Copies the source array to the destination.
      *
      * @param src  source array
      * @param dest destination array
      */
     private void copy(int[] src, int[] dest) {
         // not using System.arraycopy(src, 0, dest, 0, src.length); due to small overhead
         // note: we do not check they are the same size
         int length = src.length;
         for (int i = 0; i < length; ++i)
             dest[i] = src[i];
     }
 
     /**
      * Sets the depth to recurse on each atom
      *
      * @param depth
      */
     public void setDepth(int depth) {
         this.depth = depth;
     }
 
     /**
      * If set to true (default) the base seed will use the molecule size. This
      * will not allow comparison of sub-graph hashes. To allow sub-graph pseudo
      * sub-graph matching {@see MolecularHash#getSimilarity(MolecularHash)} this
      * should be set to false
      *
      * @param useMoleculeSize
      */
     public void setSeedWithMoleculeSize(boolean useMoleculeSize) {
         this.seedWithMoleculeSize = useMoleculeSize;
     }
 
     /**
      * Generates an array of atomic seed values for each atom in the molecule.
      * These seeds are generated using the provided methods
      *
      * @param molecule the molecule to generate the seeds for
      * @return array of integers representing the seeds for each atom in the
      *         molecule
      */
     public int[] getAtomSeeds(IAtomContainer molecule, Collection<AtomSeed> methods, BitSet hydrogens) {
 
         int[] seeds = new int[molecule.getAtomCount()];
         int n = getNonHydrogenAtomCount(molecule, hydrogens);
         int seed = seedWithMoleculeSize & n != 0
                 ? 389 % n // if ignore is not set this is an empty bitset
                 : 389;
 
         for (int i = 0; i < seeds.length; i++) {
 
             IAtom atom = molecule.getAtom(i);
 
             // set hydrogen value to be the minimum
             if (hydrogens.get(i)) {
                 seeds[i] = Integer.MIN_VALUE;
                 continue;
             }
 
             seeds[i] = seed;
 
             for (AtomSeed method : methods) {
                 seeds[i] = 257 * seeds[i] + method.seed(molecule,
                                                         atom);
             }
 
             // rotate the seed 1-5 times (using mask to get the lower bits)
             seeds[i] = rotate(seeds[i], seeds[i] & 0x5);
 
         }
 
         return seeds;
 
     }
 
     private BitSet getTetrahedralCentres(IAtomContainer container) {
 
         BitSet chiralatoms = new BitSet(container.getAtomCount());
 
         for (int i = 0; i < container.getAtomCount(); i++) {
             if (candidateTetrahedralCenter(container, container.getAtom(i))) {
                 chiralatoms.set(i);
             }
         }
 
         return chiralatoms;
 
     }
 
     private boolean candidateTetrahedralCenter(IAtomContainer container, IAtom atom) {
 
 
         if (SP3.equals(atom.getHybridization())
                 && atom.getFormalNeighbourCount() > 2
                 && hasStereoBonds(container, atom)) {
 
             // we don't use the MDL parity and need to adjust if we have hydrogens pressent
             int p = atom.getFormalNeighbourCount() == 4
                     ? ParityCalculator.getSP3Parity(atom, container)
                     : atom.getStereoParity();
             if (p != 0) {
                 if (p == -1)
                     p = 2;
                 atom.setStereoParity(p);
                 return true;
             }
         }
 
         return false;
 
     }
 
     private boolean hasStereoBonds(IAtomContainer container, IAtom atom) {
         for (IBond bond : container.getConnectedBondsList(atom)) {
 
             IBond.Stereo stereo = bond.getStereo();
 
             if (UP.equals(stereo) || DOWN.equals(stereo)
                     || UP_INVERTED.equals(stereo) || DOWN_INVERTED.equals(stereo)) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Performs pseudo random number generation on the provided seed
      *
      * @param seed
      * @return
      */
     public static int xorShift(int seed) {
         seed ^= seed << 6;
         seed ^= seed >>> 21;
         seed ^= (seed << 7);
         return seed;
     }
 
     /**
      * Rotates the seed using xor shift (pseudo random number generation) the
      * specified number of times.
      *
      * @param seed     the starting seed
      * @param rotation Number of xor rotations to perform
      * @return The starting seed rotated the specified number of times
      */
     public static int rotate(int seed, int rotation) {
         for (int j = 0; j < rotation; j++) {
             seed = xorShift(seed);
         }
         return seed;
     }
 
     /**
      * Rotates the seed if the seed has already been seen in the provided
      * occurrences map
      *
      * @param seed
      * @param occurences
      * @return
      */
     public static int rotate(int seed, Map<Integer, MutableInt> occurences) {
         if (occurences.get(seed) == null) {
             occurences.put(seed, new MutableInt());
         } else {
             occurences.get(seed).increment();
         }
         return rotate(seed, occurences.get(seed).get() - 1);
     }
 
 
     private class HashCounter extends OccurrenceCounter<Integer> {
     }
 
 }

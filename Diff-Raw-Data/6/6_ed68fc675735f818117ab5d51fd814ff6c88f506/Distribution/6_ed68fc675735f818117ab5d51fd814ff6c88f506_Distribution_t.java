 package edu.teco.dnd.deploy;
 
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.teco.dnd.blocks.FunctionBlock;
 import edu.teco.dnd.graphiti.model.FunctionBlockModel;
 import edu.teco.dnd.module.Module;
 import edu.teco.dnd.module.config.BlockTypeHolder;
 
 /**
  * A Distribution of {@link FunctionBlock} to {@link BlockTypeHolder}s.
  * 
  * @author Philipp Adolf
  */
 public class Distribution {
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(Distribution.class);
 
 	/**
 	 * Mapping from FunctionBlock to assigned BlockTarget.
 	 */
 	private final Map<FunctionBlockModel, BlockTarget> blocks;
 
 	/**
 	 * Mapping from BlockTarget to all assigned FunctionBlocks.
 	 */
 	private final Map<BlockTarget, Collection<FunctionBlockModel>> targets;
 
 	/**
 	 * Mapping from BlockTypeHolder to matching BlockTarget.
 	 */
 	private final Map<Entry<Module, BlockTypeHolder>, BlockTarget> blockTargets;
 
 	/**
 	 * Returns the mapping for this distribution.
 	 * 
 	 * @return the mapping for the distribution
 	 */
 	public Map<FunctionBlockModel, BlockTarget> getMapping() {
 		return Collections.unmodifiableMap(blocks);
 	}
 
 	/**
 	 * Initializes a new, empty distribution.
 	 */
 	public Distribution() {
 		blocks = new HashMap<FunctionBlockModel, Distribution.BlockTarget>();
 		targets = new HashMap<Distribution.BlockTarget, Collection<FunctionBlockModel>>();
 		blockTargets = new HashMap<Entry<Module, BlockTypeHolder>, Distribution.BlockTarget>();
 	}
 
 	/**
 	 * Clones a distribution. This distribution can be changed without affecting <code>old</code>.
 	 * 
 	 * @param old
 	 *            the Distribution to clone
 	 */
 	public Distribution(final Distribution old) {
 		this(new HashMap<FunctionBlockModel, BlockTarget>(old.blocks),
 				new HashMap<BlockTarget, Collection<FunctionBlockModel>>(old.targets),
 				new HashMap<Entry<Module, BlockTypeHolder>, BlockTarget>(old.blockTargets));
 	}
 
 	/**
 	 * Used to clone a distribution.
 	 * 
 	 * @param blocks
 	 *            the mapping of FunctionBlocks to assigned BlockTargets
 	 * @param targets
 	 *            the mapping of BlockTarget to assigned FunctionBlocks
 	 * @param blockTargets
 	 *            the mapping of BlockTypeHolder to BlockTarget
 	 */
 	private Distribution(final Map<FunctionBlockModel, BlockTarget> blocks,
 			final Map<BlockTarget, Collection<FunctionBlockModel>> targets,
 			final Map<Entry<Module, BlockTypeHolder>, BlockTarget> blockTargets) {
 		this.blocks = blocks;
 		this.targets = targets;
 		this.blockTargets = blockTargets;
 	}
 
 	/**
 	 * Adds a mapping if there are free slots and the typeHolder has the right type.
 	 * 
 	 * @param block
 	 *            the block to add
 	 * @param module
 	 *            the module to add the block to
 	 * @param typeHolder
 	 *            the typeHolder to add the block to
 	 * @return true if the block was added
 	 * @see #canAdd(FunctionBlock, Module, BlockTypeHolder)
 	 */
 	public boolean add(final FunctionBlockModel block, final Module module, final BlockTypeHolder typeHolder) {
 		LOGGER.entry(block, module, typeHolder);
 		final BlockTarget blockTarget = getBlockTarget(module, typeHolder);
 
 		final boolean result = blockTarget.add(this, block);
 		LOGGER.exit(result);
 		return result;
 	}
 
 	/**
 	 * Checks if a FunctionBlock can be added to a typeHolder of the given module.
 	 * 
 	 * @param block
 	 *            the block to check
 	 * @param module
 	 *            the module to check
 	 * @param typeHolder
 	 *            the typeHolder to check
 	 * @return true if the block can be added
 	 */
 	public boolean canAdd(final FunctionBlockModel block, final Module module, final BlockTypeHolder typeHolder) {
 		return getBlockTarget(module, typeHolder).canAdd(this, block);
 	}
 
 	/**
 	 * Removes a mapping if it exists.
 	 * 
 	 * @param block
 	 *            the block the mapping should be removed for
 	 */
 	public void remove(final FunctionBlockModel block) {
 		LOGGER.entry(block);
 		final BlockTarget blockTarget = this.blocks.remove(block);
 		LOGGER.trace("block target {}", blockTarget);
 		if (blockTarget != null) {
 			final Collection<FunctionBlockModel> t = targets.get(blockTarget);
 			LOGGER.trace("t {}", t);
 			if (t != null) {
 				t.remove(block);
 			}
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Returns the block target for a given module and typeHolder. The BlockTarget is created if it doesn't exist yet.
 	 * 
 	 * @param module
 	 *            the module the typeHolder belongs to
 	 * @param typeHolder
 	 *            the typeHolder for which to return the BlockTarget
 	 * @return the BlockTarget for <code>typeHolder</code>
 	 */
 	private BlockTarget getBlockTarget(final Module module, final BlockTypeHolder typeHolder) {
 		final Entry<Module, BlockTypeHolder> key =
 				new AbstractMap.SimpleEntry<Module, BlockTypeHolder>(module, typeHolder);
 		BlockTarget target = blockTargets.get(key);
 		if (target == null) {
 			target = new BlockTarget(module, typeHolder);
 			blockTargets.put(key, target);
 		}
 
 		return target;
 	}
 
 	@Override
 	public String toString() {
 		final StringBuilder sb = new StringBuilder();
 		sb.append("Distribution[");
 		boolean first = true;
 		for (Entry<FunctionBlockModel, BlockTarget> entry : blocks.entrySet()) {
 			if (first) {
 				first = false;
 			} else {
 				sb.append(",");
 			}
 			sb.append(entry.getKey());
 			sb.append(" => ");
 			sb.append(entry.getValue());
 		}
 		sb.append("]");
 		return sb.toString();
 	}
 
 	/**
 	 * Represents a target for a FunctionBlock mapping. It consists of a BlockTypeHolder and the Module the
 	 * BlockTypeHolder belongs to.
 	 * 
 	 * @author Philipp Adolf
 	 */
 	public static class BlockTarget {
 		/**
 		 * The module {@link #holder} belongs to.
 		 */
 		private final Module module;
 
 		/**
 		 * The BlockTypeHolder this BlockTarget represents.
 		 */
 		private final BlockTypeHolder holder;
 
 		/**
 		 * Initializes a new BlockTarget.
 		 * 
 		 * @param module
 		 *            the module <code>holder</code> belongs to
 		 * @param holder
 		 *            the BlockTypeHolder this object represents
 		 */
 		public BlockTarget(final Module module, final BlockTypeHolder holder) {
 			this.module = module;
 			this.holder = holder;
 		}
 
 		/**
 		 * Returns the module the BlockTypeHolder represented by this object belongs to.
 		 * 
 		 * @return the module the BlockTypeHolder represented by this object belongs to
 		 */
 		public Module getModule() {
 			return module;
 		}
 
 		/**
 		 * Returns the BlockTypeHolder represented by this object.
 		 * 
 		 * @return the BlockTypeHolder represented by this object
 		 */
 		public BlockTypeHolder getBlockTypeHolder() {
 			return holder;
 		}
 
 		/**
 		 * Returns the number of slots of the BlockTypeHolder the given Distribution uses. Does not include slots that
 		 * are used outside of the Distribution.
 		 * 
 		 * @param distribution
 		 *            the distribution to check
 		 * @return the number of slots in used by the Distribution
 		 */
 		public int getDistributionUsage(final Distribution distribution) {
 			LOGGER.entry(distribution);
 			if (holder.isLeaf()) {
 				Collection<FunctionBlockModel> targetCollection = distribution.targets.get(this);
 				if (targetCollection == null) {
 					LOGGER.exit(0);
 					return 0;
 				} else {
 					final int collectionSize = targetCollection.size();
 					LOGGER.exit(collectionSize);
 					return collectionSize;
 				}
 			} else {
 				int sum = 0;
 				for (final BlockTypeHolder child : holder.getChildren()) {
 					sum += distribution.getBlockTarget(module, child).getDistributionUsage(distribution);
 				}
 				LOGGER.exit(sum);
 				return sum;
 			}
 		}
 
 		/**
 		 * Returns the number of free slots in the BlockTypeHolder taking into account the number of slots used by the
 		 * given Distribution.
 		 * 
 		 * @param distribution
 		 *            the Distribution to check
 		 * @return the number of free slots
 		 */
 		public int getFree(final Distribution distribution) {
 			LOGGER.entry(distribution);
 			if (holder.getAmountAllowed() < 0) {
 				LOGGER.exit(Integer.MAX_VALUE);
 				return Integer.MAX_VALUE;
 			}
 			final int localFree = holder.getAmountLeft() - getDistributionUsage(distribution);
 			final BlockTarget parent = getParent(distribution);
 			if (localFree <= 0 || parent == null) {
 				LOGGER.exit(localFree);
 				return localFree;
 			} else {
 				final int result = Math.min(localFree, parent.getFree(distribution));
 				LOGGER.exit(result);
 				return result;
 			}
 		}
 
 		/**
 		 * Returns the parent of this BlockTarget for the given distribution.
 		 * 
 		 * @param distribution
 		 *            the distribution to check
 		 * @return the parent of this object or null if there is none
 		 */
 		public BlockTarget getParent(final Distribution distribution) {
 			final BlockTypeHolder parentHolder = holder.getParent();
 			if (parentHolder == null) {
 				return null;
 			} else {
 				return distribution.getBlockTarget(module, parentHolder);
 			}
 		}
 
 		/**
 		 * Checks if a mapping for a given block is valid. A mapping is valid if the BlockTypeHolder is a leave, the
 		 * type of block matches the type of the BlockTypeHolder and there are free slots.
 		 * 
 		 * @param distribution
 		 *            the Distribution to check
 		 * @param block
 		 *            the {@link FunctionBlock} to check
 		 * @return true if the mapping is valid
 		 */
 		public boolean canAdd(final Distribution distribution, final FunctionBlockModel block) {
 			LOGGER.entry(distribution, block);
 			if (!holder.isLeaf()) {
 				LOGGER.exit(false);
 				return false;
 			}
 			if (LOGGER.isTraceEnabled()) {
 				LOGGER.trace("holder type: {}, block type: {}", holder.getType(), block.getType());
 			}
			String blockType = block.getType();
			if (blockType == null) {
				blockType = "";
			}
			if (!blockType.matches(holder.getType())) {
 				LOGGER.exit(false);
 				return false;
 			}
 			if (getFree(distribution) <= 0) {
 				LOGGER.exit(false);
 				return false;
 			}
 			LOGGER.exit(true);
 			return true;
 		}
 
 		/**
 		 * Adds a mapping for the given FunctionBlock to the BlockTypeHolder represented by this object to the given
 		 * Distribution if the mapping is valid.
 		 * 
 		 * @param distribution
 		 *            the Distribution the mapping should be added to
 		 * @param block
 		 *            the block the mapping should be added for
 		 * @return true if the mapping was added
 		 * @see #canAdd(Distribution, FunctionBlock)
 		 */
 		public boolean add(final Distribution distribution, final FunctionBlockModel block) {
 			LOGGER.entry(block);
 			if (!canAdd(distribution, block)) {
 				LOGGER.exit(false);
 				return false;
 			}
 
 			distribution.blocks.put(block, this);
 			Collection<FunctionBlockModel> targetCollection = distribution.targets.get(this);
 			if (targetCollection == null) {
 				targetCollection = new ArrayList<FunctionBlockModel>();
 				distribution.targets.put(this, targetCollection);
 			}
 			targetCollection.add(block);
 
 			LOGGER.exit(true);
 			return true;
 		}
 
 		@Override
 		public String toString() {
 			final StringBuilder sb = new StringBuilder();
 			sb.append("BlockTypeHolder[module=");
 			sb.append(module.getUUID());
 			sb.append(",id=");
 			sb.append(holder.getIdNumber());
 			if (holder.isLeaf()) {
 				sb.append(",type='");
 				sb.append(holder.getType());
 				sb.append("',amount=");
 				sb.append(holder.getAmountAllowed());
 			}
 			sb.append("]");
 			return sb.toString();
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((holder == null) ? 0 : holder.hashCode());
 			result = prime * result + ((module == null) ? 0 : module.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			BlockTarget other = (BlockTarget) obj;
 			if (holder == null) {
 				if (other.holder != null)
 					return false;
 			} else if (!holder.equals(other.holder))
 				return false;
 			if (module == null) {
 				if (other.module != null)
 					return false;
 			} else if (!module.equals(other.module))
 				return false;
 			return true;
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((blockTargets == null) ? 0 : blockTargets.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Distribution other = (Distribution) obj;
 		if (blockTargets == null) {
 			if (other.blockTargets != null)
 				return false;
 		} else if (!blockTargets.equals(other.blockTargets))
 			return false;
 		return true;
 	}
 }

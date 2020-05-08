 package edu.teco.dnd.module.config;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.google.gson.annotations.SerializedName;
 
 /**
  * TreeElement that wraps an allowed blocktype (can have children/ parents).
  * 
  * @author Marvin Marx
  */
 public class BlockTypeHolder {
 	/**
 	 * logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(BlockTypeHolder.class);
 
 	/**
 	 * The type this blockTypeHolder gives the amount... for.
 	 */
 	private final String type;
 	/** allowed blocks of this type, < 0 means infinity. */
 	@SerializedName("amount")
 	private int amountAllowed = -1;
 	private int amountLeft = -1;
 	/** used during deploy to give exact position to run block on in case of doubt. */
 	private int idNumber = -1;
 	/** null if none. */
 	private Set<BlockTypeHolder> children = null;
 
 	private transient BlockTypeHolder parent = null;
 
 	/**
 	 * For Gson.
 	 */
 	@SuppressWarnings("unused")
 	private BlockTypeHolder() {
 		this((String) null, -1);
 	}
 
 	/**
 	 * constructs a new Blocktype holder as a leaf node of the tree.
 	 * 
 	 * @param type
 	 *            the type this leaf node referes to
 	 * @param amount
 	 *            the amount that must not be exceeded. (infinite if < 0)
 	 */
 	public BlockTypeHolder(String type, int amount) {
 		// leave node
 		this.type = type;
 		this.children = null;
 		this.amountAllowed = amount;
 		this.amountLeft = amount;
 	}
 
 	/**
 	 * constructs a new non leaf node. Can not have a type however does have children. Children can also be added later.
 	 * 
 	 * @param childblocks
 	 *            the children of this block
 	 * @param amount
 	 *            the maximum amount allowed of all subnodes together. ( < 0 means no limits)
 	 */
 	public BlockTypeHolder(Set<BlockTypeHolder> childblocks, int amount) {
 		// non leave node
 		this.type = null;
 		this.children = (childblocks == null) ? new HashSet<BlockTypeHolder>() : childblocks;
 		this.amountAllowed = amount;
 		this.amountLeft = amount;
 	}
 
 	/**
 	 * constructs a new non leave node with a maximum amount, yet no children. children can be added later.
 	 * 
 	 * @param amount
 	 *            the maximum amount allowed of all subnodes together. ( < 0 means no limits)
 	 */
 	public BlockTypeHolder(final int amount) {
 		// non leave node
 		this.type = null;
 		this.children = new HashSet<BlockTypeHolder>();
 		this.amountAllowed = amount;
 		this.amountLeft = amount;
 	}
 
 	/**
 	 * adds a single child to a non leave node.
 	 * 
 	 * @param child
 	 *            the child to add.
 	 * @throws IllegalStateException
 	 *             if node is a leaf node.
 	 */
 	public void addChild(BlockTypeHolder child) throws IllegalStateException {
 		addChild(Arrays.asList(child));
 	}
 
 	/**
 	 * add a collection of children to a non leaf node.
 	 * 
 	 * @param childrenToAdd
 	 *            a collection of children to add to this block.
 	 * @throws IllegalStateException
 	 *             if node is a leaf node.
 	 */
 	public void addChild(Collection<BlockTypeHolder> childrenToAdd) throws IllegalStateException {
 		if (type != null) {
 			throw new IllegalStateException("Node has type and is not a leave node.");
 		}
 		if (children == null) {
 			children = new HashSet<BlockTypeHolder>();
 		}
 		this.children.addAll(childrenToAdd);
 	}
 
 	/**
 	 * @return childblocks, an return null if leave node.
 	 */
 	public Set<BlockTypeHolder> getChildren() {
 		return children;
 	}
 
 	/**
 	 * 
 	 * @return the parent of this BlockTypeHolder.
 	 */
 	public BlockTypeHolder getParent() {
 		return parent;
 	}
 
 	/**
 	 * set a new parent for this blockTypeHolder.
 	 * 
 	 * @param parent
 	 *            the parent to set.
 	 */
 	public void setParent(BlockTypeHolder parent) {
 		this.parent = parent;
 	}
 
 	/**
 	 * 
 	 * @return the amount that was allowed to run in when this holder was set up.
 	 */
 	public int getAmountAllowed() {
 		return amountAllowed;
 	}
 
 	/**
 	 * 
 	 * @return the amount that is still left to run of this blocktype (after the ones already running have been
 	 *         subtracted).
 	 */
 	public int getAmountLeft() {
 		return amountLeft;
 	}
 
 	/**
 	 * Set the amount that is left being allowed to run to the given number.
 	 * 
 	 * @param amountLeft
 	 *            the amount to set this to.
 	 */
 	public void setAmountLeft(int amountLeft) {
 		LOGGER.trace("setting amountLeft to {}", amountLeft);
 		this.amountLeft = amountLeft;
 	}
 
 	/**
 	 * 
 	 * @return the type of the block if a leaf node, null if not.
 	 */
 	public String getType() {
 		return this.type;
 	}
 
 	/**
 	 * @return the idNumber
 	 */
 	public int getIdNumber() {
 		return idNumber;
 	}
 
 	/**
 	 * @param idNumber
 	 *            the idNumber to set >= 0
 	 */
 	public void setIdNumber(int idNumber) {
 		if (idNumber < 0) {
 			throw new IllegalArgumentException();
 		}
 		this.idNumber = idNumber;
 	}
 	
 	/**
 	 * Tries to add a block of the given type to this BlockTypeHolder. For this to succeed the type of this holder has
 	 * to match the given type and there has to be a free slot in this BlockTypeHolder and all of its parents. Can be
 	 * called concurrently, but if not all blocks can be scheduled which ones are and which are not is nondeterministic.
 	 * 
 	 * @param blockType
 	 *            the type of block to schedule
 	 * @return true if the block was succesfully added, false otherwise
 	 */
 	public synchronized boolean tryAdd(final String blockType) {
 		if (blockType == null || !blockType.matches(type)) {
 			return false;
 		}
 		return tryDecrease();
 	}
 
 	/**
 	 * Recursively tries to decrease the values of amountLeft up to the parent-node. Returns true if every node had at
 	 * least one allowed left (or was negative), if at any point 0 allowed are encountered reverses the whole process
 	 * and returns false. <br>
 	 * Process is atomic. If two different blocks mutually excluding each other are requested exactly one will succeed
 	 * however which one is not deterministic.<br>
 	 * (tl;dr: try decrease amount of allowed blocks up to parent node. If amount=0 --> reverse everything and return
 	 * false; else true)
 	 * 
 	 * @return true iff the action is possible false otherwise. see above!
 	 */
 	private synchronized boolean tryDecrease() {
 		LOGGER.entry();
 		if (amountLeft != 0 && (parent == null || parent.tryDecrease())) {
 			if (amountLeft > 0) {
 				amountLeft--;
 			}
 			LOGGER.exit(true);
 			return true;
 		} else {
 			LOGGER.exit(false);
 			return false;
 		}
 	}
 
 	/**
 	 * increase the amount left allowed to run on this block by one (not higher than amount allowed) and calls the
 	 * method recursively on it's parents.
 	 */
 	public synchronized void increase() {
 		if (amountLeft >= 0) {
 			if (amountLeft < amountAllowed) {
				parent.increase();
 				amountLeft++;
 			} else {
 				LOGGER.warn("more block=>{} freed than marked as in use.", type);
 			}
 		}
 	}
 
 	/**
 	 * Returns a Map that tells which types of functionBlocks and how many each are allowed to run according to this
 	 * node and all it's subnodes.
 	 * 
 	 * @return Map from Types to amount of available slots for function blocks of this type
 	 */
 	public HashMap<String, Integer> getAllAllowedChildTypes() {
 		HashMap<String, Integer> types = new HashMap<String, Integer>();
 		mapAllowedChildTypes(types);
 		return types;
 	}
 
 	/**
 	 * recursively adds all allowed child types and their amount to the map given as parameter.
 	 * 
 	 * @param allowedChildren
 	 *            the map to fill with the allowed child types and their amount.
 	 * @return the now filled map.
 	 */
 	private HashMap<String, Integer> mapAllowedChildTypes(HashMap<String, Integer> allowedChildren) {
 		if (type != null && amountLeft > 0) {
 			if (allowedChildren.containsKey(type)) {
 				int sum = allowedChildren.get(type);
 				sum += amountLeft;
 				allowedChildren.remove(type);
 				allowedChildren.put(type, sum);
 			} else {
 				allowedChildren.put(type, amountLeft);
 			}
 		} else if (children != null) {
 			for (BlockTypeHolder child : children) {
 				allowedChildren = child.mapAllowedChildTypes(allowedChildren);
 			}
 		}
 		return allowedChildren;
 	}
 
 	/**
 	 * 
 	 * @return true iff this node is a leaf.
 	 */
 	public boolean isLeaf() {
 		return type != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + amountAllowed;
 		result = prime * result + ((children == null) ? 0 : children.hashCode());
 		result = prime * result + ((type == null) ? 0 : type.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		BlockTypeHolder other = (BlockTypeHolder) obj;
 		if (amountAllowed != other.amountAllowed) {
 			return false;
 		}
 		if (children == null) {
 			if (other.children != null) {
 				return false;
 			}
 		} else if (!children.equals(other.children)) {
 			return false;
 		}
 		if (type == null) {
 			if (other.type != null) {
 				return false;
 			}
 		} else if (!type.equals(other.type)) {
 			return false;
 		}
 		return true;
 	}
 }

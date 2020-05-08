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
  * 
  * @author Marvin Marx
  * 
  */
 public class BlockTypeHolder {
 
 	private static final Logger LOGGER = LogManager.getLogger(BlockTypeHolder.class);
 
 	public final String type;
 	/** allowed blocks of this type, <0 means infinity. */
 	@SerializedName("amount")
 	private int amountAllowed = -1;
 	private int amountLeft = -1;
 	private int idNumber = -1;
 	/** null if none */
 	private Set<BlockTypeHolder> children = null;
 
 	private transient BlockTypeHolder parent = null;
 
 	public BlockTypeHolder() {
 		this((String) null, -1);
 	}
 
 	public BlockTypeHolder(String type, int amount) {
 		// leave node
 		this.type = type;
 		this.children = null;
 		this.amountAllowed = amount;
 		this.amountLeft = amount;
 	}
 
 	public BlockTypeHolder(Set<BlockTypeHolder> childblocks, int amount) {
 		// non leave node
 		this.type = null;
 		this.children = (childblocks == null) ? new HashSet<BlockTypeHolder>() : childblocks;
 		this.amountAllowed = amount;
 		this.amountLeft = amount;
 	}
 
 	public BlockTypeHolder(int amount) {
 		// non leave node
 		this.type = null;
 		this.children = new HashSet<BlockTypeHolder>();
 		this.amountAllowed = amount;
 		this.amountLeft = amount;
 	}
 
 	public void addChild(BlockTypeHolder child) {
 		addChild(Arrays.asList(child));
 	}
 
 	public void addChild(Collection<BlockTypeHolder> children) {
 		if (type != null)
 			throw new IllegalStateException("Node has type and is not a leave node.");
 		if (children == null)
 			children = new HashSet<BlockTypeHolder>();
 		this.children.addAll(children);
 	}
 
 	/**
 	 * @return childblocks, an return null if leave node.
 	 */
 	public Set<BlockTypeHolder> getChildren() {
 		return children;
 	}
 
 	public BlockTypeHolder getParent() {
 		return parent;
 	}
 
 	public void setParent(BlockTypeHolder parent) {
 		this.parent = parent;
 	}
 
 	public int getAmountAllowed() {
 		return amountAllowed;
 	}
 
 	public int getAmountLeft() {
 		return amountLeft;
 	}
 
 	public void setAmountLeft(int amountLeft) {
 		LOGGER.trace("setting amountLeft to {}", amountLeft);
 		this.amountLeft = amountLeft;
 	}
 
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
 	public synchronized boolean tryDecrease() {
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
 	 * Returns a Map that tells which types of function blocks can run on this module and how many. Works if this
 	 * BlockTypeHolder is the parent of all BlockTypeHolders for this module. If not: incomplete.
 	 * 
 	 * @return Map from Types to amount of available slots for function blocks of this type
 	 */
 	public HashMap<String, Integer> getTypes() {
 		HashMap<String, Integer> types = new HashMap<String, Integer>();
 		mapType(types);
 		return types;
 	}
 
 	private HashMap<String, Integer> mapType(HashMap<String, Integer> map) {
 		if (type != null && amountLeft > 0) {
 			if (map.containsKey(type)) {
 				int sum = map.get(type);
 				sum += amountLeft;
 				map.remove(type);
 				map.put(type, sum);
 			} else {
 				map.put(type, amountLeft);
 			}
 		} else if (children != null) {
 			for (BlockTypeHolder child : children) {
 				map = child.mapType(map);
 			}
 		}
 		return map;
 	}
 
 	public boolean isLeave() {
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

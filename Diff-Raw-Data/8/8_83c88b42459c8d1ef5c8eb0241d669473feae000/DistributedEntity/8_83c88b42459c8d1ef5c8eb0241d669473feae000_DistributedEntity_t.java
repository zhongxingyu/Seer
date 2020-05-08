 /*
  * DistributedEntity.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.domain;
 
 import static com.google.common.base.Preconditions.*;
 
 import java.io.IOException;
 import java.io.InvalidObjectException;
 import java.io.ObjectInputStream;
 import java.io.ObjectStreamException;
 import java.io.Serializable;
 import java.security.MessageDigest;
 import java.util.Date;
 import java.util.regex.Pattern;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.PostLoad;
 import javax.persistence.PrePersist;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import com.google.common.base.Charsets;
 import com.google.inject.Inject;
 
 /**
  * Abstract base class for entities which are "distributed" across multiple
  * client instances. All such entities are immutable and cannot be changed
  * directly. To modify a distributed entity, a completely new child object with
  * the required changes is created. The parent and child attributes store the
  * relationship between the old (overridden) and new entities. This scheme
  * provides a sort of version tracking when objects are shared by multiple
  * clients.
  * <p>
  * 
  * One of the design goals for this class is to provide a consistent object
  * identifier for use across multiple clients, without any central issuing
  * authority. This goal is achieved by (indirectly) using the result of
  * {@link #getHashBytes()} as the identifier for entities inheriting from this
  * class. As a result, all concrete subclasses must implement
  * {@link #getBytes()}, which has a stronger contract in this class than is
  * inherited from {@link HasBytes}. See that method for details.
  * 
  * @author Meyer Kizner
  * @see #getBytes()
  * 
  */
 @Entity
 @Inheritance(strategy = InheritanceType.JOINED)
 public abstract class DistributedEntity implements HasBytes, Serializable {
 	private static final long serialVersionUID = 7547760315320101572L;
 
 	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
 
 	private static final Pattern HEX_REGEX = Pattern.compile("[0-9a-f]*");
 
 	@Inject
 	private static MessageDigest digest;
 
 	/**
 	 * @return the length, in bytes, of hashes produced by
 	 *         {@link #getHashBytes()}
 	 */
 	static int getHashLength() {
 		return digest.getDigestLength();
 	}
 
 	/**
 	 * Merges a number of byte arrays into a single array. This utility method
 	 * is helpful in implementing {@link #getBytes()} in subclasses.
 	 * 
 	 * @param arrays
 	 *            an array of byte arrays
 	 * @return a single byte array representing the entire input concatenated
 	 *         together, with null bytes between each entry in the input
 	 */
 	static byte[] merge(byte[]... arrays) {
 		int totalLength = 0;
 		for (byte[] array : arrays) {
 			totalLength += array.length;
 		}
 		byte[] merged = new byte[totalLength + arrays.length];
 		int pos = 0;
 		for (byte[] array : arrays) {
 			System.arraycopy(array, 0, merged, pos, array.length);
 			merged[pos + array.length] = 0x00;
 			pos += array.length + 1;
 		}
 		return merged;
 	}
 
 	/**
 	 * Merges an {@link Iterable} of byte arrays into a single array. This
 	 * utility method is helpful in implementing {@link #getBytes()} in
 	 * subclasses.
 	 * 
 	 * @param arrays
 	 *            an {@code Iterable} of byte arrays
 	 * @return a single byte array representing the entire input concatenated
 	 *         together, with null bytes between each entry in the input
 	 */
 	static byte[] merge(Iterable<byte[]> arrays) {
 		int totalLength = 0;
 		for (byte[] array : arrays) {
 			totalLength += array.length + 1;
 		}
 		byte[] merged = new byte[totalLength];
 		int pos = 0;
 		for (byte[] array : arrays) {
 			System.arraycopy(array, 0, merged, pos, array.length);
 			merged[pos + array.length] = 0x00;
 			pos += array.length + 1;
 		}
 		return merged;
 	}
 
 	private Date createDate;
 
 	private transient Date persistDate;
 
 	private User creator;
 
 	private DistributedEntity parent;
 
 	private transient DistributedEntity child;
 
 	private transient String storedHash;
 
 	protected DistributedEntity() {
 		this(null, null);
 	}
 
 	protected DistributedEntity(User creator) {
 		this(creator, null);
 	}
 
 	protected DistributedEntity(DistributedEntity parent) {
 		this(null, parent);
 	}
 
 	protected DistributedEntity(User creator, DistributedEntity parent) {
 		createDate = new Date();
 		this.creator = creator;
 		this.parent = parent;
 	}
 
 	@Id
 	@Column(length = 64, nullable = false, updatable = false)
 	public String getHash() {
 		byte[] hashBytes = getHashBytes();
 		char[] chars = new char[2 * hashBytes.length];
 		for (int i = 0; i < hashBytes.length; i++) {
 			chars[2 * i] = HEX_CHARS[(hashBytes[i] & 0xF0) >>> 4];
 			chars[2 * i + 1] = HEX_CHARS[hashBytes[i] & 0x0F];
 		}
 		return new String(chars);
 	}
 
	@Transient
 	protected final byte[] getHashBytes() {
 		String prefix = getClass().getCanonicalName();
 		byte[] prefixBytes = prefix.getBytes(Charsets.UTF_8);
 		byte[] payload = getBytes();
 		byte[] data = merge(prefixBytes, payload);
 		return digest.digest(data);
 	}
 
 	protected void setHash(String hash) {
 		checkArgument(HEX_REGEX.matcher(hash).matches());
 		storedHash = hash;
 	}
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(nullable = false, updatable = false)
 	public Date getCreateDate() {
 		return new Date(createDate.getTime());
 	}
 
 	protected void setCreateDate(Date createDate) {
 		checkNotNull(createDate);
 		checkArgument(createDate.compareTo(new Date()) <= 0);
 		this.createDate = new Date(createDate.getTime());
 	}
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(nullable = false, updatable = false)
 	public Date getPersistDate() {
 		return new Date(persistDate.getTime());
 	}
 
 	protected void setPersistDate(Date persistDate) {
 		if (persistDate != null) {
 			checkArgument(persistDate.compareTo(new Date()) <= 0);
 			this.persistDate = new Date(persistDate.getTime());
 		} else {
 			this.persistDate = null;
 		}
 	}
 
 	@ManyToOne
 	@JoinColumn(updatable = false)
 	public User getCreator() {
 		return creator;
 	}
 
 	protected void setCreator(User creator) {
 		this.creator = creator;
 	}
 
 	@OneToOne
 	@JoinColumn(updatable = false)
 	public DistributedEntity getParent() {
 		return parent;
 	}
 
 	protected void setParent(DistributedEntity parent) {
 		this.parent = parent;
 	}
 
 	@OneToOne(mappedBy = "parent")
 	public DistributedEntity getChild() {
 		return child;
 	}
 
 	protected void setChild(DistributedEntity child) {
 		this.child = child;
 	}
 
 	@Transient
 	public DistributedEntity getCurrent() {
 		DistributedEntity parent = this;
 		DistributedEntity child = getChild();
 		while (child != null) {
 			parent = child;
 			child = parent.getChild();
 		}
 		return parent;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * 
 	 * {@code DistributedEntity} adds two additional requirements to the
 	 * contract inherited from {@code HasBytes}.
 	 * <p>
 	 * 
 	 * The first additional requirement is that the result of this method must
 	 * be consistent with {@link Object#equals(Object) equals}. For a particular
 	 * class, equal objects must generate equal byte representations, and
 	 * unequal objects must generate unequal byte representations, to a
 	 * reasonable extent. (This is stronger than the requirement for
 	 * {@link Object#hashCode() hashCode}.)
 	 * <p>
 	 * 
 	 * The second additional requirement is that the result of this method be
 	 * fully determined upon deserialization. In practice, this applies mainly
 	 * to JPA entity relationships declared by the class. The owning side of the
 	 * relationship will usually take responsibility for serialization, so the
 	 * unowned side declares its corresponding instance field as
 	 * {@code transient}. If this is the case, the entity with the unowned side
 	 * may not rely on the relationship in determining the result of
 	 * {@code getBytes()}.
 	 * <p>
 	 * 
 	 * In general, subclass implementations should include only the state of
 	 * that subclass in the result of this method. The identifier column for
 	 * {@code DistributedEntity} becomes much less useful if properties such as
 	 * the creator or creation date are used to determine the hash.
 	 */
 	@Transient
 	@Override
 	public abstract byte[] getBytes();
 
 	@PrePersist
 	@SuppressWarnings("unused")
 	private void onPersist() {
 		persistDate = new Date();
 	}
 
 	/*
 	 * This can't be done in the setter methods, because JPA doesn't guarantee
 	 * the order of setter invocation.
 	 */
 	@PostLoad
 	private void validate() {
 		if (persistDate != null) {
 			checkState(createDate.compareTo(persistDate) <= 0);
 		}
 		if (storedHash != null) {
 			checkState(getHash().equals(storedHash));
 			storedHash = null;
 		}
 	}
 
 	private void readObject(ObjectInputStream ois) throws IOException,
 			ClassNotFoundException {
 		ois.defaultReadObject();
 		setCreateDate(createDate);
 		setPersistDate(persistDate);
 		validate();
 	}
 
 	/*
 	 * See Effective Java, second edition, item 74.
 	 */
 	@SuppressWarnings("unused")
 	private void readObjectNoData() throws ObjectStreamException {
 		throw new InvalidObjectException("stream data required");
 	}
 }

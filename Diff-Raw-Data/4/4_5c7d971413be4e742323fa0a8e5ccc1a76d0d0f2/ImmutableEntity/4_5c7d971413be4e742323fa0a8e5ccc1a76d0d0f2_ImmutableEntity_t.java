 /*
  * ImmutableEntity.java
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
 import java.util.Date;
 import java.util.regex.Pattern;
 
 import javax.persistence.Column;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.PostLoad;
 import javax.persistence.PrePersist;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  * Abstract base class for entities which cannot be modified after creation.
  * Subclasses should not declare any {@code public} mutator methods, though
  * {@code protected} mutators for persistent properties are required by the JPA
  * spec. Concrete subclasses should provide one or more {@code public}
  * constructors which fully initialize the entity's state, particularly any
  * state which determines the result of {@link #toBytes()}. A no-argument
  * constructor must also be provided to comply with the JPA spec; however, it
  * may be marked {@code protected} to prevent client use.
  * 
  * @author Meyer Kizner
  * 
  */
 @MappedSuperclass
 abstract class ImmutableEntity extends Hashable implements Serializable {
 	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
 
 	private static final Pattern HEX_REGEX = Pattern.compile("[0-9a-f]*");
 
 	private Date createDate;
 
 	private transient Date persistDate;
 
 	private transient String storedHash;
 
 	protected ImmutableEntity() {
 		createDate = new Date();
 	}
 
 	@Id
 	@Column(nullable = false, updatable = false)
 	public String getHash() {
 		byte[] hashBytes = getHashBytes();
 		char[] chars = new char[2 * hashBytes.length];
 		for (int i = 0; i < hashBytes.length; i++) {
 			chars[2 * i] = HEX_CHARS[(hashBytes[i] & 0xF0) >>> 4];
 			chars[2 * i + 1] = HEX_CHARS[hashBytes[i] & 0x0F];
 		}
 		return new String(chars);
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
 	@SuppressWarnings("unused")
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
 		if (createDate == null) {
 			throw new InvalidObjectException("null create date");
		} else if (createDate.compareTo(new Date()) > 0) {
			throw new InvalidObjectException("create date is after present");
 		}
		createDate = new Date(createDate.getTime());
 	}
 
 	/*
 	 * See Effective Java, second edition, item 74.
 	 */
 	@SuppressWarnings("unused")
 	private void readObjectNoData() throws ObjectStreamException {
 		throw new InvalidObjectException("stream data required");
 	}
 }

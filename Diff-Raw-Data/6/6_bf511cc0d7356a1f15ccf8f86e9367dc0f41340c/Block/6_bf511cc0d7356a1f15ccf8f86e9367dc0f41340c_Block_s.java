 package edu.berkeley.grippus.storage;
 
 import java.io.Serializable;
 import java.util.Arrays;
 
 public class Block implements Serializable {
 	private static final long serialVersionUID = 1L;
 	final byte[] digest;
 	final int length;
 
 	public Block(byte[] digest, int length) {
 		this.digest = Arrays.copyOf(digest, digest.length);
 		this.length = length;
 	}
 }

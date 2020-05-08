 package edu.berkeley.grippus.storage;
 
 import java.io.Serializable;
 import java.util.Arrays;
 
 public class Block implements Serializable {
 	private static final long serialVersionUID = 1L;
 	final byte[] digest;
 	final int length;
 
	@SuppressWarnings("unused") // for Hessian
	private Block() {
		digest = null;
		length = -1;
	}

 	public Block(byte[] digest, int length) {
 		this.digest = Arrays.copyOf(digest, digest.length);
 		this.length = length;
 	}
 }

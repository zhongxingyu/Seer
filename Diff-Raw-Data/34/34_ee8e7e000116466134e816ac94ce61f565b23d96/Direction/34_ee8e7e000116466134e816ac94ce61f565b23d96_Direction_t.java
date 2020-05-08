package com.bzn.codestory.elevator;

public enum Direction{
	UP, DOWN, NIL; // Lisp Hommage

	public boolean isUp() {
		return this == UP;
	}

	public boolean isDown() {
		return this == DOWN;
	}

	public boolean isNil() {
		return this == NIL;
	}
}
 

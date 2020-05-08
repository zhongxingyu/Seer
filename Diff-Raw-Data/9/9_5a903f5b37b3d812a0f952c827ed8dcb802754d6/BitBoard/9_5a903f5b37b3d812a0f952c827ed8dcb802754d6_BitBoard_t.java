 package com.chroneus.atarigo;
 
 /**
  * this class looks like two-dimension BitSet with constant size
  */
 public class BitBoard implements Cloneable {
 	long a0 = 0, a1 = 0;// max 128 bit
 	// long a2,a3,a4,a5; // for 19*19
 	 byte xsize = 9, ysize = 9;
 	static final long mask=Long.parseLong("11111111111111111", 2);
 
 	public BitBoard(byte x, byte y) {
 		setXsize(x);
 		setYsize(y);
 	}
 
 	public BitBoard() {
 	}
 
 	public BitBoard(int size) {
 		this((byte) Math.sqrt(size), (byte) Math.sqrt(size));
 	}
 	public BitBoard(int[] initial) {
 		this(BoardConstant.SIZE,BoardConstant.SIZE);
 		for (int i = 0; i < initial.length; i++) {
 			set(initial[i]);
 		}
 	}
 	public BitBoard(int i, int j) {
 		this((byte) i, (byte) j);
 	}
 
 	public void set(byte x, byte y) {
 		set(x * ysize + y);
 	}
 
 	public void set(int x, int y) {
 		set(x * ysize + y);
 	}
 
 	public boolean get(byte x, byte y) {
 		return get(x * ysize + y);
 	}
 
 	public void flip(int bit) {
 		if (bit < 64)
 			a0 ^= 1L << bit;
 		else
 			a1 ^= 1L << (bit - 64);
 	}
 
 	public void set(int bit) {
 		if (bit < 64)
 			a0 |= 1L << bit;
 		else
 			a1 |= 1L << (bit - 64);
 	}
 
 	public boolean get(int bit) {
 		if (bit < 64)
 			return ((a0 & (1L << bit)) != 0);
 		else
 			return ((a1 & (1L << (bit - 64))) != 0);
 	}
 
 	public void clear(int bit) {
 		if (bit < 64)
 			a0 &= ~(1L << bit);
 		else
 			a1 &= ~(1L << (bit - 64));
 	}
 
 	public void clear() {
 		byte x = getXsize();
 		byte y = getYsize();
 		a0 = 0;
 		a1 = 0;
 		setXsize(x);
 		setYsize(y);
 	}
 
 	public byte getXsize() {
 		// return (byte) ((a1 >> (48)) & 0xff);
 		return xsize;
 	}
 
 	public byte getYsize() {
 		// return (byte) ((a1 >> (56)) & 0xff);
 		return ysize;
 	}
 
 	public void setXsize(byte x) {
 		// a1|= (0xF0FFFFFF & x);
 		xsize = x;
 	}
 
 	public void setYsize(byte y) {
 		// a1|= (0x0FFFFFFF & y);
 		ysize = y;
 	}
 
 	@Override
 	protected Object clone() {
 		BitBoard bitboard = new BitBoard();
 		bitboard.a0 = a0;
 		bitboard.a1 = a1;
 		bitboard.xsize = xsize;
 		bitboard.ysize = ysize;
 		return bitboard;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof BitBoard) {
 			if (this.ysize == ((BitBoard) obj).ysize && this.a0 == ((BitBoard) obj).a0
 					&& this.a1 == ((BitBoard) obj).a1) return true;
 		}
 		return false;
 	}
 
 	public BitBoard mirror() {
 		BitBoard bitBoard = new BitBoard(xsize, ysize);
 		for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i + 1)) {
 			bitBoard.set(i / ysize, ysize - 1 - i % ysize);
 		}
 		return bitBoard;
 	}
 
 	/**
 	 * convert (x,y) to (SIZEY-1-y,x)
 	 * 
 	 */
 	public BitBoard rotate90() {
 		BitBoard bitBoard = new BitBoard(ysize, xsize);
 
 		for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i + 1)) {
 			bitBoard.set(ysize - 1 - i % ysize, i / ysize);
 		}
 		return bitBoard;
 	}
 
 	public BitBoard moveXY(byte x, byte y, byte wrap_size_x, byte wrap_size_y) throws Exception {
 
 		BitBoard bitBoard = new BitBoard(wrap_size_x, wrap_size_y);
 		for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i + 1)) {
 			int x0 = i / ysize, y0 = i % ysize;
 			int x1 = x0 + x;
 			int y1 = y0 + y;
 			if (x1 >= wrap_size_x || y1 >= wrap_size_y) throw new Exception("move pattern is out of result");
 			bitBoard.set(wrap_size_y * (x0 + x) + y0 + y);
 		}
 		return bitBoard;
 	}
 
 	// TODO Zobrist hashing
 	public long hash() {
 		return 0L;
 	}
 
 	@Override
 	public int hashCode() {
 		return (int) (a0 * a1 * xsize * ysize) >>> 32;
 	}
 	
 
 	
 	public int nextSetBit(int bit) {
 		if (bit < 64) {
 			int trail_a0 = Long.numberOfTrailingZeros(a0 & (-1L << bit));
 			if (trail_a0 < 64)
 				return trail_a0;
 			else {
 				int trail_a1 = Long.numberOfTrailingZeros(a1);
 				if (trail_a1 < 64)
 					return trail_a1 + 64;
 				else
 					return -1;
 			}
 		}
 		else {
 			int trail_a1 = Long.numberOfTrailingZeros(a1 & (-1L << (bit - 64)));
 			if (trail_a1 < 17)
 				return trail_a1 + 64;
 			else
 				return -1;
 		}
 	}
 
 	public void not() {
 		a0 = ~a0;
 		a1 = ~a1 &mask | (a1&~mask);
 		
 	}
 
 	public void andNot(BitBoard moved_transform) {
 		a0 &= ~moved_transform.a0;
 		a1 &= (~moved_transform.a1&mask)|(a1&~mask);
 		
 	}
 
 	public boolean isEmpty() {
 		return a0 == 0 && (a1&mask) == 0;
 	}
 
 	public int cardinality() {
 		return Long.bitCount(a0) + Long.bitCount(a1&mask);
 	}
 
 	public int size() {
 		return getXsize() * getYsize();
 	}
 
 	/**
 	 * 
 	 */
 	public void and(BitBoard moved_transform) {
 		a0 &= moved_transform.a0;
 		a1 &= moved_transform.a1;
 	}
 
 	public void or(BitBoard moved_transform) {
 		a0 |= moved_transform.a0;
 		a1 |= moved_transform.a1;
 	}
 
 	public void flipInRange(int i, int j) {
 		// slow stub
 		for (int bit = i; bit <= j; bit++) {
 			flip(bit);
 		}
 	}
 
 	@Override
 	public String toString() {
 		char[][] pseudoGraphics = new char[xsize][ysize];
 		for (byte x = 0; x < xsize; x++) {
 			for (byte y = 0; y < ysize; y++) {
 				if (get(x, y))
 					pseudoGraphics[x][y] = '*';
 				else
 					pseudoGraphics[x][y] = '·';
 			}
 		}
 
 		StringBuffer out = new StringBuffer();
 		for (byte x = 0; x < xsize; x++) {
 			for (byte y = 0; y < ysize; y++) {
 				out.append(" " + pseudoGraphics[x][y]);
 			}
 			out.append("\n");
 		}
 
 		return out.toString();
 	}
 
 	/*
 	 * also use it for influence
 	 */
 	public  BitBoard nearest_stones() {
 		BitBoard firstresult=this.get_left_nearest_stones();
 		firstresult.or(this.get_right_nearest_stones());
 		firstresult.or(this.get_top_nearest_stones());
		firstresult.or(this.get_bottom_nearest_stones());
 		firstresult.andNot(this);
 		return firstresult;
 	}
 	public  BitBoard diagonal_nearest_stones() {
 		BitBoard firstresult=this.get_left_nearest_stones().get_top_nearest_stones();
 		firstresult.or(this.get_left_nearest_stones().get_bottom_nearest_stones());
 		firstresult.or(this.get_right_nearest_stones().get_top_nearest_stones());
 		firstresult.or(this.get_right_nearest_stones().get_bottom_nearest_stones());
 		firstresult.andNot(this);
 		return firstresult;
 	}
 	
 	
 	public BitBoard get_left_nearest_stones(){
 		BitBoard leftresult=(BitBoard) this.clone();
 		leftresult.andNot(BoardConstant.LeftBorder);
 		leftresult.a0>>>=1;
 		leftresult.a0 |= ((leftresult.a1&1L) << 63);
 		leftresult.a1=((leftresult.a1>>>1)&mask)|(leftresult.a1&~mask);	
 		return leftresult;
 	}
 	public BitBoard get_right_nearest_stones(){
 		BitBoard rightresult=(BitBoard) this.clone();
 		rightresult.andNot(BoardConstant.RightBorder);
 		rightresult.a1=((rightresult.a1<<1)&mask)|(rightresult.a1&~mask);
 		rightresult.a1|=((rightresult.a0&-1L)>>>63);
 		rightresult.a0<<=1;
 		return rightresult;
 	}
 	public BitBoard get_top_nearest_stones(){
 		BitBoard topresult=(BitBoard) this.clone();
 		topresult.andNot(BoardConstant.TopBorder);
 		topresult.a0>>>=9;
 		topresult.a0 |=( (topresult.a1&511L) << 55);
 		topresult.a1=((topresult.a1>>>9)&mask)|(topresult.a1&~mask);
 		return topresult;
 	}
 	public BitBoard get_bottom_nearest_stones(){
 		BitBoard bottomresult=(BitBoard) this.clone();
 		bottomresult.andNot(BoardConstant.BottomBorder);
 		bottomresult.a1=((bottomresult.a1<<9)&mask)|(bottomresult.a1&~mask);
 		bottomresult.a1|=((bottomresult.a0&-1L)>>>55);
 		bottomresult.a0<<=9;
 		return bottomresult;
 	}
 	
 
 
 }

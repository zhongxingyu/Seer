 package model.algorithms;
 
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Observable;
 import java.util.Observer;
 
 import model.Event;
 import model.Result;
 import model.ShiftVector;
 import model.Event.EventType;
 import model.algorithms.utils.QuickSort;
 
 public class RobustMatchPartialDCT extends RobustMatch implements Observer {
 	private static final int BLOCK_X_OFFS = 256;
 	private static final int BLOCK_Y_OFFS = 257;
 	private static final int BLOCK_FLAG_OFFS = 258;
 	boolean secondStep = false;
 
 	@Override
 	public void update(Observable arg0, Object o) {
 		if (o instanceof Float) {
 			setChanged();
 			float progress;
 
 			if (!secondStep) {
 				progress = (Float) o / 2.0f;
 			} else {
 				progress = 0.5f + (Float) o;
 			}
 			notifyObservers(new Event(EventType.PROGRESS, new Result(progress)));
 		}
 	}
 
 	@Override
 	public void detect(BufferedImage input, float quality, int threshold,
 			int threads) {
 
 		/* Check if image is at least 16x16 in size */
 		if (!checkImage(input)) {
 			setChanged();
 			notifyObservers(new Event(Event.EventType.ERROR,
 					"The image could not be processed!"));
 			return;
 		}
 
 		width = input.getWidth();
 		height = input.getHeight();
 		long start = System.currentTimeMillis();
 		takeTime();
 
 		/* Compute grayscale values */
 		final int[][] grayscale = getGrayscale(input);
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"Luminance matrix of image calculated in " + takeTime() + "ms"));
 
 		/*
 		 * Pre-compute values needed for DCT computation and update the
 		 * quantization matrix according to quality
 		 */
 		final float[][][][] constants = preComputeConstants(quality);
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"Precomputed constant values in " + takeTime() + "ms"));
 
 		/* Calculate 4x4 DCTs of each block */
 		short[][] blocks = new short[(width - 15) * (height - 15)][259];
 
 		/* Flag blocks */
 		for (int i = 0; i < blocks.length; i++) {
 			blocks[i][BLOCK_X_OFFS] = (short) (i % (width - 15));
 			blocks[i][BLOCK_Y_OFFS] = (short) (i / (width - 15));
 			blocks[i][BLOCK_FLAG_OFFS] = 0;
 		}
 
 		Thread[] tA = new Thread[threads];
 
 		for (int t = 0; t < threads; t++) {
 			Worker w = new Worker(t, threads, width, height, grayscale,
 					constants, blocks, 0, 4, false);
 			w.addObserver(this);
 			Thread th = new Thread(w);
 			tA[t] = th;
 			th.start();
 		}
 
 		/* Barrier */
 		for (int t = 0; t < threads; t++) {
 			try {
 				tA[t].join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"4x4 DCTs of each block were calculated in " + takeTime()
 						+ "ms"));
 
 		/*
 		 * Sort the 4x4 DCTs of each block.
 		 */
 		int[] compareMask = { 0, 1, 2, 3, 16, 17, 18, 19, 32, 33, 34, 35, 48,
 				49, 50, 51 };
 
 		
 		//testSort(blocks);
 		//QuickSort.sort(blocks, compareMask);
 		Arrays.sort(blocks,new BlockComparator(compareMask));
 		
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"Lexicographically sorted all 4x4 DCTs in " + takeTime() + "ms"));
 
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"Number of DCT blocks: " + blocks.length));
 
 		/* Mark unique elements */
 		for (int i = 0; i < blocks.length - 1; i++) {
 			if (compareDCT(blocks[i], blocks[i + 1], compareMask) == 0) {
 				blocks[i][BLOCK_FLAG_OFFS] = 1;
 				blocks[i + 1][BLOCK_FLAG_OFFS] = 1;
 			}
 
 			blocks[i][BLOCK_FLAG_OFFS] = (short) (1 - blocks[i][BLOCK_FLAG_OFFS]);
 		}
 
 		int c = 0;
 		for (int i = 0; i < blocks.length - 1; i++) {
 			if (blocks[i][BLOCK_FLAG_OFFS] == 1)
 				c++;
 		}
 
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"Number of unique DCT blocks (" + c + ") computed in "
 						+ takeTime() + " ms"));
		
 		/* Required for progress notifications. */
 		secondStep = true;
 
 		/* Calculate 16x16 DCTs of non-unique blocks */
 		for (int t = 0; t < threads; t++) {
 			Worker w = new Worker(t, threads, width, height, grayscale,
 					constants, blocks, 4, 16, true);
 			w.addObserver(this);
 			Thread th = new Thread(w);
 			tA[t] = th;
 			th.start();
 		}
 
 		/* Barrier */
 		for (int t = 0; t < threads; t++) {
 			try {
 				tA[t].join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"16x16 DCTs of non-unique blocks were calculated in "
 						+ takeTime() + "ms"));
 
 		/* Sort the 16x16 DCTs of each block */
 		compareMask = new int[256];
 
 		for (int i = 0; i < 256; i++)
 			compareMask[i] = i;
 
 		//testSort(blocks);
 		//QuickSort.sort(blocks, compareMask);
 		Arrays.sort(blocks,new BlockComparator(compareMask));
 		
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"Lexicographically sorted all non-unique 16x16 DCTs in "
 						+ takeTime() + "ms"));
 
 		// for (int i = 0; i < blocks.length; i++) {
 		// for (int j = 0; j < 258; j++) {
 		// System.out.print(blocks[i][j]);
 		// }
 		// System.out.println();
 		// }
 
 		/*
 		 * Collect vectors... The Shift Vector is an array double of the
 		 * original image size. The position in the array identifies the vector
 		 * itself, the value at a given position represents the count of the
 		 * shift vector.
 		 */
 		int shiftVectors[][] = new int[height * 2][width];
 
 		for (int i = 0; i < blocks.length - 1; i++) {
 			short[] a = blocks[i];
 			short[] b = blocks[i + 1];
 
 			if (blocks[i][BLOCK_FLAG_OFFS] == 0 && compareDCT(a, b) == 0) {
 				int sx = (int) (a[BLOCK_X_OFFS] - b[BLOCK_X_OFFS]);
 				int sy = (int) (a[BLOCK_Y_OFFS] - b[BLOCK_Y_OFFS]);
 
 				if (sx < 0) {
 					sx = -sx;
 					sy = -sy;
 				}
 
 				sy += height;
 				shiftVectors[sy][sx]++;
 			}
 		}
 
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"Calculated shift vectors in " + takeTime() + "ms"));
 
 		Event event = new Event(EventType.COPY_MOVE_DETECTION_FINISHED,
 				new Result(System.currentTimeMillis() - start));
 
 		/*
 		 * Collect shiftvectors
 		 */
 		for (int i = 0; i < blocks.length - 1; i++) {
 			short[] a = blocks[i];
 			short[] b = blocks[i + 1];
 
 			if (blocks[i][BLOCK_FLAG_OFFS] == 0 && compareDCT(a, b) == 0) {
 				int aBy = (int) a[BLOCK_Y_OFFS], aBx = (int) a[BLOCK_X_OFFS];
 				int bBy = (int) b[BLOCK_Y_OFFS], bBx = (int) b[BLOCK_X_OFFS];
 				int sx = aBx - bBx;
 				int sy = aBy - bBy;
 
 				if (sx < 0) {
 					sx = -sx;
 					sy = -sy;
 				}
 
 				sy += height;
 
 				if (shiftVectors[sy][sx] > threshold) {
 					event.getResult()
 							.addShiftVector(
 									new ShiftVector(aBx, aBy, bBx - aBx, bBy
 											- aBy, 16));
 				}
 			}
 		}
 
 		setChanged();
 		notifyObservers(event);
 	}
 
 	private class Worker extends Observable implements Runnable {
 		private final int num, threads, width, height, grayscale[][], dctStart,
 				dctEnd;
 		private final float[][][][] constants;
 		private short[][] blocks;
 		private boolean complete;
 
 		public Worker(final int num, final int threads, final int width,
 				final int height, final int grayscale[][],
 				final float[][][][] constants, short[][] blocks,
 				final int dctStart, final int dctEnd, final boolean complete) {
 			this.num = num;
 			this.threads = threads;
 			this.width = width;
 			this.height = height;
 			this.grayscale = grayscale;
 			this.constants = constants;
 			this.dctStart = dctStart;
 			this.dctEnd = dctEnd;
 			this.blocks = blocks;
 			this.complete = complete;
 		}
 
 		@Override
 		public void run() {
 
 			for (int xx = num; xx < height - 15 && !abort; xx += threads) {
 				for (int yy = 0; yy < width - 15; yy++) {
 					int idx = xx * (width - 15) + yy;
 
 					if (blocks[idx][BLOCK_FLAG_OFFS] == 0) {
 						/*
 						 * The dct matrix is divided into four parts. The value
 						 * of dctStart divides the matrix horizontally as well
 						 * as vertically.
 						 */
 						if (!this.complete) {
 							for (int u = dctStart; u < dctEnd; u++) {
 								for (int v = dctStart; v < dctEnd; v++) {
 
 									float f = 0;
 									for (int i = 0; i < 16; i++) {
 										for (int j = 0; j < 16; j++) {
 											f += grayscale[(xx + i)][yy + j]
 													* constants[u][v][i][j];
 										}
 									}
 
 									blocks[idx][u * 16 + v] = (short) Math
 											.round(f / QUANT[u][v]);
 								}
 							}
 						} else {
 
 							/*
 							 * Upper right part...
 							 */
 							for (int u = 0; u < dctStart; u++) {
 								for (int v = dctStart; v < dctEnd; v++) {
 
 									float f = 0;
 									for (int i = 0; i < 16; i++) {
 										for (int j = 0; j < 16; j++) {
 											f += grayscale[(xx + i)][yy + j]
 													* constants[u][v][i][j];
 										}
 									}
 
 									blocks[idx][u * 16 + v] = (short) Math
 											.round(f / QUANT[u][v]);
 								}
 							}
 
 							/*
 							 * Lower part (left and right together)...
 							 */
 							for (int u = dctStart; u < dctEnd; u++) {
 								for (int v = 0; v < dctEnd; v++) {
 
 									float f = 0;
 									for (int i = 0; i < 16; i++) {
 										for (int j = 0; j < 16; j++) {
 											f += grayscale[(xx + i)][yy + j]
 													* constants[u][v][i][j];
 										}
 									}
 
 									blocks[idx][u * 16 + v] = (short) Math
 											.round(f / QUANT[u][v]);
 								}
 							}
 						}
 					}
 				}
 
				if (num == threads-1 && xx % num == 0) {
 					setChanged();
 					notifyObservers((float) xx / (float) (height - 15));
 				}
 			}
 
 		}
 	}
 
 	private int compareDCT(short[] a, short[] b, int[] compareMask) {
 		for (int i : compareMask) {
 			if (a[i] < b[i]) {
 				return -1;
 			} else if (a[i] > b[i]) {
 				return 1;
 			}
 		}
 
 		return 0;
 	}
 
 	private int compareDCT(final short[] a, final short[] b) {
 		for (int i = 0; i < 256; i++) {
 			if (a[i] < b[i]) {
 				return -1;
 			} else if (a[i] > b[i]) {
 				return 1;
 			}
 		}
 
 		return 0;
 	}
 
 	private void bubblesortBlocks(short[][] blocks) {
 		int n = blocks.length;
 		boolean change = false;
 
 		do {
 
 			change = false;
 			for (int i = 0; i < n - 1; i++) {
 				if (compareDCT(blocks[i], blocks[i + 1]) > 0) {
 					change = true;
 					short[] tmp = blocks[i];
 					blocks[i] = blocks[i + 1];
 					blocks[i + 1] = tmp;
 				}
 			}
 
 			n--;
 		} while (n > 1 && change);
 	}
 	
 	private void testSort(short[][] blocks) {
 		ArrayList<ShortBlock> test = new ArrayList<RobustMatchPartialDCT.ShortBlock>();
 		for (int i = 0; i < blocks.length; i++) {
 			test.add(i, new ShortBlock(blocks[i]));
 		}
 		Collections.sort(test);
 		for (int i = 0; i < blocks.length; i++) {
 			blocks[i] = test.get(i).getBlock();
 		}
 	}
 
 	private class ShortBlock implements Comparable<ShortBlock> {
 		private short[] block;
 
 		public ShortBlock(short[] block) {
 			this.block = block;
 		}
 
 		@Override
 		public int compareTo(ShortBlock o) {
 			short[] b = o.getBlock();
 
 			for (int i = 0; i < 256; i++) {
 				if (block[i] < b[i])
 					return -1;
 				if (block[i] > b[i])
 					return 1;
 			}
 
 			return 0;
 		}
 
 		public short[] getBlock() {
 			return block;
 		}
 
 	}
 	
 	private class BlockComparator implements Comparator<short[]> {
 		private int[] mask;
 		
 		public BlockComparator(int[] mask) {
 			this.mask = mask;
 		}
 		@Override
 		public int compare(short[] a, short[] b) {
 			
 			for (int i : mask) {
 				if (a[i] < b[i])
 					return -1;
 				else if (a[i] > b[i])
 					return 1;
 			}
 			
 			return 0;
 		}
 		
 	}
 }

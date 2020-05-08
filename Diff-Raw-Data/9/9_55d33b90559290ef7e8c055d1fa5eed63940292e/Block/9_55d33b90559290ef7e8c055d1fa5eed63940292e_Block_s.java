 /**
  * Block.java   single Tetris block description
  */
 
 package org.example.tetris;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class Block {
 
 	public enum BlockColor {
 		RED(0x990000, (byte) 2), GREEN(0x009900, (byte) 3), BLUE(0x000099,
 				(byte) 4), YELLOW(0xffcc33, (byte) 5), CYAN(0x339966, (byte) 6);
 		private final Color color;
 		private final byte value;
 
 		private BlockColor(int color, byte value) {
 			this.color = new Color(color);
 			this.value = value;
 		}
 	}
 
 	// cell status values (outer):
 	public static final byte CELL_EMPTY = 0;
 	public static final byte CELL_DYNAMIC = 1;
 
 	private static Random random = new Random();
 
 	// current block state:
 	private final int shape;
 	private int frame = 0;
 	private final BlockColor color;
 
 	private Point topLeft = new Point(Model.NUM_COLS >> 1, 0);
 
 	public int getFrame() {
 		return frame;
 	}
 
 	public Color getColor() {
 		return color.color;
 	}
 
 	public byte getStatusValue() {
 		return color.value;
 	}
 
 	public int getNextFrame(int frame) {
 		if (frame + 1 >= getFramesCount()) {
 			return 0;
 		}
 		return frame + 1;
 	}
 
 	public static Color getColorForStaticValue(byte b) {
 		for (BlockColor item : BlockColor.values()) {
 			if (b == item.value) {
 				return item.color;
 			}
 		}
 		return null;
 	}
 
 	public final void setState(int frame, Point topLeft) {
 		this.frame = frame;
 		this.topLeft = topLeft;
 	}
 
 	private final int getFramesCount() {
 		return getCurrentShape().frameCount;
 	}
 
 	public final byte[][] getShape(int nFrame) {
 		return getCurrentShape().getFrame(nFrame).get();
 	}
 
 	public Point getTopLeft() {
 		return this.topLeft;
 	}
 
 	public final int getShapeWidth(int nFrame) {
 		return getCurrentShape().getFrame(nFrame).width;
 	}
 
 	public final static synchronized Block createBlock() {
 		return new Block(randomShapeIndex(), randomBlockColor());
 	}
 
 	private static int randomShapeIndex() {
 		return random.nextInt(Shape.values().length);
 	}
 
 	private static BlockColor randomBlockColor() {
 		return BlockColor.values()[random.nextInt(BlockColor.values().length)];
 	}
 
 	private final Shape getCurrentShape() {
 		return Shape.values()[shape];
 	}
 
 	private Block(int shape, BlockColor blockColor) {
 		this.shape = shape;
 		this.color = blockColor;
 	}
 
 	private enum Shape {
 		S1(1) {
 			@Override
 			public Frame getFrame(int n) {
 				return new Frame(2).add("11").add("11");
 			}
 
 		},
 		S2(2) {
 
 			@Override
 			public Frame getFrame(int n) {
 				switch (n) {
 				case 0:
 					return new Frame(4).add("1111");
 				case 1:
 					return new Frame(1).add("1").add("1").add("1").add("1");
 				}
 				throw new IllegalArgumentException("Invalid frame number: " + n);
 			}
 		},
 		S3(4) {
 			@Override
 			public Frame getFrame(int n) {
 				switch (n) {
 				case 0:
 					return new Frame(3).add("010").add("111");
 				case 1:
 					return new Frame(2).add("10").add("11").add("10");
 				case 2:
 					return new Frame(3).add("111").add("010");
 				case 3:
 					return new Frame(2).add("01").add("11").add("01");
 				}
 				throw new IllegalArgumentException("Invalid frame number: " + n);
 			}
 		},
 		S4(4) {
 			@Override
 			public Frame getFrame(int n) {
 				switch (n) {
 				case 0:
 					return new Frame(3).add("100").add("111");
 				case 1:
 					return new Frame(2).add("11").add("10").add("10");
 				case 2:
 					return new Frame(3).add("111").add("001");
 				case 3:
 					return new Frame(2).add("01").add("01").add("11");
 				}
 				throw new IllegalArgumentException("Invalid frame number: " + n);
 			}
 		},
 		S5(4) {
 			@Override
 			public Frame getFrame(int n) {
 				switch (n) {
 				case 0:
 					return new Frame(3).add("001").add("111");
 				case 1:
 					return new Frame(2).add("10").add("10").add("11");
 				case 2:
 					return new Frame(3).add("111").add("100");
 				case 3:
					return new Frame(2).add("11").add("10").add("10");
 				}
 				throw new IllegalArgumentException("Invalid frame number: " + n);
 			}
 		},
 		S6(2) {
 			@Override
 			public Frame getFrame(int n) {
 				switch (n) {
 				case 0:
 					return new Frame(3).add("110").add("011");
 				case 1:
 					return new Frame(2).add("01").add("11").add("10");
 				}
 				throw new IllegalArgumentException("Invalid frame number: " + n);
 			}
 		},
 		S7(2) {
 			@Override
 			public Frame getFrame(int n) {
 				switch (n) {
 				case 0:
 					return new Frame(3).add("011").add("110");
 				case 1:
 					return new Frame(2).add("10").add("11").add("01");
 
 				}
 				throw new IllegalArgumentException("Invalid frame number: " + n);
 			}
 		};
 		private final int frameCount;
 
 		private Shape(int frameCount) {
 			this.frameCount = frameCount;
 		}
 
 		public abstract Frame getFrame(int n);
 	}
 
 	private static class Frame {
 		private final int width;
 
 		private Frame(int width) {
 			this.width = width;
 		}
 
 		private final List<byte[]> data = new ArrayList<byte[]>(4);
 
 		private Frame add(String rowStr) {
 			byte[] row = new byte[rowStr.length()];
 			for (int i = 0; i < rowStr.length(); i++) {
 				row[i] = Byte.valueOf("" + rowStr.charAt(i));
 			}
 			data.add(row);
 			return this;
 		}
 
 		private byte[][] get() {
 			byte[][] result = new byte[data.size()][];
 			return data.toArray(result);
 		}
 	}
 }

 package net.ian.dcpu;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import net.ian.dcpu.DCPU.Register;
 
 public class Monitor extends Hardware {
 	public static final int ID = 0x7349f615;
 	public static final int VERSION = 0x1802;
 	public static final int MANUFACTURER = 0x1c6c8b36;
 	
 	public static final int COLUMNS = 32;
 	public static final int ROWS = 12;
 	
 	public static final int CHAR_WIDTH = 4;
 	public static final int CHAR_HEIGHT = 8;
 	
 	public static final int BORDER = 12;	
 	public static final int WIDTH = COLUMNS * CHAR_WIDTH + BORDER * 2;
 	public static final int HEIGHT = ROWS * CHAR_HEIGHT + BORDER * 2;
 	
 	public BufferedImage screen;
 	
 	public BufferedImage font[];
 	public MonitorCell cells[];
 	
 	public Color borderColor = Color.BLACK;
 		
 	private DCPU cpu;
 	private char memStart, fontStart;
 
 	public boolean shouldRender;
 	
 	public static class MonitorCell {
 		char character;
 		Color fgColor, bgColor;
 		boolean blink, show;
 		
 		public MonitorCell(char c, Color fg, Color bg, boolean blink) {
 			character = c;
 			fgColor = fg;
 			bgColor = bg;
 			this.blink = blink;
 			show = false;
 		}
 	}
 	
 	public Monitor(DCPU cpu) {
         super(ID, VERSION, MANUFACTURER);
 		
         screen = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
         this.cpu = cpu;
         
         cells = new MonitorCell[32 * 12];
         for (int i = 0; i < 32 * 12; i++)
         	cells[i] = new MonitorCell((char)0, Color.BLACK, Color.BLACK, false);
         
 		font = loadDefaultFont();
                 
         cpu.attachDevice(this);
         
     }
 	
 	public static Color convertColor(int colorBits) {
 		boolean h = (colorBits >> 3 & 1) == 1;
 
 		int r = 0xAA * (colorBits >> 2 & 1) + (h ? 0x55 : 0);
 		int g = 0xAA * (colorBits >> 1 & 1) + (h ? 0x55 : 0);
 		int b = 0xAA * (colorBits >> 0 & 1) + (h ? 0x55 : 0);
 		
 		if ((colorBits & 0xf) == 6)
 			g -= 0x55;
 		
 		return new Color(r, g, b);
 	}
 	
 	public void buildFont(int location, char word) {
 		// Actually builds half a font, because a full font takes two words.
 		BufferedImage fontChar = font[location / 2];
 		int half = location % 2;
 		
 		for (int col = 0; col < 2; col++) {
 			for (int row = 0; row < 8; row++) {
 				int color = ((word >> ((1 - col) * 8 + row)) & 1) == 1 ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
 				fontChar.setRGB(col + (half * 2), row, color);
 			}
 		}
 	}
 	
 	public BufferedImage[] loadDefaultFont() {
 		BufferedImage img;
 		try {
 		img = ImageIO.read(DCPU.class.getResource("/net/ian/dcpu/res/font.png"));
 		} catch (IOException e){
 			e.printStackTrace();
 			return null;
 		}
 		BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
 		Graphics2D g = img2.createGraphics();
 		g.drawImage(img, 0, 0, null);
 		g.dispose();
 				
 		BufferedImage[] defaultFont = new BufferedImage[img2.getWidth() / 4 * (img2.getHeight() / 8)];
 		
 		for (int x = 0; x < img2.getWidth() / 4; x++) {
 			for (int y = 0; y < img2.getHeight() / 8; y++) {
 				defaultFont[y * 32 + x] = img2.getSubimage(x * 4, y * 8, 4, 8);
 			}
 		}
 		
 		return defaultFont;
 	}
 	
 	public void dumpFont(char start, BufferedImage[] font) {
 		// Stick a font in DCPU memory. For use with MEM_DUMP_FONT.
 		for (int i = 0; i < font.length; i++) {
 			int word = 0;
 			for (int x = 0; x < font[i].getWidth(); x++) {
 				for (int y = 0; y < font[i].getHeight(); y++) {
 					if (font[i].getRGB(x, y) == Color.WHITE.getRGB())
 						word |= 1 << ((3 - x) * 8 + y);
 					char word2 = (char)(word & 0xffff);
 					char word1 = (char)(word >> 16);
 					cpu.memory[start + (i * 2)].value = word1;
 					cpu.memory[start + (i * 2) + 1].value = word2;
 				}
 			}
 		}
 	}
 
 	private BufferedImage replaceColor(BufferedImage img, Color fgColor, Color bgColor) {
 		int fgColorRGB = fgColor.getRGB();
 		int bgColorRGB = bgColor.getRGB();
 		
 		BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
 		Graphics2D g = img2.createGraphics();
 		g.drawImage(img, 0, 0, null);
 		g.dispose();
 		
 		for (int x = 0; x < img2.getWidth(); x++) {
 			for (int y = 0; y < img2.getHeight(); y++) {
 				int rgb = img2.getRGB(x, y);
 				if (rgb == Color.BLACK.getRGB())
 					img2.setRGB(x, y, bgColorRGB);
 				else
 					img2.setRGB(x, y, fgColorRGB);
 			}
 		}
 		return img2;
 	}
 	
 	// Returns whether the screen was updated.
 	public boolean tick() {
 		if (System.currentTimeMillis() % 100 == 0) {
 			for (int x = 0; x < COLUMNS; x++) {
 				for (int y = 0; y < ROWS; y++) {
 					MonitorCell cell = cells[y * 32 + x];
 					if (cell.blink) {
 						shouldRender = true;
 						cell.show = !cell.show;
 					}
 				}
 			}
 		}
 		
 		if (shouldRender) {
 			render();
 			shouldRender = false;
 			return true;
 		}
 		return false;
 	}
 	
 	public void render() {
 		Graphics2D g = screen.createGraphics();
 		
 		g.setColor(borderColor);
 		g.fillRect(0, 0, WIDTH, HEIGHT);
 		for (int x = 0; x < COLUMNS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				MonitorCell cell = cells[y * 32 + x];
 				if (!cell.show) {
 					g.setColor(Color.BLACK);
 					g.fillRect(x * 4 + BORDER, y * 8 + BORDER, 4, 8);
 				} else if (cell.fgColor.equals(cell.bgColor)) {
 					g.setColor(cell.bgColor);
 					g.fillRect(x * 4 + BORDER, y * 8 + BORDER, 4, 8);
 				} else
 					g.drawImage(replaceColor(font[cell.character], cell.fgColor, cell.bgColor), x * 4 + BORDER, y * 8 + BORDER, null);
 			}
 		}
 		
 		g.dispose();
 	}
 	
 	@Override
 	public void onSet(char location, char value) {
		if (location < (memStart + 0x180)) {
     		MonitorCell cell = cells[location - memStart];
     		cell.character = (char)(value & 127);
     		cell.fgColor = Monitor.convertColor(value >> 12);
     		cell.bgColor = Monitor.convertColor(value >> 8);
     		cell.blink = (value >> 7 & 1) == 1;
     		cell.show = true;
		} else if (fontStart != 0 && location < (fontStart + 0x280)) {
 			// Builds half a font
 			buildFont(location - fontStart, value);
 		}
 		shouldRender = true;
 	}
 	
 	@Override
 	public boolean inMemoryRange(char loc) {
		return (loc >= memStart && loc <= memStart + 0x180) || (loc >= fontStart && loc <= fontStart + 0x280);
 	}
 	
 	public void interrupt() {
 		char b = cpu.getRegister(Register.B).value;
 		switch (cpu.getRegister(Register.A).value) {
 		case 0: // MEM_MAP_SCREEN
 			memStart = b;
 			for (int i = 0; i < 0x180; i++) {
 				char value = cpu.memory[memStart + i].value;
 				MonitorCell cell = cells[i];
 	    		cell.character = (char)(value & 127);
 	    		cell.fgColor = Monitor.convertColor(value >> 12);
 	    		cell.bgColor = Monitor.convertColor(value >> 8);
 	    		cell.blink = (value >> 7 & 1) == 1;
 	    		cell.show = true;
 			}
 			shouldRender = true;
 			break;
 		case 1: // MEM_MAP_FONT
 			fontStart = b;
 			if (b == 0)
 		        font = loadDefaultFont();
 			else {
 				for (int i = 0; i < 0x100; i++)
 					buildFont(i, cpu.memory[fontStart + i].value);
 			}
 			shouldRender = true;
 			break;
 		case 2: // MEM_MAP_PALETTE
 			// TODO!
 			break;
 		case 3: // SET_BORDER_COLOR
 			borderColor = convertColor(b);
 			shouldRender = true;
 			break;
 		case 4: // MEM_DUMP_FONT
 			this.dumpFont(b, loadDefaultFont());
 			break;
 		case 5: // MEM_DUMP_PALETTE
 			// TODO!
 			break;
 		}
 	}
 }

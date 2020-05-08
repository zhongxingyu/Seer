 package bomberman.game;
 
 import bomberman.game.character.BomberHuman;
 import bomberman.game.objects.Bomb;
 
 public class ExplosionAreaCalculator {
 
 	final private int[][] field;
 	final private int height;
 	final private int width;
 	final private int RADIUS;
 	final private int TILESIZE;
 
 	public ExplosionAreaCalculator(final int[][] field, final int RADIUS,
 			final int TILESIZE) {
 		this.field = field;
 		this.RADIUS = RADIUS;
 		this.width = field[0].length;
 		this.height = field.length;
 		this.TILESIZE = TILESIZE;
 	}
 
 	public boolean isInExplosionArea(Bomb b, BomberHuman bman) {
 		final int posX = bman.getPosX();
 		final int posY = bman.getPosY();
 		final int arrPosX = getArrayPos(posX);
 		final int arrPosY = getArrayPos(posY);
 
 		return isInExplosionArea(b, arrPosX, arrPosY);
 	}
 
 	public boolean isInExplosionArea(Bomb b, Bomb c) {
 		final int posX = c.getPosX();
 		final int posY = c.getPosY();
 		final int arrPosX = getArrayPos(posX);
 		final int arrPosY = getArrayPos(posY);
 
 		return isInExplosionArea(b, arrPosX, arrPosY);
 	}
 
 	public boolean isInExplosionArea(Bomb b, final int X, final int Y) {
 
 		final int posX = b.getPosX();
 		final int posY = b.getPosY();
 		final int arrPosX = getArrayPos(posX);
 		final int arrPosY = getArrayPos(posY);
 
 		if (X != arrPosX && Y != arrPosY)
 			return false;
 
 		for (int i = arrPosX; i <= X; i++)
 			if (field[arrPosY][i] != 0)
 				return false;
 
 		for (int i = arrPosX; i >= X; i--)
 			if (field[arrPosY][i] != 0)
 				return false;
 
 		for (int j = arrPosY; j <= Y; j++)
 			if (field[j][arrPosX] != 0)
 				return false;
 
 		for (int j = arrPosY; j >= Y; j--)
 			if (field[j][arrPosX] != 0)
 				return false;
 
 		return true;
 	}
 
 	public int getLeftBoundsOfExplosion(final int posX) {
 		if ((posX - RADIUS) < 0)
 			return 0;
 		return posX - RADIUS;
 	}
 
 	public int getRightBoundsOfExplosion(final int posX) {
 		if ((posX + RADIUS) >= width)
 			return width - 1;
 		return posX + RADIUS;
 	}
 
 	public int getUpperBoundsOfExplosion(final int posY) {
 		if ((posY + RADIUS) >= height)
 			return height - 1;
 		return posY + RADIUS;
 	}
 
 	public int getLowerBoundsOfExplosion(final int posY) {
 		if ((posY - RADIUS) < 0)
 			return 0;
 		return posY - RADIUS;
 	}
 
 	// TODO: copied from Controls.java; maybe we could refactor again?
 	public int getArrayPos(final int pos) {
 		if (pos < 0)
 			return -1;
 		return pos / TILESIZE;
 	}
 }

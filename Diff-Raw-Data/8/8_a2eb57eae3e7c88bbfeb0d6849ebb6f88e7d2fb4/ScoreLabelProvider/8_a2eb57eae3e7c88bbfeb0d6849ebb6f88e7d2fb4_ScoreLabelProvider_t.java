 /*
  * 
  */
 package org.opendarts.prototype.ui.x01.label;
 
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.forms.IFormColors;
 import org.opendarts.prototype.ProtoPlugin;
 import org.opendarts.prototype.internal.model.dart.ThreeDartsThrow;
 import org.opendarts.prototype.internal.model.dart.x01.BrokenX01DartsThrow;
 import org.opendarts.prototype.internal.model.dart.x01.WinningX01DartsThrow;
 import org.opendarts.prototype.internal.model.game.x01.DummyX01Entry;
 import org.opendarts.prototype.internal.model.game.x01.GameX01Entry;
 import org.opendarts.prototype.model.player.IPlayer;
 import org.opendarts.prototype.ui.ISharedImages;
 import org.opendarts.prototype.ui.utils.OpenDartsFormsToolkit;
 
 /**
  * The Class ScoreLabelProvider.
  */
 public class ScoreLabelProvider extends ColumnLabelProvider {
 
 	/** The player. */
 	private final IPlayer player;
 
 	private final Map<Integer, Color> colors;
 
 	private static RGB rgb60 = new RGB(0, 128, 64);
 	private static RGB rgb100 = new RGB(0, 0, 255);
 	private static RGB rgb180 = new RGB(128, 0, 128);
 
 	/**
 	 * Instantiates a new score label provider.
 	 *
 	 * @param player the player
 	 */
 	public ScoreLabelProvider(IPlayer player) {
 		super();
 		this.player = player;
 		this.colors = new HashMap<Integer, Color>();
 		this.initColors();
 	}
 
 	/**
 	 * Inits the colors.
 	 */
 	private void initColors() {
 		Display display = Display.getDefault();
 		this.initColorRange(60, 100, rgb60, rgb100);
 		this.initColorRange(100, 180, rgb100, rgb180);
 		colors.put(180, new Color(display, rgb180));
 	}
 
 	/**
 	 * Inits the color range.
 	 *
 	 * @param from the from
 	 * @param to the to
 	 * @param rgbFrom the rgb from
 	 * @param rgbTo the rgb to
 	 */
 	private void initColorRange(int from, int to, RGB rgbFrom, RGB rgbTo) {
 		double delta;
 		double redRatio;
 		double greenRatio;
 		double blueRatio;
 		delta = (double) to - from;
 
 		if (rgbTo.red == rgbFrom.red) {
 			redRatio = 0d;
 		} else {
			redRatio = (((double) rgbTo.red - rgbFrom.red) / delta);
 		}
 
 		if (rgbTo.green == rgbFrom.green) {
 			greenRatio = 0d;
 		} else {
			greenRatio = (((double) rgbTo.green - rgbFrom.green) / delta);
 		}
 
 		if (rgbTo.blue == rgbFrom.blue) {
 			blueRatio = 0d;
 		} else {
			blueRatio = (((double) rgbTo.blue - rgbFrom.blue) / delta);
 		}
 
 		int r;
 		int b;
 		int g;
 		RGB rgb;
 		for (int i = from; i < to; i++) {
 			r = (int) (rgbFrom.red + (i - from) * redRatio);
 			g = (int) (rgbFrom.green + (i - from) * greenRatio);
 			b = (int) (rgbFrom.blue + (i - from) * blueRatio);
 			rgb = new RGB(r, g, b);
 			colors.put(i, new Color(Display.getDefault(), rgb));
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
 	 */
 	@Override
 	public String getText(Object element) {
 		if (element instanceof GameX01Entry) {
 			String result;
 			GameX01Entry gameEntry = (GameX01Entry) element;
 			ThreeDartsThrow dartThrow = gameEntry.getPlayerThrow().get(
 					this.player);
 			if (dartThrow == null) {
 				result = "";
 			} else if (dartThrow instanceof WinningX01DartsThrow) {
 				WinningX01DartsThrow winThrow = (WinningX01DartsThrow) dartThrow;
 				result = MessageFormat.format("+{0} ({1})",
 						winThrow.getNbDartToFinish(),
 						gameEntry.getNbPlayedDart());
 			} else {
 				result = String.valueOf(dartThrow.getScore());
 			}
 			return result;
 		} else if (element instanceof DummyX01Entry) {
 			return "";
 		}
 		return super.getText(element);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
 	 */
 	@Override
 	public Image getImage(Object element) {
 		if (element instanceof DummyX01Entry) {
 			DummyX01Entry entry = (DummyX01Entry) element;
 			if (this.player.equals(entry.getGame().getFirstPlayer())) {
 				return ProtoPlugin.getImage(ISharedImages.IMG_START_DECO);
 			}
 		}
 		return super.getImage(element);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getBackground(java.lang.Object)
 	 */
 	@Override
 	public Color getBackground(Object element) {
 		if (element instanceof DummyX01Entry) {
 			return OpenDartsFormsToolkit.getToolkit().getColors()
 					.getColor(IFormColors.H_GRADIENT_START);
 		} else if (element instanceof GameX01Entry) {
 			GameX01Entry entry = (GameX01Entry) element;
 			ThreeDartsThrow dartThrow = entry.getPlayerThrow().get(this.player);
 			if (dartThrow instanceof BrokenX01DartsThrow) {
 				return OpenDartsFormsToolkit.getToolkit().getColors()
 						.getColor(OpenDartsFormsToolkit.COLOR_BROKEN);
 			} else if (dartThrow instanceof WinningX01DartsThrow) {
 				return OpenDartsFormsToolkit.getToolkit().getColors()
 						.getColor(OpenDartsFormsToolkit.COLOR_WINNING);
 			}
 		}
 		return super.getBackground(element);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getFont(java.lang.Object)
 	 */
 	@Override
 	public Font getFont(Object element) {
 		return OpenDartsFormsToolkit
 				.getFont(OpenDartsFormsToolkit.FONT_SCORE_SHEET);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getForeground(java.lang.Object)
 	 */
 	@Override
 	public Color getForeground(Object element) {
 		Color result = null;
 		if (element instanceof GameX01Entry) {
 			GameX01Entry entry = (GameX01Entry) element;
 			ThreeDartsThrow dartThrow = entry.getPlayerThrow().get(this.player);
 			if (dartThrow != null) {
 				int score = dartThrow.getScore();
 				result = this.colors.get(score);
 			}
 		}
 		if (result == null) {
 			result = super.getBackground(element);
 		}
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
 	 */
 	@Override
 	public void dispose() {
 		for (Color col : this.colors.values()) {
 			col.dispose();
 		}
 	}
 
 }

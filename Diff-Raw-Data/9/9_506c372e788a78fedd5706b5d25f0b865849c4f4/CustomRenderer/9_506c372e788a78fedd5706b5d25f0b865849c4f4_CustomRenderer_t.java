 package statistik;
 
 import org.jfree.chart.renderer.category.BarRenderer;
 import java.awt.Color;
 import java.awt.Paint;
 
 class CustomRenderer extends BarRenderer {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1075558824007633555L;
 	private Paint[] colors;
 
 	public CustomRenderer(int anzahlBezeichnungenXAchse) {
 		this.colors = new Paint[] { Color.orange, Color.cyan, Color.green, Color.magenta, Color.lightGray, Color.darkGray, Color.black};
 		
 		for (int i=0; i<anzahlBezeichnungenXAchse; i++)
		{ 
			if(i>=colors.length){
				setSeriesPaint(i, this.colors[i%7]);
			}
			else{
				setSeriesPaint(i, this.colors[i]);
			}
 		}
 	}
 
 	public Paint getItemPaint(final int row, final int column) {
 		// Edit: Anfangs Column statt Row. Da war allerdings nur nach den
 		// Gruppen die Farbe differenziert.
 		return (this.colors[row % this.colors.length]);
 	}
 }

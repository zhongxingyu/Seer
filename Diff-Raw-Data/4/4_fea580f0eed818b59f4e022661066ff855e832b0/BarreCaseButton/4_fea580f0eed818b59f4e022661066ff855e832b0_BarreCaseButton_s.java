 package fr.ujm.tse.info4.pgammon.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 
 import javax.swing.ImageIcon;
 
 import fr.ujm.tse.info4.pgammon.models.Case;
 import fr.ujm.tse.info4.pgammon.models.CouleurCase;
 
 public class BarreCaseButton extends CaseButton {
	//TODO les case barre ne peut plus ce mettre en bleu
 	//TODO quand les dame son en bleu on ne fait plus la différence entre amie et ennemie
 	private static final long serialVersionUID = 1696544283522096083L;
 	private final int MAX_DAMES_DRAWED = 5;
 	private final int DAME_SEPARATION = 27;
 	private boolean isDirectionUp;
 	
 	public BarreCaseButton(Case _case, boolean _isDirectionUp) {
 		super(_case);
 		isDirectionUp = _isDirectionUp;
 		build();
 	}
 	
 	private void build() {
 		setOpaque(false);
 		setLayout(null);
 		setPreferredSize(new Dimension(33,150));
 	}
 	
 	@Override
 	protected void paintComponent(Graphics g) {
 		drawDames(g);
 	}
 	
 	private void drawDames(Graphics g) {
 		Case c = getCase();
 		if(c == null)
 			return;
 
 		int h = getHeight();
 		CouleurCase couleurDames = c.getCouleurDame();
 
 		if(couleurDames == CouleurCase.VIDE || c.getNbDame() == 0)
 			return;
 		
 		int nb_dames = c.getNbDame();
 
 		int count = Math.min(nb_dames, MAX_DAMES_DRAWED);
 
 		
 		
 		Graphics2D g2 = (Graphics2D) g.create(); 
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		
 
 		int off = 0;
 		if(model.isRollover()) off ++;
 		if(model.isPressed()) off ++;
 		
 		float ratio = 1;
 		if(nb_dames>=MAX_DAMES_DRAWED){
 			ratio = (float)(MAX_DAMES_DRAWED)/(float)(nb_dames); 
 		}
 		
 		ImageIcon icon;
 		if(getCase().getCouleurDame() == CouleurCase.VIDE){
 			icon = new ImageIcon();
 		}else{
 			icon = (getCase().getCouleurDame()==CouleurCase.BLANC)?iconeBlanche:iconeNoire;
 		}
 		int count_selected = 0;
 		if(isCandidate())
 			count_selected = 1;
 		
 		for(int i=0; i < count-count_selected ;i++)
 		{
 			int y = (int) ((h-DAME_SEPARATION)/2+(i-(count-1)/2f)*(DAME_SEPARATION)+off);
 			g2.drawImage(icon.getImage(),0,y,this);
 		}
 
 		if(isCandidate() &&  c.getNbDame() > 0){
 			float i = count-0.8f;
 			int y = (int) ((h-DAME_SEPARATION)/2+(i-(count-1)/2f)*(DAME_SEPARATION)+off);
 			ImageIcon iconTransp = (getCase().getCouleurDame()==CouleurCase.BLANC)?iconeBlancheTransp:iconeNoireTransp;
 
 			g2.drawImage(iconTransp.getImage(),1,y,this);
 		}
 		
 		if(nb_dames>MAX_DAMES_DRAWED){
 
 			String nb = new Integer(nb_dames).toString();
 			g2.setFont(new Font("Arial",Font.BOLD,18));
 			
 			if(c.getCouleurDame()==CouleurCase.BLANC)
 				g2.setColor(new Color(0x111111));
 			else
 				g2.setColor(new Color(0xCCCCCC));
 			int y = (h/2)+6;
 			
 			g2.drawChars(nb.toCharArray(), 0, nb.length(),11-(nb.length()-1)*5, y);
 		}
 
 		g2.dispose(); 
 	}
 
 	@Override
 	protected void paintBorder(Graphics g) {
 		//super.paintBorder(g);
 	}
 }

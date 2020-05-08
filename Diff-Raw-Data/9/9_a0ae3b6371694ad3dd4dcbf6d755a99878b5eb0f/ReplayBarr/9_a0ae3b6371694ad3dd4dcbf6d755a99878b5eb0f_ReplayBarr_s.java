 package fr.ujm.tse.info4.pgammon.gui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 import fr.ujm.tse.info4.pgammon.models.DeSixFaces;
 import fr.ujm.tse.info4.pgammon.models.Deplacement;
 import fr.ujm.tse.info4.pgammon.models.Tour;
 
 public class ReplayBarr extends JPanel {
 	private static final long serialVersionUID = 1318001554445843500L;
 	private List<Tour> tours;
 	private LinkedList<DeSixFaces> allDes;
 	private LinkedList<Deplacement> allDeplacements;
 	private int current;
 	private int old_current;
 	private int total;
 	private final int SEPARATION = 30;
 	private final int NB_TO_SHOW = 13;
 	private final int DELAY = 30;
 	private JPanel elmentContainer;
 
 
 	private JButton nextBtn;
 	private JButton prevBtn;
 	private JButton endBtn;
 	private JButton beginBtn;
 
 	private Timer timer;
 	private int current_position;
 	private int final_position;
 	
 	public ReplayBarr(List<Tour> tours) {
 		this.tours = tours;
 		current = 0;
 		old_current=0;
 		init();
 		build();
 		init_position();
 	}
 
 	
 	public void setTours(List<Tour> tours){
 		this.tours = tours;
 		current = 0;
 		old_current=0;
 		init();
 		init_position();
 		rebuild();
 	}
 	
 	@Override
 	public void setVisible(boolean aFlag) {
 		super.setVisible(aFlag);
 		if(aFlag)
 			timer.start();
 		else
 			timer.stop();
 	}
 	public void updatePosition(){
 		
 	}
 	public void goTo(Deplacement dep){
 		setCurrent(getIndexOf(dep));
 	}
 
 	@Deprecated
 	public void goNext(){
 		int index = current+1;
 		while(index < total-1 && allDeplacements.get(index) == null )
 			index++;
 		setCurrent(index);
 	}
 
 	@Deprecated
 	public void goPrevious(){
 		int index = current-1;
 		while(index > 0 && allDeplacements.get(index) == null )
 			index--;
 		
 		setCurrent(index);
 	}
 	
 	public void goBegin(){
 		setCurrent(0);
 	}
 	
 	public void goEnd(){
 		setCurrent(total-1);
 	}
 	
 
 	public void setCurrent(int value){
 		if(value<0)
 			value = 0;
 		if(value>= total)
 			value = total-1;
 		if(current == value)
 			return;
 		
 		old_current = current;
 		current = value;
 		rebuild();
 	}
 	private void init(){
 		allDes = new LinkedList<>();
 		allDeplacements = new LinkedList<>();
 		
 		if(tours != null){
 			for (Tour tour : tours) {
 				int length = tour.getDeSixFaces().size();
 				List<DeSixFaces> des = tour.getDeSixFaces();
 				List<Deplacement> deplacements = tour.getListDeplacement();
 				
 				for( int i = 0; i < length ; i++ ){
 					Deplacement dep = null;
 					DeSixFaces de = new DeSixFaces(des.get(i).getCouleurDe(),des.get(i).getValeur());
 					if(deplacements != null && deplacements.size()>i){
 						dep = deplacements.get(i);
 					}
 					allDes.add(de);
 					allDeplacements.add(dep);
 				}
 			}
 		}
 		total = allDes.size();
 		
 	}
 	
 	private void build() {
 		setOpaque(false);
 		setPreferredSize(new Dimension(400,200));
 		setLayout(null);
 		
 		elmentContainer = new JPanel();
 		elmentContainer.setLayout(null);
 		elmentContainer.setOpaque(false);
 		
 		nextBtn = new ReplayBarrButton("next");
 		prevBtn = new ReplayBarrButton("prev");
 		endBtn = new ReplayBarrButton("end");
 		beginBtn = new ReplayBarrButton("begin");
 		beginBtn.setBounds(  0, 0, 385, 100);
 		prevBtn.setBounds( 100, 0, 285, 100);
 		nextBtn.setBounds( 415, 0, 285, 100);
 		endBtn.setBounds(  415, 0, 385, 100);
 		add(nextBtn);
 		add(prevBtn);
 		add(endBtn);
 		add(beginBtn);
 		add(elmentContainer);
 		
 		rebuild();
 
 	}
 
 	
 	private void rebuild() {
 		elmentContainer.setPreferredSize(new Dimension((total)*30, 100));
 
 		elmentContainer.removeAll();
 		int min = Math.min(current, old_current);
 		int max = Math.max(current, old_current);
 		for(int i = 0; i < NB_TO_SHOW ; i ++){
 			putElement(min-1-i);
 			putElement(max+1+i);
 		}
 		for(int i = min; i <= max ; i ++){
 			putElement(i);
 		}
		final_position = (getSize().width-30)/2 - current * SEPARATION;
 	}
 
 	@Override
 	public void setBounds(int x, int y, int width, int height) {
 		super.setBounds(x, y, width, height);
 		rebuild();
 	}
 	private void putElement(int index){
 		if(index < 0 || index >= total)
 			return;
 		DeSixFaces de = allDes.get(index);
 		Deplacement dep = allDeplacements.get(index);
 		
 		ReplayBarrElement elt = new ReplayBarrElement(de, dep);
 		elt.setBounds(SEPARATION * index, 0,
 				elt.getPreferredSize().width, elt.getPreferredSize().height);
 		elmentContainer.add(elt);
 	}
 	
 
 	private void init_position() {
 
 		elmentContainer.setBounds(final_position,0,elmentContainer.getPreferredSize().width,elmentContainer.getPreferredSize().height);
 		if(timer == null)
 			timer = new Timer(DELAY, new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					current_position = (int) (final_position - 0.7*(final_position-current_position));
 					elmentContainer.setBounds(current_position,0,elmentContainer.getPreferredSize().width,elmentContainer.getPreferredSize().height);
 					
 				} 
 			});
 		timer.start();
 		current_position = final_position;
 	}
 	
 
 	
 	private int getIndexOf(Deplacement dep){
 		int i = 0;
 		for(Deplacement d : allDeplacements){
			if(d.getIdDeplacement() == dep.getIdDeplacement())
				return i;
 			i++;
 		}
 		return i;
 	}
 	
 	
 	public JButton getNextBtn() {
 		return nextBtn;
 	}
 
 
 	public JButton getPrevBtn() {
 		return prevBtn;
 	}
 
 
 	public JButton getEndBtn() {
 		return endBtn;
 	}
 
 
 	public JButton getBeginBtn() {
 		return beginBtn;
 	}
 
 
 	
 }

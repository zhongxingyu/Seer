 package frontend.panels;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import frontend.blocks.OpBlock;
 import frontend.utils.WrapLayout;
 
 import backend.blocks.Countable;
 import backend.blocks.Numerical;
 import backend.blocks.Op;
 import backend.computations.infrastructure.Step;
 import backend.main.Parser;
 import backend.main.ParseNode;
 
 public class Compute extends JPanel {
 
 	Map<Integer, Numerical> _numericals;
 	JPanel _computeBar, _pages, _ops, _bar;
 	Integer _id = 0;
 	Solution _solPanel, _stepPanel;
 	
 	public Compute(Solution sol, Solution step) {
 		_numericals = new LinkedHashMap<>();
 		_solPanel = sol;
 		_stepPanel = step;
 		
 		this.setLayout(new BorderLayout());
 		_computeBar = new JPanel(new BorderLayout());
 		_pages = new JPanel(new GridLayout(3, 1));
 		_ops = new JPanel(new WrapLayout(FlowLayout.LEFT));
 		
 		//compute bar
 		JButton computeButton = new JButton("Compute");
 		computeButton.addActionListener(new SolButtonListener(this));
 		_computeBar.add(computeButton, BorderLayout.EAST);
 		JPanel scrollPanel = new JPanel(new BorderLayout());
 		scrollPanel.setPreferredSize(new Dimension(100,55));
 		_bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		JScrollPane scrollBar = new JScrollPane(_bar);
 		scrollBar.getVerticalScrollBar().setPreferredSize(new Dimension(0,0));
 		scrollPanel.add(scrollBar, BorderLayout.CENTER);
 		_computeBar.add(scrollPanel, BorderLayout.CENTER);
 		
 		//pages
 		JButton rank0Button = new JButton("Rank 0");
 		JButton rank1Button = new JButton("Rank 1");
 		JButton rank2Button = new JButton("Rank 2");
 		_pages.add(rank0Button);
 		_pages.add(rank1Button);
 		_pages.add(rank2Button);
 		
 		//operations
 		for(Op o : Op.values()){
 			_ops.add(new OpBlock(o, o.getIcon(o), this));
 		}
 //		_ops.add(new OpBlock(Op.DETERMINANT, "DET", this));
 //		_ops.add(new OpBlock(Op.MM_MINUS, "-", this));
 //		_ops.add(new OpBlock(Op.MM_PLUS, "+", this));
 		
 		_computeBar.setBorder(BorderFactory.createLineBorder(Color.black));
 		_pages.setBorder(BorderFactory.createLineBorder(Color.black));
 		_ops.setBorder(BorderFactory.createLineBorder(Color.black));
 		
 		this.add(_computeBar, BorderLayout.SOUTH);
 		this.add(_pages, BorderLayout.WEST);
 		this.add(_ops, BorderLayout.CENTER);
 	}
 	
 	public void addToBar(Numerical n, String s){
 		_numericals.put(_id, n);
 		_bar.add(new BarObject(n, s, _id, this));
 		_id++;
 		this.revalidate();
 		this.repaint();
 	}
 	
 	public void removeFromBar(int id, Component c){
 		_numericals.remove(id);
 		_bar.remove(c);
 		this.revalidate();
 		this.repaint();
 	}
 	
 	public void compute(){
 		List<Numerical> l = new ArrayList<>();
 		for(Numerical n : _numericals.values()){
			System.out.println(n.getName());
 			l.add(n);
 		}
 		try{
 			ParseNode result = Parser.parse(l);
			if(result == null){
				String s = "\\text{No solution found}";
				_stepPanel.setTex(s);
				_solPanel.setTex(s);
				return;
			}
 			Countable answer = result.getSolution().getAnswer();
 			_solPanel.setTex(answer.toLatex());
 			
 			List<Step> ls = result.getSolution().getSteps();
 			StringBuilder sb = new StringBuilder();
 			for(Step s: ls){
 				sb.append(s.getCountable().toLatex());
 				sb.append("\\\\");
 			}
 			_stepPanel.setTex(sb.toString());
 		} catch (IllegalArgumentException e){
 			System.out.println(e.getMessage());
 			_solPanel.setTex("\\text{" + e.getMessage() + "}");
 		}
 	}
 	
 	private class BarObject extends JPanel implements MouseListener{
 		
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 3647938517413519896L;
 		private Compute _c;
 		private Integer _id;
 		public BarObject(Numerical n, String s, int id, Compute c){
 			JLabel label = new JLabel(s);
 			label.setForeground(Color.white);
 			this.setPreferredSize(new Dimension(30,30));
 			this.setBackground(Color.blue);
 			this.add(label);
 			_c = c;
 			_id = id;
 			this.addMouseListener(this);
 		}
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 			_c.removeFromBar(_id, this);
 		}
 		@Override
 		public void mouseEntered(MouseEvent arg0) {}
 		@Override
 		public void mouseExited(MouseEvent arg0) {}
 		@Override
 		public void mousePressed(MouseEvent arg0) {}
 		@Override
 		public void mouseReleased(MouseEvent arg0) {}
 	}
 	
 	private class SolButtonListener implements ActionListener{
 		
 		Compute _c;
 		
 		public SolButtonListener(Compute c){
 			_c = c;
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			_c.compute();
 		}
 		
 	}
 
 }

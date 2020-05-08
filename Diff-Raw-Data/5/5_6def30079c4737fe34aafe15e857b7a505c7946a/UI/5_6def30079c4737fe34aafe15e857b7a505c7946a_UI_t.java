 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 
 class UI extends JFrame implements ActionListener
 {
 	JButton prev, next;
 	JLabel info;
 	UI_ImagePanel ip;
 	JScrollPane sp;
 	String linkarr[];
 	String img_name[];
 	JLabel url_info;
 	int top;
 	UI() throws Exception
 	{
 		super("Reddit Image Browser");
 		setSize(800,600);
 
 		Container c = this.getContentPane();
 		c.setLayout(null);
 
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		int w = this.getSize().width;
 		int h = this.getSize().height;
 		int x = (dim.width-w)/2;
 		int y = (dim.height-h)/2;
 		this.setLocation(x, y);
 
 		prev = new JButton("<<Prev");
 		prev.setBounds(0,0,100,30);
 		prev.addActionListener(this);
 
 		next = new JButton("Next>>");
 		next.setBounds(700,0,100,30);
 		next.addActionListener(this);
 
 		info = new JLabel("<html><h2>debian.gif</h2></html>");
 		info.setHorizontalAlignment(JLabel.CENTER);
 		info.setBounds(100,0,600,30);
 
 		url_info = new JLabel("<html><h2>debian.gif</h2></html>");
 		url_info.setHorizontalAlignment(JLabel.CENTER);
 
 		ip = new UI_ImagePanel();
 		ip.setLayout(null);
 		ip.setOpaque(true);
 		ip.setPreferredSize(new Dimension(795, 500));
 
 		ip.add(url_info);
 
 		sp = new JScrollPane(ip);
 		sp.setBounds(0,50,795,550);
 
 		c.add(prev);
 		c.add(next);
 		c.add(info);
 		c.add(sp);
 
 		linkarr = ParseLinks.readPage("http://www.reddit.com/r/fffffffuuuuuuuuuuuu/");
 		img_name = new String[linkarr.length];
 
 		for(int i = 1; i<= Integer.parseInt(linkarr[0]) ; i++)
 			img_name[i] = getName(linkarr[i]);
 
 		img_name[0] = linkarr[0];
 
		top = 0;
 
 		this.setResizable(false);
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setVisible(true);
 	}
 
 	public void actionPerformed(ActionEvent e)
 	{
 		try{
 		if(e.getSource()==prev)
 		{
			if(top>1)
 			{
 				String url = linkarr[--top]; 
 				System.out.println(url);
 
 				if(!checkExist(img_name[top]))
 					ImageDwnl.readIm(url, new File("images/" + img_name[top]));
 
 				int ht = ip.changeImage("images/" + img_name[top]);
 				url_info.setText("<html><h2>"+url+"</h2></html>");
 				url_info.setBounds(100,0,600, 2*ht + 50);
 				ip.setPreferredSize(new Dimension(800, ht + 100));
 				sp.getVerticalScrollBar().setValue(0);
 				repaint();
 			}
 		}
 		else if(e.getSource()==next)
 		{
 			if(top<Integer.parseInt(linkarr[0]))
 			{
 				String url = linkarr[++top];
 				System.out.println(url);
 
 				if(!checkExist(img_name[top]))
 					ImageDwnl.readIm(url, new File("images/" + img_name[top]));
 
 				int ht = ip.changeImage("images/" + img_name[top]);
 				url_info.setText("<html><h2>"+url+"</h2></html>");
 				url_info.setBounds(100,0,600, 2*ht + 50);
 				ip.setPreferredSize(new Dimension(800, ht + 100));
 				sp.getVerticalScrollBar().setValue(0);
 				repaint();
 			}
 		}
 		else;
 		}catch(Exception ex){System.out.println(ex);}
 	}
 
 	public String getName(String url)
 	{
 		String temp = "";
 		temp = url.substring( url.lastIndexOf("/")+1);
 		System.out.println(temp);
 		return temp;
 	}
 
 	public boolean checkExist(String fl)
 	{
 		File f = new File("images/" + fl);
 		return f.exists();
 	}
 
 	public static void main(String abc[]) throws Exception
 	{
 		new UI();
 	}
 }

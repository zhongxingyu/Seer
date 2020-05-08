 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Scanner;
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.border.EtchedBorder;
 
 /**
  * Panel for displaying information on the currently or most
  * recently selected friend.
  * @author Justin Doyle
  *
  */
 public class FriendInfoPanel extends JPanel
 {
 	private JLabel pic;
 	private JTextArea infoArea;
 	private JPanel labelPanel;
 	private JPanel picPanel;
 	private Friend friend;
 	private User user;
 
 	/**
 	 * Default constructor.
 	 * @param u The user currently logged in
 	 */
 	public FriendInfoPanel(User u)
 	{
 		user = u;
 		createElements();
 		setupInitialView();
 		this.repaint();
 
 	}
 
 	/**
 	 * Set friend and update view.
 	 * @param f The friend to view
 	 */
 	public void initiatePanel(Friend f)
 	{
 		friend = f;
 		initComponents();
 	}
 
 	private void createElements()
 	{
 		this.setMaximumSize(new Dimension(300,100));
 		this.setMinimumSize(new Dimension(300,100));
 		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		pic = new JLabel();
 		infoArea = new JTextArea();
 		infoArea.setForeground(mainApplet.textColor);
 		infoArea.setBackground(mainApplet.backgroundColor);
 		infoArea.setLineWrap(true);
 		infoArea.setWrapStyleWord(true);
 		infoArea.setFont(pic.getFont());
 		infoArea.setEditable(false);
 		picPanel = new JPanel();
 		picPanel.setBackground(mainApplet.backgroundColor);
 		picPanel.setLayout(new BoxLayout(picPanel, BoxLayout.Y_AXIS));
 		labelPanel = new JPanel();
 		labelPanel.setBackground(mainApplet.backgroundColor);
 		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
		labelPanel.setMaximumSize(new Dimension(190,100));	 // Set this size so no border overlapping can occur
		labelPanel.setMinimumSize(new Dimension(190,100));
 		labelPanel.add(infoArea);
 		picPanel.add(pic);
 		this.add(picPanel);
 		this.add(labelPanel);
 	}
 
 	private void setupInitialView()
 	{
 		boolean returning;
 		try {
 			returning = ServerTools.isReturningUser(user);
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(this, e.getMessage());
 			returning = false;
 		} 
 		if(!returning)
 		{
 			infoArea.setText("Welcome to Baby Name Ranker!" 
 							+ "\nGet started by picking a name on the left.");
 		}
 		else
 		{
 			try {
 				Scanner fScan = 
 						ServerTools.getFileScanner("http://jdoyle65.cs2212.ca/babyname/files/prev_" + user.getID() + ".txt");
 
 				Scanner lineScan = new Scanner(fScan.nextLine());
 				lineScan.useDelimiter(",");
 				String name = lineScan.next();
 				String date = lineScan.next();
 				int rank = lineScan.nextInt();
 				String rankString;
 				if(rank == -1)
 					rankString = "No rank";
 				else
 					rankString = Integer.toString(rank);
 
 				infoArea.setText("Last friend: " + name + "."
 								+ "\nRanking: " + rankString + " on " + date + ".");
 				getPic();
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(this, e.getMessage());
 			}
 		}
 	}
 
 	private void initComponents()
 	{
 		String rankingText;
 		String compareText;
 		if(friend.getRank() == -1)
 			rankingText = friend.getFirstName() + " has not ranked.";
 		else
 			rankingText = friend.getFirstName() + " has ranked " 
 					+ friend.getRank() + ".";
 
 		if (friend.getRank() == -1)
 			compareText = friend.getFirstName() + " is not on the list of names! ";
 		else if (friend.getRank() >= user.getRank())
 			compareText = "You are " + (friend.getRank()-user.getRank())
 					+ " ranks ahead of " + friend.getFirstName() + ".";
 		else
 			compareText = friend.getFirstName() + " is " + (user.getRank()-friend.getRank())
 					+ " ranks ahead of you.";
 		
 		
 		infoArea.setText(rankingText + "\n" + compareText);
 		getPic();
 	}
 
 	private void getPic()
 	{
 		try {
 			URL picUrl = new URL("http://jdoyle65.cs2212.ca/babyname/pics/friend_pic_" + user.getID() + ".jpg");
 			BufferedImage bi = ImageIO.read(picUrl);
 			ImageIcon ii = new ImageIcon(bi);
 			pic.setIcon(ii);
 		} catch (IOException e){
 			pic.setIcon(null);
 		}
 	}
 }

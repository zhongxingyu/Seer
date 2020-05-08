 package edu.bu.cs673.AwesomeAlphabet.view;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 import java.util.Observable;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import edu.bu.cs673.AwesomeAlphabet.controller.AlphabetPageController;
 import edu.bu.cs673.AwesomeAlphabet.controller.ButtonHandler;
 import edu.bu.cs673.AwesomeAlphabet.model.Letter;
 
 
 public class AlphabetPageView extends PageView {
 
 	private AlphabetPageController m_controller;
 	
 	private JPanel letterPanel;
 
 	public AlphabetPageView(String sPageName) {
 		super(sPageName);
 		
 		m_controller = null;
 		
 		JButton titlePageButton = new JButton("Title Page");
 		titlePageButton.addActionListener(new ButtonHandler(this, "OnTitlePageButtonClick"));
 		
 		JButton alphabetSongButton = new JButton("Alphabet Song");
 		alphabetSongButton.addActionListener(new ButtonHandler(this, "OnPlayAlphabetSongButtonClick"));
 		
 		m_panel.setLayout(new BorderLayout());
 		m_panel.add(new JLabel("Alphabet Page", JLabel.CENTER), BorderLayout.PAGE_START);
 		
 		JPanel navBar = new JPanel(new FlowLayout());
 		m_panel.add(navBar, BorderLayout.PAGE_END);
 		
 		navBar.add(titlePageButton);
 		navBar.add(alphabetSongButton);
 		
 		letterPanel = new JPanel(new GridLayout(5, 6));
 		m_panel.add(letterPanel, BorderLayout.CENTER);
 	}
 	
 	public void SetController(AlphabetPageController controller)
 	{
 		if (m_controller == null) {
 			m_controller = controller;
 			
 		
 			// we can now build the letters
 			Iterator<Letter> iter = m_controller.GetLetterIterator();
 			while (iter.hasNext()) {
 				final Letter l = iter.next();
 				JButton b = new JButton("" + l.GetUppercaseLetter() + " " + l.GetLetterAsChar());
 				b.setFont(letterFont);
 				b.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent ae) {
 						OnLetterButtonClick(l);
 					}
 				});
 				letterPanel.add(b);
 			}
 		}
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	public void OnLetterButtonClick(Letter cLetter)
 	{
 		if (m_controller != null)
 			m_controller.GoToLetterPage(cLetter);
 	}
 	
 	public void OnTitlePageButtonClick()
 	{
 		if (m_controller != null)
 			m_controller.GoToTitlePage();
 	}
 	
 	public void OnPlayAlphabetSongButtonClick()
 	{
 		if (m_controller != null)
 			m_controller.PlayAlphabetSong();
 	}
 }

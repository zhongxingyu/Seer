 package edu.bu.cs673.AwesomeAlphabet.view;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.util.Observable;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import edu.bu.cs673.AwesomeAlphabet.controller.ButtonHandler;
 import edu.bu.cs673.AwesomeAlphabet.controller.LetterPageController;
 import edu.bu.cs673.AwesomeAlphabet.model.Letter;
 
 
 public class LetterPageView extends PageView {
 
 	LetterPageController m_controller;
 	
 	JLabel m_uppercase = new JLabel("-", JLabel.CENTER);
 	JLabel m_lowercase = new JLabel("-", JLabel.CENTER);
 	JButton m_image = new JButton("");
 	JLabel m_word = new JLabel("-", JLabel.CENTER);
 	
 	public LetterPageView(String sPageName) {
 		super(sPageName);
 
 		m_panel.setLayout(new BorderLayout());
 		JPanel buttons = new JPanel(new FlowLayout());
 		m_panel.add(buttons, BorderLayout.PAGE_END);
 		
 		JButton b;
 		
 		b = new JButton("Previous Letter");
 		b.addActionListener(new ButtonHandler(this, "OnPreviousLetterButtonClick"));
 		buttons.add(b);
 		
 		b = new JButton("Next Letter");
 		b.addActionListener(new ButtonHandler(this, "OnNextLetterButtonClick"));
 		buttons.add(b);
 		
 		b = new JButton("Title Page");
 		b.addActionListener(new ButtonHandler(this, "OnTitlePageButtonClick"));
 		buttons.add(b);
 		
 		b = new JButton("Alphabet Page");
 		b.addActionListener(new ButtonHandler(this, "OnAlphabetPageButtonClick"));
 		buttons.add(b);
 		
 		b = new JButton("Next Example");
 		b.addActionListener(new ButtonHandler(this, "OnGetNextExampleButtonClick"));
 		buttons.add(b);
 		
 		JPanel mid = new JPanel(new GridLayout(2, 3, 50, 10));
 		mid.add(m_uppercase);
 		mid.add(m_lowercase);
 		mid.add(m_image);
 		mid.add(new JLabel());
 		mid.add(new JLabel());
 		mid.add(m_word);
 		m_panel.add(mid, BorderLayout.CENTER);
 		
 		m_uppercase.setFont(letterFont);
 		m_lowercase.setFont(letterFont);
 		m_word.setFont(wordFont);
 		
 		m_image.addActionListener(new ButtonHandler(this, "OnPictureClick"));
 	}
 	
 	public void SetController(LetterPageController controller)
 	{
 		m_controller = controller;
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		Letter letter = (Letter) arg;
 		
 		m_uppercase.setText("" + letter.GetUppercaseLetter());
 		m_lowercase.setText("" + letter.GetLetterAsChar());
 		m_image.setIcon(letter.getIcon(m_image.getWidth(), m_image.getHeight()));
 		m_word.setText(letter.getWord());
 	}
 
 	public void OnLetterClick()
 	{
 		if(m_controller != null)
 			m_controller.LetterClicked();
 	}
 	
 	public void OnPictureClick()
 	{
 		if(m_controller != null)
 			m_controller.PictureClicked();
 	}
 	
 	public void OnWordClick()
 	{
 		if(m_controller != null)
 			m_controller.WordClicked();
 	}
 	
 	public void OnNextLetterButtonClick()
 	{
 		if(m_controller != null)
 			m_controller.GetNextLetter();
 	}
 	
 	public void OnPreviousLetterButtonClick()
 	{
 		if(m_controller != null)
 			m_controller.GetPreviousLetter();
 	}
 	
 	public void OnAlphabetPageButtonClick()
 	{
 		if(m_controller != null)
 			m_controller.GoToAlphabetPage();
 	}
 	
 	public void OnTitlePageButtonClick()
 	{
 		if(m_controller != null)
 			m_controller.GoToTitlePage();
 	}
 	
 	public void OnGetNextExampleButtonClick()
 	{
 		if(m_controller != null)
 			m_controller.GetNextExample();
 	}
 }

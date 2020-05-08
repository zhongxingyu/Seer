 package edu.bu.cs673.AwesomeAlphabet.model;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 
 import javax.swing.Icon;
 
 
 public class Letter extends Observable {
 
 	private char m_cLetter;
 	private List<WordPictureSound> m_wps = new LinkedList<WordPictureSound>();
 	private int index = 0;
 	
 	public Letter(char cLetter) {
 	
 		m_cLetter = Character.toLowerCase(cLetter);
 	}
 
 	public char GetLetterAsChar()
 	{
 		return m_cLetter;
 	}
 	
 	public char GetUppercaseLetter()
 	{
 		return Character.toUpperCase(m_cLetter);
 	}
 
 	public void addResource(String imageName, String soundName, String wordText) {
 		m_wps.add(new WordPictureSound(wordText, imageName, soundName));
 	}
 
 	public String getWord()
 	{
		WordPictureSound wps = m_wps.get(index);
 		if (wps == null)
 			return null;
 		return wps.GetWordString();
 	}
 
 	public Icon getIcon(int width, int height) {
		WordPictureSound wps = m_wps.get(index);
 		if (wps == null)
 			return null;
 		return wps.GetWordImage(width, height);
 	}
 
 	public void nextExample() {
 		index++;
 		if (index >= m_wps.size())
 			index = 0;
 		setChanged();
 		notifyObservers(this);
 	}
 
 	public void playSound() {
		WordPictureSound wps = m_wps.get(index);
 		if (wps == null)
 			return;
 		wps.PlaySound();
 	}
 }

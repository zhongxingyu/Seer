 package com.test9.irc.engine;
 
 import java.awt.Color;
 
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 
 public class User {
 
 	private String nick;
 	private String realName;
 	private boolean away;
 	private boolean invisible;
 	private boolean wallops;
 	private boolean restricted;
 	private boolean operator;
 	private boolean localOperator;
 	private boolean noticeReceipt;
 	private static int userID = 0;
 	private Color color;
 	private boolean init = false;
 	private SimpleAttributeSet userSimpleAttributeSet;
 
 	private static Color[] colors = {
 		new Color(255, 105, 105), new Color(105, 198, 252), new Color(252, 216, 105),
 		new Color(122, 105, 252), new Color(179, 252, 105), new Color(233, 105, 252), 
 		new Color(105, 252, 142), new Color(252, 105, 161), new Color(105, 252, 252), 
 		new Color(252, 159, 105), new Color(105, 142, 252), new Color(235, 252, 105), 
 		new Color(179, 105, 252), new Color(125, 252, 105), new Color(252, 105, 216), 
 		new Color(105, 252, 196)
 	};
 
 	private static SimpleAttributeSet[] attributes = new SimpleAttributeSet[colors.length];
 
 	User(String nick, boolean yourself) {
 		char prefix = nick.charAt(0);
 
 		if(!init )
 			initAttributes();
 
 		if((prefix >= 'A' && prefix <= 'Z') || (prefix >= 'a' && prefix <= 'z'))
 			this.nick = nick;
 		else
			this.nick = nick.substring(1, nick.length());
 
		System.out.println(this.nick);
 		setUserAttributeSet(userID, yourself);
 		userID++;
 	}
 
 	private void initAttributes() {
 		for(int i = 0; i < attributes.length; i++)
 		{
 			attributes[i] = new SimpleAttributeSet();
 			attributes[i].addAttribute(StyleConstants.CharacterConstants.Foreground, User.getColors()[i]);
 			attributes[i].addAttribute(StyleConstants.CharacterConstants.Bold, true);
 		}
 		init = true;
 
 	}
 
 	private void setUserAttributeSet(int index, boolean yourself) {
 		if(yourself)
 		{
 			userSimpleAttributeSet = new SimpleAttributeSet();
 			userSimpleAttributeSet.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(0x89bdff));
 			userSimpleAttributeSet.addAttribute(StyleConstants.CharacterConstants.Bold, true);
 		} else {
 			try {
 				userSimpleAttributeSet = attributes[index];
 			} catch (IndexOutOfBoundsException e) {
 				setUserAttributeSet(index-attributes.length, false);			
 			} catch (Exception e) {
 				System.err.println("Problem setting the user color.");
 			}
 		}
 	}
 
 	/**
 	 * @return the nick
 	 */
 	public String getNick() {
 		return nick;
 	}
 
 	/**
 	 * @param nick the nick to set
 	 */
 	public void setNick(String nick) {
 		this.nick = nick;
 	}
 
 	/**
 	 * @return the realName
 	 */
 	public String getRealName() {
 		return realName;
 	}
 
 	/**
 	 * @param realName the realName to set
 	 */
 	public void setRealName(String realName) {
 		this.realName = realName;
 	}
 
 	/**
 	 * @return the userID
 	 */
 	public static int getUserID() {
 		return userID;
 	}
 
 	/**
 	 * @param userID the userID to set
 	 */
 	public static void setUserID(int userID) {
 		User.userID = userID;
 	}
 
 	/**
 	 * @return the color
 	 */
 	public Color getColor() {
 		return color;
 	}
 
 	/**
 	 * @return the colors
 	 */
 	public static Color[] getColors() {
 		return colors;
 	}
 
 	/**
 	 * @return the attributes
 	 */
 	public SimpleAttributeSet[] getAttributes() {
 		return attributes;
 	}
 
 	/**
 	 * @return the userSimpleAttributeSet
 	 */
 	public SimpleAttributeSet getUserSimpleAttributeSet() {
 		return userSimpleAttributeSet;
 	}
 
 	/**
 	 * @return the away
 	 */
 	public boolean isAway() {
 		return away;
 	}
 
 	/**
 	 * @param away the away to set
 	 */
 	public void setAway(boolean away) {
 		this.away = away;
 	}
 
 	/**
 	 * @return the invisible
 	 */
 	public boolean isInvisible() {
 		return invisible;
 	}
 
 	/**
 	 * @param invisible the invisible to set
 	 */
 	public void setInvisible(boolean invisible) {
 		this.invisible = invisible;
 	}
 
 	/**
 	 * @return the wallops
 	 */
 	public boolean isWallops() {
 		return wallops;
 	}
 
 	/**
 	 * @param wallops the wallops to set
 	 */
 	public void setWallops(boolean wallops) {
 		this.wallops = wallops;
 	}
 
 	/**
 	 * @return the restricted
 	 */
 	public boolean isRestricted() {
 		return restricted;
 	}
 
 	/**
 	 * @param restricted the restricted to set
 	 */
 	public void setRestricted(boolean restricted) {
 		this.restricted = restricted;
 	}
 
 	/**
 	 * @return the operator
 	 */
 	public boolean isOperator() {
 		return operator;
 	}
 
 	/**
 	 * @param operator the operator to set
 	 */
 	public void setOperator(boolean operator) {
 		this.operator = operator;
 	}
 
 	/**
 	 * @return the localOperator
 	 */
 	public boolean isLocalOperator() {
 		return localOperator;
 	}
 
 	/**
 	 * @param localOperator the localOperator to set
 	 */
 	public void setLocalOperator(boolean localOperator) {
 		this.localOperator = localOperator;
 	}
 
 	/**
 	 * @return the noticeReceipt
 	 */
 	public boolean isNoticeReceipt() {
 		return noticeReceipt;
 	}
 
 	/**
 	 * @param noticeReceipt the noticeReceipt to set
 	 */
 	public void setNoticeReceipt(boolean noticeReceipt) {
 		this.noticeReceipt = noticeReceipt;
 	}
 
 	/**
 	 * @param userSimpleAttributeSet the userSimpleAttributeSet to set
 	 */
 	public void setUserSimpleAttributeSet(SimpleAttributeSet userSimpleAttributeSet) {
 		this.userSimpleAttributeSet = userSimpleAttributeSet;
 	}
 }

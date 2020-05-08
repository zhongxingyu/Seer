 /**
  * 
  * Governs the fields and behavior of the questions that are created
  * and displayed on our application
  * 
  * @author Dan Sanders, 4/29/13
  *
  */
 package com.huskysoft.interviewannihilator.model;
 
 import java.util.Date;
 
public class Question implements Likeable {
 	
 	private int questionId;
 	private String text;
 	private String title;
 	private int authorId;
 	private Date dateCreated;
 	private Category category;
 	private Difficulty difficulty;
 	private int likes;
 	private int dislikes;
 	
 	public Question() {
 		
 	}
 	
 	/**
 	 * Called when our android application is trying to create a new question
 	 * and load it into the database. The database will populate the rest of
 	 * the fields and return the fleshed-out Question object back to the
 	 * application
 	 * 
 	 * @param text
 	 * @param title
 	 * @param categories
 	 * @param difficulty
 	 */
 	public Question(String text, String title, Category category, 
 	Difficulty difficulty) {
 		this.text = text;
 		this.title = title;		
 		this.difficulty = difficulty;
 		this.category = category;
 	}
 
 	public int getQuestionId() {
 		return questionId;
 	}
 	
 	public String getText() {
 		return text;
 	}
 	
 	public String getTitle() {
 		return title;
 	}
 	
 	public int getAuthorId() {
 		return authorId;
 	}
 	
 	public Date getDateCreated() {
 		return dateCreated;
 	}
 	
 	public Category getCategory() {
 		return category;
 	}
 	
 	public Difficulty getDifficulty() {
 		return difficulty;
 	}
 	
 	public int getLikes() {
 		return likes;
 	}
 	
 	public int getDislikes() {
 		return dislikes;
 	}
 
 	public void setQuestionId(int id) {
 		this.questionId = id;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public void setAuthorId(int authorId) {
 		this.authorId = authorId;
 	}
 
 	public void setDateCreated(Date dateCreated) {
 		this.dateCreated = dateCreated;
 	}
 
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 
 	public void setDifficulty(Difficulty difficulty) {
 		this.difficulty = difficulty;
 	}
 
 	public void setLikes(int likes) {
 		this.likes = likes;
 	}
 
 	public void setDislikes(int dislikes) {
 		this.dislikes = dislikes;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + authorId;
 		int i;
 		if (category == null) {
 			i = 0;
 		}
 		else {
 			i = category.hashCode();
 		}
 		result = prime * result + i;
 		int diffResult;
 		if (dateCreated == null) {
 			diffResult = 0;
 		}
 		else {
 			diffResult = dateCreated.hashCode();
 		}
 		int nextPrime = diffResult;
 		int k = nextPrime;
 		result = prime * result + k;
 		int diffJ;
 		if (difficulty == null) {
 			diffJ = 0;
 		}
 		else {
 			diffJ = difficulty.hashCode();
 		}
 		result = prime * result + diffJ;
 		result = prime * result + dislikes;
 		result = prime * result + questionId;
 		result = prime * result + likes;
 		int y;
 		if (text == null) {
 			y = 0;
 		}
 		else {
 			y = text.hashCode();
 		}
 		result = prime * result + y;
 		int diffK;
 		if (title == null) {
 			diffK = 0;
 		}
 		else {
 			diffK = title.hashCode();
 		}
 		result = prime * result + diffK;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!(getClass().equals(obj.getClass()))) {
 			return false;
 		}
 		Question other = (Question) obj;
 		if (authorId != other.authorId) {
 			return false;
 		}
 		if (category != other.category) {
 			return false;
 		}
 		if (dateCreated == null) {
 			if (other.dateCreated != null) {
 				return false;
 			}
 		} 
 		else if (!dateCreated.equals(other.dateCreated)) {
 			return false;
 		}
 		if (difficulty != other.difficulty) {
 			return false;
 		}
 		if (dislikes != other.dislikes) {
 			return false;
 		}
 		if (questionId != other.questionId) {
 			return false;
 		}
 		if (likes != other.likes) {
 			return false;
 		}
 		if (text == null) {
 			if (other.text != null) {
 				return false;
 			}
 		} 
 		else if (!text.equals(other.text)) {
 			return false;
 		}
 		if (title == null) {
 			if (other.title != null) {
 				return false;
 			}
 		} 
 		else if (!title.equals(other.title)) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Question [id=" + questionId + ", text=" + text + ", " 
 		+ "title=" + title + ", authorId=" + authorId 
 		+ ", dateCreated=" + dateCreated + ", category=" + category 
 		+ ", difficulty=" + difficulty + ", likes=" + likes 
 		+ ", dislikes=" + dislikes + "]";
 	}
 
 }

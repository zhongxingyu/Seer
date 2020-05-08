 package edu.asu.krypton.model.persist.db;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.springframework.data.annotation.Id;
 import org.springframework.data.annotation.Transient;
 import org.springframework.data.mongodb.core.index.Indexed;
 import org.springframework.data.mongodb.core.mapping.Document;
 
 import edu.asu.krypton.form.annotations.CheckBox;
 import edu.asu.krypton.form.annotations.InputText;
 import edu.asu.krypton.form.annotations.TextArea;
 import edu.asu.krypton.model.repository.IndexRepository;
 import edu.asu.krypton.model.repository.Repository;
 
 @Document
 @XmlRootElement
 public class Article extends Commentable implements DbEntity {
 	@Id
 	@InputText(readOnly=true)
 	private String id;
 	
 	@Transient
 	public boolean reIndex = false;
 	
 	@Indexed(unique=true)
 	@InputText
 	private String title;
 	@InputText
 	private String description;
 	
 	private String version;
 	
 	private String patches;
 	@Indexed
 	private Date date;
 	
 	@InputText
 	private String commentMode = "custom";
 	
 //	@TextArea(applyCKEditor=true)
 	private String content;
 	
 	@CheckBox
 	private boolean obsolete;
 	// momken ne7tag nzawed author ba3deen
 	
 	@Indexed
 	@CheckBox
 	private boolean home;
 	
 	@XmlAttribute
 	public String getContent() {
 		return content;
 	}
 	
 	public void setContent(String content) {
 		this.content = content;
 	}
 	@XmlAttribute
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	@XmlAttribute
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	@XmlAttribute
 	public String getId() {
 		return id;
 	}
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	@XmlAttribute
 	public boolean isObsolete() {
 		return obsolete;
 	}
 	public void setObsolete(boolean obsolete) {
 		this.obsolete = obsolete;
 	}
 	
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 //	public Article(Article old){
 //		this.id=old.getId();
 //		this.content=old.getContent();
 //		this.title=old.getTitle();
 //		this.obsolete=old.isObsolete();
 //		this.reIndex=old.reIndex;
 //		this.commentMode=old.getCommentMode();
 //		this.date=old.getDate();
 //		this.description=getDescription();
 //		this.home=old.isHome();
 //		this.patches=old.getPatches();
 //		this.version=old.getVersion();
 //		
 //	}
 	@Override
 	public String toString() {
 		return String.format("[id=%s,content=%s,title=%s,description=%s,date=%s,comments=%s]",id,content,title,description,date,getComments());
 	}
 
 	@Override
 	public void onInsert(Repository<?> repository) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public boolean isHome() {
 		return home;
 	}
 
 	public void setHome(boolean home) {
 		this.home = home;
 	}
 
 	public String getCommentMode() {
 		return commentMode;
 	}
 
 	public void setCommentMode(String commentMode) {
 		this.commentMode = commentMode;
 	}
 		public String getVersion() {
 		return version;
 	}
 
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	public String getPatches() {
 		return patches;
 	}
 
 	public void setPatches(String patches) {
 		this.patches = patches;
 	}
 	
 	@Override
 	public void onDelete(Repository<?> repository)
 			throws ClassNotFoundException {
		IndexRepository indexRepository = (IndexRepository) repository;
 		indexRepository.obsoleteArticle(this);
 //		if (ableToDelete()) {
 			// deletion operation
 //			List<Article> obsoleteArticles = articleRepository
 //					.getObsoleteArticles();
 //			for (Article obsolete : obsoleteArticles) {
 				// get the indexArticleStatistics records whose
 				// articleNumber corresponds to this obsolete article
 				boolean intentionToDeleteArticle = true;
 				List<IndexArticleStatistics> indexArticleStatisticsRecords = indexRepository
 						.findIndexArticleStatisticsRecordsByArticle(this,
 								intentionToDeleteArticle);
 				// get the indexInArticleTitlePlaces records for these
 				// indexArticleStatistics's and for content and description too
 				List<IndexInArticleTitlePlaces> indexInArticleTitlePlacesRecords = new ArrayList<IndexInArticleTitlePlaces>();
 				List<IndexInArticleContentPlaces> indexInArticleContentPlacesRecords = new ArrayList<IndexInArticleContentPlaces>();
 				List<IndexInArticleDescriptionPlaces> indexInArticleDescriptionPlacesRecords = new ArrayList<IndexInArticleDescriptionPlaces>();
 				for (IndexArticleStatistics indexStatistics : indexArticleStatisticsRecords) {
 					for (IndexInArticleTitlePlaces indexInArticleTitlePlaces : indexRepository
 							.findIndexInArticleTitlePlacesByIndexArticleStatistics(indexStatistics)) {
 						indexInArticleTitlePlacesRecords
 								.add(indexInArticleTitlePlaces);
 					}
 					for (IndexInArticleContentPlaces indexInArticleContentPlaces : indexRepository
 							.findIndexInArticleContentPlacesByIndexArticleStatistics(indexStatistics)) {
 						indexInArticleContentPlacesRecords
 								.add(indexInArticleContentPlaces);
 					}
 					for (IndexInArticleDescriptionPlaces indexInArticleDescriptionPlaces : indexRepository
 							.findIndexInArticleDescriptionPlacesByIndexArticleStatistics(indexStatistics)) {
 						indexInArticleDescriptionPlacesRecords
 								.add(indexInArticleDescriptionPlaces);
 					}
 				}
 				// get the indices that cooperate with these
 				// indexArticleStatistics's
 				List<Indices> indicesInTheObsoleteArticle = new ArrayList<Indices>();
 				for (IndexArticleStatistics indexArticleStatistics : indexArticleStatisticsRecords) {
 					if (!isAlreadyExist(indexArticleStatistics.getIndex(),
 							indicesInTheObsoleteArticle))
 						indicesInTheObsoleteArticle.add(indexArticleStatistics
 								.getIndex());
 				}
 				// delete the indexArticleStatistics's places , then
 				// delete indexArticleStatistics records, then indices
 				indexRepository
 						.deleteIndexInArticleTitlePlacesRecords(indexInArticleTitlePlacesRecords);
 				indexRepository
 						.deleteIndexInArticleContentPlacesRecords(indexInArticleContentPlacesRecords);
 				indexRepository
 						.deleteIndexInArticleDescriptionPlacesRecords(indexInArticleDescriptionPlacesRecords);
 				for (IndexArticleStatistics indexArticleStatistics : indexArticleStatisticsRecords)
 					indexRepository
 							.deleteIndexArticleStatictics(indexArticleStatistics);
 				// get indices out of those , that has no rows existing in
 				// the IndexArticleStatistics table , then delete them
 				List<Indices> indicesToBeDeleted = new ArrayList<Indices>();
 				for (Indices index : indicesInTheObsoleteArticle) {
 					if (!(indexRepository
 							.findIndexArticleStatisticsRecordsByIndex(index)
 							.size() > 1))
 						indicesToBeDeleted.add(index);
 				}
 				for (Indices index : indicesToBeDeleted)
 					indexRepository.deleteIndex(index);
 				// delete the obsolete article itself
 //				articleRepository.deleteArticle(obsolete);
 	}
 	private boolean isAlreadyExist(Indices index, List<Indices> indices) {
 		if (indices == null || index == null || indices.size() == 0)
 			return false;
 		for (Indices i : indices) {
 			if ((i.getWord().equals(index.getWord()))
 					&& (i.getId() == index.getId()))
 				return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void merge(DbEntity newObject) {
 		Article newArticle = (Article)newObject;
 		setTitle(newArticle.getTitle());
 		setDescription(newArticle.getDescription());
 //		setContent(newArticle.getContent());
 		setObsolete(newArticle.isObsolete());
 		setHome(newArticle.isHome());
 	}
 }

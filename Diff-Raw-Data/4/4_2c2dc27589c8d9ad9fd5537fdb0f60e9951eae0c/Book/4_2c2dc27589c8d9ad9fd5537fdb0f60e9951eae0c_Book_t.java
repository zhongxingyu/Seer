 package com.toastedbits.bookish.domain;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
 import org.springframework.data.neo4j.annotation.GraphId;
 import org.springframework.data.neo4j.annotation.Indexed;
 import org.springframework.data.neo4j.annotation.NodeEntity;
 import org.springframework.data.neo4j.annotation.RelatedTo;
 
 @NodeEntity
 public class Book implements TreeView {
 	@GraphId
 	@Indexed
 	private Long id;
 	private String image;
 	private String title;
 	private String summary;
 	private String content;
 	
 	@RelatedTo(type=RelTypes.BELONGS_TO, direction=Direction.OUTGOING)
 	private Category category;
 	
 	@RelatedTo(type=RelTypes.HAS_PART, direction=Direction.OUTGOING, elementClass=Part.class, enforceTargetType=true)
	@Fetch private Set<Part> parts;
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	public String getImage() {
 		return image;
 	}
 	public void setImage(String image) {
 		this.image = image;
 	}
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public String getSummary() {
 		return summary;
 	}
 	public void setSummary(String summary) {
 		this.summary = summary;
 	}
 	public String getContent() {
 		return content;
 	}
 	public void setContent(String content) {
 		this.content = content;
 	}
 	public Category getCategory() {
 		return category;
 	}
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 	public Set<Part> getParts() {
 		return parts;
 	}
 	public void setParts(Set<Part> parts) {
 		this.parts = parts;
 	}
 	public int getDisplayPriority() {
 		return -1;
 	}
 	public String getDisplayValue() {
 		return title;
 	}
 	public Set<TreeView> getChildren() {
 		List<TreeView> children = new ArrayList<TreeView>();
 		for(Part part : parts) {
 			children.add(part);
 		}
 		Collections.sort(children, new Comparator<TreeView>() {
 			@Override
 			public int compare(TreeView lhs, TreeView rhs) {
 				return rhs.getDisplayPriority() - lhs.getDisplayPriority();
 			}
 		});
 		
 		return new LinkedHashSet<TreeView>(children);
 	}
 }

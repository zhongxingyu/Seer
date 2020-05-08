 package com.aneeshpu.gae.domain.service;
 
 import java.util.Collection;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.aneesh.gae.domain.QuickThought;
 import com.aneesh.gae.domain.Tag;
 import com.aneeshpu.gae.domain.repository.ThoughtRepository;
 
 @Service
 public class Mind {
 
 	private final ThoughtRepository thoughtRepository;
 
 	@Autowired
 	public Mind(final ThoughtRepository thoughtRepository) {
 		this.thoughtRepository = thoughtRepository;
 	}
 
	public QuickThought think(final String thought, final String tags) {
 		Tag tag = new Tag(tags);
 		Tag existingTag = thoughtRepository.find(tag);
 		tag = existingTag == null ? thoughtRepository().persist(tag) : existingTag;
 		final QuickThought quickThought = new QuickThought(thought,tag);
 		tag.add(quickThought);
 		return quickThought;
 	}
 
 	private ThoughtRepository thoughtRepository() {
 		return thoughtRepository;
 	}
 
 	public Collection<QuickThought> allMyThoughts() {
 		return thoughtRepository().allMyThoughts();
 	}
 
 	public Collection<QuickThought> allThoughtTaggedWith(String tagName) {
 		Tag tag = new Tag(tagName); 
 		return thoughtRepository.allThoughtsTaggedWith(tag);
 	}
 }

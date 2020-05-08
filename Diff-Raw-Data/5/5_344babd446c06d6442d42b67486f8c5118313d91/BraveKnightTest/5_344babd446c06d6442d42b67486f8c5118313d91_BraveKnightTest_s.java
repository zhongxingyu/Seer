package com.springinaction.knights;
 
 import static org.mockito.Mockito.*;
 
 import org.junit.Test;
 import com.springinaction.knights.*;
 
 class BraveKnightTest {
 	@Test
 	public void knightShouldEmbarkOnQuest() throws QuestException {
 		Quest mockQuest = mock(Quest.class);
 
		BraveKnight knight = new BraveKinght(mockQuest);
 		knight.embarkOnQuest();
 
 		verify(mockQuest, times(1)).embark();
 	}
 }

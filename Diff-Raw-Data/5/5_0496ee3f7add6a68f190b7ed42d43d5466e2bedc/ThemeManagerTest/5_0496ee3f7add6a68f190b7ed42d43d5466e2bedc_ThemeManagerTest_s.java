 package com.redcareditor.theme;
 
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 import com.redcareditor.theme.ThemeManager;
 
 public class ThemeManagerTest {
 	@Test
 	public void shouldLoadThemes() {
 		ThemeManager.loadThemes("input/");
		assertEquals(2, ThemeManager.themes.size());
 	}
 
 	@Test
 	public void shouldContainTwoRightThemes() {
		String [] expectedThemeNames = {"Railscasts", "Twilight"};
 		for(String theme : expectedThemeNames){
 			boolean found = false;
 			for(Theme t : ThemeManager.themes){
 				if(t.name.equals(theme)){
 					found = true;
 					break;
 				}
 			}
 			assertTrue(String.format("Could not find theme '%s', which sould be loaded by now", theme), found);
 		}
 	}
 }

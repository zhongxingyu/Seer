 /*  Copyright (C) 2012  Nicholas Wright
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package app;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import static org.mockito.Mockito.*;
 import static config.AppSetting.*;
 import static app.SettingValidator.*;
 
 import java.util.Properties;
 import org.junit.Before;
 import org.junit.Test;
 
 public class SettingValidatorTest {
 	Properties appSettings;
 	
 	@Before
 	public void setup() throws Exception{
 		appSettings = mock(Properties.class);
 	}
 	
 	// Image Thread tests
 	@Test
 	public void itNegative(){
 		when(appSettings.getProperty(image_threads.toString())).thenReturn("-5");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void itZero(){
 		when(appSettings.getProperty(image_threads.toString())).thenReturn("0");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void itPositive(){
 		when(appSettings.getProperty(image_threads.toString())).thenReturn("7");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void itNotNum(){
 		when(appSettings.getProperty(image_threads.toString())).thenReturn("a");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void itEmpty(){
 		when(appSettings.getProperty(image_threads.toString())).thenReturn("");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	// page thread tests
 	@Test
 	public void ptNegative(){
 		when(appSettings.getProperty(page_threads.toString())).thenReturn("-7");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void ptZero(){
 		when(appSettings.getProperty(page_threads.toString())).thenReturn("0");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void ptPositive(){
 		when(appSettings.getProperty(page_threads.toString())).thenReturn("9");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void ptNotNum(){
 		when(appSettings.getProperty(page_threads.toString())).thenReturn("z");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void ptEmpty(){
 		when(appSettings.getProperty(page_threads.toString())).thenReturn("");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	// write blocked tests
 	@Test
 	public void wbFalse(){
 		when(appSettings.getProperty(write_blocked.toString())).thenReturn("false");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void wbFalseAllCaps(){
 		when(appSettings.getProperty(write_blocked.toString())).thenReturn("FALSE");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void wbFalseCap(){
 		when(appSettings.getProperty(write_blocked.toString())).thenReturn("False");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void wbTrue(){
 		when(appSettings.getProperty(write_blocked.toString())).thenReturn("true");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void wbTrueAllCaps(){
 		when(appSettings.getProperty(write_blocked.toString())).thenReturn("TRUE");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void wbTrueCap(){
 		when(appSettings.getProperty(write_blocked.toString())).thenReturn("True");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void wbInvalid(){
 		when(appSettings.getProperty(write_blocked.toString())).thenReturn("catfish");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void wbEmpty(){
 		when(appSettings.getProperty(write_blocked.toString())).thenReturn("");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void wbNumber(){
 		when(appSettings.getProperty(write_blocked.toString())).thenReturn("12345");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	// base url tests
 	@Test
 	public void buCorrect(){
 		when(appSettings.getProperty(base_url.toString())).thenReturn("http://foo.bar/");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void buMissingTrailingSlash(){
 		when(appSettings.getProperty(base_url.toString())).thenReturn("http://foo.bar");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void buMissingHttp(){
 		when(appSettings.getProperty(base_url.toString())).thenReturn("foo.bar/");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void buHtttp(){
 		when(appSettings.getProperty(base_url.toString())).thenReturn("htttp://foo.bar/");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void buSubdomain(){
 		when(appSettings.getProperty(base_url.toString())).thenReturn("http://foo.moo.bar/");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void buMoreSubdomains(){
 		when(appSettings.getProperty(base_url.toString())).thenReturn("http://foo.moo.yeti.bar/");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void buNotopLevelDomain(){
 		when(appSettings.getProperty(base_url.toString())).thenReturn("http://foobar/");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void buInvalidTopLevel(){
 		when(appSettings.getProperty(base_url.toString())).thenReturn("http://foo.bar1/");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void buUrlWithNumbers(){
 		when(appSettings.getProperty(base_url.toString())).thenReturn("http://f00.b4r.net/");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void buMissingSemicolon(){
 		when(appSettings.getProperty(base_url.toString())).thenReturn("http//foo.bar/");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void buMissingSlash(){
		when(appSettings.getProperty(base_url.toString())).thenReturn("http:/foo.bar/");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	// sub pages tests
 	@Test
 	public void spCorrect(){
 		when(appSettings.getProperty(sub_pages.toString())).thenReturn("a;15,b;14");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void spDoubleComma(){
 		when(appSettings.getProperty(sub_pages.toString())).thenReturn("a;15,,b;14");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void spSingleEntry(){
 		when(appSettings.getProperty(sub_pages.toString())).thenReturn("a;15");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void spSemicolonComma(){
 		when(appSettings.getProperty(sub_pages.toString())).thenReturn("a,15,b;14");
		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void spDoubleSemicolon(){
 		when(appSettings.getProperty(sub_pages.toString())).thenReturn("a;;15,b;14");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void spCommaSemicolon(){
 		when(appSettings.getProperty(sub_pages.toString())).thenReturn("a;15;b;14");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void spColon(){
 		when(appSettings.getProperty(sub_pages.toString())).thenReturn("a;15,b:14");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void spMissingSemicolon(){
 		when(appSettings.getProperty(sub_pages.toString())).thenReturn("a15,b;14");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void spNegativePageValue(){
 		when(appSettings.getProperty(sub_pages.toString())).thenReturn("a;-15,b;14");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void spInvalidSymbol(){
 		when(appSettings.getProperty(sub_pages.toString())).thenReturn("ab%;15,b;14");
 		assertThat(validateImageThreads(appSettings), is(false));
 	}
 	
 	@Test
 	public void spMultiLetterName(){
 		when(appSettings.getProperty(sub_pages.toString())).thenReturn("foo;15,yeti;14");
 		assertThat(validateImageThreads(appSettings), is(true));
 	}
 	
 	@Test
 	public void testEmptyPropertyFile(){
 		assertThat(SettingValidator.validateAppSettings(new Properties()), is(false));
 	}
 }

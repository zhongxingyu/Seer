 /**
  * Copyright (C) 2009 Mads Mohr Christensen, <hr.mohr@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * 
  */
 package dk.cubing.liveresults.utilities;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class CountryUtilTest {
 	
 	private CountryUtil countryUtil;
 	private final String[] countryCodes = {"DK", "SE", "NL"};
 	private final String[] countryNames = {"Denmark", "Sweden", "Netherlands"};
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		countryUtil = new CountryUtil();
 	}
 
 	/**
 	 * Test method for {@link dk.cubing.liveresults.utilities.CountryUtil#getCountryByCode(java.lang.String)}.
 	 */
 	@Test
 	public void testGetCountryByCode() {
 		assertNull(countryUtil.getCountryByCode(null));
 		assertNull(countryUtil.getCountryByCode(""));
		assertNull(countryUtil.getCountryByCode("DoNotExist")); // non-existing country
 		for (int i=0; i<countryCodes.length; i++) {
 			String name = countryUtil.getCountryByCode(countryCodes[i]);
 			assertNotNull(name);
 			assertEquals(countryNames[i], name);
 		}
 	}
 
 	/**
 	 * Test method for {@link dk.cubing.liveresults.utilities.CountryUtil#getCountryCodeByName(java.lang.String)}.
 	 */
 	@Test
 	public void testGetCountryCodeByName() {
 		assertNull(countryUtil.getCountryCodeByName(null));
 		assertNull(countryUtil.getCountryCodeByName(""));
		assertNull(countryUtil.getCountryCodeByName("DoNotExist")); // non-existing country
 		for (int i=0; i<countryNames.length; i++) {
 			String code = countryUtil.getCountryCodeByName(countryNames[i]);
 			assertNotNull(code);
 			assertEquals(countryCodes[i], code);
 		}
 	}
 
 }

 /*
   This file is part of opensearch.
   Copyright © 2009, Dansk Bibliotekscenter a/s,
   Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
   opensearch is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   opensearch is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 
 package dk.dbc.opensearch.common.fedora;
 
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public class PIDTest {
 
 
     PID p;
 
     @Before
     public void setUp()
     {
         p = new PID( "test:1" );
     }
 
 
     @AfterClass
     public static void tearDownClass() throws Exception
     {
     }
 
     @Test( expected = IllegalArgumentException.class )
     public void testLongIdentifierThrows()
    {
        String id = "iamastringlongerthantwentyfivecharacters:1";
         new PID( id );
     }
 
     @Test( expected = IllegalArgumentException.class )
     public void testEmptyIdentifierThrows()
     {
         new PID( "" );
     }
 
     @Test( expected = IllegalArgumentException.class )
     public void testInvalidIdentifierThrows()
     {
         String id = "iamanillegalidentifier";
         new PID( id );
     }
 
     @Test
     public void testGetIdentifier()
     {
         String expected = "test:1";
         assertTrue( expected.equals( p.getIdentifier() ) );
     }
 
 
     @Test
     public void testGetNamespace()
     {
         String expected = "test";
         assertTrue( expected.equals( p.getNamespace() ) );
     }
 
 }

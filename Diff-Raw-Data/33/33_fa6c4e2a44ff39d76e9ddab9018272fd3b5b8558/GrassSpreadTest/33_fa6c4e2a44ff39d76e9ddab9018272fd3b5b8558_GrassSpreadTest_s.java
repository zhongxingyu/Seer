 package com.cegeka.nocturne.godgame;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import static org.mockito.Mockito.*;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 @RunWith(MockitoJUnitRunner.class)
 public class GrassSpreadTest {
 
     @Mock
     private World world;
     private Creature creature;
 
     @Before
     public void setup() {
         creature = new Grass();

     }
 
    @Test(expected = IllegalArgumentException.class)
     public void givenASCellContainingGrass_whenAgeOfGrass7Days_thenOccupyNearbyEmptyCell() {
         when(world.getCell(1,1)).thenReturn(creature);
        assertThat(creature instanceof  Grass);
 
     }
 
 
 }

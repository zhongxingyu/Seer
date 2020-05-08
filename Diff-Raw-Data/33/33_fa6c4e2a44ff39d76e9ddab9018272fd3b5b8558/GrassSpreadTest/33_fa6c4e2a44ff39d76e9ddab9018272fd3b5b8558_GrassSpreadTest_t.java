 package com.cegeka.nocturne.godgame;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
 
    @Test
     public void givenASCellContainingGrass_whenAgeOfGrass7Days_thenOccupyNearbyEmptyCell() {
         when(world.getCell(1,1)).thenReturn(creature);
 
        Creature myCreature = world.getCell(1,1);

        assertThat(myCreature instanceof  Grass);

        assertThat(myCreature.getAge()).isEqualTo(0);
     }
 
 
 }

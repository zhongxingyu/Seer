 package com.github.colorlines.domainimpl;
 
 import com.github.colorlines.domain.*;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Range;
 import com.google.common.collect.Ranges;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Provider;
 import java.util.Random;
 import java.util.Set;
 
 /**
  * @author Stanislav Kurilin
  */
 public class RandomBallGenerator implements BallGenerator {
     private final Random random = new Random();
     private final int generateByOneTime;
     private final Provider<Area> area;
 
     @Inject
     public RandomBallGenerator(@Named("ballsGenerateByOneTime") int ballsGenerateByOneTime, Provider<Area> area) {
         this.generateByOneTime = ballsGenerateByOneTime;
         this.area = area;
     }
 
     @Override
     public Set<Ball> generate() {
        final ImmutableSet.Builder<Ball> builder = ImmutableSet.builder();
        for (int i = 0; i < generateByOneTime; i++) {
            builder.add(new SimpleBall(randomColor(), randomFreeLocation()));
         }
        return builder.build();
     }
 
     private Position randomFreeLocation() {
         int x, y;
         Position candidate;
         do {
             x = integerInRange(Position.WIDTH_RANGE);
             y = integerInRange(Position.HEIGHT_RANGE);
             candidate = Position.create(x, y);
         } while (area.get().contains(candidate));
         return candidate;
     }
 
     private Color randomColor() {
         final Color[] all = Color.values();
         final int randIndex = integerInRange(Ranges.closed(0, all.length));
         return all[randIndex];
     }
 
     private int integerInRange(Range<Integer> range) {
         final int width = range.upperEndpoint() - range.lowerEndpoint();
         return random.nextInt(width) + range.lowerEndpoint();
     }
 }

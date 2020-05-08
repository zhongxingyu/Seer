 package pt.up.fe.pt.lpoo.bombermen;
 
 import pt.up.fe.pt.lpoo.utils.Ref;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.math.MathUtils;
 
 /* VERY IMPORTANT _lastGuid IS JUST AN HACK */
 
 public class MapLoader
 {
     private int _lastGuid = 100;
 
     public MapLoader(World w, EntityBuilder entB)
     {
         _world = w;
         _entityBuilder = entB;
     }
 
     public boolean TryLoad(int number, Ref<Integer> width, Ref<Integer> height)
     {
         FileHandle file = Gdx.files.internal("data/map" + number + ".txt");
 
         if (!file.exists()) return false;
 
         if (file.isDirectory()) return false;
 
         String str = file.readString(Constants.DEFAULT_MAP_FILE_CHARSET);
         String lines[] = str.split("\\r?\\n");
         for (int y = 0; y < lines.length; ++y)
         {
             char[] chars = lines[y].toCharArray();
             for (int x = 0; x < chars.length; ++x)
             {
                 switch (chars[x])
                 {
                     case ' ': // empty space, can be destroyable wall
                        if (MathUtils.random() < Constants.WALL_CHANCE) AddWall(1, x, lines.length - 1 - y);
                         break;
                     case 'X': // undestroyable wall
                        AddWall(-1, x, lines.length - 1 - y);
                         break;
                     case '#': // space reserved for players
                        ReservePlayerSpace(x, lines.length - 1 - y);
                         break;
                 }
             }
         }
 
         return true;
     }
 
     private World _world;
     private EntityBuilder _entityBuilder;
 
     private void ReservePlayerSpace(int tileX, int tileY)
     {
         // @TODO: Implement
     }
 
     private void AddWall(int hp, int tileX, int tileY)
     {
         _world.AddEntity(_entityBuilder.CreateWall(_lastGuid++, hp, tileX, tileY));
     }
 }

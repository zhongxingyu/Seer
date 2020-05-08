 public class Test
 {
     public static void main( String[] args )
     {
         Shape[] shapes = new Shape[4];
         shapes[0] = new Circle( 22, 88, "Circle", 4 );
         shapes[1] = new Square( 71, 96, "Square", 10 );
         shapes[2] = new Sphere( 8, 89, "Sphere", 2 );
         shapes[3] = new Cube( 79, 61, "Cube", 8 );
 
         for ( Shape s : shapes )
         {
             System.out.println( s );
             if ( s instanceof TwoDimensionalShape )
             {
                System.out.println( ( (TwoDimensionalShape)s ).getArea() );
             }
             else if ( s instanceof ThreeDimensionalShape )
             {
                System.out.println( ( (ThreeDimensionalShape)s ).getArea() );
                System.out.println( ( (ThreeDimensionalShape)s ).getVolume() );
             }
         }
     }
 }

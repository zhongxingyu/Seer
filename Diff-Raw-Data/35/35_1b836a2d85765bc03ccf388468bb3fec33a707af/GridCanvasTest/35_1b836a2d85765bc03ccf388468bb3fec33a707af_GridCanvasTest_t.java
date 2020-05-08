 package b2s.tictactoe;
 
import b2s.tictactoe.trophy.TrophyManager;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 
 import static b2s.tictactoe.PointToGridResolver.GridLocation.*;
 import static org.mockito.Mockito.*;
 
 
 public class GridCanvasTest {
     private GridCanvas canvas;
     private MouseEvent mouseEvent;
     private Grid grid;
     private PointToGridResolver pointToGridResolver;
     private Point point;
    private TrophyManager trophyManager;
 
     @Before
     public void setUp() throws Exception {
         pointToGridResolver = mock(PointToGridResolver.class);
         mouseEvent = mock(MouseEvent.class);
         grid = mock(Grid.class);
         point = mock(Point.class);
        trophyManager = mock(TrophyManager.class);
 
        canvas = new GridCanvas(new Dimension(300, 300), trophyManager, null);
         canvas.setGrid(grid);
         canvas.setPointToGridResolver(pointToGridResolver);
 
         when(mouseEvent.getPoint()).thenReturn(point);
     }
 
     @Test
     public void test_bottomRight() {
         when(pointToGridResolver.resolve(point)).thenReturn(BOTTOM_RIGHT);
 
         canvas.mouseClicked(mouseEvent);
 
        verify(grid).move(2, 2, 'x');
     }
 
     @Test
     public void test_bottomMid() {
         when(pointToGridResolver.resolve(point)).thenReturn(BOTTOM_MID);
 
         canvas.mouseClicked(mouseEvent);
 
        verify(grid).move(2, 1, 'x');
     }
 
     @Test
     public void test_bottomLeft() {
         when(pointToGridResolver.resolve(point)).thenReturn(BOTTOM_LEFT);
 
         canvas.mouseClicked(mouseEvent);
 
        verify(grid).move(2, 0, 'x');
     }
 
     @Test
     public void test_midRight() {
         when(pointToGridResolver.resolve(point)).thenReturn(MID_RIGHT);
 
         canvas.mouseClicked(mouseEvent);
 
        verify(grid).move(1, 2, 'x');
     }
 
     @Test
     public void test_midMid() {
         when(pointToGridResolver.resolve(point)).thenReturn(MID_MID);
 
         canvas.mouseClicked(mouseEvent);
 
        verify(grid).move(1, 1, 'x');
     }
 
     @Test
     public void test_midLeft() {
         when(pointToGridResolver.resolve(point)).thenReturn(MID_LEFT);
 
         canvas.mouseClicked(mouseEvent);
 
        verify(grid).move(1, 0, 'x');
     }
 
     @Test
     public void test_topRight() {
         when(pointToGridResolver.resolve(point)).thenReturn(TOP_RIGHT);
 
         canvas.mouseClicked(mouseEvent);
 
        verify(grid).move(0, 2, 'x');
     }
 
     @Test
     public void test_topMid() {
         when(pointToGridResolver.resolve(point)).thenReturn(TOP_MIDDLE);
 
         canvas.mouseClicked(mouseEvent);
 
        verify(grid).move(0, 1, 'x');
     }
 
     @Test
     public void test_topLeft() {
         when(pointToGridResolver.resolve(point)).thenReturn(TOP_LEFT);
 
         canvas.mouseClicked(mouseEvent);
 
        verify(grid).move(0, 0, 'x');
     }
 
 
 }

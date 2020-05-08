 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import javax.swing.*;
 import javax.imageio.*;
 import java.io.*;
 import java.util.*;
 import java.lang.*;
 
 class Sprite{
 	Vector<BufferedImage> images;
 	
 	Sprite(Vector<BufferedImage> sprites)
 	{
		images = sprites
 	}
 	
 	Vector<BufferedImage> getImages()
 	{
 		return images;
 	}
}

 package com.group2.finger_occ_demo;
 
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.view.View;
 
 import com.group2.finger_occ_demo.data.Movie;
 
 /**
  * Responsible for all shapes on screen. Note order of list is the way of doing
  * z-indexing.
  */
 public class Points {
 	private int[] rect_size = {20, 20};
 	private ArrayList<Square_Shape> squares;
 	private int radiusPX;// From the center of a shape
 	private float xStart;
 	private float yStart;
 	private float availableWidth;
 	private float availableHeight;
 	
 	private int[] xRange;
 	private int[] yRange;
 	private int xOffset;
 	private int yOffset;
 	
 	public ArrayList<Square_Shape> sqs = new ArrayList<Square_Shape>();
 		
 	/**
 	 * Responsible for all shapes in a given box. Note order of list is the way of doing
 	 * z-indexing.
 	 */
 	public Points(float xStart, float yStart, float screenWidth, float screenHeight, int[] xRange, int[] yRange){
 		xOffset = 0;
 		yOffset = 0;
 		
 		// Get the available width and ranges
 		this.availableWidth = screenWidth;
 		this.availableHeight = screenHeight;
 		this.xStart = xStart;
 		this.yStart = yStart;
 		this.xRange = xRange;
 		this.yRange = yRange;
 		
 		// Derive rectangle size from screen size so it looks nice (only for now)
 		rect_size[0] = (int) (this.availableWidth * 0.02);
 		rect_size[1] = (int) (this.availableWidth * 0.02);
 		
 		// Also derive radius from screen size, make it 4 times the shapes size
 		radiusPX = rect_size[0] * 4;
 		
 		squares = new ArrayList<Square_Shape>();
 		
 		init_from_data(null);
 	}
 	
 	/**
 	 * Currently uses year on x and rating on height. Formula is:
 	 * (n/m) * r = x or y position,
 	 * where n is the maximum graph value for the axis, n is the maximum value
 	 * for the data points i.e. for rating 10, and r is the current value of the point.
 	 * 
 	 * Note1: If movies is null the default movie list is loaded.
 	 * Note2: If the current user possesses a movie with the same title that is current that
 	 * 		  movie is used instead for the point.
 	 */
 	public void init_from_data(List<Movie> movies){
 		float x = -9999;
 		float y = -9999;
 		int color = Color.GREEN;
 		if (movies == null)
 			movies = canvasApp.data.getMovie();
 		float heightInc = availableHeight/(yRange[1] - yRange[0]);
 		float widthInc = availableWidth/(xRange[1] - xRange[0]);
 		Movie toUse;
 		ArrayList<Movie> usedFromUser = new ArrayList<Movie>();
 		
 		squares = new ArrayList<Square_Shape>();
 		for (Movie movie : movies){
 			// See if the current user has a modified version of the current movie
 			toUse = checkCurUser(movie);
 			if (!movie.equals(toUse))
 				usedFromUser.add(toUse);
 			
 			// Don't draw the square as the user deleted it
 			if (toUse != null)
 				draw_square(heightInc, widthInc, toUse, color, x, y);
 		}
 		
 		// Also must make sure any extra movies the user has are rendered.
 		if (canvasApp.users.currentUser() != null){
 			for (Movie movie : canvasApp.users.currentUser().getMovies()){
 				if (movie.isDeleted() == false && !usedFromUser.contains(movie))
 					draw_square(heightInc, widthInc, movie, color, x, y);
 			}
 		}
 	}
 	
 	private void draw_square(float heightInc, float widthInc, Movie toUse, int color, float x, float y){
 		x = (float) ( (widthInc * toUse.getYear1900()) + xOffset + xStart);
 		y = (float) ( ((availableHeight + yStart) - (heightInc * toUse.getRating())) + yOffset);//invert the ratings so 0 is at the bottom
 		
 		color = getColor(toUse.getGenre().get(0));
 			
 		squares.add(new Square_Shape(toUse, x + rect_size[0]/2, y - rect_size[1]/2, rect_size, color));// the minus size centers a shape on a tick line
 	}
 	
 	/**
 	 * Checks if the current user has a movie if not returns the movie
 	 * checking for if so returns users modified version of movie. Only when
 	 * a user has deleted a movie it is returned as null.
 	 */
 	private Movie checkCurUser(Movie movie){
 		User curUser = canvasApp.users.currentUser();
 		
 		if (curUser == null)
 			return movie;
 		else{
 			Movie userMovie = curUser.tryGetMovie(movie.getTitle());
 			if (userMovie == null)
 				return movie;
 			else if (userMovie.isDeleted() == false)
 				return userMovie;
 			else
 				return null;
 		}
 	}
 	
 	public void filter_points(String genre, String rating){
 		squares.clear();
 		List<Movie> movies;
 		
 		if(genre.equals("All") && rating.equals("All")){
 			movies = canvasApp.data.getMovie();
 		}
 		else{
 			movies = new ArrayList<Movie>();
 			
 			for(Movie movie : canvasApp.data.getMovie()){
 				
 				String g = movie.getGenre().get(0);
 				int r = movie.getRating();
 				
 				if(genre.equals("All")){
 					if(r==Integer.valueOf(rating)){
 						movies.add(movie);
 					}
 				}
 				
 				else if(rating.equals("All")){
 					if(g.equals(genre)){
 						movies.add(movie);
 					}
 				}
 				
 				else if(g.equals(genre) && r==Integer.valueOf(rating)){
 					movies.add(movie);
 				}
 			}
 		}
 		
 		init_from_data(movies);
 	}
 	
 	public void filter_points(String text, String genre, String rating)
 	{
 		text = text.trim();
 		
 		if(text.length() > 1)
 		{
 			squares.clear();
 			List<Movie> movies;
 			
 			movies = new ArrayList<Movie>();
 			
 			for(Movie movie : canvasApp.data.getMovie()){
 				
 				String g = movie.getGenre().get(0);
 				int r = movie.getRating();
 				
 				if(genre.equals("All") && rating.equals("All")){
 					if(Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE).matcher(movie.getTitle()).find())
 					{
 						movies.add(movie);
 					}
 				}
 				else
 				if(genre.equals("All")){
 					if(r==Integer.valueOf(rating) && Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE).matcher(movie.getTitle()).find()){
 						movies.add(movie);
 					}
 				}
 				
 				else if(rating.equals("All")){
 					if(g.equals(genre) && Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE).matcher(movie.getTitle()).find()){
 						movies.add(movie);
 					}
 				}
 				
 				else if(g.equals(genre) && r==Integer.valueOf(rating)){
 					if(Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE).matcher(movie.getTitle()).find())
 					{
 						movies.add(movie);
 					}
 				}
 			}
 		
 		init_from_data(movies);
 		}
 	}
 
 	
 	/**
 	 * Draw all shapes visible on the sent canvas. Due to ordering shapes in the same position end up in the
 	 * same place of an array. So can use previous coordinates to determine if shape is drawable. Also sets
 	 * the current shape to drawable.
 	 */
 	public void drawShapes(Canvas on){
 		Square_Shape square;
 		int[] prevXY = {-1,-1};// should not be on screen, meaning first shape is always drawn
 		
 		for (int i = 0; i < squares.size(); i++){
 			square =  squares.get(i);
 			square.setDrawn(false);
 			if ( !(square.getX() == prevXY[0] && square.getY() == prevXY[1])){
 				square.draw(on);
 				prevXY[0] = square.getX();
 				prevXY[1] = square.getY();
 				square.setDrawn(true);
 			}
 		}
 	}
 	
 	/**
 	 * If in any drawn shape on the canvas is under the finger and is the biggest.
 	 */
 	public ArrayList<Movie> inShape(int x, int y){
 		int[] position = {x, y};
 		
 		ArrayList<Movie> movies = new ArrayList<Movie>();
 		
 		// See if in any of the shapes, backwards loop because current biggest
 		// objects are in the end of the array list. Only retrieves object if drawn.
 		
 		
 		for (int i = squares.size() - 1; i > 0; i--){
 			
 			//System.out.println(squares.get(i).getMovie().getTitle());
 
 			if (squares.get(i).inShape(position) == true){
 				
 				if(squares.get(i).isDrawn()){
 					movies.add(squares.get(i).getMovie());
 					//System.out.println(squares.get(i).getMovie().getTitle());
 
 					return movies;
 				}
 				else{
 					
 					//squares.get(i).translate(0, 0 +20);
 					movies.add(squares.get(i).getMovie());
 					//System.out.println(squares.get(i).getMovie().getTitle());
 
 				}
 				
 
 			}
 		}
 		
 		return movies;
 	}
 	
 	public void inShapeSquares(int x, int y){
 		int[] position = {x, y};
 		
 		int xint = 0;
 		int yint = 0;
 		
 		//System.out.println("Event: action move");
 		
 		
 		// See if in any of the shapes, backwards loop because current biggest
 		// objects are in the end of the array list. Only retrieves object if drawn.
 		for (int i = squares.size() - 1; i > 0; i--){
 			
 			//System.out.println(squares.get(i).getMovie().getTitle());
 
 			if (squares.get(i).inShape(position) == true){
 				
 				if(squares.get(i).isDrawn()){
 					//System.out.println(squares.get(i).getMovie().getTitle());
 					//sqs.add(squares.get(i));
 					//System.out.println(squares.get(i).getMovie().getTitle());
 
 				}
 				else{
 					//System.out.println(squares.get(i).getMovie().getTitle());
 					squares.get(i).translate(xint, yint -10);
 					sqs.add(squares.get(i));
 					//System.out.println(squares.get(i).getMovie().getTitle());
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Checks an expands any shapes in the current radius. Does this by specifying and area
 	 * that will need to be redrawn. The biggest objects are put in front.
 	 */
 	public void checkRadius(int x, int y, View view){
 		Square_Shape square;
 		int largeRadius = (int) (radiusPX * 1.5);
 		
 		for (int i = 0; i < squares.size(); i++){
 			square = squares.get(i);
 			// if a square is double the distance away don't bother calculating it. This is a rough approximation keep in mind.
 			if ( (square.getX() > x - largeRadius && square.getX() < x + largeRadius) &&
 				 (square.getY() > y - largeRadius && square.getY() < y + largeRadius))
 				square.checkRadius(x, y, radiusPX);		
 		}		
 		
 		reorderZ();
 		
 		// invalidate only this rectangle.
 		view.invalidate(x - largeRadius, y - largeRadius, x + largeRadius, y + largeRadius);
 	}
 	
 	/**
 	 * Reorders z-indexing of shapes (list order) by the current size of
 	 * the shape. Greatest elements are last as they are rendered last
 	 * overlapping other elements.
 	 */
 	private void reorderZ(){
 		Collections.sort(squares, new Comparator<Object>(){
 			public int compare(Object obj1, Object obj2) {
 				Square_Shape sqr1 = (Square_Shape)obj1;
 				Square_Shape sqr2 = (Square_Shape)obj2;
 				
 				return sqr1.getSize() - sqr2.getSize();
 			}
         });
 	}
 	
 	/**
 	 * Sets all shapes to the default size
 	 */
 	public void goDefaultSize(){
 		for (int i = 0; i < squares.size(); i++)
 			squares.get(i).setDefaultSize();
 	}
 	
 	/*
 	 * Getters and setters
 	 */
 	
 	public void translateSquares(int xOffset, int yOffset, ArrayList<Square_Shape> sqs){
 		
 		for(Square_Shape sq:sqs)
 			
 			sq.translate(xOffset, yOffset);
 	}
 	
 	public void translate(int xOffset, int yOffset){
 		this.xOffset = xOffset;
 		this.yOffset = yOffset;
 		
 		for (Square_Shape square : squares)
 			square.translate(xOffset, yOffset);
 	}
 	
 	public void resetPosition(){
 		this.xOffset = 0;
 		this.yOffset = 0;
 		
 		for (Square_Shape square : squares)
 			square.resetPosition();
 	}
 	
 	public int getColor(String color){
 		
 		if(color.equals("Action")){
 			return Color.WHITE;
 		}
 		else if(color.equals("Drama")){
 			return Color.RED;	
 		}
 		else if(color.equals("Mystery")){
 			return Color.BLUE;
 		}
 		else if(color.equals("Comedy")){
 			return Color.MAGENTA;
 		}
 		else if(color.equals("Music")){
 			return Color.YELLOW;
 		}
 		else if(color.equals("War")){
 			return Color.GREEN;
 		}
 		else if(color.equals("Sci-Fi")){
 			return Color.rgb(255, 69, 0);//orange
 		}
 		else if(color.equals("Western")){
 			return Color.rgb(238, 221, 130);
 		}
 		else if(color.equals("Horror")){
 			return Color.rgb(152, 251, 152);
 		}
 		else if(color.equals("Family")){
 			return Color.rgb(160, 32, 240);
 		}
 		else if(color.equals("Fantasy")){
 			return Color.rgb(224, 255, 255);
 		}
 		else if(color.equals("Romance")){
 			return Color.rgb(255, 20, 147);
 		}
 		else if(color.equals("Short")){
 			return Color.GRAY;
 		}
 		else if(color.equals("Thriller")){
 			return Color.rgb(178, 34, 34);
 		}
 		return Color.WHITE;
 	}
 }

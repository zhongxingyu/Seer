 package de.fichtelmax.mastermind.vaadin;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 import org.vaadin.addon.extendedlabel.ExtendedContentMode;
 import org.vaadin.addon.extendedlabel.ExtendedLabel;
 
 import com.vaadin.annotations.Theme;
 import com.vaadin.annotations.Title;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 
 import de.fichtelmax.mastermind.vaadin.components.ColorPickupField;
 import de.fichtelmax.mastermind.vaadin.components.GuessButton;
 import de.fichtelmax.mastermind.vaadin.components.GuessInputField;
 import de.fichtelmax.mastermind.vaadin.components.SolutionZone;
 import de.fichtelmax.mastermind.vaadin.control.MastermindController;
 
 @Theme( "mastermind" )
 @Title( "Mastermind" )
 public class MastermindUI extends UI
 {
     private static final long  serialVersionUID = -8024776458773591101L;
     
     private final List<String> colors           = Arrays.asList( "#ff4500", // red
                                                         "green", // green
                                                         "#ffd700", // yellow
                                                         "#000080", // blue
                                                         "#7b68ee", // purple
                                                         "#9acd32" // yellow green
                                                 );
     
     private GuessInputField    input1;
     
     private GuessInputField    input2;
     
     private GuessInputField    input3;
     
     private GuessInputField    input4;
     
     private VerticalLayout     historyContainer;
     
     @Override
     protected void init( VaadinRequest request )
     {
        HorizontalLayout main = new HorizontalLayout();
         setContent( main );
         
         main.setSizeFull();
         
         GridLayout game = new GridLayout( 2, 3 );
         game.setMargin( true );
         
         SolutionZone solutionZone = new SolutionZone();
         Component centerZone = createCenterZone();
         Component colorPickupZone = createColorPickupZone( "90px" );
         
         List<GuessInputField> inputs = Arrays.asList( input1, input2, input3, input4 );
         MastermindController controller = new MastermindController( historyContainer, inputs, colors, solutionZone );
         
         solutionZone.setHeight( "100px" );
         solutionZone.setWidth( "400px" );
         game.addComponent( solutionZone, 0, 0 );
         
         game.addComponent( centerZone, 0, 1 );
         
         Panel colorPickup = new Panel();
         colorPickup.setWidth( "100px" );
         colorPickup.setHeight( "570px" );
         colorPickup.setContent( colorPickupZone );
         game.addComponent( colorPickup, 1, 1 );
         
         Panel guessButtonZone = new Panel();
         guessButtonZone.setWidth( "400px" );
         guessButtonZone.setHeight( "100px" );
         Button guessButton = new GuessButton( controller );
         guessButton.setSizeFull();
         guessButton.addStyleName( "guess" );
         guessButtonZone.setContent( guessButton );
         game.addComponent( guessButtonZone, 0, 2 );
         
         main.addComponent( game );
        main.setComponentAlignment( game, Alignment.TOP_CENTER );
         
         InputStream readmeStream = MastermindUI.class.getResourceAsStream( "/README.md" );
         try
         {
             String readme = IOUtils.toString( readmeStream );
             
             Label readmeLabel = new ExtendedLabel( readme, ExtendedContentMode.MARKDOWN );
             
             main.addComponent( readmeLabel );
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
     }
     
     private Component createColorPickupZone( String size )
     {
         
         Layout colorPickup = new VerticalLayout();
         for ( String color : colors )
         {
             ColorPickupField colorPickupField = new ColorPickupField( color );
             colorPickupField.setWidth( size );
             colorPickupField.setHeight( size );
             colorPickup.addComponent( colorPickupField );
         }
         
         return colorPickup;
     }
     
     private Component createCenterZone()
     {
         VerticalLayout centerLayout = new VerticalLayout();
         centerLayout.setSizeFull();
         centerLayout.addComponent( createHistoryZone() );
         Component inputZone = createGuessInputZone();
         centerLayout.addComponent( inputZone );
         centerLayout.setComponentAlignment( inputZone, Alignment.BOTTOM_CENTER );
         
         Panel centerPanel = new Panel();
         centerPanel.setWidth( "400px" );
         centerPanel.setHeight( "570px" );
         centerPanel.setContent( centerLayout );
         return centerPanel;
     }
     
     private Component createHistoryZone()
     {
         historyContainer = new VerticalLayout();
         
         // needed for scroll support
         Panel historyZone = new Panel();
         historyZone.setContent( historyContainer );
         historyZone.setHeight( "470px" );
         return historyZone;
     }
     
     private Component createGuessInputZone()
     {
         HorizontalLayout guessInput = new HorizontalLayout();
         input1 = new GuessInputField();
         input2 = new GuessInputField();
         input3 = new GuessInputField();
         input4 = new GuessInputField();
         
         guessInput.addComponent( input1 );
         guessInput.addComponent( input2 );
         guessInput.addComponent( input3 );
         guessInput.addComponent( input4 );
         
         return guessInput;
     }
 }

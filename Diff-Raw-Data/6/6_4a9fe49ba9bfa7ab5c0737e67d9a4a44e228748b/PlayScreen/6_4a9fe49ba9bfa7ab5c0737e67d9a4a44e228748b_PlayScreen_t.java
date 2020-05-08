 package pt.up.fe.lpoo.bombermen.screens;
 
 import pt.up.fe.lpoo.bombermen.Assets;
 import pt.up.fe.lpoo.bombermen.Bombermen;
 import pt.up.fe.lpoo.bombermen.Constants;
 import pt.up.fe.lpoo.bombermen.ControlPad;
 import pt.up.fe.lpoo.bombermen.Game;
 import pt.up.fe.lpoo.bombermen.Input;
 import pt.up.fe.lpoo.bombermen.entities.Player;
 
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.Net.Protocol;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.FPSLogger;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.net.Socket;
 import com.badlogic.gdx.utils.GdxRuntimeException;
 
 /**
  * The Class PlayScreen.
  */
 public class PlayScreen implements Screen
 {
 
     /** The game. */
     private Bombermen _game;
 
     /** The game impl. */
     public static Game _gameImpl;
 
     /** The ip. */
     private String _ip;
 
     /** The port. */
     private int _port;
 
     /** The socket. */
     private Socket _socket;
 
     /** The input. */
     private Input _input;
 
     /** The control pad. */
     private ControlPad _controlPad;
 
     /** The fps logger. */
     private FPSLogger _fpsLogger;
 
     /** The name. */
     private String _name;
 
     /**
      * Instantiates a new play screen.
      *
      * @param game the game
      */
     public PlayScreen(Bombermen game)
     {
         _game = game;
     }
 
     /** The offset min x. */
     private static float offsetMinX = 0;
 
     /** The offset min y. */
     private static float offsetMinY = 0;
 
     /** The old cam x. */
     private float _oldCamX = 0;
 
     /** The old cam y. */
     private float _oldCamY = 0;
 
     /* (non-Javadoc)
      * @see com.badlogic.gdx.Screen#render(float)
      */
     @Override
     public void render(float delta)
     {
         Gdx.gl.glClearColor(16f / 255, 120f / 255, 48f / 255, 1);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
         _gameImpl.Update();
 
         _game.GetStage().getCamera().update();
 
         _game.GetStage().getCamera().translate(-_oldCamX, -_oldCamY, 0f);
 
         Player p = _gameImpl.GetCurrentPlayer();
         if (p != null)
         {
             float camX = p.getX() - Constants.DEFAULT_WIDTH / 2f;
             float camY = p.getY() - Constants.DEFAULT_HEIGHT / 2f;
 
             float offsetMaxX = _gameImpl.GetMapWidth() * Constants.CELL_SIZE - Constants.DEFAULT_WIDTH;
             float offsetMaxY = _gameImpl.GetMapHeight() * Constants.CELL_SIZE - Constants.DEFAULT_HEIGHT;
 
             if (camX > offsetMaxX)
                 camX = offsetMaxX;
             else if (camX < offsetMinX) camX = offsetMinX;
 
             if (camY > offsetMaxY)
                 camY = offsetMaxY;
             else if (camY < offsetMinY) camY = offsetMinY;
            
            if (_gameImpl.GetMapWidth() * Constants.CELL_SIZE <= Constants.DEFAULT_WIDTH)
                camX = -(Constants.DEFAULT_WIDTH - _gameImpl.GetMapWidth() * Constants.CELL_SIZE) / 2;
            
            if (_gameImpl.GetMapHeight() * Constants.CELL_SIZE <= Constants.DEFAULT_HEIGHT)
                camY = -(Constants.DEFAULT_HEIGHT - _gameImpl.GetMapHeight() * Constants.CELL_SIZE) / 2;
 
             _oldCamX = camX;
             _oldCamY = camY;
 
             _game.GetStage().getCamera().translate(camX, camY, 0f);
         }
 
         _game.GetStage().act(delta);
         _game.GetStage().draw();
 
         _fpsLogger.log();
     }
 
     /* (non-Javadoc)
      * @see com.badlogic.gdx.Screen#resize(int, int)
      */
     @Override
     public void resize(int width, int height)
     {
         _game.GetStage().setViewport(Constants.DEFAULT_WIDTH, Constants.DEFAULT_HEIGHT, true);
         _game.GetStage().getCamera().translate(-_game.GetStage().getGutterWidth(), -_game.GetStage().getGutterHeight(), 0);
     }
 
     /* (non-Javadoc)
      * @see com.badlogic.gdx.Screen#show()
      */
     @Override
     public void show()
     {
         _gameImpl = new Game(_game.GetStage(), _socket, _name);
 
         _fpsLogger = new FPSLogger();
 
         _input = new Input(_gameImpl, _game.GetStage().getCamera());
 
         InputMultiplexer multiplexer = new InputMultiplexer();
         multiplexer.addProcessor(_game.GetStage());
         multiplexer.addProcessor(_input);
         Gdx.input.setInputProcessor(multiplexer);
 
         AssetManager manager = new AssetManager();
         Assets.Manager = manager;
         // Texture.setAssetManager(manager);
 
         manager.load("data/images/bomb.png", Texture.class);
         manager.load("data/images/bombButton.png", Texture.class);
         manager.load("data/images/bomberman.png", Texture.class);
         manager.load("data/images/dpad.png", Texture.class);
         manager.load("data/images/explosion.png", Texture.class);
         manager.load("data/images/powerup.png", Texture.class);
         manager.load("data/images/wall.png", Texture.class);
 
         manager.load("data/musics/bgm_01.mp3", Music.class);
         manager.load("data/musics/bgm_02.mp3", Music.class);
         manager.load("data/musics/bgm_03.mp3", Music.class);
         manager.load("data/musics/bgm_menu.mp3", Music.class);
         manager.load("data/musics/bgm_startGame.mp3", Music.class);
 
         manager.load("data/sounds/bomb_explosion.wav", Sound.class);
         manager.load("data/sounds/bomb_place.wav", Sound.class);
         manager.load("data/sounds/dying.wav", Sound.class);
         manager.load("data/sounds/menu_back.wav", Sound.class);
         manager.load("data/sounds/menu_select.wav", Sound.class);
         manager.load("data/sounds/powerup.wav", Sound.class);
         manager.load("data/sounds/victory.wav", Sound.class);
 
         manager.finishLoading(); // loads all assets (proper way should be to call manager.update() in render method)
 
         if (Gdx.app.getType() == ApplicationType.Android || Constants.SHOW_PAD)
         {
             _controlPad = new ControlPad();
             _controlPad.SetSize(Constants.DEFAULT_PAD_WIDTH, Constants.DEFAULT_PAD_HEIGHT);
             _input.SetControlPad(_controlPad);
         }
 
         Assets.PlayMusic("bgm_02", true); // todo: move me somewhere else
     }
 
     /* (non-Javadoc)
      * @see com.badlogic.gdx.Screen#hide()
      */
     @Override
     public void hide()
     {
     }
 
     /* (non-Javadoc)
      * @see com.badlogic.gdx.Screen#pause()
      */
     @Override
     public void pause()
     {
     }
 
     /* (non-Javadoc)
      * @see com.badlogic.gdx.Screen#resume()
      */
     @Override
     public void resume()
     {
     }
 
     /* (non-Javadoc)
      * @see com.badlogic.gdx.Screen#dispose()
      */
     @Override
     public void dispose()
     {
     }
 
     /**
      * Sets the server ip address.
      *
      * @param ip the ip
      */
     public void SetServerIPAddress(String ip)
     {
         _ip = ip;
     }
 
     /**
      * Sets the server port.
      *
      * @param port the port
      */
     public void SetServerPort(int port)
     {
         _port = port;
     }
 
     /**
      * Sets the player name.
      *
      * @param name the name
      */
     public void SetPlayerName(String name)
     {
         _name = name;
     }
 
     /**
      * Connect.
      *
      * @return the socket
      * @throws GdxRuntimeException the gdx runtime exception
      */
     public Socket Connect() throws GdxRuntimeException
     {
         _socket = Gdx.net.newClientSocket(Protocol.TCP, _ip, _port, null);
         return _socket;
     }
 }

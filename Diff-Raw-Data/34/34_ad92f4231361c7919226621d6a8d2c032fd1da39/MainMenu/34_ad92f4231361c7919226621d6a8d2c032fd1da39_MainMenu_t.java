 /*
  The MIT License (MIT)
 
  Copyright (c) 2013 devnewton <devnewton@bci.im>
 
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
 
  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.
 
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
  */
 package org.geekygoblin.nedetlesmaki.game.components.ui;
 
 import com.artemis.Component;
 import com.artemis.managers.GroupManager;
 import im.bci.jnuit.NuitToolkit;
 import java.util.Arrays;
 
 import org.lwjgl.LWJGLException;
 
 import im.bci.jnuit.widgets.AudioConfigurator;
 import im.bci.jnuit.widgets.Button;
 import im.bci.jnuit.widgets.Container;
 import im.bci.jnuit.widgets.ControlsConfigurator;
 import im.bci.jnuit.widgets.Root;
 import im.bci.jnuit.widgets.Table;
 import im.bci.jnuit.widgets.VideoConfigurator;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import im.bci.jnuit.NuitPreferences;
 import im.bci.jnuit.background.TexturedBackground;
 import im.bci.jnuit.NuitRenderer;
 import im.bci.jnuit.animation.IAnimation;
 import im.bci.jnuit.animation.PlayMode;
 import org.geekygoblin.nedetlesmaki.game.Game;
 import org.geekygoblin.nedetlesmaki.game.Group;
 import org.geekygoblin.nedetlesmaki.game.MainLoop;
 import im.bci.jnuit.lwjgl.assets.IAssets;
 import org.geekygoblin.nedetlesmaki.game.components.ui.AlterContainer;
 import org.geekygoblin.nedetlesmaki.game.components.IngameControls;
 import org.geekygoblin.nedetlesmaki.game.components.Triggerable;
 import org.geekygoblin.nedetlesmaki.game.events.HideMenuTrigger;
 
 @Singleton
 public class MainMenu extends Component {
 
     private final Root root;
     private AlterContainer mainMenu;
     private VideoConfigurator videoConfigurator;
     private AudioConfigurator audioConfigurator;
     private AlterContainer optionsMenu;
     private AlterContainer extrasMenu;
     private ControlsConfigurator menuControls, gameControls;
     private final LevelSelector levelSelector;
     private final MainLoop mainLoop;
     private final NuitToolkit toolkit;
     private final NuitRenderer nuitRenderer;
     private final IAssets assets;
     private final Game game;
     private final Provider<HideMenuTrigger> hideMenuTrigger;
     private NuitPreferences preferences;
     private CutScenes cutscenes;
 
     @Inject
     public MainMenu(MainLoop mainLoop, Game g, NuitToolkit toolkit, NuitRenderer nuitRenderer, IAssets assets, LevelSelector levelSelector, Provider<HideMenuTrigger> hideMenuTrigger, IngameControls ingameControls, CutScenes cutscenes, NuitPreferences preferences) throws LWJGLException {
         this.mainLoop = mainLoop;
         this.toolkit = toolkit;
         this.nuitRenderer = nuitRenderer;
         this.assets = assets;
         this.game = g;
         this.hideMenuTrigger = hideMenuTrigger;
         this.preferences = preferences;
         this.cutscenes = cutscenes;
         root = new Root(toolkit);
         root.setBackground(new TexturedBackground(MainMenu.this.assets.getAnimations("menu.png").getFirst().start(PlayMode.LOOP)));
         this.levelSelector = levelSelector;
         root.add(levelSelector);
         initVideo();
         initAudio();
         initMenuControls();
         initGameControls(ingameControls);
         initOptions();
         initMain();
         initExtras();
         root.show(mainMenu);
     }
 
     private void initVideo() throws LWJGLException {
         videoConfigurator = new VideoConfigurator(toolkit) {
             @Override
             protected void changeVideoSettings() {
                 super.changeVideoSettings();
                 assets.setIcon("icon.png");
             }
         };
         videoConfigurator.setBackground(new TexturedBackground(assets.getAnimations("menu_video.png").getFirst().start(PlayMode.LOOP)));
         root.add(videoConfigurator);
     }
 
     private void initAudio() {
         audioConfigurator = new AudioConfigurator(toolkit);
         audioConfigurator.setBackground(new TexturedBackground(assets.getAnimations("menu_audio.png").getFirst().start(PlayMode.LOOP)));
     }
 
     private void initMain() {
 
         IAnimation buttonClassicBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("2_normal");
         IAnimation buttonClassicFocusedBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("2_survol");
         IAnimation buttonSmallBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("1_normal");
         IAnimation buttonSmallFocusedBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("1_survol");
 
        mainMenu = new AlterContainer();
         final Button startButton = new Button(toolkit, "main.menu.button.start") {
             @Override
             public void onOK() {
                 onStartGame();
             }
         };
         startButton.setX(800);
         startButton.setY(800);
         startButton.setWidth(317);
         startButton.setHeight(74);
         startButton.setBackground(new TexturedBackground(buttonSmallBackgroundAnimation.start(PlayMode.LOOP)));
         startButton.setFocusedBackground(new TexturedBackground(buttonSmallFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         mainMenu.add(startButton);
 
         final Button resumeButton = new Button(toolkit, "main.menu.button.resume") {
             @Override
             public void onOK() {
                 if (!game.getManager(GroupManager.class).getEntities(Group.LEVEL).isEmpty()) {
                     game.addEntity(game.createEntity().addComponent(new Triggerable(hideMenuTrigger.get())));
                 }
             }
         };
         resumeButton.setX(800);
         resumeButton.setY(900);
         resumeButton.setWidth(317);
         resumeButton.setHeight(74);
         resumeButton.setBackground(new TexturedBackground(buttonSmallBackgroundAnimation.start(PlayMode.LOOP)));
         resumeButton.setFocusedBackground(new TexturedBackground(buttonSmallFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         mainMenu.add(resumeButton);
 
         final Button optionsButton = new Button(toolkit, "main.menu.button.options") {
             @Override
             public void onOK() {
                 root.show(optionsMenu);
             }
         };
         optionsButton.setX(1200);
         optionsButton.setY(800);
         optionsButton.setWidth(317);
         optionsButton.setHeight(74);
         optionsButton.setBackground(new TexturedBackground(buttonSmallBackgroundAnimation.start(PlayMode.LOOP)));
         optionsButton.setFocusedBackground(new TexturedBackground(buttonSmallFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         mainMenu.add(optionsButton);
 
         final Button quitButton = new Button(toolkit, "main.menu.button.quit") {
             @Override
             public void onOK() {
                 mainLoop.setCloseRequested(true);
             }
         };
         quitButton.setX(1200);
         quitButton.setY(900);
         quitButton.setWidth(317);
         quitButton.setHeight(74);
         quitButton.setBackground(new TexturedBackground(buttonSmallBackgroundAnimation.start(PlayMode.LOOP)));
         quitButton.setFocusedBackground(new TexturedBackground(buttonSmallFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         mainMenu.add(quitButton);
 
         final Button extrasButton = new Button(toolkit, "main.menu.button.extras") {
             @Override
             public void onOK() {
                 root.show(extrasMenu);
             }
         };
         extrasButton.setX(1600);
         extrasButton.setY(1000);
         extrasButton.setWidth(230);
         extrasButton.setHeight(54);
         extrasButton.setBackground(new TexturedBackground(buttonSmallBackgroundAnimation.start(PlayMode.LOOP)));
         extrasButton.setFocusedBackground(new TexturedBackground(buttonSmallFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         mainMenu.add(extrasButton);
         root.add(mainMenu);
     }
 
     private void initOptions() {
 
         IAnimation buttonClassicBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("2_normal");
         IAnimation buttonClassicFocusedBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("2_survol");
         IAnimation buttonSmallBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("1_normal");
         IAnimation buttonSmallFocusedBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("1_survol");
         
        optionsMenu = new AlterContainer();
         optionsMenu.setBackground(new TexturedBackground(assets.getAnimations("menu_options.png").getFirst().start(PlayMode.LOOP)));
         final Button videoButton = new Button(toolkit, "options.menu.button.video") {
             @Override
             public void onOK() {
                 root.show(videoConfigurator);
             }
         };
         videoButton.setX(905);
         videoButton.setY(522);
         videoButton.setWidth(396);
         videoButton.setHeight(92);
         videoButton.setBackground(new TexturedBackground(buttonSmallBackgroundAnimation.start(PlayMode.LOOP)));
         videoButton.setFocusedBackground(new TexturedBackground(buttonSmallFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         optionsMenu.add(videoButton);
 
         final Button audioButton = new Button(toolkit, "options.menu.button.audio") {
             @Override
             public void onOK() {
                 root.show(audioConfigurator);
             }
         };
         audioButton.setX(1361);
         audioButton.setY(522);
         audioButton.setWidth(396);
         audioButton.setHeight(92);
         audioButton.setBackground(new TexturedBackground(buttonSmallBackgroundAnimation.start(PlayMode.LOOP)));
         audioButton.setFocusedBackground(new TexturedBackground(buttonSmallFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         optionsMenu.add(audioButton);
         
         final Button gameControlsButton = new Button(toolkit, "options.menu.button.game.controls") {
             @Override
             public void onOK() {
                 root.show(gameControls);
             }
         };
         gameControlsButton.setX(905);
         gameControlsButton.setY(668);
         gameControlsButton.setWidth(852);
         gameControlsButton.setHeight(92);
         gameControlsButton.setBackground(new TexturedBackground(buttonClassicBackgroundAnimation.start(PlayMode.LOOP)));
         gameControlsButton.setFocusedBackground(new TexturedBackground(buttonClassicFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         optionsMenu.add(gameControlsButton);
 
         final Button menuControlsButton = new Button(toolkit, "options.menu.button.menu.controls") {
             @Override
             public void onOK() {
                 root.show(menuControls);
             }
         };
         menuControlsButton.setX(905);
         menuControlsButton.setY(814);
         menuControlsButton.setWidth(852);
         menuControlsButton.setHeight(92);
         menuControlsButton.setBackground(new TexturedBackground(buttonClassicBackgroundAnimation.start(PlayMode.LOOP)));
         menuControlsButton.setFocusedBackground(new TexturedBackground(buttonClassicFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         optionsMenu.add(menuControlsButton);
 
         final Button backButton = new Button(toolkit, "options.menu.button.back") {
             @Override
             public void onOK() {
                 root.show(mainMenu);
             }
         };
         backButton.setX(1361);
         backButton.setY(960);
         backButton.setWidth(396);
         backButton.setHeight(92);
         backButton.setBackground(new TexturedBackground(buttonSmallBackgroundAnimation.start(PlayMode.LOOP)));
         backButton.setFocusedBackground(new TexturedBackground(buttonSmallFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         optionsMenu.add(backButton);
         root.add(optionsMenu);
     }
 
     private void initMenuControls() {
         menuControls = new ControlsConfigurator(toolkit, Arrays.asList(toolkit.getMenuUp(), toolkit.getMenuDown(), toolkit.getMenuLeft(), toolkit.getMenuRight(), toolkit.getMenuOK(), toolkit.getMenuCancel()), null) {
             @Override
             public void onBack() {
                 root.show(optionsMenu);
             }
         };
         menuControls.setBackground(new TexturedBackground(assets.getAnimations("default_menu.png").getFirst().start(PlayMode.LOOP)));
         root.add(menuControls);
     }
 
     private void initGameControls(IngameControls ingameControls) {
         gameControls = new ControlsConfigurator(toolkit, Arrays.asList(ingameControls.getUp().getAction(), ingameControls.getDown().getAction(), ingameControls.getLeft().getAction(), ingameControls.getRight().getAction(), ingameControls.getRewind().getAction(), ingameControls.getShowMenu().getAction()), null) {
             @Override
             public void onBack() {
                 root.show(optionsMenu);
             }
         };
         gameControls.setBackground(new TexturedBackground(assets.getAnimations("default_menu.png").getFirst().start(PlayMode.LOOP)));
         root.add(gameControls);
     }
 
     public void update() {
         root.update(game.getDelta());
     }
 
     public void draw() {
         nuitRenderer.render(root);
     }
 
     private void onStartGame() {
         if (!preferences.getBoolean("cutscenes.intro.seen", false)) {
             NedDialogue intro = new NedDialogue(toolkit, assets) {
 
                 @Override
                 public void close() {
                     super.close();
                     root.show(levelSelector);
                 }
 
             };
             cutscenes.createIntro(intro);
             root.show(intro);
             preferences.putBoolean("cutscenes.intro.seen", true);
         } else {
             root.show(levelSelector);
         }
     }
 
     public void show() {
         root.show(mainMenu);
     }
 
     public void showLevelMenu() {
         root.show(levelSelector);
     }
 
     private void initExtras() {
         extrasMenu = new ExtrasMenu(toolkit, root, mainMenu, assets, cutscenes);
         root.add(extrasMenu);
     }
 }

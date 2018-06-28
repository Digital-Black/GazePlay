package net.gazeplay;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.ui.DefaultTranslator;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;

/**
 * Created by schwab on 17/12/2016.
 */
@Slf4j
public class GazePlay extends Application {

    @Getter
    private static GazePlay instance;

    @Getter
    private HomeMenuScreen homeMenuScreen;
    
    @Getter
    private UserProfilContext userProfileScreen;

    @Getter
    private Stage primaryStage;

    @Getter
    private Translator translator;

    @Getter
    private Scene primaryScene;

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public GazePlay() {
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        Screen screen = Screen.getPrimary();

        Rectangle2D screenBounds = screen.getBounds();

        primaryStage.setWidth(screenBounds.getWidth() * 0.95);
        primaryStage.setHeight(screenBounds.getHeight() * 0.90);

        primaryStage.setMaximized(false);

        final Configuration config = Configuration.getInstance();
        final Multilinguism multilinguism = Multilinguism.getSingleton();

        translator = new DefaultTranslator(config, multilinguism);

        homeMenuScreen = HomeMenuScreen.newInstance(this, config);
        userProfileScreen = userProfileScreen.newInstance(this, config);

       // this.primaryScene = new Scene(homeMenuScreen.getRoot(), primaryStage.getWidth(), primaryStage.getHeight(),
       //         Color.BLACK);
        
        this.primaryScene = new Scene(userProfileScreen.getRoot(), primaryStage.getWidth(), primaryStage.getHeight(),
                Color.BLACK);
        
        CssUtil.setPreferredStylesheets(config, primaryScene);

        primaryStage.setTitle("GazePlay");

        primaryStage.setScene(primaryScene);

        primaryStage.setOnCloseRequest((WindowEvent we) -> primaryStage.close());

        userProfileScreen.setUpOnStage(primaryScene);
        //homeMenuScreen.setUpOnStage(primaryScene);

        primaryStage.setFullScreen(true);
        primaryStage.centerOnScreen();
        primaryStage.show();

        this.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.SPACE && config.isGazeMouseEnable()) {
                    Platform.runLater(() -> {
                        try {
                            Robot robot = new Robot();
                            robot.mousePress(InputEvent.BUTTON1_MASK);
                            robot.mouseRelease(InputEvent.BUTTON1_MASK);
                        } catch (AWTException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    });
                }
            }
        });

        this.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.S && config.isGazeMouseEnable()) {
                    config.isMouseFree = !config.isMouseFree;
                }
            }
        });
    }

    public void onGameLaunch(GameContext gameContext) {
        gameContext.setUpOnStage(primaryScene);
        gameContext.updateMusicControler();
    }

    public void onReturnToMenu() {

        homeMenuScreen.setGazeDeviceManager(GazeDeviceManagerFactory.getInstance().createNewGazeListener());
        homeMenuScreen.getGameMenuFactory().addFilters();

        homeMenuScreen.setUpOnStage(primaryScene);
        final BackgroundMusicManager musicMananger = BackgroundMusicManager.getInstance();
        musicMananger.onEndGame();

        homeMenuScreen.getGameMenuFactory().play();

        log.info("here is the list of pausedEvent = {}", homeMenuScreen.getGameMenuFactory().getPausedEvents());

        // homeMenuScreen.updateMusicControler();
    }

    public void onDisplayStats(StatsContext statsContext) {
        statsContext.setUpOnStage(primaryScene);
    }

    public void onDisplayConfigurationManagement(ConfigurationContext configurationContext) {
        configurationContext.setUpOnStage(primaryScene);
    }

    public void toggleFullScreen() {

        boolean fullScreen = !primaryStage.isFullScreen();
        log.info("fullScreen = {}", fullScreen);
        primaryStage.setFullScreen(fullScreen);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public boolean isFullScreen() {
        return primaryStage.isFullScreen();
    }

    public ReadOnlyBooleanProperty getFullScreenProperty() {
        return primaryStage.fullScreenProperty();
    }

}

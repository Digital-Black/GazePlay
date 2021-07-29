package net.gazeplay.games.cassebrique;

import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;

public class CasseBrique implements GameLifeCycle {

    final private IGameContext gameContext;
    final private Stats stats;
    final private CasseBriqueGameVariant variant;

    final private Dimension2D dimension2D;

    private Circle ball;

    private Rectangle barre;

    private double widthbarre;
    private double heightbarre;
    private double sizeball;

    private double rad;
    private double speed;
    
    private double oldXbarre;

    final private ArrayList<Rectangle> walllist;
    final private ArrayList<Rectangle> wallhardlist;
    final private ArrayList<Rectangle> wallremovelist;

    private boolean touchbar;
    private boolean touchX;
    private boolean touchY;

    CasseBrique(final IGameContext gameContext, final Stats stats, final CasseBriqueGameVariant variant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;

        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        walllist = new ArrayList<>();
        wallhardlist = new ArrayList<>();
        wallremovelist = new ArrayList<>();
    }

    public void launch(){
        widthbarre = dimension2D.getWidth()/2.5;
        heightbarre = dimension2D.getHeight()/35;
        sizeball = dimension2D.getHeight()*0.015;

        initbackground();

        createbarre();
        createball();
        oldXbarre = barre.getX();

        if (variant.equals(CasseBriqueGameVariant.SPACE)){
            space();
        }
        else if (variant.equals(CasseBriqueGameVariant.FACE)){
            face();
        }
        else if (variant.equals(CasseBriqueGameVariant.SMILEY)){
            smiley();
        }
        else {

        }

        speed = 0;
        rad = 0;
        move();
        startafterdelay(5000);

        {
            Scene gameContextScene = gameContext.getPrimaryScene();

            EventHandler<GazeEvent> recordGazeMovements = e -> {
                Point2D toSceneCoordinate = gameContextScene.getRoot().localToScene(e.getX(), e.getY());
                barre.setX(toSceneCoordinate.getX() - widthbarre/2);

            };

            EventHandler<MouseEvent> recordMouseMovements = e -> {
                Point2D toSceneCoordinate = gameContextScene.getRoot().localToScene(e.getX(), e.getY());
                if (((toSceneCoordinate.getX()-dimension2D.getWidth()/2)*(toSceneCoordinate.getX()-dimension2D.getWidth()/2) + (toSceneCoordinate.getY()-dimension2D.getHeight()/2)*(toSceneCoordinate.getY()-dimension2D.getHeight()/2))>0.15) {
                    barre.setX(toSceneCoordinate.getX() - widthbarre/2);
                }

            };

            gameContextScene.getRoot().addEventFilter(GazeEvent.GAZE_MOVED, recordGazeMovements);
            gameContextScene.getRoot().addEventFilter(MouseEvent.MOUSE_MOVED, recordMouseMovements);
        }

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    public void dispose(){
        walllist.clear();
        wallhardlist.clear();
        wallremovelist.clear();
        gameContext.getChildren().clear();
    }

    private void ballfall(){
        speed = 0;
        rad = 0;
        gameContext.getChildren().remove(ball);
        createball();
        startafterdelay(1500);
    }

    private void createball(){
        ball = new Circle(barre.getX() + widthbarre/2, dimension2D.getHeight() * 0.85, sizeball);
        gameContext.getChildren().add(ball);
    }

    private void createbarre(){
        barre = new Rectangle(dimension2D.getWidth()/2 - widthbarre/2, dimension2D.getHeight() * 0.92 - heightbarre/2, widthbarre, heightbarre);
        barre.setFill(Color.BROWN);
        gameContext.getChildren().add(barre);
    }

    private void startafterdelay(int delay){
        PauseTransition wait = new PauseTransition(Duration.millis(delay));
        wait.setOnFinished(e -> speed = 8);
        wait.play();
    }

    private void move(){
        PauseTransition wait = new PauseTransition(Duration.millis(15));
        wait.setOnFinished(e -> {
            wait.play();
            touchbar = false;
            touchX = false;
            touchY = false;
            if (speed==0){
                ball.setFill(Color.GRAY);
                ball.setCenterX(barre.getX() + widthbarre/2);
            }
            else {
                ball.setFill(Color.RED);
            }
            if (ball.getCenterX() + sizeball >= dimension2D.getWidth()){
                touchX = true;
            }
            else if (ball.getCenterX() - sizeball <= 0){
                touchX = true;
            }
            if (ball.getCenterY() - sizeball <= 0){
                touchY = true;
            }
            else if (ball.getCenterY()>=dimension2D.getHeight()){
                ballfall();
            }
            bounceBarre();
            for (Rectangle wall : walllist){
                bounceWall(wall, true);
            }
            for (Rectangle wall : wallhardlist){
                bounceWall(wall, false);
            }
            if (touchX){
                rad = -rad;
            }
            if (touchY){
                rad = -rad + Math.PI;
            }
            if (touchbar){
                radInertiaBarre();
            }
            walllist.removeAll(wallremovelist);
            gameContext.getChildren().removeAll(wallremovelist);
            wallremovelist.clear();
            testwin();
            ball.setCenterX(ball.getCenterX() + speed * Math.sin(rad));
            ball.setCenterY(ball.getCenterY() + speed * Math.cos(rad));
            /*boolean test = false;
            for (Rectangle wall : walllist){
                test = test || ball.getCenterX()+sizeball>wall.getX() && ball.getCenterX()-sizeball<wall.getX()+wall.getWidth() && ball.getCenterY()+sizeball>wall.getY() && ball.getCenterY()-sizeball<wall.getY()+wall.getHeight();
            }
            for (Rectangle wall : wallhardlist){
                test = test || ball.getCenterX()+sizeball>wall.getX() && ball.getCenterX()-sizeball<wall.getX()+wall.getWidth() && ball.getCenterY()+sizeball>wall.getY() && ball.getCenterY()-sizeball<wall.getY()+wall.getHeight();
            }
            test = test || ball.getCenterX()+sizeball>barre.getX() && ball.getCenterX()-sizeball<barre.getX()+barre.getWidth() && ball.getCenterY()+sizeball>barre.getY() && ball.getCenterY()-sizeball<barre.getY()+barre.getHeight();
            if (test){
                ball.setCenterX(ball.getCenterX() - speed * Math.sin(rad));
                ball.setCenterY(ball.getCenterY() - speed * Math.cos(rad));
            }*/
            oldXbarre = barre.getX();
        });
        wait.play();
    }

    private void bounceBarre(){
        if (onLeft(barre) || onRight(barre)){
            touchX = true;
            touchbar = true;
        }
        if (onTop(barre) || onBottom(barre)){
            touchY = true;
            touchbar = true;
        }
    }

    private void bounceWall(Rectangle wall, boolean remove){
        boolean touch = false;
        if (onLeft(wall) || onRight(wall)) {
            touchX = true;
            touch = true;
        }
        if (onTop(wall) || onBottom(wall)) {
            touchY = true;
            touch = true;
        }
        if (touch && remove){
            wallremovelist.add(wall);
            stats.incrementNumberOfGoalsReached();
        }
    }

    private void radInertiaBarre(){
        double e = 1-Math.abs(Math.sin(rad));
        double d = (oldXbarre - barre.getX())/dimension2D.getWidth();
        rad += e*Math.cbrt(d);
    }

    private void testwin(){
        if (walllist.isEmpty()){
            stats.stop();

            gameContext.updateScore(stats, this);

            gameContext.playWinTransition(500, actionEvent -> {

                gameContext.getGazeDeviceManager().clear();

                gameContext.clear();

                gameContext.showRoundStats(stats, this);
            });
        }
    }

    private boolean onTop(Rectangle wall){
        return ball.getCenterY() + sizeball >= wall.getY() && ball.getCenterY() - sizeball <= wall.getY() && ball.getCenterX() + sizeball >= wall.getX() && ball.getCenterX() - sizeball <= wall.getX() + wall.getWidth();
    }

    private boolean onBottom(Rectangle wall){
        return ball.getCenterY() - sizeball <= wall.getY() + wall.getHeight() && ball.getCenterY() + sizeball >= wall.getY() + wall.getHeight() && ball.getCenterX() + sizeball >= wall.getX() && ball.getCenterX() - sizeball <= wall.getX() + wall.getWidth();
    }

    private boolean onLeft(Rectangle wall){
        return ball.getCenterX() + sizeball >= wall.getX() && ball.getCenterX() - sizeball <= wall.getX() && ball.getCenterY() + sizeball >= wall.getY() && ball.getCenterY() - sizeball <= wall.getY() + wall.getHeight();
    }

    private boolean onRight(Rectangle wall){
        return ball.getCenterX() - sizeball <= wall.getX() + wall.getWidth() && ball.getCenterX() + sizeball >= wall.getX() + wall.getWidth() && ball.getCenterY() + sizeball >= wall.getY() && ball.getCenterY() - sizeball <= wall.getY() + wall.getHeight();
    }

    private void initbackground(){

    }

    private void build(Color[][] map){
        int width = map[0].length;
        int height = map.length;
        double widthwall = dimension2D.getWidth()/width;
        double heightwall = dimension2D.getHeight()*0.8/height;
        Rectangle wall;
        for (int i=0; i<width; i++){
            for (int j=0; j<height; j++){
                if (map[j][i]==Color.GRAY){
                    wall = new Rectangle(i*widthwall, j*heightwall, widthwall, heightwall);
                    wall.setFill(Color.GRAY);
                    wallhardlist.add(wall);
                    gameContext.getChildren().add(wall);
                }
                else if (map[j][i]!=Color.TRANSPARENT){
                    wall = new Rectangle(i*widthwall, j*heightwall, widthwall, heightwall);
                    wall.setFill(map[j][i]);
                    walllist.add(wall);
                    gameContext.getChildren().add(wall);
                }
            }
        }
    }
    
    private void space(){
        Color cT = Color.TRANSPARENT;
        Color c1 = Color.CYAN;
        Color[][] map = new Color[][]
            {
                {cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT},
                {cT,cT,cT,cT,cT,c1,cT,cT,cT,cT,cT,cT,cT,c1,cT,cT,cT,cT,cT},
                {cT,cT,cT,cT,cT,cT,c1,cT,cT,cT,cT,cT,c1,cT,cT,cT,cT,cT,cT},
                {cT,cT,cT,cT,cT,c1,c1,c1,c1,c1,c1,c1,c1,c1,cT,cT,cT,cT,cT},
                {cT,cT,cT,cT,c1,c1,cT,c1,c1,c1,c1,c1,cT,c1,c1,cT,cT,cT,cT},
                {cT,cT,cT,c1,c1,c1,c1,c1,c1,c1,c1,c1,c1,c1,c1,c1,cT,cT,cT},
                {cT,cT,cT,c1,c1,c1,c1,c1,c1,c1,c1,c1,c1,c1,cT,c1,cT,cT,cT},
                {cT,cT,cT,c1,cT,c1,cT,cT,cT,cT,cT,cT,cT,c1,cT,c1,cT,cT,cT},
                {cT,cT,cT,cT,cT,cT,c1,c1,c1,cT,c1,c1,c1,cT,cT,cT,cT,cT,cT},
                {cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT,cT}
            };
        build(map);
    }

    private void face(){

    }

    private void smiley(){

    }
}

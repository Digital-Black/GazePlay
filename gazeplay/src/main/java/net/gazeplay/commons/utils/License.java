package net.gazeplay.commons.utils;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by schwab on 22/12/2016.
 */
@Slf4j
public class License extends Rectangle {

    public License(double X, double Y, double width, double height, GazePlay gazePlay, Scene scene, Group root) {
        super(X, Y, width, height);
        this.setFill(new ImagePattern(new Image("data/common/images/license.png"), 0, 0, 1, 1, true));
        EventHandler<Event> homeEvent = e -> {
            if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {
                root.getChildren().add(licence(width, height));
            }
        };
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);
    }

    private Text licence(double width, double height) {
        Text text = new Text();

        text.setX(width * 0.1);
        text.setY(height * 0.1);
        text.setFont(new Font(20));

        text.setFill(new Color(1, 1, 1, 1));

        String content = loadLicenseContentAsString();
        text.setText(content);

        return text;

    }

    private String loadLicenseContentAsString() {
        URL resource = getClass().getClassLoader().getResource("data/common/licence.txt");
        if (resource == null) {
            return null;
        }
        String content;
        try {
            content = IOUtils.toString(resource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content;
    }

}

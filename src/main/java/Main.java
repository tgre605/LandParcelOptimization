import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image ;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import org.locationtech.jts.geom.Polygon;

import java.awt.*;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

class DragContext {

    double mouseAnchorX;
    double mouseAnchorY;

    double translateAnchorX;
    double translateAnchorY;

}


public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Path currentDir = Paths.get(".");
        JsonReader reader = new JsonReader(currentDir.toAbsolutePath() + "/input/simpleroadnetwork.json");

        ArrayList<landParcel> landParcels  = reader.getParcels();

        ArrayList<Polygon> boundingBoxes = new ArrayList<>();
        boundingBoxes.add( new LandParcelOptimizer().BoundingBoxOptimization(landParcels.get(0)));

        SceneRenderer sceneRenderer = new SceneRenderer();
        sceneRenderer.start(stage, landParcels, boundingBoxes);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import java.awt.*;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

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
        JsonReader reader = new JsonReader(currentDir.toAbsolutePath() + "/input/roadnetwork.json");
        ArrayList<landParcel> landParcels  = reader.getParcels();
        LandParcelOptimizer landParcelOptimizer = new LandParcelOptimizer();
        SceneRenderer sceneRenderer = new SceneRenderer();
        for (landParcel parcels: landParcels) {
            parcels.surroundingParcels(reader);
            Geometry[] footprints = landParcelOptimizer.BoundingBoxOptimization(parcels, 10, 0.5, 0.25);
            SceneRenderer.render(footprints);
        }

        sceneRenderer.start(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

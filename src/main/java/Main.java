import javafx.application.Application;
import javafx.stage.Stage;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

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
        ArrayList<LandParcel> LandParcels = reader.getParcels();
        LandParcelOptimizer landParcelOptimizer = new LandParcelOptimizer();
        SceneRenderer sceneRenderer = new SceneRenderer();
        for (LandParcel parcels: LandParcels) {
            parcels.surroundingParcels(reader);
            parcels = landParcelOptimizer.BoundingBoxOptimization(parcels, 5, 0.25, 0.9,30, 6);;
            SceneRenderer.render(parcels.getFootprintGeometries());
            for(int i = 0; i < parcels.subroads.size(); i++){
                SceneRenderer.renderLine(new Coordinate[]{parcels.subroads.get(i).coordinateA, parcels.subroads.get(i).coordinateB});
            }
        }

        Footprint testFootprint = LandParcels.get(2).footprints.get(128);
        Coordinate coordinateA = testFootprint.geometry.getCoordinates()[0];
        Coordinate coordinateB = testFootprint.geometry.getCoordinates()[1];
        Coordinate[] coordinates = new Coordinate[]{coordinateA, coordinateB};
        testFootprint.roadsideEdges.put(coordinates, new Road(null, null, null));
        SceneRenderer.render(testFootprint.geometry);

        System.out.println("test");
        sceneRenderer.start(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

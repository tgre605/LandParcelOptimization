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
            Geometry[] footprints = landParcelOptimizer.BoundingBoxOptimization(parcels, 5, 0.25, 0.9,30);
            SceneRenderer.render(footprints);
        }
        Footprint testFootprint = LandParcels.get(2).footprints.get(180);
        Coordinate coordinateA = testFootprint.geometry.getCoordinates()[2];
        Coordinate coordinateB = testFootprint.geometry.getCoordinates()[3];
        Coordinate[] coordinates = new Coordinate[]{coordinateA, coordinateB};
        testFootprint.roadsideEdges.put(coordinates, new Road(null, null, null));
        BuildingPlacer placer = new BuildingPlacer();
        placer.setRoadCentreT(testFootprint);
        SceneRenderer.render(testFootprint.geometry);
        SceneRenderer.render(testFootprint.roadCentre);

        System.out.println("test");
        sceneRenderer.start(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

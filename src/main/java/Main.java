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
        BuildingPlacer placer = new BuildingPlacer();
        SceneRenderer sceneRenderer = new SceneRenderer();
        for (LandParcel parcel : LandParcels) {
            System.out.println("testStart");
            parcel.surroundingParcels(reader);
            parcel = landParcelOptimizer.BoundingBoxOptimization(parcel, 5, 0.25, 0.9, 30, 5);
            SceneRenderer.render(parcel.getFootprintGeometries());
            reader.getBuildingFootprints(currentDir.toAbsolutePath() + "/input/buildingFootprints.json");
            placer.setRoadCentre(parcel);
            placer.surroundingFootprints(parcel);
            placer.placeBuildings(parcel, reader);

            for (Footprint footprint : parcel.footprints) {
                if (footprint.id == 157 || footprint.id == 165) {
                    placer.createDriveway(footprint);
                }

                if (footprint.building != null) {
                    SceneRenderer.render(footprint.building.polygon);
                }

            }
            System.out.println("testStart");
            sceneRenderer.start(stage);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

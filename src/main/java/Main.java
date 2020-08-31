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
        for (LandParcel parcels: LandParcels) {
            parcels.surroundingParcels(reader);
            parcels = landParcelOptimizer.BoundingBoxOptimization(parcels, 5, 0.25, 0.9,30, 6);
            SceneRenderer.render(parcels.getFootprintGeometries());
            placer.setRoadCentre(parcels);
            placer.placeBuildings(parcels);
            for (Footprint footprint: parcels.footprints) {
                if(footprint.building != null){
                    SceneRenderer.render(footprint.building.polygon);
                }
                
            }
            for(int i = 0; i < parcels.subroads.size(); i++){
                SceneRenderer.renderLine(new Coordinate[]{ parcels.subroads.get(i).coordinateA,parcels.subroads.get(i).coordinateB});
            }
        }

        System.out.println("test");
        sceneRenderer.start(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

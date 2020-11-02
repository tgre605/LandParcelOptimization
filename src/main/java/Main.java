import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.io.FileReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

class DragContext {

    double mouseAnchorX;
    double mouseAnchorY;

    double translateAnchorX;
    double translateAnchorY;

}


public class Main extends Application {
    static  String inputFile = "";
    @Override
    public void start(Stage stage) throws Exception {
        JsonReader reader = new JsonReader(FileReading.getRoadNetwork(inputFile));
        ArrayList<LandParcel> LandParcels = reader.getParcels();
        BoundingBoxOptimizer boundingBoxOptimizer = new BoundingBoxOptimizer();
        BuildingPlacer placer = new BuildingPlacer();
        SceneRenderer sceneRenderer = new SceneRenderer();
        int i = 0;
        long startTime = System.currentTimeMillis();
        System.out.println("Number of parcels in file: " + LandParcels.size());
        int parcelCount = 20;

        for (LandParcel parcel : LandParcels) {
            if (i != 110 && i != 247 && i != 287 && i != 299 && i != 427 && i != 439 && i != 954 && i != 1010 && i != 1169) {
                parcel.surroundingParcels(reader);
                switch (parcel.landType) {
                    case residential:
                        boundingBoxOptimizer.BoundingBoxOptimization(parcel, 5, 5, 1.5);
                        break;
                    case commercial:
                        boundingBoxOptimizer.BoundingBoxOptimization(parcel, 4, 5, 1.5);
                        break;
                    case industry:
                        boundingBoxOptimizer.BoundingBoxOptimization(parcel, 7, 5, 1.5);
                        break;
                }

            } else {
                //SceneRenderer.render(parcel.polygon, Color.WHITESMOKE);
            }

            //landParcelOptimizer.BoundingBoxOptimization(parcel, 5, 5, 0.5, 5, 5);

            i++;
            if(i == parcelCount){
                break;
            }
            System.out.println("Parcels computed: " + i + " in " + (System.currentTimeMillis() - startTime) / 1000 + "s");
        }
        i = 0;
        startTime = System.currentTimeMillis();
        for (LandParcel parcel : LandParcels) {
            if (i != 5 && i != 370 && i != 790 && i != 526) {
                reader.getBuildingFootprints(FileReading.getBuildingFile());
                placer.setRoadCentre(parcel);
                placer.surroundingFootprints(parcel);
                placer.placeBuildings(parcel, reader);

                switch (parcel.landType) {
                    case industry:
                        SceneRenderer.render(parcel.getFootprintGeometries(), SceneRenderer.ColorSpectrum.Yellow);
                        break;
                    case commercial:
                        SceneRenderer.render(parcel.getFootprintGeometries(), SceneRenderer.ColorSpectrum.Blue);
                        break;
                    case residential:
                        SceneRenderer.render(parcel.getFootprintGeometries(), SceneRenderer.ColorSpectrum.Green);
                        break;
                    case undefined:
                        SceneRenderer.render(parcel.getFootprintGeometries(), SceneRenderer.ColorSpectrum.Red);
                        break;
                }

                for (Footprint footprint : parcel.footprints) {
                    //if (footprint.id == 157 || footprint.id == 165) {
                    //    placer.createDriveway(footprint);
                    //}

                    if (footprint.building != null) {
                        SceneRenderer.render(footprint.building.polygon, Color.GREY);
                    }

                }
            }
            i++;

            if (i == parcelCount) {
                break;
            }
            System.out.println("Buildings placed: " + i + " in " + (System.currentTimeMillis() - startTime) / 1000 + "s");

        }
        //reader.getBuildingFootprints(currentDir.toAbsolutePath() + "/input/buildingFootprints.json");
        //placer.DrawBuildings(reader);
        System.out.println("Finished Computing");
        sceneRenderer.start(stage);

    }

    public static void main(String[] args) {
        try {
            inputFile = args[0];
        } catch (ArrayIndexOutOfBoundsException e){
            inputFile = "simpleroadnetwork.json";
        }
        launch(args);
    }
}

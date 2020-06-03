import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        ArrayList<landParcel> landParcels = new ArrayList<>();

        landParcel parcel = new landParcel();

        parcel.polygon.add(new Vector2D(200,200));
        parcel.polygon.add(new Vector2D(300,200));
        parcel.polygon.add(new Vector2D(200,300));

        landParcels.add(parcel);

        landParcel parcelA = new landParcel();

        parcelA.polygon.add(new Vector2D(400,200));
        parcelA.polygon.add(new Vector2D(500,200));
        parcelA.polygon.add(new Vector2D(600,300));
        parcelA.polygon.add(new Vector2D(500,400));

        landParcels.add(parcelA);

        //Creating a Group object
        Group root = new Group();

        for(int i= 0; i < landParcels.size(); i++){
            //Get polygon land parcel polygon
            Polygon polygon = new landParcelViewer().GetLandParcelPolygon(landParcels.get(i));

            root.getChildren().add(polygon);
        }

        //Creating a scene object
        Scene scene = new Scene(root, 600, 300);

        //Setting title to the Stage
        stage.setTitle("Drawing a Polygon");

        //Adding scene to the stage
        stage.setScene(scene);

        //Displaying the contents of the stage
        stage.show();
    }

    public static void main(String[] args) {
        //JsonReader reader = new JsonReader("/LandParcelOptimization/input/simpleroadnetwork.json");
        launch(args);
    }
}

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
        JsonReader reader = new JsonReader("/LandParcelOptimization/input/roadnetwork.json");


        landParcels = reader.getParcels();

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

        launch(args);
    }
}

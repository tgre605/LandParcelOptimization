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
        LandParcel parcel = new LandParcel();

        parcel.polygon.add(new Vector2D(200,200));
        parcel.polygon.add(new Vector2D(300,200));
        parcel.polygon.add(new Vector2D(200,300));


        //Get polygon land parcel polygon
        Polygon polygon = new LandParcelViewer().GetLandParcelPolygon(parcel);

        //Creating a Group object
        Group root = new Group(polygon);

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
        //JsonReader reader = new JsonReader("/LandParcelOptimization/input/roadnetwork.json");
        launch(args);
    }
}

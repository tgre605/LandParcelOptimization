import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        ArrayList<landParcel> landParcels = new ArrayList<>();

        Path currentDir = Paths.get(".");
        JsonReader reader = new JsonReader(currentDir.toAbsolutePath() + "/input/roadnetwork.json");


        landParcels = reader.getParcels();

        //Creating a Group object
        Pane root = new Pane();

        ArrayList<Color> colorList = new ArrayList<Color>();
        colorList.add(Color.BLUE);
        colorList.add(Color.CORNFLOWERBLUE);
        colorList.add(Color.DODGERBLUE);
        colorList.add(Color.DARKBLUE);
        colorList.add(Color.DEEPSKYBLUE);

        for(int i= 0; i < landParcels.size(); i++){
            //Get polygon land parcel polygon
            Polygon polygon = new landParcelViewer().GetLandParcelPolygon(landParcels.get(i));

            polygon.setFill(colorList.get((int)(Math.random() * 4)));
            polygon.setStroke(Color.BLACK);
            root.getChildren().add(polygon);
        }

        //Creating a scene object
        Scene scene = new Scene(root, 900, 900);

        scene.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                double scale = root.getScaleX();
                if (event.getDeltaY() > 0)
                    scale /= 1.2;
                else
                    scale *= 1.2;
                root.setScaleX(scale);
                root.setScaleY(scale);
            }
        });


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

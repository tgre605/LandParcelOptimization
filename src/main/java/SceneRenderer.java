import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import org.locationtech.jts.geom.Geometry;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SceneRenderer {
    private DragContext sceneDragContext = new DragContext();


    public Polygon ConvertPolygon(org.locationtech.jts.geom.Polygon polygon){
        List<Double> points = new ArrayList<Double>();
        for(int i = 0; i < polygon.getNumPoints();i++) {
            points.add(polygon.getCoordinates()[i].getX());
            points.add(polygon.getCoordinates()[i].getY());
        }
        Polygon output = new Polygon();
        output.getPoints().addAll(points);
        return output;
    }

    public void start(Stage stage, ArrayList<landParcel> landParcels, ArrayList<org.locationtech.jts.geom.Polygon> polygons) throws Exception {
        //Creating a Group object
        Pane root = new Pane();

        FileInputStream input = new FileInputStream(Paths.get(".").toAbsolutePath() + "/input/images/water_map.png");
        Image image = new Image(input);
        root.getChildren().add(new ImageView(image));

        ArrayList<Color> colorList = new ArrayList<Color>();
        colorList.add(Color.BLUE);
        colorList.add(Color.CORNFLOWERBLUE);
        colorList.add(Color.DODGERBLUE);
        colorList.add(Color.DARKBLUE);
        colorList.add(Color.DEEPSKYBLUE);
        for(int i= 0; i < polygons.size(); i++){
            root.getChildren().add(ConvertPolygon(polygons.get(i)));
        }

        for(int i= 0; i < landParcels.size(); i++){
            //Get polygon land parcel polygon
            Polygon polygon = ConvertPolygon(landParcels.get(i).polygon);

            polygon.setFill(colorList.get((int)(Math.random() * 4)));
            root.getChildren().add(polygon);
        }

        //Creating a scene object
        Scene scene = new Scene(root, 900, 900);

        scene.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                double scale = root.getScaleX();
                if (event.getDeltaY() < 0) {
                    scale /= 1.2;
                }
                else {
                    scale *= 1.2;
                }
                root.setScaleX(scale);
                root.setScaleY(scale);

            }
        });

        scene.setOnMousePressed(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {

                // right mouse button => panning
                if( !event.isPrimaryButtonDown())
                    return;

                sceneDragContext.mouseAnchorX = event.getSceneX();
                sceneDragContext.mouseAnchorY = event.getSceneY();

                sceneDragContext.translateAnchorX = root.getTranslateX();
                sceneDragContext.translateAnchorY = root.getTranslateY();

            }

        });

        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                // right mouse button => panning
                if( !event.isPrimaryButtonDown())
                    return;

                root.setTranslateX(sceneDragContext.translateAnchorX + event.getSceneX() - sceneDragContext.mouseAnchorX);
                root.setTranslateY(sceneDragContext.translateAnchorY + event.getSceneY() - sceneDragContext.mouseAnchorY);

                event.consume();

            }
        });


        //Setting title to the Stage
        stage.setTitle("Drawing a Polygon");

        //Adding scene to the stage
        stage.setScene(scene);

        //Displaying the contents of the stage
        stage.show();
    }
}

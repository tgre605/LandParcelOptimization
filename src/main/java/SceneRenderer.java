import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.math.Vector2D;

import java.awt.geom.Rectangle2D;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SceneRenderer {
    private DragContext sceneDragContext = new DragContext();
    private Vector2D mousePosition = new Vector2D();


    public Polygon ConvertPolygon(Geometry geometry){
        List<Double> points = new ArrayList<Double>();
        for(int i = 0; i < geometry.getNumPoints();i++) {
            points.add(geometry.getCoordinates()[i].getX());
            points.add(geometry.getCoordinates()[i].getY());
        }
        Polygon output = new Polygon();
        output.getPoints().addAll(points);
        return output;
    }


    public void start(Stage stage, ArrayList<landParcel> landParcels, ArrayList<Geometry> geometries, Coordinate[] coordinates ) throws Exception {
        //Creating a Group object
        Pane root = new Pane();

        FileInputStream input = new FileInputStream("C:/LandParcelOptimization/input/water_map.png");
        Image image = new Image(input);
        root.getChildren().add(new ImageView(image));

        ArrayList<Color> colorList = new ArrayList<Color>();
        colorList.add(Color.BLUE);
        colorList.add(Color.CORNFLOWERBLUE);
        colorList.add(Color.DODGERBLUE);
        colorList.add(Color.DARKBLUE);
        colorList.add(Color.DEEPSKYBLUE);
        for(int i= 0; i < geometries.size(); i++){
            root.getChildren().add(ConvertPolygon(geometries.get(i)));
        }

        for(int i= 0; i < landParcels.size(); i++){
            //Get polygon land parcel polygon
            Polygon polygon = ConvertPolygon(landParcels.get(i).polygon);

            polygon.setFill(colorList.get((int)(Math.random() * 4)));
            root.getChildren().add(polygon);
        }

        for(int i= 0; i < coordinates.length; i++){
            Circle circle = new Circle();
            circle.setCenterX(coordinates[i].x);
            circle.setCenterY(coordinates[i].y);
            circle.setRadius(0.5);
            circle.setStroke(Color.RED);
            root.getChildren().add(circle);
        }

        //Creating a scene object
        Scene scene = new Scene(root, 900, 900);

        scene.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {

                double scale = root.getScaleX();
                double centerX = (root.getTranslateX() + root.getWidth())/2;
                double centerY = (root.getTranslateY() + root.getHeight())/2;
                if (event.getDeltaY() < 0) {
                    scale /= 1.2;

                    root.setTranslateX(root.getTranslateX() + (centerX - mousePosition.getX()) * -0.4);
                    root.setTranslateY(root.getTranslateY() + (centerY - mousePosition.getY()) * -0.4);
                }
                else {

                    root.setTranslateX(root.getTranslateX() + (centerX - mousePosition.getX()) * 0.4);
                    root.setTranslateY(root.getTranslateY() + (centerY - mousePosition.getY()) * 0.4);
                    scale *= 1.2;
                }
                root.setScaleX(scale);
                root.setScaleY(scale);
            }
        });

        scene.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                mousePosition = new Vector2D(event.getX(), event.getY());
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

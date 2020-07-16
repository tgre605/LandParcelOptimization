import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.math.Vector2D;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SceneRenderer {
    private static DragContext sceneDragContext = new DragContext();
    private static Vector2D mousePosition = new Vector2D();
    private static ArrayList<landParcel> landParcels = new ArrayList<>();
    private static ArrayList<landParcel> debugLandParcels = new ArrayList<>();
    private static ArrayList<Geometry> geometries = new ArrayList<>();
    private static ArrayList<Coordinate> coordinates = new ArrayList<>();

    private  static Text text = new Text();


    public static Polygon ConvertPolygon(Geometry geometry){
        List<Double> points = new ArrayList<Double>();
        for(int i = 0; i < geometry.getNumPoints();i++) {
            points.add(geometry.getCoordinates()[i].getX());
            points.add(geometry.getCoordinates()[i].getY());
        }
        Polygon output = new Polygon();
        output.getPoints().addAll(points);
        return output;
    }

    public static void render(Geometry geometry){
        render(new Geometry[] {geometry});
    }
    public static void render(Geometry[] geometries){
        SceneRenderer.geometries.addAll(Arrays.asList(geometries));
    }
    public static void render(landParcel landParcel){
        render(new landParcel[] {landParcel});
    }
    public static void render(landParcel[] landParcels){
        SceneRenderer.landParcels.addAll(Arrays.asList(landParcels));
    }
    public static void render(Coordinate coordinate){
        render(new Coordinate[] {coordinate});
    }
    public static void render(Coordinate[] coordinates){
        SceneRenderer.coordinates.addAll(Arrays.asList(coordinates));
    }

    public static void debugRender(landParcel landParcel){
        debugRender(new landParcel[] {landParcel});
    }
    public static void debugRender(landParcel[] landParcels){
        SceneRenderer.debugLandParcels.addAll(Arrays.asList(landParcels));
    }


    static EventHandler<MouseEvent> mouseOver = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Object obj = event.getSource();

            if ( obj instanceof Polygon )
            {
                try {
                    Geometry geometry = (Geometry) ((Polygon) obj).getUserData();

                    String textString = "Geometry: \n";
                    textString    += "center: " + geometry.getCentroid().toString() + "\n";
                    textString += "Id: " + geometry.getUserData();
                    text.setText(textString);
                } catch (ClassCastException e){
                    try {
                        Coordinate geometry = (Coordinate) ((Polygon) obj).getUserData();

                        String textString  = "Coordinate: \n";
                        textString += "center: " + geometry.toString() + "\n";
                        text.setText( textString);
                    } catch (ClassCastException ce){
                        landParcel geometry = (landParcel) ((Polygon) obj).getUserData();

                        String textString  = "Land Parcel: \n";
                        textString += "center: " + geometry.polygon.getCentroid().toString() + "\n";
                        textString += "Id: " + geometry.id;
                        text.setText( textString);
                    }
                }
            }
        }
    };


    public static void start(Stage stage) throws Exception {
        Group group = new Group();

        //Creating a Group object
        Pane root = new Pane();
        Path currentDir = Paths.get(".");
        FileInputStream input = new FileInputStream(currentDir.toAbsolutePath() + "/input/water_map.png");

        Image image = new Image(input);
        root.getChildren().add(new ImageView(image));


        for(int i= 0; i < landParcels.size(); i++){
            Random r = new Random();
            //Get polygon land parcel polygon
            Polygon polygon = ConvertPolygon(landParcels.get(i).polygon);
            Color color = Color.color(0, 0 , clamp(r.nextFloat() + 0.25, 0, 1));
            polygon.setFill(color);
            polygon.setOnMouseEntered(mouseOver);
            polygon.setUserData(landParcels.get(i));
            root.getChildren().add(polygon);
        }

        for(int i= 0; i < geometries.size(); i++){
            Polygon polygon = ConvertPolygon(geometries.get(i));
            polygon.setFill(Color.LIGHTGREEN.interpolate(Color.DARKGREEN, (double) i/geometries.size()));
            polygon.setStroke(Color.GRAY);
            polygon.setStrokeWidth(0.25f);
            polygon.setOnMouseEntered(mouseOver);
            polygon.setUserData(geometries.get(i));
            root.getChildren().add(polygon);
        }

        for(int i= 0; i < coordinates.size(); i++){
            Circle circle = new Circle();
            circle.setCenterX(coordinates.get(i).x);
            circle.setCenterY(coordinates.get(i).y);
            circle.setRadius(0.5);
            circle.setOnMouseEntered(mouseOver);
            circle.setUserData(coordinates.get(i));
            circle.setFill(Color.RED.interpolate(Color.DARKRED, (double) i/coordinates.size()));
            root.getChildren().add(circle);
        }

        text.setText("");
        text.setX(20);
        text.setY(40);
        text.setFont(Font.font ("Arial", 25));

        group.getChildren().add(root);
        group.getChildren().add(text);

        //Creating a scene object
        Scene scene = new Scene(group, 900, 900);

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

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}

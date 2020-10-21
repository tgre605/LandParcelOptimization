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
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;


import java.awt.geom.AffineTransform;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;


public class SceneRenderer {
    private static double scale = 10.76422503423912;

    private static DragContext sceneDragContext = new DragContext();
    private static Vector2D mousePosition = new Vector2D();

    private static Hashtable<Geometry, Color> geometryColor = new Hashtable<>();
    private static Hashtable<Geometry, Color> coordinateColor = new Hashtable<>();
    private static Hashtable<Geometry, Color> lineColor = new Hashtable<>();

    private static Hashtable<Geometry, ColorSpectrum> geometryColorSpectrum = new Hashtable<>();

    private static ArrayList<LandParcel> LandParcels = new ArrayList<>();
    private static ArrayList<Coordinate> coordinates = new ArrayList<>();
    private static ArrayList<Geometry> outlineGeometries = new ArrayList<>();
    private static ArrayList<Coordinate> lines = new ArrayList<>();

    private static Text text = new Text();

    public enum ColorSpectrum {Green, Blue, Red, Yellow}

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

    public static void render(Geometry[] geometry, Color color){
        for(int i=0; i < geometry.length; i++)
            geometryColor.put(geometry[i], color);
    }

    public static void render(Geometry geometry, Color color){
        geometryColor.put(geometry, color);
    }

    public static void render(Geometry geometry, ColorSpectrum color){
        geometryColorSpectrum.put(geometry, color);
    }

    public static void render(Geometry[] geometry, ColorSpectrum color){
        for(int i=0; i < geometry.length; i++)
            geometryColorSpectrum.put(geometry[i], color);
    }

    public static void renderOutline(Geometry[] geometries){
        SceneRenderer.outlineGeometries.addAll(Arrays.asList(geometries));
    }

    public static void render(LandParcel landParcel){
        render(new LandParcel[] {landParcel});
    }
    public static void render(LandParcel[] LandParcels){
        SceneRenderer.LandParcels.addAll(Arrays.asList(LandParcels));
    }
    public static void render(Coordinate coordinate){
        render(new Coordinate[] {coordinate});
    }
    public static void render(Coordinate[] coordinates){
        SceneRenderer.coordinates.addAll(Arrays.asList(coordinates));
    }

    public static void renderLine(Coordinate[] coordinates){
        SceneRenderer.lines.addAll(Arrays.asList(coordinates));
    }

    static Coordinate Scale(Point input){
        Coordinate coordinate =  new Coordinate();
        coordinate.x = input.getX() * scale;
        coordinate.y = input.getY() * scale;
        return coordinate;
    }


    static EventHandler<MouseEvent> mouseOver = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Object obj = event.getSource();

            if ( obj instanceof Polygon )
            {
                try {
                    Coordinate geometry = (Coordinate) ((Polygon) obj).getUserData();

                    String textString  = "Coordinate: \n";
                    textString += "center: " + geometry.toString() + "\n";
                    text.setText( textString);

                } catch (ClassCastException e){
                    try {
                        Geometry geometry = (Geometry) ((Polygon) obj).getUserData();

                        String textString = "Geometry: \n";
                        textString    += "center: " + Scale(geometry.getCentroid()).toString() + "\n";
                        textString += "Id: " + geometry.getUserData()+ "\n";
                        textString += "Area:" + geometry.getArea() * SceneRenderer.scale + "\n";
                        textString += "Is Triangle: " + LandParcelOptimizer.isTriangle(geometry, 0.25);
                        text.setText(textString);
                    } catch (ClassCastException ce){
                        LandParcel geometry = (LandParcel) ((Polygon) obj).getUserData();

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
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(image.getWidth());
        imageView.setFitHeight(image.getHeight());

        root.getChildren().add(imageView);


        for(int i = 0; i < LandParcels.size(); i++){
            Random r = new Random();
            //Get polygon land parcel polygon
            Polygon polygon = ConvertPolygon(LandParcels.get(i).polygon);
            Color color = Color.color(0, 0 , clamp(r.nextFloat() + 0.25, 0, 1));
            polygon.setFill(color);
            polygon.setOnMouseEntered(mouseOver);
            polygon.setUserData(LandParcels.get(i));
            root.getChildren().add(polygon);
        }

        for (Geometry geometry: geometryColor.keySet()) {
            Polygon polygon = ConvertPolygon(geometry);
            polygon.setFill(geometryColor.get(geometry));
            polygon.setStroke(Color.GRAY);
            polygon.setStrokeWidth(0.01f);
            polygon.setOnMouseEntered(mouseOver);
            polygon.setUserData(geometry);
            root.getChildren().add(polygon);
        }

        int j = 0;
        for (Geometry geometry: geometryColorSpectrum.keySet()) {
            Polygon polygon = ConvertPolygon(geometry);
            switch (geometryColorSpectrum.get(geometry)){
                case Red:
                    polygon.setFill(Color.RED.interpolate(Color.DARKRED, (double) j/geometryColorSpectrum.size()));
                    break;
                case Blue:
                    polygon.setFill(Color.DEEPSKYBLUE.interpolate(Color.DODGERBLUE, (double) j/geometryColorSpectrum.size()));
                    break;
                case Green:
                    polygon.setFill(Color.LIGHTGREEN.interpolate(Color.DARKGREEN, (double) j/geometryColorSpectrum.size()));
                    break;
                case Yellow:
                    polygon.setFill(Color.LIGHTYELLOW.interpolate(Color.YELLOW, (double) j/geometryColorSpectrum.size()));
                    break;
            }

            polygon.setStroke(Color.GRAY);
            polygon.setStrokeWidth(0.01f);
            polygon.setOnMouseEntered(mouseOver);
            polygon.setUserData(geometry);
            root.getChildren().add(polygon);
            j++;
        }

        for(int i= 0; i < outlineGeometries.size(); i++){
            Polygon polygon = ConvertPolygon(outlineGeometries.get(i));
            polygon.setFill(Color.TRANSPARENT);
            polygon.setStroke(Color.GRAY);
            polygon.setStrokeWidth(0.01f);
            polygon.setOnMouseEntered(mouseOver);
            polygon.setUserData(outlineGeometries.get(i));
            root.getChildren().add(polygon);
        }

        for(int i= 0; i < coordinates.size(); i++){
            Circle circle = new Circle();
            circle.setCenterX(coordinates.get(i).x);
            circle.setCenterY(coordinates.get(i).y);
            circle.setRadius(0.25);
            circle.setOnMouseEntered(mouseOver);
            circle.setUserData(coordinates.get(i));
            circle.setFill(Color.RED.interpolate(Color.DARKRED, (double) i/coordinates.size()));
            root.getChildren().add(circle);
        }

        for(int i= 0; i < lines.size()-1; i+=2){
            Line line = new Line(lines.get(i).x, lines.get(i).y, lines.get(i+1).x, lines.get(i+1).y);
            line.setStroke(Color.YELLOW);
            line.setStrokeWidth(0.075f);
            root.getChildren().add(line);
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

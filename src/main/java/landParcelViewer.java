import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;

public class landParcelViewer {

    public Polygon GetLandParcelPolygon(landParcel landParcel){
        Polygon polygon = new Polygon();
        List<Double> points = new ArrayList<Double>();
        for(int i = 0; i < landParcel.polygon.size();i++) {
            points.add(landParcel.polygon.get(i).getX());
            points.add(landParcel.polygon.get(i).getY());
        }
        polygon.getPoints().addAll(points);
        return polygon;
    }
}

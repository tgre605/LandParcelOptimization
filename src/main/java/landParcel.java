import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

public class landParcel {
    public Polygon polygon = new Polygon(null, null, new GeometryFactory());
    public ArrayList<building> buildingFootprints = new ArrayList<>();
    private ArrayList<Coordinate> vertices = new ArrayList<>();

   public landParcel(ArrayList<Coordinate> vertices) {
        polygon = new GeometryFactory().createPolygon(vertices.toArray(new Coordinate[0]));
        this.vertices = vertices;
    }

    public landParcel(Geometry polygon){
       this.polygon = new GeometryFactory().createPolygon(polygon.getCoordinates());
    }

    public Coordinate[] getPoints(){
        Coordinate[] points = polygon.getCoordinates();
        for(int i = 0; i < points.length; i++){
            System.out.println(points[i]);
        }
        return points;
    }
}

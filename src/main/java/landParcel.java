import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

public class landParcel {
    public Polygon polygon = new Polygon(null, null, new GeometryFactory());
    public ArrayList<building> buildingFootprints = new ArrayList<>();

   public landParcel(ArrayList<Coordinate> vertices) {
        polygon = new GeometryFactory().createPolygon(vertices.toArray(new Coordinate[0]));
    }

    public landParcel(){

    }
}

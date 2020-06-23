import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;

public class building {
    public Polygon polygon = new Polygon(null, null, new GeometryFactory());

    public building(ArrayList<Coordinate> vertices) {
        polygon = new GeometryFactory().createPolygon(vertices.toArray(new Coordinate[0]));
    }

    public building(){

    }
}

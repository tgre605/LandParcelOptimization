import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

public class LandParcelOptimizer {
    public Polygon BoundingBoxOptimization(landParcel inputParcel){
        MinimumDiameter minimumDiameter = new MinimumDiameter(inputParcel.polygon);
        Geometry boundingBox = minimumDiameter.getMinimumRectangle();
        return new GeometryFactory().createPolygon(boundingBox.getCoordinates());
    }
}

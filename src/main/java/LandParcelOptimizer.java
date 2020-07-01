import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LandParcelOptimizer {
    public Geometry BoundingBoxOptimization(landParcel inputParcel){
        MinimumDiameter minimumDiameter = new MinimumDiameter(inputParcel.polygon);
        Geometry boundingBox = minimumDiameter.getMinimumRectangle();
        Coordinate[] coordinates = boundingBox.getCoordinates();
        Coordinate[] line = new Coordinate[]{coordinates[0], coordinates[3]};
        Geometry lineGeometry = new GeometryFactory().createPolygon(line);
        Polygon landParcelPoly = new GeometryFactory().createPolygon(boundingBox.getCoordinates());
        Geometry splitPoly = splitPolygon(landParcelPoly, lineGeometry);
        return splitPoly;
    }

    // CODE FROM https://gis.stackexchange.com/questions/189976/jts-split-arbitrary-polygon-by-a-line
    public static Geometry polygonize(Geometry geometry) {
        List lines = LineStringExtracter.getLines(geometry);
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(lines);
        Collection polys = polygonizer.getPolygons();
        Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
        return geometry.getFactory().createGeometryCollection(polyArray);
    }

    public static Geometry splitPolygon(Geometry poly, Geometry line) {
        Geometry nodedLinework = poly.getBoundary().union(line);
        Geometry polys = polygonize(nodedLinework);

        // Only keep polygons which are inside the input
        List output = new ArrayList();
        for (int i = 0; i < polys.getNumGeometries(); i++) {
            Polygon candpoly = (Polygon) polys.getGeometryN(i);
            if (poly.contains(candpoly.getInteriorPoint())) {
                output.add(candpoly);
            }
        }
        return poly.getFactory().createGeometryCollection(GeometryFactory.toGeometryArray(output));
    }
}

import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static java.lang.Math.*;

public class LandParcelOptimizer {
    public Geometry BoundingBoxOptimization(landParcel inputParcel){
        MinimumDiameter minimumDiameter = new MinimumDiameter(inputParcel.polygon);
        Geometry boundingBox = minimumDiameter.getMinimumRectangle();
        Coordinate[] coordinates = boundingBox.getCoordinates();
        Coordinate[] line = new Coordinate[]{coordinates[0], coordinates[3]};
        //Geometry lineGeometry = new GeometryFactory().createPolygon(line);
        //Polygon landParcelPoly = new GeometryFactory().createPolygon(boundingBox.getCoordinates());
        //Geometry splitPoly = splitPolygon(landParcelPoly, lineGeometry);
        halfRectangle(boundingBox, coordinates);
        return boundingBox;
    }

    // CODE FROM https://gis.stackexchange.com/questions/189976/jts-split-arbitrary-polygon-by-a-line
/*    public static Geometry polygonize(Geometry geometry) {
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
    }*/

    public Geometry halfRectangle(Geometry boundingBox, Coordinate[] coordinates){
        for(int i = 0; i < coordinates.length; i++){
            System.out.println(coordinates[i]);
        }
        double coord1x = coordinates[1].getX();
        double coord1y = coordinates[1].getY();
        double coord2x = coordinates[2].getX();
        double coord2y = coordinates[2].getY();
        double coord3x = coordinates[3].getX();
        double coord3y = coordinates[3].getY();
        double coord4x = coordinates[4].getX();
        double coord4y = coordinates[4].getY();

        double x12 = abs(coord1x-coord2x);
        double y12 = abs(coord1y-coord2y);
        double dist12 = sqrt(pow(x12,2)+pow(y12,2));

        double x13 = abs(coord1x-coord3x);
        double y13 = abs(coord1y-coord3y);
        double dist13 = sqrt(pow(x13,2)+pow(y13,2));

        if (dist12 > dist13){
            double mid1x = (coord1x + coord2x)/2;
            double mid1y = (coord1y + coord2y)/2;
            double mid2x = (coord3x + coord4x)/2;
            double mid2y = (coord3y + coord4y)/2;
            Coordinate midpoint1 = new Coordinate(mid1x, mid1y);
            Coordinate midpoint2 = new Coordinate(mid2x, mid2y);
        } else {
            double mid1x = (coord1x + coord3x)/2;
            double mid1y = (coord1y + coord3y)/2;
            double mid2x = (coord2x + coord4x)/2;
            double mid2y = (coord2y + coord4y)/2;
            Coordinate midpoint1 = new Coordinate(mid1x, mid1y);
            Coordinate midpoint2 = new Coordinate(mid2x, mid2y);
        }
        //Want to create two rectangles that make up the original bounding box which can then be used to split a polygon

    return boundingBox;
    }

}

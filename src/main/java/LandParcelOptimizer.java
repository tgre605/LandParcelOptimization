import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.math.Vector2D;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static java.lang.Math.*;

public class LandParcelOptimizer {
    public Geometry[] BoundingBoxOptimization(landParcel inputParcel, double minArea){
        ArrayList<Geometry> largeFootprints = new ArrayList<>();
        ArrayList<Geometry> smallFootprints = new ArrayList<>();
        largeFootprints.add(inputParcel.polygon);

        while (largeFootprints.size() != 0){
            MinimumDiameter minimumDiameter = new MinimumDiameter(largeFootprints.get(0));

            Geometry boundingBox = minimumDiameter.getMinimumRectangle();
            Geometry[] boundingBoxes = halfRectangle(boundingBox, false);
            Geometry footprintA = splitPolygon(boundingBoxes[0], largeFootprints.get(0));
            Geometry footprintB = splitPolygon(boundingBoxes[1], largeFootprints.get(0));

            if(!hasRoadAccess(inputParcel.polygon, footprintA) || !hasRoadAccess(inputParcel.polygon, footprintB)){
                boundingBoxes = halfRectangle(boundingBox, true);
                footprintA = splitPolygon(boundingBoxes[0], largeFootprints.get(0));
                footprintB = splitPolygon(boundingBoxes[1], largeFootprints.get(0));
            }

            if(footprintA.getArea() < minArea)
                smallFootprints.add(footprintA);
            else
                largeFootprints.add(footprintA);
            if(footprintB.getArea() < minArea)
                smallFootprints.add(footprintB);
            else
                largeFootprints.add(footprintB);

            largeFootprints.remove(0);
        }

        return smallFootprints.toArray(new Geometry[0]);
    }

    boolean hasRoadAccess(Geometry landParcelPolygon, Geometry footprint){
        for(int i= 0; i < footprint.getCoordinates().length-1; i++){
            for (int j =0; j < landParcelPolygon.getCoordinates().length-1; j++){
                if(edgeOnLine(landParcelPolygon.getCoordinates()[j], landParcelPolygon.getCoordinates()[j+1],
                        footprint.getCoordinates()[i], footprint.getCoordinates()[i+1])){
                    return true;
                }
            }
        }
        return false;
    }

    // LineA -- A -- B -- LineB
    boolean edgeOnLine(Coordinate LineA, Coordinate LineB, Coordinate A, Coordinate B){
        double lineDistance = Vector2D.create(LineA, LineB).distance(Vector2D.create(0,0));
        double LineAA = Vector2D.create(LineA, A).distance(Vector2D.create(0,0));
        double AB = Vector2D.create(A, B).distance(Vector2D.create(0,0));
        double BLineB = Vector2D.create(B, LineB).distance(Vector2D.create(0,0));

        return LineAA + AB + BLineB == lineDistance;
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

    public Geometry[] halfRectangle(Geometry boundingBox, boolean invertResult){
        Coordinate[] coordinates = boundingBox.getCoordinates();

        double dist13 = coordinates[0].distance(coordinates[3]);
        double dist12 = coordinates[0].distance(coordinates[1]);

        Geometry rectangleA, rectangleB;

    if (dist12 < dist13 && !invertResult){
            double mid1x = (coordinates[0].x + coordinates[3].x)/2;
            double mid1y = (coordinates[0].y + coordinates[3].y)/2;
            double mid2x = (coordinates[1].x + coordinates[2].x)/2;
            double mid2y = (coordinates[1].y + coordinates[2].y)/2;
            Coordinate midpoint1 = new Coordinate(mid1x, mid1y);
            Coordinate midpoint2 = new Coordinate(mid2x, mid2y);
            rectangleA = new GeometryFactory().createPolygon(new Coordinate[]{coordinates[0], midpoint1, midpoint2, coordinates[1], coordinates[0]});
            rectangleB =new GeometryFactory().createPolygon(new Coordinate[]{coordinates[2], midpoint2,  midpoint1, coordinates[3], coordinates[2]});

        } else{
            double mid1x = (coordinates[0].x + coordinates[1].x)/2;
            double mid1y = (coordinates[0].y + coordinates[1].y)/2;
            double mid2x = (coordinates[2].x + coordinates[3].x)/2;
            double mid2y = (coordinates[2].y + coordinates[3].y)/2;
            Coordinate midpoint1 = new Coordinate(mid1x, mid1y);
            Coordinate midpoint2 = new Coordinate(mid2x, mid2y);
            rectangleA = new GeometryFactory().createPolygon(new Coordinate[]{coordinates[0], midpoint1, midpoint2, coordinates[2], coordinates[0]});
            rectangleB = new GeometryFactory().createPolygon(new Coordinate[]{coordinates[1], midpoint1, midpoint2, coordinates[3], coordinates[1]});
        }


        return new Geometry[]{rectangleA, rectangleB};
    }

    public Geometry splitPolygon(Geometry boundingBox, Geometry footprint){
        return boundingBox.intersection(footprint);
    }

}

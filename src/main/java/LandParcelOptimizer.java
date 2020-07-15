import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.Random;

public class LandParcelOptimizer {
    public Geometry[] BoundingBoxOptimization(landParcel inputParcel, double minArea, double minStreetWidth, double streetAccessLevel){
        ArrayList<Geometry> largeFootprints = new ArrayList<>();
        ArrayList<Geometry> smallFootprints = new ArrayList<>();
        largeFootprints.add(inputParcel.polygon);

        while (largeFootprints.size() != 0){
            MinimumDiameter minimumDiameter = new MinimumDiameter(largeFootprints.get(0));

            Geometry boundingBox = minimumDiameter.getMinimumRectangle();
            Geometry[] boundingBoxes = halfRectangle(boundingBox, false);
            Geometry footprintA = splitPolygon(boundingBoxes[0], largeFootprints.get(0));
            Geometry footprintB = splitPolygon(boundingBoxes[1], largeFootprints.get(0));

            System.out.println(footprintB.getCentroid().getCoordinate().distance(new Coordinate(383.37059381202937, 48.84644950389534)));
            if(footprintB.getCentroid().getCoordinate().distance(new Coordinate(383.37059381202937, 48.84644950389534)) == 0.0){
                if(hasRoadAccess(inputParcel.polygon, footprintB))
                    SceneRenderer.render(footprintB);
            }

            if(!hasRoadAccess(inputParcel.polygon, footprintA) || !hasRoadAccess(inputParcel.polygon, footprintB)){
                String concatDouble = String.valueOf((int)inputParcel.polygon.getCentroid().getX()).concat(String.valueOf((int)inputParcel.polygon.getCentroid().getY()));
                if(new Random(Long.parseLong(concatDouble)).nextDouble() < streetAccessLevel) {
                    boundingBoxes = halfRectangle(boundingBox, true);
                    footprintA = splitPolygon(boundingBoxes[0], largeFootprints.get(0));
                    footprintB = splitPolygon(boundingBoxes[1], largeFootprints.get(0));
                }
            }

            if(smallestEdge(footprintA) < minStreetWidth){
                smallFootprints.add(footprintA);
            } else
            if(footprintA.getArea() < minArea)
                smallFootprints.add(footprintA);
            else
                largeFootprints.add(footprintA);

            if(smallestEdge(footprintB) < minStreetWidth){
                smallFootprints.add(footprintB);
            }
            if(footprintB.getArea() < minArea)
                smallFootprints.add(footprintB);
            else
                largeFootprints.add(footprintB);

            largeFootprints.remove(0);
        }

        return smallFootprints.toArray(new Geometry[0]);
    }

    boolean hasRoadAccess(Geometry landParcelPolygon, Geometry footprint){
        for(int i= 0; i < landParcelPolygon.getCoordinates().length-1; i++){
            for(int j= 0; j < footprint.getCoordinates().length -1 ; j++){
                if(edgeOnLine(landParcelPolygon.getCoordinates()[i],  landParcelPolygon.getCoordinates()[i+1], footprint.getCoordinates()[j], footprint.getCoordinates()[j+1])){
                    return true;
                }
            }
        }
        return false;
    }

    // LineA -- A -- B -- LineB
    boolean edgeOnLine(Coordinate LineA, Coordinate LineB, Coordinate A, Coordinate B){
        double lineDistance = LineA.distance(LineB);
        double LineAA = LineA.distance(A);
        double AB = A.distance(B);
        double BLineB = B.distance(LineB);

        double total = lineDistance - (LineAA + AB + BLineB);

        return lineDistance - (LineAA + AB + BLineB) < 0.00001;
    }

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
            rectangleA = new GeometryFactory().createPolygon(new Coordinate[]{coordinates[0], midpoint1, midpoint2, coordinates[3], coordinates[0]});
            rectangleB = new GeometryFactory().createPolygon(new Coordinate[]{coordinates[1], midpoint1, midpoint2, coordinates[2], coordinates[1]});
        }

        return new Geometry[]{rectangleA, rectangleB};
    }

    public double smallestEdge(Geometry geometry){
        double smallestLength = 1000;
        for(int i= 0; i < geometry.getCoordinates().length-2; i++){
            double distance = geometry.getCoordinates()[i].distance(geometry.getCoordinates()[i+1]);
            if(distance < smallestLength && distance > 0.001){
                smallestLength = distance;
            }
        }
        return smallestLength;
    }

    public Geometry splitPolygon(Geometry boundingBox, Geometry footprint){
        return new GeometryFactory().createPolygon(CoordinateArrays.removeRepeatedPoints(boundingBox.intersection(footprint).getCoordinates()));
    }

}

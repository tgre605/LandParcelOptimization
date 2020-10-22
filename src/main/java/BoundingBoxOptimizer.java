import javafx.scene.paint.Color;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

import java.util.ArrayList;
import java.util.Random;

public class BoundingBoxOptimizer {
    public LandParcel  BoundingBoxOptimization(LandParcel inputParcel, double minArea, double minStreetWidth, double streetAccessLevel, double triangleMinArea, double roadLength){
        Mesh parcelMesh = new Mesh(inputParcel);
        //Create large and small footprint lists
        ArrayList<Mesh.Face> largeFootprints = new ArrayList<>();
        ArrayList<Mesh.Face> smallFootprints = new ArrayList<>();
        largeFootprints.add(parcelMesh.footprint);

        while (largeFootprints.size() != 0){
            // Get Bounding Box
            MinimumDiameter minimumDiameter = new MinimumDiameter(parcelMesh.faceToPolygon(largeFootprints.get(0)));
            Geometry boundingBox = minimumDiameter.getMinimumRectangle();

            Mesh.Face[] finalFootprints = null;
            boolean usingTriangleSplit = false;
            // Check if is a triangle if so chop off the end of it
            Polygon polygon = parcelMesh.faceToPolygon(largeFootprints.get(0));
            if(isTriangle(polygon, 0.5)){
                try {
                    Coordinate[] splitLine = getEdgeSplitForTriangles(boundingBox, false);
                    finalFootprints = parcelMesh.splitEdge(splitLine[0], splitLine[1], largeFootprints.get(0), roadLength);
                    usingTriangleSplit = true;
                } catch(Exception e){
                    break;
                }
            }

            Mesh.Face[] footprintsNorm = null, footprintsRot = null;
            if(!usingTriangleSplit) {
                // Split footprint normally
                try {
                    Coordinate[] splitLine = getEdgeSplit(boundingBox, false);
                    footprintsNorm = parcelMesh.splitEdge(splitLine[0], splitLine[1], largeFootprints.get(0), roadLength);
                } catch (Exception e) {
                    break;
                }
                // Rotated Split
                try {
                    Coordinate[] splitLine = getEdgeSplit(boundingBox, true);
                    footprintsRot = parcelMesh.splitEdge(splitLine[0], splitLine[1], largeFootprints.get(0), roadLength);
                } catch (Exception e) {
                    break;
                }

                // Check which split is best
                if(setHasTriangle(footprintsNorm) && !setHasTriangle(footprintsRot)){
                    finalFootprints = footprintsRot;
                } else {
                    finalFootprints = footprintsNorm;
                }
            }

            // Add new footprints to list of footprints to update
            if(parcelMesh.faceToPolygon(finalFootprints[0]).getArea() < minArea){
                smallFootprints.add(finalFootprints[0]);
            } else {
                largeFootprints.add(finalFootprints[0]);
            }

            if(parcelMesh.faceToPolygon(finalFootprints[1]).getArea() < minArea){
                smallFootprints.add(finalFootprints[1]);
            }else {
                largeFootprints.add(finalFootprints[1]);
            }

            largeFootprints.remove(0);
        }

        for (Mesh.Face face : smallFootprints) {
            Polygon polygon = parcelMesh.faceToPolygon(face);
            Footprint footprint = new Footprint(polygon);
            footprint = assignRoadSideEdges(Mesh.roads, footprint, face);
            for(Coordinate[] coords : footprint.getRoadsideEdges().keySet()){
                SceneRenderer.renderLine(new Coordinate[]{coords[0], coords[1]});
            }
            inputParcel.footprints.add(footprint);
        }

        return inputParcel;
    }

    Footprint assignRoadSideEdges(ArrayList<Road> roads, Footprint footprint, Mesh.Face face){
        ArrayList<Mesh.Vertex> vertices = face.vertices;
        for(int i= 0; i < vertices.size()-1; i++){
            if(Mesh.getEdge(vertices.get(i), vertices.get(i + 1), face.edges).roadsideEdge) {
                for (int j = 0; j < roads.size(); j++) {
                    if (edgeOnLine(roads.get(j).start, roads.get(j).end, vertices.get(i).position, vertices.get(i + 1).position)) {
                        footprint.roadsideIndex.put(i, roads.get(j));
                    }
                }
            }
        }
        return footprint;
    }

    // LineA -- A -- B -- LineB
    boolean edgeOnLine(Coordinate LineA, Coordinate LineB, Coordinate A, Coordinate B){
        double lineDistance = LineA.distance(LineB);
        double LineAA = LineA.distance(A);
        double AB = A.distance(B);
        double BLineB = B.distance(LineB);

        double total = lineDistance - (LineAA + AB + BLineB);

        double LineAB = LineA.distance(B);
        double ALineB = A.distance(LineB);

        double otherTotal = lineDistance - (LineAB + AB + ALineB);

        return (total < 0.00001 && total > -0.00001) || (otherTotal < 0.00001 && otherTotal > -0.00001);
    }

    private boolean setHasTriangle(Mesh.Face[] footprints){
        Polygon polygonA = Mesh.faceToPolygon(footprints[0]);
        Polygon polygonB = Mesh.faceToPolygon(footprints[1]);
        return isTriangle(polygonA, 0.5) ||  isTriangle(polygonB, 0.5);
    }


    public Coordinate[] getEdgeSplit(Geometry boundingBox, boolean invertResult){
        Coordinate[] coordinates = boundingBox.getCoordinates();

        double dist13 = coordinates[0].distance(coordinates[3]);
        double dist12 = coordinates[0].distance(coordinates[1]);

        if (dist12 < dist13 && !invertResult){
            double mid1x = (coordinates[0].x + coordinates[3].x)/2;
            double mid1y = (coordinates[0].y + coordinates[3].y)/2;
            double mid2x = (coordinates[1].x + coordinates[2].x)/2;
            double mid2y = (coordinates[1].y + coordinates[2].y)/2;
            Coordinate midpoint1 = new Coordinate(mid1x, mid1y);
            Coordinate midpoint2 = new Coordinate(mid2x, mid2y);
            return new Coordinate[]{midpoint1, midpoint2};
        } else{
            double mid1x = (coordinates[0].x + coordinates[1].x)/2;
            double mid1y = (coordinates[0].y + coordinates[1].y)/2;
            double mid2x = (coordinates[2].x + coordinates[3].x)/2;
            double mid2y = (coordinates[2].y + coordinates[3].y)/2;
            Coordinate midpoint1 = new Coordinate(mid1x, mid1y);
            Coordinate midpoint2 = new Coordinate(mid2x, mid2y);
            return new Coordinate[]{midpoint1, midpoint2};
        }
    }

    Coordinate[] getEdgeSplitForTriangles(Geometry boundingBox, boolean invertResult){
        double shiftAmount = 3;
        Coordinate[] coordinates = boundingBox.getCoordinates();

        double dist13 = coordinates[0].distance(coordinates[3]);
        double dist12 = coordinates[0].distance(coordinates[1]);

        if (dist12 < dist13 && !invertResult){
            double shiftX = (coordinates[0].x - coordinates[3].x)/shiftAmount;
            double shiftY = (coordinates[0].y - coordinates[3].y)/shiftAmount;
            double mid1x = (coordinates[0].x + coordinates[3].x)/2 - shiftX;
            double mid1y = (coordinates[0].y + coordinates[3].y)/2 - shiftY;
            double mid2x = (coordinates[1].x + coordinates[2].x)/2 - shiftX;
            double mid2y = (coordinates[1].y + coordinates[2].y)/2 - shiftY;
            Coordinate midpoint1 = new Coordinate(mid1x, mid1y);
            Coordinate midpoint2 = new Coordinate(mid2x, mid2y);
            return new Coordinate[]{midpoint1, midpoint2};
        } else{
            double shiftX = (coordinates[0].x - coordinates[1].x)/shiftAmount;
            double shiftY = (coordinates[0].y - coordinates[1].y)/shiftAmount;
            double mid1x = (coordinates[0].x + coordinates[1].x)/2 - shiftX;
            double mid1y = (coordinates[0].y + coordinates[1].y)/2 - shiftY;
            double mid2x = (coordinates[2].x + coordinates[3].x)/2 - shiftX;
            double mid2y = (coordinates[2].y + coordinates[3].y)/2 - shiftY;
            Coordinate midpoint1 = new Coordinate(mid1x, mid1y);
            Coordinate midpoint2 = new Coordinate(mid2x, mid2y);
            return new Coordinate[]{midpoint1, midpoint2};
        }
    }

    private Geometry getSimpleGeometry(Geometry geometry){
        return TopologyPreservingSimplifier.simplify(geometry, 0.01);
    }

    public static boolean isTriangle(Geometry geometry, double tolerance){
        return !(DouglasPeuckerSimplifier.simplify(geometry, tolerance).getCoordinates().length > 4);
    }
}

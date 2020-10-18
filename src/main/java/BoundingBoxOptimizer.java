import javafx.scene.paint.Color;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import java.util.ArrayList;
import java.util.Random;

public class BoundingBoxOptimizer {
    public void  BoundingBoxOptimization(LandParcel inputParcel, double minArea, double minStreetWidth, double streetAccessLevel, double triangleMinArea, double roadLength){
        Mesh parcelMesh = new Mesh(inputParcel);
        //Create large and small footprint lists
        ArrayList<Mesh.Face> largeFootprints = new ArrayList<>();
        ArrayList<Mesh.Face> smallFootprints = new ArrayList<>();
        largeFootprints.add(parcelMesh.footprint);

        while (largeFootprints.size() != 0){
            // Get Bounding Box
            MinimumDiameter minimumDiameter = new MinimumDiameter(parcelMesh.faceToPolygon(largeFootprints.get(0)));
            Geometry boundingBox = minimumDiameter.getMinimumRectangle();

            // Check if is a triangle if so chop off the end of it



            // Split footprint normally
            Mesh.Face[] footprints;
            try {
                Mesh.Vertex[] splitLine = getEdgeSplit(boundingBox, false);
                footprints = parcelMesh.splitEdge(splitLine[0].position, splitLine[1].position, largeFootprints.get(0));
            } catch(Exception e){
                break;
            }
            // Rotated Split

            //Mesh.Vertex[] splitLineRot = getEdgeSplit(boundingBox, true);
            //Mesh.Face[] footprintsRot = parcelMesh.splitEdge(splitLine[0].position, splitLine[1].position, largeFootprints.get(0));

            // Check which split is best

            if(parcelMesh.faceToPolygon(footprints[0]).getArea() < minArea){
                smallFootprints.add(footprints[0]);
            } else {
                largeFootprints.add(footprints[0]);
            }

            if(parcelMesh.faceToPolygon(footprints[1]).getArea() < minArea){
                smallFootprints.add(footprints[1]);
            }else {
                largeFootprints.add(footprints[1]);
            }

            largeFootprints.remove(0);

            // Add new footprints to list of footprints to update
        }

        for (Mesh.Face face : smallFootprints) {
            //SceneRenderer.render(parcelMesh.faceToPolygon(face), Color.AZURE);
        }
    }



    public Mesh.Vertex[] getEdgeSplit(Geometry boundingBox, boolean invertResult){
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
            Mesh.Vertex vertexA = new Mesh.Vertex(midpoint1);
            Mesh.Vertex vertexB = new Mesh.Vertex(midpoint2);
            return new Mesh.Vertex[]{vertexA, vertexB};
        } else{
            double mid1x = (coordinates[0].x + coordinates[1].x)/2;
            double mid1y = (coordinates[0].y + coordinates[1].y)/2;
            double mid2x = (coordinates[2].x + coordinates[3].x)/2;
            double mid2y = (coordinates[2].y + coordinates[3].y)/2;
            Coordinate midpoint1 = new Coordinate(mid1x, mid1y);
            Coordinate midpoint2 = new Coordinate(mid2x, mid2y);
            Mesh.Vertex vertexA = new Mesh.Vertex(midpoint1);
            Mesh.Vertex vertexB = new Mesh.Vertex(midpoint2);
            return new Mesh.Vertex[]{vertexA, vertexB};
        }
    }
}

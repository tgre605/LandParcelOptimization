import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class BuildingPlacer {
    ArrayList<Geometry> RBuildingFootprints = new ArrayList<>();
    ArrayList<Geometry> CBuildingFootprints = new ArrayList<>();
    ArrayList<Geometry> IBuildingFootprints = new ArrayList<>();

    public void placeBuildings(LandParcel landParcel, JsonReader reader){
        RBuildingFootprints = reader.RBuildingFootprints;
        CBuildingFootprints = reader.CBuildingFootprints;
        IBuildingFootprints = reader.IBuildingFootprints;
        for (Footprint footprint: landParcel.footprints) {
            if(footprint.hasBuilding && footprint.getRoadsideEdges().size()>0){
                switch (landParcel.landType){
                    case industry:
                        footprint.addBuilding(new Building(footprint, IBuildingFootprints, landParcel.landType));
                        break;
                    case commercial:
                        footprint.addBuilding(new Building(footprint, CBuildingFootprints, landParcel.landType));
                        break;
                    case residential:
                        footprint.addBuilding(new Building(footprint, RBuildingFootprints, landParcel.landType));
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    public void setRoadCentre(LandParcel landParcel){
        for(Footprint footprint: landParcel.footprints){
            if(footprint.getRoadsideEdges().size() > 0){
                double longestEdge = 0;
                Coordinate[] roadSideEdges = null;
                Hashtable<Coordinate[], Road> edges =footprint.getRoadsideEdges();
                A: for (Coordinate[] edge: edges.keySet()) {
                    if(edges.get(edge).roadType == Road.RoadType.subRoad){
                        if(edge[0].distance(edge[1]) >= longestEdge) {
                            longestEdge = edge[0].distance(edge[1]);
                            roadSideEdges = edge;
                        }
                    } else {
                        if(edge[0].distance(edge[1]) >= longestEdge) {
                            longestEdge = edge[0].distance(edge[1]);
                            roadSideEdges = edge;
                        }
                    }
                }
                footprint.roadCentre = findRoadCentre(roadSideEdges);
                footprint.usableRoad = edges.get(roadSideEdges);
            }
        }
    }

    private Coordinate findRoadCentre(Coordinate[] roadSideEdges){
        try{
            double xMid = (roadSideEdges[0].x+roadSideEdges[1].x)/2;
            double yMid = (roadSideEdges[0].y+roadSideEdges[1].y)/2;
            Coordinate response =  new Coordinate(xMid, yMid);
            return response;
        } catch (NullPointerException e){
            System.out.println(e);
            return roadSideEdges[0];
        }
    }
    
    public void createDriveway(Footprint footprint){
        if(footprint.drivewayVertices == null) {
            Footprint neighbourWithEdge = null;
            boolean neighbourPair = false;
            Footprint neighbourNoEdge = null;
            Footprint neighbourNEN = null;
            Footprint neighbour = null;
            Coordinate NPCoord1 = null;
            Coordinate NPCoord2 = null;
            Coordinate[] driveWayVertices = new Coordinate[2];
            if (footprint.getRoadsideEdges().size() == 0) {
                A:
                for (int i = 0; i < footprint.neighbours.size(); i++) {
                    neighbour = footprint.neighbours.get(i);
                    for (Footprint neighbourTest : footprint.neighbours) {
                        if (neighbourTest.getRoadsideEdges().size() == 0 && neighbourTest.id != footprint.id) {
                            for (Footprint neighbourTestN : neighbourTest.neighbours) {
                                if (neighbour.geometry.intersects(neighbourTestN.geometry) || neighbour.geometry.touches(neighbourTestN.geometry)) {
                                    neighbourPair = true;
                                    neighbourNoEdge = neighbourTest;
                                    neighbourNEN = neighbourTestN;
                                    break A;
                                }
                            }
                        }
                    }
                    if (neighbour.getRoadsideEdges().size() > 0) {
                        neighbourWithEdge = neighbour;
                        break A;
                    } else {
                        for (int j = 0; j < neighbour.getRoadsideEdges().size(); j++) {
                            int largestEdge = 0;
                            int tempWidth = neighbour.getRoadsideEdges().keys().nextElement().length;
                            if (tempWidth > largestEdge) {
                                largestEdge = tempWidth;
                                neighbourWithEdge = neighbour;
                            }
                        }
                    }
                }
                if (neighbourPair == true) {
                    for (Coordinate testCoord : neighbour.geometry.getCoordinates()) {
                        for (Coordinate test2Coord : neighbourNEN.geometry.getCoordinates()) {
                            if (testCoord.equals(test2Coord)) {
                                if (NPCoord1 == null) {
                                    NPCoord1 = testCoord;
                                } else {
                                    NPCoord2 = testCoord;
                                    driveWayVertices[0] = NPCoord1;
                                    driveWayVertices[1] = NPCoord2;
                                    neighbourNoEdge.drivewayVertices = driveWayVertices;
                                    return;
                                }
                            }
                        }
                    }
                }
                Coordinate closestVertexN = null;
                Coordinate closestVertex = null;
                if (neighbourWithEdge.getRoadsideEdges().size() > 0) {
                    for (Coordinate roadSideEdgeVertex : neighbourWithEdge.getRoadsideEdges().keys().nextElement()) {
                        double closestPoints = 99999;
                        for (Coordinate footprintVertex : footprint.geometry.getCoordinates()) {
                            if (roadSideEdgeVertex.distance(footprintVertex) < closestPoints) {
                                closestPoints = roadSideEdgeVertex.distance(footprintVertex);
                                closestVertex = footprintVertex;
                                closestVertexN = roadSideEdgeVertex;
                            }
                        }
                    }

                    driveWayVertices[0] = closestVertex;
                    driveWayVertices[1] = closestVertexN;
                    footprint.drivewayVertices = driveWayVertices;
                    SceneRenderer.renderLine(driveWayVertices);
                }

            }
        }
    }

    public void surroundingFootprints(LandParcel landParcel){
        for (Footprint footprint: landParcel.footprints) {
            for (Footprint footprintCompare: landParcel.footprints) {
                if (footprintCompare.geometry.getCentroid().distance(footprint.geometry.getCentroid()) < 6){
                    if(footprint.id != footprintCompare.id){
                        double xCentre = footprint.geometry.getCentroid().getX();
                        double yCentre = footprint.geometry.getCentroid().getY();
                        AffineTransformation atCentre = new AffineTransformation();
                        atCentre.scale(1.001,1.001);
                        atCentre.translate(-xCentre*0.001, -yCentre*0.001);
                        Geometry scaled = atCentre.transform(footprint.geometry);
                        if(scaled.intersects(footprintCompare.geometry)|| scaled.overlaps(footprintCompare.geometry)){
                            footprint.neighbours.add(footprintCompare);
                        }
                    }
                }
            }
        }
    }
}

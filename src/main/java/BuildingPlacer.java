import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.util.ArrayList;

public class BuildingPlacer {
    ArrayList<Polygon> RBuildingFootprints = new ArrayList<>();
    ArrayList<Geometry> CBuildingFootprints = new ArrayList<>();
    ArrayList<Geometry> IBuildingFootprints = new ArrayList<>();

    public void placeBuildings(LandParcel landParcel, JsonReader reader){
        RBuildingFootprints = reader.RBuildingFootprints;
        CBuildingFootprints = reader.CBuildingFootprints;
        IBuildingFootprints = reader.IBuildingFootprints;
        for (Footprint footprint: landParcel.footprints) {
            if(footprint.roadsideEdges.size()>0){
                footprint.addBuilding(new Building(footprint, RBuildingFootprints.get(2)));
            }
        }
    }
    
    public void setRoadCentre(LandParcel landParcel){
        for(Footprint footprint: landParcel.footprints){
            if(footprint.roadsideEdges.size() > 0){
                Coordinate[] roadSideEdges = null;
                A: for (Road road: footprint.roadsideEdges.values()) {
                    if(road.roadType == Road.RoadType.subRoad){
                        roadSideEdges = footprint.roadsideEdges.keys().nextElement();
                        break A;
                    } else {
                        roadSideEdges = footprint.roadsideEdges.keys().nextElement();
                    }
                }
                footprint.roadCentre = findRoadCentre(roadSideEdges);
            }
        }
    }

    public void setRoadCentreT(Footprint footprint){
        Coordinate[] roadSideEdges = footprint.roadsideEdges.keys().nextElement();
        footprint.roadCentre = findRoadCentre(roadSideEdges);
    }

    private Coordinate findRoadCentre(Coordinate[] roadSideEdges){
        double xMid = (roadSideEdges[0].x+roadSideEdges[1].x)/2;
        double yMid = (roadSideEdges[0].y+roadSideEdges[1].y)/2;
        Coordinate response =  new Coordinate(xMid, yMid);
        return response;
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
            if (footprint.roadsideEdges.size() == 0) {
                A:
                for (int i = 0; i < footprint.neighbours.size(); i++) {
                    neighbour = footprint.neighbours.get(i);
                    for (Footprint neighbourTest : footprint.neighbours) {
                        if (neighbourTest.roadsideEdges.size() == 0 && neighbourTest.id != footprint.id) {
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
                    if (neighbour.roadsideEdges.size() > 0) {
                        neighbourWithEdge = neighbour;
                        break A;
                    } else {
                        for (int j = 0; j < neighbour.roadsideEdges.size(); j++) {
                            int largestEdge = 0;
                            int tempWidth = neighbour.roadsideEdges.keys().nextElement().length;
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
                if (neighbourWithEdge.roadsideEdges.size() > 0) {
                    for (Coordinate roadSideEdgeVertex : neighbourWithEdge.roadsideEdges.keys().nextElement()) {
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

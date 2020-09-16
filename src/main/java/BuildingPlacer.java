import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.util.ArrayList;

public class BuildingPlacer {
    public void placeBuildings(LandParcel landParcel){

        for (Footprint footprint: landParcel.footprints) {
            if(footprint.roadsideEdges.size()>0){
                footprint.addBuilding(new Building(footprint));
            }
        }
    }

    public void placeBuildingsT(Footprint footprint){
        footprint.addBuilding(new Building(footprint));
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
        Footprint neighbourWithEdge = null;
        if(footprint.roadsideEdges.size() == 0){
            A : for (Footprint neighbour: footprint.neighbours) {
//                if (neighbour.roadsideEdges.size() == 1){
                    neighbourWithEdge = neighbour;
//                    break A;
//                } else {
//                    for(int i = 0; i < neighbour.roadsideEdges.size(); i++){
//                        int largestEdge = 0;
//                        int tempWidth = neighbour.roadsideEdges.keys().nextElement().length;
//                        if(tempWidth > largestEdge){
//                            largestEdge = tempWidth;
//                            neighbourWithEdge = neighbour;
//                        }
//                    }
//                }
            }
            Coordinate closestVertexN = null;
            Coordinate closestVertex = null;
            if (neighbourWithEdge.roadsideEdges.size() > 0) {
                for (Coordinate roadSideEdgeVertex: neighbourWithEdge.roadsideEdges.keys().nextElement()) {
                    double closestPoints = 99999;
                    for (Coordinate footprintVertex: footprint.geometry.getCoordinates()) {
                        if (roadSideEdgeVertex.distance(footprintVertex) < closestPoints){
                            closestPoints = roadSideEdgeVertex.distance(footprintVertex);
                            closestVertex = footprintVertex;
                            closestVertexN = roadSideEdgeVertex;
                        }
                    }
                }
                Coordinate[] lineTest = new Coordinate[2];
                lineTest[0] = closestVertex;
                lineTest[1] = closestVertexN;
                SceneRenderer.render(lineTest);
            }
            
        }
    }

    public void surroundingFootprints(LandParcel landParcel){
        for (Footprint footprint: landParcel.footprints) {
            for (Footprint footprintCompare: landParcel.footprints) {
                if(footprint.id != footprintCompare.id){
                    if(footprint.geometry.touches(footprintCompare.geometry)){
                        footprint.neighbours.add(footprintCompare);
                    }
                }
            }
        }
    }
}

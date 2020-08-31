import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.AffineTransformation;

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
                footprint.roadCentre = findCentre(roadSideEdges);
            }
        }
    }

    public void setRoadCentreT(Footprint footprint){
        Coordinate[] roadSideEdges = footprint.roadsideEdges.keys().nextElement();
        footprint.roadCentre = findCentre(roadSideEdges);
    }

    private Coordinate findCentre(Coordinate[] roadSideEdges){
        double xMid = (roadSideEdges[0].x+roadSideEdges[1].x)/2;
        double yMid = (roadSideEdges[0].y+roadSideEdges[1].y)/2;
        Coordinate response =  new Coordinate(xMid, yMid);
        return response;
    }
}

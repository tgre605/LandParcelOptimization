import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class Footprint {
    public Geometry geometry;
    public int id;
    static int nextId;
    HashMap<Integer, Road> roadsideIndex = new HashMap<>();
    Building building;
    Coordinate roadCentre;

    public Footprint (Geometry shape){
        this.geometry = shape;
        id = nextId;
        nextId++;
    }

    public Hashtable<Coordinate[], Road> getRoadsideEdges(){
        Hashtable<Coordinate[], Road> roadsideEdges= new Hashtable<Coordinate[], Road>();
        Coordinate[] coordinates = geometry.getCoordinates();
        for (Integer keys: roadsideIndex.keySet()) {
            roadsideEdges.put(new Coordinate[]{ coordinates[keys], coordinates[keys+1]}, roadsideIndex.get(keys));
        }
        return roadsideEdges;
    }

    public void addBuilding(Building buildingP){
        this.building = buildingP;
    }
}

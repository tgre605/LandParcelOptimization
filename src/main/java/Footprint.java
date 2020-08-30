import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Hashtable;

public class Footprint {
    public Geometry geometry;
    public int id;
    static int nextId;
    Hashtable<Coordinate[], Road> roadsideEdges= new Hashtable<Coordinate[], Road>();
    Building building;
    Coordinate roadCentre;

    public Footprint (Geometry shape){
        this.geometry = shape;
        id = nextId;
        nextId++;
    }

    public void addBuilding(Building buildingP){
        this.building = buildingP;
    }
}

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class Footprint {
    public Geometry geometry;
    public int id;
    static int nextId;
    HashMap<Integer, Road> roadsideIndex = new HashMap<>();
    public ArrayList<Footprint> neighbours = new ArrayList<>();
    public LandParcel landParcel;
    Coordinate[] drivewayVertices = null;
    Building building;
    LineString driveway;
    Coordinate roadCentre;
    public Road usableRoad;
    public double population;
    public double populationDensity;

    public Footprint (Geometry shape){
        this.geometry = shape;
        id = nextId;
        this.geometry.setUserData(id);
        population = 653;
        populationDensity = 0.003658769001167474;
        nextId++;
    }

    public void addBuilding(Building buildingP){
        this.building = buildingP;
    }

    public Hashtable<Coordinate[], Road> getRoadsideEdges(){
        Hashtable<Coordinate[], Road> roadsideEdges= new Hashtable<Coordinate[], Road>();
        Coordinate[] coordinates = geometry.getCoordinates();
        for (Integer keys: roadsideIndex.keySet()) {
            roadsideEdges.put(new Coordinate[]{ coordinates[keys], coordinates[keys+1]}, roadsideIndex.get(keys));
        }
        return roadsideEdges;
    }
}

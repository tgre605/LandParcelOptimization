import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import java.util.ArrayList;
import java.util.Hashtable;

public class Footprint {
    public Geometry geometry;
    public int id;
    static int nextId;
    public ArrayList<Footprint> neighbours = new ArrayList<>();
    public LandParcel landParcel;
    Hashtable<Coordinate[], Road> roadsideEdges= new Hashtable<Coordinate[], Road>();
    Coordinate[] drivewayVertices = null;
    Building building;
    LineString driveway;
    Coordinate roadCentre;
    public double population;
    public double populationDensity;

    public Footprint (Geometry shape){
        this.geometry = shape;
        id = nextId;
        population = 653;
        populationDensity = 0.003658769001167474;
        nextId++;
    }

    public void addBuilding(Building buildingP){
        this.building = buildingP;
    }
}

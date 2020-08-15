import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;

public class Footprint {
    public Geometry geometry;
    public int id;
    static int nextId;
    Building building;

    public Footprint (Geometry shape){
        this.geometry = shape;
        id = nextId;
        nextId++;
    }

    public void addBuilding(Building buildingP){
        this.building = buildingP;
    }
}

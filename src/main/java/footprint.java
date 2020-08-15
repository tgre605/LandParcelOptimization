import org.locationtech.jts.geom.Geometry;

public class footprint {
    public Geometry shape;
    public int id;
    static int nextId;

    public footprint (Geometry shape){
        this.shape = shape;
        id = nextId;
        nextId++;
    }
}

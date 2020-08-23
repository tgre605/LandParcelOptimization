import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geomgraph.Edge;

public class Road {
    public enum RoadType{highway, mainRoad, subRoad, undefined}
    RoadType roadType;
    public Coordinate coordinateA;
    public Coordinate coordinateB;

    public Road (Coordinate coordinateA, Coordinate coordinateB, RoadType roadType){
        this.coordinateA = coordinateA;
        this.coordinateB = coordinateB;
        this.roadType = roadType;
    }

}

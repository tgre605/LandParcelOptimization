import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;

public class Road {
    public enum RoadType{highway, mainRoad, subRoad, undefined}
    RoadType roadType;
    public ArrayList<Coordinate> vertices = new ArrayList<>();
    public Coordinate start;
    public Coordinate end;

    public Road (Coordinate start, Coordinate end, RoadType roadType){
        this.start = start;
        this.end = end;
        this.roadType = roadType;
    }

}

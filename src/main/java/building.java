import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class building {
    public enum type{residential, commercial, industry, undefined}
    public Polygon polygon = new Polygon(null, null, new GeometryFactory());
    public int id;

    private ArrayList<Coordinate> vertices = new ArrayList<>();
    private landParcel.type landType;
    private double population;
    private double populationDensity;
    private static int nextId = 0;


    public building(ArrayList<Coordinate> vertices, double population, double populationDensity) {
        polygon = new GeometryFactory().createPolygon(vertices.toArray(new Coordinate[0]));
        AffineTransformation at = new AffineTransformation();
        at.scale(0.5,0.5);
        Geometry scaledGeom = at.transform(polygon);
        this.vertices = vertices;
        this.id = building.nextId;
        this.population = population;
        this.populationDensity = populationDensity;
        building.nextId++;
    }
}

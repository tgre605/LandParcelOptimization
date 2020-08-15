import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

public class Building {
    public enum type{residential, commercial, industry, undefined}
    public Polygon polygon = new Polygon(null, null, new GeometryFactory());
    public int id;
    private LandParcel.type landType;
    private double population;
    private double populationDensity;
    private static int nextId = 0;


    public Building(Footprint footprint) {
        double xCentre = footprint.geometry.getCentroid().getX();
        double yCentre = footprint.geometry.getCentroid().getY();
        AffineTransformation at = new AffineTransformation();
        at.scale(0.5,0.5);
        at.translate(xCentre*0.5, yCentre*0.5);
        Geometry scaledGeom = at.transform(footprint.geometry);
        this.id = Building.nextId;
        this.population = population;
        this.populationDensity = populationDensity;
        Building.nextId++;
        SceneRenderer.render(scaledGeom);
    }
}

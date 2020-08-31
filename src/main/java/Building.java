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
        this.id = Building.nextId;
        this.population = footprint.population;
        this.populationDensity = footprint.populationDensity;
        double xCentre = footprint.geometry.getCentroid().getX();
        double yCentre = footprint.geometry.getCentroid().getY();
        AffineTransformation atCentre = new AffineTransformation();
        atCentre.scale(0.5,0.5);
        atCentre.translate(xCentre*0.5, yCentre*0.5);
        Geometry centredGeom = atCentre.transform(footprint.geometry);
        AffineTransformation atMove = new AffineTransformation();
        double xVector = (footprint.roadCentre.x - xCentre)*populationDensity*100;
        double yVector = (footprint.roadCentre.y - yCentre)*populationDensity*100;
        atMove.translate(xVector, yVector);
        Geometry movedGeom = atMove.transform(centredGeom);
        this.polygon = new GeometryFactory().createPolygon(movedGeom.getCoordinates());
        this.id = Building.nextId;
        this.population = footprint.population;
        this.populationDensity = footprint.populationDensity;
        Building.nextId++;
    }
}

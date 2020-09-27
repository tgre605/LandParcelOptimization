import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
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
    public double tip = 1.5;
    private static int nextId = 0;


    public Building(Footprint footprint, Polygon buildingFootprint) {
        double buildingFootprintScale = 0.3;
        this.id = Building.nextId;
        this.population = footprint.population;
        this.populationDensity = footprint.populationDensity;
        double xCentre = footprint.geometry.getCentroid().getX() - buildingFootprint.getCentroid().getX()*buildingFootprintScale;
        double yCentre = footprint.geometry.getCentroid().getY() - buildingFootprint.getCentroid().getY()*buildingFootprintScale;
        AffineTransformation atCentre = new AffineTransformation();
        atCentre.scale(buildingFootprintScale,buildingFootprintScale);
        atCentre.translate(xCentre, yCentre);
        Geometry centredGeom = atCentre.transform(buildingFootprint);
        AffineTransformation atMove = new AffineTransformation();
        AffineTransformation atRotate = new AffineTransformation();
        double xVector = (footprint.roadCentre.x - xCentre)*populationDensity*100;
        double yVector = (footprint.roadCentre.y - yCentre)*populationDensity*100;
        Coordinate tip2 = new Coordinate(centredGeom.getCentroid().getCoordinate().getX(), centredGeom.getCentroid().getCoordinate().getY()+1.5);
        double angle = Angle.angleBetweenOriented(footprint.roadCentre, centredGeom.getCentroid().getCoordinate(), tip2);
        atRotate.rotate(angle, centredGeom.getCentroid().getCoordinate().getX(), centredGeom.getCentroid().getCoordinate().getY());
        atMove.translate(xVector, yVector);
        Geometry rotatedGeom = atRotate.transform(centredGeom);
        this.polygon = new GeometryFactory().createPolygon(rotatedGeom.getCoordinates());
        this.id = Building.nextId;
        this.population = footprint.population;
        this.populationDensity = footprint.populationDensity;
        Building.nextId++;
    }
}

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.util.ArrayList;

public class Building {
    public enum type{residential, commercial, industry, undefined}
    public Polygon polygon = new Polygon(null, null, new GeometryFactory());
    public int id;
    private LandParcel.type landType;
    private double population;
    private double populationDensity;
    public double tip = 1.5;
    private static int nextId = 0;


    public Building(Footprint footprint, ArrayList<Geometry> buildingFootprints, LandParcel.type landType) {
        double buildingFootprintScale = 0.3;
        this.id = Building.nextId;
        this.population = footprint.population;
        this.populationDensity = footprint.populationDensity;
        Geometry buildingFootprint = null;
        double xVector = 0;
        double yVector = 0;
        switch (landType){
            case industry:
                buildingFootprint = buildingFootprints.get(2);
                break;
            case commercial:
                buildingFootprint = buildingFootprints.get(1);
                break;
            case residential:
                buildingFootprint = buildingFootprints.get(2);
                break;
            default:
                break;
        }
        
        double xCentre = footprint.geometry.getCentroid().getX() - buildingFootprint.getCentroid().getX()*buildingFootprintScale;
        double yCentre = footprint.geometry.getCentroid().getY() - buildingFootprint.getCentroid().getY()*buildingFootprintScale;
        AffineTransformation atCentre = new AffineTransformation();
        atCentre.scale(buildingFootprintScale,buildingFootprintScale);
        atCentre.translate(xCentre, yCentre);
        Geometry centredGeom = atCentre.transform(buildingFootprint);
        while (!footprint.geometry.contains(centredGeom)){
            xCentre = footprint.geometry.getCentroid().getX() - centredGeom.getCentroid().getX()*0.8;
            yCentre = footprint.geometry.getCentroid().getY() - centredGeom.getCentroid().getY()*0.8;
            atCentre = new AffineTransformation();
            atCentre.scale(0.8,0.8);
            atCentre.translate(xCentre, yCentre);
            centredGeom = atCentre.transform(centredGeom);
        }
        
        AffineTransformation atRotate = new AffineTransformation();
        
        Coordinate tip2 = new Coordinate(centredGeom.getCentroid().getCoordinate().getX(), centredGeom.getCentroid().getCoordinate().getY()-.5);
        double angle = -Angle.angleBetweenOriented(footprint.roadCentre, centredGeom.getCentroid().getCoordinate(), tip2);
        if(footprint.id == 216){
            //SceneRenderer.render(tip2);
            //SceneRenderer.render(centredGeom.getCentroid().getCoordinate());
            SceneRenderer.render(footprint.roadCentre);
            //SceneRenderer.render(footprint.geometry.getCoordinates());
        }
        atRotate.rotate(angle, centredGeom.getCentroid().getCoordinate().getX(), centredGeom.getCentroid().getCoordinate().getY());
        AffineTransformation atMove = null;
        switch (landType){
            case industry:
                atMove = new AffineTransformation();
                xVector = (footprint.roadCentre.x - xCentre)*populationDensity*100;
                yVector = (footprint.roadCentre.y - yCentre)*populationDensity*100;
                break;
            case commercial:
                atMove = new AffineTransformation();
                xVector = (footprint.roadCentre.x - xCentre)*populationDensity*100;
                yVector = (footprint.roadCentre.y - yCentre)*populationDensity*100;
                break;
            case residential:
                atMove = new AffineTransformation();
                xVector = (footprint.roadCentre.x - xCentre)*populationDensity*100;
                yVector = (footprint.roadCentre.y - yCentre)*populationDensity*100;
                break;
            default:
                break;
        }
        atMove.translate(xVector, yVector);
        Geometry rotatedGeom = atRotate.transform(centredGeom);
        Geometry finalGeom = atMove.transform(rotatedGeom);
        this.polygon = new GeometryFactory().createPolygon(finalGeom.getCoordinates());
        this.id = Building.nextId;
        this.population = footprint.population;
        this.populationDensity = footprint.populationDensity;
        Building.nextId++;
    }
}

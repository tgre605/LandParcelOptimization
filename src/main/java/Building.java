import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class Building {
    public enum type{residential, commercial, industry, undefined}
    public Polygon polygon = new Polygon(null, null, new GeometryFactory());
    public int id;
    private LandParcel.type landType;
    private double population;
    private double populationDensity;
    public double tip = 1.5;
    private static int nextId = 0;
    private double scalingFactorTotal = 1;


    public Building(Footprint footprint, ArrayList<Geometry> buildingFootprints, LandParcel.type landType) {
        double buildingFootprintScale = 0.3;
        double buildingFootprintScaleAdjust = 0.9;
        
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
                if(abs(footprint.usableRoad.start.x - footprint.usableRoad.end.x)>abs(footprint.usableRoad.start.y - footprint.usableRoad.end.y)){
                    buildingFootprint = buildingFootprints.get(2);
                } else {
                    buildingFootprint = buildingFootprints.get(1);
                }
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
        scalingFactorTotal = scalingFactorTotal*buildingFootprintScale;
        double areaCentredGeom = centredGeom.getArea();
        double areaFootprintGeom = footprint.geometry.getArea();
        while ((areaCentredGeom/areaFootprintGeom) >= 0.4){
            xCentre = footprint.geometry.getCentroid().getX() - centredGeom.getCentroid().getX()*buildingFootprintScaleAdjust;
            yCentre = footprint.geometry.getCentroid().getY() - centredGeom.getCentroid().getY()*buildingFootprintScaleAdjust;
            scalingFactorTotal = scalingFactorTotal * buildingFootprintScaleAdjust;
            atCentre = new AffineTransformation();
            atCentre.scale(buildingFootprintScaleAdjust,buildingFootprintScaleAdjust);
            atCentre.translate(xCentre, yCentre);
            centredGeom = atCentre.transform(centredGeom);
            areaCentredGeom = centredGeom.getArea();
        }
        
        while (!footprint.geometry.contains(centredGeom)){
            xCentre = footprint.geometry.getCentroid().getX() - centredGeom.getCentroid().getX()*buildingFootprintScaleAdjust;
            yCentre = footprint.geometry.getCentroid().getY() - centredGeom.getCentroid().getY()*buildingFootprintScaleAdjust;
            scalingFactorTotal = scalingFactorTotal * buildingFootprintScaleAdjust;
            atCentre = new AffineTransformation();
            atCentre.scale(buildingFootprintScaleAdjust,buildingFootprintScaleAdjust);
            atCentre.translate(xCentre, yCentre);
            centredGeom = atCentre.transform(centredGeom);
        }

        AffineTransformation atRotate = new AffineTransformation();
        
        Coordinate tip2 = new Coordinate(centredGeom.getCentroid().getCoordinate().getX(), centredGeom.getCentroid().getCoordinate().getY()-.5);
        double angle = -Angle.angleBetweenOriented(footprint.roadCentre, centredGeom.getCentroid().getCoordinate(), tip2);
        atRotate.rotate(angle, centredGeom.getCentroid().getCoordinate().getX(), centredGeom.getCentroid().getCoordinate().getY());
        AffineTransformation atMove = null;
        xCentre = centredGeom.getCentroid().getX();
        yCentre = centredGeom.getCentroid().getY();
        switch (landType){
            case industry:
                atMove = new AffineTransformation();
                xVector = (footprint.roadCentre.x - xCentre)*populationDensity*100;
                yVector = (footprint.roadCentre.y - yCentre)*populationDensity*100;
                break;
            case commercial:
                atMove = new AffineTransformation();
                xVector = (footprint.roadCentre.x - xCentre);
                yVector = (footprint.roadCentre.y - yCentre);
                break;
            case residential:
                atMove = new AffineTransformation();
                if(xCentre > footprint.roadCentre.x){
                    xVector = (footprint.roadCentre.x-xCentre) + scalingFactorTotal*2;
                } else {
                    xVector = (footprint.roadCentre.x-xCentre) - scalingFactorTotal*2;
                }

                if(yCentre > footprint.roadCentre.y){
                    yVector = (footprint.roadCentre.y-yCentre) + scalingFactorTotal*2;
                } else {
                    yVector = (footprint.roadCentre.y-yCentre) - scalingFactorTotal*2;
                }
                //xVector = (footprint.roadCentre.x-xCentre);
                break;
            default:
                break;
        }
        
        atMove.translate(xVector, yVector);
        Geometry rotatedGeom = atRotate.transform(centredGeom);
        Geometry finalGeom = atMove.transform(rotatedGeom);
//        while (!footprint.geometry.contains(finalGeom)){
//            atMove = new AffineTransformation();
//            xCentre = finalGeom.getCentroid().getX();
//            yCentre = finalGeom.getCentroid().getY();
//            if(xCentre > footprint.roadCentre.x){
//                xVector = (footprint.roadCentre.x-xCentre) + scalingFactorTotal;
//            } else {
//                xVector = (footprint.roadCentre.x-xCentre) - scalingFactorTotal;
//            }
//            System.out.println(scalingFactorTotal);
//            System.out.println(xVector);
//            if(yCentre > footprint.roadCentre.y){
//                yVector = (footprint.roadCentre.y-yCentre) + scalingFactorTotal;
//            } else {
//                yVector = (footprint.roadCentre.y-yCentre) - scalingFactorTotal;
//            }
//            atMove.translate(xVector, yVector);
//            finalGeom = atMove.transform(finalGeom);
//        }
        this.polygon = new GeometryFactory().createPolygon(finalGeom.getCoordinates());
        this.id = Building.nextId;
        this.population = footprint.population;
        this.populationDensity = footprint.populationDensity;
        Building.nextId++;
    }
}

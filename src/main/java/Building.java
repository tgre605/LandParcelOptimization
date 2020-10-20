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
                buildingFootprint = buildingFootprints.get(0);
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
        while ((areaCentredGeom/areaFootprintGeom) >= 0.4 && landType == LandParcel.type.residential){
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
        Geometry rotatedGeom = atRotate.transform(centredGeom);
        AffineTransformation atMove = null;
        xCentre = centredGeom.getCentroid().getX();
        yCentre = centredGeom.getCentroid().getY();
        AffineTransformation moveOp = new AffineTransformation();
        switch (landType){
            case industry:
                atMove = new AffineTransformation();
                xVector = (footprint.roadCentre.x - xCentre)*populationDensity*100;
                yVector = (footprint.roadCentre.y - yCentre)*populationDensity*100;
                atMove.translate(xVector, yVector);
                break;
            case commercial:
                atMove = new AffineTransformation();
                xVector = (footprint.roadCentre.x-footprint.geometry.getCentroid().getX());
                yVector = (footprint.roadCentre.y-footprint.geometry.getCentroid().getY());
                a: for(int i = 0; i<15; i++){
                    moveOp = new AffineTransformation();
                    moveOp.translate((i*(xVector)*scalingFactorTotal * 0.05),(i*(yVector)*scalingFactorTotal) * 0.05);
                    Geometry testGeom = moveOp.transform(rotatedGeom);
                    if (testGeom.within(footprint.geometry)) {
                        rotatedGeom = testGeom;
                        
                    } else {
                        break a;
                    }
                    
                }
                break;
            case residential:
                atMove = new AffineTransformation();
                xVector = (footprint.roadCentre.x-footprint.geometry.getCentroid().getX());
                yVector = (footprint.roadCentre.y-footprint.geometry.getCentroid().getY());
                for(int i = 0; i<5; i++){
                    moveOp = new AffineTransformation();
                    moveOp.translate((i*(-xVector)*scalingFactorTotal * 0.05),(i*(-yVector)*scalingFactorTotal) * 0.05);
                    Geometry testGeom = moveOp.transform(rotatedGeom);
                    rotatedGeom = testGeom;
                }
                break;
            default:
                break;
        }
        
        if (footprint.roadCentre.x > footprint.geometry.getCentroid().getX() && landType == LandParcel.type.residential){
            AffineTransformation atSunRotate = new AffineTransformation();
            atSunRotate.setToReflection(footprint.roadCentre.x, footprint.roadCentre.y, footprint.geometry.getCentroid().getX(), footprint.geometry.getCentroid().getY());
            rotatedGeom = atSunRotate.transform(rotatedGeom);
        }
        if(footprint.usableRoad != null && landType == LandParcel.type.residential){
            double xVectorSpaceOp = footprint.usableRoad.start.getX() - footprint.usableRoad.end.getX();
            double yVectorSpaceOp = footprint.usableRoad.start.getY() - footprint.usableRoad.end.getY();
            AffineTransformation spaceOp = new AffineTransformation();
            if(!rotatedGeom.within(footprint.geometry)){
                for(int i = 0; i<51; i++){
                    xVectorSpaceOp = -xVectorSpaceOp;
                    yVectorSpaceOp = -yVectorSpaceOp;
                    spaceOp = new AffineTransformation();
                    spaceOp.translate((i*(xVectorSpaceOp)*scalingFactorTotal * 0.05),(i*(yVectorSpaceOp)*scalingFactorTotal) * 0.05);
                    Geometry testGeom = spaceOp.transform(rotatedGeom);
                    if(testGeom.within(footprint.geometry)){
                        rotatedGeom = testGeom;
                    }
                }
            }
        }
        Geometry finalGeom = null;
        finalGeom = atMove.transform(rotatedGeom);

        
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

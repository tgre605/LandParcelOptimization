import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.operation.distance.DistanceOp;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReferenceArray;

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
    double buildingFootprintScale = 0.3;
    double buildingFootprintScaleAdjustSmaller = 0.9;
    double buildingFootprintScaleAdjustBigger = 1.2;
    double xVector = 0;
    double yVector = 0;
    Geometry finalGeom = null;
    public Building(Footprint footprint, ArrayList<Geometry> buildingFootprints, LandParcel.type landType) {
        this.id = Building.nextId;
        this.population = footprint.population;
        this.populationDensity = footprint.populationDensity;
        double areaUsed = 99999999;
        Geometry rotatedGeom = null;
        if(landType != LandParcel.type.residential){
            for (Geometry buildingFootprint: buildingFootprints
            ) {
                switch (landType) {
                    case commercial:
                        rotatedGeom = commercialBuild(footprint, buildingFootprint);
                        break;
                    case industry:
                        rotatedGeom = industrialBuild(footprint, buildingFootprint);
                        break;
                    default:
                        break;
                }
                double testAreaUsed = footprint.geometry.getArea() - rotatedGeom.getArea();
                if(testAreaUsed < areaUsed){
                    areaUsed = testAreaUsed;
                    finalGeom = rotatedGeom;
                }
            }
        } else {
            if (abs(footprint.usableRoad.start.x - footprint.usableRoad.end.x) > abs(footprint.usableRoad.start.y - footprint.usableRoad.end.y)) {
                finalGeom = residentialBuild(footprint, buildingFootprints.get(2));
            } else {
                finalGeom = residentialBuild(footprint, buildingFootprints.get(1));
            }
        }
        this.polygon = new GeometryFactory().createPolygon(finalGeom.getCoordinates());
        this.id = Building.nextId;
        this.population = footprint.population;
        this.populationDensity = footprint.populationDensity;
        Building.nextId++;
    }

    private Geometry residentialBuild(Footprint footprint, Geometry buildingFootprint){
        Geometry centredGeom = allBuildingsAdjust(footprint, buildingFootprint);
        double xCentre;
        double yCentre;
        AffineTransformation atCentre;
        double areaCentredGeom = centredGeom.getArea();
        double areaFootprintGeom = footprint.geometry.getArea();
        while ((areaCentredGeom / areaFootprintGeom) >= 0.4 && landType == LandParcel.type.residential || !footprint.geometry.contains(centredGeom)) {
            xCentre = footprint.geometry.getCentroid().getX() - centredGeom.getCentroid().getX() * buildingFootprintScaleAdjustSmaller;
            yCentre = footprint.geometry.getCentroid().getY() - centredGeom.getCentroid().getY() * buildingFootprintScaleAdjustSmaller;
            scalingFactorTotal = scalingFactorTotal * buildingFootprintScaleAdjustSmaller;
            atCentre = new AffineTransformation();
            atCentre.scale(buildingFootprintScaleAdjustSmaller, buildingFootprintScaleAdjustSmaller);
            atCentre.translate(xCentre, yCentre);
            centredGeom = atCentre.transform(centredGeom);
            areaCentredGeom = centredGeom.getArea();
        }
        AffineTransformation atRotate = new AffineTransformation();
        Geometry rotatedGeom = rotateGeom(footprint, atRotate, centredGeom);
        AffineTransformation atMove = null;
        xCentre = centredGeom.getCentroid().getX();
        yCentre = centredGeom.getCentroid().getY();
        AffineTransformation moveOp = new AffineTransformation();
        atMove = new AffineTransformation();

        //move building footprint away from road
        xVector = (footprint.roadCentre.x - footprint.geometry.getCentroid().getX());
        yVector = (footprint.roadCentre.y - footprint.geometry.getCentroid().getY());
        for (int i = 0; i < 5; i++) {
            moveOp = new AffineTransformation();
            moveOp.translate((i * (-xVector) * scalingFactorTotal * 0.05), (i * (-yVector) * scalingFactorTotal) * 0.05);
            Geometry testGeom = moveOp.transform(rotatedGeom);
            rotatedGeom = testGeom;
        }

        //flip building to orient for north facing courtyard
        if (footprint.roadCentre.x > footprint.geometry.getCentroid().getX()) {
            AffineTransformation atSunRotate = new AffineTransformation();
            atSunRotate.setToReflection(footprint.roadCentre.x, footprint.roadCentre.y, footprint.geometry.getCentroid().getX(), footprint.geometry.getCentroid().getY());
            rotatedGeom = atSunRotate.transform(rotatedGeom);
        }

        //move building parallel to road keeping same size attempting to find position that doesnt overlap neighbour
        if (footprint.usableRoad != null) {
            double xVectorSpaceOp = footprint.usableRoad.start.getX() - footprint.usableRoad.end.getX();
            double yVectorSpaceOp = footprint.usableRoad.start.getY() - footprint.usableRoad.end.getY();
            AffineTransformation spaceOp = new AffineTransformation();
            if (!rotatedGeom.within(footprint.geometry)) {
                for (int i = 0; i < 51; i++) {
                    xVectorSpaceOp = -xVectorSpaceOp;
                    yVectorSpaceOp = -yVectorSpaceOp;
                    spaceOp = new AffineTransformation();
                    spaceOp.translate((i * (xVectorSpaceOp) * scalingFactorTotal * 0.05), (i * (yVectorSpaceOp) * scalingFactorTotal) * 0.05);
                    Geometry testGeom = spaceOp.transform(rotatedGeom);
                    if (testGeom.within(footprint.geometry)) {
                        rotatedGeom = testGeom;
                    }
                }
            }
            //if building is still overlapping other footprints, scale down until within own footprint
            while (!rotatedGeom.within(footprint.geometry)) {
                xCentre = rotatedGeom.getCentroid().getX();
                yCentre = rotatedGeom.getCentroid().getY();
                AffineTransformation atScale = new AffineTransformation();
                scalingFactorTotal = scalingFactorTotal * buildingFootprintScaleAdjustSmaller;
                atScale = AffineTransformation.scaleInstance(buildingFootprintScaleAdjustSmaller, buildingFootprintScaleAdjustSmaller, xCentre, yCentre);
                rotatedGeom = atScale.transform(rotatedGeom);
            }
        }
        return rotatedGeom;
    }

    private Geometry allBuildingsAdjust(Footprint footprint, Geometry buildingFootprint) {
        double xCentre = footprint.geometry.getCentroid().getX() - buildingFootprint.getCentroid().getX() * buildingFootprintScale;
        double yCentre = footprint.geometry.getCentroid().getY() - buildingFootprint.getCentroid().getY() * buildingFootprintScale;
        AffineTransformation atCentre = new AffineTransformation();
        atCentre.scale(buildingFootprintScale, buildingFootprintScale);
        atCentre.translate(xCentre, yCentre);
        Geometry centredGeom = atCentre.transform(buildingFootprint);
        scalingFactorTotal = scalingFactorTotal * buildingFootprintScale;
        return centredGeom;
    }

    private Geometry industrialBuild(Footprint footprint, Geometry buildingFootprint){
        Geometry centredGeom = allBuildingsAdjust(footprint, buildingFootprint);
        double xCentre;
        double yCentre;
        AffineTransformation atRotate = new AffineTransformation();
        Geometry rotatedGeom = rotateGeom(footprint, atRotate, centredGeom);
        AffineTransformation moveOp = new AffineTransformation();
        xVector = (footprint.roadCentre.x - footprint.geometry.getCentroid().getX());
        yVector = (footprint.roadCentre.y - footprint.geometry.getCentroid().getY());
        xCentre = rotatedGeom.getCentroid().getX();
        yCentre = rotatedGeom.getCentroid().getY();
        b:for (int i = 1; i < 20; i++) {
            AffineTransformation atScale = new AffineTransformation();
            scalingFactorTotal = scalingFactorTotal * buildingFootprintScaleAdjustBigger;
            atScale = AffineTransformation.scaleInstance(buildingFootprintScaleAdjustBigger, buildingFootprintScaleAdjustBigger, xCentre, yCentre);
            atScale.translate((i * (-xVector) * scalingFactorTotal * 0.1), (i * (-yVector) * scalingFactorTotal) * 0.1);
            Geometry testGeom = atScale.transform(rotatedGeom);
            xCentre = testGeom.getCentroid().getX();
            yCentre = testGeom.getCentroid().getY();
            if (testGeom.within(footprint.geometry)) {
                rotatedGeom = testGeom;
            } else {
                break b;
            }
        }
        a:for (int i = 0; i < 15; i++) {
            moveOp = new AffineTransformation();
            moveOp.translate((i * (xVector) * scalingFactorTotal * 0.05), (i * (yVector) * scalingFactorTotal) * 0.05);
            Geometry testGeom = moveOp.transform(rotatedGeom);
            if (testGeom.within(footprint.geometry)) {
                rotatedGeom = testGeom;
            } else {
                break a;
            }
        }
        //if building is still overlapping other footprints, scale down until within own footprint
        while (!rotatedGeom.within(footprint.geometry)) {
            xCentre = rotatedGeom.getCentroid().getX();
            yCentre = rotatedGeom.getCentroid().getY();
            AffineTransformation atScale = new AffineTransformation();
            scalingFactorTotal = scalingFactorTotal * buildingFootprintScaleAdjustSmaller;
            atScale = AffineTransformation.scaleInstance(buildingFootprintScaleAdjustSmaller, buildingFootprintScaleAdjustSmaller, xCentre, yCentre);
            rotatedGeom = atScale.transform(rotatedGeom);
        }
        return rotatedGeom;
    }

    private Geometry rotateGeom(Footprint footprint, AffineTransformation atRotate, Geometry centredGeom) {
        Coordinate tip2 = new Coordinate(centredGeom.getCentroid().getCoordinate().getX(), centredGeom.getCentroid().getCoordinate().getY() - .5);
        double angle = -Angle.angleBetweenOriented(new LineSegment(footprint.usableRoad.start, footprint.usableRoad.end).closestPoint(centredGeom.getCentroid().getCoordinate()), centredGeom.getCentroid().getCoordinate(), tip2);
        atRotate.rotate(angle, centredGeom.getCentroid().getCoordinate().getX(), centredGeom.getCentroid().getCoordinate().getY());
        return atRotate.transform(centredGeom);
    }

    private Geometry commercialBuild(Footprint footprint, Geometry buildingFootprint) {
        Geometry centredGeom = allBuildingsAdjust(footprint, buildingFootprint);
        double xCentre;
        double yCentre;
        AffineTransformation atRotate = new AffineTransformation();
        Geometry rotatedGeom = rotateGeom(footprint, atRotate, centredGeom);
        AffineTransformation moveOp = new AffineTransformation();
        
        //move building away from road while increasing size to make most use of available land
        xVector = (footprint.roadCentre.x - footprint.geometry.getCentroid().getX());
        yVector = (footprint.roadCentre.y - footprint.geometry.getCentroid().getY());
        xCentre = rotatedGeom.getCentroid().getX();
        yCentre = rotatedGeom.getCentroid().getY();
        b:
        for (int i = 1; i < 15; i++) {
            AffineTransformation atScale = new AffineTransformation();
            scalingFactorTotal = scalingFactorTotal * buildingFootprintScaleAdjustBigger;
            atScale = AffineTransformation.scaleInstance(buildingFootprintScaleAdjustBigger, buildingFootprintScaleAdjustBigger, xCentre, yCentre);
            atScale.translate((i * (-xVector) * scalingFactorTotal * 0.1), (i * (-yVector) * scalingFactorTotal) * 0.1);
            Geometry testGeom = atScale.transform(rotatedGeom);
            xCentre = testGeom.getCentroid().getX();
            yCentre = testGeom.getCentroid().getY();
            if (testGeom.within(footprint.geometry)) {
                rotatedGeom = testGeom;
            } else {
                break b;
            }
        }

        //move building back towards road
        a:
        for (int i = 0; i < 15; i++) {
            moveOp = new AffineTransformation();
            moveOp.translate((i * (xVector) * scalingFactorTotal * 0.05), (i * (yVector) * scalingFactorTotal) * 0.05);
            Geometry testGeom = moveOp.transform(rotatedGeom);
            if (testGeom.within(footprint.geometry)) {
                rotatedGeom = testGeom;
            } else {
                break a;
            }
        }

        //if building is still overlapping other footprints, scale down until within own footprint
        while (!rotatedGeom.within(footprint.geometry)) {
            xCentre = rotatedGeom.getCentroid().getX();
            yCentre = rotatedGeom.getCentroid().getY();
            AffineTransformation atScale = new AffineTransformation();
            scalingFactorTotal = scalingFactorTotal * buildingFootprintScaleAdjustSmaller;
            atScale = AffineTransformation.scaleInstance(buildingFootprintScaleAdjustSmaller, buildingFootprintScaleAdjustSmaller, xCentre, yCentre);
            rotatedGeom = atScale.transform(rotatedGeom);
        }
        return rotatedGeom;
    }


}

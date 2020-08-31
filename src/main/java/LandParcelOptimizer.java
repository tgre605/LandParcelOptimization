import com.sun.deploy.xml.GeneralEntity;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geomgraph.Edge;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

import java.util.*;


public class LandParcelOptimizer {

    public double triangleTolerance = 0.25;

    /*
    public  Geometry[] BoundingBoxOptimization(landParcel inputParcel, double minArea, double minStreetWidth, double streetAccessLevel){
        ArrayList<Geometry> largeFootprints = new ArrayList<>();
        ArrayList<Geometry> smallFootprints = new ArrayList<>();
        largeFootprints.add(inputParcel.polygon);

        while(largeFootprints.size() > 0){
            Geometry currentFootprint = largeFootprints.get(0);
            if(currentFootprint.getArea() > minArea && getLongestRoadEdge(inputParcel.polygon, currentFootprint) > minStreetWidth){
                    MinimumDiameter minimumDiameter = new MinimumDiameter(largeFootprints.get(0));
                    Geometry boundingBox = minimumDiameter.getMinimumRectangle();

                    Geometry[] boundingBoxes = halfRectangle(boundingBox, false);
                    Geometry footprintA = splitPolygon(boundingBoxes[0], largeFootprints.get(0));
                    Geometry footprintB = splitPolygon(boundingBoxes[1], largeFootprints.get(0));

                    if (!hasRoadAccess(inputParcel.polygon, footprintA) || !hasRoadAccess(inputParcel.polygon, footprintA)) {
                        boundingBoxes = halfRectangle(boundingBox, true);
                        footprintA = splitPolygon(boundingBoxes[0], largeFootprints.get(0));
                        footprintB = splitPolygon(boundingBoxes[1], largeFootprints.get(0));
                    }

                    largeFootprints.add(footprintA);
                    largeFootprints.add(footprintB);

            } else {
                smallFootprints.add(currentFootprint);
                largeFootprints.remove(0);
            }
        }
        return smallFootprints.toArray(new Geometry[0]);
    }
    */
    public LandParcel BoundingBoxOptimization(LandParcel inputParcel, double minArea, double minStreetWidth, double streetAccessLevel, double triangleMinArea, double roadLength){
        ArrayList<Geometry> largeFootprints = new ArrayList<>();
        ArrayList<Geometry> smallFootprints = new ArrayList<>();
        largeFootprints.add(inputParcel.polygon);

        while (largeFootprints.size() != 0){
            MinimumDiameter minimumDiameter = new MinimumDiameter(largeFootprints.get(0));
            Geometry boundingBox = minimumDiameter.getMinimumRectangle();

            boolean switchedBoundingBox = false;
            //Normal Split
            Geometry[] boundingBoxes = halfRectangle(boundingBox, false);
            Geometry footprintA = splitPolygon(boundingBoxes[0], largeFootprints.get(0));
            Geometry footprintB = splitPolygon(boundingBoxes[1], largeFootprints.get(0));

            boolean hasTriangle  = true,hasRoadAccess = true;
            if(footprintA != null && footprintB != null) {
                hasTriangle = isTriangle(footprintA, triangleTolerance) || isTriangle(footprintB, triangleTolerance);
                hasRoadAccess = !hasRoadAccess(inputParcel.polygon, footprintA) || !hasRoadAccess(inputParcel.polygon, footprintB);
            }

            LineString cuttingEdge = getCommonEdge(boundingBoxes[0], boundingBoxes[1]);

            //Rotated Split
            boundingBoxes = halfRectangle(boundingBox, true);
            Geometry newFootprintA = splitPolygon(boundingBoxes[0], largeFootprints.get(0));
            Geometry newFootprintB = splitPolygon(boundingBoxes[1], largeFootprints.get(0));

            boolean newHasTriangle  = true,newHasRoadAccess  = true;
            if(newFootprintA != null && newFootprintB != null) {
                newHasTriangle = isTriangle(newFootprintA, triangleTolerance) || isTriangle(newFootprintB, triangleTolerance);
                newHasRoadAccess =!hasRoadAccess(inputParcel.polygon, newFootprintA) || !hasRoadAccess(inputParcel.polygon, newFootprintB);
            }

            LineString newCuttingEdge = getCommonEdge(boundingBoxes[0], boundingBoxes[1]);

            //Assessment
            Geometry finalFootprintA = footprintA;
            Geometry finalFootprintB = footprintB;

            boolean rotatedSplit = false;

            if(hasTriangle && !newHasTriangle){
                finalFootprintA = newFootprintA;
                finalFootprintB = newFootprintB;
                rotatedSplit = true;
            } else if(!hasTriangle && newHasTriangle){
                finalFootprintA = footprintA;
                finalFootprintB = footprintB;
            }  else if(!hasRoadAccess && newHasRoadAccess && new Random(getSeedFromPosition(largeFootprints.get(0).getCentroid())).nextDouble() > streetAccessLevel){
                finalFootprintA = newFootprintA;
                finalFootprintB = newFootprintB;
                rotatedSplit = true;
            }

            if(finalFootprintA == null || finalFootprintB == null){
                smallFootprints.add(largeFootprints.get(0));
                largeFootprints.remove(0);
                continue;
            }


            double footprintAEdge = getLongestRoadEdge(inputParcel.polygon, finalFootprintA);
            double footprintBEdge = getLongestRoadEdge(inputParcel.polygon, finalFootprintB);

            if(isTriangle(finalFootprintA, triangleTolerance)  && finalFootprintA.getArea() < triangleMinArea){
                System.out.println("TRIANGLE");
                smallFootprints.add(finalFootprintA);
            } else if(footprintAEdge > minStreetWidth){
                System.out.println("EDGE");
                smallFootprints.add(finalFootprintA);
            } else if(finalFootprintA.getArea() < minArea) {
                smallFootprints.add(finalFootprintA);
            }
            else {
                largeFootprints.add(finalFootprintA);
            }



            if(isTriangle(finalFootprintB, triangleTolerance) && finalFootprintB.getArea() < triangleMinArea){
                smallFootprints.add(finalFootprintB);
            } else if(footprintBEdge > minStreetWidth){
                System.out.println("EDGE");
                smallFootprints.add(finalFootprintB);
            } else if(finalFootprintB.getArea() < minArea) {
                smallFootprints.add(finalFootprintB);
            }
            else {
                largeFootprints.add(finalFootprintB);
            }


            //Add road
            if(rotatedSplit)
                cuttingEdge = newCuttingEdge;

            Geometry intersections = largeFootprints.get(0).intersection(cuttingEdge);
            if(intersections.getCoordinates().length == 2){
                if(intersections.getCoordinates()[0].distance(intersections.getCoordinates()[1]) > roadLength) {
                    inputParcel.subroads.add(new Road(intersections.getCoordinates()[0], intersections.getCoordinates()[1], Road.RoadType.subRoad));
                }
            }
            if(intersections.getCoordinates().length == 1) {
                if (cuttingEdge.getCoordinates()[0].distance(cuttingEdge.getCoordinates()[1]) > roadLength) {
                    SceneRenderer.render(intersections.getCoordinates());
                }
            }

            largeFootprints.remove(0);
        }

        //Assign Land Parcel Roads
        Coordinate[] parcelCoordinates = inputParcel.polygon.getCoordinates();
        for(int i = 0; i < inputParcel.polygon.getCoordinates().length-1; i++){
            inputParcel.subroads.add(new Road(parcelCoordinates[i], parcelCoordinates[i+1], Road.RoadType.mainRoad));
        }

        //Generate Footprints
        for(int i =0; i < smallFootprints.size(); i++){
            Footprint footprint = new Footprint(smallFootprints.get(i));
            footprint  = assignRoadSideEdges(inputParcel.subroads, footprint);
            inputParcel.footprints.add(footprint);
            smallFootprints.get(i).setUserData(footprint.id);
        }
        return inputParcel;
    }

    boolean hasRoadAccess(Geometry landParcelPolygon, Geometry footprint){
        for(int i= 0; i < landParcelPolygon.getCoordinates().length-1; i++){
            for(int j= 0; j < footprint.getCoordinates().length -1 ; j++){
                if(edgeOnLine(landParcelPolygon.getCoordinates()[i],  landParcelPolygon.getCoordinates()[i+1], footprint.getCoordinates()[j], footprint.getCoordinates()[j+1])){
                    return true;
                }
            }
        }
        return false;
    }

    Footprint assignRoadSideEdges(ArrayList<Road> roads, Footprint footprint){
        Coordinate[] coordinates = footprint.geometry.getCoordinates();
        for(int i= 0; i < coordinates.length-1; i++){
            for(int j = 0; j < roads.size(); j++) {
                if (edgeOnLine(roads.get(j).coordinateA, roads.get(j).coordinateB, coordinates[i], coordinates[i + 1])) {
                    footprint.roadsideEdges.put(new Coordinate[]{coordinates[i], coordinates[i + 1]}, roads.get(j));
                }

            }
        }
        return footprint;
    }

    LineString getCommonEdge(Geometry boundingBoxA, Geometry boundingBoxB){
        ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>(Arrays.asList(boundingBoxA.getCoordinates()));
        coordinates.retainAll(new ArrayList<Coordinate>(Arrays.asList(boundingBoxB.getCoordinates())));
        return new GeometryFactory().createLineString(new Coordinate[]{coordinates.get(0), coordinates.get(1)});
    }

    double getLongestRoadEdge(Geometry landParcelPolygon, Geometry footprint){
        ArrayList<Coordinate> longestEdge = new ArrayList<>();
        double longestLength = -1;


        for(int i= 0; i < landParcelPolygon.getCoordinates().length-1; i++){
            for(int j= 0; j < footprint.getCoordinates().length -1 ; j++){
                if(edgeOnLine(landParcelPolygon.getCoordinates()[i],  landParcelPolygon.getCoordinates()[i+1], footprint.getCoordinates()[j], footprint.getCoordinates()[j+1])){
                    if(footprint.getCoordinates()[j].distance(footprint.getCoordinates()[j+1]) > longestLength){
                        try {
                            longestEdge.set(0, footprint.getCoordinates()[j]);
                            longestEdge.set(0, footprint.getCoordinates()[j+1]);
                        } catch (IndexOutOfBoundsException e){
                            longestEdge.add(footprint.getCoordinates()[j]);
                            longestEdge.add(footprint.getCoordinates()[j+1]);
                        }
                    }
                }
            }
        }

        return longestLength;
    }



    // LineA -- A -- B -- LineB
    boolean edgeOnLine(Coordinate LineA, Coordinate LineB, Coordinate A, Coordinate B){
        double lineDistance = LineA.distance(LineB);
        double LineAA = LineA.distance(A);
        double AB = A.distance(B);
        double BLineB = B.distance(LineB);

        double total = lineDistance - (LineAA + AB + BLineB);

        double LineAB = LineA.distance(B);
        double ALineB = A.distance(LineB);

        double otherTotal = lineDistance - (LineAB + AB + ALineB);

        return (total < 0.00001 && total > -0.00001) || (otherTotal < 0.00001 && otherTotal > -0.00001);
    }

    public Geometry[] halfRectangle(Geometry boundingBox, boolean invertResult){
        Coordinate[] coordinates = boundingBox.getCoordinates();

        double dist13 = coordinates[0].distance(coordinates[3]);
        double dist12 = coordinates[0].distance(coordinates[1]);

        Geometry rectangleA, rectangleB;

        if (dist12 < dist13 && !invertResult){
            double mid1x = (coordinates[0].x + coordinates[3].x)/2;
            double mid1y = (coordinates[0].y + coordinates[3].y)/2;
            double mid2x = (coordinates[1].x + coordinates[2].x)/2;
            double mid2y = (coordinates[1].y + coordinates[2].y)/2;
            Coordinate midpoint1 = new Coordinate(mid1x, mid1y);
            Coordinate midpoint2 = new Coordinate(mid2x, mid2y);
            rectangleA = new GeometryFactory().createPolygon(new Coordinate[]{coordinates[0], midpoint1, midpoint2, coordinates[1], coordinates[0]});
            rectangleB =new GeometryFactory().createPolygon(new Coordinate[]{coordinates[2], midpoint2,  midpoint1, coordinates[3], coordinates[2]});

        } else{
            double mid1x = (coordinates[0].x + coordinates[1].x)/2;
            double mid1y = (coordinates[0].y + coordinates[1].y)/2;
            double mid2x = (coordinates[2].x + coordinates[3].x)/2;
            double mid2y = (coordinates[2].y + coordinates[3].y)/2;
            Coordinate midpoint1 = new Coordinate(mid1x, mid1y);
            Coordinate midpoint2 = new Coordinate(mid2x, mid2y);
            rectangleA = new GeometryFactory().createPolygon(new Coordinate[]{coordinates[0], midpoint1, midpoint2, coordinates[3], coordinates[0]});
            rectangleB = new GeometryFactory().createPolygon(new Coordinate[]{coordinates[1], midpoint1, midpoint2, coordinates[2], coordinates[1]});
        }

        return new Geometry[]{rectangleA, rectangleB};
    }

    private Long getSeedFromPosition(Point position){
        String positionX = String.valueOf(position.getX()).substring(0, 6);
        String positionY = String.valueOf(position.getY()).substring(0, 6);

        positionX = positionX.replace(".", "");
        positionY = positionY.replace(".", "");

        return Long.parseLong(positionX.concat(positionY));
    }

    public Geometry splitPolygon(Geometry boundingBox, Geometry footprint){
        try {
            Geometry intersect = boundingBox.intersection(validate(footprint));
            //Geometry intersect = boundingBox.intersection(validate(TopologyPreservingSimplifier.simplify(footprint, tolerance)));
            return new GeometryFactory().createPolygon(CoordinateArrays.removeRepeatedPoints(intersect.getCoordinates()));

        } catch (TopologyException e){
            return null;
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    public static Geometry validate(Geometry geom){
        if(geom instanceof Polygon){
            if(geom.isValid()){
                geom.normalize(); // validate does not pick up rings in the wrong order - this will fix that
                return geom; // If the polygon is valid just return it
            }
            Polygonizer polygonizer = new Polygonizer();
            addPolygon((Polygon)geom, polygonizer);
            return toPolygonGeometry(polygonizer.getPolygons(), geom.getFactory());
        }else if(geom instanceof MultiPolygon){
            if(geom.isValid()){
                geom.normalize(); // validate does not pick up rings in the wrong order - this will fix that
                return geom; // If the multipolygon is valid just return it
            }
            Polygonizer polygonizer = new Polygonizer();
            for(int n = geom.getNumGeometries(); n-- > 0;){
                addPolygon((Polygon)geom.getGeometryN(n), polygonizer);
            }
            return toPolygonGeometry(polygonizer.getPolygons(), geom.getFactory());
        }else{
            return geom; // In my case, I only care about polygon / multipolygon geometries
        }
    }
    /**
     * Add all line strings from the polygon given to the polygonizer given
     *
     * @param polygon polygon from which to extract line strings
     * @param polygonizer polygonizer
     */
    static void addPolygon(Polygon polygon, Polygonizer polygonizer){
        addLineString(polygon.getExteriorRing(), polygonizer);
        for(int n = polygon.getNumInteriorRing(); n-- > 0;){
            addLineString(polygon.getInteriorRingN(n), polygonizer);
        }
    }

    /**
     * Add the linestring given to the polygonizer
     *
     * @param lineString line string
     * @param polygonizer polygonizer
     */
    static void addLineString(LineString lineString, Polygonizer polygonizer){

        if(lineString instanceof LinearRing){ // LinearRings are treated differently to line strings : we need a LineString NOT a LinearRing
            lineString = lineString.getFactory().createLineString(lineString.getCoordinateSequence());
        }

        // unioning the linestring with the point makes any self intersections explicit.
        Point point = lineString.getFactory().createPoint(lineString.getCoordinateN(0));
        Geometry toAdd = lineString.union(point);

        //Add result to polygonizer
        polygonizer.add(toAdd);
    }

    /**
     * Get a geometry from a collection of polygons.
     *
     * @param polygons collection
     * @param factory factory to generate MultiPolygon if required
     * @return null if there were no polygons, the polygon if there was only one, or a MultiPolygon containing all polygons otherwise
     */
    static Geometry toPolygonGeometry(Collection<Polygon> polygons, GeometryFactory factory){
        switch(polygons.size()){
            case 0:
                return null; // No valid polygons!
            case 1:
                return polygons.iterator().next(); // single polygon - no need to wrap
            default:
                //polygons may still overlap! Need to sym difference them
                Iterator<Polygon> iter = polygons.iterator();
                Geometry ret = iter.next();
                while(iter.hasNext()){
                    ret = ret.symDifference(iter.next());
                }
                return ret;
        }
    }

    public static boolean isTriangle(Geometry geometry, double tolerance){
        return !(TopologyPreservingSimplifier.simplify(geometry, tolerance).getCoordinates().length > 4);
    }
}

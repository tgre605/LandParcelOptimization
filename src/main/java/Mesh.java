import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class Mesh {
    public int decimalPlaces = 1;
    public HashMap<String, Coordinate> coordinates = new HashMap<>();
    //Key footprintId, String[] the indexes of coordinates
    public Hashtable<Integer, String[]> footprintIndices = new Hashtable<>();
    public HashMap<Integer, String[]> roadIndices = new HashMap<>();
    public Geometry geo;

    public Mesh(ArrayList<Footprint> footprints){
        footprintIndices.clear();
        coordinates.clear();
        for(int i =0 ; i <footprints.size(); i++){
            ArrayList<String> keys = new ArrayList<>();
            Coordinate[] coords = footprints.get(i).geometry.getCoordinates();

            for(int j = 0; j < footprints.get(i).geometry.getCoordinates().length; j++) {
                String key = generateIndex(coords[j]);
                keys.add(key);
                if(i == 213 && j == 4){
                    SceneRenderer.render(footprints.get(i).geometry);
                    SceneRenderer.render(coords[j]);
                    geo = footprints.get(i).geometry;
                }
                if(i == 199 && j == 2){
                    SceneRenderer.render(coords[j]);
                    SceneRenderer.render(footprints.get(i).geometry);
                }
                coordinates.put(key , coords[j]);
            }
            footprintIndices.put(i, keys.toArray(new String[0]));
        }
        //drawPoints();
    }

    LandParcel mergeRoads(LandParcel landParcel){
        for(int i= 0; i < landParcel.subroads.size(); i++){
            for(int j = 0 ; j < landParcel.subroads.size(); j++){
                Road road = landParcel.subroads.get(i);
                Road otherRoad = landParcel.subroads.get(j);
                if(road != otherRoad) {
                    if(road.start.distance(otherRoad.start) < 1 && road.start != otherRoad.start){
                        //subroads.put(generateIndex(otherRoad.coordinateA), road.coordinateA);
                        landParcel.subroads.set(i, mergeRoadIntersection(road, otherRoad, road.start, otherRoad.start)[0]);
                        landParcel.subroads.set(j, mergeRoadIntersection(road, otherRoad, road.start, otherRoad.start)[1]);
                    }
                    if(road.end.distance(otherRoad.start) < 1 && road.end != otherRoad.start){
                        //subroads.put(generateIndex(otherRoad.coordinateA), road.coordinateB);
                        landParcel.subroads.set(i, mergeRoadIntersection(road, otherRoad, road.end, otherRoad.start)[0]);
                        landParcel.subroads.set(j, mergeRoadIntersection(road, otherRoad, road.end, otherRoad.start)[1]);
                    }
                    if(road.start.distance(otherRoad.end) < 1 && road.start != otherRoad.end){
                        landParcel.subroads.set(i, mergeRoadIntersection(road, otherRoad, road.start, otherRoad.end)[0]);
                        landParcel.subroads.set(j, mergeRoadIntersection(road, otherRoad, road.start, otherRoad.end)[1]);
                    }
                    if(road.end.distance(otherRoad.end) < 1 && road.end != otherRoad.end){
                        landParcel.subroads.set(i, mergeRoadIntersection(road, otherRoad, road.end, otherRoad.end)[0]);
                        landParcel.subroads.set(j, mergeRoadIntersection(road, otherRoad, road.end, otherRoad.end)[1]);
                    }
                }
            }
        }
        for(int i =0; i < landParcel.footprints.size(); i++){
            landParcel.footprints.get(i).geometry = generatePolygon(i);
            //SceneRenderer.render(landParcel.footprints.get(i).geometry);
            SceneRenderer.render(generatePolygon(i));
        }
        for (int i= 0; i < landParcel.subroads.size(); i++){
            for (int j =0; j < landParcel.footprints.size(); j++){
                if(landParcel.footprints.get(j).getRoadsideEdges().containsValue(landParcel.subroads.get(i))){

                }
            }
            //SceneRenderer.renderLine(new Coordinate[]{ landParcel.subroads.get(i).coordinateA,  landParcel.subroads.get(i).coordinateB});
        }
        for(int i = 0; i < landParcel.footprints.size(); i++){
            for (Coordinate[] road : landParcel.footprints.get(i).getRoadsideEdges().keySet()){
                //SceneRenderer.renderLine(road);
            }
        }
        //if(geo != null)
        //    SceneRenderer.render(geo);
        return landParcel;
    }

    Road[] mergeRoadIntersection(Road roadA, Road roadB, Coordinate roadAcoord, Coordinate roadBcoord){
        Coordinate midPoint = getMidPoint(roadAcoord, roadBcoord);
        if(coordinates.containsKey(generateIndex(roadAcoord))) {
            coordinates.put(generateIndex(roadAcoord), midPoint);
            //SceneRenderer.render(midPoint);
        }
        if(coordinates.containsKey(generateIndex(roadBcoord))) {
            coordinates.put(generateIndex(roadBcoord), midPoint);
            //SceneRenderer.render(midPoint);
        }
        if(roadA.start == roadAcoord){
            roadA.start = midPoint;
        } else {
            roadA.end = midPoint;
        }
        if(roadB.start == roadBcoord){
            roadB.start = midPoint;
        } else {
            roadB.end = midPoint;
        }
        return new Road[]{roadA, roadB};
    }

    Coordinate getMidPoint(Coordinate coordinateA, Coordinate coordinateB){
        return new Coordinate((coordinateA.x + coordinateB.x) / 2, (coordinateA.y + coordinateB.y) /2);
    }

    boolean pointOnLine(Coordinate start, Coordinate mid, Coordinate end){
        double lineDistance = start.distance(end);
        double startToMid = start.distance(mid);
        double midToEnd = mid.distance(end);

        double total = lineDistance - (startToMid + midToEnd);

        return (total < 0.00001 && total > -0.00001);
    }

    boolean pointOnParcelEdge(Coordinate coordinate, LandParcel parcel){
        for(int i= 0; i < parcel.polygon.getCoordinates().length -1; i++){
            if(pointOnLine(parcel.polygon.getCoordinates()[i], coordinate, parcel.polygon.getCoordinates()[i+1])){
                return true;
            }
        }
        return false;
    }

    void drawPoints(){
        for(Coordinate coordinate : coordinates.values()){
            SceneRenderer.render(coordinate);

        }
    }

    public Polygon generatePolygon(int id){
        Coordinate[] coordinates = new Coordinate[footprintIndices.get(id).length];
        String[] indices = footprintIndices.get(id);
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = this.coordinates.get(indices[i]);
        }
        Polygon polygon =new GeometryFactory().createPolygon(coordinates);
        polygon.setUserData(id);
        return polygon;
    }

    private Coordinate getCoordinateFromKey(String key){
        Coordinate coordinate = new Coordinate();
        coordinate.x = Double.parseDouble(key.substring(0, key.indexOf('.') + decimalPlaces));
        coordinate.y = Double.parseDouble(key.substring(key.indexOf('.') + decimalPlaces + 1));
        return coordinate;
    }

    private String generateIndex(Coordinate coordinate){
        double x = (double) Math.round(coordinate.x * Math.pow(10 , decimalPlaces))/(Math.pow(10 , decimalPlaces));
        double y =  (double) Math.round(coordinate.y * Math.pow(10 , decimalPlaces))/(Math.pow(10 , decimalPlaces));
        String stringX = String.valueOf (x);
        String stringY = String.valueOf (y);
        while (stringX.length() - stringX.indexOf('.') < 7){
            stringX += '0';
        }
        while (stringY.length() - stringY.indexOf('.') < 7){
            stringY += '0';
        }
        return  stringX + stringY;
    }
}
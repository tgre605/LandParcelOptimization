import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

import java.util.*;

public class ParcelMesh {
    public int decimalPlaces = 1;
    public ArrayList<Coordinate> coordinates = new ArrayList<>();
    public HashMap<Integer, Integer[]> edges = new HashMap<>();
    public HashMap<Integer, Integer[]> footprints = new HashMap<>();
    int footprintCount = 0;
    int edgeCount = 0;

    public ParcelMesh (LandParcel parcel){
        coordinates.clear();
        edges.clear();
        footprints.clear();
        footprintCount = 0;

        Coordinate[] polygonCoords = parcel.polygon.getCoordinates();
        for(int i =0; i < polygonCoords.length-1; i++){
            coordinates.add(polygonCoords[i]);
            addEdge(i, (i+1) %  (polygonCoords.length-1));
            footprints.put(i, new Integer[]{0});
        }
    }

    public void test(){
        splitFootprint(0, midPoint(coordinates.get(edges.get(0)[0]) ,coordinates.get(edges.get(0)[1])), 3, midPoint(coordinates.get(edges.get(3)[0]) ,coordinates.get(edges.get(3)[1])), 0);
    }

    public Coordinate midPoint(Coordinate a, Coordinate b){
        return new Coordinate((a.x + b.x)/2, (a.y + b.y)/2);
    }

    public Geometry getFootprintGeometry(int id){
        ArrayList<Coordinate> geometryCoords = new ArrayList<>();
        ArrayList<Integer> coordIndex = getFootprintCoords(id);
        int currentCoord = coordIndex.get(0);
        int otherCoord = coordIndex.get(1);
        int index = 1;
        int count = 0;
        geometryCoords.add(coordinates.get(currentCoord));
        while (coordIndex.size() != 1){
            if(count > coordIndex.size() * 2){
                break;
            }
            if (containsEdge(currentCoord, otherCoord)){
                coordIndex.remove(coordIndex.indexOf(currentCoord));
                currentCoord = otherCoord;
                geometryCoords.add(coordinates.get(otherCoord));
                count = 0;
            }
            index++;
            count++;
            otherCoord = coordIndex.get(index % coordIndex.size());
        }
        geometryCoords.add(coordinates.get(coordIndex.get(0)));
        geometryCoords.add(geometryCoords.get(0));
        LinearRing linear = new GeometryFactory().createLinearRing(geometryCoords.toArray(new Coordinate[0]));
        return new GeometryFactory().createPolygon(linear);
    }

    private boolean containsEdge(int coordA, int coordB){
        for (Integer[] coords: edges.values()) {
            if(coordA == coords[0] && coordB == coords[1]){
                return true;
            }
            if(coordA == coords[1] && coordB == coords[0]){
                return true;
            }
        }
        return false;
    }

    private boolean contains(Integer[] array, Integer value){
        for(int i =0; i < array.length; i++){
            if(array[i] == value) {
                return true;
            }
        }
        return false;
    }

    public void splitFootprint(Integer edgeToSplitA, Coordinate coordA, Integer edgeToSplitB, Coordinate coordB, int id){
        coordinates.add(coordA);
        coordinates.add(coordB);

        Integer[] footprintsEdgeA = footprints.get(edgeToSplitA);
        Integer[] footprintsEdgeB = footprints.get(edgeToSplitB);
        addEdge(edges.get(edgeToSplitA)[0], coordinates.indexOf(coordA));
        addEdgesToFootprint(edgeToSplitA, edgeCount-1, false);
        addEdge(coordinates.indexOf(coordA), edges.get(edgeToSplitA)[1]);
        addEdgesToFootprint(edgeToSplitA, edgeCount-1, true);

        addEdge(edges.get(edgeToSplitB)[0], coordinates.indexOf(coordB));
        addEdgesToFootprint(edgeToSplitB, edgeCount-1, false);
        addEdge(coordinates.indexOf(coordB), edges.get(edgeToSplitB)[1]);
        addEdgesToFootprint(edgeToSplitB, edgeCount-1, true);

        Integer[] edgeACoords = edges.get(edgeToSplitA);
        removeEdge(edgeToSplitA);
        removeEdge(edgeToSplitB);

        ArrayList<Integer> clockwiseIndexes = getFootprintCoords(id);
        clockwiseIndexes.remove(edgeACoords[0]);

        ArrayList<Integer> anticlockwiseIndexes = getFootprintCoords(id);
        anticlockwiseIndexes.remove(edgeACoords[1]);

        footprints.remove(edgeToSplitA);
        footprints.remove(edgeToSplitB);

        ArrayList<Integer> clockwiseLoop = getEdgeLoop(clockwiseIndexes, coordinates.indexOf(coordA), coordinates.indexOf(coordB));
        SceneRenderer.render(coordA);
        SceneRenderer.render(coordB);
        ArrayList<Integer> anticlockwiseLoop = getEdgeLoop(anticlockwiseIndexes, coordinates.indexOf(coordA), coordinates.indexOf(coordB));
        clockwiseLoop.add(edgeCount);
        anticlockwiseLoop.add(edgeCount);

        addEdge(coordinates.indexOf(coordA), coordinates.indexOf(coordB));
        //footprints.put(footprints.size()-1, new Integer[]{ footprintCount, footprintCount+1});
        addFootprint(clockwiseLoop.toArray(new Integer[0]));
        addFootprint(anticlockwiseLoop.toArray(new Integer[0]));

        removeFootprint(id);
    }

    private ArrayList<Integer> getFootprintCoords(int id){
        ArrayList<Integer> coordIndex = new ArrayList<>();
        for (Integer edge: footprints.keySet()) {
            if(contains(footprints.get(edge), id)){
                if(!coordIndex.contains(edges.get(edge)[0]))
                    coordIndex.add(edges.get(edge)[0]);
                if(!coordIndex.contains(edges.get(edge)[1]))
                    coordIndex.add(edges.get(edge)[1]);
            }
        }
        return coordIndex;
    }

    private ArrayList<Integer> getEdgeLoop(ArrayList<Integer> coords, Integer startCoord, Integer endCoord){
        ArrayList<Integer> geometryCoords = new ArrayList<>();
        int currentCoord = startCoord;
        int otherCoord = coords.get(1);
        int index = 1;
        geometryCoords.add(currentCoord);
        while (currentCoord != endCoord){
            if (containsEdge(currentCoord, otherCoord)){
                coords.remove(coords.indexOf(currentCoord));
                geometryCoords.add(getKeyByValue(edges, new Integer[]{currentCoord, otherCoord}));
                currentCoord = otherCoord;
            }
            index++;
            otherCoord = coords.get(index % coords.size());
        }
        return geometryCoords;
    }

    public int getKeyByValue(HashMap<Integer, Integer[]> map, Integer[] value){
        Integer[] swappedValue = new Integer[]{value[1], value[0]};
        for (Integer entry : map.keySet()) {
            if(Arrays.equals(map.get(entry), value) || Arrays.equals(map.get(entry), swappedValue)){
                return entry;
            }
        }
        return -1;
    }

    public void drawMesh(){
        for(int i=0; i< coordinates.size(); i++){
                //SceneRenderer.render(coordinates.get(i));
        }
        for (Integer key : edges.keySet()) {
            SceneRenderer.renderLine(new Coordinate[]{coordinates.get(edges.get(key)[0]), coordinates.get(edges.get(key)[1])});
        }
        //for (Integer[] coordIndex: edges.values()) {
        //    SceneRenderer.renderLine(new Coordinate[]{coordinates.get(coordIndex[0]), coordinates.get(coordIndex[1])});
        //}
        SceneRenderer.render(getFootprintGeometry(1));
        SceneRenderer.render(getFootprintGeometry(2));
    }

    private void addEdge(int coordinateA, int coordinateB){
        edges.put(edgeCount, new Integer[]{coordinateA, coordinateB});
        edgeCount++;
    }

    private void removeEdge(int index){
        edges.remove(index);
    }

    private void addEdgesToFootprint(int source, int target, boolean removeSource){
        Integer[] footprintList = footprints.get(source);
        List<Integer> sourceFootprintList = Arrays.asList(footprintList);
        footprintList = footprints.get(target);
        if(footprintList == null){
            footprints.put(target,sourceFootprintList.toArray(new Integer[0]));
        } else {
            List<Integer> targetFootprintList = Arrays.asList(footprintList);
            targetFootprintList.add(source);
            footprints.put(target, targetFootprintList.toArray(new Integer[0]));
        }
        if(removeSource)
            footprints.remove(source);
    }

    private void addFootprint(Integer[] edges){
        footprintCount++;
        for(int i = 0; i < edges.length; i++){
            Integer[] footprintList = footprints.get(edges[i]);
            if(footprintList != null) {
                footprintList = Arrays.copyOf(footprintList, footprintList.length + 1);
                footprintList[footprintList.length - 1] = footprintCount;
                footprints.put(edges[i], footprintList);
            }
        }
    }

    private void removeFootprint(int id){
        for (Integer key: footprints.keySet()) {
            if(contains(footprints.get(key), id)){
                List<Integer> footprintList =   new LinkedList<>(Arrays.asList(footprints.get(key)));
                footprintList.remove(id);
                footprints.put(key, footprintList.toArray(new Integer[0]));
            }
        }
    }
}

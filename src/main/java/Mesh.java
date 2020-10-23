import javafx.scene.paint.Color;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class Mesh {
    public static int nextVertexId, nextEdgeId, nextFaceId;
    public static class Vertex {
        int id;
        public Coordinate position;

        public Vertex(Coordinate position) {
            this.position = position;
            id = nextVertexId;
            nextVertexId++;
        }
    }

    public class Edge {
        int id;
        public Vertex vertexA;
        public Vertex vertexB;
        public boolean roadsideEdge;
        public Edge(Vertex vertexA, Vertex vertexB){
            this.vertexA = vertexA;
            this.vertexB = vertexB;
            id = nextEdgeId;
            nextEdgeId++;
        }
        public Edge(Vertex vertexA, Vertex vertexB, boolean roadsideEdge){
            this.vertexA = vertexA;
            this.vertexB = vertexB;
            this.roadsideEdge = roadsideEdge;
            id = nextEdgeId;
            nextEdgeId++;
        }
    }

    public class Face {
        int id;
        public ArrayList<Edge> edges = new ArrayList<>();
        public ArrayList<Vertex> vertices= new ArrayList<>();
        public Face(){
            id = nextFaceId;
            nextFaceId++;
        }
        public Face(ArrayList<Edge> edges, ArrayList<Vertex> vertices){
            this.edges = edges;
            this.vertices = vertices;
            id = nextFaceId;
            nextFaceId++;
        }
    }

    public Face footprint;
    public  static HashMap<Road, Vertex[]> roads = new HashMap<>();
    //public static ArrayList<Road> roads = new ArrayList<>();

    public Mesh(LandParcel landParcel){
        Face face = new Face();
        Coordinate[] coords = landParcel.polygon.getCoordinates();
        for (int i = 0; i < landParcel.polygon.getCoordinates().length-1; i++){
            Vertex vertex = new Vertex(coords[i]);
            face.vertices.add(vertex);
        }
        for (int i =0; i < face.vertices.size()-1; i++){
            roads.put(new Road(face.vertices.get(i).position, face.vertices.get((i+1) % face.vertices.size()).position, Road.RoadType.mainRoad),
                    new Vertex[]{face.vertices.get(i), face.vertices.get((i+1) % face.vertices.size())});
            face.edges.add(new Edge(face.vertices.get(i), face.vertices.get((i+1) % face.vertices.size()), true));
        }
        face.edges.add(new Edge(face.vertices.get(0), face.vertices.get(face.vertices.size()-1),true));
        roads.put(new Road(face.vertices.get(0).position, face.vertices.get(face.vertices.size()-1).position, Road.RoadType.mainRoad),
                new Vertex[]{face.vertices.get(0), face.vertices.get((face.vertices.size()-1) % face.vertices.size())});
        footprint = face;
    }

    public Face[] splitEdge(Coordinate coordinateA, Coordinate coordinateB, Face face, double roadLength){
        LineString line = new GeometryFactory().createLineString(new Coordinate[]{coordinateA, coordinateB});
        Coordinate[] coordinates = null;
        try {
            coordinates = faceToPolygon(face).intersection(line).getCoordinates();
        } catch (TopologyException e){
            coordinates = validate(faceToPolygon(face)).intersection(line).getCoordinates();
        }
        Vertex vertex1 = new Vertex(coordinates[0]);
        Vertex vertex2 = new Vertex(coordinates[1]);
        Edge splittingEdgeA = getEdgeOnVertex(vertex1, face.edges);
        Edge splittingEdgeB = getEdgeOnVertex(vertex2, face.edges);
        face = insertVertex(splittingEdgeA, vertex1, face);
        face = insertVertex(splittingEdgeB, vertex2, face);

        ArrayList<Vertex> clockwiseLoop = new ArrayList<>(face.vertices);
        clockwiseLoop.remove(splittingEdgeA.vertexA);
        clockwiseLoop = getEdgeLoop(vertex1, vertex2, clockwiseLoop, face.edges);

        ArrayList<Vertex> antiClockwiseLoop = new ArrayList<>(face.vertices);
        antiClockwiseLoop.remove(splittingEdgeA.vertexB);
        antiClockwiseLoop = getEdgeLoop(vertex1, vertex2, antiClockwiseLoop, face.edges);

        Face face1 = new Face(getEdgesFromVertices(clockwiseLoop, face.edges), clockwiseLoop);
        Face face2 = new Face(getEdgesFromVertices(antiClockwiseLoop, face.edges), antiClockwiseLoop);

        Edge newEdge = new Edge(vertex1, vertex2);

        if(vertex1.position.distance(vertex2.position) > roadLength){
            roads.put(new Road(vertex1.position, vertex2.position, Road.RoadType.subRoad),
                    new Vertex[] {vertex1, vertex2});
            newEdge.roadsideEdge = true;
        }

        face1.edges.add(newEdge);
        face2.edges.add(newEdge);

        return new Face[]{face1, face2};
    }

    private Face validateFace(Face face){
        for(int i = 0 ; i < face.vertices.size(); i++){
            for(int j = 0 ; j < face.vertices.size(); j++){
                if(face.vertices.get(i) != face.vertices.get(j)) {
                    if (face.vertices.get(i).position.distance(face.vertices.get(j).position) < 0.0001) {
                        getSharedVertex(face.vertices.get(i), face.vertices.get(j), face.edges).position = face.vertices.get(i).position;
                        SceneRenderer.render(face.vertices.get(i).position);
                    }
                }
            }
        }
        return face;
    }

    private Vertex getSharedVertex(Vertex vertex, Vertex otherVertex, ArrayList<Edge> edges){
        ArrayList<Vertex> vertexANeighbours = new ArrayList<>();
        ArrayList<Vertex> vertexBNeighbours = new ArrayList<>();
        for(int i = 0; i < edges.size(); i++){
            if(edges.get(i).vertexA == vertex ){
                vertexANeighbours.add(edges.get(i).vertexB);
            }
            if(edges.get(i).vertexB == vertex ){
                vertexANeighbours.add(edges.get(i).vertexA);
            }
            if(edges.get(i).vertexA == otherVertex ){
                vertexBNeighbours.add(edges.get(i).vertexB);
            }
            if(edges.get(i).vertexB == otherVertex ){
                vertexBNeighbours.add(edges.get(i).vertexA);
            }
        }
        vertexANeighbours.retainAll(vertexBNeighbours);
        return vertexANeighbours.get(0);
    }

    private ArrayList<Edge> getEdgesWithVertex(Vertex vertex, ArrayList<Edge> edges){
        ArrayList<Edge> vertEdges = new ArrayList<>();
        for(Edge edge : edges){
            if(edge.vertexB == vertex || edge.vertexA == vertex){
                vertEdges.add(edge);
            }
        }
        return vertEdges;
    }

    private ArrayList<Edge> getEdgesFromVertices(ArrayList<Vertex> vertices, ArrayList<Edge> footprintEdges){
        ArrayList<Edge> edges = new ArrayList<>();
        for(int i =0;i < vertices.size()-1; i++){
            edges.add(getEdgeFromVertices(vertices.get(i), vertices.get((i+1) % vertices.size()), footprintEdges));
        }
        return edges;
    }


    private ArrayList<Vertex> getEdgeLoop(Vertex start, Vertex end, ArrayList<Vertex> vertices, ArrayList<Edge> edges){
        ArrayList<Vertex> verticesVisited = new ArrayList<>();
        Vertex currentVertex = start;
        int index = 0;
        while(currentVertex != end) {
            Vertex otherVertex = vertices.get(index);
            if(!verticesVisited.contains(vertices.get(index))) {
                if (containsEdge(currentVertex, otherVertex, edges)) {
                    verticesVisited.add(currentVertex);
                    currentVertex = otherVertex;
                }
            }
            index = (index + 1) % vertices.size();
        }
        verticesVisited.add(end);
        return verticesVisited;
    }

    private boolean containsEdge(Vertex vertexA, Vertex vertexB, ArrayList<Edge> edges){
        for (Edge edge: edges) {
            if((edge.vertexA == vertexA && edge.vertexB == vertexB) || (edge.vertexB == vertexA && edge.vertexA == vertexB) ){
                return true;
            }
        }
        return false;
    }

    public static Edge getEdge(Vertex vertexA, Vertex vertexB, ArrayList<Edge> edges){
        for (Edge edge: edges) {
            if((edge.vertexA == vertexA && edge.vertexB == vertexB) || (edge.vertexB == vertexA && edge.vertexA == vertexB) ){
                return edge;
            }
        }
        return null;
    }

    private Edge getEdgeOnVertex(Vertex vertex, ArrayList<Edge> edges){
        for (Edge edge: edges) {
            if(vertexOnEdge(vertex, edge)){
                return edge;
            }
        }
        return null;
    }

    private boolean vertexOnEdge(Vertex vertex, Edge edge){
        double edgeAToVertex = edge.vertexA.position.distance(vertex.position);
        double edgeBToVertex = edge.vertexB.position.distance(vertex.position);
        double totalDistance = edgeAToVertex + edgeBToVertex;
        double distance = edge.vertexA.position.distance(edge.vertexB.position);
        return (totalDistance - distance) < 0.01;
    }

    private Face insertVertex(Edge edge, Vertex vertex, Face face){
        face.edges.remove(edge);
        Edge edge1 = new Edge(edge.vertexA, vertex);
        Edge edge2 = new Edge(edge.vertexB, vertex);
        edge1.roadsideEdge = edge.roadsideEdge;
        edge2.roadsideEdge = edge.roadsideEdge;
        face.edges.add(edge1);
        face.edges.add(edge2);
        face.vertices.add(vertex);
        return face;
    }

    public static Polygon faceToPolygon(Face face){
        ArrayList<Vertex> vertices = new ArrayList<>();
        ArrayList<Edge> edgesToWalk = new ArrayList<>(getValidEdges(face.edges));
        Vertex currentVertex = face.vertices.get(0);
        while (edgesToWalk.size() > 0){
            vertices.add(currentVertex);
            Edge edge = getEdgeFromVertex(currentVertex, edgesToWalk);
            if(currentVertex == edge.vertexA) {
                currentVertex = edge.vertexB;
            }
            else {
                currentVertex = edge.vertexA;
            }
            edgesToWalk.remove(edge);
        }
        return new GeometryFactory().createPolygon(vertexToCoords(vertices));
    }

    private static ArrayList<Edge> getValidEdges(ArrayList<Edge> edges){
        for(int i =0; i < edges.size(); i++){
            for(int j =0; j < edges.size(); j++){
                if(edges.get(i).vertexA == edges.get(j).vertexB && edges.get(i).vertexB == edges.get(j).vertexA){
                    edges.remove(i);
                }
            }
        }
        return edges;
    }

    private static Coordinate[] vertexToCoords(ArrayList<Vertex> vertices){
        Coordinate[] coordinates = new Coordinate[vertices.size()+1];
        for(int i =0; i < vertices.size(); i++){
            coordinates[i] = vertices.get(i).position;
        }
        coordinates[vertices.size()] = coordinates[0];
        return coordinates;
    }

    private static Edge getEdgeFromVertex(Vertex vertex, ArrayList<Edge> edges){
        for (Edge edge: edges) {
            if(edge.vertexA.position == vertex.position){
                return edge;
            }
            if(edge.vertexB.position == vertex.position){
                return edge;
            }
        }
        return null;
    }

    private Edge getEdgeFromVertices(Vertex vertex1, Vertex vertex2, ArrayList<Edge> edges){
        for (Edge edge: edges) {
            if(edge.vertexA == vertex1 && edge.vertexB == vertex2){
                return edge;
            }
            else if(edge.vertexB == vertex1 && edge.vertexA == vertex2){
                return edge;
            }
        }
        return null;
    }

    public static boolean faceIsTriangle(Face face){
        ArrayList<Vertex> vertices = face.vertices;
        double innerAngle = 0;
        for(int i =0; i < vertices.size(); i++){
            double angle = getAngleOfCorner(getValue(vertices, i-1), getValue(vertices, i), getValue(vertices, i+1));
            if(angle < (Math.PI - 0.01)){
                innerAngle += angle;
            } else {
                SceneRenderer.render(vertices.get(i).position);
            }
        }
        return innerAngle == Math.PI;
    }

    private static Vertex getValue(ArrayList<Vertex> vertices, int value){
        if(value < 0){
            return vertices.get(vertices.size() + value);
        }
        return vertices.get(value % vertices.size());
    }

    private static double getAngleOfCorner(Vertex left, Vertex joint, Vertex right){
        return Angle.angleBetween(left.position, joint.position, right.position);
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

    static void addPolygon(Polygon polygon, Polygonizer polygonizer){
        addLineString(polygon.getExteriorRing(), polygonizer);
        for(int n = polygon.getNumInteriorRing(); n-- > 0;){
            addLineString(polygon.getInteriorRingN(n), polygonizer);
        }
    }

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
}
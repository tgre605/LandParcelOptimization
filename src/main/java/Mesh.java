import javafx.scene.paint.Color;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;

public class Mesh {
    public static int nextVertexId, nextEdgeId, nextFaceId;
    public static class Vertex {
        int id;
        public Coordinate position;
        public Vertex(Coordinate position){
            this.position = position;
            id = nextVertexId;
            nextVertexId++;
        }
    }

    public class Edge {
        int id;
        public Vertex vertexA;
        public Vertex vertexB;
        public Edge(Vertex vertexA, Vertex vertexB){
            this.vertexA = vertexA;
            this.vertexB = vertexB;
            id = nextEdgeId;
            nextEdgeId++;
        }
    }

    public class Face {
        int id;
        private ArrayList<Edge> edges = new ArrayList<>();
        private ArrayList<Vertex> vertices= new ArrayList<>();
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

    public Mesh(LandParcel landParcel){
        Face face = new Face();
        Coordinate[] coords = landParcel.polygon.getCoordinates();
        for (int i = 0; i < landParcel.polygon.getCoordinates().length-1; i++){
            Vertex vertex = new Vertex(coords[i]);
            face.vertices.add(vertex);
        }
        for (int i =0; i < face.vertices.size()-1; i++){
            face.edges.add(new Edge(face.vertices.get(i), face.vertices.get((i+1) % face.vertices.size())));
        }
        face.edges.add(new Edge(face.vertices.get(0), face.vertices.get(face.vertices.size()-1)));
        footprint = face;
    }

    public Coordinate midPoint(Coordinate a, Coordinate b){
        return new Coordinate((a.x + b.x)/2, (a.y + b.y)/2);
    }


    public Face[] splitEdge(Coordinate coordinateA, Coordinate coordinateB, Face face){
        LineString line = new GeometryFactory().createLineString(new Coordinate[]{coordinateA, coordinateB});
        Coordinate[] coordinates = faceToPolygon(face).intersection(line).getCoordinates();
        Vertex vertex1 = new Vertex(coordinates[0]);
        Vertex vertex2 = new Vertex(coordinates[1]);
        SceneRenderer.render(coordinates[0]);
        SceneRenderer.render(coordinates[1]);
        Edge splittingEdgeA = getEdgeOnVertex(vertex1, face.edges);
        Edge splittingEdgeB = getEdgeOnVertex(vertex2, face.edges);
        face = insertVertex(splittingEdgeA, vertex1, face);
        face = insertVertex(splittingEdgeB, vertex2, face);

        ArrayList<Vertex> clockwiseLoop = new ArrayList<>(face.vertices);
        System.out.println(clockwiseLoop.remove(splittingEdgeA.vertexA));
        clockwiseLoop = getEdgeLoop(vertex1, vertex2, clockwiseLoop, face.edges);

        ArrayList<Vertex> antiClockwiseLoop = new ArrayList<>(face.vertices);
        System.out.println(antiClockwiseLoop.remove(splittingEdgeA.vertexB));
        antiClockwiseLoop = getEdgeLoop(vertex1, vertex2, antiClockwiseLoop, face.edges);

        Face face1 = new Face(getEdgesFromVertices(clockwiseLoop, face.edges), clockwiseLoop);
        Face face2 = new Face(getEdgesFromVertices(antiClockwiseLoop, face.edges), antiClockwiseLoop);

        Edge newEdge = new Edge(vertex1, vertex2);

        face1.edges.add(newEdge);
        face2.edges.add(newEdge);

        SceneRenderer.renderLine(new Coordinate[]{coordinateA, coordinateB});
        SceneRenderer.render(faceToPolygon(face1), Color.GRAY);
        SceneRenderer.render(faceToPolygon(face2), Color.GRAY);

        return new Face[]{face1, face2};
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
        face.edges.add(edge1);
        face.edges.add(edge2);
        face.vertices.add(vertex);
        return face;
    }

    public Polygon faceToPolygon(Face face){
        ArrayList<Vertex> vertices = new ArrayList<>();
        ArrayList<Edge> edgesToWalk = new ArrayList<>(face.edges);
        Vertex currentVertex = face.vertices.get(0);
        while (edgesToWalk.size() > 0){
            vertices.add(currentVertex);
            Edge edge = getEdgeFromVertex(currentVertex, edgesToWalk);
            if(currentVertex == edge.vertexA)
                currentVertex = edge.vertexB;
            else
                currentVertex = edge.vertexA;
            edgesToWalk.remove(edge);
        }
        return new GeometryFactory().createPolygon(vertexToCoords(vertices));
    }

    private Coordinate[] vertexToCoords(ArrayList<Vertex> vertices){
        Coordinate[] coordinates = new Coordinate[vertices.size()+1];
        for(int i =0; i < vertices.size(); i++){
            coordinates[i] = vertices.get(i).position;
        }
        coordinates[vertices.size()] = coordinates[0];
        return coordinates;
    }

    private Edge getEdgeFromVertex(Vertex vertex, ArrayList<Edge> edges){
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
        SceneRenderer.render(vertex1.position);
        SceneRenderer.render(vertex2.position);
        return null;
    }

    public boolean faceIsTriangle(Face face){
        ArrayList<Vertex> vertices = face.vertices;
        double innerAngle = 0;
        for(int i =0; i < vertices.size(); i++){
            double angle = getAngleOfCorner(vertices.get(i-1), vertices.get(i), vertices.get(i+1));
            if(angle != Math.PI){
                innerAngle += angle;
            }
        }
        return innerAngle == Math.PI/2;
    }

    private double getAngleOfCorner(Vertex left, Vertex joint, Vertex right){
        return Angle.angleBetweenOriented(left.position, joint.position, right.position);
    }
}
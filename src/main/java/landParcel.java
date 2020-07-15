import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

public class landParcel {
    public enum type{residential, commercial, industry, undefined}
    public Polygon polygon = new Polygon(null, null, new GeometryFactory());
    public ArrayList<building> buildingFootprints = new ArrayList<>();
    public ArrayList<landParcel> neighbours = new ArrayList<>();
    public int[] gridLocaiton;
    private ArrayList<Coordinate> vertices = new ArrayList<>();
    private type landType;
    private static int nextId = 0;
    private int id;

   public landParcel(ArrayList<Coordinate> vertices, type landType) {
        polygon = new GeometryFactory().createPolygon(vertices.toArray(new Coordinate[0]));
        this.landType = landType;
        this.vertices = vertices;
        this.id = landParcel.nextId;
        landParcel.nextId++;
    }

    public landParcel(Geometry polygon){
       this.polygon = new GeometryFactory().createPolygon(polygon.getCoordinates());
    }

    public Coordinate[] getPoints(){
        Coordinate[] points = polygon.getCoordinates();
        for(int i = 0; i < points.length; i++){
            System.out.println(points[i]);
        }
        return points;
    }

    public void setGridLocaiton(int[] gridLocaiton) {
        this.gridLocaiton = gridLocaiton;
    }


    public void surroundingParcels(JsonReader reader){
        ArrayList<landParcel>[][] world = reader.getWorld();
        int[] currentParcelCoord = gridLocaiton;
        for(int i = currentParcelCoord[0]-1; i<currentParcelCoord[0]+2; i++){
            for(int j = currentParcelCoord[1]-1; j<currentParcelCoord[1]+2; j++){
                if((currentParcelCoord[0]-1 < 0) || (currentParcelCoord[0] == reader.getGridWidth()-1)) {
                    continue;
                }
                if((currentParcelCoord[1]-1 < 0) || (currentParcelCoord[1] == reader.getGridHeight()-1)){
                    continue;
                }
                for (landParcel compareParcel: world[i][j]
                ) {
                    if(this.polygon.touches(compareParcel.polygon)){
                        this.neighbours.add(compareParcel);
                    };
                }
            }
        }

    }

}

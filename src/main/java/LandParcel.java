import org.locationtech.jts.geom.*;

import java.util.ArrayList;

public class LandParcel {




    public enum type{residential, commercial, industry, undefined}
    public Polygon polygon = new Polygon(null, null, new GeometryFactory());
    public ArrayList<Footprint> footprints = new ArrayList<>();
    public ArrayList<LandParcel> neighbours = new ArrayList<>();
    public ArrayList<Road> subroads = new ArrayList<>();
    public int[] gridLocaiton;
    public int id;

    private ArrayList<Coordinate> vertices = new ArrayList<>();
    private type landType;
    private double population;
    private double populationDensity;
    private static int nextId = 0;


   public LandParcel(ArrayList<Coordinate> vertices, type landType, double population, double populationDensity) {
        polygon = new GeometryFactory().createPolygon(vertices.toArray(new Coordinate[0]));
        this.landType = landType;
        this.vertices = vertices;
        this.id = LandParcel.nextId;
        this.population = population;
        this.populationDensity = populationDensity;
        LandParcel.nextId++;
    }

    public LandParcel(Geometry polygon){
       this.polygon = new GeometryFactory().createPolygon(polygon.getCoordinates());
    }

    public Geometry[] getFootprintGeometries(){
       Geometry[] geometries = new Geometry[footprints.size()];
       for(int i= 0 ; i < footprints.size(); i++){
           geometries[i] = footprints.get(i).geometry;
       }
       return geometries;
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
        ArrayList<LandParcel>[][] world = reader.getWorld();
        int[] currentParcelCoord = gridLocaiton;
        for(int i = currentParcelCoord[0]-1; i<currentParcelCoord[0]+2; i++){
            for(int j = currentParcelCoord[1]-1; j<currentParcelCoord[1]+2; j++){
                if((currentParcelCoord[0]-1 < 0) || (currentParcelCoord[0] == reader.getGridWidth()-1)) {
                    continue;
                }
                if((currentParcelCoord[1]-1 < 0) || (currentParcelCoord[1] == reader.getGridHeight()-1)){
                    continue;
                }
                for (LandParcel compareParcel: world[i][j]
                ) {
                    if(this.polygon.touches(compareParcel.polygon)){
                        this.neighbours.add(compareParcel);
                    };
                }
            }
        }
    }

    public double getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public double getPopulationDensity() {
        return populationDensity;
    }

    public void setPopulationDensity(int populationDensity) {
        this.populationDensity = populationDensity;
    }

}

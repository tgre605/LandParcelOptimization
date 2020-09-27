/*
    Class for reading json output of road network

    Should probably use org.json for reading
 */
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class JsonReader {
    int gridWidth = 20;
    int gridHeight = 20;
    ArrayList<Coordinate> temp = new ArrayList<Coordinate>();
    ArrayList<LandParcel> parcels = new ArrayList<>();
    ArrayList<LandParcel>[][] world = new ArrayList[gridWidth][gridHeight];
    ArrayList<Polygon> RBuildingFootprints = new ArrayList<>();
    ArrayList<Geometry> CBuildingFootprints = new ArrayList<>();
    ArrayList<Geometry> IBuildingFootprints = new ArrayList<>();


    public JsonReader(String file) throws IOException {
        for(int i = 0; i < gridWidth; i++){
            for(int j = 0; j < gridHeight; j++){
                world[i][j] = new ArrayList<LandParcel>();
            }
        }
        JSONParser jsonParser = new JSONParser();
        Path currentDir = Paths.get(".");
        BufferedImage img = ImageIO.read(new File(currentDir.toAbsolutePath() + "/input/water_map.png"));
        int width = img.getWidth();
        int height = img.getHeight();
        try (FileReader reader = new FileReader(file)){
            Object obj = jsonParser.parse(reader);

            JSONObject parcels = (JSONObject) obj;
            JSONArray land_usages = (JSONArray) parcels.get("land_usages");

            land_usages.forEach(land -> parsePolygons((JSONObject) land, width, height));

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parsePolygons (JSONObject landUsage, int width, int height){
        JSONArray polygon = (JSONArray) landUsage.get("polygon");
        String landTypeJSON = landUsage.get("land_usage").toString();
        Object populationO = landUsage.get("population");
        Long population = (Long) populationO;
        double populationDensity = (double) landUsage.get("population_density");
        polygon.forEach(line -> parseVertices((JSONObject) line));
        // JTS requires list coordinates to have start and end coordinates to be the same
        temp.add(temp.get(0));
        LandParcel.type landType = LandParcel.type.undefined;
        if(landTypeJSON.compareTo("residential") == 0){
            landType = LandParcel.type.residential;
        } else if(landTypeJSON.compareTo("commercial") == 0){
            landType = LandParcel.type.commercial;
        } else if(landTypeJSON.compareTo("industry") == 0){
            landType = LandParcel.type.industry;
        }
        LandParcel newPolygon = new LandParcel(temp, landType, population, populationDensity);
        Point centrePoint = newPolygon.polygon.getCentroid();
        int[] xyPoints = findPointInGrid(centrePoint, width, height);
        newPolygon.setGridLocaiton(xyPoints);
        world[xyPoints[0]][xyPoints[1]].add(newPolygon);
        parcels.add(newPolygon);
        temp.clear();
    }

    private void parseVertices (JSONObject polygon){
        double xPoint = ((double) polygon.get("x"));
        double zPoint = ((double) polygon.get("z"));
        Coordinate tempVector = new Coordinate(xPoint, zPoint);
        this.temp.add(tempVector);
    }

    public ArrayList<LandParcel> getParcels() {
        return parcels;
    }

    public ArrayList<LandParcel>[][] getWorld(){
        return world;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int[] findPointInGrid(Point centrePoint, int width, int height){
        double xDivisor = (double)width/gridWidth;
        double yDivisor = (double)height/gridHeight;
        double centrePointX = centrePoint.getX();
        double centrePointY = centrePoint.getY();
        int gridXD = (int)(centrePointX/xDivisor);
        int gridYD = (int)(centrePointY/yDivisor);
        int[] xyPoints = new int[2];
        xyPoints[0] = gridXD;
        xyPoints[1] = gridYD;
        return xyPoints;
    }
    
    public void getBuildingFootprints(String file){
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(file)){
            Object obj = jsonParser.parse(reader);

            JSONObject Obj = (JSONObject) obj;
            JSONArray residentialFootprints = (JSONArray) Obj.get("residential");
            residentialFootprints.forEach(print -> parseBuildings((JSONObject) print, 0));
            
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void parseBuildings (JSONObject buildingFootprint, int type){
        JSONArray polygon = (JSONArray) buildingFootprint.get("polygon");
        polygon.forEach(line -> parseVertices((JSONObject) line));
        // JTS requires list coordinates to have start and end coordinates to be the same
        this.temp.add(temp.get(0));
        Polygon buildingPolygon = new GeometryFactory().createPolygon(temp.toArray(new Coordinate[0]));
        temp.clear();
        switch (type){
            case 0: RBuildingFootprints.add(buildingPolygon);
                    break;
            case 1: CBuildingFootprints.add(buildingPolygon);
                    break;
            case 2: IBuildingFootprints.add(buildingPolygon);
                    break;
        }
    }
}

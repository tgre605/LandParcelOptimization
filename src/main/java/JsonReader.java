/*
    Class for reading json output of road network

    Should probably use org.json for reading
 */
import org.json.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class JsonReader {

    public JsonReader(String file){

        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(file)){
            Object obj = jsonParser.parse(reader);

            JSONObject parcels = (JSONObject) obj;
            JSONArray land_usages = (JSONArray) parcels.get("land_usages");

            land_usages.forEach(land -> parsePolygons((JSONObject) land));
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parsePolygons (JSONObject landUsage){
        JSONArray polygon = (JSONArray) landUsage.get("polygon");
        polygon.forEach(line -> parseVertices((JSONObject) line));
    }

    private static void parseVertices (JSONObject polygon){
        double xPoint = (double) polygon.get("x");
        System.out.println("x: "+xPoint);
        double zPoint = (double) polygon.get("z");
        System.out.println("z: "+zPoint);
    }
}

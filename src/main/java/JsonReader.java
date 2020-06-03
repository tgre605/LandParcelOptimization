/*
    Class for reading json output of road network

    Should probably use org.json for reading
 */
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.locationtech.jts.math.Vector2D;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class JsonReader {

    ArrayList<Vector2D> temp = new ArrayList<Vector2D>();
    ArrayList<landParcel> parcels = new ArrayList<>();

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

    private void parsePolygons (JSONObject landUsage){
        JSONArray polygon = (JSONArray) landUsage.get("polygon");
        polygon.forEach(line -> parseVertices((JSONObject) line));
        ArrayList<Vector2D> tempArray = new ArrayList<>();
        tempArray.addAll(temp);
        landParcel newPolygon = new landParcel(tempArray);
        parcels.add(newPolygon);
        temp.clear();
    }

    private void parseVertices (JSONObject polygon){
        double xPoint = (double) polygon.get("x");
        double zPoint = (double) polygon.get("z");
        Vector2D tempVector = new Vector2D(xPoint, zPoint);
        this.temp.add(tempVector);
    }

    public ArrayList<landParcel> getParcels() {
        return parcels;
    }
}

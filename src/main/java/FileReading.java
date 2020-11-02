import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReading {

    public static File getRoadNetwork(String inputFile){
        Path currentDir = Paths.get(".");
        String parent = currentDir.toAbsolutePath().toString().substring(0, currentDir.toAbsolutePath().toString().lastIndexOf("\\"));
        parent = parent.substring(0, parent.lastIndexOf("\\"));
        String file = parent + "/input/" + inputFile;
        if(Files.exists(Paths.get(file))) {
            return new File(file);
        } else {
            return new File(currentDir.toAbsolutePath() + "/input/" + inputFile);
        }
    }

    public static File getWaterMap(){
        Path currentDir = Paths.get(".");
        String parent = currentDir.toAbsolutePath().toString().substring(0, currentDir.toAbsolutePath().toString().lastIndexOf("\\"));
        parent = parent.substring(0, parent.lastIndexOf("\\"));
        String file = parent + "/input/water_map.png";
        if(Files.exists(Paths.get(file))) {
            return new File(file);
        } else {
            return new File(currentDir.toAbsolutePath() + "/input/water_map.png");
        }
    }

    public static File getBuildingFile(){
        Path currentDir = Paths.get(".");
        String parent = currentDir.toAbsolutePath().toString().substring(0, currentDir.toAbsolutePath().toString().lastIndexOf("\\"));
        parent = parent.substring(0, parent.lastIndexOf("\\"));
        String file = parent + "/input/buildingFootprints.json";
        if(Files.exists(Paths.get(file))) {
            return new File(file);
        } else {
            return new File(currentDir.toAbsolutePath() + "/input/buildingFootprints.json");
        }
    }
}

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.AffineTransformation;

public class BuildingPlacer {
    public void placeBuildings(LandParcel landParcel){
        for (Footprint footprint: landParcel.footprints) {
            footprint.addBuilding(new Building(footprint));
        }
    }
}

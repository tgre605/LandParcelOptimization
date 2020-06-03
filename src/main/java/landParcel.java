import org.locationtech.jts.math.Vector2D;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class landParcel {

    public ArrayList<Vector2D> polygon = new ArrayList<Vector2D>();

    public landParcel(ArrayList<Vector2D> vertices) {
        this.polygon = vertices;
    }

    public landParcel(){

    }

}

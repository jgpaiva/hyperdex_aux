
import java.util.Map;

public class Query { 
    public final String type;
    public final Map<String, Object> params;

    public Query(String type, Map<String, Object> params) {
        this.type = type;
        this.params = params;
    }

    @Override
    public String toString() {
        String output =  this.type;
        for (Map.Entry<String, Object> entry : this.params.entrySet()) {
            output += " " + entry.getKey() + ":" + entry.getValue();
        }
        return output;
    }
}

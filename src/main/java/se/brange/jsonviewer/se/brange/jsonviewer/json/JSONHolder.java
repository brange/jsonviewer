package se.brange.jsonviewer.se.brange.jsonviewer.json;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONHolder {
    private String key;
    private Object json;
    private Integer index;

    public JSONHolder(Integer index, Object json) {
        this.index = index;
        this.json = json;
    }

    public JSONHolder(String key, Object json) {
        this.key = key;
        this.json = json;
    }

    public boolean isJSONObject() {
        return json instanceof JSONObject;
    }

    public boolean isJSONArray() {
        return json instanceof JSONArray;
    }

    public JSONObject getJSONObject() {
        return (JSONObject) json;
    }

    public JSONArray getJSONArray() {
        return (JSONArray) json;
    }

    @Override
    public String toString() {
        if (index != null) {
            return Integer.toString(index);
        }
        return key;
    }

    public Object getRawObject() {
        return json;
    }
}

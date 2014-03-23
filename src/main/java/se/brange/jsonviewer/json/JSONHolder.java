package se.brange.jsonviewer.json;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONHolder {
    private String key;
    private Object json;
    private Integer index;
    private Object parent;

    public JSONHolder(Integer index, Object json, Object parent) {
        this.index = index;
        this.json = json;
        this.parent = parent;
    }

    public JSONHolder(String key, Object json, Object parent) {
        this.key = key;
        this.json = json;
        this.parent = parent;
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


    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        if (index != null) {
            return Integer.toString(index);
        }
        return key;
    }

    public String getKeyOrIndex() {
        return toString();
    }

    public Object getParent() {
        return parent;
    }

    public Object getRawObject() {
        return json;
    }
}

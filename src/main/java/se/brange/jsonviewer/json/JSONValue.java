package se.brange.jsonviewer.json;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONValue {
    private String key;
    private Object value;
    private Object parent;
    private Integer index;

    public JSONValue(Integer index, Object value, Object parent) {
        this.index = index;
        this.value = value;
        this.parent = parent;
    }

    public JSONValue(String key, Object value, Object parent) {
        this.key = key;
        this.value = value;
        this.parent = parent;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        Object _value;
        if (value.equals("true") || value.equals("false")) {
            _value = Boolean.parseBoolean(value.toString());
        } else if(StringUtils.isNumeric(value.toString())) {
            _value = Integer.parseInt(value.toString());
        } else {
            _value = value.toString();
        }

        this.value = _value;
        if (parent instanceof JSONObject) {
            ((JSONObject) parent).put(key, _value);
        } else if (parent instanceof JSONArray) {
            ((JSONArray)parent).put(index, _value);
        } else {
            System.err.println("Unkown class " + parent.getClass());
        }
    }

    public Object getParent() {
        return parent;
    }

    public Integer getIndex() {
        return index;
    }

    public String getKeyOrIndex() {
        return toString();
    }

    public JSONHelper.JSONValueType getType() {
        return JSONHelper.getType(value);
    }

    @Override
    public String toString() {
        if (parent != null && parent instanceof JSONArray) {
            return Integer.toString(index);
        }
        return key;
    }

    public String asString() {
        return "Key: " + toString() + ", Value: " + getValue();
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}

package json.json;

import org.json.JSONArray;

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
        System.out.println("value.getClass() = " + value.getClass());
        this.value = value;
    }

    public Object getParent() {
        return parent;
    }

    public Integer getIndex() {
        return index;
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
}

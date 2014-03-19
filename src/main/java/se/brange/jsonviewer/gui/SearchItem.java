package se.brange.jsonviewer.gui;

import se.brange.jsonviewer.json.JSONHolder;
import se.brange.jsonviewer.json.JSONValue;

public class SearchItem {
    private Object parent;
    private JSONHolder jsonHolder;
    private JSONValue jsonValue;

    public SearchItem(Object parent, JSONHolder jsonHolder) {
        this.parent = parent;
        this.jsonHolder = jsonHolder;
    }

    public SearchItem(Object parent, JSONValue jsonValue) {
        this.parent = parent;
        this.jsonValue = jsonValue;
    }

    public Object getParent() {
        return parent;
    }

    public Object getNode() {
        return jsonValue != null ? jsonValue : jsonHolder;
    }

    public JSONValue getJsonValue() {
        return jsonValue;
    }

    public void setJsonValue(JSONValue jsonValue) {
        this.jsonValue = jsonValue;
    }

    public boolean matches(String key) {
        if (jsonHolder != null) {
            return jsonHolder.toString().contains(key);
        } else {
            return jsonValue.toString().contains(key) || jsonValue.getValue().toString().contains(key);
        }
    }

    @Override
    public String toString() {
        return "parent: " + parent + ", jsonHolder: " + jsonHolder + ", jsonValue: " + (jsonValue != null ? jsonValue.asString() : null);
    }
}

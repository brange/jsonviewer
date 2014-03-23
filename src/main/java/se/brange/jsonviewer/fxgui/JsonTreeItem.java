package se.brange.jsonviewer.fxgui;

import se.brange.jsonviewer.json.JSONHolder;
import se.brange.jsonviewer.json.JSONValue;

public class JsonTreeItem {

    private JSONHolder jsonHolder;
    private JSONValue jsonValue;
    private JSONHolder parent;

    public JsonTreeItem(JSONHolder parent, Object jsonHolderOrJsonValue) {
        this.parent = parent;
        if (jsonHolderOrJsonValue instanceof JSONHolder) {
            this.jsonHolder = (JSONHolder) jsonHolderOrJsonValue;
        } else if (jsonHolderOrJsonValue instanceof JSONValue) {
            this.jsonValue = (JSONValue) jsonHolderOrJsonValue;
        } else {
            throw new RuntimeException(jsonHolderOrJsonValue.getClass() + " is not allowed.");
        }
    }

    public JSONHolder getJsonHolder() {
        return jsonHolder;
    }

    public JSONValue getJsonValue() {
        return jsonValue;
    }

    public boolean isHolder() {
        return this.jsonHolder != null;
    }

    public JSONHolder getParent() {
        return parent;
    }
}

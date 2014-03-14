package se.brange.jsonviewer.se.brange.jsonviewer.json;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONHelper {

    public enum JSONValueType {
        STRING,
        NUMBER,
        INTEGER,
        OBJECT,
        BOOLEAN,
        ARRAY,
        UNKOWN
    }

    public static JSONValueType getType(Object object) {
        if (object instanceof JSONValue) {
            object = ((JSONValue)object).getValue();
        }
        if (object instanceof String) {
            return JSONValueType.STRING;
        }
        if (object instanceof Boolean) {
            return JSONValueType.BOOLEAN;
        }
        if (object instanceof Float || object instanceof Double) {
            return JSONValueType.NUMBER;
        }
        if (object instanceof Integer) {
            return JSONValueType.INTEGER;
        }
        if (object instanceof JSONHolder || object instanceof JSONObject || object instanceof JSONArray) {
            object = convertFromJSONHolder(object);
            if (object instanceof JSONObject) {
                return JSONValueType.OBJECT;
            }
            if (object instanceof JSONArray) {
                return JSONValueType.ARRAY;
            }
        }

        return JSONValueType.UNKOWN;
    }

    public static String getStringValue(Object object) {
        switch (getType(object)) {
            case STRING: return "String";
            case NUMBER: return "Number";
            case INTEGER: return "Integer";
            case BOOLEAN: return "Boolean";
            case OBJECT: return "Object (" + (getSize(object)) + ")";
            case ARRAY: return "Array (" + (getSize(object)) + ")";
            default: return "Unkown (" + object.getClass() + ")";
        }
    }

    private static int getSize(Object object) {
        object = convertFromJSONHolder(object);
        if (object instanceof JSONObject) {
            return ((JSONObject)object).length();
        } else if (object instanceof JSONArray) {
            return ((JSONArray)object).length();
        }
        return 0;
    }

    public static Object convertFromJSONHolder(Object o) {
        if (o instanceof JSONHolder) {
            return ((JSONHolder)o).getRawObject();
        }
        return o;
    }
}

package se.brange.jsonviewer.gui;

import org.json.JSONArray;
import org.json.JSONObject;
import org.netbeans.swing.outline.RowModel;
import se.brange.jsonviewer.json.JSONHelper;
import se.brange.jsonviewer.json.JSONValue;

public class JsonRowModel implements RowModel {
    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueFor(Object node, int column) {
        if (column == 0) {
            return JSONHelper.getStringValue(node);
        } else if (column == 1) {
            if (node instanceof JSONValue) {
                return ((JSONValue) node).getValue();
            }
            return node.toString() + " ..";
        } else {
            return "wh000t column=" + column;
        }
    }

    @Override
    public Class getColumnClass(int column) {
        if (column == 0) {
            return JSONObject.class;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return column == 1;
    }

    @Override
    public void setValueFor(Object node, int column, Object value) {
        System.out.println("se.brange.jsonviewer.gui.JsonRowModel.setValueFor");
        System.out.println("node = " + node + ", node.class=" + node.getClass());
        System.out.println("value = " + value + ", value.class=" + value.getClass());
        if (node instanceof JSONValue) {
            JSONValue jsonValue = (JSONValue) node;

            try {
                value = Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                // not an Integer
            }
            if (!(value instanceof Integer)) {
                try {
                    value = Double.parseDouble(value.toString());
                } catch (NumberFormatException e) {
                    // Not and Double
                }
                try {
                    value = Float.parseFloat(value.toString());
                } catch (NumberFormatException e) {
                    // Not and Double
                }
            }
            if (value.toString().toLowerCase().equals("true")) {
                value = Boolean.TRUE;
            } else if (value.toString().toLowerCase().equals("false")) {
                value = Boolean.FALSE;
            }

            jsonValue.setValue(value);
            if (jsonValue.getParent() instanceof JSONObject) {
                final JSONObject jsonObject = (JSONObject) jsonValue.getParent();
                jsonObject.put(jsonValue.getKey(), value);
                System.out.println("putting in object");
            } else if (jsonValue.getParent() instanceof JSONArray) {
                final JSONArray jsonArray = (JSONArray) jsonValue.getParent();
                jsonArray.put(jsonValue.getIndex(), value);
                System.out.println("putting in array");
            } else {
                System.out.println(jsonValue.getParent().getClass());
            }
        }

    }

    @Override
    public String getColumnName(int column) {
        if (column==0) {
            return "Type";
        }
        return "Value";
    }
}

package se.brange.jsonviewer.gui;

import javax.swing.tree.TreeModel;
import org.json.JSONObject;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.RowModel;
import se.brange.jsonviewer.json.JSONValue;

/**
 * Extends the {@link DefaultOutlineModel} so we can change the Node-name (JSONObjects keys)
 */
public class JsonOutlineModel extends DefaultOutlineModel {

    private JsonViewGUI gui;

    public static JsonOutlineModel createJsonOutlineModel(JsonTreeModel treeModel, JsonRowModel rowModel, JsonViewGUI gui) {
        JsonOutlineModel model = new JsonOutlineModel(treeModel, rowModel, false, "Key");
        model.setGui(gui);
        return model;
    }

    protected JsonOutlineModel(TreeModel treeModel, RowModel rowModel, boolean largeModel, String nodesColumnLabel) {
        super(treeModel, rowModel, largeModel, nodesColumnLabel);
    }


    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0 && rowIndex > 0) {
            Object object = getValueAt(rowIndex, 0);
            if (object instanceof JSONValue) {
                JSONValue jsonValue = (JSONValue) object;
                if (jsonValue.getParent() instanceof JSONObject) {
                    return true;
                }
            }
            return false;
        }
        return super.isCellEditable(rowIndex, columnIndex);
    }

    @Override
    protected void setTreeValueAt(Object aValue, int rowIndex) {
        if (rowIndex > 0) {
            Object object = getValueAt(rowIndex, 0);
            if (object instanceof JSONValue) {
                JSONValue jsonValue = (JSONValue) object;
                if (jsonValue.getParent() instanceof JSONObject) {
                    JSONObject parent = (JSONObject) jsonValue.getParent();
                    Object theObject = parent.get(jsonValue.getKey());
                    parent.remove(jsonValue.getKey());
                    parent.put(aValue.toString(), theObject);
                    jsonValue.setKey(aValue.toString());
                    gui.updateTextAreaText();
                }
            }
        }
    }

    public void setGui(JsonViewGUI gui) {
        this.gui = gui;
    }
}

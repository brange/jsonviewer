package se.brange.jsonviewer.gui;

import java.util.Set;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.json.JSONArray;
import org.json.JSONObject;
import se.brange.jsonviewer.json.JSONHelper;
import se.brange.jsonviewer.json.JSONHolder;
import se.brange.jsonviewer.json.JSONValue;

public class JsonTreeModel implements TreeModel {
    private Object rootObject;

    public JsonTreeModel(Object rootObject) {
        this.rootObject = rootObject;
    }

    @Override
    public Object getRoot() {
        return rootObject;
    }

    @Override
    public Object getChild(Object o, int i) {
        o = JSONHelper.convertFromJSONHolder(o);
        if (o instanceof JSONArray) {
            final Object o1 = ((JSONArray) o).get(i);
            if (o1 instanceof JSONObject || o1 instanceof JSONArray) {
                return new JSONHolder(i, o1);
            }
            return new JSONValue(i, o1, o);
        } else if (o instanceof JSONObject) {
            final JSONObject jsonObject = (JSONObject) o;
            int count=0;
            for (String s : (Set<String>)jsonObject.keySet()) {
                if (count == i) {
                    final Object o1 = jsonObject.get(s);
                    if (o1 instanceof JSONObject || o1 instanceof JSONArray) {
                        return new JSONHolder(s, o1);
                    }
                    return new JSONValue(s, o1, jsonObject);
                }
                count++;
            }
        }
        return null;
    }

    @Override
    public int getChildCount(Object o) {
        if (o instanceof JSONHolder) {
            o = ((JSONHolder)o).getRawObject();
        }
        if (o instanceof JSONArray) {
            return ((JSONArray)o).length();
        } else if (o instanceof JSONObject) {
            return ((JSONObject) o).keySet().size();
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object o) {
        o = JSONHelper.convertFromJSONHolder(o);
        if (o instanceof JSONObject || o instanceof JSONArray) {
            return false;
        }
        return true;
    }

    @Override
    public void valueForPathChanged(TreePath treePath, Object o) {
    }

    @Override
    public int getIndexOfChild(Object o, Object o2) {
        if (o instanceof JSONArray) {
            final JSONArray jsonArray = (JSONArray) o;
            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.get(i) == o2) {
                    return i;
                }
            }
        } else if (o instanceof JSONObject) {
            final JSONObject jsonObject = (JSONObject) o;
            int count=0;
            for (String s : (Set<String>)jsonObject.keySet()) {
                if (jsonObject.get(s) == o2) {
                    return count;
                }
                count++;
            }

        }
        return 0;
    }

    @Override
    public void addTreeModelListener(TreeModelListener treeModelListener) {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener treeModelListener) {

    }

}

package se.brange.jsonviewer.controllers;

import java.util.Set;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;
import org.json.JSONArray;
import org.json.JSONObject;
import se.brange.jsonviewer.fxgui.EditingCell;
import se.brange.jsonviewer.fxgui.JsonViewer;
import se.brange.jsonviewer.json.JSONHelper;
import se.brange.jsonviewer.json.JSONHolder;
import se.brange.jsonviewer.json.JSONValue;

public class TreeBuilder {

    private JsonViewer jsonViewer;
    public TreeBuilder(JsonViewer jsonViewer) {
        this.jsonViewer = jsonViewer;
        setupTree();
    }

    private void setupTree() {
        TreeTableView treeTableView = jsonViewer.getTreeTableView();
        final TreeTableColumn<Object, Object> columnKey = new TreeTableColumn<Object, Object>("Key");
        columnKey.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object, Object>, ObservableValue<Object>>() {
            @Override
            public ObservableValue<Object> call(TreeTableColumn.CellDataFeatures<Object, Object> objectObjectCellDataFeatures) {
                final Object obj = objectObjectCellDataFeatures.getValue().getValue();
                String columnValue;
                if (obj instanceof JSONHolder) {
                    columnValue = ((JSONHolder) obj).toString();
                } else if (obj instanceof JSONValue) {
                    columnValue = ((JSONValue) obj).toString();
                } else {
                    columnValue = "Unkown class: " + obj.getClass();
                }

                return new ReadOnlyObjectWrapper<Object>(columnValue);
            }
        });

        TreeTableColumn<Object, Object> columnValue = new TreeTableColumn<Object, Object>("Value");
        columnValue.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object, Object>, ObservableValue<Object>>() {
            @Override
            public ObservableValue<Object> call(TreeTableColumn.CellDataFeatures<Object, Object> objectObjectCellDataFeatures) {
                final Object obj = objectObjectCellDataFeatures.getValue().getValue();
                String columnValue;
                if (obj instanceof JSONHolder) {
                    columnValue = "";
                } else if (obj instanceof JSONValue) {
                    columnValue = ((JSONValue)obj).getValue().toString();
                } else {
                    columnValue = "Unkown class: " + obj.getClass();
                }
                return new ReadOnlyObjectWrapper<Object>(columnValue);
            }
        });

        TreeTableColumn<Object, Object> columnType = new TreeTableColumn<Object, Object>("Type");
        columnType.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object, Object>, ObservableValue<Object>>() {
            @Override
            public ObservableValue<Object> call(TreeTableColumn.CellDataFeatures<Object, Object> objectObjectCellDataFeatures) {
                final Object obj = objectObjectCellDataFeatures.getValue().getValue();
                return new ReadOnlyObjectWrapper<Object>(JSONHelper.getStringValue(obj));
            }
        });

        columnKey.setMinWidth(100);
        columnValue.setMinWidth(100);
        columnType.setMinWidth(100);

        treeTableView.setEditable(true);
        columnKey.setEditable(true);
        columnValue.setEditable(true);
        treeTableView.getColumns().add(columnKey);
        treeTableView.getColumns().add(columnValue);
        treeTableView.getColumns().add(columnType);

        columnKey.setCellFactory(new Callback<TreeTableColumn<Object, Object>, TreeTableCell<Object, Object>>() {
            @Override
            public TreeTableCell<Object, Object> call(TreeTableColumn<Object, Object> objectObjectTreeTableColumn) {
                return new EditingCell(EditingCell.COLUMN_KEY);
            }
        });

        columnValue.setCellFactory(new Callback<TreeTableColumn<Object, Object>, TreeTableCell<Object, Object>>() {
            @Override
            public TreeTableCell<Object, Object> call(TreeTableColumn<Object, Object> objectObjectTreeTableColumn) {
                return new EditingCell(EditingCell.COLUMN_VALUE);
            }
        });

        columnKey.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Object, Object>>() {
            @Override
            public void handle(TreeTableColumn.CellEditEvent<Object, Object> objectObjectCellEditEvent) {
                if (objectObjectCellEditEvent.getNewValue().equals(objectObjectCellEditEvent.getOldValue())) {
                    System.out.println("Same value..");
                    return;
                }
                final String oldKey = objectObjectCellEditEvent.getOldValue().toString();
                final String newKey = objectObjectCellEditEvent.getNewValue().toString();

                final Object editedValue = objectObjectCellEditEvent.getRowValue().getValue();
                JSONObject parent;
                if (editedValue instanceof JSONValue) {
                    final JSONValue jsonValue = (JSONValue) editedValue;
                    jsonValue.setKey(newKey);
                    parent = (JSONObject) jsonValue.getParent();
                } else if (editedValue instanceof JSONHolder) {
                    final JSONHolder jsonHolder = (JSONHolder) editedValue;
                    jsonHolder.setKey(newKey);
                    parent = (JSONObject) jsonHolder.getParent();
                } else {
                    throw new RuntimeException("Unkown class: " + editedValue.getClass());
                }

                Object theObject = parent.get(oldKey);
                parent.put(newKey, theObject);
                parent.remove(oldKey);
            }
        });

        columnValue.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Object, Object>>() {
            @Override
            public void handle(TreeTableColumn.CellEditEvent<Object, Object> objectObjectCellEditEvent) {
                final Object editedValue = objectObjectCellEditEvent.getRowValue().getValue();
                if (editedValue instanceof JSONValue) {
                    JSONValue jsonValue = (JSONValue) editedValue;
                    jsonValue.setValue(objectObjectCellEditEvent.getNewValue().toString());
                }
            }
        });
    }

    public TreeItem<Object> constructRoot(Object json) {
        TreeItem<Object> root = createNode(new JSONHolder("root",json, null));
        root.setExpanded(true);

        return root;
    }

    /**
     * Creates a TreeItem<Object>.
     * Call this with the ROOT JSONObject or JSONArray to create the whole tree.
     * @param object a {@link org.json.JSONObject} or {@link org.json.JSONArray}
     * @return
     */
    private TreeItem<Object> createNode(final Object object) {
        final TreeItem<Object> node = new TreeItem<Object>(object) {
            private boolean isFirsttimeChildren=true;

            @Override
            public ObservableList<TreeItem<Object>> getChildren() {
                if (isFirsttimeChildren) {
                    isFirsttimeChildren = false;
                    super.getChildren().addAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                return object instanceof JSONValue;
            }
        };

        return node;
    }

    private ObservableList<TreeItem<Object>> buildChildren(TreeItem<Object> treeItem) {
        Object f = treeItem.getValue();
        if (f instanceof JSONHolder) {
            JSONHolder jsonHolder = (JSONHolder) f;
            ObservableList<TreeItem<Object>> children = FXCollections.observableArrayList();
            if (jsonHolder.isJSONArray()) {
                JSONArray arr = jsonHolder.getJSONArray();
                for (int i = 0; i < arr.length(); i++) {
                    Object objAtIndex = arr.get(i);
                    if (objAtIndex instanceof JSONArray || objAtIndex instanceof JSONObject) {
                        children.add(createNode(new JSONHolder(i, objAtIndex, arr)));
                    } else {
                        children.add(createNode(new JSONValue(i, objAtIndex, arr)));
                    }
                }
            } else {
                JSONObject obj = jsonHolder.getJSONObject();
                for (String key : (Set<String>)obj.keySet()) {
                    final Object objForKey = obj.get(key);
                    if (objForKey instanceof JSONArray || objForKey instanceof JSONObject) {
                        children.add(createNode(new JSONHolder(key, objForKey, obj)));
                    } else {
                        children.add(createNode(new JSONValue(key, objForKey, obj)));
                    }

                }
            }
            return children;
        }
        return FXCollections.emptyObservableList();
    }

    public static String getValue(TreeItem<Object> child) {
        if (child.getValue() instanceof JSONHolder) {
            return null;
        } else if (child.getValue() instanceof JSONValue) {
            JSONValue jsonValue = (JSONValue) child.getValue();
            return jsonValue.getValue().toString();
        }
        return null;
    }

    public static String getKey(TreeItem<Object> child) {
        if (child.getValue() instanceof JSONHolder) {
            JSONHolder jsonHolder = (JSONHolder) child.getValue();
            return jsonHolder.getKeyOrIndex();
        } else if (child.getValue() instanceof JSONValue) {
            JSONValue jsonValue = (JSONValue) child.getValue();
            return jsonValue.getKeyOrIndex();
        }
        return null;
    }

    public static String getType(TreeItem<Object> child) {
        if (child.getValue() instanceof JSONHolder) {
            JSONHolder jsonHolder = (JSONHolder) child.getValue();
            return JSONHelper.getStringValue(jsonHolder);
        } else if (child.getValue() instanceof JSONValue) {
            JSONValue jsonValue = (JSONValue) child.getValue();
            return JSONHelper.getStringValue(jsonValue.getValue());
        }
        return null;
    }
}

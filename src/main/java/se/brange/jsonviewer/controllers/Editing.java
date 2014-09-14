package se.brange.jsonviewer.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.dialog.Dialogs;
import org.json.JSONArray;
import org.json.JSONObject;
import se.brange.jsonviewer.fxgui.JsonViewer;
import se.brange.jsonviewer.json.JSONHolder;
import se.brange.jsonviewer.json.JSONValue;

public class Editing {

    private enum ADD_TYPE {
        VALUE,
        OBJECT,
        ARRAY;
    }


    private Scene scene;
    private TreeTableView<Object> treeTableView;
    private Button removeButton,upButton,downButton;
    private MenuButton addButton;

    public Editing(JsonViewer jsonViewer) {
        this.scene = jsonViewer.getScene();
        this.treeTableView = jsonViewer.getTreeTableView();
        setupEditButtons();
    }

    private void setupEditButtons() {
        addButton = (MenuButton) scene.lookup("#addButton");
        MenuItem addButtonValue = addButton.getItems().get(0);
        addButtonValue.setText("Value");
        addButtonValue.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                addHandler(ADD_TYPE.VALUE);
            }
        });
        MenuItem addButtonObject = addButton.getItems().get(1);
        addButtonObject.setText("Object");
        addButtonObject.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                addHandler(ADD_TYPE.OBJECT);
            }
        });
        MenuItem addButtonArray = addButton.getItems().get(2);
        addButtonArray.setText("Array");
        addButtonArray.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                addHandler(ADD_TYPE.ARRAY);
            }
        });

        removeButton = (Button) scene.lookup("#removeButton");
        upButton = (Button) scene.lookup("#upButton");
        downButton = (Button) scene.lookup("#downButton");

        removeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TreeItem<Object> selectedItem = treeTableView.getSelectionModel().getSelectedItem();
                if (selectedItem.getParent() == null) {
                    System.err.println("Can't remove the ROOT-element.");
                    return;
                }

                selectedItem.getParent().getChildren().remove(selectedItem);
                Object selected = selectedItem.getValue();
                Object parent = null;
                String key = null;
                Integer index = null;
                if (selected instanceof JSONValue) {
                    JSONValue jsonValue = (JSONValue) selected;
                    parent = jsonValue.getParent();
                    key = jsonValue.getKey();
                    index = jsonValue.getIndex();
                } else if (selected instanceof JSONHolder) {
                    JSONHolder jsonHolder = (JSONHolder) selected;
                    parent = jsonHolder.getParent();
                    key = jsonHolder.getKey();
                    index = jsonHolder.getIndex();
                }

                if (parent instanceof JSONArray) {
                    JSONArray parentArray = (JSONArray) parent;
                    parentArray.remove(index);

                } else if (parent instanceof JSONObject) {
                    JSONObject parentObject = (JSONObject) parent;
                    parentObject.remove(key);
                }
            }
        });

        upButton.setOnAction(new MoveEventHandler(true));
        downButton.setOnAction(new MoveEventHandler(false));
    }

    private void addHandler(ADD_TYPE addType) {
        TreeItem<Object> selectedItem = treeTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            System.err.println("Nothing selected");
            return;
        }

        JSONHolder parent;
        TreeItem<Object> _parent;
        if (selectedItem.getValue() instanceof  JSONHolder) {
            parent = (JSONHolder) selectedItem.getValue();
            _parent = selectedItem;
        } else {
            parent = (JSONHolder) selectedItem.getParent().getValue();
            _parent = selectedItem.getParent();
        }

        if (parent.isJSONArray()) {
            // Array, just add the thing at the end of the list.
            JSONArray jsonArray = parent.getJSONArray();
            Object child;
            switch (addType) {
                case VALUE:
                    child = new JSONValue(jsonArray.length(), "", jsonArray);
                    break;
                case OBJECT:
                    child = new JSONHolder(jsonArray.length(), new JSONObject(), jsonArray);
                    break;
                case ARRAY:
                    child = new JSONHolder(jsonArray.length(), new JSONArray(), jsonArray);
                    break;
                default:
                    throw new RuntimeException("Unkown add type " + addType);
            }
            jsonArray.put(jsonArray.length(), child);
            TreeItem<Object> item = new TreeItem<Object>(child) {
                @Override
                public boolean isLeaf() {
                    return child instanceof JSONValue;
                }
            };
            _parent.getChildren().add(item);
            treeTableView.getSelectionModel().select(item);
        } else {
            String newKey = Dialogs.create()
                .lightweight()
                .nativeTitleBar()
                .title("Select key")
                .masthead(null)
                .showTextInput();

            if (StringUtils.isEmpty(newKey)) {
                System.err.println("Empty key..");
            } else {
                JSONObject jsonObject = parent.getJSONObject();
                if (jsonObject.has(newKey)) {
                    Dialogs.create()
                        .title("Key exists")
                        .masthead(null)
                        .nativeTitleBar()
                        .lightweight()
                        .message("The key '" + newKey + "' exists.")
                        .showError();
                    return;
                }
                Object child;
                switch (addType) {
                    case VALUE:
                        child = new JSONValue(newKey, "", jsonObject);
                        break;
                    case OBJECT:
                        child = new JSONHolder(newKey, new JSONObject(), jsonObject);
                        break;
                    case ARRAY:
                        child = new JSONHolder(newKey, new JSONArray(), jsonObject);
                        break;
                    default:
                        throw new RuntimeException("Unkown add type " + addType);
                }
                jsonObject.put(newKey, child);
                TreeItem<Object> item = new TreeItem<Object>(child) {
                    @Override
                    public boolean isLeaf() {
                        return child instanceof JSONValue;
                    }
                };
                _parent.getChildren().add(item);
                treeTableView.requestFocus();
                treeTableView.getSelectionModel().select(item);

            }
        }
    }

    private class MoveEventHandler implements EventHandler<ActionEvent> {

        private boolean movingUp;
        private MoveEventHandler(boolean movingUp) {
            this.movingUp = movingUp;
        }

        @Override
        public void handle(ActionEvent actionEvent) {
            moveUpOrDown();
        }

        private int getSiblingsIndex(JSONArray parent, int index) {
            if (movingUp) {
                int siblingsIndex = index - 1;
                if (siblingsIndex < 0) {
                    siblingsIndex = parent.length()-1;
                }
                return siblingsIndex;
            } else {
                int siblingsIndex = index + 1;
                if (siblingsIndex >= parent.length()) {
                    siblingsIndex = 0;
                }
                return siblingsIndex;
            }
        }
        private void moveUpOrDown() {
            TreeItem<Object> selectedItem = treeTableView.getSelectionModel().getSelectedItem();
            if (selectedItem == null || selectedItem.getParent() == null) {
                System.err.println("Can't move nothing or the ROOT-element.");
                return;
            }

            Object selected = selectedItem.getValue();
            JSONArray parent = null;
            Integer index = null;

            if (selected instanceof JSONValue) {
                JSONValue jsonValue = (JSONValue) selected;
                if (jsonValue.getParent() instanceof JSONArray) {
                    parent = (JSONArray) jsonValue.getParent();
                    index = jsonValue.getIndex();
                    int siblingsIndex = getSiblingsIndex(parent, index);
                    jsonValue.setIndex(siblingsIndex);
                }
            } else if (selected instanceof JSONHolder) {
                JSONHolder jsonHolder = (JSONHolder) selected;
                if (jsonHolder.getParent() instanceof JSONArray) {
                    parent = (JSONArray) jsonHolder.getParent();
                    index = jsonHolder.getIndex();
                    int siblingsIndex = getSiblingsIndex(parent, index);
                    jsonHolder.setIndex(siblingsIndex);
                }
            }

            if (parent != null) {
                // Moving
                int siblingsIndex = getSiblingsIndex(parent, index);
                Object sibling = parent.get(siblingsIndex);
                parent.put(siblingsIndex, ((JSONValue) selected).getValue());
                parent.put(index, sibling);

                TreeItem<Object> _sibling = selectedItem.getParent().getChildren().get(siblingsIndex);
                selectedItem.getParent().getChildren().set(siblingsIndex, selectedItem);
                selectedItem.getParent().getChildren().set(index, _sibling);

                if (_sibling.getValue() instanceof JSONValue) {
                    ((JSONValue) _sibling.getValue()).setIndex(index);
                } else if (_sibling.getValue() instanceof JSONHolder) {
                    ((JSONHolder) _sibling.getValue()).setIndex(index);
                }

                TreeTableView.TreeTableViewSelectionModel<Object> selectionModel = treeTableView.getSelectionModel();
                // TODO: Here is a bug when you move one child from index 0 to parent.length-1,
                // the wrong child is selected. It looks like the right child is selected, but when
                // you move it again, the wrong one will be moved.
                selectionModel.select(selectedItem);
            } else {
                Dialogs.create()
                    .title("Can't move children of a JSONObject")
                    .lightweight()
                    .nativeTitleBar()
                    .masthead(null)
                    .message("Can't move children of a JSONObject")
                    .showInformation();
            }
        }
    }
}

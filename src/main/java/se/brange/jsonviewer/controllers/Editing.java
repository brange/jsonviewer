package se.brange.jsonviewer.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.controlsfx.dialog.Dialogs;
import org.json.JSONArray;
import org.json.JSONObject;
import se.brange.jsonviewer.fxgui.JsonViewer;
import se.brange.jsonviewer.json.JSONHolder;
import se.brange.jsonviewer.json.JSONValue;

public class Editing {
    private Scene scene;
    private TreeTableView<Object> treeTableView;
    private Button addButton,removeButton,upButton,downButton;

    public Editing(JsonViewer jsonViewer) {
        this.scene = jsonViewer.getScene();
        this.treeTableView = jsonViewer.getTreeTableView();
        setupEditButtons();
    }

    private void setupEditButtons() {
        addButton = (Button) scene.lookup("#addButton");
        removeButton = (Button) scene.lookup("#removeButton");
        upButton = (Button) scene.lookup("#upButton");
        downButton = (Button) scene.lookup("#downButton");

        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("Adding...");
            }
        });

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
                    .masthead(null)
                    .message("Can't move children of a JSONObject")
                    .showInformation();
            }
        }
    }
}

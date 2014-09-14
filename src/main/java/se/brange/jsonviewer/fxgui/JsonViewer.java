package se.brange.jsonviewer.fxgui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.brange.jsonviewer.controllers.Searcher;
import se.brange.jsonviewer.controllers.TreeBuilder;
import se.brange.jsonviewer.json.JSONHolder;
import se.brange.jsonviewer.json.JSONValue;

public class JsonViewer extends Application {

    private Scene scene;
    private Stage primaryStage;
    private TextArea inputArea;

    private TreeItem<Object> root;
    private TreeTableView<Object> treeTableView;

    private Searcher searcher;
    private TreeBuilder treeBuilder;
    private Button copyButton;
    private CheckBox prettyCopy;

    private Button addButton,removeButton,upButton,downButton;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/jsonviewer.fxml"));
        this.primaryStage = primaryStage;
        primaryStage.setTitle("JsonViewer");
        scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        setup();

        JSONObject testObject = new JSONObject();
        testObject.put("child1",1);
        testObject.put("child2",2);
        testObject.put("child3",3);
        testObject.put("object1", new JSONObject().put("someChild",1));
        testObject.put("array1", new JSONArray().put("aChild").put("twoChild").put("threeChild"));
        setRootJson(testObject);
    }

    private void setup() {
        setupTreeTableView();
        setupInputArea();
        setupCopyButton();
        setupEditButtons();

        searcher = new Searcher(this);
        treeBuilder = new TreeBuilder(this);
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
            if (selectedItem.getParent() == null) {
                System.err.println("Can't move the ROOT-element.");
                return;
            }

            Object selected = selectedItem.getValue();
            JSONArray parent = null;
            Integer index = null;

            if (selected instanceof JSONValue) {
                JSONValue jsonValue = (JSONValue) selected;
                System.out.println("jsonValue.getValue() = " + jsonValue.getValue());
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
                System.out.println("siblingsIndex = " + siblingsIndex + ", index = " + index);
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
                System.out.println("Not moving since this is not a JSONArray.");
            }
        }
    }

    private void setupTreeTableView() {
        treeTableView = (TreeTableView) scene.lookup("#tableView");
        treeTableView.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.isMetaDown() && event.getText().equals("f")) {
                    searcher.requestFocis();
                }
            }
        });
    }

    private void setupCopyButton() {
        copyButton = (Button) scene.lookup("#copyButton");
        prettyCopy = (CheckBox) scene.lookup("#prettyCopy");

        copyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                JSONHolder value = (JSONHolder) treeTableView.getRoot().getValue();

                int indent = 0;
                if (prettyCopy.isSelected()) {
                    indent = 2;
                }

                String text;
                if (value.isJSONObject()) {
                    text = value.getJSONObject().toString(indent);
                } else if (value.isJSONArray()) {
                    text = value.getJSONArray().toString(indent);
                } else {
                    text = "Error...";
                }

                StringSelection stringSelection = new StringSelection(text);
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard ();
                clpbrd.setContents(stringSelection, null);
            }
        });
    }

    private void setupInputArea() {
        inputArea = (TextArea) scene.lookup("#inputArea");
        inputArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldText, String newText) {
                if (newText.startsWith("{")) {
                    // JSONObject
                    try {
                        JSONObject jsonObject = new JSONObject(newText);
                        setRootJson(jsonObject);
                    } catch (JSONException e) {
                        setRootJson(new JSONObject().put("error", "Invalid JSONObject"));
                    }
                } else if (newText.startsWith("[")) {
                    // JSON array.
                    try {
                        JSONArray array = new JSONArray(newText);
                        setRootJson(array);
                    } catch (JSONException e) {
                        setRootJson(new JSONObject().put("error", "Invalid JSONArray"));
                    }
                } else {
                    setRootJson(new JSONObject().put("error", "Invalid JSON input"));
                }
            }
        });
    }


    private void setRootJson(Object jsonObject) {
        root = treeBuilder.constructRoot(jsonObject);
        treeTableView.setRoot(root);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Scene getScene() {
        return scene;
    }

    public TreeItem<Object> getRoot() {
        return root;
    }

    public TreeTableView<Object> getTreeTableView() {
        return treeTableView;
    }
}

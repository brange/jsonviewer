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
import se.brange.jsonviewer.controllers.Editing;
import se.brange.jsonviewer.controllers.Searcher;
import se.brange.jsonviewer.controllers.TreeBuilder;
import se.brange.jsonviewer.json.JSONHolder;

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
    private Editing editing;

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

        editing = new Editing(this);
        searcher = new Searcher(this);
        treeBuilder = new TreeBuilder(this);
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

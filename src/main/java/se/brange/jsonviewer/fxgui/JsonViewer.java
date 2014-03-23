package se.brange.jsonviewer.fxgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import se.brange.jsonviewer.controllers.Searcher;
import se.brange.jsonviewer.controllers.TreeBuilder;

public class JsonViewer extends Application {

    private Scene scene;
    private Stage primaryStage;
    private TextArea inputArea;

    private TreeItem<Object> root;
    private TreeTableView<Object> treeTableView;

    private Searcher searcher;
    private TreeBuilder treeBuilder;

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
        testObject.put("array1", new JSONArray().put("aChild").put("twoChild"));
        setRootJson(testObject);
    }

    private void setup() {

        treeTableView = (TreeTableView) scene.lookup("#tableView");
        searcher = new Searcher(this);
        treeBuilder = new TreeBuilder(this);
    }


    private void setRootJson(JSONObject testObject) {
        root = treeBuilder.constructRoot(testObject);
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

package se.brange.jsonviewer.controllers;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import se.brange.jsonviewer.fxgui.JsonViewer;

public class Searcher {

    private JsonViewer jsonViewer;
    final private List<TreeItem<Object>> searchResults = new ArrayList<TreeItem<Object>>();

    private TextField searchField;
    private CheckBox searchKey;
    private CheckBox searchValue;
    private CheckBox searchType;

    public Searcher(JsonViewer jsonViewer) {
        this.jsonViewer = jsonViewer;
        setup();
    }

    /**
     * Request focus for the search field
     */
    public void requestFocis() {
        this.searchField.requestFocus();
    }

    private void addEventHandler(CheckBox... checkBoxes) {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.addEventHandler(ActionEvent.ANY, new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    searchResults.clear();
                }
            });
        }
    }

    private void setup() {
        Scene scene = jsonViewer.getScene();
        searchField = (TextField) scene.lookup("#searchField");
        searchKey = (CheckBox) scene.lookup("#searchKey");
        searchValue = (CheckBox) scene.lookup("#searchValue");
        searchType = (CheckBox) scene.lookup("#searchType");
        addEventHandler(searchKey, searchValue, searchType);

        searchField.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            private int index = 0;

            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    index = doSearch(index, jsonViewer.getRoot(), true);
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    jsonViewer.getTreeTableView().requestFocus();
                } else {
                    searchResults.clear();
                    index = 0;
                }
            }
        });
    }

    private int doSearch(int index, TreeItem<Object> node, boolean first) {
        if (node != null && searchField != null) {

            boolean sKey = searchKey.isSelected();
            boolean sValue = searchValue.isSelected();
            boolean sType = searchType.isSelected();
            String sText = searchField.getText().toLowerCase();

            if (searchResults.isEmpty() || !first) {
                if (first) {
                    System.out.println("Building first");
                }
                // Build up the searchResults.
                for (TreeItem<Object> child : node.getChildren()) {
                    String key = TreeBuilder.getKey(child);
                    String type = TreeBuilder.getType(child);
                    String value = TreeBuilder.getValue(child);

                    if ((sKey && key != null && key.toLowerCase().contains(sText)) ||
                        (sValue && value != null && value.toLowerCase().contains(sText)) ||
                        (sType && type != null && type.toLowerCase().contains(sText))) {
                        searchResults.add(child);
                    }

                    if (!child.isLeaf()) {
                        doSearch(index, child, false);
                    }
                }
            }

            if (first && !searchResults.isEmpty()) {
                System.out.println("searchResults = " + searchResults.size());
                if (index >= searchResults.size()) {
                    index = 0;
                }
                final TreeItem<Object> currentFind = searchResults.get(index);
                final TreeItem<Object> parent = currentFind.getParent();
                if (!parent.isExpanded()) {
                    parent.setExpanded(true);
                }
                jsonViewer.getTreeTableView().getSelectionModel().select(currentFind);
                System.out.println("currentFind = " + currentFind);
                index++;
            }
            return index;
        }
        return 0;
    }
}

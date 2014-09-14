package se.brange.jsonviewer.fxgui;

import java.awt.event.FocusEvent;
import java.util.HashSet;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.dialog.Dialog;
import org.json.JSONObject;
import se.brange.jsonviewer.json.JSONHolder;
import se.brange.jsonviewer.json.JSONValue;

public class EditingCell extends TreeTableCell<Object, Object> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_VALUE = 1;
    public static final int COLUMN_TYPE = 2;


    private TextField textField;
    private int column;

    private String currentKey;
    private Set<String> siblingKeys;

    public EditingCell(int column) {
        this.column = column;
    }

    @Override
    public void startEdit() {
        boolean canEdit = false;
        Object parent = null;
        if (getTreeTableRow().getItem() instanceof JSONValue) {
            parent = ((JSONValue)getTreeTableRow().getItem()).getParent();
            if (column == COLUMN_VALUE) {
                // Can always edit values.
                canEdit = true;
            }
        } else if (getTreeTableRow().getItem() instanceof JSONHolder && column == COLUMN_KEY) {
            final JSONHolder jsonHolder = (JSONHolder) getTreeTableRow().getItem();
            parent = jsonHolder.getParent();
        }

        if (parent != null && parent instanceof JSONObject && column == COLUMN_KEY) {
            canEdit = true;
            currentKey = getText();
            siblingKeys = new HashSet<String>();
            for (String key : (Set<String>)((JSONObject)parent).keySet()) {
                siblingKeys.add(key);
            }
            siblingKeys.remove(currentKey);
        }

        if (!isEmpty() && canEdit) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText((String)getItem());
        setGraphic(null);
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                    textField.requestFocus();
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
        textField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    doChange();
                }
            }
        });
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldProperty, Boolean newPropery) {
                if (!newPropery) {
                    doChange();
                }
            }
        });
    }

    private void doChange() {
        final String newKey = textField.getText();
        if (siblingKeys != null && siblingKeys.contains(newKey)) {
            //TODO-brange: Make the dialog more beautiful.
            Dialog _dialog = new Dialog(null, "Error");
            _dialog.setContent("One sibling has the same key '" + newKey + "'.");
            _dialog.show();
            cancelEdit();
        } else {
            commitEdit(newKey);
        }
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}

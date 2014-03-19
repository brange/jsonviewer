package se.brange.jsonviewer.gui;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.tree.TreePath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import se.brange.jsonviewer.json.JSONHolder;
import se.brange.jsonviewer.json.JSONValue;

public class JsonViewGUI extends JFrame {

    // GUI components that are added via JsonViewGUI.form (IntelliJ GUI builder)
    private JTextPane inputArea;
    private JLabel outputLabel;
    private JLabel inputLabel;
    private JScrollPane outputScrollPane;
    private JPanel MainPanel;
    private JTextField searchField;
    private JCheckBox searchKey;
    private JCheckBox searchType;
    private JCheckBox searchValue;
    private JPanel searchPanel;
    // End of GUI components that are added via JsonViewGUI.form


    private JViewport viewPort;

    private Object currentJson;
    private Outline rootOutline;

    public JsonViewGUI() throws HeadlessException {
        super("JsonView");
    }

    public void start() {
        setContentPane(MainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    private void createUIComponents() {
        if (inputArea==null) {
            inputArea = new JTextPane();
            Style style = inputArea.addStyle("DEFAULT", null);
            StyleConstants.setForeground(style, Color.BLACK);
            currentJson = new JSONObject() {{
                put("oneString", "This is my String, my string is amazing.");
                put("anotherString", "Look at my String..");
                put("isMyStringAmazing", true);
                put("howAmazing", 57);
                put("myList",
                    new JSONArray() {{
                        put("hello");
                        put("How are you");
                        put("Im fine");
                    }});
            }};
            updateTextAreaText();


            outputScrollPane = new JScrollPane();
            viewPort = new JViewport();
            outputScrollPane.setViewport(viewPort);

            updateOutline(currentJson);

            searchField = new JTextField();
            addListener();
        }
    }

    private void startSearch() {
        System.out.println("Doing the search.");
        OutlineModel model = (OutlineModel) rootOutline.getModel();
        List<SearchItem> searchItems = new ArrayList<SearchItem>();

        buildSearchList(searchItems, null, model.getRoot(), model);
        Object child = model.getChild(model.getRoot(), 0);

        TreePath treePath = new TreePath(model.getRoot());
        int pathCount = treePath.getPathCount();
        System.out.println("pathCount = " + pathCount);
        Object pathComponent = treePath.getPathComponent(0);
        System.out.println("pathComponent = " + pathComponent);
        System.out.println("pathComponent.getClass() = " + pathComponent.getClass());

        for (SearchItem searchItem : searchItems) {
            System.out.println("searchItem = " + searchItem);
            if (searchItem.matches(searchField.getText())) {
                System.out.println("Matches");
                Object child1 = model.getChild(searchItem.getParent(), 0);
                System.out.println("child1 = " + child1);
                System.out.println("child1.getClass() = " + child1.getClass());
                System.out.println("searchItem.getParent() = " + searchItem.getParent());
                rootOutline.expandPath(new TreePath(searchItem.getParent()));
            }
        }
    }

    private void buildSearchList(List<SearchItem> paths, Object parent, Object object, OutlineModel model) {
        if (!model.isLeaf(object)) {
            paths.add(new SearchItem(parent, (JSONHolder)object));
            for (int i = 0; i < model.getChildCount(object); i++) {
                Object child = model.getChild(object, i);
                buildSearchList(paths, object, child, model);
            }
        } else {
            paths.add(new SearchItem(parent, (JSONValue) object));
        }
    }

    private void addListener() {
        searchField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {

            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    rootOutline.grabFocus();
                } else if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Do the search.
                    startSearch();
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });
        inputArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {}

            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                String json = inputArea.getText();
                if (json != null) {
                    clearErrorMessageAndStyle();
                    try {
                        if (json.startsWith("{")) {
                            JSONObject jsonObject = new JSONObject(json);
                            updateOutline(jsonObject);
                        } else if (json.startsWith("[")) {
                            JSONArray jsonArray = new JSONArray(json);
                            updateOutline(jsonArray);
                        } else {
                            inputLabel.setForeground(Color.RED);
                            inputLabel.setToolTipText("Input is not a JSON object or array.");
                        }
                    } catch (JSONException jsonException) {
                        String message = jsonException.getMessage();
                        inputLabel.setToolTipText(message);
                        inputLabel.setForeground(Color.RED);
                        Pattern pattern = Pattern.compile("(.*)at ([0-9]+) \\[character ([0-9]+) line ([0-9]+)\\](.*)");                            Matcher matcher = pattern.matcher(message);
                        if (matcher.matches()) {
                            String absoluteCharacter = matcher.group(2);
                            Style style = inputArea.addStyle("Red", null);
                            StyleConstants.setForeground(style, Color.RED);
                            inputArea.getStyledDocument().setCharacterAttributes(
                                Integer.parseInt(absoluteCharacter)-3,
                                4,
                                inputArea.getStyle("Red"),
                                true
                            );
                        }
                    }
                }
            }

        });
    }

    private void clearErrorMessageAndStyle() {
        String json = inputArea.getText();
        inputLabel.setForeground(null);
        inputLabel.setToolTipText(null);
        inputArea.getStyledDocument().setCharacterAttributes(0, json.length(), inputArea.getStyle("DEFAULT"), true);
    }

    private void updateOutline(Object jsonObject) {
        this.currentJson = jsonObject;
        OutlineModel outlineModel =  JsonOutlineModel.createJsonOutlineModel(new JsonTreeModel(new JSONHolder("ROOT", jsonObject)), new JsonRowModel(this), this);
        rootOutline = new Outline(outlineModel);

        rootOutline.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {

            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_F && keyEvent.getModifiers() == 4) {
                    //TODO-brange: Make the check better.
                    searchField.grabFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });
        rootOutline.setRenderDataProvider(new JsonRenderDataProvider());
        viewPort.add(rootOutline);
    }


    public void updateTextAreaText() {
        inputArea.setText(((JSONObject)currentJson).toString(4));
        if (inputLabel != null) {
            clearErrorMessageAndStyle();
        }
    }
}

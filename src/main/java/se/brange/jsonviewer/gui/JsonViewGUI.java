package se.brange.jsonviewer.gui;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import se.brange.jsonviewer.json.JSONHolder;

public class JsonViewGUI extends JFrame {

    // GUI components that are added via JsonViewGUI.form (IntelliJ GUI builder)
    private JTextPane inputArea;
    private JLabel outputLabel;
    private JLabel inputLabel;
    private JScrollPane outputScrollPane;
    private JButton button1;
    private JButton button2;
    private JPanel MainPanel;
    private JButton button3;
    // End of GUI components that are added via JsonViewGUI.form


    private JViewport viewPort;

    private Object currentJson;

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
            button1 = new JButton();
            button2 = new JButton();
            button3 = new JButton();
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

            button2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    System.out.println("currentJson = " + currentJson.toString());
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
        Outline rootOutline = new Outline(outlineModel);
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

package se.brange.jsonviewer.gui;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventObject;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import se.brange.jsonviewer.json.JSONHolder;

public class JsonViewGUI extends JFrame {

    // GUI components that are added via JsonViewGUI.form (IntelliJ GUI builder)
    private JTextArea inputArea;
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
            inputArea = new JTextArea("hej1");
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
                        if (json.startsWith("{")) {
                            JSONObject jsonObject = new JSONObject(json);
                            System.out.println("jsonObject = " + jsonObject.toString(2));
                            updateOutline(jsonObject);
                        } else if (json.startsWith("[")) {
                            JSONArray jsonArray = new JSONArray(json);
                            System.out.println("jsonArray = " + jsonArray.toString(2));
                            updateOutline(jsonArray);
                        } else {
                            System.out.println("error, not a JSONObjec/JSONArray '" + json + "'.");
                            // TODO: Warn, not a JSONObject and not a JSONArray.
                        }
                    }
                }

            });
        }
    }

    private void updateOutline(Object jsonObject) {
        this.currentJson = jsonObject;
        OutlineModel outlineModel =  DefaultOutlineModel.createOutlineModel(new JsonTreeModel(new JSONHolder("ROOT", jsonObject)), new JsonRowModel(this), false, "Key");
        Outline rootOutline = new Outline(outlineModel);;
        viewPort.add(rootOutline);
    }


    public void updateTextAreaText() {
        inputArea.setText(((JSONObject)currentJson).toString(4));
    }
}

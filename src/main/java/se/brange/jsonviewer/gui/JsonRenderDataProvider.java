package se.brange.jsonviewer.gui;

import java.awt.Color;
import javax.swing.Icon;
import org.netbeans.swing.outline.RenderDataProvider;

public class JsonRenderDataProvider implements RenderDataProvider {
    @Override
    public String getDisplayName(Object o) {
        return null;
    }

    @Override
    public boolean isHtmlDisplayName(Object o) {
        return false;
    }

    @Override
    public Color getBackground(Object o) {
        return null;
    }

    @Override
    public Color getForeground(Object o) {
        return null;
    }

    @Override
    public String getTooltipText(Object o) {
        return null;
    }

    @Override
    public Icon getIcon(Object o) {
        return null;
    }
}

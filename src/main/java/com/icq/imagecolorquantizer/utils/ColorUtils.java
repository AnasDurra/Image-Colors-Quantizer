package com.icq.imagecolorquantizer.utils;

import java.awt.*;

public class ColorUtils {
    public static javafx.scene.paint.Color convertColorToPaint(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();
        double opacity = a / 255.0;
        return javafx.scene.paint.Color.rgb(r, g, b, opacity);
    }
}

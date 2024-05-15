package cn.iselab.mooctest.device.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class UINodeVO {

    private Map<String, String> attributes = new LinkedHashMap<>();
    private int xPosition;
    private int yPosition;
    private int width;
    private int height;
    private boolean hasBounds;

    public UINodeVO() {
    }

    public UINodeVO(int xPosition, int yPosition, int width, int height) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public int getxPosition() {
        return xPosition;
    }

    public void setxPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public void setyPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHasBounds() {
        return hasBounds;
    }

    public void setHasBounds(boolean hasBounds) {
        this.hasBounds = hasBounds;
    }
}

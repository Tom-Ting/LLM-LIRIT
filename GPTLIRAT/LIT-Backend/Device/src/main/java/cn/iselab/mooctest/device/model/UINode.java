package cn.iselab.mooctest.device.model;

import java.util.*;

public class UINode {

    private final Map<String, String> mAttributes = new LinkedHashMap<>();
    private UINode mParent;
    private List<UINode> mChildren = new ArrayList<>();

    public UINode getParent() {
        return mParent;
    }

    public void setParent(UINode mParent) {
        this.mParent = mParent;
    }

    public List<UINode> getChildren() {
        return Collections.unmodifiableList(mChildren);
    }

    public void setChildren(List<UINode> mChildren) {
        this.mChildren = mChildren;
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(mAttributes);
    }

    public boolean hasChild() {
        return !mChildren.isEmpty();
    }

    public int getChildCount() {
        return mChildren.size();
    }

    public void clearAllChildren() {
        for (UINode child : mChildren) {
            child.clearAllChildren();
        }
        mChildren.clear();
    }

    public void addChild(UINode child) {
        if (child == null) {
            throw new NullPointerException("Cannot add null child");
        }
        if (mChildren.contains(child)) {
            throw new IllegalArgumentException("node is already a child");
        }
        mChildren.add(child);
        child.mParent = this;

    }

    public void addAtrribute(String key, String value) {
        mAttributes.put(key, value);
    }

    public String getAttribute(String key) {
        return mAttributes.get(key);
    }

    public String getXpath() {
        String className = getAttribute("class");
        String resourceId = getAttribute("resource-id");
        if (resourceId != null && !resourceId.equals("")) {
            return "//" + className + "[@resource-id='" + resourceId + "']";
        }
        String xpath = mParent.getAttribute("xpath") + "/";
        xpath += className;
        String text = getAttribute("text");
        String contentDesc = getAttribute("content-desc");
        if (text != null && !text.equals("")) {
            xpath += "[@text='" + text + "']";
            return xpath;
        } else if (contentDesc != null && !contentDesc.equals("")) {
            xpath += "[@content-desc='" + contentDesc + "']";
            return xpath;
        } else {
            xpath += "[" + getAttribute("index") + "]";
            return xpath;
        }
    }
}

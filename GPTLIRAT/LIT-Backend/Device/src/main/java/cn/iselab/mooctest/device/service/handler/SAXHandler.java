package cn.iselab.mooctest.device.service.handler;

import cn.iselab.mooctest.device.model.UINode;
import cn.iselab.mooctest.device.model.UINodeVO;
import cn.iselab.mooctest.device.wrapper.UINodeVOWrapper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsx on 2018/12/18.
 */
public class SAXHandler extends DefaultHandler {

    private UINode mRootNode;
    private UINode mParentNode;
    private UINode mWorkingNode;
    private List<UINode> nodeList = new ArrayList<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        mParentNode = mWorkingNode;
        UINode tmpNode = new UINode();
        if("hierarchy".equals(qName) || "node".equals(qName)) {
            for (int i = 0; i < attributes.getLength(); i++) {
                tmpNode.addAtrribute(attributes.getQName(i), attributes.getValue(i));
            }
            mWorkingNode = tmpNode;
        }
        if (mRootNode == null) {
            mRootNode = mWorkingNode;
            mRootNode.addAtrribute("xpath", "/");
        }
        if (mParentNode != null) {
            mParentNode.addChild(mWorkingNode);
            mWorkingNode.addAtrribute("xpath", mWorkingNode.getXpath());
            nodeList.add(mWorkingNode);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (mParentNode != null) {
            // closing an element implies that we are back to working on
            // the parent node of the element just closed, i.e. continue to
            // parse more child nodes
            mWorkingNode = mParentNode;
            mParentNode = mParentNode.getParent();
        }
    }

    public UINode getRootNode() {
        return mRootNode;
    }

    public List<UINodeVO> getNodeList() {
        return new UINodeVOWrapper().wrap(nodeList);
    }
}

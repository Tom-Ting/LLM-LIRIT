package cn.iselab.mooctest.device.wrapper;

import cn.iselab.mooctest.device.model.UINode;
import cn.iselab.mooctest.device.model.UINodeVO;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UINodeVOWrapper extends BaseWrapper<UINodeVO, UINode> {

    private static final Pattern BOUNDS_PATTERN = Pattern.compile("\\[-?(\\d+),-?(\\d+)\\]\\[-?(\\d+),-?(\\d+)\\]");

    @Override
    public UINodeVO wrap(UINode uiNode) {
        UINodeVO uiNodeVO = new UINodeVO();
        uiNodeVO.setAttributes(uiNode.getAttributes());
        String bounds = uiNode.getAttribute("bounds");
        if (bounds != null) {
            Matcher m = BOUNDS_PATTERN.matcher(bounds);
            if (m.matches()) {
                uiNodeVO.setxPosition(Integer.parseInt(m.group(1)));
                uiNodeVO.setyPosition(Integer.parseInt(m.group(2)));
                uiNodeVO.setWidth(Integer.parseInt(m.group(3)) - uiNodeVO.getxPosition());
                uiNodeVO.setHeight(Integer.parseInt(m.group(4)) - uiNodeVO.getyPosition());
                uiNodeVO.setHasBounds(true);
            } else {
                uiNodeVO.setHasBounds(false);
                log.error("Invalid bounds:{}" + bounds);
            }
        }
        return uiNodeVO;
    }

    @Override
    public UINode unwrap(UINodeVO data) {
        return null;
    }
}

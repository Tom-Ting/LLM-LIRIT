package cn.iselab.mooctest.device.util;

import cn.iselab.mooctest.device.model.UINodeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

public class ImageUtil {
    private static final Logger log = LoggerFactory.getLogger(ImageUtil.class);

    public static void cutImg(String inputImagePath, String outputImagePath, UINodeVO uiNode) {
        FileInputStream fis = null;
        ImageInputStream iis = null;
        try {
            fis = new FileInputStream(inputImagePath);
            Iterator<ImageReader> imgIterator = ImageIO.getImageReadersByFormatName("png");
            ImageReader imageReader = imgIterator.next();
            iis = ImageIO.createImageInputStream(fis);
            imageReader.setInput(iis, true);
            ImageReadParam param = imageReader.getDefaultReadParam();
            Rectangle rectBound = new Rectangle(uiNode.getxPosition(), uiNode.getyPosition(), uiNode.getWidth(), uiNode.getHeight());
            param.setSourceRegion(rectBound);
            BufferedImage bufferedImage = imageReader.read(0, param);
            ImageIO.write(bufferedImage, "png", new File(outputImagePath));

        } catch (Exception e) {
            log.error("cutImg error:{}", e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    log.error("FileInputStream close error:{}", e.getMessage());
                }
            }
            if (iis != null) {
                try {
                    iis.close();
                } catch (Exception e) {
                    log.error("ImageInputStream close error:{}", e.getMessage());
                }
            }
        }
    }

}

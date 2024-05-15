package cn.iselab.mooctest.device.service.handler;

import cn.iselab.mooctest.device.common.enums.Schema;
import cn.iselab.mooctest.device.model.Command;
import cn.iselab.mooctest.device.service.DeviceManagementService;
import cn.iselab.mooctest.device.util.CommandUtil;
import com.android.ddmlib.IDevice;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * Created by zsx on 2018/12/6.
 */
public class AndroidControlHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private Logger log = LoggerFactory.getLogger(getClass());

    private String deviceSerialNumber;

    private OutputStream outputStream;

    public AndroidControlHandler(String deviceSerialNumber, OutputStream outputStream) {
        this.deviceSerialNumber = deviceSerialNumber;
        this.outputStream = outputStream;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);
        Command command = new Command(new String(result));
        if(command.getSchema().equals(Schema.KEYEVENT)) {
            IDevice device = DeviceManagementService.getInstance().getIDevice(deviceSerialNumber);
            if(device != null) {
                CommandUtil.executeShellCommand(device, command.getContent());
            }
        } else if (command.getSchema().equals(Schema.MINITOUCH)){
            outputStream.write(command.getContent().getBytes());
            outputStream.flush();
            String endCommand = "c\n";
            outputStream.write(endCommand.getBytes());
            outputStream.flush();
        } else {
            log.info("undefined command");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String text = "serialNo:" + deviceSerialNumber;
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(text.getBytes()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("NIO channel exception", cause);
        ctx.close();
    }
}

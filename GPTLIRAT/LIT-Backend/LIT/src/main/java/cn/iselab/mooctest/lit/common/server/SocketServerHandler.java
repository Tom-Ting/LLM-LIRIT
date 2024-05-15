package cn.iselab.mooctest.lit.common.server;

import cn.iselab.mooctest.lit.util.DeviceChannelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);
        String serialNo = DeviceChannelUtil.getDeviceSerialNoByChannel(ctx.channel());
        if (serialNo == null) {
            String resultStr = new String(result);
            if (resultStr.startsWith("serialNo:")) {
                DeviceChannelUtil.putOrUpdateDevice2Channel(resultStr.replace("serialNo:", ""), ctx.channel());
            }
        } else {
            Channel channel = DeviceChannelUtil.getWSChannelByDevice(serialNo);
            if (channel != null) {
                channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(result)));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Socket NIO channel exception", cause);
        ctx.close();
    }
}

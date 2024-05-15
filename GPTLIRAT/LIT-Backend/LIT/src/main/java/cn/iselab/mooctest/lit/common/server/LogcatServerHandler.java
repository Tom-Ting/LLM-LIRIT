package cn.iselab.mooctest.lit.common.server;

import cn.iselab.mooctest.lit.util.DeviceChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogcatServerHandler extends SimpleChannelInboundHandler<String> {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        String serialNo = DeviceChannelUtil.getDeviceSerialNoByLogChannel(ctx.channel());
        if (serialNo == null) {
            if (msg.startsWith("serialNo:")) {
                DeviceChannelUtil.putOrUpdateDevice2LogChannel(msg.replace("serialNo:", ""), ctx.channel());
            }
        } else {
            Channel channel = DeviceChannelUtil.getLogWSChannelByDevice(serialNo);
            if (channel != null) {
                channel.writeAndFlush(new TextWebSocketFrame(msg));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("log Socket NIO channel exception", cause);
        ctx.close();
    }
}

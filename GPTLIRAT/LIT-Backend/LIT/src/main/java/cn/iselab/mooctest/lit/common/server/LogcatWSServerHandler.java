package cn.iselab.mooctest.lit.common.server;

import cn.iselab.mooctest.lit.util.DeviceChannelUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogcatWSServerHandler extends SimpleChannelInboundHandler<Object> {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        log.info("Log: {}", msg);
        if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("logcat Websocket NIO channel exception", cause);
        ctx.close();
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            log.info("Log: {}", text);
            if (text.startsWith("device://")) {
                DeviceChannelUtil.putOrUpdateDevice2LogWSChannel(text.replace("device://", ""), ctx.channel());
            }
        }
    }
}

package cn.iselab.mooctest.device.service.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zsx on 2019/1/10.
 */
public class LogHandler extends SimpleChannelInboundHandler<String> {
    private Logger log = LoggerFactory.getLogger(getClass());

    private String deviceSerialNumber;

    public LogHandler(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String text = "serialNo:" + deviceSerialNumber;
        ctx.channel().writeAndFlush(text);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("log NIO channel exception", cause);
        ctx.close();
    }
}

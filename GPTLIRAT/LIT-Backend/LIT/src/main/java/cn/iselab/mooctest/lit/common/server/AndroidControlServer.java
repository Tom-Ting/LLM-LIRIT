package cn.iselab.mooctest.lit.common.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

/**
 * A server to receive screenshots and device log data from an eureka
 * service {@code DEVICE} and send them out to corresponding websocket channels.
 */
public class AndroidControlServer {

    private Logger log = LoggerFactory.getLogger(getClass());

    private boolean isSocketStart = false;

    private boolean isWebSocketStart = false;

    private boolean isLogSocketStart = false;

    private boolean isLogWebSocketStart = false;

    private AndroidControlServer() {

    }

    public static AndroidControlServer getInstance() {
        return AndroidControlServerInstance.INSTANCE;
    }

    public boolean isSocketStart() {
        return isSocketStart;
    }

    public boolean isWebSocketStart() {
        return isWebSocketStart;
    }

    public boolean isLogSocketStart() {
        return isLogSocketStart;
    }

    public boolean isLogWebSocketStart() {
        return isLogWebSocketStart;
    }

    /**
     * Start the screenshots receiver socket at {@code localhost:1800}.
     * This socket will be connected to by a screenshots sender socket in
     * the eureka service {@code DEVICE} and receive screenshots
     * data in real time. It consumes this screenshots data by sending it
     * over to a websocket channel bounded to a certain device. The websocket
     * is at {@code ws://localhost:1801/ws}.
     *
     * <br>To manage devices with their socket channels, each device
     * should send a message with exactly the content
     * {@literal "serialNo:<serialNo>"} where {@literal <serialNo>}
     * should be replaced by the real serial number of your device before
     * any further data transmission.
     *
     * @see #startWebSocket()
     */
    public void startSocket() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1000)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline ch = socketChannel.pipeline();
                            ch.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            ch.addLast(new LengthFieldPrepender(4));
                            ch.addLast(new SocketServerHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(1800).sync();
            isSocketStart = true;
            log.info("Socket started.");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("AndroidControlServer socket InterruptedException", e);
            Thread.currentThread().interrupt();
        } finally {
            isSocketStart = false;
            log.info("Socket closing.");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * Start screenshots websocket at {@code ws://localhost:1801/ws}.
     * It receives the screenshots data from port 1800 on localhost and sends
     * the data to its clients respectively.
     *
     * <br>To manage devices with their websocket channels, each client should
     * send a message with exactly the content {@literal "device://<serialNo>"}
     * where {@literal <serialNo>} should be replaced by the real serial number
     * of your device before any further data transmission.
     *
     * @see #startSocket()
     */
    public void startWebSocket() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1000)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline ch = socketChannel.pipeline();
                            ch.addLast("httpServerCodec", new HttpServerCodec());
                            ch.addLast("chunkedWriteHandler", new ChunkedWriteHandler());
                            ch.addLast("httpObjectAggregator", new HttpObjectAggregator(65536));
                            ch.addLast("webSocketServerProtocolHandler", new WebSocketServerProtocolHandler("/ws"));
                            ch.addLast("myWebSocketHandler", new WSSocketServerHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(1801).sync();
            isWebSocketStart = true;
            log.info("Web socket started.");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("AndroidControlServer websocket InterruptedException", e);
            Thread.currentThread().interrupt();
        } finally {
            isWebSocketStart = false;
            log.info("Web socket closing.");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * Start a device log receiver socket at {@code localhost:1802}.
     * This socket will be connected to by a device log sender socket in the
     * eureka service {@code device-service} and receive device log data in
     * real time. It consumes the data by passing it over to a websocket
     * channel bounded to a certain device. The websocket is at
     * {@code ws://localhost:1803/ws}.
     *
     * <br>To manage devices with their socket channels, each device
     * should send a message with exactly the content
     * {@literal "serialNo:<serialNo>"} where {@literal <serialNo>}
     * should be replaced by the real serial number of your device before
     * any further data transmission.
     * <p>
     * <br/><br/><b>Documented by</b>: Yexiao Yun
     *
     * @see #startLogWebSocket()
     */
    public void startLogSocket() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1000)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline ch = socketChannel.pipeline();
                            ch.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            ch.addLast(new LengthFieldPrepender(4));
                            ch.addLast(new StringEncoder());
                            ch.addLast(new StringDecoder());
                            ch.addLast(new LogcatServerHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(1802).sync();
            isLogSocketStart = true;
            log.info("Log socket started.");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("AndroidControlServer log socket InterruptedException", e);
            Thread.currentThread().interrupt();
        } finally {
            isLogSocketStart = false;
            log.info("Log socket closing.");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * Start the device log websocket at {@code ws://localhost:1803/ws}.
     * It receives the device log data from port 1802 on localhost and sends
     * the data to its clients respectively.
     *
     * <br>To manage devices with their websocket channels, each client should
     * send a message with exactly the content {@literal "device://<serialNo>"}
     * where {@literal <serialNo>} should be replaced by the real serial number
     * of your device before any further data transmission.
     * <p>
     * <br/><br/><b>Documented by</b>: Yexiao Yun
     *
     * @see #startLogSocket()
     */
    public void startLogWebSocket() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1000)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline ch = socketChannel.pipeline();
                            ch.addLast("httpServerCodec", new HttpServerCodec());
                            ch.addLast("chunkedWriteHandler", new ChunkedWriteHandler());
                            ch.addLast("httpObjectAggregator", new HttpObjectAggregator(65536));
                            ch.addLast("webSocketServerProtocolHandler", new WebSocketServerProtocolHandler("/ws"));
                            ch.addLast("myWebSocketHandler", new LogcatWSServerHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(1803).sync();
            isLogWebSocketStart = true;
            log.info("Log web socket started.");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("AndroidControlServer log websocket InterruptedException", e);
            Thread.currentThread().interrupt();
        } finally {
            isLogWebSocketStart = false;
            log.info("Log web socket closing.");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class AndroidControlServerInstance {
        private static final AndroidControlServer INSTANCE = new AndroidControlServer();
    }
}

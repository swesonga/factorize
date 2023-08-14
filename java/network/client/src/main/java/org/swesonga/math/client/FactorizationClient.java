package org.swesonga.math.client;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.SocketChannel;

public class FactorizationClient {
    private final String host;
    private final int port;
    private final String number;

    public FactorizationClient(String host, int port, String number) {
        this.port = port;
        this.host = host;
        this.number = number;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: " + FactorizationClient.class.getSimpleName() + " <host> <port> [number]");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        String number = args.length > 2 ? args[2] : "";
        new FactorizationClient(host, port, number).start();
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            var clientBootstrap = new Bootstrap();
            
            clientBootstrap
                .group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new FactorizationClientHandler(number));
                    }
                });

            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
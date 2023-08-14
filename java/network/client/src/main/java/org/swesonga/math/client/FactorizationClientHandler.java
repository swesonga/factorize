package org.swesonga.math.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.math.BigInteger;

import org.swesonga.math.FactorizationUtils;
import org.swesonga.math.Factorize;

@Sharable
public class FactorizationClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        long seed = 0;
        int bytes = 12;
        byte[] array = FactorizationUtils.getRandomBytes(seed, bytes);
        var number = new BigInteger(array).abs();

        String numberAsString = number.toString();

        // Send number to the server when the channel becomes active
        ctx.writeAndFlush(Unpooled.copiedBuffer(numberAsString, CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        String message = in.toString(CharsetUtil.UTF_8);
        System.out.println("Factorization Results: " + message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

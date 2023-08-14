package org.swesonga.math.client;

import java.math.BigInteger;
import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

@Sharable
public class FactorizationClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final String number;
    private final boolean hasNumber;

    public FactorizationClientHandler(String number) {
        this.number = number;
        this.hasNumber = number != null && number.length() > 0 && !number.equals("0");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String numberAsString = hasNumber ? number : RandomPayloadGenerator.generateRandomNumber();
        
        // Send number to the server when the channel becomes active
        System.out.println("Sending " + numberAsString);
        ctx.writeAndFlush(Unpooled.copiedBuffer(numberAsString, CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        String message = in.toString(CharsetUtil.UTF_8);

        String[] factorsAsStrings = message.split(",");
        var factorsStream = Arrays.stream(factorsAsStrings).map(x -> new BigInteger(x));
        var product = factorsStream.reduce(BigInteger.ONE, (a, b) -> a.multiply(b));

        System.out.println("Prime Factors: " + message);
        System.out.println("Product of Prime Factors: " + product);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

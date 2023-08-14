package org.swesonga.math.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

@Sharable
public class FactorizationServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf messageBytes = (ByteBuf)msg;
        String input = messageBytes.toString(CharsetUtil.UTF_8);

        String result;
        try {
            result = FactorizationService.factorize(input);
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
            result = "The computation was interrupted";
        }

        ByteBuf byteBuf = Unpooled.buffer();
        int bytesWritten = ByteBufUtil.writeUtf8(byteBuf, result);

        ctx.write(byteBuf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
            .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

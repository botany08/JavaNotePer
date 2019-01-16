package com.lin.nettyDemo.nettyBeginner;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/*	
 *  ChannelInboundHandlerAdapter是 ChannelInboundHandler. ChannelInboundHandler的实现。
 *  提供可以覆盖的各种事件处理程序方法。只需要扩展ChannelInboundHandlerAdapter即可，而不是自己实现处理程序接口。
 * */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {
	/* 1.事件触发的read方法
	 * 重写通道读取channelRead（）事件处理方法。
	 * 每当从客户端收到新数据时，都会使用接收到的消息调用此方法。
	 * 为了实现DISCARD协议，处理程序必须忽略收到的消息。
	 * */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// ByteBuf是一个引用计数对象，必须通过release（）方法显式释放。
		// 处理程序有责任释放传递给处理程序的引用计数对象。
//		((ByteBuf) msg).release();
		ByteBuf inByteBuf = (ByteBuf) msg;
		try {
            while (inByteBuf.isReadable()) {
                System.out.print((char) inByteBuf.readByte());
                System.out.flush();
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
	}
	
	/* 2.异常触发的方法
	 * 当由于 I / O 错误或由于在处理事件时抛出异常而使得 Netty 抛出异常时，exceptionCaught() 事件将会被 Throwable 抛出。
	 * 在大多数情况下，应该记录捕获到的异常，并在此关闭其关联的通道。
	 * */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
	
}

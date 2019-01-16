package com.lin.nettyDemo.timeServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Monster_0522
 * Function:  实现 time 协议,发送一个包含 32 字节大小的 int 消息，不接受任何请求并且一旦发送消息马上关闭连接。
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {
	
	@Override
	//当产生了一个连接并且准备进行传输时channelActive 方法会被调用。
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// 为了发送一个新消息，我们需要分配一个新 buffer 缓冲区来存放消息。
		// 通过 ChannelHandlerContext.alloc() 得到一个当前的 ByteBufAllocator，然后分配一个新的缓冲。
		final ByteBuf time = ctx.alloc().buffer(4);
    	time.writeInt((int)(System.currentTimeMillis()/1000l+2208988800l));
    	
    	/*
    	 * ChannelHandlerContext.write（或 writeAndFlush）方法执行后会返回一个 ChannelFuture。
    	 * 一个 ChannelFuture 代表了还未发生的一个 IO 操作。任何请求操作可能还没有被执行，因为所有的操作在 netty 中都是一步的。
    	 * ChannelFuture 是调用 ChannelHandlerContext.write 方法返回的，并且它会通知他的监听器写操作已经完成了。
    	 * 需要在 ChannelFuture 完成之后调用 close 方法,close 方法可能不会马上关闭 connction，并且他也会返回一个 channelFuture 对象。
    	 */
    	final ChannelFuture future = ctx.writeAndFlush(time);
    	
    	//监听写入连接事件,作用是当写请求完成后通知我们 
    	future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				  assert future == f;
	              ctx.close();
			}
		});

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
	
}

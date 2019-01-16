package com.lin.nettyDemo.timeServer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TimeClient {
	public static void main(String[] args) throws InterruptedException {
		int port = 8080;
		
		//如果只声明了一个 EventLoopGroup，那么这个对象将即作为 boss group 又作为 worker group。
		//但是在客户端 boss group 中没有用到。
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			//Bootstrap 类似于 ServerBootstrap，区别是它是一个非服务端 channels 而言，比如一个客户端或者非连接 channel。
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(workerGroup);
			// NioServerSocketChannel创建服务端程序，NioSocketChannel 创建客户端程序。
			bootstrap.channel(NioSocketChannel.class);
			//不用 childOption 方法，不像我们处理 ServerBooststrap 一样。
			//因为客户端 SocketChannel 没有一个父类。
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel channel) throws Exception {
					
					//添加业务处理器
					channel.pipeline().addLast(new TimeClientHandler());
				}
			});
			
			//调用connect方法连接服务端
			//服务端则是绑定端口
	    	ChannelFuture future = bootstrap.connect("localhost", port);
	    	
	    	//等待知道连接被关闭
	    	future.channel().closeFuture().sync();
		}finally{
			workerGroup.shutdownGracefully();
		}
	}

}

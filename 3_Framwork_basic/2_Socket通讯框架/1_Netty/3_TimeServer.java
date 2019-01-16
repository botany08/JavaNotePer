package com.lin.nettyDemo.timeServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TimeServer {
	
	private int port;

	public TimeServer(int port){
		this.port = port;
	}
	
	public static void main(String[] args) {
		int port = 8080;
		try {
			//开启服务端接受消息
			new TimeServer(port).run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 开启服务器
	 * @author 
	 * @throws InterruptedException 
	 */
	public void run() throws InterruptedException{
		//1.开启时间处理线程
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			//2.设置服务器
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
				//NioServerSocketChannel用来创建服务端程序
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
 
				@Override
				protected void initChannel(SocketChannel channel) throws Exception {
					channel.pipeline().addLast(new TimeServerHandler());
				}
			})
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);
			
			//3.绑定端口并启动服务器
			ChannelFuture future = bootstrap.bind(port).sync();
			future.channel().closeFuture().sync();
		}finally{
			
			//优雅的关闭
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

}

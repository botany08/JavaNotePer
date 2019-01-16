package com.lin.nettyDemo.nettyBeginner;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class DiscardServer {
    private int port;
    //初始化接口属性
    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws InterruptedException {
    	/*
    	 * 1.创建多线程时间循环
    	 * NioEventLoopGroup 是一个处理 I / O 操作的多线程事件循环。
    	 * Netty 为不同类型的传输提供了各种 EventLoopGroup 实现。
    	 * 第一个，通常称为 “老板”，接受传入的连接。
    	 * 第二个，通常称为 “工人”，一旦老板接受连接并将接受的连接注册给工作人员，就处理接受的连接的流量。
    	 * 使用多少线程以及它们如何映射到创建的通道取决于 EventLoopGroup 实现，甚至可以通过构造函数进行配置。
    	 * */
        EventLoopGroup bossGroup = new NioEventLoopGroup(); 
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
        	/* 2.设置服务器
        	 * ServerBootstrap 是一个帮助类，用于设置服务器。
        	 * 可以直接使用 Channel 设置服务器。但是请注意，这是一个繁琐的过程，在大多数情况下不需要这样做。
        	 * */
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
            		//指定使用 NioServerSocketChannel 类来实例化一个新的 Channel 来接受传入的连接。
                    .channel(NioServerSocketChannel.class) 
                     //ChannelInitializer 是一个特殊的处理程序，旨在帮助用户配置新的 Channel。
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    /*	
                     * 设置特定于 Channel 实现的参数。
                     * 一个 TCP / IP 服务器，可以设置套接字选项，如 tcpNoDelay 和 keepAlive
                     * option（）用于接受传入连接的 NioServerSocketChannel，用于配置父级Channel。
                     * childOption（）用于在这种情况下由父级 ServerChannel 接受的通道，用于配置子级Channel。
                     */
                    .option(ChannelOption.SO_BACKLOG, 128)          
                    .childOption(ChannelOption.SO_KEEPALIVE, true); 
            
            //3.启动服务器
            //绑定到端口并启动服务器
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync(); // (7)
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    
	public static void main(String[] args) throws InterruptedException {
		//初始化端口
		int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        //启动服务器
        new DiscardServer(port).run();
}
}

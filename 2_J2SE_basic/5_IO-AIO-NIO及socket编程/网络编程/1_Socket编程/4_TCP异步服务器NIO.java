package com.lin.springDemo.socket.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;


/**
 * @author Monster_0522
 * Function: 异步服务器
 */
public class ToUpperTCPNonBlockServer {
	//服务器IP
	public static final String SERVER_IP = "127.0.0.1";
	//服务器端口号
	public static final int SERVER_PORT = 10005;
	//请求终结字符串
	public static final char REQUEST_END_CHAR = '#';
	
	/**
	 * 启动服务器
	 * @param serverIP
	 * @param serverPort
	 * @throws IOException
	 */
	public void startServer(String serverIP, int serverPort) throws IOException {
		//使用NIO需要用到ServerSocketChannel
		//其中包含一个ServerSocket对象
		//新的服务器使用ServerSocketChannel和SocketChannel
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		//1.获取地址对象，服务器绑定地址，设置为非阻塞
		//创建地址对象
		InetSocketAddress localAddr = new InetSocketAddress(serverIP, serverPort);
		//服务器绑定地址
		serverChannel.bind(localAddr);
		//设置为非阻塞
		serverChannel.configureBlocking(false);
		
		//注册到selector，会调用ServerSocket的accept
		//我们用selector监听accept能否返回
		//当调用accept可以返回时，会得到通知
		//注意，是可以返回，还需要调用accept
		//2.在服务器中注册selector
		Selector selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		while (true) {
			//3.监听接口，直到获取到满足条件的channel
			//调用select，阻塞在这里，直到有注册的channel满足条件
			selector.select();
			//4.获取到迭代器，遍历满足条件的keys
			//如果走到这里，有符合条件的channel
			//可以通过selector.selectedKeys().iterator()拿到符合条件的迭代器
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			//遍历keys
			//处理满足条件的keys
			while (keys.hasNext()) {
				//取出一个key并移除
				SelectionKey key = keys.next();
				keys.remove();
				try {
					//5.如果有连接则建立TCP连接，将channel注册到selector中
					if (key.isAcceptable()) {
						//有accept可以返回
						//取得可以操作的channel
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						
						//调用accept完成三次握手，返回与客户端可以通信的channel
						SocketChannel channel = server.accept();
						
						//将该channel置非阻塞
						channel.configureBlocking(false);
						
						//channel注册进selector，当可读或可写时将得到通知，select返回
						channel.register(selector, SelectionKey.OP_READ);
					} 
					//6.如果channel有内容传输，则取出相应的channel
					else if (key.isReadable()) {
						//有channel可读,取出可读的channel
						SocketChannel channel = (SocketChannel) key.channel();
						
						//创建读取缓冲区,一次读取1024字节
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						channel.read(buffer);
						
						//锁住缓冲区，缓冲区使用的大小将固定
						buffer.flip();
						
						//附加上buffer，供写出使用
						key.attach(buffer);
						key.interestOps(SelectionKey.OP_WRITE);
					} 
					//7.如果channel有内容要写进，则取出相应的channel
					else if (key.isWritable()) {
						//有channel可写,取出可写的channel
						SocketChannel channel = (SocketChannel) key.channel();
						
						//取出可读时设置的缓冲区
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						
						//将缓冲区指针移动到缓冲区开始位置
						buffer.rewind();
						
						//读取为String
						String recv = new String(buffer.array());
						
						//清空缓冲区
						buffer.clear();
						buffer.flip();
						
						//写回数据
						byte[] sendBytes = recv.toUpperCase().getBytes();
						channel.write(ByteBuffer.wrap(sendBytes));
 
                        //变为等待读
                        key.interestOps(SelectionKey.OP_READ);
					}
				} catch (IOException e) {
					//当客户端Socket关闭时，会走到这里，清理资源
					key.cancel();
					try {
						key.channel().close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		ToUpperTCPNonBlockServer server = new ToUpperTCPNonBlockServer();
		try {
			server.startServer(SERVER_IP, SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

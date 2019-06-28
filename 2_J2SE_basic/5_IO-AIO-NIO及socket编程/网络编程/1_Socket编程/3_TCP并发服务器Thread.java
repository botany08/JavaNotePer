package com.lin.springDemo.socket.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Monster_0522
 * Function: 并发服务器
 */
public class ToUpperTCPThreadServer {
	//服务器IP
	public static final String SERVER_IP = "127.0.0.1";
	//服务器端口号
	public static final int SERVER_PORT = 10005;
	//请求终结字符串
	public static final char REQUEST_END_CHAR = '#';
	
	/**
	 * 启动服务器
	 * @param serverIP	服务器IP 无需指定系统自动分配
	 * @param serverPort 服务器端口号
	 */
	public void startServer(String serverIP, int serverPort) {
		//创建服务器地址对象
		InetAddress serverAddr;
		try {
			serverAddr = InetAddress.getByName(serverIP);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			return;
		}
		
		//Java提供了ServerSocket作为服务器
		//这里使用了Java的自动关闭的语法，很好用
		try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT, 5, serverAddr)) {
			//创建并初始化线程池
			Executor executor = Executors.newFixedThreadPool(100);
			while (true) {
				StringBuilder recvStrBuilder = new StringBuilder();
				
				try {
					//有客户端向服务器发起tcp连接时，accept会返回一个Socket
					//该Socket的対端就是客户端的Socket
					//具体过程可以查看TCP三次握手过程
					Socket connection = serverSocket.accept();
					
					//利用线程池，启动线程
					executor.execute(new Runnable() {
						@Override
						public void run() {
							//使用局部引用，防止connection被回收
							Socket conn = connection;
							try {
								InputStream in = conn.getInputStream();
								
								//读取客户端的请求字符串，请求字符串以#终结
								for (int c = in.read(); c != REQUEST_END_CHAR; c = in.read()) {
									recvStrBuilder.append((char)c);
								}
								recvStrBuilder.append('#');
								
								String recvStr = recvStrBuilder.toString();
								
								//向客户端写出处理后的字符串
								OutputStream out = conn.getOutputStream();
								out.write(recvStr.toUpperCase().getBytes());
							} catch (IOException e) {
								e.printStackTrace();
							} finally {
								try {
									if (conn != null) {
										conn.close();
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ToUpperTCPThreadServer server = new ToUpperTCPThreadServer();
		server.startServer(SERVER_IP, SERVER_PORT);
	}


}

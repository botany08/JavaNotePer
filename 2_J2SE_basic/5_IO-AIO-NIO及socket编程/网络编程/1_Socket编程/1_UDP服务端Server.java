package com.lin.springDemo.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ToUpperUDPServer {
	//服务器IP
	public static final String SERVER_IP = "127.0.0.1";
	//服务器端口号
	public static final int SERVER_PORT = 10005;
	//最多处理1024个字符
	public static final int MAX_BYTES = 1024;
	//UDP使用Datagramsocket发送数据包
	private DatagramSocket serverSocket;
	
	/**
	 * 启动服务器
	 * @param serverIp	服务器IP无需指定，系统自动分配
	 * @param serverPort 服务器监听的端口号
	 */
	public void startServer(String serverIp,int serverPort) {
		try {
			//创建Datagramsocket
			//getByName，通过主机名获取对应的IP地址
			InetAddress serverAddr = InetAddress.getByName(serverIp);
			//DatagramSocket类表示用于发送或接收数据包数据的套接字，UDP专用速度比较快
			//服务端端口有分配地址端口
			serverSocket = new DatagramSocket(serverPort,serverAddr);
			
			//创建数据接收对象
			byte[] recvBuf = new byte[MAX_BYTES];
			//DatagramPacket表示数据包
			DatagramPacket recvPacket = new DatagramPacket(recvBuf , recvBuf.length);
			
			//死循环，一直运行服务器
			while(true) {
				//接收数据，会在这里阻塞，直到有数据到来
				try {
					//服务器的监听端口不断的接收数据
					serverSocket.receive(recvPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//将收到的数据存到String类recvStr中
				String recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
				
				//拿到客户端的IP和接口
				//getAddress()表示 返回该数据包发送或接收数据报的计算机的IP地址
				InetAddress clientAddr = recvPacket.getAddress();
				//getPort()表示 返回发送数据报的远程主机上的端口号，或从中接收数据报的端口号
				int clientPort = recvPacket.getPort();
				
				//回传数据
				//将要传回的数据存到字节数组中
				String upperString = recvStr.toUpperCase();
				byte[] sendBuf = upperString.getBytes();
				
				//将数据打包放进sendPacket中
				DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length,clientAddr,clientPort);
				
				//利用serverSocket发送数据
				try {
					serverSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
		} finally {
			//关闭socket
			if(null != serverSocket) {
				serverSocket.close();
				serverSocket = null;
			}
		}
	}
	
	public static void main(String[] args) {
		ToUpperUDPServer server = new ToUpperUDPServer();
		//启动服务器
		server.startServer(SERVER_IP, SERVER_PORT);
	}

}

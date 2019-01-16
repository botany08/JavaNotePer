package com.lin.springDemo.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ToUpperUDPClient {
	
	//创建客户端UDP端口
	private DatagramSocket clientSocket;
	
	public String toUpperRemote(String serverIp,int serverPort,String str) {
		String recvStr = "";
		
		try {
			//创建客户端UDP Socket
			clientSocket = new DatagramSocket();
			
			//向服务器发送数据
			//将数据转化成字节数组
			byte[] sendBuf = str.getBytes();
			//获取服务端的IP地址
			InetAddress serverAddr = InetAddress.getByName(serverIp);
			//创建要发送的数据包
			DatagramPacket sendPacket = 
					new DatagramPacket(sendBuf, sendBuf.length, serverAddr, serverPort);
			
			//发送数据
			try {
				//clientSocket中表示客户端端口，没有分配端口
				//sendPacket数据包中包含有要发送的服务端地址和端口
				clientSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//创建要接收数据的数据包，可以理解为数据容器
			byte[] recvBuf = new byte[ToUpperUDPServer.MAX_BYTES];
			DatagramPacket recvPacket = 
					new DatagramPacket(recvBuf, recvBuf.length);
			
			//接收数据
			try {
				clientSocket.receive(recvPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//将接收到数据包拆解为String字符串
			recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
		} finally {
			//关闭端口
			if(null != clientSocket) {
				clientSocket.close();
				clientSocket = null;
			}
		}
		
		//返回收到的字符串
		return recvStr;
	}
	
	public static void main(String[] args) {
		ToUpperUDPClient client = new ToUpperUDPClient();
		String recvStr = client.toUpperRemote(ToUpperUDPServer.SERVER_IP, ToUpperUDPServer.SERVER_PORT, "aaaAAAbbbBBBcccCCC");
		System.out.println("收到的数据："+recvStr);
	}

}

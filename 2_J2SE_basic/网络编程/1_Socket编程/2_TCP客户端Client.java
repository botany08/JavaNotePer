package com.lin.springDemo.socket.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ToUpperTCPClient {
		//客户端使用的TCP Socket
		private Socket clientSocket;
		
		/**
		 * @param serverIp		服务器IP
		 * @param serverPort	服务端接口
		 * @param str			输入的字符串
		 * @return
		 */
		public String toUpperRemote(String serverIp, int serverPort, String str) {
			StringBuilder recvStrBuilder = new StringBuilder();
			try {
				//1.建立TCP连接，创建连接服务器的Socket
				clientSocket = new Socket(serverIp, serverPort);
				
				//2.先建立输出流，写出请求字符串，发送给服务端
				OutputStream out = clientSocket.getOutputStream();
				out.write(str.getBytes());
				
				//3.建立输入流，读取服务器响应
				InputStream in = clientSocket.getInputStream();
				for (int c = in.read(); c != '#'; c = in.read()) {
					recvStrBuilder.append((char)c);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					//4.关闭TCP连接
					if (clientSocket != null) {
						clientSocket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return recvStrBuilder.toString();
		}
		
		public static void main(String[] args) {
			ToUpperTCPClient client = new ToUpperTCPClient();
			String recvStr = client.toUpperRemote(ToUpperTCPBlockServer.SERVER_IP, ToUpperTCPBlockServer.SERVER_PORT, 
					"aaaAAAbbbBBBcccCCC" + ToUpperTCPBlockServer.REQUEST_END_CHAR);
			System.out.println("收到:" + recvStr);
		}

}

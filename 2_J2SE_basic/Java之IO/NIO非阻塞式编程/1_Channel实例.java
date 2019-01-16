package com.lin.springDemo.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class NioChannel {
	public static void main(String[] args) {
		try {
			/** 
             * model 访问模式各个参数详解 
             * r 代表以只读方式打开指定文件 
             * rw 以读写方式打开指定文件 
             * rws 读写方式打开，并对内容或元数据都同步写入底层存储设备 
             * rwd 读写方式打开，对文件内容的更新同步更新至底层存储设备 
             *  
             * **/  
			//随机读取文件内容，第一个参数为文件路径，第二个参数为访问模式
			RandomAccessFile aFile = new RandomAccessFile("d:/data.txt", "rw");
			//创建文件读取通道Channel
			FileChannel inChannel = aFile.getChannel();
			//创建字节缓冲区
			ByteBuffer buf = ByteBuffer.allocate(48);
			//inChannel.read(buffer)返回读取的字节数
			int bytesRead = inChannel.read(buf);
			
			while (bytesRead != -1) {
			System.out.println("Read " + bytesRead);
			//通过 buffer.flip(); 这个语句，就能把 buffer 的当前位置更改为 buffer 缓冲区的第一个位置。
			buf.flip();
			//hasRemaining表示当前位置和最大长度间有没有元素
			while(buf.hasRemaining()){
			System.out.print((char) buf.get());
			}
			//清除缓冲区
			buf.clear();
			bytesRead = inChannel.read(buf);
			}
			
			//关闭文件
			aFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}

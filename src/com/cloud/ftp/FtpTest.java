package com.cloud.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
//import java.text.SimpleDateFormat;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;


/** 
* Created by IntelliJ IDEA. 
* 
* @author leizhimin 2008-9-12 10:32:39 
*/

public class FtpTest {
	public static Log logger = LogFactory.getLog(FtpTest.class);
	
	private static String userName; 
	private static String password;
	private static String ip;
	private static int port;
	private static Properties property = null; //属性集
	private static String configFile = "F:\\test\\comftp\\ftpconfig.properties"; //配置文件的路径名 
	private static FTPClient ftpClient = null; //FTP客户端代理
	//时间格式化
//	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
	//FTP状态码
	public static int STATUS = 1;
	
	public static void main(String args[]){
		connectServer();
		setFileType(FTP.BINARY_FILE_TYPE);//设置传输二进制文件
		uploadManyFile(new File( "F:\\test\\upLoadDir"), new File( "F:\\test\\upLoadDir"), "/home/chinacloud/uploadFile"); 
		closeConnect();//关闭连接
	}
	
	/**
	 * 上传单个文件 
	 * @param localFile 本地文件
	 * @param localRootFile 本地的根文件夹
	 * @param distFolder 远程目录
	 * @return 成功返回true，否则返回false.
	 */
	public static boolean uploadFile(File localFile,final File localRootFile,final String distFolder)
	{
		System.out.println("function uploadFile invoked");
		System.out.println("distFolder: " + distFolder);
		boolean flag = true;
		try{
			connectServer();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
			InputStream input = new FileInputStream(localFile);
			
			System.out.println("input: " + input.toString());
			// if (input == null) { 
			// System.out.println("本地文件"+localFile.getPath()+"不存在!"); 
			// } 
			// if (newFileName.trim().equals("")) { 
			// newFileName = localFile.getName(); 
			// } 
			System.out.println("localFile: " + localFile);
			System.out.println("localRootFile: " + localRootFile);
			String furi1 = localFile.getParentFile().getAbsoluteFile().toURI().toString();
			String furi2 = localRootFile.getParentFile().getAbsoluteFile().toURI().toString();
			
			System.out.println("furi1: " + furi1);
			System.out.println("furi2: " + furi2);
			
//			String objFolder = distFolder + File.separator + furi1.substring(furi2.length());
			String objFolder = distFolder + "/" + furi1.substring(furi2.length());
			
			ftpClient.changeWorkingDirectory("/home/chinacloud");
			System.out.println(ftpClient.makeDirectory(objFolder) );
			System.out.println("objFolder: " + objFolder);
			
			System.out.println("a>>>>>>>>:" + distFolder + File.separator + localFile.getParent());
			System.out.println("x>>>>>>>>:" + localFile.getPath() + ">>>>>>> " + ftpClient.printWorkingDirectory());
//			flag = ftpClient.storeFile(localFile.getName(), input);
			flag = ftpClient.storeFile(distFolder, input);
			if(flag){
				System.out.println("上传文件成功！ ");
			}else{
				System.out.println("上传文件失败！");
			}
			input.close();
		}catch(IOException e){
			e.printStackTrace();
			logger.debug("本地文件上传失败！", e);
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}

	public static String uploadManyFile(String localFile)
	{
		boolean flag = true;
		StringBuffer strBuf = new StringBuffer();
		try{
			connectServer();
			File file = new File(localFile);
			File fileList[] = file.listFiles();
			for(File f:fileList){
				if(f.isDirectory()){    //文件夹中还有文件夹
					uploadManyFile(f.getAbsolutePath());
				}else{
				}
				if(!flag){
					strBuf.append(f.getName() + "\r\n");
				}
			}
			System.out.println(strBuf.toString());
		}catch(NullPointerException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
			logger.debug("本地文件上传失败！", e);
		}
		return strBuf.toString();
	}
	
	
	
	/**
	 *上传多个文件 
	 * @param localFile--本地文件夹路径
	 * @param localRootFile--本地根文件
	 * @param distFolder--服务器文件夹目录
	 */
	private static String uploadManyFile(File localFile,final File localRootFile,final String distFolder) 
	{
		System.out.println("------------------");
		boolean flag = true;
		StringBuffer strBuf = new StringBuffer();
//		int n = 0;
		try{
			connectServer();
			ftpClient.makeDirectory(distFolder + File.separator + localFile.getParent());
			File fileList[] = localFile.listFiles();
			for(File upfile:fileList){
				if(upfile.isDirectory()){
					uploadManyFile(upfile, localRootFile, distFolder);
				}else{
					flag = uploadFile(upfile, localRootFile, distFolder);
				}
				if(!flag){
					strBuf.append(upfile.getName() + "\r\n");
				}
			}
			System.out.println(strBuf.toString());
		}catch(NullPointerException e){
			e.printStackTrace();
			logger.debug("本地文件上传失败！找不到上传文件",e);
		}catch(Exception e){
			e.printStackTrace();
			logger.debug("本地文件上传失败！", e); 
		}
		return strBuf.toString();
	}

	public static boolean loadFile(String remoteFileName, String localFileName)
	{
		boolean flag = true;
		connectServer();
		//下载文件
		BufferedOutputStream buffOut = null;
		try{
			buffOut = new BufferedOutputStream( new FileOutputStream(localFileName));
			flag = ftpClient.retrieveFile(remoteFileName, buffOut);
		}catch(Exception e){
			e.printStackTrace();
			logger.debug("本地文件下载失败！",e);
		}finally{
			try{
				if(buffOut != null)
					buffOut.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return flag;
	}

	/**
	 * 删除一个文件
	 * @param filename 待删除的文件名称
	 * @return
	 */
	public static boolean deleteFile(String filename)
	{
		boolean flag = true;
		try{
			connectServer();
			flag = ftpClient.deleteFile(filename);
			if(flag){
				System.out.println("删除文件成功！");
			}else{
				System.out.println("删除文件失败！");
			}
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 删除目录
	 * @param pathname 目录名称
	 */
	public static void delteleDirectory(String pathname)
	{
		try{
			connectServer();
			File file = new File(pathname);
			if(file.isDirectory()){
//				File file2[] = file.listFiles(); //?
			}else{
				deleteFile(pathname);
			}
			ftpClient.removeDirectory(pathname);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/**
	 * 删除空目录
	 * @param pathname
	 */
	public static void deleteEmptyDirectory(String pathname)
	{
		try{
			connectServer();
			ftpClient.removeDirectory(pathname);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	/**
	 * 列出服务器上文件和目录
	 * @param regStr--匹配的正则表达式
	 */
	public static void listRemoteFiles(String regStr)
	{
		connectServer();
		try{
			String files[] = ftpClient.listNames(regStr);
			if(files == null || files.length == 0)
				System.out.println("没有任何文件！");
			else{
				for(int i=0; i<files.length; i++){
					System.out.println(files[i]);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 列出Ftp服务器上的所有文件和目录
	 */
	public static void listRemoteAllFiles()
	{
		connectServer();
		try{
			String[] names = ftpClient.listNames();
			for(int i=0; i<names.length; i++){
				System.out.println(names[i]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 *设置配置文件 
	 * @param configFile--配置文件路径
	 */
	public static void setConfigFile(String configFile)
	{
		FtpTest.configFile = configFile;
	}
	
	/**
	 * 设置传输文件的类型[文本文件或者二进制文件]
	 * 
	 * @param fileType--BINARY_FILE_TYPE、ASCII_FILE_TYPE
	 */
	public static void setFileType(int fileType)
	{
		try{
			connectServer();
			ftpClient.setFileType(fileType);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 扩展使用
	 * @return ftpClient
	 */
	protected static FTPClient getFtpClient()
	{
		connectServer();
		return ftpClient;
	}
	
	/**
	 * 设置参数
	 * 
	 * @param configFile--参数的配置文件
	 */
	private static void setArg(String configFile)
	{
		property = new Properties();
		BufferedInputStream inBuff = null;
		try{
			File file = new File(configFile);
			inBuff = new BufferedInputStream(new FileInputStream(file));
			property.load(inBuff);
			userName = property.getProperty("username");
			password = property.getProperty("password");
			ip = property.getProperty("ip");
			port = Integer.parseInt(property.getProperty("port"));
		}catch(FileNotFoundException e){
			System.out.println("配置文件" + configFile + "不存在！");
		}catch(IOException e){
			System.out.println("配置文件" + configFile + "无法读取！");
		}
	}
	
	
	/**
	 * 连接到服务器
	 * 
	 * @return true 连接服务器成功; false 连接服务器失败
	 */
	private static boolean connectServer() 
	{
		boolean flag = true;
		if(ftpClient == null){
			int reply;
			try{
				setArg(configFile);
				ftpClient = new FTPClient();
				ftpClient.setControlEncoding("GBK");
				ftpClient.setDefaultPort(port);
				ftpClient.configure(getFtpConfig());
				ftpClient.connect(ip);
				ftpClient.login(userName, password);
				ftpClient.setDefaultPort(port);
				System.out.println(ftpClient.getReplyString());
				reply = ftpClient.getReplyCode();
				ftpClient.setDataTimeout(120000);
				
				if(!FTPReply.isPositiveCompletion(reply)){
					ftpClient.disconnect();
					System.err.println("FTP server refused connection");
					logger.debug("FTP服务器拒绝连接！");
					flag = false;
				}
				System.out.println(STATUS);
				STATUS++;
			}catch(SocketException e){
				flag = false;
				e.printStackTrace();
				System.err.println("登录ftp服务器" + ip + "失败，连接超时！");
			}catch(IOException e){
				flag = false;
				e.printStackTrace();
				System.err.println("登录ftp服务器" + ip + "失败，FTP服务器无法打开！");
			}
		}
		return flag;
	}
	
	/**
	 * 进入到服务器的某个目录下
	 * 
	 * @param directory 
	 */
	public static void changeWorkingDirectory(String directory)
	{
		try{
			connectServer();
			ftpClient.changeWorkingDirectory(directory);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/**
	 * 返回到上一层目录
	 */
	public static void changeToParentDirectory()
	{
		try{
			connectServer();
			ftpClient.changeToParentDirectory();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/**
	 * 重命名文件
	 * 
	 * @param oldFileName--原文件名
	 * @param newFileName--新文件名
	 */
	public static void renameFile(String oldFileName,String newFileName)
	{
		try{
			connectServer();
			ftpClient.rename(oldFileName, newFileName);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/**
	 * 设置FTP客户端的配置 -- 一般可以不设置
	 * @return
	 */
	private static FTPClientConfig getFtpConfig() 
	{
		FTPClientConfig ftpConfig = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
		ftpConfig.setServerLanguageCode(FTP.DEFAULT_CONTROL_ENCODING);
		return ftpConfig;
	}
	
	/**
	 * 转码[ISO-8859-1 -> GBK]不同的平台需要不同的转码
	 * 
	 * @param obj
	 * @return ""
	 */
//	private static String ios8895togbk(Object obj){
//		try{
//			if(obj == null)
//				return "";
//			else
//				return new String(obj.toString().getBytes("iso-8859-1"),"GBK");
//		}catch(Exception e){
//			return "";
//		}
//	}
	
	/**
	 * 在服务器上创建一个文件夹
	 * 
	 * @param dir 文件夹名称，不能含有特殊字符，如\ / : * ? 等
	 * @return
	 */
	public static boolean makeDirectory(String dir)
	{
		connectServer();
		boolean flag = true;
		try{
			System.out.println("dir =======" + dir);
			flag = ftpClient.makeDirectory(dir);
			if(flag){
				System.out.println("make Directory" + dir + "succeed");
			}else{
				System.out.println("make Directory" + dir + "false");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 关闭连接
	 */
	private static void closeConnect() 
	{
		try{
			if(ftpClient != null){
				ftpClient.logout();
				ftpClient.disconnect();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}



























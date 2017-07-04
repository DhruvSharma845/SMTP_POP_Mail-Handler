package ServerSMTP;

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ThreadedSMTPServerHandler implements Runnable
{
	private Socket incomingSock;
	private String serverAddress;
	private String clientAddress;

	private String mailBoxPath;
	private String receiverDirectory;

	private String sendermailbox;
	private String receivermailbox;

	private String senderNewMailFile;
	private String logFilePath;

	/**
	*converts client IP in string format and makes a directory in C drive with Mailboxes.
	*
	*@param  s -> IP address of Client
	*	ad -> string of server's IP address
	*        
	**/

	public ThreadedSMTPServerHandler(Socket s,String ad)
	{
		incomingSock=s;
		serverAddress=ad;
		clientAddress=incomingSock.getInetAddress().getHostAddress();
		
		//String currentDirectory=System.getProperty("user.dir");
		mailBoxPath="C:\\MailBoxes";

		File f=new File(mailBoxPath);
		if(!f.exists())
			f.mkdir();
	}
	
	/**
	* checks if client is connected...
	* if connected calls processRequest() 
	*       
	**/

	public void run()
	{
		//System.out.println("Connection request from: "+clientAddress); 
		if(incomingSock.isConnected()==true)
		{
			processRequest();
		}
	}
	
	/**
	* after establishing connection...perform all the funtions serially for processing the request from the client 
	*
	*/
	public void processRequest()
	{
		try
		{
			InputStream inStream=incomingSock.getInputStream();
			OutputStream outStream=incomingSock.getOutputStream();

			sendServerReady(outStream);
			if(processHelo(inStream)==false)
			{
				incomingSock.close();
				throw new UserException("HELO not received.");
			}
			sendSaysHelo(outStream);
			if(processMail(inStream)==false)
			{
				incomingSock.close();
				throw new UserException("MAIL FROM command not received.");
			}
			sendSenderOK(outStream);
			if(processRcpt(inStream)==false)
			{
				incomingSock.close();
				throw new UserException("RCPT TO command not received.");
			}
			sendReceiverOK(outStream);
			if(processData(inStream)==false)
			{
				incomingSock.close();
				throw new UserException("DATA command not received.");
			}
			sendReadyToAccept(outStream);
			boolean res=makeNewFile();
			if(res==false)
			{
				incomingSock.close();
				throw new UserException("New mail file not created.");
			}
			makeLogEntry();
			receiveMessage(inStream,outStream);
			sendMsgAccepted(outStream);
			if(checkQuitAccepted(inStream)==false)
			{
				incomingSock.close();
				throw new UserException("QUIT command not received.");
			}
			sendClosingMsg(outStream);
			System.out.println("Mail has been saved.");
			System.out.println("Waiting for new Connection....");
			incomingSock.close();
		}
		catch(IOException io_excep)
		{
			io_excep.printStackTrace();
		}
		catch(UserException u_excep)
		{
			u_excep.debugPrint();
		}
	}

	/**
	* sends a messege to client that the server is ready
	* @param o :object of OutputStream
	**/
	protected void sendServerReady(OutputStream o)
	{
		String ready="220 "+serverAddress+" SMTP Service Ready";
		PrintWriter outs=new PrintWriter(o,true);
		outs.println(ready);
	}
	
	/**
	* recieves HELO string from Client and Checks if the first four characters are HELO
	* 
	* @param in: object of InputStream
	*
	* @returns true if string is HELO.. else returns false
	*/
	protected boolean processHelo(InputStream in)
	{
		String helostring=receive(in);
		if(helostring.equals("HELO"))
			return true;
		return false;
	}
	
	/**
	* sends "250 server says helo" message to client
	*
	* @param out: object of OutputStream
	*/
	protected void sendSaysHelo(OutputStream out)
	{
		String line="250 "+serverAddress+" says hello to "+clientAddress;
		//System.out.println("Sending: "+line);
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(line);
	}
	
	/**
	* receives Mail message from Client and 
	*
	* @param in: object of InputStream
	*
	* @returns true if string is MAIL..and assigns Senders Username to sendermailbox 
	*		else returns false		
	*/
	protected boolean processMail(InputStream in)
	{
		String mailString=receiveFullString(in);
		System.out.println(mailString);
		String strtCmd=mailString.substring(0,4);
		if(!strtCmd.equals("MAIL"))
			return false;

		sendermailbox=mailString.substring(11,mailString.indexOf('@'));

		//System.out.println("Sender's MailBox: "+sendermailbox);

		return true;
	}
	
	/**
	* sends "250 sender ok" message to client  
	*
	* @param out: object of OutputStream
	*	
	*/
	protected void sendSenderOK(OutputStream out)
	{
		String line="250 sender ok";
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(line);
	}
	
	/**
	* Receives "RCPT" message from client 
	*
	* @param in: object of InputStream
	*
	* @returns true if fir first character are "RCPT" and makes "Receiver mail box" in MailBoxes directory
	* and a log file in Receiver mail box (if not exist)...
	*			else returns false
	*/
	protected boolean processRcpt(InputStream in) throws IOException
	{
		String mailString=receiveFullString(in);
		System.out.println(mailString);
		String strtCmd=mailString.substring(0,4);

		if(!strtCmd.equals("RCPT"))
			return false;

		receivermailbox=mailString.substring(9,mailString.indexOf('@'));

		//System.out.println("Receiver's MailBox: "+receivermailbox);

		//String currentDirectory=System.getProperty("user.dir");//.getAbsolutePath();
		receiverDirectory="C:\\MailBoxes\\"+receivermailbox;
		File file=new File(receiverDirectory);
		logFilePath=receiverDirectory+"\\logfile.txt";
		if(!file.exists())
		{
			file.mkdir();
			makeLogFile();
		}	
		
		return true;
	}
	
	/**
	* sends "250 recipient ok" message to Client
	* 
	* @param out: object of Outputstream
	*/
	protected void sendReceiverOK(OutputStream out)
	{
		String line="250 recipient ok";
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(line);
	}
		
	/**
	* Receives "DATA" message from client and Checks it.
	*
	* @param in: object of InputStream
	*
	* @returns true if string is "DATA" 
	*          else returns false
	*/
	
	protected boolean processData(InputStream in)
	{
		String mailString=receive(in);
		if(!mailString.equals("DATA"))
			return false;
		return true;
	}
	
	/**
	* sends "354 Send mail;end with <CRLF>.<CRLF> on a line" message to Client
	* 
	* @param out: object of Outputstream
	*/
	protected void sendReadyToAccept(OutputStream out)
	{
		String line="354 Send mail;end with <CRLF>.<CRLF> on a line";
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(line);
	}
	
	/**
	* Creates a new txt File with sender's username appending current time in sender's mail box
	*       and returns a File object 'f' pointing to the file created
	*
	* @returns true if file is created
	*       else returns false
	*/
	protected boolean makeNewFile() throws IOException
	{
		DateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
		Date date=new Date();
		String time=sdf.format(date);

		//String currentDirectory=System.getProperty("user.dir");

		senderNewMailFile=receiverDirectory+"\\"+sendermailbox+time+".txt";
		//System.out.println(senderNewMailFile);
		File f=new File(senderNewMailFile);
		return f.createNewFile();
	}
	
	/**
	* Makes a log entry for new mail at the top of the LogFile
	*       
	*/
	protected void makeLogEntry() throws IOException
	{	
		File f=new File(logFilePath);
		FileInputStream fis=new FileInputStream(f);
		BufferedReader br=new BufferedReader(new InputStreamReader(fis));
		
		String result="";
		String line="";
		while((line=br.readLine())!=null)
			result=result+"\n"+line;

		result=senderNewMailFile+result;
		f.delete();
		
		File f1=new File(logFilePath);
		f1.createNewFile();

		FileOutputStream fos=new FileOutputStream(f1);
		fos.write(result.getBytes());

		fos.flush(); 
	}
	
	/**
	* Receives whole message from client and writes it to the "senderNewMailFile"
	* It stops when <CRLF> . <CRLF> are received in three consecutive frames.
	* 
	* @param in: object of InputStream
	*      out: object of OutputStream
	*/
	
	protected void receiveMessage(InputStream in,OutputStream out) throws IOException
	{
		sendEmptyFrame(out);

		FileWriter fw=new FileWriter(new File(senderNewMailFile),true);
		BufferedWriter pw=new BufferedWriter(fw);
		PrintWriter bw=new PrintWriter(pw,true);

		
		String fromString=receiveFullString(in);
		bw.println(fromString);
		sendEmptyFrame(out);
               
		String toString=receiveFullString(in);
		bw.println(toString);
		sendEmptyFrame(out);
   
		
		String sbString=receiveFullString(in);
		bw.println(sbString);
		sendEmptyFrame(out);
              

		String content;
		while(true)
		{
			content=receiveFullString(in);
			sendEmptyFrame(out);
			bw.println(content);
			if(content.equals(""))
			{
				
				do{	content=receiveFullString(in);
					sendEmptyFrame(out);
					bw.println(content);
				}while(content.equals(""));
				if(content.equals("."))
				{
					content=receiveFullString(in);
					sendEmptyFrame(out);
					bw.println(content);
					if(content.equals(""))
						break;
					else
						continue;
				}
				else
					continue;	
			}
		}
		receiveEmptyFrame(in);
	}
	
	/**
	* Sends empty frame to the client
	*
	* @param out: object of OutputStream
	*
	*/
	protected void sendEmptyFrame(OutputStream out)
	{
		String x="";
		PrintWriter pw=new PrintWriter(out,true);
		pw.println(x);
	}
	
	/**
	* Receives empty frame from the client
	* 
	* @param in: object of InputStream
	*
	*/
	protected void receiveEmptyFrame(InputStream in)
	{
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();	
	}
	
	/**
	* sends "250 message accepted" to client
	*
	* @param out: object of OutputStream
	*
	*/
	protected void sendMsgAccepted(OutputStream out)
	{
		String line="250 message accepted";
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(line);
	}
	
	/**
	* Receives message from client and checks it
	*
	* @param in: object of InputStream
	*
	* @returns true if message received is "QUIT"
	*     else returns false
	*/
	protected boolean checkQuitAccepted(InputStream in)
	{
		String quitString=receive(in);
		if(quitString.equals("QUIT"))
			return true;
		return false;
	}
	
	/**
	* Sends "221 "+serverAddress+" closing connection" message to client
	*
	* @param out: object of OutputStream
	*
	*/
	protected void sendClosingMsg(OutputStream out)
	{
		String line="221 "+serverAddress+" closing connection";
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(line);
	}
	
	/**
	* Receives message from client and prints it
	*
	* @param in: object of InputStream 
	*
	* @returns a string of first 4 characters received
	*/
	protected String receive(InputStream in)
	{
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();
		System.out.println(line);
		String heloString=line.substring(0,4);
		return heloString;
	}
	
	/**
	* Receives message from client
	*
	* @param in: object of InputStream
	*
	* @returns the message received as a string
	*/
	protected String receiveFullString(InputStream in)
	{
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();
		return line;
	}
	
	/**
	* Creates a LogFile(if not exist)
	*
	*/
	protected void makeLogFile() throws IOException
	{
		File f=new File(logFilePath);
		f.createNewFile();
	}
	
	
}

class UserException extends Exception
{
	private String excep;
	
	public UserException(String s)
	{
		excep=s;
	}
	public void debugPrint()
	{
		System.out.println("Exception caught: "+excep);
	}
}
package ServerPOP;

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ThreadedPOPServerHandler implements Runnable
{
	private Socket incomingSock;
	private String serverAddress;
	private String clientAddress;

	private String mailBoxPath;

	private String username;
	private String password;

	private String authenticationFilePath;

	private String receiverDirectory;
	private int mailNo=1;

	private String sendermailbox;
	private String receivermailbox;

	private String senderNewMailFile;

	private String logFilePath;

	public ThreadedPOPServerHandler(Socket s,String ad)
	{
		incomingSock=s;
		serverAddress=ad;
		clientAddress=incomingSock.getInetAddress().getHostAddress();
		
		mailBoxPath="C:\\MailBoxes";
	}
	
	public void run()
	{
		//System.out.println("Connection request from: "+clientAddress); 
		if(incomingSock.isConnected()==true)
		{
			processRequest();
		}
	}
	
	protected void processRequest()
	{
		try
		{
			InputStream inStream=incomingSock.getInputStream();
			OutputStream outStream=incomingSock.getOutputStream();

			sendServerReady(outStream);
			if(processUser(inStream)==false)
			{
				sendNotOK(outStream);
				throw new UserException("User doesn't exist.");
			}
			sendSaysOk(outStream);
			if(processPass(inStream)==false)
			{
				sendNotOK(outStream);
				throw new UserException("Password is not matched.");
			}
			sendReceiverOKPass(outStream);
			int rep;
			if((rep=processList(inStream))!=0)
			{
				if(rep==1)
				{
					sendClosingMsg(outStream);
					return;
				}
				incomingSock.close();
				throw new UserException("LIST command not received.");
			}
			sendInboxList(outStream,inStream);
			
			if(checkQuitorRetreive(outStream,inStream)==false)
			{
				incomingSock.close();
			}
			
			sendClosingMsg(outStream);
			System.out.println("Waiting for connection...");
		}
		catch(IOException io_excep)
		{
			io_excep.printStackTrace();
		}
		catch(UserException ue)
		{
			ue.debugPrint();
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}
	}

	private String receive(InputStream in)
	{
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();
		//System.out.println(line);
		String heloString=line.substring(0,4);
		return heloString;
	}
	private String receiveFullString(InputStream in)
	{
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();
		System.out.println(line);
		return line;
	}
	protected void sendEmptyFrame(OutputStream out)
	{
		 PrintWriter pr=new PrintWriter(out,true);
		 pr.println("");
	}
	protected void sendServerReady(OutputStream o) throws InterruptedException
	{
		Thread.sleep(100);
		String ready="+OK POP3 server ready ";
		PrintWriter outs=new PrintWriter(o,true);
		outs.println(ready);
	}
	protected boolean processUser(InputStream in) throws IOException,FileNotFoundException
	{
		String userstring=receiveFullString(in);

		username=userstring.substring(5);
		
		authenticationFilePath="C:\\Authentication.txt";

		File f=new File(authenticationFilePath);
		FileReader fis=new FileReader(f);
		BufferedReader br=new BufferedReader(fis);

		if(userstring.substring(0,4).equals("USER"))
		{
			for(String line;(line=br.readLine())!=null;)
			{
				String u=line.substring(0,line.indexOf(' '));
				if(u.equals(username))
				{
					password=line.substring(line.indexOf(' ')+1);
					return true;
				}
			
			}		
		}
		return false;		
	}
	protected void sendNotOK(OutputStream out)
	{
		String line="NOT OK";
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(line);
	}
	protected void sendSaysOk(OutputStream out)
	{
		String line="+OK ";
		//System.out.println("Sending: "+line);
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(line);
	}
	protected boolean processPass(InputStream in)
	{
		String passString=receiveFullString(in);

		String strtCmd=passString.substring(0,4);
		//take password from text box amd check from file if its correct

		if(strtCmd.equals("PASS"))
		{
			String pass=passString.substring(5);
			if(pass.equals(password))
			{
				return true;
			}
		}
		return false;
	}
	protected void sendReceiverOKPass(OutputStream out)
	{
		String line="+OK login successful";
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(line);
		//outp.close();
	}
	protected int processList(InputStream in)
	{
		String strtCmd=receive(in);
		if(strtCmd.equals("LIST"))
		{
			return 0;	 
		}
		else if(strtCmd.equals("QUIT"))
		{
			return 1;
		}
		return -1;
	}
	protected void receiveEmptyFrame(InputStream in)
	{
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();
	}
	protected void sendInboxList(OutputStream out,InputStream in) throws IOException,InterruptedException
	{
		//After receiving empty frame, server is unblocked
		receiveEmptyFrame(in);
		
		mailNo=1;
		PrintWriter pw=new PrintWriter(out,true);
		logFilePath=mailBoxPath+"\\"+username+"\\logFile.txt";
		File f=new File(logFilePath);
		if(f.exists())
		{	
			FileReader fr=new FileReader(f);
			BufferedReader br = new BufferedReader(fr);

			for(String line;(line=br.readLine())!=null;)
			{
				File f1=new File(line);
				FileReader fr1=new FileReader(f1);
				BufferedReader br1 = new BufferedReader(fr1);
				//Extracting From part
				String from =br1.readLine();
				//Ignoring To part
				br1.readLine();
				//Extracting subject part
				String subject=br1.readLine();
			
				String fstring=""+mailNo+" "+from+" "+subject;
			
				pw.println(fstring);
			
				receiveEmptyFrame(in);
				Thread.sleep(100);
				mailNo++;
			}
		}
		String end=".";
		pw.println(end);

		//Now the server is blocked
		receiveEmptyFrame(in);
	}
	
	protected boolean checkQuitorRetreive(OutputStream out, InputStream in) throws IOException,InterruptedException
	{
		/*After sending list,the server is blocked 
		  on the following receiveFullString*/
 
		do{
			String message=receiveFullString(in);
			String mess=message.substring(0,4);
			if(mess.equals("RETR"))
			{
				sendEmptyFrame(out);
				receiveEmptyFrame(in);
				processRetrieve(out,in,message.substring(5));
			
			}
			else if(mess.equals("QUIT"))
			{
				break;
			}
			else if(mess.equals("LIST"))
			{
				sendInboxList(out,in);
			}
			else
			{
				return false;
			}
		}while(true);

		return true;
	}
	protected void processRetrieve(OutputStream out, InputStream in,String mailIndex) throws IOException
	{
		int index=Integer.parseInt(mailIndex);

		File f=new File(logFilePath);
		FileReader fr=new FileReader(f);
		BufferedReader br=new BufferedReader(fr);
			
		String messagePath="";
		for(int i=0;i<index;i++)
		{
			messagePath=br.readLine();
		}
		
	
		File f1=new File(messagePath);
		FileInputStream fis=new FileInputStream(f1);
		BufferedReader br1=new BufferedReader(new InputStreamReader(fis));
		
		//String result="";
		String line="";
		PrintWriter pw=new PrintWriter(out,true);
		while((line=br1.readLine())!=null)
		{
			pw.println(line);
			receiveEmptyFrame(in);
		}
		pw.println("END");
		receiveEmptyFrame(in);
			
	}
	public void sendClosingMsg(OutputStream out)
	{
		String line="+OK POP3 sever disconnecting";
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(line);
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
		System.out.println("Waiting for connection...");
	}
}
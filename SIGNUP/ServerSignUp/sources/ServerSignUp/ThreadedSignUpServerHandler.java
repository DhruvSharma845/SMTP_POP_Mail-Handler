package ServerSignUp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ThreadedSignUpServerHandler implements Runnable
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

	public ThreadedSignUpServerHandler(Socket s,String ad)
	{
		incomingSock=s;
		serverAddress=ad;
		clientAddress=incomingSock.getInetAddress().getHostAddress();
		
	
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
			if(processUser(inStream,outStream)==false)
			{
				sendEmptyFrame(outStream);
				throw new UserException("User doesn't exist.");
			}
			sendSaysOk(outStream);
			if(processPass(inStream)==false)
			{
				sendEmptyFrame(outStream);
				throw new UserException("Password is not matched.");
			}
			sendReceiverOKPass(outStream);
			
			
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
		String ready="+OK Sign Up server ready ";
		PrintWriter outs=new PrintWriter(o,true);
		outs.println(ready);
	}
	protected boolean processUser(InputStream in,OutputStream o) throws IOException,FileNotFoundException
	{
		String userstring=receiveFullString(in);

		username=userstring.substring(5);
		//String currentdir=System.getProperty("user.dir");
		authenticationFilePath="C:\\Authentication.txt";

		File f=new File(authenticationFilePath);
		if(!f.exists())
		{
			f.createNewFile();
		}
		FileReader fis=new FileReader(f);
		BufferedReader br=new BufferedReader(fis);

		if(userstring.substring(0,4).equals("USER"))
		{
			for(String line;(line=br.readLine())!=null;)
			{
				String u=line.substring(0,line.indexOf(' '));
				if(u.equals(username))
				{
					String s="NAK";
					PrintWriter outs=new PrintWriter(o,true);
					outs.println(s);
					return false;
				}
				
			
			}		
			return true;
		}
		return false;
	
	}
	protected void sendSaysOk(OutputStream out)
	{
		String line="+OK ";
		//System.out.println("Sending: "+line);
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(line);
	}
	protected boolean processPass(InputStream in) throws IOException
	{
		String passString=receiveFullString(in);

		String strtCmd=passString.substring(0,4);
		//take password from text box amd check from file if its correct

		if(strtCmd.equals("PASS"))
		{
			String pass=passString.substring(5);
			String finalstring="\n"+username+" "+pass;
			FileWriter fw=new FileWriter(new File(authenticationFilePath),true);
			BufferedWriter pw=new BufferedWriter(fw);
			PrintWriter bw=new PrintWriter(pw,true);
			bw.println(finalstring);
				
			return true;
		}
		return false;
	}
	protected void sendReceiverOKPass(OutputStream out)
	{
		String line="+OK Sign Up successful";
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(line);
		//outp.close();
	}
	
	protected void receiveEmptyFrame(InputStream in)
	{
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();
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
package ClientMailService;

import java.io.*;
import java.util.*;
import java.net.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ClientMailSending
{
	private Socket s;
	private String clientAddress;

	private String fromText;
	private String toText;
	private String subText;
	private String bodyText;
		
	private JPanel parent;

	public ClientMailSending(JPanel p,String _from,String _to,String _sub,String _body)
	{
		parent=p;
		fromText=_from;
		toText=_to;
		subText=_sub;
		bodyText=_body;
	}
	
	/**
	* Connects to the server using port no. of server
	* And perform all the function serially for sending the mail to the server
	* And closes the connection if any Exception occurs in between
	*/
	public void sendMail()
	{
		try
		{
			s=new Socket("127.0.0.1",59417);
			InputStream ins=s.getInputStream();
			OutputStream outs=s.getOutputStream();
		
		
			if(checkServerReady(ins)==false)
			{
				s.close();
				throw new UserExceptionClass(parent,"Server Not Ready.");	
			}
			sendHeloCommand(outs);
			if(checkServerHelo(ins)==false)
			{
				s.close();
				throw new UserExceptionClass(parent,"Hello is not received from server.");
			}
			sendMailCommand(outs);
			if(checkServerOk(ins)==false)
			{
				s.close();
				throw new UserExceptionClass(parent,"Sender OK message not received.");
			}
			sendRcptCommand(outs);
			if(checkRcptOk(ins)==false)
			{
				s.close();
				throw new UserExceptionClass(parent,"Receiver OK message not received.");
			}
			sendDataCommand(outs);
			if(checkDataOk(ins)==false)
			{
				s.close();
				throw new UserExceptionClass(parent,"Server ready to accept message not received.");
			}
			sendMessage(outs,ins);
			if(checkMsgAccepted(ins,outs)==false)
			{
				s.close();
				throw new UserExceptionClass(parent,"Message accepted frame not received.");
			}
			sendEndCommand(outs);
			if(receiveClosingMessage(ins)==false)
			{
				s.close();
				throw new UserExceptionClass(parent,"Closing message not received.");
			}

			JOptionPane.showMessageDialog(parent,"Message Sent");
			s.close();
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(parent,"Socket Not Connected.");
			e.printStackTrace();
		}
		catch(UserExceptionClass ue)
		{
			ue.printTrace();
		}
	}
	/**
	* checks if the server is ready
	*
	* @param in of InputStream
	*
	* @returns true if server is ready..else returns false.
	*/
	
	protected boolean checkServerReady(InputStream in)
	{
		String reply=receive(in);
		if(reply.equals("220"))
		{
			return true;
		}
		return false;
	}
	
	/**
	* sends HELO to server with self address string
	*
	* @param out of OutputStream
	* 
	*/
	protected void sendHeloCommand(OutputStream out)
	{
		clientAddress=s.getLocalAddress().getHostAddress();

		String heloString="HELO "+clientAddress;
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(heloString);
	}
	
	/**
	* checks "250 server says helo" message 
	*
	*@param in: object of InputStream
	*
	*@return true if first three characters are "250"... else returns false
	*/
	
	protected boolean checkServerHelo(InputStream in)
	{
		String reply=receive(in);
		if(reply.equals("250"))
		{
			return true;
		}
		return false;
	}
	
	/**
	* sends "MAIL From" message to server with sender's user ID 
	* 
	* @param out: object of OutputStream
	*
	*/
	protected void sendMailCommand(OutputStream out)
	{
		String mailFrom="MAIL FROM: "+fromText;
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(mailFrom);
	}
	
	/**
	* checks "250 sender ok" message 
	*
	* @param in: object of InputStream
	*
	* @returns true if first three characters received are "250"..
	*		else false
	*/
	protected boolean checkServerOk(InputStream in)
	{
		String reply=receive(in);
		if(reply.equals("250"))
		{
			return true;
		}
		return false;
	}
	/**
	* sends rcpt message to server with recipient user ID 
	*
	* @param out: object of OutputStream
	*
	*/
	protected void sendRcptCommand(OutputStream out)
	{
		 String mailTo="RCPT TO: "+toText; 
		 PrintWriter outp=new PrintWriter(out,true);
		 outp.println(mailTo);
	}
	
	/**
	* receives and checks the 'rcptok' message
	*
	* @returns true if the first four characters are "250"
	*/
	protected boolean checkRcptOk(InputStream in)
	{
		String reply=receive(in);
		if(reply.equals("250"))
		{
			return true;
		}
		return false;
	}
	
	/**
	*sends DATA command to server
	*
	*@param out: object of OutputStream
	*/
	protected void sendDataCommand(OutputStream out)
	{
		 String data="DATA";
		 PrintWriter outp= new PrintWriter(out,true);
		 outp.println(data);
	}
	
	/**
	* Receives and checks "DATA OK" command
	*
	* @param in: object of InputStream
	*
	* @returns true if first three characters are "354"
	*       else returns false
	*/
	protected boolean checkDataOk(InputStream in)
	{
		String reply=receive(in);
		if(reply.equals("354"))
		{
			return true;
		}
		return false;
	}
	
	/**
	* Sending Whole message line by line to the Server
	* After sending whole message,<CRLF> . <CRLF> frames are sent representing the end of data.
	*
	*@param out: object of OutputStream
	*        in: object of InputStream
	*
	*/
	protected void sendMessage(OutputStream out,InputStream in)
	{
		 receiveEmptyFrame(in);
		
		 String from="FROM : "+fromText;
		 PrintWriter outp= new PrintWriter(out,true);
		 outp.println(from);
		 receiveEmptyFrame(in);
		 
		 
		 String to="TO : "+toText;
		 outp.println(to);
		 receiveEmptyFrame(in);
		
		 String sub="SUBJECT : "+subText;
		 outp.println(sub);
		 receiveEmptyFrame(in);
		 
		 int firstindex=0,lastindex=0;
		 for(int i=0;i<bodyText.length();)
		 {
			 if(bodyText.charAt(i)!='\n' && i!=bodyText.length()-1)
			 {
				 lastindex++;
				 i++;
			 }
			 else if(bodyText.charAt(i)=='\n'||i==bodyText.length()-1)
			 {
				 if(i==bodyText.length()-1)
 				 {
				 	 if(bodyText.charAt(i)=='\n')
				 	 {
					 }
					 else 
						lastindex++;
				 }
						
				 
				 String newstr=bodyText.substring(firstindex,lastindex);
				 //System.out.println("hello moto"+newstr+"hello moto");
				 outp.println(newstr);
				 receiveEmptyFrame(in);
				 i++;
				 firstindex=i;
				 lastindex=i;
			 }
		 }
		 String end1="";
		 outp.println(end1);
		 receiveEmptyFrame(in);
		 
		 String end2=".";
		 outp.println(end2);
		 receiveEmptyFrame(in);
		 
		 String end3="";
		 outp.println(end3);
		 receiveEmptyFrame(in);	 
		 
	}
	
	
	/**
	* Check Message accepted frame
	*
	* @param in: object of InputStream
	*       out: object of OutputStream
	*
	* @returns true if first 3 characters are "250"
	*      else returns false
	*
	*/
	protected boolean checkMsgAccepted(InputStream in,OutputStream out)
	{
		sendEmptyFrame(out);
		String reply=receive(in);
		if(reply.equals("250"))
		{
			return true;
		}
		return false;
	}
	
	/**
	* receives message from server
	*
	* @param in: object of InputStream
	*
	* @returns the first 3 characters of message as a string
	*
	*/
	protected String receive(InputStream in)
	{ 
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();
		//System.out.println(line);

		String replycode=line.substring(0,3);
		return replycode;
	}
	
	/**
	* receives empty frame
	*
	*@param in: object of InputStream
	*/
	
	protected void receiveEmptyFrame(InputStream in)
	{
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();	
	}
	
	
	/**
	* sends empty frame to Server
	*
	* @param out: object of OutputStream
	*/
	protected void sendEmptyFrame(OutputStream out)
	{
		String x="";
		PrintWriter pw=new PrintWriter(out,true);
		pw.println(x);
	}

	/**
	* Sends QUIT command to the server
	*
	*@param out: object of OutputStream
	*/
	protected void sendEndCommand(OutputStream out)
	{
		 String quit="QUIT";
		 PrintWriter outp= new PrintWriter(out,true);
		 outp.println(quit);
	}
	
	/**
	* receive closing message from Server
	*
	*@param in: objectof InputStream
	*
	*@returns true if first 3 characters of received message is equal to "221"
	*      else returns false
	*/
	protected boolean receiveClosingMessage(InputStream in)
	{
		String reply=receive(in);
		if(reply.equals("221"))
		{
			return true;
		}
		return false;
	}
}

class UserExceptionClass extends Exception
{
	private String excep;
	private JPanel parent;

	public UserExceptionClass(JPanel p,String s)
	{
		excep=s;
		parent=p;
	}
	public void printTrace()
	{
		JOptionPane.showMessageDialog(parent,excep,"Error Message",JOptionPane.WARNING_MESSAGE);
		System.exit(0);
	}
}
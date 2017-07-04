package ClientMailService;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.geom.*;
import javax.swing.text.*;
import javax.swing.text.PlainDocument;
import javax.swing.JScrollPane;

/**
*	Client handler class that interacts with signup server
*/
public class SignupHandler
{	

	private String username;
	private String password;

	private JPanel parent;
	
	private Socket signupSock;
	private String serverAddress="127.0.0.1";
	
	public SignupHandler(JPanel pan,String u,String p)
	{
		username=u;
		password=p;
		parent=pan;
	}
	public int signup()
	{
		try
		{
			signupSock=new Socket(serverAddress,48517);
			InputStream ins=signupSock.getInputStream();
			OutputStream outs=signupSock.getOutputStream();
			
			if(checkServerReady(ins)==false)
			{
				JOptionPane.showMessageDialog(parent,"Signup Unsuccessful: Server Not Ready!","Error Message",JOptionPane.WARNING_MESSAGE);
				signupSock.close();
				return -1;
			}
			sendUserName(outs);
			if(checkServerOk(ins)==false)
			{
				JOptionPane.showMessageDialog(parent,"Signup Unsuccessful: Invalid Signup Entries!","Error Message",JOptionPane.WARNING_MESSAGE);
				signupSock.close();
				return -1;
			}
			sendPass(outs);
			if(checkServerOk1(ins)==false)
			{
				JOptionPane.showMessageDialog(parent,"Signup Unsuccessful: Invalid Signup Entries!","Error Message",JOptionPane.WARNING_MESSAGE);
				signupSock.close();
				return -1;
			}
			JOptionPane.showMessageDialog(parent,"Signup Successful");
			signupSock.close();
			Thread.sleep(150);
			return 1;
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}
		return 1;
	}
	protected String receiveFullString(InputStream in)
	{
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();
		return line;
	}
	protected String receive(InputStream in)
	{ 
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();

		String replycode=line.substring(0,3);
		return replycode;
	}
	protected boolean checkServerReady(InputStream in)
	{
		String reply=receive(in);
		if(reply.equals("+OK"))
		{
			return true;
		}
		return false;
	}
	protected void sendUserName(OutputStream out) throws InterruptedException
	{
		Thread.sleep(100);
		String UserString="USER "+username;
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(UserString);
	}
	protected boolean checkServerOk(InputStream in)
	{
		String reply=receive(in);
		if(reply.equals("+OK"))
		{
			return true;
		}
		return false;
	}
	protected void sendPass(OutputStream out) throws InterruptedException
	{
		Thread.sleep(100);
		String pass="PASS "+password;
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(pass);
	}
	 
	protected boolean checkServerOk1(InputStream in) throws InterruptedException
	{
		String reply=receive(in);
		if(reply.equals("+OK"))
		{
			return true;
		}
		return false;
	}
		
}
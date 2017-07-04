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

public class ClientLoginHandler
{	
	private InputStream ins;
	private OutputStream outs;

	private String username;
	private String password;

	private JPanel parent;
	
	public ClientLoginHandler(InputStream _ins,OutputStream _outs,JPanel pan,String u,String p)
	{
		username=u;
		password=p;
		parent=pan;
		ins=_ins;
		outs=_outs;
	}
	public void login()
	{
		try
		{
			if(checkServerReady(ins)==false)
			{
				throw new UserException(parent,"Server not ready");
			}
			sendUserName(outs);
			if(checkServerOk(ins)==false)
			{
				throw new UserException(parent,"Invalid Login Entries");
			}
			sendPass(outs);
			if(checkServerOk1(ins)==false)
			{
				throw new UserException(parent,"Invalid Login Entries");
			}
			JOptionPane.showMessageDialog(parent,"Login Successful");
			Thread.sleep(200);
		}
		catch(UserException ue)
		{
			ue.printTrace();
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}
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

class UserException extends Exception
{
	private String excep;
	private JPanel parent;

	public UserException(JPanel p,String s)
	{
		excep=s;
		parent=p;
	}
	public void printTrace()
	{
		JOptionPane.showMessageDialog(parent,"Login Unsuccessful: "+excep,"Error Message",JOptionPane.WARNING_MESSAGE);
		System.exit(0);
	}
}
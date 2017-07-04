package ClientMailService;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.*;
import java.util.*;
import java.net.*;

public class LoginAndDisplayChoice
{
	private JPanel mainPanel;

	private String user;
	private String passw;

	private Socket popSock;
	private String serverAdd="127.0.0.1";

	public LoginAndDisplayChoice(JPanel _pan,String _user,String _pass)
	{
		mainPanel=_pan;
		user=_user;
		passw=_pass;
		
		try
		{
			popSock=new Socket(serverAdd,59517);
			InputStream ins=popSock.getInputStream();
			OutputStream outs=popSock.getOutputStream();

			ClientLoginHandler cmh=new ClientLoginHandler(ins,outs,mainPanel,user,passw);
			cmh.login();

						
			ChoiceFrame cif=new ChoiceFrame(ins,outs,user);
			cif.setResizable(false);
			//cif.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			
			cif.setLocationRelativeTo(null);
			cif.setVisible(true);
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}
	}
}

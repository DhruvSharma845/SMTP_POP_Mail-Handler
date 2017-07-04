package ServerSignUp;

import java.io.*;
import java.net.*;
import java.util.*;

public class ThreadedSignUpServer
{
	private static int LISTENQ = 40;

	public static void main(String[] args)
	{
		try
		{
			ServerSocket serv_sock=new ServerSocket(48517,LISTENQ);

			//System.out.println("Current IP-address is: "+serv_sock.getInetAddress().getHostAddress());
			System.out.println("Waiting for connection...");
			while(true)
			{
				Socket incoming_sock=serv_sock.accept();
				ThreadedSignUpServerHandler newThreadServer=new ThreadedSignUpServerHandler(incoming_sock,serv_sock.getInetAddress().getHostAddress());
				Thread th=new Thread(newThreadServer);
				th.start();
			}
		}
		catch(IOException io_excep)
		{
			System.out.println("Error while opening the socket \n or while waiting for connection.\n");
			io_excep.printStackTrace();
		}
		catch(IllegalArgumentException arg_excep)
		{
			System.out.println("Port parameter is outside the specified \n range of valid port numbers(0 and 65535).\n");
			arg_excep.printStackTrace();
		}
	}
}
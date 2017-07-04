package ClientMailService;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.WindowAdapter;
import javax.swing.*;
import javax.swing.ImageIcon;

import java.net.*;
import java.io.*;
import java.util.*;

public class ChoiceFrame extends JFrame
{
	
	private InputStream ins;
	private OutputStream outs;
	private String username;

	private static final int FONTSIZE=30;

	public ChoiceFrame(InputStream _in,OutputStream _out,String u)
	{

		ins=_in;
		outs=_out;
		username=u;

		JPanel northp=new JPanel();
		northp.setLayout(new FlowLayout(FlowLayout.CENTER));

		Box upperBox=Box.createVerticalBox();
		JPanel subp1=new JPanel();
		subp1.setLayout(new FlowLayout(FlowLayout.CENTER));

		JPanel subp2=new JPanel();
		subp2.setLayout(new FlowLayout(FlowLayout.CENTER));

		JLabel label1=new JLabel("<html>Hi <b><i><font size=5 color=\"cyan\">"+username+"</font><i></b>...!</html>");
		JLabel label2=new JLabel("<html>Welcome to <font size=5 color=\"blue\">SPARTAN MAIL SERVICE</font></html>");
		
		subp1.add(label1);
		subp2.add(label2);

		upperBox.add(subp1);
		upperBox.add(subp2);
		
		northp.add(upperBox);

		JPanel centerPanel=new JPanel();
		centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		ImageIcon img1=new ImageIcon(getClass().getResource("Resources\\compose.png"));
		JButton sendButton=new JButton(img1);
		
		sendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event)
			{
				//setVisible(false);
			
				ComposeFrame comFrame=new ComposeFrame(username);
				Toolkit kit=Toolkit.getDefaultToolkit();
				Dimension screen=kit.getScreenSize();
				int sWidth=screen.width;
				int sHeight=screen.height;


				Double d1=0.38*sWidth;
				int DEFAULT_WIDTH=d1.intValue();
				Double d2=0.55*sHeight;
				int DEFAULT_HEIGHT=d2.intValue();

		                //Setting some fields

				comFrame.setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
				comFrame.setResizable(false);
				//comFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				comFrame.setLocationRelativeTo(null);

				comFrame.setVisible(true);
			}
		}		
		);

		ImageIcon img2=new ImageIcon(getClass().getResource("Resources\\inbox.png"));
		JButton inboxButton=new JButton(img2);
		
		
		inboxButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event)
			{

				sendListCommand(outs);

				InboxFrame inFrame=new InboxFrame(ins,outs,username);
				inFrame.setResizable(false);
				inFrame.setLocationRelativeTo(null);
				inFrame.setVisible(true);
			}
		}		
		);
		
		centerPanel.add(sendButton);
		centerPanel.add(inboxButton);

		add(northp,BorderLayout.NORTH);
		add(centerPanel,BorderLayout.CENTER);
		
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				QuitAction qa=new QuitAction(ins,outs);
				qa.performQuit();
				System.exit(0);
			}
		}
		);
		pack();
	}
	protected void sendListCommand(OutputStream out)
	{
		 String list="LIST"; 
		 PrintWriter outp=new PrintWriter(out,true);
		 outp.println(list);
	}
}
class QuitAction
{	
	private InputStream ins;
	private OutputStream outs;

	public QuitAction(InputStream _ins,OutputStream _outs)
	{	
		ins=_ins;
		outs=_outs;
	}
	public void performQuit()
	{
		sendEndCommand(outs);
		receiveClosingMsg(ins);
		//System.exit(0);
	}
	protected String receive(InputStream in)
	{ 
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();

		String replycode=line.substring(0,3);
		return replycode;
	}
	protected void sendEndCommand(OutputStream out)
	{
		String quit="QUIT";
		System.out.println(quit);
		PrintWriter outp= new PrintWriter(out,true);
		outp.println(quit);
	}
	protected boolean receiveClosingMsg(InputStream in)
	{
		String reply=receive(in);
		if(reply.equals("+OK"))
			return true;
		return false;
	}
}
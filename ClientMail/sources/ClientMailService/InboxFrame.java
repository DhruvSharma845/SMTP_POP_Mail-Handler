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
import javax.swing.ImageIcon;

public class InboxFrame extends JFrame
{
	private InputStream ins;
	private OutputStream outs;
	private String username;

	private int maxLength;
	
	public static int FONTSIZE=20;
 
	public InboxFrame(InputStream _ins,OutputStream _outs,String _uname)
	{
		
		username=_uname;

		maxLength=0;
		
		ins=_ins;
		outs=_outs;
		setTitle("Inbox List");
		
		Toolkit kit=Toolkit.getDefaultToolkit();
		Dimension screen=kit.getScreenSize();
		int sWidth=screen.width;
		int sHeight=screen.height;


		Double d1=0.32*sWidth;
		int DEFAULT_WIDTH=d1.intValue();
		Double d2=0.40*sHeight;
		int DEFAULT_HEIGHT=d2.intValue();

                //Setting some fields

		JPanel userPanel=new JPanel();
		userPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		JLabel l=new JLabel(username+"!  Your Inbox List: ");
		l.setFont(new Font("Serif",Font.ITALIC,FONTSIZE));
		userPanel.add(l);

		Box inboxListBox = Box.createVerticalBox();
		JScrollPane scrollArea=new JScrollPane(inboxListBox,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		ArrayList<String> list=new ArrayList<String>();
		try
		{
			displayList(ins,outs,list);
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}
		//JPanel bPanel=null;

		if(list.size()==0)
		{
			JOptionPane.showMessageDialog(userPanel,"Empty Inbox");
			dispose();
		}
		else
		{
			int index=1;
			JPanel[] bPanels=new JPanel[list.size()];
			for(String str:list)
			{
				JButton b=new JButton();
				b.addActionListener(new InboxListActionListener(index,ins,outs));
				b.setPreferredSize(new Dimension(DEFAULT_WIDTH-40,30));
				b.setHorizontalTextPosition(AbstractButton.LEADING);
				b.setText(str);

				bPanels[index-1]=new JPanel();
				bPanels[index-1].setLayout(new FlowLayout(FlowLayout.CENTER));
				bPanels[index-1].add(b);

				inboxListBox.add(bPanels[index-1]);
				index++;
			}
			/*bPanel=new JPanel();
			JButton quitButton=new JButton("QUIT");
			quitButton.addActionListener(new QuitActionListener(ins,outs));
			bPanel.add(quitButton);*/
			add(scrollArea,BorderLayout.CENTER);
			//add(bPanel,BorderLayout.SOUTH);
		}		
		add(userPanel,BorderLayout.NORTH);
		

		setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
		
	}
	protected String receiveFullString(InputStream in)
	{
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();
		return line;
	}
	protected void sendEmptyFrame(OutputStream out)
	{
		 PrintWriter pr=new PrintWriter(out,true);
		 pr.println("");
	}
	protected void displayList(InputStream in,OutputStream out,ArrayList<String> buttons) throws InterruptedException
	{
		//Server is blocked to receive empty frame
		//so,it is used to unblock it.
		sendEmptyFrame(out);

		String line=receiveFullString(in);
		Thread.sleep(100);
		sendEmptyFrame(out);
		while(!line.equals("."))
		{
			if(line.length() > maxLength)
				maxLength=line.length();
			buttons.add(line);
			line=receiveFullString(in);
			Thread.sleep(100);
			sendEmptyFrame(out);
		}
	}
} 
class InboxListActionListener implements ActionListener
{
	private int mailIndex;	
	private InputStream ins;
	private OutputStream outs;

	public InboxListActionListener(int _index,InputStream _ins,OutputStream _outs)
	{
		mailIndex=_index;	
		ins=_ins;
		outs=_outs;
	}
	public void actionPerformed(ActionEvent event)
	{

		JFrame frame=new JFrame();
		frame.setTitle("Mail"+mailIndex);
		frame.setSize(300,300);
		frame.setResizable(false);
		
		/*JPanel panel=new JPanel();
		panel.setSize(295,295);*/
		
		JTextArea tArea=new JTextArea();
		tArea.setLineWrap(true);
		tArea.setEditable(false);
		tArea.setVisible(true);

		JScrollPane scroll=new JScrollPane(tArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		retrieveMsgCommand(outs,mailIndex);
		try
		{
			tArea.setText(receiveMsg(ins,outs));
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
			System.exit(0);
		}
		
		frame.add(scroll);
		frame.setVisible(true);
	}
	protected void receiveEmptyFrame(InputStream in)
	{
		Scanner inp=new Scanner(in);
		String line=inp.nextLine();
	}
	protected void retrieveMsgCommand(OutputStream out,int msgno)
	{
		String msg="RETR "+ msgno;
		//System.out.println(msg);
		PrintWriter outp=new PrintWriter(out,true);
		outp.println(msg);
		receiveEmptyFrame(ins);
	}
	protected void sendEmptyFrame(OutputStream out)
	{
		 PrintWriter pr=new PrintWriter(out,true);
		 pr.println("");
	}
	protected String receiveMsg(InputStream in,OutputStream out) throws InterruptedException
	{
		Thread.sleep(100);
		//Server is made unblocked using the following sendEmptyFrame command
		sendEmptyFrame(out);
		
		Scanner ins=new Scanner(in);
		System.out.println("The mail contents are:");
		String msg="";
		String result="";
		do
		{
			msg=ins.nextLine();
			if(msg.equals("END"))
			{
				sendEmptyFrame(out);
				break;
			}
			result=result+msg+"\n";
			System.out.println(result);
			sendEmptyFrame(out);
		}while(!msg.equals("END"));
		
		return result;
	}
}

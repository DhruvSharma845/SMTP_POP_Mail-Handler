package ClientMailService;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

class TextFieldLimit extends PlainDocument
{
	private int limit;
	
	public TextFieldLimit(int limit)
	{
		super();
		this.limit=limit;
	}
	
	public void insertString(int offset, String str, AttributeSet attr)
	{
		if(str==null)
			return;
		try
		{
			if((getLength() + str.length()) <= limit)
			{
				super.insertString(offset, str, attr);
			}
		}
		catch(BadLocationException b)
		{
			b.printStackTrace();
		}
	}
}

public class ComposeFrame extends JFrame
{
	private JTextField fromTextBox;
	private JTextField toTextBox;
	private JTextField subTextBox;
	private JTextArea bodyTextBox;

	private String username;
	
	public ComposeFrame(String u)
	{
		setTitle("Compose");

		username=u;

		JPanel bottomPanel=new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		JPanel mainPanel=new JPanel();
		mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		
		JPanel fromPanel=new JPanel();
		fromPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JPanel toPanel=new JPanel();
		toPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JPanel subPanel=new JPanel();
		subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JPanel bodyPanel=new JPanel();
		bodyPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		fromPanel.add(new JLabel("From      :"));
		fromTextBox=new JTextField(44);
		fromTextBox.setDocument(new TextFieldLimit(50));
		fromTextBox.setText(username+"@127.0.0.1");
		fromTextBox.setEditable(false);
		fromPanel.add(fromTextBox);
		
		
		toPanel.add(new JLabel("To           :"));
		toTextBox=new JTextField(44);
		toTextBox.setDocument(new TextFieldLimit(50));
		toPanel.add(toTextBox);

		
		subPanel.add(new JLabel("Subject :"));
		subTextBox=new JTextField(44);
		subTextBox.setDocument(new TextFieldLimit(50));
		subPanel.add(subTextBox);

		
		bodyPanel.add(new JLabel("Body      :"));
		bodyTextBox=new JTextArea(15,43);
		bodyTextBox.setLineWrap(true);
		JScrollPane scroll=new JScrollPane(bodyTextBox,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		bodyPanel.add(scroll);
		
		

		//Creating Send Button
		JButton sendButton=new JButton("Send Mail");
	
		sendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event)
			{
				if(checkValidUserInput()==true)
				{
					ClientMailSending cmh=new ClientMailSending(mainPanel,fromTextBox.getText(),toTextBox.getText(),subTextBox.getText(),bodyTextBox.getText());
					cmh.sendMail();
					fromTextBox.setText(username+"@127.0.0.1");
					toTextBox.setText("");
					subTextBox.setText("");
					bodyTextBox.setText("");
				}
				else
				{
					JOptionPane.showMessageDialog(mainPanel,"Invalid Field Entries","Error Message",JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		bottomPanel.add(sendButton);


		add(bottomPanel,BorderLayout.SOUTH);
		add(mainPanel,BorderLayout.NORTH);
		mainPanel.add(fromPanel);
		mainPanel.add(toPanel);
		mainPanel.add(subPanel);
		mainPanel.add(bodyPanel);

		pack();
		
	}
	public boolean checkValidUserInput()
	{
		String patternString="[A-Za-z0-9_\\.]+@[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";
		Pattern patternOfUserId=Pattern.compile(patternString);

		//Checking from textfield
		String testString=fromTextBox.getText();
		Matcher mfrom=patternOfUserId.matcher(testString);
		if(!mfrom.matches())
		{
			fromTextBox.setText("");
			return false;
		}
		//Checking to textfield
		testString=toTextBox.getText();
		Matcher mto=patternOfUserId.matcher(testString);
		if(!mto.matches())
		{
			toTextBox.setText("");
			return false;
		}
		
		return true;
	}
	
}


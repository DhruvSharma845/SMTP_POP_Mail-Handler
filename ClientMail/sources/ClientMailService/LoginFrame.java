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
import javax.swing.ImageIcon;

/**
*	Class that creates login frame
*/
public class LoginFrame extends JFrame
{
	private JTextField userNameTextBox;
	private JPasswordField passwordTextBox;

	private static final int FONTSIZE=30;
	
	public LoginFrame()
	{

		setTitle("Spartan Mail Service");

		//UPPER PANEL
		JPanel UpperPanel=new JPanel();
		UpperPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		JLabel logo=new JLabel("<html>Log in to <br> <b><font size=6 color=\"blue\" face =\"Verdana\">SPARTAN MAIL SERVICE</font></b></html>");
		UpperPanel.add(logo);

		
		//BOTTOM PANEL
		JPanel bottomPanel=new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		JLabel label1=new JLabel("Don't have login ID?");
		bottomPanel.add(label1);

		ImageIcon img2=new ImageIcon(getClass().getResource("Resources\\signup.png"));
		JButton signupButton=new JButton(img2);
		signupButton.addActionListener( new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				//setVisible(false);
				SignUpFrame suf=new SignUpFrame();
			}
		}
		);
		bottomPanel.add(signupButton);


		//CENTER PANEL
		JPanel mainPanel=new JPanel();
		mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		

		Box firstBox=Box.createVerticalBox();
		ImageIcon img1=new ImageIcon(getClass().getResource("Resources\\login.png"));
		JButton ImageButton=new JButton(img1);
		firstBox.add(ImageButton);

		JPanel userNamePanel=new JPanel();
		userNamePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		JPanel passwordPanel=new JPanel();
		passwordPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		

		userNamePanel.add(new JLabel("Username :"));
		userNameTextBox=new JTextField(20);
		userNameTextBox.setDocument(new JTextFieldLimit(40));
		userNamePanel.add(userNameTextBox);
		
		
		passwordPanel.add(new JLabel("Password  :"));
		passwordTextBox=new JPasswordField(20);
		PlainDocument document = (PlainDocument) passwordTextBox.getDocument();
        document.setDocumentFilter(new DocumentFilter() 
		{
			public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException 
			{
                String string = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;

                if (string.length() <= 12 ) 
				{
                    super.replace(fb, offset, length, text, attrs); 
                }
            }

        });
		passwordPanel.add(passwordTextBox);
		
		
		//Creating Send Button
		JPanel buttonPanel=new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		JButton loginButton=new JButton("Login");
		
	
		loginButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event)
			{
				if(checkValidUserInput()==true)
				{	
					LoginAndDisplayChoice ldc=new LoginAndDisplayChoice(mainPanel,userNameTextBox.getText(),passwordTextBox.getText());
					setVisible(false);
				}
				else
				{
					JOptionPane.showMessageDialog(mainPanel,"Invalid Field Entries..!","Error Message",JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		buttonPanel.add(loginButton);

		Box secondBox=Box.createVerticalBox();
		secondBox.add(userNamePanel);
		secondBox.add(Box.createVerticalStrut(10));
		secondBox.add(passwordPanel);
		secondBox.add(Box.createVerticalStrut(10));
		secondBox.add(buttonPanel);

		mainPanel.add(firstBox);
		mainPanel.add(secondBox);

		add(UpperPanel,BorderLayout.NORTH);
		add(bottomPanel,BorderLayout.SOUTH);
		add(mainPanel,BorderLayout.CENTER);
		
		pack();
		
	}
	public boolean checkValidUserInput()
	{
		String patternString="[A-Za-z0-9_\\.]+";         
		Pattern patternOfUserId=Pattern.compile(patternString);

		//Checking from textfield
		String testString=userNameTextBox.getText();
		Matcher mfrom=patternOfUserId.matcher(testString);
		if(!mfrom.matches())
		{
			userNameTextBox.setText("");
			return false;
		}

		//Checking to textfield
		testString=passwordTextBox.getText();
		if(testString.equals(""))
		{
			passwordTextBox.setText("");
			return false;
		}
		
		return true;
	}	
}


class JTextFieldLimit extends PlainDocument
{
	private int limit;
	
	public JTextFieldLimit(int limit)
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
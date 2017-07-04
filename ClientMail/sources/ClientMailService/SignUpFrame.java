package ClientMailService;

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.File;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.awt.geom.*;
import javax.swing.text.*;
import javax.swing.text.PlainDocument;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;

	
class JTextFieldLimit1 extends PlainDocument
{
	private int limit;
	
	public JTextFieldLimit1(int limit)
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
/**
*	Class that creates signup frame
*/
public class SignUpFrame extends JFrame
{
	
	private JTextField firstNameTextBox;
	private JTextField lastNameTextBox;
	private JTextField usernameTextBox;
	private JPasswordField passwordTextBox;
	private JPasswordField confirmPassTextBox;

	private Socket signupSock;

	public SignUpFrame()
	{
		setTitle("Create Your Account");


		JPanel mainPanel=new JPanel();
		mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		
		JPanel namePanel=new JPanel(new GridBagLayout());
		namePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		namePanel.setLayout(new GridLayout(0,1));
		
		JPanel usernamePanel=new JPanel();
		usernamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		usernamePanel.setLayout(new GridLayout(0,1));
		
		JPanel passwordPanel=new JPanel();
		passwordPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		passwordPanel.setLayout(new GridLayout(0,1));

		
		JPanel genderPanel=new JPanel();
		genderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		genderPanel.setLayout(new BoxLayout(genderPanel,BoxLayout.Y_AXIS));
		genderPanel.setLayout(new GridLayout(0,1));
		//genderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel bottomPanel=new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		//GridBagConstraints c=new GridBagConstraints();
		JLabel firstName=new JLabel("First Name");
		//c.gridx=0;
		//c.gridy=0;
		namePanel.add(firstName);
		
		firstNameTextBox=new JTextField(30);
		firstNameTextBox.setDocument(new JTextFieldLimit(50));
		//c.gridx=0;
		//c.gridy=1;
		namePanel.add(firstNameTextBox);
		
		JLabel lastName=new JLabel("Last Name");
		//c.gridx=0;
		//c.gridy=2;
		namePanel.add(lastName);
		
		lastNameTextBox=new JTextField(30);
		lastNameTextBox.setDocument(new JTextFieldLimit(50));
		//c.gridx=0;
		//c.gridy=3;
		namePanel.add(lastNameTextBox);
		
		/*
		namePanel.add(new JLabel("Name      :"));
		firstNameTextBox=new JTextField(44);
		firstNameTextBox.setDocument(new JTextFieldLimit(50));
		lastNameTextBox=new JTextField(44);
		lastNameTextBox.setDocument(new JTextFieldLimit(50));
		namePanel.add(firstNameTextBox);
		namePanel.add(lastNameTextBox);
		*/
		
		
		JLabel username=new JLabel("Choose your Username");
		usernamePanel.add(username);
		usernameTextBox=new JTextField(30);
		usernameTextBox.setDocument(new JTextFieldLimit1(50));
		usernamePanel.add(usernameTextBox);
		
		
		/*usernamePanel.add(new JLabel("User Name     :"));
		usernameTextBox=new JTextField(30);
		usernameTextBox.setDocument(new JTextFieldLimit1(50));
		usernamePanel.add(usernameTextBox);*/

		JLabel password=new JLabel("Create a Password");
		passwordPanel.add(password);
		passwordTextBox=new JPasswordField(30);
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
		
		JLabel confirmPass=new JLabel("Confirm your Password");
		passwordPanel.add(confirmPass);
		confirmPassTextBox=new JPasswordField(30);
		PlainDocument document1 = (PlainDocument) confirmPassTextBox.getDocument();
		document1.setDocumentFilter(new DocumentFilter() 
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
		passwordPanel.add(confirmPassTextBox);
		/*passwordPanel.add(new JLabel("Password :"));
		passwordTextBox=new JTextField(30);
		passwordTextBox.setDocument(new JTextFieldLimit(50));
		passwordPanel.add(passwordTextBox);

		
		confirmPassPanel.add(new JLabel("Confirm Password :"));
		confirmPassTextBox=new JTextField(30);
		confirmPassTextBox.setDocument(new JTextFieldLimit(50));
		confirmPassPanel.add(confirmPassTextBox);*/
		
		JLabel gender=new JLabel("Gender");
		genderPanel.add(gender);
		JRadioButton option1 = new JRadioButton("Male");
	        JRadioButton option2 = new JRadioButton("Female");
 
 	        ButtonGroup group = new ButtonGroup();
		//group.setLayout(new FlowLayout(FlowLayout.LEFT));
	        group.add(option1);
        	group.add(option2);
        	setLayout(new FlowLayout());
 
	        genderPanel.add(option1);
       		genderPanel.add(option2);
        
		
		//Creating Send Button
		JButton signupButton=new JButton("Sign Up");
		

		signupButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event)
			{
				if(checkValidUserInput())
				{
					SignupHandler sgn=new SignupHandler(mainPanel,usernameTextBox.getText(),passwordTextBox.getText());
					if(sgn.signup()==-1)
					{
						usernameTextBox.setText("");
						passwordTextBox.setText("");
						confirmPassTextBox.setText("");
					}
					else
					{
						dispose();
					}
				}
				else
				{
					JOptionPane.showMessageDialog(mainPanel,"Invalid Field Entries..!","Error Message",JOptionPane.WARNING_MESSAGE);
					usernameTextBox.setText("");
					passwordTextBox.setText("");
					confirmPassTextBox.setText("");
				}
			}
		});
		bottomPanel.add(signupButton);


		
		add(mainPanel,BorderLayout.NORTH);
		mainPanel.add(namePanel);
		mainPanel.add(usernamePanel);
		mainPanel.add(passwordPanel);
		mainPanel.add(genderPanel);
		add(bottomPanel,BorderLayout.SOUTH);
		pack();

		//Setting some fields
		setSize(440,550);
		//frame.setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultLookAndFeelDecorated(true);
		setLocationRelativeTo(null);

		setVisible(true);
	}
	protected boolean checkValidUserInput()
	{
		if(passwordTextBox.getText().length()==confirmPassTextBox.getText().length())
			return true;
		return false;
	}
}		
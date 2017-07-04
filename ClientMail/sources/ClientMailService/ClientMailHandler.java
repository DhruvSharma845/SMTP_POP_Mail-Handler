package ClientMailService;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
*	Main Class for client mail handling
*
*	Creates login frame
*/
public class ClientMailHandler
{
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception c)
		{
			c.printStackTrace();
		} 

		LoginFrame slFrame=new LoginFrame();
		
		Toolkit kit=Toolkit.getDefaultToolkit();
		Dimension screen=kit.getScreenSize();
		int sWidth=screen.width;
		int sHeight=screen.height;


		Double d1=0.40*sWidth;
		int DEFAULT_WIDTH=d1.intValue();
		Double d2=0.44*sHeight;
		int DEFAULT_HEIGHT=d2.intValue();

                //Setting some fields

		slFrame.setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
		slFrame.setResizable(false);
		slFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		slFrame.setLocationRelativeTo(null);

		slFrame.setVisible(true);
	}
}
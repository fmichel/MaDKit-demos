/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit_Demos.
 * 
 * MaDKit_Demos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit_Demos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit_Demos. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.marketorg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import madkit.gui.OutputPanel;
import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.message.ObjectMessage;

/**
 * @author Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * @version 5.1
 * 
 */
public class Provider extends Agent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4080055981945317639L;
	private String competence;
	static private int nbOfProvidersOnScreen=0;
	private JPanel blinkPanel;


	public Provider()
	{
		competence = Broker.availableTransports.get((int) (Math.random()*Broker.availableTransports.size()));
	}

	public void activate()
	{
		createGroupIfAbsent("travel","travel-providers",true,null);
		requestRole("travel","travel-providers",competence+"-provider",null);
	}


	@SuppressWarnings("unchecked")
	public void live()
	{
		while (true) {
			Message m = waitNextMessage();
			if (m.getSender().getRole().equals("broker"))
				handleBrokerMessage((ObjectMessage<String>) m);
			else
				finalizeContract((ObjectMessage<String>) m);
		}
	}

	private void handleBrokerMessage(ObjectMessage<String> m) {
		if(m.getContent().equals("make-bid-please")){
			if(logger != null)
				logger.info("I received a call for bid from "+m.getSender());
			sendReply(m, new ObjectMessage<Integer>((int) (Math.random()*500)));
		}
		else{
			iHaveBeenSelected(m);
		}
	}

	private void iHaveBeenSelected(ObjectMessage<String> m) {
		if (hasGUI()) {
			blinkPanel.setBackground(Color.YELLOW);
		}
		if(logger != null)
			logger.info("I have been selected :)");
		String contractGroup = m.getContent();
		createGroup("travel", contractGroup,true);
		requestRole("travel", contractGroup, "provider");
		sendReply(m, new Message()); // just an acknowledgment
	}

	private void finalizeContract(ObjectMessage<String> m) {
		if (hasGUI()) {
		blinkPanel.setBackground(Color.GREEN);
		}
		if(logger != null)
			logger.info("I have sold something: That's great !");
		sendReply(m, new ObjectMessage<String>("ticket"));
		pause((int) (Math.random()*2000+1000));//let us celebrate !!
		leaveGroup("travel", m.getSender().getGroup());
		if (hasGUI()) {
			blinkPanel.setBackground(Color.LIGHT_GRAY);
		}
	}

	@Override
	public void setupFrame(JFrame frame) {
		JPanel p = new JPanel(new BorderLayout());
		//customizing but still using the OutputPanel from MaDKit GUI
		p.add(new OutputPanel(this),BorderLayout.CENTER);
		blinkPanel = new JPanel();
		blinkPanel.add(new JLabel(new ImageIcon(getClass().getResource("images/"+competence+".png"))));
		p.add(blinkPanel,BorderLayout.NORTH);
		blinkPanel.setBackground(Color.LIGHT_GRAY);
		p.validate();
		frame.add(p);
		int xLocation = nbOfProvidersOnScreen++*300;
		if(xLocation+300 > Toolkit.getDefaultToolkit().getScreenSize().getWidth())
			nbOfProvidersOnScreen=0;
		frame.setLocation(xLocation, 640);
		frame.setSize(300, 300);
	}


}










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
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import madkit.gui.OutputPanel;
import madkit.kernel.Agent;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;
import madkit.kernel.Message;
import madkit.message.ObjectMessage;


/**
 * @author Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * @version 5.1
 * 
 */
@SuppressWarnings("serial")
public class Client extends Agent
{
	
	static int nbOfClientsOnScreen = 0;
	private JPanel blinkPanel;
	private static ImageIcon clientImage = new ImageIcon(new ImageIcon(Client.class.getResource("images/client.png")).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH));
	private String product = Broker.availableTransports.get((int) (Math.random()*Broker.availableTransports.size()));

	@Override
	protected void activate()
	{
		createGroupIfAbsent("travel","travel-clients",true,null);
		requestRole("travel","travel-clients", "client",null);
		int pause = 1000 + (int) (Math.random()*2000);
		if(logger != null)
			logger.info("I will be looking for a "+product+" in "+pause+" ms !");
		pause(pause);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void live()
	{
		boolean haveTicket = false;
		while (! haveTicket)
		{
			Message brokerAnswer = null;
			while (brokerAnswer == null) {
					sendMessageWithRole(
							"travel",
							"travel-clients", 
							"broker",
							new ObjectMessage<String>(product), 
							"client");
					if(logger != null)
						logger.info("Waiting for a broker answer...");
					brokerAnswer = waitNextMessage(1000);
					if(logger != null && brokerAnswer == null)
						logger.info("For now there is nothing for me :(");
				}
			logFindBroker(brokerAnswer);// I found a broker and he found something for me
			haveTicket = buyTicket((ObjectMessage<String>) brokerAnswer);
			}
		}

	@Override
	protected void end() {
		if(logger != null)
			logger.info("I will quit soon now, buit I will launch another one like me !");
		pause((int) (Math.random() * 2000 + 500));
		launchAgent(new Client(),4,true);
	}

	private void logFindBroker(Message brokerAnswer) {
		if(logger != null)
			logger.info("I found a broker : "+brokerAnswer.getSender());
		if (blinkPanel != null) {
			blinkPanel.setBackground(Color.YELLOW);
			pause(1000);
		}
	}

	private boolean buyTicket(ObjectMessage<String> brokerAnswer) {
		String contractGroupID = brokerAnswer.getContent();
		requestRole("travel",contractGroupID, "client");
		Message ticket = sendMessageAndWaitForReply("travel", contractGroupID, "provider",new ObjectMessage<String>("money"), 4000);
		if(ticket != null){
			if(logger != null)
				logger.info("Yeeeaah: I have my ticket :) ");
			if (hasGUI()) {
				blinkPanel.setBackground(Color.GREEN);
			}
			leaveGroup("travel", "travel-clients");
			pause((int) (1000+Math.random()*2000));
			return true;
		}
		return false;

	}

	@Override
	public void setupFrame(JFrame frame) {
		JPanel p = new JPanel(new BorderLayout());
		//customizing but still using the OutputPanel from MaDKit GUI
		p.add(new OutputPanel(this),BorderLayout.CENTER);
		blinkPanel = new JPanel();
		blinkPanel.add(new JLabel(clientImage));
		blinkPanel.add(new JLabel(new ImageIcon(getClass().getResource("images/"+product+".png"))));
		p.add(blinkPanel,BorderLayout.NORTH);
		p.validate();
		frame.add(p);
		int xLocation = nbOfClientsOnScreen++*390;
		if(xLocation+390 > Toolkit.getDefaultToolkit().getScreenSize().getWidth())
			nbOfClientsOnScreen=0;
		frame.setLocation(xLocation, 0);
		frame.setSize(390, 300);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		nbOfClientsOnScreen=0;
		Broker.nbOfBrokersOnScreen=0;
		String[] argss = {
//				BooleanOption.network.toString()
//				,BooleanOption.desktop.toString()
//				,BooleanOption.autoConnectMadkitWebsite.toString()
				LevelOption.agentLogLevel.toString(),"INFO",
				LevelOption.kernelLogLevel.toString(),"OFF",
				Option.launchAgents.toString(),
								 Client.class.getName()+",true,2;"+
								 Broker.class.getName()+",true,3;"+
								 Provider.class.getName()+",true,7"
		};
		Madkit.main(argss);
	}
}
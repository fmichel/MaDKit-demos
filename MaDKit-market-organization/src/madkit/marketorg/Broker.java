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
import java.util.Arrays;
import java.util.List;

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
public class Broker extends Agent
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1217908977100108396L;
	static int nbOfBrokersOnScreen=0;
	private JPanel blinkPanel;
	public static List<String> availableTransports = Arrays.asList("train","boat","plane","bus");
	private static ImageIcon brokerImage= new ImageIcon(new ImageIcon(Client.class.getResource("images/broker.png")).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH));

	public void activate()
	{
		createGroupIfAbsent("travel","travel-clients",true,null);
		createGroupIfAbsent("travel","travel-providers",true,null);
		requestRole("travel","travel-providers", "broker",null);
		requestRole("travel","travel-clients", "broker",null);
	}


	@SuppressWarnings("unchecked")
	public void live()
	{
		while (true)
		{
			Message m;
			m = purgeMailbox();
			if (m == null) {
				m = waitNextMessage();
			}
			String role = m.getSender().getRole();
			if(role.equals("client")){
				handleClientRequest((ObjectMessage<String>) m);
			}
		}
	}

	private void handleClientRequest(ObjectMessage<String> request) {
		if(! request.getSender().exists())
			return;
		if (hasGUI()) {
			blinkPanel.setBackground(Color.YELLOW);
		}
		if(logger != null)
			logger.info("I received a request for a "+request.getContent()+" \nfrom "+request.getSender());
		List<Message> bids = broadcastMessageWithRoleAndWaitForReplies(
				"travel",
				"travel-providers",  
				request.getContent()+"-provider",
				new ObjectMessage<String>("make-bid-please"),
				"broker",
				900);
		if(bids == null){
			if(logger != null)
				logger.info("No bids at all !!");
			if (hasGUI()) {
				blinkPanel.setBackground(Color.LIGHT_GRAY);
			}
			return;
		}
		Message m = selectBestBid(bids);
		if (m != null) {
			if(logger != null)
				logger.info("There is one interesting offer from "+m.getSender());
			String contractGroupId = ""+System.nanoTime();
			Message ack = sendMessageWithRoleAndWaitForReply(
					m.getSender(),
					new ObjectMessage<String>(contractGroupId), 
					"broker",
					1000);
			if(ack == null){
				if(logger != null)
					logger.info("Provider disappears !!");
				return;
			}
			if(logger != null)
				logger.info("Provider is ready !\nSending the contract number to client");
			sendReply(request, new ObjectMessage<String>(contractGroupId)); 
			pause((int) (Math.random()*2000+1000));//let us celebrate !!
		}
		if (hasGUI()) {
			blinkPanel.setBackground(Color.LIGHT_GRAY);
		}
	}



	@SuppressWarnings("unchecked")
	private Message selectBestBid(List<Message> bids) {
		ObjectMessage<Integer> best = (ObjectMessage<Integer>) bids.get(0);
		for(Message m : bids){
			ObjectMessage<Integer> offer = (ObjectMessage<Integer>) m;
			if(best.getContent() > offer.getContent()){
				best = offer;
			}
		}
		return best;
	}

	@Override
	public void setupFrame(JFrame frame) {
		JPanel p = new JPanel(new BorderLayout());
		//customizing but still using the OutputPanel from MaDKit GUI
		p.add(new OutputPanel(this),BorderLayout.CENTER);
		blinkPanel = new JPanel();
		blinkPanel.add(new JLabel(brokerImage));
		p.add(blinkPanel,BorderLayout.NORTH);
		blinkPanel.setBackground(Color.LIGHT_GRAY);
		p.validate();
		frame.add(p);
		int xLocation = nbOfBrokersOnScreen++*390;
		if(xLocation+390 > Toolkit.getDefaultToolkit().getScreenSize().getWidth())
			nbOfBrokersOnScreen=0;
		frame.setLocation(xLocation, 320);
		frame.setSize(390, 300);
	}

}








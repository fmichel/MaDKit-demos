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
package madkit.bees;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import madkit.action.KernelAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Message;
import madkit.message.EnumMessage;

/**
 * The agent that launches the simulation
 * 
 * @version 2.0.0.3
 * @author Fabien Michel
 */
public class BeeLauncher extends madkit.kernel.Agent
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7558468322705057201L;
	public static final String SIMU_GROUP = "bees";
	public static final String BEE_ROLE = "bee";
	public static final String QUEEN_ROLE = "queen";
	public static final String FOLLOWER_ROLE = "follower";
	public static final String VIEWER_ROLE = "viewer";
	public static final String SCHEDULER_ROLE = "scheduler";
	public static final String LAUNCHER_ROLE = "launcher";
	public static final String COMMUNITY = "buzz";
	//	public String communityID = COMMUNITY;

	private int beenumber = 30000;
	private ArrayList<AbstractAgent> queensList = new ArrayList<AbstractAgent>();
	private ArrayList<AbstractAgent> beesList = new ArrayList<AbstractAgent>(beenumber*2);
	private boolean randomMode=true;
	private BeeViewer beeViewer;
	private boolean alive = true;

	protected void activate()
	{
		//	communityID = COMMUNITY;
		//	int i=2;
		//	while(isCommunity(communityID)){
		//		communityID+="_"+i++;
		//	}
		setLogLevel(Level.INFO);
		if(logger != null)
			logger.info("Launching bees simulation...");
		createGroup(COMMUNITY,SIMU_GROUP,false,null);
		requestRole(COMMUNITY,SIMU_GROUP,LAUNCHER_ROLE,null);


		long timeL = System.nanoTime();
		launchBees(beenumber);
		if(logger != null)
			logger.fine("launch time : "+(System.nanoTime()-timeL));

//		launchQueens(1);
		BeeScheduler beeScheduler = new BeeScheduler(COMMUNITY);
		beeViewer=new BeeViewer(beeScheduler,COMMUNITY);
		launchAgent(beeViewer,true);

		launchAgent(beeScheduler,false);

	}

	/**
	 * So that I can react to {@link KernelAction#EXIT} message
	 */
	@SuppressWarnings("unused")
	private void exit() {
		alive = false;
	}

	/**
	 * So that I can react to {@link BeeLauncherAction#RANDOM_MODE} message
	 */
	@SuppressWarnings("unused")
	private void randomMode(boolean on){
		randomMode = on;
	}

	enum BeeLauncherAction{

		RANDOM_MODE,
		LAUNCH_BEES,
	}

	protected void live()
	{
		while(alive)
		{
			Message m = waitNextMessage(500 + (int) (Math.random()*2000) );
			if(m != null){
				proceedEnumMessage((EnumMessage<?>) m);
			}
			if(randomMode)
			{
				killBees(false, 150);
				if(Math.random()<.8)
				{
					if(Math.random()<.5)
					{
						if(queensList.size()>1)
							if(queensList.size()>7)
								killBees(true, (int) (Math.random()*7) + 1);
							else
								killBees(true, (int) (Math.random()*2) + 1);
					}
					else if(queensList.size() < 10)
						launchQueens((int) (Math.random()*2) + 1);
				}
				else
					if(Math.random()<.3)
					{
						if(beesList.size() < 200000 && Runtime.getRuntime().freeMemory() > 100000){
							launchBees((int) (Math.random()*15000) + 5000);
							//						System.err.println(Runtime.getRuntime().freeMemory());
						}
					}
					else{
						killBees(false, (int) (Math.random()*500) + 1);
					}
			}		
		}
	}

	@Override
	protected void end() {
//		queensList.clear();
		queensList = null;
//		beesList.clear();
		beesList = null;
		if(logger != null)
			logger.info("I am done. Bye !");
	}

	private void launchBees(int numberOfBees)
	{
		if(logger != null)
			logger.info("Launching "+numberOfBees+" bees");
			beesList.addAll(
					launchAgentBucket(
							Bee.class.getName(), 
							numberOfBees, 
							COMMUNITY+";"+SIMU_GROUP+";"+BEE_ROLE, 
							COMMUNITY+";"+SIMU_GROUP+";"+FOLLOWER_ROLE));
	}	

	private void launchQueens(int numberOfQueens)
	{
		if(logger != null)
			logger.info("Launching "+numberOfQueens+" queen bees");
		queensList.addAll(launchAgentBucket(
				QueenBee.class.getName(), 
				numberOfQueens, 
				COMMUNITY+";"+SIMU_GROUP+";"+BEE_ROLE, 
				COMMUNITY+";"+SIMU_GROUP+";"+QUEEN_ROLE));
	}	

	private void killBees(boolean queen,int number)
	{
		List<AbstractAgent> l;
		int j = 0;
		if(queen)
			l = queensList;
		else
			l = beesList;
		for(final Iterator<AbstractAgent> i = l.iterator();i.hasNext() && j < number;j++)
		{
			if (j % 100 == 0) {
				Thread.yield();
			}
			final AbstractAgent a = i.next();
			if(a != null)
			{
				i.remove();
				killAgent(a);
			}
			else
				break;
		}
	}

	public static void main(String[] args) {
		executeThisAgent(1,false);
	}

}

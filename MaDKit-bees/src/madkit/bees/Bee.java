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

import static madkit.bees.BeeLauncher.COMMUNITY;
import static madkit.bees.BeeLauncher.QUEEN_ROLE;
import static madkit.bees.BeeLauncher.SIMU_GROUP;

import java.awt.Point;
import java.util.List;

import madkit.kernel.AgentAddress;
import madkit.message.ObjectMessage;

/**
 * @version 2.0.0.2
 * @author Fabien Michel, Olivier Gutknecht 
*/
public class Bee extends AbstractBee
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2393301912353816186L;
	BeeInformation leaderInfo = null;
	AgentAddress leader = null;


	/** The "do it" method called by the activator */
	public void buzz()
	{
		updateLeader();
		super.buzz();
	}  
	
	@Override
	protected void computeNewVelocities() {
		final Point location = myInformation.getCurrentPosition();
		int dtx, dty, dist;		// distances from bee to queen
		int acc = 0;
		if (beeWorld != null) {
			acc = beeWorld.getBeeAcceleration().getValue();
		}
		if (leaderInfo != null)
		{
			final Point leaderLocation = leaderInfo.getCurrentPosition();
			if(logger != null)
				logger.finer("following leader :"+leaderLocation);
			dtx = leaderLocation.x - location.x;
			dty = leaderLocation.y - location.y;
		}
		else
		{
			dtx = generator.nextInt(5);
			dty = generator.nextInt(5);
			if(generator.nextBoolean()){
				dtx = -dtx;
				dty = -dty;
			}
		}	
		dist = Math.abs(dtx) + Math.abs(dty);
		if (dist == 0) 
			dist = 1;		// avoid dividing by zero
		// the randomFromRange adds some extra jitter to prevent the bees from flying in formation
		dX += ((dtx * acc )/dist) + randomFromRange(2);
		dY += ((dty * acc )/dist) + randomFromRange(2);
	}

	/**
	 * 
	 */
	private void updateLeader() {
		ObjectMessage<BeeInformation> m = (ObjectMessage<BeeInformation>) nextMessage();
		if(m == null){
			return;
		}
		if(m.getSender().equals(leader)){
			if(logger != null)
				logger.info("my leader is getting out of the game");
			leader = null;
			leaderInfo = null;
		}
		else{
			if(logger != null)
				logger.fine("A leader has appeared or disappeared ");
			if(leader == null)
				followNewLeader(m);
			else{
				List<AgentAddress> queens = getAgentsWithRole(COMMUNITY, SIMU_GROUP, QUEEN_ROLE);
				if(queens != null && generator.nextDouble() < (1.0 / queens.size())){
					followNewLeader(m);
				}
			}
		}

	}
	/**
	 * @param leaderMessage
	 */
	private void followNewLeader(ObjectMessage<BeeInformation> leaderMessage) {
		leader = leaderMessage.getSender();
		leaderInfo = leaderMessage.getContent();
		myInformation.setBeeColor(leaderInfo.getBeeColor());
	}

	public void activate()
	{
//		setLogLevel(Level.INFO);
		requestRole("buzz",SIMU_GROUP,"bee",null);
		requestRole("buzz",SIMU_GROUP,"follower",null);
	}


	/* (non-Javadoc)
	 * @see madkit.demos.bees.AbstractBee#getMaxVelocity()
	 */
	@Override
	protected int getMaxVelocity() {
		if (beeWorld != null) {
			return beeWorld.getBeeVelocity().getValue();
		}
		return 0;
	}
}

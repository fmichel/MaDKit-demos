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
package madkit.pingpong;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;

/**
 * @author Fabien Michel
 * @version 2.0
 * 
 */
public class PingPongDemoLauncherAgent extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -724796019973276140L;
	private List<AbstractAgent> agentsList = new ArrayList<>();

	@Override
	protected void activate() {
//		setLogLevel(Level.ALL);
		int initialPause = 1000;
		int screenWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()-200);
		int screenHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()-200);
		for (int i = 0; i < screenWidth;i=i+400) {
			for (int j = 0; j < screenHeight; j = j +300) {
//				if(j==900) return;
				PingPong p = new PingPong();
				if(launchAgent(p, true) == ReturnCode.SUCCESS){
					agentsList.add(p);
					if(logger != null)
						logger.info("Ping Pong launched");
					p.setFrameLocation(i,j);
					if(logger != null)
						logger.info("Ping Pong launched");
					pause((initialPause > 0 ? initialPause : 20));
					initialPause-=Math.random()*100;
				}
			}
		}
	}
	
	@Override
	protected void live() {
		pause(6000);
		int initialPause = 2000;
		while(! agentsList.isEmpty()){
			AbstractAgent a = agentsList.remove((int) (agentsList.size()*Math.random())); 
			if(killAgent(a) != ReturnCode.SUCCESS)
				continue;
			pause((initialPause > 0 ? initialPause : 100));
			initialPause-=Math.random()*100;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		executeThisAgent(1,false);
	}

}

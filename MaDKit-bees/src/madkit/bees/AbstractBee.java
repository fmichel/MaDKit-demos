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

import java.awt.Color;
import java.awt.Point;
import java.util.Random;

import madkit.kernel.AbstractAgent;

/**
 * @version 2.0.0.3
 * @author Fabien Michel
 */
public abstract class AbstractBee extends AbstractAgent
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3781069534623890714L;

	final protected static Random generator = new Random(System.currentTimeMillis());

	public BeeInformation myInformation;//public visibility speeds up the probing process
	
	protected int dX;		// current velocity in x dir
	protected int dY;		// current velocity in y dir

	protected BeeEnvironment beeWorld;

	public AbstractBee()
	{
		myInformation = new BeeInformation();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+" on "+beeWorld+" "+myInformation;
	}


	protected void buzz()
	{
		Point location = myInformation.getCurrentPosition();
		myInformation.getPreviousPosition().setLocation(location);

		computeNewVelocities();
		normalizeVelocities(getMaxVelocity());
		// update the bee's position
		location.x += dX;
		location.y += dY;
		
	}


	protected abstract int getMaxVelocity();

	protected abstract void computeNewVelocities();


	private void normalizeVelocities(int maxVelocity) {
		// keep speed limited to maximums
		if (dX > maxVelocity)
			dX = maxVelocity;
		else
			if (dX < -maxVelocity)
				dX = -maxVelocity;

		if (dY > maxVelocity)
			dY = maxVelocity;
		else
			if (dY < -maxVelocity)
				dY = -maxVelocity;
	}


	public int randomFromRange(int val)
	{
		val /= 2;
		return generator.nextInt(val*2+1) - val;
	}

	public void setEnvironment(BeeEnvironment bw)
	{
		beeWorld = bw;
		final Point myLocation = myInformation.getCurrentPosition();
		if (myLocation.x > beeWorld.getWidth() || 
				myLocation.y > beeWorld.getHeight() ||
				myLocation.x <= 0 ||
				myLocation.y <= 0){
			myInformation.getCurrentPosition().setLocation(generator.nextInt(beeWorld.getWidth() - 20) + 10, generator.nextInt(beeWorld.getHeight() - 20) + 10);
			myInformation.getPreviousPosition().setLocation(myInformation.getCurrentPosition());
		}
		int beeMAcceleration = beeWorld.getBeeAcceleration().getValue();
		dX = randomFromRange(beeMAcceleration);
		dY = randomFromRange(beeMAcceleration);      
	}

}


class BeeInformation {
	final private Point currentPosition, previousPosition;
	private Color beeColor;
	
	public BeeInformation() {
		currentPosition = new Point();
		previousPosition = new Point();
		beeColor = Color.getHSBColor(AbstractBee.generator.nextFloat(),1.0f,1.0f);
	}

	/**
	 * @return the currentPosition
	 */
	public Point getCurrentPosition() {
		return currentPosition;
	}

	/**
	 * @return the previousPosition
	 */
	public Point getPreviousPosition() {
		return previousPosition;
	}
	
	@Override
	public String toString() {
		return "<"+previousPosition+","+currentPosition+">";
	}


	/**
	 * @return the beeColor
	 */
	public Color getBeeColor() {
		return beeColor;
	}

	/**
	 * @param beeColor the beeColor to set
	 */
	public void setBeeColor(Color beeColor) {
		this.beeColor = beeColor;
	}
}


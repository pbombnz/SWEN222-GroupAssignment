package game.objects.scene;

import game.objects.GameObject;

import javax.swing.ImageIcon;

/**
 * Simple class used for the fence object on the board
 * which is a decorative feature, no interaction possible with this object
 * 
 *@author Sushant Balajee
 *@author Donald Tang
 */

public class Fence implements GameObject {

	private static final long serialVersionUID = -5093601402600649692L;

	public Fence(){
	}	
	
	@Override
	public ImageIcon getSpriteImage() {
		return GameObject.FENCE_RIGHT;
	}

}


package game.objects;

import javax.swing.ImageIcon;

public class Tree implements GameObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4087421334363366222L;

	@Override
	public ImageIcon getSpriteImage() {
		return GameObject.TREE;
	}

}
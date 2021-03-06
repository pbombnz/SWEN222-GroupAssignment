package game.objects.monster;

import game.Direction;
import game.objects.GameObject;

import javax.swing.ImageIcon;

import sun.java2d.pipe.SpanClipRenderer;

/**
 * Represents a Zubat enemy, the second weakest enemy in the game
 * 
 *@author Sushant Balajee
 *@author Donald Tang
 */

public class Zubat extends Monster {

	private static final long serialVersionUID = 6696995125055330253L;

	public int health;
	public int attack;
	public Direction direction = Direction.FACE_RIGHT;

	public Zubat() {
		spriteImage = GameObject.ZUBATFL;
	}

	public Zubat(int attack, int health) {
		this.attack = attack;
		this.health = health;
	}

	@Override
	public String getName() {
		return "Zubat";
	}

	@Override
	public int attack() {
		return attack;
	}

	@Override
	public int getHealth() {
		return health;
	}

	public void setHealth(int change) {
		health = change;
		if (health < 0) {
			health = 0;
		}
	}

	public boolean isDead() {
		return health <= 0;
	}

	@Override
	public ImageIcon getSpriteImage() {
		if(direction==Direction.BACK_LEFT){
			return GameObject.ZUBATBL;
		}else if(direction==Direction.BACK_RIGHT){
			return GameObject.ZUBATBR;
		}else if(direction==Direction.FACE_LEFT){
			return GameObject.ZUBATFL;
		}else {
			return GameObject.ZUBATFR;
		}
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	@Override
	public Direction getDirection() {
		return direction;
	}
}

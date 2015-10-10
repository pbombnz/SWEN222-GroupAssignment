package game.avatar;

import java.io.Serializable;

import javax.swing.ImageIcon;

public class Evolution implements Serializable {
	private static final long serialVersionUID = -4465642051843622394L;
	
	private final String name;
	private final ImageIcon displayPictureGIF;
	private final ImageIcon faceLeft;
	private final ImageIcon faceRight;
	private final ImageIcon backLeft;
	private final ImageIcon backRight;
	private final ImageIcon attackGIF;
	private final ImageIcon dieGIF;
	private final ImageIcon evolvingGIF;

	public Evolution() {
		this.name = null;
		this.displayPictureGIF = null;
		this.faceLeft = null;
		this.faceRight = null;
		this.backLeft = null;
		this.backRight = null;
		this.attackGIF = null;
		this.dieGIF = null;
		this.evolvingGIF = null;	
	}
	
	public Evolution(String name, ImageIcon displayPictureGIF,
			ImageIcon faceLeft, ImageIcon faceRight, ImageIcon backLeft,
			ImageIcon backRight, ImageIcon attackGIF, ImageIcon dieGIF, ImageIcon evolvingGIF) {
		super();
		this.name = name;
		this.displayPictureGIF = displayPictureGIF;
		this.faceLeft = faceLeft;
		this.faceRight = faceRight;
		this.backLeft = backLeft;
		this.backRight = backRight;
		this.attackGIF = attackGIF;
		this.dieGIF = dieGIF;
		this.evolvingGIF = evolvingGIF;
	}	

	public Evolution(String name, ImageIcon displayPictureGIF,
			ImageIcon faceLeft, ImageIcon faceRight, ImageIcon backLeft,
			ImageIcon backRight, ImageIcon attackGIF, ImageIcon dieGIF) {
		this.name = name;
		this.displayPictureGIF = displayPictureGIF;
		this.faceLeft = faceLeft;
		this.faceRight = faceRight;
		this.backLeft = backLeft;
		this.backRight = backRight;
		this.attackGIF = attackGIF;
		this.dieGIF = dieGIF;
		this.evolvingGIF = null;
	}

	public String getName() {
		return name;
	}

	public ImageIcon getDisplayPictureGIF() {
		return displayPictureGIF;
	}
	
	public ImageIcon getFaceLeft() {
		return faceLeft;
	}

	public ImageIcon getFaceRight() {
		return faceRight;
	}

	public ImageIcon getBackLeft() {
		return backLeft;
	}

	public ImageIcon getBackRight() {
		return backRight;
	}

	public ImageIcon getAttackGIF() {
		return attackGIF;
	}

	public ImageIcon getDieGIF() {
		return dieGIF;
	}

	public ImageIcon getEvolvingGIF() {
		return evolvingGIF;
	}
}
package rooms;

import java.io.Serializable;

/**
 *@author Wang Zhen
 */
public class Room4 extends Room {
	private static final long serialVersionUID = -6734434217984592426L;

	public Room4() {
		this.level=4;
		this.roomName = "Final Stage";
		this.board = new Board4();
	}

	public String getName() {
		return roomName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((board == null) ? 0 : board.hashCode());
		result = prime * result
				+ ((roomName == null) ? 0 : roomName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Room1 other = (Room1) obj;
		if (board == null) {
			if (other.board != null)
				return false;
		} else if (!board.equals(other.board))
			return false;
		if (roomName == null) {
			if (other.roomName != null)
				return false;
		} else if (!roomName.equals(other.roomName))
			return false;
		return true;
	}

	public Board getBoard() {
		return board;

	}
}

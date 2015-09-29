package game;


public class Location {
	private Room room;
	private int x;
	private int y;
	
	public Location(Room room, int x, int y) {
		super();
		this.room = room;
		this.x = x;
		this.y = y;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	public void moveNorth() {
		this.y = this.y - 1;
	}

	public void moveEast() {
		this.x = this.x + 1;
	}
	
	public void moveSouth() {
		this.y = this.y - 1;
	}
	
	public void moveWest() {
		this.x = this.x - 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((room == null) ? 0 : room.hashCode());
		result = prime * result + x;
		result = prime * result + y;
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
		Location other = (Location) obj;
		if (room == null) {
			if (other.room != null)
				return false;
		} else if (!room.equals(other.room))
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	

}
package server.model.objects;


public class Objects {

	public int objectId;
	public int objectX;
	public int objectY;
	public int objectHeight;
	public int objectFace;
	public int objectType;
	public int objectTicks;
	
	
	public Objects(int id, int x, int y, int height, int face, int type, int ticks) {
		this.objectId = id;
		this.objectX = x;
		this.objectY = y;
		this.objectHeight = height;
		this.objectFace = face;
		this.objectType = type;
		this.objectTicks = ticks;
	}
	

	public int getObjectId() {
		return this.objectId;
	}
	
	public int getObjectX() {
		return this.objectX;
	}
	
	public int getObjectY() {
		return this.objectY;
	}
	
	public int getObjectHeight() {
		return this.objectHeight;
	}
	
	public int getObjectFace() {
		return this.objectFace;
	}
	
	public int getObjectType() {
		return this.objectType;
	}
	
	
}
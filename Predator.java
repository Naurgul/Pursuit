import java.io.*;
import java.lang.*;
import java.util.*;

/** This class defines the functionality of the predator. */
public class Predator extends Agent
{
	public enum AgentType{PREY, PREDATOR};
	public enum Direction{UP, DOWN, LEFT, RIGHT, NONE};
	
	class Position
	{
		@Override
		public String toString()
		{
			return "(" + x + ", " + y + ")";
		}

		public int x;
		public int y;
		
		Position(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (!(obj instanceof Position))
			{
				return false;
			}
			Position other = (Position) obj;
			if (!getOuterType().equals(other.getOuterType()))
			{
				return false;
			}
			if (x != other.x)
			{
				return false;
			}
			if (y != other.y)
			{
				return false;
			}
			return true;
		}

		private Predator getOuterType()
		{
			return Predator.this;
		}
	}

	class ObjectSeen
	{
		public AgentType agent;
		public int x;
		public int y;

		ObjectSeen(AgentType agent, int x, int y)
		{
			this.agent = agent;
			this.x = x;
			this.y = y;
		}
	}

	public LinkedList<ObjectSeen> seen;
	public Direction defaultDir;
	public Position me;

	public Predator()
	{
		seen = new LinkedList<ObjectSeen>();
		defaultDir = Direction.NONE;
		me = new Position(0,0);
	}

	/**
	 * This method initialize the predator by sending the initialization message
	 * to the server.
	 */
	public void initialize() throws IOException
	{
		g_socket.send("(init predator)");
	}

	/**
	 * This message determines a new movement command. Currently it only moves
	 * random. This can be improved..
	 */
	public String determineMovementCommand()
	{
		Direction dir = determineMovementDirection();
		if (dir.equals(Direction.UP))
		{
			return ("(move north)");
		}
		else if (dir.equals(Direction.DOWN))
		{
			return ("(move south)");
		}
		else if (dir.equals(Direction.LEFT))
		{
			return ("(move west)");
		}
		else if (dir.equals(Direction.RIGHT))
		{
			return ("(move east)");
		}
		else
		{
			return ("(move none)");
		}
	}
	
	private Direction determineMovementDirection()
	{
		Position target = me;
		int bestDist = Integer.MAX_VALUE;
		
		for (ObjectSeen o : seen)
		{
			if (o.agent.equals(AgentType.PREY))
			{
				int dist = Math.abs(o.x) + Math.abs(o.y);
				if (dist < bestDist)
				{
					bestDist = dist;
					target = new Position (o.x, o.y);
				}
			}
		}
		
		//cover shortest dimension first
		if (getDistance(getNewPos(me, defaultDir), target) < getDistance(me, target))
		{
			return defaultDir;
		}
		if (Math.abs(target.x) <= Math.abs(target.y) && Math.abs(target.x) != 0)
		{
			if (target.x > 0)
			{
				defaultDir = Direction.RIGHT;
			}
			else 
			{
				defaultDir = Direction.LEFT;
			}
		}
		else
		{
			if (target.y > 0)
			{
				defaultDir = Direction.UP;
			}
			else
			{
				defaultDir = Direction.DOWN;
			}
		}
		
		return defaultDir;
	}
	
	private int getDistance(Position pos1, Position pos2)
	{
		
		int xDist = pos1.x - pos2.x;
		if (xDist > 7)
		{
			xDist -= 15;
		}
		else if (xDist < -7)
		{
			xDist += 15;
		}
		
		int yDist = pos1.y - pos2.y;
		if (yDist > 7)
		{
			yDist -= 15;
		}
		else if (yDist < -7)
		{
			yDist += 15;
		}
		
		return Math.abs(xDist) + Math.abs(yDist);
	}
	
	private Position getNewPos(Position pos, Direction dir)
	{
		Position newPos = new Position(pos.x, pos.y);
		if (dir.equals(Direction.UP))
		{
			newPos.y++;
		}
		else if (dir.equals(Direction.DOWN))
		{
			newPos.y--;
		}
		else if (dir.equals(Direction.RIGHT))
		{
			newPos.x++;
		}
		else if (dir.equals(Direction.LEFT))
		{
			newPos.x--;
		}
		return newPos;
	}
	

	/**
	 * This method processes the visual information. It receives a message
	 * containing the information of the preys and the predators that are
	 * currently in the visible range of the predator.
	 */
	public void processVisualInformation(String strMessage)
	{
		int i = 0, x = 0, y = 0;
		String strName = "";
		StringTokenizer tok = new StringTokenizer(strMessage.substring(5),
				") (");

		seen.clear();
		while (tok.hasMoreTokens())
		{
			if (i == 0)
				strName = tok.nextToken(); // 1st = name
			if (i == 1)
				x = Integer.parseInt(tok.nextToken()); // 2nd = x coord
			if (i == 2)
				y = Integer.parseInt(tok.nextToken()); // 3rd = y coord
			if (i == 2)
			{
				if (strName.equals("prey"))
				{
					seen.add(new ObjectSeen(AgentType.PREY, x, y));
				} else if (strName.equals("predator"))
				{
					seen.add(new ObjectSeen(AgentType.PREDATOR, x, y));
				}
			}
			i = (i + 1) % 3;
		}

	}

	/**
	 * This method is called after a communication message has arrived from
	 * another predator.
	 */
	public void processCommunicationInformation(String strMessage)
	{
		// TODO: exercise 3 to improve capture times
	}

	/**
	 * This method is called and can be used to send a message to all other
	 * predators. Note that this only will have effect when communication is
	 * turned on in the server.
	 */
	public String determineCommunicationCommand()
	{
		// TODO: exercise 3 to improve capture times
		return "";
	}

	/**
	 * This method is called when an episode is ended and can be used to reset
	 * some variables.
	 */
	public void episodeEnded()
	{
		// this method is called when an episode has ended and can be used to
		// reinitialize some variables
		System.out.println("EPISODE ENDED\n");
	}

	/**
	 * This method is called when this predator is involved in a collision.
	 */
	public void collisionOccured()
	{
		// this method is called when a collision occured and can be used to
		// reinitialize some variables
		System.out.println("COLLISION OCCURED\n");
	}

	/**
	 * This method is called when this predator is involved in a penalization.
	 */
	public void penalizeOccured()
	{
		// this method is called when a predator is penalized and can be used to
		// reinitialize some variables
		System.out.println("PENALIZED\n");
	}

	/**
	 * This method is called when this predator is involved in a capture of a
	 * prey.
	 */
	public void preyCaught()
	{
		System.out.println("PREY CAUGHT\n");
	}

	public static void main(String[] args)
	{
		Predator predator = new Predator();
		if (args.length == 2)
			predator.connect(args[0], Integer.parseInt(args[1]));
		else
			predator.connect();
	}

}

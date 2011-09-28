import java.io.*;
import java.lang.*;
import java.util.*;

/** This class defines the functionality of the predator. */
public class Predator extends Agent
{
	public enum AgentType{PREY, PREDATOR};
	public enum Direction{UP, DOWN, LEFT, RIGHT, NONE};

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

	public Predator()
	{
		seen = new LinkedList<ObjectSeen>();
		
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
		int targetX = 0;
		int targetY = 0;
		int bestDist = Integer.MAX_VALUE;
		
		for (ObjectSeen o : seen)
		{
			if (o.agent.equals(AgentType.PREY))
			{
				int dist = Math.abs(o.x) + Math.abs(o.y);
				if (dist < bestDist)
				{
					bestDist = dist;
					targetX = o.x;
					targetY = o.y;
				}
			}
		}
		
		//select path randomly
		if (new Random().nextInt(2) == 0)
		{
			if (targetX > 0)
			{
				return Direction.RIGHT;
			}
			else if (targetX < 0)
			{
				return Direction.LEFT;
			}
			else if (targetY > 0)
			{
				return Direction.UP;
			}
			else if (targetY < 0)
			{
				return Direction.DOWN;
			}
		}
		else
		{
			if (targetY > 0)
			{
				return Direction.UP;
			}
			else if (targetY < 0)
			{
				return Direction.DOWN;
			}
			else if (targetX > 0)
			{
				return Direction.RIGHT;
			}
			else if (targetX < 0)
			{
				return Direction.LEFT;
			}
		}
		
		return Direction.NONE;
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

import java.io.*;
import java.lang.*;
import java.util.*;

/** This class defines the functionality of the predator. */
public class Predator extends Agent
{
	public enum AgentType{PREY, PREDATOR};
	public enum Direction{UP, DOWN, LEFT, RIGHT, NONE};
	private int targetID;
	
	class Position
	{
		public int x;
		public int y;
		
		Position(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
	}

	class ObjectSeen
	{
		public AgentType type;
		public Position pos;

		ObjectSeen(AgentType agent, int x, int y)
		{
			this.type = agent;
			this.pos = new Position(x, y);
		}
	}

	public LinkedList<ObjectSeen> seen;

	public Predator()
	{
		seen = new LinkedList<ObjectSeen>();
		targetID = -1;
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
		if (targetID == -1)
		{
			findTarget();			
		}

		return followTarget();
	}
	
	private void findTarget()
	{
		int id = 0;
		int minMax = 15;
		
		for (ObjectSeen prey : seen)
		{
			if (prey.type.equals(AgentType.PREY))
			{
				int max = Math.abs(prey.pos.x) - Math.abs(prey.pos.y);
				for (ObjectSeen predator : seen)
				{
					if (predator.type.equals(AgentType.PREDATOR))
					{
						int xDist = prey.pos.x - predator.pos.x;
						if (xDist > 7)
						{
							xDist -= 15;
						}
						else if (xDist < -7)
						{
							xDist += 15;
						}
						
						int yDist = prey.pos.y - predator.pos.y;
						if (yDist > 7)
						{
							yDist -= 15;
						}
						else if (yDist < -7)
						{
							yDist += 15;
						}
						
						int distance = Math.abs(xDist) + Math.abs(yDist);
						if (distance > max)
						{
							max = distance;
						}
					}
				}
				if (max < minMax)
				{
					minMax = max;
					targetID = id; 
				}
			}
			id++;
		}		
	}

	private Direction followTarget()
	{
		
		//we assume that targetID has a valid value here
		Position target = null;
		int id = 0;
		
		for (ObjectSeen o : seen)
		{
			if (targetID == id)
			{
				target = o.pos;
				break;
			}
			id++;
		}
		
		//select path randomly
		Direction move = Direction.NONE;
		if (new Random().nextInt(2) == 0)
		{
			if (target.x > 1)
			{
				move = Direction.RIGHT;
			}
			else if (target.x < -1)
			{
				move = Direction.LEFT;
			}
			else if (target.y > 1)
			{
				move = Direction.UP;
			}
			else if (target.y < -1)
			{
				move = Direction.DOWN;
			}
		}
		else
		{
			if (target.y > 1)
			{
				move = Direction.UP;
			}
			else if (target.y < -1)
			{
				move = Direction.DOWN;
			}
			else if (target.x > 1)
			{
				move = Direction.RIGHT;
			}
			else if (target.x < -1)
			{
				move = Direction.LEFT;
			}
		}		
		
		if(resultsInCollision(move))
		{
			return Direction.NONE;
		}
		else
		{
			return move;
		}
	}
	

	private boolean resultsInCollision(Direction move)
	{
		Position target = new Position(0,0);
		
		if (move.equals(Direction.UP))
		{
			target.y = 1;
		}		
		if (move.equals(Direction.DOWN))
		{
			target.y = -1;
		}
		if (move.equals(Direction.RIGHT))
		{
			target.x = 1;
		}
		if (move.equals(Direction.LEFT))
		{
			target.x = -1;
		}
		
		for (ObjectSeen other : seen)
		{
			if (other.type.equals(AgentType.PREDATOR))
			{
				if (Math.abs(target.x - other.pos.x) + Math.abs(target.y - other.pos.y) <= 1)
				{
					return true;
				}
			}
		}
		return false;
		
	}

	/**
	 * This method processes the vQisual information. It receives a message
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

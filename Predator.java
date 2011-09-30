import java.io.*;
import java.lang.*;
import java.util.*;

/** This class defines the functionality of the predator. */
public class Predator extends Agent
{
	private int targetID;
	private HashMap<Integer, Direction> roles;
	public LinkedList<ObjectSeen> seen;
	private int cycles;
	
	
	public enum AgentType{PREY, PREDATOR};
	public enum Direction{UP, DOWN, LEFT, RIGHT, NONE};
	
	class Position
	{
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
		public AgentType type;
		public Position pos;

		ObjectSeen(AgentType agent, int x, int y)
		{
			this.type = agent;
			this.pos = new Position(x, y);
		}
	}
	
	public class MoveComparator implements Comparator<Direction>
	{
		private Position predator;
		private Position target;
		private Direction role;
		private int maxDist;
		
		public MoveComparator(Position predator, Position target, Direction role, int maxDist)
		{
			this.predator = predator;
			this.target = target;
			this.role = role;
			this.maxDist = maxDist;
		}

	    @Override
	    public int compare(Direction dir1, Direction dir2) throws RuntimeException
	    {
	        int rank1 = getRank(dir1);
	        int rank2 = getRank(dir2);

	        if (rank1 > rank2)
	        {
	            return -1;
	        }
	        else if (rank1 < rank2)
	        {
	            return +1;
	        }
	        else
	        {   	
	        	//resolve with arbitrary rule
	        	if (arbitraryBetter(dir1, dir2))
	        	{
	        		return -1;
	        	}
	        	else
	        	{
	        		return +1;
	        	}
	        }
	    }
	    
		private int getRank(Direction dir)
		{
			int roleDist = getDistance(getNewPos(predator, dir), getNewPos(target, role));
			int targetDist = getDistance(getNewPos(predator, dir), target);
			int curDist = getDistance(predator, target);
			int minPredatorDist = getMinPredatorDist(getNewPos(predator, dir));
			int rank = 0;
			
			
			
			//rank is mainly affected by which move gets us closer to the target
			rank += 15 - roleDist;
			
			// if someone's really far away  we ought to wait up
			if (roleDist < 1 && maxDist - roleDist > 2)
			{
				rank -= 2*(maxDist - roleDist);
			}

			
			//a big boost if the predator is in the right quadrant
			if (inQuadrant(role, getNewPos(predator,dir), target))
			{
				rank += 2*Math.abs(rank);	
			}
			 
			

				
			

			
			//also, try to avoid bumping into other predators...
			//rank -= 9*minPredatorDist;		
			
			return rank;
		}

		private boolean arbitraryBetter(Direction dir1, Direction dir2)
		{
			//each role has a minor preference over the type of move to break ties
			if (role.equals(Direction.UP))	// down > left > right > up
			{
				if (dir1.equals(Direction.DOWN))
				{
					return true;
				}
				else if (dir2.equals(Direction.DOWN))
				{
					return false;
				}
				else if (dir1.equals(Direction.LEFT))
				{
					return true;
				}
				else if (dir2.equals(Direction.LEFT))
				{
					return false;
				}
				else if (dir1.equals(Direction.RIGHT))
				{
					return true;
				}
				else if (dir2.equals(Direction.RIGHT))
				{
					return false;
				}
				else if (dir1.equals(Direction.UP))
				{
					return true;
				}
				else if (dir2.equals(Direction.UP))
				{
					return false;
				}
			}
			else if (role.equals(Direction.DOWN))	// up > right > left > down
			{
				if (dir1.equals(Direction.UP))
				{
					return true;
				}
				else if (dir2.equals(Direction.UP))
				{
					return false;
				}
				else if (dir1.equals(Direction.RIGHT))
				{
					return true;
				}
				else if (dir2.equals(Direction.RIGHT))
				{
					return false;
				}
				else if (dir1.equals(Direction.LEFT))
				{
					return true;
				}
				else if (dir2.equals(Direction.LEFT))
				{
					return false;
				}
				else if (dir1.equals(Direction.DOWN))
				{
					return true;
				}
				else if (dir2.equals(Direction.DOWN))
				{
					return false;
				}
			}
			else if (role.equals(Direction.RIGHT))	// left > up > down > right
			{
				if (dir1.equals(Direction.LEFT))
				{
					return true;
				}
				else if (dir2.equals(Direction.LEFT))
				{
					return false;
				}
				else if (dir1.equals(Direction.UP))
				{
					return true;
				}
				else if (dir2.equals(Direction.UP))
				{
					return false;
				}				
				else if (dir1.equals(Direction.DOWN))
				{
					return true;
				}
				else if (dir2.equals(Direction.DOWN))
				{
					return false;
				}
				else if (dir1.equals(Direction.RIGHT))
				{
					return true;
				}
				else if (dir2.equals(Direction.RIGHT))
				{
					return false;
				}
			}
			else if (role.equals(Direction.LEFT))	//right > down > up > left
			{
				if (dir1.equals(Direction.RIGHT))
				{
					return true;
				}
				else if (dir2.equals(Direction.RIGHT))
				{
					return false;
				}
				else if (dir1.equals(Direction.DOWN))
				{
					return true;
				}
				else if (dir2.equals(Direction.DOWN))
				{
					return false;
				}
				else if (dir1.equals(Direction.UP))
				{
					return true;
				}
				else if (dir2.equals(Direction.UP))
				{
					return false;
				}
				else if (dir1.equals(Direction.LEFT))
				{
					return true;
				}
				else if (dir2.equals(Direction.LEFT))
				{
					return false;
				}
			}	
			//needless to say, this should never occur!
        	throw new RuntimeException("Could not find strict preference over directions.");
        	//return true;
		}



	}


	public Predator()
	{
		seen = new LinkedList<ObjectSeen>();
		targetID = -1;
		roles = new HashMap<Integer, Direction>();
		cycles = 0;
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
		cycles++;
		if (targetID == -1)
		{
			findTarget();
			castRoles();
		}
		
		return followTarget();
	}

	private void findTarget()
	{
		int id = 1;
		int minMax = 15;
		
		for (ObjectSeen prey : seen)
		{

			if (prey.type.equals(AgentType.PREY))
			{
				int max = getMaxDistance(prey);
				if (max < minMax)
				{
					minMax = max;
					targetID = id; 
				}
			}
			id++;
		}	
	}

	private void castRoles()
	{
		roles.clear();
		Position target = null;
		try
		{
			target = getObject(targetID).pos;	
		}
		catch (Exception e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
		
		for (Direction dir : Direction.values())
		{
			if (!dir.equals(Direction.NONE))
			{
				int roleID = -1;
				int min = 15;
				Position minPos = null;
				
				Position roleTarget = getNewPos(target, dir);
				if (!roles.containsKey(0))	//initialise role to self if we don't already have a role
				{
					roleID = 0;
					min = Math.abs(roleTarget.x) + Math.abs(roleTarget.y);
					minPos = new Position(0,0);
				}
				
				int id = 0;
				
				for (ObjectSeen predator : seen)
				{
					id++;
					if (predator.type.equals(AgentType.PREDATOR))
					{	
						if (roles.containsKey(id))
						{
							continue;
						}
						int distance = getDistance(roleTarget, predator.pos);
						if (distance < min || (distance == min && breakTie(predator.pos, minPos, roleTarget)))
						{
							roleID = id;
							minPos = predator.pos;
							min = distance;
						}
					}
				}
				if (roleID >= 0)
				{
					roles.put(roleID, dir);	
				}
				
			}
		}
		//System.out.println(roles.toString());
	}



	private Direction followTarget()
	{
		HashMap<Integer, LinkedList<Direction>> allMoves = new HashMap<Integer, LinkedList<Direction>>();
		
		ObjectSeen targetPrey = null;
		try
		{
			targetPrey = getObject(targetID);
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
		
		int maxDist = getMaxRoleDistance(targetPrey);
		
		//get lists for every predator's possible moves (ordered by preference) 
		allMoves.put(0, getMovesPreference(0, maxDist));
		int id = 1;
		for (ObjectSeen predator : seen)
		{
			if (predator.type.equals(AgentType.PREDATOR))
			{
				allMoves.put(id, getMovesPreference(id, maxDist));
				id++;	
			}			
		}
		
		//start with each predator's most preferred move
		HashMap<Integer, Position> nextSquare = new HashMap<Integer, Position>();
		HashMap<Integer, Direction> nextMove = new HashMap<Integer, Direction>();	
		for (int predatorID : allMoves.keySet())
		{
			Direction dir = allMoves.get(predatorID).pollFirst();
			ObjectSeen predator = null;
			try
			{
				predator = getObject(predatorID);
			}
			catch (Exception e)
			{
				System.err.println(e);
				e.printStackTrace();
			}
			Position newPos = getNewPos(predator.pos, dir);
			
			nextMove.put(predatorID, dir);
			nextSquare.put(predatorID, newPos);
		}

		
		//look through all pairs of predators
		boolean foundCollision;
		Random die = new Random();
		do
		{
			Integer prIds[] = nextSquare.keySet().toArray(new Integer[nextSquare.keySet().size()]);
			foundCollision = false;
			
			for (int i = 0; i < prIds.length-1; i++)
			{
				for (int j = i+1; j < prIds.length; j++)
				{
					Position iSquare = nextSquare.get(prIds[i]);
					Position jSquare = nextSquare.get(prIds[j]);
					
					//check if their plans lead to a collision
					if (iSquare.equals(jSquare))
					{
						foundCollision = true;
						
						//decide who gives way with an arbitrary deterministic rule 
						if (breakTie(iSquare, jSquare, targetPrey.pos))
						{
							if (allMoves.get(j).isEmpty())
							{
								throw new RuntimeException("Out of alternate moves!");
							}
							Direction dir = allMoves.get(j).pollFirst();
							ObjectSeen predator = null;
							try
							{
								predator = getObject(j);
							}
							catch (Exception e)
							{
								System.err.println(e);
								e.printStackTrace();
							}
							Position newPos = getNewPos(predator.pos, dir);
							
							//System.out.println("Changing move from " + nextMove.get(j) + " to " + dir + "(" + allMoves.get(j).size() + " alternates left)");
							nextMove.put(j, dir);
							nextSquare.put(j, newPos);
						}
						else 
						{
							if (allMoves.get(i).isEmpty())
							{
								throw new RuntimeException("Out of alternate moves!");
							}
							Direction dir = allMoves.get(i).pollFirst();
							ObjectSeen predator = null;
							try
							{
								predator = getObject(i);
							}
							catch (Exception e)
							{
								System.err.println(e);
								e.printStackTrace();
							}
							Position newPos = getNewPos(predator.pos, dir);
							
							//System.out.println("Changing move from " + nextMove.get(i) + " to " + dir + "(" + allMoves.get(i).size() + " alternates left)");
							nextMove.put(i, dir);
							nextSquare.put(i, newPos);
						}
						break;

					}					
				}
				if (foundCollision)
				{
					break;
				}
			}
			
			
		}while (foundCollision && die.nextInt(cycles) < 35);
		
		
		return nextMove.get(0);
	}
	

	/* 
	 	-1		no one
	 	 0		self
	 	 1+ 	others
	*/
	private ObjectSeen getObject(int id) throws Exception
	{
		//we assume that targetID has a valid value here
		Position pos = null;
		
		if (id < 0)
		{
			throw new Exception("Invalid object id");
		}
		else if (id == 0)
		{
			return new ObjectSeen(AgentType.PREDATOR, 0, 0);
		}
		else
		{
			return seen.get(id-1);
		}
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
	
	private boolean breakTie(Position pos1, Position pos2, Position ref)
	{
		int xDist1 = pos1.x - ref.x;
	
		if (xDist1 > 7)
		{
			xDist1 -= 15;
		}
		else if (xDist1 < -7)
		{
			xDist1 += 15;
		}		
		int yDist1 = pos1.y - ref.y;
		if (yDist1 > 7)
		{
			yDist1 -= 15;
		}
		else if (yDist1 < -7)
		{
			yDist1 += 15;
		}
		
		int xDist2 = pos2.x - ref.x;
		if (xDist2 > 7)
		{
			xDist2 -= 15;
		}
		else if (xDist2 < -7)
		{
			xDist2 += 15;
		}		
		int yDist2 = pos2.y - ref.y;
		if (yDist2 > 7)
		{
			yDist2 -= 15;
		}
		else if (yDist2 < -7)
		{
			yDist2 += 15;
		}
		
		if (xDist1 > xDist2)
		{
			return true;
		}
		else if (xDist1 == xDist2)
		{
			if (yDist1 > yDist2)
			{
				return true;
			}
		}
		
		return false;
	}
	
	private LinkedList<Direction> getMovesPreference(int id, int maxDist)
	{
		Position prPos = null;
		Position tPos = null;
		try
		{
			prPos = getObject(id).pos;
			tPos = getObject(targetID).pos;
		}
		catch (Exception e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
		LinkedList<Direction> moves = new LinkedList<Direction>();
		for (Direction d : Direction.values())
		{
			moves.add(d);
		}
		Collections.sort(moves, new MoveComparator(prPos, tPos, roles.get(id), maxDist));
		return moves;
	}
	
	private int getMaxDistance(ObjectSeen prey)
	{		
		int max = Math.abs(prey.pos.x) + Math.abs(prey.pos.y);
		for (ObjectSeen predator : seen)
		{
			if (predator.type.equals(AgentType.PREDATOR))
			{
				int distance = getDistance(prey.pos, predator.pos);
				if (distance > max)
				{
					max = distance;
				}

			}
		}
		return max;
	}
	
	private int getMinPredatorDist(Position pos)
	{
		int min = 15;
		
		for (ObjectSeen predator : seen)
		{
			if (predator.type.equals(AgentType.PREDATOR))
			{
				int dist = getDistance(pos, predator.pos);
				if (dist < min)
				{
					min = dist;
				}
			}
		}
		
		return min;
	}
	
	private int getMaxRoleDistance(ObjectSeen prey)
	{		
		int id=0;
		Direction dir = roles.get(id);
		int max = Math.abs(getNewPos(prey.pos, dir).x) + Math.abs(getNewPos(prey.pos, dir).x);		
		for (ObjectSeen predator : seen)
		{
			id++;
			if (predator.type.equals(AgentType.PREDATOR))
			{
				dir = roles.get(id);
				int distance = getDistance(getNewPos(prey.pos,dir), predator.pos);
				if (distance > max)
				{
					max = distance;
				}

			}
		}
		return max;
	}
	
	public boolean inQuadrant(Direction role, Position pos, Position ref)
	{
		float quad;
		if (role.equals(Direction.UP) && pos.x >= ref.x && pos.y > ref.y)
		{
			return true;
		}
		else if (role.equals(Direction.RIGHT) && pos.x > ref.x && pos.y <= ref.y)
		{
			return true;
		}
		else if (role.equals(Direction.DOWN) && pos.x <= ref.x && pos.y < ref.y)
		{
			return true;
		}
		else if (role.equals(Direction.LEFT) && pos.x < ref.x && pos.y >= ref.y)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Deprecated 
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
		targetID = -1;
		cycles = 0;
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

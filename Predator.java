

import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.Map.Entry;


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
	
	class FloatVector
	{
		public float x;
		public float y;
		
		FloatVector(float x, float y)
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString()
		{
			return "(" + x + ", " + y + ")";
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
		private Position targetNow;
		private Position targetExpected;
		private Direction role;
		
		public MoveComparator(Position predator, Position targetNow, Position targetExpected, Direction role)
		{
			this.predator = predator;
			this.targetNow = targetNow;
			this.targetExpected = targetExpected;
			this.role = role;
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
	        	//resolve tie with arbitrary rule
	        	if (breakTie(dir1, dir2))
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
			int roleDist = getDistance(getNewPos(predator, dir), getNewPos(targetExpected, role));
			int targetDist = getDistance(getNewPos(predator, dir), targetExpected);
			//int curDist = getDistance(predator, target);
			//int minPredatorDist = getMinPredatorDist(getNewPos(predator, dir));
			int maxDist = getMaxDistance(targetNow);
			Position preyDif = new Position(targetExpected.x- targetNow.x, targetExpected.y- targetNow.y);
			Position predatorDif = new Position(getNewPos(predator, dir).x- predator.x, getNewPos(predator, dir).y- predator.y);
			
			int rank = 0;
			
			//rank is mainly affected by which move gets us closer to the target
			rank = 15 - roleDist;
			
			//keep some distance until everyone's close
			if (maxDist > 3 && targetDist <= 1 && roleDist == 0)
			{
				//System.out.println("Waiting...");
				rank = 0;
			}
	
			
			//if the path ahead is obstructed or the prey is moving in the same direction as me
			//then go the other way
			//System.out.println("obstructed=" + (!hasFreePath(predator, targetNow) || !hasFreePath(predator, targetExpected)) + ", preyDif=" + preyDif + ", predatorDif=" + predatorDif);
			if (maxDist < 4 && !hasFreePath(predator, targetNow) /* || !hasFreePath(predator, targetExpected)  || preyDif.x * predatorDif.x > 0 || preyDif.y * predatorDif.y > 0 */ )
			{				
				rank = roleDist;
			}
		


/*			
			//weight the rank by how close to the right quadrant we are
			float quad = inQuadrant(role, getNewPos(predator,dir), target); 
			rank += 3*quad;				

			//also, try to avoid bumping into other predators...			
			rank += minPredatorDist/2;
*/								
			
			return rank;
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
 		else 
		{
			try
			{
				//ObjectSeen prey = getObject(targetID);
				if ( /* getMaxDistance(prey.pos) > 2  && */ cycles >= 20  )
				{
					System.out.println("Reset!");
					cycles = 0;
					castRoles();
				}
			}
			catch (Exception e)
			{
				System.err.println(e);
				e.printStackTrace();
			}
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
				int max = getMaxDistance(prey.pos);
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
			return;
		}
		

		LinkedList<Integer> predatorList = new LinkedList<Integer>();
		int id = 0;
		predatorList.add(id);
		for (ObjectSeen predator : seen)
		{
			id++;
			if (predator.type.equals(AgentType.PREDATOR))
			{	
				predatorList.add(id);
			}
		}
		
		boolean hasMore;
		int minRoleCost = 100;
		do
		{
			HashMap<Integer, Direction> tempRoles = new  HashMap<Integer, Direction>();
			int i=0;
			for (Direction dir : Direction.values())
			{
				if (!dir.equals(Direction.NONE))
				{
					tempRoles.put(predatorList.get(i), dir);
					i++;	
				}
			}
			int rolesCost = getRolesCost(tempRoles);
			if (rolesCost < minRoleCost || (rolesCost == minRoleCost && metaBreakTie(roles, tempRoles, target)))
			{
				//System.out.println("Switching " + roles + "(" + minRoleCost + ") with " + tempRoles + "(" + rolesCost + ")");
				roles = tempRoles;
				minRoleCost = rolesCost;
			}
			int k, l;
			hasMore = false;
			for(k = predatorList.size()-2; k >= 0; k--)
			{
				if (predatorList.get(k) < predatorList.get(k+1))
				{
					hasMore = true;
					break;
				}
			}
			
			if (!hasMore)
			{
				break;
			}

			for(l = predatorList.size()-1; l >= 0; l--)
			{
				if (predatorList.get(k) < predatorList.get(l))
				{
					break;
				}
			}

			Collections.swap(predatorList, k, l);
			List<Integer> lastPart = predatorList.subList(k+1, predatorList.size());
			Collections.reverse(lastPart);
		}
		while(true);	//condition is inside: if (!hasMore) break;
		
		//System.out.println("Roles: " + roles + "(" + minRoleCost + ")");
				
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
		
		
		//get lists for every predator's possible moves (ordered by preference) 
		allMoves.put(0, getMovesPreference(0));
		//System.out.println(roles.get(0) + ": " + getMovesPreference(0));
		int id = 1;
		for (ObjectSeen predator : seen)
		{
			if (predator.type.equals(AgentType.PREDATOR))
			{
				//System.out.println(roles.get(0) + ": " + getMovesPreference(id));
				allMoves.put(id, getMovesPreference(id));
				id++;	
			}			
		}
		
		//start with each predator's most preferred move
		HashMap<Direction, Position> nextSquare = new HashMap<Direction, Position>();
		HashMap<Direction, Direction> nextMove = new HashMap<Direction, Direction>();	
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
			
			nextMove.put(roles.get(predatorID), dir);
			nextSquare.put(roles.get(predatorID), newPos);
		}

		
		//look through all pairs of predators 
		//this operation yields different results if the order of the predators is different
		//so the list of predators used here should be exactly the same in each predator's instance
		boolean foundCollision;
		Random die = new Random();
		do
		{			
			LinkedList<Direction> predators = new LinkedList<Direction>(Arrays.asList(Direction.values()));
			predators.remove(Direction.NONE);
			//System.out.println(roles.get(0) + ": " + predators);
			
			foundCollision = false;			
			for (int i = 0; i < predators.size()-1; i++)
			{
				for (int j = i+1; j < predators.size(); j++)
				{
					Position iSquare = nextSquare.get(predators.get(i));
					Position jSquare = nextSquare.get(predators.get(j));
					int iID = getID(predators.get(i));
					int jID = getID(predators.get(j));					
					
					//check if their plans lead to a collision
					if (iSquare.equals(jSquare))
					{
						foundCollision = true;
						
						//decide who gives way with an (arbitrary) deterministic rule 
						if (breakTie(iSquare, jSquare, targetPrey.pos))
						{
							
							if (allMoves.get(jID).isEmpty())
							{
								throw new RuntimeException("Out of alternate moves!");
							}
							Direction dir = allMoves.get(jID).pollFirst();
							ObjectSeen predator = null;
							try
							{
								predator = getObject(jID);
							}
							catch (Exception e)
							{
								System.err.println(e);
								e.printStackTrace();
							}
							Position newPos = getNewPos(predator.pos, dir);
							
							//System.out.println(predators.get(j) + " changed his move from " + nextMove.get(predators.get(j)) + " to " + dir + " to avoid colliding with " + predators.get(i));
							nextMove.put(predators.get(j), dir);
							nextSquare.put(predators.get(j), newPos);
						}
						else 
						{
							if (allMoves.get(iID).isEmpty())
							{
								throw new RuntimeException("Out of alternate moves!");
							}
							Direction dir = allMoves.get(iID).pollFirst();
							ObjectSeen predator = null;
							try
							{
								predator = getObject(iID);
							}
							catch (Exception e)
							{
								System.err.println(e);
								e.printStackTrace();
							}
							Position newPos = getNewPos(predator.pos, dir);
							
							//System.out.println(predators.get(i) + " changed his move from " + nextMove.get(predators.get(i)) + " to " + dir + " to avoid colliding with " + predators.get(j));
							nextMove.put(predators.get(i), dir);
							nextSquare.put(predators.get(i), newPos);
						}
						break;

					}					
				}
				if (foundCollision)
				{
					break;
				}
			}
			
			
		}
		while (foundCollision && die.nextInt(cycles+1) < 10);
		
		
		return nextMove.get(roles.get(0));
	}
	



	/* 
	 	-1		no one
	 	 0		self
	 	 1+ 	others
	*/
	private ObjectSeen getObject(int id) throws Exception
	{
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
	
	private int getID(Direction role)
	{
		for (int id : roles.keySet())
		{
			if (roles.get(id).equals(role))
			{
				return id;
			}
		}
		return -1;
	}
	
	private int getDistance(Position pos1, Position pos2)
	{
		pos1 = fixPosition(pos1);
		pos2 = fixPosition(pos2);
		
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
	
	private Position fixPosition(Position pos)
	{
		if (pos.x > 7)
		{
			pos.x -= 15;
		}
		if (pos.x < -7)
		{
			pos.x += 15;
		}
		
		if (pos.y > 7)
		{
			pos.y -= 15;
		}
		if (pos.y < -7)
		{
			pos.y += 15;
		}
		return pos;
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
		return fixPosition(newPos);
	}
	
	private boolean breakTie(Position pos1, Position pos2, Position ref)
	{
		if (getDistance(pos1, ref) < getDistance(pos2,ref))
		{
			return true;
		}
		else if (getDistance(pos1, ref) > getDistance(pos2,ref))
		{
			return false;
		}
		
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
	
	public boolean breakTie(Direction dir1, Direction dir2)
	{
		int score = 0;
		if (dir1.equals(Direction.UP))
		{
			score+=5;
		}
		if (dir2.equals(Direction.UP))
		{
			score-=5;
		}
		
		if (dir1.equals(Direction.RIGHT))
		{
			score+=4;
		}
		if (dir2.equals(Direction.RIGHT))
		{
			score-=4;
		}
		
		if (dir1.equals(Direction.DOWN))
		{
			score+=3;
		}
		if (dir2.equals(Direction.DOWN))
		{
			score-=3;
		}
		
		if (dir1.equals(Direction.LEFT))
		{
			score+=2;
		}
		if (dir2.equals(Direction.LEFT))
		{
			score-=2;
		}
		
		if (dir1.equals(Direction.NONE))
		{
			score+=1;
		}
		if (dir2.equals(Direction.NONE))
		{
			score-=1;
		}
		
		if (score > 0)
		{
			return true;
		}
		else if (score < 0)
		{
			return false;
		}
		
		throw new RuntimeException("Could not resolve tie!");
	}
	
	private boolean metaBreakTie(HashMap<Integer, Direction> roles1, HashMap<Integer, Direction> roles2, Position ref)
	{
		for (Direction dir : Direction.values())
		{
			ObjectSeen predator1 = reverseLookUp(roles1, dir);
			ObjectSeen predator2 = reverseLookUp(roles2, dir);
			if (!predator1.pos.equals(predator2.pos))
			{
				return breakTie(predator1.pos, predator2.pos, ref);
			}
		}
		
		throw new RuntimeException("Could not brake tie in roles!");
	}


	private ObjectSeen reverseLookUp(HashMap<Integer, Direction> rolesMap, Direction dir)
	{
		for (Entry<Integer, Direction> entry : rolesMap.entrySet())
		{
			if (entry.getValue().equals(dir))
			{
				try
				{
					return getObject(entry.getKey()); 
				}
				catch (Exception e)
				{
					System.err.println(e);
					e.printStackTrace();
				}
			}
		}
		return null;
	}


	private int getRolesCost(HashMap<Integer, Direction> tempRoles)
	{
		int cost = 0;
		for (int id : tempRoles.keySet())
		{
			ObjectSeen predator = null;
			ObjectSeen prey = null;
			try
			{
				predator = getObject(id);
				prey = getObject(targetID);
			}
			catch (Exception e)
			{
				System.err.println(e);
				e.printStackTrace();
			}
			Direction role = tempRoles.get(id);
			cost += getDistance(predator.pos, getNewPos(prey.pos, role));
		}
		return cost;
	}
	
	private LinkedList<Direction> getMovesPreference(int id)
	{
		Position predatorPos = null;
		Position tPos = null;
		try
		{
			predatorPos = getObject(id).pos;
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
		

		Position expectedPreyPosition = getExpectedPosition(predatorPos, tPos);
/*		
		if (getDistance(tPos, expectedPreyPosition) > 0)
		{
			System.out.println("Moving towards " + expectedPreyPosition + " instead of " + tPos + " to intercept target;");
			System.out.println("\t the two points have a distance of " + getDistance(tPos, expectedPreyPosition) + " between them.");
		}
*/	

		Collections.sort(moves, new MoveComparator(predatorPos, tPos, expectedPreyPosition, roles.get(id)));	
		return moves;
	}
	
	private Position getExpectedPosition(Position predator, Position prey)
	{		
		int x_e;
		int y_e;
		Position e = new Position(0,0);
		
		FloatVector c = getCVector(prey);
		
		int dist = getDistance(predator, prey);		
		int max = getMaxDistance(prey);
		
/*		
  		float weight = 1.5f;		
		if (dist > 3)
		{
			c.x *= weight;
			c.y *= weight;	
		}
		
		if (c.x > 0 || c.y > 0)
		{
			System.out.println("dist=" + dist + ", c= " + c);		
		}
*/		
		
		
		// trial 1:
		
		x_e = Math.round( ( prey.x - c.y * prey.x + c.x * prey.y - c.x * (predator.x + predator.y) / ( 1 - c.x - c.y ) ) );
		y_e = Math.round( ( prey.y - c.x * prey.y + c.y * prey.x - c.y * (predator.x + predator.y) / ( 1 - c.x - c.y ) ) );
		
		e.x = x_e; 
		e.y = y_e;
		e = fixPosition(e);
		//System.out.println("1: " + x_e + " >= " + predator.x + " and " + y_e + " >= " + predator.y);
		if (e.x >= predator.x && e.y >= predator.y)
		{
			return e;	
		}
		
		// trial 2:
		
		x_e = Math.round( ( prey.x + c.y * prey.x - c.x * prey.y + c.x * (predator.x + predator.y) / ( 1 + c.x + c.y ) ) );
		y_e = Math.round( ( prey.y + c.x * prey.y - c.y * prey.x + c.y * (predator.x + predator.y) / ( 1 + c.x + c.y ) ) );
		
		e.x = x_e; 
		e.y = y_e;
		e = fixPosition(e);
		//System.out.println("2: " + x_e + " < " + predator.x + " and " + y_e + " < " + predator.y);
		if (e.x < predator.x && e.y < predator.y)
		{
			return e;	
		}
		
		// trial 3:
		
		x_e = Math.round( ( prey.x + c.y * prey.x - c.x * prey.y + c.x * (predator.y - predator.x) / ( 1 + c.y - c.x ) ) );
		y_e = Math.round( ( prey.y - c.x * prey.y + c.y * prey.x + c.y * (predator.y - predator.x) / ( 1 + c.y - c.x ) ) );
		
		e.x = x_e; 
		e.y = y_e;
		e = fixPosition(e);
		//System.out.println("3: " + x_e + " >= " + predator.x + " and " + y_e + " < " + predator.y);
		if (e.x >= predator.x && e.y < predator.y)
		{
			return e;	
		}
		
		// trial 4:
		
		x_e = Math.round( ( prey.x - c.y * prey.x + c.x * prey.y + c.x * (predator.x - predator.y) / ( 1 + c.x - c.y ) ) );
		y_e = Math.round( ( prey.y + c.x * prey.y - c.y * prey.x + c.y * (predator.x - predator.y) / ( 1 + c.x - c.y ) ) );
		
		e.x = x_e; 
		e.y = y_e;
		e = fixPosition(e);
		//System.out.println("4: " + x_e + " < " + predator.x + " and " + y_e + " >= " + predator.y);
		if (e.x < predator.x && e.y >= predator.y)
		{
			return e;	
		}
		
		//System.out.println("System of equations wasn't solved!");
		//return the current position if something goes wrong.
		return prey;
	}



	private FloatVector getCVector(Position prey)
	{

		LinkedList<Direction> freeDirs = new LinkedList<Direction>();
		for (Direction dir : Direction.values())
		{
			freeDirs.add(dir);
		}
		for (ObjectSeen predator : seen)
		{
			if (predator.type.equals(AgentType.PREDATOR))
			{
				for (Direction dir : Direction.values())
				{
					if (getNewPos(prey,dir).equals(predator.pos))
					{
						freeDirs.remove(dir);
					}
				}
			}
		}
		
		float c_x = 0;
		float c_y = 0;
		if (freeDirs.contains(Direction.UP) && !freeDirs.contains(Direction.DOWN))
		{
			c_y = (float)1 / (float)freeDirs.size(); 
		}
		else if (freeDirs.contains(Direction.DOWN) && !freeDirs.contains(Direction.UP))
		{
			c_y = -(float)1 / (float)freeDirs.size(); 
		}
		if (freeDirs.contains(Direction.LEFT) && !freeDirs.contains(Direction.RIGHT))
		{
			c_x = -(float)1 / (float)freeDirs.size(); 
		}
		else if (freeDirs.contains(Direction.RIGHT) && !freeDirs.contains(Direction.LEFT))
		{
			c_x = (float)1 / (float)freeDirs.size(); 
		}
		
		//System.out.println(freeDirs + " are free, so c = " + new FloatVector(c_x, c_y));
		return new FloatVector(c_x, c_y);
	}



	//gets the distance between a prey and the predator who's the farthest from it
	private int getMaxDistance(Position prey)
	{		
		int max = Math.abs(prey.x) + Math.abs(prey.y);
		for (ObjectSeen predator : seen)
		{
			if (predator.type.equals(AgentType.PREDATOR))
			{
				int distance = getDistance(prey, predator.pos);
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
	
	public float inQuadrant(Direction role, Position pos, Position ref)
	{
		float quad;
		if (role.equals(Direction.UP))
		{
			if (pos.x >= ref.x && pos.y > ref.y)
			{
				return (float)1.0;
			}
			else
			{
				return (float)1.0 - (float)(ref.x - pos.x + ref.y-pos.y)/(float)10.0 ;
			}
		}
		else if (role.equals(Direction.RIGHT))
		{
			if (pos.x > ref.x && pos.y <= ref.y)
			{
				return (float)1.0;
			}
			else
			{
				return (float)1.0 - (float)(ref.x - pos.x + pos.y - ref.y)/(float)10.0 ;
			}
			
		}
		else if (role.equals(Direction.DOWN))
		{
			if (pos.x <= ref.x && pos.y < ref.y)
			{
				return (float)1.0;
			}
			else
			{
				return (float)1.0 - (float)(pos.x - ref.x + pos.y - ref.y)/(float)10.0 ;
			}
			
		}
		else if (role.equals(Direction.LEFT))
		{
			if (pos.x < ref.x && pos.y >= ref.y)
			{
				return (float)1.0;
			}
			else
			{
				return (float)1.0 - (float)(pos.x - ref.x + ref.y - pos.y)/(float)10.0 ;
			}
		}
		else
		{
			//should never happen! 
			return 0;
		}
	}
	
	public boolean hasFreePath(Position start, Position finish)
	{
		for (Direction dir : Direction.values())
		{
			Position nextSquare = getNewPos(start, dir);
			if (nextSquare.equals(finish))
			{
				return true;
			}
			else if (getDistance(nextSquare, finish) < getDistance(start, finish) && isFree(nextSquare))
			{
				return hasFreePath(nextSquare, finish);
			}
		}
		return false;
	}
	

	private boolean isFree(Position nextSquare)
	{
		for (ObjectSeen o : seen)
		{
			if (o.type.equals(AgentType.PREDATOR) && o.pos.equals(nextSquare))
			{
				return false;
			}
		}
		return true;
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

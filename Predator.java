import java.io.*;
import java.lang.*;
import java.util.*;

import Predator.State;

/** This class defines the functionality of the predator. */
public class Predator
  extends Agent
{
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
	
	class State
	{
		public Position predatorPos;
		public Position preyPos;
		State(Position predatorPos, Position preyPos)
		{
			this.predatorPos = predatorPos;
			this.preyPos = preyPos;
		}
	}
	
	class StateAction
	{
		public State s;
		public Direction a;
		StateAction(State s, Direction a)
		{
			this.s = s;
			this.a = a;
		}
	}
	
	public enum AgentType{PREY, PREDATOR};
	public enum Direction{UP, DOWN, LEFT, RIGHT, NONE};
	private HashMap<StateAction, Double> Q;
	private State currentState;
	private int cycles;
	
	
  public Predator() 
  {
	  Q = new HashMap<StateAction, Double>();
	  cycles = 0;
  }
  
  /** This method initialize the predator by sending the initialization message
      to the server. */
  public void initialize()
    throws IOException
  {
    g_socket.send( "(init predator)" );
  }
  
  /** This message determines a new movement command. Currently it only moves
      random. This can be improved.. */
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
	updateQValues();
	return getAction();
}

private void updateQValues()
{
	// TODO Auto-generated method stub	
}

private Direction getAction()
{
	double[] probabilities = new double[5];
	int i=0;
	for (Direction a : Direction.values())
	{
		if (i > 0)
		{
			probabilities[i] = getProb(a) + probabilities[i-1];	
		}
		else
		{
			probabilities[i] = getProb(a);
		}
		
		i++;
	}
	Random die = new Random();
	double result = die.nextDouble();
	i = 0;
	for (Direction a: Direction.values())
	{
		if (result < probabilities[i])
		{
			return a;
		}
		i++;
	}
	throw new RuntimeException("Could not choose an action");
}

private double getProb(Direction a)
{
	StateAction sa = new StateAction(currentState, a);
	double numerator = Math.exp(Q.get(sa)/ getTau());
	double denominator = 0;
	for (Direction aPrime : Direction.values())
	{
		StateAction sa_prime = new StateAction(currentState, aPrime);
		denominator += Math.exp(Q.get(sa_prime)/ getTau());
	}
	return numerator / denominator;
}

private Double getTau()
{
	// TODO Auto-generated method stub
	return null;
}

/** This method processes the visual information. It receives a message
      containing the information of the preys and the predators that are
      currently  in the visible range of the predator. */
  public void processVisualInformation( String strMessage ) 
  {
    int i = 0, x = 0, y = 0;
    String strName = "";
    StringTokenizer tok = new StringTokenizer( strMessage.substring(5), ") (");
    
    while( tok.hasMoreTokens( ) )
    {
      if( i == 0 ) strName = tok.nextToken();                // 1st = name
      if( i == 1 ) x = Integer.parseInt( tok.nextToken() );  // 2nd = x coord
      if( i == 2 ) y = Integer.parseInt( tok.nextToken() );  // 3rd = y coord
      if( i == 2 )
      {	
        System.out.println( strName + " seen at (" + x + ", " + y + ")" );
      // TODO: do something nice with this information!
      }
      i = (i+1)%3;
    }
  }


  /** This method is called after a communication message has arrived from
      another predator. */
  public void processCommunicationInformation( String strMessage) 
  {
    // TODO: exercise 3 to improve capture times
  }
  
  /** This method is called and can be used to send a message to all other
       predators. Note that this only will have effect when communication is
      turned on in the server. */
  public String determineCommunicationCommand() 
  { 
    // TODO: exercise 3 to improve capture times
    return "" ; 
  }

  /**
   * This method is called when an episode is ended and can be used to
   * reset some variables.
   */
  public void episodeEnded( )
  {
     // this method is called when an episode has ended and can be used to
     // reinitialize some variables
     System.out.println( "EPISODE ENDED\n" );
  }

  /**
   * This method is called when this predator is involved in a
   * collision.
   */
  public void collisionOccured( )
  {
     // this method is called when a collision occured and can be used to
     // reinitialize some variables
     System.out.println( "COLLISION OCCURED\n" );
  }

  /**
   * This method is called when this predator is involved in a
   * penalization.
   */
  public void penalizeOccured( )
  {
    // this method is called when a predator is penalized and can be used to
    // reinitialize some variables
    System.out.println( "PENALIZED\n" );
  }

  /**
   * This method is called when this predator is involved in a
   * capture of a prey.
   */
  public void preyCaught( )
  {
    System.out.println( "PREY CAUGHT\n" );
  }
 
  public static void main( String[] args )
  {
    Predator predator = new Predator();
    if( args.length == 2 )
      predator.connect( args[0], Integer.parseInt( args[1] ) );
    else
      predator.connect();
  }
}
  

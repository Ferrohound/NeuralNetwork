package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;

import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;


public class OurAgent extends BasicMarioAIAgent implements Agent
{
int trueJumpCounter = 0;
int trueSpeedCounter = 0;
int trueCounter = 0;
boolean flee = false;
float runAway = 0.5f;
//private MarioEnvironment _environment;

public OurAgent()
{
    super("OurAgent");
    reset();
}

public void reset()
{
    action = new boolean[Environment.numberOfButtons];
    action[Mario.KEY_RIGHT] = true;
    action[Mario.KEY_LEFT] = false;
    action[Mario.KEY_SPEED] = true;
    trueJumpCounter = 0;
    trueSpeedCounter = 0;
}

private boolean DangerOfGap(byte[][] levelScene)
{
	//receptiveFieldWidth
    int fromX = receptiveFieldWidth / 2;
    int fromY = receptiveFieldHeight / 2;
    
    //getReceptiveFieldCellValue(int x, int y) returns 0 if out of range
    if (fromX > 3)
    {
        fromX -= 2;
    }

    for (int x = fromX; x < receptiveFieldWidth; ++x)
    {
        boolean f = true;
        for (int y = fromY; y < receptiveFieldHeight; ++y)
        {
            if (getReceptiveFieldCellValue(y, x) != 0)
                f = false;
        }
        if (f ||
                getReceptiveFieldCellValue(marioCenter[0] + 1, marioCenter[1]) == 0 ||
                (/*marioState[1] > 0 &&*/
                        (getReceptiveFieldCellValue(marioCenter[0] + 1, marioCenter[1] - 1) != 0 ||
                                getReceptiveFieldCellValue(marioCenter[0] + 1, marioCenter[1]) != 0)))
            return true;
    }
    return false;
}


private boolean DangerOfGap()
{
    return DangerOfGap(levelScene);
}

//if there's an enemy within a certain range...
private boolean enemyNear(byte[][] levelScene){
	//byte[][] enemies = getEnemiesObservationZ(); 
	int one_ahead_e = getEnemyFieldCellValue(marioCenter[0], marioCenter[1] + 1);
	int two_ahead_e = getEnemyFieldCellValue(marioCenter[0], marioCenter[1] + 2);
	int two_up = getEnemyFieldCellValue(marioCenter[0]+1, marioCenter[1]+3);
	int three_up = getEnemyFieldCellValue(marioCenter[0]+2, marioCenter[1]+4);
	
	return ((one_ahead_e != 0 && one_ahead_e != 25) ||
			(two_ahead_e != 0 && two_ahead_e != 25) || (two_up!=0 &&two_up!=25)
			//||(three_up!=0 &&three_up!=25)
			);
}

private boolean enemyNear(){
	return enemyNear(levelScene);
}

private boolean enemyBehind(byte[][] levelScene){
	int right_behind = getEnemyFieldCellValue(marioCenter[0], marioCenter[1]-1);
	int one_behind_l = getReceptiveFieldCellValue(marioCenter[0], marioCenter[1] - 1);
	return (right_behind!=0 && right_behind!=25) && one_behind_l!=0;
}

private boolean enemyBehind(){
	return enemyBehind(levelScene);
}



//byte[][] getLevelSceneObservationZ(level) from MarioEnvironment
//byte[][] getEnemiesObservationZ(level) from MarioEnvironment

/*
 *http://www.marioai.org/gameplay-track/marioai-benchmark 
 * 
 * 
 * 
 * 
 * (non-Javadoc)
 * @see ch.idsia.agents.controllers.BasicMarioAIAgent#getAction()
 */

public boolean[] getAction()
{
    // this Agent requires observation integrated in advance.
	//byte[][] scene = _environment.getLevelSceneObservationZ(1);
	//byte[][] enemies = _environment.getEnemiesObservationZ(1);
	
	//System.out.printf(getName(), levelScene)
	//System.out.printf("GRFCV returns: %d\n", getReceptiveFieldCellValue(marioCenter[0], marioCenter[1] + 2));
	trueCounter++;
	int one_ahead_l = getReceptiveFieldCellValue(marioCenter[0], marioCenter[1] + 1);
	int two_ahead_l = getReceptiveFieldCellValue(marioCenter[0], marioCenter[1] + 2);
	//System.out.printf("one_l: %d  two_l: %d\n", one_ahead_l, two_ahead_l);
	//int three_ahead = getReceptiveFieldCellValue(marioCenter[0], marioCenter[1] + 3);
	
	int one_ahead_e = getEnemyFieldCellValue(marioCenter[0], marioCenter[1] + 1);
	int two_ahead_e = getEnemyFieldCellValue(marioCenter[0], marioCenter[1] + 2);
	
	
	//if ( (one_ahead_e != 0 && one_ahead_e != 25) ||
		//	(two_ahead_e != 0 && two_ahead_e != 25) )
		//System.out.printf(format, args)
	//if (one_ahead_l != 0 || two_ahead_l != 0)
		//System.out.printf("something DIRECTLY AHEAD\n");
	if (one_ahead_l != 0 || two_ahead_l != 0 || DangerOfGap())
    {
		//System.out.printf("something in the way - going to jump\n");
        if (isMarioAbleToJump || (!isMarioOnGround && action[Mario.KEY_JUMP]))
        {
            action[Mario.KEY_JUMP] = true;
        }
        ++trueJumpCounter;
    } else
    {
        action[Mario.KEY_JUMP] = false;
        trueJumpCounter = 0;
    }

    if (trueJumpCounter > 16)
    {
        trueJumpCounter = 0;
        action[Mario.KEY_JUMP] = false;
    }
    
    

    //action[Mario.KEY_SPEED] = DangerOfGap();
    
    action[Mario.KEY_SPEED] =  (trueCounter % 2) == 0; 
    
    action[Mario.KEY_RIGHT] = !(enemyNear());
    
  //check if Mario is able to shoot
    if(isMarioAbleToShoot){
    	if (enemyNear() && !enemyBehind()/*&& getReceptiveFieldCellValue(marioCenter[0]-2,marioCenter[0]-1)!=0*/)
        {
    		flee = true;
    		runAway = 0.32f; 
        	action[Mario.KEY_LEFT] = true;
        	//System.out.printf("pressing left\n");
        }
        
    	if(flee && !enemyBehind()){
        	runAway-=0.1;
        	action[Mario.KEY_RIGHT] = false;
        }
    	else if(flee && enemyBehind()){
    		runAway = 0.32f;
    		action[Mario.KEY_LEFT] = false;
    		action[Mario.KEY_RIGHT] = true;
    		flee = false;
    	}
        
        if(runAway<0){
        	flee = false;
        	action[Mario.KEY_LEFT] = false;
        	action[Mario.KEY_RIGHT] = true;
        }
    }
    
    
    return action;
}
}
package ch.idsia.agents.learning;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.evolution.Evolvable;
import ch.idsia.evolution.MLP;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Apr 28, 2009
 * Time: 2:09:42 PM
 */
public class SimpleMLPAgent implements Agent, Evolvable
{

    private MLP mlp;
    private String name = "SimpleMLPAgent";
    final int numberOfOutputs = 8;
    final int numberOfInputs = 20;
    private Environment environment;
    private float rewardLevel = 0;

    /*final*/
    protected byte[][] levelScene;
    /*final */
    protected byte[][] enemies;
    protected byte[][] mergedObservation;

    protected float[] marioFloatPos = null;
    protected float[] marioFloatPosArray = null;
    protected float[] enemiesFloatPos = null;

    protected int[] marioState = null;

    protected int marioStatus;
    protected int marioMode;
    protected boolean isMarioOnGround;
    protected boolean isMarioAbleToJump;
    protected boolean isMarioAbleToShoot;
    protected boolean isMarioCarrying;
    protected int getKillsTotal;
    protected int getKillsByFire;
    protected int getKillsByStomp;
    protected int getKillsByShell;
    
    float runAway = 0.32f;
    boolean flee = false;

    // values of these variables could be changed during the Agent-Environment interaction.
    // Use them to get more detailed or less detailed description of the level.
    // for information see documentation for the benchmark <link: marioai.org/marioaibenchmark/zLevels
    int zLevelScene = 1;
    int zLevelEnemies = 0;


    public SimpleMLPAgent()
    {
        mlp = new MLP(numberOfInputs, 10, numberOfOutputs);
        this.marioFloatPosArray = new float[10];
    }
    /*
    public SimpleMLPAgent(int inputs, int hidden, int outputs)
    {
        mlp = new MLP(inputs, hidden, outputs);
    }
    */

    private SimpleMLPAgent(MLP mlp)
    {
        this.mlp = mlp;
        this.marioFloatPosArray = new float[10];
    }

    public Evolvable getNewInstance()
    {
        return new SimpleMLPAgent(mlp.getNewInstance());
    }

    public Evolvable copy()
    {
        return new SimpleMLPAgent(mlp.copy());
    }

    public void integrateObservation(Environment environment)
    {
        this.environment = environment;
        levelScene = environment.getLevelSceneObservationZ(zLevelScene);
        enemies = environment.getEnemiesObservationZ(zLevelEnemies);
        mergedObservation = environment.getMergedObservationZZ(1, 0);

        this.marioFloatPos = environment.getMarioFloatPos();
        //our function
        putPosInOrder();
        
        this.enemiesFloatPos = environment.getEnemiesFloatPos();
        this.marioState = environment.getMarioState();

        // It also possible to use direct methods from Environment interface.
        //
        marioStatus = marioState[0];
        marioMode = marioState[1];
        isMarioOnGround = marioState[2] == 1;
        isMarioAbleToJump = marioState[3] == 1;
        isMarioAbleToShoot = marioState[4] == 1;
        isMarioCarrying = marioState[5] == 1;
        getKillsTotal = marioState[6];
        getKillsByFire = marioState[7];
        getKillsByStomp = marioState[8];
        getKillsByShell = marioState[9];
    }
    
    //our function
    private void putPosInOrder(){
    	for(int i=0; i<8;i++){
    		this.marioFloatPosArray[i] = this.marioFloatPosArray[i+2];
    	}
    	this.marioFloatPosArray[8] = this.marioFloatPos[0];
    	this.marioFloatPosArray[9] = this.marioFloatPos[1];
    }
    
    //our function
    public boolean isStuck(){
    	float[] arr = this.marioFloatPosArray;
    	if(arr[0]-arr[2] < .1f && arr[2]-arr[4] < .1f && arr[4]-arr[6] < .1f && arr[6]-arr[8] < .1f &&
    			arr[1]-arr[3] < .1f && arr[3]-arr[5] < .1f && arr[5]-arr[7] < .1f && arr[7]-arr[9] < .1f){
    		return true;
    	}
    	return false;
    }

    public void giveIntermediateReward(float intermediateReward)
    {
    	rewardLevel+=intermediateReward;
    }

    public void reset()
    { mlp.reset(); }

    public void mutate()
    { mlp.mutate(); }

    public boolean[] getAction()
    {
//        double[] inputs = new double[]{probe(-1, -1, levelScene), probe(0, -1, levelScene), probe(1, -1, levelScene),
//                              probe(-1, 0, levelScene), probe(0, 0, levelScene), probe(1, 0, levelScene),
//                                probe(-1, 1, levelScene), probe(0, 1, levelScene), probe(1, 1, levelScene),
//                                1};
        double[] inputs = new double[] {
        		//probe(-1, -1, mergedObservation), 
        		probe(0, -1, mergedObservation), 
        		probe(1, -1, mergedObservation),
        		
        		marioState[0],
                
                enemyNear(1,0, enemies),
                enemyNear(2,0, enemies),
                enemyNear(2,1, enemies),
                enemyNear(3,0, enemies),
                marioState[2],
                marioState[3],
                marioState[4],
                
                //probe(0, 0, mergedObservation), 
                //enemyNear(0,0, enemies),
                enemyNear(0,1, enemies),
                enemyNear(0,2, enemies),
                enemyNear(1,1, enemies),
                probe(1, 0, mergedObservation),
                //probe(-1, 1, mergedObservation),
                //probe(0, 1, mergedObservation), 
                probe(1, 1, mergedObservation), 
                
                probe(0,2, mergedObservation),
                checkWall(),
        		probe(-1,2, mergedObservation),
                
        		// check for gap
        		checkGap(),
                1
                };
        double[] outputs = mlp.propagate(inputs);
        boolean[] action = new boolean[numberOfOutputs];
        for (int i = 0; i < action.length; i++)
            action[i] = outputs[i] > 0;

        return action;
    }
    
    public MLP getMLP(){
    	return mlp;
    }
    
    public void setMLP(MLP newMlp){
    	this.mlp = newMlp;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    private double probe(int x, int y, byte[][] scene)
    {
        int realX = x + 11;
        int realY = y + 11;
        return (scene[realX][realY] != 0) ? 1 : 0;
    }
    
    private double checkGap(){
        if(		probe(1,-1, mergedObservation) == 1 &&
        		probe(2,-1, mergedObservation)== 1 &&
        		probe(1,-2, mergedObservation)== 1 &&
        		probe(2,-2, mergedObservation)== 1 &&
        		probe(1,-3, mergedObservation)== 1 &&
        		probe(2,-3, mergedObservation)==1)
        {
        	return 1;
        }
        
        return 0;
    }
    
    private double checkWall()
    {
    	if(	probe(1, 0, mergedObservation) == 1 &&
    		probe(1,1, mergedObservation) == 1 &&
        	probe(1,2, mergedObservation)== 1 &&
        	probe(1,3, mergedObservation)== 1)
        {
        	return 1;
        }
    	return 0;
    }
    
    //check if enemies are in front of yourself
    private double enemyNear(int x, int y, byte[][] levelScene){
    	//byte[][] enemies = getEnemiesObservationZ(); 
    	x+=11;
    	y+=11;
    	int one_ahead_e = enemies[x][y + 1];
    	int two_ahead_e = enemies[x][ y + 2];
    	int two_up = enemies[x+1][y+3];
    	int three_up = enemies[x+2][y+4];
    	
    	if ((one_ahead_e != 0 && one_ahead_e != 25) ||
    			(two_ahead_e != 0 && two_ahead_e != 25) || (two_up!=0 &&two_up!=25)){
    			//||(three_up!=0 &&three_up!=25)
    			return 1;
    	};
    	return 0;
    }
/*
    private boolean enemyNear(){
    	return enemyNear(levelScene);
    }
*/
    //check if enemies are behind yourself
    private double enemyBehind(int x, int y, byte[][] levelScene)
    {
    	x+=11;
    	y+=11;
    	int right_behind = enemies[x][y-1];
    	int one_behind_l = enemies[x][y - 1];
    	if((right_behind!=0 && right_behind!=25) && one_behind_l!=0){
    		return 1;
    	}
    	return 0;
    	//return (right_behind!=0 && right_behind!=25) && one_behind_l!=0;
    }
/*
    private boolean enemyBehind(){
    	return enemyBehind(levelScene);
    }
    */
    
    /* ADDITIONAL CODE//check if Mario is able to shoot
    if(isMarioAbleToShoot){
    	if (enemyNear() && !enemyBehind())
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
     * */
}

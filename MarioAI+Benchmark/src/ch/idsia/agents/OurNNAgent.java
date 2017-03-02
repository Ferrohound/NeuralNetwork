package ch.idsia.agents;

import ch.idsia.agents.learning.SimpleMLPAgent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.ProgressTask;
import ch.idsia.evolution.ea.ES;
//import ch.idsia.agents.learning.SimpleMLPAgent;
import ch.idsia.agents.learning.SmallMLPAgent;
//import ch.idsia.benchmark.tasks.ProgressTask;


/**
 * Created by IntelliJ IDEA.
 * User: odin
 * Neil and Fuller
 */


public class OurNNAgent extends SimpleMLPAgent implements LearningAgent
{
    private SimpleMLPAgent agent;
    Agent bestAgent;
    private static float bestScore = 0;
    private BasicTask task;
    ES es;
    int populationSize = 75;
    int generations = 350;
    int numberOfTrials = 1; //common number of trials
    int exhausted; // number of exhausted trials

    //when the agent is initialized, create a new ES with the size of the population
    //then go to the learn function
    public void init()
    {
    	//System.out.println("Has my journey even begun?");
        es = new ES(task, agent, populationSize);
        learn();
    }

    public OurNNAgent(SimpleMLPAgent agent)
    {
        this.agent = agent;
    }

    public OurNNAgent()
    {
    	this.agent = new SimpleMLPAgent();
    	//init();
    }

    //every generation, call es.nextGeneration
    public void learn()
    {
        this.exhausted++;

        int locBestScore = 0; // local best score

        for (int gen = 0; gen < generations; gen++)
        {
            System.out.println("Generation " + gen + "------------------------------------------");
            es.nextGeneration();
            //System.out.println("Next Gen Completed!");
            float fitn = es.getBestFitnesses()[0];
            System.out.println("Best fitness was: " + fitn + " while current best score is: " + bestScore);

            if (fitn > bestScore)
            {
            	//System.out.println("Best score exceeded! Rewarding agent.");
                bestScore = fitn;
                bestAgent = (Agent) es.getBests()[0];
                bestAgent.giveIntermediateReward(3);
            }
        }
    }

    public void giveReward(float r)
    {
    	
    }

    public void newEpisode()
    {
        task = null;
        agent.reset();
    }

    public void setTask(ProgressTask task)
    {
        this.task = task;
    }

    public void setNumberOfTrials(int num)
    {
        this.numberOfTrials = num;
    }

    public Agent getBestAgent()
    {
        return bestAgent;
    }
}

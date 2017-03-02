package ch.idsia.evolution.ea;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.tasks.ProgressTask;
import ch.idsia.agents.learning.SimpleMLPAgent;
import ch.idsia.agents.learning.SmallMLPAgent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.Task;
import ch.idsia.evolution.EA;
import ch.idsia.evolution.Evolvable;
import ch.idsia.evolution.MLP;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Apr 29, 2009
 * Time: 12:16:49 PM
 */
public class ES implements EA
{

    private final Evolvable[] population;
    private final double[] deviations;
    private final float[] fitness;
    private final int elite;
    private final ProgressTask task;
    private final int evaluationRepetitions = 1;

    public ES(Task task, Evolvable initial, int populationSize)
    {
        this.population = new Evolvable[populationSize];
        this.deviations = new double [populationSize];
        for (int i = 0; i < population.length; i++)
        {
            population[i] = initial.getNewInstance();
            //System.out.println("Member number "+ i + " is " + population[i]);
        }
        this.fitness = new float[populationSize];
        this.elite = populationSize / 3;
        this.task = (ProgressTask)task;
    }

    //iterate over the members of the population, running the simulation for
    //each one, then calling mutate
    //finally recombine
    public void nextGeneration()
    {
    	//System.out.println("Start of next gen!");
    	
        for (int i = 0; i < population.length; i++)
        {
        	//System.out.println("Evaluate population member number " + i);
        	population[i].mutate();
            evaluate(i);
            
            double oldDev = ((SimpleMLPAgent)population[i]).getMLP().getDev();
            deviations[i] = oldDev;
            //System.out.println("Member " + i+"'s std dev is => " + oldDev);
        }
        /*
        for (int i = elite; i < population.length; i++)
        {
        	System.out.println("Mutate Member Number  " + i);
            population[i] = population[i - elite].copy();
            population[i].mutate();
            evaluate(i);
        }
        */
        shuffle();
        sortPopulationByFitness();
        //Sort by fitness then replace the worst ones by recombining the better ones
        System.out.println("Recombining...");
        for(int i =elite; i<population.length;i++){
        	//heartburn
        	//call recombine(this, i+1, i+2);
        	//System.out.println(i);
        	/*
        	if(i<population.length -2){
        	((SmallMLPAgent)population[i]).getMLP().psoRecombine(((SmallMLPAgent)population[i]).getMLP(),
        			((SmallMLPAgent)population[i]).getMLP(),
        			((SmallMLPAgent)population[i+1]).getMLP());
        	}
        	*/
        	//population[i] = population[i - elite].copy();
        	//since getMLP would return a copy
        	//MLP tmp = ((SimpleMLPAgent)population[i]).getMLP();
        	
        	((SimpleMLPAgent)population[i]).getMLP().psoRecombine(((SimpleMLPAgent)population[i]).getMLP(),
            		((SimpleMLPAgent)population[0]).getMLP(),
    				((SimpleMLPAgent)population[i - elite + 1]).getMLP());
        	
        	//set that pop. member's mlp to tmp
        	//((SimpleMLPAgent)population[i]).setMLP(tmp);
        	//((SimpleMLPAgent)population[i]).getMLP().setDev(oldDev);
        	//population[i].mutate();
        	double oldDev = deviations[i];
        	((SimpleMLPAgent)population[i]).getMLP().setDev(oldDev);
        	//System.out.println("Member " + i+"'s std dev is => " + ((SimpleMLPAgent)population[i]).getMLP().getDev());
        }
    }

    private void evaluate(int which)
    {
    	
        fitness[which] = 0;
        for (int i = 0; i < evaluationRepetitions; i++)
        {
        	population[which].reset();
        	for(int j=0; j<10;j++){
        		//System.out.println("Seed number " +j);
        		task.setSeed(j);
	            //task.doEpisodes(evaluationRepetitions, false);
	            
	            //System.out.println("Time to evaluate!");
	            
	            //System.out.println(population[which]);
	            //System.out.printf("size of population is %d\n", population.length);
	            //System.out.println("THIS IS THE AGENT=> " + (Agent) population[which]);
	            //System.out.println("THIS IS THE TASK => " + task);
	            fitness[which] += task.evaluate((Agent) population[which])[0];
	            //RESULTS SCREEN
	            
	            //System.out.println(((BasicTask)task).getEnvironment().getEvaluationInfoAsString());
	            
	            //System.out.println("Last check!");
	//            System.out.println("which " + which + " fitness " + fitness[which]);
        	}
        }
        fitness[which] = fitness[which] / evaluationRepetitions/10;
        task.setSeed(1);
    }

    private void shuffle()
    {
        for (int i = 0; i < population.length; i++)
        {
            swap(i, (int) (Math.random() * population.length));
        }
    }

    private void sortPopulationByFitness()
    {
        for (int i = 0; i < population.length; i++)
        {
            for (int j = i + 1; j < population.length; j++)
            {
                if (fitness[i] < fitness[j])
                {
                    swap(i, j);
                }
            }
        }
    }

    private void swap(int i, int j)
    {
        float cache = fitness[i];
        fitness[i] = fitness[j];
        fitness[j] = cache;
        Evolvable gcache = population[i];
        population[i] = population[j];
        population[j] = gcache;
    }

    public Evolvable[] getBests()
    {
        return new Evolvable[]{population[0]};
    }

    public float[] getBestFitnesses()
    {
        return new float[]{fitness[0]};
    }

}

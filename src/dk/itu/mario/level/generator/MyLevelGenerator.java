package dk.itu.mario.level.generator;

import java.util.Random;
import java.util.ArrayList;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelGenerator;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.level.CustomizedLevel;
import dk.itu.mario.level.MyLevel;



//TEST COMMENT

//LEVELS 320x15
//CHUNKS 36 LONG
//GAPS 4 LONG


public class MyLevelGenerator extends CustomizedLevelGenerator implements LevelGenerator{

	private final int INITIALPOPSIZE = 1;
	private final int CHILDRENPERLEVEL = 5;
	Random rng;

	public MyLevelGenerator() {
		rng = new Random();
	}

	public LevelInterface generateLevel(GamePlay playerMetrics) {
		//LevelInterface level = new MyLevel(320,15,new Random().nextLong(),1,LevelInterface.TYPE_OVERGROUND,playerMetrics);
		//return level;

		int deaths = playerMetrics.timesOfDeathByRedTurtle + 
		playerMetrics.timesOfDeathByGoomba + 
		playerMetrics.timesOfDeathByGreenTurtle + 
		playerMetrics.timesOfDeathByArmoredTurtle + 
		playerMetrics.timesOfDeathByJumpFlower + 
		playerMetrics.timesOfDeathByCannonBall + 
		playerMetrics.timesOfDeathByChompFlower +
		(int)Math.floor(playerMetrics.timesOfDeathByFallingIntoGap);

		int difficulty = 3 - deaths;
		
		int[] playerScores = getPlayerScores(playerMetrics);

		ArrayList<MyLevel> initialPop = new ArrayList<MyLevel>();
		for (int i = 0; i < INITIALPOPSIZE; i++) {
			initialPop.add(new MyLevel(340,15,rng.nextLong(),playerScores,difficulty,LevelInterface.TYPE_OVERGROUND,playerMetrics)); //static method will determine difficulty/etc from metrics
		}

		return initialPop.get(0);
		
		//ArrayList<MyLevel> generation = getSuccessors(initialPop);


		//DO THE GENETICS loop and evaluate



	}

	@Override
	public LevelInterface generateLevel(String detailedInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<MyLevel> getSuccessors(ArrayList<MyLevel> levels) {
		ArrayList<MyLevel> newGeneration = new ArrayList<MyLevel>();
		for (int i = 0; i < levels.size(); i++) {
			for (int j = 0; j < CHILDRENPERLEVEL; j++) {
				int randIndex = rng.nextInt()%levels.size();
				newGeneration.add(levels.get(i).generateChild(levels.get(randIndex)));
			}
			newGeneration.add(levels.get(i));
		}
		return newGeneration;
	}


	public int evaluateLevel(LevelInterface level, GamePlay player, int difficulty) {
		return 0;
	}

	public int evaluateChunk(LevelInterface level, int chunkStartColumn, int difficulty, int chunkWidth) {
		return 0;
	}	
	
	public int[] getPlayerScores(GamePlay player){
		int kills = player.RedTurtlesKilled //number of Red Turtle Mario killed
				+ player.GreenTurtlesKilled//number of Green Turtle Mario killed
				+ player.ArmoredTurtlesKilled //number of Armored Turtle Mario killed
				+ player.GoombasKilled //number of Goombas Mario killed
				+player.CannonBallKilled //number of Cannon Ball Mario killed
				+player.JumpFlowersKilled //number of Jump Flower Mario killed
				+player.ChompFlowersKilled;
		
		int[] scores = new int[3];
		
		scores[0] = (int)(100*(player.jumpsNumber / (.7 * player.totalTime)));
		scores[1] = (int)(100*(player.coinsCollected / (double)player.totalCoins));
		scores[2] = (int)(100*( kills / (double)player.totalEnemies));
		
//		System.out.println("Jumps, Time, jumps/time");
//		System.out.println(player.jumpsNumber);
//		System.out.println(player.totalTime);
//		System.out.println(player.jumpsNumber / (.7 *player.totalTime));
//		
//		System.out.println("-----");
//		System.out.println("coins, total, coins/total");
//		System.out.println(player.coinsCollected);
//		System.out.println(player.totalCoins);
//		System.out.println(player.coinsCollected / (double)player.totalCoins);
//		
//		System.out.println("-----");
//		System.out.println("enemies, total, enemies/total");
//		System.out.println(kills);
//		System.out.println(player.totalEnemies);
//		System.out.println( kills / (double)player.totalEnemies);
		
		
		return scores;
	}



}

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

	private final int INITIALPOPSIZE = 20;
	private final int CHILDRENPERLEVEL = 5;
	Random rng;

	public MyLevelGenerator() {
		rng = new Random();
	}

	public LevelInterface generateLevel(GamePlay playerMetrics) {
		//LevelInterface level = new MyLevel(320,15,new Random().nextLong(),1,LevelInterface.TYPE_OVERGROUND,playerMetrics);
		//return level;

		ArrayList<LevelInterface> initialPop = new ArrayList<LevelInterface>();
		for (int i = 0; i < INITIALPOPSIZE; i++) {
			initialPop.add(MyLevel.generateInitialLevel(playerMetrics)); //static method will determine difficulty/etc from metrics
		}
		ArrayList<LevelInterface> generation = getSuccessors(initialPop);


		//DO THE GENETICS



	}

	@Override
	public LevelInterface generateLevel(String detailedInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<LevelInterface> getSuccessors(ArrayList<LevelInterface> levels) {
		ArrayList<LevelInterface> newGeneration = new ArrayList<LevelInterface>();
		for (int i = 0; i < levels.size(), i++) {
			for (int j = 0; j < CHILDRENPERLEVEL, j++) {
				int randIndex = rng.nextInt()%levels.size();
				newGeneration.add(levels.get(i).generateChild(levels.get(randIndex)));
			}
			newGeneration.add(levels.get(i));
		}
		return newGeneration;
	}


	public int evaluateLevel(LevelInterface level, GamePlay player, int difficulty) {

	}

	public int evaluateChunk(LevelInterface level, int chunkStartColumn, int difficulty, int chunkWidth) {

	}



}

package dk.itu.mario.level.generator;

import java.util.Random;
import java.util.ArrayList;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelGenerator;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.level.CustomizedLevel;
import dk.itu.mario.level.Level;
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

		
		System.out.println(evaluateLevel(initialPop.get(0), playerMetrics, playerScores, difficulty));
		
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
			newGeneration.add(levels.get(i)); //add originals to new gen
		}
		return newGeneration;
	}


	public int evaluateLevel(MyLevel level, GamePlay player, int[] playerScores, int difficulty) {
		int score = 0;
		
		
		for (int chunkStart = MyLevel.LEVEL_MARGIN_SIZE; 
				chunkStart < level.numChunks*MyLevel.CHUNK_SIZE + MyLevel.LEVEL_MARGIN_SIZE;
				chunkStart+=MyLevel.CHUNK_SIZE) {
			score += evaluateChunk(level, chunkStart, playerScores, difficulty, MyLevel.CHUNK_SIZE);
		}
			
		
		
		return score;
	}

	public int evaluateChunk(MyLevel level, int chunkStartColumn, int[]playerScores, int difficulty, int chunkWidth) {
		int score = 0;
		MyLevel.Archetype type = level.chunkTypes[(chunkStartColumn - MyLevel.LEVEL_MARGIN_SIZE) / MyLevel.CHUNK_SIZE];
		switch(type) {
		case JUMPER:
			score = evaluateJumperChunk(level, chunkStartColumn, playerScores, difficulty, chunkWidth);
			break;
		case HUNTER:
			score = evaluateHunterChunk(level, chunkStartColumn, playerScores, difficulty, chunkWidth);
			break;
		case HOARDER:
			score = evaluateHoarderChunk(level, chunkStartColumn, playerScores, difficulty, chunkWidth);
			break;
		}
		
		return score;
	}	
	
	private int evaluateHoarderChunk(MyLevel level, int chunkStartColumn,
			int[] playerScores, int difficulty, int chunkWidth) {
				return 0;
	}

	private int evaluateHunterChunk(MyLevel level, int chunkStartColumn,
			int[] playerScores, int difficulty, int chunkWidth) {
		return 0;
	}

	private int evaluateJumperChunk(MyLevel level, int chunkStartColumn,
			int[] playerScores, int difficulty, int chunkWidth) {
		int score = 0;
		byte[][] chunk = new byte[chunkWidth][level.getHeight()];
		for (int x = 0; x< chunkWidth; x++){
			for (int y = 0; y < level.getHeight(); y++){
				chunk[x][y] = level.getBlock(x+chunkStartColumn, y);
			}
		}
		ArrayList<Platform> plats = findPlatforms(chunk);
		
		System.out.println(plats.size());
		
		
		
		
				
		return score;
	}
	
	private ArrayList<Platform> findPlatforms(byte[][] chunk) {
		ArrayList<Platform> plats = new ArrayList<>();
		
		for (int y = 0; y < chunk[0].length; y++) {
			for (int x = 0; x < chunk.length; x++) {
				if (isSurface(chunk[x][y])){
					boolean solid = chunk[x][y] == (byte) (14) || chunk[x][y] == (byte)(11)  || chunk[x][y] == (byte) (10+0*16);
					int length = 1;
					int startx = x;
					x++;
					while (x < chunk.length && isSurface(chunk[x][y])) {
						length++;
						x++;
					}
					plats.add(new Platform(startx, y, length, solid));
				}
			}
		}
		
		return plats;
	}

	private boolean isSurface(byte b) {
		
		return b == (byte) (14) || b == (byte)(11)  || b == (byte) (10+0*16) || b == (byte) (5 + 8 * 16) || b == (byte) (4 + 8 * 16) || b == (byte) (6 + 8 * 16) || b == (byte) (4 + 11 * 16) || b == (byte) (6 + 11 * 16) || b == (byte) (0 + 1 * 16) || b == (byte) (4 + 2 + 1 * 16) || b == (byte) (4+1+1*16);
		//           CANNON           TUBETOPRIGHT             TUBETOPLEFT            HILLTOP                 HILLTOPLEFT                HILLTOPRIGHT              HILLLTOPLEFTIN             HILLTOPIN                        BLOCKEMPTY                       BLOCKPOWER                BLOCKCOIN
	}

	private class Platform {
		int x;
		int y;
		int length;
		boolean solid;
		
		public Platform(int x, int y, int length, boolean solid) {
			this.x=x;
			this.y=y;
			this.length=length;
			this.solid=solid;
		}
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

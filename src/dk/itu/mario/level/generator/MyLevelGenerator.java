package dk.itu.mario.level.generator;

import java.util.Collections;
import java.util.Random;
import java.util.ArrayList;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelGenerator;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.level.CustomizedLevel;
import dk.itu.mario.level.Level;
import dk.itu.mario.level.MyLevel;



//TEST COMMENT

//LEVELS 320x15
//CHUNKS 36 LONG
//GAPS 4 LONG

//player score order: jump, hoard, hunt


public class MyLevelGenerator extends CustomizedLevelGenerator implements LevelGenerator{

	private final int INITIALPOPSIZE = 15;
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

		ArrayList<MyLevel> population = new ArrayList<MyLevel>();
		for (int i = 0; i < INITIALPOPSIZE; i++) {
			population.add(new MyLevel(340,15,rng.nextLong(),playerScores,difficulty,LevelInterface.TYPE_OVERGROUND,playerMetrics)); //static method will determine difficulty/etc from metrics
		}

		
//		int score = evaluateLevel(population.get(0), playerMetrics, playerScores, difficulty);
//		System.out.print("LEVEL OVERALL SCORE: " );
//		System.out.println(score);
		System.out.print("\nDIFFICULTY: " );
		System.out.println(difficulty);
		System.out.print("PLAYER JUMPER SCORE: " );
		System.out.println(playerScores[0]);
		System.out.print("PLAYER HOARDER SCORE: " );
		System.out.println(playerScores[1]);
		System.out.print("PLAYER HUNTER SCORE: " );
		System.out.println(playerScores[2]);
		
		//return initialPop.get(0);
		
		ArrayList<MyLevel> generation = getSuccessors(population);

		int g = 0;
		ArrayList<Integer> scores = new ArrayList<>();
		
//		for (int g = 0; g < 10; g++) { //change to while below level target thresh?
		while (!flattenedOut(scores) && g < 200) {
			System.out.print("GENERATION ");
			System.out.println(g);
			population = new ArrayList<>();
			ArrayList<Integer> childScores = new ArrayList<>();
			ArrayList<Integer> sortedChildScores = new ArrayList<>();
			for (MyLevel level : generation) {
				Integer childScore = evaluateLevel(level, playerMetrics, playerScores, difficulty);
				while (childScores.contains(childScore)){
					childScore--;
				}
				childScores.add(childScore);
				sortedChildScores.add(childScore);
			}
			Collections.sort(sortedChildScores);
			Collections.reverse(sortedChildScores);
			for (int i = 0; i < INITIALPOPSIZE; i++){
				if (childScores.indexOf(sortedChildScores.get(i)) == -1) {
					System.out.println("noooo");
				}
				population.add(generation.get(childScores.indexOf(sortedChildScores.get(i))));
			}
			generation = getSuccessors(population);
			System.out.print("BEST SCORE FOR GENERATION: ");
			System.out.println(sortedChildScores.get(0));
			scores.add(sortedChildScores.get(0));
			g++;
						
		}
		return population.get(0);
		//DO THE GENETICS loop and evaluate



	}

	private boolean flattenedOut(ArrayList<Integer> scores) {
		if (scores.size() < 30) {
			return false;
		}
		if (scores.get(scores.size()-1) - scores.get(scores.size()-25) > 100) {
			return false;
		}
		return true;
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
				int randIndex = rng.nextInt(levels.size());
				if (levels.get(randIndex) == null) {
					System.out.println("WHOOPSY");
				}
//				System.out.println("gonna generate a child here");
				newGeneration.add(levels.get(i).generateChild(levels.get(randIndex)));
			}
			newGeneration.add(levels.get(i)); //add originals to new gen
		}
		return newGeneration;
	}


	public int evaluateLevel(MyLevel level, GamePlay player, int[] playerScores, int difficulty) {
		int score = 0;
		
		//score chunx
		for (int chunkStart = MyLevel.LEVEL_MARGIN_SIZE; 
				chunkStart < level.numChunks*MyLevel.CHUNK_SIZE + MyLevel.LEVEL_MARGIN_SIZE;
				chunkStart+=MyLevel.CHUNK_SIZE) {
			score += evaluateChunk(level, chunkStart, playerScores, difficulty, MyLevel.CHUNK_SIZE);
		}
		
//		System.out.print("SUM CHUNK SCORE: " );
//		System.out.println(score);
		
		//tune total gap size to difficulty
		score += (int) (level.numChunks * (100 - 4*(Math.abs(difficulty*3*level.numChunks - level.TOTAL_GAP_SIZE)))); //magic numbers
		//tune total enemies to difficulty
		score += (int) (level.numChunks * (100 - 4*(Math.abs(difficulty*3*level.numChunks - level.ENEMIES))));
		//there should be about one powerup per chunk
		score += (int) (level.numChunks * (100 - 30*(Math.abs(level.BLOCKS_POWER - level.numChunks))));
		//cannons are hard, should scale by difficulty
		int numCannons = 0;
		for (int x = 0; x < level.getWidth(); x++){
			for (int y = 0; y < level.getHeight(); y++) {
				if (level.getBlock(x, y) == (byte) (14)){
					numCannons++;
				}
			}
		}
		int idealNumCannons = difficulty * 8;
		score += (int) (level.numChunks * (100 - 20*(Math.abs(idealNumCannons - numCannons))));
		
		
		return score;
	}

	public int evaluateChunk(MyLevel level, int chunkStartColumn, int[]playerScores, int difficulty, int chunkWidth) {
		int score = 0;
		//jump0, hoard1, hunt2!
		//weight score by different aspect according to user score
		score += (playerScores[0]/(double)(playerScores[0]+playerScores[1]+playerScores[2]))
				* evaluateJumperChunk(level, chunkStartColumn, playerScores, difficulty, chunkWidth);
		
		score += (playerScores[2]/(double)(playerScores[0]+playerScores[1]+playerScores[2]))
				* evaluateHunterChunk(level, chunkStartColumn, playerScores, difficulty, chunkWidth);
			
		score += (playerScores[1]/(double)(playerScores[0]+playerScores[1]+playerScores[2]))
				* evaluateHoarderChunk(level, chunkStartColumn, playerScores, difficulty, chunkWidth);
		
		return score;
	}	
	
	private int evaluateHoarderChunk(MyLevel level, int chunkStartColumn,
			int[] playerScores, int difficulty, int chunkWidth) {
		
				int score = 0;
				int playerCoinsCollected = playerScores[1];
				
				byte[][] chunk = new byte[chunkWidth][level.getHeight()];
				SpriteTemplate[][] chunksprites = new SpriteTemplate[chunkWidth][level.getHeight()];
				int numEnemies = 0;
				for (int x = 0; x< chunkWidth; x++){
					for (int y = 0; y < level.getHeight(); y++){
						chunk[x][y] = level.getBlock(x+chunkStartColumn, y);
						chunksprites[x][y] = level.getSpriteTemplate(x+chunkStartColumn, y);
						if (chunksprites[x][y] != null) {
							numEnemies++;
						}
					}
				}
				ArrayList<Platform> plats = findPlatforms(chunk);
				ArrayList<CoinRow> coinRows = findCoinRows(chunk);
				
				int groundRows = 0;
				int platformRows = 0;
				for (CoinRow row : coinRows) {
					if (row.y > 8) {
						groundRows++;
					} else {
						for (Platform p: plats){
							if (p.y > row.y && p.y < row.y+4
									&& p.x < row.x + row.length/2 && row.x < p.x + p.length/2 ) {
								platformRows++;
								break;
							}
						}						
					}
				}
				int strandedRows = coinRows.size() - platformRows - groundRows;
				score -= 100*strandedRows;
				
				score += (int) (300 - 15*(Math.abs(playerCoinsCollected - (100*(platformRows / (double)plats.size()))))); //match platforms with coins ratio to coin collection ratio
				
				//score distribution of coins
				double coinMeanX = 0;
				double coinMeanY = 0;
				for (CoinRow r : coinRows) {
					coinMeanX += r.x;
					coinMeanY += r.y;
				}
				coinMeanX /= (double) coinRows.size();
				coinMeanY /= (double) coinRows.size();
				double coinDiffMeanX = 0;
				double coinDiffMeanY = 0;
				for (CoinRow r : coinRows) {
					coinDiffMeanX += Math.pow(coinMeanX - r.x,2);
					coinDiffMeanY += Math.pow(coinMeanY - r.y,2);
				}
				coinDiffMeanX /= (double) coinRows.size();
				coinDiffMeanX = Math.sqrt(coinDiffMeanX);
				coinDiffMeanY /= (double) coinRows.size();
				coinDiffMeanY = Math.sqrt(coinDiffMeanY);
				int coinYstdevScore = (int) (150 - 35*(Math.abs(4-coinDiffMeanY))); //MAGIC NUMBERS
				int coinXstdevScore = (int) (150 - 25*(Math.abs(10-coinDiffMeanX)));
				score += coinYstdevScore + coinXstdevScore;
				
				
				
				
				
//				System.out.print("CHUNK HOARDER SCORE: " );
//				System.out.println(score);
				return score;
	}

	private int evaluateHunterChunk(MyLevel level, int chunkStartColumn,
			int[] playerScores, int difficulty, int chunkWidth) {
		int playerKillScore = playerScores[2];
		int score = 0;
		byte[][] chunk = new byte[chunkWidth][level.getHeight()];
		SpriteTemplate[][] chunksprites = new SpriteTemplate[chunkWidth][level.getHeight()];
		int numEnemies = 0;
		for (int x = 0; x< chunkWidth; x++){
			for (int y = 0; y < level.getHeight(); y++){
				chunk[x][y] = level.getBlock(x+chunkStartColumn, y);
				chunksprites[x][y] = level.getSpriteTemplate(x+chunkStartColumn, y);
				if (chunksprites[x][y] != null) {
					numEnemies++;
				}
			}
		}
		ArrayList<Platform> plats = findPlatforms(chunk);
		
		//score num enemies
		double idealNumEnemies = difficulty * 3;
		idealNumEnemies *= (playerKillScore/100.0);
		score += (int) (200 - 30*(Math.abs(numEnemies - idealNumEnemies))); //MAGIC NUMBERS
		
		//score enemies on plats
		int enemiesOnPlats = 0;
		for (Platform p :plats ){
			for (int x = p.x; x < p.x + p.length; x++) {
				for (int y = p.y-1; y > p.y-4 && y >= 0; y--) {
					if (chunksprites[x][y] != null){
						enemiesOnPlats++;
					}
				}
			}
		}
		score += (int) (200 - 30*(Math.abs(enemiesOnPlats - (numEnemies/2)))); //MAGIC NUMBERS
		
		//score enemy distribution
		double meanEnemyX = 0;
		for (int x = 0; x< chunkWidth; x++){
			for (int y = 0; y < level.getHeight(); y++){
				if (chunksprites[x][y] != null) {
					meanEnemyX += x;
				}
			}
		}
		meanEnemyX /= (double) numEnemies;
		double meanDiffEnemyX = 0;
		for (int x = 0; x< chunkWidth; x++){
			for (int y = 0; y < level.getHeight(); y++){
				if (chunksprites[x][y] != null) {
					meanDiffEnemyX = Math.pow(meanEnemyX - x, 2);
				}
			}
		}
		meanDiffEnemyX /= (double) numEnemies;
		meanDiffEnemyX = Math.sqrt(meanDiffEnemyX);
		
		
		int meanEnemyXScore = (int) (100 - 10*(Math.abs(20-meanEnemyX))); //MAGIC NUMBERS
		int idealEnemyXstdev = 7 - difficulty;
		int enemyXstdevScore = (int) (100 - 25*(Math.abs(idealEnemyXstdev-meanDiffEnemyX)));
		score += meanEnemyXScore + enemyXstdevScore;
		//end enemy distro
		
		
//		System.out.print("CHUNK HUNTER SCORE: " );
//		System.out.println(score);
		return score;
	}

	private int evaluateJumperChunk(MyLevel level, int chunkStartColumn,
		int[] playerScores, int difficulty, int chunkWidth) {
		int score = 0;
		byte[][] chunk = new byte[chunkWidth][level.getHeight()];
		SpriteTemplate[][] chunksprites = new SpriteTemplate[chunkWidth][level.getHeight()];
		int numEnemies = 0;
		for (int x = 0; x< chunkWidth; x++){
			for (int y = 0; y < level.getHeight(); y++){
				chunk[x][y] = level.getBlock(x+chunkStartColumn, y);
				chunksprites[x][y] = level.getSpriteTemplate(x+chunkStartColumn, y);
				if (chunksprites[x][y] != null) {
					numEnemies++;
				}
			}
		}
		ArrayList<Platform> plats = findPlatforms(chunk);
		ArrayList<Wall> walls = findWalls(chunk);
		
		
	
		//score distribution of platforms and walls
		double platMeanX = 0;
		double platMeanY = 0;
		double wallMeanX = 0;
		for (Platform p : plats) {
			platMeanX += p.x;
			platMeanY += p.y;
		}
		platMeanX /= (double) plats.size();
		platMeanY /= (double) plats.size();
		for (Wall w: walls) {
			wallMeanX += w.x;
		}
		wallMeanX /= (double) walls.size();
		double platDiffMeanX = 0;
		double platDiffMeanY = 0;
		double wallDiffMeanX = 0;
		for (Platform p : plats) {
			platDiffMeanX += Math.pow(platMeanX - p.x,2);
			platDiffMeanY += Math.pow(platMeanY - p.y,2);
		}
		platDiffMeanX /= (double) plats.size();
		platDiffMeanX = Math.sqrt(platDiffMeanX);
		platDiffMeanY /= (double) plats.size();
		platDiffMeanY = Math.sqrt(platDiffMeanY);
		for (Wall w: walls) {
			wallDiffMeanX += Math.pow(w.x - wallMeanX, 2);
		}
		wallDiffMeanX /= (double) walls.size();
		wallDiffMeanX = Math.sqrt(wallDiffMeanX);
		
		int platMeanYScore = (int) (100 - 40*(Math.abs(9-platMeanY)));
		int platMeanXScore = (int) (100 - 10*(Math.abs(20-platMeanX)));
		int platYstdevScore = (int) (100 - 20*(Math.abs(4-platDiffMeanY))); //MAGIC NUMBERS
		int platXstdevScore = (int) (100 - 10*(Math.abs(10-platDiffMeanX)));
		int wallXstdevScore = (int) (100 - 10*(Math.abs(10-wallDiffMeanX)));
		
		score += platMeanYScore + platYstdevScore + platXstdevScore + wallXstdevScore + platMeanXScore;
		//end scoring distribution of platforms and walls
		
		//possible todo verify tall walls?
		//score tall walls?
		
		//score platform sizes
		int avgPlatSize = 0;
		for (Platform p : plats) {
			avgPlatSize += p.length;
		}
		avgPlatSize /= (double) plats.size();
		int platSizeScore = (int) (100 - 40*(Math.abs((9-(2*difficulty))-avgPlatSize)));
		score += platSizeScore;                                                  //MAGIC NUMBERS
		//end score platform sizes
		
		
		
		
		
		
//		System.out.print("CHUNK JUMPER SCORE: " );
//		System.out.println(score);
				
		return score;
	}
	
	
	private ArrayList<CoinRow> findCoinRows(byte[][] chunk) {
		ArrayList<CoinRow> coinRows = new ArrayList<>();
		
		for (int y = 0; y < chunk[0].length; y++) {
			for (int x = 0; x < chunk.length; x++) {
				if (chunk[x][y] == (byte)(2+2*16)) {
					int length = 1;
					int startx = x;
					x++;
					while (x < chunk.length && isSurface(chunk[x][y])) {
						length++;
						x++;
					}
					coinRows.add(new CoinRow(startx, y, length));
				}
			}
		}
		
		return coinRows;
	}
	
	private ArrayList<Wall> findWalls(byte[][] chunk) {
		ArrayList<Wall> walls = new ArrayList<>();
		
		for (int x = 0; x < chunk.length; x++) {
			for (int y = 0; y < chunk[0].length; y++) {
				if (isWall(chunk[x][y])) {
					int length = 1;
					int starty = y;
					y++;
					while (y < chunk[x].length && isWall(chunk[x][y])) {
						length++;
						y++;
					}
					walls.add(new Wall(x, starty, length, true));
				}
			}
		}
		
		return walls;
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
	
	private boolean isWall(byte b) {
		return  b == (byte) (10+0*16)  || b == (byte) (10 + 1 * 16) || b == (byte)(14+1*16) || b == (byte)(14+2*16) || b == (byte) (14) || b==(byte)(0+9*16) || b == (byte)(0+8*16) ;
		//          TUBETOPLEFT                     TUBESIDELEFT                 cannon neck          cannon foot              cannon head     left grass           left grass corner  
	}

	private boolean isSurface(byte b) {
		
		return  b == (byte)(11)  || b == (byte) (10+0*16) || b == (byte) (5 + 8 * 16) || b == (byte) (4 + 8 * 16) || b == (byte) (6 + 8 * 16) || b == (byte) (4 + 11 * 16) || b == (byte) (6 + 11 * 16) || b == (byte) (0 + 1 * 16) || b == (byte) (4 + 2 + 1 * 16) || b == (byte) (4+1+1*16) || b == (byte)(3+1*16);
		//           TUBETOPRIGHT             TUBETOPLEFT            HILLTOP                 HILLTOPLEFT                HILLTOPRIGHT              HILLLTOPLEFTIN             HILLTOPIN                        BLOCKEMPTY                       BLOCKPOWER                BLOCKCOIN
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
	private class Wall {
		int x;
		int y;
		int length;
		boolean solid;
		
		public Wall(int x, int y, int length, boolean solid) {
			this.x=x;
			this.y=y;
			this.length=length;
			this.solid=solid;
		}
	}
	private class CoinRow {
		int x;
		int y;
		int length;
		public CoinRow(int x, int y, int length) {
			this.x=x;
			this.y=y;
			this.length=length;
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

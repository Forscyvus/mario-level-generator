package dk.itu.mario.level;

import java.util.Random;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.engine.sprites.Enemy;


public class MyLevel extends Level{
	
	
	
	
	//Store information about the level
	 public   int ENEMIES = 0; //the number of enemies the level contains
	 public   int BLOCKS_EMPTY = 0; // the number of empty blocks
	 public   int BLOCKS_COINS = 0; // the number of coin blocks
	 public   int BLOCKS_POWER = 0; // the number of power blocks
	 public   int COINS = 0; //These are the coins that Mario collect
	 public int TOTAL_GAP_SIZE = 0;
	 public int TOTAL_PLATFORMS = 0; //not the floor
	 public int AVG_PLATFORM_SIZE = 0;
	 public int SHELLED_ENEMIES = 0;
	 public int TALL_WALLS = 0;//walls/pipes/etc that cannot be directly jumped over (require another platform)

	 private static final int ODDS_STRAIGHT = 0;
	 private static final int ODDS_HILL_STRAIGHT = 1;
	 private static final int ODDS_TUBES = 2;
	 private static final int ODDS_JUMP = 3;
	 private static final int ODDS_CANNONS = 4;

	 private int[] odds = new int[5];
	 private int totalOdds;
 
	private static Random levelSeedRandom = new Random();
    public static long lastSeed;

    Random rng;
    public static final int CHUNK_SIZE = 40;
    public static final int LEVEL_MARGIN_SIZE = 10;


    private int difficulty;
    private int type;
	private int gaps;

	private GamePlay player;
	private int[] playerScores;

	public Archetype[] chunkTypes;
	public int numChunks;
	
	public enum Archetype {
		HUNTER, HOARDER, JUMPER
	}

	// public static MyLevel generateInitialLevel(GamePlay player) {
	// 	return new Mylevel();
	// }
	
	public MyLevel(int width, int height)
    {
		super(width, height);
		numChunks = (width - 2*LEVEL_MARGIN_SIZE) / CHUNK_SIZE;
		rng = new Random();
		chunkTypes = new Archetype[numChunks];
 
    }


	public MyLevel(int width, int height, long seed, int[] playerScores, int difficulty, int type, GamePlay playerMetrics)
    {
        this(width, height);
        player = playerMetrics;
        this.playerScores = playerScores; 
        creat(seed, difficulty, type); //generates initial level
    }

    public void creat(long seed, int difficulty,int type){
    	//create exit
    	xExit = getWidth() - LEVEL_MARGIN_SIZE;
        yExit = 14;
        
    	initializeFloor();
    	int floorheight = getHeight()-1; //for stitching purposes
    	
    	//generate chunks
    	int playerScoreTotal = playerScores[0] + playerScores[1] + playerScores[2];
    	for (int i = 0; i < numChunks; i++){
    		int rando = rng.nextInt(playerScoreTotal);
    		if (rando < playerScores[0]){
    			chunkTypes[i] = Archetype.JUMPER;
    		} else if (rando - playerScores[0] < playerScores[1]) {
    			chunkTypes[i] = Archetype.HOARDER;
    		} else {
    			chunkTypes[i] = Archetype.HUNTER;
    		}
    		chunkTypes[i] = Archetype.HUNTER;
    		floorheight = generateChunk(i, floorheight);
    	}
        System.out.println("Coins: " + COINS + ", Total Gap Width: " + TOTAL_GAP_SIZE + ", Enemies: " + ENEMIES);
        fixWalls();
    }
    
    public int generateChunk(int i, int floorheight) {
    	int chunkloc = LEVEL_MARGIN_SIZE + i*CHUNK_SIZE;
    	switch(chunkTypes[i]) {
    	case JUMPER:
    		floorheight = generateJumperChunk(chunkloc, floorheight);
    		break;
    	case HOARDER:
    		floorheight = generateHoarderChunk(chunkloc, floorheight);
    		break;
    	case HUNTER:
    		floorheight = generateHunterChunk(chunkloc, floorheight);
    		break;
    	}
    	
    	setBlock(chunkloc, 0, ROCK); //blockmarker
    	return floorheight;
    }

    private int generateJumperChunk(int chunkloc, int floorheight) {
        odds[ODDS_STRAIGHT] = 10;
        odds[ODDS_HILL_STRAIGHT] = 40;
        odds[ODDS_TUBES] = 20;
        odds[ODDS_JUMP] = 5;
        if(difficulty > 1){
            odds[ODDS_JUMP] += 10;
        }
        odds[ODDS_CANNONS] = 10 + 5*difficulty;

        for (int i = 0; i < odds.length; i++) {
            //failsafe (no negative odds)
            if (odds[i] < 0) {
                odds[i] = 0;
            }

            totalOdds += odds[i];
            odds[i] = totalOdds - odds[i];
        }


        int length = chunkloc;

        //create the chunk, zone by zone
         while (length < chunkloc + CHUNK_SIZE - 4) {
            length += buildZone(length, Math.min(CHUNK_SIZE - 4, getWidth() - length), Archetype.JUMPER);
        }

        int floor = height - 1 - rng.nextInt(4);

        for (int x = length; x < chunkloc + CHUNK_SIZE; x++) {
            for (int y = 0; y < height; y++) {
                if (y >= floor) {
                    setBlock(x, y, Level.GROUND);
                }
            }
        }

        
        
        //should definitely do something with this
		return floorheight;
	}

    private int buildZone(int x, int maxLength, Archetype chunkType) {
        int t = rng.nextInt(totalOdds);
        int type = 0;

        for (int i = 0; i < odds.length; i++) {
            if (odds[i] <= t) {
                type = i;
            }
        }

        switch (type) {
        case ODDS_STRAIGHT:
            return buildStraight(x, maxLength, false, chunkType);
        case ODDS_HILL_STRAIGHT:
            if(maxLength > 10) break;
            return buildHillStraight(x, maxLength, chunkType);
        case ODDS_TUBES:
            return buildTubes(x, maxLength);
        case ODDS_JUMP:
            return buildJump(x, maxLength);
        case ODDS_CANNONS:
            return buildCannons(x, maxLength);
        }
        return 0;
    }

	private int generateHoarderChunk(int chunkloc, int floorheight) {
		odds[ODDS_STRAIGHT] = 30;
        odds[ODDS_HILL_STRAIGHT] = 30;
        odds[ODDS_TUBES] = 10;
        odds[ODDS_JUMP] = 5;
        if(difficulty > 1){
            odds[ODDS_JUMP] += 5;
        }
        odds[ODDS_CANNONS] = 5 + 5*difficulty;

        for (int i = 0; i < odds.length; i++) {
            //failsafe (no negative odds)
            if (odds[i] < 0) {
                odds[i] = 0;
            }

            totalOdds += odds[i];
            odds[i] = totalOdds - odds[i];
        }


        int length = chunkloc;

        //create the chunk, zone by zone
         while (length < chunkloc + CHUNK_SIZE) {
            length += buildZone(length, chunkloc + CHUNK_SIZE - length, Archetype.HOARDER);
        }
        
        int floor = height - 1 - rng.nextInt(4);

        for (int x = length; x < chunkloc + CHUNK_SIZE; x++) {
            for (int y = 0; y < height; y++) {
                if (y >= floor) {
                    setBlock(x, y, Level.GROUND);
                }
            }
        }

        //should definitely do something with this
        return floorheight;
	}


	private int generateHunterChunk(int chunkloc, int floorheight) {
		odds[ODDS_STRAIGHT] = 20;
        odds[ODDS_HILL_STRAIGHT] = 30;
        odds[ODDS_TUBES] = 20;
        odds[ODDS_JUMP] = 5;
        if(difficulty > 1){
            odds[ODDS_JUMP] += 5;
        }
        odds[ODDS_CANNONS] = 10 + 10*difficulty;

        for (int i = 0; i < odds.length; i++) {
            //failsafe (no negative odds)
            if (odds[i] < 0) {
                odds[i] = 0;
            }

            totalOdds += odds[i];
            odds[i] = totalOdds - odds[i];
        }


        int length = chunkloc;

        //create the chunk, zone by zone
         while (length < chunkloc + CHUNK_SIZE) {
            length += buildZone(length, chunkloc + CHUNK_SIZE - length, Archetype.HUNTER);
        }
        
        int floor = height - 1 - rng.nextInt(4);

        for (int x = length; x < chunkloc + CHUNK_SIZE; x++) {
            for (int y = 0; y < height; y++) {
                if (y >= floor) {
                    setBlock(x, y, Level.GROUND);
                }
            }
        }

        //should definitely do something with this
		return floorheight;
	}


	public void initializeFloor(){
    	/*for (int x = 0; x < getWidth(); x++) {
    		setBlock(x, getHeight()-1, HILL_TOP);
    	}*/
        //only initialize start and end zones
        for (int x = 0; x < LEVEL_MARGIN_SIZE; x++) {
            setBlock(x, getHeight()-1, HILL_TOP);
        }
        for (int x = getWidth()-LEVEL_MARGIN_SIZE; x < getWidth(); x++) {
            setBlock(x, getHeight()-1, HILL_TOP);
        }
    }

    public MyLevel generateChild(MyLevel otherLevel) {
    	MyLevel child = null;
    	try {
    		child = clone();
    	} catch (CloneNotSupportedException cnsE) {
    		return null;
    	}
    	for(int chunkStart = LEVEL_MARGIN_SIZE; chunkStart < getWidth() - LEVEL_MARGIN_SIZE - CHUNK_SIZE; chunkStart += CHUNK_SIZE){
    		
    		int check = rng.nextInt(2);  //50-50 chance to use otherLevel's chunk
    		byte[][] map = otherLevel.getMap();
    		SpriteTemplate[][] st = otherLevel.getSpriteTemplate();
    		if(check == 1){

    			//child.chunkTypes[chunk] = otherLevel.chunkTypes[chunk];
    			for(int i = chunkStart; i < chunkStart + CHUNK_SIZE - 4; i++){
    				for(int j = 0; j < getHeight(); j++){
    					child.setBlock(i, j, map[i][j]);
	    				child.setSpriteTemplate(i, j, st[i][j]);
    				}
    			}
    		}
    	}
    	return child;
    }


	public MyLevel clone() throws CloneNotSupportedException {

	    	MyLevel clone = new MyLevel(width, height);

	    	clone.xExit = xExit;
	    	clone.yExit = yExit;
	    	byte[][] map = getMap();
	    	SpriteTemplate[][] st = getSpriteTemplate();

	    	clone.numChunks = numChunks;
	    	clone.chunkTypes = chunkTypes.clone();
	    	
	    	for (int i = 0; i < map.length; i++)
	    		for (int j = 0; j < map[i].length; j++) {
	    			clone.setBlock(i, j, map[i][j]);
	    			clone.setSpriteTemplate(i, j, st[i][j]);
	    	}
	    	clone.BLOCKS_COINS = BLOCKS_COINS;
	    	clone.BLOCKS_EMPTY = BLOCKS_EMPTY;
	    	clone.BLOCKS_POWER = BLOCKS_POWER;
	    	clone.ENEMIES = ENEMIES;
	    	clone.COINS = COINS;
	    	
	        return clone;
	}

    private int buildJump(int xo, int maxLength)
    {	gaps++;
    	//jl: jump length
    	//js: the number of blocks that are available at either side for free
        int js = rng.nextInt(4) + 2;
        int jl = rng.nextInt(2) + 2;
        TOTAL_GAP_SIZE += jl;
        int length = js * 2 + jl;

        boolean hasStairs = rng.nextInt(3) == 0;

        int floor = height - 1 - rng.nextInt(4);
      //run from the start x position, for the whole length
        for (int x = xo; x < xo + length; x++)
        {
            if (x < xo + js || x > xo + length - js - 1)
            {
            	//run for all y's since we need to paint blocks upward
                for (int y = 0; y < height; y++)
                {	//paint ground up until the floor
                    if (y >= floor)
                    {
                        setBlock(x, y, GROUND);
                    }
                  //if it is above ground, start making stairs of rocks
                    else if (hasStairs)
                    {	//LEFT SIDE
                        if (x < xo + js)
                        { //we need to max it out and level because it wont
                          //paint ground correctly unless two bricks are side by side
                            if (y >= floor - (x - xo) + 1)
                            {
                                setBlock(x, y, ROCK);
                            }
                        }
                        else
                        { //RIGHT SIDE
                            if (y >= floor - ((xo + length) - x) + 2)
                            {
                                setBlock(x, y, ROCK);
                            }
                        }
                    }
                }
            }
        }

        return length;
    }

    private int buildCannons(int xo, int maxLength)
    {
        int length = rng.nextInt(10) + 2;
        if (length > maxLength) length = maxLength;

        int floor = height - 1 - rng.nextInt(4);
        int xCannon = xo + 1 + rng.nextInt(4);
        for (int x = xo; x < xo + length; x++)
        {
            if (x > xCannon)
            {
                xCannon += 2 + rng.nextInt(4);
            }
            if (xCannon == xo + length - 1) xCannon += 10;
            int cannonHeight = floor - rng.nextInt(4) - 1;

            for (int y = 0; y < height; y++)
            {
                if (y >= floor)
                {
                    setBlock(x, y, GROUND);
                }
                else
                {
                    if (x == xCannon && y >= cannonHeight)
                    {
                        if (y == cannonHeight)
                        {
                            setBlock(x, y, (byte) (14 + 0 * 16));
                        }
                        else if (y == cannonHeight + 1)
                        {
                            setBlock(x, y, (byte) (14 + 1 * 16));
                        }
                        else
                        {
                            setBlock(x, y, (byte) (14 + 2 * 16));
                        }
                    }
                }
            }
        }

        return length;
    }

    private int buildHillStraight(int xo, int maxLength, Archetype chunkType)
    {
        int length = rng.nextInt(10) + 10;
        if (length > maxLength) length = maxLength;

        int floor = height - 1 - rng.nextInt(4);
        for (int x = xo; x < xo + length; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if (y >= floor)
                {
                    setBlock(x, y, GROUND);
                }
            }
        }

        addEnemyLine(xo + 1, xo + length - 1, floor - 1, chunkType);
        if(chunkType == Archetype.HUNTER){
            addEnemyLine(xo + 1, xo + length - 1, floor - 1, chunkType);
        }

        int h = floor;

        boolean keepGoing = true;

        boolean[] occupied = new boolean[length];
        while (keepGoing )
        {
            h = h - 2 - rng.nextInt(3);

            if (h <= 0)
            {
                keepGoing = false;
            }
            else
            {
                int l = rng.nextInt(5) + 3;
                if((length - l - 2) <= 0){
                    return 0;
                }
                int xxo = rng.nextInt(length - l - 2) + xo + 1;

                if (occupied[xxo - xo] || occupied[xxo - xo + l] || occupied[xxo - xo - 1] || occupied[xxo - xo + l + 1])
                {
                    keepGoing = false;
                }
                else
                {
                    occupied[xxo - xo] = true;
                    occupied[xxo - xo + l] = true;
                    addEnemyLine(xxo, xxo + l, h - 1, chunkType);
                    if(chunkType == Archetype.HUNTER){
                        addEnemyLine(xo + 1, xo + length - 1, floor - 1, chunkType);
                    }
                    if (rng.nextInt(4) == 0)
                    {
                        decorate(xxo - 1, xxo + l + 1, h, chunkType);
                        if(chunkType == Archetype.HOARDER){
                            decorate(xxo - 1, xxo + l + 1, h, chunkType);
                        }
                        keepGoing = false;
                    }
                    for (int x = xxo; x < xxo + l; x++)
                    {
                        for (int y = h; y < floor; y++)
                        {
                            int xx = 5;
                            if (x == xxo) xx = 4;
                            if (x == xxo + l - 1) xx = 6;
                            int yy = 9;
                            if (y == h) yy = 8;

                            if (getBlock(x, y) == 0)
                            {
                                setBlock(x, y, (byte) (xx + yy * 16));
                            }
                            else
                            {
                                if (getBlock(x, y) == HILL_TOP_LEFT) setBlock(x, y, HILL_TOP_LEFT_IN);
                                if (getBlock(x, y) == HILL_TOP_RIGHT) setBlock(x, y, HILL_TOP_RIGHT_IN);
                            }
                        }
                    }
                }
            }
        }

        return length;
    }

    private void addEnemyLine(int x0, int x1, int y, Archetype chunkType)
    {
        for (int x = x0; x < x1; x++)
        {
            int die = 35;
            if(chunkType == Archetype.HUNTER){
                die = 15;
            }
            if (rng.nextInt(die) < difficulty + 1)
            {
                int type = rng.nextInt(4);

                if (difficulty < 1)
                {
                    type = Enemy.ENEMY_GOOMBA;
                }
                else if (difficulty < 3)
                {
                    type = rng.nextInt(3);
                }

                setSpriteTemplate(x, y, new SpriteTemplate(type, rng.nextInt(35) < difficulty));
                ENEMIES++;
            }
        }
    }

    private int buildTubes(int xo, int maxLength)
    {
        int length = rng.nextInt(10) + 5;
        if (length > maxLength) length = maxLength;

        int floor = height - 1 - rng.nextInt(4);
        int xTube = xo + 1 + rng.nextInt(4);
        int tubeHeight = floor - rng.nextInt(2) - 2;
        for (int x = xo; x < xo + length; x++)
        {
            if (x > xTube + 1)
            {
                xTube += 3 + rng.nextInt(4);
                tubeHeight = floor - rng.nextInt(2) - 2;
            }
            if (xTube >= xo + length - 2) xTube += 10;

            if (x == xTube && rng.nextInt(11) < difficulty + 1)
            {
                setSpriteTemplate(x, tubeHeight, new SpriteTemplate(Enemy.ENEMY_FLOWER, false));
                ENEMIES++;
            }

            for (int y = 0; y < height; y++)
            {
                if (y >= floor)
                {
                    setBlock(x, y,GROUND);

                }
                else
                {
                    if ((x == xTube || x == xTube + 1) && y >= tubeHeight)
                    {
                        int xPic = 10 + x - xTube;

                        if (y == tubeHeight)
                        {
                        	//tube top
                            setBlock(x, y, (byte) (xPic + 0 * 16));
                        }
                        else
                        {
                        	//tube side
                            setBlock(x, y, (byte) (xPic + 1 * 16));
                        }
                    }
                }
            }
        }

        return length;
    }

    private int buildStraight(int xo, int maxLength, boolean safe, Archetype chunkType)
    {
        int length = rng.nextInt(10) + 2;

        if (safe)
        	length = 10 + rng.nextInt(5);

        if (length > maxLength)
        	length = maxLength;

        int floor = height - 1 - rng.nextInt(4);

        //runs from the specified x position to the length of the segment
        for (int x = xo; x < xo + length; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if (y >= floor)
                {
                    setBlock(x, y, GROUND);
                }
            }
        }

        if (!safe)
        {
            if (length > 5)
            {
                decorate(xo, xo + length, floor, chunkType);
                if(chunkType == Archetype.HOARDER){
                    decorate(xo, xo + length, floor,chunkType);
                }
            }
        }

        return length;
    }

    private void decorate(int xStart, int xLength, int floor, Archetype chunkType)
    {
    	//if its at the very top, just return
        if (floor < 1)
        	return;

        //        boolean coins = rng.nextInt(3) == 0;
        boolean rocks = true;

        //add an enemy line above the box
        addEnemyLine(xStart + 1, xLength - 1, floor - 1, chunkType);
        if(chunkType == Archetype.HUNTER){
            addEnemyLine(xStart + 1, xLength - 1, floor - 1, chunkType);
        }

        int s = rng.nextInt(4);
        int e = rng.nextInt(4);

        if (floor - 2 > 0){
            if ((xLength - 1 - e) - (xStart + 1 + s) > 1){
                for(int x = xStart + 1 + s; x < xLength - 1 - e; x++){
                    setBlock(x, floor - 2, COIN);
                    COINS++;
                }
            }
        }

        s = rng.nextInt(4);
        e = rng.nextInt(4);
        
        //this fills the set of blocks and the hidden objects inside them
        if (floor - 4 > 0)
        {
            if ((xLength - 1 - e) - (xStart + 1 + s) > 2)
            {
                for (int x = xStart + 1 + s; x < xLength - 1 - e; x++)
                {
                    if (rocks)
                    {
                        if (x != xStart + 1 && x != xLength - 2 && rng.nextInt(3) == 0)
                        {
                            if (rng.nextInt(4) == 0)
                            {
                                setBlock(x, floor - 4, BLOCK_POWERUP);
                                BLOCKS_POWER++;
                            }
                            else
                            {	//the fills a block with a hidden coin
                                setBlock(x, floor - 4, BLOCK_COIN);
                                BLOCKS_COINS++;
                            }
                        }
                        else if (rng.nextInt(4) == 0)
                        {
                            if (rng.nextInt(4) == 0)
                            {
                                setBlock(x, floor - 4, (byte) (2 + 1 * 16));
                            }
                            else
                            {
                                setBlock(x, floor - 4, (byte) (1 + 1 * 16));
                            }
                        }
                        else
                        {
                            setBlock(x, floor - 4, BLOCK_EMPTY);
                            BLOCKS_EMPTY++;
                        }
                    }
                }
            }
        }
    }

    private void fixWalls()
    {
        boolean[][] blockMap = new boolean[width + 1][height + 1];

        for (int x = 0; x < width + 1; x++)
        {
            for (int y = 0; y < height + 1; y++)
            {
                int blocks = 0;
                for (int xx = x - 1; xx < x + 1; xx++)
                {
                    for (int yy = y - 1; yy < y + 1; yy++)
                    {
                        if (getBlockCapped(xx, yy) == GROUND){
                        	blocks++;
                        }
                    }
                }
                blockMap[x][y] = blocks == 4;
            }
        }
        blockify(this, blockMap, width + 1, height + 1);
    }

    private void blockify(Level level, boolean[][] blocks, int width, int height){
        int to = 0;
        if (type == LevelInterface.TYPE_CASTLE)
        {
            to = 4 * 2;
        }
        else if (type == LevelInterface.TYPE_UNDERGROUND)
        {
            to = 4 * 3;
        }

        boolean[][] b = new boolean[2][2];

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                for (int xx = x; xx <= x + 1; xx++)
                {
                    for (int yy = y; yy <= y + 1; yy++)
                    {
                        int _xx = xx;
                        int _yy = yy;
                        if (_xx < 0) _xx = 0;
                        if (_yy < 0) _yy = 0;
                        if (_xx > width - 1) _xx = width - 1;
                        if (_yy > height - 1) _yy = height - 1;
                        b[xx - x][yy - y] = blocks[_xx][_yy];
                    }
                }

                if (b[0][0] == b[1][0] && b[0][1] == b[1][1])
                {
                    if (b[0][0] == b[0][1])
                    {
                        if (b[0][0])
                        {
                            level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                        }
                        else
                        {
                            // KEEP OLD BLOCK!
                        }
                    }
                    else
                    {
                        if (b[0][0])
                        {
                        	//down grass top?
                            level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
                        }
                        else
                        {
                        	//up grass top
                            level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
                        }
                    }
                }
                else if (b[0][0] == b[0][1] && b[1][0] == b[1][1])
                {
                    if (b[0][0])
                    {
                    	//right grass top
                        level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
                    }
                    else
                    {
                    	//left grass top
                        level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
                    }
                }
                else if (b[0][0] == b[1][1] && b[0][1] == b[1][0])
                {
                    level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                }
                else if (b[0][0] == b[1][0])
                {
                    if (b[0][0])
                    {
                        if (b[0][1])
                        {
                            level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
                        }
                        else
                        {
                            level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
                        }
                    }
                    else
                    {
                        if (b[0][1])
                        {
                        	//right up grass top
                            level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
                        }
                        else
                        {
                        	//left up grass top
                            level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
                        }
                    }
                }
                else if (b[0][1] == b[1][1])
                {
                    if (b[0][1])
                    {
                        if (b[0][0])
                        {
                        	//left pocket grass
                            level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
                        }
                        else
                        {
                        	//right pocket grass
                            level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
                        }
                    }
                    else
                    {
                        if (b[0][0])
                        {
                            level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
                        }
                        else
                        {
                            level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
                        }
                    }
                }
                else
                {
                    level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
                }
            }
        }
    }
    
    // public RandomLevel clone() throws CloneNotSupportedException {

    // 	RandomLevel clone=new RandomLevel(width, height);

    // 	clone.xExit = xExit;
    // 	clone.yExit = yExit;
    // 	byte[][] map = getMap();
    // 	SpriteTemplate[][] st = getSpriteTemplate();
    	
    // 	for (int i = 0; i < map.length; i++)
    // 		for (int j = 0; j < map[i].length; j++) {
    // 			clone.setBlock(i, j, map[i][j]);
    // 			clone.setSpriteTemplate(i, j, st[i][j]);
    // 	}
    // 	clone.BLOCKS_COINS = BLOCKS_COINS;
    // 	clone.BLOCKS_EMPTY = BLOCKS_EMPTY;
    // 	clone.BLOCKS_POWER = BLOCKS_POWER;
    // 	clone.ENEMIES = ENEMIES;
    // 	clone.COINS = COINS;
    	
    //     return clone;

    //   }


}

package com.example.testapplication;

import android.util.Log;

import java.util.Random;

//NOTE TO SELF: TRY TO MAKE FIELD SIZE A CONSTANT OR SOMETHING
//SO GAME CAN AUTO UPDATE

public class MinesweeperModel {
    private static MinesweeperModel instance = null;
    private static int difficulty = 5;

    Random rand = new Random();

    private MinesweeperModel() {
    }

    public static MinesweeperModel getInstance() {
        if(instance == null) {
            instance = new MinesweeperModel();
        }
        return instance;
    }

    public static final short EMPTY = 0; //no neighbors are mines and not mine itself
    public static final short ONE = 1; //1 neighbor is a mine and not mine itself
    public static final short TWO = 2; //2 neighbors are a mine and not mine itself
    public static final short THREE = 3; //3 neighbors are a mine and not mine itself
    public static final short FOUR = 4; //4 neighbors are a mine and not mine itself
    public static final short FIVE = 5; //5 neighbors are a mine and not mine itself
    public static final short SIX = 6; //6 neighbors are a mine and not mine itself
    public static final short SEVEN = 7; //7 neighbors are a mine and not mine itself
    public static final short EIGHT = 8; //8 neighbors are a mine and not mine itself
    public static final short MINE = 9; //tile is a mine

    private short[][] model = {
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY}
    };

    public static final short REVEAL = 10; //tile is revealed
    public static final short FLAG = 11; //tile is flagged

    private short actionType = REVEAL;

    public static final short TOUCHED = 12;
    public static final short UNTOUCHED = 13;

    public short[][] cover = {
            {UNTOUCHED, UNTOUCHED, UNTOUCHED, UNTOUCHED, UNTOUCHED},
            {UNTOUCHED, UNTOUCHED, UNTOUCHED, UNTOUCHED, UNTOUCHED},
            {UNTOUCHED, UNTOUCHED, UNTOUCHED, UNTOUCHED, UNTOUCHED},
            {UNTOUCHED, UNTOUCHED, UNTOUCHED, UNTOUCHED, UNTOUCHED},
            {UNTOUCHED, UNTOUCHED, UNTOUCHED, UNTOUCHED, UNTOUCHED}
    };

    public void clearBoard() {
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                model[i][j] = EMPTY;
                cover[i][j] = UNTOUCHED;
            }
        }
        actionType = REVEAL;
    }

    public short getFieldContent(int x, int y) {
        return model[x][y];
    }

    public short getCoverContent(int x, int y) {
        return cover[x][y];
    }

    public void setCoverContent(int x, int y, short content) {
        cover[x][y] = content;
    }

    public short getTouched() { return TOUCHED; }

    public short getFlagged() {
        return FLAG;
    }

    public short getActionType() {
        return actionType;
    }

    public void actionFlag() {
        actionType = FLAG;
        Log.d("MODEL_TAG", "actionType = FLAG!");
    }

    public void actionReveal() {
        actionType = REVEAL;
        Log.d("MODEL_TAG", "actionType = REVEAL!");
    }

    public void setMines() {
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                if(rand.nextInt(difficulty) == 1) {
                    model[i][j] = MINE;
                    Log.i("MODEL_TAG", "Model[" + i + "][" + j + "] has a mine!");

                }
            }
        }
    }

    public void setMineCount() {
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                if(model[i][j] != MINE) {
                    int mineCounter = 0;
                    if(i > 0 && j > 0 && model[i-1][j-1] == MINE) {
                        mineCounter++;
                    }
                    if(i > 0 && model[i-1][j] == MINE) {
                        mineCounter++;
                    }
                    if(i > 0 && j < 4 && model[i-1][j+1] == MINE) {
                        mineCounter++;
                    }
                    if(j > 0 && model[i][j-1] == MINE) {
                        mineCounter++;
                    }
                    if(j < 4 && model[i][j+1] == MINE) {
                        mineCounter++;
                    }
                    if(i < 4 && j > 0 && model[i+1][j-1] == MINE) {
                        mineCounter++;
                    }
                    if(i < 4 && model[i+1][j] == MINE) {
                        mineCounter++;
                    }
                    if(i < 4 && j < 4 && model[i+1][j+1] == MINE) {
                        mineCounter++;
                    }

                    short mineCounterShort = intToShort(mineCounter);
                    model[i][j] = mineCounterShort;
                    Log.i("MODEL_TAG", "Model[" + i + "][" + j + "] has " + mineCounter + " mines around it!");
                }
            }
        }
    }

    private int countMines() {
        int mineCounter = 0;
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                if(model[i][j] == MINE) {
                    mineCounter++;
                }
            }
        }
        return mineCounter;
    }

    public boolean mineRevealed() {
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                if(model[i][j] == MINE && cover[i][j] == TOUCHED) {
                    Log.i("MODEL_TAG", "Mine touched!");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean nonmineFlagged() {
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                if(model[i][j] != MINE && cover[i][j] == FLAG) {
                    Log.i("MODEL_TAG", "Non-mine flagged");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean gameLost() {
        return mineRevealed() || nonmineFlagged();
    }

    public boolean checkAllTiles() {
        int minesFlagged = 0;
        int nonMinesOpened = 0;

        for(int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (model[i][j] == MINE && cover[i][j] == FLAG) {
                    minesFlagged++;
                }
                if (model[i][j] != MINE && cover[i][j] == TOUCHED) {
                    nonMinesOpened++;
                }
            }
        }
        return minesFlagged == countMines() && nonMinesOpened == (25 - countMines());
    }

    private short intToShort(int x) {
        switch(x) {
            case 1:
                return ONE;
            case 2:
                return TWO;
            case 3:
                return THREE;
            case 4:
                return FOUR;
            case 5:
                return FIVE;
            case 6:
                return SIX;
            case 7:
                return SEVEN;
            case 8:
                return EIGHT;
            default:
                return EMPTY;
        }
    }

    public static void setDifficulty(int newDifficulty) {
        difficulty = newDifficulty;
    }
}

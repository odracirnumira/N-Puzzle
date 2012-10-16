#ifndef ES_ODRACIRNUMIRA_NPUZZLE_NPUZZLE_H
#define ES_ODRACIRNUMIRA_NPUZZLE_NPUZZLE_H

#include <vector>
#include <inttypes.h>

using namespace std;

namespace npuzzle {

/**
 *
 */
class NPuzzle {

private:

	vector<int32_t> tilePositions;
	vector<int32_t> positionContents;
	int32_t n;
	int32_t numTiles;
	int32_t sideNumTiles;

	NPuzzle(int n);
	NPuzzle(int n, vector<int32_t> initialConfiguration);
	void chechTileConfiguration(vector<int32_t> tiles);
	vector<int32_t> getPositionsFromConfiguration(vector<int32_t> tiles);
	vector<int32_t> getNextPositions(int32_t tilePos);
	bool checkTilePosition(int32_t tilePos);
	bool checkTile(int32_t tile);
	static vector<int32_t> createDefaultTileConfiguration(int32_t n);
	static void checkN(int32_t n);
	static void checkSideSize(int32_t sideSize);

public:
	static const int32_t MIN_N = 3;
	static const int32_t MAX_N = 2147483647;
	static const int32_t MAX_SIDE_SIZE = 46340; //sqrt(MAX_N)
	static const int32_t MIN_SIDE_SIZE = 2;

	enum Direction {
		UP, DOWN, LEFT, RIGHT
	};

	void moveTileByPosition(int32_t tilePos);
	void moveTilesByPosition(vector<int32_t> tilePositions);
	void moveTile(int32_t tile);
	void moveTiles(vector<int32_t> tiles);
	int32_t getTilePosition(int32_t tile);
	int32_t getEmptyTilePosition();
	int32_t getTileAtPosition(int32_t tilePos);
	int32_t getNumTiles();
	int32_t getN();
	int32_t getSideNumTiles();
	int32_t** getPuzzleMatrix();
	int32_t* getTilePositions();
	int32_t* getTiles();
	bool isSolved();
	bool canMove(int32_t tile);
	bool canMoveByPosition(int32_t tilePos);
	bool isSolvable();
	Direction moveDirection(int32_t tile);
	Direction moveDirectionFromPosition(int32_t tilePos);
	Direction moveDirection(int32_t firstTilePos, int32_t secondTilePos);
	static NPuzzle newNPuzzleFromN(int32_t n);
	static NPuzzle newNPuzzleFromSideSize(int32_t sideSize);
	static NPuzzle newNPuzzleFromNAndConfiguration(int32_t n,
			vector<int32_t> initialConfiguration);
	static NPuzzle newNPuzzleFromSideSizeAndConfiguration(int32_t sideSize,
			vector<int32_t> initialConfiguration);
	static NPuzzle newRandomNPuzzleFromN(int32_t n);
	static NPuzzle newRandomNPuzzleFromSideSize(int32_t sideSize);

};

}

#endif

import java.util.ArrayList;

public class AI {

    //  depth the AI searches game tree
    private int depth;

    //  number to select which heuristic function AI will use
    private int evalFunc;

    //  used to generate legal moves in a given game position
    private MoveGenerator moveGenerator;

    //  point value for each piece
    int[] pieceScores;

    /**
     * Creates an AI to play checkers
     * @param depth - How deep you want this to check in the game tree
     * @param evalFunc - which heuristic function you want to use
     */
    public AI(int depth,int evalFunc){
        this.depth = depth;
        this.evalFunc = evalFunc;
        this.moveGenerator = new MoveGenerator();
        this.pieceScores = new int[]{1,1,5,5,3,3,3,3,9,9,0,0};
    }

    /**
     * Searches for the best move in the position and returns it
     * @param gameboard - state of the board
     * @return - randomly selects one move of the best moves in the position
     */
    public String getMove(Board gameboard){

        //  set up list of best possible moves
        ArrayList<Integer> possibleMoves = new ArrayList<>();

        //  generate all legal moves in position
        ArrayList<String> moves = this.moveGenerator.genMoves(gameboard);

        //  if its whites turn set value to -infinity else set to +infinity
        int value = (Integer.MIN_VALUE+1)*gameboard.turn;

        //  for all moves
        for(int i=0;i<moves.size();i++){

            //  calculate score for move
            int moveScore = alphaBeta(gameboard.makeMove(moves.get(i)),this.depth,Integer.MIN_VALUE,Integer.MAX_VALUE);

            //  if move is just as good as best move make it a candidate
            if(value==moveScore){
                possibleMoves.add(i);
            }

            //  if black minimise moves
            if(gameboard.turn==-1){
                if(value>moveScore){
                    possibleMoves.clear();
                    value = moveScore;
                    possibleMoves.add(i);
                }
            }

            //  if white maximise moves
            else {
                if(value<moveScore){
                    possibleMoves.clear();
                    value = moveScore;
                    possibleMoves.add(i);
                }
            }
        }

        //  select random move out of best move list
        return moves.get(possibleMoves.get((int)(Math.random()*possibleMoves.size())));
    }

    /**
     * Generates a score for a given checkers position using alpha beta pruning
     * @param gameboard - The state of the game
     * @param depth - How far you want to search in the game tree
     * @param alpha - pruning value set to -infinity
     * @param beta - pruning value set to +infinity
     * @return Score of position
     */
    private int alphaBeta(Board gameboard, int depth, int alpha, int beta){

        //  if game is over or depth is reached return heuristic value of board state
        if(depth==0){
            return (this.evalFunc==0?this.eval(gameboard):this.eval1(gameboard));
        }

        //  get all children of board
        ArrayList<String> moves = this.moveGenerator.genMoves(gameboard);

        //  if no moves exist than return as losing position
        if(moves.size()==0){
            Board newBoard = new Board(gameboard);
            newBoard.turn *= -1;
            if(moveGenerator.isKingSafe(newBoard)){
                return 0;
            }
            return gameboard.turn*(Integer.MIN_VALUE+1);
        }

        //  if white maximise
        if(gameboard.turn==1){
            int value = Integer.MIN_VALUE+1;
            for(int i=0;i<moves.size();i++){
                value = Math.max(value,alphaBeta(gameboard.makeMove(moves.get(i)),depth-1,alpha,beta));
                alpha = Math.max(alpha,value);
                if(alpha>=beta){
                    break;
                }
            }
            return value;
        }

        //  if black minimise
        else {
            int value = Integer.MAX_VALUE;
            for(int i=0;i<moves.size();i++){

                value = Math.min(value,alphaBeta(gameboard.makeMove(moves.get(i)),depth-1,alpha,beta));
                beta = Math.min(beta,value);
                if(alpha>=beta){
                    break;
                }
            }
            return value;
        }
    }

    /**
     * Gives the board a heuristic score based on
     *
     * @param gameboard - state of board
     * @return - number representing heuristic score of position
     */
    private int eval(Board gameboard){

        long kings;
        if(gameboard.turn==1){
            kings = gameboard.bitBoards[Names.whiteKings];
        }
        else {
            kings = gameboard.bitBoards[Names.blackKings];
        }

        ArrayList<String> moves = moveGenerator.genMoves(gameboard);
        if(moves.size()==0){
            Board newBoard = new Board(gameboard);
            newBoard.turn *= -1;
            if(!moveGenerator.isKingSafe(newBoard)){
                return Integer.MIN_VALUE*gameboard.turn;
            }
            return 0;
        }

        int score = 0;
        for(int i=0;i<5;i++){
            score += gameboard.pieceCounts[2*i];
            score -= gameboard.pieceCounts[2*i+1];
        }
        return score;
    }

    /**
     */
    private int eval1(Board gameboard){
        return 1;
    }

    public static void main(String[] args) {
        MoveGenerator.printBitBoard((Long.MIN_VALUE>>>53));
    }
}

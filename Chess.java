import java.util.ArrayList;
import java.util.Scanner;

public class Chess {

    public static void main(String[] args) {

        //testStuff();

        Scanner in = new Scanner(System.in);
        String move;

        AI ai = new AI(5,0);
        MoveGenerator mg = new MoveGenerator();

        Board board = new Board();
        board.print();


        while(true){

            ArrayList<String> moves = mg.genMoves(board);
            if(board.turn==1){
                while(true){
                    System.out.println("Moves: "+moves);
                    move = in.nextLine();
                    if(moves.contains(move)){
                        break;
                    }
                    System.out.println("Error please try again");
                }
            }
            else {
                System.out.println("AI is thinking");
                move = ai.getMove(board);
                System.out.println("AI chooses "+move);
            }

            board = board.makeMove(move);

            board.print();
        }

    }

    public static void testStuff(){
        MoveGenerator moveGenerator = new MoveGenerator();
        Board test = new Board(getTestBoard());
        test.print();

        System.out.println(moveGenerator.genMoves(test));

        System.exit(0);
    }

    public static String[][] getTestBoard(){

        return new String[][]{
                {"r","n","b","q","k","b","n","r"},
                {"p","p","p",".","p","p","p","p"},
                {".",".",".",".",".",".",".","."},
                {".",".",".",".",".",".",".","."},
                {".",".",".",".",".",".",".","."},
                {".",".",".",".",".",".",".","."},
                {"P","P","P",".","P","P","P","P"},
                {"R","N","B","Q","K","B","N","R"}
        };
    }
}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Board {

    int turn;

    boolean canKingCastleWhite;
    boolean canQueenCastleWhite;
    boolean canKingCastleBlack;
    boolean canQueenCastleBlack;

    long enpasant;

    long[] bitBoards;

    int[] pieceCounts;

    public Board(){

        this.turn = 1;

        this.canKingCastleWhite = true;
        this.canQueenCastleWhite = true;
        this.canKingCastleBlack = true;
        this.canQueenCastleBlack = true;

        this.enpasant = 0l;
        this.bitBoards = new long[]{    0xFFFF00000000FFFFl,0x000000000000FFFFl,0xFFFF000000000000l,
                                        0x000000000000FF00l,0x00FF000000000000l,0x0000000000000081l,
                                        0x8100000000000000l,0x0000000000000042l,0x4200000000000000l,
                                        0x0000000000000024l,0x2400000000000000l,0x0000000000000010l,
                                        0x1000000000000000l,0x0000000000000008l,0x0800000000000000l};
        this.pieceCounts = new int[]{8,8,2,2,2,2,1,1,1,1};
    }

    public Board(String[][] board){

        this.turn = 1;

        this.canKingCastleWhite = true;
        this.canQueenCastleWhite = true;
        this.canKingCastleBlack = true;
        this.canQueenCastleBlack = true;

        this.enpasant = 0l;

        this.bitBoards = new long[]{0l,0l,0l,0l,0l,0l,0l,0l,0l,0l,0l,0l,0l,0l,0l};
        this.pieceCounts = new int[]{8,8,2,2,2,2,1,1,1,1};
        long bit = Long.MIN_VALUE;
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                //  if square is empty
                if(board[i][j].equals(".")){
                    bit>>>=1;
                    continue;
                }

                this.bitBoards[Names.allPieces] |= bit;

                //  if white piece

                if(!board[i][j].toLowerCase().equals(board[i][j])){
                    this.bitBoards[Names.whitePieces] |= bit;


                    if(board[i][j].equalsIgnoreCase("q")){
                        this.bitBoards[Names.whiteQueens] |= bit;
                    }
                    else if(board[i][j].equalsIgnoreCase("r")){
                        this.bitBoards[Names.whiteRooks] |= bit;
                    }
                    else if(board[i][j].equalsIgnoreCase("b")){
                        this.bitBoards[Names.whiteBishops] |= bit;
                    }
                    else if(board[i][j].equalsIgnoreCase("p")){
                        this.bitBoards[Names.whitePawns] |= bit;
                    }
                    else if(board[i][j].equalsIgnoreCase("k")){
                        this.bitBoards[Names.whiteKings] |= bit;
                    }
                    else if(board[i][j].equalsIgnoreCase("n")){
                        this.bitBoards[Names.whiteKnights] |= bit;
                    }
                }

                //  if black piece
                else {
                    this.bitBoards[Names.blackPieces] |= bit;


                    if(board[i][j].equalsIgnoreCase("q")){
                        this.bitBoards[Names.blackQueens] |= bit;
                    }
                    else if(board[i][j].equalsIgnoreCase("r")){
                        this.bitBoards[Names.blackRooks] |= bit;
                    }
                    else if(board[i][j].equalsIgnoreCase("b")){
                        this.bitBoards[Names.blackBishops] |= bit;
                    }
                    else if(board[i][j].equalsIgnoreCase("p")){
                        this.bitBoards[Names.blackPawns] |= bit;
                    }
                    else if(board[i][j].equalsIgnoreCase("k")){
                        this.bitBoards[Names.blackKings] |= bit;
                    }
                    else if(board[i][j].equalsIgnoreCase("n")){
                        this.bitBoards[Names.blackKnights] |= bit;
                    }
                }

                bit>>>=1;
            }
        }
    }

    public Board(Board board){
        this.turn = board.turn;
        this.canKingCastleWhite = board.canKingCastleWhite;
        this.canQueenCastleWhite = board.canQueenCastleWhite;
        this.canKingCastleBlack = board.canKingCastleBlack;
        this.canQueenCastleBlack = board.canQueenCastleBlack;
        this.enpasant = board.enpasant;
        this.bitBoards = new long[15];
        for(int i=0;i<board.bitBoards.length;i++){
            this.bitBoards[i] = board.bitBoards[i];
        }
        this.pieceCounts = new int[12];
        for(int i=0;i<board.pieceCounts.length;i++){
            this.pieceCounts[i] = board.pieceCounts[i];
        }
    }

    public Board makeMove(String move){

        //  create new board to make move on
        Board newBoard = new Board(this);

        //  reset enpasant
        long enpasantBuffer = newBoard.enpasant;
        if(newBoard.turn==1){
            newBoard.enpasant &= 0x0000000000FF0000l;
        }
        else {
            newBoard.enpasant &= 0x0000FF0000000000l;
        }

        //  split up move into parts
        String[] moveArray = move.split("-");

        //  find starting square
        int startSQ = Integer.parseInt(moveArray[0]);
        long startBit = (Long.MIN_VALUE)>>>startSQ;

        //  find ending square
        int endSQ = Integer.parseInt(moveArray[1]);
        long endBit = (Long.MIN_VALUE)>>>endSQ;

        //  get special flag
        int promotion = 0;
        if(moveArray.length==3){
            String flag = moveArray[2];
            String[] pieces = {"R","N","B","Q"};
            for(int i=0;i<pieces.length;i++){
                if(flag.equalsIgnoreCase(pieces[i])){

                    if(flag.equals(pieces[i])){
                        promotion = i*2+5;

                    }
                    else {
                        promotion = i*2+6;
                    }
                }
            }
        }


        //  for all bit boards
        for(int i=3;i<newBoard.bitBoards.length;i++){


            //  kill the piece at the ending sq
            if((newBoard.bitBoards[i]&endBit)!=0){
                newBoard.bitBoards[i] &= ~endBit;
                newBoard.pieceCounts[i-3]--;
            }

            //  move the piece from its starting sq to ending sq
            if((newBoard.bitBoards[i]&startBit)!=0){

                //  clear the piece of this part of the board
                newBoard.bitBoards[i] &= ~startBit;

                //  set the piece on the new square
                newBoard.bitBoards[i] |= endBit;

                //  if white pawn move
                if(i==Names.whitePawns){

                    //  if move is double pawn move
                    if(startSQ-endSQ==16){
                        newBoard.enpasant = endBit>>>8;
                        break;
                    }
                }

                //  if black pawn move
                if(i==Names.blackPawns){

                    //  if move is double pawn move
                    if(endSQ-startSQ==16){
                        newBoard.enpasant = endBit<<8;
                        break;
                    }
                }

                //  if white king move
                else if(i==Names.whiteKings){

                    //  cant castle if you moved your king
                    newBoard.canKingCastleWhite = false;
                    newBoard.canQueenCastleWhite = false;

                    //  if castling king side
                    if(endSQ-startSQ==2){
                        //  move the king side rook
                        newBoard.bitBoards[Names.whiteRooks] &= ~0x1l;
                        newBoard.bitBoards[Names.whiteRooks] |= 0x4l;
                    }

                    //  if castling queen side
                    if(startSQ-endSQ==2){

                        //  move the queen side rook
                        newBoard.bitBoards[Names.whiteRooks] &= ~0x80l;
                        newBoard.bitBoards[Names.whiteRooks] |= 0x10l;
                    }
                }

                //  if black king move
                else if(i==Names.blackKings){

                    //  cant castle if you moved your king
                    newBoard.canKingCastleBlack = false;
                    newBoard.canQueenCastleBlack = false;

                    //  if castling king side
                    if(endSQ-startSQ==2){

                        //  move the king side rook
                        newBoard.bitBoards[Names.blackRooks] &= ~0x0100000000000000l;
                        newBoard.bitBoards[Names.blackRooks] |= 0x0400000000000000l;
                    }

                    //  if castling queen side
                    if(startSQ-endSQ==2){

                        //  move the queen side rook
                        newBoard.bitBoards[Names.blackRooks] &= ~0x8000000000000000l;
                        newBoard.bitBoards[Names.blackRooks] |= 0x1000000000000000l;
                    }
                }

                //  if white rook moves
                else if(i==Names.whiteRooks){
                    if(startSQ==56){
                        newBoard.canQueenCastleWhite = false;
                    }
                    else if(startSQ==63){
                        newBoard.canKingCastleWhite = false;
                    }
                }

                //  if black rook moves
                else if(i==Names.blackRooks){
                    if(startSQ==0){
                        newBoard.canQueenCastleBlack = false;
                    }
                    else if(startSQ==7){
                        newBoard.canKingCastleBlack = false;
                    }
                }
            }
        }

        //  if move was enpasant capture
        if((enpasantBuffer&endBit)!=0){

            if((enpasantBuffer&newBoard.bitBoards[Names.whitePawns])!=0){
                newBoard.bitBoards[Names.blackPawns] &= (~enpasantBuffer)>>>8;
                newBoard.pieceCounts[Names.blackPawns-3]--;
            }
            else if((enpasantBuffer&newBoard.bitBoards[Names.blackPawns])!=0){
                newBoard.bitBoards[Names.whitePawns] &= (~enpasantBuffer)<<8;
                newBoard.pieceCounts[Names.whitePawns-3]--;
            }
        }

        //  if promotion move
        if(promotion!=0){


            //  if whites move
            if(newBoard.turn==1){
                long bit = newBoard.bitBoards[Names.whitePawns]&0xFF00000000000000l;
                bit &= -bit;
                newBoard.bitBoards[promotion] |= bit;
                newBoard.bitBoards[Names.whitePawns] &= ~bit;
                newBoard.pieceCounts[Names.whitePawns-3]--;
                newBoard.pieceCounts[promotion-3]++;
            }

            //  if blacks move
            else {
                long bit = newBoard.bitBoards[Names.blackPawns]&0x00000000000000FFl;
                bit &= -bit;
                newBoard.bitBoards[promotion] |= bit;
                newBoard.bitBoards[Names.blackPawns] &= ~bit;
                newBoard.pieceCounts[Names.blackPawns-3]--;
                newBoard.pieceCounts[promotion-3]++;
            }
        }

        //  change turn
        newBoard.turn *= -1;

        //  fix bitboards
        newBoard.bitBoards[Names.whitePieces] = 0l;
        newBoard.bitBoards[Names.blackPieces] = 0l;
        for(int i=0;i<6;i++){
            newBoard.bitBoards[Names.whitePieces] |= newBoard.bitBoards[2*i+3];
            newBoard.bitBoards[Names.blackPieces] |= newBoard.bitBoards[2*i+4];
        }
        newBoard.bitBoards[Names.allPieces] = newBoard.bitBoards[Names.whitePieces] | newBoard.bitBoards[Names.blackPieces];

        //  return new board with move applied
        return newBoard;
        //  castle
        //  enpasant
        //  promotion
    }

    public void print(){

        String[] symbols = {"P","p","R","r","N","n","B","b","Q","q","K","k"};

        long bit = Long.MIN_VALUE;
        String[][] board = new String[8][8];
        for(int y=0;y<8;y++){
            for(int x=0;x<8;x++){
                board[x][y] = ".";
                for(int i=3;i<this.bitBoards.length;i++){
                    if((this.bitBoards[i]&bit)!=0){
                        board[x][y] = symbols[i-3];
                        break;
                    }
                }
                bit >>>= 1;
            }
        }

        System.out.print("   0 1 2 3 4 5 6 7\n");
        for(int y=0;y<8;y++) {
            System.out.printf("%2s ",(y*8));
            for (int x = 0; x < 8; x++) {
                System.out.print(board[x][y]+" ");
            }
            System.out.printf("%2s \n",(y*8));
        }
        System.out.print("   0 1 2 3 4 5 6 7\n");
    }

    public static void main(String[] args) {
        Board board = new Board();
        board = board.makeMove("4-2");
        System.out.println();
        board.print();
    }
}
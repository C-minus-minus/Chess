import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MoveGenerator {

    long[] rookMask;
    long[] rookMagic;
    long[] rookShift;
    long[][] rookDB;

    long[] bishopMask;
    long[] bishopMagic;
    long[] bishopShift;
    long[][] bishopDB;

    long[] kingDB;
    long[] knightDB;

    Map<Long,Integer> bitToSquare;

    public MoveGenerator(){

        //  pull rook mask from file
        this.rookMask = new long[64];
        try{
            Scanner in = new Scanner(new FileInputStream("numbers/rook_masks.txt"));
            for(int i=0;in.hasNextLine();i++){
                rookMask[i] = Long.parseLong(in.nextLine().trim(),16);
            }
        }catch (Exception e){}

        //  pull rook magics from file
        this.rookMagic = new long[64];
        try{
            Scanner in = new Scanner(new FileInputStream("numbers/rook_magics.txt"));
            for(int i=0;in.hasNextLine();i++){
                rookMagic[i] = Long.parseLong(in.nextLine().trim(),16);
            }
        }catch (Exception e){}

        //  pull rook shifts from file
        this.rookShift = new long[64];
        try{
            Scanner in = new Scanner(new FileInputStream("numbers/rook_shifts.txt"));
            for(int i=0;in.hasNextLine();i++){
                rookShift[i] = Long.parseLong(in.nextLine().trim(),10);
            }
        }catch (Exception e){}

        //  pull rook attack db from file
        this.rookDB = new long[64][];
        for(int i=0;i<64;i++) {
            rookDB[i] = new long[(int) (1l << (64 - rookShift[i]))];
        }
        try{
            Scanner in = new Scanner(new FileInputStream("numbers/rook_db.txt"));
            for(int i=0;i<rookDB.length;i++){
                for(int j=0;j<rookDB[i].length;j++){
                    this.rookDB[i][j] = Long.parseUnsignedLong(in.nextLine().trim());
                }
            }
        }catch (Exception e){}

        //  pull rook mask from file
        this.bishopMask = new long[64];
        try{
            Scanner in = new Scanner(new FileInputStream("numbers/bishop_masks.txt"));
            for(int i=0;in.hasNextLine();i++){
                bishopMask[i] = Long.parseLong(in.nextLine().trim(),16);
            }
        }catch (Exception e){}

        //  pull rook magics from file
        this.bishopMagic = new long[64];
        try{
            Scanner in = new Scanner(new FileInputStream("numbers/bishop_magics.txt"));
            for(int i=0;in.hasNextLine();i++){
                bishopMagic[i] = Long.parseLong(in.nextLine().trim(),16);
            }
        }catch (Exception e){}

        //  pull rook shifts from file
        this.bishopShift = new long[64];
        try{
            Scanner in = new Scanner(new FileInputStream("numbers/bishop_shifts.txt"));
            for(int i=0;in.hasNextLine();i++){
                bishopShift[i] = Long.parseLong(in.nextLine().trim(),10);
            }
        }catch (Exception e){}

        //  pull rook attack db from file
        this.bishopDB = new long[64][];
        for(int i=0;i<64;i++) {
            bishopDB[i] = new long[(int) (1l << (64 - bishopShift[i]))];
        }
        try{
            Scanner in = new Scanner(new FileInputStream("numbers/bishop_db.txt"));
            for(int i=0;i<bishopDB.length;i++){
                for(int j=0;j<bishopDB[i].length;j++){
                    this.bishopDB[i][j] = Long.parseUnsignedLong(in.nextLine().trim());
                }
            }
        }catch (Exception e){}

        //  pull king attack db from file
        this.kingDB = new long[64];
        try{
            Scanner in = new Scanner(new FileInputStream("numbers/king_db.txt"));
            for(int i=0;i<64;i++){
                this.kingDB[i] = Long.parseUnsignedLong(in.nextLine().trim());
            }

        }catch (Exception e){}

        //  pull knight attack db from file
        this.knightDB = new long[64];
        try{
            Scanner in = new Scanner(new FileInputStream("numbers/knight_db.txt"));
            for(int i=0;i<64;i++){
                this.knightDB[i] = Long.parseUnsignedLong(in.nextLine().trim());
            }

        }catch (Exception e){}


        //  populate map that maps single bit longs to their square ID
        this.bitToSquare = new HashMap<>();
        long bit = Long.MIN_VALUE;
        for(int i=0;i<64;i++){
            this.bitToSquare.put(bit,i);
            bit >>>=1;
        }
    }

    public ArrayList<String> genMoves(Board board){

        //  create list to store legal moves
        ArrayList<String> moves = new ArrayList<>();

        //  generate rook moves
        moves.addAll(this.genRookMoves(board));

        //  generate bishop moves
        moves.addAll(this.genBishopMoves(board));

        //  generate queen moves
        moves.addAll(this.genQueenMoves(board));

        //  generate knight moves
        moves.addAll(this.genKnightMoves(board));

        //  generate king moves
        moves.addAll(this.genKingMoves(board));

        //  generate pawn moves

        moves.addAll(this.genPawnMoves(board));


        return moves;
    }

    private ArrayList<String> genRookMoves(Board board){

        //  create list to store legal moves
        ArrayList<String> moves = new ArrayList<>();

        //  if its whites turn
        if(board.turn==1){

            //  for all white rooks
            long rooks = board.bitBoards[Names.whiteRooks];
            long bit = rooks&(-rooks);
            while(bit!=0){

                //  find att set of this rook
                int square = 63-bitToSquare.get(bit);
                long attSet = (this.rookMask[square]&board.bitBoards[Names.allPieces]);
                attSet = (attSet*this.rookMagic[square])>>>this.rookShift[square];
                attSet = this.rookDB[square][(int)attSet]&~board.bitBoards[Names.whitePieces];

                //  for every att in att set add it to move list
                int startSq = this.bitToSquare.get(bit);
                long moveBit = attSet&-attSet;
                while(moveBit!=0){

                    int endSq = this.bitToSquare.get(moveBit);
                    String move = startSq+"-"+endSq;
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }

                    attSet &= ~moveBit;
                    moveBit = attSet&-attSet;
                }
                rooks &= ~bit;
                bit = rooks&(-rooks);;
            }
        }

        //  if it is blacks turn to move
        else {
            //  for all black rooks
            long rooks = board.bitBoards[Names.blackRooks];
            long bit = rooks&(-rooks);
            while(bit!=0){

                //  find att set of this rook
                int square = 63-bitToSquare.get(bit);
                long attSet = (this.rookMask[square]&board.bitBoards[Names.allPieces]);
                attSet = (attSet*this.rookMagic[square])>>>this.rookShift[square];
                attSet = this.rookDB[square][(int)attSet]&~board.bitBoards[Names.blackPieces];

                //  for every att in att set add it to move list
                int startSq = this.bitToSquare.get(bit);
                long moveBit = attSet&-attSet;
                while(moveBit!=0){

                    int endSq = this.bitToSquare.get(moveBit);
                    String move = startSq+"-"+endSq;
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }

                    attSet &= ~moveBit;
                    moveBit = attSet&-attSet;
                }
                rooks &= ~bit;
                bit = rooks&(-rooks);;
            }
        }

        return moves;
    }

    private ArrayList<String> genBishopMoves(Board board){

        //  create list to store legal moves
        ArrayList<String> moves = new ArrayList<>();

        //  if its whites turn
        if(board.turn==1){

            //  for all white bishops
            long bishops = board.bitBoards[Names.whiteBishops];
            long bit = bishops&(-bishops);
            while(bit!=0){

                //  find att set of this bishop
                int square = 63-bitToSquare.get(bit);
                long attSet = (this.bishopMask[square]&board.bitBoards[Names.allPieces]);
                attSet = (attSet*this.bishopMagic[square])>>>this.bishopShift[square];
                attSet = this.bishopDB[square][(int)attSet]&~board.bitBoards[Names.whitePieces];

                //  for every att in att set add it to move list
                int startSq = this.bitToSquare.get(bit);
                long moveBit = attSet&-attSet;
                while(moveBit!=0){

                    int endSq = this.bitToSquare.get(moveBit);
                    String move = startSq+"-"+endSq;
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }

                    attSet &= ~moveBit;
                    moveBit = attSet&-attSet;
                }
                bishops &= ~bit;
                bit = bishops&(-bishops);;
            }
        }

        //  if it is blacks turn to move
        else {
            //  for all black bishops
            long bishops = board.bitBoards[Names.blackBishops];
            long bit = bishops&(-bishops);
            while(bit!=0){

                //  find att set of this bishop
                int square = 63-bitToSquare.get(bit);
                long attSet = (this.bishopMask[square]&board.bitBoards[Names.allPieces]);
                attSet = (attSet*this.bishopMagic[square])>>>this.bishopShift[square];
                attSet = this.bishopDB[square][(int)attSet]&~board.bitBoards[Names.blackPieces];

                //  for every att in att set add it to move list
                int startSq = this.bitToSquare.get(bit);
                long moveBit = attSet&-attSet;
                while(moveBit!=0){

                    int endSq = this.bitToSquare.get(moveBit);
                    String move = startSq+"-"+endSq;
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }

                    attSet &= ~moveBit;
                    moveBit = attSet&-attSet;
                }
                bishops &= ~bit;
                bit = bishops&(-bishops);;
            }
        }

        return moves;
    }

    private ArrayList<String> genQueenMoves(Board board){

        //  create list to store legal moves
        ArrayList<String> moves = new ArrayList<>();

        //  if its whites turn
        if(board.turn==1){

            //  for all white queens
            long queens = board.bitBoards[Names.whiteQueens];
            long bit = queens&(-queens);
            while(bit!=0){

                //  find cross att set of this queen
                int square = 63-bitToSquare.get(bit);
                long cAttSet = (this.rookMask[square]&board.bitBoards[Names.allPieces]);
                cAttSet = (cAttSet*this.rookMagic[square])>>>this.rookShift[square];
                cAttSet = this.rookDB[square][(int)cAttSet]&~board.bitBoards[Names.whitePieces];


                //  find diagonal att set of this queen
                long dAttSet = (this.bishopMask[square]&board.bitBoards[Names.allPieces]);
                dAttSet = (dAttSet*this.bishopMagic[square])>>>this.bishopShift[square];
                dAttSet = this.bishopDB[square][(int)dAttSet]&~board.bitBoards[Names.whitePieces];

                //  combine attack sets
                long attSet = cAttSet|dAttSet;



                //  for every att in att set add it to move list
                int startSq = this.bitToSquare.get(bit);
                long moveBit = attSet&(-attSet);
                while(moveBit!=0){

                    int endSq = this.bitToSquare.get(moveBit);
                    String move = startSq+"-"+endSq;
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }

                    attSet &= ~moveBit;
                    moveBit = attSet&(-attSet);
                }
                queens &= ~bit;
                bit = queens&(-queens);;
            }
        }

        //  if it is blacks turn to move
        else {
            //  for all black queens
            long queens = board.bitBoards[Names.blackQueens];
            long bit = queens&(-queens);
            while(bit!=0){

                //  find cross att set of this queen
                int square = 63-bitToSquare.get(bit);
                long cAttSet = (this.rookMask[square]&board.bitBoards[Names.allPieces]);
                cAttSet = (cAttSet*this.rookMagic[square])>>>this.rookShift[square];
                cAttSet = this.rookDB[square][(int)cAttSet]&~board.bitBoards[Names.blackPieces];

                //  find diagonal att set of this queen
                long dAttSet = (this.bishopMask[square]&board.bitBoards[Names.allPieces]);
                dAttSet = (dAttSet*this.bishopMagic[square])>>>this.bishopShift[square];
                dAttSet = this.bishopDB[square][(int)dAttSet]&~board.bitBoards[Names.blackPieces];

                //  combine attack sets
                long attSet = cAttSet|dAttSet;

                //  for every att in att set add it to move list
                int startSq = this.bitToSquare.get(bit);
                long moveBit = attSet&(-attSet);
                while(moveBit!=0){

                    int endSq = this.bitToSquare.get(moveBit);
                    String move = startSq+"-"+endSq;
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }

                    attSet &= ~moveBit;
                    moveBit = attSet&(-attSet);
                }
                queens &= ~bit;
                bit = queens&(-queens);;
            }
        }

        return moves;
    }

    private ArrayList<String> genKnightMoves(Board board){

        //  create list to store legal moves
        ArrayList<String> moves = new ArrayList<>();

        //  if its whites turn
        if(board.turn==1){

            //  for all white knights
            long knights = board.bitBoards[Names.whiteKnights];
            long bit = knights&(-knights);
            while(bit!=0) {

                //  find att set of this knight
                int square = 63-bitToSquare.get(bit);
                long attSet = this.knightDB[square]&~board.bitBoards[Names.whitePieces];

                //  for every att in att set add it to move list
                int startSq = this.bitToSquare.get(bit);
                long moveBit = attSet&-attSet;
                while(moveBit!=0){

                    int endSq = this.bitToSquare.get(moveBit);
                    String move = startSq+"-"+endSq;
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }

                    attSet &= ~moveBit;
                    moveBit = attSet&-attSet;
                }
                knights &= ~bit;
                bit = knights&(-knights);
            }
        }
        //  if its blacks turn
        else {

            //  for all black knights
            long knights = board.bitBoards[Names.blackKnights];
            long bit = knights&(-knights);
            while(bit!=0) {

                //  find att set of this knight
                int square = 63-bitToSquare.get(bit);
                long attSet = this.knightDB[square]&~board.bitBoards[Names.blackPieces];;

                //  for every att in att set add it to move list
                int startSq = this.bitToSquare.get(bit);
                long moveBit = attSet&-attSet;
                while(moveBit!=0){

                    int endSq = this.bitToSquare.get(moveBit);
                    String move = startSq+"-"+endSq;
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }

                    attSet &= ~moveBit;
                    moveBit = attSet&-attSet;
                }
                knights &= ~bit;
                bit = knights&(-knights);;
            }
        }

        return moves;
    }

    private ArrayList<String> genKingMoves(Board board){

        //  create list to store legal moves
        ArrayList<String> moves = new ArrayList<>();

        //  if its whites turn
        if(board.turn==1){

            //  for all white kings
            long kings = board.bitBoards[Names.whiteKings];
            long bit = kings&(-kings);
            while(bit!=0) {

                //  find att set of this king
                int square = 63-bitToSquare.get(bit);
                long attSet = this.kingDB[square]&~board.bitBoards[Names.whitePieces];

                //  check for castle
                if(board.canKingCastleWhite){
                    if((board.bitBoards[Names.allPieces]&0x0000000000000006l)==0l){
                        if(isSquareSafe(board,(63-square)+1)&&isSquareSafe(board,(63-square)+2)){
                            attSet |= 0x0000000000000002;
                        }
                    }
                }
                if(board.canQueenCastleWhite){
                    if((board.bitBoards[Names.allPieces]&0x0000000000000070l)==0l){
                        if(isSquareSafe(board,(63-square)-1)&&isSquareSafe(board,(63-square)-2)){
                            attSet |= 0x0000000000000020l;
                        }
                    }
                }

                //  for every att in att set add it to move list
                int startSq = this.bitToSquare.get(bit);
                long moveBit = attSet&-attSet;
                while(moveBit!=0){

                    int endSq = this.bitToSquare.get(moveBit);
                    String move = startSq+"-"+endSq;
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }

                    attSet &= ~moveBit;
                    moveBit = attSet&-attSet;
                }
                kings &= ~bit;
                bit = kings&(-kings);

            }
        }
        //  if its blacks turn
        else {

            //  for all black kings
            long kings = board.bitBoards[Names.blackKings];
            long bit = kings&(-kings);
            while(bit!=0) {

                //  find att set of this king
                int square = 63-bitToSquare.get(bit);
                long attSet = this.kingDB[square]&~board.bitBoards[Names.blackPieces];;

                //  check for castle
                if(board.canKingCastleBlack){
                    if((board.bitBoards[Names.allPieces]&0x0600000000000000l)==0){
                        if(isSquareSafe(board,(63-square)+1)&&isSquareSafe(board,(63-square)+2)){
                            attSet |= 0x0200000000000000l;
                        }
                    }
                }
                if(board.canQueenCastleBlack){
                    if((board.bitBoards[Names.allPieces]&0x7000000000000000l)==0){
                        if(isSquareSafe(board,(63-square)-1)&&isSquareSafe(board,(63-square)-2)){
                            attSet |= 0x2000000000000000l;
                        }
                    }
                }

                //  for every att in att set add it to move list
                int startSq = this.bitToSquare.get(bit);
                long moveBit = attSet&-attSet;
                while(moveBit!=0){

                    int endSq = this.bitToSquare.get(moveBit);
                    String move = startSq+"-"+endSq;
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }

                    attSet &= ~moveBit;
                    moveBit = attSet&-attSet;
                }
                kings &= ~bit;
                bit = kings&(-kings);;
            }
        }

        return moves;
    }

    public ArrayList<String> genPawnMoves(Board board){

        //  create list to store legal moves
        ArrayList<String> moves = new ArrayList<>();


        //  if its whites turn
        if(board.turn==1){

            //  calc forward 1 move set
            long moveSet = (board.bitBoards[Names.whitePawns]<<8);
            moveSet &= ~board.bitBoards[Names.allPieces];

            //  for all foward moves
            long bit = moveSet&(-moveSet);
            while(bit!=0){

                int endSq = this.bitToSquare.get(bit);
                int startSq = endSq+8;
                String move = startSq+"-"+endSq;

                //  if promotion
                if(endSq<8){
                    String moveq = move+"-Q";
                    Board boardq = board.makeMove(moveq);
                    if(this.isKingSafe(boardq)){
                        moves.add(moveq);
                        String moven = move+"-N";
                        moves.add(moven);
                        String moveb = move+"-B";
                        moves.add(moveb);
                        String mover = move+"-R";
                        moves.add(mover);
                    }
                }
                else {
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }
                }

                moveSet &= ~bit;
                bit = moveSet&(-moveSet);
            }

            //  calc forward 2 move set
            moveSet = (board.bitBoards[Names.whitePawns]<<16);
            moveSet &= ~(board.bitBoards[Names.allPieces])&0x00000000FF000000l;

            //  for all foward 2 moves
            bit = moveSet&(-moveSet);
            while(bit!=0){

                int endSq = this.bitToSquare.get(bit);
                int startSq = endSq+16;
                String move = startSq+"-"+endSq;

                Board board1 = board.makeMove(move);
                if(this.isKingSafe(board1)){
                    moves.add(move);
                }

                moveSet &= ~bit;
                bit = moveSet&(-moveSet);
            }

            //  calc diagonal left attack
            moveSet = ((board.bitBoards[Names.whitePawns]&0x7F7F7F7F7F7F7F7Fl)<<9);
            moveSet &= (board.bitBoards[Names.blackPieces]|board.enpasant);

            //  for all diagonal left attacks
            bit = moveSet&(-moveSet);
            while(bit!=0){

                int endSq = this.bitToSquare.get(bit);
                int startSq = endSq+9;
                String move = startSq+"-"+endSq;

                //  if promotion
                if(endSq<8){
                    String moveq = move+"-Q";
                    Board boardq = board.makeMove(moveq);
                    if(this.isKingSafe(boardq)){
                        moves.add(moveq);
                        String moven = move+"-N";
                        moves.add(moven);
                        String moveb = move+"-B";
                        moves.add(moveb);
                        String mover = move+"-R";
                        moves.add(mover);
                    }
                }
                else {

                    Board board1 = board.makeMove(move);

                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }
                }

                moveSet &= ~bit;
                bit = moveSet&(-moveSet);
            }

            //  calc diagonal right attack
            moveSet = ((board.bitBoards[Names.whitePawns]&0xFEFEFEFEFEFEFEFEl)<<7);
            moveSet &= (board.bitBoards[Names.blackPieces]|board.enpasant);

            //  for all diagonal right attacks
            bit = moveSet&(-moveSet);
            while(bit!=0){

                int endSq = this.bitToSquare.get(bit);
                int startSq = endSq+7;
                String move = startSq+"-"+endSq;

                //  if promotion
                if(endSq<8){
                    String moveq = move+"-Q";
                    Board boardq = board.makeMove(moveq);
                    if(this.isKingSafe(boardq)){
                        moves.add(moveq);
                        String moven = move+"-N";
                        moves.add(moven);
                        String moveb = move+"-B";
                        moves.add(moveb);
                        String mover = move+"-R";
                        moves.add(mover);
                    }
                }
                else {
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }
                }

                moveSet &= ~bit;
                bit = moveSet&(-moveSet);
            }
        }

        //  if its blacks turn
        else {

            //  calc forward 1 move set
            long moveSet = (board.bitBoards[Names.blackPawns]>>>8);
            moveSet &= ~board.bitBoards[Names.allPieces];

            //  for all foward 1 moves
            long bit = moveSet&(-moveSet);
            while(bit!=0){

                int endSq = this.bitToSquare.get(bit);
                int startSq = endSq-8;
                String move = startSq+"-"+endSq;

                //  if promotion
                if(endSq>55){
                    String moveq = move+"-Q";
                    Board boardq = board.makeMove(moveq);
                    if(this.isKingSafe(boardq)){
                        moves.add(moveq);
                        String moven = move+"-N";
                        moves.add(moven);
                        String moveb = move+"-B";
                        moves.add(moveb);
                        String mover = move+"-R";
                        moves.add(mover);
                    }
                }
                else {
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }
                }

                moveSet &= ~bit;
                bit = moveSet&(-moveSet);
            }

            //  calc forward 2 move set
            moveSet = (board.bitBoards[Names.blackPawns]>>>16);
            moveSet &= ~board.bitBoards[Names.allPieces]&0x000000FF00000000l;

            //  for all foward 2 moves
            bit = moveSet&(-moveSet);
            while(bit!=0){

                int endSq = this.bitToSquare.get(bit);
                int startSq = endSq-16;
                String move = startSq+"-"+endSq;

                Board board1 = board.makeMove(move);
                if(this.isKingSafe(board1)){
                    moves.add(move);
                }

                moveSet &= ~bit;
                bit = moveSet&(-moveSet);
            }

            //  calc diagonal left attack
            moveSet = ((board.bitBoards[Names.blackPawns]&0xFEFEFEFEFEFEFEFEl)>>>7);
            moveSet &= (board.bitBoards[Names.whitePieces]|board.enpasant);

            //  for all diagonal left attacks
            bit = moveSet&(-moveSet);
            while(bit!=0){

                int endSq = this.bitToSquare.get(bit);
                int startSq = endSq-7;
                String move = startSq+"-"+endSq;

                //  if promotion
                if(endSq>55){
                    String moveq = move+"-Q";
                    Board boardq = board.makeMove(moveq);
                    if(this.isKingSafe(boardq)){
                        moves.add(moveq);
                        String moven = move+"-N";
                        moves.add(moven);
                        String moveb = move+"-B";
                        moves.add(moveb);
                        String mover = move+"-R";
                        moves.add(mover);
                    }
                }
                else {
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }
                }

                moveSet &= ~bit;
                bit = moveSet&(-moveSet);
            }

            //  calc diagonal right attack
            moveSet = ((board.bitBoards[Names.blackPawns]&0x7F7F7F7F7F7F7F7Fl)>>>9);
            moveSet &= (board.bitBoards[Names.whitePieces]|board.enpasant);

            //  for all diagonal right attacks
            bit = moveSet&(-moveSet);
            while(bit!=0){

                int endSq = this.bitToSquare.get(bit);
                int startSq = endSq-9;
                String move = startSq+"-"+endSq;

                //  if promotion
                if(endSq>55){
                    String moveq = move+"-Q";
                    Board boardq = board.makeMove(moveq);
                    if(this.isKingSafe(boardq)){
                        moves.add(moveq);
                        String moven = move+"-N";
                        moves.add(moven);
                        String moveb = move+"-B";
                        moves.add(moveb);
                        String mover = move+"-R";
                        moves.add(mover);
                    }
                }
                else {
                    Board board1 = board.makeMove(move);
                    if(this.isKingSafe(board1)){
                        moves.add(move);
                    }
                }

                moveSet &= ~bit;
                bit = moveSet&(-moveSet);
            }
        }

        return moves;
    }

    public boolean isKingSafe(Board board){

        //  if its whites move (checking black king
        if(board.turn==1){

            //  find black king
            int square = 63-bitToSquare.get(board.bitBoards[Names.blackKings]);

            //  check horizontal / varietals
            long attSet = (this.rookMask[square]&board.bitBoards[Names.allPieces]);
            attSet = (attSet*this.rookMagic[square])>>>this.rookShift[square];
            attSet = this.rookDB[square][(int)attSet];
            if((attSet&(board.bitBoards[Names.whiteRooks]|board.bitBoards[Names.whiteQueens]))!=0){
                return false;
            }

            //  check diagonals
            attSet = (this.bishopMask[square]&board.bitBoards[Names.allPieces]);
            attSet = (attSet*this.bishopMagic[square])>>>this.bishopShift[square];
            attSet = this.bishopDB[square][(int)attSet];
            if((attSet&(board.bitBoards[Names.whiteBishops]|board.bitBoards[Names.whiteQueens]))!=0){
                return false;
            }

            //  check for knights
            if((this.knightDB[square]&board.bitBoards[Names.whiteKnights])!=0){
                return false;
            }

            //  check for kings
            if((this.kingDB[square]&board.bitBoards[Names.whiteKings])!=0){
                return false;
            }

            //  check for pawns
            long pawnMask = (board.bitBoards[Names.blackKings]>>>7)|(board.bitBoards[Names.blackKings]>>>9);
            if((pawnMask&board.bitBoards[Names.whitePawns])!=0){
                return false;
            }

            //  king is safe
            return true;
        }

        //  if its blacks move (checking whites king)
        else {

            //  find black king
            int square = 63 - bitToSquare.get(board.bitBoards[Names.whiteKings]);

            //  check horizontal / varietals
            long attSet = (this.rookMask[square]&board.bitBoards[Names.allPieces]);
            attSet = (attSet*this.rookMagic[square])>>>this.rookShift[square];
            attSet = this.rookDB[square][(int)attSet];
            if((attSet&(board.bitBoards[Names.blackRooks]|board.bitBoards[Names.blackQueens]))!=0){
                return false;
            }

            //  check diagonals
            attSet = (this.bishopMask[square]&board.bitBoards[Names.allPieces]);
            attSet = (attSet*this.bishopMagic[square])>>>this.bishopShift[square];
            attSet = this.bishopDB[square][(int)attSet];
            if((attSet&(board.bitBoards[Names.blackBishops]|board.bitBoards[Names.blackQueens]))!=0){
                return false;
            }

            //  check for knights
            if((this.knightDB[square]&board.bitBoards[Names.blackKnights])!=0){
                return false;
            }


            //  check for kings
            if((this.kingDB[square]&board.bitBoards[Names.blackKings])!=0){
                return false;
            }

            //  check for pawns
            long pawnMask = (board.bitBoards[Names.whiteKings]<<7)|(board.bitBoards[Names.whiteKings]<<9);
            if((pawnMask&board.bitBoards[Names.blackPawns])!=0){
                return false;
            }

            //  king is safe
            return true;
        }
    }

    public boolean isSquareSafe(Board board, int square){

        //  if its whites move
        if(board.turn==1){

            //  check horizontal / varietals
            long attSet = (this.rookMask[square]&board.bitBoards[Names.allPieces]);
            attSet = (attSet*this.rookMagic[square])>>>this.rookShift[square];
            attSet = this.rookDB[square][(int)attSet];
            if((attSet&(board.bitBoards[Names.blackRooks]|board.bitBoards[Names.blackQueens]))!=0){
                return false;
            }

            //  check diagonals
            attSet = (this.bishopMask[square]&board.bitBoards[Names.allPieces]);
            attSet = (attSet*this.bishopMagic[square])>>>this.bishopShift[square];
            attSet = this.bishopDB[square][(int)attSet];
            if((attSet&(board.bitBoards[Names.blackBishops]|board.bitBoards[Names.blackQueens]))!=0){
                return false;
            }

            //  check for knights
            if((this.knightDB[square]&board.bitBoards[Names.blackKnights])!=0){
                return false;
            }


            //  check for kings
            if((this.kingDB[square]&board.bitBoards[Names.blackKings])!=0){
                return false;
            }

            //  check for pawns
            long pawnMask = (board.bitBoards[Names.whiteKings]<<7)|(board.bitBoards[Names.whiteKings]<<9);
            if((pawnMask&board.bitBoards[Names.blackPawns])!=0){
                return false;
            }

            //  king is safe
            return true;
        }

        //  if its blacks move
        else {

            //  check horizontal / varietals
            long attSet = (this.rookMask[square]&board.bitBoards[Names.allPieces]);
            attSet = (attSet*this.rookMagic[square])>>>this.rookShift[square];
            attSet = this.rookDB[square][(int)attSet];
            if((attSet&(board.bitBoards[Names.whiteRooks]|board.bitBoards[Names.whiteQueens]))!=0){
                return false;
            }

            //  check diagonals
            attSet = (this.bishopMask[square]&board.bitBoards[Names.allPieces]);
            attSet = (attSet*this.bishopMagic[square])>>>this.bishopShift[square];
            attSet = this.bishopDB[square][(int)attSet];
            if((attSet&(board.bitBoards[Names.whiteBishops]|board.bitBoards[Names.whiteQueens]))!=0){
                return false;
            }

            //  check for knights
            if((this.knightDB[square]&board.bitBoards[Names.whiteKnights])!=0){
                return false;
            }

            //  check for kings
            if((this.kingDB[square]&board.bitBoards[Names.whiteKings])!=0){
                return false;
            }

            //  check for pawns
            long pawnMask = (board.bitBoards[Names.blackKings]>>>7)|(board.bitBoards[Names.blackKings]>>>9);
            if((pawnMask&board.bitBoards[Names.whitePawns])!=0){
                return false;
            }

            //  king is safe
            return true;
        }
    }

    public static void printBitBoard(long board){

        long bit = Long.MIN_VALUE;
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                if((bit&board)!=0){
                    System.out.print("1");
                }
                else {
                    System.out.print("0");
                }
                bit>>>=1;
            }
            System.out.println();
        }
    }



}

/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.ArrayList;
import java.util.Random;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

/** A Player that computes its own moves.
 *  @author Felipe Zuluaga
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 1;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     *  a random-number generator for use in move computations.  Identical
     *  seeds produce identical behaviour. */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to the findMove method
     *  above. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove, int sense,
                       int alpha, int beta) {
        /* We use WINNING_VALUE + depth as the winning value so as to favor
         * wins that happen sooner rather than later (depth is larger the
         * fewer moves have been made. */
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }

        Move best;
        best = null;
        int bestScore;
        bestScore = 0;
        _validMoves = new ArrayList<>();

        if (sense == 1) {
            bestScore = -INFTY;
        } else {
            bestScore = INFTY;
        }

        allPossibleMoves(board);
        for (int i = 0; i < _validMoves.size(); i++) {
            Move move = _validMoves.get(i);
            board.makeMove(move);
            int response = minMax(board, depth - 1,
                    false, -sense, alpha, beta);
            if (sense == 1) {
                if (response > bestScore) {
                    bestScore = response;
                    alpha = max(alpha, bestScore);
                    if (saveMove) {
                        _lastFoundMove = move;
                    }
                }
            } else {
                if (response < bestScore) {
                    bestScore = response;
                    beta = min(beta, bestScore);
                    if (saveMove) {
                        _lastFoundMove = move;
                    }
                }
            }
            board.undo();
            if (alpha >= beta) {
                return bestScore;
            }
        }
        return bestScore;
    }

    public void allPossibleMoves(Board board) {
        for (char c0 = 'a'; c0 < 'a' + board.SIDE; c0++) {
            for (char r0 = '1'; r0 < '1' + board.SIDE; r0++) {
                if (board.get(c0, r0) == board.whoseMove()) {
                    for (int i = -2; i < 3; i++) {
                        for (int j = -2; j < 3; j++) {
                            char c1 = (char) (c0 + i);
                            char r1 = (char) (r0 + j);
                            Move move = Move.move(c0, r0, c1, r1);
                            if (board.legalMove(move)) {
                                _validMoves.add(move);
                            }
                        }
                    }
                }
            }
        }
        if (_validMoves.size() == 0) {
            _validMoves.add(Move.pass());
        }
    }

    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
                case RED -> winningValue;
                case BLUE -> -winningValue;
                default -> 0;
            };
        }
        return board.redPieces() - board.bluePieces();
    }

    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();

    /** ArrayList of all validMoves that the AI can make. */
    private ArrayList<Move> _validMoves;
}

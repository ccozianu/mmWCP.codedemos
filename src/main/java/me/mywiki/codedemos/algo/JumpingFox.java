package me.mywiki.codedemos.algo;


import java.io.*;
import java.util.*;

/**
Problem Statement

Find number of ways a fox, starting at 0,0 can reach the destination (Tx, Ty) 
in exactly R jumps

Problem Constraints

In one jump, she can move 0, 1, 2, ..., Mx units in the positive x direction 
    and 0, 1, 2, ..., My units in the positive y direction

All jumps must have a non-zero distance

In addition, there are some jumps which she is not good at. 
These jumps are described in the list bad. 
If bad contains the number b, it means she cannot make a diagonal jump where she moves exactly b units in the positive x direction and b units in the positive y direction

Each element of bad will be a multiple of 10

Find the number of ways she can start at (0, 0), jump exactly R times, 
and land at (Tx, Ty), modulo 10,007

Two ways are considered to be different if there is an index i, 0 <= i < R, 
such that fox lands at different points after the i-th (0-based) jump in these ways

Input Format

Line 1: Tx
Line 2: Ty
Line 3: Mx
Line 4: My
Line 5: R
Line 6: Comma separated list of bad jumps, if no jump is present the value would be -

Sample Input

2
2
1
1
2

-

Sample Output

1

Sample Input

123
456
70
80
90
30,40,20,10,50

Sample Output
6723
 */

public class JumpingFox {
    
    public static class ProblemDefinition {
        public final int tgtX;
        public final int tgtY;
        public final int maxX;
        public final int maxY;
        public final int rounds;
        // this will be sorted from 0 to maxBadJump
        public final ArrayList<Integer> badJumps;
        private final BitSet badJumpFlags;
        private ProblemDefinition( int tgtX_, int tgtY_,
                                  int maxX_, int maxY_,
                                  int rounds_,
                                  Set<Integer> badJumps_ ) 
        {
            this.tgtX= tgtX_; this.tgtY= tgtY_;
            this.maxX= maxX_; this.maxY= maxY_;
            this.rounds= rounds_;
            
            this.badJumps= new ArrayList<>(badJumps_);
            this.badJumpFlags= new BitSet(Math.max(maxX_, maxY_));
            Collections.sort(this.badJumps);
            this.badMoveIndexes= initializeMemoArray(-1, (Integer)badJumps.get(badJumps.size()-1) + 1);
            for (int idx=0; idx<badJumps.size(); idx++) {
                int  d= badJumps.get(idx);
                this.badJumpFlags.set(d);
                badMoveIndexes.set(idx,d);
            }
        }
        
        public static ProblemDefinition readFrom(Reader in_) {
            BufferedReader in = new BufferedReader(in_); 
            int tgtX_= Integer.valueOf(readLineNotNull(in));
            int tgtY_= Integer.valueOf(readLineNotNull(in));
            int maxX_= Integer.valueOf(readLineNotNull(in));
            int maxY_= Integer.valueOf(readLineNotNull(in));
            int rounds_= Integer.valueOf(readLineNotNull(in));
            HashSet<Integer> badJumps_= new HashSet<>();
            String strBads= readLineNotNull(in);
            if (!"-".equals(strBads)) {
                String [] strBadsArr= strBads.split(",");
                for (String s: strBadsArr) { badJumps_.add(Integer.valueOf(s)); }
            }
            badJumps_.add(0);
            
            return new ProblemDefinition(tgtX_, tgtY_, maxX_, maxY_, rounds_, badJumps_);
        }
        
        private static String readLineNotNull(BufferedReader in) 
        {
            try {
                String result= in.readLine();
                if (result == null) throw new IllegalStateException("Expecting data but got end of stream.");
                return result;
            }
            catch (IOException ex) {
                throw new IllegalStateException("Unexpected IOException.", ex);
            }
        }
        
        public static ProblemDefinition readFromStream(InputStream in) {
            return readFrom(new InputStreamReader(in));
        }

        public boolean isBadJump(int delta) {
            return badJumpFlags.get(delta);
        }

        final MultiDimIntArray badMoveIndexes;
        public int badMoveIdxOf(int badMoveVal) {
            return badMoveIndexes.get(badMoveVal);
        }
        
        
    }
    
    
    private static final String testCases []= {
        "2\n"
        + "2\n"
        + "1\n"
        + "1\n"
        + "2\n"
        + "-"
        ,
        
        "2\n"
        + "2\n"
        + "1\n"
        + "1\n"
        + "3\n"
        + "-\n",
        
        "123\n"+
                
        "456\n"+
        "70\n"+
        "80\n"+
        "90\n"+
        "30,40,20,10,50\n",
        // testcase 7
        "776\n"
        + "612\n"
        + "800\n"
        + "800\n"
        + "333\n"
        + "30,60,80,110,200\n"};
    
    public static void main(String [] args) throws Exception{ 
        try {
            //ProblemDefinition problem= ProblemDefinition.readFromStream(System.in);
            /**/ProblemDefinition problem= ProblemDefinition.readFrom(
                        new StringReader(testCases[2])
                    );/**/
            
            int solutionNumber= new SolvingAlgorithm4(problem).solveIt();
            System.out.println(solutionNumber);
        }
        catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace(System.err);
        }
        
    }
    
    private static final int COUNT_MODULO = 10007;

    private static class SolvingAlgorithm1 {
         // array memoization is way more efficient than Java's hashmap
        // and our array is almost full
        //private final Map<Triple,Integer> solved= new HashMap<>();
        private final ProblemDefinition pd;
        private int solved=0;
        
        public SolvingAlgorithm1(ProblemDefinition problem_) {
            this.pd= problem_;
            this.solutions= initializeMemoArray(-1, pd.rounds,pd.tgtX+1, pd.tgtY+1); 
                    
        }
        
        public int solveIt() {
            return solveWithState(pd.tgtX,pd.tgtY, pd.rounds);
        }
        
        private int solveWithState( int tgtX, int tgtY, 
                                    int rRemaining) 
        {
            if (rRemaining == 0) {
                return (tgtX == 0 && tgtY==0 )  
                    ? 1
                    : 0;
            }
            else {
                int prevSolved= this.recordedSolution(rRemaining, tgtX, tgtY);
                if (prevSolved >= 0) {
                    return prevSolved;
                }
                int sum=0;
                
                int maxDx= Math.min(pd.maxX, tgtX);
                int minDx = Math.max(0,tgtX -(pd.maxX*(rRemaining - 1)));
                int maxDy=  Math.min(pd.maxY, tgtY);
                int minDy= Math.max(0,tgtY -(pd.maxY*(rRemaining - 1)));
                
                for (int dx= minDx; dx <= maxDx;  dx++ ) 
                    for (int dy=minDy; dy<= maxDy; dy++ ) {
                    if (dx==dy && pd.isBadJump(dx)){
                        continue;
                    }
                    sum += solveWithState(tgtX - dx, tgtY - dy, rRemaining - 1);
                    sum %= COUNT_MODULO;
                }
                this.recordSolution(rRemaining,tgtX,tgtY, sum);
                
                return sum;
            }
        }
        
        final MultiDimIntArray solutions;
        
        private void recordSolution(int nRounds, int tgtX, int tgtY, int sum) {
            if (sum < 0) throw new IllegalArgumentException("invalid sum: "+sum);
            solved++;
            if (solved % 100000 == 0) {
                System.err.println("Solved: " + solved+" nodes");
            }
            solutions.set(sum, nRounds-1, tgtX , tgtY);
        }
        
        private int recordedSolution(int nRounds, int tgtX, int tgtY) {
            return solutions.get(nRounds-1 , tgtX , tgtY);
        }
        
    }
    

    /**
     * We return object because dyanmic dim arrays are not typable
     */
    private static MultiDimIntArray 
            initializeMemoArray( int initVal , int ... sizes)
    {
        long startTime= System.currentTimeMillis();
        MultiDimIntArray result= new MultiDimIntArray( sizes);
        result.fill(initVal);
        long endTime= System.currentTimeMillis();
        debug("Mechanism initialized in: %s  ms\n",(endTime- startTime));
        return result;
    }
    
    /**
     * Counts sum=x_1+x_2 + ... x_Rounds
     * on a single axis discounting that some moves
     * are illegal in the bigger game
     */
    private static class SingleAxisSolver {
        final MultiDimIntArray memos;
        private final int maxSum;
        private final int maxStep;
        private final int maxRounds;
        private final int countModulo;
        
        SingleAxisSolver(int maxSum_, int maxStep_, int maxRounds_, int countModulo_) {
            this.maxSum= maxSum_;
            this.maxStep= maxStep_;
            this.maxRounds= maxRounds_;
            this.countModulo= countModulo_;
            memos= initializeMemoArray(-1, maxSum_+1, maxRounds_);
            
        }
        
        public int countTo( int tgtX, int roundsLeft) {
            assert( tgtX<= maxSum  && tgtX >= 0 
                    && roundsLeft >=0 && roundsLeft <= maxRounds);
            if (tgtX==0 && roundsLeft==0) {
                return 1;
            }
            int result= this.memos.get(tgtX,roundsLeft);
            if ( result == -1 ) {
                if (roundsLeft == 1) {
                    result= (tgtX <= maxStep) 
                                ? 1 :0;
                }
                else {
                    result = 0;
                    int stepMax= Math.min(this.maxStep, tgtX);
                    int stepMin= Math.max(0, tgtX - (roundsLeft-1) * maxStep);
                    if (roundsLeft>1) 
                        for (int step=stepMin; step<= stepMax; step++) {
                            result += countTo(tgtX - step, roundsLeft - 1);
                            result %= COUNT_MODULO;                    
                    }
 
                }
                this.memos.set(tgtX,roundsLeft, result);
            }
            return result;
        }
         
    }


    private static class SolvingAlgorithm2 {
        // array memoization is way more efficient than Java's hashmap
        // and our array is almost full
        private final ProblemDefinition pd;
        final MultiDimIntArray legalSolutions;
        final SingleAxisSolver illegalSolverX;
        final SingleAxisSolver illegalSolverY;
    
        
        public SolvingAlgorithm2(ProblemDefinition problem_) {
            this.pd= problem_;
            long startTime= System.currentTimeMillis();
            this.legalSolutions= initializeMemoArray(-1,pd.rounds,pd.tgtX+1, pd.tgtY+1);
            long endTime= System.currentTimeMillis();
            this.illegalSolverX= new SingleAxisSolver( pd.tgtX, pd.maxX, 
                                                       pd.rounds, COUNT_MODULO );
            this.illegalSolverY= new SingleAxisSolver( pd.tgtY, pd.maxY, 
                    pd.rounds, COUNT_MODULO );
            System.err.println("Mechanism initialized in: " +(endTime- startTime)+ "ms");
        }
        
 

        public int solveIt() {
           return countLegalFor(pd.tgtX,pd.tgtY, pd.rounds);
        }



        private int countLegalFor(int tgtX, int tgtY, int rounds) {
            int result= legalSolutions.get(rounds-1,tgtX,tgtY);
            if (result != -1) {
                return result;
            }
            if (rounds== 1) {
               result= (tgtX<= pd.maxX) && (tgtY <= pd.maxY) 
                       && !((tgtX == tgtY) && pd.isBadJump(tgtX) ) 
                           ?1 :0;
            }
            else {
                // if not memoized alreayd compute recursively with memoization
                int countWithIllegal= countWithIllegals(tgtX, tgtY, rounds);
                int sumIllegals=0;
                // if the illegal jump is in round 1 recursion is easy
                for (int k=0; k< pd.badJumps.size(); k++) {
                    int badJump=  pd.badJumps.get(k);
                    
                    if (badJump> tgtX || badJump> tgtY)
                        break;
                    
                    sumIllegals += countWithIllegals(tgtX - badJump, tgtY - badJump, rounds-1);
                }
                // we make first illegal move in round: r from 2 to rounds
                for (int r=2; r<=rounds; r++) {
                      
                      for (int k=0; k< pd.badJumps.size(); k++) {
                          int badJump=  pd.badJumps.get(k);
                          int minXBad= minBadCoord( tgtX, rounds, r, 
                                                    badJump, pd.maxX);
                          int maxXBad= maxBadCoord( tgtX, rounds, r, 
                                                    badJump, pd.maxX);
                          int minYBad= minBadCoord( tgtY, rounds,r,
                                                    badJump, pd.maxY);
                          int maxYBad= maxBadCoord( tgtY, rounds, r, 
                                                    badJump, pd.maxY );
                          for (int xBad=minXBad; xBad<= maxXBad; xBad++) {
                              for (int yBad=minYBad; yBad<= maxYBad; yBad++) {
                              sumIllegals += countLegalFor(xBad,yBad, r-1) 
                                             * 
                                             countWithIllegals( tgtX - (xBad + badJump), 
                                                                tgtY - (yBad + badJump), 
                                                                rounds - r);
                              sumIllegals %= COUNT_MODULO;
                          }}
                      }
                }    
                result = (countWithIllegal - sumIllegals) %COUNT_MODULO;
                if (result<0) result+= COUNT_MODULO;
            }
            memoizeLegal(tgtX,tgtY, rounds, result);
            return result;
        }


        int solved=0;
        int maxRoundSoFar=0;
        int maxXSoFar=0;
        int maxYSoFar=0;
        private void memoizeLegal(int tgtX, int tgtY, int rounds, int result) {
            assert(result>=0);
            maxRoundSoFar= Math.max(maxRoundSoFar,rounds);
            maxXSoFar= Math.max(maxXSoFar,tgtX);
            maxYSoFar= Math.max(maxYSoFar,tgtY);
            solved++;
            if (solved % 10000 == 0) {
                System.err.println("Solved: " + solved+" nodes");
            }
            assert(legalSolutions.get(rounds-1,tgtX,tgtY) == -1);
            legalSolutions.set( result, 
                                rounds-1,tgtX,tgtY);
        }



        /**
         * What's the minimumX such that
         * in round r, to take a jump xbad[k] from xmin
         * and still there could be a possible  path :
         * xmin + xbad[k]+rounds*maxX >= targetX
         */
        private int minBadCoord( int tgt, int rounds, int r, int badVal, int maxJump) {
            return Math.max(0, tgt - (badVal +(rounds-r)*maxJump));
        }
        
        /**
         * What's the maximum X (or Y) such that
         * in round k  out of total r rounds, to take a jump xbad[k] from X
         * and still there could be a possible  path with r rounds.
         * xmax + xbad[k] <= targetX
         * xmax <= k-1* maxX
         */
        private int maxBadCoord(int tgt, int rounds, int r, int badVal, int maxJump) {
            return Math.min((r-1)*maxJump, tgt - badVal);
        }



        private int countWithIllegals(int tgtX, int tgtY, int rounds) {
            if (rounds == 0) {
                assert(tgtX == 0 && tgtY == 0);
                return 1;
            }
            else 
            return (illegalSolverX.countTo(tgtX, rounds)
                                  * illegalSolverY.countTo(tgtY, rounds))
                                  % COUNT_MODULO;
        }
        
         
        

    }

    private static class SolvingAlgorithm3 {
        private final ProblemDefinition pd;
        final MultiDimIntArray memoized;
        final SingleAxisSolver singleSolver;
        
        
        public SolvingAlgorithm3(ProblemDefinition problem_) {
            this.pd= problem_;
            this.memoized= initializeMemoArray( -1,
                                                (pd.tgtX+1) , (pd.tgtY+1) ,
                                                (pd.badJumps.size()+1),
                                                (pd.rounds+1),(pd.rounds+1));
            this.singleSolver= new SingleAxisSolver( pd.tgtX, pd.maxX, 
                                                       pd.rounds, COUNT_MODULO );
        }
         
        public int solveIt() {
            return recurseSolve(pd.tgtX, pd.tgtY,-1, pd.rounds, 0);
        }
        
        private int recurseSolve(int tgtX, int tgtY,  int badMoveVal, int roundsX, int roundsAheadX) {
            //debug("<>Solving for: tgtX=%s  , tgtY=%s badMoveVal=%s roundsX=%s, roundsAeadX=%s", tgtX, tgtY, badMoveVal, roundsX, roundsAheadX);
            int result= memoizedSolution(tgtX, tgtY, badMoveVal, roundsX, roundsAheadX);
            if (result != -1) {
                return result;
            }
            
            if (roundsX == 0 && badMoveVal == -1) // finish making choices on X axis but last move wasn't bad
            {
                assert(tgtX == 0);
                result= singleSolver.countTo(tgtY, roundsX + roundsAheadX);
            }
            else {
                result=0;
                if (badMoveVal == -1) // no bad move on X axis, continue recursion on X
                {
                    final int minMoveX= minimumMove(tgtX, roundsX, pd.maxX);
                    final int maxMoveX= Math.min(tgtX, pd.maxX);
                    for( int moveX= minMoveX; moveX<= maxMoveX; moveX++ ){
                        result += recurseSolve( tgtX-moveX, tgtY,
                                                pd.isBadJump(moveX) ? moveX : -1,
                                                roundsX - 1,
                                                roundsAheadX + 1
                                    );
                        result %= COUNT_MODULO;
                    }
                }
                else // we have a badMove on X axis continue on Y until catch up with X 
                {
                    final int minMoveY= minimumMove(tgtY, roundsX + roundsAheadX, pd.maxY);
                    final int maxMoveY= Math.min(tgtY, pd.maxY);
                    boolean skippingBadMove = roundsAheadX == 1;
                    for (int moveY= minMoveY; moveY<= maxMoveY; moveY++) {
                        if (skippingBadMove && badMoveVal == moveY) continue;
                        result +=  recurseSolve( tgtX, tgtY - moveY, 
                                                 skippingBadMove ? -1 : badMoveVal,
                                                 roundsX, roundsAheadX - 1);
                        result %= COUNT_MODULO;
                    }
                }
            }

            //debug("</>Solved for: result=%s tgtX=%s  , tgtY=%s badMoveVal=%s roundsX=%s, roundsAheadX=%s", result, tgtX, tgtY, badMoveVal, roundsX, roundsAheadX);
            memoizeSolution(result, tgtX,tgtY,badMoveVal, roundsX,roundsAheadX);
            return result;
        }

        int solved;
        private void memoizeSolution(int result,int tgtX, int tgtY, int badMoveVal,
                int roundsX, int roundsAheadX) {
            int badMoveIdx= badMoveVal == -1? pd.badJumps.size() : pd.badMoveIdxOf(badMoveVal);
            this.memoized.set(result,tgtX,tgtY,badMoveIdx,roundsX,roundsAheadX);
            solved ++;
            if (solved%10000 ==0) {
                debug("Nodes solved: %s", solved);
            }
        }

        private int memoizedSolution( int tgtX, int tgtY, int badMoveVal,
                                      int roundsX, int roundsAheadX ) 
        {
            int badMoveIdx= badMoveVal == -1? pd.badJumps.size() : pd.badMoveIdxOf(badMoveVal);
            return this.memoized.get(tgtX,tgtY,badMoveIdx,roundsX,roundsAheadX);
        }
    }
       
    private static class SolvingAlgorithm4 {
        private final ProblemDefinition pd;
        final MultiDimIntArray memoized;
        
        
        public SolvingAlgorithm4(ProblemDefinition problem_) {
            this.pd= problem_;
            this.memoized= initializeMemoArray( -1,
                                                2, (pd.badJumps.size()+1),
                                                (pd.tgtX+1) , (pd.tgtY+1) ,
                                                pd.rounds+1 );
        }
         
        public int solveIt() {
            return recurseSolve(false, -1, pd.tgtX, pd.tgtY,pd.rounds);
        }
        
        private int recurseSolve(boolean turnForY, int badMoveVal, int tgtX, int tgtY,  int rounds) {
            //debug("<>Solving for: tgtX=%s  , tgtY=%s badMoveVal=%s roundsX=%s, roundsAeadX=%s", tgtX, tgtY, badMoveVal, roundsX, roundsAheadX);
            int result= memoizedSolution(turnForY, badMoveVal, tgtX, tgtY, rounds);
            if (result != -1) {
                return result;
            }
            
            if (turnForY && rounds == 1 ) // finish making choices on X axis but last move wasn't bad
            {
                result= (tgtY == badMoveVal) ? 0 : 1;
            }
            else {
                result=0;
                if (! turnForY ) // no bad move on X axis, continue recursion on X
                {
                    final int minMoveX= minimumMove(tgtX, rounds, pd.maxX);
                    final int maxMoveX= Math.min(tgtX, pd.maxX);
                    for( int moveX= minMoveX; moveX<= maxMoveX; moveX++ ){
                        result += recurseSolve( true, pd.isBadJump(moveX) ? moveX : -1, 
                                                tgtX-moveX, tgtY,
                                                rounds
                                              );
                        result %= COUNT_MODULO;
                    }
                }
                else  
                {
                    final int minMoveY= minimumMove(tgtY, rounds, pd.maxY);
                    final int maxMoveY= Math.min(tgtY, pd.maxY);
                    boolean skippingBadMove = badMoveVal != -1;
                    for (int moveY= minMoveY; moveY<= maxMoveY; moveY++) {
                        if (skippingBadMove && badMoveVal == moveY) continue;
                        result +=  recurseSolve( false, -1,
                                                 tgtX, tgtY - moveY, 
                                                 rounds - 1);
                        result %= COUNT_MODULO;
                    }
                }
            }

            //debug("</>Solved for: result=%s tgtX=%s  , tgtY=%s badMoveVal=%s roundsX=%s, roundsAheadX=%s", result, tgtX, tgtY, badMoveVal, roundsX, roundsAheadX);
            memoizeSolution(result,turnForY,badMoveVal, tgtX,tgtY, rounds);
            return result;
        }

        int solved;
        private void memoizeSolution(int result,
                                      boolean turnForY, int badMoveVal,
                                      int tgtX, int tgtY, 
                                      int rounds) 
        {
            int badMoveIdx= badMoveVal == -1? pd.badJumps.size() : pd.badMoveIdxOf(badMoveVal);
            int turnIdx= turnForY ? 1 : 0;
            this.memoized.set(result,turnIdx,badMoveIdx,tgtX,tgtY,rounds);
            solved ++;
            if (solved%100000 ==0) {
                debug("Nodes solved: %s", solved);
            }
        }

        private int memoizedSolution( boolean turnForY, int badMoveVal, int tgtX, int tgtY, 
                                      int rounds ) 
        {
            int badMoveIdx= badMoveVal == -1? pd.badJumps.size() : pd.badMoveIdxOf(badMoveVal);
            int turnIdx= turnForY ? 1 : 0;
            return this.memoized.get(turnIdx,badMoveIdx,tgtX,tgtY,rounds);
        }
    }
    
    
    /**
     * What is the minimu move that mast be taken with roundsRemaining
     * so we can still reach target
     */
    private static int minimumMove(int tgt, int roundsRemaining, int maxMove) {
        assert(roundsRemaining > 0);
        return Math.max(0, tgt - (roundsRemaining-1)* maxMove);
    }

    private static boolean _debug= System.getenv("DEBUG")!= null;
    private static void debug(String fmtMessage, Object ... vals) {
        if (_debug)
            System.err.println(String.format(fmtMessage, vals));
    }

    /**
     * poor man's java.lang.reflect.Array for restricted envs
     */
    private static class MultiDimIntArray {

        final int[] backingArray;
        final int[] dimensions;
        final int[] subProds;

        public MultiDimIntArray (int ... sizes) {
            this.dimensions= (int[])sizes.clone();
            this.subProds= new int[dimensions.length];
            int prod= 1;
            int safety= Integer.MAX_VALUE % prod;
            for (int i=sizes.length-1; i >=0 ; i--) {
                int d=sizes[i];
                if ( d <=0) 
                    throw new IllegalArgumentException("Invalid array dimension: "+d );
                safety /= d;
                if (safety==0) {
                    throw new IllegalArgumentException("MultiDimensional array exceeded safe size limits");
                }
                subProds[i]= prod;
                prod *= d;
            }
            this.backingArray= new int[prod];
        }

        public void fill(int initVal) {
            Arrays.fill(backingArray, initVal);
        }

        public void set(int d, int ...coord) {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        public int get(int ... coordinates) {
            int idx=0;
            assert(coordinates.length == dimensions.length);
            int i,c;
            for (i=0; i<coordinates.length; i++) {
                c=coordinates[i];
                if (c >= dimensions[i])
                    throw new ArrayIndexOutOfBoundsException(c);
                idx += c * subProds[i];
            }
            return backingArray[idx];
        }
        
    }
}
/**
 Test Case: 1
Status: Passed
Input:
2
2
1
1
2
-

Program Output: 1

Expected Output: 1
----------------
Test Case: 2
Status: Passed
Input:
2
2
1
1
3
-

Program Output: 6

Expected Output: 6
----------------
Test Case: 3
Status: Passed
Input:
10
10
10
10
1
-

Program Output: 1

Expected Output: 1
----------------
Test Case: 4
Status: Passed
Input:
10
10
10
10
1
10

Program Output: 0

Expected Output: 0
----------------
Test Case: 5
Status: Passed
Input:
11
11
11
11
2
10

Program Output: 140

Expected Output: 140
----------------
Test Case: 6
Status: Failed
Input:
123
456
70
80
90
30,40,20,10,50

Program Output: 
Execution Timed Out
Expected Output: 6723
----------------
Test Case: 7
Status: Failed
Input:
776
612
800
800
333
30,60,80,110,200

Program Output: 
Execution Timed Out
Expected Output: 6355
----------------
Test Case: 8
Status: Failed
Input:
725
677
23
481
33
20

Program Output: 
Execution Timed Out
Expected Output: 6041
----------------
Test Case: 9
Status: Passed
Input:
714
2
91
800
202
-

Program Output: 2805

Expected Output: 2805
----------------
Test Case: 10
Status: Passed
Input:
1
1
1
1
1600
-

Program Output: 0

Expected Output: 0
----------------

 */

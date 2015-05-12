package dk.brics.automaton;

import java.util.Arrays;

/**
 * Date: 12.05.2015
 * Time: 17:23
 */
public class CompressedRunAutomaton extends RunAutomaton {
    int[] transitionOffsets;
    int[] transitionPoints;

    public CompressedRunAutomaton(RunAutomaton runAutomaton) {
        super(Automaton.makeAnyChar());
        size = runAutomaton.size;
        initial = runAutomaton.initial;
        accept = runAutomaton.accept;
        points = runAutomaton.points;
        classmap = runAutomaton.classmap;

        int[] oldTransitions = runAutomaton.transitions;
        int[] newTransitions = new int[oldTransitions.length];
        transitionOffsets = new int[size];
        transitionPoints = new int[size];

        int stateIndex = 0;
        int newOffset = 0;
        for (int oldOffset = 0; oldOffset < oldTransitions.length; oldOffset += points.length) {
            int beginPoint = 0;
            while (beginPoint < points.length && oldTransitions[oldOffset + beginPoint] == -1) {
                beginPoint++;
            }
            int endPoint = points.length;
            while (endPoint >= beginPoint && oldTransitions[oldOffset + endPoint - 1] == -1) {
                endPoint--;
            }
            transitionOffsets[stateIndex] = newOffset;
            transitionPoints[stateIndex] = beginPoint;
            stateIndex++;
            if (endPoint > beginPoint) {
                System.arraycopy(oldTransitions, oldOffset + beginPoint,
                    newTransitions, newOffset, endPoint - beginPoint);
                newOffset += endPoint - beginPoint;
            }
        }
        this.transitions = Arrays.copyOf(newTransitions, newOffset);
    }

    @Override
    public int step(int state, char c) {
        int point = classmap == null ? getCharClass(c) : classmap[c - Character.MIN_VALUE];
        int beginPoint = transitionPoints[state];
        int pointOffset = point - beginPoint;
        if (pointOffset < 0) {
            return -1;
        }
        int beginOffset = transitionOffsets[state];
        int endOffset = state + 1 < size ? transitionOffsets[state + 1] : transitions.length;
        if (beginOffset + pointOffset >= endOffset) {
            return -1;
        }
        return transitions[beginOffset + pointOffset];
    }
}

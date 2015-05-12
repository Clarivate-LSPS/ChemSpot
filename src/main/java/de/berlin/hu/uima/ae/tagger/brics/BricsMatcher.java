/*
 * Copyright (c) 2012. Humboldt-Universität zu Berlin, Dept. of Computer Science and Dept.
 * of Wissensmanagement in der Bioinformatik
 * -------------------------------
 *
 * THE ACCOMPANYING PROGRAM IS PROVIDED UNDER THE TERMS OF THIS COMMON PUBLIC
 * LICENSE ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THE PROGRAM
 * CONSTITUTES RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT.
 *
 * http://www.opensource.org/licenses/cpl1.0
 */

package de.berlin.hu.uima.ae.tagger.brics;

import de.berlin.hu.chemspot.Mention;
import de.berlin.hu.uima.ae.normalizer.Normalizer;
import dk.brics.automaton.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * User: Tim Rocktaeschel
 * Date: 7/2/12
 * Time: 2:20 PM
 */
public class BricsMatcher {
	public static final int DEFAULT_TERMS_PER_AUTOMATON = 100000;
	
    private Collection<RunAutomaton> matchers = new ArrayList<RunAutomaton>();

    public BricsMatcher() throws IOException, ClassNotFoundException {
    	this(Normalizer.getIds().keySet(), DEFAULT_TERMS_PER_AUTOMATON);
    }
    
    public BricsMatcher(Collection<String> chemicals) throws IOException, ClassNotFoundException {
    	System.out.print("Creating brics automaton...");
    	matchers.add(BricsMatcher.createAutomaton(chemicals));
    	System.out.println("Done.");
    }
    
    public BricsMatcher(Collection<String> chemicals, int termsPerAutomaton) throws IOException, ClassNotFoundException {
    	System.out.print("Creating brics automata");
    	matchers.addAll(BricsMatcher.createAutomata(chemicals, termsPerAutomaton));
    	
    	System.out.println("Created " + matchers.size() + " brics automata.");
    }

	private void storeAutomaton(RunAutomaton automaton, int pieceNumber) throws IOException {
		FileOutputStream f = new FileOutputStream(String.format("unitedAutomata-%d.bin", pieceNumber));
		try {
			automaton.store(f);
		} finally {
			f.close();
		}
	}

    /**
     * BricsMatcher loads a set of brics dictionary matchers packed in a zip file.
     * @param pathToZippedBinaries Path to the zip file containing a set of brics dictionary matchers.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public BricsMatcher(String pathToZippedBinaries) throws IOException, ClassNotFoundException {
		int bricsAutomata = 0;
    	if (pathToZippedBinaries.endsWith(".zip")) {
	        ZipFile zipFile = new ZipFile(pathToZippedBinaries);
	        List<? extends ZipEntry> entries = Collections.list(zipFile.entries());
			Automaton unitedAutomaton = null;
			int piecesCount = 0;
			int pieceSize = 1;
	        for (ZipEntry entry : entries) {
				bricsAutomata++;
				System.out.printf("Loading %s (%d of %d)...\n", entry.getName(), bricsAutomata, entries.size());
				RunAutomaton automaton = RunAutomaton.load(zipFile.getInputStream(entry));
				if (pieceSize > 1) {
					Automaton tempAutomaton = restoreAutomaton(automaton);
					unitedAutomaton = unitedAutomaton != null ? unitedAutomaton.union(tempAutomaton) : tempAutomaton;
					if (bricsAutomata % pieceSize == 0) {
						RunAutomaton runAutomaton = new RunAutomaton(unitedAutomaton);
						storeAutomaton(runAutomaton, ++piecesCount);
						matchers.add(runAutomaton);
						unitedAutomaton = null;
					}
				} else {
					matchers.add(new CompressedRunAutomaton(automaton));
				}
	        }
			if (unitedAutomaton != null) {
				RunAutomaton runAutomaton = new RunAutomaton(unitedAutomaton);
				storeAutomaton(runAutomaton, ++piecesCount);
				matchers.add(runAutomaton);
			}
    	} else {
			matchers.add(RunAutomaton.load(new FileInputStream(pathToZippedBinaries)));
			bricsAutomata++;
    	}
        System.out.println("Loaded " + bricsAutomata + " brics automata.");
    }
    
    public static List<RunAutomaton> createAutomata(Collection<String> chemicals, int termsPerAutomaton) {
    	List<RunAutomaton> result = new ArrayList<RunAutomaton>();
    	
    	int count = 0;
    	int total = 0;
    	List<String> terms = new ArrayList<String>();
    	for (String chemical : chemicals) {
    		terms.add(chemical);
    		
    		count++;
    		total++;
    		if (count >= termsPerAutomaton) {
    			result.add(createAutomaton(terms));
    			System.out.print(".");
    			terms.clear();
    			count = 0;
    		}
    	}
    	
    	if (!terms.isEmpty()) {
    		result.add(createAutomaton(terms));
    		System.out.println(" Done.");
			terms.clear();
			count = 0;
    	}
    	
    	return result;
    }
    
    public static RunAutomaton createAutomaton(Collection<String> chemicals) {
    	List<String> sortedList = new ArrayList<String>(chemicals);
		Collections.sort(sortedList, StringUnionOperations.LEXICOGRAPHIC_ORDER);
		String[] sortedArray = sortedList.toArray(new String[sortedList.size()]);
		sortedList = null;
		State state = StringUnionOperations.build(sortedArray);
		Automaton automaton = new Automaton();
		automaton.setInitialState(state);
		
		RunAutomaton runAutomaton = new RunAutomaton(automaton);
		
		return runAutomaton;
    }

    /**
     * Uses the set of brics dictionary matchers to extract mentions of chemical entities in natural language text.
     * @param text Input natural language text.
     * @return a collection of Mentions.
     */
    public Collection<Mention> match(String text) {
        Collection<Mention> matches = new HashSet<Mention>();
        for (RunAutomaton automat : matchers) {
            AutomatonMatcher matcher = automat.newMatcher(text);
            while (matcher.find()) {
                char left = ' ';
                char right = ' ';
                char nright = ' ';
                try {
                    left = text.charAt(matcher.start() - 1);
                } catch (ArrayIndexOutOfBoundsException e) {
                    //ignore
                } catch (StringIndexOutOfBoundsException e) {
                    //ignore
                }
                try {
                    right = text.charAt(matcher.end());
                } catch (ArrayIndexOutOfBoundsException e) {
                    //ignore
                } catch (StringIndexOutOfBoundsException e) {
                    //ignore
                }
                try {
                    nright = text.charAt(matcher.end() + 1);
                } catch (ArrayIndexOutOfBoundsException e) {
                    //ignore
                } catch (StringIndexOutOfBoundsException e) {
                    //ignore
                }
                String coveredText = text.substring(matcher.start(), matcher.end());

                //only add if not within a text and longer than two characters
                if (coveredText.length() > 2 && !Character.isLetter(left) && 
                		(!Character.isLetter(right) || (right == 's' && Character.isLetter(nright)))) {
                    matches.add(new Mention(matcher.start(), matcher.end() + (right == 's' ? 1 : 0), text.substring(matcher.start(), matcher.end())));
                }
            }
        }
        return matches;
    }

	static class StatePair {
		int runState;
		State state;

		public StatePair(int runState, State state) {
			this.runState = runState;
			this.state = state;
		}

		public int getRunState() {
			return runState;
		}

		public State getState() {
			return state;
		}
	}

	public static Automaton restoreAutomaton(RunAutomaton runAutomaton) {
		char[] points = runAutomaton.getCharIntervals();
		Automaton automaton = new Automaton();
		automaton.setDeterministic(true);
		Map<Integer, State> visitedStates = new HashMap<Integer, State>();
		LinkedList<StatePair> statesToVisit = new LinkedList<StatePair>();
		statesToVisit.add(new StatePair(runAutomaton.getInitialState(), automaton.getInitialState()));
		visitedStates.put(runAutomaton.getInitialState(), automaton.getInitialState());

		while (!statesToVisit.isEmpty()) {
			StatePair pair = statesToVisit.removeFirst();
			State state = pair.getState();
			int runState = pair.getRunState();
			for (int i = 0; i < points.length; i++) {
				char c = points[i];
				int toRunState = runAutomaton.step(runState, c);
				if (toRunState == -1) {
					continue;
				}
				State toState = visitedStates.get(toRunState);
				if (toState == null) {
					toState = new State();
					toState.setAccept(runAutomaton.isAccept(toRunState));
					visitedStates.put(toRunState, toState);
					statesToVisit.add(new StatePair(toRunState, toState));
				}
				state.addTransition(new Transition(c, i < points.length ? points[i+1] : Character.MAX_VALUE, toState));
			}
		}
		return automaton;
	}

	public static void addAutomaton(Automaton automaton, RunAutomaton runAutomaton) {

	}

	public static void main(String[] args) {
		RunAutomaton runAutomaton = new RunAutomaton(BasicAutomata.makeString("Hello world"));
		RunAutomaton runAutomaton2 = new RunAutomaton(BasicAutomata.makeStringUnion("bar", "foo"));
		Automaton automaton = restoreAutomaton(runAutomaton);


		System.out.println(runAutomaton.newMatcher("Hello world is here").find());
		System.out.println(runAutomaton2.run("Hello world"));
		System.out.println(runAutomaton2.run("bar"));
		System.out.println(automaton.run("Hello world"));
		System.out.println(automaton.run("bar"));
		Automaton common = automaton.union(restoreAutomaton(runAutomaton2));
		System.out.println(common.run("Hello world"));
		System.out.println(common.run("foo"));
		System.out.println(common.run("bar"));
		RunAutomaton runCommon = new RunAutomaton(common);
		System.out.println(runCommon.run("Hello world"));
		System.out.println(runCommon.run("foo"));
		System.out.println(runCommon.run("bar"));
	}
}

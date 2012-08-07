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

package de.berlin.hu.uima.ae.tagger.linnaeus;

import de.berlin.hu.chemspot.Mention;
import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * User: Tim Rocktaeschel
 * Date: 7/2/12
 * Time: 2:20 PM
 */
public class BricsMatcher {
    private Collection<RunAutomaton> matchers = new ArrayList<RunAutomaton>();

    /**
     * BricsMatcher loads a set of brics dictionary matchers packed in a zip file.
     * @param pathToZippedBinaries Path to the zip file containing a set of brics dictionary matchers.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public BricsMatcher(String pathToZippedBinaries) throws IOException, ClassNotFoundException {
        ZipFile zipFile = new ZipFile(pathToZippedBinaries);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            matchers.add(RunAutomaton.load(zipFile.getInputStream(entry)));
        }
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
              matches.add(new Mention(matcher.start(), matcher.end()));
            }
        }
        return matches;
    }
}
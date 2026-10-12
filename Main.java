import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

//NAME: Naomi Hayes
//ASSIGNMENT: PGRM 3
//LAB SECTION: W - 7-9
//LECTURE SECTION: MWF - 10-10:50

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        ArrayList<SentList> sentList = new ArrayList<SentList>();
        ArrayList<SentList> posList = new ArrayList<SentList>();
        ArrayList<SentList> negList = new ArrayList<SentList>();
        ArrayList<Words> wordList = new ArrayList<Words>();

        //load sentiment, positive words and negative words arraylists
        readSentimentList(sentList, posList, negList);

        //read review
        //load ArrayList wordList that will contain original review & pos & neg
        String outFileName = "reviews.txt";
        PrintWriter outFile = new PrintWriter(outFileName);

        // open input file adding review + number + ".txt" to review
        // if not able to open, print a message and continue
        // else process the file
        // if the file can be read properly, print the results
        for (int i = 1; i <= 8; i++){
            String inFileName = "review" + i + ".txt";
            wordList.clear();

            boolean success = readReview(sentList, posList, negList, wordList, inFileName);

            if (success) {
                printReview(wordList, inFileName, outFile);

                makeMorePositive(wordList, posList, outFile);

                makeMoreNegative(wordList, negList, outFile);

            } else {
                outFile.println("Could not open file: " + inFileName);
                outFile.println();
            }
 
        }

        outFile.close();
    }

    //PRE:  accept the empty ArrayLists created in main
    //POST: the arrays are loaded with the proper words and information
    public static void readSentimentList(ArrayList<SentList> sentList,
                                         ArrayList<SentList> posList,
                                         ArrayList<SentList> negList){
        String csvFilePath = "sentiment.txt";
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            while ((line = br.readLine()) != null) {
                 // Split the line by commas into an array of strings
                  String[] parts = line.split(",");
                  if (parts.length <2) continue;

                  String word = parts[0].trim().toLowerCase();
                  double value = Double.parseDouble(parts[1].trim());

                  SentList item = new SentList(word, value);
                  sentList.add(item);

                  if (value > 1.25) {
                    posList.add(item);
                  } else if (value < -1.25) {
                    negList.add(item);
                  }
                 //Create object SentList & add to sentList arraylist
 
                 //if word values are pos add to posList
                 //if word values are neg, add to neglist
            }
        }
        catch (IOException e) {
            System.out.println("Error reading the file.");
            e.printStackTrace();
        }
    }

    //PRE: accept the word lists and file name to open
    //POST: read the file while the line is not null
    //      each word is edited (to lower case without punctuation)
    //      the sentiment value is accessed
    //      if the word is positive - update to a random word in the negative list and update the word value
    //      if the word is negative - update to a random positive word in the positive list & update the word value

    public static boolean readReview(ArrayList<SentList> sentList,
                                  ArrayList<SentList> posList,
                                  ArrayList<SentList> negList,
                                  ArrayList<Words> wordList,
                                  String fileName){

        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                 // Split the line by commas into an array of strings
                 String[] values = line.split("  ");
                 
                 //for each word
                 //  strip off punctuation at the end of word set charWord to null or value of punctuation
                 //  save origWord, posWord, negWord
                 //  change to lower case  (set editWord)
                 //  look up word in sentList (set origValue, posVlue, negValue)
                 //  if positive enough, find new negword in negList & reset negWord & NegValue, reset negFlag
                 //  if negative enough, find new posword in posList & reset posWord & posValue, reset posFlag
                    for (String token : values) {
                        if (token.isEmpty()) continue; // Skip empty tokens

                        String origWord = token;

                    StringBuilder clean = new StringBuilder();

                    for (int i = 0; i < token.length(); i++) {
                        char c = token.charAt(i);
                        if (isPunctuation(c)) {
    
                        } else {
                            clean.append(c);
                        }
                    }

                    String editWord = clean.toString();
                    double value = getSentiment(sentList, editWord);

                    Words w = new Words(origWord, editWord, "", "",value, 0.0, 0.0);
                    wordList.add(w);
                 }

            }
            br.close();
            return true;
        }
        catch (IOException e) {
            System.out.println("Error reading the file: " + fileName);
            return false;
        }
    }

    //PRE:  accept the updated wordlist
    //POST: loop through word list, create a string that will be the original, positive & negative reviews
    //      print each review
    public static void printReview(ArrayList<Words> wordList, String inFile,
                                  PrintWriter outFile){

        outFile.println("PROCESSING FILE:  " + inFile);
        outFile.println("FORMATTED REVIEW:");

        StringBuilder line = new StringBuilder();
        double totalSentiment = 0.0;

        for (Words w : wordList) {
            totalSentiment += w.getValue();

            String wordToPrint = w.getOrigWord();

            if (line.length() + wordToPrint.length() + 1 > 80) {
                outFile.println(line.toString().trim());
                line = new StringBuilder();
            }
            line.append(wordToPrint).append(" ");

        }

        if (line.length() > 0) {
            outFile.println(line.toString().trim());
        }

        outFile.printf("ORIGINAL SENTIMENT: %.2f\n", totalSentiment);
        outFile.println();
        outFile.println();
        outFile.println();
    }

    //PRE:  accept a character
    //POST: return true if this character is punctuation; false otherwise
    //      There may be a better way to write this!
    static boolean isPunctuation(char ch) {
        if (ch == '!' || ch == '\"' || ch == '#' || ch == '$' || ch == '%' || ch == '&' || ch == '\'' || ch == '(' || ch == ')' || ch == '*' || ch == '+' || ch == ',' || ch == '-' || ch == '.' || ch == '/' || ch == ':' || ch == ';' || ch == '<' || ch == '=' || ch == '>' || ch == '?' || ch == '@' || ch == '[' || ch == '\\' || ch == ']' || ch == '^' || ch == '`' || ch == '{' || ch == '|' || ch == '}')
          return true;
        return false;
    }

    //PRE:  accept the sentiment words list and a word to find
    //POST: return the value of the sentiment if found, 0 otherwise
    static double getSentiment (ArrayList<SentList> sentList, String eWord) {
        for (SentList s : sentList) {
            if (s.getWord().equalsIgnoreCase(eWord)) {
                return s.getValue();
            }

        }
        
        return 0.0; //Word not found
    }

    static SentList getRandomWord(ArrayList<SentList> list) {
        if (list.isEmpty()) return null;
        int index = (int)(Math.random() * list.size());
        return list.get(index);

    }
    
    public static void makeMorePositive (ArrayList<Words> wordList,
                                         ArrayList<SentList> posList,
                                         PrintWriter outFile) {

outFile.println("WORDS UPDATED TO BE MORE POSTIVE:");
boolean anyChanged = false;
double newTotal = 0.0;

for (Words w : wordList) {
    double val = w.getValue();
    newTotal += val;

    if (val < -1.0) {
        SentList replacement = getRandomWord(posList);
        if (replacement != null) {
            anyChanged = true;
            outFile.printf("%15s %8,.2f     %-15s %*.2f%n",
                           w.getEditWord(), val,
                           replacement.getWord(), replacement.getValue());
            w.setPosWord(replacement.getWord());
            w.setPosValue(replacement.getValue());

            newTotal = newTotal - val + replacement.getValue();

            
        }
    }
}

if (!anyChanged) {
    outFile.println("No word were replaced.");

}

outFile.println();
outFile.println("UPDATED REVIEW (MORE POSTIVE):");

StringBuilder line = new StringBuilder();
for (Words w : wordList) {
    String wordToPrint;
    if (w.getPosWord() != null) {
        wordToPrint = w.getPosWord();
    } else {
        wordToPrint = w.getOrigWord();
    }
    if (line.length() + wordToPrint.length() + 1 > 80) {
        outFile.println(line.toString().trim());
        line = new StringBuilder();
    }
    line.append(wordToPrint).append(" ");
}
if (line.length() > 0) {
    outFile.println(line.toString().trim());
}

outFile.printf("UPDATED SENTIMENT: %.2f%n", newTotal);
outFile.println();
outFile.println();
}

public static void makeMoreNegative (ArrayList<Words> wordList,
                                         ArrayList<SentList> negList,
                                         PrintWriter outFile) {
outFile.println("WORDS UPDATED TO BE MORE NEGATIVE:");
boolean anyChanged = false;
double newTotal = 0.0;

for (Words w : wordList) {
    double val = w.getValue();
    newTotal += val;

    if (val > 1.0) {
        SentList replacement = getRandomWord(negList);
        if (replacement != null) {
            anyChanged = true;
            outFile.printf("%15s %8,.2f     %-15s %*.2f%n",
                           w.getEditWord(), val,
                           replacement.getWord(), replacement.getValue());

            w.setNegWord(replacement.getWord());
            w.setNegValue(replacement.getValue());

            newTotal = newTotal - val + replacement.getValue();
        }
    }
}

if (!anyChanged) {
    outFile.println("No word were replaced.");
}

outFile.println();
outFile.println("UPDATED REVIEW (MORE NEGATIVE):");

StringBuilder line = new StringBuilder();
for (Words w : wordList) {
    String wordToPrint;
    if (w.getNegWord() != null) {
        wordToPrint = w.getNegWord();
    } else {
        wordToPrint = w.getOrigWord();
    }
    if (line.length() + wordToPrint.length() + 1 > 80) {
        outFile.println(line.toString().trim());
        line = new StringBuilder();
    }
    line.append(wordToPrint).append(" ");

}
if (line.length() > 0) {
    outFile.println(line.toString().trim());
}

outFile.printf("UPDATED SENTIMENT: %.2f%n", newTotal);
outFile.println();
outFile.println();
outFile.println();

}
}

package cs224n.wordaligner;  

import cs224n.util.*;
import java.util.List;

/**
 * Simple word alignment baseline model that maps source positions to target 
 * positions along the diagonal of the alignment grid.
 * 
 * IMPORTANT: Make sure that you read the comments in the
 * cs224n.wordaligner.WordAligner interface.
 * 
 * @author Dan Klein
 * @author Spence Green
 */
public class PMIAligner implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;
  
  // TODO: Use arrays or Counters for collecting sufficient statistics
  // from the training data.
  private CounterMap<String,String> sourceTargetCounts;
  private Counter<Pair<String, String>> wordPairCounts;
  private Counter<String> sourceWordCounts;
  private Counter<String> targetWordCounts;
  public Alignment align(SentencePair sentencePair) {
    // Placeholder code below. 
    // TODO Implement an inference algorithm for Eq.1 in the assignment
    // handout to predict alignments based on the counts you collected with train().
    Alignment alignment = new Alignment();

    // YOUR CODE HERE
    List<String> targetWords = sentencePair.getTargetWords();
    List<String> sourceWords = sentencePair.getSourceWords();
    
    int numSourceWords = sourceWords.size();
    int numTargetWords = targetWords.size();
    for (int srcIndex = 0; srcIndex < numSourceWords; srcIndex++){
      String source = sourceWords.get(srcIndex);
      if (sourceWordCounts.getCount(source) == 0) {
        continue;
      }
      double bestScore = 0.0;
      int bestIndex = 0;
      for (int tgtIndex = 0; tgtIndex < numTargetWords; tgtIndex++){
        String target = targetWords.get(tgtIndex);
        if (targetWordCounts.getCount(target)==0){
          continue;
        }
        double score = ( (double) wordPairCounts.getCount(new Pair(target, source))) / (sourceWordCounts.getCount(source) * targetWordCounts.getCount(target));
        if (score > bestScore){
          bestScore = score;
          bestIndex = tgtIndex;
        }
      }
      if (bestScore == 0){
        continue;
      }
      alignment.addPredictedAlignment(bestIndex, srcIndex);
    }
    return alignment;
  }

  public void train(List<SentencePair> trainingPairs) {
    
    // YOUR CODE HERE
    wordPairCounts = new Counter<Pair<String, String>>();
    sourceWordCounts = new Counter<String>();
    targetWordCounts = new Counter<String>();
    for (SentencePair pair : trainingPairs){
      List<String> targetWords = pair.getTargetWords();
      List<String> sourceWords = pair.getSourceWords();
      for (String source : sourceWords){
	sourceWordCounts.incrementCount(source, 1);
        for (String target : targetWords){
	  Pair wordPair = new Pair(target, source);
	  wordPairCounts.incrementCount(wordPair, 1);
        }
      }
      for (String target : targetWords){
	targetWordCounts.incrementCount(target, 1);
      }
    }
  }
}

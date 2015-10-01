package cs224n.wordaligner;  

import cs224n.util.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
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
public class IBMModel1Aligner implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;
  
  // TODO: Use arrays or Counters for collecting sufficient statistics
  // from the training data.
  private CounterMap<String,String> sourceTargetCounts;

  public Alignment align(SentencePair sentencePair) {
    // Placeholder code below. 
    // TODO Implement an inference algorithm for Eq.1 in the assignment
    // handout to predict alignments based on the counts you collected with train().
    Alignment alignment = new Alignment();

    // YOUR CODE HERE
    int numSourceWords = sentencePair.getSourceWords().size();
    int numTargetWords = sentencePair.getTargetWords().size();

    for (int srcIndex = 0; srcIndex < numSourceWords; srcIndex++){
      String source = sentencePair.getSourceWords().get(srcIndex);
      double bestScore = 0;
      int bestIndex = 0;
      for (int tgtIndex = 0; tgtIndex < numTargetWords; tgtIndex++){
        String target = sentencePair.getTargetWords().get(tgtIndex);
        double  score = sourceTargetCounts.getCount(target, source);
        if (score > bestScore){
          bestScore = score;
          bestIndex = tgtIndex;
        }
      }
      double score = sourceTargetCounts.getCount(NULL_WORD, source);
      if (score >= bestScore){
        continue;
      }
      alignment.addPredictedAlignment(bestIndex, srcIndex);
    }
    return alignment;
  } 
   

  public void train(List<SentencePair> trainingPairs) {
    
    // YOUR CODE HERE
    System.out.println("training ......");
    sourceTargetCounts = new CounterMap<String, String>();
    for (SentencePair pair : trainingPairs){
      List<String> targetWords = pair.getTargetWords();
      List<String> sourceWords = pair.getSourceWords();
      for (String source : sourceWords){
        for (String target : targetWords){
          sourceTargetCounts.setCount(target, source, 1);
        }
        sourceTargetCounts.setCount(NULL_WORD, source, 1);
      }
    }
  
    for (int n = 0; n < 1000; n++){
      CounterMap<String,String> sourceTargetCounts_temp = new CounterMap<String, String>();
      System.out.println(n);
      for (SentencePair pair : trainingPairs){
        List<String> targetWords = pair.getTargetWords();
        List<String> sourceWords = pair.getSourceWords();
        for (String source : sourceWords){
          double den = 0;
          for (String target : targetWords){
            den += sourceTargetCounts.getCount(target, source);
          }
          den += sourceTargetCounts.getCount(NULL_WORD, source);
          for (String target : targetWords){
            double p = ( (double) sourceTargetCounts.getCount(target, source)  )/ den;
            sourceTargetCounts_temp.incrementCount(target, source, p);
          }
            double p = ( (double) sourceTargetCounts.getCount(NULL_WORD, source) ) / den;  
            sourceTargetCounts_temp.incrementCount(NULL_WORD, source, p);
        }
      }
        Counters counters = new Counters();
        sourceTargetCounts = sourceTargetCounts_temp;
        sourceTargetCounts = counters.conditionalNormalize(sourceTargetCounts);
      
    }

 
  }
}

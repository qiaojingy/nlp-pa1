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
public class IBMModel2Aligner implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;
  
  // TODO: Use arrays or Counters for collecting sufficient statistics
  // from the training data.
  private CounterMap<String,String> sourceTargetCounts;
  class MyObjectArrayList extends CounterMap<String, String> {
    MyObjectArrayList(CounterMap<String, String> counterMap){
      super();
      for(String key : counterMap.keySet()){
        Counter<String> counter = counterMap.getCounter(key);
        for(String value : counter.keySet()){
          setCount(key, value, counterMap.getCount(key, value));
        }
      }
    }
    MyObjectArrayList(){
      super();
    }     
  }

  private IBMModel2Aligner.MyObjectArrayList alignCounts[][];

  public Alignment align(SentencePair sentencePair) {
    // Placeholder code below. 
    // TODO Implement an inference algorithm for Eq.1 in the assignment
    // handout to predict alignments based on the counts you collected with train().
    Alignment alignment = new Alignment();

    // YOUR CODE HERE
    int numSourceWords = sentencePair.getSourceWords().size();
    int numTargetWords = sentencePair.getTargetWords().size();
    int m = numSourceWords;
    int l = numTargetWords;
    for (int srcIndex = 0; srcIndex < numSourceWords; srcIndex++){
      String source = sentencePair.getSourceWords().get(srcIndex);
      double bestScore = 0;
      int bestIndex = 0;
      for (int tgtIndex = 0; tgtIndex < numTargetWords; tgtIndex++){
        String target = sentencePair.getTargetWords().get(tgtIndex);
        double  score = alignCounts[m][l].getCount(Integer.toString(srcIndex+1), Integer.toString(tgtIndex+1)) * sourceTargetCounts.getCount(target, source);
        if (score > bestScore){
          bestScore = score;
          bestIndex = tgtIndex;
        }
      }
      double score = alignCounts[m][l].getCount(Integer.toString(srcIndex+1), Integer.toString(0)) * sourceTargetCounts.getCount(NULL_WORD, source);
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
    Set<String> sourceVocab = new HashSet<String>();
    Set<String> targetVocab = new HashSet<String>();
    int maxLenTarget = 0;
    int maxLenSource = 0;
    for (SentencePair pair : trainingPairs){
      List<String> targetWords = pair.getTargetWords();
      List<String> sourceWords = pair.getSourceWords();
      if (targetWords.size() > maxLenTarget){
        maxLenTarget = targetWords.size();
      }
      if (sourceWords.size() > maxLenSource){
        maxLenSource = sourceWords.size();
      }
      for (String source : sourceWords){
        sourceVocab.add(source);
      }
      for (String target : targetWords){
        targetVocab.add(target);
      }
    }
    System.out.println(maxLenTarget);
    System.out.println(maxLenSource);
    targetVocab.add(NULL_WORD);
    for (String target : targetVocab){
      for (String source : sourceVocab){
        sourceTargetCounts.setCount(target, source, 1);
      }
    }
    alignCounts = new MyObjectArrayList [maxLenSource+1] [maxLenTarget+1];
    for (int m=1; m<maxLenSource+1; m++){
      for (int l=0; l<maxLenTarget+1; l++){
        alignCounts[m][l] = new MyObjectArrayList();
        for (int i=1; i<m+1; i++){
          for (int j=0; j<l+1; j++){
            alignCounts[m][l].setCount(Integer.toString(i), Integer.toString(j), 1);
          }
        }
      }
    }
  
    for (int n = 0; n < 200; n++){
      CounterMap<String,String> sourceTargetCounts_temp = new CounterMap<String, String>();
      IBMModel2Aligner.MyObjectArrayList alignCounts_temp[][] = new IBMModel2Aligner.MyObjectArrayList [maxLenSource+1] [maxLenTarget+1];
      for (int m=1; m<maxLenSource+1; m++){
        for (int l=0; l<maxLenTarget+1; l++){
          alignCounts_temp[m][l] = new IBMModel2Aligner.MyObjectArrayList();
        }
      }
      System.out.println(n);
      for (SentencePair pair : trainingPairs){
        List<String> targetWords = pair.getTargetWords();
        List<String> sourceWords = pair.getSourceWords();
        int l = targetWords.size();
        int m = sourceWords.size();
        for (int i=1; i<m+1; i++){
          double den = 0;
          String source = sourceWords.get(i-1);
          for (int j=0; j<l+1; j++){
            String target;
            if (j==0) {
              target = NULL_WORD;
            }
            else {
              target = targetWords.get(j-1);
            }
            den += alignCounts[m][l].getCount(Integer.toString(i),Integer.toString(j)) * sourceTargetCounts.getCount(target, source);
          }
          for (int j=0; j<l+1; j++){
            String target;
            if (j==0) {
              target = NULL_WORD;
            }
            else {
              target = targetWords.get(j-1);
            }
            double p = ((double) alignCounts[m][l].getCount(Integer.toString(i), Integer.toString(j))) *( (double) sourceTargetCounts.getCount(target, source)  )/ den;
            sourceTargetCounts_temp.incrementCount(target, source, p);
            alignCounts_temp[m][l].incrementCount(Integer.toString(i), Integer.toString(j), p);
          }
        }
      }
      Counters counters = new Counters();
      sourceTargetCounts = sourceTargetCounts_temp;
      sourceTargetCounts = counters.conditionalNormalize(sourceTargetCounts);
      alignCounts = alignCounts_temp;
      for (int m=1; m<maxLenSource+1; m++){
        for (int l=0; l<maxLenTarget+1; l++){
          alignCounts[m][l] = new IBMModel2Aligner.MyObjectArrayList( counters.conditionalNormalize((CounterMap<String, String>) alignCounts[m][l]));
        }
      }
    }    

  }
}

import computation.contextfreegrammar.*;
import computation.parser.*;
import computation.parsetree.*;
import computation.derivation.*;

import java.util.ArrayList;
import java.util.List;

public class Parser implements IParser {
  public boolean isInLanguage(ContextFreeGrammar cfg, Word w){
    try{
      int lengthOfWord = w.length(), totalDerivations;
      totalDerivations = (lengthOfWord == 0) ? 1 : (2*lengthOfWord) - 1 ;
      List<Derivation> allDerivations = Derivations(cfg, totalDerivations);
      for (Derivation derivation: allDerivations) {
        if (w.equals(derivation.getLatestWord())) {
          return true;
        }
      } return false;
    } catch (Exception e){
        System.out.println("Error in isInLanguage : " + e);
        return false;
    }
  }

  private List<Rule> ruleFinder(Symbol s, ContextFreeGrammar cfg){
    List<Rule> returnTheseRules = new ArrayList<Rule>();
    try{
      //check if r is Terminal
      if (s.isTerminal()){
        return returnTheseRules; //escape method
      }
      List<Rule> rules = cfg.getRules();
      for (Rule rule: rules) {
        if (rule.getVariable().equals(s)) {
          returnTheseRules.add(rule);
        }
      }
      return returnTheseRules;
    } catch (Exception e){
      System.out.println("Error in ruleFinder method : " + e );
      return returnTheseRules;
    }
  }
  
  private List<Derivation> Derivations(ContextFreeGrammar cfg, int n) {
    List<Derivation> returnDerivations = new ArrayList<Derivation>();
    try{
      Variable stVar = cfg.getStartVariable();
      Derivation preDerivation = new Derivation(new Word(stVar));
      returnDerivations.add(preDerivation);

      List<Derivation> derivations = new ArrayList<Derivation>();
      for (int i=0;i<n;i++){
        for(Derivation derivation: derivations){
          returnDerivations.add(derivation);
        }
        derivations = new ArrayList<Derivation>();

        //clone derivation in case it is required for branching
        //later
        for(Derivation derivation:returnDerivations){
          Derivation derivationCopy = new Derivation(derivation);
          Word w = derivation.getLatestWord();
          int indexOfWord = 0; boolean stop = false;
          for(Symbol s:w){
            if(s.isTerminal()){
              indexOfWord++;
              continue;
            }
          for(Rule rule:ruleFinder(s, cfg)){
            Word wordExpansion = rule.getExpansion();
            Word wordReplaced = w.replace(indexOfWord,wordExpansion);
            if(stop){
              Derivation newDerivation = new Derivation(derivationCopy);
              newDerivation.addStep(wordReplaced,rule,indexOfWord);
              derivations.add(newDerivation);
            } else {
              derivation.addStep(wordReplaced, rule, indexOfWord);
              stop = true;
            }
          }
          indexOfWord++;
          } 
        }
      }
      for(Derivation derivation: derivations){
        returnDerivations.add(derivation);
      }
      return returnDerivations;
    }catch (Exception e){
      System.out.println("Error in Derivations : " + e);
      return returnDerivations;
    }
  }

  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {
    return null;
  }
}
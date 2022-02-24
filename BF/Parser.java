package BF;
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
      //Check if symbol is Terminal
      if (s.isTerminal()){
        return returnTheseRules;
      }
      //Loop through rules to find the rules for our symbol
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

        //For each derivation, save a copy to check with rules later
        for(Derivation derivation:returnDerivations){
          Derivation derivationCopy = new Derivation(derivation);
          Word w = derivation.getLatestWord();
          int indexOfWord = 0; boolean stop = false;
          for(Symbol s:w){
            //If symbol is terminl skip
            if(s.isTerminal()){
              indexOfWord++;
              continue;
            }
          //Get all rules for our symbol
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
    try{
      if (isInLanguage(cfg, w)) {
        int lengthOfWord = w.length();
        //Check that there is indeed a word given
        if(lengthOfWord > 0){
          int totalDerivations = (2 * lengthOfWord) - 1;
          List<Derivation> allPossibleDerivations = Derivations(cfg, totalDerivations);
          //Loop through all derivations
          for (Derivation derivation: allPossibleDerivations) {
            //If we can match this, return the tree
            if (w.equals(derivation.getLatestWord())) { 
              ParseTreeNode newNode = buildParseTreeNode(derivation);
              return newNode;
            }
          }
        }
        if (lengthOfWord == 0) {
          ParseTreeNode parseTreeFinal = ParseTreeNode.emptyParseTree(cfg.getStartVariable());
          return parseTreeFinal;
        }
      }
    
    System.out.println("Word is not in language!");
    Variable nullSymbol = new Variable('Ⅰ');
    return new ParseTreeNode(nullSymbol);
    } catch (Exception e){
      System.out.println("Error in generateParseTree : " + e);
      return null;
    }
  }
  
  private ParseTreeNode buildParseTreeNode(Derivation derivation) {
    Word finalWord = derivation.getLatestWord();
    List<ParseTreeNode> endNodes = new ArrayList<ParseTreeNode>();
    for (Symbol symbol: finalWord) {
      endNodes.add(new ParseTreeNode(symbol));
    }
    for (Step step: derivation) {
      Rule parentRule = step.getRule();
      if (parentRule == null) {
        break;
      }
      Symbol parentSymbol = parentRule.getVariable();
      int stepIndex = step.getIndex();
      Word expansion = step.getRule().getExpansion();
      if (expansion.length() > 1) {
        ParseTreeNode parentNode = new ParseTreeNode(parentSymbol, endNodes.get(stepIndex), endNodes.get(stepIndex + 1));
        endNodes.remove(stepIndex);
        endNodes.remove(stepIndex);
        endNodes.add(stepIndex, parentNode);
      } else {
        ParseTreeNode parentNode = new ParseTreeNode(parentSymbol, endNodes.get(stepIndex));
        endNodes.remove(stepIndex);
        endNodes.add(stepIndex, parentNode);
      }

    }
    return endNodes.get(0);
  }

}
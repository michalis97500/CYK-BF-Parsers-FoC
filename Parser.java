import java.util.ArrayList;
import java.util.List;

import javax.naming.ldap.PagedResultsResponseControl;

import computation.contextfreegrammar.*;
import computation.parser.IParser;
import computation.parsetree.ParseTreeNode;

public class Parser implements IParser{

  Variable nullSymbol = new Variable('Ⅰ');
  ArrayList<ArrayList<ArrayList<Symbol>>> GlobalSymbolTable = new ArrayList<>();
  ArrayList<ArrayList<ArrayList<Rule>>> GlobalRuleTable = new ArrayList<>();
  ArrayList<ArrayList<ArrayList<Object>>> GlobalParseTreeTable = new ArrayList<>();
  public boolean isInLanguage(ContextFreeGrammar cfg, Word w) {
    try{
      //Begin by creating 3 tables of size (word length) * (word length)
      //where each of the cells inside will contain 1)symbols 2)rules 3)parse tree nodes
      Symbol stVar = cfg.getStartVariable();
      List<Rule> rules = cfg.getRules();
      ArrayList<ArrayList<ArrayList<Symbol>>> SymbolTable = new ArrayList<>();
      ArrayList<ArrayList<ArrayList<Rule>>> RuleTable = new ArrayList<>();
      ArrayList<ArrayList<ArrayList<Object>>> ParseTreeTable = new ArrayList<>();
      //Table generation
      int lengthOfWord = w.length();
      for(int i=0; i<lengthOfWord;i++){
        SymbolTable.add(new ArrayList<>(lengthOfWord));
        RuleTable.add(new ArrayList<>(lengthOfWord));
        ParseTreeTable.add(new ArrayList<>(lengthOfWord));
        for(int j=0; j<lengthOfWord;j++){
          SymbolTable.get(i).add(new ArrayList<>());
          RuleTable.get(i).add(new ArrayList<>());
          ParseTreeTable.get(i).add(new ArrayList<>());
        }
      }
      for(int i=0;i<SymbolTable.size();i++){
        for(int j=0;j<SymbolTable.get(i).size();j++){
          SymbolTable.get(i).get(j).add(nullSymbol);
        }
      }
      //Begin parsing of word
      //First determine if word is empty
      if(w.equals(Word.emptyWord)){
        for (Rule rule : rules) {
            if (rule.getExpansion().equals(Word.emptyWord) && rule.getVariable().equals(stVar)){
                return true;
            }
        }
      }
      //Start generating the diagonal (terminals)
      for(int i=0;i<lengthOfWord;i++){
        for (Rule rule : rules){
          //Check if rule leads to empty string
          //With a Chomsky normal form this should never be the case but lets defend against it
          if (rule.getExpansion().toString() == "ε"){
            continue;
          }
          Symbol expansionSymbol = rule.getExpansion().get(0);
          Symbol terminalSymbol = w.get(i);
          //Check if symbols are equal
          if(expansionSymbol.equals(terminalSymbol)){
            //Add to [i,i] of table
            SymbolTable.get(i).get(i).add(rule.getVariable());
            RuleTable.get(i).get(i).add(rule);
            ParseTreeNode child = new ParseTreeNode(rule.getExpansion().get(0));
            ParseTreeTable.get(i).get(i).add(new ParseTreeNode(rule.getVariable(),child));
          }
        }
      }
      //Start looping to generate next rows of table. Require a substring of
      //length lengthOfSubstring >=1 but <lengthOfWord for all lengths of substrings
      for(int lengthOfSubstring=2; lengthOfSubstring<=lengthOfWord;lengthOfSubstring++){
        //For all substring starts
        for(int i=1;i<=(lengthOfWord-lengthOfSubstring+1);i++){
          //Substring end
          int substringEnd = i+lengthOfSubstring-1;
          for(int substring=i;substring<=substringEnd-1;substring++){
            //Loop through all rules
            for(Rule rule : rules){
              //Chomsky normal form so length MUST be 2 since it is non-terminal
              if(rule.getExpansion().length() == 2){
                Symbol firstExpansion = rule.getExpansion().get(0);
                Symbol secondExpansion = rule.getExpansion().get(1);
                //Check if the previous cells contain the first and second variable. i.e. this rule is what generated them
                if(SymbolTable.get(i-1).get(substring-1).contains(firstExpansion) && SymbolTable.get(substring).get(substringEnd-1).contains(secondExpansion)){
                  SymbolTable.get(i-1).get(substringEnd-1).add(rule.getVariable());
                  RuleTable.get(i-1).get(substringEnd-1).add(rule);
                  ParseTreeNode child1 = new ParseTreeNode(rule.getExpansion().get(0));
                  ParseTreeNode child2 = new ParseTreeNode(rule.getExpansion().get(1));
                  ParseTreeTable.get(i-1).get(substringEnd-1).add(new ParseTreeNode(rule.getVariable(),child1,child2));
                }
              }
            }
          }
        }
      }
      //Clone our tables to the global tables
      GlobalSymbolTable = (ArrayList)SymbolTable.clone();
      GlobalRuleTable = (ArrayList)RuleTable.clone();
      GlobalParseTreeTable = (ArrayList)ParseTreeTable.clone();
      return SymbolTable.get(0).get(lengthOfWord-1).contains(cfg.getStartVariable());
    }catch(Exception e){
      System.out.println("Error in CYK : " + e);
      return false;
    }
  }

  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {
    try{
      //Check if word is in language
      if(isInLanguage(cfg,w)){
        int lengthOfWord = w.length();
        Symbol stVar = cfg.getStartVariable();
        ArrayList<ArrayList<ArrayList<Object>>> ParseTreeTable = new ArrayList<>();
        //Table generation
        for(int i=0; i<lengthOfWord;i++){
          ParseTreeTable.add(new ArrayList<>(lengthOfWord));
          for(int j=0; j<lengthOfWord;j++){
            ParseTreeTable.get(i).add(new ArrayList<>());
          }
        }
        //Start building parse trees
        for(int addition=0;addition<lengthOfWord-2;addition++){
          int bounds = lengthOfWord-1;
          for(int i=1;i<lengthOfWord;i++){
            if(i+addition > bounds){
              continue;
            }
            //If the current cell is not empty, get the children and build a parse tree
            if(GlobalSymbolTable.get(i-1).get(i+addition).size() > 1){
              ParseTreeNode thisNode =new ParseTreeNode(nullSymbol);
              //Make sure we are NOT getting a duplicate/start symbol
              for(int index=0;index<GlobalParseTreeTable.get(i-1).get(i+addition).size();index++){
                thisNode = (ParseTreeNode)GlobalParseTreeTable.get(i-1).get(i+addition).get(index);
                if(thisNode.getSymbol() != nullSymbol && thisNode.getSymbol() != stVar){
                  break;
                }
              }
              //Check that we dont have the wrong symbol
              if(thisNode.getSymbol() !=nullSymbol){
                ParseTreeNode terminalNode =  new ParseTreeNode(nullSymbol);
                ParseTreeNode terminalNode2 =  new ParseTreeNode(nullSymbol);
                for(int index=0;index<GlobalParseTreeTable.get(i-1).get(i-1+addition).size();index++){
                  terminalNode = (ParseTreeNode)GlobalParseTreeTable.get(i-1).get(i-1+addition).get(index);
                  if(terminalNode.getSymbol() != nullSymbol && terminalNode.getSymbol() != stVar){
                    break;
                  }
                }
                for(int index=0;index<GlobalParseTreeTable.get(i).get(i+addition).size();index++){
                  terminalNode2 = (ParseTreeNode)GlobalParseTreeTable.get(i).get(i+addition).get(index);
                  if(terminalNode2.getSymbol() != nullSymbol && terminalNode2.getSymbol() != stVar){
                    break;
                  }
                }
                if(terminalNode.getSymbol() !=null && terminalNode2.getSymbol() !=null && terminalNode.getSymbol() !=nullSymbol && terminalNode2.getSymbol() !=nullSymbol){
                  ParseTreeNode newNode = new ParseTreeNode(thisNode.getSymbol(),terminalNode,terminalNode2);
                  GlobalSymbolTable.get(i-1).get(i-1+addition).set(1,nullSymbol);
                  GlobalSymbolTable.get(i).get(i+addition).set(1,nullSymbol);
                  GlobalParseTreeTable.get(i-1).get(i+addition).set(0,newNode);
                }
              }
            //If the current cell is empty
            } else{
              //We have several possibilites that got us here. We could be at the edges meaning we have to
              //move the previous cell accordingly. If we are at a top edge we need to move the cell to our left
              //on us. If we are at a right edge we need to move the cell below us to us. 
              //Since we are moving diagonally, when i=1 we are at top, and when i=lengthOfWord-1 we are at right
              if(i==1){
                GlobalParseTreeTable.get(i-1).get(i+addition).add((ParseTreeNode)GlobalParseTreeTable.get(i-1).get(i-1+addition).get(0));
                GlobalSymbolTable.get(i-1).get(i+addition).add(GlobalSymbolTable.get(i-1).get(i-1+addition).get(1));
                GlobalSymbolTable.get(i-1).get(i-1+addition).set(1,nullSymbol);
              }
              if(i==lengthOfWord-1){
                GlobalParseTreeTable.get(i-1).get(i+addition).add((ParseTreeNode)GlobalParseTreeTable.get(i).get(i+addition).get(0));
                GlobalSymbolTable.get(i-1).get(i+addition).add(GlobalSymbolTable.get(i).get(i+addition).get(1));
                GlobalSymbolTable.get(i).get(i+addition).set(1,nullSymbol);
              }
            }
          }
          //Loop end
        }
        //We are at the final place
        ParseTreeNode terminalNode = (ParseTreeNode)GlobalParseTreeTable.get(0).get(lengthOfWord-2).get(0);
        ParseTreeNode terminalNode2 = (ParseTreeNode)GlobalParseTreeTable.get(1).get(lengthOfWord-1).get(0);
        ParseTreeNode finalParseTree = new ParseTreeNode(stVar,terminalNode,terminalNode2);
        return finalParseTree;
      }
      return null;
    }catch(Exception e){
      System.out.println("Error in CYK tree generation : " + e);
      return null;
    }
  }
}

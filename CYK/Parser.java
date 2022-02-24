package CYK;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import computation.contextfreegrammar.*;
import computation.parser.IParser;
import computation.parsetree.ParseTreeNode;

public class Parser implements IParser{

  Variable nullSymbol = new Variable('Ⅰ');
  ArrayList<ArrayList<ArrayList<Symbol>>> GlobalSymbolTable = new ArrayList<>();
  ArrayList<ArrayList<ArrayList<Object>>> GlobalParseTreeTable = new ArrayList<>();
  public boolean isInLanguage(ContextFreeGrammar cfg, Word w) {
    try{
      //Begin by creating 3 tables of size (word length) * (word length)
      //where each of the cells inside will contain 1)symbols 2)rules 3)parse tree nodes
      Symbol stVar = cfg.getStartVariable();
      List<Rule> rules = cfg.getRules();
      ArrayList<ArrayList<ArrayList<Symbol>>> SymbolTable = new ArrayList<>();
      ArrayList<ArrayList<ArrayList<Object>>> ParseTreeTable = new ArrayList<>();
      //Table generation
      int lengthOfWord = w.length();
      for(int i=0; i<lengthOfWord;i++){
        SymbolTable.add(new ArrayList<>(lengthOfWord));
        ParseTreeTable.add(new ArrayList<>(lengthOfWord));
        for(int j=0; j<lengthOfWord;j++){
          SymbolTable.get(i).add(new ArrayList<>());
          ParseTreeTable.get(i).add(new ArrayList<>());
        }
      }
      for(int i=0;i<ParseTreeTable.size();i++){
        for(int j=0;j<ParseTreeTable.get(i).size();j++){
          ParseTreeNode nullTree = new ParseTreeNode(nullSymbol);
          ParseTreeTable.get(i).get(j).add(nullTree);
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
      setGlobalSymbolTable(SymbolTable);
      setGlobalParseTreeTable(ParseTreeTable);
      return SymbolTable.get(0).get(lengthOfWord-1).contains(cfg.getStartVariable());
    }catch(Exception e){
      System.out.println("Error in CYK : " + e);
      return false;
    }
  }

  private void setGlobalSymbolTable(ArrayList<ArrayList<ArrayList<Symbol>>> var) {
    this.GlobalSymbolTable = var;
  }

  private void setGlobalParseTreeTable(ArrayList<ArrayList<ArrayList<Object>>> var) {
    this.GlobalParseTreeTable = var;
  }

  private ParseTreeNode treeFromSymbol(Symbol symbol, int y, int x){
    ParseTreeNode node = new ParseTreeNode(nullSymbol);
    ArrayList<ParseTreeNode> listOfNodes = new ArrayList<>();
    //Loop to find matching trees
    for(int index=0;index<GlobalParseTreeTable.get(y).get(x).size();index++){
      node = (ParseTreeNode)GlobalParseTreeTable.get(y).get(x).get(index);
      if(node.getSymbol() == symbol){
        listOfNodes.add(node);
      }
    }
    int maxLength = 0;
    //Return the longest tree
    for(ParseTreeNode nodeToTest : listOfNodes){
      int length = nodeToTest.toString().length();
      if(length > maxLength){
        maxLength = length;
      }
    }
    for(ParseTreeNode nodeToTest : listOfNodes){
      int lengthTest = nodeToTest.toString().length();
      if(lengthTest >= maxLength){
        return nodeToTest;      
      }
    }

    return null; 
  }

  private int treeTerminalCounter(ParseTreeNode treeToTest, Set<Terminal> terminalsList){
    String[] treeCharacters = treeToTest.toString().split("");
    int terminalsFound = 0;
    for(String character : treeCharacters){
      for(Terminal terminalCharacter : terminalsList){
        if(character.equals(terminalCharacter.toString())){
          terminalsFound += 1;
        }                      
      }
    }
    return terminalsFound;
  }

  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {
    try{
      //Check if word is in language
      if(isInLanguage(cfg,w)){
        List<Rule> rules = cfg.getRules();
        int lengthOfWord = w.length();
        Symbol stVar = cfg.getStartVariable();
        Set<Terminal> terminalsList = cfg.getTerminals();
        for(int lengthOfSubstring=2; lengthOfSubstring<=lengthOfWord;lengthOfSubstring++){
          //For all substring starts
          for(int i=1;i<=(lengthOfWord-lengthOfSubstring+1);i++){
            //Substring end
            int substringEnd = i+lengthOfSubstring-1;
            for(int substring=i;substring<=substringEnd-1;substring++){
              //Loop through all rules
              for(Rule rule : rules){
                //Chomsky normal form so length MUST be 2 since it is non-terminal
                //The only exception is for terminals
                if(rule.getExpansion().length() == 2){
                  Symbol firstExpansion = rule.getExpansion().get(0);
                  Symbol secondExpansion = rule.getExpansion().get(1);
                  //Check if the previous cells contain the first and second variable. i.e. this rule is that generated them
                  for(int index1 = 0 ; index1 < GlobalParseTreeTable.get(i-1).get(substring-1).size(); index1++){
                    for(int index2 = 0 ; index2 < GlobalParseTreeTable.get(substring).get(substringEnd-1).size(); index2++){
                      if(((ParseTreeNode)GlobalParseTreeTable.get(i-1).get(substring-1).get(index1)).getSymbol() == firstExpansion && ((ParseTreeNode)GlobalParseTreeTable.get(substring).get(substringEnd-1).get(index2)).getSymbol() == secondExpansion){
                        ParseTreeNode child1 = (ParseTreeNode)GlobalParseTreeTable.get(i-1).get(substring-1).get(index1);
                        ParseTreeNode child2 = (ParseTreeNode)GlobalParseTreeTable.get(substring).get(substringEnd-1).get(index2);
                        if(treeTerminalCounter(child1, terminalsList) > 0 && treeTerminalCounter(child2, terminalsList) > 0){
                          GlobalParseTreeTable.get(i-1).get(substringEnd-1).add(new ParseTreeNode(rule.getVariable(),child1,child2));
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        return treeFromSymbol(stVar, 0, lengthOfWord-1);
      }
      System.out.println("Word is not in language!");
      return new ParseTreeNode(nullSymbol);
    }catch(Exception e){
      System.out.println("Error in CYK tree generation : " + e);
      return null;
    }
  }

 
}
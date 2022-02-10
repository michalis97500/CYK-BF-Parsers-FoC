package CYK;
import java.util.ArrayList;
import java.util.List;
import computation.contextfreegrammar.*;
import computation.parser.IParser;
import computation.parsetree.ParseTreeNode;

public class Parser implements IParser{

  ArrayList<ArrayList<ArrayList<Symbol>>> Table = new ArrayList<>();

  @Override
  public boolean isInLanguage(ContextFreeGrammar cfg, Word w) {
    //Begin by creating a table of size (word length) * (word length)
    //where each of the cells inside will contain a list of the symbols
    int lengthOfWord = w.length();
    for(int i=0; i<lengthOfWord;i++){
      Table.add(new ArrayList<>(lengthOfWord));
      for(int j=0; j<lengthOfWord;j++){
        Table.get(i).add(new ArrayList<>());
      }
    }
    Symbol stVar = cfg.getStartVariable();
    List<Rule> rules = cfg.getRules();

    //First determine if word is empty
    if(w.equals(Word.emptyWord)){
      for (Rule rule : rules) {
          if (rule.getExpansion().equals(Word.emptyWord) && rule.getVariable().equals(stVar)){
              return true;
          }
      }
    }

    //Start looping over the table to generate diagonal
    for(int i=0;i<lengthOfWord;i++){
      for (Rule rule : rules){
        Symbol expansionsSymbol = rule.getExpansion().get(0);
        Symbol terminalSymbol = w.get(i);
        //Check if symbols are equal
        if(expansionsSymbol.equals(terminalSymbol)){
          //Add to [i,i] of table
          Table.get(i).get(i).add(rule.getVariable());
        }
      }
    }

    //Start looping to generate next rows of table. Require a substring of
    //length lengthOfSubstring >=1 but <lengthOfWord
    //for all lengths of substrings
    for(int lengthOfSubstring=2; lengthOfSubstring<=lengthOfWord;lengthOfSubstring++){
      //for all substring starts
      for(int i=1;i<=(lengthOfWord-lengthOfSubstring+1);i++){
        //substring end
        int substringEnd = i+lengthOfSubstring-1;
        for(int substring=i;substring<=substringEnd-1;substring++){
          //loop through all rules
          for(Rule rule : rules){
            if(rule.getExpansion().length() == 2){
              Symbol firstVariable = rule.getExpansion().get(0);
              Symbol secondVariable = rule.getExpansion().get(1);
              //Check if 
              if(Table.get(i-1).get(substring-1).contains(firstVariable) && Table.get(substring).get(substringEnd-1).contains(secondVariable)){
                Table.get(i-1).get(substringEnd-1).add(rule.getVariable());
              }
            }
          }
        }

      }
    }
    return Table.get(0).get(lengthOfWord-1).contains(cfg.getStartVariable());
  }

  @Override
  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {
    // TODO Auto-generated method stub
    return null;
  }
  
}

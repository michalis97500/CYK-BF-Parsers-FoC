import java.util.ArrayList;

import computation.contextfreegrammar.*;

public class MyGrammar {
	public static ContextFreeGrammar makeGrammar() {
		// You can write your code here to make the context-free grammar from the assignment
		Variable S = new Variable('S');
		Variable E = new Variable('E');
		Variable Eone = new Variable('R'); //E1 -> R
		Variable Etwo = new Variable('Y'); //E2 -> Y
		Variable A = new Variable('A');
		Variable B = new Variable('B');
		Variable C = new Variable('C');
		Variable D = new Variable('D');
		Variable F = new Variable('F');
		Variable T = new Variable('T');

		Terminal one = new Terminal('1');
		Terminal zero = new Terminal('0');
		Terminal x = new Terminal('x');
		Terminal add = new Terminal('+');
		Terminal mult = new Terminal('*');
		Terminal sub = new Terminal('-');

		
		ArrayList<Rule> rules = new ArrayList<>();
		rules.add(new Rule(S, new Word(E, Eone)));
		rules.add(new Rule(S, new Word(T, Etwo)));
		rules.add(new Rule(S, new Word(D, C)));
		rules.add(new Rule(S, new Word(one)));
		rules.add(new Rule(S, new Word(zero)));
		rules.add(new Rule(S, new Word(x)));

		rules.add(new Rule(E, new Word(E, Eone)));
		rules.add(new Rule(E, new Word(T, Etwo)));
		rules.add(new Rule(E, new Word(D, C)));
		rules.add(new Rule(E, new Word(one)));
		rules.add(new Rule(E, new Word(zero)));
		rules.add(new Rule(E, new Word(x)));

		rules.add(new Rule(Eone, new Word(A,T)));
		rules.add(new Rule(Etwo, new Word(B,F)));

		rules.add(new Rule(T, new Word(T, Etwo)));
		rules.add(new Rule(T, new Word(D, C)));
		rules.add(new Rule(T, new Word(one)));
		rules.add(new Rule(T, new Word(zero)));
		rules.add(new Rule(T, new Word(x)));

		rules.add(new Rule(F, new Word(D, C)));
		rules.add(new Rule(F, new Word(one)));
		rules.add(new Rule(F, new Word(zero)));
		rules.add(new Rule(F, new Word(x)));

		rules.add(new Rule(A, new Word(add)));
		rules.add(new Rule(B, new Word(mult)));
		rules.add(new Rule(C, new Word(one)));
		rules.add(new Rule(C, new Word(zero)));
		rules.add(new Rule(C, new Word(x)));
		rules.add(new Rule(D, new Word(sub)));

		ContextFreeGrammar cfg = new ContextFreeGrammar(rules);
		return cfg;
	}
}

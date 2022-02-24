For the BF Algorithm:

This algorithm is straight forward brute force approach and a description
of how it works is given by Paola Bruscoli in the engage forums.
https://engage.bath.ac.uk/learn/mod/forum/discuss.php?d=50550

For the CYK Algorithm:

The path through the array is quite complex to code but I found it easier to visualise. Imagine an array of n by n, where upper-left cell is [0,0] and bottom-right cell is [n,n].
You can populate the array by first producing the diagonals 0,0 -> n,n and then move to form rules (towards the upper right corner, [n,0]).
I imagine this is what you did for the symbols.

Imagine now while populating the Symbol Array you create another object array of the same lengths, only this time you populate JUST the diagonal (0,0 -> n,n) by ParseTreeNodes at each position (from the Symbol pointing to the terminal).

Now, you will need some way of working out which cells can be combined to form a parent cell, for example, the terminals at [0,0] and [1,1] are produced by the parent cell [1,0]. If such a rule exists then you can just get these cells (as they are already parsetreenodes) and build a new one using the symbol of the rule and place the new tree @[1,0]. The tricky part is to figure out how to loop through the array. Ideally, this should be using the exact same way you populated the IsInLanguage method. A good way to do that is illustrated in our handbook (Sipser p291)


Both files include comments to aid comprehension of my code

In MonteCarloOct6th20000Sims.txt,
I ran around 250 testcases with 20000 simulations and found the expected num points for each action
ASSUMING everyone continues to play like the current bad versions of the basic AI I hard-coded.

Every test was run twice to see if the answer changes. With 20K sims, it mostly averages out except when
1) there's many choices
2) the choices are about the same suit and same rank

To see both runs for every test case, search for:
END OF SIMULATION  PLAY: TEST:

example results:
"
RUN SIMULATION
Comparing utility of different cards to play:
Average util (point diff) of playing the 2S: 34.80659097705273
Average util (point diff) of playing the 4S: 35.753570895623064
Average util (point diff) of playing the 8S: 37.96606892107142
Average util (point diff) of playing the JS: 31.66726720107251
Average util (point diff) of playing the KS: 27.088056597369786
Average util (point diff) of playing the AS: 27.088056597369786
Average util (point diff) of playing the JH: 38.10966490990548
Sum of impact of simulation: 14829.898148151195 out of a possible 20000 (74.15%)
END OF SIMULATION  PLAY: TEST: Michael plays the JH
****************************
RUN SIMULATION
Comparing utility of different cards to play:
Average util (point diff) of playing the 2S: 34.350561116271315
Average util (point diff) of playing the 4S: 35.3680300044791
Average util (point diff) of playing the 8S: 37.58340071474987
Average util (point diff) of playing the JS: 31.37439212272032
Average util (point diff) of playing the KS: 26.76357442921852
Average util (point diff) of playing the AS: 26.76357442921852
Average util (point diff) of playing the JH: 37.85636934826736
Sum of impact of simulation: 14823.7083333364 out of a possible 20000 (74.12%)
END OF SIMULATION  PLAY: TEST: Michael plays the JH
"

These 2 monty carlo sim runs got the same result but the expected point diff 
is only .30, so it was close and with less simulations it could have gone
differently.

Explanation of "Sum of impact of simulation":
I make unrealistic distributions of cards impact the avg less, so 
because some sims counted for less, I ended up dividing the total by 14823.7083333364 instead of the num simulations (20000)

What good is this?
I think these test cases are interesting because it might point to a 
different way of playing... and weaknesses in the hard-coded ai.

It also suggests that I have work to do to improve the AI this 
monty carlo simulator is relying on.

Good night!

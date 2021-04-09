* C-Minus Compilation to TM Code
* File: C-programs/codegen.tm
* code for prelude
  0:    LD 6, 0(0) 	load up with maxaddr
  1:   LDA 5, 0(6) 	copy gp to fp
  2:    ST 0, 0(0) 	clear content at loc 0
* jumping around i/o functions
* code for input routine
  4:    ST 0, -1(5) 	store return
  5:    IN 0, 0, 0 	input
  6:    LD 7, -1(5) 	return to caller
* code for output routine
  7:    ST 0, -1(5) 	store return
  8:    LD 0, -2(5) 	load output value
  9:   OUT 0, 0, 0 	output
 10:    LD 7, -1(5) 	return to caller
  3:   LDA 7, 7(7) 	jump around i/o code
* code for body of program
* processing function: main
* jump around function body here
 12:    ST 0, -1(5) 	store return
 13:    LD 7, -1(5) 	return to caller
 11:   LDA 7, 2(7) 	jump around fn body
* <- fundecl
* code for finale
 14:    ST 5, 0(5) 	push ofp
 15:   LDA 5, 0(5) 	push frame
 16:   LDA 0, 1(7) 	load ac with ret ptr
 17:   LDA 7, -6(7) 	jump to main loc
 18:    LD 5, 0(5) 	pop frame
 19:  HALT 0, 0, 0 	

Feature: Execution of the intra-statement, mutation based approach to program repair, in situations that do not demand program mutation.
			Since the situations do not demand mutation (i.e., repair), the repair process should return without starting the repair
			process, and communicating the user about the situation.

        Scenario: Application of repair process on a program that does not compile.
                Given a program "Program" to be repaired 
                But "Program" does not compile 
                When the repair process is attempted on "P"
                Then the non-compilation of "P" is detected
                And the repair process is inhibited
                And the user is informed of the situation
                
        Scenario: Application of repair process on a non-existent program.
                Given a program "P" to be repaired 
                But "P" cannot be found 
                When the repair process is attempted on "P"
                Then the user is informed that "P" cannot be found
                And the repair process is inhibited

        Scenario: Application of repair process on a simple correct program.
                Given a program "P" that satisfies its specification
                When the repair process is attempted on "P"
                Then the verification stage detects no specification violation for "P"
                And the repair process is inhibited
                And the user is informed of the situation
                
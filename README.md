Stryker
=======

Stryker is a tool for automatically fixing bugs on JML-annotated Java programs. Stryker is based on the use of intra-statement mutations, of the kind performed by Mutation Testing tools, to attempt to repair a failing program. Essentially, Stryker "reverts" the direction of mutation testing: instead of "injecting" code modifications in code assumed to be correct, to see if a test suite can spot them, Stryker injects code modifications in buggy code to attempt to repair these bugs. 


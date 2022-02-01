This is a prototype for running tests from documentation.
The test in [EvalTest.kt](src/test/kotlin/EvalTest.kt) runs the documentation tests in [Sum.kt](src/main/kotlin/Sum.kt) (the last test fails on purpose).

Currently, each kdoc line that starts with ``>>>`` marks an assertion that will be executed as a separate test.
Only ```assertEquals``` method is currently supported for assertions.


Todo: 
- Produce more relevant information on test failure (file and line number, maybe the failed expression itself).
- Support functions not in global scope (currently it cannot be used to call functions from packages).
- Make stack trace nicer.
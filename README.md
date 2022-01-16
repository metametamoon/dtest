This is a prototype for running tests from documentation.
The test in [EvalTest.kt](src/test/kotlin/EvalTest.kt) runs the documentation tests in [Sum.kt](src/main/kotlin/Sum.kt) (the last test fails on purpose).

Current test format (must be somewhere in the javadoc):

.*>>>actualExpression == expectedResult


Todo: 
- Produce more relevant information on test failure (file and line number, maybe the failed expression itself).
- Split the tests from the documentation into JUnit-recognized test cases.
- Fix the workaround that requires replacing "\r\n" with "\n" in files/.
- Support functions not in global scope (currently it cannot be used to call functions from packages).
- If possible, not to parse the equality string twice (currently it is parsed once as a comment and once again int the script interpreter)
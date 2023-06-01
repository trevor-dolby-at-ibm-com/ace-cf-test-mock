# ace-cf-test-mock
Examples of using callable flows as test mocks for ACE testing

## Overview

The [CountMQMessages](CountMQMessages) application contains a simple example of a flow that is 
difficult to unit test using static mock nodes (such as using propagatesMessage() on a NodeStub 
object), but can be tested with the more dynamic approach of using callable flows to implement a 
mock node. The flow itself looks as follows:

![CountMQMessages/flow-picture.png](CountMQMessages/flow-picture.png)

and is designed to count the number of messages on an MQ queue when invoked via HTTP. Note that as 
this is an example of testing rather than application design, the MQGet node does not browse the 
messages and therefore this flow clears the queue: using browse would avoid this, but would also 
require the flow to reset the browse cursor for the first MQGet node invocation for each HTTP request, 
thereby complicating the flow.

The flow can be deployed and invoked via curl, producing results such as
```
$ curl http://localhost:7800/CountMQMessages
{"count":4}
```

## Unit testing

- test project references both main app and mock provider app
- start message flow threads must be set to true
- Environment.?


![UnitTest_Mocks/mqget-mock-flow-picture.png](UnitTest_Mocks/mqget-mock-flow-picture.png)




![CountMQMessages_UnitTest/cf-mock-test-picture.png](CountMQMessages_UnitTest/cf-mock-test-picture.png)



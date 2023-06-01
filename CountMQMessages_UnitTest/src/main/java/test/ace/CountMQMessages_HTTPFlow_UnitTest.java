package test.ace;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.ibm.integration.test.v1.NodeSpy;
import com.ibm.integration.test.v1.NodeStub;
import com.ibm.integration.test.v1.SpyObjectReference;
import com.ibm.integration.test.v1.TestMessageAssembly;
import com.ibm.integration.test.v1.TestSetup;
import com.ibm.integration.test.v1.exception.TestException;

import static com.ibm.integration.test.v1.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;

class CountMQMessages_HTTPFlow_UnitTest {

	@AfterEach
	public void cleanupTest() throws TestException {
		// Ensure any mocks created by a test are cleared after the test runs 
		TestSetup.restoreAllMocks();
	}

	@Test
	public void wholeFlowTestWithMockedMQ() throws TestException
	{
		// Define the SpyObjectReference objects
		SpyObjectReference mqgetRef = new SpyObjectReference().application("CountMQMessages")
				.messageFlow("HTTPFlow").node("MQ Get");
		SpyObjectReference httpInputObjRef = new SpyObjectReference().application("CountMQMessages")
				.messageFlow("HTTPFlow").node("HTTP Input");
		SpyObjectReference httpReplyObjRef = new SpyObjectReference().application("CountMQMessages")
				.messageFlow("HTTPFlow").node("HTTP Reply");
		SpyObjectReference incrementCountObjRef = new SpyObjectReference().application("CountMQMessages")
				.messageFlow("HTTPFlow").node("IncrementCount");

		
		// Initialise NodeSpy objects
		NodeSpy httpInputSpy = new NodeSpy(httpInputObjRef); // Input
		NodeSpy httpReplySpy = new NodeSpy(httpReplyObjRef); // Reply
		NodeSpy incrementCountSpy = new NodeSpy(incrementCountObjRef); // Validation - should be called only once
		
		// Initialize the service stub to avoid needing an API key for unit testing
		NodeStub mqgetStub = new NodeStub(mqgetRef);
		
		// Program the stub to call a flow so we can change the behavior of the 
		// mock during the test run. The callable flow can override the output
		// terminal name, and we use noMessage as the default to avoid infinite
		// loops during development.
		mqgetStub.onCall().invokesCallableFlow("in",  "noMessage", "UnitTest_Mocks", "MQGetMock");
		
		// Set the various message objects to configure the mock within the callable flow
		setUpMock();
		
		// Declare a new TestMessageAssembly object for the message being sent into the node
		// We use a blank body in this case, but could load a message assembly.
		TestMessageAssembly inputMessageAssembly = new TestMessageAssembly();

		// Configure the "in" terminal on the HTTP Reply node not to propagate.
		// If we don't do this, then the reply node will throw exceptions when it  
		// realises we haven't actually used the HTTP transport.
		httpReplySpy.setStopAtInputTerminal("in");

		// Now call propagate on the "out" terminal of the HTTP Input node.
		// This takes the place of an actual HTTP message: we simple hand the node
		// the message assembly and tell it to propagate that as if it came from an
		// actual client. This line is where the flow is actually run.
		httpInputSpy.propagate(inputMessageAssembly, "out");


		// Assert we only had one valid message
		assertThat(incrementCountSpy, nodeCallCountIs(1));

		/* Compare Output Message 1 at output terminal out */
        TestMessageAssembly actualMessageAssembly = httpReplySpy.receivedMessageAssembly("in", 1);
        // Validate the results
        assertThat(actualMessageAssembly.messagePath("JSON.Data.count").getLongValue(), equalTo(1L));	
    }

	
	public void setUpMock() throws TestException
	{
		TestMessageAssembly firstMessageAssembly = new TestMessageAssembly();
		firstMessageAssembly.buildFromRecordedMessageAssembly(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("/000007EC-644FF855-00000001-2.mxml"));
		SpyObjectReference firstMessageRef = new SpyObjectReference().application("UnitTest_Mocks")
				.messageFlow("MQGet").node("FirstMessage");
		// Initialize the service stub to avoid needing an API key for unit testing
		NodeStub firstStub = new NodeStub(firstMessageRef);
		// Program the stub to return this dummy result instead of calling the service
		firstStub.onCall().propagatesMessage("in", "out", firstMessageAssembly);

		TestMessageAssembly defaultMessageAssembly = new TestMessageAssembly();
		defaultMessageAssembly.buildFromRecordedMessageAssembly(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("/000007EC-644FF855-00000001-4.mxml"));
		SpyObjectReference defaultMessageRef = new SpyObjectReference().application("UnitTest_Mocks")
				.messageFlow("MQGet").node("DefaultMessage");
		// Initialize the service stub to avoid needing an API key for unit testing
		NodeStub defaultStub = new NodeStub(defaultMessageRef);
		// Program the stub to return this dummy result instead of calling the service
		defaultStub.onCall().propagatesMessage("in", "out", defaultMessageAssembly);
	}

}

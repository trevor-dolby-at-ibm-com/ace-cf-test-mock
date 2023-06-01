#!/bin/bash
#
# This script automates the building and testing of the application.
#
# Copyright (c) 2023 Open Technologies for Integration
# Licensed under the MIT license (see LICENSE for details)
#

# Exit on any failure
set -e

# Create the work directory
rm -rf /tmp/ace-cf-test-mock-work-dir junit-reports
mqsicreateworkdir /tmp/ace-cf-test-mock-work-dir

# Build everything; we can do this in this case because we want to include the unit
# tests, but production builds should specify the projects.
ibmint deploy --input-path . --output-work-directory /tmp/ace-cf-test-mock-work-dir

# ibmint optimize server new for v12.0.4 - speed up test runs
ibmint optimize server --work-directory /tmp/ace-cf-test-mock-work-dir --enable JVM --disable NodeJS

# Run the server to run the unit tests
IntegrationServer -w /tmp/ace-cf-test-mock-work-dir --test-project CountMQMessages_UnitTest --mq-queue-manager-name dummyQM --test-junit-options "--reports-dir=junit-reports"

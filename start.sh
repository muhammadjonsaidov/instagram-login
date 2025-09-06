#!/bin/bash

echo "Starting Instagram Business Discovery Application..."
echo

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 17 or higher"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven 3.6 or higher"
    exit 1
fi

echo "Checking environment variables..."
if [ -z "$FACEBOOK_CLIENT_ID" ]; then
    echo "Warning: FACEBOOK_CLIENT_ID environment variable is not set"
    echo "Please set your Facebook App ID in .env file"
fi

if [ -z "$FACEBOOK_CLIENT_SECRET" ]; then
    echo "Warning: FACEBOOK_CLIENT_SECRET environment variable is not set"
    echo "Please set your Facebook App Secret in .env file"
fi

echo
echo "Building and starting the application..."
echo "This may take a few minutes on first run..."
echo

# Build and run the application
mvn spring-boot:run
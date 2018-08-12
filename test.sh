#!/bin/sh

curl -i -X POST --data-binary @target/classes/Example.class http://localhost:${1:-8080}/Example

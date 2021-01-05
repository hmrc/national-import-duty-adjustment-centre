#!/usr/bin/env bash

sbt clean coverage test scalafmt::test test:scalafmt::test coverageReport

// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

import org.scalacheck._
import org.scalacheck.Test._

import org.junit.runner._
import org.junit.runner.Description._
import org.junit.runner.notification._

class ScalaCheckRunner(cls: Class[_ <: Properties]) extends Runner {
  def instance = Class.forName(cls.getName + "$").getField("MODULE$").get(null).asInstanceOf[Properties]

  override def getDescription: Description = {
    val desc = createSuiteDescription(instance.getClass.getName)
    for ((name, p) <- instance.properties) {
      desc.addChild(createTestDescription(p.getClass, name))
    }
    desc
  }

  override def run(notifier: RunNotifier) {
    val params = Params(testCallback = ConsoleReporter())

    for ((name, p) <- instance.properties) {
      val desc = createTestDescription(p.getClass, name)
      notifier.fireTestStarted(desc)
      val result = Test.check(params, p)
      if (!result.passed) {
        val desc = createTestDescription(p.getClass, result.status.toString)
        val e = result.status match {
          case s:GenException  => s.e
          case s:PropException => s.e
          case _               => new Exception
        }
        notifier.fireTestFailure(new Failure(desc, e))
      }
      notifier.fireTestFinished(desc)
    }
  }
}

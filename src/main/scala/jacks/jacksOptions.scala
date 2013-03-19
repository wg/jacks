// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

sealed abstract class JacksOption
object JacksOption {
  case class CheckCaseClassNulls(enabled:Boolean) extends JacksOption
}


private[jacks] class JacksOptions(opts:Seq[JacksOption]=Seq.empty) {
  def checkCaseClassNulls=opts contains JacksOption.CheckCaseClassNulls(true)
}
private[jacks] object JacksOptions {
  def apply(opts:JacksOption*) =
    new JacksOptions(opts.groupBy(_.getClass()).toSeq.map(_._2.last))
  def defaults=new JacksOptions()
}

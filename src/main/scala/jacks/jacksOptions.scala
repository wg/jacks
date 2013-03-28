// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jacks

sealed abstract class JacksOption
object JacksOption {
  case class CaseClassCheckNulls(enabled:Boolean) extends JacksOption
}


private[jacks] class JacksOptions(opts:Seq[JacksOption]=Seq.empty) {
  def caseClassCheckNulls=opts contains JacksOption.CaseClassCheckNulls(true)
}
private[jacks] object JacksOptions {
  def apply(opts:JacksOption*) =
    new JacksOptions(opts.groupBy(_.getClass()).toSeq.map(_._2.last))
  def defaults=new JacksOptions()
}

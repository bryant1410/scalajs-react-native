package chandu0101.scalajs.rn.apis

import scala.scalajs.js

trait NetInfo  extends js.Object{

  val isConnected : NetInfoIsConnected = js.native
  val reachabilityIOS : NetInfoReachabilityIOS = js.native


}

trait NetInfoIsConnected extends js.Object{

  def addEventListener(eventName : String,handler : (Boolean) => _) : Unit = js.native
  def removeEventListener(eventName : String,handler : (Boolean) => _) : Unit = js.native
  def fetch() : js.Dynamic = js.native
}

trait NetInfoReachabilityIOS extends js.Object{

  def addEventListener(eventName : String,handler : (String) => _) : Unit = js.native
  def removeEventListener(eventName : String,handler : (String) => _) : Unit = js.native
  def fetch() : js.Dynamic = js.native
}
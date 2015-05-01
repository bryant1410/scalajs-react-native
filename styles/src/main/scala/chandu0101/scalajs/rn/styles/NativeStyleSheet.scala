package main.scala.chandu0101.scalajs.rn.styles

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => json}
import scala.scalajs.js.JSConverters.JSRichGenMap


trait NativeStyleSheet extends NativeAttrs{
  /** if duplicate attrs found then last one wins */
  @inline def styleE(maps: js.Dictionary[Any]*)(v: NativeStylePair[_]*) = {
    maps.fold(js.Dictionary.empty[Any])((d1, d2) => d1.++(d2).toJSDictionary)
      .++(style(v: _*))
      .toJSDictionary
  }

  @inline def style(v: NativeStylePair[_]*) : js.Dictionary[Any] = {
    val p = js.Dictionary.empty[Any]
    v.foreach(t => p.update(t.key, t.value))
    p
  }
}


case class NativeStyle[T](name : String) {
  def := (v :T) = new NativeStylePair[T](name,v)
}

case class NativeStylePair[T](key : String ,value : T) {

}
